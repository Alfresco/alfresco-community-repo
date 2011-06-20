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

package org.alfresco.repo.workflow.activiti;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowNode;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTimer;
import org.alfresco.service.namespace.QName;
import org.junit.Test;

/**
 * Spring-configured JUnit 4 test case.
 * Uses Spring to load up a test context and runs each test case in a transaction which gets rolled back.
 * Loads up the activiti-context.xml and test-database-context.xml.
 * @since 3.4.e
 * @author Nick Smith
 *
 */
public class ActivitiWorkflowComponentTest extends AbstractActivitiComponentTest
{
    @Test
    public void testDeployDefinition() throws Exception
    {
        ProcessDefinition defInDB = repo.createProcessDefinitionQuery()
            .processDefinitionKey(TEST_TASK_KEY)
            .singleResult();
        assertNull("The definition is already deployed!", defInDB);
        
        WorkflowDefinition definition = deployTestTaskDefinition();
        String localDefId = BPMEngineRegistry.getLocalId(definition.getId());
        ProcessDefinition processDef = repo.createProcessDefinitionQuery()
            .processDefinitionId(localDefId)
            .singleResult();
        assertNotNull("Process Definition should have been deployed!", processDef);
 
        ProcessDefinition def2InDB = repo.createProcessDefinitionQuery()
        .processDefinitionKey(TEST_ADHOC_KEY)
        .singleResult();
        assertNull("The definition is already deployed!", def2InDB);
        
        WorkflowDefinition definition2 = deployTestTaskDefinition();
        String localDef2Id = BPMEngineRegistry.getLocalId(definition2.getId());
        ProcessDefinition processDef2 = repo.createProcessDefinitionQuery()
        .processDefinitionId(localDef2Id)
        .singleResult();
        assertNotNull("Process Definition should have been deployed!", processDef2);
    }

    @Test
    public void testIsDefinitionDeployed() throws Exception
    {
        InputStream input = getInputStream(TEST_TASK_DEF);
        
        boolean result = workflowEngine.isDefinitionDeployed(input, XML);
        assertFalse("Should return false before process def deployed.", result);
        
        deployTestTaskDefinition();
        
        input = getInputStream(TEST_TASK_DEF);
        result = workflowEngine.isDefinitionDeployed(input, XML);
        assertTrue("Should return true after process def deployed.", result);
        
        // Check doesn't find Adhoc definition.
        input = getInputStream(TEST_ADHOC_DEF);
        result = workflowEngine.isDefinitionDeployed(input, XML);
        assertFalse("Should not find Adhoc definition.", result);
    }

    @Test
    public void testUndeployDefinition() throws Exception
    {
        WorkflowDefinition definition = deployTestTaskDefinition();
        String localId = BPMEngineRegistry.getLocalId(definition.getId());
        
        long defCount = repo.createProcessDefinitionQuery()
            .processDefinitionId(localId)
            .count();
        assertEquals("The deployed process definition should exist!", 1, defCount);

        workflowEngine.undeployDefinition(definition.getId());
        
        defCount = repo.createProcessDefinitionQuery()
            .processDefinitionId(localId)
            .count();
        assertEquals("The undeployed process definition should not exist!", 0, defCount);
        
    }
    
    @Test
    public void testGetDefinitionById() throws Exception
    {
        WorkflowDefinition definition = deployTestTaskDefinition();
        WorkflowDefinition result = workflowEngine.getDefinitionById(definition.getId());

        assertNotNull("The workflow definition was not found!", result);
        
        assertEquals(definition.getId(), result.getId());
        assertEquals(definition.getDescription(), result.getDescription());
        assertEquals(definition.getName(), result.getName());
        assertEquals(definition.getTitle(), result.getTitle());
        assertEquals(definition.getVersion(), result.getVersion());

        WorkflowTaskDefinition resultStartDef = result.getStartTaskDefinition();
        assertNotNull("Start task is null!", resultStartDef);

        WorkflowTaskDefinition originalStartDef = definition.getStartTaskDefinition();
        assertEquals("Start task Id does not match!", originalStartDef.getId(), resultStartDef.getId());
        
        WorkflowNode resultNode = resultStartDef.getNode();
        assertNotNull("Start Task Node is null!", resultNode);
        assertEquals("Start Task Node Name does not match!", originalStartDef.getNode().getName(), resultNode.getName());
        
        TypeDefinition metaData = resultStartDef.getMetadata();
        assertNotNull("Start Task Metadata is null!", metaData);
        assertEquals("Start Task Metadata name does not match!", originalStartDef.getMetadata().getName(), metaData.getName());
        
        workflowEngine.undeployDefinition(definition.getId());
        WorkflowDefinition nullResult = workflowEngine.getDefinitionById(definition.getId());
        assertNull("The workflow definition was found but should be null!", nullResult);
    }

