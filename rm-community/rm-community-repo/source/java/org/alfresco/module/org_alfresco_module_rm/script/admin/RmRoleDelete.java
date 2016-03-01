 
package org.alfresco.module.org_alfresco_module_rm.script.admin;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
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
        if (StringUtils.isBlank(roleParam))
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
                                         "The role " + roleParam + " does not exist on the records managment root " + filePlan.toString());
        }

        filePlanRoleService.deleteRole(filePlan, roleParam);

        return model;
    }
}