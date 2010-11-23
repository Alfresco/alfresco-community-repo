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
package org.alfresco.repo.web.scripts.action;

import java.util.List;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.replication.ReplicationDefinitionImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.person.TestPersonManager;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.action.ActionStatus;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.action.ExecutionDetails;
import org.alfresco.service.cmr.action.ExecutionSummary;
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
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Tests for the Running Action Webscripts
 * @author Nick Burch
 */
public class RunningActionRestApiTest extends BaseWebScriptTest
{
    private static final String URL_RUNNING_ACTION = "api/running-action/";
    private static final String URL_RUNNING_ACTIONS = "api/running-actions";
    private static final String URL_RUNNING_REPLICATION_ACTIONS = "api/running-replication-actions";
    
    private static final String JSON = "application/json";
    
    private static final String USER_NORMAL = "Normal" + GUID.generate();
    
    private NodeService nodeService;
    private TestPersonManager personManager;
    private ReplicationService replicationService;
    private TransactionService transactionService;
    private ActionTrackingService actionTrackingService;
    private SimpleCache<String, ExecutionDetails> executingActionsCache;
    
    private Repository repositoryHelper;
    private NodeRef dataDictionary;
    
    /**
     * When we're run from a suite, check the previous
     *  test's work has finished before we run. If other
     *  things end up on the work queue with us, we'll
     *  get confused...
     */
    public void testEnsureAllQuiet() throws Exception
    {
        // Wait up to 2 seconds
        for(int i=0; i<200; i++)
        {
            List<ExecutionSummary> actions = actionTrackingService.getAllExecutingActions();
            if(actions.size() == 0)
            {
                // Good, nothing running
                break;
            }
            else
            {
                // Wait for the previous test's stuff to finish
                System.out.println("Waiting on " + actions.size() + " actions to finish, top is " + actions.get(0));
                Thread.sleep(10);
            }
        }
    }
    
    public void testRunningActionsGet() throws Exception
    {
        Response response;
        
        
        // Not allowed if you're not an admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getGuestUserName());
        response = sendRequest(new GetRequest(URL_RUNNING_ACTIONS), Status.STATUS_UNAUTHORIZED);
        assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());
        
