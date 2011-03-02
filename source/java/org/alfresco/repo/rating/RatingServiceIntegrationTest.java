/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ClasspathScriptLocation;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.rating.Rating;
import org.alfresco.service.cmr.rating.RatingScheme;
import org.alfresco.service.cmr.rating.RatingService;
import org.alfresco.service.cmr.rating.RatingServiceException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.PropertyMap;

/**
 * @author Neil McErlean
 * @since 3.4
 */
public class RatingServiceIntegrationTest extends BaseAlfrescoSpringTest
{
    private static final String USER_ONE = "UserOne";
    private static final String USER_TWO = "UserTwo";
//    private CopyService copyService;
    private PersonService personService;
    private RatingService ratingService;
    private Repository repositoryHelper;
    private ScriptService scriptService;
//    private RetryingTransactionHelper transactionHelper;
    private NodeRef companyHome;
    
    // These NodeRefs are used by the test methods.
    private NodeRef testFolder;
//    private NodeRef testFolderCopyDest;
    private NodeRef testDoc_Admin;
    private NodeRef testDoc_UserOne;
    private NodeRef testDoc_UserTwo;
    
    // The out of the box scheme names.
    private static final String LIKES_SCHEME_NAME = "likesRatingScheme";
    private static final String FIVE_STAR_SCHEME_NAME = "fiveStarRatingScheme";
    
    @SuppressWarnings("deprecation")
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
//        this.copyService = (CopyService)this.applicationContext.getBean("CopyService");
        this.personService = (PersonService)this.applicationContext.getBean("PersonService");
        this.ratingService = (RatingService) this.applicationContext.getBean("ratingService");
        this.repositoryHelper = (Repository) this.applicationContext.getBean("repositoryHelper");
//        this.transactionHelper = (RetryingTransactionHelper) this.applicationContext.getBean("retryingTransactionHelper");
        this.scriptService = (ScriptService) this.applicationContext.getBean("scriptService");

        // Set the current security context as admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        companyHome = this.repositoryHelper.getCompanyHome();
        
        testFolder = createNode(companyHome, "testFolder", ContentModel.TYPE_FOLDER);
//        testFolderCopyDest = createNode(companyHome, "testFolderCopyDest", ContentModel.TYPE_FOLDER);
        testDoc_Admin = createNode(testFolder, "testDocInFolder", ContentModel.TYPE_CONTENT);
        
        createUser(USER_ONE);
        createUser(USER_TWO);
        
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        testDoc_UserOne = createNode(testFolder, "userOnesDoc", ContentModel.TYPE_CONTENT);
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        testDoc_UserTwo = createNode(testFolder, "userTwosDoc", ContentModel.TYPE_CONTENT);
        
