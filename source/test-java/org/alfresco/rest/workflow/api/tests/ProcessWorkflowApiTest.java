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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.task.Task;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.workflow.api.model.ProcessInfo;
import org.alfresco.rest.workflow.api.tests.WorkflowApiClient.ProcessesClient;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ISO8601DateFormat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.springframework.http.HttpStatus;

/**
 * Process related Rest api tests using http client to communicate with the rest apis in the repository.
 * 
 * @author Tijs Rademakers
 *
 */
public class ProcessWorkflowApiTest extends EnterpriseWorkflowTestApi
{   
    @Test
    @SuppressWarnings("unchecked")
    public void testCreateProcessInstanceWithId() throws Exception
    {
        final RequestContext requestContext = initApiClientWithTestUser();
        
        org.activiti.engine.repository.ProcessDefinition processDefinition = activitiProcessEngine
                .getRepositoryService()
                .createProcessDefinitionQuery()
                .processDefinitionKey("@" + requestContext.getNetworkId() + "@activitiAdhoc")
                .singleResult();

        ProcessesClient processesClient = publicApiClient.processesClient();
        
        JSONObject createProcessObject = new JSONObject();
        createProcessObject.put("processDefinitionId", processDefinition.getId());
        final JSONObject variablesObject = new JSONObject();
        variablesObject.put("bpm_dueDate", ISO8601DateFormat.format(new Date()));
        variablesObject.put("bpm_priority", 1);
        variablesObject.put("bpm_description", "test description");
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                variablesObject.put("bpm_assignee", requestContext.getRunAsUser());
                return null;
            }
        }, requestContext.getRunAsUser(), requestContext.getNetworkId());
        
        
        createProcessObject.put("variables", variablesObject);
        
        final ProcessInfo processRest = processesClient.createProcess(createProcessObject.toJSONString());
        assertNotNull(processRest);
        
        final Map<String, Object> variables = activitiProcessEngine.getRuntimeService().getVariables(processRest.getId());
       
        assertEquals("test description", variables.get("bpm_description"));
        assertEquals(1, variables.get("bpm_priority"));
        
        cleanupProcessInstance(processRest.getId());
        
        // Try with unexisting process definition ID
        createProcessObject = new JSONObject();
        createProcessObject.put("processDefinitionId", "unexisting");
        try 
        {
            processesClient.createProcess(createProcessObject.toJSONString());
            fail();
        } 
        catch(PublicApiException e)
        {
            // Exception expected because of wrong process definition id
            assertEquals(HttpStatus.BAD_REQUEST.value(), e.getHttpResponse().getStatusCode());
            assertErrorSummary("No workflow definition could be found with id 'unexisting'.", e.getHttpResponse());
        }
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testCreateProcessInstanceWithKey() throws Exception
    {
        final RequestContext requestContext = initApiClientWithTestUser();
        
        ProcessesClient processesClient = publicApiClient.processesClient();
        
        JSONObject createProcessObject = new JSONObject();
        createProcessObject.put("processDefinitionKey", "activitiAdhoc");
        final JSONObject variablesObject = new JSONObject();
        variablesObject.put("bpm_dueDate", ISO8601DateFormat.format(new Date()));
        variablesObject.put("bpm_priority", 1);
        variablesObject.put("bpm_description", "test description");
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                variablesObject.put("bpm_assignee", requestContext.getRunAsUser());
                return null;
            }
        }, requestContext.getRunAsUser(), requestContext.getNetworkId());
        
        
        createProcessObject.put("variables", variablesObject);
        
        ProcessInfo processRest = processesClient.createProcess(createProcessObject.toJSONString());
        assertNotNull(processRest);
        
        final Map<String, Object> variables = activitiProcessEngine.getRuntimeService().getVariables(processRest.getId());
       
        assertEquals("test description", variables.get("bpm_description"));
        assertEquals(1, variables.get("bpm_priority"));
        
        cleanupProcessInstance(processRest.getId());
        
        createProcessObject = new JSONObject();
        createProcessObject.put("processDefinitionKey", "activitiAdhoc2");
        
        try 
        {
            processRest = processesClient.createProcess(createProcessObject.toJSONString());
            fail();
        } 
        catch(PublicApiException e)
        {
            // Exception expected because of wrong process definition key
            assertEquals(HttpStatus.BAD_REQUEST.value(), e.getHttpResponse().getStatusCode());
            assertErrorSummary("No workflow definition could be found with key 'activitiAdhoc2'.", e.getHttpResponse());
        }
    }
    
    @Test
    public void testCreateProcessInstanceWithItems() throws Exception
    {
        final RequestContext requestContext = initApiClientWithTestUser();
        
        NodeRef[] docNodeRefs = createTestDocuments(requestContext);
        final ProcessInfo processRest = startAdhocProcess(requestContext, docNodeRefs);
        assertNotNull(processRest);
        
        final Map<String, Object> variables = activitiProcessEngine.getRuntimeService().getVariables(processRest.getId());
        assertEquals(1, variables.get("bpm_priority"));
        final ActivitiScriptNode packageScriptNode = (ActivitiScriptNode) variables.get("bpm_package");
        assertNotNull(packageScriptNode);
        
        final Map<String, Document> documentMap = new HashMap<String, Document>();
        
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                List<ChildAssociationRef> documentList = nodeService.getChildAssocs(packageScriptNode.getNodeRef());
                for (ChildAssociationRef childAssociationRef : documentList)
                {
                    Document doc = getTestFixture().getRepoService().getDocument(requestContext.getNetworkId(), childAssociationRef.getChildRef());
                    documentMap.put(doc.getName(), doc);
                }
                
                final Task task = activitiProcessEngine.getTaskService().createTaskQuery().processInstanceId(processRest.getId()).singleResult();
                assertEquals(requestContext.getRunAsUser(), task.getAssignee());
                
                activitiProcessEngine.getTaskService().complete(task.getId());
                
                final Task task2 = activitiProcessEngine.getTaskService().createTaskQuery().processInstanceId(processRest.getId()).singleResult();
                assertEquals(requestContext.getRunAsUser(), task2.getAssignee());
                
                activitiProcessEngine.getTaskService().complete(task2.getId());
                return null;
            }
            
        }, requestContext.getRunAsUser(), requestContext.getNetworkId());
        
        assertEquals(2, documentMap.size());
        assertTrue(documentMap.containsKey("Test Doc1"));
        Document doc = documentMap.get("Test Doc1");
        assertEquals("Test Doc1", doc.getName());
        assertEquals("Test Doc1 Title", doc.getTitle());
        
        assertTrue(documentMap.containsKey("Test Doc2"));
        doc = documentMap.get("Test Doc2");
        assertEquals("Test Doc2", doc.getName());
        assertEquals("Test Doc2 Title", doc.getTitle());
        
        cleanupProcessInstance(processRest.getId());
    }
    
    @Test
    public void testGetProcessInstanceById() throws Exception
    {
        final RequestContext requestContext = initApiClientWithTestUser();
        ProcessesClient processesClient = publicApiClient.processesClient();
        
        final ProcessInfo process = startAdhocProcess(requestContext, null);
        try 
        {
            ProcessInfo processInfo = processesClient.findProcessById(process.getId());
            assertNotNull(processInfo);
            
            assertEquals(process.getId(), processInfo.getId());
            assertEquals(process.getBusinessKey(), processInfo.getBusinessKey());
            assertNull(processInfo.getDeleteReason());
            assertEquals(process.getDurationInMillis(), processInfo.getDurationInMillis());
            assertEquals(process.getEndedAt(), processInfo.getEndedAt());
            assertEquals(process.getProcessDefinitionId(), processInfo.getProcessDefinitionId());
            assertEquals(process.getProcessDefinitionKey(), processInfo.getProcessDefinitionKey());
            assertEquals(process.getStartedAt(), processInfo.getStartedAt());
            assertEquals(process.getSuperProcessInstanceId(), processInfo.getSuperProcessInstanceId());
        }
        finally
        {
            cleanupProcessInstance(process.getId());
        }
    }
    
    @Test
    public void testGetProcessInstanceByIdUnexisting() throws Exception
    {
        initApiClientWithTestUser();
        ProcessesClient processesClient = publicApiClient.processesClient();
        
        try {
            processesClient.findProcessById("unexisting");
            fail("Exception expected");
        } catch(PublicApiException expected) {
            assertEquals(HttpStatus.NOT_FOUND.value(), expected.getHttpResponse().getStatusCode());
            assertErrorSummary("The entity with id: unexisting was not found", expected.getHttpResponse());
        }
    }
    
    @Test
    public void testDeleteProcessInstanceById() throws Exception
    {
        final RequestContext requestContext = initApiClientWithTestUser();
        ProcessesClient processesClient = publicApiClient.processesClient();
        
        final ProcessInfo process = startAdhocProcess(requestContext, null);
        try 
        {
            processesClient.deleteProcessById(process.getId());
            
            // Check if the process was actually deleted
            assertNull(activitiProcessEngine.getRuntimeService().createProcessInstanceQuery()
                        .processInstanceId(process.getId()).singleResult());
            
            HistoricProcessInstance deletedInstance = activitiProcessEngine.getHistoryService()
                .createHistoricProcessInstanceQuery().processInstanceId(process.getId()).singleResult();
            assertNotNull(deletedInstance);
            assertNotNull(deletedInstance.getEndTime());
            assertNull(deletedInstance.getDeleteReason());
        }
        finally
        {
            cleanupProcessInstance(process.getId());
        }
    }
    
    @Test
    public void testDeleteProcessInstanceByIdUnexisting() throws Exception
    {
        initApiClientWithTestUser();
        ProcessesClient processesClient = publicApiClient.processesClient();
        
        try {
            processesClient.deleteProcessById("unexisting");
            fail("Exception expected");
        } catch(PublicApiException expected) {
            assertEquals(HttpStatus.NOT_FOUND.value(), expected.getHttpResponse().getStatusCode());
            assertErrorSummary("The entity with id: unexisting was not found", expected.getHttpResponse());
        }
    }
    
    @Test
    public void testGetProcessInstances() throws Exception
    {
        final RequestContext requestContext = initApiClientWithTestUser();
        
        final ProcessInfo process1 = startAdhocProcess(requestContext, null);
        final ProcessInfo process2 = startAdhocProcess(requestContext, null);
        final ProcessInfo process3 = startAdhocProcess(requestContext, null);
        
        ProcessesClient processesClient = publicApiClient.processesClient();
        Map<String, String> paramMap = new HashMap<String, String>();
        ListResponse<ProcessInfo> processList = processesClient.getProcesses(paramMap);
        assertNotNull(processList);
        assertEquals(3, processList.getList().size());
        
        Map<String, ProcessInfo> processMap = new HashMap<String, ProcessInfo>();
        for (ProcessInfo processRest : processList.getList())
        {
            processMap.put(processRest.getId(), processRest);
        }
        
        assertTrue(processMap.containsKey(process1.getId()));
        assertTrue(processMap.containsKey(process2.getId()));
        assertTrue(processMap.containsKey(process3.getId()));
        
        paramMap = new HashMap<String, String>();
        paramMap.put("where", "(processDefinitionKey = 'activitiAdhoc')");
        processList = processesClient.getProcesses(paramMap);
        assertNotNull(processList);
        assertEquals(3, processList.getList().size());
        
        paramMap = new HashMap<String, String>();
        paramMap.put("where", "(processDefinitionKey = 'activitiAdhoc2')");
        processList = processesClient.getProcesses(paramMap);
        assertNotNull(processList);
        assertEquals(0, processList.getList().size());
        
        paramMap = new HashMap<String, String>();
        paramMap.put("where", "(processDefinitionKey = 'activitiAdhoc')");
        paramMap.put("maxItems", "2");
        processList = processesClient.getProcesses(paramMap);
        assertNotNull(processList);
        assertEquals(2, processList.getList().size());
        
        paramMap = new HashMap<String, String>();
        paramMap.put("where", "(processDefinitionKey = 'activitiAdhoc')");
        paramMap.put("maxItems", "3");
        paramMap.put("skipCount", "1");
        processList = processesClient.getProcesses(paramMap);
        assertNotNull(processList);
        assertEquals(2, processList.getList().size());
        
        paramMap = new HashMap<String, String>();
        paramMap.put("where", "(processDefinitionKey = 'activitiAdhoc')");
        paramMap.put("maxItems", "5");
        paramMap.put("skipCount", "2");
        processList = processesClient.getProcesses(paramMap);
        assertNotNull(processList);
        assertEquals(1, processList.getList().size());
        
        paramMap = new HashMap<String, String>();
        paramMap.put("where", "(processDefinitionKey = 'activitiAdhoc')");
        paramMap.put("maxItems", "5");
        paramMap.put("skipCount", "5");
        processList = processesClient.getProcesses(paramMap);
        assertNotNull(processList);
        assertEquals(0, processList.getList().size());
        
        paramMap = new HashMap<String, String>();
        paramMap.put("where", "(status = 'completed')");
        processList = processesClient.getProcesses(paramMap);
        assertNotNull(processList);
        assertEquals(0, processList.getList().size());
        
        paramMap = new HashMap<String, String>();
        paramMap.put("where", "(status = 'any')");
        processList = processesClient.getProcesses(paramMap);
        assertNotNull(processList);
        assertEquals(3, processList.getList().size());
        
        paramMap = new HashMap<String, String>();
        paramMap.put("where", "(status = 'active')");
        processList = processesClient.getProcesses(paramMap);
        assertNotNull(processList);
        assertEquals(3, processList.getList().size());
        
        paramMap = new HashMap<String, String>();
        paramMap.put("where", "(status = 'active2')");
        try 
        {
            processList = processesClient.getProcesses(paramMap);
            fail();
        }
        catch (PublicApiException e)
        {
            // expected exception
        }
        
        // Test the variable where-clause
        paramMap = new HashMap<String, String>();
        paramMap.put("where", "(variables/bpm_priority = 'd_int 1'))");
        processList = processesClient.getProcesses(paramMap);
        assertNotNull(processList);
        assertEquals(3, processList.getList().size());
        
        paramMap = new HashMap<String, String>();
        paramMap.put("where", "(variables/bpm_priority = 'd_int 5'))");
        processList = processesClient.getProcesses(paramMap);
        assertNotNull(processList);
        assertEquals(0, processList.getList().size());
        
        cleanupProcessInstance(process1.getId(), process2.getId(), process3.getId());
    }
    
    @Test
    public void testGetProcessItems() throws Exception
    {
        final RequestContext requestContext = initApiClientWithTestUser();
        
        NodeRef[] docNodeRefs = createTestDocuments(requestContext);
        final ProcessInfo processRest = startAdhocProcess(requestContext, docNodeRefs);
        assertNotNull(processRest);
        
        final String newProcessInstanceId = processRest.getId();
        ProcessesClient processesClient = publicApiClient.processesClient();
        JSONObject itemsJSON = processesClient.findProcessItems(newProcessInstanceId);
        assertNotNull(itemsJSON);
        JSONArray entriesJSON = (JSONArray) itemsJSON.get("entries");
        assertNotNull(entriesJSON);
        assertTrue(entriesJSON.size() == 2);
        boolean doc1Found = false;
        boolean doc2Found = false;
        for (Object entryObject : entriesJSON)
        {
            JSONObject entryObjectJSON = (JSONObject) entryObject;
            JSONObject entryJSON = (JSONObject) entryObjectJSON.get("entry");
            if (entryJSON.get("name").equals("Test Doc1")) {
                doc1Found = true;
                assertEquals(docNodeRefs[0].toString(), entryJSON.get("id"));
                assertEquals("Test Doc1", entryJSON.get("name"));
                assertEquals("Test Doc1 Title", entryJSON.get("title"));
                assertEquals("Test Doc1 Description", entryJSON.get("description"));
                assertNotNull(entryJSON.get("createdAt"));
                assertEquals(requestContext.getRunAsUser(), entryJSON.get("createdBy"));
                assertNotNull(entryJSON.get("modifiedAt"));
                assertEquals(requestContext.getRunAsUser(), entryJSON.get("modifiedBy"));
                assertNotNull(entryJSON.get("size"));
                assertNotNull(entryJSON.get("mimeType"));
            } else {
                doc2Found = true;
                assertEquals(docNodeRefs[1].toString(), entryJSON.get("id"));
                assertEquals("Test Doc2", entryJSON.get("name"));
                assertEquals("Test Doc2 Title", entryJSON.get("title"));
                assertEquals("Test Doc2 Description", entryJSON.get("description"));
                assertNotNull(entryJSON.get("createdAt"));
                assertEquals(requestContext.getRunAsUser(), entryJSON.get("createdBy"));
                assertNotNull(entryJSON.get("modifiedAt"));
                assertEquals(requestContext.getRunAsUser(), entryJSON.get("modifiedBy"));
                assertNotNull(entryJSON.get("size"));
                assertNotNull(entryJSON.get("mimeType"));
            }
        }
        assertTrue(doc1Found);
        assertTrue(doc2Found);
        
        cleanupProcessInstance(processRest.getId());
    }
    
    @Test
    public void testGetProcessItem() throws Exception
    {
        final RequestContext requestContext = initApiClientWithTestUser();
        
        NodeRef[] docNodeRefs = createTestDocuments(requestContext);
        final ProcessInfo processRest = startAdhocProcess(requestContext, docNodeRefs);
        assertNotNull(processRest);
        
        final String newProcessInstanceId = processRest.getId();
        ProcessesClient processesClient = publicApiClient.processesClient();
        JSONObject itemJSON = processesClient.findProcessItem(newProcessInstanceId, docNodeRefs[0].toString());
        assertNotNull(itemJSON);
        
        assertEquals(docNodeRefs[0].toString(), itemJSON.get("id"));
        assertEquals("Test Doc1", itemJSON.get("name"));
        assertEquals("Test Doc1 Title", itemJSON.get("title"));
        assertEquals("Test Doc1 Description", itemJSON.get("description"));
        assertNotNull(itemJSON.get("createdAt"));
        assertEquals(requestContext.getRunAsUser(), itemJSON.get("createdBy"));
        assertNotNull(itemJSON.get("modifiedAt"));
        assertEquals(requestContext.getRunAsUser(), itemJSON.get("modifiedBy"));
        assertNotNull(itemJSON.get("size"));
        assertNotNull(itemJSON.get("mimeType"));
        
        cleanupProcessInstance(processRest.getId());
    }
    
    @Test
    public void testDeleteProcessItem() throws Exception
    {
        final RequestContext requestContext = initApiClientWithTestUser();
        
        NodeRef[] docNodeRefs = createTestDocuments(requestContext);
        final ProcessInfo processRest = startAdhocProcess(requestContext, docNodeRefs);
        try
        {
            assertNotNull(processRest);
            
            final String newProcessInstanceId = processRest.getId();
            ProcessesClient processesClient = publicApiClient.processesClient();
            
            // Delete the item
            processesClient.deleteProcessItem(newProcessInstanceId, docNodeRefs[0].toString());
            
            // Fetching the item should result in 404
            try {
                publicApiClient.processesClient().findProcessItem(newProcessInstanceId, docNodeRefs[0].toString());
                fail("Exception expected");
            } catch(PublicApiException expected) {
                assertEquals(HttpStatus.NOT_FOUND.value(), expected.getHttpResponse().getStatusCode());
                assertErrorSummary("The entity with id: " + docNodeRefs[0].toString() + " was not found", expected.getHttpResponse());
            }
        }
        finally
        {
            cleanupProcessInstance(processRest.getId());
        }
    }
    
    @Test
    public void testGetProcessVariables() throws Exception
    {
        RequestContext requestContext = initApiClientWithTestUser();
        
        ProcessInfo processRest = startAdhocProcess(requestContext, null);
        
        try
        {
            assertNotNull(processRest);
            String processInstanceId = processRest.getId();
            
            JSONObject processvariables = publicApiClient.processesClient().getProcessvariables(processInstanceId);
            assertNotNull(processvariables);
            
            // Add process variables to map for easy lookup
            Map<String, JSONObject> variablesByName = new HashMap<String, JSONObject>();
            JSONObject entry = null;
            JSONArray entries = (JSONArray) processvariables.get("entries");
            assertNotNull(entries);
            for(int i=0; i<entries.size(); i++) 
            {
                entry = (JSONObject) entries.get(i);
                assertNotNull(entry);
                entry = (JSONObject) entry.get("entry");
                assertNotNull(entry);
                variablesByName.put((String) entry.get("name"), entry);
            }
            
            // Test some well-known variables
            JSONObject variable = variablesByName.get("bpm_description");
            assertNotNull(variable);
            assertEquals("d:text", variable.get("type"));
            assertNull(variable.get("value"));
            
            variable = variablesByName.get("bpm_percentComplete");
            assertNotNull(variable);
            assertEquals("d:int", variable.get("type"));
            assertEquals(0L, variable.get("value"));
            
            variable = variablesByName.get("bpm_sendEMailNotifications");
            assertNotNull(variable);
            assertEquals("d:boolean", variable.get("type"));
            assertEquals(Boolean.FALSE, variable.get("value"));
            
            variable = variablesByName.get("bpm_package");
            assertNotNull(variable);
            assertEquals("bpm:workflowPackage", variable.get("type"));
            assertNotNull(variable.get("value"));
            
            variable = variablesByName.get("bpm_assignee");
            assertNotNull(variable);
            assertEquals("cm:person", variable.get("type"));
            assertEquals(requestContext.getRunAsUser(), variable.get("value"));
        }
        finally
        {
            cleanupProcessInstance(processRest.getId());
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateProcessVariables() throws Exception
    {
        RequestContext requestContext = initApiClientWithTestUser();
        
        ProcessInfo processRest = startAdhocProcess(requestContext, null);
        
        try
        {
            assertNotNull(processRest);
            String processId = processRest.getId();
            
            // Update an unexisting variable, creates a new one using explicit typing (d:long)
            JSONObject variableJson = new JSONObject();
            variableJson.put("name", "newVariable");
            variableJson.put("value", 1234L);
            variableJson.put("type", "d:long");
            
            JSONObject resultEntry = publicApiClient.processesClient().updateVariable(processId, "newVariable", variableJson);
            assertNotNull(resultEntry);
            JSONObject result = (JSONObject) resultEntry.get("entry");
            
            assertEquals("newVariable", result.get("name"));
            assertEquals(1234L, result.get("value"));
            assertEquals("d:long", result.get("type"));
            assertEquals(1234L, activitiProcessEngine.getRuntimeService().getVariable(processId, "newVariable"));
            
            
            // Update an unexisting variable, creates a new one using no tying
            variableJson = new JSONObject();
            variableJson.put("name", "stringVariable");
            variableJson.put("value", "This is a string value");
            
            resultEntry = publicApiClient.processesClient().updateVariable(processId, "stringVariable", variableJson);
            assertNotNull(resultEntry);
            result = (JSONObject) resultEntry.get("entry");
            
            assertEquals("stringVariable", result.get("name"));
            assertEquals("This is a string value", result.get("value"));
            assertEquals("d:text", result.get("type"));
            assertEquals("This is a string value", activitiProcessEngine.getRuntimeService().getVariable(processId, "stringVariable"));
            
            
            // Update an existing variable, creates a new one using explicit typing (d:long)
            variableJson = new JSONObject();
            variableJson.put("name", "newVariable");
            variableJson.put("value", 4567L);
            variableJson.put("type", "d:long");
            
            resultEntry = publicApiClient.processesClient().updateVariable(processId, "newVariable", variableJson);
            assertNotNull(resultEntry);
            result = (JSONObject) resultEntry.get("entry");
            
            assertEquals("newVariable", result.get("name"));
            assertEquals(4567L, result.get("value"));
            assertEquals("d:long", result.get("type"));
            assertEquals(4567L, activitiProcessEngine.getRuntimeService().getVariable(processId, "newVariable"));
            
            
            // Update an existing variable, creates a new one using no explicit typing 
            variableJson = new JSONObject();
            variableJson.put("name", "stringVariable");
            variableJson.put("value", "Updated string variable");
            
            resultEntry = publicApiClient.processesClient().updateVariable(processId, "stringVariable", variableJson);
            assertNotNull(resultEntry);
            result = (JSONObject) resultEntry.get("entry");
            
            assertEquals("stringVariable", result.get("name"));
            assertEquals("Updated string variable", result.get("value"));
            assertEquals("d:text", result.get("type"));
            assertEquals("Updated string variable", activitiProcessEngine.getRuntimeService().getVariable(processId, "stringVariable"));
        }
        finally
        {
            cleanupProcessInstance(processRest.getId());
        }
    }
    
    @Test
    public void testDeleteProcessVariable() throws Exception
    {
        RequestContext requestContext = initApiClientWithTestUser();
        ProcessInfo processRest = startAdhocProcess(requestContext, null);
        
        try
        {
            assertNotNull(processRest);
            String processId = processRest.getId();
            
            // Create a variable to be deleted
            activitiProcessEngine.getRuntimeService().setVariable(processId, "deleteMe", "This is a string");
            
            // Delete variable
            publicApiClient.processesClient().deleteVariable(processId, "deleteMe");
            assertFalse(activitiProcessEngine.getRuntimeService().hasVariable(processId, "deleteMe"));
            
            // Deleting again should fail with 404, as variable doesn't exist anymore
            try {
                publicApiClient.processesClient().deleteVariable(processId, "deleteMe");
                fail("Exception expected");
            } catch(PublicApiException expected) {
                assertEquals(HttpStatus.NOT_FOUND.value(), expected.getHttpResponse().getStatusCode());
                assertErrorSummary("The entity with id: deleteMe was not found", expected.getHttpResponse());
            }
        }
        finally
        {
            cleanupProcessInstance(processRest.getId());
        }
    }
    
    
    @Test
    public void testDeleteProcessVariableUnexistingProcess() throws Exception
    {
        initApiClientWithTestUser();
        
        try {
            publicApiClient.processesClient().deleteVariable("unexisting", "deleteMe");
            fail("Exception expected");
        } catch(PublicApiException expected) {
            assertEquals(HttpStatus.NOT_FOUND.value(), expected.getHttpResponse().getStatusCode());
            assertErrorSummary("The entity with id: unexisting was not found", expected.getHttpResponse());
        }
    }
    
    protected void completeAdhocTasks(String instanceId, RequestContext requestContext) {
        final Task task = activitiProcessEngine.getTaskService().createTaskQuery().processInstanceId(instanceId).singleResult();
        assertEquals(requestContext.getRunAsUser(), task.getAssignee());
        
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                activitiProcessEngine.getTaskService().complete(task.getId());
                return null;
            }
        }, requestContext.getRunAsUser(), requestContext.getNetworkId());
        
        final Task task2 = activitiProcessEngine.getTaskService().createTaskQuery().processInstanceId(instanceId).singleResult();
        assertEquals(requestContext.getRunAsUser(), task2.getAssignee());
        
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                activitiProcessEngine.getTaskService().complete(task2.getId());
                return null;
            }
        }, requestContext.getRunAsUser(), requestContext.getNetworkId());
        
        assertEquals(0, activitiProcessEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(instanceId).count());
        cleanupProcessInstance(instanceId);
    }
    
    protected void cleanupProcessInstance(String... processInstances)
    {
        // Clean up process-instance regardless of test success/failure
        try 
        {
            for (String processInstanceId : processInstances)
            {
                // try catch because runtime process may not exist anymore
                try 
                {
                    activitiProcessEngine.getRuntimeService().deleteProcessInstance(processInstanceId, null);
                } 
                catch(Exception e) {}
                activitiProcessEngine.getHistoryService().deleteHistoricProcessInstance(processInstanceId);
            }
        }
        catch (Throwable t)
        {
            // Ignore error during cleanup
        }
    }
}
