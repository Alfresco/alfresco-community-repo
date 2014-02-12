/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.rating;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ClasspathScriptLocation;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.rating.Rating;
import org.alfresco.service.cmr.rating.RatingScheme;
import org.alfresco.service.cmr.rating.RatingService;
import org.alfresco.service.cmr.rating.RatingServiceException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.test.junitrules.AlfrescoPerson;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.RunAsFullyAuthenticatedRule;
import org.alfresco.util.test.junitrules.RunAsFullyAuthenticatedRule.RunAsUser;
import org.alfresco.util.test.junitrules.TemporaryNodes;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.RuleChain;

/**
 * @author Neil McErlean
 * @since 3.4
 */
@Category(OwnJVMTestsCategory.class)
public class RatingServiceIntegrationTest
{
    // Rule to initialise the default Alfresco spring configuration
    public static ApplicationContextInit APP_CONTEXT_INIT
                                         = ApplicationContextInit.createStandardContextWithOverrides("classpath:ratings/test-"
                                                 + RatingServiceIntegrationTest.class.getSimpleName() + "-context.xml");
    
    public static final String USER_ONE_NAME = "UserOne";
    public static final String USER_TWO_NAME = "UserTwo";
    // Rules to create 2 test users.
    public static AlfrescoPerson TEST_USER1 = new AlfrescoPerson(APP_CONTEXT_INIT, USER_ONE_NAME);
    public static AlfrescoPerson TEST_USER2 = new AlfrescoPerson(APP_CONTEXT_INIT, USER_TWO_NAME);
    
    // A rule to manage test nodes reused across all the test methods
    public static TemporaryNodes STATIC_TEST_NODES = new TemporaryNodes(APP_CONTEXT_INIT);
    
    // Tie them together in a static Rule Chain
    @ClassRule public static RuleChain STATIC_RULE_CHAIN = RuleChain.outerRule(APP_CONTEXT_INIT)
                                                            .around(TEST_USER1)
                                                            .around(TEST_USER2)
                                                            .around(STATIC_TEST_NODES);
    
    // A JUnit Rule to manage test nodes use in each test method
    public TemporaryNodes testNodes = new TemporaryNodes(APP_CONTEXT_INIT);
    
    // A rule to allow individual test methods all to be run as "UserOne".
    // Some test methods need to switch user during execution which they are free to do.
    @Rule public RunAsFullyAuthenticatedRule runAsRule = new RunAsFullyAuthenticatedRule(TEST_USER1);
    
    // Tie them together in a non-static rule chain.
    @Rule public RuleChain ruleChain = RuleChain.outerRule(runAsRule)
                                                .around(testNodes);

    // Various services
    private static CopyService                 COPY_SERVICE;
    private static NodeService                 NODE_SERVICE;
    private static RatingService               RATING_SERVICE;
    private static RetryingTransactionHelper   TRANSACTION_HELPER;
    private static ScriptService               SCRIPT_SERVICE;
    private static RatingNamingConventionsUtil RATING_NAMING_CONVENTIONS;
    
    private static NodeRef COMPANY_HOME;
    
    // These NodeRefs are used by the test methods.
    private static NodeRef COPY_DEST_FOLDER;
    private static NodeRef TEST_FOLDER;
    private NodeRef testDoc_Admin;
    private NodeRef testDoc_Copy;
    private NodeRef testDoc_UserOne;
    private NodeRef testDoc_UserTwo;
    
    // The out of the box scheme names.
    private static final String LIKES_SCHEME_NAME = "likesRatingScheme";
    private static final String FIVE_STAR_SCHEME_NAME = "fiveStarRatingScheme";
    
