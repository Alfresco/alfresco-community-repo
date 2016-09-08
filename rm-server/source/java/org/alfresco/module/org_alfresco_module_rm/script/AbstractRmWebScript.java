/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * 
 * @author Neil McErlean
 */
public abstract class AbstractRmWebScript extends DeclarativeWebScript
{
    protected NodeService nodeService;
    protected RecordsManagementService rmService;
    protected DispositionService dispositionService;
    protected NamespaceService namespaceService;

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
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String storeType = templateVars.get("store_type");
        String storeId = templateVars.get("store_id");
        String nodeId = templateVars.get("id");
        
        // create the NodeRef and ensure it is valid
        StoreRef storeRef = new StoreRef(storeType, storeId);
        NodeRef nodeRef = new NodeRef(storeRef, nodeId);
        
        if (!this.nodeService.exists(nodeRef))
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find node: " + 
                        nodeRef.toString());
        }
        
        return nodeRef;
    }

    /**
     * Sets the RecordsManagementService instance
     * 
     * @param rmService The RecordsManagementService instance
     */
    public void setRecordsManagementService(RecordsManagementService rmService)
    {
        this.rmService = rmService;
    }

    /**
     * @param dispositionService    the disposition serviceS
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }
    
    /**
     * Sets the NodeService instance
     * 
     * @param nodeService The NodeService instance
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Sets the NamespaceService instance
     * 
     * @param namespaceService The NamespaceService instance
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
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
        if (json.has(paramName) == false)
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
            this.checkMandatoryJsonParam(json, name);
        }
    }
}