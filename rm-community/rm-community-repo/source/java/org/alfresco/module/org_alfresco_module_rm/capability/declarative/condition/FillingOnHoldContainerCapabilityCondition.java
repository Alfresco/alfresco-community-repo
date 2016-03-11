package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * Filling capability for hold condition.
 * 
 * @author Roy Wetherall
 * @since 2.3
 */
public class FillingOnHoldContainerCapabilityCondition extends AbstractCapabilityCondition
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean evaluateImpl(NodeRef nodeRef)
    {
        boolean result = false;
        NodeRef holdContainer = nodeRef;      
        
        // if we have a file plan, go get the hold container
        if (filePlanService.isFilePlan(nodeRef) == true)
        {
            holdContainer = filePlanService.getHoldContainer(nodeRef);
        }
        
        // ensure we are dealing with a hold container
        if (TYPE_HOLD_CONTAINER.equals(nodeService.getType(holdContainer)))
        {        
            if (permissionService.hasPermission(holdContainer, RMPermissionModel.FILE_RECORDS) != AccessStatus.DENIED)
            {
                result = true;
            }
        }
        
        return result;     
    }
}
