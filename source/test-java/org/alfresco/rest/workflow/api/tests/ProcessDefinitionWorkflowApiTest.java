/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.rest.workflow.api.tests;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.workflow.api.model.ProcessDefinition;
import org.alfresco.rest.workflow.api.tests.WorkflowApiClient.ProcessDefinitionsClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.springframework.http.HttpStatus;

/**
 * @author Tijs Rademakers 
 * @author Frederik Heremans
 */
public class ProcessDefinitionWorkflowApiTest extends EnterpriseWorkflowTestApi
{   
    
    @Test
    public void testGetProcessDefinitions() throws Exception
    {
        RequestContext requestContext = initApiClientWithTestUser();

        // Get all process definitions
        ProcessDefinitionsClient processDefinitionsClient = publicApiClient.processDefinitionsClient();
        ListResponse<ProcessDefinition> processDefinitionsResponse = processDefinitionsClient.getProcessDefinitions(null);
        Map<String, ProcessDefinition>  processDefinitionMap = getProcessDefinitionMapByKey(processDefinitionsResponse.getList());
        
        assertTrue(processDefinitionMap.containsKey("activitiReviewPooled"));
        assertTrue(processDefinitionMap.containsKey("activitiReview"));
        assertTrue(processDefinitionMap.containsKey("activitiParallelGroupReview"));
        assertTrue(processDefinitionMap.containsKey("activitiParallelReview"));
        assertTrue(processDefinitionMap.containsKey("activitiAdhoc"));
        assertEquals(5, processDefinitionMap.size());
        
        
        // Check fields of a resulting process-definition
        String adhocKey = createProcessDefinitionKey("activitiAdhoc", requestContext);
        org.activiti.engine.repository.ProcessDefinition activitiDefinition = activitiProcessEngine.getRepositoryService()
            .createProcessDefinitionQuery()
            .processDefinitionKey(adhocKey)
            .singleResult();
        
        assertNotNull(activitiDefinition);
        
        ProcessDefinition adhocDefinitionRest = processDefinitionMap.get("activitiAdhoc");
        
        assertEquals(activitiDefinition.getId(), adhocDefinitionRest.getId());
        assertEquals("activitiAdhoc", adhocDefinitionRest.getKey());
        assertEquals(activitiDefinition.getDeploymentId(), adhocDefinitionRest.getDeploymentId());
        assertEquals(activitiDefinition.getCategory(), adhocDefinitionRest.getCategory());
        assertEquals(activitiDefinition.getName(), adhocDefinitionRest.getName());
        assertEquals(activitiDefinition.getVersion(), adhocDefinitionRest.getVersion());
        assertEquals(((ProcessDefinitionEntity) activitiDefinition).isGraphicalNotationDefined(), adhocDefinitionRest.isGraphicNotationDefined());
        assertEquals("wf:submitAdhocTask", adhocDefinitionRest.getStartFormResourceKey());
        assertEquals("New Task", adhocDefinitionRest.getTitle());
        assertEquals("Assign a new task to yourself or a colleague", adhocDefinitionRest.getDescription());
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("maxItems", "2");
        JSONObject definitionListObject = processDefinitionsClient.getProcessDefinitionsWithRawResponse(params);
        assertNotNull(definitionListObject);
        JSONObject paginationJSON = (JSONObject) definitionListObject.get("pagination");
        assertEquals(2l, paginationJSON.get("count"));
        assertEquals(5l, paginationJSON.get("totalItems"));
        assertEquals(0l, paginationJSON.get("skipCount"));
        assertEquals(true, paginationJSON.get("hasMoreItems"));
        
        params = new HashMap<String, String>();
        definitionListObject = processDefinitionsClient.getProcessDefinitionsWithRawResponse(params);
        assertNotNull(definitionListObject);
        paginationJSON = (JSONObject) definitionListObject.get("pagination");
        assertEquals(5l, paginationJSON.get("count"));
        assertEquals(5l, paginationJSON.get("totalItems"));
        assertEquals(0l, paginationJSON.get("skipCount"));
        assertEquals(false, paginationJSON.get("hasMoreItems"));
        
        params = new HashMap<String, String>();
        params.put("skipCount", "2");
        params.put("maxItems", "2");
        definitionListObject = processDefinitionsClient.getProcessDefinitionsWithRawResponse(params);
        assertNotNull(definitionListObject);
        paginationJSON = (JSONObject) definitionListObject.get("pagination");
        assertEquals(2l, paginationJSON.get("count"));
        assertEquals(5l, paginationJSON.get("totalItems"));
        assertEquals(2l, paginationJSON.get("skipCount"));
        assertEquals(true, paginationJSON.get("hasMoreItems"));
        
        params = new HashMap<String, String>();
        params.put("skipCount", "2");
        params.put("maxItems", "5");
        definitionListObject = processDefinitionsClient.getProcessDefinitionsWithRawResponse(params);
        assertNotNull(definitionListObject);
        paginationJSON = (JSONObject) definitionListObject.get("pagination");
        assertEquals(3l, paginationJSON.get("count"));
        assertEquals(5l, paginationJSON.get("totalItems"));
        assertEquals(2l, paginationJSON.get("skipCount"));
        assertEquals(true, paginationJSON.get("hasMoreItems"));
        
        params = new HashMap<String, String>();
        params.put("skipCount", "0");
        params.put("maxItems", "7");
        definitionListObject = processDefinitionsClient.getProcessDefinitionsWithRawResponse(params);
        assertNotNull(definitionListObject);
        paginationJSON = (JSONObject) definitionListObject.get("pagination");
        assertEquals(5l, paginationJSON.get("count"));
        assertEquals(5l, paginationJSON.get("totalItems"));
        assertEquals(0l, paginationJSON.get("skipCount"));
        assertEquals(false, paginationJSON.get("hasMoreItems"));
    }
    
