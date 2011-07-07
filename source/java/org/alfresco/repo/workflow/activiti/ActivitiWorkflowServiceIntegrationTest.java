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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.workflow.AbstractWorkflowServiceIntegrationTest;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @since 3.4.e
 * @author Nick Smith
 *
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

    @Override
    protected void checkTaskQueryStartTaskCompleted(String workflowInstanceId, WorkflowTask startTask) 
    {
    	// In activiti, start-tasks only show up when the taskId or task name is passed in.
		List<String> expectedTasks = Arrays.asList(startTask.getId());
		
		List<String> noTaskIds = Collections.emptyList();
        checkProcessIdQuery(workflowInstanceId, noTaskIds, WorkflowTaskState.COMPLETED);
	    checkTaskIdQuery(startTask.getId(), WorkflowTaskState.COMPLETED);
	    
	    // Check additional filtering, when workflowInstanceId is passed
	    QName startTaskName = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "submitAdhocTask");
	    checkTaskNameQuery(startTaskName, expectedTasks, WorkflowTaskState.COMPLETED, workflowInstanceId);
	    checkActorIdQuery(USER1, noTaskIds, WorkflowTaskState.COMPLETED, workflowInstanceId);
	    checkIsActiveQuery(noTaskIds, WorkflowTaskState.COMPLETED, workflowInstanceId);
	    checkTaskPropsQuery(noTaskIds, WorkflowTaskState.COMPLETED, workflowInstanceId);
	}
    
    @Override
    protected void checkTaskQueryTaskCompleted(String workflowInstanceId, WorkflowTask theTask, WorkflowTask startTask) 
	{
		List<String> withoutStartTask = Arrays.asList(theTask.getId());
        
		checkProcessIdQuery(workflowInstanceId, withoutStartTask, WorkflowTaskState.COMPLETED);
        
        // Adhoc task should only be returned
        QName taskName = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "adhocTask");
        checkTaskNameQuery(taskName, withoutStartTask, WorkflowTaskState.COMPLETED, null);

        // Completed adhocTask is assigned to USER2
        checkActorIdQuery(USER2, withoutStartTask, WorkflowTaskState.COMPLETED, null);
        
        // Workflow is still active, but in activiti, active start-task is not returned when
        // no process-instance ID is provided
        checkIsActiveQuery(withoutStartTask, WorkflowTaskState.COMPLETED, workflowInstanceId);
       
        // Task has custom property set
        checkTaskPropsQuery(withoutStartTask, WorkflowTaskState.COMPLETED, null);
        
        // Process properties
        checkProcessPropsQuery(withoutStartTask, WorkflowTaskState.COMPLETED);
	}
    
    @Override
    protected void checkQueryTasksInactiveWorkflow(String workflowInstanceId) {
		WorkflowTaskQuery taskQuery = createWorkflowTaskQuery(WorkflowTaskState.COMPLETED);
		taskQuery.setActive(false);
		taskQuery.setProcessId(workflowInstanceId);
		
		List<WorkflowTask> tasks = workflowService.queryTasks(taskQuery);
        assertNotNull(tasks);
        
        // Activiti doesn't return start-task when no process/task id is set in query, so only 2 tasks will be returned
        assertEquals(2, tasks.size());
        
        taskQuery = createWorkflowTaskQuery(WorkflowTaskState.COMPLETED);
		taskQuery.setActive(true);
        taskQuery.setProcessId(workflowInstanceId);
		checkNoTasksFoundUsingQuery(taskQuery);
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
	
	@Override
	protected QName getAdhocProcessName() {
		return QName.createQName("activitiAdhoc");
	}
    
}
