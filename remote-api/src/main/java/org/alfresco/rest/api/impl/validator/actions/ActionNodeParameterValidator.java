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

import static org.alfresco.service.cmr.dictionary.DataTypeDefinition.NODE_REF;
import static org.alfresco.service.cmr.security.AccessStatus.ALLOWED;
import static org.alfresco.service.cmr.security.PermissionService.WRITE;

import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.repo.action.executer.CheckOutActionExecuter;
import org.alfresco.repo.action.executer.CopyActionExecuter;
import org.alfresco.repo.action.executer.ImageTransformActionExecuter;
import org.alfresco.repo.action.executer.ImporterActionExecuter;
import org.alfresco.repo.action.executer.LinkCategoryActionExecuter;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.action.executer.MoveActionExecuter;
import org.alfresco.repo.action.executer.ScriptActionExecuter;
import org.alfresco.repo.action.executer.SimpleWorkflowActionExecuter;
import org.alfresco.repo.action.executer.TransformActionExecuter;
import org.alfresco.rest.api.Actions;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.actions.ActionValidator;
import org.alfresco.rest.api.model.ActionDefinition;
import org.alfresco.rest.api.model.rules.Action;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.collections.MapUtils;

/**
 * This class provides logic for validation of permissions for action parameters which reference node.
 */
public class ActionNodeParameterValidator implements ActionValidator
{
    private static final boolean IS_ENABLED = true;
    /**
     * This list holds action parameter names which require only READ permission on a referenced node
     * That means, all other parameters that reference nodes will require WRITE permission
     */
    static final List<String> REQUIRE_READ_PERMISSION_PARAMS =
            List.of(MailActionExecuter.PARAM_TEMPLATE, LinkCategoryActionExecuter.PARAM_CATEGORY_VALUE, ScriptActionExecuter.PARAM_SCRIPTREF);

    static final String NO_PROPER_PERMISSIONS_FOR_NODE = "No proper permissions for node: ";

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
     * @param action Action to be validated
     */
    @Override
    public void validate(Action action)
    {
        final ActionDefinition actionDefinition = actions.getActionDefinitionById(action.getActionDefinitionId());
        final List<ActionDefinition.ParameterDefinition> nodeRefParams = actionDefinition.getParameterDefinitions().stream()
                .filter(pd -> NODE_REF.toPrefixString(namespaceService).equals(pd.getType())).collect(
                        Collectors.toList());
        validateNodePermissions(nodeRefParams, action);
    }

    @Override
    public boolean isEnabled()
    {
        return IS_ENABLED;
    }

    @Override
    public List<String> getActionDefinitionIds()
    {
        return List.of(CopyActionExecuter.NAME, MoveActionExecuter.NAME, CheckOutActionExecuter.NAME, ImporterActionExecuter.NAME,
                LinkCategoryActionExecuter.NAME, MailActionExecuter.NAME, ScriptActionExecuter.NAME, SimpleWorkflowActionExecuter.NAME,
                TransformActionExecuter.NAME, ImageTransformActionExecuter.NAME);
    }

    @Override
    public int getPriority()
    {
        return Integer.MIN_VALUE + 1;
    }

    private void validateNodePermissions(final List<ActionDefinition.ParameterDefinition> nodeRefParamDefinitions,
                                         final Action action)
    {
        if (MapUtils.isNotEmpty(action.getParams()))
        {
            nodeRefParamDefinitions.stream()
                    .filter(pd -> action.getParams().containsKey(pd.getName()))
                    .forEach(p -> validatePermission(p.getName(), action.getParams().get(p.getName()).toString()));
        }
    }

    private void validatePermission(final String paramName, final String nodeId)
    {
        final NodeRef nodeRef = nodes.validateNode(nodeId);
        if (permissionService.hasReadPermission(nodeRef) != ALLOWED)
        {
            throw new EntityNotFoundException(nodeId);
        }
        if (!REQUIRE_READ_PERMISSION_PARAMS.contains(paramName))
        {
            if (permissionService.hasPermission(nodeRef, WRITE) != ALLOWED)
            {
                throw new PermissionDeniedException(NO_PROPER_PERMISSIONS_FOR_NODE + nodeId);
            }
        }
    }
}
