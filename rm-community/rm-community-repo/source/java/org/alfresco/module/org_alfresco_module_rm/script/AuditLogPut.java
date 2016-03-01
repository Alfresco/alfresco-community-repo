 
package org.alfresco.module.org_alfresco_module_rm.script;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Implementation for Java backed webscript to start
 * and stop Records Management auditing.
 * 
 * @author Gavin Cornwell
 */
public class AuditLogPut extends BaseAuditAdminWebScript
{
    protected static final String PARAM_ENABLED = "enabled";
    
    /*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        try
        {
            // determine whether to start or stop auditing
            JSONObject json = new JSONObject(new JSONTokener(req.getContent().getContent()));
            
            // check the enabled property present
            if (!json.has(PARAM_ENABLED))
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Mandatory 'enabled' parameter was not provided in request body");
            }
            
            boolean enabled = json.getBoolean(PARAM_ENABLED);
            if (enabled)
            {
                this.rmAuditService.startAuditLog(getDefaultFilePlan());
            }
            else
            {
                this.rmAuditService.stopAuditLog(getDefaultFilePlan());
            }
            
            // create model object with the audit status model
            Map<String, Object> model = new HashMap<String, Object>(1);
            model.put("auditstatus", createAuditStatusModel());
            return model;
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
    }
}