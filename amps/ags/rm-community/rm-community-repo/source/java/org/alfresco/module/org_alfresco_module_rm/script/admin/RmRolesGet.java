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

package org.alfresco.module.org_alfresco_module_rm.script.admin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Get information about record management roles
 *
 * @author Roy Wetherall
 */
public class RmRolesGet extends RoleDeclarativeWebScript
{
    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache)
     */
    @Override
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<>();
        Set<Role> roles = null;

        // get the file plan
        NodeRef filePlan = getFilePlan(req);
        if (filePlan == null)
        {
            throw new WebScriptException(Status.STATUS_FOUND, "File plan does not exist.");
        }

        // get the includesystem parameter
        boolean includeSystem = false;
        String includeSystemValue = req.getParameter("is");
        if (includeSystemValue != null && includeSystemValue.length() != 0)
        {
            includeSystem = Boolean.parseBoolean(includeSystemValue);
        }

        // get the user filter
        String user = req.getParameter("user");
        if (user != null && user.length() != 0)
        {
            roles = filePlanRoleService.getRolesByUser(filePlan, user, includeSystem);
        }
        else
        {
            roles = filePlanRoleService.getRoles(filePlan, includeSystem);
        }

        // get the auths parameter
        boolean showAuths = false;
        String auths = req.getParameter("auths");
        if (auths != null && auths.length() != 0)
        {
            showAuths = Boolean.parseBoolean(auths);
        }

        Set<RoleItem> items = createRoleItems(filePlan, roles, showAuths);
        model.put("roles", items);
        return model;
    }
}
