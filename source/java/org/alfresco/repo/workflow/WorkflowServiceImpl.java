/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.workflow;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTimer;
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
    private AuthorityService authorityService;
    private BPMEngineRegistry registry;
    private WorkflowPackageComponent workflowPackageComponent;
    private NodeService nodeService;
    private ContentService contentService;

    
    /**
     * Sets the Authority Service
     * 
     * @param authorityService
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

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
    
    /**
     * Sets the Node Service
     * 
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Sets the Content Service
     * 
     * @param contentService
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
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
     * @see org.alfresco.service.cmr.workflow.WorkflowService#isDefinitionDeployed(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isDefinitionDeployed(NodeRef workflowDefinition)
    {
        if (! nodeService.getType(workflowDefinition).equals(WorkflowModel.TYPE_WORKFLOW_DEF))
        {
            throw new WorkflowException("Node " + workflowDefinition + " is not of type 'bpm:workflowDefinition'");
        }
        
        String engineId = (String)nodeService.getProperty(workflowDefinition, WorkflowModel.PROP_WORKFLOW_DEF_ENGINE_ID);
        ContentReader contentReader = contentService.getReader(workflowDefinition, ContentModel.PROP_CONTENT);

        return isDefinitionDeployed(engineId, contentReader.getContentInputStream(), contentReader.getMimetype());
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
        if (! nodeService.getType(definitionContent).equals(WorkflowModel.TYPE_WORKFLOW_DEF))
        {
            throw new WorkflowException("Node " + definitionContent + " is not of type 'bpm:workflowDefinition'");
        }
        
        String engineId = (String)nodeService.getProperty(definitionContent, WorkflowModel.PROP_WORKFLOW_DEF_ENGINE_ID);
        ContentReader contentReader = contentService.getReader(definitionContent, ContentModel.PROP_CONTENT);

        return deployDefinition(engineId, contentReader.getContentInputStream(), contentReader.getMimetype());
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
     * @see org.alfresco.service.cmr.workflow.WorkflowService#getAllDefinitions()
     */
    public List<WorkflowDefinition> getAllDefinitions()
    {
        List<WorkflowDefinition> definitions = new ArrayList<WorkflowDefinition>(10);
        String[] ids = registry.getWorkflowComponents();
        for (String id: ids)
        {
            WorkflowComponent component = registry.getWorkflowComponent(id);
            definitions.addAll(component.getAllDefinitions());
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
     * @see org.alfresco.service.cmr.workflow.WorkflowService#getAllDefinitionsByName(java.lang.String)
     */
    public List<WorkflowDefinition> getAllDefinitionsByName(String workflowName)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowName);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getAllDefinitionsByName(workflowName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#getDefinitionImage(java.lang.String)
     */
    public byte[] getDefinitionImage(String workflowDefinitionId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowDefinitionId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        byte[] definitionImage = component.getDefinitionImage(workflowDefinitionId);
        if (definitionImage == null)
        {
            definitionImage = new byte[0];
        }
        return definitionImage;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#getAllTaskDefinitions(java.lang.String)
     */
    public List<WorkflowTaskDefinition> getTaskDefinitions(final String workflowDefinitionId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowDefinitionId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getTaskDefinitions(workflowDefinitionId);
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
     * @see org.alfresco.service.cmr.workflow.WorkflowService#getPathProperties(java.lang.String)
     */
    public Map<QName, Serializable> getPathProperties(String pathId)
    {
        String engineId = BPMEngineRegistry.getEngineId(pathId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getPathProperties(pathId);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#cancelWorkflow(java.lang.String)
     */
    public WorkflowInstance cancelWorkflow(String workflowId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        WorkflowInstance instance = component.cancelWorkflow(workflowId);
        // NOTE: Delete workflow package after cancelling workflow, so it's still available
        //       in process-end events of workflow definition
        workflowPackageComponent.deletePackage(instance.workflowPackage);
        return instance;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#deleteWorkflow(java.lang.String)
     */
    public WorkflowInstance deleteWorkflow(String workflowId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        WorkflowInstance instance = component.deleteWorkflow(workflowId);
        // NOTE: Delete workflow package after deleting workflow, so it's still available
        //       in process-end events of workflow definition
        workflowPackageComponent.deletePackage(instance.workflowPackage);
        return instance;
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
     * @see org.alfresco.service.cmr.workflow.WorkflowService#fireEvent(java.lang.String, java.lang.String)
     */
    public WorkflowPath fireEvent(String pathId, String event)
    {
        String engineId = BPMEngineRegistry.getEngineId(pathId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.fireEvent(pathId, event);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#getTimers(java.lang.String)
     */
    public List<WorkflowTimer> getTimers(String workflowId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getTimers(workflowId);
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
        // Expand authorities to include associated groups (and parent groups)
        List<String> authorities = new ArrayList<String>();
        authorities.add(authority);
        Set<String> parents = authorityService.getContainingAuthorities(AuthorityType.GROUP, authority, false);
        authorities.addAll(parents);

        // Retrieve pooled tasks for authorities (from each of the registered task components)
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
     * @see org.alfresco.service.cmr.workflow.WorkflowService#queryTasks(org.alfresco.service.cmr.workflow.WorkflowTaskFilter)
     */
    public List<WorkflowTask> queryTasks(WorkflowTaskQuery query)
    {
        // extract task component to perform query
        String engineId = null;
        String processId = query.getProcessId();
        if (processId != null)
        {
            engineId = BPMEngineRegistry.getEngineId(processId);
        }
        String taskId = query.getTaskId();
        if (taskId != null)
        {
            String taskEngineId = BPMEngineRegistry.getEngineId(taskId);
            if (engineId != null && !engineId.equals(taskEngineId))
            {
                throw new WorkflowException("Cannot query for tasks across multiple task components: " + engineId + ", " + taskEngineId);
            }
            engineId = taskEngineId; 
        }
        
        // perform query
        List<WorkflowTask> tasks = new ArrayList<WorkflowTask>(10);
        String[] ids = registry.getTaskComponents();
        for (String id: ids)
        {
            TaskComponent component = registry.getTaskComponent(id);
            // NOTE: don't bother asking task component if specific task or process id
            //       are in the filter and do not correspond to the component
            if (engineId != null && !engineId.equals(id))
            {
                continue;
            }
            tasks.addAll(component.queryTasks(query));
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
            if (instance != null && instance.active == active)
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
