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

import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEvent;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService;
import org.alfresco.util.GUID;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Records management event POST web script
 *
 * @author Roy Wetherall
 */
public class RmEventsPost extends RMEventBase
{
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

            // Get the event name
            String eventName = getEventName(json);

            // Check the event display label
            String eventDisplayLabel = getValue(json, "eventDisplayLabel");
            doCheck(eventDisplayLabel, "No event display label was provided.");

            // Check if the event can be created
            canCreateEvent(eventDisplayLabel, eventName);

            // Check the event type
            String eventType = getValue(json, "eventType");
            doCheck(eventType, "No event type was provided.");

            // Create event
            RecordsManagementEvent event = rmEventService.addEvent(eventType, eventName, eventDisplayLabel);

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
     * Helper method for getting the event name
     *
     * @param json The request content as JSON object
     * @return String The event name. A generated GUID if it doesn't exist
     * @throws JSONException If there is no string value for the key
     */
    private String getEventName(JSONObject json) throws JSONException
    {
        String eventName = getValue(json, "eventName");

        if (StringUtils.isBlank(eventName))
        {
            // Generate the event name
            eventName = GUID.generate();
        }

        return eventName;
    }

    /**
     * Helper method for checking if an event can be created or not. Throws an
     * error if the event already exists.
     *
     * @param eventDisplayLabel The display label of the event
     * @param eventName The name of the event
     */
    private boolean canCreateEvent(String eventDisplayLabel, String eventName)
    {
        boolean canCreateEvent = true;

        if (!rmEventService.canCreateEvent(eventDisplayLabel, eventName))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Cannot create event. An event with the display label '"
                          + eventDisplayLabel + "' already exists.");
        }

        return canCreateEvent;
    }
}
