package org.alfresco.service.cmr.workflow;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Workflow Node Data Object
 * 
 * Represents a Node within the Workflow Definition.
 * 
 * @author davidc
 */
@AlfrescoPublicApi
public class WorkflowNode
{
    /** Workflow Node Name */
    @Deprecated
    public String name;
    
    /** Workflow Node Title (Localised) */
    @Deprecated
    public String title;
    
    /** Workflow Node Description (Localised) */
    @Deprecated
    public String description;

    /** Type of the Workflow Node (typically this is BPM engine specific - informational only */
    @Deprecated
    public String type;

    /** Does this Workflow Node represent human interaction? */
    @Deprecated
    public boolean isTaskNode;
    
    /** The transitions leaving this node (or null, if none) */
    @Deprecated
    public WorkflowTransition[] transitions;
    
    public WorkflowNode(String name,
                String title, String description, 
                String type, boolean isTaskNode,
                WorkflowTransition... transitions)
    {
        this.name = name;
        this.title = title;
        this.description = description;
        this.type = type;
        this.isTaskNode = isTaskNode;
        this.transitions = transitions;
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
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    /**
     * @return the isTaskNode
     */
    public boolean isTaskNode()
    {
        return isTaskNode;
    }

    /**
     * @return the transitions
     */
    public WorkflowTransition[] getTransitions()
    {
        return transitions;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        String transitionsArray = "{";
        for (int i = 0; i < transitions.length; i++)
        {
            transitionsArray += ((i == 0) ? "" : ",") + "'" + transitions[i] + "'";  
        }
        transitionsArray += "}";
        return "WorkflowNode[title=" + title + ",type=" + type + ",transitions=" + transitionsArray + "]";
    }
}
