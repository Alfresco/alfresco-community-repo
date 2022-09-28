/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.service.cmr.workflow;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


/**
 * Workflow Service.
 * 
 * Client facing API for interacting with Alfresco Workflows and Tasks.
 * 
 * @author davidc
 */
@AlfrescoPublicApi
public interface WorkflowService
{
    //
    // Workflow Definition Management
    //

    /**
     * Deploy a Workflow Definition to the Alfresco Repository
     * 
     * @param  engineId  the bpm engine id
     * @param  workflowDefinition  the workflow definition
     * @param  mimetype  the mimetype of the workflow definition
     * @return  workflow deployment descriptor
     */
    @Auditable(
            parameters = {"engineId", "workflowDefinition", "mimetype"},
            recordable = {true,       false,                true})
    public WorkflowDeployment deployDefinition(String engineId, InputStream workflowDefinition, String mimetype);
    
    /**
     * Deploy a Workflow Definition to the Alfresco Repository
     * 
     * @param  engineId  the bpm engine id
     * @param  workflowDefinition  the workflow definition
     * @param  mimetype  the mimetype of the workflow definition
     * @param  name  a name representing the deployment
     * @return  workflow deployment descriptor
     * @since 4.0
     */
    @Auditable(
            parameters = {"engineId", "workflowDefinition", "mimetype", "name"},
            recordable = {true,       false,                true,       true})
    public WorkflowDeployment deployDefinition(String engineId, InputStream workflowDefinition, String mimetype, String name);

    /**
     * Deploy a Workflow Definition to the Alfresco Repository
     *
     * @param  engineId  the bpm engine id
     * @param  workflowDefinition  the workflow definition
     * @param  mimetype  the mimetype of the workflow definition
     * @param  name  a name representing the deployment
     * @parm   fullAccess true if workflow should be considered secure (e.g., if it is deployed in classpath) and should have full access to the execution context,
     *                    false if it should be executed in a sandbox context (more restricted)
     * @return  workflow deployment descriptor
     * @since 4.0
     */
    @Auditable(
            parameters = {"engineId", "workflowDefinition", "mimetype", "name", "fullAccess"},
            recordable = {true,       false,                true,       true,   true})
    public WorkflowDeployment deployDefinition(String engineId, InputStream workflowDefinition, String mimetype, String name, boolean fullAccess);

    /**
     * Deploy a Workflow Definition to the Alfresco Repository
     * 
     * Note: The specified content object must be of type bpm:workflowdefinition.
     *       This type describes for which BPM engine the definition is appropriate. 
     * 
     * @param workflowDefinition  the content object containing the definition
     * @return  workflow deployment descriptor
     */
    @Auditable(parameters = {"workflowDefinition"})
    public WorkflowDeployment deployDefinition(NodeRef workflowDefinition);

    /**
     * Is the specified Workflow Definition already deployed?
     * 
     * Note: the notion of "already deployed" may differ between bpm engines. For example,
     *       different versions of the same process may be considered equal.
     *       
     * @param  workflowDefinition  the content object containing the definition
     * @return  true => already deployed
     */
    @Auditable(parameters = {"definitionContent"})
    public boolean isDefinitionDeployed(NodeRef workflowDefinition);
    
    /**
     * Is the specified Workflow Definition already deployed?
     * 
     * Note: the notion of "already deployed" may differ between bpm engines. For example,
     *       different versions of the same process may be considered equal.
     *       
     * @param  engineId  the bpm engine id
     * @param  workflowDefinition  the definition to check
     * @param  mimetype  the mimetype of the definition
     * @return  true => already deployed
     */
    @Auditable(
            parameters = {"engineId", "workflowDefinition", "mimetype"},
            recordable = {true,       false,                true})
    public boolean isDefinitionDeployed(String engineId, InputStream workflowDefinition, String mimetype);

