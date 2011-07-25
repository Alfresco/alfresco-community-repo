/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.repo.workflow.TaskComponent;
import org.alfresco.repo.workflow.WorkflowComponent;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.springframework.core.io.ClassPathResource;


/**
 * Review and Approve workflow specific Tests
 * 
 * @author davidc
 */
public class ReviewAndApproveTest extends BaseSpringTest
{
    AuthenticationComponent authenticationComponent;
    PersonService personService;
    WorkflowComponent workflowComponent;
    TaskComponent taskComponent;
    WorkflowDefinition testWorkflowDef;
    NodeRef testNodeRef;
    
    @SuppressWarnings("deprecation")
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        personService = (PersonService)applicationContext.getBean("personService");
        BPMEngineRegistry registry = (BPMEngineRegistry)applicationContext.getBean("bpm_engineRegistry");
        workflowComponent = registry.getWorkflowComponent(JBPMEngine.ENGINE_ID);
        taskComponent = registry.getTaskComponent(JBPMEngine.ENGINE_ID);
        
        // deploy latest review and approve process definition
        ClassPathResource processDef = new ClassPathResource("alfresco/workflow/review_processdefinition.xml");
        WorkflowDeployment deployment = workflowComponent.deployDefinition(processDef.getInputStream(), MimetypeMap.MIMETYPE_XML); 
        testWorkflowDef = deployment.getDefinition(); 
        assertNotNull(testWorkflowDef);

        // run as system
        authenticationComponent = (AuthenticationComponent)applicationContext.getBean("authenticationComponent");
        authenticationComponent.setSystemUserAsCurrentUser();
        
        // get valid node ref
        NodeService nodeService = (NodeService)applicationContext.getBean(ServiceRegistry.NODE_SERVICE.getLocalName());
        testNodeRef = nodeService.getRootNode(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"));
    }

    @Override
    protected void onTearDownInTransaction()
    {
        authenticationComponent.clearCurrentSecurityContext();
    }

    
    public void testSubmitForReview()
    {
        WorkflowDefinition workflowDef = testWorkflowDef;
        
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.ASSOC_PACKAGE, testNodeRef);
        Date reviewDueDate = new Date();
        params.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, reviewDueDate);
        NodeRef reviewer = personService.getPerson(AuthenticationUtil.getAdminUserName());
        params.put(WorkflowModel.ASSOC_ASSIGNEE, reviewer);
        params.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, "Test review");
        
        WorkflowPath path = workflowComponent.startWorkflow(workflowDef.getId(), params);
        assertNotNull(path);
        List<WorkflowTask> tasks1 = workflowComponent.getTasksForWorkflowPath(path.getId());
        assertNotNull(tasks1);
        assertEquals(1, tasks1.size());

        WorkflowTask task = tasks1.get(0);
        assertTrue(task.getProperties().containsKey(WorkflowModel.ASSOC_PACKAGE));
        WorkflowTask endedTask = taskComponent.endTask(task.getId(), null);
        assertNotNull(endedTask);
        assertTrue(endedTask.getProperties().containsKey(WorkflowModel.PROP_OUTCOME));
        assertEquals("", endedTask.getProperties().get(WorkflowModel.PROP_OUTCOME));
        assertEquals("Test review", endedTask.getProperties().get(WorkflowModel.PROP_DESCRIPTION));
        assertEquals("Test review", endedTask.getPath().getInstance().getDescription());
        
        List<WorkflowTask> assignedTasks = taskComponent.getAssignedTasks(AuthenticationUtil.getAdminUserName(), WorkflowTaskState.IN_PROGRESS);
        assertNotNull(assignedTasks);
        assignedTasks = filterTasksByWorkflowInstance(assignedTasks, path.getInstance().getId());
        
        assertEquals(testNodeRef, assignedTasks.get(0).getProperties().get(WorkflowModel.ASSOC_PACKAGE));
        assertEquals(reviewDueDate, assignedTasks.get(0).getProperties().get(WorkflowModel.PROP_DUE_DATE));
    }

    public void testCompletedItems()
    {
        WorkflowDefinition workflowDef = testWorkflowDef;
        
        List<NodeRef> nodeRefs = new ArrayList<NodeRef>();
        nodeRefs.add(testNodeRef);
        nodeRefs.add(testNodeRef);
        
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.ASSOC_PACKAGE, testNodeRef);
        params.put(WorkflowModel.PROP_COMPLETED_ITEMS, (Serializable)nodeRefs);
        Date reviewDueDate = new Date();
        params.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, reviewDueDate);
        NodeRef reviewer = personService.getPerson(AuthenticationUtil.getAdminUserName());
        params.put(WorkflowModel.ASSOC_ASSIGNEE, reviewer);
        
        WorkflowPath path = workflowComponent.startWorkflow(workflowDef.getId(), params);
        assertNotNull(path);
        List<WorkflowTask> tasks1 = workflowComponent.getTasksForWorkflowPath(path.getId());
        assertNotNull(tasks1);
        assertEquals(1, tasks1.size());

        WorkflowTask task = tasks1.get(0);
        assertTrue(task.getProperties().containsKey(WorkflowModel.PROP_COMPLETED_ITEMS));
        assertEquals(2, ((List<?>)task.getProperties().get(WorkflowModel.PROP_COMPLETED_ITEMS)).size());
    }
    
    
    /**
     * Filter task list by workflow instance
     * 
     * @param tasks
     * @param processInstanceId
     * @return
     */
    private List<WorkflowTask> filterTasksByWorkflowInstance(List<WorkflowTask> tasks, String workflowInstanceId)
    {
        List<WorkflowTask> filteredTasks = new ArrayList<WorkflowTask>();
        for (WorkflowTask task : tasks)
        {
            if (task.getPath().getInstance().getId().equals(workflowInstanceId))
            {
                filteredTasks.add(task);
            }
        }
        return filteredTasks;
    }
    
}
