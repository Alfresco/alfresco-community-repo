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
package org.alfresco.repo.action.executer;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.action.ActionDefinitionImpl;
import org.alfresco.repo.action.ParameterizedItemAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Rule action executor abstract base.
 * 
 * @author Roy Wetherall
 */
public abstract class ActionExecuterAbstractBase extends ParameterizedItemAbstractBase implements ActionExecuter
{
	/**
	 * Action definition
	 */
	protected ActionDefinition actionDefinition;
	
	/**
	 * Indicated whether the action is public or internal
	 */
	protected boolean publicAction = true;
    
    /** List of types and aspects for which this action is applicable */
    protected List<QName> applicableTypes = new ArrayList<QName>();
    
    /**
     * 
     */
    private String queueName = "";
    

	
	/**
	 * Init method	 
	 */
	public void init()
	{
		if (this.publicAction == true)
		{
			this.runtimeActionService.registerActionExecuter(this);
		}
	}
	
	/**
	 * Set whether the action is public or not.
	 * 
	 * @param publicAction	true if the action is public, false otherwise
	 */
	public void setPublicAction(boolean publicAction)
	{
		this.publicAction = publicAction;
	}
    
    /**
     * Set the list of types for which this action is applicable
     * 
     * @param applicableTypes   arry of applicable types
     */
    public void setApplicableTypes(String[] applicableTypes)
    {
        for (String type : applicableTypes)
        {
            this.applicableTypes.add(QName.createQName(type));
        }
    }
	
	/**
	 * Get rule action definition
	 * 
	 * @return	the action definition object
	 */
	public ActionDefinition getActionDefinition() 
	{
		if (this.actionDefinition == null)
		{
			this.actionDefinition = new ActionDefinitionImpl(this.name);
			((ActionDefinitionImpl)this.actionDefinition).setTitleKey(getTitleKey());
			((ActionDefinitionImpl)this.actionDefinition).setDescriptionKey(getDescriptionKey());
			((ActionDefinitionImpl)this.actionDefinition).setAdhocPropertiesAllowed(getAdhocPropertiesAllowed());
			((ActionDefinitionImpl)this.actionDefinition).setRuleActionExecutor(this.name);
			((ActionDefinitionImpl)this.actionDefinition).setParameterDefinitions(getParameterDefintions());
            ((ActionDefinitionImpl)this.actionDefinition).setApplicableTypes(this.applicableTypes);
		}
		return this.actionDefinition;
	}
	
	/**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    public void execute(Action action, NodeRef actionedUponNodeRef)
    {        
        // Check the mandatory properties
        checkMandatoryProperties(action, getActionDefinition());
        
        // Execute the implementation
        executeImpl(action, actionedUponNodeRef);        
    }
	
    /**
     * Execute the action implementation
     * 
     * @param action				the action
     * @param actionedUponNodeRef   the actioned upon node
     */
	protected abstract void executeImpl(Action action, NodeRef actionedUponNodeRef);
	
	/**
	 * Set the queueName which will execute this action
	 * if blank or null then the action will be executed on the "default" queue
	 * @param the name of the execution queue which should execute this action.
	 */ 
	public void setQueueName(String queueName) 
	{
		this.queueName = queueName;
	}

	public String getQueueName() {
		return queueName;
	}
}