    /**
     * Checks if the deployment for supplied workflow definition has the proper category
     *
     * @param  engineId  the bpm engine id
     * @param  workflowDefinition  the definition to check
     */
    @Auditable(
            parameters = {"engineId", "workflowDefinition"},
            recordable = {true,       false})
    public void checkDeploymentCategory(String engineId, InputStream workflowDefinition);
    
    /**
     * Undeploy an exisiting Workflow Definition
     * 
     * TODO: Determine behaviour when "in-flight" workflow instances exist
     *  
     * @param workflowDefinitionId  the id of the definition to undeploy
     */
    @Auditable(parameters = {"workflowDefinitionId"})
    public void undeployDefinition(String workflowDefinitionId);

    /**
     * Gets latest deployed Workflow Definitions
     * 
     * @return  the latest deployed workflow definitions
     */
    @Auditable
    public List<WorkflowDefinition> getDefinitions();
    
    /**
     * Gets all deployed Workflow Definitions (with all previous versions)
     * 
     * @return  the deployed (and previous) workflow definitions
     */
    @Auditable
    public List<WorkflowDefinition> getAllDefinitions();
        
    /**
     * Gets a Workflow Definition by unique Id
     * 
     * @param workflowDefinitionId  the workflow definition id
     * @return  the deployed workflow definition (or null if not found)
     */
    @Auditable(parameters = {"workflowDefinitionId"})
    public WorkflowDefinition getDefinitionById(String workflowDefinitionId);

    /**
     * Gets the latest Workflow Definition by unique name
     * 
     * @param workflowName  workflow name e.g. activiti$activitiReview
     * @return  the deployed workflow definition (or null if not found)
     */
    @Auditable(parameters = {"workflowName"})
    public WorkflowDefinition getDefinitionByName(String workflowName);

    /**
     * Gets all (including previous) Workflow Definitions for the given unique name
     * 
     * @param workflowName  workflow name e.g. activiti$activitiReview
     * @return  the deployed workflow definition (or null if not found)
     */
    @Auditable(parameters = {"workflowName"})
    public List<WorkflowDefinition> getAllDefinitionsByName(String workflowName);

    /**
     * Gets a graphical view of the Workflow Definition
     * 
     * @param workflowDefinitionId  the workflow definition id
     * @return  image view of the workflow definition
     */
    @Auditable(parameters = {"workflowDefinitionId"})
    public byte[] getDefinitionImage(String workflowDefinitionId);

    /**
     * Gets the Task Definitions for the given Workflow Definition
     * 
     * @param workflowDefinitionId  the workflow definition id
     * @return the deployed task definitions (or null if not found)
     */
    @Auditable(parameters = {"workflowDefinitionId"})
    public List<WorkflowTaskDefinition> getTaskDefinitions(final String workflowDefinitionId);
   
    
    //
    // Workflow Instance Management
    //
    

    /**
     * Start a Workflow Instance
     * 
     * @param workflowDefinitionId  the workflow definition id
     * @param parameters  the initial set of parameters used to populate the "Start Task" properties
     * @return  the initial workflow path
     */
    @Auditable(parameters = {"workflowDefinitionId", "parameters"})
    public WorkflowPath startWorkflow(String workflowDefinitionId, Map<QName, Serializable> parameters);

    /**
     * Start a Workflow Instance from an existing "Start Task" template node held in the
     * Repository.  The node must be of the Type as described in the Workflow Definition.
     * 
     * @param templateDefinition  the node representing the Start Task properties
     * @return  the initial workflow path
     */
    @Auditable(parameters = {"templateDefinition"})
    public WorkflowPath startWorkflowFromTemplate(NodeRef templateDefinition);
    
    /**
     * Gets all active workflow instances of the specified Workflow Definition
     * 
     * @param workflowDefinitionId  the workflow definition id
     * @return  the list of "in-flight" workflow instances
     */
    @Auditable(parameters = {"workflowDefinitionId"})
    public List<WorkflowInstance> getActiveWorkflows(String workflowDefinitionId);

