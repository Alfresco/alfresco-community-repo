/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.script.admin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    /** Logger */
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(RmRolesGet.class);

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache)
     */
    @Override
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        Set<Role> roles = null;

        // get the file plan
        NodeRef filePlan = getFilePlan(req);
        if (filePlan == null)
        {
            throw new WebScriptException(Status.STATUS_FOUND, "File plan does not exist.");
        }

        // get the user filter
        String user = req.getParameter("user");
        if (user != null && user.length() != 0)
        {
            roles = filePlanRoleService.getRolesByUser(filePlan, user, false);
        }
        else
        {
            roles = filePlanRoleService.getRoles(filePlan, false);
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