    @Test
    public void testGetProcessDefinitionsWhereClause() throws Exception
    {
        RequestContext requestContext = initApiClientWithTestUser();
        
        String adhocKey = createProcessDefinitionKey("activitiAdhoc", requestContext);
        org.activiti.engine.repository.ProcessDefinition activitiDefinition = activitiProcessEngine.getRepositoryService()
            .createProcessDefinitionQuery()
            .processDefinitionKey(adhocKey)
            .singleResult();
        
        assertNotNull(activitiDefinition);

        ProcessDefinitionsClient processDefinitionsClient = publicApiClient.processDefinitionsClient();
        
        // Filter on category equals
        Map<String, ProcessDefinition>  processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(category = 'http://alfresco.org')");
        
        assertTrue(processDefinitionMap.containsKey("activitiReviewPooled"));
        assertTrue(processDefinitionMap.containsKey("activitiReview"));
        assertTrue(processDefinitionMap.containsKey("activitiParallelGroupReview"));
        assertTrue(processDefinitionMap.containsKey("activitiParallelReview"));
        assertTrue(processDefinitionMap.containsKey("activitiAdhoc"));
        assertEquals(5, processDefinitionMap.size());
        
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(category = 'unexisting')");
        assertEquals(0, processDefinitionMap.size());
        
        // Filter on name equals
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(name = 'Adhoc Activiti Process')");
        assertTrue(processDefinitionMap.containsKey("activitiAdhoc"));
        assertEquals(1, processDefinitionMap.size());
        
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(name = 'unexisting')");
        assertEquals(0, processDefinitionMap.size());
        
        // Filter on key equals
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(key='activitiAdhoc')");
        assertTrue(processDefinitionMap.containsKey("activitiAdhoc"));
        assertEquals(1, processDefinitionMap.size());
        
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(key='unexisting')");
        assertEquals(0, processDefinitionMap.size());
        
        // Filter on version equals
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(version='1')");
        assertEquals(5, processDefinitionMap.size());
        
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(version='2')");
        assertEquals(0, processDefinitionMap.size());
        
        // Filter on deploymentId equals
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(deploymentId='" + activitiDefinition.getDeploymentId() + "')");
        assertTrue(processDefinitionMap.containsKey("activitiAdhoc"));
        assertEquals(1, processDefinitionMap.size());
        
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(deploymentId='unexisting')");
        assertEquals(0, processDefinitionMap.size());
        
        // Filter on category matches
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(category matches('%alfresco.o%'))");
        
        assertTrue(processDefinitionMap.containsKey("activitiReviewPooled"));
        assertTrue(processDefinitionMap.containsKey("activitiReview"));
        assertTrue(processDefinitionMap.containsKey("activitiParallelGroupReview"));
        assertTrue(processDefinitionMap.containsKey("activitiParallelReview"));
        assertTrue(processDefinitionMap.containsKey("activitiAdhoc"));
        assertEquals(5, processDefinitionMap.size());
        
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(category matches('unexisting'))");
        assertEquals(0, processDefinitionMap.size());
        
        // Filter on name matches
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(name matches('Adhoc Activiti %'))");
        assertTrue(processDefinitionMap.containsKey("activitiAdhoc"));
        assertEquals(1, processDefinitionMap.size());
        
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(name matches('unexisting'))");
        assertEquals(0, processDefinitionMap.size());
        
        // Filter on key matches
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(key matches('activitiAd%'))");
        assertTrue(processDefinitionMap.containsKey("activitiAdhoc"));
        assertEquals(1, processDefinitionMap.size());
        
        // Use AND operator
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(category = 'http://alfresco.org' AND name = 'Adhoc Activiti Process')");
        assertTrue(processDefinitionMap.containsKey("activitiAdhoc"));
        assertEquals(1, processDefinitionMap.size());
        
        // Use OR operator
        try
        {
            processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(category = 'http://alfresco.org' OR name = 'Adhoc Activiti Process')");
            fail("Expected exception");
        }
        catch (PublicApiException e)
        {
            assertEquals(400, e.getHttpResponse().getStatusCode());
        }
    }
    