    /**
     * Gets all completed workflow instances of the specified Workflow Definition
     * 
     * @param workflowDefinitionId  the workflow definition id
     * @return  the list of "in-flight" workflow instances
     * @since 3.4
     */
    @Auditable(parameters = {"workflowDefinitionId"})
    public List<WorkflowInstance> getCompletedWorkflows(String workflowDefinitionId);
    
    /**
     * Gets all workflow instances (both active and completed) of the specified Workflow Definition
     * 
     * @param workflowDefinitionId  the workflow definition id     
     * @return  the list of "in-flight" workflow instances
     * @since 3.4
     */
    @Auditable(parameters = {"workflowDefinitionId"})
    public List<WorkflowInstance> getWorkflows(String workflowDefinitionId);
    
    /**
     * Gets all "in-flight" workflow instances according to the specified workflowInstanceQuery parameter
     * 
     * @param workflowInstanceQuery WorkflowInstanceQuery
     */
    public List<WorkflowInstance> getWorkflows(WorkflowInstanceQuery workflowInstanceQuery);
    
    /**
     * Gets maxItems "in-flight" workflow instances according to the specified workflowInstanceQuery parameter
     * Get maxItems and skipCount parameters form request
     * 
     * @param workflowInstanceQuery WorkflowInstanceQuery
     * @param maxItems int
     * @param skipCount int
     * @return maxItems workflow instances
     */
    public List<WorkflowInstance> getWorkflows(WorkflowInstanceQuery workflowInstanceQuery, int maxItems, int skipCount);
    
    /**
     * Get count of workflow instances
     * 
     * @param workflowInstanceQuery WorkflowInstanceQuery
     * @return count of workflow instances
     */
    public long countWorkflows(WorkflowInstanceQuery workflowInstanceQuery);
    
    /**
     * Gets all active workflow instances.
     * 
     * @return  the list of "in-flight" workflow instances
     * @since 4.0
     */
    @Auditable
    public List<WorkflowInstance> getActiveWorkflows();
    
    /**
     * Gets all completed workflow instances.
     * 
     * @return  the list of "in-flight" workflow instances
     * @since 4.0
     */
    @Auditable
    public List<WorkflowInstance> getCompletedWorkflows();
    
    /**
     * Gets all workflow instances (both active and completed).
     * 
     * @return  the list of "in-flight" workflow instances
     * @since 4.0
     */
    public List<WorkflowInstance> getWorkflows();

    /**
     * Gets a specific workflow instances
     *
     * @param workflowId  the id of the workflow to retrieve
     * @return  the workflow instance (or null if not found)
     */
    @Auditable(parameters = {"workflowId"})
    public WorkflowInstance getWorkflowById(String workflowId);
    
    /**
     * Gets all Paths for the specified Workflow instance.
     * NOTE: It only returns information for an active Workflow instance.
     * 
     * @param workflowId  workflow instance id
     * @return  the list of workflow paths
     */
    @Auditable(parameters = {"workflowId"})
    public List<WorkflowPath> getWorkflowPaths(String workflowId);
    
    /**
     * Gets the properties associated with the specified path (and parent paths)
     * 
     * @param pathId  workflow path id
     * @return  map of path properties
     */
    @Auditable(parameters = {"pathId"})
    public Map<QName, Serializable> getPathProperties(String pathId); 

    /**
     * Cancel an "in-flight" Workflow instance
     * 
     * @param workflowId  the workflow instance to cancel
     * @return  an updated representation of the workflow instance
     */
    @Auditable(parameters = {"workflowId"})
    public WorkflowInstance cancelWorkflow(String workflowId);

    /**
     * Cancel a batch of "in-flight" Workflow instances
     * 
     * @param workflowIds  List of the workflow instances to cancel
     * @return List of updated representations of the workflow instances
     */
    @Auditable(parameters = {"workflowIds"})
    public List<WorkflowInstance> cancelWorkflows(List<String> workflowIds);

