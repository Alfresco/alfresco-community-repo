package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Composite capability condition implementation that required at least one of the
 * capability conditions to be true.
 *
 * @author Roy Wetherall
 */
public class AtLeastOneCondition extends AbstractCapabilityCondition
{
    /** capability conditions */
    private List<CapabilityCondition> conditions;

    /**
     * @param conditions    capability conditions
     */
    public void setConditions(List<CapabilityCondition> conditions)
    {
        this.conditions = conditions;
    }
    
    /**
     * Don't use the transaction cache for the composite condition
     * 
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean evaluate(NodeRef nodeRef)
    {
        return evaluateImpl(nodeRef);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean evaluateImpl(NodeRef nodeRef)
    {
        boolean result = false;

        if (conditions != null)
        {
            for (CapabilityCondition condition : conditions)
            {
                if (condition.evaluate(nodeRef))
                {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }
}
