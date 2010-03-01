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
