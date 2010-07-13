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

import org.alfresco.repo.rating.RatingSchemeRegistry;

/**
 * This data type represents a rating scheme as used in the {@link RatingService}.
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
}
