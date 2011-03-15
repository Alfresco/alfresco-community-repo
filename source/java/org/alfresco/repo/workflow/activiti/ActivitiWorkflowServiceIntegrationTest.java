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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.workflow.AbstractWorkflowServiceIntegrationTest;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
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
    public void testQueryTasks() {
    	// TODO: Revive test once all pieces of queryTasks() are finished on ActivitiWorkflowEngine
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
	protected String getTestTimerDefinitionPath() 
	{
		return "activiti/testTimer.bpmn20.xml";
	}
	
	@Override
	protected QName getAdhocProcessName() {
		return QName.createQName("activitiAdhoc");
	}
    
}
