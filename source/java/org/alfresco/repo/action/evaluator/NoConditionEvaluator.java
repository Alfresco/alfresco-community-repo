package org.alfresco.repo.action.evaluator;

import java.util.List;

import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * No condition evaluator implementation.
 * 
 * @author Roy Wetherall
 */
public class NoConditionEvaluator extends ActionConditionEvaluatorAbstractBase
{
    /**
     * Evaluator constants
     */
    public static final String NAME = "no-condition";     
    
    /**
     * @see org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase#evaluateImpl(org.alfresco.service.cmr.action.ActionCondition, org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean evaluateImpl(ActionCondition ruleCondition, NodeRef actionedUponNodeRef)
    {
        return true;
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        // No parameters to add
    }

}
