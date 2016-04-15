package org.alfresco.repo.web.scripts.audit;

import java.util.HashMap;
import java.util.Map;

import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Derek Hulley
 * @since 3.4
 */
public class AuditControlPost extends AbstractAuditWebScript
{
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>(7);

        String appName = getParamAppName(req);
        String path = getParamPath(req);
        
        boolean enable = getParamEnableDisable(req);
        
        if (appName == null)
        {
            // Global operation
            auditService.setAuditEnabled(enable);
        }
        else
        {
            // Apply to a specific application
            if (enable)
            {
                auditService.enableAudit(appName, path);
            }
            else
            {
                auditService.disableAudit(appName, path);
            }
        }
        model.put(JSON_KEY_ENABLED, enable);
        
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
        }
        return model;
    }
}