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
package org.alfresco.repo.workflow.jscript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.mozilla.javascript.Scriptable;

/**
 * The Workflow Manager serves as the main entry point for scripts 
 * to create and interact with workflows.
 * It is made available in the root scripting scope
 * 
 * @author glenj
 *
 */
public class WorkflowManager extends BaseScopableProcessorExtension
{
    /** Registry Service property */ 
	private ServiceRegistry services;

    /**
     * Sets the Service Registry property
     * 
     * @param services  the service registry
     */
    public void setServiceRegistry(ServiceRegistry services)
    {
        this.services = services;
    }
    
	/**
	 * Get deployed workflow definition by ID
	 * 
	 * @param id the workflow definition ID
	 * @return the workflow definition matching the given ID
	 */
	public JscriptWorkflowDefinition getDefinition(String id)
	{
		WorkflowDefinition cmrWorkflowDefinition =
			this.services.getWorkflowService().getDefinitionById(id);
		return new JscriptWorkflowDefinition(cmrWorkflowDefinition, this.services, getScope());
	}
		
	/**
	 * Get tasks assigned to the current user. Note that this will only return in-progress 
	 * tasks.
	 * 
     * @return  the list of assigned (in-progress) tasks
	 */
	public Scriptable getAssignedTasks()
	{
		return getAssignedTasksByState(WorkflowTaskState.IN_PROGRESS);  
	}
	
	/**
	 * Get completed tasks assigned to the current user.
	 * 
     * @return  the list of completed tasks
	 */
	public Scriptable getCompletedTasks()
	{
		return getAssignedTasksByState(WorkflowTaskState.COMPLETED);
	}
	
	/**
	 * Get Workflow Instance by ID
	 * 
	 * @param workflowInstanceID ID of the workflow instance to retrieve
	 * @return the workflow instance for the given ID
	 */
	public JscriptWorkflowInstance getInstance(String workflowInstanceID)
	{
		WorkflowInstance cmrWorkflowInstance = this.services.getWorkflowService().getWorkflowById(
			workflowInstanceID);
		return new JscriptWorkflowInstance(cmrWorkflowInstance, this.services, getScope());
	}
	
	/**
	 * Get pooled tasks
	 * 
     * @param authority  the authority
     * @return  the list of assigned tasks
	 */
	public Scriptable getPooledTasks(final String authority)
	{
		List<WorkflowTask> cmrPooledTasks = this.services.getWorkflowService().getPooledTasks(
			authority);
		ArrayList<Serializable> pooledTasks = new ArrayList<Serializable>();
		for (WorkflowTask cmrPooledTask : cmrPooledTasks)
		{
			pooledTasks.add(new JscriptWorkflowTask(cmrPooledTask, this.services));
		}
		
		Scriptable pooledTasksScriptable = (Scriptable)new ValueConverter().convertValueForScript(
			this.services, getScope(), null, pooledTasks);
		return pooledTasksScriptable;
	}
	
	/**
	 * Get task by id
	 * 
	 * @param id task id
	 * @return the task (null if not found)
	 */
	public JscriptWorkflowTask getTask(String id)
	{
		WorkflowTask cmrWorkflowTask = this.services.getWorkflowService().getTaskById(id);
		return new JscriptWorkflowTask(cmrWorkflowTask, this.services);
	}
	
	/**
	 * Get task by id. Alternative method signature to <code>getTask(String id)</code> for 
	 * those used to the Template API
	 * 
	 * @param id task id
	 * @return the task (null if not found)
	 */
	public JscriptWorkflowTask getTaskById(String id)
	{
		return getTask(id);
	}
	
	/**
	 * Gets the latest versions of the deployed, workflow definitions
	 *
	 * @return the latest versions of the deployed workflow definitions
	 */
	public Scriptable getLatestDefinitions()
	{
		List<WorkflowDefinition> cmrDefinitions = this.services.getWorkflowService().getDefinitions();
		ArrayList<Serializable> workflowDefs = new ArrayList<Serializable>();
		for (WorkflowDefinition cmrDefinition : cmrDefinitions)
		{
			workflowDefs.add(new JscriptWorkflowDefinition(cmrDefinition, this.services, getScope()));
		}
		
		Scriptable workflowDefsScriptable = (Scriptable)new ValueConverter().convertValueForScript(
			this.services, this.getScope(), null, workflowDefs);
		return workflowDefsScriptable;
	}

	/**
	 * Gets all versions of the deployed workflow definitions
	 *
	 * @return all versions of the deployed workflow definitions
	 */
	public Scriptable getAllDefinitions()
	{
		List<WorkflowDefinition> cmrDefinitions = this.services.getWorkflowService().getAllDefinitions();
		ArrayList<Serializable> workflowDefs = new ArrayList<Serializable>();
		for (WorkflowDefinition cmrDefinition : cmrDefinitions)
		{
			workflowDefs.add(new JscriptWorkflowDefinition(cmrDefinition, this.services, getScope()));
		}
		
		Scriptable workflowDefsScriptable = (Scriptable)new ValueConverter().convertValueForScript(
			this.services, this.getScope(), null, workflowDefs);
		return workflowDefsScriptable;
	}
	
	/**
	 * Create a workflow package (a container of content to route through a workflow)
	 * 
	 * @return the created workflow package
	 */
	public ScriptNode createPackage()
	{
		NodeRef node = this.services.getWorkflowService().createPackage(null);
		return new ScriptNode(node, services);
	}

	/**
	 * Get tasks assigned to the current user, filtered by workflow task state.
	 * Only tasks having the specified state will be returned.
	 * 
     * @param state  workflow task state to filter assigned tasks by
     * @return  the list of assigned tasks, filtered by state
	 */
	private Scriptable getAssignedTasksByState(WorkflowTaskState state)
	{
		List<WorkflowTask> cmrAssignedTasks = this.services.getWorkflowService().getAssignedTasks(
			services.getAuthenticationService().getCurrentUserName(), state);
		ArrayList<Serializable> assignedTasks = new ArrayList<Serializable>();
		for (WorkflowTask cmrTask : cmrAssignedTasks)
		{
			assignedTasks.add(new JscriptWorkflowTask(cmrTask, this.services));
		}
				
		Scriptable assignedTasksScriptable =
			(Scriptable)new ValueConverter().convertValueForScript(this.services, getScope(), null, assignedTasks);
		
		return assignedTasksScriptable;
	}
}
