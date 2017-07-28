/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.web.scripts.rating;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.rating.Rating;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the ratings.get web script.
 * 
 * @author Neil McErlean
 * @since 3.4
 */
public class RatingsGet extends AbstractRatingWebScript
{
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        NodeRef nodeRef = parseRequestForNodeRef(req);
        
        // These are the data for the current user's ratings of this node, if any.
        List<Rating> myRatings = new ArrayList<Rating>();
        
        // These maps hold the average rating, accumulated total of all ratings and
        // the number of ratings applied for this node as a function of rating scheme.
        Map<String, Float> averageRatings = new HashMap<String, Float>();
        Map<String, Float> ratingsTotals = new HashMap<String, Float>();
        Map<String, Integer> ratingsCounts = new HashMap<String, Integer>();

        for (String schemeName : ratingService.getRatingSchemes().keySet())
        {
            final Rating ratingByCurrentUser = ratingService.getRatingByCurrentUser(nodeRef, schemeName);
            if (ratingByCurrentUser != null)
            {
                myRatings.add(ratingByCurrentUser);
            }
            averageRatings.put(schemeName, ratingService.getAverageRating(nodeRef, schemeName));
            ratingsTotals.put(schemeName, ratingService.getTotalRating(nodeRef, schemeName));
            ratingsCounts.put(schemeName, ratingService.getRatingsCount(nodeRef, schemeName));
        }

        model.put(NODE_REF, nodeRef.toString());
        model.put(RATINGS, myRatings);
        model.put(AVERAGE_RATINGS, averageRatings);
        model.put(RATINGS_TOTALS, ratingsTotals);
        model.put(RATINGS_COUNTS, ratingsCounts);
      
        return model;
    }
}
