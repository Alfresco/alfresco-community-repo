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

/*
 * An action access restriction that allows configuration of which groups are allowed to use the action.
 *
 * The configuration is done in the repository.properties file, using the following property:
 *
 * action.<actionDefinitionName>.allowedGroups=<comma separated list of group names>
 *
 * If the property is not set, then the action is unrestricted.  If the property is set to an empty value, then only admin or system user can use the action.
 *
 * Note: a missing setting means unrestricted. Mail relies on its setting in repository.properties. If that setting is removed,
 * any authenticated user can send mail through rules and the action API, and no warning is logged.
 *
 * @author sbhattacharya
 */
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
        if (allowedGroups == null) // No setting: unrestricted
        {
            return;
        }

        if (authorityService.hasAdminAuthority() || AuthenticationUtil.isRunAsUserTheSystemUser()) // Empty setting: no groups, so only admins
        {
            return;
        }

        String user = AuthenticationUtil.getRunAsUser();
        List<String> groups = parseGroups(allowedGroups);
        if (isMemberOfAnyGroup(user, groups)) // Listed groups: members of any listed group get past this line
        {
            return;
        }

        throw new ActionAccessException("Only admin, system user or members of the allowed groups can define uses of or directly execute this action");
    }

    private List<String> parseGroups(String allowedGroups)
    {
        return Arrays.stream(allowedGroups.split(",")).map(String::trim).filter(e -> !e.isEmpty()).toList();
    }

    private boolean isMemberOfAnyGroup(String user, List<String> groups)
    {
        if (user == null || groups.isEmpty())
        {
            return false;
        }

        Set<String> containingAuthorities = authorityService.getContainingAuthorities(AuthorityType.GROUP, user, false);
        return groups.stream().map(group -> PermissionService.GROUP_PREFIX + group).anyMatch(containingAuthorities::contains);
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
