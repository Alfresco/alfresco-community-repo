package org.alfresco.module.org_alfresco_module_rm.action.evaluator;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionConditionEvaluatorAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;

/**
 * Records management evaluator base implementation that delegates to a configured capability condition
 * implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class CapabilityConditionEvaluator extends RecordsManagementActionConditionEvaluatorAbstractBase
{
    /** Capability Condition */
    private CapabilityCondition capabilityCondition;
    
    /**
     * @param capabilityCondition   capability condition
     */
    public void setCapabilityCondition(CapabilityCondition capabilityCondition)
    {
        this.capabilityCondition = capabilityCondition;
    }

    /**
     * @see org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase#evaluateImpl(org.alfresco.service.cmr.action.ActionCondition, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected boolean evaluateImpl(ActionCondition actionCondition, NodeRef actionedUponNodeRef)
    {
        // check a capability condition has been set and delegate
        ParameterCheck.mandatory("capabilityCondition", capabilityCondition);        
        return capabilityCondition.evaluate(actionedUponNodeRef);
    }
}
