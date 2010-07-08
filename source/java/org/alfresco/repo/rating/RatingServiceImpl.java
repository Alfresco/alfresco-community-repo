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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.rating.Rating;
import org.alfresco.service.cmr.rating.RatingScheme;
import org.alfresco.service.cmr.rating.RatingService;
import org.alfresco.service.cmr.rating.RatingServiceException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * @author Neil McErlean
 * @since 3.4
 */
public class RatingServiceImpl implements RatingService
{
    //TODO Add links to ActivityService. Straight calls? Behaviours?
    
    private static final Log log = LogFactory.getLog(RatingServiceImpl.class);
    private RatingSchemeRegistry schemeRegistry;
    
    // Injected services
    private NodeService nodeService;

    public void setRatingSchemeRegistry(RatingSchemeRegistry schemeRegistry)
    {
        this.schemeRegistry = schemeRegistry;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public Map<String, RatingScheme> getRatingSchemes()
    {
        // This is already an unmodifiable Map.
        return schemeRegistry.getRatingSchemes();
    }
    
    public RatingScheme getRatingScheme(String ratingSchemeName)
    {
        return schemeRegistry.getRatingSchemes().get(ratingSchemeName);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rating.RatingService#applyRating(org.alfresco.service.cmr.repository.NodeRef, int, java.lang.String)
     */
    public void applyRating(final NodeRef targetNode, final int rating,
            final String ratingSchemeName) throws RatingServiceException
    {
        final String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        boolean isCreator = isCurrentUserNodeCreator(targetNode);
        if (isCreator)
        {
            throw new RatingServiceException("Users can't rate their own content.");
        }
        
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
            public Void doWork() throws Exception
            {
                applyRating(targetNode, rating, ratingSchemeName, currentUser);
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    private boolean isCurrentUserNodeCreator(NodeRef targetNode)
    {
        final String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        // TODO Is creator the right property here?
        Serializable creator = nodeService.getProperty(targetNode, ContentModel.PROP_CREATOR);
        return currentUser.equals(creator);
    }

    @SuppressWarnings("unchecked")
    private void applyRating(NodeRef targetNode, int rating,
            String ratingSchemeName, final String userName) throws RatingServiceException
    {
        //TODO More logging.
        
        // Sanity check the rating scheme being used and the rating being applied.
        final RatingScheme ratingScheme = this.getRatingScheme(ratingSchemeName);
        if (ratingScheme == null)
        {
            throw new RatingServiceException("Unrecognised rating scheme: " + ratingSchemeName);
        }
        if (rating < ratingScheme.getMinRating() || rating > ratingScheme.getMaxRating())
        {
            throw new RatingServiceException("Rating " + rating + " violates range for " + ratingScheme);
        }

        // Add the cm:rateable aspect if it's not there already.
        if (nodeService.hasAspect(targetNode, ContentModel.ASPECT_RATEABLE) == false)
        {
            nodeService.addAspect(targetNode, ContentModel.ASPECT_RATEABLE, null);
        }

        // We're looking for child cm:rating nodes whose qname matches the current user.
        // i.e. we're looking for previously applied ratings by this user.
        final QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, userName);
        List<ChildAssociationRef> myRatingChildren = nodeService.getChildAssocs(targetNode, ContentModel.ASSOC_RATINGS, assocQName);
        if (myRatingChildren.isEmpty())
        {
            // There are no previous ratings from this user, so we create a new cm:rating child node.
            
            // These are multivalued properties.
            Map<QName, Serializable> ratingProps = new HashMap<QName, Serializable>();
            ratingProps.put(ContentModel.PROP_RATING_SCORE, new Integer[]{rating});
            ratingProps.put(ContentModel.PROP_RATING_SCORE, toSerializableList(new Integer[]{rating}));
            ratingProps.put(ContentModel.PROP_RATED_AT, toSerializableList(new Date[]{new Date()}));
            ratingProps.put(ContentModel.PROP_RATING_SCHEME, toSerializableList(new String[]{ratingSchemeName}));

            nodeService.createNode(targetNode, ContentModel.ASSOC_RATINGS, assocQName, ContentModel.TYPE_RATING, ratingProps);
        }
        else
        {
            // There are previous ratings by this user. Things are a little more complex.
            if (myRatingChildren.size() > 1 && log.isDebugEnabled())
            {
                log.debug("");
            }
            NodeRef myPreviousRatingsNode = myRatingChildren.get(0).getChildRef();
            
            Map<QName, Serializable> existingProps = nodeService.getProperties(myPreviousRatingsNode);
            List<String> existingRatingSchemes = (List<String>)existingProps.get(ContentModel.PROP_RATING_SCHEME);
            List<Integer> existingRatingScores = (List<Integer>)existingProps.get(ContentModel.PROP_RATING_SCORE);
            List<Date> existingRatingDates = (List<Date>)existingProps.get(ContentModel.PROP_RATED_AT);
            
            //TODO These should all be the same length lists. Log if not.
            
            // If the schemes list already contains an entry matching the rating we're setting
            // we need to delete it and then delete the score and date at the corresponding indexes.
            int indexOfExistingRating = existingRatingSchemes.indexOf(ratingSchemeName);
            if (indexOfExistingRating != -1)
            {
                existingRatingSchemes.remove(indexOfExistingRating);
                existingRatingScores.remove(indexOfExistingRating);
                existingRatingDates.remove(indexOfExistingRating);
            }
            
            existingRatingSchemes.add(ratingSchemeName);
            existingRatingScores.add(rating);
            existingRatingDates.add(new Date());
            
            nodeService.setProperties(myPreviousRatingsNode, existingProps);
        }
    }
    
    private Serializable toSerializableList(Object[] array)
    {
        return (Serializable)Arrays.asList(array);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rating.RatingService#getRatingByCurrentUser(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public Rating getRatingByCurrentUser(NodeRef targetNode, String ratingSchemeName)
    {
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        return this.getRating(targetNode, ratingSchemeName, currentUser);
    }

    @SuppressWarnings("unchecked")
    private Rating getRating(NodeRef targetNode, String ratingSchemeName, String user)
    {
        List<ChildAssociationRef> ratingChildren = getRatingNodeChildren(targetNode, user);
        
        // If there are none, return null
        if (ratingChildren.isEmpty())
        {
            return null;
        }
        
        // Take the last node pertaining to the current user.
        ChildAssociationRef lastChild = ratingChildren.get(ratingChildren.size() - 1);
        Map<QName, Serializable> properties = nodeService.getProperties(lastChild.getChildRef());

        // Find the index of the rating scheme we're interested in.
        RatingScheme ratingScheme = getRatingScheme(ratingSchemeName);
        int index = findIndexOfRatingScheme(properties, ratingScheme);
        if (index == -1)
        {
            // There is no rating in this scheme by the specified user.
            return null;
        }
        else
        {
            // There is a rating and the associated data are at the index'th place in each multivalued property.
            List<Integer> ratingScores = (List<Integer>)properties.get(ContentModel.PROP_RATING_SCORE);
            List<Date> ratingDates = (List<Date>)properties.get(ContentModel.PROP_RATED_AT);
            
            Rating result = new Rating(ratingScheme,
                    ratingScores.get(index),
                    user,
                    ratingDates.get(index));
            return result;
        }
        
        //TODO Don't forget that it is possible on read to have out-of-range ratings.
    }
    
    @SuppressWarnings("unchecked")
    private int findIndexOfRatingScheme(Map<QName, Serializable> properties, RatingScheme scheme)
    {
        List<String> ratingSchemes = (List<String>)properties.get(ContentModel.PROP_RATING_SCHEME);
        return ratingSchemes.indexOf(scheme.getName());
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rating.RatingService#removeRatingByCurrentUser(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public Rating removeRatingByCurrentUser(NodeRef targetNode,
            String ratingScheme)
    {
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        return removeRating(targetNode, ratingScheme, currentUser);
    }

    @SuppressWarnings("unchecked")
    private Rating removeRating(NodeRef targetNode, String ratingSchemeName, String user)
    {
        List<ChildAssociationRef> ratingChildren = getRatingNodeChildren(targetNode, user);
        if (ratingChildren.isEmpty())
        {
            // There are no ratings by any user.
            return null;
        }
        // Take the last node pertaining to the specified user.
        ChildAssociationRef lastChild = ratingChildren.get(ratingChildren.size() - 1);
        Map<QName, Serializable> properties = nodeService.getProperties(lastChild.getChildRef());

        // Find the index of the rating scheme we're interested in.
        RatingScheme ratingScheme = getRatingScheme(ratingSchemeName);
        int index = this.findIndexOfRatingScheme(properties, ratingScheme);
        if (index == -1)
        {
            // There is no rating in this scheme by the specified user.
            return null;
        }
        else
        {
            // There is a rating and the associated data are at the index'th place in each property.
            List<Integer> ratingScores = (List<Integer>)properties.get(ContentModel.PROP_RATING_SCORE);
            List<Date> ratingDates = (List<Date>)properties.get(ContentModel.PROP_RATED_AT);
            List<String> ratingSchemes = (List<String>)properties.get(ContentModel.PROP_RATING_SCHEME);
            
            Integer oldScore = ratingScores.remove(index);
            Date oldDate = ratingDates.remove(index);
            String oldScheme = ratingSchemes.remove(index);
            
            return new Rating(this.getRatingScheme(oldScheme),
                              oldScore, user, oldDate);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rating.RatingService#getTotalRating(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public int getTotalRating(NodeRef targetNode, String ratingSchemeName)
    {
        //TODO Put these in the db as properties?
        List<ChildAssociationRef> ratingsNodes = this.getRatingNodeChildren(targetNode, null);
        
        // It's one node per user so the size of this list is the number of ratings applied.
        // However not all of these users' ratings need be in the specified scheme.
        // So we need to go through and check that the rating node contains a rating for the
        // specified scheme.
        int result = 0;
        for (ChildAssociationRef ratingsNode : ratingsNodes)
        {
            List<Rating> ratings = getRatingsFrom(ratingsNode.getChildRef());
            for (Rating rating : ratings)
            {
                if (rating.getScheme().getName().equals(ratingSchemeName))
                {
                    result += rating.getScore();
                }
            }
        }
        return result;
    }
    
    // TODO We can at least amagamate these into one looping call.
    public float getAverageRating(NodeRef targetNode, String ratingSchemeName)
    {
        //TODO Put these in the db as properties?
        List<ChildAssociationRef> ratingsNodes = this.getRatingNodeChildren(targetNode, null);
        
        // It's one node per user so the size of this list is the number of ratings applied.
        // However not all of these users' ratings need be in the specified scheme.
        // So we need to go through and check that the rating node contains a rating for the
        // specified scheme.
        int ratingCount = 0;
        int ratingTotal = 0;
        for (ChildAssociationRef ratingsNode : ratingsNodes)
        {
            List<Rating> ratings = getRatingsFrom(ratingsNode.getChildRef());
            for (Rating rating : ratings)
            {
                if (rating.getScheme().getName().equals(ratingSchemeName))
                {
                    ratingCount++;
                    ratingTotal += rating.getScore();
                }
            }
        }
        return (float)ratingTotal / (float)ratingCount;
    }

    public int getRatingsCount(NodeRef targetNode, String ratingSchemeName)
    {
        //TODO Put these in the db as properties?
        List<ChildAssociationRef> ratingsNodes = this.getRatingNodeChildren(targetNode, null);
        
        // It's one node per user so the size of this list is the number of ratings applied.
        // However not all of these users' ratings need be in the specified scheme.
        // So we need to go through and check that the rating node contains a rating for the
        // specified scheme.
        int result = 0;
        for (ChildAssociationRef ratingsNode : ratingsNodes)
        {
            List<Rating> ratings = getRatingsFrom(ratingsNode.getChildRef());
            for (Rating rating : ratings)
            {
                if (rating.getScheme().getName().equals(ratingSchemeName))
                {
                    result++;
                }
            }
        }
        return result;
    }


    /**
     * This method gets all the cm:rating child nodes of the specified targetNode that
     * have been applied by the specified user.
     * 
     * @param targetNode the target node under which the cm:rating nodes reside.
     * @param user the user name of the user whose ratings are sought, <code>null</code>
     *             for all users.
     * @return
     */
    private List<ChildAssociationRef> getRatingNodeChildren(NodeRef targetNode,
            String user)
    {
        QNamePattern qnamePattern = null;
        if (user != null)
        {
            qnamePattern = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, user);
        }
        else
        {
            qnamePattern = RegexQNamePattern.MATCH_ALL;
        }
        List<ChildAssociationRef> results = nodeService.getChildAssocs(targetNode, ContentModel.ASSOC_RATINGS, qnamePattern);

        return results;
    }
    
    /**
     * This method returns a List of {@link Rating} objects for the specified cm:rating
     * node. As it's one ratingNode the results will be form one user, but will represent
     * 0..n schemes.
     * @param ratingNode
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<Rating> getRatingsFrom(NodeRef ratingNode)
    {
        // The appliedBy is encoded in the parent assoc qname.
        // It will be the same user for all ratings in this node.
        ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(ratingNode);
        String appliedBy = parentAssoc.getQName().getLocalName();
        
        Map<QName, Serializable> properties = nodeService.getProperties(ratingNode);
        List<String> ratingSchemes = (List<String>)properties.get(ContentModel.PROP_RATING_SCHEME);
        List<Integer> ratingScores = (List<Integer>)properties.get(ContentModel.PROP_RATING_SCORE);
        List<Date> ratingDates = (List<Date>)properties.get(ContentModel.PROP_RATED_AT);
        
        List<Rating> result = new ArrayList<Rating>(ratingSchemes.size());
        for (int i = 0; i < ratingSchemes.size(); i++)
        {
            final String schemeName = ratingSchemes.get(i);
            RatingScheme scheme = getRatingScheme(schemeName);
            result.add(new Rating(scheme, ratingScores.get(i), appliedBy, ratingDates.get(i)));
        }
        return result;
    }
}
