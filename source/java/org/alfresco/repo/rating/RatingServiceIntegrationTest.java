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
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.BaseAlfrescoSpringTest;

/**
 * @author Neil McErlean
 * @since 3.4
 */
public class RatingServiceIntegrationTest extends BaseAlfrescoSpringTest
{
    private RatingService ratingService;
    private Repository repositoryHelper;
    private NodeRef companyHome;
    
    // These NodeRefs are used by the test methods.
    private NodeRef testFolder;
    private NodeRef testSubFolder;
    private NodeRef testDocInFolder;
    private NodeRef testDocInSubFolder;
    
    // The out of the box scheme names.
    private static final String LIKES_SCHEME_NAME = "likesRatingScheme";
    private static final String FIVE_STAR_SCHEME_NAME = "fiveStarRatingScheme";
    
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        this.ratingService = (RatingService) this.applicationContext.getBean("ratingService");
        this.repositoryHelper = (Repository) this.applicationContext.getBean("repositoryHelper");

        // Set the current security context as admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        companyHome = this.repositoryHelper.getCompanyHome();
        
        //TODO These could be created @BeforeClass
        testFolder = createNode(companyHome, "testFolder", ContentModel.TYPE_FOLDER);
        testSubFolder = createNode(testFolder, "testSubFolder", ContentModel.TYPE_FOLDER);

        testDocInFolder = createNode(testFolder, "testDocInFolder", ContentModel.TYPE_CONTENT);
        testDocInSubFolder = createNode(testSubFolder, "testDocInSubFolder", ContentModel.TYPE_CONTENT);
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
        assertEquals("'5*' rating scheme had wrong min.", 0, fiveStarRS.getMinRating());
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
            applyIllegalRating(testDocInFolder, illegalRating, LIKES_SCHEME_NAME);
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
        //Before we start, let's ensure the read behaviour on a pristine node is correct.
        final RatingScheme likesRatingScheme = ratingService.getRatingScheme(LIKES_SCHEME_NAME);
        Rating nullRating = ratingService.getRatingByCurrentUser(testDocInFolder, likesRatingScheme);
        assertNull("Expected a null rating,", nullRating);
        assertNull("Expected a null remove result.", ratingService.removeRatingByCurrentUser(testDocInFolder, likesRatingScheme));
        
        final int likesScore = 1;
        final int fiveStarScore = 5;
        
        // Both of these ratings will be applied by the same user: the 'current' user.
        ratingService.applyRating(testDocInFolder, likesScore, LIKES_SCHEME_NAME);
        ratingService.applyRating(testDocInFolder, fiveStarScore, FIVE_STAR_SCHEME_NAME);
        
        // Some basic node structure tests.
        assertTrue(ContentModel.ASPECT_RATEABLE + " aspect missing.",
                nodeService.hasAspect(testDocInFolder, ContentModel.ASPECT_RATEABLE));

        List<ChildAssociationRef> allChildren = nodeService.getChildAssocs(testDocInFolder,
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
        Rating likeRating = ratingService.getRatingByCurrentUser(testDocInFolder, likesRatingScheme);
        
        final RatingScheme fiveStarRatingScheme = ratingService.getRatingScheme(FIVE_STAR_SCHEME_NAME);
        Rating fiveStarRating = ratingService.getRatingByCurrentUser(testDocInFolder, fiveStarRatingScheme);

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
        ratingService.applyRating(testDocInFolder, updatedFiveStarScore, FIVE_STAR_SCHEME_NAME);
        
        // Some basic node structure tests.
        allChildren = nodeService.getChildAssocs(testDocInFolder,
                ContentModel.ASSOC_RATINGS, RegexQNamePattern.MATCH_ALL);

        // Still one cm:rating node
        assertEquals("Wrong number of ratings nodes.", 1, allChildren.size());
        // Same assoc names
        assertEquals("Wrong type qname on ratings assoc", ContentModel.ASSOC_RATINGS, allChildren.get(0).getTypeQName());
        assertEquals("Wrong qname on ratings assoc", expectedAssocName, allChildren.get(0).getQName());
        // node structure seems ok.
        

        // Now to check the updated ratings data are ok.
        Rating updatedFiveStarRating = ratingService.getRatingByCurrentUser(testDocInFolder, fiveStarRatingScheme);

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
        Rating deletedLikesRating = ratingService.removeRatingByCurrentUser(testDocInFolder, likesRatingScheme);
        // 'like' rating data should be unchanged.
        assertNotNull("'like' rating was null.", deletedLikesRating);
        assertEquals("Wrong score for rating", likesScore, deletedLikesRating.getScore());
        assertEquals("Wrong user for rating", AuthenticationUtil.getFullyAuthenticatedUser(), deletedLikesRating.getAppliedBy());
        assertEquals("Wrong date for rating", likeRatingAppliedAt, deletedLikesRating.getAppliedAt());

        // And delete the 'five star' rating.
        Rating deletedStarRating = ratingService.removeRatingByCurrentUser(testDocInFolder, fiveStarRatingScheme);
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
        
    //TODO Multiple users applying ratings to a doc.
}
