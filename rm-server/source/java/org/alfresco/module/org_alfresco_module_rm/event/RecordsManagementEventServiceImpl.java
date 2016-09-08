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
import org.json.JSONArray;
import org.json.JSONObject;

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
    private Map<String, RecordsManagementEventType> eventTypes = new HashMap<String, RecordsManagementEventType>(7);
    
    /** Available events */
    private Map<String, RecordsManagementEvent> events;
    
    /**
     * Set the node service
     * 
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the content service
     * 
     * @param contentService    content service
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
        return new ArrayList<RecordsManagementEventType>(this.eventTypes.values());
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService#getEvents()
     */
    public List<RecordsManagementEvent> getEvents()
    {
        return new ArrayList<RecordsManagementEvent>(this.getEventMap().values());
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService#getEvent(java.lang.String)
     */
    public RecordsManagementEvent getEvent(String eventName) 
    {
        if (getEventMap().containsKey(eventName) == false)
        {
            throw new AlfrescoRuntimeException("The event " + eventName + " does not exist.");
        }
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
    * @see org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService#existsEventDisplayLabel(java.lang.String)
    */
   @SuppressWarnings("rawtypes")
   public boolean existsEventDisplayLabel(String eventDisplayLabel)
   {
      for (Iterator iterator = getEventMap().values().iterator(); iterator.hasNext();)
      {
         RecordsManagementEvent recordsManagementEvent = (RecordsManagementEvent) iterator.next();
         if (recordsManagementEvent.getDisplayLabel().equalsIgnoreCase(eventDisplayLabel))
         {
            return true;
         }
      }
      return false;
   }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService#addEvent(java.lang.String, java.lang.String, java.lang.String)
     */
    public RecordsManagementEvent addEvent(String eventType, String eventName, String eventDisplayLabel)
    {
        // Check that the eventType is valid
        if (eventTypes.containsKey(eventType) == false)
        {
            throw new AlfrescoRuntimeException(
                        "Can not add event because event " + 
                        eventName + 
                        " has an undefined eventType. (" 
                        + eventType + ")");
        }
        
        // Create event and add to map
        RecordsManagementEvent event = new RecordsManagementEvent(eventType, eventName, eventDisplayLabel);
        getEventMap().put(event.getName(), event);
        
        // Persist the changes to the event list
        saveEvents();
        
        return new RecordsManagementEvent(eventType, eventName, eventDisplayLabel);
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
     * Helper method to get the event map.  Loads initial instance from persisted configuration file.
     * 
     * @return Map<String, RecordsManagementEvent>  map of available events by event name
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
                if (nodeService.exists(CONFIG_NODE_REF) == false)
                {
                    throw new AlfrescoRuntimeException("Unable to find records management event configuration node.");
                }
                
                // Read content from config node
                ContentReader reader = contentService.getReader(CONFIG_NODE_REF, ContentModel.PROP_CONTENT);
                String jsonString = reader.getContentString();
                
                JSONObject configJSON = new JSONObject(jsonString);
                JSONArray eventsJSON = configJSON.getJSONArray("events");
                
                events = new HashMap<String, RecordsManagementEvent>(eventsJSON.length());
                
                for (int i = 0; i < eventsJSON.length(); i++)
                {
                    // Get the JSON object that represents the event
                    JSONObject eventJSON = eventsJSON.getJSONObject(i);
                    
                    // Get the details of the event
                    String eventType = eventJSON.getString("eventType");
                    String eventName = eventJSON.getString("eventName");
                    String eventDisplayLabel = eventJSON.getString("eventDisplayLabel");
                    
                    // Check that the eventType is valid
                    if (eventTypes.containsKey(eventType) == false)
                    {
                        throw new AlfrescoRuntimeException(
                                    "Can not load rm event configuration because event " + 
                                    eventName + 
                                    " has an undefined eventType. (" 
                                    + eventType + ")");
                    }
                    
                    // Create event and add to map
                    RecordsManagementEvent event = new RecordsManagementEvent(eventType, eventName, eventDisplayLabel);
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
                if (nodeService.exists(CONFIG_NODE_REF) == false)
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
                ContentWriter contentWriter = contentService.getWriter(CONFIG_NODE_REF, ContentModel.PROP_CONTENT, true);
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


