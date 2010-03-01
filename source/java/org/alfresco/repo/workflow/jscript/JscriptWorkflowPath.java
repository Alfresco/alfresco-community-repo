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
package org.alfresco.repo.workflow.jscript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.workflow.WorkflowNode;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.mozilla.javascript.Scriptable;

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
public class JscriptWorkflowPath implements Serializable
{
	static final long serialVersionUID = 8271566861210368614L;
	
	/** Unique ID for workflow path */
	private final String id;
	
	/** State of workflow path 'true':active 'false':complete */
	private boolean active;
	
	/** Workflow node that the path has reached */
	private WorkflowNode node;
	
	/** Workflow instance path is part of */
	private JscriptWorkflowInstance instance;
	
	/** Service Registry object */
	private ServiceRegistry serviceRegistry;

	/** Root scripting scope for this object */
	private final Scriptable scope;
	
	/**
	 * Creates a new instance of a workflow path
	 * 
	 * @param id workflow path ID
	 * @param node workflow node the path has reached
	 * @param instance instance to which the workflow path belongs 
	 * @param serviceRegistry Service Registry object 
	 * @param scope the root scripting scope for this object 
	 */
	public JscriptWorkflowPath(final String id, final WorkflowNode node, final JscriptWorkflowInstance instance,
				final ServiceRegistry serviceRegistry, final Scriptable scope)
	{
		this.id = id;
		this.node = node;
		this.instance = instance;
		this.active = false;
		this.serviceRegistry = serviceRegistry;
		this.scope = scope;
	}
	
	/**
	 * Creates a new instance of WorkflowPath from an instance of the WorkflowPath
	 * class provided by the CMR workflow model 
	 * 
	 * @param cmrWorkflowPath an instance of WorkflowPath from the CMR
	 * 		workflow object model 
	 * @param serviceRegistry Service Registry object
	 * @param scope the root scripting scope for this object 
	 */
	public JscriptWorkflowPath(final WorkflowPath cmrWorkflowPath,
				final ServiceRegistry serviceRegistry, Scriptable scope)
	{
		this.id = cmrWorkflowPath.id;
		this.node = cmrWorkflowPath.node;
		this.instance = new JscriptWorkflowInstance(cmrWorkflowPath.instance, serviceRegistry, scope);
		this.active = cmrWorkflowPath.active;
		this.serviceRegistry = serviceRegistry;
		this.scope = scope;
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
	public JscriptWorkflowInstance getInstance()
	{
		return instance;
	}

	/**
	 * Get all tasks associated with this workflow path
	 * 
	 * @return all the tasks associated with this workflow path instance
	 */
	public Scriptable getTasks()
	{
		WorkflowService workflowService = serviceRegistry.getWorkflowService();
		
		List<WorkflowTask> cmrTasks = workflowService.getTasksForWorkflowPath(id);
		ArrayList<Serializable> tasks = new ArrayList<Serializable>();
		for (WorkflowTask cmrTask : cmrTasks)
		{
			tasks.add(new JscriptWorkflowTask(cmrTask, this.serviceRegistry, this.scope));
		}
		
		Scriptable tasksScriptable =
			(Scriptable)new ValueConverter().convertValueForScript(this.serviceRegistry, scope, null, tasks);
		
		return tasksScriptable;
	}
	
    /**
     * Signal a transition to another node in the workflow
     * 
     * @param transitionId  ID of the transition to follow (or null, for the default transition)
     * @return  the updated workflow path
     */
	public JscriptWorkflowPath signal(String transitionId)
	{
		WorkflowPath path = serviceRegistry.getWorkflowService().signal(this.id, transitionId);
		return new JscriptWorkflowPath(path, this.serviceRegistry, this.scope);
	}
}
