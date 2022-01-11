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

package org.alfresco.module.org_alfresco_module_rm.script;

import static org.alfresco.util.WebScriptUtils.getTemplateVars;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Abstract base class for all RM webscript classes.
 * Includes utility methods for processing the webscript request.
 *
 * @author Neil McErlean
 * @author Tuna Aksoy
 * @author Gavin Cornwell
 */
public abstract class AbstractRmWebScript extends DeclarativeWebScript
{
    /** Constants */
    protected static final String PATH_SEPARATOR = "/";
    protected static final String STORE_TYPE = "store_type";
    protected static final String STORE_ID = "store_id";
    protected static final String ID = "id";
    protected static final String SUCCESS = "success";
    protected static final String INVERT = "__invert";

    /** Disposition service */
    private DispositionService dispositionService;

    /** Namespace service */
    private NamespaceService namespaceService;

    /** Node service */
    private NodeService nodeService;

    /**
     * Gets the disposition service instance
     *
     * @return The disposition service instance
     */
    protected DispositionService getDispositionService()
    {
        return this.dispositionService;
    }

    /**
     * Sets the disposition service instance
     *
     * @param dispositionService The disposition service instance
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    /**
     * Gets the namespace service instance
     *
     * @return The namespace service instance
     */
    protected NamespaceService getNamespaceService()
    {
        return this.namespaceService;
    }

    /**
     * Sets the namespace service instance
     *
     * @param namespaceService The namespace service instance
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * Gets the node service instance
     *
     * @return The node service instance
     */
    protected NodeService getNodeService()
    {
        return this.nodeService;
    }

    /**
     * Sets the node service instance
     *
     * @param nodeService The node service instance
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
     */
    protected NodeRef parseRequestForNodeRef(WebScriptRequest req)
    {
        // get the parameters that represent the NodeRef, we know they are present
        // otherwise this webscript would not have matched
        Map<String, String> templateVars = getTemplateVars(req);
        String storeType = templateVars.get(STORE_TYPE);
        String storeId = templateVars.get(STORE_ID);
        String nodeId = templateVars.get(ID);

        // create the NodeRef and ensure it is valid
        NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);

        if (!getNodeService().exists(nodeRef))
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find node: '" +
                        nodeRef.toString() + "'.");
        }

        return nodeRef;
    }
}
