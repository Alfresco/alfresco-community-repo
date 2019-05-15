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