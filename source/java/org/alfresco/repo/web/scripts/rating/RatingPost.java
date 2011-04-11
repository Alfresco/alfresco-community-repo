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
package org.alfresco.repo.web.scripts.rating;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.rating.RatingScheme;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the rating.post webscript.
 * 
 * @author Neil McErlean
 * @since 3.4
 */
public class RatingPost extends AbstractRatingWebScript
{
    // Url format
    private final static String NODE_RATINGS_URL_FORMAT = "/api/node/{0}/ratings";

    private final static String AVERAGE_RATING = "averageRating";
    private final static String RATINGS_TOTAL = "ratingsTotal";
    private final static String RATINGS_COUNT = "ratingsCount";

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        NodeRef nodeRefToBeRated = parseRequestForNodeRef(req);

        JSONObject json = null;
        try
        {
            // read request json
            json = new JSONObject(new JSONTokener(req.getContent().getContent()));
            
            // Check mandatory parameters.
            if (json.has(RATING) == false)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "rating parameter missing when applying rating");
            }
            if (json.has(RATING_SCHEME) == false)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "schemeName parameter missing when applying rating");
            }
            
            // Check that the scheme name actually exists
            final String schemeName = json.getString(RATING_SCHEME);
            RatingScheme scheme = ratingService.getRatingScheme(schemeName);
            if (scheme == null)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Unknown scheme name: " + schemeName);
            }
            
            // Range checking of the rating score will be done within the RatingService.
            // So we can just apply the rating.
            final float rating = (float)json.getDouble(RATING);
            ratingService.applyRating(nodeRefToBeRated, rating, schemeName);

            // We'll return the URL to the ratings of the just-rated node.
            String ratedNodeUrlFragment = nodeRefToBeRated.toString().replace("://", "/");
            String ratedNodeUrl = MessageFormat.format(NODE_RATINGS_URL_FORMAT, ratedNodeUrlFragment);

            model.put(RATED_NODE, ratedNodeUrl);
            model.put(RATING, rating);
            model.put(RATING_SCHEME, schemeName);
            model.put(AVERAGE_RATING, ratingService.getAverageRating(nodeRefToBeRated, schemeName));
            model.put(RATINGS_TOTAL, ratingService.getTotalRating(nodeRefToBeRated, schemeName));
            model.put(RATINGS_COUNT, ratingService.getRatingsCount(nodeRefToBeRated, schemeName));
        }
        catch (IOException iox)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not read content from req.", iox);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from req.", je);
        }

        return model;
    }
}