    @Test
    public void testGetDefinitionByName() throws Exception
    {
        WorkflowDefinition definition = deployTestTaskDefinition();
        WorkflowDefinition result = workflowEngine.getDefinitionByName(definition.getName());
        
        assertNotNull("The workflow definition was not found!", result);
        
        assertEquals(definition.getId(), result.getId());
        assertEquals(definition.getDescription(), result.getDescription());
        assertEquals(definition.getName(), result.getName());
        assertEquals(definition.getTitle(), result.getTitle());
        assertEquals(definition.getVersion(), result.getVersion());
        
        workflowEngine.undeployDefinition(definition.getId());
        WorkflowDefinition nullResult = workflowEngine.getDefinitionByName(definition.getName());
        assertNull("The workflow definition was found but should be null!", nullResult);
    }
    
    @Test
    public void testGetDefinitions() throws Exception
    {
        List<WorkflowDefinition> startDefs = workflowEngine.getDefinitions();
        
        WorkflowDefinition defV1 = deployTestTaskDefinition();
        List<WorkflowDefinition> definitions = workflowEngine.getDefinitions();
        checkDefinitions(definitions, startDefs, defV1);
        
        // Deploy version 2 of testTask def.
        WorkflowDefinition defV2 = deployTestTaskDefinition();
        // Check new version replaces old version.
        definitions = workflowEngine.getDefinitions();
        checkDefinitions(definitions, startDefs, defV2);
        
        // Deploy new type of definition.
        WorkflowDefinition adhocDef = deployTestAdhocDefinition();
        // Check that definitions of a different type are picked up.
        definitions = workflowEngine.getDefinitions();
        checkDefinitions(definitions, startDefs, defV2, adhocDef);
    }
    
    
    @Test
    public void testGetAllDefinitions() throws Exception
    {
        List<WorkflowDefinition> startDefs = workflowEngine.getAllDefinitions();
        
        WorkflowDefinition defV1 = deployTestTaskDefinition();
        List<WorkflowDefinition> definitions = workflowEngine.getAllDefinitions();
        checkDefinitions(definitions, startDefs, defV1);
        
        // Deploy version 2 of testTask def.
        WorkflowDefinition defV2 = deployTestTaskDefinition();
        // Check new version replaces old version.
        definitions = workflowEngine.getAllDefinitions();
        checkDefinitions(definitions, startDefs, defV1, defV2);
        
        // Deploy new type of definition.
        WorkflowDefinition adhocDef = deployTestAdhocDefinition();
        // Check that definitions of a different type are picked up.
        definitions = workflowEngine.getAllDefinitions();
        checkDefinitions(definitions, startDefs, defV1, defV2, adhocDef);
    }
    
