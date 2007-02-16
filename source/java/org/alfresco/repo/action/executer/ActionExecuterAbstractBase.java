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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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

     
}
