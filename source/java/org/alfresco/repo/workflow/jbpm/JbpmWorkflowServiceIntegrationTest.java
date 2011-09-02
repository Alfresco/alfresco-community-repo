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
package org.alfresco.repo.workflow.jbpm;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.workflow.AbstractWorkflowServiceIntegrationTest;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * JBPM Workflow Service Implementation Tests
 * 
 * @author Nick Smith
 * @since 3.4.e
 */
public class JbpmWorkflowServiceIntegrationTest extends AbstractWorkflowServiceIntegrationTest
{

    @SuppressWarnings("deprecation")
    public void disabledTestAsynchronousTaskExecutes() throws Exception
    {
        setComplete();
        endTransaction();
        
        String defId = null;
        String instanceId = null;
        try 
        {
            WorkflowDefinition def = deployDefinition(getAsyncAdhocPath());
            defId = def.getId();
            
            // Create workflow parameters
            Map<QName, Serializable> params = new HashMap<QName, Serializable>();
            Serializable wfPackage = workflowService.createPackage(null);
            params.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
            Date dueDate = new Date();
            params.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
            params.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 1);
            NodeRef assignee = personManager.get(USER2);
            params.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);

            WorkflowPath path = workflowService.startWorkflow(defId, params);
            instanceId = path.getInstance().getId();
            
            // End the Start Task.
            List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
            assertEquals(1, tasks.size());
            WorkflowTask startTask = tasks.get(0);
            workflowService.endTask(startTask.getId(), null);

            // Wait for async execution to occur.
            Thread.sleep(1000);

            // Should move past the asynchronous adhoc task.
            tasks = workflowService.getTasksForWorkflowPath(path.getId());
            assertEquals(1, tasks.size());
            WorkflowTask endTask = tasks.get(0);
            assertEquals("wf:completedAdhocTask", endTask.getName());

            // Check async task assigned to USER2
            tasks = workflowService.getAssignedTasks(USER2, WorkflowTaskState.IN_PROGRESS);
            assertEquals(1, tasks.size());
            WorkflowTask adhocTask = tasks.get(0);
            assertEquals("wf:adhocTask", adhocTask.getName());
        }
        finally
        {
            if(instanceId != null)
            {
                workflowService.cancelWorkflow(instanceId);
            }
            if(defId != null)
            {
                workflowService.undeployDefinition(defId);
            }
            
        }
    }
    
    private String getAsyncAdhocPath()
    {
        return "jbpmresources/async_adhoc_processdefinition.xml";
    }

    @Override
    protected String getEngine()
    {
        return JBPMEngine.ENGINE_ID;
    }

    @Override
    protected String getTestDefinitionPath()
    {
        return "jbpmresources/test_simple_processdefinition.xml";
    }

    @Override
    protected String getAdhocDefinitionPath()
    {
        return "alfresco/workflow/adhoc_processdefinition.xml";
    }
    
    @Override
    protected String getPooledReviewDefinitionPath()
    {
        return "alfresco/workflow/review_pooled_processdefinition.xml";
    }

    @Override
    protected String getParallelReviewDefinitionPath()
    {
        return "alfresco/workflow/parallelreview_processdefinition.xml";
    }
    
    @Override
    protected String getTestTimerDefinitionPath() 
    {
        return "jbpmresources/test_timer.xml";
    }

    @Override
    protected QName getAdhocProcessName() 
    {
        return QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "adhoc");
    }
}
