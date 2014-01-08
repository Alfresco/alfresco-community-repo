/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.module.org_alfresco_module_rm.script.admin;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Records management event delete web script
 *
 * @author Roy Wetherall
 */
public class RmEventDelete extends DeclarativeWebScript
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

    /**
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        // Event name
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String eventName = templateVars.get("eventname");
        if (eventName == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "No event name was provided on the URL.");
        }

        // Check the event exists
        if (rmEventService.existsEvent(eventName) == false)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "The event " + eventName + " does not exist.");
        }

        // Remove the event
        rmEventService.removeEvent(eventName);

        return model;
    }
}