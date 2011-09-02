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

import java.util.Date;

/**
 * This struct class holds the essential data of a rating.
 * 
 * @author Neil McErlean
 * @since 3.4
 */
public class Rating
{
    private final float ratingScore;
    private final String ratingAppliedBy;
    private final Date ratingAppliedAt;
    private final RatingScheme ratingScheme;
    
    public Rating(RatingScheme scheme, float score, String appliedBy, Date appliedAt)
    {
        this.ratingScheme = scheme;
        this.ratingScore = score;
        this.ratingAppliedBy = appliedBy;
        this.ratingAppliedAt = appliedAt;
    }

    /**
     * Gets the score applied as part of this rating. In normal circumstances a score
     * should always lie within the bounds defined by the {@link RatingScheme}.
     * 
     * @return the score.
     */
    public float getScore()
    {
        return ratingScore;
    }

    /**
     * Gets the user name of the user who applied this rating.
     * 
     * @return the user who applied the rating.
     */
    public String getAppliedBy()
    {
        return ratingAppliedBy;
    }

    /**
     * Gets the time/date at which the rating was applied.
     * 
     * @return the date/time at which the rating was applied.
     */
    public Date getAppliedAt()
    {
        return ratingAppliedAt;
    }

    /**
     * Gets the {@link RatingScheme} under which the rating was applied.
     * 
     * @return the rating scheme used for this rating.
     */
    public RatingScheme getScheme()
    {
        return ratingScheme;
    }
}