    @BeforeClass public static void initStaticData() throws Exception
    {
        COPY_SERVICE              = (CopyService)                 APP_CONTEXT_INIT.getApplicationContext().getBean("copyService");
        NODE_SERVICE              = (NodeService)                 APP_CONTEXT_INIT.getApplicationContext().getBean("nodeService");
        RATING_NAMING_CONVENTIONS = (RatingNamingConventionsUtil) APP_CONTEXT_INIT.getApplicationContext().getBean("rollupNamingConventions");
        RATING_SERVICE            = (RatingService)               APP_CONTEXT_INIT.getApplicationContext().getBean("ratingService");
        SCRIPT_SERVICE            = (ScriptService)               APP_CONTEXT_INIT.getApplicationContext().getBean("scriptService");
        TRANSACTION_HELPER        = (RetryingTransactionHelper)   APP_CONTEXT_INIT.getApplicationContext().getBean("retryingTransactionHelper");
        
        Repository repositoryHelper = (Repository) APP_CONTEXT_INIT.getApplicationContext().getBean("repositoryHelper");
        COMPANY_HOME = repositoryHelper.getCompanyHome();
        
        // Create some static test content
        TEST_FOLDER = STATIC_TEST_NODES.createNode(COMPANY_HOME, "testFolder", ContentModel.TYPE_FOLDER, AuthenticationUtil.getAdminUserName());
        COPY_DEST_FOLDER = STATIC_TEST_NODES.createNode(COMPANY_HOME, "testCopyDestinationFolder", ContentModel.TYPE_FOLDER, AuthenticationUtil.getAdminUserName());

    }
    
    @Before public void createTestContent()
    {
        // Create some test content
        testDoc_Admin   = testNodes.createNode(TEST_FOLDER,   "testDocInFolder", ContentModel.TYPE_CONTENT, AuthenticationUtil.getAdminUserName());
        testDoc_UserOne = testNodes.createNode(TEST_FOLDER,   "userOnesDoc",     ContentModel.TYPE_CONTENT, TEST_USER1.getUsername());
        testDoc_UserTwo = testNodes.createNode(TEST_FOLDER,   "userTwosDoc",     ContentModel.TYPE_CONTENT, TEST_USER2.getUsername());
    }
    
    /**
     * This method tests that the expected 'out of the box' rating schemes are available
     * and correctly initialised.
     */
    @Test public void outOfTheBoxRatingSchemes() throws Exception
    {
        Map<String, RatingScheme> schemes = RATING_SERVICE.getRatingSchemes();
        
        assertNotNull("rating scheme collection was null.", schemes);
        assertTrue("rating scheme collection was empty.", schemes.isEmpty() == false);
        
        RatingScheme likesRS = schemes.get(LIKES_SCHEME_NAME);
        assertNotNull("'likes' rating scheme was missing.", likesRS);
        assertEquals("'likes' rating scheme had wrong name.", LIKES_SCHEME_NAME, likesRS.getName());
        assertEquals("'likes' rating scheme had wrong min.", 1, (int)likesRS.getMinRating());
        assertEquals("'likes' rating scheme had wrong max.", 1, (int)likesRS.getMaxRating());
        
        RatingScheme fiveStarRS = schemes.get(FIVE_STAR_SCHEME_NAME);
        assertNotNull("'5*' rating scheme was missing.", fiveStarRS);
        assertEquals("'5*' rating scheme had wrong name.", FIVE_STAR_SCHEME_NAME, fiveStarRS.getName());
        assertEquals("'5*' rating scheme had wrong min.", 1, (int)fiveStarRS.getMinRating());
        assertEquals("'5*' rating scheme had wrong max.", 5, (int)fiveStarRS.getMaxRating());
    }
    
    /**
     * This test method ensures that an attempt to apply an out-of-range rating value
     * throws the expected exception.
     */
    @Test public void applyIllegalRatings() throws Exception
    {
        // See rating-services-context.xml for definitions of these rating schemes.
        float[] illegalRatings = new float[]{0.0f, 2.0f};
        for (float illegalRating : illegalRatings)
        {
            applyIllegalRating(testDoc_Admin, illegalRating, LIKES_SCHEME_NAME);
        }
    }

    private void applyIllegalRating(NodeRef nodeRef, float illegalRating, String schemeName)
    {
        try
        {
            RATING_SERVICE.applyRating(nodeRef, illegalRating, schemeName);
        } 
        catch (RatingServiceException expectedException)
        {
            return;
        }
        fail("Illegal rating " + illegalRating + " should have caused exception.");
    }

