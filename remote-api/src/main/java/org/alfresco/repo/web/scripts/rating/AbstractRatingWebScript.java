/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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

import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import org.alfresco.service.cmr.rating.RatingService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is an abstract base class for the various webscript controllers in the
 * RatingService.
 * 
 * @author Neil McErlean
 * @since 3.4
 */
public abstract class AbstractRatingWebScript extends DeclarativeWebScript
{
    // Web script parameters.
    protected static final String RATING_SCHEME = "ratingScheme";
    protected static final String RATING = "rating";
    protected static final String RATED_NODE = "ratedNode";
    protected static final String NODE_REF = "nodeRef";
    protected static final String RATINGS = "ratings";
    
    protected static final String AVERAGE_RATINGS = "averageRatings";
    protected static final String RATINGS_TOTALS = "ratingsTotals";
    protected static final String RATINGS_COUNTS = "ratingsCounts";

    // Injected services
    protected NodeService nodeService;
    protected RatingService ratingService;

    /**
     * Sets the node service instance
     * 
     * @param nodeService the node service to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Sets the rating service instance
     * 
     * @param ratingService the rating service to set
     */
    public void setRatingService(RatingService ratingService)
    {
        this.ratingService = ratingService;
    }

    protected NodeRef parseRequestForNodeRef(WebScriptRequest req)
    {
        // get the parameters that represent the NodeRef, we know they are present
        // otherwise this webscript would not have matched
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String storeType = templateVars.get("store_type");
        String storeId = templateVars.get("store_id");
        String nodeId = templateVars.get("id");

        // create the NodeRef and ensure it is valid
        StoreRef storeRef = new StoreRef(storeType, storeId);
        NodeRef nodeRef = new NodeRef(storeRef, nodeId);

        if (!this.nodeService.exists(nodeRef))
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find node: " + nodeRef.toString());
        }

        return nodeRef;
    }
}
