package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Indicates whether an item is held or not.
 * <p>
 * A hold object is by definition considered to be held.
 * 
 * @author Roy Wetherall
 */
public class FrozenCapabilityCondition extends AbstractCapabilityCondition
{
    /** indicates whether children should be checked */
    private boolean checkChildren = false;
    
    /** hold service */
    private HoldService holdService;

    /**
     * @param checkChildren true to check children, false otherwise
     */
    public void setCheckChildren(boolean checkChildren)
    {
        this.checkChildren = checkChildren;
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
        
        // check whether we are working with a hold or not
        if (holdService.isHold(nodeRef))
        {
            result = true;
        }
        else
        {
            result = freezeService.isFrozen(nodeRef);
            if (!result && checkChildren)
            {
                result = freezeService.hasFrozenChildren(nodeRef);
            }
        }
        return result;
    }

}
