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
package org.alfresco.repo.web.scripts.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.repo.search.SearchTrackingComponent;
import org.alfresco.repo.solr.Acl;

/**
 * Support for SOLR: Track ACLs
 *
 * @since 4.0
 */
public class AclsGet extends DeclarativeWebScript
{
    protected static final Log logger = LogFactory.getLog(AclsGet.class);

    private SearchTrackingComponent searchTrackingComponent;

    public void setSearchTrackingComponent(SearchTrackingComponent searchTrackingComponent)
    {
        this.searchTrackingComponent = searchTrackingComponent;
    }

    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        try
        {
            Map<String, Object> model = buildModel(req);
            if (logger.isDebugEnabled())
            {
                logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
            }
            return model;
        }
        catch (IOException e)
        {
            throw new WebScriptException("IO exception parsing request", e);
        }
        catch (JSONException e)
        {
            throw new WebScriptException("Invalid JSON", e);
        }
    }

    private Map<String, Object> buildModel(WebScriptRequest req) throws JSONException, IOException
    {
        List<Long> aclChangeSetIds = null;

        Content content = req.getContent();
        if (content == null)
        {
            throw new WebScriptException("Request content is empty");
        }
        JSONObject o = new JSONObject(content.getContent());
        JSONArray aclChangeSetIdsJSON = o.has("aclChangeSetIds") ? o.getJSONArray("aclChangeSetIds") : null;
        if (aclChangeSetIdsJSON == null)
        {
            throw new WebScriptException(
                    Status.STATUS_BAD_REQUEST,
                    "Parameter 'aclChangeSetIds' not provided in request content.");
        }
        else if (aclChangeSetIdsJSON.length() == 0)
        {
            throw new WebScriptException(
                    Status.STATUS_BAD_REQUEST,
                    "Parameter 'aclChangeSetIds' must hold from 1 or more IDs.");
        }
        aclChangeSetIds = new ArrayList<Long>(aclChangeSetIdsJSON.length());
        for (int i = 0; i < aclChangeSetIdsJSON.length(); i++)
        {
            aclChangeSetIds.add(aclChangeSetIdsJSON.getLong(i));
        }

        String fromIdParam = req.getParameter("fromId");
        String maxResultsParam = req.getParameter("maxResults");

        Long fromId = (fromIdParam == null ? null : Long.valueOf(fromIdParam));
        int maxResults = (maxResultsParam == null ? 1024 : Integer.valueOf(maxResultsParam));

        // Request according to the paging query style required
        List<Acl> acls = searchTrackingComponent.getAcls(aclChangeSetIds, fromId, maxResults);

        Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
        model.put("acls", acls);

        if (logger.isDebugEnabled())
        {
            logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
        }

        return model;
    }
}