    @Test
    public void testGetProcessDefinitionById() throws Exception
    {
        RequestContext requestContext = initApiClientWithTestUser();
        
        String tenantAdmin = AuthenticationUtil.getAdminUserName() + "@" + requestContext.getNetworkId();
        RequestContext adminContext = new RequestContext(requestContext.getNetworkId(), tenantAdmin);

        String adhocKey = createProcessDefinitionKey("activitiAdhoc", requestContext);
        org.activiti.engine.repository.ProcessDefinition activitiDefinition = activitiProcessEngine.getRepositoryService()
            .createProcessDefinitionQuery()
            .processDefinitionKey(adhocKey)
            .singleResult();
        
        assertNotNull(activitiDefinition);
        
        // Get a single process definitions
        ProcessDefinitionsClient processDefinitionsClient = publicApiClient.processDefinitionsClient();
        ProcessDefinition adhocDefinition = processDefinitionsClient.findProcessDefinitionById(activitiDefinition.getId());
        assertNotNull(adhocDefinition);
        
        // Check fields of a resulting process-definition
        assertEquals(activitiDefinition.getId(), adhocDefinition.getId());
        assertEquals("activitiAdhoc", adhocDefinition.getKey());
        assertEquals(activitiDefinition.getDeploymentId(), adhocDefinition.getDeploymentId());
        assertEquals(activitiDefinition.getCategory(), adhocDefinition.getCategory());
        assertEquals(activitiDefinition.getName(), adhocDefinition.getName());
        assertEquals(activitiDefinition.getVersion(), adhocDefinition.getVersion());
        assertEquals(((ProcessDefinitionEntity) activitiDefinition).isGraphicalNotationDefined(), adhocDefinition.isGraphicNotationDefined());
        assertEquals("wf:submitAdhocTask", adhocDefinition.getStartFormResourceKey());
        
        // get process definition with admin
        publicApiClient.setRequestContext(adminContext);
        adhocDefinition = processDefinitionsClient.findProcessDefinitionById(activitiDefinition.getId());
        assertNotNull(adhocDefinition);
        
        // Check fields of a resulting process-definition
        assertEquals(activitiDefinition.getId(), adhocDefinition.getId());
        assertEquals("activitiAdhoc", adhocDefinition.getKey());
    }
    
    @Test
    public void testGetProcessDefinitionByIdUnexisting() throws Exception
    {
        initApiClientWithTestUser();
        ProcessDefinitionsClient processDefinitionsClient = publicApiClient.processDefinitionsClient();
        try 
        {
            processDefinitionsClient.findProcessDefinitionById("unexisting");
            fail("Exception expected");
        }
        catch(PublicApiException expected)
        {
            assertEquals(HttpStatus.NOT_FOUND.value(), expected.getHttpResponse().getStatusCode());
            assertErrorSummary("The entity with id: unexisting was not found", expected.getHttpResponse());
        }
    }
    
