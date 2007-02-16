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
package org.alfresco.repo.action.evaluator;

import org.alfresco.repo.action.ActionConditionDefinitionImpl;
import org.alfresco.repo.action.ParameterizedItemAbstractBase;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionConditionDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Rule condition evaluator abstract base implementation.
 * 
 * @author Roy Wetherall
 */
public abstract class ActionConditionEvaluatorAbstractBase extends ParameterizedItemAbstractBase implements ActionConditionEvaluator
{	
    /**
     * Indicates whether the condition is public or not
     */
    private boolean publicCondition = true;
    
	/**
     * The action condition definition
	 */
	protected ActionConditionDefinition actionConditionDefinition;		
	
    /**
     * Initialise method
     */
	public void init()
	{
        if (this.publicCondition == true)
        {
            // Call back to the action service to register the condition
            this.runtimeActionService.registerActionConditionEvaluator(this);
        }
	}
    
    /**
     * Set the value that indicates whether a condition is public or not
     * 
     * @param publicCondition   true if the condition is public, false otherwise
     */
    public void setPublicCondition(boolean publicCondition)
    {
        this.publicCondition = publicCondition;
    }
	
    /**
     * Get the action condition definition.
     * 
     * @return  the action condition definition
     */
	public ActionConditionDefinition getActionConditionDefintion() 
	{
		if (this.actionConditionDefinition == null)
		{
			this.actionConditionDefinition = new ActionConditionDefinitionImpl(this.name);
			((ActionConditionDefinitionImpl)this.actionConditionDefinition).setTitleKey(getTitleKey());
			((ActionConditionDefinitionImpl)this.actionConditionDefinition).setDescriptionKey(getDescriptionKey());
			((ActionConditionDefinitionImpl)this.actionConditionDefinition).setAdhocPropertiesAllowed(getAdhocPropertiesAllowed());
			((ActionConditionDefinitionImpl)this.actionConditionDefinition).setConditionEvaluator(this.name);
			((ActionConditionDefinitionImpl)this.actionConditionDefinition).setParameterDefinitions(getParameterDefintions());
		}
		return this.actionConditionDefinition;
	}
	
	/**
     * @see org.alfresco.repo.action.evaluator.ActionConditionEvaluator#evaluate(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean evaluate(ActionCondition actionCondition, NodeRef actionedUponNodeRef)
    {
        checkMandatoryProperties(actionCondition, getActionConditionDefintion());
        boolean result = evaluateImpl(actionCondition, actionedUponNodeRef);
        if (actionCondition.getInvertCondition() == true)
        {
            result = !result;
        }
        return result;
    }
	
    /**
     * Evaluation implementation
     * 
     * @param actionCondition       the action condition
     * @param actionedUponNodeRef   the actioned upon node reference
     * @return                      the result of the condition evaluation
     */
	protected abstract boolean evaluateImpl(ActionCondition actionCondition, NodeRef actionedUponNodeRef);
}
