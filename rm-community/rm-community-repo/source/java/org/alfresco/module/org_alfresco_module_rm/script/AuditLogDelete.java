 
package org.alfresco.module.org_alfresco_module_rm.script;

import java.util.HashMap;
import java.util.Map;

import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to clear the 
 * Records Management audit log.
 * 
 * @author Gavin Cornwell
 */
public class AuditLogDelete extends BaseAuditAdminWebScript
{
    /*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        this.rmAuditService.clearAuditLog(getDefaultFilePlan());
            
        // create model object with the audit status model
        Map<String, Object> model = new HashMap<String, Object>(1);
        model.put("auditstatus", createAuditStatusModel());
        return model;
    }
}