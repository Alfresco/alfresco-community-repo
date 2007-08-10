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
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.workflow.WorkflowService;

/**
 * Class representing an active or in-flight workflow
 * 
 * @author glenj
 *
 */
public class WorkflowInstance
{
	/** Workflow Manager reference */
	private WorkflowService workflowService;
	
	/** Workflow instance id */
	private final String id;
	
	/** Workflow instance description */
	private final String description;
	
	/** Flag this Workflow instance as active-'true' or complete-'false' */
	private boolean active;
	
	/** Workflow instance start date */
	private Date startDate;
	
	/** Workflow instance end date */
	private Date endDate;

	/**
	 * Creates a new instance of <code>WorkflowInstance</code>  
	 * 
	 * @param id
	 * @param description
	 * @param active
	 * @param startDate
	 * @param workflowService reference to the Workflow Service 
	 */
	public WorkflowInstance(final String id, final String description, final Date startDate,
				final WorkflowService workflowService)
	{
		this.id = id;
		this.description = description;
		this.active = true;
		this.startDate = startDate;
		this.workflowService = workflowService;
	}
	
	/**
	 * Create a new instance of <code>WorkflowInstance</code> from a
	 * WorkflowInstance object from the CMR workflow object model
	 *
	 * @param cmrWorkflowInstance CMR workflow instance
	 * @param workflowService reference to the Workflow Service 
	 */
	public WorkflowInstance(final org.alfresco.service.cmr.workflow.WorkflowInstance
			cmrWorkflowInstance, final WorkflowService workflowService)
	{
		this.id = cmrWorkflowInstance.id;
		this.description = cmrWorkflowInstance.description;
		this.active = cmrWorkflowInstance.active;
		this.startDate = cmrWorkflowInstance.startDate;
		this.workflowService = workflowService;
	}
	
	/**
	 * Get all paths for the specified workflow instance
	 */
	public List<WorkflowPath> getPaths(final String instanceId)
	{
		List<org.alfresco.service.cmr.workflow.WorkflowPath> cmrPaths = workflowService.getWorkflowPaths(instanceId);
		List<WorkflowPath> paths = new ArrayList<WorkflowPath>();
		for (org.alfresco.service.cmr.workflow.WorkflowPath cmrPath : cmrPaths)
		{
			paths.add(new WorkflowPath(cmrPath, workflowService));
		}
		return paths;
	}
	
	/**
	 * Getter for 'id' property
	 * 
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * Getter for 'description' property
	 * 
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Get state for 'active' property
	 * 
	 * @return the active
	 */
	public boolean isActive()
	{
		return active;
	}

	/**
	 * Getter for 'startDate' property
	 * 
	 * @return the startDate
	 */
	public Date getStartDate()
	{
		return startDate;
	}

	/**
	 * Getter for 'endDate' property
	 * 
	 * @return the endDate
	 */
	public Date getEndDate()
	{
		return endDate;
	}
	
	/**
	 * Cancel workflow instance
	 */
	public void cancel()
	{
		workflowService.cancelWorkflow(this.id);
	}
	
	/**
	 * Delete workflow instance
	 */
	public void delete()
	{
		workflowService.deleteWorkflow(this.id);
	}
}
