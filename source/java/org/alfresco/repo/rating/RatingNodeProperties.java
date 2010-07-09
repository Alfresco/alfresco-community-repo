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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.rating.RatingServiceException;
import org.alfresco.service.namespace.QName;

/**
 * The <code>cm:rating</code> content type defines the properties which are used in
 * managing user-applied ratings. See <code>contentModel.xml</code> for details.
 * <p/>
 * Briefly, these properties are multi-valued where a 'slice' through the property
 * set constitutes an individual user-applied rating. So if we were to take the value
 * at index <em>n</em> of each multi-valued property, that would represent the
 * score, appliedAt and scheme for one rating.
 * <p/>
 * This class simplifies and centralises the handling of these property sets.
 * 
 * @author Neil McErlean
 * @since 3.4
 */
public class RatingNodeProperties
{
    // These lists are declared as the concrete type 'ArrayList' as they must implement Serializable
    private final ArrayList<String> schemes;
    private final ArrayList<Integer> scores;
    private final ArrayList<Date> dates;

    public RatingNodeProperties(List<String> schemes, List<Integer> scores, List<Date> dates)
    {
        // No null lists.
        if (schemes == null) schemes = new ArrayList<String>();
        if (scores == null)  scores = new ArrayList<Integer>();
        if (dates == null)   dates = new ArrayList<Date>();
        
        // All lists must be the same length.
        if (scores.size() != schemes.size() || dates.size() != schemes.size())
        {
            throw new RatingServiceException("Rating node properties have unequal list lengths: "
                    + schemes.size() + " " + scores.size() + " " + dates.size());
        }
        
        // Copy all these data to ensure no leakage of this class' state into the original properties.
        this.schemes = new ArrayList<String>(schemes.size());
        for (String s : schemes)
            this.schemes.add(s);
        
        this.scores = new ArrayList<Integer>(scores.size());
        for (Integer i : scores)
            this.scores.add(i);
        
        this.dates = new ArrayList<Date>(dates.size());
        // We can't copy Dates straight over as Date objects are mutable, so clone.
        for (Date d : dates)
            this.dates.add((Date)d.clone());
    }

    /**
     * This factory method creates a new {@link RatingNodeProperties} from the specified
     * properties map.
     * @param props
     * @return
     */
    @SuppressWarnings("unchecked")
    public static RatingNodeProperties createFrom(Map<QName, Serializable> props)
    {
        List<String> schemes = (List<String>) props.get(ContentModel.PROP_RATING_SCHEME);
        List<Integer> scores = (List<Integer>)props.get(ContentModel.PROP_RATING_SCORE);
        List<Date> dates     = (List<Date>)   props.get(ContentModel.PROP_RATED_AT);
        
        return new RatingNodeProperties(schemes, scores, dates);
    }
    
    /**
     * This method returns the number of ratings currently held by the multivalued properties.
     * @return the number of ratings.
     */
    public int size()
    {
        return this.schemes.size();
    }
    
    /**
     * This method gets the {@link Rating} for the specified index.
     * @param index
     * @return
     */
    public RatingStruct getRatingAt(int index)
    {
        String scheme = this.schemes.get(index);
        int score = this.scores.get(index);
        Date d = this.dates.get(index);
        return new RatingStruct(scheme, score, d);
    }
    
    /**
     * This method appends a new rating in the specified schemeName. The ratedAt date
     * will be set to 'now'.
     * 
     * @param schemeName the scheme name.
     * @param score the score.
     */
    public void appendRating(String schemeName, int score)
    {
        this.schemes.add(schemeName);
        this.scores.add(score);
        this.dates.add(new Date());
    }

    /**
     * This method sets the rating at the specified index.
     * Note that to persist these changes, {@link RatingNodeProperties#toNodeProperties()}
     * can be called to retrieve a property map which should then be saved via the
     * {@link NodeService} in the usual way.
     * <p/>
     * The ratedAt property will be automatically set to 'now'.
     * @param index the index at which the change is to be made.
     * @param scheme the new rating scheme name.
     * @param score the new rating score.
     */
    public void setRatingAt(int index, String scheme, int score)
    {
        this.schemes.set(index, scheme);
        this.scores.set(index, score);
        this.dates.set(index, new Date());
    }
    
