/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.script.admin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEvent;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService;
import org.alfresco.util.ParameterCheck;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Records management event PUT web script
 *
 * @author Roy Wetherall
 */
public class RmEventPut extends RMEventBase
{
	/** Parameter names */
	public static final String PARAM_EVENTNAME = "eventname";

    /** Records management event service */
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
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest,
     *      org.springframework.extensions.webscripts.Status,
     *      org.springframework.extensions.webscripts.Cache)
     */
    @Override
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        ParameterCheck.mandatory("req", req);

        Map<String, Object> model = new HashMap<>();
        JSONObject json = null;
        try
        {
            // Convert the request content to JSON
            json = new JSONObject(new JSONTokener(req.getContent().getContent()));

            // Check the event name
            Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
            String eventName = templateVars.get(PARAM_EVENTNAME);
            if (eventName == null ||
                eventName.isEmpty() ||
                !rmEventService.existsEvent(eventName))
            {
                throw new WebScriptException(Status.STATUS_NOT_FOUND, "No event name was provided.");
            }

            // Check the event display label
            String eventDisplayLabel = getValue(json, "eventDisplayLabel");
            doCheck(eventDisplayLabel, "No event display label was provided.");

            // Check the event type
            String eventType = getValue(json, "eventType");
            doCheck(eventType, "No event type was provided.");

            // Check if the event can be edited or not
            RecordsManagementEvent event = null;
            if (canEditEvent(eventDisplayLabel, eventName, eventType))
            {
                // Create event
                event = rmEventService.addEvent(eventType, eventName, eventDisplayLabel);
            }
            else
            {
                // Get event
                event = rmEventService.getEvent(eventName);
            }

            model.put("event", event);
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

        return model;
    }

    /**
     * Helper method for checking if an event can be edited or not. Throws an
     * error if an event with the same display label already exists.
     *
     * @param eventDisplayLabel The display label of the event
     * @param eventName The name of the event
     * @param eventType The type of the event
     * @return true if the event can be edited, false otherwise
     */
    private boolean canEditEvent(String eventDisplayLabel, String eventName, String eventType)
    {
        boolean canEditEvent;

        try
        {
           canEditEvent = rmEventService.canEditEvent(eventDisplayLabel, eventName, eventType);
        }
        catch (AlfrescoRuntimeException are)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, are.getLocalizedMessage(), are);
        }

        return canEditEvent;
    }
}
