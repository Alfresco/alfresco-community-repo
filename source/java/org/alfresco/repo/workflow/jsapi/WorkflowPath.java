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

import org.alfresco.service.cmr.workflow.WorkflowNode;
import org.alfresco.service.cmr.workflow.WorkflowService;

/**
 * Class that represents a path of execution through a workflow.
 * 
 * A simple workflow consists of only one root path of execution.
 * That path may branch at some subsequent transition, so that execution
 * follows multiple paths through the workflow.
 * 
 * @author glenj
 *
 */
public class WorkflowPath
{
	/** Unique ID for workflow path */
	private final String id;
	
	/** State of workflow path 'true':active 'false':complete */
	private boolean active;
	
	/** Workflow node that the path has reached */
	private WorkflowNode node;
	
	/** Workflow instance path is part of */
	private WorkflowInstance instance;
	
	/** Workflow Service reference */
	private WorkflowService workflowService;

	/**
	 * Creates a new instance of a workflow path
	 * 
	 * @param id workflow path ID
	 * @param node workflow node the path has reached
	 * @param instance instance to which the workflow path belongs 
	 * @param workflowService reference to the Workflow Service 
	 */
	public WorkflowPath(final String id, final WorkflowNode node, final WorkflowInstance instance,
				final WorkflowService workflowService)
	{
		this.id = id;
		this.node = node;
		this.instance = instance;
		this.active = false;
		this.workflowService = workflowService;
	}
	
	/**
	 * Creates a new instance of WorkflowPath from an instance of the WorkflowPath
	 * class provided by the CMR workflow model 
	 * 
	 * @param cmrWorkflowPath an instance of WorkflowPath from the CMR
	 * 		workflow object model 
	 * @param workflowService reference to the Workflow Service 
	 */
	public WorkflowPath(final org.alfresco.service.cmr.workflow.WorkflowPath cmrWorkflowPath,
				final WorkflowService workflowService)
	{
		this.id = cmrWorkflowPath.id;
		this.node = cmrWorkflowPath.node;
		this.instance = new WorkflowInstance(cmrWorkflowPath.instance, workflowService);
		this.active = cmrWorkflowPath.active;
		this.workflowService = workflowService;
	}

	/**
	 * Creates a new instance of a workflow path from 
	 */

	/**
	 * Gets the value of the <code>id</code> property
	 *
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * Gets the value of the <code>active</code> property
	 *
	 * @return the active
	 */
	public boolean isActive()
	{
		return active;
	}

	/**
	 * Gets the value of the <code>node</code> property
	 *
	 * @return the node
	 */
	public WorkflowNode getNode()
	{
		return node;
	}

	/**
	 * Gets the value of the <code>instance</code> property
	 *
	 * @return the instance
	 */
	public WorkflowInstance getInstance()
	{
		return instance;
	}

	/**
	 * Get all tasks associated with this workflow path
	 * 
	 * @return all the tasks associated with this workflow path instance
	 */
	public List<WorkflowTask> getTasks()
	{
		List<org.alfresco.service.cmr.workflow.WorkflowTask> cmrTasks = workflowService.getTasksForWorkflowPath(id);
		List<WorkflowTask> tasks = new ArrayList<WorkflowTask>();
		for (org.alfresco.service.cmr.workflow.WorkflowTask cmrTask : cmrTasks)
		{
			tasks.add(new WorkflowTask(cmrTask, workflowService));
		}
		
		return tasks;
	}
}
