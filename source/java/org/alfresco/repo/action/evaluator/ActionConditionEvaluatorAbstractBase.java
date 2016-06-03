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
			((ActionConditionDefinitionImpl)this.actionConditionDefinition).setLocalizedParameterDefinitions(getLocalizedParameterDefinitions());
		}
		return this.actionConditionDefinition;
	}
	
	/**
     * @see org.alfresco.repo.action.evaluator.ActionConditionEvaluator#evaluate(org.alfresco.service.cmr.action.ActionCondition, org.alfresco.service.cmr.repository.NodeRef)
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
