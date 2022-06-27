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
package org.alfresco.repo.action.access;

import org.alfresco.repo.rule.RuleModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;

import java.util.List;

public class AdminActionAccessRestriction implements ActionAccessRestriction {

    private AuthorityService authorityService;
    private NodeService nodeService;

    private static final List<String> CONTROLLED_ACTION_ACCESS_CONTEXT =
            List.of(ActionAccessRestriction.RULE_ACTION_CONTEXT, ActionAccessRestriction.V1_ACTION_CONTEXT);

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Override
    public void checkAccess(Action action) {
        if (isActionFromControlledContext(action)) {
            boolean isAdminOrSystemUser = authorityService.hasAdminAuthority() || AuthenticationUtil.isRunAsUserTheSystemUser();
            if (!isAdminOrSystemUser) {
                throw new ActionAccessException("Only admin or system user is allowed to execute this action");
            }
        }
    }

    @Override
    public void checkRunningActionAccess(Action action) {
        if (!isActionFromControlledContext(action) || isActionCausedByRule(action)) {
            //Already existing rules are deemed secure. Newly created/updated rules are verified
            return;
        }

        checkAccess(action);
    }

    private boolean isActionFromControlledContext(Action action) {
        return CONTROLLED_ACTION_ACCESS_CONTEXT.contains(ActionAccessRestriction.getActionContext(action));
    }

    private boolean isActionCausedByRule(Action action) {
        return nodeService.getParentAssocs(action.getNodeRef())
                .stream()
                .map(pa -> nodeService.getType(pa.getParentRef()))
                .filter(t -> RuleModel.RULE_MODEL_URI.equals(t.getNamespaceURI()))
                .filter(t -> RuleModel.RULE_MODEL_PREFIX.equals(t.getPrefixString()))
                .findFirst().isPresent();
    }
}
