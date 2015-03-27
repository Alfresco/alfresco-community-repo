/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
