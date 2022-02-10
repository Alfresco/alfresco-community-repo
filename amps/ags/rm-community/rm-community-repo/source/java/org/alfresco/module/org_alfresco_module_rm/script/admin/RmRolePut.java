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

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 *
 *
 * @author Roy Wetherall
 */
public class RmRolePut extends RoleDeclarativeWebScript
{
    private CapabilityService capabilityService;

    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }

    @Override
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<>();
        JSONObject json = null;
        try
        {
            // Role name
            Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
            String roleParam = templateVars.get("rolename");
            if (roleParam == null)
            {
                throw new WebScriptException(Status.STATUS_NOT_FOUND, "No role name was provided on the URL.");
            }

            json = new JSONObject(new JSONTokener(req.getContent().getContent()));
            String name = json.getString("name");
            // TODO check
            String displayLabel = json.getString("displayLabel");
            // TODO check

            JSONArray capabilitiesArray = json.getJSONArray("capabilities");
            Set<Capability> capabilites = new HashSet<>(capabilitiesArray.length());
            for (int i = 0; i < capabilitiesArray.length(); i++)
            {
                Capability capability = capabilityService.getCapability(capabilitiesArray.getString(i));
                capabilites.add(capability);
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

            Role role = filePlanRoleService.updateRole(filePlan, name, displayLabel, capabilites);
            model.put("role", new RoleItem(role));

        }
        catch (IOException iox)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Could not read content from req.", iox);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "Could not parse JSON from req.", je);
        }

        return model;
    }
}
