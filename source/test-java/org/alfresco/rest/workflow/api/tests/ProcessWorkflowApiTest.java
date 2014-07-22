/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.task.Task;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.workflow.api.model.ProcessInfo;
import org.alfresco.rest.workflow.api.model.Variable;
import org.alfresco.rest.workflow.api.tests.WorkflowApiClient.ProcessesClient;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ISO8601DateFormat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.experimental.categories.Category;
import org.junit.Test;
import org.springframework.http.HttpStatus;
/**
 * Process related Rest api tests using http client to communicate with the rest apis in the repository.
 * 
 * @author Tijs Rademakers
 *
 */
@Category(OwnJVMTestsCategory.class)
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
        
        ProcessInfo processRest = processesClient.createProcess(createProcessObject.toJSONString());
        assertNotNull(processRest);
        assertNotNull(processRest.getId());
        
        HistoricProcessInstance processInstance = activitiProcessEngine.getHistoryService().createHistoricProcessInstanceQuery()
                .processInstanceId(processRest.getId()).singleResult();
        
        assertEquals(processInstance.getId(), processRest.getId());
        assertEquals(processInstance.getStartActivityId(), processRest.getStartActivityId());
        assertEquals(processInstance.getStartUserId(), processRest.getStartUserId());
        assertEquals(processInstance.getStartTime(), processRest.getStartedAt());
        assertEquals(processInstance.getProcessDefinitionId(), processRest.getProcessDefinitionId());
        assertEquals("activitiAdhoc", processRest.getProcessDefinitionKey());
        assertNull(processRest.getBusinessKey());
        assertNull(processRest.getDeleteReason());
        assertNull(processRest.getDurationInMs());
        assertNull(processRest.getEndActivityId());
        assertNull(processRest.getEndedAt());
        assertNull(processRest.getSuperProcessInstanceId());
        
        Map<String, Object> variables = activitiProcessEngine.getRuntimeService().getVariables(processRest.getId());
       
        assertEquals("test description", variables.get("bpm_description"));
        assertEquals(1, variables.get("bpm_priority"));
        
        cleanupProcessInstance(processRest.getId());
        
        // Test same create method with an admin user
        String tenantAdmin = AuthenticationUtil.getAdminUserName() + "@" + requestContext.getNetworkId();
        publicApiClient.setRequestContext(new RequestContext(requestContext.getNetworkId(), tenantAdmin));
        
        processRest = processesClient.createProcess(createProcessObject.toJSONString());
        assertNotNull(processRest);
        
        variables = activitiProcessEngine.getRuntimeService().getVariables(processRest.getId());
       
        assertEquals("test description", variables.get("bpm_description"));
        assertEquals(1, variables.get("bpm_priority"));
        
        cleanupProcessInstance(processRest.getId());
        
        // Try with unexisting process definition ID
        publicApiClient.setRequestContext(requestContext);
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
        variablesObject.put("bpm_dueDate", "2013-09-30T00:00:00.000+0300");
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
        
        Map<String, Object> variables = activitiProcessEngine.getRuntimeService().getVariables(processRest.getId());
       
        assertEquals("test description", variables.get("bpm_description"));
        assertEquals(1, variables.get("bpm_priority"));
        
        cleanupProcessInstance(processRest.getId());
        
        // Test same create method with an admin user
        String tenantAdmin = AuthenticationUtil.getAdminUserName() + "@" + requestContext.getNetworkId();
        publicApiClient.setRequestContext(new RequestContext(requestContext.getNetworkId(), tenantAdmin));
        
        processRest = processesClient.createProcess(createProcessObject.toJSONString());
        assertNotNull(processRest);
        
        variables = activitiProcessEngine.getRuntimeService().getVariables(processRest.getId());
       
        assertEquals("test description", variables.get("bpm_description"));
        assertEquals(1, variables.get("bpm_priority"));
        
        cleanupProcessInstance(processRest.getId());
        
        // Test create process with wrong key
        publicApiClient.setRequestContext(requestContext);
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
    public void testCreateProcessInstanceWithNoParams() throws Exception
    {
        initApiClientWithTestUser();
        
        ProcessesClient processesClient = publicApiClient.processesClient();
        
        JSONObject createProcessObject = new JSONObject();
        try
        {
            processesClient.createProcess(createProcessObject.toJSONString());
            fail("Exception excpected");
        }
        catch (PublicApiException e)
        {
            assertEquals(400, e.getHttpResponse().getStatusCode());
        }
    }
    
    @Test
    public void testMethodNotAllowedURIs() throws Exception
    {
        RequestContext requestContext = initApiClientWithTestUser();
        HttpResponse response = publicApiClient.get("public", "processes", null, null, null, null);
        assertEquals(200, response.getStatusCode());
        response = publicApiClient.put("public", "processes", null, null, null, null, null);
        assertEquals(405, response.getStatusCode());
  
        final ProcessInfo processInfo = startAdhocProcess(requestContext, null);
        
        try
        {
            response = publicApiClient.get("public", "processes", processInfo.getId(), null, null, null);
            assertEquals(200, response.getStatusCode());
            response = publicApiClient.post("public", "processes", processInfo.getId(), null, null, null);
            assertEquals(405, response.getStatusCode());
            response = publicApiClient.put("public", "processes", processInfo.getId(), null, null, null, null);
            assertEquals(405, response.getStatusCode());
            
            response = publicApiClient.get("public", "processes", processInfo.getId(), "activities", null, null);
            assertEquals(200, response.getStatusCode());
            response = publicApiClient.post("public", "processes", processInfo.getId(), "activities", null, null);
            assertEquals(405, response.getStatusCode());
            response = publicApiClient.delete("public", "processes", processInfo.getId(), "activities", null);
            assertEquals(405, response.getStatusCode());
            response = publicApiClient.put("public", "processes", processInfo.getId(), "activities", null, null, null);
            assertEquals(405, response.getStatusCode());
            
            response = publicApiClient.get("public", "processes", processInfo.getId(), "tasks", null, null);
            assertEquals(200, response.getStatusCode());
            response = publicApiClient.post("public", "processes", processInfo.getId(), "tasks", null, null);
            assertEquals(405, response.getStatusCode());
            response = publicApiClient.delete("public", "processes", processInfo.getId(), "tasks", null);
            assertEquals(405, response.getStatusCode());
            response = publicApiClient.put("public", "processes", processInfo.getId(), "tasks", null, null, null);
            assertEquals(405, response.getStatusCode());
        }
        finally
        {
            cleanupProcessInstance(processInfo.getId());
        }
    }
    
    @Test
    public void testCreateProcessInstanceForPooledReview() throws Exception
    {
        final RequestContext requestContext = initApiClientWithTestUser();
        final ProcessInfo processInfo = startReviewPooledProcess(requestContext);
        assertNotNull(processInfo);
        assertNotNull(processInfo.getId());
        cleanupProcessInstance(processInfo.getId());
    }
    
    @Test
    public void testCreateProcessInstanceForParallelReview() throws Exception
    {
        final RequestContext requestContext = initApiClientWithTestUser();
        final ProcessInfo processInfo = startParallelReviewProcess(requestContext);
        assertNotNull(processInfo);
        assertNotNull(processInfo.getId());
        cleanupProcessInstance(processInfo.getId());
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testCreateProcessInstanceFromOtherNetwork() throws Exception
    {
        final RequestContext requestContext = initApiClientWithTestUser();
        
        org.activiti.engine.repository.ProcessDefinition processDefinition = activitiProcessEngine
                .getRepositoryService()
                .createProcessDefinitionQuery()
                .processDefinitionKey("@" + requestContext.getNetworkId() + "@activitiAdhoc")
                .singleResult();

        TestNetwork anotherNetwork = getOtherNetwork(requestContext.getNetworkId());
        String tenantAdmin = AuthenticationUtil.getAdminUserName() + "@" + anotherNetwork.getId();
        RequestContext otherContext = new RequestContext(anotherNetwork.getId(), tenantAdmin);
        publicApiClient.setRequestContext(otherContext);
        
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
        
        try
        {
            processesClient.createProcess(createProcessObject.toJSONString());
        }
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.BAD_REQUEST.value(), e.getHttpResponse().getStatusCode());
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
            
            final Map<String, Object> variables = activitiProcessEngine.getRuntimeService().getVariables(processInfo.getId());
            assertEquals(1, variables.get("bpm_priority"));

            HistoricProcessInstance processInstance = activitiProcessEngine.getHistoryService().createHistoricProcessInstanceQuery()
                    .processInstanceId(processInfo.getId()).singleResult();
            
            assertNotNull(processInfo.getId());
            assertEquals(processInstance.getId(), processInfo.getId());
            assertNotNull(processInfo.getStartActivityId());
            assertEquals(processInstance.getStartActivityId(), processInfo.getStartActivityId());
            assertNotNull(processInfo.getStartUserId());
            assertEquals(processInstance.getStartUserId(), processInfo.getStartUserId());
            assertNotNull(processInfo.getStartedAt());
            assertEquals(processInstance.getStartTime(), processInfo.getStartedAt());
            assertNotNull(processInfo.getProcessDefinitionId());
            assertEquals(processInstance.getProcessDefinitionId(), processInfo.getProcessDefinitionId());
            assertNotNull(processInfo.getProcessDefinitionKey());
            assertEquals("activitiAdhoc", processInfo.getProcessDefinitionKey());
            assertNull(processInfo.getBusinessKey());
            assertNull(processInfo.getDeleteReason());
            assertNull(processInfo.getDurationInMs());
            assertNull(processInfo.getEndActivityId());
            assertNull(processInfo.getEndedAt());
            assertNull(processInfo.getSuperProcessInstanceId());
            assertFalse(processInfo.isCompleted());
            
            TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    // now complete the process and see if ending info is available in the REST response
                    Task task = activitiProcessEngine.getTaskService().createTaskQuery().processInstanceId(process.getId()).singleResult();
                    activitiProcessEngine.getTaskService().complete(task.getId());
                    task = activitiProcessEngine.getTaskService().createTaskQuery().processInstanceId(process.getId()).singleResult();
                    activitiProcessEngine.getTaskService().complete(task.getId());
                    return null;
                }
            }, requestContext.getRunAsUser(), requestContext.getNetworkId());
            
            processInstance = activitiProcessEngine.getHistoryService().createHistoricProcessInstanceQuery()
                    .processInstanceId(processInfo.getId()).singleResult();
            
            processInfo = processesClient.findProcessById(processInfo.getId());
            
            assertNotNull(processInfo.getId());
            assertEquals(processInstance.getId(), processInfo.getId());
            assertNotNull(processInfo.getStartActivityId());
            assertEquals(processInstance.getStartActivityId(), processInfo.getStartActivityId());
            assertNotNull(processInfo.getStartUserId());
            assertEquals(processInstance.getStartUserId(), processInfo.getStartUserId());
            assertNotNull(processInfo.getStartedAt());
            assertEquals(processInstance.getStartTime(), processInfo.getStartedAt());
            assertNotNull(processInfo.getProcessDefinitionId());
            assertEquals(processInstance.getProcessDefinitionId(), processInfo.getProcessDefinitionId());
            assertNotNull(processInfo.getProcessDefinitionKey());
            assertEquals("activitiAdhoc", processInfo.getProcessDefinitionKey());
            assertNull(processInfo.getBusinessKey());
            assertNull(processInfo.getDeleteReason());
            assertNotNull(processInfo.getDurationInMs());
            assertEquals(processInstance.getDurationInMillis(), processInfo.getDurationInMs());
            assertNotNull(processInfo.getEndActivityId());
            assertEquals(processInstance.getEndActivityId(), processInfo.getEndActivityId());
            assertNotNull(processInfo.getEndedAt());
            assertEquals(processInstance.getEndTime(), processInfo.getEndedAt());
            assertNull(processInfo.getSuperProcessInstanceId());
            assertTrue(processInfo.isCompleted());
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
        
        try 
        {
            processesClient.findProcessById("unexisting");
            fail("Exception expected");
        } 
        catch(PublicApiException expected) 
        {
            assertEquals(HttpStatus.NOT_FOUND.value(), expected.getHttpResponse().getStatusCode());
            assertErrorSummary("The entity with id: unexisting was not found", expected.getHttpResponse());
        }
    }
    
    @Test
    public void testDeleteProcessInstanceById() throws Exception
    {
        final RequestContext requestContext = initApiClientWithTestUser();
        
        String tenantAdmin = AuthenticationUtil.getAdminUserName() + "@" + requestContext.getNetworkId();
        final RequestContext adminContext = new RequestContext(requestContext.getNetworkId(), tenantAdmin);
        
        TestNetwork anotherNetwork = getOtherNetwork(requestContext.getNetworkId());
        tenantAdmin = AuthenticationUtil.getAdminUserName() + "@" + anotherNetwork.getId();
        final RequestContext otherContext = new RequestContext(anotherNetwork.getId(), tenantAdmin);
        
        ProcessesClient processesClient = publicApiClient.processesClient();
        
        // delete with user starting the process instance
        ProcessInfo process = startAdhocProcess(requestContext, null);
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
            assertEquals("deleted through REST API call", deletedInstance.getDeleteReason());
            
            try
            {
                processesClient.deleteProcessById(process.getId());
                fail("expected exeception");
            }
            catch (PublicApiException e)
            {
                assertEquals(HttpStatus.NOT_FOUND.value(), e.getHttpResponse().getStatusCode());
            }
        }
        finally
        {
            cleanupProcessInstance(process.getId());
        }
        
        // delete with admin in same network as the user starting the process instance
        process = startAdhocProcess(requestContext, null);
        try 
        {
            publicApiClient.setRequestContext(adminContext);
            processesClient.deleteProcessById(process.getId());
            
            // Check if the process was actually deleted
            assertNull(activitiProcessEngine.getRuntimeService().createProcessInstanceQuery()
                        .processInstanceId(process.getId()).singleResult());
            
            HistoricProcessInstance deletedInstance = activitiProcessEngine.getHistoryService()
                .createHistoricProcessInstanceQuery().processInstanceId(process.getId()).singleResult();
            assertNotNull(deletedInstance);
            assertNotNull(deletedInstance.getEndTime());
            assertEquals("deleted through REST API call", deletedInstance.getDeleteReason());
        }
        finally
        {
            cleanupProcessInstance(process.getId());
        }
        
        // delete with admin from other network as the user starting the process instance
        process = startAdhocProcess(requestContext, null);
        try 
        {
            publicApiClient.setRequestContext(otherContext);
            processesClient.deleteProcessById(process.getId());
            fail("Expect permission exception");
        }
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.FORBIDDEN.value(), e.getHttpResponse().getStatusCode());
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
        
        try
        {
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
            assertNull(processList.getList().get(0).getProcessVariables());
            
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
            paramMap.put("where", "(variables/bpm_priority = 'd_int 1')");
            processList = processesClient.getProcesses(paramMap);
            assertNotNull(processList);
            assertEquals(3, processList.getList().size());
            
            paramMap = new HashMap<String, String>();
            paramMap.put("where", "(variables/bpm_priority = 'd:int 1')");
            processList = processesClient.getProcesses(paramMap);
            assertNotNull(processList);
            assertEquals(3, processList.getList().size());
            
            paramMap = new HashMap<String, String>();
            paramMap.put("where", "(variables/bpm_priority = 'd_int 5')");
            processList = processesClient.getProcesses(paramMap);
            assertNotNull(processList);
            assertEquals(0, processList.getList().size());
            
            // test with date variable
            Calendar dateCal = Calendar.getInstance();
            Map<String, Object> variablesToSet = new HashMap<String, Object>();
            variablesToSet.put("testVarDate", dateCal.getTime());
            
            activitiProcessEngine.getRuntimeService().setVariables(process1.getId(), variablesToSet);
            paramMap = new HashMap<String, String>();
            paramMap.put("where", "(variables/testVarDate = 'd_datetime " + ISO8601DateFormat.format(dateCal.getTime())+ "')");
            processList = processesClient.getProcesses(paramMap);
            assertNotNull(processList);
            assertEquals(1, processList.getList().size());
            
            // include process variables as well
            paramMap = new HashMap<String, String>();
            paramMap.put("where", "(includeVariables=true)");
            paramMap.put("maxItems", "1");
            paramMap.put("skipCount", "0");
            processList = processesClient.getProcesses(paramMap);
            assertNotNull(processList);
            
            assertEquals(1, processList.getList().size());
            
            paramMap = new HashMap<String, String>();
            paramMap.put("where", "(processDefinitionKey = 'activitiAdhoc' AND includeVariables = true)");
            paramMap.put("maxItems", "1");
            paramMap.put("skipCount", "0");
            processList = processesClient.getProcesses(paramMap);
            assertNotNull(processList);
            
            assertEquals(1, processList.getList().size());
            
            ProcessInfo processInfo = processList.getList().get(0);
            assertNotNull(processInfo.getProcessVariables());
            
            boolean foundDescription = false;
            boolean foundAssignee = false;
            for (Variable variable : processInfo.getProcessVariables()) 
            {
                if ("bpm_description".equals(variable.getName())) 
                {
                    assertEquals("d:text", variable.getType());
                    assertNull(variable.getValue());
                    foundDescription = true;
                }
                else if ("bpm_assignee".equals(variable.getName())) 
                {
                    assertEquals("cm:person", variable.getType());
                    assertEquals(requestContext.getRunAsUser(), variable.getValue());
                    foundAssignee = true;
                }
            }
            
            assertTrue(foundDescription);
            assertTrue(foundAssignee);
            
            // include process variables with paging
            paramMap = new HashMap<String, String>();
            paramMap.put("where", "(processDefinitionKey = 'activitiAdhoc' AND includeVariables = true)");
            paramMap.put("maxItems", "3");
            paramMap.put("skipCount", "1");
            processList = processesClient.getProcesses(paramMap);
            assertNotNull(processList);
            
            assertEquals(2, processList.getList().size());
            
            processInfo = processList.getList().get(0);
            assertNotNull(processInfo.getProcessVariables());
            
            foundDescription = false;
            foundAssignee = false;
            for (Variable variable : processInfo.getProcessVariables()) 
            {
                if ("bpm_description".equals(variable.getName())) 
                {
                    assertEquals("d:text", variable.getType());
                    assertNull(variable.getValue());
                    foundDescription = true;
                }
                else if ("bpm_assignee".equals(variable.getName())) 
                {
                    assertEquals("cm:person", variable.getType());
                    assertEquals(requestContext.getRunAsUser(), variable.getValue());
                    foundAssignee = true;
                }
            }
            
            assertTrue(foundDescription);
            assertTrue(foundAssignee);
            
            // include process variables with paging outside boundaries
            paramMap = new HashMap<String, String>();
            paramMap.put("where", "(processDefinitionKey = 'activitiAdhoc' AND includeVariables = true)");
            paramMap.put("maxItems", "4");
            paramMap.put("skipCount", "5");
            processList = processesClient.getProcesses(paramMap);
            assertNotNull(processList);
            
            assertEquals(0, processList.getList().size());
            
        } 
        finally
        {
            cleanupProcessInstance(process1.getId(), process2.getId(), process3.getId());
        }
    }
    
    @Test
    public void testGetProcessInstancesWithDifferentProcessDefs() throws Exception
    {
        final RequestContext requestContext = initApiClientWithTestUser();
        
        final ProcessInfo process1 = startAdhocProcess(requestContext, null);
        final ProcessInfo process2 = startParallelReviewProcess(requestContext);
        final ProcessInfo process3 = startReviewPooledProcess(requestContext);
        
        try
        {
            ProcessesClient processesClient = publicApiClient.processesClient();
            Map<String, String> paramMap = new HashMap<String, String>();
            ListResponse<ProcessInfo> processList = processesClient.getProcesses(paramMap);
            assertNotNull(processList);
            assertEquals(3, processList.getList().size());
            
            // include process variables as well
            paramMap = new HashMap<String, String>();
            paramMap.put("where", "(includeVariables=true)");
            processList = processesClient.getProcesses(paramMap);
            assertNotNull(processList);
            
            assertEquals(3, processList.getList().size());
        } 
        finally
        {
            cleanupProcessInstance(process1.getId(), process2.getId(), process3.getId());
        }
    }
    
    @Test
    public void testGetProcessInstancesWithPaging() throws Exception
    {
        final RequestContext requestContext = initApiClientWithTestUser();
        
        final ProcessInfo process1 = startAdhocProcess(requestContext, null);
        final ProcessInfo process2 = startAdhocProcess(requestContext, null);
        final ProcessInfo process3 = startAdhocProcess(requestContext, null);
        
        try
        {
            ProcessesClient processesClient = publicApiClient.processesClient();
            
            // Test with existing processDefinitionId
            Map<String, String> params = new HashMap<String, String>();
            params.put("processDefinitionId", process1.getProcessDefinitionId());
            JSONObject processListJSONObject = processesClient.getProcessesJSON(params);
            assertNotNull(processListJSONObject);
            JSONObject paginationJSON = (JSONObject) processListJSONObject.get("pagination");
            assertEquals(3l, paginationJSON.get("count"));
            assertEquals(3l, paginationJSON.get("totalItems"));
            assertEquals(0l, paginationJSON.get("skipCount"));
            assertEquals(false, paginationJSON.get("hasMoreItems"));
            JSONArray jsonEntries = (JSONArray) processListJSONObject.get("entries");
            assertEquals(3, jsonEntries.size());
            
            // Test with existing processDefinitionId and max items
            params.clear();
            params.put("maxItems", "2");
            params.put("processDefinitionId",  process1.getProcessDefinitionId());
            processListJSONObject = processesClient.getProcessesJSON(params);
            assertNotNull(processListJSONObject);
            paginationJSON = (JSONObject) processListJSONObject.get("pagination");
            assertEquals(2l, paginationJSON.get("count"));
            assertEquals(3l, paginationJSON.get("totalItems"));
            assertEquals(0l, paginationJSON.get("skipCount"));
            assertEquals(true, paginationJSON.get("hasMoreItems"));
            jsonEntries = (JSONArray) processListJSONObject.get("entries");
            assertEquals(2, jsonEntries.size());
            
            // Test with existing processDefinitionId and skip count
            params.clear();
            params.put("skipCount", "1");
            params.put("processDefinitionId", process1.getProcessDefinitionId());
            processListJSONObject = processesClient.getProcessesJSON(params);
            assertNotNull(processListJSONObject);
            paginationJSON = (JSONObject) processListJSONObject.get("pagination");
            assertEquals(2l, paginationJSON.get("count"));
            assertEquals(3l, paginationJSON.get("totalItems"));
            assertEquals(1l, paginationJSON.get("skipCount"));

            // MNT-10977: Workflow process retrieval returns incorrect hasMoreItems value
            // Repository must answer 'false' for 'hasMoreItems' since the total number of items is 3, 2 items are requested and 1 is skipped
            assertEquals(false, paginationJSON.get("hasMoreItems"));
            jsonEntries = (JSONArray) processListJSONObject.get("entries");
            assertEquals(2, jsonEntries.size());
            
            // Test with existing processDefinitionId and max items and skip count
            params.clear();
            params.put("maxItems", "3");
            params.put("skipCount", "2");
            params.put("processDefinitionId", process1.getProcessDefinitionId());
            processListJSONObject = processesClient.getProcessesJSON(params);
            assertNotNull(processListJSONObject);
            paginationJSON = (JSONObject) processListJSONObject.get("pagination");
            assertEquals(1l, paginationJSON.get("count"));
            assertEquals(3l, paginationJSON.get("totalItems"));
            assertEquals(2l, paginationJSON.get("skipCount"));

            // MNT-10977: Workflow process retrieval returns incorrect hasMoreItems value
            // Repository must answer 'false' for 'hasMoreItems' since the total number of items is 3 and 2 items are skipped
            assertEquals(false, paginationJSON.get("hasMoreItems"));
            jsonEntries = (JSONArray) processListJSONObject.get("entries");
            assertEquals(1, jsonEntries.size());
        } 
        finally
        {
            cleanupProcessInstance(process1.getId(), process2.getId(), process3.getId());
        }
    }
    
    @Test
    public void testGetProcessInstancesWithSorting() throws Exception
    {
        final RequestContext requestContext = initApiClientWithTestUser();
        
        final ProcessInfo process1 = startAdhocProcess(requestContext, null, "akey");
        final ProcessInfo process2 = startAdhocProcess(requestContext, null, "bkey");
        final ProcessInfo process3 = startAdhocProcess(requestContext, null, "aakey");
        
        try
        {
            // sort on business key ascending
            ProcessesClient processesClient = publicApiClient.processesClient();
            Map<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("orderBy", "businessKey ASC");
            ListResponse<ProcessInfo> processList = processesClient.getProcesses(paramMap);
            assertNotNull(processList);
            assertEquals(3, processList.getList().size());
            
            assertEquals(process3.getId(), processList.getList().get(0).getId());
            assertEquals(process1.getId(), processList.getList().get(1).getId());
            assertEquals(process2.getId(), processList.getList().get(2).getId());
            
            // sort on business key descending
            paramMap.put("orderBy", "businessKey DESC");
            processList = processesClient.getProcesses(paramMap);
            assertNotNull(processList);
            assertEquals(3, processList.getList().size());
            
            assertEquals(process2.getId(), processList.getList().get(0).getId());
            assertEquals(process1.getId(), processList.getList().get(1).getId());
            assertEquals(process3.getId(), processList.getList().get(2).getId());
            
            // sort on non existing key
            paramMap.put("orderBy", "businessKey2 ASC");
            try 
            {
                processList = processesClient.getProcesses(paramMap);
                fail();
            }
            catch (PublicApiException e)
            {
                assertEquals(HttpStatus.BAD_REQUEST.value(), e.getHttpResponse().getStatusCode());
            }
            
            // sort on non existing sort order (default ASC is taken)
            paramMap.put("orderBy", "businessKey ASC2");
            processList = processesClient.getProcesses(paramMap);
            assertNotNull(processList);
            assertEquals(3, processList.getList().size());
            
            assertEquals(process3.getId(), processList.getList().get(0).getId());
            assertEquals(process1.getId(), processList.getList().get(1).getId());
            assertEquals(process2.getId(), processList.getList().get(2).getId());
        }
        finally
        {
            cleanupProcessInstance(process1.getId(), process2.getId(), process3.getId());
        }
    }
    
    @Test
    public void testGetProcessTasks() throws Exception
    {
        final RequestContext requestContext = initApiClientWithTestUser();
        
        String tenantAdmin = AuthenticationUtil.getAdminUserName() + "@" + requestContext.getNetworkId();
        final RequestContext adminContext = new RequestContext(requestContext.getNetworkId(), tenantAdmin);
        
        TestNetwork anotherNetwork = getOtherNetwork(requestContext.getNetworkId());
        tenantAdmin = AuthenticationUtil.getAdminUserName() + "@" + anotherNetwork.getId();
        final RequestContext otherContext = new RequestContext(anotherNetwork.getId(), tenantAdmin);
        
        String otherPerson = getOtherPersonInNetwork(requestContext.getRunAsUser(), requestContext.getNetworkId()).getId();
        RequestContext otherPersonContext = new RequestContext(requestContext.getNetworkId(), otherPerson);
        
        final ProcessInfo process1 = startAdhocProcess(requestContext, null);
        
        try
        {
            ProcessesClient processesClient = publicApiClient.processesClient();
            Map<String, String> paramMap = new HashMap<String, String>();
            JSONObject tasksJSON = processesClient.getTasks(process1.getId(), paramMap);
            assertNotNull(tasksJSON);
            JSONArray entriesJSON = (JSONArray) tasksJSON.get("entries");
            assertNotNull(entriesJSON);
            assertTrue(entriesJSON.size() == 1);
            JSONObject taskJSONObject = (JSONObject) ((JSONObject) entriesJSON.get(0)).get("entry");
            assertNotNull(taskJSONObject.get("id"));
            assertEquals(process1.getId(), taskJSONObject.get("processId"));
            assertEquals(process1.getProcessDefinitionId(), taskJSONObject.get("processDefinitionId"));
            assertEquals("adhocTask", taskJSONObject.get("activityDefinitionId"));
            assertEquals("Adhoc Task", taskJSONObject.get("name"));
            assertEquals(requestContext.getRunAsUser(), taskJSONObject.get("assignee"));
            assertEquals(2l, taskJSONObject.get("priority"));
            assertEquals("wf:adhocTask", taskJSONObject.get("formResourceKey"));
            assertNull(taskJSONObject.get("endedAt"));
            assertNull(taskJSONObject.get("durationInMs"));
            
            paramMap = new HashMap<String, String>();
            paramMap.put("status", "active");
            tasksJSON = processesClient.getTasks(process1.getId(), paramMap);
            assertNotNull(tasksJSON);
            entriesJSON = (JSONArray) tasksJSON.get("entries");
            assertNotNull(entriesJSON);
            assertTrue(entriesJSON.size() == 1);
            
            paramMap = new HashMap<String, String>();
            paramMap.put("status", "completed");
            tasksJSON = processesClient.getTasks(process1.getId(), paramMap);
            assertNotNull(tasksJSON);
            entriesJSON = (JSONArray) tasksJSON.get("entries");
            assertNotNull(entriesJSON);
            assertTrue(entriesJSON.size() == 0);
            
            paramMap = new HashMap<String, String>();
            try {
                processesClient.getTasks("fakeid", paramMap);
                fail("Exception expected");
            } catch(PublicApiException expected) {
                assertEquals(HttpStatus.NOT_FOUND.value(), expected.getHttpResponse().getStatusCode());
                assertErrorSummary("The entity with id: fakeid was not found", expected.getHttpResponse());
            }
            
            // get tasks with admin from the same tenant as the process initiator
            publicApiClient.setRequestContext(adminContext);
            paramMap = new HashMap<String, String>();
            tasksJSON = processesClient.getTasks(process1.getId(), paramMap);
            assertNotNull(tasksJSON);
            entriesJSON = (JSONArray) tasksJSON.get("entries");
            assertNotNull(entriesJSON);
            assertTrue(entriesJSON.size() == 1);
            
            // get tasks with admin from another tenant as the process initiator
            publicApiClient.setRequestContext(otherContext);
            paramMap = new HashMap<String, String>();
            try
            {
                tasksJSON = processesClient.getTasks(process1.getId(), paramMap);
                fail("forbidden expected");
            }
            catch (PublicApiException e)
            {
                assertEquals(HttpStatus.FORBIDDEN.value(), e.getHttpResponse().getStatusCode());
            }
            
            // get task with other not-involved person
            publicApiClient.setRequestContext(otherPersonContext);
            paramMap = new HashMap<String, String>();
            try
            {
                tasksJSON = processesClient.getTasks(process1.getId(), paramMap);
                fail("forbidden expected");
            }
            catch (PublicApiException e)
            {
                assertEquals(HttpStatus.FORBIDDEN.value(), e.getHttpResponse().getStatusCode());
            }
            
            // involve other person and get task
            final Task task = activitiProcessEngine.getTaskService().createTaskQuery().
                    processInstanceId(process1.getId())
                    .singleResult();
            
            activitiProcessEngine.getTaskService().addCandidateUser(task.getId(), otherPersonContext.getRunAsUser());
            
            publicApiClient.setRequestContext(otherPersonContext);
            paramMap = new HashMap<String, String>();
            tasksJSON = processesClient.getTasks(process1.getId(), paramMap);
            assertNotNull(tasksJSON);
            entriesJSON = (JSONArray) tasksJSON.get("entries");
            assertNotNull(entriesJSON);
            assertTrue(entriesJSON.size() == 1);
            
            // complete task and get tasks
            TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    activitiProcessEngine.getTaskService().complete(task.getId());
                    return null;
                }
            }, requestContext.getRunAsUser(), requestContext.getNetworkId());
            
            publicApiClient.setRequestContext(requestContext);
            paramMap = new HashMap<String, String>();
            paramMap.put("status", "any");
            tasksJSON = processesClient.getTasks(process1.getId(), paramMap);
            assertNotNull(tasksJSON);
            entriesJSON = (JSONArray) tasksJSON.get("entries");
            assertNotNull(entriesJSON);
            assertTrue(entriesJSON.size() == 2);
            
            publicApiClient.setRequestContext(otherPersonContext);
            paramMap = new HashMap<String, String>();
            paramMap.put("status", "any");
            tasksJSON = processesClient.getTasks(process1.getId(), paramMap);
            assertNotNull(tasksJSON);
            entriesJSON = (JSONArray) tasksJSON.get("entries");
            assertNotNull(entriesJSON);
            assertTrue(entriesJSON.size() == 2);
            
        }
        finally
        {
            cleanupProcessInstance(process1.getId());
        }
    }
    
    @Test
    public void testGetProcessActivities() throws Exception
    {
        final RequestContext requestContext = initApiClientWithTestUser();
        
        String tenantAdmin = AuthenticationUtil.getAdminUserName() + "@" + requestContext.getNetworkId();
        final RequestContext adminContext = new RequestContext(requestContext.getNetworkId(), tenantAdmin);
        
        TestNetwork anotherNetwork = getOtherNetwork(requestContext.getNetworkId());
        tenantAdmin = AuthenticationUtil.getAdminUserName() + "@" + anotherNetwork.getId();
        final RequestContext otherContext = new RequestContext(anotherNetwork.getId(), tenantAdmin);
        
        final ProcessInfo process1 = startAdhocProcess(requestContext, null);
        
        try
        {
            ProcessesClient processesClient = publicApiClient.processesClient();
            Map<String, String> paramMap = new HashMap<String, String>();
            JSONObject activitiesJSON = processesClient.getActivities(process1.getId(), paramMap);
            assertNotNull(activitiesJSON);
            JSONArray entriesJSON = (JSONArray) activitiesJSON.get("entries");
            assertNotNull(entriesJSON);
            assertTrue(entriesJSON.size() == 2);
            
            Map<String, JSONObject> activitiesMap = new HashMap<String, JSONObject>();
            for (Object entry : entriesJSON) {
                JSONObject jsonEntry = (JSONObject) entry;
                JSONObject activityJSONObject = (JSONObject) jsonEntry.get("entry");
                activitiesMap.put((String) activityJSONObject.get("activityDefinitionId"), activityJSONObject);
            }
            
            JSONObject activityJSONObject = activitiesMap.get("start");
            assertNotNull(activityJSONObject);
            assertNotNull(activityJSONObject.get("id"));
            assertEquals("start", activityJSONObject.get("activityDefinitionId"));
            assertNull(activityJSONObject.get("activityDefinitionName"));
            assertEquals("startEvent", activityJSONObject.get("activityDefinitionType"));
            assertNotNull(activityJSONObject.get("startedAt"));
            assertNotNull(activityJSONObject.get("endedAt"));
            assertNotNull(activityJSONObject.get("durationInMs"));
            
            activityJSONObject = activitiesMap.get("adhocTask");
            assertNotNull(activityJSONObject);
            assertNotNull(activityJSONObject.get("id"));
            assertEquals("adhocTask", activityJSONObject.get("activityDefinitionId"));
            assertEquals("Adhoc Task", activityJSONObject.get("activityDefinitionName"));
            assertEquals("userTask", activityJSONObject.get("activityDefinitionType"));
            assertNotNull(activityJSONObject.get("startedAt"));
            assertNull(activityJSONObject.get("endedAt"));
            assertNull(activityJSONObject.get("durationInMs"));
            
            paramMap = new HashMap<String, String>();
            paramMap.put("status", "active");
            activitiesJSON = processesClient.getActivities(process1.getId(), paramMap);
            assertNotNull(activitiesJSON);
            entriesJSON = (JSONArray) activitiesJSON.get("entries");
            assertNotNull(entriesJSON);
            assertTrue(entriesJSON.size() == 1);
            
            paramMap = new HashMap<String, String>();
            paramMap.put("status", "completed");
            activitiesJSON = processesClient.getActivities(process1.getId(), paramMap);
            assertNotNull(activitiesJSON);
            entriesJSON = (JSONArray) activitiesJSON.get("entries");
            assertNotNull(entriesJSON);
            assertTrue(entriesJSON.size() == 1);
            
            paramMap = new HashMap<String, String>();
            try {
                processesClient.getActivities("fakeid", paramMap);
                fail("Exception expected");
            } catch(PublicApiException expected) {
                assertEquals(HttpStatus.NOT_FOUND.value(), expected.getHttpResponse().getStatusCode());
                assertErrorSummary("The entity with id: fakeid was not found", expected.getHttpResponse());
            }
            
            // get activities with admin from the same tenant as the process initiator
            publicApiClient.setRequestContext(adminContext);
            paramMap = new HashMap<String, String>();
            activitiesJSON = processesClient.getActivities(process1.getId(), paramMap);
            assertNotNull(activitiesJSON);
            entriesJSON = (JSONArray) activitiesJSON.get("entries");
            assertNotNull(entriesJSON);
            assertTrue(entriesJSON.size() == 2);
            
            // get tasks with admin from another tenant as the process initiator
            publicApiClient.setRequestContext(otherContext);
            paramMap = new HashMap<String, String>();
            try
            {
                processesClient.getActivities(process1.getId(), paramMap);
                fail("forbidden expected");
            }
            catch (PublicApiException e)
            {
                assertEquals(HttpStatus.FORBIDDEN.value(), e.getHttpResponse().getStatusCode());
            }
        }
        finally
        {
            cleanupProcessInstance(process1.getId());
        }
    }
    
    @Test
    public void getProcessImage() throws Exception
    {
        final RequestContext requestContext = initApiClientWithTestUser();
        final ProcessInfo processRest = startAdhocProcess(requestContext, null);
        HttpResponse response = publicApiClient.processesClient().getImage(processRest.getId());
        assertEquals(200, response.getStatusCode());
        cleanupProcessInstance(processRest.getId());
        try
        {
            response = publicApiClient.processesClient().getImage("fakeId");
            fail("Exception expected");
        }
        catch (PublicApiException e)
        {
            assertEquals(404, e.getHttpResponse().getStatusCode());
        }
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
                assertEquals(docNodeRefs[0].getId(), entryJSON.get("id"));
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
                assertEquals(docNodeRefs[1].getId(), entryJSON.get("id"));
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
        
        try
        {
            processesClient.findProcessItems("fakeid");
            fail("Exception expected");
        }
        catch (PublicApiException e)
        {
            assertEquals(404, e.getHttpResponse().getStatusCode());
        }
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
        JSONObject itemJSON = processesClient.findProcessItem(newProcessInstanceId, docNodeRefs[0].getId());
        assertNotNull(itemJSON);
        
        assertEquals(docNodeRefs[0].getId(), itemJSON.get("id"));
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
    @SuppressWarnings("unchecked")
    public void testAddProcessItem() throws Exception
    {
        final RequestContext requestContext = initApiClientWithTestUser();
        
        NodeRef[] docNodeRefs = createTestDocuments(requestContext);
        final ProcessInfo processRest = startAdhocProcess(requestContext, null);
        try
        {
            assertNotNull(processRest);
            
            final String newProcessInstanceId = processRest.getId();
            ProcessesClient processesClient = publicApiClient.processesClient();
            
            JSONObject createItemObject = new JSONObject();
            createItemObject.put("id", docNodeRefs[0].getId());
            
            // Add the item
            processesClient.addProcessItem(newProcessInstanceId, createItemObject.toJSONString());
            
            // Fetching the item
            JSONObject itemJSON = publicApiClient.processesClient().findProcessItem(newProcessInstanceId, docNodeRefs[0].getId());
            assertEquals(docNodeRefs[0].getId(), itemJSON.get("id"));
            assertEquals("Test Doc1", itemJSON.get("name"));
            assertEquals("Test Doc1 Title", itemJSON.get("title"));
            assertEquals("Test Doc1 Description", itemJSON.get("description"));
            assertNotNull(itemJSON.get("createdAt"));
            assertEquals(requestContext.getRunAsUser(), itemJSON.get("createdBy"));
            assertNotNull(itemJSON.get("modifiedAt"));
            assertEquals(requestContext.getRunAsUser(), itemJSON.get("modifiedBy"));
            assertNotNull(itemJSON.get("size"));
            assertNotNull(itemJSON.get("mimeType"));
            
            // add non existing item
            createItemObject = new JSONObject();
            createItemObject.put("id", "blablabla");
            
            // Add the item
            try
            {
                processesClient.addProcessItem(newProcessInstanceId, createItemObject.toJSONString());
                fail("not found expected");
            }
            catch (PublicApiException e)
            {
                assertEquals(HttpStatus.NOT_FOUND.value(), e.getHttpResponse().getStatusCode());
            }
        }
        finally
        {
            cleanupProcessInstance(processRest.getId());
        }
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
            processesClient.deleteProcessItem(newProcessInstanceId, docNodeRefs[0].getId());
            
            // Fetching the item should result in 404
            try 
            {
                publicApiClient.processesClient().findProcessItem(newProcessInstanceId, docNodeRefs[0].getId());
                fail("Exception expected");
            } 
            catch(PublicApiException expected) 
            {
                assertEquals(HttpStatus.NOT_FOUND.value(), expected.getHttpResponse().getStatusCode());
                assertErrorSummary("The entity with id: " + docNodeRefs[0].getId() + " was not found", expected.getHttpResponse());
            }
            
            // Deleting the item again should give an error
            try
            {
                processesClient.deleteProcessItem(newProcessInstanceId, docNodeRefs[0].getId());
                fail("Expected not found");
            }
            catch (PublicApiException e)
            {
                assertEquals(HttpStatus.NOT_FOUND.value(), e.getHttpResponse().getStatusCode());
            }
            
        }
        finally
        {
            cleanupProcessInstance(processRest.getId());
        }
    }
    
    @Test
    public void testDeleteProcessItemWithAdmin() throws Exception
    {
        final RequestContext requestContext = initApiClientWithTestUser();
        
        String tenantAdmin = AuthenticationUtil.getAdminUserName() + "@" + requestContext.getNetworkId();
        final RequestContext adminContext = new RequestContext(requestContext.getNetworkId(), tenantAdmin);
        publicApiClient.setRequestContext(adminContext);
        
        // start process with admin user
        NodeRef[] docNodeRefs = createTestDocuments(adminContext);
        final ProcessInfo processRest = startAdhocProcess(adminContext, docNodeRefs);
        try
        {
            assertNotNull(processRest);
            
            final String newProcessInstanceId = processRest.getId();
            ProcessesClient processesClient = publicApiClient.processesClient();
            
            // Delete the item
            processesClient.deleteProcessItem(newProcessInstanceId, docNodeRefs[0].getId());
            
            // Fetching the item should result in 404
            try 
            {
                publicApiClient.processesClient().findProcessItem(newProcessInstanceId, docNodeRefs[0].getId());
                fail("Exception expected");
            } 
            catch(PublicApiException expected) 
            {
                assertEquals(HttpStatus.NOT_FOUND.value(), expected.getHttpResponse().getStatusCode());
                assertErrorSummary("The entity with id: " + docNodeRefs[0].getId() + " was not found", expected.getHttpResponse());
            }
            
        }
        finally
        {
            cleanupProcessInstance(processRest.getId());
        }
        
        // start process with default user and delete item with admin
        publicApiClient.setRequestContext(requestContext);
        docNodeRefs = createTestDocuments(requestContext);
        final ProcessInfo processRestDefaultUser = startAdhocProcess(requestContext, docNodeRefs);
        try
        {
            assertNotNull(processRestDefaultUser);
            
            publicApiClient.setRequestContext(adminContext);
            final String newProcessInstanceId = processRestDefaultUser.getId();
            ProcessesClient processesClient = publicApiClient.processesClient();
            
            // Delete the item
            processesClient.deleteProcessItem(newProcessInstanceId, docNodeRefs[0].getId());
            
            // Fetching the item should result in 404
            try 
            {
                publicApiClient.processesClient().findProcessItem(newProcessInstanceId, docNodeRefs[0].getId());
                fail("Exception expected");
            } 
            catch(PublicApiException expected) 
            {
                assertEquals(HttpStatus.NOT_FOUND.value(), expected.getHttpResponse().getStatusCode());
                assertErrorSummary("The entity with id: " + docNodeRefs[0].getId() + " was not found", expected.getHttpResponse());
            }
            
        }
        finally
        {
            cleanupProcessInstance(processRestDefaultUser.getId());
        }
    }
    
    @Test
    public void testGetProcessVariables() throws Exception
    {
        RequestContext requestContext = initApiClientWithTestUser();
        
        String tenantAdmin = AuthenticationUtil.getAdminUserName() + "@" + requestContext.getNetworkId();
        RequestContext adminContext = new RequestContext(requestContext.getNetworkId(), tenantAdmin);
        
        TestNetwork anotherNetwork = getOtherNetwork(requestContext.getNetworkId());
        tenantAdmin = AuthenticationUtil.getAdminUserName() + "@" + anotherNetwork.getId();
        final RequestContext otherContext = new RequestContext(anotherNetwork.getId(), tenantAdmin);
        
        ProcessInfo processRest = startAdhocProcess(requestContext, null);
        
        try
        {
            assertNotNull(processRest);
            String processInstanceId = processRest.getId();
            
            JSONObject processvariables = publicApiClient.processesClient().getProcessvariables(processInstanceId);
            assertNotNull(processvariables);
            validateVariablesResponse(processvariables, requestContext.getRunAsUser());
            
            // get variables with admin from same network
            publicApiClient.setRequestContext(adminContext);
            processvariables = publicApiClient.processesClient().getProcessvariables(processInstanceId);
            assertNotNull(processvariables);
            validateVariablesResponse(processvariables, requestContext.getRunAsUser());
            
            // get variables with admin from other network
            publicApiClient.setRequestContext(otherContext);
            try
            {
                processvariables = publicApiClient.processesClient().getProcessvariables(processInstanceId);
                fail("forbidden expected");
            }
            catch (PublicApiException e)
            {
                assertEquals(HttpStatus.FORBIDDEN.value(), e.getHttpResponse().getStatusCode());
            }
            
            // get variables with non existing process id
            try
            {
                processvariables = publicApiClient.processesClient().getProcessvariables("fakeid");
                fail("not found expected");
            }
            catch (PublicApiException e)
            {
                assertEquals(HttpStatus.NOT_FOUND.value(), e.getHttpResponse().getStatusCode());
            }
        }
        finally
        {
            cleanupProcessInstance(processRest.getId());
        }
    }
    
    protected void validateVariablesResponse(JSONObject processvariables, String user) 
    {
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
        assertEquals(user, variable.get("value"));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testCreateVariablesPresentInModel() throws Exception
    {
        RequestContext requestContext = initApiClientWithTestUser();

        ProcessInfo processInstance = startAdhocProcess(requestContext, null);
        try
        {
            JSONArray variablesArray = new JSONArray();
            JSONObject variableBody = new JSONObject();
            variableBody.put("name", "bpm_percentComplete");
            variableBody.put("value", 20);
            variableBody.put("type", "d:int");
            variablesArray.add(variableBody);
            variableBody = new JSONObject();
            variableBody.put("name", "bpm_workflowPriority");
            variableBody.put("value", 50);
            variableBody.put("type", "d:int");
            variablesArray.add(variableBody);
            
            JSONObject result = publicApiClient.processesClient().createVariables(processInstance.getId(), variablesArray);
            assertNotNull(result);
            JSONObject resultObject = (JSONObject) result.get("list");
            JSONArray resultList = (JSONArray) resultObject.get("entries");
            assertEquals(2, resultList.size());
            JSONObject firstResultObject = (JSONObject) ((JSONObject) resultList.get(0)).get("entry");
            assertEquals("bpm_percentComplete", firstResultObject.get("name"));
            assertEquals(20L, firstResultObject.get("value"));
            assertEquals("d:int", firstResultObject.get("type"));
            assertEquals(20, activitiProcessEngine.getRuntimeService().getVariable(processInstance.getId(), "bpm_percentComplete"));
            
            JSONObject secondResultObject = (JSONObject) ((JSONObject) resultList.get(1)).get("entry");
            assertEquals("bpm_workflowPriority", secondResultObject.get("name"));
            assertEquals(50L, secondResultObject.get("value"));
            assertEquals("d:int", secondResultObject.get("type"));
            assertEquals(50, activitiProcessEngine.getRuntimeService().getVariable(processInstance.getId(), "bpm_workflowPriority"));
        }
        finally
        {
            cleanupProcessInstance(processInstance.getId());
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateProcessVariables() throws Exception
    {
        RequestContext requestContext = initApiClientWithTestUser();
        
        String tenantAdmin = AuthenticationUtil.getAdminUserName() + "@" + requestContext.getNetworkId();
        RequestContext adminContext = new RequestContext(requestContext.getNetworkId(), tenantAdmin);
        
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
            
            JSONObject processvariables = publicApiClient.processesClient().getProcessvariables(processId);
            assertNotNull(processvariables);
            JSONObject newVariableEntry = null;
            JSONArray entries = (JSONArray) processvariables.get("entries");
            assertNotNull(entries);
            for(int i=0; i<entries.size(); i++) 
            {
                JSONObject entry = (JSONObject) entries.get(i);
                assertNotNull(entry);
                entry = (JSONObject) entry.get("entry");
                assertNotNull(entry);
                if ("newVariable".equals((String) entry.get("name"))) {
                    newVariableEntry = entry;
                }
            }
            
            assertNotNull(newVariableEntry);
            assertEquals(4567L, newVariableEntry.get("value"));
            
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
            
            // Update an unexisting variable with wrong variable data
            variableJson = new JSONObject();
            variableJson.put("name", "newLongVariable");
            variableJson.put("value", "text");
            variableJson.put("type", "d:long");
            
            try
            {
                publicApiClient.processesClient().updateVariable(processId, "newLongVariable", variableJson);
                fail("Expected server error exception");
            }
            catch (PublicApiException e)
            {
                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getHttpResponse().getStatusCode());
            }
            
            // Update an unexisting variable with no variable data
            variableJson = new JSONObject();
            variableJson.put("name", "newNoValueVariable");
            variableJson.put("type", "d:datetime");
            
            resultEntry = publicApiClient.processesClient().updateVariable(processId, "newNoValueVariable", variableJson);
            assertNotNull(resultEntry);
            result = (JSONObject) resultEntry.get("entry");
            
            assertEquals("newNoValueVariable", result.get("name"));
            assertNotNull(result.get("value"));
            assertEquals("d:datetime", result.get("type"));
            assertNotNull(activitiProcessEngine.getRuntimeService().getVariable(processId, "newNoValueVariable"));
            
            // Test update variable with admin user
            publicApiClient.setRequestContext(adminContext);
            variableJson = new JSONObject();
            variableJson.put("name", "newVariable");
            variableJson.put("value", 1234L);
            variableJson.put("type", "d:long");
            
            resultEntry = publicApiClient.processesClient().updateVariable(processId, "newVariable", variableJson);
            assertNotNull(resultEntry);
            result = (JSONObject) resultEntry.get("entry");
            
            assertEquals("newVariable", result.get("name"));
            assertEquals(1234L, result.get("value"));
            assertEquals("d:long", result.get("type"));
            assertEquals(1234L, activitiProcessEngine.getRuntimeService().getVariable(processId, "newVariable"));
        }
        finally
        {
            cleanupProcessInstance(processRest.getId());
        }
        
        // test update variable with admin user that also started the process instance
        
        processRest = startAdhocProcess(adminContext, null);
        
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
            
            // test update variable with user not involved in the process instance
            publicApiClient.setRequestContext(requestContext);
            try
            {
                publicApiClient.processesClient().updateVariable(processId, "newVariable", variableJson);
                fail("Expected forbidden exception");
            }
            catch (PublicApiException e)
            {
                assertEquals(HttpStatus.FORBIDDEN.value(), e.getHttpResponse().getStatusCode());
            }
        }
        finally
        {
            cleanupProcessInstance(processRest.getId());
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateProcessVariableWithWrongType() throws Exception
    {
        final RequestContext requestContext = initApiClientWithTestUser();
        
        ProcessInfo processRest = startParallelReviewProcess(requestContext);
        
        try
        {
            assertNotNull(processRest);
            String processId = processRest.getId();
            
            // Update an existing variable with wrong type
            JSONObject variableJson = new JSONObject();
            variableJson.put("name", "wf_requiredApprovePercent");
            variableJson.put("value", 55.99);
            variableJson.put("type", "d:double");
            
            try 
            {
                publicApiClient.processesClient().updateVariable(processId, "wf_requiredApprovePercent", variableJson);
                fail("Exception expected");
            }
            catch (PublicApiException e)
            {
                assertEquals(HttpStatus.BAD_REQUEST.value(), e.getHttpResponse().getStatusCode());
            }
            
            variableJson = new JSONObject();
            variableJson.put("name", "wf_requiredApprovePercent");
            variableJson.put("value", 55.99);
            variableJson.put("type", "d:int");
            
            JSONObject resultEntry = publicApiClient.processesClient().updateVariable(processId, "wf_requiredApprovePercent", variableJson);
            assertNotNull(resultEntry);
            JSONObject result = (JSONObject) resultEntry.get("entry");
            
            assertEquals("wf_requiredApprovePercent", result.get("name"));
            assertEquals(55l, result.get("value"));
            assertEquals("d:int", result.get("type"));
            assertEquals(55, activitiProcessEngine.getRuntimeService().getVariable(processId, "wf_requiredApprovePercent"));
            
            JSONObject processvariables = publicApiClient.processesClient().getProcessvariables(processId);
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
            
            JSONObject approvePercentObject = variablesByName.get("wf_requiredApprovePercent");
            assertNotNull(approvePercentObject);
            assertEquals(55l, approvePercentObject.get("value"));
            assertEquals("d:int", approvePercentObject.get("type"));
            
            // set a new variable
            variableJson = new JSONObject();
            variableJson.put("name", "testVariable");
            variableJson.put("value", "text");
            variableJson.put("type", "d:text");
            
            resultEntry = publicApiClient.processesClient().updateVariable(processId, "testVariable", variableJson);
            assertNotNull(resultEntry);
            result = (JSONObject) resultEntry.get("entry");
            
            assertEquals("testVariable", result.get("name"));
            assertEquals("text", result.get("value"));
            assertEquals("d:text", result.get("type"));
            assertEquals("text", activitiProcessEngine.getRuntimeService().getVariable(processId, "testVariable"));
            
            // change the variable value and type (should be working because no content model type)
            variableJson = new JSONObject();
            variableJson.put("name", "testVariable");
            variableJson.put("value", 123);
            variableJson.put("type", "d:int");
            
            resultEntry = publicApiClient.processesClient().updateVariable(processId, "testVariable", variableJson);
            assertNotNull(resultEntry);
            result = (JSONObject) resultEntry.get("entry");
            
            assertEquals("testVariable", result.get("name"));
            assertEquals(123l, result.get("value"));
            assertEquals("d:int", result.get("type"));
            assertEquals(123, activitiProcessEngine.getRuntimeService().getVariable(processId, "testVariable"));
            
            // change the variable value for a list of noderefs (bpm_assignees)
            final JSONObject updateAssigneesJson = new JSONObject();
            updateAssigneesJson.put("name", "bpm_assignees");
            updateAssigneesJson.put("type", "d:noderef");
            
            TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    JSONArray assigneeArray = new JSONArray();
                    assigneeArray.add(requestContext.getRunAsUser());
                    updateAssigneesJson.put("value", assigneeArray);
                    return null;
                }
            }, requestContext.getRunAsUser(), requestContext.getNetworkId());
            
            resultEntry = publicApiClient.processesClient().updateVariable(processId, "bpm_assignees", updateAssigneesJson);
            assertNotNull(resultEntry);
            final JSONObject updateAssigneeResult = (JSONObject) resultEntry.get("entry");
            
            assertEquals("bpm_assignees", updateAssigneeResult.get("name"));
            TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    JSONArray assigneeArray = (JSONArray) updateAssigneeResult.get("value");
                    assertNotNull(assigneeArray);
                    assertEquals(1, assigneeArray.size());
                    return null;
                }
            }, requestContext.getRunAsUser(), requestContext.getNetworkId());
            
            assertEquals("d:noderef", updateAssigneeResult.get("type"));
            
            // update the bpm_assignees with a single entry, should result in an error
            final JSONObject updateAssigneeJson = new JSONObject();
            updateAssigneeJson.put("name", "bpm_assignees");
            updateAssigneeJson.put("type", "d:noderef");
            
            TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    updateAssigneeJson.put("value", requestContext.getRunAsUser());
                    return null;
                }
            }, requestContext.getRunAsUser(), requestContext.getNetworkId());
            
            try 
            {
                publicApiClient.processesClient().updateVariable(processId, "bpm_assignees", updateAssigneeJson);
                fail("Exception expected");
            }
            catch (PublicApiException e)
            {
                assertEquals(HttpStatus.BAD_REQUEST.value(), e.getHttpResponse().getStatusCode());
            }
            
            // change the variable value with a non-existing person
            variableJson = new JSONObject();
            variableJson.put("name", "bpm_assignees");
            JSONArray assigneeArray = new JSONArray();
            assigneeArray.add("nonExistingPerson");
            variableJson.put("value", assigneeArray);
            variableJson.put("type", "d:noderef");
            
            try 
            {
                publicApiClient.processesClient().updateVariable(processId, "bpm_assignees", variableJson);
                fail("Exception expected");
            }
            catch (PublicApiException e)
            {
                assertEquals(HttpStatus.BAD_REQUEST.value(), e.getHttpResponse().getStatusCode());
            }
            
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
        
        try 
        {
            publicApiClient.processesClient().deleteVariable("unexisting", "deleteMe");
            fail("Exception expected");
        } 
        catch(PublicApiException expected) 
        {
            assertEquals(HttpStatus.NOT_FOUND.value(), expected.getHttpResponse().getStatusCode());
            assertErrorSummary("The entity with id: unexisting was not found", expected.getHttpResponse());
        }
    }
    
    protected void completeAdhocTasks(String instanceId, RequestContext requestContext) 
    {
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
                catch(Exception e) 
                {
                    log("Error while cleaning up process instance", e);
                }
                activitiProcessEngine.getHistoryService().deleteHistoricProcessInstance(processInstanceId);
            }
        }
        catch (Throwable t)
        {
            // Ignore error during cleanup
            log("Error while cleaning up process instance", t);
        }
    }
}
