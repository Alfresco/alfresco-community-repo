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
import java.util.List;
import java.util.Map;

import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTimer;
import org.alfresco.service.namespace.QName;


/**
 * SPI to be implemented by a BPM Engine that provides Workflow instance management.
 * 
 * @author davidc
 */
public interface WorkflowComponent
{

    //
    // Workflow Definition Support
    //

    
    /**
     * Deploy a Workflow Definition
     * 
     * @param workflowDefinition  the content object containing the definition
     * @param mimetype (optional)  the mime type of the workflow definition
     * @return workflow deployment descriptor
     */
    public WorkflowDeployment deployDefinition(InputStream workflowDefinition, String mimetype);
    
    /**
     * Deploy a Workflow Definition
     * 
     * @param workflowDefinition  the content object containing the definition
     * @param mimetype (optional)  the mime type of the workflow definition
     * @param name (optional)  a name to represent the deployment
     * @return workflow deployment descriptor
     * @since 4.0
     */
    public WorkflowDeployment deployDefinition(InputStream workflowDefinition, String mimetype, String name);

    /**
     * Is the specified Workflow Definition already deployed?
     * 
     * Note: the notion of "already deployed" may differ between bpm engines. For example,
     *       different versions of the same process may be considered equal.
     *       
     * @param workflowDefinition  the definition to check
     * @param mimetype  the mimetype of the definition
     * @return  true => already deployed
     */
    public boolean isDefinitionDeployed(InputStream workflowDefinition, String mimetype);
    
    /**
     * Undeploy an exisiting Workflow Definition
     * 
     * TODO: Determine behaviour when "in-flight" workflow instances exist
     *  
     * @param workflowDefinitionId  the id of the definition to undeploy
     */
    public void undeployDefinition(String workflowDefinitionId);
    
    /**
     * Gets all deployed Workflow Definitions
     * 
     * @return  the deployed workflow definitions
     */
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
     * @return  the deployed workflow definition
     */
    public WorkflowDefinition getDefinitionById(String workflowDefinitionId);
    
    /**
     * Gets a Workflow Definition by unique name
     * 
     * @param workflowName  workflow name e.g. jbpm$wf:review
     * @return  the deployed workflow definition
     */
    public WorkflowDefinition getDefinitionByName(String workflowName);
    
    /**
     * Gets all (including previous) Workflow Definitions for the given unique name
     * 
     * @param workflowName  workflow name e.g. jbpm$wf:review
     * @return  the deployed workflow definition (or null if not found)
     */
    @Auditable(parameters = {"workflowName"})
    public List<WorkflowDefinition> getAllDefinitionsByName(String workflowName);
    
    /**
     * Gets a graphical view of the Workflow Definition
     * 
     * @param workflowDefinitionId  the workflow definition id
     * @return   graphical image of workflow definition
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
    // Workflow Instance Support
    //
    
    
    /**
     * Start a Workflow Instance
     * 
     * @param workflowDefinitionId  the workflow definition id
     * @param parameters  the initial set of parameters used to populate the "Start Task" properties
     * @return  the initial workflow path
     */
    public WorkflowPath startWorkflow(String workflowDefinitionId, Map<QName, Serializable> parameters);
    
    /**
     * Gets all "in-flight" active workflow instances of the specified Workflow Definition
     * 
     * @param workflowDefinitionId  the workflow definition id
     * @return  the list of "in-flight" workflow instances
     */
    public List<WorkflowInstance> getActiveWorkflows(String workflowDefinitionId);
    
    /**
     * Gets all "in-flight" completed workflow instances of the specified Workflow Definition
     * 
     * @param workflowDefinitionId  the workflow definition id
     * @return  the list of "in-flight" workflow instances
     */
    public List<WorkflowInstance> getCompletedWorkflows(String workflowDefinitionId);
    
    /**
     * Gets all "in-flight" workflow instances (both active and completed) of the specified Workflow Definition
     * 
     * @param workflowDefinitionId  the workflow definition id     
     * @return  the list of "in-flight" workflow instances
     */
    public List<WorkflowInstance> getWorkflows(String workflowDefinitionId);
    
    /**
     * Gets a specific workflow instances
     *
     * @param workflowId  the id of the workflow to retrieve
     * @return  the workflow instance 
     */
    public WorkflowInstance getWorkflowById(String workflowId);
    
    /**
     * Gets all Paths for the specified Workflow instance
     * 
     * @param workflowId  workflow instance id
     * @return  the list of workflow paths
     */
    public List<WorkflowPath> getWorkflowPaths(String workflowId);
    
    /**
     * Gets the properties associated with the specified path (and parent paths)
     * 
     * @param pathId  workflow path id
     * @return  map of path properties
     */
    public Map<QName, Serializable> getPathProperties(String pathId); 
    
    /**
     * Cancel an "in-flight" Workflow instance
     * 
     * @param workflowId  the workflow instance to cancel
     * @return  an updated representation of the workflow instance
     */
    public WorkflowInstance cancelWorkflow(String workflowId);

    /**
     * Delete an "in-flight" Workflow instance
     * 
     * @param workflowId  the workflow instance to cancel
     * @return  an updated representation of the workflow instance
     */
    public WorkflowInstance deleteWorkflow(String workflowId);

    /**
     * Signal the transition from one Workflow Node to another within an "in-flight"
     * process.
     * 
     * @param pathId  the workflow path to signal on
     * @param transition  the transition to follow (or null, for the default transition)
     * @return  the updated workflow path
     */
    public WorkflowPath signal(String pathId, String transitionId);
    
    /**
     * Fire custom event against specified path
     * 
     * @param pathId  the workflow path to fire event on
     * @param event  name of event
     * @return  workflow path (it may have been updated as a result of firing the event
     */
    public WorkflowPath fireEvent(String pathId, String event);

    /**
     * Gets all Tasks associated with the specified path
     * 
     * @param pathId  the path id
     * @return  the list of associated tasks
     */    
    public List<WorkflowTask> getTasksForWorkflowPath(String pathId);
    
    /**
     * Gets all active timers for the specified workflow
     * 
     * @return  the list of active timers
     */
    public List<WorkflowTimer> getTimers(String workflowId);
    
    /**
     * Determines if a graphical view of the workflow instance exists
     * 
     * @param workflowInstanceId  the workflow instance id
     * @return true if there is a workflow instance diagram available
     * @since 4.0
     */
    public boolean hasWorkflowImage(String workflowInstanceId);
    
    /**
     * Gets a graphical view of the workflow instance
     * 
     * @param workflowInstanceId  the workflow instance id
     * @return image view of the workflow instance as an InputStream or null if a diagram is not available
     * @since 4.0
     */
    public InputStream getWorkflowImage(String workflowInstanceId);

}

