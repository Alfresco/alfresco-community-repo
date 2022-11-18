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

package org.alfresco.rest.api.impl.validator.actions;

import static org.alfresco.model.ContentModel.TYPE_CATEGORY;
import static org.alfresco.model.ContentModel.TYPE_FOLDER;
import static org.alfresco.service.cmr.dictionary.DataTypeDefinition.NODE_REF;
import static org.alfresco.service.cmr.security.AccessStatus.ALLOWED;
import static org.alfresco.service.cmr.security.PermissionService.WRITE;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.repo.action.executer.CheckOutActionExecuter;
import org.alfresco.repo.action.executer.CopyActionExecuter;
import org.alfresco.repo.action.executer.ImageTransformActionExecuter;
import org.alfresco.repo.action.executer.ImporterActionExecuter;
import org.alfresco.repo.action.executer.LinkCategoryActionExecuter;
import org.alfresco.repo.action.executer.MoveActionExecuter;
import org.alfresco.repo.action.executer.SimpleWorkflowActionExecuter;
import org.alfresco.repo.action.executer.TransformActionExecuter;
import org.alfresco.rest.api.Actions;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.actions.ActionValidator;
import org.alfresco.rest.api.model.ActionDefinition;
import org.alfresco.rest.api.model.rules.Action;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.collections.MapUtils;
import org.apache.logging.log4j.util.Strings;

/**
 * This class provides logic for validation of permissions for action parameters which reference node.
 */
public class ActionNodeParameterValidator implements ActionValidator
{
    /**
     * This list holds action parameter names which require only READ permission on a referenced node
     * That means, all other parameters that reference nodes will require WRITE permission
     */
    static final Map<String, List<String>> REQUIRE_READ_PERMISSION_PARAMS =
            Map.of(LinkCategoryActionExecuter.NAME, List.of(LinkCategoryActionExecuter.PARAM_CATEGORY_VALUE));

    static final String NO_PROPER_PERMISSIONS_FOR_NODE = "No proper permissions for node: ";
    static final String NOT_A_CATEGORY = "Node is not a category ";
    static final String NOT_A_FOLDER = "Node is not a folder ";

    private final Actions actions;
    private final NamespaceService namespaceService;
    private final Nodes nodes;
    private final PermissionService permissionService;

    public ActionNodeParameterValidator(Actions actions, NamespaceService namespaceService, Nodes nodes,
                                        PermissionService permissionService)
    {
        this.actions = actions;
        this.namespaceService = namespaceService;
        this.nodes = nodes;
        this.permissionService = permissionService;
    }

    /**
     * Validates action parameters that reference nodes against access permissions for executing user.
     *
     * @param action Action to be validated
     */
    @Override
    public void validate(Action action)
    {
        final ActionDefinition actionDefinition = actions.getRuleActionDefinitionById(action.getActionDefinitionId());
        final List<ActionDefinition.ParameterDefinition> nodeRefParams = actionDefinition.getParameterDefinitions().stream()
                .filter(pd -> NODE_REF.toPrefixString(namespaceService).equals(pd.getType()))
                .collect(Collectors.toList());
        validateNodes(nodeRefParams, action);
    }

    /**
     * @return List of action definitions applicable to this validator
     */
    @Override
    public List<String> getActionDefinitionIds()
    {
        return List.of(CopyActionExecuter.NAME, MoveActionExecuter.NAME, CheckOutActionExecuter.NAME, ImporterActionExecuter.NAME,
                LinkCategoryActionExecuter.NAME, SimpleWorkflowActionExecuter.NAME, TransformActionExecuter.NAME,
                ImageTransformActionExecuter.NAME);
    }

    @Override
    public int getPriority()
    {
        return Integer.MIN_VALUE + 1;
    }

    private void validateNodes(final List<ActionDefinition.ParameterDefinition> nodeRefParamDefinitions,
                               final Action action)
    {
        if (MapUtils.isNotEmpty(action.getParams()))
        {
            nodeRefParamDefinitions.stream()
                    .filter(pd -> action.getParams().containsKey(pd.getName()))
                    .forEach(p -> {
                        final String nodeId = Objects.toString(action.getParams().get(p.getName()), Strings.EMPTY);
                        final NodeRef nodeRef = nodes.validateNode(nodeId);
                        validatePermission(action.getActionDefinitionId(), p.getName(), nodeRef);
                        validateType(action.getActionDefinitionId(), nodeRef);
                    });
        }
    }

    private void validatePermission(final String actionDefinitionId, final String paramName, final NodeRef nodeRef)
    {
        if (permissionService.hasReadPermission(nodeRef) != ALLOWED)
        {
            throw new EntityNotFoundException(nodeRef.getId());
        }
        if (!REQUIRE_READ_PERMISSION_PARAMS.containsKey(actionDefinitionId) ||
                REQUIRE_READ_PERMISSION_PARAMS.get(actionDefinitionId).stream().noneMatch(paramName::equals))
        {
            if (permissionService.hasPermission(nodeRef, WRITE) != ALLOWED)
            {
                throw new PermissionDeniedException(NO_PROPER_PERMISSIONS_FOR_NODE + nodeRef.getId());
            }
        }
    }

    private void validateType(final String actionDefinitionId, final NodeRef nodeRef)
    {
        if (!LinkCategoryActionExecuter.NAME.equals(actionDefinitionId))
        {
            if (!nodes.nodeMatches(nodeRef, Set.of(TYPE_FOLDER), Collections.emptySet()))
            {
                throw new InvalidArgumentException(NOT_A_FOLDER + nodeRef.getId());
            }
        } else if (!nodes.nodeMatches(nodeRef, Set.of(TYPE_CATEGORY), Collections.emptySet()))
        {
            throw new InvalidArgumentException(NOT_A_CATEGORY + nodeRef.getId());
        }
    }
}
