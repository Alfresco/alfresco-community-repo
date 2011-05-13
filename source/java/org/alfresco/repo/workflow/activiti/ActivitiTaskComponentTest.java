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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.task.Task;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.4.e
 * @author Nick Smith
 * @author Frederik Heremans
 */
public class ActivitiTaskComponentTest extends AbstractActivitiComponentTest
{
    private WorkflowDefinition workflowDef;
    
    @Test
    public void testGetStartTask()
    {
        try 
        {
            workflowEngine.getStartTask("Foo");
            fail("Should blow up if Id is wrong format!");
        }
        catch(WorkflowException e)
        {
            // Do Nothing
        }
        
        WorkflowTask result = workflowEngine.getStartTask(ActivitiConstants.ENGINE_ID + "$Foo");
        assertNull("Should not find any result for fake (but valid) Id.", result);
        
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        String comment = "Start task description";
        params.put(WorkflowModel.PROP_COMMENT, comment);
        params.put(WorkflowModel.PROP_PRIORITY, 1 );
        Date dueDate = new Date();
        params.put(WorkflowModel.PROP_DUE_DATE, dueDate );
        
        WorkflowPath path = workflowEngine.startWorkflow(workflowDef.getId(), params);
        String instanceId = path.getInstance().getId();
        WorkflowTask task = workflowEngine.getStartTask(instanceId);
        assertNotNull("Task shoudl exist!", task);
        
        String localId = ActivitiConstants.START_TASK_PREFIX+BPMEngineRegistry.getLocalId(instanceId);
        String taskId = BPMEngineRegistry.createGlobalId(ActivitiConstants.ENGINE_ID, localId);
        assertEquals("Start Task Id is wrong", taskId, task.getId());
        
        assertEquals("The start task path is wrong!", path.getId(), task.getPath().getId());
        TypeDefinition definition = task.getDefinition().getMetadata();
        assertNotNull(definition);
        String name = definition.getName().toPrefixString(namespaceService).replace(':', '_');
        assertEquals("bpm_foo", name);
        
        assertEquals(name, task.getName());
        assertEquals(name, task.getTitle());
        assertEquals(name, task.getDescription());
        assertEquals(WorkflowTaskState.IN_PROGRESS, task.getState());
        assertEquals(name, task.getDescription());
        
        // Check start task properties populated.
        Map<QName, Serializable> properties = task.getProperties();
        assertEquals(comment, properties.get(WorkflowModel.PROP_COMMENT));
        assertEquals(1, properties.get(WorkflowModel.PROP_PRIORITY));
        assertEquals(dueDate, properties.get(WorkflowModel.PROP_DUE_DATE));

        // Check start task after task is completed.
        task = workflowEngine.endTask(task.getId(), null);
        
        assertEquals("Start Task Id is wrong", taskId, task.getId());
        
        assertEquals("The start task path is wrong!", path.getId(), task.getPath().getId());
        definition = task.getDefinition().getMetadata();
        assertNotNull(definition);
        name = definition.getName().toPrefixString(namespaceService).replace(':', '_');
        assertEquals("bpm_foo", name);
        
        assertEquals(name, task.getName());
        assertEquals(name, task.getTitle());
        assertEquals(name, task.getDescription());
        assertEquals(WorkflowTaskState.COMPLETED, task.getState());
        assertEquals(name, task.getDescription());
        
        // Check start task properties populated.
        properties = task.getProperties();
        assertEquals(comment, properties.get(WorkflowModel.PROP_COMMENT));
        assertEquals(1, properties.get(WorkflowModel.PROP_PRIORITY));
        assertEquals(dueDate, properties.get(WorkflowModel.PROP_DUE_DATE));

        // Check start task for historic process.
        workflowEngine.cancelWorkflow(instanceId);
        task = workflowEngine.getStartTask(instanceId);
        
        assertNull(task);
        
        
//        assertEquals("Start Task Id is wrong", taskId, task.getId());
//        
//        assertEquals("The start task path is wrong!", path.getId(), task.getPath().getId());
//        definition = task.getDefinition().getMetadata();
//        assertNotNull(definition);
//        name = definition.getName().toPrefixString(namespaceService).replace(':', '_');
//        assertEquals("bpm_foo", name);
//        
//        assertEquals(name, task.getName());
//        assertEquals(name, task.getTitle());
//        assertEquals(name, task.getDescription());
//        assertEquals(WorkflowTaskState.COMPLETED, task.getState());
//        assertEquals(name, task.getDescription());
//        
//        // Check start task properties populated.
//        properties = task.getProperties();
//        assertEquals(comment, properties.get(WorkflowModel.PROP_COMMENT));
//        assertEquals(1, properties.get(WorkflowModel.PROP_PRIORITY));
//        assertEquals(dueDate, properties.get(WorkflowModel.PROP_DUE_DATE));
     }
    
