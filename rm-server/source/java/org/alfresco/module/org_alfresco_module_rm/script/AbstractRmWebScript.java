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
package org.alfresco.module.org_alfresco_module_rm.script;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Abstract base class for all RM webscript classes.
 * Includes util methods for processing the webscript request.
 *
 * @author Neil McErlean
 * @author Tuna Aksoy
 */
public abstract class AbstractRmWebScript extends DeclarativeWebScript
{
    /** Constants */
    protected static final String PATH_SEPARATOR = "/";

    /** Disposition service */
    protected DispositionService dispositionService;

    /** Namespace service */
    protected NamespaceService namespaceService;

    /** Node service */
    protected NodeService nodeService;

    /**
     * Sets the disposition service
     *
     * @param dispositionService The disposition serviceS
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    /**
     * Sets the namespace service
     *
     * @param namespaceService The namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * Sets the node service
     *
     * @param nodeService The node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Parses the request and providing it's valid returns the NodeRef.
     *
     * @param req The webscript request
     * @return The NodeRef passed in the request
     *
     * @author Gavin Cornwell
     */
    protected NodeRef parseRequestForNodeRef(WebScriptRequest req)
    {
        // get the parameters that represent the NodeRef, we know they are present
        // otherwise this webscript would not have matched
        Map<String, String> templateVars = getTemplateVars(req);
        String storeType = templateVars.get("store_type");
        String storeId = templateVars.get("store_id");
        String nodeId = templateVars.get("id");

        // create the NodeRef and ensure it is valid
        NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);

        if (!nodeService.exists(nodeRef))
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find node: '" +
                        nodeRef.toString() + "'.");
        }

        return nodeRef;
    }

    /**
     * This method checks if the json object contains an entry with the specified name.
     *
     * @param json the json object.
     * @param paramName the name to check for.
     * @throws WebScriptException if the specified entry is missing.
     */
    protected void checkMandatoryJsonParam(JSONObject json, String paramName)
    {
        if (!json.has(paramName))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Mandatory '" + paramName + "' parameter was not provided in request body");
        }
    }

    /**
     * This method checks if the json object contains entries with the specified names.
     *
     * @param json the json object.
     * @param paramNames the names to check for.
     * @throws WebScriptException if any of the specified entries are missing.
     */
    protected void checkMandatoryJsonParams(JSONObject json, List<String> paramNames)
    {
        for (String name : paramNames)
        {
            checkMandatoryJsonParam(json, name);
        }
    }

    /**
     * Gets the template variable substitutions map
     *
     * @param req The webscript request
     * @return The template variable substitutions
     */
    protected Map<String, String> getTemplateVars(WebScriptRequest req)
    {
        if (req == null)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "The webscript request is null.");
        }

        if (req.getServiceMatch() == null)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "The matching API Service for the request is null.");
        }

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        if (templateVars == null)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "The template variable substitutions map is null");
        }

        return templateVars;
    }

    /**
     * Gets the value of a request parameter
     *
     * @param req The webscript request
     * @param parameter The request parameter
     * @return The value of the request parameter
     */
    protected String getRequestParameterValue(WebScriptRequest req, String parameter)
    {
        Map<String, String> templateVars = getTemplateVars(req);
        return templateVars.get(parameter);
    }

    /**
     * Gets the request content as JSON object
     *
     * @param req The webscript request
     * @return The request content as JSON object
     */
    protected JSONObject getRequestContentAsJsonObject(WebScriptRequest req)
    {
        Content reqContent = req.getContent();
        if (reqContent == null)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Missing request body.");
        }

        String content;
        try
        {
            content = reqContent.getContent();
        }
        catch (IOException error)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not get conent from the request.", error);
        }

        if (StringUtils.isBlank(content))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Content does not exist.");
        }

        JSONTokener jsonTokener;
        try
        {
            jsonTokener = new JSONTokener(req.getContent().getContent());
        }
        catch (IOException error)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not get content.", error);
        }

        JSONObject json;
        try
        {
            json = new JSONObject(jsonTokener);
        }
        catch (JSONException error)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Unable to parse request body.", error);
        }

        return json;
    }

    /**
     * Gets the value of a given key from a json object
     *
     * @param jsonObject The json object from which the value should be retrieved
     * @param key The key for which the value is requested
     * @return The value of the given key from the json object
     */
    protected Serializable getJSONObjectValue(JSONObject jsonObject, String key)
    {
        Serializable value;

        try
        {
            checkMandatoryJsonParam(jsonObject, key);
            value = (Serializable) jsonObject.get(key);
        }
        catch (JSONException error)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not get value for the key '" + key + "'.", error);
        }

        return value;
    }
}