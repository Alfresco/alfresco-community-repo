/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.script.slingshot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchParameters;
import org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchService;
import org.alfresco.module.org_alfresco_module_rm.search.SavedSearchDetailsCompatibility;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Records management saved search POST web script.
 *
 * @author Roy Wetherall
 */
public class RMSavedSearchesPost extends DeclarativeWebScript
{
    /** Records management search service */
    protected RecordsManagementSearchService recordsManagementSearchService;

    /** Site service */
    protected SiteService siteService;

    /** Namespace service */
    protected NamespaceService namespaceService;

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

    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
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

        // Example format of posted Saved Search JSON:
        // {
        //    "name": "search name",
        //    "description": "the search description",
        //    "query": "the complete search query string",
        //    "public": boolean,
        //    "params": "terms=keywords:xyz&undeclared=true",
        //    "sort": "cm:name/asc"
        //}

        try
        {
            // Parse the JSON passed in the request
            JSONObject json = new JSONObject(new JSONTokener(req.getContent().getContent()));

            // Get the details of the saved search
            if (!json.has("name"))
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                                             "Mandatory 'name' parameter was not provided in request body");
            }
            String name = json.getString("name");
            String description = null;
            if (json.has("description"))
            {
                description = json.getString("description");
            }
            boolean isPublic = true;
            if (json.has("public"))
            {
                isPublic = json.getBoolean("public");
            }
            // NOTE: we do not need to worry about the query
            if (!json.has("params"))
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                                             "Mandatory 'params' parameter was not provided in request body");
            }
            String params = json.getString("params");
            String sort = null;
            if (json.has("sort"))
            {
                sort = json.getString("sort");
            }

            // Use the compatibility class to create a saved search details and save
            String search = SavedSearchDetailsCompatibility.getSearchFromParams(params);
            if (search == null)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                "Mandatory 'terms' was not provided in 'params' parameter found in the request body");
            }
            RecordsManagementSearchParameters searchParameters = SavedSearchDetailsCompatibility.createSearchParameters(params, sort, namespaceService);
            recordsManagementSearchService.saveSearch(siteId, name, description, search, searchParameters, isPublic);

        }
        catch (IOException iox)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Could not read content from req.", iox);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "Could not parse JSON from req.", je);
        }

        // Indicate success in the model
        Map<String, Object> model = new HashMap<String, Object>(1);
        model.put("success", true);
        return model;
    }
}