    @Test
    public void testGetProcessDefinitionStartModel() throws Exception
    {
        RequestContext requestContext = initApiClientWithTestUser();
        
        String tenantAdmin = AuthenticationUtil.getAdminUserName() + "@" + requestContext.getNetworkId();
        RequestContext adminContext = new RequestContext(requestContext.getNetworkId(), tenantAdmin);
        
        ProcessDefinitionsClient processDefinitionsClient = publicApiClient.processDefinitionsClient();
        
        String adhocKey = createProcessDefinitionKey("activitiAdhoc", requestContext);
        org.activiti.engine.repository.ProcessDefinition activitiDefinition = activitiProcessEngine.getRepositoryService()
            .createProcessDefinitionQuery()
            .processDefinitionKey(adhocKey)
            .singleResult();
        
        assertNotNull(activitiDefinition);
        
        JSONObject model = processDefinitionsClient.findStartFormModel(activitiDefinition.getId());
        assertNotNull(model);
        
        JSONArray entries = (JSONArray) model.get("entries");
        assertNotNull(entries);
        
        // Add all entries to a map, to make lookup easier
        Map<String, JSONObject> modelFieldsByName = new HashMap<String, JSONObject>();
        JSONObject entry = null;
        for(int i=0; i<entries.size(); i++) 
        {
            entry = (JSONObject) entries.get(i);
            assertNotNull(entry);
            entry = (JSONObject) entry.get("entry");
            assertNotNull(entry);
            modelFieldsByName.put((String) entry.get("name"), entry);
        }
        
        // Check well-known properties and their types
        
        // Validate bpm:description
        JSONObject modelEntry = modelFieldsByName.get("bpm_workflowDescription");
        assertNotNull(modelEntry);
        assertEquals("Description", modelEntry.get("title"));
        assertEquals("{http://www.alfresco.org/model/bpm/1.0}workflowDescription", modelEntry.get("qualifiedName"));
        assertEquals("d:text", modelEntry.get("dataType"));
        assertFalse((Boolean)modelEntry.get("required"));
        
        // Validate bpm:description
        modelEntry = modelFieldsByName.get("bpm_completionDate");
        assertNotNull(modelEntry);
        assertEquals("Completion Date", modelEntry.get("title"));
        assertEquals("{http://www.alfresco.org/model/bpm/1.0}completionDate", modelEntry.get("qualifiedName"));
        assertEquals("d:date", modelEntry.get("dataType"));
        assertFalse((Boolean)modelEntry.get("required"));
        
        // Validate cm:owner
        modelEntry = modelFieldsByName.get("cm_owner");
        assertNotNull(modelEntry);
        assertEquals("Owner", modelEntry.get("title"));
        assertEquals("{http://www.alfresco.org/model/content/1.0}owner", modelEntry.get("qualifiedName"));
        assertEquals("d:text", modelEntry.get("dataType"));
        assertFalse((Boolean)modelEntry.get("required"));
        
        // Validate bpm:sendEmailNotifications
        modelEntry = modelFieldsByName.get("bpm_sendEMailNotifications");
        assertNotNull(modelEntry);
        assertEquals("Send Email Notifications", modelEntry.get("title"));
        assertEquals("{http://www.alfresco.org/model/bpm/1.0}sendEMailNotifications", modelEntry.get("qualifiedName"));
        assertEquals("d:boolean", modelEntry.get("dataType"));
        assertFalse((Boolean)modelEntry.get("required"));
        
        // Validate bpm:priority
        modelEntry = modelFieldsByName.get("bpm_workflowPriority");
        assertNotNull(modelEntry);
        assertEquals("Workflow Priority", modelEntry.get("title"));
        assertEquals("{http://www.alfresco.org/model/bpm/1.0}workflowPriority", modelEntry.get("qualifiedName"));
        assertEquals("d:int", modelEntry.get("dataType"));
        assertEquals("2", modelEntry.get("defaultValue"));
        assertFalse((Boolean)modelEntry.get("required"));
        
        // Validate bpm:package
        modelEntry = modelFieldsByName.get("bpm_package");
        assertNotNull(modelEntry);
        assertEquals("Content Package", modelEntry.get("title"));
        assertEquals("{http://www.alfresco.org/model/bpm/1.0}package", modelEntry.get("qualifiedName"));
        assertEquals("bpm:workflowPackage", modelEntry.get("dataType"));
        assertFalse((Boolean)modelEntry.get("required"));
        
        // Validate bpm:status
        modelEntry = modelFieldsByName.get("bpm_status");
        assertNotNull(modelEntry);
        assertEquals("Status", modelEntry.get("title"));
        assertEquals("{http://www.alfresco.org/model/bpm/1.0}status", modelEntry.get("qualifiedName"));
        assertEquals("d:text", modelEntry.get("dataType"));
        assertEquals("Not Yet Started", modelEntry.get("defaultValue"));
        assertTrue((Boolean)modelEntry.get("required"));
        JSONArray allowedValues = (JSONArray) modelEntry.get("allowedValues");
        assertNotNull(allowedValues);
        assertEquals(5, allowedValues.size());
        assertTrue(allowedValues.contains("Not Yet Started"));
        assertTrue(allowedValues.contains("In Progress"));
        assertTrue(allowedValues.contains("On Hold"));
        assertTrue(allowedValues.contains("Cancelled"));
        assertTrue(allowedValues.contains("Completed"));
        
        // get start form model with admin
        publicApiClient.setRequestContext(adminContext);
        model = processDefinitionsClient.findStartFormModel(activitiDefinition.getId());
        assertNotNull(model);
        
        entries = (JSONArray) model.get("entries");
        assertNotNull(entries);
        
        // Add all entries to a map, to make lookup easier
        modelFieldsByName = new HashMap<String, JSONObject>();
        for(int i=0; i<entries.size(); i++) 
        {
            entry = (JSONObject) entries.get(i);
            assertNotNull(entry);
            entry = (JSONObject) entry.get("entry");
            assertNotNull(entry);
            modelFieldsByName.put((String) entry.get("name"), entry);
        }
        
        // Check well-known properties and their types
        
        // Validate bpm:description
        modelEntry = modelFieldsByName.get("bpm_workflowDescription");
        assertNotNull(modelEntry);
        assertEquals("Description", modelEntry.get("title"));
        assertEquals("{http://www.alfresco.org/model/bpm/1.0}workflowDescription", modelEntry.get("qualifiedName"));
        assertEquals("d:text", modelEntry.get("dataType"));
        assertFalse((Boolean)modelEntry.get("required"));
        
        // Validate bpm:description
        modelEntry = modelFieldsByName.get("bpm_completionDate");
        assertNotNull(modelEntry);
        assertEquals("Completion Date", modelEntry.get("title"));
        assertEquals("{http://www.alfresco.org/model/bpm/1.0}completionDate", modelEntry.get("qualifiedName"));
        assertEquals("d:date", modelEntry.get("dataType"));
        assertFalse((Boolean)modelEntry.get("required"));
    }
    
