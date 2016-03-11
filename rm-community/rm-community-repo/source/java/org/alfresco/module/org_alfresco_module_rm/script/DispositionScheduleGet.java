package org.alfresco.module.org_alfresco_module_rm.script;

import java.util.Map;

import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to return full details
 * about a disposition schedule.
 *
 * @author Gavin Cornwell
 */
public class DispositionScheduleGet extends DispositionAbstractBase
{
    /*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // return the disposition schedule model
        return getDispositionScheduleModel(req);
    }
}
