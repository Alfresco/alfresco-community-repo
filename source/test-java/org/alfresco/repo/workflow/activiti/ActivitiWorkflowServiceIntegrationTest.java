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

package org.alfresco.repo.workflow.activiti;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.workflow.AbstractWorkflowServiceIntegrationTest;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 * @since 3.4.e
 */
public class ActivitiWorkflowServiceIntegrationTest extends AbstractWorkflowServiceIntegrationTest
{
    public void testOutcome() throws Exception
    {
        WorkflowDefinition definition = deployDefinition("alfresco/workflow/review.bpmn20.xml");
        
        personManager.setUser(USER1);
        
        // Create workflow parameters
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        Serializable wfPackage = workflowService.createPackage(null);
        params.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        NodeRef assignee = personManager.get(USER2);
        params.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);  // task instance field

        WorkflowPath path = workflowService.startWorkflow(definition.getId(), params);
        String instanceId = path.getInstance().getId();
        
        WorkflowTask startTask = workflowService.getStartTask(instanceId);
        workflowService.endTask(startTask.getId(), null);
        
        List<WorkflowPath> paths = workflowService.getWorkflowPaths(instanceId);
        assertEquals(1, paths.size());
        path = paths.get(0);
        
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
        assertEquals(1, tasks.size());
        WorkflowTask reviewTask = tasks.get(0);
        
