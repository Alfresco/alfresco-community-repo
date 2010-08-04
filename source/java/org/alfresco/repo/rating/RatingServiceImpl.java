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
     * @see org.alfresco.service.cmr.rating.RatingService#applyRating(org.alfresco.service.cmr.repository.NodeRef, float, java.lang.String)
     */
    public void applyRating(final NodeRef targetNode, final float rating,
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
        // TODO Is creator the right property to use here?
        Serializable creator = nodeService.getProperty(targetNode, ContentModel.PROP_CREATOR);
        return currentUser.equals(creator);
    }

    private void applyRating(NodeRef targetNode, float rating,
            String ratingSchemeName, final String userName) throws RatingServiceException
    {
        if (log.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Applying rating ")
               .append(rating).append(" in scheme ")
               .append(ratingSchemeName).append(" as user ")
               .append(userName).append(" on ").append(targetNode);
            log.debug(msg.toString());
        }
        
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
            
            Map<QName, Serializable> ratingProps = new HashMap<QName, Serializable>();
            ratingProps.put(ContentModel.PROP_RATING_SCORE, rating);
            ratingProps.put(ContentModel.PROP_RATED_AT, new Date());
            ratingProps.put(ContentModel.PROP_RATING_SCHEME, ratingSchemeName);

            nodeService.createNode(targetNode, ContentModel.ASSOC_RATINGS, assocQName, ContentModel.TYPE_RATING, ratingProps);
        }
        else
        {
            // There are previous ratings by this user.
            if (myRatingChildren.size() > 1 && log.isDebugEnabled())
            {
                log.debug("");
            }
            NodeRef myPreviousRatingsNode = myRatingChildren.get(0).getChildRef();
            
            Map<QName, Serializable> existingProps = nodeService.getProperties(myPreviousRatingsNode);
            String existingRatingScheme = (String)existingProps.get(ContentModel.PROP_RATING_SCHEME);
            
            // If it's a re-rating in the existing scheme, replace.
            if (ratingScheme.getName().equals(existingRatingScheme))
            {
                Map<QName, Serializable> ratingProps = new HashMap<QName, Serializable>();
                ratingProps.put(ContentModel.PROP_RATING_SCHEME, ratingSchemeName);
                ratingProps.put(ContentModel.PROP_RATING_SCORE, rating);
                ratingProps.put(ContentModel.PROP_RATED_AT, new Date());
                
                nodeService.setProperties(myPreviousRatingsNode, ratingProps);
            }
            // But if it's a new rating in a different scheme, we don't support this scenario.
            else
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Cannot apply rating ")
                   .append(rating).append(" [")
                   .append(ratingSchemeName).append("] to node ")
                   .append(targetNode).append(". Already rated in ")
                   .append(existingRatingScheme);
                
                throw new RatingServiceException(msg.toString());
            }
        }
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

    private Rating getRating(NodeRef targetNode, String ratingSchemeName, String user)
    {
        List<ChildAssociationRef> ratingChildren = getRatingNodeChildren(targetNode, user);
        
        // If there are none, return null
        if (ratingChildren.isEmpty())
        {
            return null;
        }
        
        // Take the node pertaining to the current user.
        ChildAssociationRef ratingNodeAssoc = ratingChildren.get(0);
        Map<QName, Serializable> properties = nodeService.getProperties(ratingNodeAssoc.getChildRef());
        
        // Find the index of the rating scheme we're interested in.
        String existingRatingScheme = (String)properties.get(ContentModel.PROP_RATING_SCHEME);
        if (existingRatingScheme.equals(ratingSchemeName) == false)
        {
            // There is no rating in this scheme by the specified user.
            return null;
        }
        else
        {
            Float existingRatingScore = (Float)properties.get(ContentModel.PROP_RATING_SCORE);
            Date existingRatingDate = (Date)properties.get(ContentModel.PROP_RATED_AT);
            
            Rating result = new Rating(getRatingScheme(existingRatingScheme),
                    existingRatingScore,
                    user,
                    existingRatingDate);
            return result;
        }
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

    private Rating removeRating(NodeRef targetNode, String ratingSchemeName, final String user)
    {
        List<ChildAssociationRef> ratingChildren = getRatingNodeChildren(targetNode, user);
        if (ratingChildren.isEmpty())
        {
            return null;
        }
        ChildAssociationRef lastChild = ratingChildren.get(0);
        Map<QName, Serializable> properties = nodeService.getProperties(lastChild.getChildRef());
        
        Rating result = null;
        // If the rating is for the specified scheme delete it.
        // Get the scheme name and check it.
        if (ratingSchemeName.equals(properties.get(ContentModel.PROP_RATING_SCHEME)))
        {
            Float score = (Float) properties.get(ContentModel.PROP_RATING_SCORE);
            Date date = (Date)properties.get(ContentModel.PROP_RATED_AT);
            
            nodeService.deleteNode(lastChild.getChildRef());
            result = new Rating(getRatingScheme(ratingSchemeName), score, user, date);
        }
        
        return result;
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rating.RatingService#getTotalRating(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public float getTotalRating(NodeRef targetNode, String ratingSchemeName)
    {
        //TODO Performance improvement? : put node rating total/count/average into a
        //                                property in the db.
        List<ChildAssociationRef> ratingsNodes = this.getRatingNodeChildren(targetNode, null);
        
        // It's one node per user so the size of this list is the number of ratings applied.
        // However not all of these users' ratings need be in the specified scheme.
        // So we need to go through and check that the rating node contains a rating for the
        // specified scheme.
        float result = 0;
        for (ChildAssociationRef ratingsNode : ratingsNodes)
        {
            Rating rating = getRatingFrom(ratingsNode.getChildRef());
            if (rating.getScheme().getName().equals(ratingSchemeName))
            {
                result += rating.getScore();
            }
        }
        return result;
    }
    
    public float getAverageRating(NodeRef targetNode, String ratingSchemeName)
    {
        List<ChildAssociationRef> ratingsNodes = this.getRatingNodeChildren(targetNode, null);
        
        // It's one node per user so the size of this list is the number of ratings applied.
        // However not all of these users' ratings need be in the specified scheme.
        // So we need to go through and check that the rating node contains a rating for the
        // specified scheme.
        int ratingCount = 0;
        float ratingTotal = 0;
        for (ChildAssociationRef ratingsNode : ratingsNodes)
        {
            Rating rating = getRatingFrom(ratingsNode.getChildRef());
            if (rating.getScheme().getName().equals(ratingSchemeName))
            {
                ratingCount++;
                ratingTotal += rating.getScore();
            }
        }
        if (ratingCount == 0)
        {
            return -1;
        }
        else
        {
            return (float)ratingTotal / (float)ratingCount;
        }
    }

    public int getRatingsCount(NodeRef targetNode, String ratingSchemeName)
    {
        List<ChildAssociationRef> ratingsNodes = this.getRatingNodeChildren(targetNode, null);
        
        // It's one node per user so the size of this list is the number of ratings applied.
        // However not all of these users' ratings need be in the specified scheme.
        // So we need to go through and check that the rating node contains a rating for the
        // specified scheme.
        int result = 0;
        for (ChildAssociationRef ratingsNode : ratingsNodes)
        {
            Rating rating = getRatingFrom(ratingsNode.getChildRef());
            if (rating.getScheme().getName().equals(ratingSchemeName))
            {
                result++;
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
     * This method returns a {@link Rating} object for the specified cm:rating node.
     * @param ratingNode
     * @return
     */
    private Rating getRatingFrom(NodeRef ratingNode)
    {
        // The appliedBy is encoded in the parent assoc qname.
        // It will be the same user for all ratings in this node.
        ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(ratingNode);
        String appliedBy = parentAssoc.getQName().getLocalName();
        
        Map<QName, Serializable> properties = nodeService.getProperties(ratingNode);
        
        final String schemeName = (String)properties.get(ContentModel.PROP_RATING_SCHEME);
        final Float score = (Float)properties.get(ContentModel.PROP_RATING_SCORE);
        final Date ratedAt = (Date)properties.get(ContentModel.PROP_RATED_AT);
        RatingScheme scheme = getRatingScheme(schemeName);
        Rating result = new Rating(scheme, score, appliedBy, ratedAt);
        return result;
    }
}
