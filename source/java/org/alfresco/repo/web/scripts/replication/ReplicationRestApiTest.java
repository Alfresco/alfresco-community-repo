/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.replication;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.person.TestPersonManager;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.alfresco.service.cmr.replication.ReplicationService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.GUID;
import org.alfresco.util.ISO8601DateFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Tests for the Replication Webscripts
 * @author Nick Burch
 */
public class ReplicationRestApiTest extends BaseWebScriptTest
{
    private static final String URL_DEFINITION = "api/replication-definition/";
    private static final String URL_DEFINITIONS = "api/replication-definitions";
    private static final String URL_RUNNING_ACTION = "api/running-action/";
    
    private static final String USER_NORMAL = "Normal" + GUID.generate();
    
    private TestPersonManager personManager;
    private ReplicationService replicationService;
    private ActionTrackingService actionTrackingService;
    
    public void testReplicationDefinitionsGet() throws Exception
    {
        Response response;
        
        
        // Not allowed if you're not an admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getGuestUserName());
        response = sendRequest(new GetRequest(URL_DEFINITIONS), Status.STATUS_UNAUTHORIZED);
        assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());
        
        AuthenticationUtil.setFullyAuthenticatedUser(USER_NORMAL);
        response = sendRequest(new GetRequest(URL_DEFINITIONS), Status.STATUS_UNAUTHORIZED);
        assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());
        
       
        // If no definitions exist, you don't get anything back
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        response = sendRequest(new GetRequest(URL_DEFINITIONS), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        String jsonStr = response.getContentAsString();
        JSONObject json = new JSONObject(jsonStr);
        JSONArray results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(0, results.length());

        
        // Add a definition, it should show up
        ReplicationDefinition rd = replicationService.createReplicationDefinition("Test1", "Testing");
        replicationService.saveReplicationDefinition(rd);
        response = sendRequest(new GetRequest(URL_DEFINITIONS), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(1, results.length());
        
        JSONObject jsonRD = (JSONObject)results.get(0);
        assertNotNull(jsonRD);
        assertEquals("Test1", jsonRD.get("name"));
        assertEquals("New", jsonRD.get("status"));
        assertEquals(true, jsonRD.get("enabled"));
        assertEquals(JSONObject.NULL, jsonRD.get("startedAt"));
        assertEquals("/api/replication-definition/Test1", jsonRD.get("details"));
        
        
        // Change the status to running, and re-check
        actionTrackingService.recordActionExecuting(rd);
        String startedAt = ISO8601DateFormat.format(rd.getExecutionStartDate());
        
        response = sendRequest(new GetRequest(URL_DEFINITIONS), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(1, results.length());
        
        jsonRD = (JSONObject)results.get(0);
        assertNotNull(jsonRD);
        assertEquals("Test1", jsonRD.get("name"));
        assertEquals("Running", jsonRD.get("status"));
        assertEquals(true, jsonRD.get("enabled"));
        assertEquals(startedAt, jsonRD.get("startedAt"));
        assertEquals("/api/replication-definition/Test1", jsonRD.get("details"));
        
       
        // Add a 2nd and 3rd
        rd = replicationService.createReplicationDefinition("Test2", "2nd Testing");
        replicationService.saveReplicationDefinition(rd);
        rd = replicationService.createReplicationDefinition("AnotherTest", "3rd Testing");
        replicationService.saveReplicationDefinition(rd);
        
        // They should come back sorted by name
        response = sendRequest(new GetRequest(URL_DEFINITIONS), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(3, results.length());
        
        jsonRD = (JSONObject)results.get(0);
        assertNotNull(jsonRD);
        assertEquals("AnotherTest", jsonRD.get("name"));
        assertEquals("New", jsonRD.get("status"));
        assertEquals(true, jsonRD.get("enabled"));
        assertEquals(JSONObject.NULL, jsonRD.get("startedAt"));
        assertEquals("/api/replication-definition/AnotherTest", jsonRD.get("details"));
        
        jsonRD = (JSONObject)results.get(1);
        assertNotNull(jsonRD);
        assertEquals("Test1", jsonRD.get("name"));
        assertEquals("Running", jsonRD.get("status"));
        assertEquals(true, jsonRD.get("enabled"));
        assertEquals(startedAt, jsonRD.get("startedAt"));
        assertEquals("/api/replication-definition/Test1", jsonRD.get("details"));
        
        jsonRD = (JSONObject)results.get(2);
        assertNotNull(jsonRD);
        assertEquals("Test2", jsonRD.get("name"));
        assertEquals("New", jsonRD.get("status"));
        assertEquals(true, jsonRD.get("enabled"));
        assertEquals(JSONObject.NULL, jsonRD.get("startedAt"));
        assertEquals("/api/replication-definition/Test2", jsonRD.get("details"));
        
        
        // Sort by status
        response = sendRequest(new GetRequest(URL_DEFINITIONS + "?sort=status"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(3, results.length());
        
        // New, name sorts higher 
        jsonRD = (JSONObject)results.get(0);
        assertNotNull(jsonRD);
        assertEquals("AnotherTest", jsonRD.get("name"));
        assertEquals("New", jsonRD.get("status"));
        assertEquals(true, jsonRD.get("enabled"));
        assertEquals(JSONObject.NULL, jsonRD.get("startedAt"));
        assertEquals("/api/replication-definition/AnotherTest", jsonRD.get("details"));
        
        // New, name sorts lower
        jsonRD = (JSONObject)results.get(1);
        assertNotNull(jsonRD);
        assertEquals("Test2", jsonRD.get("name"));
        assertEquals("New", jsonRD.get("status"));
        assertEquals(true, jsonRD.get("enabled"));
        assertEquals(JSONObject.NULL, jsonRD.get("startedAt"));
        assertEquals("/api/replication-definition/Test2", jsonRD.get("details"));
        
        // Running
        jsonRD = (JSONObject)results.get(2);
        assertNotNull(jsonRD);
        assertEquals("Test1", jsonRD.get("name"));
        assertEquals("Running", jsonRD.get("status"));
        assertEquals(true, jsonRD.get("enabled"));
        assertEquals(startedAt, jsonRD.get("startedAt"));
        assertEquals("/api/replication-definition/Test1", jsonRD.get("details"));
                
        
        // Set start times and statuses on these other two
        rd = replicationService.loadReplicationDefinition("Test2");
        actionTrackingService.recordActionExecuting(rd);
        actionTrackingService.recordActionComplete(rd);
        String startedAt2 = ISO8601DateFormat.format(rd.getExecutionStartDate());
        
        
        // Try the different sorts
        response = sendRequest(new GetRequest(URL_DEFINITIONS + "?sort=status"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(3, results.length());
        
        // Complete
        jsonRD = (JSONObject)results.get(0);
        assertNotNull(jsonRD);
        assertEquals("Test2", jsonRD.get("name"));
        assertEquals("Completed", jsonRD.get("status"));
        assertEquals(true, jsonRD.get("enabled"));
        assertEquals(startedAt2, jsonRD.get("startedAt"));
        assertEquals("/api/replication-definition/Test2", jsonRD.get("details"));
        
        // New
        jsonRD = (JSONObject)results.get(1);
        assertNotNull(jsonRD);
        assertEquals("AnotherTest", jsonRD.get("name"));
        assertEquals("New", jsonRD.get("status"));
        assertEquals(true, jsonRD.get("enabled"));
        assertEquals(JSONObject.NULL, jsonRD.get("startedAt"));
        assertEquals("/api/replication-definition/AnotherTest", jsonRD.get("details"));
        
        // Running
        jsonRD = (JSONObject)results.get(2);
        assertNotNull(jsonRD);
        assertEquals("Test1", jsonRD.get("name"));
        assertEquals("Running", jsonRD.get("status"));
        assertEquals(true, jsonRD.get("enabled"));
        assertEquals(startedAt, jsonRD.get("startedAt"));
        assertEquals("/api/replication-definition/Test1", jsonRD.get("details"));
        
        
        // By last run
        response = sendRequest(new GetRequest(URL_DEFINITIONS + "?sort=lastRun"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(3, results.length());

        // Never run first
        jsonRD = (JSONObject)results.get(0);
        assertNotNull(jsonRD);
        assertEquals("AnotherTest", jsonRD.get("name"));
        assertEquals("New", jsonRD.get("status"));
        assertEquals(true, jsonRD.get("enabled"));
        assertEquals(JSONObject.NULL, jsonRD.get("startedAt"));
        assertEquals("/api/replication-definition/AnotherTest", jsonRD.get("details"));
        
        // Ran most recently
        jsonRD = (JSONObject)results.get(1);
        assertNotNull(jsonRD);
        assertEquals("Test2", jsonRD.get("name"));
        assertEquals("Completed", jsonRD.get("status"));
        assertEquals(true, jsonRD.get("enabled"));
        assertEquals(startedAt2, jsonRD.get("startedAt"));
        assertEquals("/api/replication-definition/Test2", jsonRD.get("details"));
        
        // Ran least recently
        jsonRD = (JSONObject)results.get(2);
        assertNotNull(jsonRD);
        assertEquals("Test1", jsonRD.get("name"));
        assertEquals("Running", jsonRD.get("status"));
        assertEquals(true, jsonRD.get("enabled"));
        assertEquals(startedAt, jsonRD.get("startedAt"));
        assertEquals("/api/replication-definition/Test1", jsonRD.get("details"));
        
       
        // Cancel one of these
        rd = replicationService.loadReplicationDefinition("AnotherTest");
        rd.setEnabled(false);
        replicationService.saveReplicationDefinition(rd);
        actionTrackingService.recordActionExecuting(rd);
        actionTrackingService.requestActionCancellation(rd);
        String startedAt3 = ISO8601DateFormat.format(rd.getExecutionStartDate());
        
        response = sendRequest(new GetRequest(URL_DEFINITIONS), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(3, results.length());
        
        jsonRD = (JSONObject)results.get(0);
        assertNotNull(jsonRD);
        assertEquals("AnotherTest", jsonRD.get("name"));
        assertEquals("CancelRequested", jsonRD.get("status"));
        assertEquals(false, jsonRD.get("enabled"));
        assertEquals(startedAt3, jsonRD.get("startedAt"));
        assertEquals("/api/replication-definition/AnotherTest", jsonRD.get("details"));
        
        jsonRD = (JSONObject)results.get(1);
        assertNotNull(jsonRD);
        assertEquals("Test1", jsonRD.get("name"));
        assertEquals("Running", jsonRD.get("status"));
        assertEquals(true, jsonRD.get("enabled"));
        assertEquals(startedAt, jsonRD.get("startedAt"));
        assertEquals("/api/replication-definition/Test1", jsonRD.get("details"));
        
        jsonRD = (JSONObject)results.get(2);
        assertNotNull(jsonRD);
        assertEquals("Test2", jsonRD.get("name"));
        assertEquals("Completed", jsonRD.get("status"));
        assertEquals(true, jsonRD.get("enabled"));
        assertEquals(startedAt2, jsonRD.get("startedAt"));
        assertEquals("/api/replication-definition/Test2", jsonRD.get("details"));
    }
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ApplicationContext appContext = getServer().getApplicationContext();

        replicationService = (ReplicationService)appContext.getBean("ReplicationService");
        actionTrackingService = (ActionTrackingService)appContext.getBean("actionTrackingService");
        
        MutableAuthenticationService authenticationService = (MutableAuthenticationService)appContext.getBean("AuthenticationService");
        PersonService personService = (PersonService)appContext.getBean("PersonService");
        NodeService nodeService = (NodeService)appContext.getBean("NodeService");
        personManager = new TestPersonManager(authenticationService, personService, nodeService);

        personManager.createPerson(USER_NORMAL);
        
        // Ensure we start with no replication definitions
        // (eg another test left them behind)
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        for(ReplicationDefinition rd : replicationService.loadReplicationDefinitions()) {
           replicationService.deleteReplicationDefinition(rd);
        }
        AuthenticationUtil.clearCurrentSecurityContext();
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        personManager.clearPeople();
        
        // Zap any replication definitions we created
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        for(ReplicationDefinition rd : replicationService.loadReplicationDefinitions()) {
           replicationService.deleteReplicationDefinition(rd);
        }
        AuthenticationUtil.clearCurrentSecurityContext();
    }
}