        // Set the transition property
        QName outcomePropName = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "reviewOutcome");
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(outcomePropName, "Approve");
        workflowService.updateTask(reviewTask.getId(), props, null, null);
        
        // End task and check outcome property
        WorkflowTask result = workflowService.endTask(reviewTask.getId(), null);
        Serializable outcome = result.getProperties().get(WorkflowModel.PROP_OUTCOME);
        assertEquals("Approve", outcome);
    }

    public void testStartTaskEndsAutomatically()
    {
        // Deploy the test workflow definition which uses the 
        // default Start Task type, so it should end automatically.
        WorkflowDefinition definition = deployDefinition(getTestDefinitionPath());
        
        // Start the Workflow
        WorkflowPath path = workflowService.startWorkflow(definition.getId(), null);
        String instanceId = path.getInstance().getId();

        // Check the Start Task is completed.
        WorkflowTask startTask = workflowService.getStartTask(instanceId);
        assertEquals(WorkflowTaskState.COMPLETED, startTask.getState());
        
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
        assertEquals(1, tasks.size());
        String taskName = tasks.get(0).getName();
        assertEquals("bpm_foo_task", taskName);
    }
    
    /**
     * Actually tests if the priority is the default value.  This is based on the assumption that custom
     * tasks are defaulted to a priority of 50 (which is invalid).  I'm testing that the code I wrote decides this is an
     * invalid number and sets it to the default value (2).
     */
    public void testPriorityIsValid()
    {
        WorkflowDefinition definition = deployDefinition("activiti/testCustomActiviti.bpmn20.xml");
        
        personManager.setUser(USER1);
        
        // Start the Workflow
        WorkflowPath path = workflowService.startWorkflow(definition.getId(), null);
        String instanceId = path.getInstance().getId();

        // Check the Start Task is completed.
        WorkflowTask startTask = workflowService.getStartTask(instanceId);
        assertEquals(WorkflowTaskState.COMPLETED, startTask.getState());
        
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
        for (WorkflowTask workflowTask : tasks)
        {
            Map<QName, Serializable> props = workflowTask.getProperties();
            TypeDefinition typeDefinition = workflowTask.getDefinition().getMetadata();
            Map<QName, PropertyDefinition> propertyDefs = typeDefinition.getProperties();        
            PropertyDefinition priorDef =  propertyDefs.get(WorkflowModel.PROP_PRIORITY);
            assertEquals(props.get(WorkflowModel.PROP_PRIORITY),Integer.valueOf(priorDef.getDefaultValue()));        
        }
    }   
    
    public void testGetWorkflowTaskDefinitionsWithMultiInstanceTask()
    {
    	// Test added to validate fix for ALF-14224
        WorkflowDefinition definition = deployDefinition(getParallelReviewDefinitionPath());
        String workflowDefId = definition.getId();
        List<WorkflowTaskDefinition> taskDefs = workflowService.getTaskDefinitions(workflowDefId);
        assertEquals(4, taskDefs.size());
        
        // The first task is the start-task, the second one is a multi-instance UserTask. This should have the right form-key
        WorkflowTaskDefinition taskDef = taskDefs.get(1);
        assertEquals("wf:activitiReviewTask", taskDef.getId());
    }
    
    public void testAccessStartTaskAsAssigneeFromTaskPartOfProcess()
    {
        // Test added to validate fix for CLOUD-1929 - start-task can be accesses by assignee of a task
        // part of that process
        WorkflowDefinition definition = deployDefinition(getAdhocDefinitionPath());
        
        // Start process as USER1
        personManager.setUser(USER1);
        
        // Create workflow parameters
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        Serializable wfPackage = workflowService.createPackage(null);
        params.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        NodeRef assignee = personManager.get(USER2);
        params.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);  // task instance field

        WorkflowPath path = workflowService.startWorkflow(definition.getId(), params);
        String instanceId = path.getInstance().getId();
        
        WorkflowTask startTask = workflowService.getStartTask(instanceId);
        workflowService.endTask(startTask.getId(), null);
        
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
        assertEquals(1, tasks.size());
        
        
        // Assign task to user3
        workflowService.updateTask(tasks.get(0).getId(), Collections.singletonMap(WorkflowModel.ASSOC_ASSIGNEE, 
                    (Serializable) personManager.get(USER3)), null, null);
        
        // Authenticate as user3
        personManager.setUser(USER3);
        
        // When fetchin the start-task, no exception should be thrown
        startTask = workflowService.getStartTask(instanceId);
        assertNotNull(startTask);
        startTask = workflowService.getTaskById(startTask.getId());
        assertNotNull(startTask);
        
        // Accessing by user4 shouldn't be possible
        personManager.setUser(USER4);
        try
        {
            workflowService.getStartTask(instanceId);
            fail("AccessDeniedException expected");
        }
        catch(AccessDeniedException expected) 
        {
            // Expected excaption
        }
        
        try
        {
            workflowService.getTaskById(startTask.getId());
            fail("AccessDeniedException expected");
        }
        catch(AccessDeniedException expected) 
        {
            // Expected exception
        }
    }
    
    /**
     * Test to validate fix for ALF-19822
     */
    public void testMultiInstanceListenersCalled() throws Exception
    {
        // start pooled review and approve workflow
        WorkflowDefinition workflowDef = deployDefinition(getParallelReviewDefinitionPath());
        assertNotNull(workflowDef);
        
        // Create workflow parameters
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        Serializable wfPackage = workflowService.createPackage(null);
        params.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        Date dueDate = new Date();
        params.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
        params.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 1);
        params.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, "This is the description");
        NodeRef group = groupManager.get(GROUP);
        assertNotNull(group);
        
        List<NodeRef> assignees = Arrays.asList(personManager.get(USER2), personManager.get(USER3));
        params.put(WorkflowModel.ASSOC_ASSIGNEES, (Serializable) assignees);
        
        // Start a workflow instance
        WorkflowPath path = workflowService.startWorkflow(workflowDef.getId(), params);
        assertNotNull(path);
        assertTrue(path.isActive());
        String instnaceId = path.getInstance().getId();
        
        WorkflowTask startTask = workflowService.getStartTask(instnaceId);
        workflowService.endTask(startTask.getId(), null);
        
        personManager.setUser(USER2);
        List<WorkflowTask> tasks = workflowService.getAssignedTasks(USER2, WorkflowTaskState.IN_PROGRESS);
        assertEquals(1, tasks.size());
        assertEquals("This is the description", tasks.get(0).getDescription());
    }
    
    /**
     * Test to validate fix for WOR-107
     */
    public void testLongTextValues() throws Exception
    {
        String veryLongTextValue = getLongString(10000);
        // start pooled review and approve workflow
        WorkflowDefinition workflowDef = deployDefinition(getAdhocDefinitionPath());
        assertNotNull(workflowDef);
        
        // Create workflow parameters
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        Serializable wfPackage = workflowService.createPackage(null);
        params.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        Date dueDate = new Date();
        params.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
        params.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 1);
        params.put(WorkflowModel.PROP_COMMENT, veryLongTextValue);
        
        NodeRef assignee = personManager.get(USER2);
        params.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);

        // No exception should be thrown when using *very* long String variables (in this case, 10000)
        WorkflowPath path = workflowService.startWorkflow(workflowDef.getId(), params);
        assertNotNull(path);
        
        WorkflowTask startTask = workflowService.getStartTask(path.getInstance().getId());
        assertNotNull(startTask);
        
        assertEquals(veryLongTextValue, startTask.getProperties().get(WorkflowModel.PROP_COMMENT));
    }
    
    /**
     * Test for MNT-11247
     */
    public void testAssignmentListener()
    {
        WorkflowDefinition definition = deployDefinition(getAssignmentListenerDefinitionPath());
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(ContentModel.PROP_OWNER, USER1);
        NodeRef assignee = personManager.get(USER1);
        params.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);
        WorkflowPath path = workflowService.startWorkflow(definition.getId(), params);

        // end start task
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
        workflowService.endTask(tasks.get(0).getId(), null);

        // end user task 1
        tasks = workflowService.getTasksForWorkflowPath(path.getId());
        workflowService.updateTask(tasks.get(0).getId(), params, null, null);
        workflowService.endTask(tasks.get(0).getId(), null);

        WorkflowTask result = workflowService.getTaskById(tasks.get(0).getId());
        Map<QName, Serializable> props = result.getProperties();
        Double create1 = (Double) props.get(QName.createQName("http://www.alfresco.org/model/bpm/1.0", "create1"));
        assertEquals("Create listener was not triggered", new Double(1), create1);
        Double complete1 = (Double) props.get(QName.createQName("http://www.alfresco.org/model/bpm/1.0", "complete1"));
        assertEquals("Complete listener was not triggered", new Double(1), complete1);
        Double assignment1 = (Double) props.get(QName.createQName("http://www.alfresco.org/model/bpm/1.0", "assignment1"));
        assertEquals("Assign listener was not triggered", new Double(1), assignment1);
    }
    
    protected String getLongString(int numberOfCharacters) {
        StringBuffer stringBuffer = new StringBuffer();
        for(int i=0; i<numberOfCharacters/10;i++) {
            stringBuffer.append("ABCDEFGHIJ");
        }
        return stringBuffer.toString();
    }
    
    
    @Override
    protected void checkTaskQueryStartTaskCompleted(String workflowInstanceId, WorkflowTask startTask) 
    {
        // In activiti, start-tasks only show up when the taskId or workflowInstanceId is passed in.
        List<String> expectedTasks = Arrays.asList(startTask.getId());
        
        checkProcessIdQuery(workflowInstanceId, expectedTasks, WorkflowTaskState.COMPLETED);
        checkTaskIdQuery(startTask.getId(), WorkflowTaskState.COMPLETED);
        
        // Check additional filtering, when workflowInstanceId is passed
        QName startTaskName = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "submitAdhocTask");
        checkTaskNameQuery(startTaskName, expectedTasks, WorkflowTaskState.COMPLETED, workflowInstanceId);
        checkActorIdQuery(USER1, expectedTasks, WorkflowTaskState.COMPLETED, workflowInstanceId);
        checkIsActiveQuery(expectedTasks, WorkflowTaskState.COMPLETED, workflowInstanceId);
        checkTaskPropsQuery(expectedTasks, WorkflowTaskState.COMPLETED, workflowInstanceId);
    }
    
    @Override
    protected void checkTaskQueryTaskCompleted(String workflowInstanceId, WorkflowTask theTask, WorkflowTask startTask) 
    {
        List<String> withoutStartTask = Arrays.asList(theTask.getId());
        List<String> bothTasks= Arrays.asList(theTask.getId(), startTask.getId());
        
        checkProcessIdQuery(workflowInstanceId, bothTasks, WorkflowTaskState.COMPLETED);
        
        // Adhoc task should only be returned
        QName taskName = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "adhocTask");
        checkTaskNameQuery(taskName, withoutStartTask, WorkflowTaskState.COMPLETED, null);

        // Completed adhocTask is assigned to USER2
        checkActorIdQuery(USER2, withoutStartTask, WorkflowTaskState.COMPLETED, null);
        
        checkIsActiveQuery(bothTasks, WorkflowTaskState.COMPLETED, workflowInstanceId);
       
        // Task has custom property set
        checkTaskPropsQuery(withoutStartTask, WorkflowTaskState.COMPLETED, null);
        
        // Process properties
        checkProcessPropsQuery(withoutStartTask, WorkflowTaskState.COMPLETED);
    }
    
    @Override
    protected void checkQueryTasksInactiveWorkflow(String workflowInstanceId) 
    {
        WorkflowTaskQuery taskQuery = createWorkflowTaskQuery(WorkflowTaskState.COMPLETED);
        taskQuery.setActive(false);
        taskQuery.setProcessId(workflowInstanceId);

        List<WorkflowTask> tasks = workflowService.queryTasks(taskQuery);
        assertNotNull(tasks);
        
        assertEquals(3, tasks.size());
        
        taskQuery = createWorkflowTaskQuery(WorkflowTaskState.COMPLETED);
        taskQuery.setActive(true);
        taskQuery.setProcessId(workflowInstanceId);
        checkNoTasksFoundUsingQuery(taskQuery);
    }
    
    public void testStartWorkflowFromTaskListener() throws Exception
    {
        WorkflowDefinition testDefinition = deployDefinition("activiti/testStartWfFromListener.bpmn20.xml");
        
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, "MNT-11926-testfile.txt");
        final ChildAssociationRef childAssoc = nodeService.createNode(companyHome, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "MNT-11926-test"), ContentModel.TYPE_CONTENT, props);

        try
        {
            // Create workflow parameters
            Map<QName, Serializable> params = new HashMap<QName, Serializable>();
            Serializable wfPackage = workflowService.createPackage(null);
            params.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
            NodeRef assignee = personManager.get(USER1);
            params.put(WorkflowModel.ASSOC_ASSIGNEE, assignee); // task instance field

            WorkflowPath path = workflowService.startWorkflow(testDefinition.getId(), params);
            String instanceId = path.getInstance().getId();

            WorkflowTask startTask = workflowService.getStartTask(instanceId);
            workflowService.endTask(startTask.getId(), null);
        }
        finally
        {
            // tidy up
            nodeService.deleteNode(childAssoc.getChildRef());
        }
    }
    
    @Override
    protected String getEngine()
    {
        return ActivitiConstants.ENGINE_ID;
    }

    @Override
    protected String getTestDefinitionPath()
    {
        return "activiti/testTransaction.bpmn20.xml";
    }

    @Override
    protected String getAdhocDefinitionPath()
    {
        return "alfresco/workflow/adhoc.bpmn20.xml";
    }

    @Override
    protected String getPooledReviewDefinitionPath()
    {
        return "alfresco/workflow/review-pooled.bpmn20.xml";
    }
    
    @Override
    protected String getParallelReviewDefinitionPath()
    {
        return "alfresco/workflow/parallel-review.bpmn20.xml";
    }

    @Override
    protected String getTestTimerDefinitionPath() 
    {
        return "activiti/testTimer.bpmn20.xml";
    }

    protected String getAssignmentListenerDefinitionPath()
    {
        return "activiti/testAssignmentListener.bmn20.xml";
    }
    
    @Override
    protected QName getAdhocProcessName() 
    {
        return QName.createQName("activitiAdhoc");
    }
}
