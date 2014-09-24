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

import static org.alfresco.util.WebScriptUtils.getTemplateVars;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
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
    protected static final String SUCCESS = "success";

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
}