    /**
     * Delete a Workflow instance.
     * 
     * NOTE: This will force a delete, meaning that the workflow instance may not
     *       go through all the appropriate cancel events.
     * 
     * @param workflowId  the workflow instance to cancel
     * @return  an updated representation of the workflow instance
     */
    @Auditable(parameters = {"workflowId"})
    public WorkflowInstance deleteWorkflow(String workflowId);

    /**
     * Signal the transition from one Workflow Node to another
     * 
     * @param pathId  the workflow path to signal on
     * @param transitionId  the transition id to follow (or null, for the default transition)
     * @return  the updated workflow path
     */
    @Auditable(parameters = {"pathId", "transitionId"})
    public WorkflowPath signal(String pathId, String transitionId);

    /**
     * Fire custom event against specified path
     * 
     * @param pathId  the workflow path to fire event on
     * @param event  name of event
     * @return  workflow path (it may have been updated as a result of firing the event
     */
    @Auditable(parameters = {"pathId", "event"})
    public WorkflowPath fireEvent(String pathId, String event);
    
    /**
     * Gets all Tasks associated with the specified path
     * 
     * @param pathId  the path id
     * @return  the list of associated tasks
     */
    @Auditable(parameters = {"pathId"})
    public List<WorkflowTask> getTasksForWorkflowPath(String pathId);
    
    /**
     * Gets the start task instance for the given workflow instance.
     * 
     * @param workflowInstanceId String
     * @return WorkflowTask
     */
    @Auditable(parameters = {"pathId"})
    public WorkflowTask getStartTask(String workflowInstanceId);

    /**
     * Gets the start task instances for the given workflow instances.
     * 
     * @param sameSession indicates that the returned {@link WorkflowTask} elements will be used in
     *        the same session. If {@code true}, the returned List will be a lazy loaded list
     *        providing greater performance.
     */
    @Auditable(parameters = {"pathIds"})
    public List<WorkflowTask> getStartTasks(List<String> workflowInstanceIds, boolean sameSession);

    /**
     * Determines if a graphical view of the workflow instance exists
     * 
     * @param workflowInstanceId  the workflow instance id
     * @return true if there is a workflow instance diagram available
     * @since 4.0
     */
    @Auditable(parameters = {"workflowInstanceId"})
    public boolean hasWorkflowImage(String workflowInstanceId);
    
    /**
     * Gets a graphical view of the workflow instance
     * 
     * @param workflowInstanceId  the workflow instance id
     * @return image view of the workflow instance as an InputStream or null if a diagram is not available
     * @since 4.0
     */
    @Auditable(parameters = {"workflowInstanceId"}, recordReturnedObject=false)
    public InputStream getWorkflowImage(String workflowInstanceId);
    
    //
    // Workflow Timer Management
    //

    /**
     * Gets all active timers for the specified workflow
     * 
     * @return  the list of active timers
     */
    @Auditable(parameters = {"workflowId"})
    public List<WorkflowTimer> getTimers(String workflowId);

    
    //
    // Task Management
    //
    
    /**
     * Gets a Task by unique Id
     * 
     * @param taskId  the task id
     * @return  the task (or null, if not found)
     */
    @Auditable(parameters = {"taskId"})
    public WorkflowTask getTaskById(String taskId);
    
    /**
     * Gets all tasks assigned to the specified authority
     * 
     * @param authority  the authority
     * @param state  filter by specified workflow task state
     * @param lazyInitialization hint to the underlying workflow-engine to allow returning {@link WorkflowTask}s which
     * 		  aren't fully initialized but will be when the required values are required. If <code>true</code>, the
     * 		  returned enities should be used inside of the transaction-boundaries of this service-call.
     *        If <code>false</code>, fully initialized entities are returned, just as with {@link #getAssignedTasks(String, WorkflowTaskState)}.
     *        <br>It's a hint to the underlying workflow-engine and may be ignored by the actual implementation.
     * @return  the list of assigned tasks
     */
    @Auditable(parameters = {"authority", "state", "lazyInitialization"})
    public List<WorkflowTask> getAssignedTasks(String authority, WorkflowTaskState state, boolean lazyInitialization);
    
