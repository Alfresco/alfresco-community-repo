/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
    @Deprecated
    public String id;
    
    /** Task Name */
    @Deprecated
    public String name;
    
    /** Task Title (Localised) */
    @Deprecated
    public String title;

    /** Task Description (Localised) */
    @Deprecated
    public String description;
    
    /** Task State */
    @Deprecated
    public WorkflowTaskState state;
    
    /** Workflow path this Task is associated with */
    @Deprecated
    public WorkflowPath path;
    
    /** Task Definition */
    @Deprecated
    public WorkflowTaskDefinition definition;
    
    /** Task Properties as described by Task Definition */
    @Deprecated
    public Map<QName, Serializable> properties;
    
    
    public WorkflowTask(String id,
                WorkflowTaskDefinition definition, 
                String name, String title, String description,
                WorkflowTaskState state, WorkflowPath path,
                Map<QName, Serializable> properties)
    {
        this.id = id;
        this.definition = definition;
        this.name = name;
        this.title = title;
        this.description = description;
        this.state = state;
        this.path = path;
        this.properties = properties;
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
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @return the state
     */
    public WorkflowTaskState getState()
    {
        return state;
    }

    /**
     * @return the path
     */
    public WorkflowPath getPath()
    {
        return path;
    }

    /**
     * @return the definition
     */
    public WorkflowTaskDefinition getDefinition()
    {
        return definition;
    }

    /**
     * @return the properties
     */
    public Map<QName, Serializable> getProperties()
    {
        return properties;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        String propCount = (properties == null) ? "null" : "" + properties.size();
        String pathString = path==null ? "null" : path.toString();
        return "WorkflowTask[id=" + id + ",title=" + title + ",state=" + state + ",props=" + propCount + ",def=" + definition + ",path=" + pathString + "]";
    }
}
