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
package org.alfresco.rest.api.impl.rules;

import static org.alfresco.service.cmr.security.AccessStatus.ALLOWED;
import static org.alfresco.service.cmr.security.PermissionService.CHANGE_PERMISSIONS;

import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.rules.RuleSet;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;

/** Responsible for validating nodes when working with rules. */
@Experimental
public class NodeValidator
{
    private static final String RULE_SET_EXPECTED_TYPE_NAME = "rule set";

    private Nodes nodes;
    private RuleService ruleService;
    private PermissionService permissionService;
    private NodeService nodeService;

    /**
     * Validates if folder node exists and the user has permission to use it.
     *
     * @param folderNodeId - folder node ID
     * @param requireChangePermission - Whether to require change permission or just read permission.
     * @return folder node reference
     * @throws InvalidArgumentException if node is not of an expected type
     * @throws PermissionDeniedException if the user doesn't have the appropriate permission for the folder.
     * @throws EntityNotFoundException if the folder node isn't found
     */
    public NodeRef validateFolderNode(final String folderNodeId, boolean requireChangePermission)
    {
        try
        {
            final NodeRef nodeRef = nodes.validateOrLookupNode(folderNodeId);
            validatePermission(requireChangePermission, nodeRef);
            verifyNodeType(nodeRef, ContentModel.TYPE_FOLDER, null);

            return nodeRef;
        } catch (EntityNotFoundException e)
        {
            throw new EntityNotFoundException("Folder with id " + folderNodeId + " was not found.", e);
        }
    }

    /**
     * Validates if rule set ID is default, node exists and associated folder node matches.
     *
     * @param ruleSetId - rule set node ID
     * @param associatedFolderNodeRef - folder node ref to check the association
     * @return rule set node reference
     * @throws InvalidArgumentException in case of not matching associated folder node
     * @throws RelationshipResourceNotFoundException if the folder doesn't have a -default- rule set
     * @throws EntityNotFoundException if the rule set node isn't found
     */
    public NodeRef validateRuleSetNode(final String ruleSetId, final NodeRef associatedFolderNodeRef)
    {
        if (RuleSet.isDefaultId(ruleSetId))
        {
            final NodeRef ruleSetNodeRef = ruleService.getRuleSetNode(associatedFolderNodeRef);
            if (ruleSetNodeRef == null)
            {
                //folder doesn't have a -default- rule set
                throw new RelationshipResourceNotFoundException(associatedFolderNodeRef.getId(), ruleSetId);
            }
            return ruleSetNodeRef;
        }

        try {
            final NodeRef ruleSetNodeRef = validateNode(ruleSetId, ContentModel.TYPE_SYSTEM_FOLDER, RULE_SET_EXPECTED_TYPE_NAME);

            if (!ruleService.isRuleSetAssociatedWithFolder(ruleSetNodeRef, associatedFolderNodeRef))
            {
                throw new InvalidArgumentException("Rule set is not associated with folder node!");
            }
            return ruleSetNodeRef;

        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Rule set with id " + ruleSetId + " was not found.", e);
        }
    }

    public NodeRef validateRuleSetNode(String linkToNodeId, boolean requireChangePermission)
    {
        final Node ruleSetNode = nodes.getNode(linkToNodeId);
        final ChildAssociationRef primaryParent = nodeService.getPrimaryParent(ruleSetNode.getNodeRef());
        final NodeRef parentNode = primaryParent.getParentRef();
        validatePermission(requireChangePermission, parentNode);
        return parentNode;
    }


    /**
     * Validates if rule node exists and associated rule set node matches.
     *
     * @param ruleId - rule node ID
     * @param associatedRuleSetNodeRef - rule set node ref to check the association. Can be null
     * @return rule node reference
     * @throws InvalidArgumentException in case of not matching associated rule set node
     */
    public NodeRef validateRuleNode(final String ruleId, final NodeRef associatedRuleSetNodeRef)
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

    private void validatePermission(boolean requireChangePermission, NodeRef nodeRef)
    {
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
    }

    private void verifyNodeType(final NodeRef nodeRef, final QName expectedType, final String expectedTypeName)
    {
        final Set<QName> expectedTypes = Set.of(expectedType);
        if (!nodes.nodeMatches(nodeRef, expectedTypes, null))
        {
            final String expectedTypeLocalName = (expectedTypeName != null) ? expectedTypeName : expectedType.getLocalName();
            throw new InvalidArgumentException(String.format("NodeId of a %s is expected!", expectedTypeLocalName));
        }
    }

    public boolean isRuleSetNode(String nodeId) {
        try
        {
            validateNode(nodeId, ContentModel.TYPE_SYSTEM_FOLDER, RULE_SET_EXPECTED_TYPE_NAME);
            return true;
        } catch (InvalidArgumentException e) {
            return false;
        }
    }

    /**
     * Verifies if rule set node or folder node's default rule set is shared
     * @param ruleSetNodeRef
     * @param folderNodeRef
     * @return
     */
    public boolean isRuleSetNotNullAndShared(final NodeRef ruleSetNodeRef, final NodeRef folderNodeRef)
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

    public boolean isRuleSetNotNullAndShared(final NodeRef ruleSetNodeRef)
    {
        return ruleSetNodeRef != null && ruleService.isRuleSetShared(ruleSetNodeRef);
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
}
