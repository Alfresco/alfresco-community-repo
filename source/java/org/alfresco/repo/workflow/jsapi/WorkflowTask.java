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
import java.util.Map;

import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;

/**
 * This class represents a workflow task (an instance of a workflow
 * task definition)
 * 
 * @author glenj
 *
 */
public class WorkflowTask
{
	/** Unique ID for workflow task */
	private final String id;
	
	/** Name for workflow task */
	private final String name;
	
	/** Title for workflow task */
	private final String title;
	
	/** Description of workflow task */
	private final String description;
	
    /** Properties (key/value pairs) */
    private Map<QName, Serializable> properties;
    
    /** Whether task is complete or not - 'true':complete, 'false':in-progress */
    private boolean complete = false;
    
    /** Whether task is pooled or not */
    private boolean pooled = false;
    
    /** Workflow Service reference */
    private WorkflowService workflowService;
    
	/**
	 * Creates a new instance of a workflow task (instance of a workflow task definition)
	 * 
	 * @param id workflow task ID
	 * @param name workflow task name
	 * @param title workflow task title
	 * @param description workflow task description
	 * @param workflowService reference to the Workflow Service 
	 */
	public WorkflowTask(final String id, final String name, final String title, final String description,
				final WorkflowService workflowService)
	{
		this.id = id;
		this.name = name;
		this.title = title;
		this.description = description;
		this.workflowService = workflowService;
	}
	
	/**
	 * Creates a new instance of a workflow task from a WorkflowTask from the CMR 
	 * workflow object model 
	 * 
	 * @param cmrWorkflowTask an instance of WorkflowTask from CMR workflow object model
	 * @param workflowService reference to the Workflow Service 
	 */
	public WorkflowTask(final org.alfresco.service.cmr.workflow.WorkflowTask cmrWorkflowTask,
				WorkflowService workflowService)
	{
		this.id = cmrWorkflowTask.id;
		this.name = cmrWorkflowTask.name;
		this.title = cmrWorkflowTask.title;
		this.description = cmrWorkflowTask.description;
		this.workflowService = workflowService;
	}

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
	 * Gets the value of the <code>name</code> property
	 *
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Gets the value of the <code>title</code> property
	 *
	 * @return the title
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * Gets the value of the <code>description</code> property
	 *
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Gets the value of the <code>properties</code> property
	 *
	 * @return the properties
	 */
	public Map<QName, Serializable> getProperties()
	{
		return properties;
	}

	/**
	 * Sets the value of the <code>properties</code> property
	 * 
	 * @param properties the properties to set
	 */
	public void setProperties(Map<QName, Serializable> properties)
	{
		this.properties = properties;
	}

	/**
	 * Returns whether the task is complete
	 * 	'true':complete, 'false':in-progress
	 *
	 * @return the complete
	 */
	public boolean isComplete()
	{
		return complete;
	}

	/**
	 * Sets whether the task is complete or in-progress
	 * 	'true':complete, 'false':in-progress
	 * 
	 * @param complete the complete to set
	 */
	public void setComplete(boolean complete)
	{
		this.complete = complete;
	}

	/**
	 * Returns whether this task is pooled or not
	 *
	 * @return 'true': task is pooled, 'false': task is not pooled 
	 */
	public boolean isPooled()
	{
		return pooled;
	}

	/**
	 * Sets whether task is pooled('true') or not('false')
	 * 
	 * @param pooled the pooled to set
	 */
	public void setPooled(boolean pooled)
	{
		this.pooled = pooled;
	}
	
	/**
	 * End the task
	 * 
	 * @param transition transition to end the task for
	 */
	public void endTask(String transitionId)
	{
		workflowService.endTask(this.id, transitionId);
	}
}
