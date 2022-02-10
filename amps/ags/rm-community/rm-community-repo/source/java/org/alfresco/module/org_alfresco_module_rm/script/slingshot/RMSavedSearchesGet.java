/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.script.slingshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchService;
import org.alfresco.module.org_alfresco_module_rm.search.SavedSearchDetails;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * RM saved searches GET web script
 *
 * @author Roy Wetherall
 */
public class RMSavedSearchesGet extends DeclarativeWebScript
{
    /** Records management search service */
    protected RecordsManagementSearchService recordsManagementSearchService;

    /** Site service */
    protected SiteService siteService;

    /**
     * @param recordsManagementSearchService    records management search service
     */
    public void setRecordsManagementSearchService(RecordsManagementSearchService recordsManagementSearchService)
    {
        this.recordsManagementSearchService = recordsManagementSearchService;
    }

    /**
     * @param siteService   site service
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    /*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // create model object with the lists model
        Map<String, Object> model = new HashMap<>(13);

        // Get the site id and confirm it is valid
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String siteId = templateVars.get("site");
        if (siteId == null || siteId.length() == 0)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Site id not provided.");
        }
        if (siteService.getSite(siteId) == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Site not found.");
        }

        // Get the saved search details
        List<SavedSearchDetails> details = recordsManagementSearchService.getSavedSearches(siteId);
        List<Item> items  = new ArrayList<>();
        for (SavedSearchDetails savedSearchDetails : details)
        {
            String name = savedSearchDetails.getName();
            String description = savedSearchDetails.getDescription();
            String query = savedSearchDetails.getCompatibility().getQuery();
            String params = savedSearchDetails.getCompatibility().getParams();
            String sort = savedSearchDetails.getCompatibility().getSort();

            Item item = new Item(name, description, query, params, sort);
            items.add(item);
        }

        model.put("savedSearches", items);
        return model;
    }

    /**
     * Item class to contain information about items being placed in model.
     */
    public class Item
    {
        private String name;
        private String description;
        private String query;
        private String params;
        private String sort;

        public Item(String name, String description, String query, String params, String sort)
        {
            this.name = name;
            this.description = description;
            this.query = query;
            this.params = params;
            this.sort = sort;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public String getQuery()
        {
            return query;
        }

        public String getParams()
        {
            return params;
        }

        public String getSort()
        {
            return sort;
        }
    }
}
