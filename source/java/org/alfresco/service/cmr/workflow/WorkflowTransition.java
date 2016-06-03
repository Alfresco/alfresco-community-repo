package org.alfresco.service.cmr.workflow;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Workflow Transition.
 * 
 * @author davidc
 */
@AlfrescoPublicApi
public class WorkflowTransition
{
    /** Transition Id */
    @Deprecated
    public String id;

    /** Transition Title (Localised) */
    @Deprecated
    public String title;
    
    /** Transition Description (Localised) */
    @Deprecated
    public String description;
    
    /** Is this the default transition */
    @Deprecated
    public boolean isDefault;

    public WorkflowTransition(String id, String title, String description, boolean isDefault)
    {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isDefault = isDefault;
    }

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
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
     * @return the isDefault
     */
    public boolean isDefault()
    {
        return isDefault;
    }
    
    /**
    * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "WorkflowTransition[id=" + id + ",title=" + title + "]";
    }
}
