package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Helper condition that always fails.  Useful for deprecation of 
 * old capabilities.
 * 
 * @author Roy Wetherall
 */
public class FailCapabilityCondition extends AbstractCapabilityCondition
{
    @Override
    public boolean evaluateImpl(NodeRef nodeRef)
    {
        return false;
    }
}
