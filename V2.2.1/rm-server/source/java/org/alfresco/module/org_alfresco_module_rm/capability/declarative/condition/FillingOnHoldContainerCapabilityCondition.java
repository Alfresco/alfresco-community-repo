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
