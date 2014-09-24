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

import static org.alfresco.util.WebScriptUtils.getRequestContentAsJsonObject;
import static org.alfresco.util.WebScriptUtils.getStringValueFromJSONObject;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to add RM custom reference instances to a node.
 *
 * @author Neil McErlean
 * @author Tuna Aksoy
 */
public class CustomRefPost extends AbstractRmWebScript
{
    /** Constants */
    private static final String TO_NODE = "toNode";
    private static final String REF_ID = "refId";

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
            ruleService.disableRuleType(RuleType.INBOUND);
            addCustomReferenceInstance(req);
            model.put(SUCCESS, true);
        }
        finally
        {
            ruleService.enableRuleType(RuleType.INBOUND);
        }

        return model;
    }

    /**
     * Adds a custom reference instance
     *
     * @param req The webscript request
     */
    protected void addCustomReferenceInstance(WebScriptRequest req)
    {
        NodeRef fromNode = parseRequestForNodeRef(req);
        JSONObject json = getRequestContentAsJsonObject(req);
        NodeRef toNode = getToNode(json);
        QName associationQName = getAssociationQName(json);

        rmAdminService.addCustomReference(fromNode, toNode, associationQName);
    }

    /**
     * Gets the node to which the reference will be added
     *
     * @param json Request content as json object
     * @return The node to which the reference will be added
     */
    private NodeRef getToNode(JSONObject json)
    {
        String toNodeString = getStringValueFromJSONObject(json, TO_NODE);
        NodeRef toNode = new NodeRef(toNodeString);

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
     * @param json Request content as json object
     * @return QName of the association
     */
    private QName getAssociationQName(JSONObject json)
    {
        String clientsRefId = getStringValueFromJSONObject(json, REF_ID);
        QName qName = rmAdminService.getQNameForClientId(clientsRefId);

        if (qName == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND,
                    "Unable to find reference type: '" + clientsRefId + "'.");
        }

        return qName;
    }
}