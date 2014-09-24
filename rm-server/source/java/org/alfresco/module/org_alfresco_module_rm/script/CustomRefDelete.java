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

import static org.alfresco.util.WebScriptUtils.getRequestParameterValue;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to remove RM custom reference instances from a node.
 *
 * @author Neil McErlean
 * @author Tuna Aksoy
 */
public class CustomRefDelete extends AbstractRmWebScript
{
    /** Constants */
    private static final String REF_ID = "refId";
    private static final String ST = "st";
    private static final String SI = "si";
    private static final String ID = "id";

    /** RM admin service */
    private RecordsManagementAdminService rmAdminService;

    /** Rule service */
    private RuleService ruleService;

    /**
     * Sets the RM admin service
     *
     * @param rmAdminService RM admin service
     */
    public void setRecordsManagementAdminService(RecordsManagementAdminService rmAdminService)
    {
        this.rmAdminService = rmAdminService;
    }

    /**
     * Sets the rule service
     *
     * @param ruleService Rule service
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }

    /**
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>(1);

        try
        {
            ruleService.disableRuleType(RuleType.OUTBOUND);
            removeCustomReferenceInstance(req);
            model.put(SUCCESS, true);
        }
        finally
        {
            ruleService.enableRuleType(RuleType.OUTBOUND);
        }

        return model;
    }

    /**
     * Removes custom reference instance
     *
     * @param req The webscript request
     */
    private void removeCustomReferenceInstance(WebScriptRequest req)
    {
        NodeRef fromNode = parseRequestForNodeRef(req);
        NodeRef toNodeRef = getToNode(req);
        QName associationQName = getAssociationQName(req);

        rmAdminService.removeCustomReference(fromNode, toNodeRef, associationQName);
        rmAdminService.removeCustomReference(toNodeRef, fromNode, associationQName);
    }

    /**
     * Gets the node from which the reference will be removed
     *
     * @param req The webscript request
     * @return The node from which the reference will be removed
     */
    private NodeRef getToNode(WebScriptRequest req)
    {
        // Get the toNode from the URL query string.
        String storeType = req.getParameter(ST);
        String storeId = req.getParameter(SI);
        String nodeId = req.getParameter(ID);

        // Create the NodeRef and ensure it is valid
        NodeRef toNode = new NodeRef(storeType, storeId, nodeId);
        if (!nodeService.exists(toNode))
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Unable to find toNode: '" +
                    toNode.toString() + "'.");
        }

        return toNode;
    }

    /**
     * Gets the QName of the association
     *
     * @param req The webscript request
     * @return QName of the association
     */
    private QName getAssociationQName(WebScriptRequest req)
    {
        String clientsRefId = getRequestParameterValue(req, REF_ID);
        QName qName = rmAdminService.getQNameForClientId(clientsRefId);

        if (qName == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND,
                    "Unable to find reference type: '" + clientsRefId + "'.");
        }

        return qName;
    }
}