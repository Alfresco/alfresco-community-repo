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

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.person.TestPersonManager;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.alfresco.service.cmr.replication.ReplicationService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.alfresco.util.ISO8601DateFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
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
    
    private static final String JSON = "application/json";
    
    private static final String USER_NORMAL = "Normal" + GUID.generate();
    
    private NodeService nodeService;
    private TestPersonManager personManager;
    private ReplicationService replicationService;
    private TransactionService transactionService;
    private ActionTrackingService actionTrackingService;
    
    private Repository repositoryHelper;
    
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
        
        
        // Ensure we didn't get any unexpected data back,
        //  only the keys we should have done
        JSONArray keys = jsonRD.names();
        for(int i=0; i<keys.length(); i++) {
           String key = keys.getString(0);
           if(key.equals("name") || key.equals("status") ||
               key.equals("startedAt") || key.equals("enabled") ||
               key.equals("details")) {
              // All good
           } else {
              fail("Unexpected key '"+key+"' found in json, raw json is\n" + jsonStr);
           }
        }
        
        
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
    
    public void testReplicationDefinitionGet() throws Exception
    {
        Response response;
        
        
        // Not allowed if you're not an admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getGuestUserName());
        response = sendRequest(new GetRequest(URL_DEFINITION + "madeup"), Status.STATUS_UNAUTHORIZED);
        assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());
        
        AuthenticationUtil.setFullyAuthenticatedUser(USER_NORMAL);
        response = sendRequest(new GetRequest(URL_DEFINITION + "madeup"), Status.STATUS_UNAUTHORIZED);
        assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());
        
       
        // If an invalid name is given, you get a 404
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        response = sendRequest(new GetRequest(URL_DEFINITION + "madeup"), 404);
        assertEquals(Status.STATUS_NOT_FOUND, response.getStatus());
        
        
        // Add a definition, it should show up
        ReplicationDefinition rd = replicationService.createReplicationDefinition("Test1", "Testing");
        replicationService.saveReplicationDefinition(rd);
        response = sendRequest(new GetRequest(URL_DEFINITION + "Test1"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        String jsonStr = response.getContentAsString();
        JSONObject json = new JSONObject(jsonStr);
        assertNotNull(json);
        
        // Check 
        assertEquals("Test1", json.get("name"));
        assertEquals("Testing", json.get("description"));
        assertEquals("New", json.get("status"));
        assertEquals(JSONObject.NULL, json.get("startedAt"));
        assertEquals(JSONObject.NULL, json.get("endedAt"));
        assertEquals(JSONObject.NULL, json.get("failureMessage"));
        assertEquals(JSONObject.NULL, json.get("executionDetails"));
        assertEquals(JSONObject.NULL, json.get("transferLocalReport"));
        assertEquals(JSONObject.NULL, json.get("transferRemoteReport"));
        assertEquals(true, json.get("enabled"));
        assertEquals(JSONObject.NULL, json.get("targetName"));
        // Payload is empty
        assertEquals(0, json.getJSONArray("payload").length());
        
        
        // Ensure we didn't get any unexpected data back
        JSONArray keys = json.names();
        for(int i=0; i<keys.length(); i++) {
           String key = keys.getString(0);
           if(key.equals("name") || key.equals("description") || 
               key.equals("status") || key.equals("startedAt") ||
               key.equals("endedAt") || key.equals("failureMessage") ||
               key.equals("executionDetails") || key.equals("payload") ||
               key.equals("transferLocalReport") ||
               key.equals("transferRemoteReport") ||
               key.equals("enabled")) {
              // All good
           } else {
              fail("Unexpected key '"+key+"' found in json, raw json is\n" + jsonStr);
           }
        }
        
        
        // Change the status to running, and re-check
        actionTrackingService.recordActionExecuting(rd);
        String actionId = rd.getId();
        String startedAt = ISO8601DateFormat.format(rd.getExecutionStartDate());
        int instanceId = ((ActionImpl)rd).getExecutionInstance();
        
        response = sendRequest(new GetRequest(URL_DEFINITION + "Test1"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        
        assertEquals("Test1", json.get("name"));
        assertEquals("Testing", json.get("description"));
        assertEquals("Running", json.get("status"));
        assertEquals(startedAt, json.get("startedAt"));
        assertEquals(JSONObject.NULL, json.get("endedAt"));
        assertEquals(JSONObject.NULL, json.get("failureMessage"));
        assertEquals("/api/running-action/replicationActionExecutor="+
              actionId + "=" + instanceId, json.get("executionDetails"));
        assertEquals(JSONObject.NULL, json.get("transferLocalReport"));
        assertEquals(JSONObject.NULL, json.get("transferRemoteReport"));
        assertEquals(true, json.get("enabled"));
        assertEquals(JSONObject.NULL, json.get("targetName"));
        // Payload is empty
        assertEquals(0, json.getJSONArray("payload").length());
        
        
        // Cancel it
        actionTrackingService.requestActionCancellation(rd);
        
        response = sendRequest(new GetRequest(URL_DEFINITION + "Test1"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        
        assertEquals("Test1", json.get("name"));
        assertEquals("Testing", json.get("description"));
        assertEquals("CancelRequested", json.get("status"));
        assertEquals(startedAt, json.get("startedAt"));
        assertEquals(JSONObject.NULL, json.get("endedAt"));
        assertEquals(JSONObject.NULL, json.get("failureMessage"));
        assertEquals("/api/running-action/replicationActionExecutor="+
              actionId + "=" + instanceId, json.get("executionDetails"));
        assertEquals(JSONObject.NULL, json.get("transferLocalReport"));
        assertEquals(JSONObject.NULL, json.get("transferRemoteReport"));
        assertEquals(true, json.get("enabled"));
        assertEquals(JSONObject.NULL, json.get("targetName"));
        // Payload is empty
        assertEquals(0, json.getJSONArray("payload").length());

        
        // Add some payload details, ensure that they get expanded
        //  as they should be
        rd.getPayload().add(
              repositoryHelper.getCompanyHome()
        );
        NodeRef dataDictionary = 
              nodeService.getChildByName(
                    repositoryHelper.getCompanyHome(),
                    ContentModel.ASSOC_CONTAINS,
                    "Data Dictionary"
              );
        rd.getPayload().add( dataDictionary );
        replicationService.saveReplicationDefinition(rd);
        
        response = sendRequest(new GetRequest(URL_DEFINITION + "Test1"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        
        assertEquals("Test1", json.get("name"));
        assertEquals("Testing", json.get("description"));
        assertEquals("CancelRequested", json.get("status"));
        assertEquals(startedAt, json.get("startedAt"));
        assertEquals(JSONObject.NULL, json.get("endedAt"));
        assertEquals(JSONObject.NULL, json.get("failureMessage"));
        assertEquals("/api/running-action/replicationActionExecutor="+
              actionId + "=" + instanceId, json.get("executionDetails"));
        assertEquals(JSONObject.NULL, json.get("transferLocalReport"));
        assertEquals(JSONObject.NULL, json.get("transferRemoteReport"));
        assertEquals(true, json.get("enabled"));
        assertEquals(JSONObject.NULL, json.get("targetName"));
        
        // Check Payload
        assertEquals(2, json.getJSONArray("payload").length());
        
        JSONObject payload = json.getJSONArray("payload").getJSONObject(0);
        assertEquals(repositoryHelper.getCompanyHome().toString(), payload.get("nodeRef"));
        assertEquals(true, payload.get("isFolder"));
        assertEquals("Company Home", payload.get("name"));
        assertEquals("/Company Home", payload.get("path"));

        payload = json.getJSONArray("payload").getJSONObject(1);
        assertEquals(dataDictionary.toString(), payload.get("nodeRef"));
        assertEquals(true, payload.get("isFolder"));
        assertEquals("Data Dictionary", payload.get("name"));
        assertEquals("/Company Home/Data Dictionary", payload.get("path"));

        
        // Add a 2nd and 3rd definition
        rd = replicationService.createReplicationDefinition("Test2", "2nd Testing");
        replicationService.saveReplicationDefinition(rd);
        
        rd = replicationService.createReplicationDefinition("Test3", "3rd Testing");
        rd.setLocalTransferReport( repositoryHelper.getRootHome() );
        rd.setRemoteTransferReport( repositoryHelper.getCompanyHome() );
        rd.setEnabled(false);
        
        // Have the 3rd one flagged as having failed
        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();
        replicationService.saveReplicationDefinition(rd);
        actionTrackingService.recordActionExecuting(rd);
        actionTrackingService.recordActionFailure(rd, new Exception("Test Failure"));
        txn.commit();
        Thread.sleep(50);
        replicationService.saveReplicationDefinition(rd);
        
        
        // Original one comes back unchanged
        response = sendRequest(new GetRequest(URL_DEFINITION + "Test1"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        
        assertEquals("Test1", json.get("name"));
        assertEquals("Testing", json.get("description"));
        assertEquals("CancelRequested", json.get("status"));
        assertEquals(startedAt, json.get("startedAt"));
        assertEquals(JSONObject.NULL, json.get("endedAt"));
        assertEquals(JSONObject.NULL, json.get("failureMessage"));
        assertEquals("/api/running-action/replicationActionExecutor="+
              actionId + "=" + instanceId, json.get("executionDetails"));
        assertEquals(JSONObject.NULL, json.get("transferLocalReport"));
        assertEquals(JSONObject.NULL, json.get("transferRemoteReport"));
        assertEquals(true, json.get("enabled"));
        assertEquals(JSONObject.NULL, json.get("targetName"));
        
        // Check Payload
        assertEquals(2, json.getJSONArray("payload").length());
        
        payload = json.getJSONArray("payload").getJSONObject(0);
        assertEquals(repositoryHelper.getCompanyHome().toString(), payload.get("nodeRef"));
        assertEquals(true, payload.get("isFolder"));
        assertEquals("Company Home", payload.get("name"));
        assertEquals("/Company Home", payload.get("path"));

        payload = json.getJSONArray("payload").getJSONObject(1);
        assertEquals(dataDictionary.toString(), payload.get("nodeRef"));
        assertEquals(true, payload.get("isFolder"));
        assertEquals("Data Dictionary", payload.get("name"));
        assertEquals("/Company Home/Data Dictionary", payload.get("path"));
        
        
        // They show up things as expected
        response = sendRequest(new GetRequest(URL_DEFINITION + "Test2"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        
        assertEquals("Test2", json.get("name"));
        assertEquals("2nd Testing", json.get("description"));
        assertEquals("New", json.get("status"));
        assertEquals(JSONObject.NULL, json.get("startedAt"));
        assertEquals(JSONObject.NULL, json.get("endedAt"));
        assertEquals(JSONObject.NULL, json.get("failureMessage"));
        assertEquals(JSONObject.NULL, json.get("executionDetails"));
        assertEquals(JSONObject.NULL, json.get("transferLocalReport"));
        assertEquals(JSONObject.NULL, json.get("transferRemoteReport"));
        assertEquals(true, json.get("enabled"));
        assertEquals(JSONObject.NULL, json.get("targetName"));
        assertEquals(0, json.getJSONArray("payload").length());
        
        
        // And the 3rd one, which is failed
        response = sendRequest(new GetRequest(URL_DEFINITION + "Test3"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        startedAt = ISO8601DateFormat.format(rd.getExecutionStartDate());
        String endedAt = ISO8601DateFormat.format(rd.getExecutionEndDate());
        
        assertEquals("Test3", json.get("name"));
        assertEquals("3rd Testing", json.get("description"));
        assertEquals("Failed", json.get("status"));
        assertEquals(startedAt, json.get("startedAt"));
        assertEquals(endedAt, json.get("endedAt"));
        assertEquals("Test Failure", json.get("failureMessage"));
        assertEquals(JSONObject.NULL, json.get("executionDetails"));
        assertEquals(repositoryHelper.getRootHome().toString(), json.get("transferLocalReport"));
        assertEquals(repositoryHelper.getCompanyHome().toString(), json.get("transferRemoteReport"));
        assertEquals(false, json.get("enabled"));
        assertEquals(JSONObject.NULL, json.get("targetName"));
        assertEquals(0, json.getJSONArray("payload").length());
    }
    
    public void testReplicationDefinitionsPost() throws Exception
    {
       Response response;
       
       
       // Not allowed if you're not an admin
       AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getGuestUserName());
       response = sendRequest(new PostRequest(URL_DEFINITIONS, "", JSON), Status.STATUS_UNAUTHORIZED);
       assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());
       
       AuthenticationUtil.setFullyAuthenticatedUser(USER_NORMAL);
       response = sendRequest(new PostRequest(URL_DEFINITIONS, "", JSON), Status.STATUS_UNAUTHORIZED);
       assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());

       
       // Ensure there aren't any to start with
       AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
       assertEquals(0, replicationService.loadReplicationDefinitions().size());

       
       // If you don't give it name + description, it won't like you
       JSONObject json = new JSONObject();
       response = sendRequest(new PostRequest(URL_DEFINITIONS, json.toString(), JSON), Status.STATUS_BAD_REQUEST);
       assertEquals(Status.STATUS_BAD_REQUEST, response.getStatus());
       
       json.put("name", "NewDefinition");
       response = sendRequest(new PostRequest(URL_DEFINITIONS, json.toString(), JSON), Status.STATUS_BAD_REQUEST);
       assertEquals(Status.STATUS_BAD_REQUEST, response.getStatus());
       
       
       // If it has both, it'll work
       json.put("description", "Testing");
       response = sendRequest(new PostRequest(URL_DEFINITIONS, json.toString(), JSON), Status.STATUS_OK);
       assertEquals(Status.STATUS_OK, response.getStatus());
       
       
       // Check we got the right information back
       // TODO
       
     
       // Check that the right stuff ended up in the database
       // TODO
       
       
       // Post with the full set of options
       // TODO
       
       
       // Check the response for these
       // TODO
       
       
       // Check the database for these
       // TODO
       
    }
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ApplicationContext appContext = getServer().getApplicationContext();

        nodeService = (NodeService)appContext.getBean("nodeService");
        replicationService = (ReplicationService)appContext.getBean("replicationService");
        actionTrackingService = (ActionTrackingService)appContext.getBean("actionTrackingService");
        repositoryHelper = (Repository)appContext.getBean("repositoryHelper");
        transactionService = (TransactionService)appContext.getBean("transactionService");
        
        MutableAuthenticationService authenticationService = (MutableAuthenticationService)appContext.getBean("AuthenticationService");
        PersonService personService = (PersonService)appContext.getBean("PersonService");
        personManager = new TestPersonManager(authenticationService, personService, nodeService);

        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();
        
        personManager.createPerson(USER_NORMAL);
        
        // Ensure we start with no replication definitions
        // (eg another test left them behind)
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        for(ReplicationDefinition rd : replicationService.loadReplicationDefinitions()) {
           replicationService.deleteReplicationDefinition(rd);
        }
        txn.commit();
        
        AuthenticationUtil.clearCurrentSecurityContext();
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();
        
        personManager.clearPeople();
        
        // Zap any replication definitions we created
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        for(ReplicationDefinition rd : replicationService.loadReplicationDefinitions()) {
           replicationService.deleteReplicationDefinition(rd);
        }
        AuthenticationUtil.clearCurrentSecurityContext();
        
        txn.commit();
    }
}
