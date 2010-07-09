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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.rating.Rating;
import org.alfresco.service.cmr.rating.RatingScheme;
import org.alfresco.service.cmr.rating.RatingService;
import org.alfresco.service.cmr.rating.RatingServiceException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
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
    private static final String USER_ERNIE = "Ernie";
    private static final String USER_ERIC = "Eric";
    private PersonService personService;
    private RatingService ratingService;
    private Repository repositoryHelper;
    private NodeRef companyHome;
    
    // These NodeRefs are used by the test methods.
    private NodeRef testFolder;
    private NodeRef testDoc_Admin;
    private NodeRef testDoc_Eric;
    private NodeRef testDoc_Ernie;
    
    // The out of the box scheme names.
    private static final String LIKES_SCHEME_NAME = "likesRatingScheme";
    private static final String FIVE_STAR_SCHEME_NAME = "fiveStarRatingScheme";
    
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        this.personService = (PersonService)this.applicationContext.getBean("PersonService");
        this.ratingService = (RatingService) this.applicationContext.getBean("ratingService");
        this.repositoryHelper = (Repository) this.applicationContext.getBean("repositoryHelper");

        // Set the current security context as admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        companyHome = this.repositoryHelper.getCompanyHome();
        
        //TODO These could be created @BeforeClass
        testFolder = createNode(companyHome, "testFolder", ContentModel.TYPE_FOLDER);
        testDoc_Admin = createNode(testFolder, "testDocInFolder", ContentModel.TYPE_CONTENT);
        
        createUser(USER_ERIC);
        createUser(USER_ERNIE);
        
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ERIC);
        testDoc_Eric = createNode(testFolder, "ericsDoc", ContentModel.TYPE_CONTENT);
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ERNIE);
        testDoc_Ernie = createNode(testFolder, "erniesDoc", ContentModel.TYPE_CONTENT);
        
        // And back to admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
    }
    
    @Override
    protected void onTearDownInTransaction() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        deleteUser(USER_ERIC);
        deleteUser(USER_ERNIE);
    }
    
    private NodeRef createNode(NodeRef parentNode, String name, QName type)
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        String fullName = name + System.currentTimeMillis();
        props.put(ContentModel.PROP_NAME, fullName);
        QName docContentQName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, fullName);
        NodeRef node = nodeService.createNode(parentNode,
                    ContentModel.ASSOC_CONTAINS,
                    docContentQName,
                    type,
                    props).getChildRef();
        return node;
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
        assertEquals("'likes' rating scheme had wrong min.", 1, likesRS.getMinRating());
        assertEquals("'likes' rating scheme had wrong max.", 1, likesRS.getMaxRating());
        
        RatingScheme fiveStarRS = schemes.get(FIVE_STAR_SCHEME_NAME);
        assertNotNull("'5*' rating scheme was missing.", fiveStarRS);
        assertEquals("'5*' rating scheme had wrong name.", FIVE_STAR_SCHEME_NAME, fiveStarRS.getName());
        assertEquals("'5*' rating scheme had wrong min.", 1, fiveStarRS.getMinRating());
        assertEquals("'5*' rating scheme had wrong max.", 5, fiveStarRS.getMaxRating());
    }
    
    /**
     * This test method ensures that an attempt to apply an out-of-range rating value
     * throws the expected exception.
     */
    public void testApplyIllegalRatings() throws Exception
    {
        // See rating-services-context.xml for definitions of these rating schemes.
        int[] illegalRatings = new int[]{0, 2};
        for (int illegalRating : illegalRatings)
        {
            applyIllegalRating(testDoc_Admin, illegalRating, LIKES_SCHEME_NAME);
        }
    }

    private void applyIllegalRating(NodeRef nodeRef, int illegalRating, String schemeName)
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

    public void testApplyUpdateDeleteRatings_SingleUserMultipleSchemes() throws Exception
    {
        // We'll do all this as user 'eric'.
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ERIC);
        
        //Before we start, let's ensure the read behaviour on a pristine node is correct.
        Rating nullRating = ratingService.getRatingByCurrentUser(testDoc_Admin, LIKES_SCHEME_NAME);
        assertNull("Expected a null rating,", nullRating);
        assertNull("Expected a null remove result.", ratingService.removeRatingByCurrentUser(testDoc_Admin, LIKES_SCHEME_NAME));
        
        final int likesScore = 1;
        final int fiveStarScore = 5;
        
        ratingService.applyRating(testDoc_Admin, likesScore, LIKES_SCHEME_NAME);
        ratingService.applyRating(testDoc_Admin, fiveStarScore, FIVE_STAR_SCHEME_NAME);
        
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
        Rating likeRating = ratingService.getRatingByCurrentUser(testDoc_Admin, LIKES_SCHEME_NAME);
        
        Rating fiveStarRating = ratingService.getRatingByCurrentUser(testDoc_Admin, FIVE_STAR_SCHEME_NAME);

        assertNotNull("'like' rating was null.", likeRating);
        assertEquals("Wrong score for rating", likesScore, likeRating.getScore());
        assertEquals("Wrong user for rating", AuthenticationUtil.getFullyAuthenticatedUser(), likeRating.getAppliedBy());
        final Date likeRatingAppliedAt = likeRating.getAppliedAt();
        assertDateIsCloseToNow(likeRatingAppliedAt);

        assertNotNull("'5*' rating was null.", fiveStarRating);
        assertEquals("Wrong score for rating", fiveStarScore, fiveStarRating.getScore());
        assertEquals("Wrong user for rating", AuthenticationUtil.getFullyAuthenticatedUser(), fiveStarRating.getAppliedBy());
        final Date fiveStarRatingAppliedAt = fiveStarRating.getAppliedAt();
        assertDateIsCloseToNow(fiveStarRatingAppliedAt);
        
        // Now we'll update a rating
        final int updatedFiveStarScore = 3;
        ratingService.applyRating(testDoc_Admin, updatedFiveStarScore, FIVE_STAR_SCHEME_NAME);
        
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

        // 'like' rating data should be unchanged.
        assertNotNull("'like' rating was null.", likeRating);
        assertEquals("Wrong score for rating", likesScore, likeRating.getScore());
        assertEquals("Wrong user for rating", AuthenticationUtil.getFullyAuthenticatedUser(), likeRating.getAppliedBy());
        assertEquals("Wrong date for rating", likeRatingAppliedAt, likeRating.getAppliedAt());

        // But these 'five star' data should be changed - new score, new date
        assertNotNull("'5*' rating was null.", updatedFiveStarRating);
        assertEquals("Wrong score for rating", updatedFiveStarScore, updatedFiveStarRating.getScore());
        assertEquals("Wrong user for rating", AuthenticationUtil.getFullyAuthenticatedUser(), updatedFiveStarRating.getAppliedBy());
        assertTrue("five star rating date was unchanged.", fiveStarRatingAppliedAt.equals(updatedFiveStarRating.getAppliedAt()) == false);
        assertDateIsCloseToNow(updatedFiveStarRating.getAppliedAt());
        
        // Now we'll delete the 'likes' rating.
        Rating deletedLikesRating = ratingService.removeRatingByCurrentUser(testDoc_Admin, LIKES_SCHEME_NAME);
        // 'like' rating data should be unchanged.
        assertNotNull("'like' rating was null.", deletedLikesRating);
        assertEquals("Wrong score for rating", likesScore, deletedLikesRating.getScore());
        assertEquals("Wrong user for rating", AuthenticationUtil.getFullyAuthenticatedUser(), deletedLikesRating.getAppliedBy());
        assertEquals("Wrong date for rating", likeRatingAppliedAt, deletedLikesRating.getAppliedAt());

        // And delete the 'five star' rating.
        Rating deletedStarRating = ratingService.removeRatingByCurrentUser(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
        // 'five star' rating data should be unchanged.
        assertNotNull("'5*' rating was null.", deletedStarRating);
        assertEquals("Wrong score for rating", updatedFiveStarScore, deletedStarRating.getScore());
        assertEquals("Wrong user for rating", AuthenticationUtil.getFullyAuthenticatedUser(), deletedStarRating.getAppliedBy());
        assertEquals("Wrong date for rating", updatedFiveStarRating.getAppliedAt(), deletedStarRating.getAppliedAt());
    }
    
    /**
     * This test method asserts that the specified date is effectively equal to now.
     * We can't assert that the two dates are exactly equal but we do assert that
     * they are equal to within a specified tolerance.
     * @param d the date to check
     */
    private void assertDateIsCloseToNow(Date d)
    {
        assertNotNull("Date was unexpected null", d);
        Date now = new Date();
        assertTrue(now.after(d));
        final long millisTolerance = 5000l; // 5 seconds
        assertTrue("Date was not within " + millisTolerance + "ms of 'now'.", now.getTime() - d.getTime() < millisTolerance);
    }
    
    public void testApplyRating_MultipleUsers() throws Exception
    {
        // 2 different users rating the same piece of content in the same rating scheme
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ERNIE);
        ratingService.applyRating(testDoc_Admin, 4, FIVE_STAR_SCHEME_NAME);
        
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ERIC);
        ratingService.applyRating(testDoc_Admin, 2, FIVE_STAR_SCHEME_NAME);
        
        float meanRating = ratingService.getAverageRating(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
        assertEquals("Document had wrong mean rating.", 3f, meanRating);

        int totalRating = ratingService.getTotalRating(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
        assertEquals("Document had wrong total rating.", 6, totalRating);

        int ratingsCount = ratingService.getRatingsCount(testDoc_Admin, FIVE_STAR_SCHEME_NAME);
        assertEquals("Document had wrong ratings count.", 2, ratingsCount);
    }
    
    public void testUsersCantRateTheirOwnContent() throws Exception
    {
        try
        {
            AuthenticationUtil.setFullyAuthenticatedUser(USER_ERNIE);
            ratingService.applyRating(testDoc_Ernie, 4, FIVE_STAR_SCHEME_NAME);
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

}
