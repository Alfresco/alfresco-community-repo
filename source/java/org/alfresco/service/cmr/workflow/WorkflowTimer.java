/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.service.cmr.workflow;

import java.util.Date;

import org.alfresco.api.AlfrescoPublicApi;

@AlfrescoPublicApi
public class WorkflowTimer
{
    /** Timer Id */
	@Deprecated
    public String id;

    /** Transition Name */
	@Deprecated
    public String name;

    /** Associated Workflow Path */
	@Deprecated
    public WorkflowPath path;
    
    /** Associated Workflow Task (if any) */
	@Deprecated
    public WorkflowTask task;
    
    /** Due Date */
	@Deprecated
    public Date dueDate;

    /** Error */
	@Deprecated
    public String error;
    
    
    
    public WorkflowTimer(String id, String name, WorkflowPath path,
			WorkflowTask task, Date dueDate, String error) {
		super();
		this.id = id;
		this.name = name;
		this.path = path;
		this.task = task;
		this.dueDate = dueDate;
		this.error = error;
	}

	/**
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the path
     */
    public WorkflowPath getPath()
    {
        return path;
    }

    /**
     * @return the task
     */
    public WorkflowTask getTask()
    {
        return task;
    }

    /**
     * @return the dueDate
     */
    public Date getDueDate()
    {
        return dueDate;
    }

    /**
     * @return the error
     */
    public String getError()
    {
        return error;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "WorkflowTimer[id=" + id + ",name=" + name + ",dueDate=" + dueDate + ",path=" + path + ",task=" + task + "]";
    }

}
