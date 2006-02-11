/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
     * The owning node reference
     */
    private NodeRef owningNodeRef;
    
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
     * @param id					the action id
     * @param actionDefinitionName  the name of the action definition
     */
    public ActionImpl(String id, String actionDefinitionName, NodeRef owningNodeRef)
    {
        this(id, actionDefinitionName, owningNodeRef, null);
    }

    /**
     * Constructor 
     * 
     * @param id					the action id
     * @param actionDefinitionName  the action definition name
     * @param parameterValues       the parameter values
     */
    public ActionImpl(
    		String id,
    		String actionDefinitionName, 
            NodeRef owningNodeRef,
            Map<String, Serializable> parameterValues)
    {
        super(id, parameterValues);
        this.actionDefinitionName = actionDefinitionName;
        this.owningNodeRef = owningNodeRef;
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
     * @see org.alfresco.service.cmr.action.Action#getOwningNodeRef()
     */
    public NodeRef getOwningNodeRef()
    {
        return this.owningNodeRef;
    }
    
    public void setOwningNodeRef(NodeRef owningNodeRef)
    {
        this.owningNodeRef = owningNodeRef;
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
}
