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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Action implementation
 * 
 * @author Roy Wetherall
 */
public class ActionImpl extends ParameterizedItemImpl 
						implements Serializable, Action
{
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 3258135760426186548L;
    
    /** The node reference for the action */
    private NodeRef nodeRef;
    
    /**
     * The title 
     */
    private String title;
    
    /**
     * The description 
     */
    private String description;

    /**
     * Inidcates whether the action should be executed asynchronously or not
     */
	private boolean executeAsynchronously = false;
	
	/**
	 * The compensating action
	 */
	private Action compensatingAction;
    
    /**
     * The created date 
     */
    private Date createdDate;
    
    /**
     * The creator
     */
    private String creator;
    
    /**
     * The modified date
     */
    private Date modifiedDate;
    
    /**
     * The modifier
     */
    private String modifier;
    
    /**
     * Rule action definition name
     */
    private String actionDefinitionName;
    
    /**
     * The run as user name
     */
    private String runAsUserName;
    
    /**
     * The chain of actions that have lead to this action
     */
    private Set<String> actionChain;
    
    /**
     * Action conditions
     */
    private List<ActionCondition> actionConditions = new ArrayList<ActionCondition>();

    /**
     * Constructor
     * 
     * @param nodeRef               the action node reference (null if not saved)
     * @param id					the action id
     * @param actionDefinitionName  the name of the action definition
     */
    public ActionImpl(NodeRef nodeRef, String id, String actionDefinitionName)
    {
        this(nodeRef, id, actionDefinitionName, null);       
    }

    /**
     * Constructor 
     * 
     * @param nodeRef               the action node reference (null if not saved)
     * @param id					the action id
     * @param actionDefinitionName  the action definition name
     * @param parameterValues       the parameter values
     */
    public ActionImpl(
            NodeRef nodeRef,
    		String id,
    		String actionDefinitionName, 
            Map<String, Serializable> parameterValues)
    {
        super(id, parameterValues);
        this.nodeRef = nodeRef;
        this.actionDefinitionName = actionDefinitionName;
    }
    
    /**
     * @see org.alfresco.service.cmr.action.Action#getTitle()
     */
    public String getTitle()
	{
		return this.title;
	}

    /**
     * @see org.alfresco.service.cmr.action.Action#setTitle(java.lang.String)
     */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * @see org.alfresco.service.cmr.action.Action#getDescription()
	 */
	public String getDescription()
	{
		return this.description;
	}

	/**
	 * @see org.alfresco.service.cmr.action.Action#setDescription(java.lang.String)
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	/**
	 * @see org.alfresco.service.cmr.action.Action#getExecuteAsychronously()
	 */
	public boolean getExecuteAsychronously()
	{
		return this.executeAsynchronously ;
	}

	/**
	 * @see org.alfresco.service.cmr.action.Action#setExecuteAsynchronously(boolean)
	 */
	public void setExecuteAsynchronously(boolean executeAsynchronously)
	{
		this.executeAsynchronously = executeAsynchronously;
	}
	
	/**
	 * @see org.alfresco.service.cmr.action.Action#getCompensatingAction()
	 */
	public Action getCompensatingAction()
	{
		return this.compensatingAction;
	}
	
	/**
	 * @see org.alfresco.service.cmr.action.Action#setCompensatingAction(org.alfresco.service.cmr.action.Action)
	 */
	public void setCompensatingAction(Action action)
	{
		this.compensatingAction = action;
	}

	/**
	 * @see org.alfresco.service.cmr.action.Action#getCreatedDate()
	 */
	public Date getCreatedDate()
	{
		return this.createdDate;
	}
	
	/**
	 * Set the created date
	 * 
	 * @param createdDate  the created date
	 */
	public void setCreatedDate(Date createdDate)
	{
		this.createdDate = createdDate;
	}

	/**
	 * @see org.alfresco.service.cmr.action.Action#getCreator()
	 */
	public String getCreator()
	{
		return this.creator;
	}
	
	/**
	 * Set the creator
	 * 
	 * @param creator  the creator
	 */
	public void setCreator(String creator)
	{
		this.creator = creator;
	}

	/**
	 * @see org.alfresco.service.cmr.action.Action#getModifiedDate()
	 */
	public Date getModifiedDate()
	{
		return this.modifiedDate;
	}
	
	/**
	 * Set the modified date
	 * 
	 * @param modifiedDate	the modified date
	 */
	public void setModifiedDate(Date modifiedDate)
	{
		this.modifiedDate = modifiedDate;
	}

	/**
	 * @see org.alfresco.service.cmr.action.Action#getModifier()
	 */
	public String getModifier()
	{
		return this.modifier;
	}
	
	/**
	 * Set the modifier
	 * 
	 * @param modifier	the modifier
	 */
	public void setModifier(String modifier)
	{
		this.modifier = modifier;
	}
    
	/**
	 * @see org.alfresco.service.cmr.action.Action#getActionDefinitionName()
	 */
    public String getActionDefinitionName()
    {
    	return this.actionDefinitionName;
    }

    /**
     * @see org.alfresco.service.cmr.action.Action#hasActionConditions()
     */
	public boolean hasActionConditions()
	{
		return (this.actionConditions.isEmpty() == false);
	}

	/**
	 * @see org.alfresco.service.cmr.action.Action#indexOfActionCondition(org.alfresco.service.cmr.action.ActionCondition)
	 */
	public int indexOfActionCondition(ActionCondition actionCondition)
	{
		return this.actionConditions.indexOf(actionCondition);
	}

	/**
	 * @see org.alfresco.service.cmr.action.Action#getActionConditions()
	 */
	public List<ActionCondition> getActionConditions()
	{
		return this.actionConditions;
	}

	/**
	 * @see org.alfresco.service.cmr.action.Action#getActionCondition(int)
	 */
	public ActionCondition getActionCondition(int index)
	{
		return this.actionConditions.get(index);
	}

	/**
	 * @see org.alfresco.service.cmr.action.Action#addActionCondition(org.alfresco.service.cmr.action.ActionCondition)
	 */
	public void addActionCondition(ActionCondition actionCondition)
	{
		this.actionConditions.add(actionCondition);
	}

	/**
	 * @see org.alfresco.service.cmr.action.Action#addActionCondition(int, org.alfresco.service.cmr.action.ActionCondition)
	 */
	public void addActionCondition(int index, ActionCondition actionCondition)
	{
		this.actionConditions.add(index, actionCondition);
	}

	/**
	 * @see org.alfresco.service.cmr.action.Action#setActionCondition(int, org.alfresco.service.cmr.action.ActionCondition)
	 */
	public void setActionCondition(int index, ActionCondition actionCondition)
	{
		this.actionConditions.set(index, actionCondition);
	}

	/**
	 * @see org.alfresco.service.cmr.action.Action#removeActionCondition(org.alfresco.service.cmr.action.ActionCondition)
	 */
	public void removeActionCondition(ActionCondition actionCondition)
	{
		this.actionConditions.remove(actionCondition);
	}

	/**
	 * @see org.alfresco.service.cmr.action.Action#removeAllActionConditions()
	 */
	public void removeAllActionConditions()
	{
		this.actionConditions.clear();
	}	
    
    /**
     * Set the action chain
     * 
     * @param actionChain   the list of actions that lead to this action
     */
    public void setActionChain(Set<String> actionChain)
    {
        this.actionChain = actionChain;
    }
    
    /**
     * Get the action chain 
     * 
     * @return  the list of actions that lead to this action
     */
    public Set<String> getActionChain()
    {
        return actionChain;
    }

    public String getRunAsUser()
    {
        return this.runAsUserName;
    }

    public void setRunAsUser(String runAsUserName)
    {
        this.runAsUserName = runAsUserName;
    }

    /**
     * @see org.alfresco.service.cmr.action.Action#getNodeRef()
     */
    public NodeRef getNodeRef()
    {
        return this.nodeRef;
    }
    
    /**
     * Set the node reference
     * 
     * @param nodeRef   the node reference
     */
    public void setNodeRef(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef;
    }
}