    @Test
    public void testGetProcessDefinitionStartModelUnexisting() throws Exception
    {
        initApiClientWithTestUser();
        ProcessDefinitionsClient processDefinitionsClient = publicApiClient.processDefinitionsClient();
        try 
        {
            processDefinitionsClient.findStartFormModel("unexisting");
            fail("Exception expected");
        }
        catch(PublicApiException expected)
        {
            assertEquals(HttpStatus.NOT_FOUND.value(), expected.getHttpResponse().getStatusCode());
            assertErrorSummary("The entity with id: unexisting was not found", expected.getHttpResponse());
        }
    }
    
    @Test
    public void testMethodNotAllowedURIs() throws Exception
    {
        RequestContext requestContext = initApiClientWithTestUser();
        HttpResponse response = publicApiClient.get("public", "process-definitions", null, null, null, null);
        assertEquals(200, response.getStatusCode());
        response = publicApiClient.post("public", "process-definitions", null, null, null, null);
        assertEquals(405, response.getStatusCode());
        response = publicApiClient.delete("public", "process-definitions", null, null, null);
        assertEquals(405, response.getStatusCode());
        response = publicApiClient.put("public", "process-definitions", null, null, null, null, null);
        assertEquals(405, response.getStatusCode());
  
        String adhocKey = createProcessDefinitionKey("activitiAdhoc", requestContext);
        org.activiti.engine.repository.ProcessDefinition processDefinition = activitiProcessEngine.getRepositoryService()
            .createProcessDefinitionQuery()
            .processDefinitionKey(adhocKey)
            .singleResult();
        
        assertNotNull(processDefinition);
        
        response = publicApiClient.get("public", "process-definitions", processDefinition.getId(), null, null, null);
        assertEquals(200, response.getStatusCode());
        response = publicApiClient.post("public", "process-definitions", processDefinition.getId(), null, null, null);
        assertEquals(405, response.getStatusCode());
        response = publicApiClient.delete("public", "process-definitions", processDefinition.getId(), null, null);
        assertEquals(405, response.getStatusCode());
        response = publicApiClient.put("public", "process-definitions", processDefinition.getId(), null, null, null, null);
        assertEquals(405, response.getStatusCode());
        
        response = publicApiClient.get("public", "process-definitions", processDefinition.getId(), "start-form-model", null, null);
        assertEquals(200, response.getStatusCode());
        response = publicApiClient.post("public", "process-definitions", processDefinition.getId(), "start-form-model", null, null);
        assertEquals(405, response.getStatusCode());
        response = publicApiClient.delete("public", "process-definitions", processDefinition.getId(), "start-form-model", null);
        assertEquals(405, response.getStatusCode());
        response = publicApiClient.put("public", "process-definitions", processDefinition.getId(), "start-form-model", null, null, null);
        assertEquals(405, response.getStatusCode());
    }
    
