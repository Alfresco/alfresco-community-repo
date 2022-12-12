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
import org.alfresco.utility.model.SiteModel;

/**
 * Handle collection of <SiteModel>
 * Example:
{
        "list": {
                "pagination": {
                        "count": 100,
                        "hasMoreItems": true,
                        "totalItems": 269,
                        "skipCount": 0,
                        "maxItems": 100
                },
                "entries": [{
                        "entry": {
                                "visibility": "PUBLIC",
                                "guid": "79e140e1-5039-4efa-acaf-c22b5ba7c947",
                                "description": "Description1470255221170",
                                "id": "0-C2291-1470255221170",
                                "title": "0-C2291-1470255221170"
                        }
                },
                ]
}
 *
 */
public class RestSiteModelsCollection extends RestModels<RestSiteModel, RestSiteModelsCollection>
{
    
    /**
     * Get site from sites list
     * 
     * @param siteId
     * @return the site model or null if a site with this id wasn't found
     */
    public RestSiteModel getSite(String siteId)
    {
        STEP(String.format("REST API: Get site with '%s' id", siteId));
        List<RestSiteModel> sites = getEntries();

        for (RestSiteModel site : sites)
        {
            if (site.onModel().getId().equals(siteId))
            {
                return site.onModel();
            }
        }

        return null;
    }
    
    /**
     * Get site from sites list
     * 
     * @param siteId
     * @return
     */
    public RestSiteModel getSite(SiteModel siteModel)
    {    	
    	return getSite(siteModel.getId());
    }
}