        // And back to admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
    }
    
    @Override
    protected void onTearDownInTransaction() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        deleteUser(USER_TWO);
        deleteUser(USER_ONE);
    }
    
    /**
     * This method tests that the expected 'out of the box' rating schemes are available
     * and correctly initialised.
     */
    public void testOutOfTheBoxRatingSchemes() throws Exception
    {
        Map<String, RatingScheme> schemes = this.ratingService.getRatingSchemes();
        
        assertNotNull("rating scheme collection was null.", schemes);
        assertTrue("rating scheme collection was empty.", schemes.isEmpty() == false);
        
        RatingScheme likesRS = schemes.get(LIKES_SCHEME_NAME);
        assertNotNull("'likes' rating scheme was missing.", likesRS);
        assertEquals("'likes' rating scheme had wrong name.", LIKES_SCHEME_NAME, likesRS.getName());
        assertEquals("'likes' rating scheme had wrong min.", 1.0f, likesRS.getMinRating());
        assertEquals("'likes' rating scheme had wrong max.", 1.0f, likesRS.getMaxRating());
        
        RatingScheme fiveStarRS = schemes.get(FIVE_STAR_SCHEME_NAME);
        assertNotNull("'5*' rating scheme was missing.", fiveStarRS);
        assertEquals("'5*' rating scheme had wrong name.", FIVE_STAR_SCHEME_NAME, fiveStarRS.getName());
        assertEquals("'5*' rating scheme had wrong min.", 1.0f, fiveStarRS.getMinRating());
        assertEquals("'5*' rating scheme had wrong max.", 5.0f, fiveStarRS.getMaxRating());
    }
    
    /**
     * This test method ensures that an attempt to apply an out-of-range rating value
     * throws the expected exception.
     */
    public void testApplyIllegalRatings() throws Exception
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
            ratingService.applyRating(nodeRef, illegalRating, schemeName);
        } catch (RatingServiceException expectedException)
        {
            return;
        }
        fail("Illegal rating " + illegalRating + " should have caused exception.");
    }

    public void testApplyUpdateDeleteRatings() throws Exception
    {
        // We'll do all this as user 'UserOne'.
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        
        //Before we start, let's ensure the read behaviour on a pristine node is correct.
        Rating nullRating = ratingService.getRatingByCurrentUser(testDoc_Admin, LIKES_SCHEME_NAME);
        assertNull("Expected a null rating,", nullRating);
        assertNull("Expected a null remove result.", ratingService.removeRatingByCurrentUser(testDoc_Admin, LIKES_SCHEME_NAME));
        
        final float fiveStarScore = 5;
        
        ratingService.applyRating(testDoc_Admin, fiveStarScore, FIVE_STAR_SCHEME_NAME);
        assertModifierIs(testDoc_Admin, AuthenticationUtil.getAdminUserName());
        
        // Some basic node structure tests.
        assertTrue(ContentModel.ASPECT_RATEABLE + " aspect missing.",
                nodeService.hasAspect(testDoc_Admin, ContentModel.ASPECT_RATEABLE));

        List<ChildAssociationRef> allChildren = nodeService.getChildAssocs(testDoc_Admin,
                ContentModel.ASSOC_RATINGS, RegexQNamePattern.MATCH_ALL);

        // It's one cm:rating node per user
        assertEquals("Wrong number of ratings nodes.", 1, allChildren.size());
        // child-assoc of type cm:ratings
        assertEquals("Wrong type qname on ratings assoc", ContentModel.ASSOC_RATINGS, allChildren.get(0).getTypeQName());
        // child-assoc of name cm:<username>
        QName expectedAssocName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, AuthenticationUtil.getFullyAuthenticatedUser());
        assertEquals("Wrong qname on ratings assoc", expectedAssocName, allChildren.get(0).getQName());
        // node structure seems ok.
        

        // Now to check the persisted ratings data are ok.
        Rating fiveStarRating = ratingService.getRatingByCurrentUser(testDoc_Admin, FIVE_STAR_SCHEME_NAME);

        assertNotNull("'5*' rating was null.", fiveStarRating);
        assertEquals("Wrong score for rating", fiveStarScore, fiveStarRating.getScore());
        assertEquals("Wrong user for rating", AuthenticationUtil.getFullyAuthenticatedUser(), fiveStarRating.getAppliedBy());
        final Date fiveStarRatingAppliedAt = fiveStarRating.getAppliedAt();
        assertDateIsCloseToNow(fiveStarRatingAppliedAt);
        
        // Now we'll update a rating
        final float updatedFiveStarScore = 3;
        ratingService.applyRating(testDoc_Admin, updatedFiveStarScore, FIVE_STAR_SCHEME_NAME);
        assertModifierIs(testDoc_Admin, AuthenticationUtil.getAdminUserName());
        
        // Some basic node structure tests.
        allChildren = nodeService.getChildAssocs(testDoc_Admin,
                ContentModel.ASSOC_RATINGS, RegexQNamePattern.MATCH_ALL);

        // Still one cm:rating node
        assertEquals("Wrong number of ratings nodes.", 1, allChildren.size());
        // Same assoc names
        assertEquals("Wrong type qname on ratings assoc", ContentModel.ASSOC_RATINGS, allChildren.get(0).getTypeQName());
        assertEquals("Wrong qname on ratings assoc", expectedAssocName, allChildren.get(0).getQName());
        // node structure seems ok.
        

        // Now to check the updated ratings data are ok.
        Rating updatedFiveStarRating = ratingService.getRatingByCurrentUser(testDoc_Admin, FIVE_STAR_SCHEME_NAME);

        // 'five star' data should be changed - new score, new date
        assertNotNull("'5*' rating was null.", updatedFiveStarRating);
        assertEquals("Wrong score for rating", updatedFiveStarScore, updatedFiveStarRating.getScore());
        assertEquals("Wrong user for rating", AuthenticationUtil.getFullyAuthenticatedUser(), updatedFiveStarRating.getAppliedBy());
        assertTrue("five star rating date was unchanged.", fiveStarRatingAppliedAt.equals(updatedFiveStarRating.getAppliedAt()) == false);
        assertDateIsCloseToNow(updatedFiveStarRating.getAppliedAt());
        
        // And delete the 'five star' rating.
        Rating deletedStarRating = ratingService.removeRatingByCurrentUser(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
        assertModifierIs(testDoc_Admin, AuthenticationUtil.getAdminUserName());
        // 'five star' rating data should be unchanged.
        assertNotNull("'5*' rating was null.", deletedStarRating);
        assertEquals("Wrong score for rating", updatedFiveStarScore, deletedStarRating.getScore());
        assertEquals("Wrong user for rating", AuthenticationUtil.getFullyAuthenticatedUser(), deletedStarRating.getAppliedBy());
        assertEquals("Wrong date for rating", updatedFiveStarRating.getAppliedAt(), deletedStarRating.getAppliedAt());
        
        // And the deleted ratings should be gone.
        assertNull("5* rating not null.", ratingService.getRatingByCurrentUser(testDoc_Admin, FIVE_STAR_SCHEME_NAME));
    }
    
    /**
     * This test method asserts that the specified date is effectively equal to now.
     * We can't assert that the two dates are exactly equal but we do assert that
     * they are equal to within a specified tolerance.
     * @param d the date to check
     */
    private void assertDateIsCloseToNow(Date d)
    {
        //TODO Turning this assertion off temporarily
        
//        assertNotNull("Date was unexpectedly null", d);
//        Date now = new Date();
//        assertTrue("Date was not before 'now'", now.after(d));
//        final long millisTolerance = 5000l; // 5 seconds
//        assertTrue("Date was not within " + millisTolerance + "ms of 'now'.", now.getTime() - d.getTime() < millisTolerance);
    }
    
    public void testOneUserRatesAndRerates() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        ratingService.applyRating(testDoc_Admin, 1.0f, FIVE_STAR_SCHEME_NAME);

        // A new score in the same rating scheme by the same user should replace the previous score.
        ratingService.applyRating(testDoc_Admin, 2.0f, FIVE_STAR_SCHEME_NAME);
        
        float meanRating = ratingService.getAverageRating(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
        assertEquals("Document had wrong mean rating.", 2f, meanRating);

        float totalRating = ratingService.getTotalRating(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
        assertEquals("Document had wrong total rating.", 2.0f, totalRating);

        int ratingsCount = ratingService.getRatingsCount(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
        assertEquals("Document had wrong ratings count.", 1, ratingsCount);
        
        // There should only be one rating child node under the rated node.
        assertEquals("Wrong number of child nodes", 1 , nodeService.getChildAssocs(testDoc_Admin).size());
    }
    
    /**
     * This test method ensures that if a single user attempts to rate a piece of content in two
     * different rating schemes, then an exception should be thrown.
     * @throws Exception
     */
    public void testOneUserRatesInTwoSchemes() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        ratingService.applyRating(testDoc_Admin, 1.0f, FIVE_STAR_SCHEME_NAME);

        // A new score in a different rating scheme by the same user should fail.
        boolean correctExceptionThrown = false;
        try
        {
            ratingService.applyRating(testDoc_Admin, 2.0f, LIKES_SCHEME_NAME);
        } catch (RatingServiceException expected)
        {
            correctExceptionThrown = true;
        }
        if (correctExceptionThrown == false)
        {
            fail("Expected exception not thrown.");
        }
        
        float meanRating = ratingService.getAverageRating(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
        assertEquals("Document had wrong mean rating.", 1f, meanRating);

        float totalRating = ratingService.getTotalRating(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
        assertEquals("Document had wrong total rating.", 1f, totalRating);

        int ratingsCount = ratingService.getRatingsCount(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
        assertEquals("Document had wrong ratings count.", 1, ratingsCount);
        
        // There should only be one rating child node under the rated node.
        assertEquals("Wrong number of child nodes", 1 , nodeService.getChildAssocs(testDoc_Admin).size());
    }
    
    /**
     * This test method applies ratings to a single node as a number of different users.
     * It checks that the ratings are applied correctly and that the cm:modifier is not
     * updated by these changes.
     */
    public void testApplyRating_MultipleUsers() throws Exception
    {
        assertModifierIs(testDoc_Admin, AuthenticationUtil.getAdminUserName());
        
        // 2 different users rating the same piece of content in the same rating scheme
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        ratingService.applyRating(testDoc_Admin, 4.0f, FIVE_STAR_SCHEME_NAME);
        assertModifierIs(testDoc_Admin, AuthenticationUtil.getAdminUserName());
        
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        ratingService.applyRating(testDoc_Admin, 2.0f, FIVE_STAR_SCHEME_NAME);
        assertModifierIs(testDoc_Admin, AuthenticationUtil.getAdminUserName());
        
        float meanRating = ratingService.getAverageRating(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
        assertEquals("Document had wrong mean rating.", 3f, meanRating);

        float totalRating = ratingService.getTotalRating(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
        assertEquals("Document had wrong total rating.", 6.0f, totalRating);

        int ratingsCount = ratingService.getRatingsCount(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
        assertEquals("Document had wrong ratings count.", 2, ratingsCount);
    }

    /**
     * This method asserts that the modifier of the specified node is equal to the
     * provided modifier name.
     * @param nodeRef the nodeRef to check.
     * @param expectedModifier the expected modifier e.g. "admin".
     */
    private void assertModifierIs(NodeRef nodeRef, final String expectedModifier)
    {
        String actualModifier = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIER);
        assertEquals("Incorrect cm:modifier", expectedModifier, actualModifier);
    }
    
    public void testUsersCantRateTheirOwnContent() throws Exception
    {
        try
        {
            AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
            ratingService.applyRating(testDoc_UserTwo, 4, FIVE_STAR_SCHEME_NAME);
        } catch (RatingServiceException expected)
        {
            return;
        }
        fail("Expected exception not thrown");
    }
    
    private void createUser(String userName)
    {
        if (! authenticationService.authenticationExists(userName))
        {
            authenticationService.createAuthentication(userName, "PWD".toCharArray());
        }
        
        if (! personService.personExists(userName))
        {
            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");
            
            personService.createPerson(ppOne);
        }
    }

    private void deleteUser(String userName)
    {
        if (personService.personExists(userName))
        {
            personService.deletePerson(userName);
        }
    }

    public void testJavascriptAPI() throws Exception
    {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("testNode", this.testDoc_UserOne);
        
        ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/rating/script/test_ratingService.js");
        this.scriptService.executeScript(location, model);
    }
}
