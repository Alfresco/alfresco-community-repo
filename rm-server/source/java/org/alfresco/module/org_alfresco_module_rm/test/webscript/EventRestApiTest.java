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
package org.alfresco.module.org_alfresco_module_rm.test.webscript;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.GUID;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import org.json.JSONObject;

/**
 * RM event REST API test
 * 
 * @author Roy Wetherall
 */
public class EventRestApiTest extends BaseWebScriptTest implements RecordsManagementModel
{
    protected static StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
    protected static final String GET_EVENTS_URL = "/api/rma/admin/rmevents";
    protected static final String GET_EVENTTYPES_URL = "/api/rma/admin/rmeventtypes";
    protected static final String SERVICE_URL_PREFIX = "/alfresco/service";
    protected static final String APPLICATION_JSON = "application/json";
    protected static final String DISPLAY_LABEL = "display label";
    protected static final String EVENT_TYPE = "rmEventType.simple";
    protected static final String KEY_EVENT_NAME = "eventName";
    protected static final String KEY_EVENT_TYPE = "eventType";
    protected static final String KEY_EVENT_DISPLAY_LABEL = "eventDisplayLabel";
    
    protected NodeService nodeService;
    protected RecordsManagementService rmService;
    protected RecordsManagementEventService rmEventService;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        this.nodeService = (NodeService) getServer().getApplicationContext().getBean("NodeService");
        this.rmService = (RecordsManagementService)getServer().getApplicationContext().getBean("RecordsManagementService");
        this.rmEventService = (RecordsManagementEventService)getServer().getApplicationContext().getBean("RecordsManagementEventService");
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());        
    }    

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
        rmEventService.addEvent(EVENT_TYPE, event1, DISPLAY_LABEL);
        rmEventService.addEvent(EVENT_TYPE, event2, DISPLAY_LABEL);
        
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
            rmEventService.removeEvent(event1);
            rmEventService.removeEvent(event2);
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
            rmEventService.removeEvent(eventName);
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
            rmEventService.removeEvent(eventName);
        }
    }
    
    public void testPutRole() throws Exception
    {
        String eventName = GUID.generate();        
        rmEventService.addEvent(EVENT_TYPE, eventName, DISPLAY_LABEL);
        
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
            sendRequest(new PutRequest(GET_EVENTS_URL + "/cheese", obj.toString(), APPLICATION_JSON), 404);   
        }
        finally
        {
            // Clean up 
            rmEventService.removeEvent(eventName);
        }
        
    }
    
    public void testGetRole() throws Exception
    {
        String eventName = GUID.generate();        
        rmEventService.addEvent(EVENT_TYPE, eventName, DISPLAY_LABEL);
        
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
            rmEventService.removeEvent(eventName);
        }
        
    }
    
    public void testDeleteRole() throws Exception
    {
        String eventName = GUID.generate();
        assertFalse(rmEventService.existsEvent(eventName));        
        rmEventService.addEvent(EVENT_TYPE, eventName, DISPLAY_LABEL);       
        assertTrue(rmEventService.existsEvent(eventName));           
        sendRequest(new DeleteRequest(GET_EVENTS_URL + "/" + eventName),200);        
        assertFalse(rmEventService.existsEvent(eventName));    
        
        // Bad request
        sendRequest(new DeleteRequest(GET_EVENTS_URL + "/cheese"), 404);  
    }
    
}
