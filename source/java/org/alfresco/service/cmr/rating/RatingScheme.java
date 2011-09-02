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

import java.util.List;

import org.alfresco.repo.rating.AbstractRatingRollupAlgorithm;
import org.alfresco.repo.rating.RatingSchemeRegistry;

/**
 * This interface defines a Rating Scheme, which is a named scheme for user-supplied
 * ratings with a defined minimum value and a defined maximum value. The minimum must
 * not be greater than the maximum but the two values can be equal.
 * These schemes are defined within spring context files and injected into the
 * {@link RatingSchemeRegistry} at startup.
 * 
 * @author Neil McErlean
 * @since 3.4
 */
public interface RatingScheme
{
    /**
     * This method returns the name which uniquely identifies the rating scheme.
     * 
     * @return the name.
     */
    public String getName();

    /**
     * This method returns the minimum rating defined for this scheme.
     * 
     * @return the minimum rating.
     */
    public float getMinRating();

    /**
     * This method returns the maximum rating defined for this scheme.
     * 
     * @return the maximum rating.
     */
    public float getMaxRating();
    
    /**
     * This method returns <code>true</code> if the cm:creator of the node is allowed
     * to apply a rating to it, else <code>false</code>.
     * 
     * @return whether or not the cm:creator of the node can apply a rating in this scheme.
     */
    public boolean isSelfRatingAllowed();
    
    /**
     * This method returns a List of {@link AbstractRatingRollupAlgorithm property rollup algorithms}
     * which are used in order to calculate rating totals, counts etc for a rated node.
     * 
     * @return an unmodifiable list of property rollup algorithms.
     * @since 3.5
     */
    public List<AbstractRatingRollupAlgorithm> getPropertyRollups();

}
