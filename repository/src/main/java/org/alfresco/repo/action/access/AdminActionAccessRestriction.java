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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;

import java.util.List;

public class AdminActionAccessRestriction implements ActionAccessRestriction {

    private AuthorityService authorityService;
    private NodeService nodeService;

    private static final List<String> CONTROLLED_ACTION_ACCESS_CONTEXT =
            List.of(ActionAccessRestriction.RULE_ACTION_CONTEXT, ActionAccessRestriction.FORM_PROCESSOR_ACTION_CONTEXT,
                    ActionAccessRestriction.V0_ACTION_CONTEXT, ActionAccessRestriction.V1_ACTION_CONTEXT);

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * {@inheritDoc}
     */
    public void verifyAccessRestriction(Action action) {
        if (isActionFromControlledContext(action) && !isActionCausedByRule(action)) {
            boolean isAdminOrSystemUser = authorityService.hasAdminAuthority() || AuthenticationUtil.isRunAsUserTheSystemUser();
            if (!isAdminOrSystemUser) {
                throw new ActionAccessException("Only admin or system user is allowed to define uses of/directly execute this action");
            }
        }
    }

    private boolean isActionFromControlledContext(Action action) {
        String actionContext = ActionAccessRestriction.getActionContext(action);
        return actionContext != null && CONTROLLED_ACTION_ACCESS_CONTEXT.contains(actionContext);
    }

    /**
     * Checks the hierarchy of primary parents of action node ref to look for Rule node ref
     * Finding it means that the action was triggered by an existing rule, which are deemed secure.
     * Direct parent can be a composite action, if that's the case then we need to look for a parent 1 level higher
     *
     * @param action
     * @return
     */
    private boolean isActionCausedByRule(Action action) {
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
}