    /**
     * Gets all tasks assigned to the specified authority
     * 
     * @param authority  the authority
     * @param state  filter by specified workflow task state
     * @return  the list of assigned tasks
     */
    @Auditable(parameters = {"authority", "state", "lazy"})
    public List<WorkflowTask> getAssignedTasks(String authority, WorkflowTaskState state);
    
    /**
     * Gets the pooled tasks available to the specified authority
     * 
     * @param authority   the authority
     * @return  the list of pooled tasks
     */
    @Auditable(parameters = {"authority"})
    public List<WorkflowTask> getPooledTasks(String authority);
    /**
     * Gets the pooled tasks available to the specified authority
     * 
     * @param authority   the authority
     * @param lazyinitialization hint to the underlying workflow-engine to allow returning {@link WorkflowTask}s which
     * 		  aren't fully initialized but will be when the required values are required. If <code>true</code>, the
     * 		  returned enities should be used inside of the transaction-boundaries of this service-call.
     *        If <code>false</code>, fully initialized entities are returned, just as with {@link #getPooledTasks(String)}.
     *        <br>It's a hint to the underlying workflow-engine and may be ignored by the actual implementation.
     * @return  the list of pooled tasks
     */
    @Auditable(parameters = {"authority"})
    public List<WorkflowTask> getPooledTasks(String authority, boolean lazyinitialization);
    
    /**
     * @deprecated Use overloaded method with the {@code sameSession} parameter
     * (this method defaults the parameter to {@code false}).
     */ 
    public List<WorkflowTask> queryTasks(WorkflowTaskQuery query);

    /**
     * Query for tasks
     * 
     * @param query  the filter by which tasks are queried
     * @param sameSession indicates that the returned {@link WorkflowTask} elements will be used in
     *        the same session. If {@code true}, the returned List will be a lazy loaded list
     *        providing greater performance.
     * @return  the list of tasks matching the specified query
     */
    @Auditable(parameters = {"query"})
    public List<WorkflowTask> queryTasks(WorkflowTaskQuery query, boolean sameSession);
    
    /**
     * Get the number of tasks matching the given query
     * 
     * @param workflowTaskQuery the filter by which tasks are queried
     * @return count of matching tasks
     */
    public long countTasks(WorkflowTaskQuery workflowTaskQuery);
    
    /**
     * Update the Properties and Associations of a Task
     * 
     * @param taskId  the task id to update
     * @param properties  the map of properties to set on the task (or null, if none to set)
     * @param add  the map of items to associate with the task (or null, if none to add)
     * @param remove  the map of items to dis-associate with the task (or null, if none to remove)
     * @return  the update task
     */
    @Auditable(parameters = {"taskId", "properties", "add", "remove"})
    public WorkflowTask updateTask(String taskId, Map<QName, Serializable> properties, Map<QName, List<NodeRef>> add, Map<QName, List<NodeRef>> remove);
    
    /**
     * End the Task (i.e. complete the task)
     * 
     * @param taskId  the task id to end
     * @param transitionId  the task transition id to take on completion (or null, for the default transition)
     * @return  the updated task
     */
    @Auditable(parameters = {"taskId", "transitionId"})
    public WorkflowTask endTask(String taskId, String transitionId);
    
    /**
     * Determines if the given user can edit the given task
     * 
     * @param task The task to check
     * @param username The user to check
     * @return true if the user can edit the task
     * @since 3.4
     */
    @Auditable(parameters = {"task", "username"})
    public boolean isTaskEditable(WorkflowTask task, String username);
    
    /**
     * Determines if the given user can edit the given task
     * 
     * @param task The task to check
     * @param username The user to check
     * @param refreshTask Whether or not to refresh the {@link WorkflowTask} before check is perfromed
     * @return true if the user can edit the task
     * @since 4.1
     */
    @Auditable(parameters = {"task", "username", "refreshTask"})
    public boolean isTaskEditable(WorkflowTask task, String username, boolean refreshTask);
    
