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
package org.alfresco.service.cmr.workflow;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


/**
 * Workflow Service.
 * 
 * Client facing API for interacting with Alfresco Workflows and Tasks.
 * 
 * @author davidc
 */
@PublicService
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
    @Auditable(parameters = {"engineId", "workflowDefinition", "mimetype"})
    public WorkflowDeployment deployDefinition(String engineId, InputStream workflowDefinition, String mimetype);
    
    /**
     * Deploy a Workflow Definition to the Alfresco Repository
     * 
     * Note: The specified content object must be of type bpm:workflowdefinition.
     *       This type describes for which BPM engine the definition is appropriate. 
     * 
     * @param workflowDefinition  the content object containing the definition
     * @return  workflow deployment descriptor
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"workflowDefinition"})
    public WorkflowDeployment deployDefinition(NodeRef workflowDefinition);

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
    @Auditable(parameters = {"engineId", "workflowDefinition", "mimetype"})
    public boolean isDefinitionDeployed(String engineId, InputStream workflowDefinition, String mimetype);
    
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
     * Gets all deployed Workflow Definitions
     * 
     * @return  the deployed workflow definitions
     */
    @Auditable
    public List<WorkflowDefinition> getDefinitions();
    
    /**
     * Gets a Workflow Definition by unique Id
     * 
     * @param workflowDefinitionId  the workflow definition id
     * @return  the deployed workflow definition
     */
    @Auditable(parameters = {"workflowDefinitionId"})
    public WorkflowDefinition getDefinitionById(String workflowDefinitionId);

    /**
     * Gets a Workflow Definition by unique name
     * 
     * @param workflowName  workflow name e.g. jbpm://review
     * @return  the deployed workflow definition
     */
    @Auditable(parameters = {"workflowName"})
    public WorkflowDefinition getDefinitionByName(String workflowName);

    
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
     * Gets all "in-flight" workflow instances of the specified Workflow Definition
     * 
     * @param workflowDefinitionId  the workflow definition id
     * @return  the list of "in-fligth" workflow instances
     */
    @Auditable(parameters = {"workflowDefinitionId"})
    public List<WorkflowInstance> getActiveWorkflows(String workflowDefinitionId);

    /**
     * Gets a specific workflow instances
     *
     * @param workflowId  the id of the workflow to retrieve
     * @return  the workflow instance 
     */
    @Auditable(parameters = {"workflowId"})
    public WorkflowInstance getWorkflowById(String workflowId);
    
    /**
     * Gets all Paths for the specified Workflow instance
     * 
     * @param workflowId  workflow instance id
     * @return  the list of workflow paths
     */
    @Auditable(parameters = {"workflowId"})
    public List<WorkflowPath> getWorkflowPaths(String workflowId);
    
    /**
     * Cancel an "in-fligth" Workflow instance
     * 
     * @param workflowId  the workflow instance to cancel
     * @return  an updated representation of the workflow instance
     */
    @Auditable(parameters = {"workflowId"})
    public WorkflowInstance cancelWorkflow(String workflowId);

    /**
     * Signal the transition from one Workflow Node to another
     * 
     * @param pathId  the workflow path to signal on
     * @param transition  the transition to follow (or null, for the default transition)
     * @return  the updated workflow path
     */
    @Auditable(parameters = {"pathId", "transitionId"})
    public WorkflowPath signal(String pathId, String transitionId);

    /**
     * Gets all Tasks associated with the specified path
     * 
     * @param pathId  the path id
     * @return  the list of associated tasks
     */
    @Auditable(parameters = {"pathId"})
    public List<WorkflowTask> getTasksForWorkflowPath(String pathId);
    

    //
    // Task Management
    //
    
    /**
     * Gets a Task by unique Id
     * 
     * @param taskId  the task id
     * @return  the task
     */
    @Auditable(parameters = {"taskId"})
    public WorkflowTask getTaskById(String taskId);
    
    /**
     * Gets all tasks assigned to the specified authority
     * 
     * @param authority  the authority
     * @param state  filter by specified workflow task state
     * @return  the list of assigned tasks
     */
    @Auditable(parameters = {"authority", "state"})
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
     * @param transition  the task transition to take on completion (or null, for the default transition)
     * @return  the updated task
     */
    @Auditable(parameters = {"taskId", "transitionId"})
    public WorkflowTask endTask(String taskId, String transitionId);
    
    
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
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"container"})
    public NodeRef createPackage(NodeRef container);

    /**
     * Gets the Workflows that act upon the specified Repository content.
     *  
     * @param packageItem  the repository content item to get workflows for
     * @param active  true => active workflows only, false => completed workflows only
     * @return  list of workflows which act upon the specified content
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"packageItem", "active"})
    public List<WorkflowInstance> getWorkflowsForContent(NodeRef packageItem, boolean active);
    
}
