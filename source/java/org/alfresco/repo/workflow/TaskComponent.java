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

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;


/**
 * SPI to be implemented by a BPM Engine that provides Task management.
 * 
 * @author davidc
 */
public interface TaskComponent
{

    /**
     * Gets a Task by unique Id
     * 
     * @param taskId  the task id
     * @return  the task
     */
    public WorkflowTask getTaskById(String taskId);

    /**
     * Gets all tasks assigned to the specified authority
     * 
     * @param authority  the authority
     * @param state  filter by specified workflow task state
     * @return  the list of assigned tasks
     */
    public List<WorkflowTask> getAssignedTasks(String authority, WorkflowTaskState state);
    
    /**
     * Gets the pooled tasks available to the specified authority
     * 
     * @param authority   the authority
     * @return  the list of pooled tasks
     */
    public List<WorkflowTask> getPooledTasks(List<String> authorities);
    
    /**
     * Update the Properties and Associations of a Task
     * 
     * @param taskId  the task id to update
     * @param properties  the map of properties to set on the task (or null, if none to set)
     * @param add  the map of items to associate with the task (or null, if none to add)
     * @param remove  the map of items to dis-associate with the task (or null, if none to remove)
     * @return  the update task
     */
    public WorkflowTask updateTask(String taskId, Map<QName, Serializable> properties, Map<QName, List<NodeRef>> add, Map<QName, List<NodeRef>> remove);
    
    /**
     * Start the specified Task
     * 
     * Note: this is an optional task operation.  It may be used to track
     *       when work started on a task as well as resume a suspended task.
     * 
     * @param taskId  the task to start
     * @return  the updated task
     */
    public WorkflowTask startTask(String taskId);
    
    /**
     * Suspend the specified Task
     * 
     * @param taskId
     * @return  the update task
     */
    public WorkflowTask suspendTask(String taskId);

    /**
     * End the Task (i.e. complete the task)
     * 
     * @param taskId  the task id to end
     * @param transition  the task transition to take on completion (or null, for the default transition)
     * @return  the updated task
     */    
    public WorkflowTask endTask(String taskId, String transitionId);
        
}

