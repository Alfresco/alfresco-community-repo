package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * Add to hold evaluator.
 * <p>
 * Determines whether the current user has access to any holds.
 * 
 * @author Roy Wetherall
 */
public class HoldCapabilityCondition extends AbstractCapabilityCondition
{  
    /** indicates whether to evaluate holds that the node is within or not within */
    private boolean includedInHold = false;;
    
    /** hold service */
    private HoldService holdService;
    
    /**
     * @param includedInHold    true if holds node within, false otherwise
     */
    public void setIncludedInHold(boolean includedInHold)
    {
        this.includedInHold = includedInHold;
    }
    
    /**
     * @param holdService   hold service
     */
    public void setHoldService(HoldService holdService)
    {
        this.holdService = holdService;
    }

    @Override
    public boolean evaluateImpl(NodeRef nodeRef)
    {
        boolean result = false;
      
        if (holdService.isHold(nodeRef))
        {
            result = AccessStatus.ALLOWED.equals(permissionService.hasPermission(nodeRef, RMPermissionModel.FILING));
        }
        else
        {
            List<NodeRef> holds = holdService.heldBy(nodeRef, includedInHold);
            for (NodeRef hold : holds)
            {
                // return true as soon as we find one hold we have filling permission on
                if (AccessStatus.ALLOWED.equals(permissionService.hasPermission(hold, RMPermissionModel.FILING)))
                {
                    result = true;
                    break;
                }
            }
        }
        
        return result;
    }
}
