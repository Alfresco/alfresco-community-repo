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

package org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase;
import org.alfresco.util.GUID;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * RM event REST API test
 * 
 * @author Roy Wetherall
 */
public class EventRestApiTest extends BaseRMWebScriptTestCase implements RecordsManagementModel
{
    protected static final String GET_EVENTS_URL = "/api/rma/admin/rmevents";
    protected static final String GET_EVENTTYPES_URL = "/api/rma/admin/rmeventtypes";
    protected static final String SERVICE_URL_PREFIX = "/alfresco/service";
    protected static final String APPLICATION_JSON = "application/json";
   
    protected static final String DISPLAY_LABEL = "display label";
    protected static final String EVENT_TYPE = "rmEventType.simple";
    protected static final String KEY_EVENT_NAME = "eventName";
    protected static final String KEY_EVENT_TYPE = "eventType";
    protected static final String KEY_EVENT_DISPLAY_LABEL = "eventDisplayLabel";
       
    public void testGetEventTypes() throws Exception
    {
        Response rsp = sendRequest(new GetRequest(GET_EVENTTYPES_URL),200);
        String rspContent = rsp.getContentAsString();
        
        JSONObject obj = new JSONObject(rspContent);
        JSONObject types = obj.getJSONObject("data");
        assertNotNull(types);
        
        JSONObject type = types.getJSONObject("rmEventType.simple");
        assertNotNull(type);
        assertEquals("rmEventType.simple", type.getString("eventTypeName"));
        assertNotNull(type.getString("eventTypeDisplayLabel"));
        
        System.out.println(rspContent);
    }
    
    public void testGetEvents() throws Exception
    {
        String event1 = GUID.generate();
        String event2 = GUID.generate();
        
        // Create a couple or events by hand
        eventService.addEvent(EVENT_TYPE, event1, DISPLAY_LABEL);
        eventService.addEvent(EVENT_TYPE, event2, DISPLAY_LABEL);
        
        try
        {
            // Get the events
            Response rsp = sendRequest(new GetRequest(GET_EVENTS_URL),200);
            String rspContent = rsp.getContentAsString();
            
            JSONObject obj = new JSONObject(rspContent);
            JSONObject roles = obj.getJSONObject("data");
            assertNotNull(roles);
            
            JSONObject eventObj = roles.getJSONObject(event1);
            assertNotNull(eventObj);
            assertEquals(event1, eventObj.get(KEY_EVENT_NAME));
            assertEquals(DISPLAY_LABEL, eventObj.get(KEY_EVENT_DISPLAY_LABEL));
            assertEquals(EVENT_TYPE, eventObj.get(KEY_EVENT_TYPE));
            
            eventObj = roles.getJSONObject(event2);
            assertNotNull(eventObj);
            assertEquals(event2, eventObj.get(KEY_EVENT_NAME));
            assertEquals(DISPLAY_LABEL, eventObj.get(KEY_EVENT_DISPLAY_LABEL));
            assertEquals(EVENT_TYPE, eventObj.get(KEY_EVENT_TYPE));                    
        }
        finally
        {
            // Clean up 
            eventService.removeEvent(event1);
            eventService.removeEvent(event2);
        }
        
    }
    
    public void testPostEvents() throws Exception
    {        
        String eventName= GUID.generate();
        
        JSONObject obj = new JSONObject();
        obj.put(KEY_EVENT_NAME, eventName);
        obj.put(KEY_EVENT_DISPLAY_LABEL, DISPLAY_LABEL);
        obj.put(KEY_EVENT_TYPE, EVENT_TYPE);
        
        Response rsp = sendRequest(new PostRequest(GET_EVENTS_URL, obj.toString(), APPLICATION_JSON),200);
        try
        {
            String rspContent = rsp.getContentAsString();
            
            JSONObject resultObj = new JSONObject(rspContent);
            JSONObject eventObj = resultObj.getJSONObject("data");
            assertNotNull(eventObj);
            
            assertEquals(eventName, eventObj.get(KEY_EVENT_NAME));
            assertEquals(DISPLAY_LABEL, eventObj.get(KEY_EVENT_DISPLAY_LABEL));
            assertEquals(EVENT_TYPE, eventObj.get(KEY_EVENT_TYPE));
           
        }
        finally
        {
            eventService.removeEvent(eventName);
        }  
        
        // Test with no event name set
        obj = new JSONObject();
        obj.put(KEY_EVENT_DISPLAY_LABEL, DISPLAY_LABEL);
        obj.put(KEY_EVENT_TYPE, EVENT_TYPE);
        rsp = sendRequest(new PostRequest(GET_EVENTS_URL, obj.toString(), APPLICATION_JSON),200);
        try
        {
            String rspContent = rsp.getContentAsString();
            
            JSONObject resultObj = new JSONObject(rspContent);
            JSONObject eventObj = resultObj.getJSONObject("data");
            assertNotNull(eventObj);
            
            assertNotNull(eventObj.get(KEY_EVENT_NAME));
            assertEquals(DISPLAY_LABEL, eventObj.get(KEY_EVENT_DISPLAY_LABEL));
            assertEquals(EVENT_TYPE, eventObj.get(KEY_EVENT_TYPE));
           
            eventName = eventObj.getString(KEY_EVENT_NAME);
        }
        finally
        {
            eventService.removeEvent(eventName);
        }
    }
    
