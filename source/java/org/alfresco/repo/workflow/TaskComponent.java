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

