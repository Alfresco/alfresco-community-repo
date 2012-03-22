/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.capability.group;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.capability.impl.AbstractCapability;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Composite Declare capability
 * 
 * @author andyh
 */
public class DeclareCapability extends AbstractCapability
{
    /*
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Capability#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    public int evaluate(NodeRef declaree)
    {
        Capability recordsCapability = capabilityService.getCapability(RMPermissionModel.DECLARE_RECORDS);
        Capability inClosedCapability = capabilityService.getCapability(RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS);
        
        if (recordsCapability.hasPermissionRaw(declaree) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        if (inClosedCapability.hasPermissionRaw(declaree) == AccessDecisionVoter.ACCESS_GRANTED) 
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        return AccessDecisionVoter.ACCESS_DENIED;
    }

}
