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

package org.alfresco.repo.workflow;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.workflow.WorkflowAdminService;
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
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Function;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default Alfresco Workflow Service whose implementation is backed by
 * registered BPM Engine plug-in components.
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
    private AVMSyncService avmSyncService;
    private DictionaryService dictionaryService;
    private NodeService protectedNodeService;
    private ServiceRegistry services;
    private WorkflowAdminService workflowAdminService;
    
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
     * @param registry bpm engine registry
     */
    public void setBPMEngineRegistry(BPMEngineRegistry registry)
    {
        this.registry = registry;
    }

    /**
     * @param workflowAdminService the workflowAdminService to set
     */
    public void setWorkflowAdminService(WorkflowAdminService workflowAdminService)
    {
        this.workflowAdminService = workflowAdminService;
    }
    
    /**
     * Sets the Workflow Package Component
     * 
     * @param workflowPackageComponent workflow package component
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

    /**
     * Set the avm sync service
     * 
     * @param avmSyncService
     */
    public void setAvmSyncService(AVMSyncService avmSyncService)
    {
        this.avmSyncService = avmSyncService;
    }

    /**
     * Set the dictionary service
     * 
     * @param dictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Set the node service which applies permissions
     * 
     * @param protectedNodeService
     */
    public void setProtectedNodeService(NodeService protectedNodeService)
    {
        this.protectedNodeService = protectedNodeService;
    }
    
    /**
     * Set the service registry
     * 
     * @param services  service registry
     */
    public void setServices(ServiceRegistry services)
    {
        this.services = services;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#deployDefinition(java
     * .lang.String, java.io.InputStream, java.lang.String)
     */
    public WorkflowDeployment deployDefinition(String engineId, InputStream workflowDefinition, String mimetype)
    {
        return deployDefinition(engineId, workflowDefinition, mimetype, null);
    }
    
    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#deployDefinition(java
     * .lang.String, java.io.InputStream, java.lang.String, java.lang.String)
     */
    public WorkflowDeployment deployDefinition(String engineId, InputStream workflowDefinition, String mimetype, String name)
    {
        WorkflowComponent component = getWorkflowComponent(engineId);
        WorkflowDeployment deployment = component.deployDefinition(workflowDefinition, mimetype, name);

        if (logger.isDebugEnabled() && deployment.getProblems().length > 0)
        {
            for (String problem : deployment.getProblems())
            {
                logger.debug("Workflow definition '" + deployment.getDefinition().getTitle() + "' problem: " + problem);
            }
        }

        return deployment;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#isDefinitionDeployed
     * (org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isDefinitionDeployed(NodeRef workflowDefinition)
    {
        if (!nodeService.getType(workflowDefinition).equals(WorkflowModel.TYPE_WORKFLOW_DEF)) { throw new WorkflowException(
                    "Node " + workflowDefinition + " is not of type 'bpm:workflowDefinition'"); }

        String engineId = (String) nodeService.getProperty(workflowDefinition,
                    WorkflowModel.PROP_WORKFLOW_DEF_ENGINE_ID);
        ContentReader contentReader = contentService.getReader(workflowDefinition, ContentModel.PROP_CONTENT);

        return isDefinitionDeployed(engineId, contentReader.getContentInputStream(), contentReader.getMimetype());
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#isDefinitionDeployed
     * (java.lang.String, java.io.InputStream, java.lang.String)
     */
    public boolean isDefinitionDeployed(String engineId, InputStream workflowDefinition, String mimetype)
    {
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.isDefinitionDeployed(workflowDefinition, mimetype);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#deployDefinition(org
     * .alfresco.service.cmr.repository.NodeRef)
     */
    public WorkflowDeployment deployDefinition(NodeRef definitionContent)
    {
        if (!nodeService.getType(definitionContent).equals(WorkflowModel.TYPE_WORKFLOW_DEF)) { throw new WorkflowException(
                    "Node " + definitionContent + " is not of type 'bpm:workflowDefinition'"); }

        String engineId = (String) nodeService
                    .getProperty(definitionContent, WorkflowModel.PROP_WORKFLOW_DEF_ENGINE_ID);
        ContentReader contentReader = contentService.getReader(definitionContent, ContentModel.PROP_CONTENT);

        return deployDefinition(engineId, contentReader.getContentInputStream(), contentReader.getMimetype());
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#undeployDefinition(
     * java.lang.String)
     */
    public void undeployDefinition(String workflowDefinitionId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowDefinitionId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        component.undeployDefinition(workflowDefinitionId);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.workflow.WorkflowService#getDefinitions()
     */
    public List<WorkflowDefinition> getDefinitions()
    {
        List<WorkflowDefinition> definitions = new ArrayList<WorkflowDefinition>(10);
        String[] ids = registry.getWorkflowComponents();
        for (String id : ids)
        {
            if(workflowAdminService.isEngineVisible(id))
            {
                WorkflowComponent component = registry.getWorkflowComponent(id);
                definitions.addAll(component.getDefinitions());
            }
        }
        return Collections.unmodifiableList(definitions);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#getAllDefinitions()
     */
    public List<WorkflowDefinition> getAllDefinitions()
    {
        List<WorkflowDefinition> definitions = new ArrayList<WorkflowDefinition>(10);
        String[] ids = registry.getWorkflowComponents();
        for (String id : ids)
        {
            if(workflowAdminService.isEngineVisible(id))
            {
                WorkflowComponent component = registry.getWorkflowComponent(id);
                definitions.addAll(component.getAllDefinitions());
            }
        }
        return Collections.unmodifiableList(definitions);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#getDefinitionById(java
     * .lang.String)
     */
    public WorkflowDefinition getDefinitionById(String workflowDefinitionId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowDefinitionId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getDefinitionById(workflowDefinitionId);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#getDefinitionByName
     * (java.lang.String)
     */
    public WorkflowDefinition getDefinitionByName(String workflowName)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowName);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getDefinitionByName(workflowName);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#getAllDefinitionsByName
     * (java.lang.String)
     */
    public List<WorkflowDefinition> getAllDefinitionsByName(String workflowName)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowName);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getAllDefinitionsByName(workflowName);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#getDefinitionImage(
     * java.lang.String)
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

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#getAllTaskDefinitions
     * (java.lang.String)
     */
    public List<WorkflowTaskDefinition> getTaskDefinitions(final String workflowDefinitionId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowDefinitionId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getTaskDefinitions(workflowDefinitionId);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#startWorkflow(java.
     * lang.String, java.util.Map)
     */
    public WorkflowPath startWorkflow(String workflowDefinitionId, Map<QName, Serializable> parameters)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowDefinitionId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        WorkflowPath path = component.startWorkflow(workflowDefinitionId, parameters);
        if(parameters!=null && parameters.containsKey(WorkflowModel.ASSOC_PACKAGE))
        {
            WorkflowInstance instance = path.getInstance();
            workflowPackageComponent.setWorkflowForPackage(instance);
        }
        return path;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#startWorkflowFromTemplate
     * (org.alfresco.service.cmr.repository.NodeRef)
     */
    public WorkflowPath startWorkflowFromTemplate(NodeRef templateDefinition)
    {
        // TODO
        throw new UnsupportedOperationException();
    }

    /**
    * {@inheritDoc}
     */
    public List<WorkflowInstance> getActiveWorkflows(String workflowDefinitionId)
    {
        if(workflowDefinitionId==null)
        {
            return getActiveWorkflows();
        }
        String engineId = BPMEngineRegistry.getEngineId(workflowDefinitionId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getActiveWorkflows(workflowDefinitionId);
    }

    /**
    * {@inheritDoc}
     */
    public List<WorkflowInstance> getCompletedWorkflows(String workflowDefinitionId)
    {   
        if(workflowDefinitionId==null)
        {
            return getCompletedWorkflows();
        }

        String engineId = BPMEngineRegistry.getEngineId(workflowDefinitionId);
        WorkflowComponent component = getWorkflowComponent(engineId);        
        return component.getCompletedWorkflows(workflowDefinitionId);
    }
    
    /**
    * {@inheritDoc}
     */
    public List<WorkflowInstance> getWorkflows(String workflowDefinitionId)
    {
        if(workflowDefinitionId==null)
        {
            return getWorkflows();
        }
        String engineId = BPMEngineRegistry.getEngineId(workflowDefinitionId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getWorkflows(workflowDefinitionId);
    }

    /**
    * {@inheritDoc}
    */
    public List<WorkflowInstance> getActiveWorkflows()
    {
        List<String> ids = Arrays.asList(registry.getWorkflowComponents());
        return CollectionUtils.transformFlat(ids, new Function<String, Collection<WorkflowInstance>>()
        {
            public List<WorkflowInstance> apply(String id)
            {
                WorkflowComponent component = registry.getWorkflowComponent(id);
                return component.getActiveWorkflows();
            }
        });
    }

    /**
    * {@inheritDoc}
    */
    public List<WorkflowInstance> getCompletedWorkflows()
    {
        List<String> ids = Arrays.asList(registry.getWorkflowComponents());
        return CollectionUtils.transformFlat(ids, new Function<String, Collection<WorkflowInstance>>()
        {
            public List<WorkflowInstance> apply(String id)
            {
                WorkflowComponent component = registry.getWorkflowComponent(id);
                return component.getCompletedWorkflows();
            }
        });
    }

    /**
    * {@inheritDoc}
    */
    public List<WorkflowInstance> getWorkflows()
    {
        List<String> ids = Arrays.asList(registry.getWorkflowComponents());
        return CollectionUtils.transformFlat(ids, new Function<String, Collection<WorkflowInstance>>()
        {
            public List<WorkflowInstance> apply(String id)
            {
                WorkflowComponent component = registry.getWorkflowComponent(id);
                return component.getWorkflows();
            }
        });
    }
    
    /**
    * {@inheritDoc}
     */
    public WorkflowInstance getWorkflowById(String workflowId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getWorkflowById(workflowId);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#getWorkflowPaths(java
     * .lang.String)
     */
    public List<WorkflowPath> getWorkflowPaths(String workflowId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getWorkflowPaths(workflowId);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#getPathProperties(java
     * .lang.String)
     */
    public Map<QName, Serializable> getPathProperties(String pathId)
    {
        String engineId = BPMEngineRegistry.getEngineId(pathId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getPathProperties(pathId);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#cancelWorkflow(java
     * .lang.String)
     */
    public WorkflowInstance cancelWorkflow(String workflowId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        WorkflowInstance instance = component.cancelWorkflow(workflowId);
        // NOTE: Delete workflow package after cancelling workflow, so it's
        // still available
        // in process-end events of workflow definition
        workflowPackageComponent.deletePackage(instance.getWorkflowPackage());
        return instance;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#deleteWorkflow(java
     * .lang.String)
     */
    public WorkflowInstance deleteWorkflow(String workflowId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        WorkflowInstance instance = component.deleteWorkflow(workflowId);
        // NOTE: Delete workflow package after deleting workflow, so it's still
        // available
        // in process-end events of workflow definition
        workflowPackageComponent.deletePackage(instance.getWorkflowPackage());
        return instance;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#signal(java.lang.String
     * , java.lang.String)
     */
    public WorkflowPath signal(String pathId, String transition)
    {
        String engineId = BPMEngineRegistry.getEngineId(pathId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.signal(pathId, transition);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#fireEvent(java.lang
     * .String, java.lang.String)
     */
    public WorkflowPath fireEvent(String pathId, String event)
    {
        String engineId = BPMEngineRegistry.getEngineId(pathId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.fireEvent(pathId, event);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#getTimers(java.lang
     * .String)
     */
    public List<WorkflowTimer> getTimers(String workflowId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getTimers(workflowId);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#getTasksForWorkflowPath
     * (java.lang.String)
     */
    public List<WorkflowTask> getTasksForWorkflowPath(String pathId)
    {
        String engineId = BPMEngineRegistry.getEngineId(pathId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getTasksForWorkflowPath(pathId);
    }

    /**
    * {@inheritDoc}
    */
    
    @Override
    public WorkflowTask getStartTask(String workflowInstanceId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowInstanceId);
        TaskComponent component = getTaskComponent(engineId);
        return component.getStartTask(workflowInstanceId);
    }
    
    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#hasWorkflowImage(
     * java.lang.String)
     */
    public boolean hasWorkflowImage(String workflowInstanceId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowInstanceId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.hasWorkflowImage(workflowInstanceId);
    }
    
    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#getWorkflowImage(
     * java.lang.String)
     */
    public InputStream getWorkflowImage(String workflowInstanceId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowInstanceId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getWorkflowImage(workflowInstanceId);
    }
    
    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#getAssignedTasks(java
     * .lang.String, org.alfresco.service.cmr.workflow.WorkflowTaskState)
     */
    public List<WorkflowTask> getAssignedTasks(String authority, WorkflowTaskState state)
    {
        List<WorkflowTask> tasks = new ArrayList<WorkflowTask>(10);
        String[] ids = registry.getTaskComponents();
        for (String id : ids)
        {
            TaskComponent component = registry.getTaskComponent(id);
            tasks.addAll(component.getAssignedTasks(authority, state));
        }
        return Collections.unmodifiableList(tasks);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#getPooledTasks(java
     * .lang.String)
     */
    public List<WorkflowTask> getPooledTasks(String authority)
    {
        // Expand authorities to include associated groups (and parent groups)
        List<String> authorities = new ArrayList<String>();
        authorities.add(authority);
        Set<String> parents = authorityService.getContainingAuthoritiesInZone(AuthorityType.GROUP, authority,
                null, null, 100);
        authorities.addAll(parents);
        
        // Retrieve pooled tasks for authorities (from each of the registered
        // task components)
        List<WorkflowTask> tasks = new ArrayList<WorkflowTask>(10);
        String[] ids = registry.getTaskComponents();
        for (String id : ids)
        {
            TaskComponent component = registry.getTaskComponent(id);
            tasks.addAll(component.getPooledTasks(authorities));
        }
        return Collections.unmodifiableList(tasks);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#queryTasks(org.alfresco
     * .service.cmr.workflow.WorkflowTaskFilter)
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
            if (engineId != null && !engineId.equals(taskEngineId)) { throw new WorkflowException(
                        "Cannot query for tasks across multiple task components: " + engineId + ", " + taskEngineId); }
            engineId = taskEngineId;
        }

        // perform query
        List<WorkflowTask> tasks = new ArrayList<WorkflowTask>(10);
        String[] ids = registry.getTaskComponents();
        for (String id : ids)
        {
            TaskComponent component = registry.getTaskComponent(id);
            // NOTE: don't bother asking task component if specific task or
            // process id
            // are in the filter and do not correspond to the component
            if (engineId != null && !engineId.equals(id))
            {
                continue;
            }
            tasks.addAll(component.queryTasks(query));
        }
        return Collections.unmodifiableList(tasks);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#updateTask(java.lang
     * .String, java.util.Map, java.util.Map, java.util.Map)
     */
    public WorkflowTask updateTask(String taskId, Map<QName, Serializable> properties, Map<QName, List<NodeRef>> add,
                Map<QName, List<NodeRef>> remove)
    {
        String engineId = BPMEngineRegistry.getEngineId(taskId);
        TaskComponent component = getTaskComponent(engineId);
        // get the current assignee before updating the task
        String originalAsignee = (String)component.getTaskById(taskId).getProperties().get(ContentModel.PROP_OWNER);
        WorkflowTask task = component.updateTask(taskId, properties, add, remove);
        if (add != null && add.containsKey(WorkflowModel.ASSOC_PACKAGE))
        {
            WorkflowInstance instance = task.getPath().getInstance();
            workflowPackageComponent.setWorkflowForPackage(instance);
        }
        
        // Get the 'new' assignee
        String assignee = (String)properties.get(ContentModel.PROP_OWNER);
        if (assignee != null && assignee.length() != 0)
        {
            // if the assignee has changed get the start task
            if (!assignee.equals(originalAsignee))
            {
                String instanceId = task.getPath().getInstance().getId();
                WorkflowTask startTask = component.getStartTask(instanceId);
                
                if (startTask != null)
                {
                    // Get the email notification flag
                    Boolean sendEMailNotification = (Boolean) startTask.getProperties().get(WorkflowModel.PROP_SEND_EMAIL_NOTIFICATIONS);
                    if (Boolean.TRUE.equals(sendEMailNotification) == true)
                    {
                        // Send the notification
                        WorkflowNotificationUtils.sendWorkflowAssignedNotificationEMail(
                                    services, 
                                    taskId,
                                    assignee,
                                    false);
                    }
                }
            }
        }
        
        return task;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#endTask(java.lang.String
     * , java.lang.String)
     */
    public WorkflowTask endTask(String taskId, String transition)
    {
        String engineId = BPMEngineRegistry.getEngineId(taskId);
        TaskComponent component = getTaskComponent(engineId);
        return component.endTask(taskId, transition);
    }

    /*
     * @see org.alfresco.service.cmr.workflow.WorkflowService#isTaskEditable(org.alfresco.service.cmr.workflow.WorkflowTask, java.lang.String)
     */
    public boolean isTaskEditable(WorkflowTask task, String username)
    {
        task = getTaskById(task.getId()); // Refresh the task.
        
        // if the task is complete it is not editable
        if (task.getState() == WorkflowTaskState.COMPLETED)
        {
            return false;
        }

        if (isUserOwnerOrInitiator(task, username))
        {
            // editable if the current user is the task owner or initiator
            return true;
        }
        
        if (task.getProperties().get(ContentModel.PROP_OWNER) == null)
        {
            // if the user is not the owner or initiator check whether they are
            // a member of the pooled actors for the task (if it has any)
            return isUserInPooledActors(task, username);
        }
        else
        {
            // if the task has an owner and the user is not the owner
            // or the initiator do not allow editing
            return false;
        }
    }
    
    /*
     * @see org.alfresco.service.cmr.workflow.WorkflowService#isTaskReassignable(org.alfresco.service.cmr.workflow.WorkflowTask, java.lang.String)
     */
    public boolean isTaskReassignable(WorkflowTask task, String username)
    {
        task = getTaskById(task.getId()); // Refresh the task.
        
        // if the task is complete it is not reassignable
        if (task.getState() == WorkflowTaskState.COMPLETED)
        {
            return false;
        }
        
        // if a task does not have an owner it can not be reassigned
        if (task.getProperties().get(ContentModel.PROP_OWNER) == null)
        {
            return false;
        }
        
        // if the task has the 'reassignable' property set to false it can not be reassigned
        Map<QName, Serializable> properties = task.getProperties();
        Boolean reassignable = (Boolean)properties.get(WorkflowModel.PROP_REASSIGNABLE);
        if (reassignable != null && reassignable.booleanValue() == false)
        {
            return false;
        }
        
        // if the task has pooled actors and an owner it can not be reassigned (it must be released)
        Collection<?> actors = (Collection<?>) properties.get(WorkflowModel.ASSOC_POOLED_ACTORS);
        String owner = (String)properties.get(ContentModel.PROP_OWNER);
        if (actors != null && !actors.isEmpty() && owner != null)
        {
            return false;
        }

        if (isUserOwnerOrInitiator(task, username))
        {
            // reassignable if the current user is the task owner or initiator
            return true;
        }
        
        return false;
    }
    
    /*
     * @see org.alfresco.service.cmr.workflow.WorkflowService#isTaskClaimable(org.alfresco.service.cmr.workflow.WorkflowTask, java.lang.String)
     */
    public boolean isTaskClaimable(WorkflowTask task, String username)
    {
        task = getTaskById(task.getId()); // Refresh the task.
        
        // if the task is complete it is not claimable
        if (task.getState() == WorkflowTaskState.COMPLETED)
        {
            return false;
        }
        
        // if the task has an owner it can not be claimed
        if (task.getProperties().get(ContentModel.PROP_OWNER) != null)
        {
            return false;
        }
        
        // a task can only be claimed if the user is a member of
        // of the pooled actors for the task
        return isUserInPooledActors(task, username);
    }
    
    /*
     * @see org.alfresco.service.cmr.workflow.WorkflowService#isTaskReleasable(org.alfresco.service.cmr.workflow.WorkflowTask, java.lang.String)
     */
    public boolean isTaskReleasable(WorkflowTask task, String username)
    {
        task = getTaskById(task.getId()); // Refresh the task.
        
        // if the task is complete it is not releasable
        if (task.getState() == WorkflowTaskState.COMPLETED)
        {
            return false;
        }
        
        // if the task doesn't have pooled actors it is not releasable
        Map<QName, Serializable> properties = task.getProperties();
        Collection<?> actors = (Collection<?>) properties.get(WorkflowModel.ASSOC_POOLED_ACTORS);
        if (actors == null || actors.isEmpty())
        {
            return false;
        }
        
        // if the task does not have an owner it is not releasable
        String owner = (String)properties.get(ContentModel.PROP_OWNER);
        if (owner == null)
        {
            return false;
        }

        if (isUserOwnerOrInitiator(task, username))
        {
            // releasable if the current user is the task owner or initiator
            return true;
        }
        
        return false;
    }
    
    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#getTaskById(java.lang
     * .String)
     */
    public WorkflowTask getTaskById(String taskId)
    {
        String engineId = BPMEngineRegistry.getEngineId(taskId);
        TaskComponent component = getTaskComponent(engineId);
        return component.getTaskById(taskId);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#createPackage(java.
     * lang.String, org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef createPackage(NodeRef container)
    {
        return workflowPackageComponent.createPackage(container);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.workflow.WorkflowService#getWorkflowsForContent
     * (org.alfresco.service.cmr.repository.NodeRef, boolean)
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
            if (instance != null && instance.isActive() == active)
            {
                workflowInstances.add(instance);
            }
        }
        return workflowInstances;
    }

    /**
     * Gets the Workflow Component registered against the specified BPM Engine
     * Id
     * 
     * @param engineId engine id
     */
    private WorkflowComponent getWorkflowComponent(String engineId)
    {
        WorkflowComponent component = registry.getWorkflowComponent(engineId);
        if (component == null) { throw new WorkflowException("Workflow Component for engine id '" + engineId
                    + "' is not registered"); }
        return component;
    }

    /**
     * Gets the Task Component registered against the specified BPM Engine Id
     * 
     * @param engineId engine id
     */
    private TaskComponent getTaskComponent(String engineId)
    {
        TaskComponent component = registry.getTaskComponent(engineId);
        if (component == null) { throw new WorkflowException("Task Component for engine id '" + engineId
                    + "' is not registered"); }
        return component;
    }

    public List<NodeRef> getPackageContents(String taskId)
    {
        NodeRef workflowPackage = getWorkflowPackageIfExists(taskId);
        if (workflowPackage == null)
        {
            return Collections.emptyList();
        }
        else if (workflowPackage.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_AVM))
        {
            return getAvmPackageContents(workflowPackage);
        }
        else
        {
            return getRepositoryPackageContents(workflowPackage);
        }
    }

    /**
     * Attempts to get the workflow package node from the workflow task
     * specified by the task Id. If the task Id is invalid or no workflow
     * package is associated with the specified task then this method returns
     * null.
     * 
     * @param taskId
     * @return The workflow package NodeRef or null.
     */
    private NodeRef getWorkflowPackageIfExists(String taskId)
    {
        WorkflowTask workflowTask = getTaskById(taskId);
        if (workflowTask != null) { return (NodeRef) workflowTask.getProperties().get(WorkflowModel.ASSOC_PACKAGE); }
        return null;
    }

    /**
     * @param contents
     * @param workflowPackage
     */
    private List<NodeRef> getRepositoryPackageContents(NodeRef workflowPackage)
    {
        List<NodeRef> contents = new ArrayList<NodeRef>();
        // get existing workflow package items
        List<ChildAssociationRef> packageAssocs = protectedNodeService.getChildAssocs(workflowPackage);
        for (ChildAssociationRef assoc : packageAssocs)
        {
            // create our Node representation from the NodeRef
            NodeRef nodeRef = assoc.getChildRef();
            QName assocType = assoc.getTypeQName();
            if (!protectedNodeService.exists(nodeRef))
            {
                if (logger.isDebugEnabled())
                    logger.debug("Ignoring " + nodeRef + " as it has been removed from the repository");
            }
            else if (!ContentModel.ASSOC_CONTAINS.equals(assocType) && !WorkflowModel.ASSOC_PACKAGE_CONTAINS.equals(assocType))
            {
                if (logger.isDebugEnabled())
                    logger.debug("Ignoring " + nodeRef + " as it has an invalid association type: "+assocType);
            }
            else
            {
                if (checkTypeIsInDataDictionary(nodeRef))
                {
                    contents.add(nodeRef);
                }
            }
        }
        return contents;
    }

    /**
     * Gets teh type of the nodeRef and checks that the type exists in the data
     * dcitionary. If the type is not in the data dictionary then the method
     * logs a warning and returns false. Otherwise it returns true.
     * 
     * @param nodeRef
     * @return True if the nodeRef type is in the data dictionary, otherwise
     *         false.
     */
    private boolean checkTypeIsInDataDictionary(NodeRef nodeRef)
    {
        QName type = protectedNodeService.getType(nodeRef);
        if (dictionaryService.getType(type) == null)
        {
            if (logger.isWarnEnabled())
                logger.warn("Found invalid object in database: id = " + nodeRef + ", type = " + type);
            return false;
        }
        return true;
    }

    /**
     * @param contents
     * @param workflowPackage
     */
    private List<NodeRef> getAvmPackageContents(NodeRef workflowPackage)
    {
        List<NodeRef> contents = new ArrayList<NodeRef>();
        if (protectedNodeService.exists(workflowPackage))
        {
            final NodeRef stagingNodeRef = (NodeRef) protectedNodeService.getProperty(workflowPackage,
                        WCMModel.PROP_AVM_DIR_INDIRECTION);
            final String stagingAvmPath = AVMNodeConverter.ToAVMVersionPath(stagingNodeRef).getSecond();
            final String packageAvmPath = AVMNodeConverter.ToAVMVersionPath(workflowPackage).getSecond();
            if (logger.isDebugEnabled())
                logger.debug("comparing " + packageAvmPath + " with " + stagingAvmPath);
            for (AVMDifference d : avmSyncService.compare(-1, packageAvmPath, -1, stagingAvmPath, null))
            {
                if (logger.isDebugEnabled())
                    logger.debug("got difference " + d);
                if (d.getDifferenceCode() == AVMDifference.NEWER || d.getDifferenceCode() == AVMDifference.CONFLICT)
                {
                    contents.add(AVMNodeConverter.ToNodeRef(d.getSourceVersion(), d.getSourcePath()));
                }
            }
        }
        return contents;
    }
    
    /**
     * Determines if the given user is a member of the pooled actors assigned to the task
     * 
     * @param task The task instance to check
     * @param username The username to check
     * @return true if the user is a pooled actor, false otherwise
     */
    @SuppressWarnings("unchecked")
    private boolean isUserInPooledActors(WorkflowTask task, String username)
    {
        // Get the pooled actors
        Collection<NodeRef> actors = (Collection<NodeRef>)task.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS);
        if (actors != null)
        {
            for (NodeRef actor : actors)
            {
                QName type = nodeService.getType(actor);
                if (dictionaryService.isSubClass(type, ContentModel.TYPE_PERSON))
                {
                    Serializable name = nodeService.getProperty(actor, ContentModel.PROP_USERNAME);
                    if(name!=null && name.equals(username))
                    {
                        return true;
                    }
                }
                else if (dictionaryService.isSubClass(type, ContentModel.TYPE_AUTHORITY_CONTAINER))
                {
                    if (isUserInGroup(username, actor))
                    {
                        // The user is a member of the group
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isUserInGroup(String username, NodeRef group)
    {
        // Get the group name
        String name = (String)nodeService.getProperty(group, ContentModel.PROP_AUTHORITY_NAME);

        // Get all group members
        Set<String> groupMembers = authorityService.getContainedAuthorities(AuthorityType.USER, name, false);

        // Chekc if the user is a group member.
        return groupMembers != null && groupMembers.contains(username);
    }
    
    /**
     * Determines if the given user is the owner of the given task or
     * the initiator of the workflow the task is part of
     * 
     * @param task The task to check
     * @param username The username to check
     * @return true if the user is the owner or the workflow initiator
     */
    private boolean isUserOwnerOrInitiator(WorkflowTask task, String username)
    {
        boolean result = false;
        String owner = (String)task.getProperties().get(ContentModel.PROP_OWNER);

        if (username.equals(owner))
        {
            // user owns the task
            result = true;
        }
        else if (username.equals(getWorkflowInitiatorUsername(task)))
        {
            // user is the workflow initiator
            result = true;
        }
        
        return result;
    }
    
    /**
     * Returns the username of the user that initiated the workflow the
     * given task is part of.
     * 
     * @param task The task to get the workflow initiator for
     * @return Username or null if the initiator could not be found
     */
    private String getWorkflowInitiatorUsername(WorkflowTask task)
    {
        String initiator = null;
        
        NodeRef initiatorRef = task.getPath().getInstance().getInitiator();
        
        if (initiatorRef != null && this.nodeService.exists(initiatorRef))
        {
            initiator = (String)this.nodeService.getProperty(initiatorRef, ContentModel.PROP_USERNAME);
        }
        
        return initiator;
    }
}
