package org.alfresco.repo.web.scripts.audit;

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
public class AuditClearPost extends AbstractAuditWebScript
{
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>(7);
        
        String appName = getParamAppName(req);
        if (appName == null)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "audit.err.app.notProvided");
        }
        AuditApplication app = auditService.getAuditApplications().get(appName);
        if (app == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "audit.err.app.notFound", appName);
        }
        // Get from/to times
        Long fromTime = getParamFromTime(req);           // might be null
        Long toTime = getParamToTime(req);               // might be null
        
        // Clear
        int cleared = auditService.clearAudit(appName, fromTime, toTime);
        
        model.put(JSON_KEY_CLEARED, cleared);
        
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
        }
        return model;
    }
}