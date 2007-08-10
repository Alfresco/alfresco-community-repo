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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;

public class WorkflowDefinition
{
	/** Workflow Service reference */
	private WorkflowService workflowService;
	
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
	
	/**
	 * Create a new instance of <code>WorkflowDefinition</code> from a
	 * CMR workflow object model WorkflowDefinition instance
	 * 
	 * @param cmrWorkflowDefinition an instance of WorkflowDefinition from the CMR workflow object model
	 * @param workflowService reference to the Workflow Service 
	 */
	public WorkflowDefinition(final org.alfresco.service.cmr.workflow.WorkflowDefinition cmrWorkflowDefinition,
				final WorkflowService workflowService)
	{
		this.id = cmrWorkflowDefinition.id;
		this.name = cmrWorkflowDefinition.name;
		this.version = cmrWorkflowDefinition.version;
		this.title = cmrWorkflowDefinition.title;
		this.description = cmrWorkflowDefinition.description;
		this.workflowService = workflowService;
	}
	
	/**
	 * Creates a new instance of WorkflowDefinition 
	 * 
	 * @param id workflow definition ID
	 * @param name name of workflow definition
	 * @param version version of workflow definition
	 * @param title title of workflow definition
	 * @param description description of workflow definition
	 * @param workflowService reference to the Workflow Service 
	 */
	public WorkflowDefinition(final String id, final String name, final String version,
			final String title, final String description, WorkflowService workflowService)
	{
		this.id = id;
		this.name = name;
		this.version = version;
		this.title = title;
		this.description = description;
	}
	
	/**
	 * Get value of 'id' property
	 * 
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}
	
	/**
	 * Get value of 'name' property
	 * 
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Get value of 'version' property
	 * 
	 * @return the version
	 */
	public String getVersion()
	{
		return version;
	}
	
	/**
	 * Get value of 'title' property
	 * 
	 * @return the title
	 */
	public String getTitle()
	{
		return title;
	}
	
	/**
	 * Get value of 'description' property
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
	 * @param properties properties (map of key-value pairs used to populate the 
	 * 		start task properties
	 * @return the initial workflow path
	 */
	public WorkflowPath startWorkflow(Map<QName, Serializable> properties)
	{
		org.alfresco.service.cmr.workflow.WorkflowPath cmrWorkflowPath = 
			workflowService.startWorkflow(id, properties);
		return new WorkflowPath(cmrWorkflowPath, workflowService);
	}
	
	/**
	 * Get active workflow instances of this workflow definition
	 * 
	 * @return the active workflow instances spawned from this workflow definition
	 */
	public synchronized List<WorkflowInstance> getActiveInstances()
	{
		List<org.alfresco.service.cmr.workflow.WorkflowInstance> cmrWorkflowInstances = workflowService.getActiveWorkflows(this.id);
		List<WorkflowInstance> activeInstances = new ArrayList<WorkflowInstance>();
		for (org.alfresco.service.cmr.workflow.WorkflowInstance cmrWorkflowInstance : cmrWorkflowInstances)
		{
			activeInstances.add(new WorkflowInstance(cmrWorkflowInstance, workflowService));
		}
		
		return activeInstances;
	}
}
