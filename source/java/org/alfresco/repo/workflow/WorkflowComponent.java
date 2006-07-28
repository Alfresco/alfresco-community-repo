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
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;


/**
 * SPI to be implemented by a BPM Engine that provides Workflow instance management.
 * 
 * @author davidc
 */
public interface WorkflowComponent
{

    /**
     * Start a Workflow Instance
     * 
     * @param workflowDefinitionId  the workflow definition id
     * @param parameters  the initial set of parameters used to populate the "Start Task" properties
     * @return  the initial workflow path
     */
    public WorkflowPath startWorkflow(String workflowDefinitionId, Map<QName, Serializable> parameters);
    
    /**
     * Gets all "in-flight" workflow instances of the specified Workflow Definition
     * 
     * @param workflowDefinitionId  the workflow definition id
     * @return  the list of "in-fligth" workflow instances
     */
    public List<WorkflowInstance> getActiveWorkflows(String workflowDefinitionId);
    
    /**
     * Gets all Paths for the specified Workflow instance
     * 
     * @param workflowId  workflow instance id
     * @return  the list of workflow paths
     */
    public List<WorkflowPath> getWorkflowPaths(String workflowId);
    
    /**
     * Cancel an "in-fligth" Workflow instance
     * 
     * @param workflowId  the workflow instance to cancel
     * @return  an updated representation of the workflow instance
     */
    public WorkflowInstance cancelWorkflow(String workflowId);

    /**
     * Signal the transition from one Workflow Node to another within an "in-flight"
     * process.
     * 
     * @param pathId  the workflow path to signal on
     * @param transition  the transition to follow (or null, for the default transition)
     * @return  the updated workflow path
     */
    public WorkflowPath signal(String pathId, String transition);
    
    /**
     * Gets all Tasks associated with the specified path
     * 
     * @param pathId  the path id
     * @return  the list of associated tasks
     */    
    public List<WorkflowTask> getTasksForWorkflowPath(String pathId);

}

