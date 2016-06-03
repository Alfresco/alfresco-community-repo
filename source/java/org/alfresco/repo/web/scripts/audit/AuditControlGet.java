package org.alfresco.repo.web.scripts.audit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.audit.AuditService.AuditApplication;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Derek Hulley
 * @since 3.4
 */
public class AuditControlGet extends AbstractAuditWebScript
{
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>(7);
        
        String appName = getParamAppName(req);
        String path = getParamPath(req);
        boolean enabledGlobal = auditService.isAuditEnabled();
        Map<String, AuditApplication> appsByName = auditService.getAuditApplications();
        
        // Check that the application exists
        if (appName != null)
        {
            if (path == null)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "audit.err.path.notProvided");
            }
            
            AuditApplication app = appsByName.get(appName);
            if (app == null)
            {
                throw new WebScriptException(Status.STATUS_NOT_FOUND, "audit.err.app.notFound", appName);
            }
            // Discard all the other applications
            appsByName = Collections.singletonMap(appName, app);
        }
        
        model.put(JSON_KEY_ENABLED, enabledGlobal);
        model.put(JSON_KEY_APPLICATIONS, appsByName.values());
        
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
        }
        return model;
    }
}