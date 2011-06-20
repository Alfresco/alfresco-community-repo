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

package org.alfresco.service.cmr.rating;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.rating.AbstractRatingRollupAlgorithm;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Interface for public and internal rating operations.
 * <p/>
 * The RatingService can be used to manage ratings on any content node in the repository.
 * These ratings are defined by {@link RatingScheme rating schemes}
 * which are injected via spring (see <code>rating-service-context.xml</code>). The rating
 * schemes define a minimum and a maximum score value for that scheme.
 * <p/>
 * Ratings can be {@link RatingService#applyRating(NodeRef, float, String) applied},
 * {@link RatingService#applyRating(NodeRef, float, String) updated} and
 * {@link RatingService#removeRatingByCurrentUser(NodeRef, RatingScheme) removed}.
 * 
 * @author Neil McErlean
 * @since 3.4
 */
public interface RatingService
{
    /**
     * Returns the available {@link RatingScheme rating schemes} keyed by name.
     * 
     * @return The {@link RatingScheme rating schemes}.
     */
    @NotAuditable
    Map<String, RatingScheme> getRatingSchemes();

    /**
     * Returns the named {@link RatingScheme rating scheme} if there is one.
     * 
     * @param ratingSchemeName name of the rating scheme.
     * @return The {@link RatingScheme rating schemes} if one of that name is registered,
     *         else <code>null</code>.
     */
    @NotAuditable
    RatingScheme getRatingScheme(String ratingSchemeName);

    /**
     * This method applies the given rating to the specified target node. If a rating
     * from the current user in the specified scheme already exists, it will be replaced.
     * 
     * @param targetNode the node to which the rating is to be applied.
     * @param rating the rating which is to be applied.
     * @param ratingSchemeName the name of the rating scheme to use.
     * 
     * @throws RatingServiceException if the rating is not within the range defined by the named scheme
     *                                or if the named scheme is not registered.
     * @see RatingService#getRatingSchemes()
     * @see RatingScheme
     */
    @NotAuditable
    void applyRating(NodeRef targetNode, float rating, String ratingSchemeName) throws RatingServiceException;

    /**
     * This method gets the number of individual ratings which have been applied to
     * the specified node in the specified {@link RatingScheme}.
     * 
     * @param targetNode the node on which the rating is sought.
     * @param ratingScheme the rating scheme to use.
     * 
     * @return the number of individual ratings applied to this node.
     * @see RatingService#getRatingSchemes()
     * @see RatingScheme
     */
    @NotAuditable
    int getRatingsCount(NodeRef targetNode, String ratingSchemeName);

    /**
     * This method gets the total accumulated rating score for
     * the specified node in the specified {@link RatingScheme}.
     * That is, the rating scores for all users for the specified
     * node are summed to give the result.
     * 
     * @param targetNode the node on which the rating total is sought.
     * @param ratingScheme the rating scheme to use.
     * 
     * @return the sum of all individual ratings applied to this node in the specified scheme.
     * @see RatingService#getRatingSchemes()
     * @see RatingScheme
     */
    @NotAuditable
    float getTotalRating(NodeRef targetNode, String ratingSchemeName);

    /**
     * This method returns the average (mean) rating in the specified scheme for the
     * specified nodeRef. If there have been no ratings applied, -1 is returned.
     * @param targetNode the node for which an average is sought.
     * @param ratingSchemeName the rating scheme name in which the average is defined.
     * @return the average (mean) value if there is one, else -1.
     */
    @NotAuditable
    float getAverageRating(NodeRef targetNode, String ratingSchemeName);

    /**
     * This method gets the {@link Rating} applied by the current user to the specified node in the specified
     * {@link RatingScheme} - if there is one.
     * 
     * @param targetNode the node on which the rating is sought.
     * @param ratingScheme the rating scheme to use.
     * 
     * @return the Rating object if there is one, else <code>null</code>.
     * @see RatingService#getRatingSchemes()
     * @see RatingScheme
     */
    @NotAuditable
    Rating getRatingByCurrentUser(NodeRef targetNode, String ratingSchemeName);
    
    /**
     * This method gets the {@link Rating ratings} applied by the current user to the specified node.
     * 
     * @param targetNode the node on which the ratings are sought.
     * 
     * @return a List of Rating objects if there are any, else {@link java.util.Collections#emptyList()}.
     * @see RatingService#getRatingSchemes()
     * @see RatingScheme
     * 
     * @since 3.5
     */
    @NotAuditable
    List<Rating> getRatingsByCurrentUser(NodeRef targetNode);
    
    /**
     * This method removes any {@link Rating} applied by the current user to the specified node in the specified
     * {@link RatingScheme}.
     * 
     * @param targetNode the node from which the rating is to be removed.
     * @param ratingScheme the rating scheme to use.
     * 
     * @return the deleted Rating object if there was one, else <code>null</code>.
     * @see RatingService#getRatingSchemes()
     * @see RatingScheme
     */
    @NotAuditable
    Rating removeRatingByCurrentUser(NodeRef targetNode, String ratingSchemeName);
    
    /**
     * This method returns a 'rolled up' property value for the specified targetNode. Examples
     * of rolled up property values are 'ratingTotal', 'ratingCount', but other values can be added.
     * <p/>
     * Rolled up properties in the RatingService are stored as properties on the rated node and have their values
     * calculated by running a fixed algorithm on properties stored across the <code>cm:rating</code> child nodes.
     * An example of such a roll up would be 'ratingTotal' which would be the sum of all ratings applied to the
     * targetNode in the specified rating scheme.
     * <p/>
     * By rolling up property values from the various <code>cm:rating</code> child nodes and persisting them
     * as individual properties on the <code>cm:rateable</code> node itself, we are able to support indexing, searching
     * and sorting of such properties.
     * 
     * @param targetNode the rated node whose rolled up property we wish to read.
     * @param ratingSchemeName the rating scheme name in which the property is relevant.
     * @param ratingRollupName the name of the rating rollup property, as given in {@link AbstractRatingRollupAlgorithm#getRollupName()}.
     * @return A value for the rolled up property, which will depend of course on the rollup requested.
     * 
     * @since 3.5
     */
    @NotAuditable
    Serializable getRatingRollup(NodeRef targetNode, String ratingSchemeName, String ratingRollupName);
}
