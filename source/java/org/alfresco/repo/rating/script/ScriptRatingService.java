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
package org.alfresco.repo.rating.script;

import java.util.Date;
import java.util.Set;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.rating.Rating;
import org.alfresco.service.cmr.rating.RatingService;
import org.alfresco.service.cmr.rating.RatingServiceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Script object representing the rating service.
 * 
 * @author Neil McErlean
 * @since 3.4
 */
public class ScriptRatingService extends BaseScopableProcessorExtension
{
    private static Log logger = LogFactory.getLog(ScriptRatingService.class);
    
    /** The Services registry */
    private ServiceRegistry serviceRegistry;
    private RatingService ratingService;

    /**
     * Set the service registry
     * 
     * @param serviceRegistry the service registry.
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
        this.ratingService = serviceRegistry.getRatingService();
    }
    
    /**
     * Gets the names for rating schemes currently in the system.
     * @return
     */
    public String[] getRatingSchemeNames()
    {
        Set<String> schemeNames = ratingService.getRatingSchemes().keySet();
        String[] result = new String[0];
        result = schemeNames.toArray(result);
        return result;
    }
    
    /**
     * Gets the minimum allowed rating for the specified rating scheme.
     * 
     * @param ratingSchemeName
     * @return
     */
    public float getMin(String ratingSchemeName)
    {
        return ratingService.getRatingScheme(ratingSchemeName).getMinRating();
    }
    
    /**
     * Gets the maximum allowed rating for the specified rating scheme.
     * 
     * @param ratingSchemeName
     * @return
     */
    public float getMax(String ratingSchemeName)
    {
        return ratingService.getRatingScheme(ratingSchemeName).getMaxRating();
    }
    
    /**
     * This method checks whether self-rating is allowed for the specified rating scheme.
     * If self-rating is allowed in the specified scheme, then the cm:creator of a node can apply a rating,
     * otherwise they cannot.
     * 
     * @param ratingSchemeName the rating scheme bean name.
     * @return <code>true</code> if users can rate their own content, else <code>false</code>.
     */
    public boolean isSelfRatingAllowed(String ratingSchemeName)
    {
        return ratingService.getRatingScheme(ratingSchemeName).isSelfRatingAllowed();
    }

    /**
     * Applies the given rating to the specified node using the specified ratingScheme.
     * It is the responsibility of the caller to ensure that the rating scheme exists
     * and that the rating is within the limits defined for that scheme.
     * <p/>Furthermore, only one rating scheme per user per target node is supported. Any attempt
     * by one user to apply a second rating in a different scheme will result in a {@link RatingServiceException}.
     * 
     * @param node
     * @param rating
     * @param ratingSchemeName
     * @throws RatingServiceException
     * @see ScriptRatingService#getMin(String)
     * @see ScriptRatingService#getMax(String)
     */
    public void applyRating(ScriptNode node, float rating, String ratingSchemeName)
    {
        ratingService.applyRating(node.getNodeRef(), rating, ratingSchemeName);
    }
    
    /**
     * Removes any rating by the current user in the specified scheme from the specified
     * noderef.
     * @param node
     * @param ratingSchemeName
     */
    public void removeRating(ScriptNode node, String ratingSchemeName)
    {
        ratingService.removeRatingByCurrentUser(node.getNodeRef(), ratingSchemeName);
    }
    
    /**
     * Gets the rating applied to the specified node in the specified scheme by
     * the currently authenticated user.
     * @param node
     * @param ratingSchemeName
     * @return rating if there is one, else -1.
     * TODO -1 could be a valid rating.
     */
    public float getRating(ScriptNode node, String ratingSchemeName)
    {
        final Rating ratingByCurrentUser = ratingService.getRatingByCurrentUser(node.getNodeRef(), ratingSchemeName);
        return ratingByCurrentUser == null ? -1f : ratingByCurrentUser.getScore();
    }
    
    /**
     * Gets the rating applied date for the specified node in the specified scheme by
     * the currently authenticated user.
     * @param node
     * @param ratingSchemeName
     * @return rating applied date if there is one, else <code>null</code>
     */
    public Date getRatingAppliedAt(ScriptNode node, String ratingSchemeName)
    {
        final Rating ratingByCurrentUser = ratingService.getRatingByCurrentUser(node.getNodeRef(), ratingSchemeName);
        return ratingByCurrentUser == null ? null : ratingByCurrentUser.getAppliedAt();
    }

    /**
     * Gets the number of ratings applied to the specified node by all users in the specified
     * scheme.
     * @param node
     * @param ratingSchemeName
     * @return
     */
    public int getRatingsCount(ScriptNode node, String ratingSchemeName)
    {
        return ratingService.getRatingsCount(node.getNodeRef(), ratingSchemeName);
    }

    /**
     * Gets the total (sum) rating by all users on the specified node in the specified scheme.
     * @param node
     * @param ratingSchemeName
     * @return
     */
    public float getTotalRating(ScriptNode node, String ratingSchemeName)
    {
        return ratingService.getTotalRating(node.getNodeRef(), ratingSchemeName);
    }

    /**
     * Gets the average (mean) rating by all users on the specified node in the specified scheme.
     * @param node
     * @param ratingSchemeName
     * @return
     */
    public float getAverageRating(ScriptNode node, String ratingSchemeName)
    {
        return ratingService.getAverageRating(node.getNodeRef(), ratingSchemeName);
    }
}
