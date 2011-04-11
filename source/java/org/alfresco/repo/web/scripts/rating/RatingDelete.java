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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.cmr.rating.Rating;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the rating.delete web script.
 * 
 * @author Neil McErlean
 * @since 3.4
 */
public class RatingDelete extends AbstractRatingWebScript
{
    private final static String AVERAGE_RATING = "averageRating";
    private final static String RATINGS_TOTAL = "ratingsTotal";
    private final static String RATINGS_COUNT = "ratingsCount";

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        NodeRef nodeRef = parseRequestForNodeRef(req);
        String ratingSchemeName = parseRequestForScheme(req);
        
        Rating deletedRating = ratingService.removeRatingByCurrentUser(nodeRef, ratingSchemeName);
        if (deletedRating == null)
        {
            // There was no rating in the specified scheme to delete.
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Unable to delete non-existent rating: "
                    + ratingSchemeName + " from " + nodeRef.toString());
        }
        
        model.put(NODE_REF, nodeRef.toString());
        model.put(AVERAGE_RATING, ratingService.getAverageRating(nodeRef, ratingSchemeName));
        model.put(RATINGS_TOTAL, ratingService.getTotalRating(nodeRef, ratingSchemeName));
        model.put(RATINGS_COUNT, ratingService.getRatingsCount(nodeRef, ratingSchemeName));
      
        return model;
    }
    
    private String parseRequestForScheme(WebScriptRequest req)
    {
        // We know the 'scheme' URL element is there because if it wasn't
        // the URL would not have matched.
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String scheme = templateVars.get("scheme");

        return scheme;
    }
}
