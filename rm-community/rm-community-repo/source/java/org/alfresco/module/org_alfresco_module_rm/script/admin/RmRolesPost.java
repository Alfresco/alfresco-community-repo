 
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
 * RM Roles Post implementation
 *
 * @author Roy Wetherall
 */
public class RmRolesPost extends RoleDeclarativeWebScript
{
    private CapabilityService capabilityService;

    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }

    @Override
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        JSONObject json = null;
        try
        {
            json = new JSONObject(new JSONTokener(req.getContent().getContent()));
            String name = json.getString("name");
            // TODO check
            String displayString = json.getString("displayLabel");
            // TODO check

            JSONArray capabilitiesArray = json.getJSONArray("capabilities");
            Set<Capability> capabilites = new HashSet<Capability>(capabilitiesArray.length());
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

            Role role = filePlanRoleService.createRole(filePlan, name, displayString, capabilites);
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