    public void testPutRole() throws Exception
    {
        String eventName = GUID.generate();        
        eventService.addEvent(EVENT_TYPE, eventName, DISPLAY_LABEL);
        
        try
        {
            JSONObject obj = new JSONObject();
            obj.put(KEY_EVENT_NAME, eventName);
            obj.put(KEY_EVENT_DISPLAY_LABEL, "changed");
            obj.put(KEY_EVENT_TYPE, EVENT_TYPE);
            
            // Get the roles
            Response rsp = sendRequest(new PutRequest(GET_EVENTS_URL + "/" + eventName, obj.toString(), APPLICATION_JSON),200);
            String rspContent = rsp.getContentAsString();
            
            JSONObject result = new JSONObject(rspContent);
            JSONObject eventObj = result.getJSONObject("data");
            assertNotNull(eventObj);
            
            assertEquals(eventName, eventObj.get(KEY_EVENT_NAME));
            assertEquals("changed", eventObj.get(KEY_EVENT_DISPLAY_LABEL));
            assertEquals(EVENT_TYPE, eventObj.get(KEY_EVENT_TYPE));     
            
            // Bad requests
            obj = new JSONObject();
            obj.put(KEY_EVENT_NAME, "cheese");
            obj.put(KEY_EVENT_DISPLAY_LABEL, "whatever");
            obj.put(KEY_EVENT_TYPE, EVENT_TYPE);
            
            sendRequest(new PutRequest(GET_EVENTS_URL + "/cheese", obj.toString(), APPLICATION_JSON), 404);   
        }
        finally
        {
            // Clean up 
            eventService.removeEvent(eventName);
        }
        
    }
    
    public void testGetRole() throws Exception
    {
        String eventName = GUID.generate();        
        eventService.addEvent(EVENT_TYPE, eventName, DISPLAY_LABEL);
        
        try
        {
            // Get the roles
            Response rsp = sendRequest(new GetRequest(GET_EVENTS_URL + "/" + eventName),200);
            String rspContent = rsp.getContentAsString();
            
            JSONObject obj = new JSONObject(rspContent);
            JSONObject eventObj = obj.getJSONObject("data");
            assertNotNull(eventObj);
            
            assertEquals(eventName, eventObj.get(KEY_EVENT_NAME));
            assertEquals(DISPLAY_LABEL, eventObj.get(KEY_EVENT_DISPLAY_LABEL));
            assertEquals(EVENT_TYPE, eventObj.get(KEY_EVENT_TYPE));      
            
            // Bad requests
            sendRequest(new GetRequest(GET_EVENTS_URL + "/cheese"), 404);
        }
        finally
        {
            // Clean up 
            eventService.removeEvent(eventName);
        }
        
    }
    
    public void testDeleteRole() throws Exception
    {
        String eventName = GUID.generate();
        assertFalse(eventService.existsEvent(eventName));        
        eventService.addEvent(EVENT_TYPE, eventName, DISPLAY_LABEL);       
        assertTrue(eventService.existsEvent(eventName));           
        sendRequest(new DeleteRequest(GET_EVENTS_URL + "/" + eventName),200);        
        assertFalse(eventService.existsEvent(eventName));    
        
        // Bad request
        sendRequest(new DeleteRequest(GET_EVENTS_URL + "/cheese"), 404);  
    }
    
}
