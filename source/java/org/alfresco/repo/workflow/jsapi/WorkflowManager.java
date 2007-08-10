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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.workflow.jsapi;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.processor.BaseProcessorExtension;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;

/**
 * The Workflow Manager serves as the main entry point for scripts 
 * to create and interact with workflows.
 * It is made available in the root scripting scope
 * 
 * @author glenj
 *
 */
public class WorkflowManager extends BaseProcessorExtension
{
	/** Workflow Service to make calls to workflow service API */
	private WorkflowService workflowService;
		
	/**
	 * Get deployed workflow definition by ID
	 * 
	 * @param id the workflow definition ID
	 * @return the workflow definition matching the given ID
	 */
	public WorkflowDefinition getDefinition(String id)
	{
		org.alfresco.service.cmr.workflow.WorkflowDefinition cmrWorkflowDefinition =
			workflowService.getDefinitionById(id);
		return new WorkflowDefinition(cmrWorkflowDefinition, workflowService);
	}
	
	/**
	 * Set the workflow service property
	 * 
	 * @param workflowService the workflow service
	 */
	public void setWorkflowService(final WorkflowService workflowService)
	{
		this.workflowService = workflowService;
	}
	
	/**
	 * Get assigned tasks
	 * 
     * @param authority  the authority
     * @param state  filter by specified workflow task state
     * @return  the list of assigned tasks
	 */
	public List<WorkflowTask> getAssignedTasks(final String authority, final WorkflowTaskState state)
	{
		List<org.alfresco.service.cmr.workflow.WorkflowTask> cmrAssignedTasks = workflowService.getAssignedTasks(authority, state);
		List<WorkflowTask> assignedTasks = new ArrayList<WorkflowTask>();
		for (org.alfresco.service.cmr.workflow.WorkflowTask cmrTask : cmrAssignedTasks)
		{
			assignedTasks.add(new WorkflowTask(cmrTask, workflowService));
		}
		
		return assignedTasks;
	}
	
	/**
	 * Get Workflow Instance by ID
	 * 
	 * @param workflowInstanceID ID of the workflow instance to retrieve
	 * @return the workflow instance for the given ID
	 */
	public WorkflowInstance getInstance(String workflowInstanceID)
	{
		org.alfresco.service.cmr.workflow.WorkflowInstance cmrWorkflowInstance = workflowService.getWorkflowById(workflowInstanceID);
		return new WorkflowInstance(cmrWorkflowInstance, workflowService);
	}
	
	/**
	 * Get pooled tasks
	 * 
     * @param authority  the authority
     * @param state  filter by specified workflow task state
     * @return  the list of assigned tasks
	 */
	public List<WorkflowTask> getPooledTasks(final String authority, final WorkflowTaskState state)
	{
		List<org.alfresco.service.cmr.workflow.WorkflowTask> cmrPooledTasks = workflowService.getPooledTasks(authority);
		List<WorkflowTask> pooledTasks = new ArrayList<WorkflowTask>();
		for (org.alfresco.service.cmr.workflow.WorkflowTask cmrPooledTask : cmrPooledTasks)
		{
			pooledTasks.add(new WorkflowTask(cmrPooledTask, workflowService));
		}
		
		return pooledTasks;
	}
	
	/**
	 * Get task by id
	 * 
	 * @param id task id
	 * @return the task (null if not found)
	 */
	public WorkflowTask getTask(String id)
	{
		org.alfresco.service.cmr.workflow.WorkflowTask cmrWorkflowTask = workflowService.getTaskById(id);
		return new WorkflowTask(cmrWorkflowTask, workflowService);
	}
	
	/**
	 * Gets the latest versions of the deployed, workflow definitions
	 *
	 * @return the latest versions of the deployed workflow definitions
	 */
	public List<WorkflowDefinition> getLatestDefinitions()
	{
		List<org.alfresco.service.cmr.workflow.WorkflowDefinition> cmrDefinitions = workflowService.getDefinitions();
		List<WorkflowDefinition> workflowDefs = new ArrayList<WorkflowDefinition>();
		for (org.alfresco.service.cmr.workflow.WorkflowDefinition cmrDefinition : cmrDefinitions)
		{
			workflowDefs.add(new WorkflowDefinition(cmrDefinition, workflowService));
		}
		
		return workflowDefs;
	}

	/**
	 * Gets all versions of the deployed workflow definitions
	 *
	 * @return all versions of the deployed workflow definitions
	 */
	public List<WorkflowDefinition> getAllDefinitions()
	{
		List<org.alfresco.service.cmr.workflow.WorkflowDefinition> cmrDefinitions = workflowService.getAllDefinitions();
		List<WorkflowDefinition> workflowDefs = new ArrayList<WorkflowDefinition>();
		for (org.alfresco.service.cmr.workflow.WorkflowDefinition cmrDefinition : cmrDefinitions)
		{
			workflowDefs.add(new WorkflowDefinition(cmrDefinition, workflowService));
		}
		
		return workflowDefs;
	}
}
