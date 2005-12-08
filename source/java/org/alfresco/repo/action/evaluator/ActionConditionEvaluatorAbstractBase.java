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
