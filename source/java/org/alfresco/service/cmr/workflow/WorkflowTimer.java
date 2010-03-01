/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.service.cmr.workflow;

import java.util.Date;

public class WorkflowTimer
{
    /** Timer Id */
    public String id;

    /** Transition Name */
    public String name;

    /** Associated Workflow Path */
    public WorkflowPath path;
    
    /** Associated Workflow Task (if any) */
    public WorkflowTask task;
    
    /** Due Date */
    public Date dueDate;

    /** Error */
    public String error;
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "WorkflowTimer[id=" + id + ",name=" + name + ",dueDate=" + dueDate + ",path=" + path + ",task=" + task + "]";
    }

}
