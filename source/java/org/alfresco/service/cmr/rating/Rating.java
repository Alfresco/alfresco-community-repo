
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
