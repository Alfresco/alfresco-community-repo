 
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