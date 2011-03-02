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
    @Deprecated
    public String id;
    
    /** Workflow Instance this path is part of */
    @Deprecated
    public WorkflowInstance instance;
    
    /** The Workflow Node the path is at */
    @Deprecated
    public WorkflowNode node;
    
    /** Is the path still active? */
    @Deprecated
    public boolean active;
    
    public WorkflowPath(String id, WorkflowInstance instance, WorkflowNode node, boolean active)
    {
        this.id = id;
        this.instance = instance;
        this.node = node;
        this.active = active;
    }

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @return the instance
     */
    public WorkflowInstance getInstance()
    {
        return instance;
    }

    /**
     * @return the node
     */
    public WorkflowNode getNode()
    {
        return node;
    }

    /**
     * @return the active
     */
    public boolean isActive()
    {
        return active;
    }

    /**
    * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "WorkflowPath[id=" + id + ",instance=" + instance.toString() + ",active=" + active + ",node=" + ((node != null) ? node.toString() : "null") + "]";
    }
}
