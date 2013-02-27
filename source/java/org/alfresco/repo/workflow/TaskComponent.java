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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
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
     * @param lazyInitialization hint in order to return partially-initialized entities
     * @return  the list of assigned tasks
     */
    public List<WorkflowTask> getAssignedTasks(String authority, WorkflowTaskState state, boolean lazyInitialization);
    
    /**
     * Gets the pooled tasks available to the specified authority
     * 
     * @param authority   the authority
     * @param lazyInitialization hint in order to return partially-initialized entities
     * @return  the list of pooled tasks
     */
    public List<WorkflowTask> getPooledTasks(List<String> authorities, boolean lazyInitialization);
    
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
    public List<WorkflowTask> queryTasks(final WorkflowTaskQuery query, boolean sameSession);

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
    
    /**
     * Gets all active timers for the specified workflow
     * 
     * @return  the list of active timers
     */
    public WorkflowTask getStartTask(String workflowInstanceId);
    
    
    /**
     * Gets all start tasks for the specified workflow
     * 
     * @return  the list of start tasks
     */
    public List<WorkflowTask> getStartTasks(final List<String> workflowInstanceIds, final boolean sameSession);    
}

