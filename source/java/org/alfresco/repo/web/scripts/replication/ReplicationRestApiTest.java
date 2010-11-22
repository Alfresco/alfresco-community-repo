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
import org.alfresco.service.cmr.action.ActionStatus;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.alfresco.service.cmr.replication.ReplicationService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.alfresco.util.ISO8601DateFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Tests for the Replication Webscripts
 * @author Nick Burch
 * 
 * TODO - Scheduling parts
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
    private NodeRef dataDictionary;
    
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
        
        
        // Mark it as pending execution, and re-check
        actionTrackingService.recordActionPending(rd);
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
        assertEquals("Pending", jsonRD.get("status"));
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
        assertEquals(startedAt, jsonRD.getJSONObject("startedAt").get("iso8601"));
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
        assertEquals(startedAt, jsonRD.getJSONObject("startedAt").get("iso8601"));
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
        assertEquals(startedAt, jsonRD.getJSONObject("startedAt").get("iso8601"));
        assertEquals("/api/replication-definition/Test1", jsonRD.get("details"));
                
        
        // Set start times and statuses on these other two
        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();
        rd = replicationService.loadReplicationDefinition("Test2");
        actionTrackingService.recordActionExecuting(rd);
        actionTrackingService.recordActionComplete(rd);
        String startedAt2 = ISO8601DateFormat.format(rd.getExecutionStartDate());
        txn.commit();
        
        
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
        assertEquals(startedAt2, jsonRD.getJSONObject("startedAt").get("iso8601"));
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
        assertEquals(startedAt, jsonRD.getJSONObject("startedAt").get("iso8601"));
        assertEquals("/api/replication-definition/Test1", jsonRD.get("details"));
        
        
        // By last run
        response = sendRequest(new GetRequest(URL_DEFINITIONS + "?sort=lastRun"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(3, results.length());

        // Ran most recently
        jsonRD = (JSONObject)results.get(0);
        assertNotNull(jsonRD);
        assertEquals("Test2", jsonRD.get("name"));
        assertEquals("Completed", jsonRD.get("status"));
        assertEquals(true, jsonRD.get("enabled"));
        assertEquals(startedAt2, jsonRD.getJSONObject("startedAt").get("iso8601"));
        assertEquals("/api/replication-definition/Test2", jsonRD.get("details"));
        
        // Ran least recently
        jsonRD = (JSONObject)results.get(1);
        assertNotNull(jsonRD);
        assertEquals("Test1", jsonRD.get("name"));
        assertEquals("Running", jsonRD.get("status"));
        assertEquals(true, jsonRD.get("enabled"));
        assertEquals(startedAt, jsonRD.getJSONObject("startedAt").get("iso8601"));
        assertEquals("/api/replication-definition/Test1", jsonRD.get("details"));
        
        // Never run last
        jsonRD = (JSONObject)results.get(2);
        assertNotNull(jsonRD);
        assertEquals("AnotherTest", jsonRD.get("name"));
        assertEquals("New", jsonRD.get("status"));
        assertEquals(true, jsonRD.get("enabled"));
        assertEquals(JSONObject.NULL, jsonRD.get("startedAt"));
        assertEquals("/api/replication-definition/AnotherTest", jsonRD.get("details"));
        
       
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
        assertEquals(startedAt3, jsonRD.getJSONObject("startedAt").get("iso8601"));
        assertEquals("/api/replication-definition/AnotherTest", jsonRD.get("details"));
        
        jsonRD = (JSONObject)results.get(1);
        assertNotNull(jsonRD);
        assertEquals("Test1", jsonRD.get("name"));
        assertEquals("Running", jsonRD.get("status"));
        assertEquals(true, jsonRD.get("enabled"));
        assertEquals(startedAt, jsonRD.getJSONObject("startedAt").get("iso8601"));
        assertEquals("/api/replication-definition/Test1", jsonRD.get("details"));
        
        jsonRD = (JSONObject)results.get(2);
        assertNotNull(jsonRD);
        assertEquals("Test2", jsonRD.get("name"));
        assertEquals("Completed", jsonRD.get("status"));
        assertEquals(true, jsonRD.get("enabled"));
        assertEquals(startedAt2, jsonRD.getJSONObject("startedAt").get("iso8601"));
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
        JSONObject json = new JSONObject(jsonStr).getJSONObject("data");
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
               key.equals("enabled") || key.equals("schedule")) {
              // All good
           } else {
              fail("Unexpected key '"+key+"' found in json, raw json is\n" + jsonStr);
           }
        }
        
        
        // Mark it as pending, and check
        actionTrackingService.recordActionPending(rd);
        String actionId = rd.getId();
        int instanceId = ((ActionImpl)rd).getExecutionInstance();
        
        response = sendRequest(new GetRequest(URL_DEFINITION + "Test1"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr).getJSONObject("data");
        
        assertEquals("Test1", json.get("name"));
        assertEquals("Testing", json.get("description"));
        assertEquals("Pending", json.get("status"));
        assertEquals(JSONObject.NULL, json.get("startedAt"));
        assertEquals(JSONObject.NULL, json.get("endedAt"));
        assertEquals(JSONObject.NULL, json.get("failureMessage"));
        assertEquals("/" + URL_RUNNING_ACTION + "replicationActionExecutor="+
              actionId + "=" + instanceId, json.get("executionDetails"));
        assertEquals(JSONObject.NULL, json.get("transferLocalReport"));
        assertEquals(JSONObject.NULL, json.get("transferRemoteReport"));
        assertEquals(true, json.get("enabled"));
        assertEquals(JSONObject.NULL, json.get("targetName"));
        // Payload is empty
        assertEquals(0, json.getJSONArray("payload").length());
        
        
        // Change the status to running, and re-check
        actionTrackingService.recordActionExecuting(rd);
        assertEquals(actionId, rd.getId());
        assertEquals(instanceId, ((ActionImpl)rd).getExecutionInstance());
        String startedAt = ISO8601DateFormat.format(rd.getExecutionStartDate());
        
        response = sendRequest(new GetRequest(URL_DEFINITION + "Test1"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr).getJSONObject("data");
        
        assertEquals("Test1", json.get("name"));
        assertEquals("Testing", json.get("description"));
        assertEquals("Running", json.get("status"));
        assertEquals(startedAt, json.getJSONObject("startedAt").get("iso8601"));
        assertEquals(JSONObject.NULL, json.get("endedAt"));
        assertEquals(JSONObject.NULL, json.get("failureMessage"));
        assertEquals("/" + URL_RUNNING_ACTION + "replicationActionExecutor="+
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
        json = new JSONObject(jsonStr).getJSONObject("data");
        
        assertEquals("Test1", json.get("name"));
        assertEquals("Testing", json.get("description"));
        assertEquals("CancelRequested", json.get("status"));
        assertEquals(startedAt, json.getJSONObject("startedAt").get("iso8601"));
        assertEquals(JSONObject.NULL, json.get("endedAt"));
        assertEquals(JSONObject.NULL, json.get("failureMessage"));
        assertEquals("/" + URL_RUNNING_ACTION + "replicationActionExecutor="+
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
        rd.getPayload().add( dataDictionary );
        replicationService.saveReplicationDefinition(rd);
        
        response = sendRequest(new GetRequest(URL_DEFINITION + "Test1"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr).getJSONObject("data");
        
        assertEquals("Test1", json.get("name"));
        assertEquals("Testing", json.get("description"));
        assertEquals("CancelRequested", json.get("status"));
        assertEquals(startedAt, json.getJSONObject("startedAt").get("iso8601"));
        assertEquals(JSONObject.NULL, json.get("endedAt"));
        assertEquals(JSONObject.NULL, json.get("failureMessage"));
        assertEquals("/" + URL_RUNNING_ACTION + "replicationActionExecutor="+
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
        
        
        // Add a deleted NodeRef too, will be silently ignored
        //  by the webscript layer
        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();
        NodeRef deleted = nodeService.createNode(
              dataDictionary, ContentModel.ASSOC_CONTAINS,
              QName.createQName("IwillBEdeleted"),
              ContentModel.TYPE_CONTENT
        ).getChildRef();
        nodeService.deleteNode(deleted);
        txn.commit();
        
        rd.getPayload().add( deleted );
        replicationService.saveReplicationDefinition(rd);
        
        response = sendRequest(new GetRequest(URL_DEFINITION + "Test1"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr).getJSONObject("data");
        
        assertEquals("Test1", json.get("name"));
        assertEquals("Testing", json.get("description"));
        assertEquals("CancelRequested", json.get("status"));
        assertEquals(startedAt, json.getJSONObject("startedAt").get("iso8601"));
        assertEquals(JSONObject.NULL, json.get("endedAt"));
        assertEquals(JSONObject.NULL, json.get("failureMessage"));
        assertEquals("/" + URL_RUNNING_ACTION + "replicationActionExecutor="+
              actionId + "=" + instanceId, json.get("executionDetails"));
        assertEquals(JSONObject.NULL, json.get("transferLocalReport"));
        assertEquals(JSONObject.NULL, json.get("transferRemoteReport"));
        assertEquals(true, json.get("enabled"));
        assertEquals(JSONObject.NULL, json.get("targetName"));
        
        // Check Payload
        assertEquals(2, json.getJSONArray("payload").length());
        payload = json.getJSONArray("payload").getJSONObject(0);
        assertEquals("Company Home", payload.get("name"));
        payload = json.getJSONArray("payload").getJSONObject(1);
        assertEquals("Data Dictionary", payload.get("name"));

        
        // Add a 2nd and 3rd definition
        rd = replicationService.createReplicationDefinition("Test2", "2nd Testing");
        replicationService.saveReplicationDefinition(rd);
        
        rd = replicationService.createReplicationDefinition("Test3", "3rd Testing");
        rd.setLocalTransferReport( repositoryHelper.getRootHome() );
        rd.setRemoteTransferReport( repositoryHelper.getCompanyHome() );
        rd.setEnabled(false);
        
        // Have the 3rd one flagged as having failed
        txn = transactionService.getUserTransaction();
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
        json = new JSONObject(jsonStr).getJSONObject("data");
        
        assertEquals("Test1", json.get("name"));
        assertEquals("Testing", json.get("description"));
        assertEquals("CancelRequested", json.get("status"));
        assertEquals(startedAt, json.getJSONObject("startedAt").get("iso8601"));
        assertEquals(JSONObject.NULL, json.get("endedAt"));
        assertEquals(JSONObject.NULL, json.get("failureMessage"));
        assertEquals("/" + URL_RUNNING_ACTION + "replicationActionExecutor="+
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
        json = new JSONObject(jsonStr).getJSONObject("data");
        
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
        json = new JSONObject(jsonStr).getJSONObject("data");
        startedAt = ISO8601DateFormat.format(rd.getExecutionStartDate());
        String endedAt = ISO8601DateFormat.format(rd.getExecutionEndDate());
        
        assertEquals("Test3", json.get("name"));
        assertEquals("3rd Testing", json.get("description"));
        assertEquals("Failed", json.get("status"));
        assertEquals(startedAt, json.getJSONObject("startedAt").get("iso8601"));
        assertEquals(endedAt, json.getJSONObject("endedAt").get("iso8601"));
        assertEquals("Test Failure", json.get("failureMessage"));
        assertEquals(JSONObject.NULL, json.get("executionDetails"));
        assertEquals(repositoryHelper.getRootHome().toString(), json.get("transferLocalReport"));
        assertEquals(repositoryHelper.getCompanyHome().toString(), json.get("transferRemoteReport"));
        assertEquals(false, json.get("enabled"));
        assertEquals(JSONObject.NULL, json.get("targetName"));
        assertEquals(0, json.getJSONArray("payload").length());
        
        
        // When pending/running, the previous end time, transfer reports and
        //  failure details are hidden
        rd = replicationService.loadReplicationDefinition("Test3");
        assertEquals(0, actionTrackingService.getExecutingActions(rd).size());
        actionTrackingService.recordActionPending(rd);
        assertEquals(1, actionTrackingService.getExecutingActions(rd).size());
        instanceId = ((ActionImpl)rd).getExecutionInstance();
        actionId = rd.getId();
        
        response = sendRequest(new GetRequest(URL_DEFINITION + "Test3"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr).getJSONObject("data");
        
        assertEquals("Test3", json.get("name"));
        assertEquals("3rd Testing", json.get("description"));
        assertEquals("Pending", json.get("status"));
        assertEquals(JSONObject.NULL, json.get("startedAt"));
        assertEquals(JSONObject.NULL, json.get("endedAt"));
        assertEquals(JSONObject.NULL, json.get("failureMessage"));
        assertEquals("/" + URL_RUNNING_ACTION + "replicationActionExecutor="+
              actionId + "=" + instanceId, json.get("executionDetails"));
        assertEquals(JSONObject.NULL, json.get("transferLocalReport"));
        assertEquals(JSONObject.NULL, json.get("transferRemoteReport"));
        assertEquals(false, json.get("enabled"));
        assertEquals(JSONObject.NULL, json.get("targetName"));
        assertEquals(0, json.getJSONArray("payload").length());
        
        
        actionTrackingService.recordActionExecuting(rd);
        response = sendRequest(new GetRequest(URL_DEFINITION + "Test3"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr).getJSONObject("data");
        startedAt = ISO8601DateFormat.format(rd.getExecutionStartDate());
        
        assertEquals("Test3", json.get("name"));
        assertEquals("3rd Testing", json.get("description"));
        assertEquals("Running", json.get("status"));
        assertEquals(startedAt, json.getJSONObject("startedAt").get("iso8601"));
        assertEquals(JSONObject.NULL, json.get("endedAt"));
        assertEquals(JSONObject.NULL, json.get("failureMessage"));
        assertEquals("/" + URL_RUNNING_ACTION + "replicationActionExecutor="+
              actionId + "=" + instanceId, json.get("executionDetails"));
        assertEquals(JSONObject.NULL, json.get("transferLocalReport"));
        assertEquals(JSONObject.NULL, json.get("transferRemoteReport"));
        assertEquals(false, json.get("enabled"));
        assertEquals(JSONObject.NULL, json.get("targetName"));
        assertEquals(0, json.getJSONArray("payload").length());
        
        
        actionTrackingService.requestActionCancellation(rd);
        response = sendRequest(new GetRequest(URL_DEFINITION + "Test3"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr).getJSONObject("data");
        startedAt = ISO8601DateFormat.format(rd.getExecutionStartDate());
        
        assertEquals("Test3", json.get("name"));
        assertEquals("3rd Testing", json.get("description"));
        assertEquals("CancelRequested", json.get("status"));
        assertEquals(startedAt, json.getJSONObject("startedAt").get("iso8601"));
        assertEquals(JSONObject.NULL, json.get("endedAt"));
        assertEquals(JSONObject.NULL, json.get("failureMessage"));
        assertEquals("/" + URL_RUNNING_ACTION + "replicationActionExecutor="+
              actionId + "=" + instanceId, json.get("executionDetails"));
        assertEquals(JSONObject.NULL, json.get("transferLocalReport"));
        assertEquals(JSONObject.NULL, json.get("transferRemoteReport"));
        assertEquals(false, json.get("enabled"));
        assertEquals(JSONObject.NULL, json.get("targetName"));
        assertEquals(0, json.getJSONArray("payload").length());

        
        // These show up again when no longer running
        txn = transactionService.getUserTransaction();
        txn.begin();
        actionTrackingService.recordActionComplete(rd);
        txn.commit();
        response = sendRequest(new GetRequest(URL_DEFINITION + "Test3"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr).getJSONObject("data");
        startedAt = ISO8601DateFormat.format(rd.getExecutionStartDate());
        endedAt = ISO8601DateFormat.format(rd.getExecutionEndDate());
        
        assertEquals("Test3", json.get("name"));
        assertEquals("3rd Testing", json.get("description"));
        assertEquals("Completed", json.get("status"));
        assertEquals(startedAt, json.getJSONObject("startedAt").get("iso8601"));
        assertEquals(endedAt, json.getJSONObject("endedAt").get("iso8601"));
        assertEquals(JSONObject.NULL, json.get("failureMessage"));
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
       
       json.put("name", "New Definition");
       response = sendRequest(new PostRequest(URL_DEFINITIONS, json.toString(), JSON), Status.STATUS_BAD_REQUEST);
       assertEquals(Status.STATUS_BAD_REQUEST, response.getStatus());
       
       
       // If it has both, it'll work
       json.put("description", "Testing");
       response = sendRequest(new PostRequest(URL_DEFINITIONS, json.toString(), JSON), Status.STATUS_OK);
       assertEquals(Status.STATUS_OK, response.getStatus());
       
       
       // Check we got the right information back
       String jsonStr = response.getContentAsString();
       json = new JSONObject(jsonStr).getJSONObject("data");
       assertNotNull(json);
       
       assertEquals("New Definition", json.get("name"));
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
       assertEquals(0, json.getJSONArray("payload").length());
       
     
       // Check that the right stuff ended up in the repository
       ReplicationDefinition rd = replicationService.loadReplicationDefinition("New Definition");
       assertEquals("New Definition", rd.getReplicationName());
       assertEquals("Testing", rd.getDescription());
       assertEquals(ActionStatus.New, rd.getExecutionStatus());
       assertEquals(null, rd.getExecutionStartDate());
       assertEquals(null, rd.getExecutionEndDate());
       assertEquals(null, rd.getExecutionFailureMessage());
       assertEquals(null, rd.getLocalTransferReport());
       assertEquals(null, rd.getRemoteTransferReport());
       assertEquals(null, rd.getTargetName());
       assertEquals(0, rd.getPayload().size());
       assertEquals(true, rd.isEnabled());
       
       
       // Post with the full set of options
       json = new JSONObject();
       json.put("name", "Test");
       json.put("description", "Test Description");
       json.put("targetName", "Target");
       json.put("enabled", false);
       JSONArray payloadRefs = new JSONArray();
       payloadRefs.put(repositoryHelper.getCompanyHome().toString());
       payloadRefs.put(dataDictionary.toString());
       json.put("payload", payloadRefs);
       
       response = sendRequest(new PostRequest(URL_DEFINITIONS, json.toString(), JSON), Status.STATUS_OK);
       assertEquals(Status.STATUS_OK, response.getStatus());
       
       
       // Check the response for this
       jsonStr = response.getContentAsString();
       json = new JSONObject(jsonStr).getJSONObject("data");
       assertNotNull(json);
       
       assertEquals("Test", json.get("name"));
       assertEquals("Test Description", json.get("description"));
       assertEquals("New", json.get("status"));
       assertEquals(JSONObject.NULL, json.get("startedAt"));
       assertEquals(JSONObject.NULL, json.get("endedAt"));
       assertEquals(JSONObject.NULL, json.get("failureMessage"));
       assertEquals(JSONObject.NULL, json.get("executionDetails"));
       assertEquals(JSONObject.NULL, json.get("transferLocalReport"));
       assertEquals(JSONObject.NULL, json.get("transferRemoteReport"));
       assertEquals(false, json.get("enabled"));
       assertEquals("Target", json.get("targetName"));
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
       
       
       // Check the database for this
       rd = replicationService.loadReplicationDefinition("Test");
       assertEquals("Test", rd.getReplicationName());
       assertEquals("Test Description", rd.getDescription());
       assertEquals(ActionStatus.New, rd.getExecutionStatus());
       assertEquals(null, rd.getExecutionStartDate());
       assertEquals(null, rd.getExecutionEndDate());
       assertEquals(null, rd.getExecutionFailureMessage());
       assertEquals(null, rd.getLocalTransferReport());
       assertEquals(null, rd.getRemoteTransferReport());
       assertEquals("Target", rd.getTargetName());
       assertEquals(false, rd.isEnabled());       
       assertEquals(2, rd.getPayload().size());
       assertEquals(repositoryHelper.getCompanyHome(), rd.getPayload().get(0));
       assertEquals(dataDictionary, rd.getPayload().get(1));

       
       // Ensure that the original one wasn't changed by anything
       rd = replicationService.loadReplicationDefinition("New Definition");
       assertEquals("New Definition", rd.getReplicationName());
       assertEquals("Testing", rd.getDescription());
       assertEquals(ActionStatus.New, rd.getExecutionStatus());
       assertEquals(null, rd.getExecutionStartDate());
       assertEquals(null, rd.getExecutionEndDate());
       assertEquals(null, rd.getExecutionFailureMessage());
       assertEquals(null, rd.getLocalTransferReport());
       assertEquals(null, rd.getRemoteTransferReport());
       assertEquals(null, rd.getTargetName());
       assertEquals(0, rd.getPayload().size());
       assertEquals(true, rd.isEnabled());
       
       
       // Ensure we can't create with a duplicate name
       json = new JSONObject();
       json.put("name", "Test");
       json.put("description", "New Duplicate");
       json.put("targetName", "New Duplicate Target");
       
       response = sendRequest(new PostRequest(URL_DEFINITIONS, json.toString(), JSON), Status.STATUS_BAD_REQUEST);
       assertEquals(Status.STATUS_BAD_REQUEST, response.getStatus());
       
       // Ensure that even though we got BAD REQUEST back, nothing changed
       rd = replicationService.loadReplicationDefinition("New Definition");
       assertEquals("New Definition", rd.getReplicationName());
       assertEquals("Testing", rd.getDescription());
       assertEquals(ActionStatus.New, rd.getExecutionStatus());
       assertEquals(null, rd.getExecutionStartDate());
       assertEquals(null, rd.getExecutionEndDate());
       assertEquals(null, rd.getExecutionFailureMessage());
       assertEquals(null, rd.getLocalTransferReport());
       assertEquals(null, rd.getRemoteTransferReport());
       assertEquals(null, rd.getTargetName());
       assertEquals(0, rd.getPayload().size());
       assertEquals(true, rd.isEnabled());
    }
    
    public void testReplicationDefinitionPut() throws Exception
    {
       Response response;
       
       
       // Not allowed if you're not an admin
       AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getGuestUserName());
       response = sendRequest(new PutRequest(URL_DEFINITION + "MadeUp", "", JSON), Status.STATUS_UNAUTHORIZED);
       assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());
       
       AuthenticationUtil.setFullyAuthenticatedUser(USER_NORMAL);
       response = sendRequest(new PutRequest(URL_DEFINITION + "MadeUp", "", JSON), Status.STATUS_UNAUTHORIZED);
       assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());

       
       // Ensure there aren't any to start with
       AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
       assertEquals(0, replicationService.loadReplicationDefinitions().size());
       
       
       // You need to specify a real definition
       response = sendRequest(new PutRequest(URL_DEFINITION + "MadeUp", "", JSON), Status.STATUS_NOT_FOUND);
       assertEquals(Status.STATUS_NOT_FOUND, response.getStatus());
       
       
       // Create one, and change it
       ReplicationDefinition rd = replicationService.createReplicationDefinition("Test", "Testing");
       replicationService.saveReplicationDefinition(rd);
       
       response = sendRequest(new PutRequest(URL_DEFINITION + "Test", "{}", JSON), Status.STATUS_OK);
       assertEquals(Status.STATUS_OK, response.getStatus());
       
       
       // Check we got the right information back on it
       String jsonStr = response.getContentAsString();
       JSONObject json = new JSONObject(jsonStr).getJSONObject("data");
       assertNotNull(json);
       
       assertEquals("Test", json.get("name"));
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
              key.equals("enabled") || key.equals("schedule")) {
             // All good
          } else {
             fail("Unexpected key '"+key+"' found in json, raw json is\n" + jsonStr);
          }
       }
       
       
       
       // Change some details, and see them updated in both
       //  the JSON and on the object in the repo
       json = new JSONObject();
       json.put("description", "Updated Description");
       json.put("enabled", false);
       response = sendRequest(new PutRequest(URL_DEFINITION + "Test", json.toString(), JSON), Status.STATUS_OK);
       assertEquals(Status.STATUS_OK, response.getStatus());
       
       jsonStr = response.getContentAsString();
       json = new JSONObject(jsonStr).getJSONObject("data");
       assertNotNull(json);
       
       assertEquals("Test", json.get("name"));
       assertEquals("Updated Description", json.get("description"));
       assertEquals("New", json.get("status"));
       assertEquals(JSONObject.NULL, json.get("startedAt"));
       assertEquals(JSONObject.NULL, json.get("endedAt"));
       assertEquals(JSONObject.NULL, json.get("failureMessage"));
       assertEquals(JSONObject.NULL, json.get("executionDetails"));
       assertEquals(JSONObject.NULL, json.get("transferLocalReport"));
       assertEquals(JSONObject.NULL, json.get("transferRemoteReport"));
       assertEquals(false, json.get("enabled"));
       assertEquals(JSONObject.NULL, json.get("targetName"));
       assertEquals(0, json.getJSONArray("payload").length());
       
       rd = replicationService.loadReplicationDefinition("Test");
       assertEquals("Test", rd.getReplicationName());
       assertEquals("Updated Description", rd.getDescription());
       assertEquals(ActionStatus.New, rd.getExecutionStatus());
       assertEquals(null, rd.getExecutionStartDate());
       assertEquals(null, rd.getExecutionEndDate());
       assertEquals(null, rd.getExecutionFailureMessage());
       assertEquals(null, rd.getLocalTransferReport());
       assertEquals(null, rd.getRemoteTransferReport());
       assertEquals(null, rd.getTargetName());
       assertEquals(0, rd.getPayload().size());
       assertEquals(false, rd.isEnabled());
       
       
       
       // Create a 2nd definition, and check that the correct
       //  one gets updated
       rd = replicationService.createReplicationDefinition("Test2", "Testing2");
       rd.setTargetName("Target");
       replicationService.saveReplicationDefinition(rd);
       
       json = new JSONObject();
       json.put("description", "Updated Description 2");
       json.put("enabled", false);
       response = sendRequest(new PutRequest(URL_DEFINITION + "Test2", json.toString(), JSON), Status.STATUS_OK);
       assertEquals(Status.STATUS_OK, response.getStatus());
       
       // Check the response we got
       jsonStr = response.getContentAsString();
       json = new JSONObject(jsonStr).getJSONObject("data");
       assertNotNull(json);
       
       assertEquals("Test2", json.get("name"));
       assertEquals("Updated Description 2", json.get("description"));
       assertEquals("New", json.get("status"));
       assertEquals(JSONObject.NULL, json.get("startedAt"));
       assertEquals(JSONObject.NULL, json.get("endedAt"));
       assertEquals(JSONObject.NULL, json.get("failureMessage"));
       assertEquals(JSONObject.NULL, json.get("executionDetails"));
       assertEquals(JSONObject.NULL, json.get("transferLocalReport"));
       assertEquals(JSONObject.NULL, json.get("transferRemoteReport"));
       assertEquals(false, json.get("enabled"));
       assertEquals("Target", json.get("targetName"));
       assertEquals(0, json.getJSONArray("payload").length());
       
       // Check the 1st definition
       rd = replicationService.loadReplicationDefinition("Test");
       assertEquals("Test", rd.getReplicationName());
       assertEquals("Updated Description", rd.getDescription());
       assertEquals(ActionStatus.New, rd.getExecutionStatus());
       assertEquals(null, rd.getExecutionStartDate());
       assertEquals(null, rd.getExecutionEndDate());
       assertEquals(null, rd.getExecutionFailureMessage());
       assertEquals(null, rd.getLocalTransferReport());
       assertEquals(null, rd.getRemoteTransferReport());
       assertEquals(null, rd.getTargetName());
       assertEquals(0, rd.getPayload().size());
       assertEquals(false, rd.isEnabled());
       
       // Check the 2nd definition
       rd = replicationService.loadReplicationDefinition("Test2");
       assertEquals("Test2", rd.getReplicationName());
       assertEquals("Updated Description 2", rd.getDescription());
       assertEquals(ActionStatus.New, rd.getExecutionStatus());
       assertEquals(null, rd.getExecutionStartDate());
       assertEquals(null, rd.getExecutionEndDate());
       assertEquals(null, rd.getExecutionFailureMessage());
       assertEquals(null, rd.getLocalTransferReport());
       assertEquals(null, rd.getRemoteTransferReport());
       assertEquals("Target", rd.getTargetName());
       assertEquals(0, rd.getPayload().size());
       assertEquals(false, rd.isEnabled());
       
       
       // Mark it as running, then change some details and
       //  see it change as expected
       rd = replicationService.loadReplicationDefinition("Test");
       actionTrackingService.recordActionExecuting(rd);
       replicationService.saveReplicationDefinition(rd);
       String startedAt = ISO8601DateFormat.format(rd.getExecutionStartDate());
       String actionId = rd.getId();
       int instanceId = ((ActionImpl)rd).getExecutionInstance();
       
       json = new JSONObject();
       json.put("enabled", true);
       json.put("targetName", "Another Target");
       response = sendRequest(new PutRequest(URL_DEFINITION + "Test", json.toString(), JSON), Status.STATUS_OK);
       assertEquals(Status.STATUS_OK, response.getStatus());
       
       jsonStr = response.getContentAsString();
       json = new JSONObject(jsonStr).getJSONObject("data");
       assertNotNull(json);
       
       assertEquals("Test", json.get("name"));
       assertEquals("Updated Description", json.get("description"));
       assertEquals("Running", json.get("status"));
       assertEquals(startedAt, json.getJSONObject("startedAt").get("iso8601"));
       assertEquals(JSONObject.NULL, json.get("endedAt"));
       assertEquals(JSONObject.NULL, json.get("failureMessage"));
       assertEquals("/" + URL_RUNNING_ACTION + "replicationActionExecutor="+
             actionId + "=" + instanceId, json.get("executionDetails"));
       assertEquals(JSONObject.NULL, json.get("transferLocalReport"));
       assertEquals(JSONObject.NULL, json.get("transferRemoteReport"));
       assertEquals(true, json.get("enabled"));
       assertEquals("Another Target", json.get("targetName"));
       assertEquals(0, json.getJSONArray("payload").length());

       
       // Change the payload, and see the right information in
       //  the response JSON for it
       JSONArray payloadRefs = new JSONArray();
       payloadRefs.put(repositoryHelper.getCompanyHome().toString());
       payloadRefs.put(dataDictionary.toString());
       json = new JSONObject();
       json.put("payload", payloadRefs);
       
       response = sendRequest(new PutRequest(URL_DEFINITION + "Test", json.toString(), JSON), Status.STATUS_OK);
       assertEquals(Status.STATUS_OK, response.getStatus());
       
       jsonStr = response.getContentAsString();
       json = new JSONObject(jsonStr).getJSONObject("data");
       assertNotNull(json);
       
       assertEquals("Test", json.get("name"));
       assertEquals("Updated Description", json.get("description"));
       assertEquals("Running", json.get("status"));
       assertEquals(startedAt, json.getJSONObject("startedAt").get("iso8601"));
       assertEquals(JSONObject.NULL, json.get("endedAt"));
       assertEquals(JSONObject.NULL, json.get("failureMessage"));
       assertEquals("/" + URL_RUNNING_ACTION + "replicationActionExecutor="+
             actionId + "=" + instanceId, json.get("executionDetails"));
       assertEquals(JSONObject.NULL, json.get("transferLocalReport"));
       assertEquals(JSONObject.NULL, json.get("transferRemoteReport"));
       assertEquals(true, json.get("enabled"));
       assertEquals("Another Target", json.get("targetName"));
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
       
       
       // Remove the payload again
       json = new JSONObject();
       payloadRefs = new JSONArray();
       json.put("payload", payloadRefs);
       
       response = sendRequest(new PutRequest(URL_DEFINITION + "Test", json.toString(), JSON), Status.STATUS_OK);
       assertEquals(Status.STATUS_OK, response.getStatus());
       
       jsonStr = response.getContentAsString();
       json = new JSONObject(jsonStr).getJSONObject("data");
       assertNotNull(json);
       
       assertEquals("Test", json.get("name"));
       assertEquals("Updated Description", json.get("description"));
       assertEquals("Running", json.get("status"));
       assertEquals(startedAt, json.getJSONObject("startedAt").get("iso8601"));
       assertEquals(JSONObject.NULL, json.get("endedAt"));
       assertEquals(JSONObject.NULL, json.get("failureMessage"));
       assertEquals("/" + URL_RUNNING_ACTION + "replicationActionExecutor="+
             actionId + "=" + instanceId, json.get("executionDetails"));
       assertEquals(JSONObject.NULL, json.get("transferLocalReport"));
       assertEquals(JSONObject.NULL, json.get("transferRemoteReport"));
       assertEquals(true, json.get("enabled"));
       assertEquals("Another Target", json.get("targetName"));
       assertEquals(0, json.getJSONArray("payload").length());

       
       // Rename to a taken name, won't be allowed
       json = new JSONObject();
       json.put("name", "Test2");
       response = sendRequest(new PutRequest(URL_DEFINITION + "Test", json.toString(), JSON), Status.STATUS_BAD_REQUEST);
       assertEquals(Status.STATUS_BAD_REQUEST, response.getStatus());
       
       
       // Rename to a spare name, will be updated
       json = new JSONObject();
       json.put("name", "Renamed");
       
       response = sendRequest(new PutRequest(URL_DEFINITION + "Test", json.toString(), JSON), Status.STATUS_OK);
       assertEquals(Status.STATUS_OK, response.getStatus());
       
       jsonStr = response.getContentAsString();
       json = new JSONObject(jsonStr).getJSONObject("data");
       assertNotNull(json);
       
       assertEquals("Renamed", json.get("name"));
       assertEquals("Updated Description", json.get("description"));
       assertEquals("Running", json.get("status"));
       assertEquals(startedAt, json.getJSONObject("startedAt").get("iso8601"));
       assertEquals(JSONObject.NULL, json.get("endedAt"));
       assertEquals(JSONObject.NULL, json.get("failureMessage"));
       assertEquals("/" + URL_RUNNING_ACTION + "replicationActionExecutor="+
             actionId + "=" + instanceId, json.get("executionDetails"));
       assertEquals(JSONObject.NULL, json.get("transferLocalReport"));
       assertEquals(JSONObject.NULL, json.get("transferRemoteReport"));
       assertEquals(true, json.get("enabled"));
       assertEquals("Another Target", json.get("targetName"));
       assertEquals(0, json.getJSONArray("payload").length());
       
       // Check the repo too
       assertEquals(null, replicationService.loadReplicationDefinition("Test"));
       assertNotNull(replicationService.loadReplicationDefinition("Renamed"));

       
       // Rename can both rename + change details
       json = new JSONObject();
       json.put("name", "Renamed Again");
       json.put("description", "Was Renamed");
       json.put("targetName", "New Target");
       
       response = sendRequest(new PutRequest(URL_DEFINITION + "Renamed", json.toString(), JSON), Status.STATUS_OK);
       assertEquals(Status.STATUS_OK, response.getStatus());
       
       jsonStr = response.getContentAsString();
       json = new JSONObject(jsonStr).getJSONObject("data");
       assertNotNull(json);
       
       assertEquals("Renamed Again", json.get("name"));
       assertEquals("Was Renamed", json.get("description"));
       assertEquals("Running", json.get("status"));
       assertEquals(startedAt, json.getJSONObject("startedAt").get("iso8601"));
       assertEquals(JSONObject.NULL, json.get("endedAt"));
       assertEquals(JSONObject.NULL, json.get("failureMessage"));
       assertEquals("/" + URL_RUNNING_ACTION + "replicationActionExecutor="+
             actionId + "=" + instanceId, json.get("executionDetails"));
       assertEquals(JSONObject.NULL, json.get("transferLocalReport"));
       assertEquals(JSONObject.NULL, json.get("transferRemoteReport"));
       assertEquals(true, json.get("enabled"));
       assertEquals("New Target", json.get("targetName"));
       assertEquals(0, json.getJSONArray("payload").length());
    }
    
    public void testReplicationDefinitionDelete() throws Exception 
    {
       Response response;
       
       
       // Not allowed if you're not an admin
       AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getGuestUserName());
       response = sendRequest(new DeleteRequest(URL_DEFINITION + "MadeUp"), Status.STATUS_UNAUTHORIZED);
       assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());
       
       AuthenticationUtil.setFullyAuthenticatedUser(USER_NORMAL);
       response = sendRequest(new DeleteRequest(URL_DEFINITION + "MadeUp"), Status.STATUS_UNAUTHORIZED);
       assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());

       
       // Ensure there aren't any to start with
       AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
       assertEquals(0, replicationService.loadReplicationDefinitions().size());
       
       
       // You need to specify a real definition
       response = sendRequest(new DeleteRequest(URL_DEFINITION + "MadeUp"), Status.STATUS_NOT_FOUND);
       assertEquals(Status.STATUS_NOT_FOUND, response.getStatus());
       
       
       // Create one, and then delete it
       ReplicationDefinition rd = replicationService.createReplicationDefinition("Test", "Testing");
       replicationService.saveReplicationDefinition(rd);
       assertEquals(1, replicationService.loadReplicationDefinitions().size());
       
       // Because some of the delete operations happen post-commit, and
       //  because we don't have real transactions, fake it
       UserTransaction txn = transactionService.getUserTransaction();
       txn.begin();
       
       // Call the delete webscript
       response = sendRequest(new DeleteRequest(URL_DEFINITION + "Test"), Status.STATUS_NO_CONTENT);
       assertEquals(Status.STATUS_NO_CONTENT, response.getStatus());
       
       // Let the node service do its work
       txn.commit();
       Thread.sleep(50);
       
       
       // Check the details webscript to ensure it went
       response = sendRequest(new GetRequest(URL_DEFINITION + "Test"), Status.STATUS_NOT_FOUND);
       assertEquals(Status.STATUS_NOT_FOUND, response.getStatus());
       
       
       // Check the replication service to ensure it went
       assertNull(replicationService.loadReplicationDefinition("Test"));
       assertEquals(0, replicationService.loadReplicationDefinitions().size());
       
       
       // If there are several, make sure the right one goes
       rd = replicationService.createReplicationDefinition("Test", "Testing");
       replicationService.saveReplicationDefinition(rd);
       rd = replicationService.createReplicationDefinition("Test 2", "Testing");
       replicationService.saveReplicationDefinition(rd);
       rd = replicationService.createReplicationDefinition("Test 3", "Testing");
       replicationService.saveReplicationDefinition(rd);
       
       // Delete one of three, correct one goes
       assertEquals(3, replicationService.loadReplicationDefinitions().size());
       
       txn = transactionService.getUserTransaction();
       txn.begin();
       
       response = sendRequest(new DeleteRequest(URL_DEFINITION + "Test"), Status.STATUS_NO_CONTENT);
       assertEquals(Status.STATUS_NO_CONTENT, response.getStatus());
       
       txn.commit();
       Thread.sleep(50);
       
       assertEquals(2, replicationService.loadReplicationDefinitions().size());
       assertNull(replicationService.loadReplicationDefinition("Test"));
       assertNotNull(replicationService.loadReplicationDefinition("Test 2"));
       assertNotNull(replicationService.loadReplicationDefinition("Test 3"));
       
       // Delete the next one, correct one goes
       txn = transactionService.getUserTransaction();
       txn.begin();
       
       response = sendRequest(new DeleteRequest(URL_DEFINITION + "Test 3"), Status.STATUS_NO_CONTENT);
       assertEquals(Status.STATUS_NO_CONTENT, response.getStatus());
       
       txn.commit();
       Thread.sleep(50);
       
       assertEquals(1, replicationService.loadReplicationDefinitions().size());
       assertNull(replicationService.loadReplicationDefinition("Test"));
       assertNotNull(replicationService.loadReplicationDefinition("Test 2"));
       assertNull(replicationService.loadReplicationDefinition("Test 3"));
       
       
       // Ensure you can't delete for a 2nd time
       txn = transactionService.getUserTransaction();
       txn.begin();
       
       response = sendRequest(new DeleteRequest(URL_DEFINITION + "Test 3"), Status.STATUS_NOT_FOUND);
       assertEquals(Status.STATUS_NOT_FOUND, response.getStatus());
       
       txn.commit();
       Thread.sleep(50);
       
       assertEquals(1, replicationService.loadReplicationDefinitions().size());
       assertNull(replicationService.loadReplicationDefinition("Test"));
       assertNotNull(replicationService.loadReplicationDefinition("Test 2"));
       assertNull(replicationService.loadReplicationDefinition("Test 3"));
    }
    
    /**
     * Test that when creating and working with replication
     *  definitions with a name that includes "nasty"
     *  characters, things still work.
     * Related to ALF-4610.
     */
    public void testReplicationDefinitionsNastyNames() throws Exception
    {
       AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
       Response response;
       String jsonStr;
       
       String nastyName = "~!@#$%^&()_+-={}[];";
       String nastyNameURL = URLEncoder.encodeUriComponent(nastyName);
       
       
       // Create
       JSONObject json = new JSONObject();
       json.put("name", nastyName);
       json.put("description", "Nasty Characters");
       response = sendRequest(new PostRequest(URL_DEFINITIONS, json.toString(), JSON), Status.STATUS_OK);
       assertEquals(Status.STATUS_OK, response.getStatus());
       
       jsonStr = response.getContentAsString();
       json = new JSONObject(jsonStr).getJSONObject("data");
       assertNotNull(json);
       
       assertEquals(nastyName, json.get("name"));
       assertEquals("Nasty Characters", json.get("description"));
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
       
       
       // Check it turned up
       assertEquals(1, replicationService.loadReplicationDefinitions().size());
       assertEquals(nastyName, replicationService.loadReplicationDefinitions().get(0).getReplicationName());
       
       
       // Fetch the details
       response = sendRequest(new GetRequest(URL_DEFINITION + nastyNameURL), 200);
       assertEquals(Status.STATUS_OK, response.getStatus());
       
       jsonStr = response.getContentAsString();
       json = new JSONObject(jsonStr).getJSONObject("data");
       assertNotNull(json);
       
       assertEquals(nastyName, json.get("name"));
       assertEquals("Nasty Characters", json.get("description"));
       assertEquals("New", json.get("status"));
       
       
       // Delete
       // Because some of the delete operations happen post-commit, and
       //  because we don't have real transactions, fake it
       UserTransaction txn = transactionService.getUserTransaction();
       txn.begin();
       
       // Call the delete webscript
       response = sendRequest(new DeleteRequest(URL_DEFINITION + nastyNameURL), Status.STATUS_NO_CONTENT);
       assertEquals(Status.STATUS_NO_CONTENT, response.getStatus());
       
       // Let the node service do its work
       txn.commit();
       Thread.sleep(50);
       
       // Check the details webscript to ensure it went
       response = sendRequest(new GetRequest(URL_DEFINITION + nastyNameURL), Status.STATUS_NOT_FOUND);
       assertEquals(Status.STATUS_NOT_FOUND, response.getStatus());
       
       // And check the service too
       assertEquals(0, replicationService.loadReplicationDefinitions().size());
    }
    
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ApplicationContext appContext = getServer().getApplicationContext();

        nodeService = (NodeService)appContext.getBean("nodeService");
        replicationService = (ReplicationService)appContext.getBean("ReplicationService");
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
        
        // Grab a reference to the data dictionary
        dataDictionary = nodeService.getChildByName(
                 repositoryHelper.getCompanyHome(),
                 ContentModel.ASSOC_CONTAINS,
                 "Data Dictionary"
        );
        
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
