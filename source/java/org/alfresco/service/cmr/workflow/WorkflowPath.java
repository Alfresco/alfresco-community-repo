/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.service.cmr.workflow;


/**
 * Workflow Path Data Object
 * 
 * Represents a path within an "in-flight" workflow instance.
 * 
 * Simple workflows consists of a single "root" path.  Multiple paths occur when a workflow
 * instance branches, therefore more than one concurrent path is taken.
 * 
 * @author davidc
 */
public class WorkflowPath
{
    /** Unique id of Workflow Path */
    public String id;
    
    /** Workflow Instance this path is part of */
    public WorkflowInstance instance;
    
    /** The Workflow Node the path is at */
    public WorkflowNode node;
    
    /** Is the path still active? */
    public boolean active;

    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "WorkflowPath[id=" + id + ",instance=" + instance.toString() + ",active=" + active + ",node=" + node.toString()+ "]";
    }
}
