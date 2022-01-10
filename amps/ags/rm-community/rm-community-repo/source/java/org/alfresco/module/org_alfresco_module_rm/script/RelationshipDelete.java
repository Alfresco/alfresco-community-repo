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

import static org.alfresco.util.WebScriptUtils.getRequestParameterValue;
import static org.alfresco.util.WebScriptUtils.getTemplateVars;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to delete a relationship from a node.
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RelationshipDelete extends AbstractRmWebScript
{
    /** Constants */
    private static final String STORE_TYPE = "target_store_type";
    private static final String STORE_ID = "target_store_id";
    private static final String ID = "target_id";
    private static final String UNIQUE_NAME = "uniqueName";

    /** Relationship service */
    private RelationshipService relationshipService;

    /**
     * Gets the relationship service
     *
     * @return The relationship service
     */
    protected RelationshipService getRelationshipService()
    {
        return this.relationshipService;
    }

    /**
     * Sets the relationship service
     *
     * @param relationshipService The relationship service
     */
    public void setRelationshipService(RelationshipService relationshipService)
    {
        this.relationshipService = relationshipService;
    }

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest,
     *      org.springframework.extensions.webscripts.Status,
     *      org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        String uniqueName = getRequestParameterValue(req, UNIQUE_NAME);
        NodeRef source = parseRequestForNodeRef(req);
        NodeRef target = parseRequestForTargetNodeRef(req);

        getRelationshipService().removeRelationship(uniqueName, source, target);
        getRelationshipService().removeRelationship(uniqueName, target, source);

        Map<String, Object> model = new HashMap<>(1);
        model.put(SUCCESS, true);
        return model;
    }

    /**
     * Gets the node reference of target
     *
     * @param req The webscript request
     * @return The node reference of the target
     */
    private NodeRef parseRequestForTargetNodeRef(WebScriptRequest req)
    {
        Map<String, String> templateVars = getTemplateVars(req);
        String storeType = templateVars.get(STORE_TYPE);
        String storeId = templateVars.get(STORE_ID);
        String nodeId = templateVars.get(ID);

        NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);

        if (!getNodeService().exists(nodeRef))
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find node: '" +
                        nodeRef.toString() + "'.");
        }

        return nodeRef;
    }
}
