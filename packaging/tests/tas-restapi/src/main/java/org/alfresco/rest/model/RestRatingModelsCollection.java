/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.model;

import static org.alfresco.utility.report.log.Step.STEP;

import java.util.List;

import org.alfresco.rest.core.RestModels;
import org.testng.Assert;

/**
 * Handle collection of <RestRatingModel>
{
  "list": {
    "pagination": {
      "count": 0,
      "hasMoreItems": true,
      "totalItems": 0,
      "skipCount": 0,
      "maxItems": 0
    },
    "entries": [
      {
        "entry": {
          "id": "string",
          "aggregate": {
            "numberOfRatings": 0,
            "average": 0
          },
          "ratedAt": "2016-09-28T13:56:58.931Z",
          "myRating": "string"
        }
      }
    ]
  }
}
 */
public class RestRatingModelsCollection extends RestModels<RestRatingModel, RestRatingModelsCollection>
{
    public RestRatingModelsCollection assertNodeIsLiked()
    {
        STEP("REST API: Assert that document is liked");
        Assert.assertTrue(getNumberOfRatingsFor("likes") > 0, "Node should have like ratings");

        return this;
    }

    public RestRatingModelsCollection assertNodeIsNotLiked()
    {
        STEP("REST API: Assert that document is not liked");
        Assert.assertTrue(getNumberOfRatingsFor("likes") == 0, "Node should have no like ratings");

        return this;
    }

    public RestRatingModelsCollection assertNodeHasFiveStarRating()
    {
        STEP("REST API: Assert that document has five star rating");
        Assert.assertTrue(getNumberOfRatingsFor("fiveStar") > 0, "Node should have five star ratings");

        return this;
    }

    public RestRatingModelsCollection assertNodeHasNoFiveStarRating()
    {
        STEP("REST API: Assert that document has no five star rating");
        Assert.assertTrue(getNumberOfRatingsFor("fiveStar") == 0, "Node should have no five star ratings");
       
        return this;
    }
    
    /**
     * Default, the rating value can be: "likes" or "fiveStar"
     * @param ratingValue
     * @return
     */
    public int getNumberOfRatingsFor(String ratingValue)
    {
        List<RestRatingModel> ratings = getEntries();
        int noOfRatings = 0;
        for (int i = 0; i < ratings.size(); i++)
        {
            if (ratings.get(i).onModel().getId().equals(ratingValue))
            {
                noOfRatings = ratings.get(i).onModel().getAggregate().getNumberOfRatings();
            }
        }

        return noOfRatings;
    }
}    