    @Test
    public void testGetTaskById() throws Exception
    {
        try 
        {
            workflowEngine.getTaskById("Foo");
            fail("Should blow up if Id is wrong format!");
        }
        catch(WorkflowException e)
        {
            // Do Nothing
        }
        
        WorkflowTask result = workflowEngine.getTaskById(ActivitiConstants.ENGINE_ID + "$Foo");
        assertNull("Should not find any result for fake (but valid) Id.", result);
        
        WorkflowPath path = workflowEngine.startWorkflow(workflowDef.getId(), new HashMap<QName, Serializable>());
        Task task = taskService.createTaskQuery()
            .executionId(BPMEngineRegistry.getLocalId(path.getId()))
            .singleResult();
        assertNotNull("Task shoudl exist!", task);
        
        String taskId = BPMEngineRegistry.createGlobalId(ActivitiConstants.ENGINE_ID, task.getId());
        WorkflowTask wfTask = workflowEngine.getTaskById(taskId);
        assertNotNull(wfTask);
    }
    
    @Test
    public void testGetStartTaskById() throws Exception
    {
      
        WorkflowTask result = workflowEngine.getTaskById(ActivitiConstants.ENGINE_ID + "$Foo");
        assertNull("Should not find any result for fake (but valid) Id.", result);
        
        WorkflowPath path = workflowEngine.startWorkflow(workflowDef.getId(), new HashMap<QName, Serializable>());
       
        Task task = taskService.createTaskQuery()
            .executionId(BPMEngineRegistry.getLocalId(path.getId()))
            .singleResult();
        
        // A start task should be available for the process instance
        String startTaskId = ActivitiConstants.START_TASK_PREFIX + task.getProcessInstanceId();
        
        String taskId = createGlobalId(startTaskId);
        WorkflowTask wfTask = workflowEngine.getTaskById(taskId);
        assertNotNull(wfTask);
        assertEquals(createGlobalId(task.getProcessInstanceId()), wfTask.getPath().getId());
    }
    
