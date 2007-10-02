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

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.jscript.ScriptableQNameMap;
import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.mozilla.javascript.Scriptable;

public class JscriptWorkflowDefinition implements Serializable
{
	static final long serialVersionUID = 1641614201321129544L;	
	
	/** Service Registry */
	private ServiceRegistry serviceRegistry;
	
	/** Workflow definition id */
	private final String id;
	
	/** Workflow definition name */
	private final String name;
	
	/** Workflow definition version */
	private final String version;
	
	/** Workflow definition title */
	private final String title;
	
	/** Workflow definition description */
	private final String description;
	
	/** Root scripting scope for this object */
	private final Scriptable scope;
	
	/**
	 * Create a new instance of <code>WorkflowDefinition</code> from a
	 * CMR workflow object model WorkflowDefinition instance
	 * 
	 * @param cmrWorkflowDefinition an instance of WorkflowDefinition from the CMR workflow object model
	 * @param serviceRegistry reference to the Service Registry
	 * @param scope the root scripting scope for this object 
	 */
	JscriptWorkflowDefinition(final WorkflowDefinition cmrWorkflowDefinition,
				final ServiceRegistry serviceRegistry, final Scriptable scope)
	{
		this.id = cmrWorkflowDefinition.id;
		this.name = cmrWorkflowDefinition.name;
		this.version = cmrWorkflowDefinition.version;
		this.title = cmrWorkflowDefinition.title;
		this.description = cmrWorkflowDefinition.description;
		this.serviceRegistry = serviceRegistry;
		this.scope = scope;
	}
	
	/**
	 * Creates a new instance of WorkflowDefinition from scratch
	 * 
	 * @param id workflow definition ID
	 * @param name name of workflow definition
	 * @param version version of workflow definition
	 * @param title title of workflow definition
	 * @param description description of workflow definition
	 * @param serviceRegistry reference to the Service Registry
	 * @param scope root scripting scope for this object
	 */
	JscriptWorkflowDefinition(final String id, final String name, final String version,
			final String title, final String description, ServiceRegistry serviceRegistry,
			final Scriptable scope)
	{
		this.id = id;
		this.name = name;
		this.version = version;
		this.title = title;
		this.description = description;
		this.serviceRegistry = serviceRegistry;
		this.scope = scope;
	}
	
	/**
	 * Get value of <code>id</code> property
	 * 
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}
	
	/**
	 * Get value of <code>name</code> property
	 * 
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Get value of <code>version</code> property
	 * 
	 * @return the version
	 */
	public String getVersion()
	{
		return version;
	}
	
	/**
	 * Get value of <code>title</code> property
	 * 
	 * @return the title
	 */
	public String getTitle()
	{
		return title;
	}
	
	/**
	 * Get value of <code>description</code> property
	 * 
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}
	
	/**
	 * Start workflow instance from workflow definition
	 * 
	 * @param workflowPackage workflow package object to 'attach' to the new workflow
	 * 		instance
	 * @param properties properties (map of key-value pairs) used to populate the 
	 * 		start task properties
	 * @return the initial workflow path
	 */
	@SuppressWarnings("unchecked")
	public JscriptWorkflowPath startWorkflow(ScriptNode workflowPackage,
		ScriptableQNameMap<String, Serializable> properties)
	{
		WorkflowService workflowService = this.serviceRegistry.getWorkflowService();
		
		properties.put(WorkflowModel.ASPECT_WORKFLOW_PACKAGE, workflowPackage);
		
		WorkflowPath cmrWorkflowPath = 
			workflowService.startWorkflow(id, properties);
		return new JscriptWorkflowPath(cmrWorkflowPath, this.serviceRegistry, this.scope);
	}
	
	/**
	 * Get active workflow instances of this workflow definition
	 * 
	 * @return the active workflow instances spawned from this workflow definition
	 */
	public synchronized Scriptable getActiveInstances()
	{
		WorkflowService workflowService = this.serviceRegistry.getWorkflowService();
		
		List<WorkflowInstance> cmrWorkflowInstances = workflowService.getActiveWorkflows(this.id);
		ArrayList<Serializable> activeInstances = new ArrayList<Serializable>();
		for (WorkflowInstance cmrWorkflowInstance : cmrWorkflowInstances)
		{
			activeInstances.add(new JscriptWorkflowInstance(cmrWorkflowInstance, this.serviceRegistry, this.scope));
		}
		
		Scriptable activeInstancesScriptable =
			(Scriptable)new ValueConverter().convertValueForScript(this.serviceRegistry, this.scope, null, activeInstances);
		
		return activeInstancesScriptable;
	}
}
