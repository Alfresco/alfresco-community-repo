package org.alfresco.service.cmr.workflow;

import org.alfresco.api.AlfrescoPublicApi;

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
@AlfrescoPublicApi
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
