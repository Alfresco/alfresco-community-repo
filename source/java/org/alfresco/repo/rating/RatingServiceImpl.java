/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.rating.Rating;
import org.alfresco.service.cmr.rating.RatingScheme;
import org.alfresco.service.cmr.rating.RatingService;
import org.alfresco.service.cmr.rating.RatingServiceException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * @author Neil McErlean
 * @since 3.4
 */
public class RatingServiceImpl implements RatingService
{
    private static final Log log = LogFactory.getLog(RatingServiceImpl.class);
    private RatingSchemeRegistry schemeRegistry;
    
    // Injected services
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private BehaviourFilter behaviourFilter;
    
    private RatingNamingConventionsUtil ratingNamingConventions;
    
    public void setRatingSchemeRegistry(RatingSchemeRegistry schemeRegistry)
    {
        this.schemeRegistry = schemeRegistry;
    }
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }
    
    public void setRollupNamingConventions(RatingNamingConventionsUtil namingConventions)
    {
        this.ratingNamingConventions = namingConventions;
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
        if (isCreator && this.getRatingScheme(ratingSchemeName).isSelfRatingAllowed() == false)
        {
            throw new RatingServiceException("Users can't rate their own content for scheme " + ratingSchemeName);
        }
        
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() 
        {
            public Void doWork() throws Exception
            {
                applyRating(targetNode, rating, ratingSchemeName, currentUser);
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * This method checks if the current fully authenticated user is the cm:creator of the specified node.
     */
    private boolean isCurrentUserNodeCreator(NodeRef targetNode)
    {
        final String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
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
        
        // To support rolling up rating totals, counts etc, we use aspects named by convention.
        // Before we start writing data into the db, we'll check that an aspect has been defined
        //   with the expected name.
        QName rollupAspectName = ratingNamingConventions.getRollupAspectNameFor(ratingScheme);
        final boolean rollupAspectIsDefined = dictionaryService.getAspect(rollupAspectName) != null;

        
        // Ensure that the application of a rating does not cause updates
        // to the modified, modifier properties on the rated node.
        if (nodeService.hasAspect(targetNode, ContentModel.ASPECT_RATEABLE) == false)
        {
            behaviourFilter.disableBehaviour(targetNode, ContentModel.ASPECT_AUDITABLE);
            try
            {
                // Add the cm:rateable aspect if it's not there already.
                nodeService.addAspect(targetNode, ContentModel.ASPECT_RATEABLE, null);
                
                // We'll also add the rollup aspect specific for this rating scheme - if one has been defined in the content model.
                if (rollupAspectIsDefined)
                {
                    nodeService.addAspect(targetNode, rollupAspectName, null);
                }
                
            }
            finally
            {
                behaviourFilter.enableBehaviour(targetNode, ContentModel.ASPECT_AUDITABLE);
            }
        }

        // We're looking for child cm:rating nodes whose assoc qname matches the current user & rating scheme.
        // i.e. we're looking for previously applied ratings by this user in this scheme.
        // See RatingNamingConventionsUtil.java for details.
        final QName assocQName = ratingNamingConventions.getRatingAssocNameFor(userName, ratingScheme.getName());
        List<ChildAssociationRef> myRatingChildren = nodeService.getChildAssocs(targetNode, ContentModel.ASSOC_RATINGS, assocQName);
        if (myRatingChildren.isEmpty())
        {
            // There are no previous ratings from this user/scheme combination, so we create a new cm:rating child node.
            
            Map<QName, Serializable> ratingProps = new HashMap<QName, Serializable>();
            ratingProps.put(ContentModel.PROP_RATING_SCORE, rating);
            ratingProps.put(ContentModel.PROP_RATED_AT, new Date());
            ratingProps.put(ContentModel.PROP_RATING_SCHEME, ratingSchemeName);

            behaviourFilter.disableBehaviour(targetNode, ContentModel.ASPECT_AUDITABLE);
            try
            {
                nodeService.createNode(targetNode, ContentModel.ASSOC_RATINGS, assocQName, ContentModel.TYPE_RATING, ratingProps);
            }
            finally
            {
                behaviourFilter.enableBehaviour(targetNode, ContentModel.ASPECT_AUDITABLE);
            }
        }
        else
        {
            // There are previous ratings by this user/ratingScheme combination.
            NodeRef myPreviousRatingsNode = myRatingChildren.get(0).getChildRef();
            
            Map<QName, Serializable> ratingProps = new HashMap<QName, Serializable>();
            ratingProps.put(ContentModel.PROP_RATING_SCHEME, ratingSchemeName);
            ratingProps.put(ContentModel.PROP_RATING_SCORE, rating);
            ratingProps.put(ContentModel.PROP_RATED_AT, new Date());
            
            nodeService.setProperties(myPreviousRatingsNode, ratingProps);
        }
        
        // Now that we have applied the rating, we need to recalculate the rollup properties.
        recalculateRatingRollups(targetNode, ratingScheme);
    }

    private void recalculateRatingRollups(NodeRef targetNode,
            final RatingScheme ratingScheme)
    {
        QName rollupAspectName = ratingNamingConventions.getRollupAspectNameFor(ratingScheme);
        AspectDefinition rollupAspect = dictionaryService.getAspect(rollupAspectName);

        // Only run the rating rollups for this node if the aspect which will hold the results has been defined
        if ((rollupAspect != null))
        {
            behaviourFilter.disableBehaviour(targetNode, ContentModel.ASPECT_AUDITABLE);
            try
            {
                for (AbstractRatingRollupAlgorithm rollupAlgorithm : ratingScheme.getPropertyRollups())
                {
                    Serializable s = rollupAlgorithm.recalculate(targetNode);
                    QName rollupPropertyName = ratingNamingConventions.getRollupPropertyNameFor(ratingScheme, rollupAlgorithm.getRollupName());
                    nodeService.setProperty(targetNode, rollupPropertyName, s);
                    
                    if (!rollupAspect.getProperties().containsKey(rollupPropertyName) && log.isDebugEnabled())
                    {
                        StringBuilder msg = new StringBuilder();
                        msg.append("Rating property rollup property ").append(rollupPropertyName)
                           .append(" on aspect " ).append(rollupAspectName)
                           .append(" is not defined in the content model.");
                        log.debug(msg.toString());
                    }
                }
            }
            finally
            {
                behaviourFilter.enableBehaviour(targetNode, ContentModel.ASPECT_AUDITABLE);
            }
        }
        else
        {
            if (log.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Rating property rollup aspect ").append(rollupAspectName)
                   .append(" is not defined in the content model & therefore the rollup was not persisted.");
                log.debug(msg.toString());
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

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rating.RatingService#getRatingsByCurrentUser(org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<Rating> getRatingsByCurrentUser(NodeRef targetNode)
    {
        final String fullyAuthenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();
        
        List<ChildAssociationRef> children = getRatingNodeChildren(targetNode, null, fullyAuthenticatedUser);
        List<Rating> result = new ArrayList<Rating>(children.size());
        
        for (ChildAssociationRef child : children)
        {
            result.add(convertNodeRefToRating(fullyAuthenticatedUser, child.getChildRef()));
        }
        return result;
    }

    /**
     * This method gets the rating for the specified node, in the specified scheme by the specified user.
     * @param targetNode the node whose rating we are looking for.
     * @param ratingSchemeName the rating scheme name in which we are looking for a rating.
     * @param user the user name of the user whose rating we are looking for.
     * @return the {@link Rating} if there is one.
     */
    private Rating getRating(NodeRef targetNode, String ratingSchemeName, String user)
    {
        List<ChildAssociationRef> ratingChildren = getRatingNodeChildren(targetNode, ratingSchemeName, user);
        
        // If there are none, return null
        if (ratingChildren.isEmpty())
        {
            return null;
        }
        
        // Take the node pertaining to the current user & scheme.
        ChildAssociationRef ratingNodeAssoc = ratingChildren.get(0);
        
        return convertNodeRefToRating(user, ratingNodeAssoc.getChildRef());
    }

    /**
     * This method converts a NodeRef (which must be an instance of a cm:rating node)
     * into a {@link Rating} object.
     * @param ratingSchemeName
     * @param user
     * @param ratingNode
     * @return
     */
    private Rating convertNodeRefToRating(String user, NodeRef ratingNode)
    {
        Map<QName, Serializable> properties = nodeService.getProperties(ratingNode);
        
        String existingRatingScheme = (String)properties.get(ContentModel.PROP_RATING_SCHEME);

        Float existingRatingScore = (Float)properties.get(ContentModel.PROP_RATING_SCORE);
        Date existingRatingDate = (Date)properties.get(ContentModel.PROP_RATED_AT);
        
        Rating result = new Rating(getRatingScheme(existingRatingScheme),
                existingRatingScore,
                user,
                existingRatingDate);
        return result;
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
        List<ChildAssociationRef> ratingChildren = getRatingNodeChildren(targetNode, ratingSchemeName, user);
        if (ratingChildren.isEmpty())
        {
            return null;
        }
        ChildAssociationRef child = ratingChildren.get(0);
        Map<QName, Serializable> properties = nodeService.getProperties(child.getChildRef());
        
        Rating result = null;
        // If the rating is for the specified scheme delete it.
        // Get the scheme name and check it.
        if (ratingSchemeName.equals(properties.get(ContentModel.PROP_RATING_SCHEME)))
        {
            Float score = (Float) properties.get(ContentModel.PROP_RATING_SCORE);
            Date date = (Date)properties.get(ContentModel.PROP_RATED_AT);
            
            nodeService.deleteNode(child.getChildRef());
            
            recalculateRatingRollups(targetNode, getRatingScheme(ratingSchemeName));

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
        Serializable result = this.getRatingRollup(targetNode, ratingSchemeName, RatingTotalRollupAlgorithm.ROLLUP_NAME);
        if (result == null)
        {
            result = new Float(0f);
        }
        
        return (Float)result;
    }
    
    public float getAverageRating(NodeRef targetNode, String ratingSchemeName)
    {
        float totalRating = getTotalRating(targetNode, ratingSchemeName);
        int ratingCount = getRatingsCount(targetNode, ratingSchemeName);
        
        return ratingCount == 0 ? -1f : totalRating / (float)ratingCount;
    }

    public int getRatingsCount(NodeRef targetNode, String ratingSchemeName)
    {
        Serializable result = this.getRatingRollup(targetNode, ratingSchemeName, RatingCountRollupAlgorithm.ROLLUP_NAME);
        if (result == null)
        {
            result = new Integer(0);
        }
        
        return (Integer)result;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rating.RatingService#getRatingRollup(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String)
     */
    public Serializable getRatingRollup(NodeRef targetNode, String ratingSchemeName, String ratingRollupName)
    {
        RatingScheme scheme = schemeRegistry.getRatingSchemes().get(ratingSchemeName);
        if (scheme == null)
        {
            throw new RatingServiceException("Cannot retrieve rollup. Unrecognized rating scheme " + ratingSchemeName);
        }
        
        QName rollupAspectName = ratingNamingConventions.getRollupAspectNameFor(ratingSchemeName);

        Serializable result = null;
        // If the rated node has the rollup aspect applied
        if (nodeService.hasAspect(targetNode, rollupAspectName))
        {
            QName rollupPropertyName = ratingNamingConventions.getRollupPropertyNameFor(ratingSchemeName, ratingRollupName);
            result = nodeService.getProperty(targetNode, rollupPropertyName);
        }
        
        return result;
    }

    /**
     * This method gets all the cm:rating child nodes of the specified targetNode that
     * have been applied by the specified user in the specified rating scheme.
     * 
     * @param targetNode the target node under which the cm:rating nodes reside.
     * @param user the user name of the user whose ratings are sought, <code>null</code>
     *             for all users.
     * @param ratingSchemeName the name of the rating scheme, <code>null</code> for all schemes.
     * @return
     */
    List<ChildAssociationRef> getRatingNodeChildren(NodeRef targetNode,
            String ratingSchemeName, String user)
    {
        QNamePattern qnamePattern = ratingNamingConventions.getRatingAssocPatternForUser(user, ratingSchemeName);
        List<ChildAssociationRef> results = nodeService.getChildAssocs(targetNode, ContentModel.ASSOC_RATINGS, qnamePattern);

        return results;
    }
    
    /**
     * This method returns a {@link Rating} object for the specified cm:rating node.
     * @param ratingNode
     * @return
     */
    Rating getRatingFrom(NodeRef ratingNode)
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
