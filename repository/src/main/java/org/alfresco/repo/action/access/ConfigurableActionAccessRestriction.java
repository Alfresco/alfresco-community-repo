/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;

public class ConfigurableActionAccessRestriction extends ActionAccessRestrictionAbstractBase
{

    private AuthorityService authorityService;

    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    @Override
    protected void innerVerifyAccessRestriction(Action action)
    {

        String allowedGroups = getAllowedGroups(action.getActionDefinitionName());
        if (allowedGroups == null)
        {
            return;
        }

        boolean isAdminOrSystemUser = authorityService.hasAdminAuthority() || AuthenticationUtil.isRunAsUserTheSystemUser();
        if (isAdminOrSystemUser)
        {
            return;
        }

        List<String> groups = Arrays.stream(allowedGroups.split(",")).map(String::trim).filter(e -> !e.isEmpty()).toList();
        if (groups.isEmpty())
        {
            throw new ActionAccessException("Only admin or system user is allowed to define uses of or directly execute this action");
        }
        String user = AuthenticationUtil.getFullyAuthenticatedUser();
        if (user == null || user.isEmpty())
        {
            throw new ActionAccessException("Unknown user");
        }

        Set<String> containingAuthorities = authorityService.getContainingAuthorities(AuthorityType.GROUP, user, false);

        boolean groupFound = groups.stream().map(e -> PermissionService.GROUP_PREFIX + e.trim()).anyMatch(containingAuthorities::contains);
        if (!groupFound)
        {
            throw new ActionAccessException("User " + user + " is not allowed to define uses of or directly execute this action");
        }

    }

    /**
     * Returns the configured groups for the action, or null when there is no setting (the action is unrestricted).
     */
    private String getAllowedGroups(String actionDefinitionName)
    {
        return configProperties.getProperty(settingKey(actionDefinitionName));
    }

    private static String settingKey(String actionDefinitionName)
    {
        return "action." + actionDefinitionName + ".allowedGroups";
    }
}