    @Test
    public void testStartWorkflow() throws Exception
    {
        WorkflowDefinition def = deployTestTaskDefinition();
        
        // Fill a map of default properties to start the workflow with
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        Date dueDate = Calendar.getInstance().getTime();
        properties.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, "description123");
        properties.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
        properties.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 2);
      //  properties.put(WorkflowModel.ASSOC_PACKAGE, null);
      //  properties.put(WorkflowModel.PROP_CONTEXT, null);
        
        
        // Call the start method
        WorkflowPath path = workflowEngine.startWorkflow(def.getId(), properties);
        assertNotNull("The workflow path is null!", path);
        String executionId = BPMEngineRegistry.getLocalId(path.getId());
        Execution execution = runtime.createExecutionQuery()
            .executionId(executionId)
            .singleResult();
        assertNotNull("No execution was created int he DB!", execution);

        WorkflowInstance instance = path.getInstance();
        assertNotNull("The workflow instance is null!",instance);
        String procInstanceId = BPMEngineRegistry.getLocalId(instance.getId());
        ProcessInstance procInstance = runtime.createProcessInstanceQuery()
            .processInstanceId(procInstanceId)
            .singleResult();
        assertNotNull("No process instance was created!", procInstance);
        
        WorkflowNode node = path.getNode();
        assertNotNull("The workflow node is null!", node);
        String nodeName = node.getName();

        assertEquals("task", nodeName);
        
        // Check if company home is added as variable and can be fetched
        ScriptNode companyHome = (ScriptNode) runtime.getVariable(procInstanceId, "companyhome");
        assertNotNull(companyHome);
        assertEquals("companyHome", companyHome.getNodeRef().getStoreRef().getIdentifier());
        
        // Check if the initiator is added as variable
        ScriptNode initiator = (ScriptNode) runtime.getVariable(procInstanceId, "initiator");
        assertNotNull(initiator);
        assertEquals("admin", initiator.getNodeRef().getStoreRef().getIdentifier());
        
        // Check if the initiator home is also set as variable
        ScriptNode initiatorHome = (ScriptNode) runtime.getVariable(procInstanceId, "initiatorhome");
        assertNotNull(initiatorHome);
        assertEquals("admin-home", initiatorHome.getNodeRef().getStoreRef().getIdentifier());
        
        // Check if start-date is set and no end-date is set
        assertNotNull(path.getInstance().getStartDate());
        assertNull(path.getInstance().getEndDate());
        
        // Also check if the task that is created, has all default properties initialised
        Task task = taskService.createTaskQuery().processInstanceId(procInstanceId).singleResult();
        assertNotNull("Task should have been created", task);
        assertEquals("task", task.getTaskDefinitionKey());
        String defaultSetVariable = (String) taskService.getVariableLocal(task.getId(), "test_myProp");
        assertEquals("Default value", defaultSetVariable);
        
        // Also check default value of task description is taken from WF-porps
        assertEquals("description123", task.getDescription());
    }
    
    @Test
    public void testSignal() throws Exception
    {
        WorkflowDefinition def = deployTestSignallingDefinition();
        ProcessInstance processInstance = runtime.startProcessInstanceById(BPMEngineRegistry.getLocalId(def.getId()));
        
        String procId = processInstance.getId();
        List<String> nodeIds = runtime.getActiveActivityIds(procId);
        assertEquals(1, nodeIds.size());
        assertEquals("task1", nodeIds.get(0));
        
        String pathId = BPMEngineRegistry.createGlobalId(ActivitiConstants.ENGINE_ID, procId);
        WorkflowPath path = workflowEngine.signal(pathId, null);
        assertEquals(pathId, path.getId());
        assertEquals("task2", path.getNode().getName());
        assertEquals(pathId, path.getInstance().getId());
        assertTrue(path.isActive());
        
        nodeIds = runtime.getActiveActivityIds(procId);
        assertEquals(1, nodeIds.size());
        assertEquals("task2", nodeIds.get(0));

        // Should end the WorkflowInstance
        path = workflowEngine.signal(pathId, null);
        assertEquals(pathId, path.getId());
        assertNull(path.getNode());
        assertEquals(pathId, path.getInstance().getId());
        assertFalse(path.isActive());
    }
    
    @Test
    public void testCancelWorkflow() throws Exception
    {
        WorkflowDefinition def = deployTestAdhocDefinition();
        
        ProcessInstance processInstance = runtime.startProcessInstanceById(BPMEngineRegistry.getLocalId(def.getId()));
        
        // Validate if a workflow exists
        List<WorkflowInstance> instances = workflowEngine.getActiveWorkflows(def.getId());
        assertNotNull(instances);
        assertEquals(1, instances.size());
        assertEquals(processInstance.getId(), BPMEngineRegistry.getLocalId(instances.get(0).getId()));
        
        // Call cancel method on component
        WorkflowInstance cancelledWorkflow = workflowEngine.cancelWorkflow(instances.get(0).getId());
        assertFalse(cancelledWorkflow.isActive());
        
        instances = workflowEngine.getActiveWorkflows(def.getId());
        assertNotNull(instances);
        assertEquals(0, instances.size());
        
        // Histrotic process instance shouldn't be present 
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
            .processInstanceId(processInstance.getProcessInstanceId())
            .singleResult();
        
        assertNull(historicProcessInstance);
    }
    
    @Test
    public void testCancelUnexistingWorkflow() throws Exception
    {
        try 
        {
            String globalId = workflowEngine.createGlobalId("unexistingWorkflowId");
            workflowEngine.cancelWorkflow(globalId);
            fail("Exception expected");
        }
        catch(WorkflowException e) 
        {
            // Inore this
        }
    }
    
    @Test
    public void testDeleteWorkflow() throws Exception
    {
        WorkflowDefinition def = deployTestAdhocDefinition();
        
        ProcessInstance processInstance = runtime.startProcessInstanceById(BPMEngineRegistry.getLocalId(def.getId()));
        
        // Validate if a workflow exists
        List<WorkflowInstance> instances = workflowEngine.getActiveWorkflows(def.getId());
        assertNotNull(instances);
        assertEquals(1, instances.size());
        assertEquals(processInstance.getId(), BPMEngineRegistry.getLocalId(instances.get(0).getId()));
        
        // Call delete method on component
        workflowEngine.deleteWorkflow(instances.get(0).getId());
        
        instances = workflowEngine.getActiveWorkflows(def.getId());
        assertNotNull(instances);
        assertEquals(0, instances.size());
        
        // Historic process instance shouldn't be present
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
            .processInstanceId(processInstance.getProcessInstanceId())
            .singleResult();
        
        assertNull(historicProcessInstance);
    }
    
    @Test
    public void testDeleteUnexistingWorkflow() throws Exception
    {
        try 
        {
            String globalId = workflowEngine.createGlobalId("unexistingWorkflowId");
            workflowEngine.deleteWorkflow(globalId);
            fail("Exception expected");
        }
        catch(WorkflowException e) 
        {
            // Inore this
        }
    }
    
    @Test
    public void testGetActiveWorkflows() throws Exception 
    {
        WorkflowDefinition def = deployTestAdhocDefinition();
        String activitiProcessDefinitionId = BPMEngineRegistry.getLocalId(def.getId());
        
        ProcessInstance activeInstance = runtime.startProcessInstanceById(activitiProcessDefinitionId);
        ProcessInstance completedInstance = runtime.startProcessInstanceById(activitiProcessDefinitionId);
        ProcessInstance cancelledInstance = runtime.startProcessInstanceById(activitiProcessDefinitionId);
        ProcessInstance deletedInstance = runtime.startProcessInstanceById(activitiProcessDefinitionId);
        
        // Complete completedProcessInstance.
        String completedId = completedInstance.getId();
        boolean isActive = true;
        while (isActive)
        {
            Execution execution = runtime.createExecutionQuery()
                .processInstanceId(completedId)
                .singleResult();
            runtime.signal(execution.getId());
            ProcessInstance instance = runtime.createProcessInstanceQuery()
                .processInstanceId(completedId)
                .singleResult();
            isActive = instance != null;
        }
        
        // Deleted and canceled instances shouldn't be returned
        workflowEngine.cancelWorkflow(workflowEngine.createGlobalId(cancelledInstance.getId()));
        workflowEngine.deleteWorkflow(workflowEngine.createGlobalId(deletedInstance.getId()));
        
        // Validate if a workflow exists
        List<WorkflowInstance> instances = workflowEngine.getActiveWorkflows(def.getId());
        assertNotNull(instances);
        assertEquals(1, instances.size());
        String instanceId = instances.get(0).getId();
        assertEquals(activeInstance.getId(), BPMEngineRegistry.getLocalId(instanceId));
    }
    
    @Test
    public void testGetCompletedWorkflows() throws Exception 
    {
        WorkflowDefinition def = deployTestAdhocDefinition();
        String activitiProcessDefinitionId = BPMEngineRegistry.getLocalId(def.getId());
        
        runtime.startProcessInstanceById(activitiProcessDefinitionId);
        ProcessInstance completedInstance = runtime.startProcessInstanceById(activitiProcessDefinitionId);
        ProcessInstance cancelledInstance = runtime.startProcessInstanceById(activitiProcessDefinitionId);
        ProcessInstance deletedInstance = runtime.startProcessInstanceById(activitiProcessDefinitionId);
        
        // Complete completedProcessInstance.
        String completedId = completedInstance.getId();
        boolean isActive = true;
        while (isActive)
        {
            Execution execution = runtime.createExecutionQuery()
            .processInstanceId(completedId)
            .singleResult();
            runtime.signal(execution.getId());
            ProcessInstance instance = runtime.createProcessInstanceQuery()
            .processInstanceId(completedId)
            .singleResult();
            isActive = instance != null;
        }
        
        // Deleted and canceled instances shouldn't be returned
        workflowEngine.cancelWorkflow(workflowEngine.createGlobalId(cancelledInstance.getId()));
        workflowEngine.deleteWorkflow(workflowEngine.createGlobalId(deletedInstance.getId()));
        
        // Validate if a workflow exists
        List<WorkflowInstance> instances = workflowEngine.getCompletedWorkflows(def.getId());
        assertNotNull(instances);
        assertEquals(1, instances.size());
        String instanceId = instances.get(0).getId();
        assertEquals(completedId, BPMEngineRegistry.getLocalId(instanceId));
    }
    
    @Test
    public void testGetWorkflows() throws Exception 
    {
        WorkflowDefinition def = deployTestAdhocDefinition();
        String activitiProcessDefinitionId = BPMEngineRegistry.getLocalId(def.getId());
        
        ProcessInstance activeInstance = runtime.startProcessInstanceById(activitiProcessDefinitionId);
        ProcessInstance completedInstance = runtime.startProcessInstanceById(activitiProcessDefinitionId);
        ProcessInstance cancelledInstance = runtime.startProcessInstanceById(activitiProcessDefinitionId);
        ProcessInstance deletedInstance = runtime.startProcessInstanceById(activitiProcessDefinitionId);
        
        // Complete completedProcessInstance.
        String completedId = completedInstance.getId();
        boolean isActive = true;
        while (isActive)
        {
            Execution execution = runtime.createExecutionQuery()
            .processInstanceId(completedId)
            .singleResult();
            runtime.signal(execution.getId());
            ProcessInstance instance = runtime.createProcessInstanceQuery()
            .processInstanceId(completedId)
            .singleResult();
            isActive = instance != null;
        }
        
        // Deleted and canceled instances shouldn't be returned
        workflowEngine.cancelWorkflow(workflowEngine.createGlobalId(cancelledInstance.getId()));
        workflowEngine.deleteWorkflow(workflowEngine.createGlobalId(deletedInstance.getId()));
        
        // Validate if a workflow exists
        List<WorkflowInstance> instances = workflowEngine.getWorkflows(def.getId());
        assertNotNull(instances);
        assertEquals(2, instances.size());
        String instanceId = instances.get(0).getId();
        assertEquals(activeInstance.getId(), BPMEngineRegistry.getLocalId(instanceId));
        instanceId = instances.get(1).getId();
        assertEquals(completedId, BPMEngineRegistry.getLocalId(instanceId));
    }
    
    @Test
    public void testGetWorkflowById() throws Exception
    {
        WorkflowDefinition def = deployTestAdhocDefinition();
        
        Date startTime = new SimpleDateFormat("dd-MM-yyy hh:mm:ss").parse("01-01-2011 12:11:10");
        ClockUtil.setCurrentTime(startTime);
        
        // Add some variables which should be used in the WorkflowInstance
        Map<String, Object> variables = new HashMap<String, Object>();
        
        Date dueDate = Calendar.getInstance().getTime();
        putVariable(variables, WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
        putVariable(variables, WorkflowModel.PROP_WORKFLOW_DESCRIPTION, "I'm the description");
        putVariable(variables, WorkflowModel.PROP_CONTEXT, new ActivitiScriptNode(testWorkflowContext, serviceRegistry));
        putVariable(variables, WorkflowModel.ASSOC_PACKAGE, new ActivitiScriptNode(testWorkflowPackage, serviceRegistry));
        putVariable(variables, WorkflowModel.PROP_WORKFLOW_PRIORITY, 3);
        variables.put(WorkflowConstants.PROP_INITIATOR, new ActivitiScriptNode(adminHomeNode, serviceRegistry));        
        
        ProcessInstance processInstance = runtime.startProcessInstanceById(BPMEngineRegistry.getLocalId(def.getId()), variables);
        String globalProcessInstanceId = BPMEngineRegistry.createGlobalId(
            ActivitiConstants.ENGINE_ID, processInstance.getProcessInstanceId());
        
        WorkflowInstance workflowInstance = workflowEngine.getWorkflowById(globalProcessInstanceId);
        assertNotNull(workflowInstance);
        
        assertEquals(globalProcessInstanceId, workflowInstance.getId());
        assertNull(workflowInstance.getEndDate());
        assertTrue(workflowInstance.isActive());
        assertEquals("I'm the description", workflowInstance.getDescription());
        assertEquals(dueDate, workflowInstance.getDueDate());
        assertEquals(def.getId(), workflowInstance.getDefinition().getId());
        
        assertEquals(adminHomeNode, workflowInstance.getInitiator());
        assertEquals(testWorkflowContext, workflowInstance.getContext());
        assertEquals(testWorkflowPackage, workflowInstance.getWorkflowPackage());
        
        assertNotNull(workflowInstance.getPriority());
        assertEquals(3, workflowInstance.getPriority().intValue());
        
        assertEquals(startTime, workflowInstance.getStartDate());
        
        // Reset current time used in activiti
        ClockUtil.setCurrentTime(null);
    }
    
    @Test
    public void testGetCompletedWorkflowById() throws Exception
    {
        WorkflowDefinition def = deployTestAdhocDefinition();
        
        Date startTime = new SimpleDateFormat("dd-MM-yyy hh:mm:ss").parse("01-01-2011 01:02:03");
        ClockUtil.setCurrentTime(startTime);
        
        // Add some variables which should be used in the WorkflowInstance
        Map<String, Object> variables = new HashMap<String, Object>();
        
        Date dueDate = Calendar.getInstance().getTime();
        putVariable(variables, WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
        putVariable(variables, WorkflowModel.PROP_WORKFLOW_DESCRIPTION, "I'm the description");
        putVariable(variables, WorkflowModel.PROP_CONTEXT, new ActivitiScriptNode(testWorkflowContext, serviceRegistry));
        putVariable(variables, WorkflowModel.ASSOC_PACKAGE, new ActivitiScriptNode(testWorkflowPackage, serviceRegistry));
        putVariable(variables, WorkflowModel.PROP_WORKFLOW_PRIORITY, 3);
        variables.put(WorkflowConstants.PROP_INITIATOR, new ActivitiScriptNode(adminHomeNode, serviceRegistry));        
        
        ProcessInstance processInstance = runtime.startProcessInstanceById(BPMEngineRegistry.getLocalId(def.getId()), variables);
        String globalProcessInstanceId = BPMEngineRegistry.createGlobalId(
            ActivitiConstants.ENGINE_ID, processInstance.getProcessInstanceId());
        
        Date endTime = new SimpleDateFormat("dd-MM-yyy hh:mm:ss").parse("01-01-2011 02:03:04");
        ClockUtil.setCurrentTime(endTime);
        
        // Finish the task
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());
        
        WorkflowInstance workflowInstance = workflowEngine.getWorkflowById(globalProcessInstanceId);
        assertNotNull(workflowInstance);
        
        assertEquals(globalProcessInstanceId, workflowInstance.getId());
        assertEquals(endTime, workflowInstance.getEndDate());
        assertFalse(workflowInstance.isActive());
        assertEquals("I'm the description", workflowInstance.getDescription());
        assertEquals(dueDate, workflowInstance.getDueDate());
        assertEquals(def.getId(), workflowInstance.getDefinition().getId());
        
        assertEquals(adminHomeNode, workflowInstance.getInitiator());
        assertEquals(testWorkflowContext, workflowInstance.getContext());
        assertEquals(testWorkflowPackage, workflowInstance.getWorkflowPackage());
        
        assertNotNull(workflowInstance.getPriority());
        assertEquals(3, workflowInstance.getPriority().intValue());
        assertEquals(startTime, workflowInstance.getStartDate());
        
        // Reset current time used in activiti
        ClockUtil.setCurrentTime(null);
    }
    
    @Test
    public void testGetTimers() throws Exception
    {
        WorkflowDefinition def = deployTestJobDefinition();
        
        ProcessInstance processInstance = runtime.startProcessInstanceById(BPMEngineRegistry.getLocalId(def.getId()));
        
        // One timer should be active on workflow
        String workflowInstanceId = BPMEngineRegistry.createGlobalId(ActivitiConstants.ENGINE_ID, 
        		processInstance.getProcessInstanceId());
        
        // Query the timer in activity to have reference
        Job timerJob = managementService.createJobQuery().timers().processInstanceId(processInstance.getId()).singleResult();
        String globalJobId = BPMEngineRegistry.createGlobalId(ActivitiConstants.ENGINE_ID, timerJob.getId());
        
        // Ask workflowEngine for timers
        List<WorkflowTimer> timers = workflowEngine.getTimers(workflowInstanceId);
        assertNotNull(timers);
        assertEquals(1, timers.size());
        
        WorkflowTimer timer = timers.get(0);
        assertEquals(globalJobId, timer.getId());
        assertEquals(timerJob.getDuedate(), timer.getDueDate());
        
        // Check the path of the timer
        String expectedTimerPathId = BPMEngineRegistry.createGlobalId(ActivitiConstants.ENGINE_ID, timerJob.getExecutionId());
        assertNotNull(timer.getPath());
        assertEquals(expectedTimerPathId, timer.getPath().getId());
        
        // Check the workflow-instance associated with the path
        assertEquals(workflowInstanceId, timer.getPath().getInstance().getId());
        
        // Check the task returned by the timer
        Task waitingTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        
        assertNotNull(timer.getTask());
        assertEquals(BPMEngineRegistry.createGlobalId(ActivitiConstants.ENGINE_ID, waitingTask.getId()), timer.getTask().getId());
        
        // When task with boundry-timer on it is finished, no timers should be available
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());
        
        timers = workflowEngine.getTimers(workflowInstanceId);
        
        assertNotNull(timers);
        assertEquals(0, timers.size());
    }
    
    @Test
    public void testGetWorkflowImage() 
    {
        WorkflowDefinition definitionWithoutImage = deployTestAdhocDefinition();
        WorkflowDefinition definitionWithImage = deployTestDiagramDefinition();
        
        // Start process-instance that shouldn't have an image
        ProcessInstance processInstance = runtime.startProcessInstanceById(BPMEngineRegistry.getLocalId(definitionWithoutImage.getId()));
        String worklfowId = BPMEngineRegistry.createGlobalId(ActivitiConstants.ENGINE_ID, processInstance.getId());
        
        assertFalse(workflowEngine.hasWorkflowImage(worklfowId));
        assertNull(workflowEngine.getWorkflowImage(worklfowId));
        
        // Start process-instance that SHOULD have an image
        ProcessInstance processInstanceWithImage = runtime.startProcessInstanceById(BPMEngineRegistry.getLocalId(definitionWithImage.getId()));
        String worklfowWithImageId = BPMEngineRegistry.createGlobalId(ActivitiConstants.ENGINE_ID, processInstanceWithImage.getId());
        
        assertTrue(workflowEngine.hasWorkflowImage(worklfowWithImageId));
        assertNotNull(workflowEngine.getWorkflowImage(worklfowWithImageId));
    }
    
    
    private void putVariable(Map<String, Object> variables, QName varName, Object value)
    {
        String variableName = mapQNameToName(varName);
        variables.put(variableName, value);
    }

    private void checkDefinitions(List<WorkflowDefinition> actual,List<WorkflowDefinition> startDefs, WorkflowDefinition... expected)
    {
        assertEquals("The number of process definitions expected does not match the actual number!", startDefs.size() + expected.length, actual.size());
        ArrayList<String> ids = new ArrayList<String>(actual.size());
        for (WorkflowDefinition def : actual)
        {
            ids.add(def.getId());
        }
        for (WorkflowDefinition exp: expected)
        {
            assertTrue("Results did not contain expected definition: "+exp, ids.contains(exp.getId()));
        }
        List<String> startIds = new ArrayList<String>(startDefs.size());
        for (WorkflowDefinition def : startDefs)
        {
            startIds.add(def.getId());
        }
        for (WorkflowDefinition exp: expected)
        {
            assertFalse("Starting Definitions should not contain expected definition: "+exp, startIds.contains(exp.getId()));
        }
    }
    
}
