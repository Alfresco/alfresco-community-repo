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

package org.alfresco.repo.workflow.jscript;

import java.io.Serializable;
import java.util.ArrayList;

import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.workflow.WorkflowNode;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.mozilla.javascript.Scriptable;

/**
 * Represents a Workflow Node within the Workflow Definition.
 */
public class JscriptWorkflowNode implements Serializable
{
	static final long serialVersionUID = 6785972019256246499L;
	
    /** Name of workflow node */
    private final String name;
    
    /** Localised title of workflow node */
    private final String title;
    
    /** Localised description of workflow node */
    private final String description;

    /** <code>true</code> if this workflow node is associated with a workflow task */
    private final boolean isTaskNode;
    
    /** The transitions leaving this node (or null, if none) */
    private final ArrayList<JscriptWorkflowTransition> transitions;
    
	/** Root scripting scope for this object */
	private final Scriptable scope;
	
	/** Service Registry */
	private final ServiceRegistry serviceRegistry;
	
    /**
     * Constructor to create a new instance of this class
     * 
     * @param name Name of workflow node
     * @param title Title of workflow node
     * @param description Description of workflow node
     * @param isTaskNode <code>true</code> if this node is a task node
     * @param transitions transitions leaving this node (null if none)
     * @param scope root scripting scope for this object
     * @param serviceRegistry service registry object
     */
    public JscriptWorkflowNode(String name, String title, String description,
    	boolean isTaskNode, ArrayList<JscriptWorkflowTransition> transitions,
    	Scriptable scope, ServiceRegistry serviceRegistry)
	{
    	this.name = name;
    	this.title = title;
    	this.description = description;
    	this.isTaskNode = isTaskNode;
    	this.transitions = transitions;
    	this.scope = scope;
    	this.serviceRegistry = serviceRegistry;
	}
    
    /**
     * Constructor to create a new instance of this class from an 
     * existing instance of WorkflowNode from the CMR workflow 
     * object model
     * 
     * @param workflowNode CMR workflow node object to create 
     * 		new <code>JscriptWorkflowNode</code> from
     * @param scope root scripting scope for this newly instantiated object
     * @param serviceRegistry service registry object
     */
    public JscriptWorkflowNode(WorkflowNode workflowNode, Scriptable scope, ServiceRegistry serviceRegistry)
    {
    	this.name = workflowNode.name;
    	this.title = workflowNode.title;
    	this.description = workflowNode.description;
    	this.isTaskNode = workflowNode.isTaskNode;
    	this.transitions = new ArrayList<JscriptWorkflowTransition>();
    	WorkflowTransition[] cmrWorkflowTransitions = workflowNode.transitions;
    	for (WorkflowTransition cmrWorkflowTransition : cmrWorkflowTransitions)
    	{
    		transitions.add(new JscriptWorkflowTransition(cmrWorkflowTransition));
    	}
    	this.scope = scope;
    	this.serviceRegistry = serviceRegistry;
    }

	/**
	 * Gets the value of the <code>name</code> property
	 *
	 * @return the name
	 */
	public String getName()
	{
		return name;
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

	/**
	 * Gets the value of the <code>isTaskNode</code> property
	 *
	 * @return the isTaskNode
	 */
	public boolean isTaskNode()
	{
		return isTaskNode;
	}

	/**
	 * Gets the value of the <code>transitions</code> property
	 *
	 * @return the transitions
	 */
	public Scriptable getTransitions()
	{
		Scriptable transitionsScriptable = (Scriptable)new ValueConverter().convertValueForScript(
			this.serviceRegistry, this.scope, null, this.transitions);
		
		return transitionsScriptable;
	}
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        String transitionsList = "{";
        for (JscriptWorkflowTransition transition : this.transitions)
        {
            transitionsList += ", '" + transition + "'";  
        }
        transitionsList += "}";
        return "WorkflowNode[title=" + title + ",transitions=" + transitionsList + "]";
    }
}
