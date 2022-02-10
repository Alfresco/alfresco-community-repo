/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
    private boolean includedInHold = false;
    
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
