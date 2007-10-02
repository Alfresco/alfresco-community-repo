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

import org.alfresco.service.cmr.workflow.WorkflowTransition;

public class JscriptWorkflowTransition implements Serializable
{
	static final long serialVersionUID = 8370298400161156357L;
	
    /** Workflow transition id */
    private String id;

    /** Localised workflow transition title */
    private String title;
    
    /** Localised workflow transition description */
    private String description;
    
    /**
     * Constructor to create a new instance of this class
     * from scratch
     * 
     * @param id Workflow transition ID
     * @param title Workflow transition title
     * @param description Workflow transition description
     */
    JscriptWorkflowTransition(String id, String title, String description)
    {
    	this.id = id;
    	this.title = title;
    	this.description = description;
    }
    
    /**
     * Constructor to create a new instance of this class from an existing 
     * instance of WorkflowTransition from the CMR workflow object model
     * 
     * @param transition CMR WorkflowTransition object from which
     * 		to create a new instance of this class
     */
    JscriptWorkflowTransition(WorkflowTransition transition)
    {
    	this.id = transition.id;
    	this.title = transition.title;
    	this.description = transition.description;
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
	
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "JscriptWorkflowTransition[id=" + id + ",title=" + title + ",description=" + description + "]";
    }
}