        AuthenticationUtil.setFullyAuthenticatedUser(USER_NORMAL);
        response = sendRequest(new GetRequest(URL_RUNNING_ACTIONS), Status.STATUS_UNAUTHORIZED);
        assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());
        
       
        // If nothing running, you don't get anything back
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        response = sendRequest(new GetRequest(URL_RUNNING_ACTIONS), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        String jsonStr = response.getContentAsString();
        JSONObject json = new JSONObject(jsonStr);
        JSONArray results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(0, results.length());

        
        // Add a running action, it should show up
        ReplicationDefinition rd = replicationService.createReplicationDefinition("Test1", "Testing");
        replicationService.saveReplicationDefinition(rd);
        actionTrackingService.recordActionExecuting(rd);
        String id = rd.getId();
        String instance = Integer.toString( ((ActionImpl)rd).getExecutionInstance() );
        String startedAt = ISO8601DateFormat.format(rd.getExecutionStartDate()); 
        
        response = sendRequest(new GetRequest(URL_RUNNING_ACTIONS), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(1, results.length());
        
        JSONObject jsonRD = (JSONObject)results.get(0);
        assertNotNull(jsonRD);
        assertEquals(id, jsonRD.get("actionId"));
        assertEquals(ReplicationDefinitionImpl.EXECUTOR_NAME, jsonRD.get("actionType"));
        assertEquals(instance, jsonRD.get("actionInstance"));
        assertEquals(rd.getNodeRef().toString(), jsonRD.get("actionNodeRef"));
        assertEquals(startedAt, jsonRD.get("startedAt"));
        assertEquals(false, jsonRD.getBoolean("cancelRequested"));
        assertEquals("/" + URL_RUNNING_ACTION + "replicationActionExecutor=" +
              id + "=" + instance, jsonRD.get("details"));
        
        
        // Ensure we didn't get any unexpected data back,
        //  only the keys we should have done
        JSONArray keys = jsonRD.names();
        for(int i=0; i<keys.length(); i++) {
           String key = keys.getString(0);
           if(key.equals("actionId") || key.equals("actionType") ||
               key.equals("actionInstance") || key.equals("actionNodeRef") ||
               key.equals("startedAt") || key.equals("cancelRequested") ||
               key.equals("details")) {
              // All good
           } else {
              fail("Unexpected key '"+key+"' found in json, raw json is\n" + jsonStr);
           }
        }
        
        
        // Change the status to pending cancel, and re-check
        actionTrackingService.requestActionCancellation(rd);
        
        response = sendRequest(new GetRequest(URL_RUNNING_ACTIONS), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(1, results.length());
        
        jsonRD = (JSONObject)results.get(0);
        assertNotNull(jsonRD);
        assertEquals(id, jsonRD.get("actionId"));
        assertEquals(ReplicationDefinitionImpl.EXECUTOR_NAME, jsonRD.get("actionType"));
        assertEquals(instance, jsonRD.get("actionInstance"));
        assertEquals(rd.getNodeRef().toString(), jsonRD.get("actionNodeRef"));
        assertEquals(startedAt, jsonRD.get("startedAt"));
        assertEquals(true, jsonRD.getBoolean("cancelRequested"));
        assertEquals("/" + URL_RUNNING_ACTION + "replicationActionExecutor=" +
              id + "=" + instance, jsonRD.get("details"));
        
       
        // Add a 2nd and 3rd - one pending, one running
        rd = replicationService.createReplicationDefinition("Test2", "2nd Testing");
        replicationService.saveReplicationDefinition(rd);
        actionTrackingService.recordActionExecuting(rd);
        String id2 = rd.getId();
        String instance2 = Integer.toString( ((ActionImpl)rd).getExecutionInstance() );
        String startedAt2 = ISO8601DateFormat.format(rd.getExecutionStartDate()); 
        
        rd = replicationService.createReplicationDefinition("AnotherTest", "3rd Testing");
        replicationService.saveReplicationDefinition(rd);
        actionTrackingService.recordActionPending(rd);
        String id3 = rd.getId();

        
        // Check we got all 3
        boolean has1 = false;
        boolean has2 = false;
        boolean has3 = false;

        response = sendRequest(new GetRequest(URL_RUNNING_ACTIONS), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(3, results.length());
        
        for(int i=0; i<3; i++) {
           jsonRD = (JSONObject)results.get(i);
           if(jsonRD.get("actionId").equals(id)) {
              has1 = true;
           }
           if(jsonRD.get("actionId").equals(id2)) {
              has2 = true;
           }
           if(jsonRD.get("actionId").equals(id3)) {
              has3 = true;
           }
        }
        assertTrue(has1);
        assertTrue(has2);
        assertTrue(has3);
        
        
        // Remove one, check it goes
        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();
        actionTrackingService.recordActionComplete(rd);
        txn.commit();
        has1 = false;
        has2 = false;
        has3 = false;
        
        response = sendRequest(new GetRequest(URL_RUNNING_ACTIONS), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(2, results.length());
        
        for(int i=0; i<2; i++) {
           jsonRD = (JSONObject)results.get(i);
           if(jsonRD.get("actionId").equals(id)) {
              has1 = true;
           }
           if(jsonRD.get("actionId").equals(id2)) {
              has2 = true;
           }
           if(jsonRD.get("actionId").equals(id3)) {
              has3 = true;
           }
        }
        assertTrue(has1);
        assertTrue(has2);
        assertFalse(has3);

        
        // Check we correctly filter by node ID
        rd = replicationService.loadReplicationDefinition("Test1");
        
        response = sendRequest(new GetRequest(URL_RUNNING_ACTIONS + "?nodeRef=" +
              rd.getNodeRef().toString()), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(1, results.length());
        
        jsonRD = (JSONObject)results.get(0);
        assertNotNull(jsonRD);
        assertEquals(id, jsonRD.get("actionId"));
        assertEquals(ReplicationDefinitionImpl.EXECUTOR_NAME, jsonRD.get("actionType"));
        assertEquals(instance, jsonRD.get("actionInstance"));
        assertEquals(rd.getNodeRef().toString(), jsonRD.get("actionNodeRef"));
        assertEquals(startedAt, jsonRD.get("startedAt"));
        assertEquals(true, jsonRD.getBoolean("cancelRequested"));
        assertEquals("/" + URL_RUNNING_ACTION + "replicationActionExecutor=" +
              id + "=" + instance, jsonRD.get("details"));
        
        
        // Check the other one
        rd = replicationService.loadReplicationDefinition("Test2");
        
        response = sendRequest(new GetRequest(URL_RUNNING_ACTIONS + "?nodeRef=" +
              rd.getNodeRef().toString()), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(1, results.length());
        
        jsonRD = (JSONObject)results.get(0);
        assertNotNull(jsonRD);
        assertEquals(id2, jsonRD.get("actionId"));
        assertEquals(ReplicationDefinitionImpl.EXECUTOR_NAME, jsonRD.get("actionType"));
        assertEquals(instance2, jsonRD.get("actionInstance"));
        assertEquals(rd.getNodeRef().toString(), jsonRD.get("actionNodeRef"));
        assertEquals(startedAt2, jsonRD.get("startedAt"));
        assertEquals(false, jsonRD.getBoolean("cancelRequested"));
        assertEquals("/" + URL_RUNNING_ACTION + "replicationActionExecutor=" +
              id2 + "=" + instance2, jsonRD.get("details"));

        
        // Check we correctly filter by type
        ActionImpl alt1 = new ActionImpl(null, "12345", "MadeUp1");
        ActionImpl alt2 = new ActionImpl(null, "54321", "MadeUp2");
        actionTrackingService.recordActionExecuting(alt1);
        actionTrackingService.recordActionExecuting(alt2);
        String startAtAlt2 = ISO8601DateFormat.format( alt2.getExecutionStartDate() );
        String instanceAlt2 = Integer.toString( alt2.getExecutionInstance() );
        
        // Goes up to 4 not by type
        response = sendRequest(new GetRequest(URL_RUNNING_ACTIONS), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(4, results.length());
        
        // 2 replication actions
        response = sendRequest(new GetRequest(URL_RUNNING_ACTIONS +
              "?type=replicationActionExecutor"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(2, results.length());
        
        // 0 if doesn't exist
        response = sendRequest(new GetRequest(URL_RUNNING_ACTIONS +
              "?type=MadeUp4"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(0, results.length());
        
        // 1 each of the made up ones
        response = sendRequest(new GetRequest(URL_RUNNING_ACTIONS +
              "?type=MadeUp1"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(1, results.length());
        
        response = sendRequest(new GetRequest(URL_RUNNING_ACTIONS +
              "?type=MadeUp2"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(1, results.length());
        
        // Check the details of one of these
        jsonRD = (JSONObject)results.get(0);
        assertNotNull(jsonRD);
        assertEquals("54321", jsonRD.get("actionId"));
        assertEquals("MadeUp2", jsonRD.get("actionType"));
        assertEquals(instanceAlt2, jsonRD.get("actionInstance"));
        assertEquals(JSONObject.NULL, jsonRD.get("actionNodeRef"));
        assertEquals(startAtAlt2, jsonRD.get("startedAt"));
        assertEquals(false, jsonRD.getBoolean("cancelRequested"));
        assertEquals("/" + URL_RUNNING_ACTION + "MadeUp2=54321=" +
              instanceAlt2, jsonRD.get("details"));
    }
    
    
    public void testRunningReplicationActionsGet() throws Exception
    {
       Response response;
       
       
       // Not allowed if you're not an admin
       AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getGuestUserName());
       response = sendRequest(new GetRequest(URL_RUNNING_REPLICATION_ACTIONS), Status.STATUS_UNAUTHORIZED);
       assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());
       
       AuthenticationUtil.setFullyAuthenticatedUser(USER_NORMAL);
       response = sendRequest(new GetRequest(URL_RUNNING_REPLICATION_ACTIONS), Status.STATUS_UNAUTHORIZED);
       assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());
       
      
       // If nothing running, you don't get anything back
       AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
       response = sendRequest(new GetRequest(URL_RUNNING_REPLICATION_ACTIONS), 200);
       assertEquals(Status.STATUS_OK, response.getStatus());
       
       String jsonStr = response.getContentAsString();
       JSONObject json = new JSONObject(jsonStr);
       JSONArray results = json.getJSONArray("data");
       assertNotNull(results);
       assertEquals(0, results.length());

       
       // Add a running action, it should show up
       ReplicationDefinition rd = replicationService.createReplicationDefinition("Test1", "Testing");
       replicationService.saveReplicationDefinition(rd);
       actionTrackingService.recordActionExecuting(rd);
       String id = rd.getId();
       String instance = Integer.toString( ((ActionImpl)rd).getExecutionInstance() );
       String startedAt = ISO8601DateFormat.format(rd.getExecutionStartDate()); 
       
       response = sendRequest(new GetRequest(URL_RUNNING_REPLICATION_ACTIONS), 200);
       assertEquals(Status.STATUS_OK, response.getStatus());
       
       jsonStr = response.getContentAsString();
       json = new JSONObject(jsonStr);
       results = json.getJSONArray("data");
       assertNotNull(results);
       assertEquals(1, results.length());
       
       JSONObject jsonRD = (JSONObject)results.get(0);
       assertNotNull(jsonRD);
       assertEquals(id, jsonRD.get("actionId"));
       assertEquals(ReplicationDefinitionImpl.EXECUTOR_NAME, jsonRD.get("actionType"));
       assertEquals(instance, jsonRD.get("actionInstance"));
       assertEquals(rd.getNodeRef().toString(), jsonRD.get("actionNodeRef"));
       assertEquals(startedAt, jsonRD.get("startedAt"));
       assertEquals(false, jsonRD.getBoolean("cancelRequested"));
       assertEquals("/" + URL_RUNNING_ACTION + "replicationActionExecutor=" +
             id + "=" + instance, jsonRD.get("details"));
       
       
       // Ensure we didn't get any unexpected data back,
       //  only the keys we should have done
       JSONArray keys = jsonRD.names();
       for(int i=0; i<keys.length(); i++) {
          String key = keys.getString(0);
          if(key.equals("actionId") || key.equals("actionType") ||
              key.equals("actionInstance") || key.equals("actionNodeRef") ||
              key.equals("startedAt") || key.equals("cancelRequested") ||
              key.equals("details")) {
             // All good
          } else {
             fail("Unexpected key '"+key+"' found in json, raw json is\n" + jsonStr);
          }
       }
       
       
       // Change the status to pending cancel, and re-check
       actionTrackingService.requestActionCancellation(rd);
       
       response = sendRequest(new GetRequest(URL_RUNNING_REPLICATION_ACTIONS), 200);
       assertEquals(Status.STATUS_OK, response.getStatus());
       
       jsonStr = response.getContentAsString();
       json = new JSONObject(jsonStr);
       results = json.getJSONArray("data");
       assertNotNull(results);
       assertEquals(1, results.length());
       
       jsonRD = (JSONObject)results.get(0);
       assertNotNull(jsonRD);
       assertEquals(id, jsonRD.get("actionId"));
       assertEquals(ReplicationDefinitionImpl.EXECUTOR_NAME, jsonRD.get("actionType"));
       assertEquals(instance, jsonRD.get("actionInstance"));
       assertEquals(rd.getNodeRef().toString(), jsonRD.get("actionNodeRef"));
       assertEquals(startedAt, jsonRD.get("startedAt"));
       assertEquals(true, jsonRD.getBoolean("cancelRequested"));
       assertEquals("/" + URL_RUNNING_ACTION + "replicationActionExecutor=" +
             id + "=" + instance, jsonRD.get("details"));
       
      
       // Add a 2nd and 3rd, one running and one pending
       rd = replicationService.createReplicationDefinition("Test2", "2nd Testing");
       replicationService.saveReplicationDefinition(rd);
       actionTrackingService.recordActionExecuting(rd);
       String id2 = rd.getId();
       String instance2 = Integer.toString( ((ActionImpl)rd).getExecutionInstance() );
       String startedAt2 = ISO8601DateFormat.format(rd.getExecutionStartDate()); 
       
       rd = replicationService.createReplicationDefinition("AnotherTest", "3rd Testing");
       replicationService.saveReplicationDefinition(rd);
       actionTrackingService.recordActionPending(rd);
       String id3 = rd.getId();

       
       // Check we got all 3
       boolean has1 = false;
       boolean has2 = false;
       boolean has3 = false;

       response = sendRequest(new GetRequest(URL_RUNNING_REPLICATION_ACTIONS), 200);
       assertEquals(Status.STATUS_OK, response.getStatus());
       
       jsonStr = response.getContentAsString();
       json = new JSONObject(jsonStr);
       results = json.getJSONArray("data");
       assertNotNull(results);
       assertEquals(3, results.length());
       
       for(int i=0; i<3; i++) {
          jsonRD = (JSONObject)results.get(i);
          if(jsonRD.get("actionId").equals(id)) {
             has1 = true;
          }
          if(jsonRD.get("actionId").equals(id2)) {
             has2 = true;
          }
          if(jsonRD.get("actionId").equals(id3)) {
             has3 = true;
          }
       }
       assertTrue(has1);
       assertTrue(has2);
       assertTrue(has3);
       
       
       // Remove one, check it goes
       UserTransaction txn = transactionService.getUserTransaction();
       txn.begin();
       actionTrackingService.recordActionComplete(rd);
       txn.commit();
       has1 = false;
       has2 = false;
       has3 = false;
       
       response = sendRequest(new GetRequest(URL_RUNNING_REPLICATION_ACTIONS), 200);
       assertEquals(Status.STATUS_OK, response.getStatus());
       
       jsonStr = response.getContentAsString();
       json = new JSONObject(jsonStr);
       results = json.getJSONArray("data");
       assertNotNull(results);
       assertEquals(2, results.length());
       
       for(int i=0; i<2; i++) {
          jsonRD = (JSONObject)results.get(i);
          if(jsonRD.get("actionId").equals(id)) {
             has1 = true;
          }
          if(jsonRD.get("actionId").equals(id2)) {
             has2 = true;
          }
          if(jsonRD.get("actionId").equals(id3)) {
             has3 = true;
          }
       }
       assertTrue(has1);
       assertTrue(has2);
       assertFalse(has3);

       
       // Check we correctly filter by name
       rd = replicationService.loadReplicationDefinition("Test1");
       
       response = sendRequest(new GetRequest(URL_RUNNING_REPLICATION_ACTIONS + "?name=Test1"), 200);
       assertEquals(Status.STATUS_OK, response.getStatus());
       
       jsonStr = response.getContentAsString();
       json = new JSONObject(jsonStr);
       results = json.getJSONArray("data");
       assertNotNull(results);
       assertEquals(1, results.length());
       
       jsonRD = (JSONObject)results.get(0);
       assertNotNull(jsonRD);
       assertEquals(id, jsonRD.get("actionId"));
       assertEquals(ReplicationDefinitionImpl.EXECUTOR_NAME, jsonRD.get("actionType"));
       assertEquals(instance, jsonRD.get("actionInstance"));
       assertEquals(rd.getNodeRef().toString(), jsonRD.get("actionNodeRef"));
       assertEquals(startedAt, jsonRD.get("startedAt"));
       assertEquals(true, jsonRD.getBoolean("cancelRequested"));
       assertEquals("/" + URL_RUNNING_ACTION + "replicationActionExecutor=" +
             id + "=" + instance, jsonRD.get("details"));
       

       // Check we correctly filter by type
       ActionImpl alt1 = new ActionImpl(null, "12345", "MadeUp1");
       ActionImpl alt2 = new ActionImpl(null, "54321", "MadeUp2");
       actionTrackingService.recordActionExecuting(alt1);
       actionTrackingService.recordActionExecuting(alt2);
       String startAtAlt2 = ISO8601DateFormat.format( alt2.getExecutionStartDate() );
       String instanceAlt2 = Integer.toString( alt2.getExecutionInstance() );
       
       // 2 replication actions
       response = sendRequest(new GetRequest(URL_RUNNING_REPLICATION_ACTIONS), 200);
       assertEquals(Status.STATUS_OK, response.getStatus());
       jsonStr = response.getContentAsString();
       json = new JSONObject(jsonStr);
       results = json.getJSONArray("data");
       assertNotNull(results);
       assertEquals(2, results.length());
    }
    
    
    public void testRunningActionGet() throws Exception
    {
        Response response;
        
        
        // Not allowed if you're not an admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getGuestUserName());
        response = sendRequest(new GetRequest(URL_RUNNING_ACTION + "MadeUp"), Status.STATUS_UNAUTHORIZED);
        assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());
        
        AuthenticationUtil.setFullyAuthenticatedUser(USER_NORMAL);
        response = sendRequest(new GetRequest(URL_RUNNING_ACTION + "MadeUp"), Status.STATUS_UNAUTHORIZED);
        assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());
        
       
        // If not found, you get a 404
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        response = sendRequest(new GetRequest(URL_RUNNING_ACTION + "MadeUp"), Status.STATUS_NOT_FOUND);
        assertEquals(Status.STATUS_NOT_FOUND, response.getStatus());

        
        // Create one
        ReplicationDefinition rd = replicationService.createReplicationDefinition("Test1", "Testing");
        replicationService.saveReplicationDefinition(rd);
        actionTrackingService.recordActionExecuting(rd);
        String id = rd.getId();
        String instance = Integer.toString( ((ActionImpl)rd).getExecutionInstance() );
        String startedAt = ISO8601DateFormat.format(rd.getExecutionStartDate());
        String key1 = "replicationActionExecutor=" + id + "=" + instance; 
        
        
        // Fetch the details of it
        response = sendRequest(new GetRequest(URL_RUNNING_ACTION + key1), Status.STATUS_OK);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        String jsonStr = response.getContentAsString();
        JSONObject jsonRD = new JSONObject(jsonStr).getJSONObject("data");
        assertNotNull(jsonRD);
        assertEquals(id, jsonRD.get("actionId"));
        assertEquals(ReplicationDefinitionImpl.EXECUTOR_NAME, jsonRD.get("actionType"));
        assertEquals(instance, jsonRD.get("actionInstance"));
        assertEquals(rd.getNodeRef().toString(), jsonRD.get("actionNodeRef"));
        assertEquals(startedAt, jsonRD.get("startedAt"));
        assertEquals(false, jsonRD.getBoolean("cancelRequested"));
        assertEquals("/" + URL_RUNNING_ACTION + key1, jsonRD.get("details"));
        
        
        // Ensure we didn't get any unexpected data back,
        //  only the keys we should have done
        JSONArray keys = jsonRD.names();
        for(int i=0; i<keys.length(); i++) {
           String key = keys.getString(0);
           if(key.equals("actionId") || key.equals("actionType") ||
               key.equals("actionInstance") || key.equals("actionNodeRef") ||
               key.equals("startedAt") || key.equals("cancelRequested") ||
               key.equals("details")) {
              // All good
           } else {
              fail("Unexpected key '"+key+"' found in json, raw json is\n" + jsonStr);
           }
        }
        
        
        // Add another which is cancelled, check that
        //  we get the correct, different details for it
        rd = replicationService.createReplicationDefinition("Test2", "Testing");
        replicationService.saveReplicationDefinition(rd);
        actionTrackingService.recordActionExecuting(rd);
        actionTrackingService.requestActionCancellation(rd);
        String id2 = rd.getId();
        String instance2 = Integer.toString( ((ActionImpl)rd).getExecutionInstance() );
        String startedAt2 = ISO8601DateFormat.format(rd.getExecutionStartDate());
        String key2 = "replicationActionExecutor=" + id2 + "=" + instance2; 
        
        response = sendRequest(new GetRequest(URL_RUNNING_ACTION + key2), Status.STATUS_OK);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        jsonRD = new JSONObject(jsonStr).getJSONObject("data");
        assertNotNull(jsonRD);
        assertEquals(id2, jsonRD.get("actionId"));
        assertEquals(ReplicationDefinitionImpl.EXECUTOR_NAME, jsonRD.get("actionType"));
        assertEquals(instance2, jsonRD.get("actionInstance"));
        assertEquals(rd.getNodeRef().toString(), jsonRD.get("actionNodeRef"));
        assertEquals(startedAt2, jsonRD.get("startedAt"));
        assertEquals(true, jsonRD.getBoolean("cancelRequested"));
        assertEquals("/" + URL_RUNNING_ACTION + key2, jsonRD.get("details"));
        
        
        // Check that the original is unchanged
        response = sendRequest(new GetRequest(URL_RUNNING_ACTION + key1), Status.STATUS_OK);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        rd = replicationService.loadReplicationDefinition("Test1");
        jsonStr = response.getContentAsString();
        jsonRD = new JSONObject(jsonStr).getJSONObject("data");
        assertNotNull(jsonRD);
        assertEquals(id, jsonRD.get("actionId"));
        assertEquals(ReplicationDefinitionImpl.EXECUTOR_NAME, jsonRD.get("actionType"));
        assertEquals(instance, jsonRD.get("actionInstance"));
        assertEquals(rd.getNodeRef().toString(), jsonRD.get("actionNodeRef"));
        assertEquals(startedAt, jsonRD.get("startedAt"));
        assertEquals(false, jsonRD.getBoolean("cancelRequested"));
        assertEquals("/" + URL_RUNNING_ACTION + key1, jsonRD.get("details"));
        
        
        // Add one that is pending - has everything except start date
        rd = replicationService.createReplicationDefinition("Test3", "Testing");
        replicationService.saveReplicationDefinition(rd);
        actionTrackingService.recordActionPending(rd);
        String id3 = rd.getId();
        String instance3 = Integer.toString( ((ActionImpl)rd).getExecutionInstance() );
        String key3 = "replicationActionExecutor=" + id3 + "=" + instance3; 
        
        response = sendRequest(new GetRequest(URL_RUNNING_ACTION + key3), Status.STATUS_OK);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        jsonStr = response.getContentAsString();
        jsonRD = new JSONObject(jsonStr).getJSONObject("data");
        assertNotNull(jsonRD);
        assertEquals(id3, jsonRD.get("actionId"));
        assertEquals(ReplicationDefinitionImpl.EXECUTOR_NAME, jsonRD.get("actionType"));
        assertEquals(instance3, jsonRD.get("actionInstance"));
        assertEquals(rd.getNodeRef().toString(), jsonRD.get("actionNodeRef"));
        assertEquals(JSONObject.NULL, jsonRD.get("startedAt"));
        assertEquals(false, jsonRD.getBoolean("cancelRequested"));
        assertEquals("/" + URL_RUNNING_ACTION + key3, jsonRD.get("details"));
    }
    
    public void testRunningActionCancel() throws Exception 
    {
       Response response;
       
       
       // Not allowed if you're not an admin
       AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getGuestUserName());
       response = sendRequest(new DeleteRequest(URL_RUNNING_ACTION + "MadeUp"), Status.STATUS_UNAUTHORIZED);
       assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());
       
       AuthenticationUtil.setFullyAuthenticatedUser(USER_NORMAL);
       response = sendRequest(new DeleteRequest(URL_RUNNING_ACTION + "MadeUp"), Status.STATUS_UNAUTHORIZED);
       assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());
       
      
       // If not found, you get a 404
       AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
       response = sendRequest(new DeleteRequest(URL_RUNNING_ACTION + "MadeUp"), Status.STATUS_NOT_FOUND);
       assertEquals(Status.STATUS_NOT_FOUND, response.getStatus());

       
       // Create one
       ReplicationDefinition rd = replicationService.createReplicationDefinition("Test1", "Testing");
       replicationService.saveReplicationDefinition(rd);
       actionTrackingService.recordActionExecuting(rd);
       String id = rd.getId();
       String instance = Integer.toString( ((ActionImpl)rd).getExecutionInstance() );
       String key = "replicationActionExecutor=" + id + "=" + instance;
       
       assertEquals(false, actionTrackingService.isCancellationRequested(rd));

       
       // Request it to cancel
       response = sendRequest(new DeleteRequest(URL_RUNNING_ACTION + key), Status.STATUS_NO_CONTENT);
       assertEquals(Status.STATUS_NO_CONTENT, response.getStatus());
       
       
       // Check it was cancelled
       assertEquals(true, actionTrackingService.isCancellationRequested(rd));
       
       
       // Request again - no change
       response = sendRequest(new DeleteRequest(URL_RUNNING_ACTION + key), Status.STATUS_NO_CONTENT);
       assertEquals(Status.STATUS_NO_CONTENT, response.getStatus());
       
       assertEquals(true, actionTrackingService.isCancellationRequested(rd));
       
       
       // You can ask a pending action to cancel
       // At this time, it will remain in the queue though
       rd = replicationService.createReplicationDefinition("Test2", "Testing");
       replicationService.saveReplicationDefinition(rd);
       actionTrackingService.recordActionPending(rd);
       String id2 = rd.getId();
       String instance2 = Integer.toString( ((ActionImpl)rd).getExecutionInstance() );
       String key2 = "replicationActionExecutor=" + id2 + "=" + instance2;
       
       
       // Should be in the cache, but not not running and not cancelled
       assertEquals(false, actionTrackingService.isCancellationRequested(rd));
       assertEquals(null, rd.getExecutionStartDate());
       
       assertNotNull(executingActionsCache.get(key2));
       assertFalse(executingActionsCache.get(key2).isCancelRequested());

       
       // Ask for it to be cancelled via the webscript
       response = sendRequest(new DeleteRequest(URL_RUNNING_ACTION + key2), Status.STATUS_NO_CONTENT);
       assertEquals(Status.STATUS_NO_CONTENT, response.getStatus());
       
       
       // Should still be in the cache, not running but cancelled
       assertNotNull(executingActionsCache.get(key2));
       assertTrue(executingActionsCache.get(key2).isCancelRequested());
       
       assertEquals(true, actionTrackingService.isCancellationRequested(rd));
       assertEquals(null, rd.getExecutionStartDate());
    }
    
    public void testRunningActionsPost() throws Exception
    {
       Response response;
       
       
       // Not allowed if you're not an admin
       AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getGuestUserName());
       response = sendRequest(new PostRequest(URL_RUNNING_ACTIONS, "{}", JSON), Status.STATUS_UNAUTHORIZED);
       assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());
       
       AuthenticationUtil.setFullyAuthenticatedUser(USER_NORMAL);
       response = sendRequest(new PostRequest(URL_RUNNING_ACTIONS, "{}", JSON), Status.STATUS_UNAUTHORIZED);
       assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());
       
      
       // If no noderef supplied, will get an error
       AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
       response = sendRequest(new PostRequest(URL_RUNNING_ACTIONS, "{}", JSON), Status.STATUS_BAD_REQUEST);
       assertEquals(Status.STATUS_BAD_REQUEST, response.getStatus());
       
       
       // Add a running action
       UserTransaction txn = transactionService.getUserTransaction();
       txn.begin();
       
       ReplicationDefinition rd = replicationService.createReplicationDefinition("Test1", "Testing");
       replicationService.saveReplicationDefinition(rd);
       String id = rd.getId();
       
       txn.commit();
       
       
       // Ask for it to be started
       // (It should start but fail due to missing definition parts)
       JSONObject json = new JSONObject();
       json.put("nodeRef", rd.getNodeRef().toString());

       response = sendRequest(new PostRequest(URL_RUNNING_ACTIONS, json.toString(), JSON), Status.STATUS_OK);
       assertEquals(Status.STATUS_OK, response.getStatus());
       
       // Check we got back some details on it
       String jsonStr = response.getContentAsString();
       JSONObject jsonRD = new JSONObject(jsonStr).getJSONObject("data");
       assertNotNull(jsonRD);
       
       assertEquals(id, jsonRD.get("actionId"));
       assertEquals(ReplicationDefinitionImpl.EXECUTOR_NAME, jsonRD.get("actionType"));
       assertEquals(rd.getNodeRef().toString(), jsonRD.get("actionNodeRef"));
       assertEquals(false, jsonRD.getBoolean("cancelRequested"));
       
       // Should be pending 
       // Wait for it to fail (we didn't
       //  specify enough options for a valid transfer)
       for(int i=0; i<50; i++) {
          txn = transactionService.getUserTransaction();
          txn.begin();
          rd = replicationService.loadReplicationDefinition("Test1");
          txn.commit();
             
          if(rd.getExecutionStatus() == ActionStatus.New) {
             // Still pending, or maybe running
             try {
                Thread.sleep(100);
             } catch(InterruptedException e) {}
          } else if (rd.getExecutionStatus() == ActionStatus.Failed) {
             // We're done
             break;
          } else {
             fail("Unexpected status in repo of " + rd.getExecutionStatus());
          }
       }
       assertEquals(ActionStatus.Failed, rd.getExecutionStatus());
       
       
       // Ensure you can't start with an invalid nodeRef
       json = new JSONObject();
       json.put("nodeRef", "XX"+rd.getNodeRef().toString()+"ZZ");

       response = sendRequest(new PostRequest(URL_RUNNING_ACTIONS, json.toString(), JSON), Status.STATUS_NOT_FOUND);
       assertEquals(Status.STATUS_NOT_FOUND, response.getStatus());
    }
    
    public void testRunningReplicationsActionsPost() throws Exception
    {
       Response response;
       
       
       // Not allowed if you're not an admin
       AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getGuestUserName());
       response = sendRequest(new PostRequest(URL_RUNNING_REPLICATION_ACTIONS, "{}", JSON), Status.STATUS_UNAUTHORIZED);
       assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());
       
       AuthenticationUtil.setFullyAuthenticatedUser(USER_NORMAL);
       response = sendRequest(new PostRequest(URL_RUNNING_REPLICATION_ACTIONS, "{}", JSON), Status.STATUS_UNAUTHORIZED);
       assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());
       
      
       // If no noderef supplied, will get an error
       AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
       response = sendRequest(new PostRequest(URL_RUNNING_REPLICATION_ACTIONS, "{}", JSON), Status.STATUS_BAD_REQUEST);
       assertEquals(Status.STATUS_BAD_REQUEST, response.getStatus());
       
       
       // Add a running action
       UserTransaction txn = transactionService.getUserTransaction();
       txn.begin();
       
       ReplicationDefinition rd = replicationService.createReplicationDefinition("Test1", "Testing");
       replicationService.saveReplicationDefinition(rd);
       String id = rd.getId();
       
       txn.commit();
       
       
       // Ask for it to be started
       // (It should start but fail due to missing definition parts)
       JSONObject json = new JSONObject();
       json.put("name", "Test1");

       response = sendRequest(new PostRequest(URL_RUNNING_REPLICATION_ACTIONS, json.toString(), JSON), Status.STATUS_OK);
       assertEquals(Status.STATUS_OK, response.getStatus());
       
       // Check we got back some details on it
       String jsonStr = response.getContentAsString();
       JSONObject jsonRD = new JSONObject(jsonStr).getJSONObject("data");
       assertNotNull(jsonRD);
       
       assertEquals(id, jsonRD.get("actionId"));
       assertEquals(ReplicationDefinitionImpl.EXECUTOR_NAME, jsonRD.get("actionType"));
       assertEquals(rd.getNodeRef().toString(), jsonRD.get("actionNodeRef"));
       assertEquals(false, jsonRD.getBoolean("cancelRequested"));
       
       // Should be pending 
       // Wait for it to fail (we didn't
       //  specify enough options for a valid transfer)
       for(int i=0; i<50; i++) {
          txn = transactionService.getUserTransaction();
          txn.begin();
          rd = replicationService.loadReplicationDefinition("Test1");
          txn.commit();
             
          if(rd.getExecutionStatus() == ActionStatus.New) {
             // Still pending, or maybe running
             try {
                Thread.sleep(100);
             } catch(InterruptedException e) {}
          } else if (rd.getExecutionStatus() == ActionStatus.Failed) {
             // We're done
             break;
          } else {
             fail("Unexpected status in repo of " + rd.getExecutionStatus());
          }
       }
       assertEquals(ActionStatus.Failed, rd.getExecutionStatus());
       
       
       // Ensure you can't start with an invalid name
       json = new JSONObject();
       json.put("name", "MadeUpName");

       response = sendRequest(new PostRequest(URL_RUNNING_REPLICATION_ACTIONS, json.toString(), JSON), Status.STATUS_NOT_FOUND);
       assertEquals(Status.STATUS_NOT_FOUND, response.getStatus());
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
        executingActionsCache = (SimpleCache<String, ExecutionDetails>)appContext.getBean("executingActionsCache");
        
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
        
        // Clear out the running actions
        for(ExecutionSummary es : actionTrackingService.getAllExecutingActions()) {
           executingActionsCache.remove(
                 AbstractActionWebscript.getRunningId(es)
           );
        }
        
        txn.commit();
    }
}
