package org.alfresco.repo.workflow.jscript;

import java.io.Serializable;

import org.alfresco.service.cmr.workflow.WorkflowTransition;

public class JscriptWorkflowTransition implements Serializable
{
	static final long serialVersionUID = 8370298400161156357L;
	
    /** Workflow transition id */
    private String id;

    /** Localised workflow transition title */
    private String title;
    
    /** Localised workflow transition description */
    private String description;
    
    /**
     * Constructor to create a new instance of this class
     * from scratch
     * 
     * @param id Workflow transition ID
     * @param title Workflow transition title
     * @param description Workflow transition description
     */
    public JscriptWorkflowTransition(String id, String title, String description)
    {
    	this.id = id;
    	this.title = title;
    	this.description = description;
    }
    
    /**
     * Constructor to create a new instance of this class from an existing 
     * instance of WorkflowTransition from the CMR workflow object model
     * 
     * @param transition CMR WorkflowTransition object from which
     * 		to create a new instance of this class
     */
    public JscriptWorkflowTransition(WorkflowTransition transition)
    {
    	this.id = transition.id;
    	this.title = transition.title;
    	this.description = transition.description;
    }
    
	/**
	 * Gets the value of the <code>id</code> property
	 *
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * Gets the value of the <code>title</code> property
	 *
	 * @return the title
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * Gets the value of the <code>description</code> property
	 *
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}
	
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "JscriptWorkflowTransition[id=" + id + ",title=" + title + ",description=" + description + "]";
    }
}