    @Test
    public void testAuthenticationAndAuthorization() throws Exception
    {
        // Fetching process definitions as admin should be possible
        RequestContext requestContext = initApiClientWithTestUser();
        String tenantAdmin = AuthenticationUtil.getAdminUserName() + "@" + requestContext.getNetworkId();
        publicApiClient.setRequestContext(new RequestContext(requestContext.getNetworkId(), tenantAdmin));
        
        ProcessDefinitionsClient processDefinitionsClient = publicApiClient.processDefinitionsClient();
        ListResponse<ProcessDefinition> processDefinitionsResponse = processDefinitionsClient.getProcessDefinitions(null);
        Map<String, ProcessDefinition>  processDefinitionMap = getProcessDefinitionMapByKey(processDefinitionsResponse.getList());
        
        assertTrue(processDefinitionMap.containsKey("activitiReviewPooled"));
        assertTrue(processDefinitionMap.containsKey("activitiReview"));
        assertTrue(processDefinitionMap.containsKey("activitiParallelGroupReview"));
        assertTrue(processDefinitionMap.containsKey("activitiParallelReview"));
        assertTrue(processDefinitionMap.containsKey("activitiAdhoc"));
        assertEquals(5, processDefinitionMap.size());
        
        // Fetching process definitions as admin from another tenant shouldn't be possible
        TestNetwork anotherNetwork = getOtherNetwork(requestContext.getNetworkId());
        tenantAdmin = AuthenticationUtil.getAdminUserName() + "@" + anotherNetwork.getId();
        RequestContext otherContext = new RequestContext(anotherNetwork.getId(), tenantAdmin);
        publicApiClient.setRequestContext(otherContext);
        
        processDefinitionsResponse = processDefinitionsClient.getProcessDefinitions(null);
        processDefinitionMap = getProcessDefinitionMapByKey(processDefinitionsResponse.getList());
        
        // the response should contain process definitions from the new tenant
        assertTrue(processDefinitionMap.containsKey("activitiReviewPooled"));
        assertTrue(processDefinitionMap.containsKey("activitiReview"));
        assertTrue(processDefinitionMap.containsKey("activitiParallelGroupReview"));
        assertTrue(processDefinitionMap.containsKey("activitiParallelReview"));
        assertTrue(processDefinitionMap.containsKey("activitiAdhoc"));
        assertEquals(5, processDefinitionMap.size());
        
        // Fetching a specific process definitions as admin should be possible
        publicApiClient.setRequestContext(requestContext);
        
        String adhocKey = createProcessDefinitionKey("activitiAdhoc", requestContext);
        org.activiti.engine.repository.ProcessDefinition activitiDefinition = activitiProcessEngine.getRepositoryService()
            .createProcessDefinitionQuery()
            .processDefinitionKey(adhocKey)
            .singleResult();
        
        assertNotNull(activitiDefinition);
        
        // Get a single process definitions
        ProcessDefinition adhocDefinition = processDefinitionsClient.findProcessDefinitionById(activitiDefinition.getId());
        assertNotNull(adhocDefinition);
        
        // Check fields of a resulting process-definition
        assertEquals(activitiDefinition.getId(), adhocDefinition.getId());
        assertEquals("activitiAdhoc", adhocDefinition.getKey());
        assertEquals(activitiDefinition.getDeploymentId(), adhocDefinition.getDeploymentId());
        assertEquals(activitiDefinition.getCategory(), adhocDefinition.getCategory());
        assertEquals(activitiDefinition.getName(), adhocDefinition.getName());
        assertEquals(activitiDefinition.getVersion(), adhocDefinition.getVersion());
        assertEquals(((ProcessDefinitionEntity) activitiDefinition).isGraphicalNotationDefined(), adhocDefinition.isGraphicNotationDefined());
        assertEquals("wf:submitAdhocTask", adhocDefinition.getStartFormResourceKey());
        
        // Fetching a specific process definitions as admin from another tenant should not be possible
        publicApiClient.setRequestContext(otherContext);
        try
        {
            adhocDefinition = processDefinitionsClient.findProcessDefinitionById(activitiDefinition.getId());
            fail("not found expected");
        }
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.NOT_FOUND.value(), e.getHttpResponse().getStatusCode());
        }
        
        // Fetching the start form model of a process definition as admin should be possible
        publicApiClient.setRequestContext(requestContext);
        JSONObject model = processDefinitionsClient.findStartFormModel(activitiDefinition.getId());
        assertNotNull(model);
        
        JSONArray entries = (JSONArray) model.get("entries");
        assertNotNull(entries);
        
        // Add all entries to a map, to make lookup easier
        Map<String, JSONObject> modelFieldsByName = new HashMap<String, JSONObject>();
        JSONObject entry = null;
        for(int i=0; i<entries.size(); i++) 
        {
            entry = (JSONObject) entries.get(i);
            assertNotNull(entry);
            entry = (JSONObject) entry.get("entry");
            assertNotNull(entry);
            modelFieldsByName.put((String) entry.get("name"), entry);
        }
        
        // Check well-known properties and their types
        
        // Validate bpm:description
        JSONObject modelEntry = modelFieldsByName.get("bpm_workflowDescription");
        assertNotNull(modelEntry);
        assertEquals("Description", modelEntry.get("title"));
        assertEquals("{http://www.alfresco.org/model/bpm/1.0}workflowDescription", modelEntry.get("qualifiedName"));
        assertEquals("d:text", modelEntry.get("dataType"));
        assertFalse((Boolean)modelEntry.get("required"));
        
        // Fetching a specific process definitions as admin from another tenant should not be possible
        publicApiClient.setRequestContext(otherContext);
        try
        {
            model = processDefinitionsClient.findStartFormModel(activitiDefinition.getId());
            fail("not found expected");
        }
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.NOT_FOUND.value(), e.getHttpResponse().getStatusCode());
        }
    }
    
    protected String createProcessDefinitionKey(String key, RequestContext requestContext) 
    {
        return "@" + requestContext.getNetworkId() + "@" + key;
    }
    
    protected Map<String, ProcessDefinition> getProcessDefinitionMapByKey(List<ProcessDefinition> processDefinitions) 
    {
        Map<String, ProcessDefinition>  processDefinitionMap = new HashMap<String, ProcessDefinition>();
        for (ProcessDefinition processDefinition : processDefinitions)
        {
            processDefinitionMap.put(processDefinition.getKey(), processDefinition);
        }
        return processDefinitionMap;
    }
    
    protected Map<String, ProcessDefinition> getProcessDefinitions(ProcessDefinitionsClient processDefinitionsClient, String whereClause) throws PublicApiException
    {
        Map<String, String> params = null;
        if(whereClause != null)
        {
            params = Collections.singletonMap("where", whereClause);
        }
        
        ListResponse<ProcessDefinition> processDefinitionsResponse = processDefinitionsClient.getProcessDefinitions(params);
        return getProcessDefinitionMapByKey(processDefinitionsResponse.getList());
    }
}
