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

import static org.alfresco.util.WebScriptUtils.getRequestContentAsJSONObject;
import static org.alfresco.util.WebScriptUtils.getStringValueFromJSONObject;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to add RM custom relationship to a node.
 *
 * @author Neil McErlean
 * @author Tuna Aksoy
 */
public class CustomRefPost extends AbstractRmWebScript
{
    /** Constants */
    private static final String TO_NODE = "toNode";
    private static final String REF_ID = "refId";

    /** Relationship service */
    private RelationshipService relationshipService;

    /** Rule service */
    private RuleService ruleService;

    /**
     * Gets the relationship service instance
     *
     * @return The relationship service instance
     */
    protected RelationshipService getRelationshipService()
    {
        return this.relationshipService;
    }

    /**
     * Sets the relationship service instance
     *
     * @param relationshipService The relationship service instance
     */
    public void setRelationshipService(RelationshipService relationshipService)
    {
        this.relationshipService = relationshipService;
    }

    /**
     * Gets the rule service instance
     *
     * @return The rule service instance
     */
    protected RuleService getRuleService()
    {
        return this.ruleService;
    }

    /**
     * Sets the rule service instance
     *
     * @param ruleService The rule service instance
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest,
     *      org.springframework.extensions.webscripts.Status,
     *      org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<>(1);
        try
        {
            getRuleService().disableRuleType(RuleType.INBOUND);
            addCustomRelationship(req);
            model.put(SUCCESS, true);
        }
        finally
        {
            getRuleService().enableRuleType(RuleType.INBOUND);
        }
        return model;
    }

    /**
     * Adds a custom relationship
     *
     * @param req The webscript request
     */
    protected void addCustomRelationship(WebScriptRequest req)
    {
        JSONObject json = getRequestContentAsJSONObject(req);
        String uniqueName = getStringValueFromJSONObject(json, REF_ID);
        NodeRef target = getTargetNode(json);
        NodeRef source = parseRequestForNodeRef(req);

        if (uniqueName.endsWith(INVERT))
        {
            String uniqueNameStem = uniqueName.split(INVERT)[0];
            getRelationshipService().addRelationship(uniqueNameStem, target, source);
        }
        else
        {
            getRelationshipService().addRelationship(uniqueName, source, target);
        }
    }

    /**
     * Gets the target node
     *
     * @param json Request content as json object
     * @return The target node
     */
    private NodeRef getTargetNode(JSONObject json)
    {
        String targetNodeString = getStringValueFromJSONObject(json, TO_NODE);
        NodeRef targetNode = new NodeRef(targetNodeString);

        if (!getNodeService().exists(targetNode))
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Unable to find the target node: '" +
                    targetNode.toString() + "'.");
        }

        return targetNode;
    }
}
