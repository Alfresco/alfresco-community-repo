/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
 * 
 *
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
        
        assertTrue(processDefinitionMap.containsKey(createProcessDefinitionKey("activitiReviewPooled", requestContext)));
        assertTrue(processDefinitionMap.containsKey(createProcessDefinitionKey("activitiReview", requestContext)));
        assertTrue(processDefinitionMap.containsKey(createProcessDefinitionKey("activitiParallelGroupReview", requestContext)));
        assertTrue(processDefinitionMap.containsKey(createProcessDefinitionKey("activitiParallelReview", requestContext)));
        assertTrue(processDefinitionMap.containsKey(createProcessDefinitionKey("activitiAdhoc", requestContext)));
        assertEquals(5, processDefinitionMap.size());
        
        
        // Check fields of a resulting process-definition
        String adhocKey = createProcessDefinitionKey("activitiAdhoc", requestContext);
        org.activiti.engine.repository.ProcessDefinition activitiDefinition = activitiProcessEngine.getRepositoryService()
            .createProcessDefinitionQuery()
            .processDefinitionKey(adhocKey)
            .singleResult();
        
        assertNotNull(activitiDefinition);
        
        ProcessDefinition adhocDefinition = processDefinitionMap.get(adhocKey);
        
        assertEquals(activitiDefinition.getId(), adhocDefinition.getId());
        assertEquals(activitiDefinition.getKey(), adhocDefinition.getKey());
        assertEquals(activitiDefinition.getDeploymentId(), adhocDefinition.getDeploymentId());
        assertEquals(activitiDefinition.getCategory(), adhocDefinition.getCategory());
        assertEquals(activitiDefinition.getName(), adhocDefinition.getName());
        assertEquals(activitiDefinition.getVersion(), adhocDefinition.getVersion());
        assertEquals(((ProcessDefinitionEntity) activitiDefinition).isGraphicalNotationDefined(), adhocDefinition.isGraphicNotationDefined());
        assertEquals("wf:submitAdhocTask", adhocDefinition.getStartFormResourceKey());
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
        
        assertTrue(processDefinitionMap.containsKey(createProcessDefinitionKey("activitiReviewPooled", requestContext)));
        assertTrue(processDefinitionMap.containsKey(createProcessDefinitionKey("activitiReview", requestContext)));
        assertTrue(processDefinitionMap.containsKey(createProcessDefinitionKey("activitiParallelGroupReview", requestContext)));
        assertTrue(processDefinitionMap.containsKey(createProcessDefinitionKey("activitiParallelReview", requestContext)));
        assertTrue(processDefinitionMap.containsKey(createProcessDefinitionKey("activitiAdhoc", requestContext)));
        assertEquals(5, processDefinitionMap.size());
        
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(category = 'unexisting')");
        assertEquals(0, processDefinitionMap.size());
        
        // Filter on name equals
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(name = 'Adhoc Activiti Process')");
        assertTrue(processDefinitionMap.containsKey(createProcessDefinitionKey("activitiAdhoc", requestContext)));
        assertEquals(1, processDefinitionMap.size());
        
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(name = 'unexisting')");
        assertEquals(0, processDefinitionMap.size());
        
        // Filter on key equals
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(key='" + adhocKey +"')");
        assertTrue(processDefinitionMap.containsKey(createProcessDefinitionKey("activitiAdhoc", requestContext)));
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
        assertTrue(processDefinitionMap.containsKey(createProcessDefinitionKey("activitiAdhoc", requestContext)));
        assertEquals(1, processDefinitionMap.size());
        
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(deploymentId='unexisting')");
        assertEquals(0, processDefinitionMap.size());
        
        // Filter on category matches
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(category matches('%alfresco.o%'))");
        
        assertTrue(processDefinitionMap.containsKey(createProcessDefinitionKey("activitiReviewPooled", requestContext)));
        assertTrue(processDefinitionMap.containsKey(createProcessDefinitionKey("activitiReview", requestContext)));
        assertTrue(processDefinitionMap.containsKey(createProcessDefinitionKey("activitiParallelGroupReview", requestContext)));
        assertTrue(processDefinitionMap.containsKey(createProcessDefinitionKey("activitiParallelReview", requestContext)));
        assertTrue(processDefinitionMap.containsKey(createProcessDefinitionKey("activitiAdhoc", requestContext)));
        assertEquals(5, processDefinitionMap.size());
        
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(category matches('unexisting'))");
        assertEquals(0, processDefinitionMap.size());
        
        // Filter on name matches
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(name matches('Adhoc Activiti %'))");
        assertTrue(processDefinitionMap.containsKey(createProcessDefinitionKey("activitiAdhoc", requestContext)));
        assertEquals(1, processDefinitionMap.size());
        
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(name matches('unexisting'))");
        assertEquals(0, processDefinitionMap.size());
        
        // Filter on key matches
        processDefinitionMap = getProcessDefinitions(processDefinitionsClient, "(key matches('" + adhocKey.substring(0, adhocKey.length() - 3) +"%'))");
        assertTrue(processDefinitionMap.containsKey(createProcessDefinitionKey("activitiAdhoc", requestContext)));
        assertEquals(1, processDefinitionMap.size());
        
    }
    
    @Test
    public void testGetProcessDefinitionById() throws Exception
    {
        RequestContext requestContext = initApiClientWithTestUser();

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
        assertEquals(activitiDefinition.getKey(), adhocDefinition.getKey());
        assertEquals(activitiDefinition.getDeploymentId(), adhocDefinition.getDeploymentId());
        assertEquals(activitiDefinition.getCategory(), adhocDefinition.getCategory());
        assertEquals(activitiDefinition.getName(), adhocDefinition.getName());
        assertEquals(activitiDefinition.getVersion(), adhocDefinition.getVersion());
        assertEquals(((ProcessDefinitionEntity) activitiDefinition).isGraphicalNotationDefined(), adhocDefinition.isGraphicNotationDefined());
        assertEquals("wf:submitAdhocTask", adhocDefinition.getStartFormResourceKey());
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
        JSONObject modelEntry = modelFieldsByName.get("bpm_description");
        assertNotNull(modelEntry);
        assertEquals("Description", modelEntry.get("title"));
        assertEquals("{http://www.alfresco.org/model/bpm/1.0}description", modelEntry.get("qualifiedName"));
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
        modelEntry = modelFieldsByName.get("bpm_priority");
        assertNotNull(modelEntry);
        assertEquals("Priority", modelEntry.get("title"));
        assertEquals("{http://www.alfresco.org/model/bpm/1.0}priority", modelEntry.get("qualifiedName"));
        assertEquals("d:int", modelEntry.get("dataType"));
        assertEquals("2", modelEntry.get("defaultValue"));
        assertTrue((Boolean)modelEntry.get("required"));
        
        // Validate bpm:package
        modelEntry = modelFieldsByName.get("bpm_package");
        assertNotNull(modelEntry);
        assertEquals("Content Package", modelEntry.get("title"));
        assertEquals("{http://www.alfresco.org/model/bpm/1.0}package", modelEntry.get("qualifiedName"));
        assertEquals("bpm:workflowPackage", modelEntry.get("dataType"));
        assertFalse((Boolean)modelEntry.get("required"));
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
