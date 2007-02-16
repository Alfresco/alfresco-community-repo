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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.service.cmr.workflow;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.namespace.QName;


/**
 * Workflow Task Data Object
 * 
 * Represents a human-oriented task within an "in-flight" workflow instance
 * 
 * @author davidc
 */
public class WorkflowTask
{
    /** Unique id of Task */
    public String id;
    
    /** Task Name */
    public String name;
    
    /** Task Title (Localised) */
    public String title;

    /** Task Description (Localised) */
    public String description;
    
    /** Task State */
    public WorkflowTaskState state;
    
    /** Workflow path this Task is associated with */
    public WorkflowPath path;
    
    /** Task Definition */
    public WorkflowTaskDefinition definition;
    
    /** Task Properties as described by Task Definition */
    public Map<QName, Serializable> properties;
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        String propCount = (properties == null) ? "null" : "" + properties.size();
        return "WorkflowTask[id=" + id + ",title=" + title + ",state=" + state + ",props=" + propCount + ",def=" + definition + ",path=" + path.toString() + "]";
    }
}