    /**
     * Determines if the given user can reassign the given task
     * 
     * @param task The task to check
     * @param username The user to check
     * @return true if the user can reassign the task
     * @since 3.4
     */
    @Auditable(parameters = {"task", "username"})
    public boolean isTaskReassignable(WorkflowTask task, String username);
    
    /**
     * Determines if the given user can reassign the given task
     * 
     * @param task The task to check
     * @param username The user to check
     * @param refreshTask Whether or not to refresh the {@link WorkflowTask} before check is perfromed
     * @return true if the user can reassign the task
     * @since 4.1
     */
    @Auditable(parameters = {"task", "username", "refreshTask"})
    public boolean isTaskReassignable(WorkflowTask task, String username, boolean refreshTask);
    
    /**
     * Determines if the given user can claim the given task
     * 
     * @param task The task to check
     * @param username The user to check
     * @return true if the user can claim the task
     * @since 3.4
     */
    @Auditable(parameters = {"task", "username"})
    public boolean isTaskClaimable(WorkflowTask task, String username);
    
    /**
     * Determines if the given user can claim the given task
     * 
     * @param task The task to check
     * @param username The user to check
     * @param refreshTask Whether or not to refresh the {@link WorkflowTask} before check is perfromed
     * @return true if the user can claim the task
     * @since 4.1
     */
    @Auditable(parameters = {"task", "username", "refreshTask"})
    public boolean isTaskClaimable(WorkflowTask task, String username, boolean refreshTask);
    
    /**
     * Determines if the given user can release the given task
     * 
     * @param task The task to check
     * @param username The user to check
     * @return true if the user can release the task
     * @since 3.4
     */
    @Auditable(parameters = {"task", "username"})
    public boolean isTaskReleasable(WorkflowTask task, String username);
    
    /**
     * Determines if the given user can release the given task
     * 
     * @param task The task to check
     * @param username The user to check
     * @param refreshTask Whether or not to refresh the {@link WorkflowTask} before check is perfromed
     * @return true if the user can release the task
     * @since 4.1
     */
    @Auditable(parameters = {"task", "username", "refreshTask"})
    public boolean isTaskReleasable(WorkflowTask task, String username, boolean refreshTask);
    
    //
    // Package Management
    //
    
    /**
     * Create a Workflow Package (a container of content to route through the Workflow).
     * 
     * If an existing container is supplied, it's supplemented with the workflow package aspect.
     * 
     * @param container  (optional) a pre-created container (e.g. folder, versioned folder or layered folder)
     * @return  the workflow package
     */
    @Auditable(parameters = {"container"})
    public NodeRef createPackage(NodeRef container);

    /**
     * Gets the Workflows that act upon the specified Repository content.
     *  
     * @param packageItem  the repository content item to get workflows for
     * @param active  true => active workflows only, false => completed workflows only
     * @return  list of workflows which act upon the specified content
     */
    @Auditable(parameters = {"packageItem", "active"})
    public List<WorkflowInstance> getWorkflowsForContent(NodeRef packageItem, boolean active);
    
    /**
     * Get a list of node refs to all the package contents for the given task id.
     * @param taskId - the task id
     * @return - A list of NodeRefs
     */
    @Auditable(parameters = {"packageItem", "active"})
    public List<NodeRef> getPackageContents(String taskId);
    
     /**
     * Get a list of node refs to all the package contents.
     * 
     * @param packageRef the nodeRef to the package
     * @return A list of nodeRefs the package is referring to
     */
    @Auditable(parameters = {"packageRef"})
    public List<NodeRef> getPackageContents(NodeRef packageRef);
    
    /**
     * @return true, if all workflows (in workflowDeployers) have a copy deployed per tenant and workflows
     * can be deployed in tenant. False when workflows are shared system-wide, regardless of the tenant context.
     */
    @Auditable
    public boolean isMultiTenantWorkflowDeploymentEnabled();
}
