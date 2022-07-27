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

import static org.alfresco.service.cmr.security.AccessStatus.ALLOWED;
import static org.alfresco.service.cmr.security.PermissionService.CHANGE_PERMISSIONS;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.Rules;
import org.alfresco.rest.api.model.rules.Rule;
import org.alfresco.rest.api.model.rules.RuleSet;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.ListPage;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Experimental
public class RulesImpl implements Rules
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RulesImpl.class);
    private static final String RULE_SET_EXPECTED_TYPE_NAME = "rule set";

    private Nodes nodes;

    private PermissionService permissionService;

    private RuleService ruleService;

    @Override
    public CollectionWithPagingInfo<Rule> getRules(final String folderNodeId, final String ruleSetId, final Paging paging)
    {
        final NodeRef folderNodeRef = validateFolderNode(folderNodeId, false);
        final NodeRef ruleSetNodeRef = validateRuleSetNode(ruleSetId, folderNodeRef);

        final boolean isShared = isRuleSetNotNullAndShared(ruleSetNodeRef);
        final List<Rule> rules = ruleService.getRules(folderNodeRef).stream()
            .map(ruleModel -> Rule.from(ruleModel, isShared))
            .collect(Collectors.toList());

        return ListPage.of(rules, paging);
    }

    @Override
    public Rule getRuleById(final String folderNodeId, final String ruleSetId, final String ruleId)
    {
        final NodeRef folderNodeRef = validateFolderNode(folderNodeId, false);
        final NodeRef ruleSetNodeRef = validateRuleSetNode(ruleSetId, folderNodeRef);
        final NodeRef ruleNodeRef = validateRuleNode(ruleId, ruleSetNodeRef);

        return Rule.from(ruleService.getRule(ruleNodeRef), isRuleSetNotNullAndShared(ruleSetNodeRef));
    }

    @Override
    public List<Rule> createRules(final String folderNodeId, final String ruleSetId, final List<Rule> rules)
    {
        final NodeRef folderNodeRef = validateFolderNode(folderNodeId, true);
        // Don't validate the ruleset node if -default- is passed since we may need to create it.
        final NodeRef ruleSetNodeRef = (RuleSet.isNotDefaultId(ruleSetId)) ? validateRuleSetNode(ruleSetId, folderNodeRef) : null;

        return rules.stream()
                    .map(rule -> rule.toServiceModel(nodes))
                    .map(rule -> ruleService.saveRule(folderNodeRef, rule))
                    .map(rule -> Rule.from(rule, isRuleSetNotNullAndShared(ruleSetNodeRef, folderNodeRef)))
                    .collect(Collectors.toList());
    }

    @Override
    public Rule updateRuleById(String folderNodeId, String ruleSetId, String ruleId, Rule rule)
    {
        LOGGER.debug("Updating rule in folder {}, rule set {}, rule {} to {}", folderNodeId, ruleSetId, ruleId, rule);

        NodeRef folderNodeRef = validateFolderNode(folderNodeId, true);
        NodeRef ruleSetNodeRef = validateRuleSetNode(ruleSetId, folderNodeRef);
        validateRuleNode(ruleId, ruleSetNodeRef);

        boolean shared = isRuleSetNotNullAndShared(ruleSetNodeRef, folderNodeRef);
        return Rule.from(ruleService.saveRule(folderNodeRef, rule.toServiceModel(nodes)), shared);
    }

    @Override
    public void deleteRuleById(String folderNodeId, String ruleSetId, String ruleId)
    {
        final NodeRef folderNodeRef = validateFolderNode(folderNodeId, true);
        final NodeRef ruleSetNodeRef = validateRuleSetNode(ruleSetId, folderNodeRef);
        final NodeRef ruleNodeRef = validateRuleNode(ruleId, ruleSetNodeRef);
        final org.alfresco.service.cmr.rule.Rule rule = ruleService.getRule(ruleNodeRef);
        ruleService.removeRule(folderNodeRef, rule);
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

    /**
     * Validates if folder node exists and the user has permission to use it.
     *
     * @param folderNodeId - folder node ID
     * @param requireChangePermission - Whether to require change permission or just read permission.
     * @return folder node reference
     * @throws InvalidArgumentException if node is not of an expected type
     * @throws PermissionDeniedException if the user doesn't have the appropriate permission for the folder.
     */
    private NodeRef validateFolderNode(final String folderNodeId, boolean requireChangePermission)
    {
        final NodeRef nodeRef = nodes.validateOrLookupNode(folderNodeId, null);
        if (requireChangePermission)
        {
            if (permissionService.hasPermission(nodeRef, CHANGE_PERMISSIONS) != ALLOWED)
            {
                throw new PermissionDeniedException("Insufficient permissions to manage rules.");
            }
        }
        else
        {
            if (permissionService.hasReadPermission(nodeRef) != ALLOWED)
            {
                throw new PermissionDeniedException("Cannot read from this node!");
            }
        }
        verifyNodeType(nodeRef, ContentModel.TYPE_FOLDER, null);

        return nodeRef;
    }

    /**
     * Validates if rule set ID is default, node exists and associated folder node matches.
     *
     * @param ruleSetId - rule set node ID
     * @param associatedFolderNodeRef - folder node ref to check the association
     * @return rule set node reference
     * @throws InvalidArgumentException in case of not matching associated folder node
     */
    private NodeRef validateRuleSetNode(final String ruleSetId, final NodeRef associatedFolderNodeRef)
    {
        if (RuleSet.isDefaultId(ruleSetId))
        {
            return ruleService.getRuleSetNode(associatedFolderNodeRef);
        }

        final NodeRef ruleSetNodeRef = validateNode(ruleSetId, ContentModel.TYPE_SYSTEM_FOLDER, RULE_SET_EXPECTED_TYPE_NAME);
        if (!ruleService.isRuleSetAssociatedWithFolder(ruleSetNodeRef, associatedFolderNodeRef)) {
            throw new InvalidArgumentException("Rule set is not associated with folder node!");
        }

        return ruleSetNodeRef;
    }

    /**
     * Validates if rule node exists and associated rule set node matches.
     *
     * @param ruleId - rule node ID
     * @param associatedRuleSetNodeRef - rule set node ref to check the association. Can be null
     * @return rule node reference
     * @throws InvalidArgumentException in case of not matching associated rule set node
     */
    private NodeRef validateRuleNode(final String ruleId, final NodeRef associatedRuleSetNodeRef)
    {
        final NodeRef ruleNodeRef = validateNode(ruleId, RuleModel.TYPE_RULE, null);
        if (associatedRuleSetNodeRef != null && !ruleService.isRuleAssociatedWithRuleSet(ruleNodeRef, associatedRuleSetNodeRef))
        {
            throw new InvalidArgumentException("Rule is not associated with rule set node!");
        }

        return ruleNodeRef;
    }

    private NodeRef validateNode(final String nodeId, final QName expectedType, final String expectedTypeName)
    {
        final NodeRef nodeRef = nodes.validateNode(nodeId);
        verifyNodeType(nodeRef, expectedType, expectedTypeName);

        return nodeRef;
    }

    private void verifyNodeType(final NodeRef nodeRef, final QName expectedType, final String expectedTypeName)
    {
        final Set<QName> expectedTypes = Set.of(expectedType);
        if (!nodes.nodeMatches(nodeRef, expectedTypes, null)) {
            final String expectedTypeLocalName = (expectedTypeName != null)? expectedTypeName : expectedType.getLocalName();
            throw new InvalidArgumentException(String.format("NodeId of a %s is expected!", expectedTypeLocalName));
        }
    }

    private boolean isRuleSetNotNullAndShared(final NodeRef ruleSetNodeRef, final NodeRef folderNodeRef)
    {
        if (ruleSetNodeRef == null && folderNodeRef != null)
        {
            final NodeRef ruleSetNode = ruleService.getRuleSetNode(folderNodeRef);
            return ruleSetNode != null && ruleService.isRuleSetShared(ruleSetNode);
        }
        else
        {
            return isRuleSetNotNullAndShared(ruleSetNodeRef);
        }
    }

    private boolean isRuleSetNotNullAndShared(final NodeRef ruleSetNodeRef)
    {
        return ruleSetNodeRef != null && ruleService.isRuleSetShared(ruleSetNodeRef);
    }
}
