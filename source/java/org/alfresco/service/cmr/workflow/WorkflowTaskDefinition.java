package org.alfresco.service.cmr.workflow;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.dictionary.TypeDefinition;


/**
 * Workflow Task Definition Data Object.
 * 
 * Represents meta-data for a Workflow Task.  The meta-data is described in terms
 * of the Alfresco Data Dictionary.
 * 
 * @author davidc
 */
@AlfrescoPublicApi
public class WorkflowTaskDefinition
{
    /** Unique id of Workflow Task Definition */
    @Deprecated
    public String id;

    /** Workflow Node this task created from */
    @Deprecated
    public WorkflowNode node;
    
    /** Task Metadata */
    @Deprecated
    public TypeDefinition metadata;

    public WorkflowTaskDefinition(String id, WorkflowNode node, TypeDefinition metadata)
    {
        this.id = id;
        this.node = node;
        this.metadata = metadata;
    }

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }
    
    /**
     * @return the metadata
     */
    public TypeDefinition getMetadata()
    {
        return metadata;
    }
    
    /**
     * @return the node
     */
    public WorkflowNode getNode()
    {
        return node;
    }
    
    /**
     * 
    * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "WorkflowTaskDefinition[id=" + id + ",metadata=" + metadata + "]";
    }
}
