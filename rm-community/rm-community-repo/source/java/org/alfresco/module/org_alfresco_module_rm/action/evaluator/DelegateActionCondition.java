 
package org.alfresco.module.org_alfresco_module_rm.action.evaluator;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionConditionEvaluatorAbstractBase;
import org.alfresco.repo.action.evaluator.ActionConditionEvaluator;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Records management action condition who's implementation is delegated to an existing 
 * action condition.
 * <p>
 * Useful for creating a RM version of an existing action condition implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class DelegateActionCondition extends RecordsManagementActionConditionEvaluatorAbstractBase
{
    /** Delegate action evaluator */
    private ActionConditionEvaluator actionConditionEvaluator;
    
    /**
     * @param actionEvaluator   action evaluator
     */
    public void setActionConditionEvaluator(ActionConditionEvaluator actionConditionEvaluator)
    {
        this.actionConditionEvaluator = actionConditionEvaluator;
    }

    /**
     * @see org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase#evaluateImpl(org.alfresco.service.cmr.action.ActionCondition, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected boolean evaluateImpl(ActionCondition actionCondition, NodeRef actionedUponNodeRef)
    {        
        return actionConditionEvaluator.evaluate(actionCondition, actionedUponNodeRef);
    }
    
    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#getParameterDefintions()
     */
    @Override
    protected List<ParameterDefinition> getParameterDefintions()
    {
        return actionConditionEvaluator.getActionConditionDefintion().getParameterDefinitions();
    }

}
