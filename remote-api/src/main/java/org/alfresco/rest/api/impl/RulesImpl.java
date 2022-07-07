/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.api.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.Rules;
import org.alfresco.rest.api.model.Rule;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.ListPage;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Experimental
public class RulesImpl implements Rules
{

    private static final String DEFAULT_RULE_SET_ID = "-default-";

    private Nodes nodes;

    private PermissionService permissionService;

    private RuleService ruleService;

    @Override
    public CollectionWithPagingInfo<Rule> getRules(final String folderNodeId, final String ruleSetId, final Paging paging)
    {
        final NodeRef folderNodeRef = validateNode(folderNodeId, ContentModel.TYPE_FOLDER);

        if (isNotDefaultId(ruleSetId)) {
            final NodeRef ruleSetNodeRef = validateNode(ruleSetId, ContentModel.TYPE_SYSTEM_FOLDER, "rule set");

            if (!ruleService.isRuleSetAssociatedWithFolder(ruleSetNodeRef, folderNodeRef)) {
                throw new InvalidArgumentException("Rule set is not associated with folder node!");
            }
        }

        final List<Rule> rules = ruleService.getRules(folderNodeRef).stream()
            .map(Rule::from)
            .collect(Collectors.toList());

        return ListPage.of(rules, paging);
    }

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }

    private NodeRef validateNode(final String nodeId, final QName namespaceType)
    {
        return validateNode(nodeId, namespaceType, null);
    }

    /**
     * Validates if node exists, user have permission to read from it and is of a given type.
     *
     * @param nodeId - node ID
     * @param expectedType - expected type
     * @param expectedTypeName - expected type local name
     * @return node reference
     */
    private NodeRef validateNode(final String nodeId, final QName expectedType, final String expectedTypeName)
    {
        final NodeRef nodeRef = nodes.validateNode(nodeId);

        if (permissionService.hasReadPermission(nodeRef) != AccessStatus.ALLOWED) {
            throw new PermissionDeniedException("Cannot read from this node!");
        }

        final Set<QName> expectedTypes = Set.of(expectedType);
        if (!nodes.nodeMatches(nodeRef, expectedTypes, null)) {
            final String expectedTypeLocalName = (expectedTypeName != null)? expectedTypeName : expectedType.getLocalName();
            throw new InvalidArgumentException(String.format("NodeId of a %s is expected!", expectedTypeLocalName));
        }

        return nodeRef;
    }

    private static boolean isNotDefaultId(final String ruleSetId) {
        return !DEFAULT_RULE_SET_ID.equals(ruleSetId);
    }
}
