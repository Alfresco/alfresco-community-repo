/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.workflow.jbpm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.repo.workflow.TaskComponent;
import org.alfresco.repo.workflow.WorkflowComponent;
import org.alfresco.repo.workflow.WorkflowDefinitionComponent;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;


/**
 * JBPM Engine Tests
 * 
 * @author davidc
 */
public class JBPMEngineTest extends BaseSpringTest
{
    WorkflowDefinitionComponent workflowDefinitionComponent;
    WorkflowComponent workflowComponent;
    TaskComponent taskComponent;

    
    //@Override
    protected void onSetUpInTransaction() throws Exception
    {
        BPMEngineRegistry registry = (BPMEngineRegistry)applicationContext.getBean("bpm_engineRegistry");
        workflowDefinitionComponent = registry.getWorkflowDefinitionComponent("jbpm");
        workflowComponent = registry.getWorkflowComponent("jbpm");
        taskComponent = registry.getTaskComponent("jbpm");
    }

    
    public void testGetWorkflowDefinitions()
    {
        List<WorkflowDefinition> workflowDefs = workflowDefinitionComponent.getDefinitions();
        assertNotNull(workflowDefs);
        assertTrue(workflowDefs.size() > 0);
    }
    
    
    public void testStartWorkflow()
    {
        try
        {
            @SuppressWarnings("unused") WorkflowPath path = workflowComponent.startWorkflow("norfolknchance", null);
            fail("Failed to catch invalid definition id");
        }
        catch(WorkflowException e)
        {
        }

        // TODO: Determine why process definition is loaded, even though it doesn't exist
//        try
//        {
//            @SuppressWarnings("unused") WorkflowPosition pos = workflowComponent.startProcess("1000", null);
//            fail("Failed to catch workflow definition id that does not exist");
//        }
//        catch(WorkflowException e)
//        {
//        }

        WorkflowDefinition workflowDef = getTestDefinition();
        WorkflowPath path = workflowComponent.startWorkflow(workflowDef.id, null);
        assertNotNull(path);
        assertTrue(path.id.endsWith("::/"));
        assertNotNull(path.node);
        assertNotNull(path.instance);
        assertEquals(workflowDef.id, path.instance.definition.id);
    }

    
    public void testGetWorkflowInstances()
    {
        WorkflowDefinition workflowDef = getTestDefinition();
        workflowComponent.startWorkflow(workflowDef.id, null);
        workflowComponent.startWorkflow(workflowDef.id, null);
        List<WorkflowInstance> instances = workflowComponent.getActiveWorkflows(workflowDef.id);
        assertNotNull(instances);
        assertEquals(2, instances.size());
        for (WorkflowInstance instance : instances)
        {
            assertEquals(workflowDef.id, instance.definition.id);
        }
    }
    
    
    public void testGetPositions()
    {
        WorkflowDefinition workflowDef = getTestDefinition();
        workflowComponent.startWorkflow(workflowDef.id, null);
        List<WorkflowInstance> instances = workflowComponent.getActiveWorkflows(workflowDef.id);
        assertNotNull(instances);
        assertEquals(1, instances.size());
        List<WorkflowPath> paths = workflowComponent.getWorkflowPaths(instances.get(0).id);
        assertNotNull(paths);
        assertEquals(1, paths.size());
        assertEquals(instances.get(0).id, paths.get(0).instance.id);
        assertTrue(paths.get(0).id.endsWith("::/"));
    }

    
    public void testCancelWorkflowInstance()
    {
        WorkflowDefinition workflowDef = getTestDefinition();
        workflowComponent.startWorkflow(workflowDef.id, null);
        List<WorkflowInstance> instances1 = workflowComponent.getActiveWorkflows(workflowDef.id);
        assertNotNull(instances1);
        assertEquals(1, instances1.size());
        WorkflowInstance cancelledInstance = workflowComponent.cancelWorkflow(instances1.get(0).id);
        assertNotNull(cancelledInstance);
        assertFalse(cancelledInstance.active);
        List<WorkflowInstance> instances2 = workflowComponent.getActiveWorkflows(workflowDef.id);
        assertNotNull(instances2);
        assertEquals(0, instances2.size());
    }
    
 
    public void testSignal()
    {
        WorkflowDefinition workflowDef = getTestDefinition();
        Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
        parameters.put(QName.createQName(NamespaceService.DEFAULT_URI, "reviewer"), "admin");
        WorkflowPath path = workflowComponent.startWorkflow(workflowDef.id, parameters);
        assertNotNull(path);
        WorkflowPath updatedPath = workflowComponent.signal(path.id, path.node.transitions[0]);
        assertNotNull(updatedPath);
    }
    

    public void testGetAssignedTasks()
    {
        WorkflowDefinition workflowDef = getTestDefinition();
        Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
        parameters.put(QName.createQName(NamespaceService.DEFAULT_URI, "reviewer"), "admin");
        WorkflowPath path = workflowComponent.startWorkflow(workflowDef.id, parameters);
        assertNotNull(path);
        assertNotNull(path);
        WorkflowPath updatedPath = workflowComponent.signal(path.id, path.node.transitions[0]);
        assertNotNull(updatedPath);
        List<WorkflowTask> completedTasks = taskComponent.getAssignedTasks("admin", WorkflowTaskState.COMPLETED);
        assertNotNull(completedTasks);
        assertEquals(0, completedTasks.size());
        List<WorkflowTask> assignedTasks = taskComponent.getAssignedTasks("admin", WorkflowTaskState.IN_PROGRESS);
        assertNotNull(assignedTasks);
        assertEquals(1, assignedTasks.size());
        assertEquals("Review", assignedTasks.get(0).name);
    }

    
    public void testEndTask()
    {
        WorkflowDefinition workflowDef = getTestDefinition();
        Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
        parameters.put(QName.createQName(NamespaceService.DEFAULT_URI, "reviewer"), "admin");
        WorkflowPath path = workflowComponent.startWorkflow(workflowDef.id, parameters);
        assertNotNull(path);
        assertNotNull(path);
        List<WorkflowTask> tasks1 = workflowComponent.getTasksForWorkflowPath(path.id);
        assertNotNull(tasks1);
        assertEquals(1, tasks1.size());
        assertEquals(WorkflowTaskState.IN_PROGRESS, tasks1.get(0).state);
        WorkflowTask updatedTask = taskComponent.endTask(tasks1.get(0).id, null);
        assertNotNull(updatedTask);
        assertEquals(WorkflowTaskState.COMPLETED, updatedTask.state);
    }
    
    
    /**
     * Locate the Test Workflow Definition
     * 
     * @return  workflow definition
     */
    private WorkflowDefinition getTestDefinition()
    {
        List<WorkflowDefinition> workflowDefs = workflowDefinitionComponent.getDefinitions();
        for (WorkflowDefinition workflowDef : workflowDefs)
        {
            if (workflowDef.name.equals("Review and Approve"))
            {
                return workflowDef;
            }
        }
        fail("Test Workflow Definition not found");
        return null;
    }
    
}
