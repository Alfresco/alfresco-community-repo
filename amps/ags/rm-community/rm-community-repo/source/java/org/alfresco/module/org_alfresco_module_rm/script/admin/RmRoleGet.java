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

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Role GET web script API
 *
 * @author Roy Wetherall
 */
public class RmRoleGet extends RoleDeclarativeWebScript
{
    @Override
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<>();

        // Role name
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String roleParam = templateVars.get("rolename");
        if (roleParam == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "No role name was provided on the URL.");
        }

        // get the file plan
        NodeRef filePlan = getFilePlan(req);
        if (filePlan == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "File plan does not exist.");
        }

        // Check that the role exists
        if (!filePlanRoleService.existsRole(filePlan, roleParam))
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND,
                                         "The role " + roleParam + " does not exist on the records managment root " + filePlan);
        }

        RoleItem item = new RoleItem(filePlanRoleService.getRole(filePlan, roleParam));
        model.put("role", item);

        return model;
    }
}
