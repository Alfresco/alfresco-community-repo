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
 * @author andyh
 */
public class DeleteCapability extends AbstractCapability
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Capability#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    public int evaluate(NodeRef deletee)
    {
        Capability schedRec = capabilityService.getCapability(RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION);
        Capability destroy = capabilityService.getCapability(RMPermissionModel.DESTROY_RECORDS);
        Capability delete = capabilityService.getCapability(RMPermissionModel.DELETE_RECORDS);
        Capability desfileplan = capabilityService.getCapability(RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA);
        Capability desfolder = capabilityService.getCapability(RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS);
        
        if (schedRec.evaluate(deletee) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        if (destroy.evaluate(deletee) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        if (delete.evaluate(deletee) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        if (desfileplan.evaluate(deletee) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        if (desfolder.evaluate(deletee, null) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        return AccessDecisionVoter.ACCESS_DENIED;
    }

}
