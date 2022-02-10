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

package org.alfresco.module.org_alfresco_module_rm.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.ParameterCheck;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Records management event service implementation
 *
 * @author Roy Wetherall
 */
public class RecordsManagementEventServiceImpl implements RecordsManagementEventService
{
    /** Reference to the rm event config node */
    private static final StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

    private static final NodeRef CONFIG_NODE_REF = new NodeRef(SPACES_STORE, "rm_event_config");

    /** Node service */
    private NodeService nodeService;

    /** Content service */
    private ContentService contentService;

    /** Registered event types */
    private Map<String, RecordsManagementEventType> eventTypes = new HashMap<>(7);

    /** Available events */
    private Map<String, RecordsManagementEvent> events;

    /**
     * Set the node service
     *
     * @param nodeService node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the content service
     *
     * @param contentService content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService#registerEventType(org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventType)
     */
    public void registerEventType(RecordsManagementEventType eventType)
    {
        this.eventTypes.put(eventType.getName(), eventType);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService#getEventTypes()
     */
    public List<RecordsManagementEventType> getEventTypes()
    {
        return new ArrayList<>(this.eventTypes.values());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService#getEvents()
     */
    public List<RecordsManagementEvent> getEvents()
    {
        return new ArrayList<>(this.getEventMap().values());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService#getEvent(java.lang.String)
     */
    public RecordsManagementEvent getEvent(String eventName)
    {
        if (!getEventMap().containsKey(eventName)) { throw new AlfrescoRuntimeException("The event "
                + eventName + " does not exist."); }
        return getEventMap().get(eventName);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService#existsEvent(java.lang.String)
     */
    public boolean existsEvent(String eventName)
    {
        return getEventMap().containsKey(eventName);
    }

    /**
     * Indicates whether a particular event display label exists.  Returns true if it does, false otherwise.
     *
     * @param eventDisplayLabel event display label
     * @return boolean      true if event exists, false otherwise
     */
    public boolean existsEventDisplayLabel(String eventDisplayLabel)
    {
        for (RecordsManagementEvent recordsManagementEvent : getEventMap().values())
        {
            if (recordsManagementEvent.getDisplayLabel().equalsIgnoreCase(eventDisplayLabel)) { return true; }
        }
        return false;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService#canCreateEvent(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("rawtypes")
    public boolean canCreateEvent(String eventDisplayLabel, String eventName)
    {
        ParameterCheck.mandatoryString("eventDisplayLabel", eventDisplayLabel);
        ParameterCheck.mandatoryString("eventName", eventName);

        boolean canCreateEvent = true;

        if (existsEvent(eventName))
        {
            canCreateEvent = false;
        }

        if (canCreateEvent)
        {
            for (Iterator iterator = getEventMap().values().iterator(); iterator.hasNext();)
            {
                RecordsManagementEvent recordsManagementEvent = (RecordsManagementEvent) iterator.next();
                if (recordsManagementEvent.getDisplayLabel().equalsIgnoreCase(eventDisplayLabel))
                {
                    canCreateEvent = false;
                    break;
                }
            }
        }

        return canCreateEvent;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService#canEditEvent(java.lang.String, java.lang.String, java.lang.String)
     */
    @SuppressWarnings("rawtypes")
	public boolean canEditEvent(String eventDisplayLabel, String eventName, String eventType)
    {
        ParameterCheck.mandatoryString("eventDisplayLabel", eventDisplayLabel);
        ParameterCheck.mandatoryString("eventName", eventName);
        ParameterCheck.mandatoryString("eventType", eventType);

        boolean canEditEvent = true;

        if (!existsEvent(eventName))
        {
            throw new AlfrescoRuntimeException("The event '" + eventName + "' does not exist.");
        }

        for (Iterator iterator = getEventMap().values().iterator(); iterator.hasNext();)
        {
            RecordsManagementEvent recordsManagementEvent = (RecordsManagementEvent) iterator.next();
            if (recordsManagementEvent.getDisplayLabel().equalsIgnoreCase(eventDisplayLabel))
            {
                if (recordsManagementEvent.getName().equalsIgnoreCase(eventName))
                {
                    if (!recordsManagementEvent.getType().equalsIgnoreCase(eventType))
                    {
                        canEditEvent = true;
                    }
                    else
                    {
                        canEditEvent = false;
                    }
                }
                else
                {
                    throw new AlfrescoRuntimeException("Cannot edit event. An event with the display label '"
                            + eventDisplayLabel + "' already exist.");
                }
                break;
            }
        }

        return canEditEvent;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService#addEvent(java.lang.String, java.lang.String, java.lang.String)
     */
    public RecordsManagementEvent addEvent(String eventType, String eventName, String eventDisplayLabel)
    {
        // Check that the eventType is valid
        if (!eventTypes.containsKey(eventType))
        {
            throw new AlfrescoRuntimeException(
                        "Can not add event because event " +
                        eventName +
                        " has an undefined eventType. ("
                        + eventType + ")");
        }

        // Create event and add to map
        RecordsManagementEvent event = new RecordsManagementEvent(eventTypes.get(eventType), eventName, eventDisplayLabel);
        getEventMap().put(event.getName(), event);

        // Persist the changes to the event list
        saveEvents();

        return new RecordsManagementEvent(eventTypes.get(eventType), eventName, eventDisplayLabel);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService#removeEvent(java.lang.String)
     */
    public void removeEvent(String eventName)
    {
        // Remove the event from the map
        getEventMap().remove(eventName);

        // Persist the changes to the event list
        saveEvents();
    }

    /**
     * Helper method to get the event map. Loads initial instance from persisted
     * configuration file.
     *
     * @return Map<String, RecordsManagementEvent> map of available events by
     *         event name
     */
    private Map<String, RecordsManagementEvent> getEventMap()
    {
        if (this.events == null)
        {
            loadEvents();
        }
        return this.events;
    }

    /**
     * Load the events from the persistant storage
     */
    private void loadEvents()
    {
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                // Get the event config node
                if (!nodeService.exists(CONFIG_NODE_REF))
                {
                    throw new AlfrescoRuntimeException("Unable to find records management event configuration node.");
                }

                // Read content from config node
                ContentReader reader = contentService.getReader(CONFIG_NODE_REF, ContentModel.PROP_CONTENT);
                String jsonString = reader.getContentString();

                JSONObject configJSON = new JSONObject(jsonString);
                JSONArray eventsJSON = configJSON.getJSONArray("events");

                events = new HashMap<>(eventsJSON.length());

                for (int i = 0; i < eventsJSON.length(); i++)
                {
                    // Get the JSON object that represents the event
                    JSONObject eventJSON = eventsJSON.getJSONObject(i);

                    // Get the details of the event
                    String eventType = eventJSON.getString("eventType");
                    String eventName = eventJSON.getString("eventName");

                    String eventDisplayLabel = eventJSON.getString("eventDisplayLabel");
                    String translated = I18NUtil.getMessage(eventDisplayLabel);
                    if (translated != null)
                    {
                        eventDisplayLabel = translated;
                    }


                    // Check that the eventType is valid
                    if (!eventTypes.containsKey(eventType))
                    {
                        throw new AlfrescoRuntimeException(
                                    "Can not load rm event configuration because event " +
                                    eventName +
                                    " has an undefined eventType. ("
                                    + eventType + ")");
                    }

                    // Create event and add to map
                    RecordsManagementEvent event = new RecordsManagementEvent(eventTypes.get(eventType), eventName, eventDisplayLabel);
                    events.put(event.getName(), event);
                }
                return null;
            }

        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Save the events to the peristant storage
     */
    private void saveEvents()
    {
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                // Get the event config node
                if (!nodeService.exists(CONFIG_NODE_REF))
                {
                    throw new AlfrescoRuntimeException("Unable to find records management event configuration node.");
                }

                JSONObject configJSON = new JSONObject();
                JSONArray eventsJSON = new JSONArray();

                int index = 0;
                for (RecordsManagementEvent event : events.values())
                {
                    JSONObject eventJSON = new JSONObject();
                    eventJSON.put("eventType", event.getType());
                    eventJSON.put("eventName", event.getName());
                    eventJSON.put("eventDisplayLabel", event.getDisplayLabel());

                    eventsJSON.put(index, eventJSON);
                    index++;
                }
                configJSON.put("events", eventsJSON);

                // Get content writer
                ContentWriter contentWriter = contentService
                        .getWriter(CONFIG_NODE_REF, ContentModel.PROP_CONTENT, true);
                contentWriter.putContent(configJSON.toString());

                return null;
            }

        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService#getEventType(java.lang.String)
     */
    public RecordsManagementEventType getEventType(String eventTypeName)
    {
        return this.eventTypes.get(eventTypeName);
    }
}