    @SuppressWarnings("unchecked")
	@Test
    public void testGetFinishedTaskById() throws Exception
    {
        WorkflowPath path = workflowEngine.startWorkflow(workflowDef.getId(), new HashMap<QName, Serializable>());
        
        // Finish the start-task
        WorkflowTask startTask = workflowEngine.getStartTask(path.getInstance().getId());
        workflowEngine.endTask(startTask.getId(), null);
        
        // Set some task properties on the first task, different types
        List<WorkflowTask> tasks = workflowEngine.getTasksForWorkflowPath(path.getId());
        String finishedTaskId = tasks.get(0).getId();

        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(WorkflowModel.PROP_DESCRIPTION, "Task description");
        props.put(WorkflowModel.PROP_PRIORITY, 1234);
        props.put(QName.createQName("myprop1"), "Property value");
        props.put(QName.createQName("myprop2"), Boolean.TRUE);
        props.put(QName.createQName("myprop3"), 12345);
        props.put(QName.createQName("myprop4"), 45678L);
        
        workflowEngine.updateTask(finishedTaskId, props, null, null);
        
        // Finish the first task, this task will be used in this test
        workflowEngine.endTask(finishedTaskId, null);
        
        // Get the finished task
        WorkflowTask finishedTask = workflowEngine.getTaskById(finishedTaskId);
        assertNotNull(finishedTask);

        Assert.assertEquals("Task description", finishedTask.getDescription());
        
        Assert.assertEquals(finishedTaskId, finishedTask.getId());
        Assert.assertEquals("bpm_foo_task", finishedTask.getName());
        Assert.assertEquals("Task", finishedTask.getTitle());
        Assert.assertEquals(WorkflowTaskState.COMPLETED, finishedTask.getState());

        // Check if typeDefinition (formKey) is preserved on finished tasks
        Assert.assertEquals("task name", finishedTask.getDefinition().getId(), "bpm_foo_task");
        
        // Check workflowpath
        Assert.assertEquals(path.getId(), finishedTask.getPath().getId());
        Assert.assertEquals(path.getInstance().getId(), finishedTask.getPath().getInstance().getId());
        
        // Check variables
        Assert.assertEquals("Property value", finishedTask.getProperties().get(QName.createQName("myprop1")));
        Assert.assertEquals(Boolean.TRUE, finishedTask.getProperties().get(QName.createQName("myprop2")));
        Assert.assertEquals(12345, finishedTask.getProperties().get(QName.createQName("myprop3")));
        Assert.assertEquals(45678L, finishedTask.getProperties().get(QName.createQName("myprop4")));
        
        // Check pooled actors, should be one user and one group
        List<NodeRef> pooledActors = (List<NodeRef>) finishedTask.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS);
        Assert.assertNotNull(pooledActors);
        Assert.assertEquals(2, pooledActors.size());
        Assert.assertTrue(pooledActors.contains(testGroupNode));
        Assert.assertTrue(pooledActors.contains(testUserNode));
    }
    
    @Test
    public void testEndTask() throws Exception
    {
        WorkflowPath path = workflowEngine.startWorkflow(workflowDef.getId(), new HashMap<QName, Serializable>());
        
        Task task = taskService.createTaskQuery()
            .executionId(BPMEngineRegistry.getLocalId(path.getId()))
            .singleResult();
        
        assertNotNull("Task should exist!", task);
        
        String globalTaskId = createGlobalId(task.getId());
        // Set a custom property on the task, this will be flushed
        // to process-instance once task is completed
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(QName.createQName("http://test", "myVar"), "test123");
        
        workflowEngine.updateTask(globalTaskId, props, null, null);
        
        // Now end the task
        workflowEngine.endTask(globalTaskId, null);
        
        // Check if process-instance now contains the variable of the finished task
        List<HistoricDetail> updates = historyService.createHistoricDetailQuery()
            .variableUpdates()
            .processInstanceId(task.getProcessInstanceId())
            .list();
        
        boolean found = false;
        for(HistoricDetail detail : updates)
        {
            HistoricVariableUpdate update = (HistoricVariableUpdate) detail;
            if(update.getVariableName().equals("test_myVar"))
            {
                Assert.assertEquals("test123", update.getValue());
                found = true;
            }
        }
        
        Assert.assertTrue("Task variables are not flushed to process-instance", found);
    }
      
    @SuppressWarnings("unchecked")
    @Test
    public void testGetPooledTasks() throws Exception 
    {
        // The first task in the TestTaskDefinition has candidate group 'testGroup'
        // and candidate-user 'testUser'
        WorkflowPath path = workflowEngine.startWorkflow(workflowDef.getId(), new HashMap<QName, Serializable>());
        
        // Get start task
        WorkflowTask startTask = workflowEngine.getStartTask(path.getInstance().getId());
        assertNotNull(startTask);
        
        // Finish the start task
        workflowEngine.endTask(startTask.getId(), null);
        
        List<WorkflowTask> tasks = workflowEngine.getTasksForWorkflowPath(path.getId());
        assertNotNull(tasks);
        Assert.assertEquals(1, tasks.size());
        
        // Check if the ASSOC_POOLED_ACTORS is set on the task, to be sure
        // pooled actors are used on task
        WorkflowTask theTask = tasks.get(0);
        
        Serializable pooledActors = theTask.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS);
        assertNotNull(pooledActors);
        
        // Group and user should be present
        List<NodeRef> pooledActorNodes = (List<NodeRef>) pooledActors;
        Assert.assertEquals(2, pooledActorNodes.size());
        Assert.assertTrue(pooledActorNodes.contains(testUserNode));
        Assert.assertTrue(pooledActorNodes.contains(testGroupNode));
        
        // The task should be found when pooled tasks are requested
        List<WorkflowTask> pooledUserTasks = workflowEngine.getPooledTasks(Arrays.asList(TEST_USER));
        assertNotNull(pooledUserTasks);
        Assert.assertEquals(1, pooledUserTasks.size());
        Assert.assertEquals(theTask.getId(), pooledUserTasks.get(0).getId());
        
        // The task should be found when pooled taskes are requested
        List<WorkflowTask> pooledGroupTasks = workflowEngine.getPooledTasks(Arrays.asList(TEST_GROUP));
        assertNotNull(pooledGroupTasks);
        Assert.assertEquals(1, pooledGroupTasks.size());
        Assert.assertEquals(theTask.getId(), pooledGroupTasks.get(0).getId());
        
        // Only a single task should be found when task is both pooled for testUser and testGroup
        List<WorkflowTask> pooledTasks = workflowEngine.getPooledTasks(Arrays.asList(TEST_USER, TEST_GROUP));
        assertNotNull(pooledTasks);
        Assert.assertEquals(1, pooledTasks.size());
        Assert.assertEquals(theTask.getId(), pooledTasks.get(0).getId());

        // No tasks should be found
        List<WorkflowTask> unexistingPooledTasks = workflowEngine.getPooledTasks(Arrays.asList("unexisting"));
        assertNotNull(unexistingPooledTasks);
        Assert.assertEquals(0, unexistingPooledTasks.size());
        
        // If one authority matches, task should be returned
        pooledGroupTasks = workflowEngine.getPooledTasks(Arrays.asList("unexistinggroup",TEST_GROUP));
        assertNotNull(pooledGroupTasks);
        Assert.assertEquals(1, pooledGroupTasks.size());
        Assert.assertEquals(theTask.getId(), pooledGroupTasks.get(0).getId());
    }
    
    @Test
    public void testQueryTasksInProgress() throws Exception {
        // Testing all query functionality for WorkflowTaskState.IN_PROGRESS
        WorkflowPath path = workflowEngine.startWorkflow(workflowDef.getId(), new HashMap<QName, Serializable>());
        
        Task task = taskService.createTaskQuery()
            .executionId(BPMEngineRegistry.getLocalId(path.getId()))
            .singleResult();
        assertNotNull("Task should exist!", task);
        
        String globalTaskId = createGlobalId(task.getId());
        
        // Test query by taskId
        WorkflowTaskQuery taskQuery = createWorkflowTaskQuery(WorkflowTaskState.IN_PROGRESS);
        taskQuery.setTaskId(globalTaskId);
        
        List<WorkflowTask> tasks = workflowEngine.queryTasks(taskQuery);
        Assert.assertNotNull(tasks);
        Assert.assertEquals(1, tasks.size());
        Assert.assertEquals(globalTaskId, tasks.get(0).getId());
        
        // Test query by nonexistent taskId
        taskQuery = createWorkflowTaskQuery(WorkflowTaskState.IN_PROGRESS);
        taskQuery.setTaskId(createGlobalId("nonexistentTask"));
        
        tasks = workflowEngine.queryTasks(taskQuery);
        Assert.assertNotNull(tasks);
        Assert.assertEquals(0, tasks.size());
        
        // Test query by process ID
        taskQuery =  createWorkflowTaskQuery(WorkflowTaskState.IN_PROGRESS);
        taskQuery.setProcessId(createGlobalId(task.getProcessInstanceId()));
        
        tasks = workflowEngine.queryTasks(taskQuery);
        Assert.assertNotNull(tasks);
        Assert.assertEquals(1, tasks.size());
        Assert.assertEquals(globalTaskId, tasks.get(0).getId());
        
        // Test query by nonexistent processId
        taskQuery = createWorkflowTaskQuery(WorkflowTaskState.IN_PROGRESS);
        taskQuery.setProcessId(createGlobalId("nonexistentProcess"));
        
        tasks = workflowEngine.queryTasks(taskQuery);
        Assert.assertNotNull(tasks);
        Assert.assertEquals(0, tasks.size());
        
        // Test query by actor ID
        taskQuery =  createWorkflowTaskQuery(WorkflowTaskState.IN_PROGRESS);
        taskQuery.setActorId(TEST_USER);
        tasks = workflowEngine.queryTasks(taskQuery);
        
        // No tasks should be assigned to testUser
        Assert.assertNotNull(tasks);
        Assert.assertEquals(0, tasks.size());
        
        // Assign the task
        taskService.setAssignee(task.getId(), TEST_USER);
        taskQuery =  createWorkflowTaskQuery(WorkflowTaskState.IN_PROGRESS);
        taskQuery.setActorId(TEST_USER);
        tasks = workflowEngine.queryTasks(taskQuery);
        // Task is assigned to testUser
        Assert.assertNotNull(tasks);
        Assert.assertEquals(1, tasks.size());
        Assert.assertEquals(globalTaskId, tasks.get(0).getId());
        
        // Test by nonexistent actor ID
        taskQuery =  createWorkflowTaskQuery(WorkflowTaskState.IN_PROGRESS);
        taskQuery.setActorId("nonexistentUser");
        tasks = workflowEngine.queryTasks(taskQuery);
        
        Assert.assertNotNull(tasks);
        Assert.assertEquals(0, tasks.size());
        
        // Test query by process-name
        taskQuery =  createWorkflowTaskQuery(WorkflowTaskState.IN_PROGRESS);
        taskQuery.setProcessName(QName.createQName("testTask"));
        tasks = workflowEngine.queryTasks(taskQuery);
        
        Assert.assertNotNull(tasks);
        Assert.assertEquals(1, tasks.size());
        
        taskQuery.setProcessName(QName.createQName("unexistingTaskName"));
        tasks = workflowEngine.queryTasks(taskQuery);
        Assert.assertNotNull(tasks);
        Assert.assertEquals(0, tasks.size());
        
        // Test query by task-name
        taskQuery =  createWorkflowTaskQuery(WorkflowTaskState.IN_PROGRESS);
        taskQuery.setTaskName(QName.createQName("bpm_foo_task"));
        tasks = workflowEngine.queryTasks(taskQuery);
        
        Assert.assertNotNull(tasks);
        Assert.assertEquals(1, tasks.size());
        
        taskQuery.setTaskName(QName.createQName("unexisting_task_name"));
        tasks = workflowEngine.queryTasks(taskQuery);
        Assert.assertNotNull(tasks);
        Assert.assertEquals(0, tasks.size());
        
        // Test querying task variables, using all possible (and allowed) types of variables
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("longVar", 928374L);
        variables.put("shortVar", (short) 123);
        variables.put("integerVar", 1234);
        variables.put("stringVar", "stringValue");
        variables.put("booleanVar", true);
        Date date = Calendar.getInstance().getTime();
        variables.put("dateVar", date);
        variables.put("nullVar", null);
        ActivitiScriptNode scriptNode = new ActivitiScriptNode(testGroupNode, serviceRegistry);
        variables.put("scriptNodeVar", scriptNode);
        
        taskService.setVariablesLocal(task.getId(), variables);

        // Query long variable
        checkTaskVariableTaskPresent(WorkflowTaskState.IN_PROGRESS, QName.createQName("longVar"), 928374L, globalTaskId);
        checkTaskVariableNoMatch(WorkflowTaskState.IN_PROGRESS, QName.createQName("longVar"), 444444L);
        
        // Query short variable
        checkTaskVariableTaskPresent(WorkflowTaskState.IN_PROGRESS, QName.createQName("shortVar"), (short) 123, globalTaskId);
        checkTaskVariableNoMatch(WorkflowTaskState.IN_PROGRESS, QName.createQName("shortVar"), (short) 456);
        
        // Query integer variable
        checkTaskVariableTaskPresent(WorkflowTaskState.IN_PROGRESS, QName.createQName("integerVar"), 1234, globalTaskId);
        checkTaskVariableNoMatch(WorkflowTaskState.IN_PROGRESS, QName.createQName("integerVar"), 5678);
        
        // Query string variable
        checkTaskVariableTaskPresent(WorkflowTaskState.IN_PROGRESS, QName.createQName("stringVar"), "stringValue", globalTaskId);
        checkTaskVariableNoMatch(WorkflowTaskState.IN_PROGRESS, QName.createQName("stringVar"), "noMatchString");
        
        // Query string variable
        checkTaskVariableTaskPresent(WorkflowTaskState.IN_PROGRESS, QName.createQName("booleanVar"), true, globalTaskId);
        checkTaskVariableNoMatch(WorkflowTaskState.IN_PROGRESS, QName.createQName("booleanVar"), false);
        
        // Query date variable
        checkTaskVariableTaskPresent(WorkflowTaskState.IN_PROGRESS, QName.createQName("dateVar"), date, globalTaskId);
        Calendar otherDate = Calendar.getInstance();
        otherDate.add(Calendar.YEAR, 1);
        checkTaskVariableNoMatch(WorkflowTaskState.IN_PROGRESS, QName.createQName("dateVar"), otherDate.getTime());
        
        // Query null variable
        checkTaskVariableTaskPresent(WorkflowTaskState.IN_PROGRESS, QName.createQName("nullVar"), null, globalTaskId);
        checkTaskVariableNoMatch(WorkflowTaskState.IN_PROGRESS, QName.createQName("nullVar"), "notNull");
        
        // Query script-node variable
        checkTaskVariableTaskPresent(WorkflowTaskState.IN_PROGRESS, QName.createQName("scriptNodeVar"), scriptNode, globalTaskId);
        ActivitiScriptNode otherNode = new ActivitiScriptNode(testUserNode, serviceRegistry);
        checkTaskVariableNoMatch(WorkflowTaskState.IN_PROGRESS, QName.createQName("scriptNodeVar"), otherNode);
        
        
        // Query task based on process variable
        runtime.setVariable(task.getExecutionId(), "processVar", "testing");
        taskQuery = createWorkflowTaskQuery(WorkflowTaskState.IN_PROGRESS);
        Map<QName, Object> props = new HashMap<QName, Object>();
        props.put(QName.createQName("processVar"), "testing");
        taskQuery.setProcessCustomProps(props);
        
        tasks = workflowEngine.queryTasks(taskQuery);
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        
        props.put(QName.createQName("processVar"), "notmatching");
        
        tasks = workflowEngine.queryTasks(taskQuery);
        assertNotNull(tasks);
        assertEquals(0, tasks.size());
    }
    
    
    @Test
    public void testQueryTasksCompleted() throws Exception {
        // Testing all query functionality for WorkflowTaskState.COMPLETED
        WorkflowPath path = workflowEngine.startWorkflow(workflowDef.getId(), new HashMap<QName, Serializable>());
        
        Task task = taskService.createTaskQuery()
	        .executionId(BPMEngineRegistry.getLocalId(path.getId()))
	        .singleResult();
        
        taskService.setVariableLocal(task.getId(), "taskVar", "theValue");
	    assertNotNull("Task should exist!", task);
	    String globalTaskId = createGlobalId(task.getId());
	    
	    // Set the actor
	    taskService.setAssignee(task.getId(), TEST_USER);
	    
	    // Set process prop
	    runtime.setVariable(task.getExecutionId(), "processVar", "testing");
	    
	    // End the task
	    workflowEngine.endTask(globalTaskId, null);

	    // Test query by taskId
        WorkflowTaskQuery taskQuery = createWorkflowTaskQuery(WorkflowTaskState.COMPLETED);
        taskQuery.setActive(Boolean.FALSE); // Set to false, since workflow this task is in, has finished
        taskQuery.setTaskId(globalTaskId);
        
        List<WorkflowTask> tasks = workflowEngine.queryTasks(taskQuery);
        Assert.assertNotNull(tasks);
        Assert.assertEquals(1, tasks.size());
        Assert.assertEquals(globalTaskId, tasks.get(0).getId());
        
        // Test query by nonexistent task ID
        taskQuery = createWorkflowTaskQuery(WorkflowTaskState.COMPLETED);
        taskQuery.setTaskId(createGlobalId("nonexistantTask"));
        
        tasks = workflowEngine.queryTasks(taskQuery);
        Assert.assertNotNull(tasks);
        Assert.assertEquals(0, tasks.size());
        
        // Test query by process ID, this should also return the start-task
        taskQuery =  createWorkflowTaskQuery(WorkflowTaskState.COMPLETED);
        taskQuery.setProcessId(createGlobalId(task.getProcessInstanceId()));
        taskQuery.setActive(Boolean.FALSE);
        
        tasks = workflowEngine.queryTasks(taskQuery);
        Assert.assertNotNull(tasks);
        Assert.assertEquals(2, tasks.size());
        
        boolean taskFound = false;
        boolean startTaskFound = false;
        for(WorkflowTask wfTask : tasks)
        {
        	if(wfTask.getId().equals(globalTaskId))
        	{
        		taskFound = true;
        	}
        	if(wfTask.getId().contains(ActivitiConstants.START_TASK_PREFIX))
        	{
        		startTaskFound = true;
        	}
        }
        Assert.assertTrue("Task should have been returned", taskFound);
        Assert.assertTrue("Start-task should have been returned", startTaskFound);
        
        // Test query by nonexistent process ID
        taskQuery =  createWorkflowTaskQuery(WorkflowTaskState.COMPLETED);
        taskQuery.setProcessId(createGlobalId("nonexistantProcess"));
        
        tasks = workflowEngine.queryTasks(taskQuery);
        Assert.assertNotNull(tasks);
        Assert.assertEquals(0, tasks.size());
        
        // Test query by actor
        taskQuery =  createWorkflowTaskQuery(WorkflowTaskState.COMPLETED);
        taskQuery.setActorId(TEST_USER);
        taskQuery.setActive(Boolean.FALSE);
        
        tasks = workflowEngine.queryTasks(taskQuery);
        Assert.assertNotNull(tasks);
        Assert.assertEquals(1, tasks.size());
        
        // Test by nonexistent actor
        taskQuery =  createWorkflowTaskQuery(WorkflowTaskState.COMPLETED);
        taskQuery.setActorId("unexistingUser");
        
        tasks = workflowEngine.queryTasks(taskQuery);
        Assert.assertNotNull(tasks);
        Assert.assertEquals(0, tasks.size());
        
        // Test query by process-name
        taskQuery =  createWorkflowTaskQuery(WorkflowTaskState.COMPLETED);
        taskQuery.setProcessName(QName.createQName("testTask"));
        taskQuery.setActive(Boolean.FALSE);
        tasks = workflowEngine.queryTasks(taskQuery);
        
        Assert.assertNotNull(tasks);
        Assert.assertEquals(1, tasks.size());
        
        taskQuery.setProcessName(QName.createQName("unexistingTaskName"));
        tasks = workflowEngine.queryTasks(taskQuery);
        Assert.assertNotNull(tasks);
        Assert.assertEquals(0, tasks.size());
        
        // Query task based on task variable
        taskQuery = createWorkflowTaskQuery(WorkflowTaskState.COMPLETED);
        Map<QName, Object> props = new HashMap<QName, Object>();
        props.put(QName.createQName("taskVar"), "theValue");
        taskQuery.setActive(false);
        taskQuery.setTaskCustomProps(props);
        
        tasks = workflowEngine.queryTasks(taskQuery);
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        
        props.put(QName.createQName("processVar"), "notmatching");
        
        tasks = workflowEngine.queryTasks(taskQuery);
        assertNotNull(tasks);
        assertEquals(0, tasks.size());
        
        // Query task based on process variable
        taskQuery = createWorkflowTaskQuery(WorkflowTaskState.COMPLETED);
        props = new HashMap<QName, Object>();
        props.put(QName.createQName("processVar"), "testing");
        taskQuery.setActive(false);
        taskQuery.setProcessCustomProps(props);
        
        tasks = workflowEngine.queryTasks(taskQuery);
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        
        props.put(QName.createQName("processVar"), "notmatching");
        
        tasks = workflowEngine.queryTasks(taskQuery);
        assertNotNull(tasks);
        assertEquals(0, tasks.size());
        
    }
    
    private void checkTaskVariableTaskPresent(WorkflowTaskState state,
			QName varName, Object varValue, String expectedTask) {
        WorkflowTaskQuery taskQuery =  createWorkflowTaskQuery(state);
        Map<QName, Object> customProperties = new HashMap<QName, Object>();
        customProperties.put(varName, varValue);
        taskQuery.setTaskCustomProps(customProperties);

        assertTaskPresent(taskQuery, expectedTask);
	}
    
    private void checkTaskVariableNoMatch(WorkflowTaskState state,
			QName varName, Object varValue) {
        WorkflowTaskQuery taskQuery =  createWorkflowTaskQuery(state);
        Map<QName, Object> customProperties = new HashMap<QName, Object>();
        customProperties.put(varName, varValue);
        taskQuery.setTaskCustomProps(customProperties);

        assertNoTaskPresent(taskQuery);
	}

	private WorkflowTaskQuery createWorkflowTaskQuery(WorkflowTaskState state) 
    {
    	WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
        taskQuery.setTaskState(state);
        return taskQuery;
    }
   
        
	private void assertTaskPresent(WorkflowTaskQuery taskQuery,
			String taskId) 
	{
		List<WorkflowTask> tasks = workflowEngine.queryTasks(taskQuery);
        Assert.assertNotNull(tasks);
        Assert.assertEquals(1, tasks.size());
        Assert.assertEquals(taskId, tasks.get(0).getId());
	}
	
	private void assertNoTaskPresent(WorkflowTaskQuery taskQuery)
	{
		List<WorkflowTask> tasks = workflowEngine.queryTasks(taskQuery);
        Assert.assertNotNull(tasks);
        Assert.assertEquals(0, tasks.size());
    }
    
    private String createGlobalId(String id)
    {
        return BPMEngineRegistry.createGlobalId(ActivitiConstants.ENGINE_ID, id);
    }

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        this.workflowDef = deployTestTaskDefinition();
    }
}
