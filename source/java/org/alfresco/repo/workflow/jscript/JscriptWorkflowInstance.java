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
import java.util.Date;
import java.util.List;

import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.mozilla.javascript.Scriptable;

/**
 * Class representing an active or in-flight workflow
 * 
 * @author glenj
 *
 */
public class JscriptWorkflowInstance implements Serializable
{
	static final long serialVersionUID = 1015996328908978487L;
	
	/** Service Registry object */
	private final ServiceRegistry serviceRegistry;
	
	/** Root scripting scope for this object */
	private final Scriptable scope;
	
	/** Workflow instance id */
	private final String id;
	
	/** Workflow instance description */
	private final String description;
	
	/** Workflow instance start date */
	private final Date startDate;
	
	/** Workflow instance end date */
	private final Date endDate;

	/** Flag this Workflow instance as active-'true' or complete-'false' */
	private final boolean active;
	
	/**
	 * Creates a new instance of <code>WorkflowInstance</code> from
	 * 		scratch 
	 * 
	 * @param id ID of new workflow instance object
	 * @param description Description of new workflow instance object
	 * @param startDate Start Date of new workflow instance object
	 * @param serviceRegistry Service Registry instance
	 * @param scope the root scripting scope for this object 
	 */
	JscriptWorkflowInstance(final String id, final String description, final Date startDate,
				final ServiceRegistry serviceRegistry, final Scriptable scope)
	{
		this.id = id;
		this.description = description;
		this.active = true;
		this.startDate = startDate;
		this.endDate = null;
		this.serviceRegistry = serviceRegistry;
		this.scope = scope;
	}
	
	/**
	 * Create a new instance of <code>WorkflowInstance</code> from a
	 * WorkflowInstance object from the CMR workflow object model
	 *
	 * @param cmrWorkflowInstance CMR workflow instance
	 * @param serviceRegistry Service Registry instance 
	 * @param scope the root scripting scope for this object 
	 */
	JscriptWorkflowInstance(final WorkflowInstance
			cmrWorkflowInstance, final ServiceRegistry serviceRegistry, final Scriptable scope)
	{
		this.id = cmrWorkflowInstance.id;
		this.description = cmrWorkflowInstance.description;
		this.active = cmrWorkflowInstance.active;
		this.startDate = cmrWorkflowInstance.startDate;
		this.endDate = cmrWorkflowInstance.endDate;
		this.serviceRegistry = serviceRegistry;
		this.scope = scope;
	}
	
	/**
	 * Get all paths for the specified workflow instance
	 */
	public Scriptable getPaths()
	{
		WorkflowService workflowService = serviceRegistry.getWorkflowService();
		
		List<WorkflowPath> cmrPaths = workflowService.getWorkflowPaths(this.id);
		ArrayList<Serializable> paths = new ArrayList<Serializable>();
		for (WorkflowPath cmrPath : cmrPaths)
		{
			paths.add(new JscriptWorkflowPath(cmrPath, this.serviceRegistry, this.scope));
		}
		
		Scriptable pathsScriptable =
			(Scriptable)new ValueConverter().convertValueForScript(this.serviceRegistry, this.scope, null, paths);
		
		return pathsScriptable;
	}
	
	/**
	 * Getter for <code>id</code> property
	 * 
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * Getter for <code>description</code> property
	 * 
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Get state for <code>active</code> property
	 * 
	 * @return the active
	 */
	public boolean isActive()
	{
		return active;
	}

	/**
	 * Getter for <code>startDate</code> property
	 * 
	 * @return the startDate
	 */
	public Scriptable getStartDate()
	{
		return (Scriptable)new ValueConverter().convertValueForScript(
				this.serviceRegistry, this.scope, null, this.startDate);
	}

	/**
	 * Getter for <code>endDate</code> property
	 * 
	 * @return the endDate
	 */
	public Scriptable getEndDate()
	{
		return (Scriptable)new ValueConverter().convertValueForScript(
			this.serviceRegistry, this.scope, null, this.endDate);
	}
	
	/**
	 * Cancel workflow instance
	 */
	public void cancel()
	{
		serviceRegistry.getWorkflowService().cancelWorkflow(this.id);
	}
	
	/**
	 * Delete workflow instance
	 */
	public void delete()
	{
		serviceRegistry.getWorkflowService().deleteWorkflow(this.id);
	}
}
