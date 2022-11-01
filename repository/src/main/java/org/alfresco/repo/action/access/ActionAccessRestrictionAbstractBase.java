/*
 * #%L
 * Alfresco Repository
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
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.action.access;

import org.alfresco.repo.action.ActionModel;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ActionAccessRestrictionAbstractBase implements ActionAccessRestriction {

    private static final Set<String> CONTROLLED_ACTION_ACCESS_CONTEXT =
            Set.of(ActionAccessRestriction.RULE_ACTION_CONTEXT, ActionAccessRestriction.FORM_PROCESSOR_ACTION_CONTEXT,
                    ActionAccessRestriction.V0_ACTION_CONTEXT, ActionAccessRestriction.V1_ACTION_CONTEXT);

    protected NodeService nodeService;
    private Properties configProperties;


    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setConfigProperties(Properties configProperties) {
        this.configProperties = configProperties;
    }

    /**
     * Base for verifying access restriction,
     * manages common checks for exposing action in config or action being ran as a consequence of running a rule (safe)
     *
     * @param action
     */
    public void verifyAccessRestriction(Action action) {
        if (blockAccessRestriction(action)) {
            return;
        }

        innerVerifyAccessRestriction(action);
    }

    protected boolean blockAccessRestriction(Action action) {
        return isActionExposed(action) || isActionCausedByRule(action);
    }

    protected boolean isActionExposed(Action action) {
        return !isActionFromControlledContext(action) || isExposedInConfig(action).orElse(Boolean.FALSE);
    }

    private boolean isActionFromControlledContext(Action action) {
        String actionContext = ActionAccessRestriction.getActionContext(action);
        return actionContext != null && CONTROLLED_ACTION_ACCESS_CONTEXT.contains(actionContext);
    }

    private Optional<Boolean> isExposedInConfig(Action action)
    {
        return getConfigKeys(action).
                map(configProperties::getProperty).
                filter(Objects::nonNull).
                map(Boolean::parseBoolean).
                findFirst();
    }

    private Stream<String> getConfigKeys(Action action)
    {
        String context = ActionAccessRestriction.getActionContext(action);
        String actionName = action.getActionDefinitionName();

        if (context != null)
        {
            return Stream.of(
                    getConfigKey(actionName, context),
                    getConfigKey(actionName));
        }
        return Stream.of(getConfigKey(actionName));
    }

    private String getConfigKey(String... parts)
    {
        return Stream.of(parts)
                .collect(Collectors.joining(".", "org.alfresco.repo.action.", ".exposed"));
    }

    /**
     * Checks the hierarchy of primary parents of action node ref to look for Rule node ref
     * Finding it means that the action was triggered by an existing rule, which are deemed secure
     * as their validation happens at their setup.
     *
     * @param action
     * @return
     */
    protected boolean isActionCausedByRule(Action action) {
        if (action.getNodeRef() == null) {
            return false;
        }

        NodeRef ruleParent = getPotentialRuleParent(action.getNodeRef());
        return isRule(ruleParent);
    }

    private NodeRef getPotentialRuleParent(NodeRef nodeRef) {
        NodeRef parentNode = nodeService.getPrimaryParent(nodeRef).getParentRef();

        while (isCompositeAction(parentNode))
        {
            parentNode = nodeService.getPrimaryParent(parentNode).getParentRef();
        }

        return parentNode;
    }

    private boolean isCompositeAction(NodeRef nodeRef) {
        return ActionModel.TYPE_COMPOSITE_ACTION.equals(nodeService.getType(nodeRef));
    }

    private boolean isRule(NodeRef nodeRef) {
        return RuleModel.TYPE_RULE.equals(nodeService.getType(nodeRef));
    }

    /**
     * Restriction specific implementation of extensions
     * @param action
     */
    protected abstract void innerVerifyAccessRestriction(Action action);
}
