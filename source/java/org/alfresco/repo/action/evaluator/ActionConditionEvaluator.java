package org.alfresco.repo.action.evaluator;

import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionConditionDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Action Condition Evaluator
 * 
 * @author Roy Wetherall
 */
public interface ActionConditionEvaluator
{
    /**
     * Get the action condition deinfinition
     * 
     * @return  the action condition definition
     */
	public ActionConditionDefinition getActionConditionDefintion();
	
    /**
     * Evaluate the action condition
     * 
     * @param actionCondition       the action condition
     * @param actionedUponNodeRef   the actioned upon node
     * @return                      true if the condition passes, false otherwise
     */
    public boolean evaluate(
			ActionCondition actionCondition,
            NodeRef actionedUponNodeRef);
}
