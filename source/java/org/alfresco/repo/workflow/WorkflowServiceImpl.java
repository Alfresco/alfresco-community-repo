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

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Default Alfresco Workflow Service whose implementation is backed by registered 
 * BPM Engine plug-in components.
 * 
 * @author davidc
 */
public class WorkflowServiceImpl implements WorkflowService
{
    // Logging support
    private static Log logger = LogFactory.getLog("org.alfresco.repo.workflow");

    // Dependent services
    private BPMEngineRegistry registry;
    private WorkflowPackageComponent workflowPackageComponent;
    

    /**
     * Sets the BPM Engine Registry
     * 
     * @param registry  bpm engine registry
     */
    public void setBPMEngineRegistry(BPMEngineRegistry registry)
    {
        this.registry = registry;
    }
    
    /**
     * Sets the Workflow Package Component
     * 
     * @param workflowPackage  workflow package component
     */
    public void setWorkflowPackageComponent(WorkflowPackageComponent workflowPackageComponent)
    {
        this.workflowPackageComponent = workflowPackageComponent;
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#deployDefinition(java.lang.String, java.io.InputStream, java.lang.String)
     */
    public WorkflowDeployment deployDefinition(String engineId, InputStream workflowDefinition, String mimetype)
    {
        WorkflowComponent component = getWorkflowComponent(engineId);
        WorkflowDeployment deployment = component.deployDefinition(workflowDefinition, mimetype);
        
        if (logger.isDebugEnabled() && deployment.problems.length > 0)
        {
            for (String problem : deployment.problems)
            {
                logger.debug("Workflow definition '" + deployment.definition.title + "' problem: " + problem);
            }
        }
        
        return deployment;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#isDefinitionDeployed(java.lang.String, java.io.InputStream, java.lang.String)
     */
    public boolean isDefinitionDeployed(String engineId, InputStream workflowDefinition, String mimetype)
    {
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.isDefinitionDeployed(workflowDefinition, mimetype);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#deployDefinition(org.alfresco.service.cmr.repository.NodeRef)
     */
    public WorkflowDeployment deployDefinition(NodeRef definitionContent)
    {
        // TODO
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#undeployDefinition(java.lang.String)
     */
    public void undeployDefinition(String workflowDefinitionId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowDefinitionId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        component.undeployDefinition(workflowDefinitionId);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#getDefinitions()
     */
    public List<WorkflowDefinition> getDefinitions()
    {
        List<WorkflowDefinition> definitions = new ArrayList<WorkflowDefinition>(10);
        String[] ids = registry.getWorkflowComponents();
        for (String id: ids)
        {
            WorkflowComponent component = registry.getWorkflowComponent(id);
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
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getDefinitionById(workflowDefinitionId);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#getDefinitionByName(java.lang.String)
     */
    public WorkflowDefinition getDefinitionByName(String workflowName)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowName);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getDefinitionByName(workflowName);
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
     * @see org.alfresco.service.cmr.workflow.WorkflowService#getWorkflowById(java.lang.String)
     */
    public WorkflowInstance getWorkflowById(String workflowId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getWorkflowById(workflowId);
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
        String engineId = BPMEngineRegistry.getEngineId(taskId);
        TaskComponent component = getTaskComponent(engineId);
        return component.getTaskById(taskId);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#createPackage(java.lang.String, org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef createPackage(NodeRef container)
    {
        return workflowPackageComponent.createPackage(container);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#getWorkflowsForContent(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    public List<WorkflowInstance> getWorkflowsForContent(NodeRef packageItem, boolean active)
    {
        List<String> workflowIds = workflowPackageComponent.getWorkflowIdsForContent(packageItem);
        List<WorkflowInstance> workflowInstances = new ArrayList<WorkflowInstance>(workflowIds.size());
        for (String workflowId : workflowIds)
        {
            String engineId = BPMEngineRegistry.getEngineId(workflowId);
            WorkflowComponent component = getWorkflowComponent(engineId);
            WorkflowInstance instance = component.getWorkflowById(workflowId);
            if (instance.active == active)
            {
                workflowInstances.add(instance);
            }
        }
        return workflowInstances;
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
