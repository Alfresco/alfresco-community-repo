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

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Delete role web script
 * 
 * @author Roy Wetherall
 */
public class RmRoleDelete extends RoleDeclarativeWebScript
{
    /** Logger */
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(RmRoleDelete.class);

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache)
     */
    @Override
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        
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
        if (filePlanRoleService.existsRole(filePlan, roleParam) == false)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, 
                                         "The role " + roleParam + " does not exist on the records managment root " + filePlan.toString());
        }
        
        filePlanRoleService.deleteRole(filePlan, roleParam);
        
        return model;
    }
}