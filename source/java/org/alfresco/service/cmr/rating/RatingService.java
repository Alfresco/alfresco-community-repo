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

package org.alfresco.service.cmr.rating;

import java.util.Map;

import org.alfresco.service.NotAuditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The Rating service. TODO
 * 
 * @author Neil McErlean
 * @since 3.4
 */
@PublicService
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
    void applyRating(NodeRef targetNode, int rating, String ratingSchemeName) throws RatingServiceException;

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
    
    // TODO Get average/total ratings on node
    
    Rating getRatingByCurrentUser(NodeRef targetNode, RatingScheme ratingScheme);
    
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
    Rating removeRatingByCurrentUser(NodeRef targetNode, RatingScheme ratingScheme);
}
