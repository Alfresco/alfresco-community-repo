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
package org.alfresco.repo.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;


/**
 * Default Alfresco Workflow Service whose implementation is backed by registered 
 * BPM Engine plug-in components.
 * 
 * @author davidc
 */
public class WorkflowServiceImpl implements WorkflowService
{
    private BPMEngineRegistry registry;
    

    /**
     * Sets the BPM Engine Registry
     * 
     * @param registry  bpm engine registry
     */
    public void setBPMEngineRegistry(BPMEngineRegistry registry)
    {
        this.registry = registry;
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#deployDefinition(org.alfresco.service.cmr.repository.NodeRef)
     */
    public WorkflowDefinition deployDefinition(NodeRef definitionContent)
    {
        // TODO
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#undeployDefinition(java.lang.String)
     */
    public void undeployDefinition(String processDefinitionId)
    {
        // TODO
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#getDefinitions()
     */
    public List<WorkflowDefinition> getDefinitions()
    {
        List<WorkflowDefinition> definitions = new ArrayList<WorkflowDefinition>(10);
        String[] ids = registry.getWorkflowDefinitionComponents();
        for (String id: ids)
        {
            WorkflowDefinitionComponent component = registry.getWorkflowDefinitionComponent(id);
            definitions.addAll(component.getDefinitions());
        }
        return Collections.unmodifiableList(definitions);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#getDefinitionById(java.lang.String)
     */
    public WorkflowDefinition getDefinitionById(String workflowDefinitionId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowDefinitionId);
        WorkflowDefinitionComponent component = getWorkflowDefinitionComponent(engineId);
        return component.getDefinitionById(workflowDefinitionId);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#startWorkflow(java.lang.String, java.util.Map)
     */
    public WorkflowPath startWorkflow(String workflowDefinitionId, Map<QName, Serializable> parameters)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowDefinitionId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.startWorkflow(workflowDefinitionId, parameters);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#startWorkflowFromTemplate(org.alfresco.service.cmr.repository.NodeRef)
     */
    public WorkflowPath startWorkflowFromTemplate(NodeRef templateDefinition)
    {
        // TODO
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#getActiveWorkflows(java.lang.String)
     */
    public List<WorkflowInstance> getActiveWorkflows(String workflowDefinitionId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowDefinitionId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getActiveWorkflows(workflowDefinitionId);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#getWorkflowPaths(java.lang.String)
     */
    public List<WorkflowPath> getWorkflowPaths(String workflowId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getWorkflowPaths(workflowId);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#cancelWorkflow(java.lang.String)
     */
    public WorkflowInstance cancelWorkflow(String workflowId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.cancelWorkflow(workflowId);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#signal(java.lang.String, java.lang.String)
     */
    public WorkflowPath signal(String pathId, String transition)
    {
        String engineId = BPMEngineRegistry.getEngineId(pathId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.signal(pathId, transition);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#getTasksForWorkflowPath(java.lang.String)
     */
    public List<WorkflowTask> getTasksForWorkflowPath(String pathId)
    {
        String engineId = BPMEngineRegistry.getEngineId(pathId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getTasksForWorkflowPath(pathId);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#getAssignedTasks(java.lang.String, org.alfresco.service.cmr.workflow.WorkflowTaskState)
     */
    public List<WorkflowTask> getAssignedTasks(String authority, WorkflowTaskState state)
    {
        List<WorkflowTask> tasks = new ArrayList<WorkflowTask>(10);
        String[] ids = registry.getTaskComponents();
        for (String id: ids)
        {
            TaskComponent component = registry.getTaskComponent(id);
            tasks.addAll(component.getAssignedTasks(authority, state));
        }
        return Collections.unmodifiableList(tasks);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#getPooledTasks(java.lang.String)
     */
    public List<WorkflowTask> getPooledTasks(String authority)
    {
        // TODO: Expand authorities to include associated groups (and parent groups)
        List<String> authorities = new ArrayList<String>();
        authorities.add(authority);
        
        List<WorkflowTask> tasks = new ArrayList<WorkflowTask>(10);
        String[] ids = registry.getTaskComponents();
        for (String id: ids)
        {
            TaskComponent component = registry.getTaskComponent(id);
            tasks.addAll(component.getPooledTasks(authorities));
        }
        return Collections.unmodifiableList(tasks);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#updateTask(java.lang.String, java.util.Map, java.util.Map, java.util.Map)
     */
    public WorkflowTask updateTask(String taskId, Map<QName, Serializable> properties, Map<QName, List<NodeRef>> add, Map<QName, List<NodeRef>> remove)
    {
        String engineId = BPMEngineRegistry.getEngineId(taskId);
        TaskComponent component = getTaskComponent(engineId);
        return component.updateTask(taskId, properties, add, remove);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#endTask(java.lang.String, java.lang.String)
     */
    public WorkflowTask endTask(String taskId, String transition)
    {
        String engineId = BPMEngineRegistry.getEngineId(taskId);
        TaskComponent component = getTaskComponent(engineId);
        return component.endTask(taskId, transition);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#getTaskById(java.lang.String)
     */
    public WorkflowTask getTaskById(String taskId)
    {
        // TODO
        throw new UnsupportedOperationException();
    }

    
    /**
     * Gets the Workflow Definition Component registered against the specified BPM Engine Id
     * 
     * @param engineId  engine id
     */
    private WorkflowDefinitionComponent getWorkflowDefinitionComponent(String engineId)
    {
        WorkflowDefinitionComponent component = registry.getWorkflowDefinitionComponent(engineId);
        if (component == null)
        {
            throw new WorkflowException("Workflow Definition Component for engine id '" + engineId + "' is not registered");
        }
        return component;
    }
    
    /**
     * Gets the Workflow Component registered against the specified BPM Engine Id
     * 
     * @param engineId  engine id
     */
    private WorkflowComponent getWorkflowComponent(String engineId)
    {
        WorkflowComponent component = registry.getWorkflowComponent(engineId);
        if (component == null)
        {
            throw new WorkflowException("Workflow Component for engine id '" + engineId + "' is not registered");
        }
        return component;
    }

    /**
     * Gets the Task Component registered against the specified BPM Engine Id
     * 
     * @param engineId  engine id
     */
    private TaskComponent getTaskComponent(String engineId)
    {
        TaskComponent component = registry.getTaskComponent(engineId);
        if (component == null)
        {
            throw new WorkflowException("Task Component for engine id '" + engineId + "' is not registered");
        }
        return component;
    }
    
}