    /**
     * This method removes the rating at the specified index.
     * @param index
     * @return the removed rating data.
     */
    public RatingStruct removeRatingAt(int index)
    {
        String removedScheme = this.schemes.remove(index);
        int removedScore = this.scores.remove(index);
        Date removedDate = this.dates.remove(index);
        
        return new RatingStruct(removedScheme, removedScore, removedDate);
    }
    
    /**
     * This method returns all ratings as a List of {@link RatingStruct} objects.
     * @return
     */
    public List<RatingStruct> getAllRatings()
    {
        List<RatingStruct> result = new ArrayList<RatingStruct>(schemes.size());
        for (int i = 0; i < schemes.size(); i++)
        {
            result.add(new RatingStruct(schemes.get(i), scores.get(i), dates.get(i)));
        }
        return result;
    }

    /**
     * This method gets the rating which has the specified schemeName. There should only
     * be one such rating.
     * @param schemeName
     * @return the requested {@link RatingStruct} if there is one, else <code>null</code>.
     */
    public RatingStruct getRating(String schemeName)
    {
        for (int i = 0; i < schemes.size(); i ++)
        {
            if (schemes.get(i).equals(schemeName))
            {
                return new RatingStruct(schemes.get(i), scores.get(i), dates.get(i));
            }
        }
        return null;
    }
    
    /**
     * This method returns the index of the rating with the specified scheme name, if there
     * is such a rating.
     * @param schemeName
     * @return the index of the specified rating if there is one, else -1.
     */
    public int getIndexOfRating(String schemeName)
    {
        for (int i = 0; i < schemes.size(); i ++)
        {
            if (schemes.get(i).equals(schemeName))
            {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * This method converts this {@link RatingNodeProperties} object into a Map of
     * properties consistent with the Alfresco ratings content model. See contentModel.xml.
     * These can then be set in the database via the {@link NodeService} in the usual way.
     * @return
     */
    public Map<QName, Serializable> toNodeProperties()
    {
        Map<QName, Serializable> results = new HashMap<QName, Serializable>();
        results.put(ContentModel.PROP_RATING_SCHEME, this.schemes);
        results.put(ContentModel.PROP_RATING_SCORE,  this.scores);
        results.put(ContentModel.PROP_RATED_AT,      this.dates);
        
        return results;
    }
    
    /**
     * A simple struct class to help in handling the related properties within a cm:rating.
     * @author Neil Mc Erlean.
     *
     */
    public class RatingStruct
    {
        private String scheme;
        private int score;
        private Date date;
        
        public RatingStruct(String scheme, int score, Date d)
        {
            RatingStruct.this.scheme = scheme;
            RatingStruct.this.score = score;
            RatingStruct.this.date = d;
        }

        public String getScheme()
        {
            return scheme;
        }

        public int getScore()
        {
            return score;
        }

        public Date getDate()
        {
            return date;
        }

        @Override
        public boolean equals(Object thatObj)
        {
            if (thatObj == null || thatObj.getClass().equals(RatingStruct.this.getClass()) == false)
            {
                return false;
            }
            RatingStruct that = (RatingStruct)thatObj;
            return RatingStruct.this.scheme.equals(that.scheme) &&
                   RatingStruct.this.score == that.score &&
                   RatingStruct.this.date.equals(that.date);
        }

        @Override
        public int hashCode()
        {
            return scheme.hashCode() + 7 * score + 11 * date.hashCode();
        }

        @Override
        public String toString()
        {
            StringBuilder msg = new StringBuilder();
            msg.append(RatingStruct.this.getClass().getSimpleName())
               .append(" '").append(scheme).append("' ")
               .append(score).append(" ").append(date);
            return msg.toString();
        }
}
}
