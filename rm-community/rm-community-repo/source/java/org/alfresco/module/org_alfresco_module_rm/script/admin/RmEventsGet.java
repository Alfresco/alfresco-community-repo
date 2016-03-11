package org.alfresco.module.org_alfresco_module_rm.script.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEvent;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Records management events GET web script
 *
 * @author Roy Wetherall
 */
public class RmEventsGet extends DeclarativeWebScript
{
    /** Reccords management event service */
    private RecordsManagementEventService rmEventService;

    /**
     * Set the records management event service
     *
     * @param rmEventService
     */
    public void setRecordsManagementEventService(RecordsManagementEventService rmEventService)
    {
        this.rmEventService = rmEventService;
    }

    @Override
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        // Get the events
        List<RecordsManagementEvent> events = rmEventService.getEvents();
        model.put("events", events);

        return model;
    }
}
