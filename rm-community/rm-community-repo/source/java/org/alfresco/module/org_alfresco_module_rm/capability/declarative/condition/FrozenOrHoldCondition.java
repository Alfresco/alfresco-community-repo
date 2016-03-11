package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Indicates whether the node is either frozen or is a hold object
 * 
 * @author Roy Wetherall
 */
public class FrozenOrHoldCondition extends AbstractCapabilityCondition
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean evaluateImpl(NodeRef nodeRef)
    {
        FilePlanComponentKind kind = filePlanService.getFilePlanComponentKind(nodeRef);
        return (freezeService.isFrozen(nodeRef) || 
                (kind != null && kind.equals(FilePlanComponentKind.HOLD)));
    }

}
