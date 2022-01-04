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

import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to remove RM custom relationship from a node.
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
     * Returns the rule service instance
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
            getRuleService().disableRuleType(RuleType.OUTBOUND);
            removeCustomRelationship(req);
            model.put(SUCCESS, true);
        }
        finally
        {
            getRuleService().enableRuleType(RuleType.OUTBOUND);
        }
        return model;
    }

    /**
     * Removes a custom relationship
     *
     * @param req The webscript request
     */
    private void removeCustomRelationship(WebScriptRequest req)
    {
        String uniqueName = getRequestParameterValue(req, REF_ID);
        NodeRef source = parseRequestForNodeRef(req);
        NodeRef target = getTargetNode(req);

        getRelationshipService().removeRelationship(uniqueName, source, target);
        getRelationshipService().removeRelationship(uniqueName, target, source);
    }

    /**
     * Gets the target node
     *
     * @param req The webscript request
     * @return The target node
     */
    private NodeRef getTargetNode(WebScriptRequest req)
    {
        String storeType = req.getParameter(ST);
        String storeId = req.getParameter(SI);
        String nodeId = req.getParameter(ID);

        NodeRef targetNode = new NodeRef(storeType, storeId, nodeId);
        if (!getNodeService().exists(targetNode))
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Unable to find the target node: '" +
                    targetNode.toString() + "'.");
        }

        return targetNode;
    }
}