    @Test @RunAsUser(userName=USER_TWO_NAME) public void applyUpdateDeleteRatings() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                //Before we start, let's ensure the read behaviour on a pristine node is correct.
                Rating nullRating = RATING_SERVICE.getRatingByCurrentUser(testDoc_Admin, LIKES_SCHEME_NAME);
                assertNull("Expected a null rating,", nullRating);
                assertNull("Expected a null remove result.", RATING_SERVICE.removeRatingByCurrentUser(testDoc_Admin, LIKES_SCHEME_NAME));
                
                final int fiveStarScore = 5;
                
                RATING_SERVICE.applyRating(testDoc_Admin, fiveStarScore, FIVE_STAR_SCHEME_NAME);
                assertModifierIs(testDoc_Admin, AuthenticationUtil.getAdminUserName());
                
                // Some basic node structure tests.
                assertTrue(ContentModel.ASPECT_RATEABLE + " aspect missing.",
                        NODE_SERVICE.hasAspect(testDoc_Admin, ContentModel.ASPECT_RATEABLE));
                
                List<ChildAssociationRef> allChildren = NODE_SERVICE.getChildAssocs(testDoc_Admin,
                        ContentModel.ASSOC_RATINGS, RegexQNamePattern.MATCH_ALL);
                
                // It's one cm:rating node per user
                assertEquals("Wrong number of ratings nodes.", 1, allChildren.size());
                // child-assoc of type cm:ratings
                assertEquals("Wrong type qname on ratings assoc", ContentModel.ASSOC_RATINGS, allChildren.get(0).getTypeQName());
                // child-assoc of name cm:<username__ratingScheme>
                QName expectedAssocName = RATING_NAMING_CONVENTIONS.getRatingAssocNameFor(AuthenticationUtil.getFullyAuthenticatedUser(), FIVE_STAR_SCHEME_NAME);
                assertEquals("Wrong qname on ratings assoc", expectedAssocName, allChildren.get(0).getQName());
                // node structure seems ok.
                
                
                // Now to check the persisted ratings data are ok.
                Rating fiveStarRating = RATING_SERVICE.getRatingByCurrentUser(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
                
                assertNotNull("'5*' rating was null.", fiveStarRating);
                assertEquals("Wrong score for rating", fiveStarScore, (int)fiveStarRating.getScore());
                assertEquals("Wrong user for rating", AuthenticationUtil.getFullyAuthenticatedUser(), fiveStarRating.getAppliedBy());
                final Date fiveStarRatingAppliedAt = fiveStarRating.getAppliedAt();
                
                // Now we'll update a rating
                final int updatedFiveStarScore = 3;
                RATING_SERVICE.applyRating(testDoc_Admin, updatedFiveStarScore, FIVE_STAR_SCHEME_NAME);
                assertModifierIs(testDoc_Admin, AuthenticationUtil.getAdminUserName());
                
                // Some basic node structure tests.
                allChildren = NODE_SERVICE.getChildAssocs(testDoc_Admin,
                        ContentModel.ASSOC_RATINGS, RegexQNamePattern.MATCH_ALL);
                
                // Still one cm:rating node
                assertEquals("Wrong number of ratings nodes.", 1, allChildren.size());
                // Same assoc names
                assertEquals("Wrong type qname on ratings assoc", ContentModel.ASSOC_RATINGS, allChildren.get(0).getTypeQName());
                assertEquals("Wrong qname on ratings assoc", expectedAssocName, allChildren.get(0).getQName());
                // node structure seems ok.
                
                
                // Now to check the updated ratings data are ok.
                Rating updatedFiveStarRating = RATING_SERVICE.getRatingByCurrentUser(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
                
                // 'five star' data should be changed - new score, new date
                assertNotNull("'5*' rating was null.", updatedFiveStarRating);
                assertEquals("Wrong score for rating", updatedFiveStarScore, (int)updatedFiveStarRating.getScore());
                assertEquals("Wrong user for rating", AuthenticationUtil.getFullyAuthenticatedUser(), updatedFiveStarRating.getAppliedBy());
                assertTrue("five star rating date was unchanged.", fiveStarRatingAppliedAt.equals(updatedFiveStarRating.getAppliedAt()) == false);
                
                // And delete the 'five star' rating.
                Rating deletedStarRating = RATING_SERVICE.removeRatingByCurrentUser(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
                assertModifierIs(testDoc_Admin, AuthenticationUtil.getAdminUserName());
                // 'five star' rating data should be unchanged.
                assertNotNull("'5*' rating was null.", deletedStarRating);
                assertEquals("Wrong score for rating", updatedFiveStarScore, (int)deletedStarRating.getScore());
                assertEquals("Wrong user for rating", AuthenticationUtil.getFullyAuthenticatedUser(), deletedStarRating.getAppliedBy());
                assertEquals("Wrong date for rating", updatedFiveStarRating.getAppliedAt(), deletedStarRating.getAppliedAt());
                
                // And the deleted ratings should be gone.
                assertNull("5* rating not null.", RATING_SERVICE.getRatingByCurrentUser(testDoc_Admin, FIVE_STAR_SCHEME_NAME));
                
                return null;
            }
        });
    }
    
    @Test public void oneUserRatesAndRerates() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                RATING_SERVICE.applyRating(testDoc_Admin, 1.0f, FIVE_STAR_SCHEME_NAME);
                
                // A new score in the same rating scheme by the same user should replace the previous score.
                RATING_SERVICE.applyRating(testDoc_Admin, 2.0f, FIVE_STAR_SCHEME_NAME);
                
                float meanRating = RATING_SERVICE.getAverageRating(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
                assertEquals("Document had wrong mean rating.", 2, (int)meanRating);
                
                float totalRating = RATING_SERVICE.getTotalRating(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
                assertEquals("Document had wrong total rating.", 2, (int)totalRating);
                
                int ratingsCount = RATING_SERVICE.getRatingsCount(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
                assertEquals("Document had wrong ratings count.", 1, ratingsCount);
                
                // There should only be one rating child node under the rated node.
                assertEquals("Wrong number of child nodes", 1 , NODE_SERVICE.getChildAssocs(testDoc_Admin).size());
                
                return null;
            }
        });
    }
    
    /**
     * This test method ensures that if a single user attempts to rate a piece of content in two
     * different rating schemes, then an exception should not be thrown.
     */
    @Test public void oneUserRatesInTwoSchemes() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                
                RATING_SERVICE.applyRating(testDoc_UserTwo, 2.0f, FIVE_STAR_SCHEME_NAME);
                
                // A new score in a different rating scheme by the same user should not fail.
                RATING_SERVICE.applyRating(testDoc_UserTwo, 1.0f, LIKES_SCHEME_NAME);
                
                // There should be two rating child nodes under the rated node.
                assertEquals("Wrong number of child nodes", 2 , NODE_SERVICE.getChildAssocs(testDoc_UserTwo).size());
                
                List<Rating> ratings = RATING_SERVICE.getRatingsByCurrentUser(testDoc_UserTwo);
                assertEquals(2, ratings.size());
                assertEquals(FIVE_STAR_SCHEME_NAME, ratings.get(0).getScheme().getName());
                assertEquals(LIKES_SCHEME_NAME, ratings.get(1).getScheme().getName());
                
                return null;
            }
        });
    }
    
    /**
     * This test method applies ratings to a single node as a number of different users.
     * It checks that the ratings are applied correctly and that the cm:modifier is not
     * updated by these changes.
     */
    @Test public void applyRating_MultipleUsers() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                assertModifierIs(testDoc_Admin, AuthenticationUtil.getAdminUserName());
                
                // 2 different users rating the same piece of content in the same rating scheme
                AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER1.getUsername());
                RATING_SERVICE.applyRating(testDoc_Admin, 4.0f, FIVE_STAR_SCHEME_NAME);
                assertModifierIs(testDoc_Admin, AuthenticationUtil.getAdminUserName());
                
                AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER2.getUsername());
                RATING_SERVICE.applyRating(testDoc_Admin, 2.0f, FIVE_STAR_SCHEME_NAME);
                assertModifierIs(testDoc_Admin, AuthenticationUtil.getAdminUserName());
                
                float meanRating = RATING_SERVICE.getAverageRating(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
                assertEquals("Document had wrong mean rating.", 3, (int)meanRating);
                
                float totalRating = RATING_SERVICE.getTotalRating(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
                assertEquals("Document had wrong total rating.", 6, (int)totalRating);
                
                int ratingsCount = RATING_SERVICE.getRatingsCount(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
                assertEquals("Document had wrong ratings count.", 2, ratingsCount);
                
                assertModifierIs(testDoc_Admin, AuthenticationUtil.getAdminUserName());
                
                // One user removes their rating.
                RATING_SERVICE.removeRatingByCurrentUser(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
                
                meanRating = RATING_SERVICE.getAverageRating(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
                assertEquals("Document had wrong mean rating.", 4, (int)meanRating);
                
                totalRating = RATING_SERVICE.getTotalRating(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
                assertEquals("Document had wrong total rating.", 4, (int)totalRating);
                
                ratingsCount = RATING_SERVICE.getRatingsCount(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
                assertEquals("Document had wrong ratings count.", 1, ratingsCount);
                
                assertModifierIs(testDoc_Admin, AuthenticationUtil.getAdminUserName());
                
                return null;
            }
        });
    }
    
    /** ALF-18312 */
    @Test @RunAsUser(userName="admin") public void ratingsShouldNotBeCopied() throws Exception
    {
        // To realise this, we must prevent the copying of the cm:rateable aspect & other rollups.
        final NodeRef testFolder = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            @Override public NodeRef execute() throws Throwable
            {
                // Create a new folder to copy the rated node to.
                NodeRef testFolder = testNodes.createNode(COMPANY_HOME,
                                                          "copyTarget",
                                                          ContentModel.TYPE_FOLDER,
                                                          AuthenticationUtil.getAdminUserName());
                // Ensure the test document is not rated initially.
                assertNull(RATING_SERVICE.getRatingByCurrentUser(testDoc_Admin, LIKES_SCHEME_NAME));
                
                // Apply a rating to the test document.
                RATING_SERVICE.applyRating(testDoc_Admin, 1, LIKES_SCHEME_NAME);
                return testFolder;
            }
        });
        
        // Now copy the test document.
        final NodeRef copiedDoc = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            @Override public NodeRef execute() throws Throwable
            {
                return COPY_SERVICE.copy(testDoc_Admin, testFolder, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS);
            }
        });
        
        // Validate the copy behaviour for ratings.
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            @Override public NodeRef execute() throws Throwable
            {
                List<Rating> ratings = RATING_SERVICE.getRatingsByCurrentUser(copiedDoc);
                assertTrue("Expected there to be no ratings on the copied test doc.", ratings.isEmpty());
                assertFalse("Unexpected cm:rateable aspect.", NODE_SERVICE.hasAspect(copiedDoc, ContentModel.ASPECT_RATEABLE));
                assertFalse("Unexpected cm:likesRatingSchemeRollups aspect.", NODE_SERVICE.hasAspect(copiedDoc, ContentModel.ASPECT_LIKES_RATING_SCHEME_ROLLUPS));
                assertFalse("Unexpected cm:fiveStarRatingSchemeRollups aspect.", NODE_SERVICE.hasAspect(copiedDoc, ContentModel.ASPECT_FIVESTAR_RATING_SCHEME_ROLLUPS));
                
                return null;
            }
        });
    }
    
    /** ALF-17861 */
    @Test public void itIsPossibleToDefineCustomRatingSchemes() throws Exception
    {
        // This is obviously only visible in test code.
        final String spinalTapScheme = "spinalTapRatingScheme";
        final QName rollupAspect = QName.createQName("http://www.alfresco.org/model/testratings/1.0", "spinalTapRatingSchemeRollups");
        final QName countProp = QName.createQName("http://www.alfresco.org/model/testratings/1.0", "spinalTapRatingSchemeCount");
        final QName totalProp = QName.createQName("http://www.alfresco.org/model/testratings/1.0", "spinalTapRatingSchemeTotal");
        
        RatingScheme spinalTap = RATING_SERVICE.getRatingScheme(spinalTapScheme);
        assertEquals(11.0f, spinalTap.getMaxRating(), 0.0001f);
        
        
        // So the rating scheme exists. Now to use it.
        // Apply a rating.
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            @Override public NodeRef execute() throws Throwable
            {
                RATING_SERVICE.applyRating(testDoc_UserOne, 11.0F, spinalTapScheme);
                return null;
            }
        });
        
        // Now we'll go back to the node and check that the rollups were calculated.
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            @Override public NodeRef execute() throws Throwable
            {
                assertTrue(NODE_SERVICE.hasAspect(testDoc_UserOne, rollupAspect));
                final int count = (Integer)NODE_SERVICE.getProperty(testDoc_UserOne, countProp);
                final float total = (Float)NODE_SERVICE.getProperty(testDoc_UserOne, totalProp);
                
                assertEquals(1, count);
                assertEquals("But this one goes to 11. See? 11.", 11, total, 0.1);
                return null;
            }
        });
    }
    
    /**
     * This method asserts that the modifier of the specified node is equal to the
     * provided modifier name.
     * @param nodeRef the nodeRef to check.
     * @param expectedModifier the expected modifier e.g. "admin".
     */
    private void assertModifierIs(NodeRef nodeRef, final String expectedModifier)
    {
        String actualModifier = (String)NODE_SERVICE.getProperty(nodeRef, ContentModel.PROP_MODIFIER);
        assertEquals("Incorrect cm:modifier", expectedModifier, actualModifier);
    }
    
    @Test @RunAsUser(userName=USER_ONE_NAME) public void usersCantRateTheirOwnContent() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // In the likes rating scheme, users can rate their own content.
                RATING_SERVICE.applyRating(testDoc_UserOne, 1, LIKES_SCHEME_NAME);
                
                // But fiveStar rating scheme disallows rating your own content.
                boolean expectedExceptionThrown = false;
                try
                {
                    RATING_SERVICE.applyRating(testDoc_UserOne, 4, FIVE_STAR_SCHEME_NAME);
                } catch (RatingServiceException expected)
                {
                    expectedExceptionThrown = true;
                }
                assertTrue(expectedExceptionThrown);
                
                return null;
            }
        });
    }
    
    @Test @RunAsUser(userName=USER_TWO_NAME) public void javascriptAPI() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                Map<String, Object> model = new HashMap<String, Object>();
                model.put("testNode", testDoc_UserOne);
                
                ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/rating/script/test_ratingService.js");
                SCRIPT_SERVICE.executeScript(location, model);
                
                return null;
            }
        });
    }
    
    @Test public void copyNodeNotRatings() 
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // Apply ratings to node
                AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER1.getUsername());

                RATING_SERVICE.applyRating(testDoc_UserTwo, 2.0f, FIVE_STAR_SCHEME_NAME);
                
                // A new score in a different rating scheme by the same user should not fail.
                RATING_SERVICE.applyRating(testDoc_UserTwo, 1.0f, LIKES_SCHEME_NAME);
                
                // There should be two rating child nodes under the rated node.
                assertEquals("Wrong number of child nodes", 2 , NODE_SERVICE.getChildAssocs(testDoc_UserTwo).size());
                
                List<Rating> ratings = RATING_SERVICE.getRatingsByCurrentUser(testDoc_UserTwo);
                assertEquals(2, ratings.size());
                assertEquals(FIVE_STAR_SCHEME_NAME, ratings.get(0).getScheme().getName());
                assertEquals(LIKES_SCHEME_NAME, ratings.get(1).getScheme().getName());

                // Copy the node
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
                
                final QName childName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "userTwosDoc");;
                final NodeRef testDocCopy = COPY_SERVICE.copy(testDoc_UserTwo, COPY_DEST_FOLDER, ContentModel.ASSOC_CONTAINS, childName, true);
                
                testNodes.addNodeRef(testDocCopy);

                // Check that the user ratings aren't copied
                AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER1.getUsername());

                List<Rating> copiedRatings = RATING_SERVICE.getRatingsByCurrentUser(testDocCopy);
                assertEquals(0, copiedRatings.size());

                // Check that the roll ups aren't copied
                final int likesRatingCount = RATING_SERVICE.getRatingsCount(testDocCopy, LIKES_SCHEME_NAME);
                final int fiveStarRatingCount = RATING_SERVICE.getRatingsCount(testDocCopy, FIVE_STAR_SCHEME_NAME);

                assertEquals(0, fiveStarRatingCount);
                assertEquals(0, likesRatingCount);

                return null;
            }
        });
    }
}
