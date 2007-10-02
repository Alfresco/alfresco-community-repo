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
import java.util.Set;

import org.alfresco.repo.jscript.ScriptableQNameMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.Scriptable;

/**
 * This class represents a workflow task (an instance of a workflow
 * task definition)
 * 
 * @author glenj
 *
 */
public class JscriptWorkflowTask implements Serializable
{
	static final long serialVersionUID = -8285971359421912313L;
	
	/** Unique ID for workflow task */
	private final String id;
	
	/** Name for workflow task */
	private final String name;
	
	/** Title for workflow task */
	private final String title;
	
	/** Description of workflow task */
	private final String description;
	
    /** Properties (key/value pairs) for this Workflow Task */
    private ScriptableQNameMap<String, Serializable> properties;
    
    /** Whether task is complete or not - 'true':complete, 'false':in-progress */
    private boolean complete = false;
    
    /** Whether task is pooled or not */
    private boolean pooled = false;
    
    /** Service Registry object */
    private ServiceRegistry serviceRegistry;
    
	/**
	 * Creates a new instance of a workflow task (instance of a workflow task definition)
	 * 
	 * @param id workflow task ID
	 * @param name workflow task name
	 * @param title workflow task title
	 * @param description workflow task description
	 * @param serviceRegistry Service Registry object
	 */
	JscriptWorkflowTask(final String id, final String name, final String title,
		final String description, final ServiceRegistry serviceRegistry,
		final ScriptableQNameMap<String, Serializable> properties)
	{
		this.id = id;
		this.name = name;
		this.title = title;
		this.description = description;
		this.serviceRegistry = serviceRegistry;
		this.properties = properties;
	}
	
	/**
	 * Creates a new instance of a workflow task from a WorkflowTask from the CMR 
	 * workflow object model 
	 * 
	 * @param cmrWorkflowTask an instance of WorkflowTask from CMR workflow object model
	 * @param serviceRegistry Service Registry object
	 */
	JscriptWorkflowTask(final WorkflowTask cmrWorkflowTask,
				final ServiceRegistry serviceRegistry)
	{
		this.id = cmrWorkflowTask.id;
		this.name = cmrWorkflowTask.name;
		this.title = cmrWorkflowTask.title;
		this.description = cmrWorkflowTask.description;
		this.serviceRegistry = serviceRegistry;
		
		// instantiate ScriptableQNameMap<String, Serializable> properties
		// from WorkflowTasks's Map<QName, Serializable> properties
		this.properties = new ScriptableQNameMap<String, Serializable>(
			serviceRegistry.getNamespaceService());
		
		Set<QName> keys = cmrWorkflowTask.properties.keySet();
		for (QName key : keys)
		{
			Serializable value = cmrWorkflowTask.properties.get(key);
			this.properties.put(key.toString(), value);
		}
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
	public Scriptable getProperties()
	{
		return properties;
	}

	/**
	 * Sets the value of the <code>properties</code> property
	 * 
	 * @param properties the properties to set
	 */
	public void setProperties(ScriptableQNameMap<String, Serializable> properties)
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
		serviceRegistry.getWorkflowService().endTask(this.id, transitionId);
	}
}
