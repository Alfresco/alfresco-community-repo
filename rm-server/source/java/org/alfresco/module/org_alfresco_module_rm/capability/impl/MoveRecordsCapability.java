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
package org.alfresco.module.org_alfresco_module_rm.capability.impl;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.DeclarativeCapability;
import org.alfresco.module.org_alfresco_module_rm.capability.group.CreateCapability;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;

public class MoveRecordsCapability extends DeclarativeCapability
{
    @Override
    public int evaluate(NodeRef nodeRef)
    {
        // no way to know ...
        return AccessDecisionVoter.ACCESS_ABSTAIN;
    }
    
    public int evaluate(NodeRef movee, NodeRef destination)
    {
        int state = AccessDecisionVoter.ACCESS_ABSTAIN;

        if (rmService.isFilePlanComponent(destination))
        {
            state = checkRead(movee, true);
            if (state != AccessDecisionVoter.ACCESS_GRANTED)
            {
                return AccessDecisionVoter.ACCESS_DENIED;
            }

            if (rmService.isFilePlanComponent(movee) == true)
            {
                state = capabilityService.getCapability("Delete").evaluate(movee);
            }
            else
            {
                if (checkPermissionsImpl(movee, PermissionService.DELETE) == true)
                {
                    state = AccessDecisionVoter.ACCESS_GRANTED;
                }
            }

            if (state == AccessDecisionVoter.ACCESS_GRANTED)
            {
                QName type = nodeService.getType(movee);
                // now we know the node - we can abstain for certain types and aspects (eg, rm)
                CreateCapability createCapability = (CreateCapability)capabilityService.getCapability("Create");
                state = createCapability.evaluate(destination, movee, type, null);

                if (state == AccessDecisionVoter.ACCESS_GRANTED)
                {
                    if (rmService.isFilePlanComponent(movee) == true)
                    {
                        if (checkPermissionsImpl(movee, MOVE_RECORDS) == true)
                        {
                            return AccessDecisionVoter.ACCESS_GRANTED;
                        }
                    }
                    else
                    {
                        return AccessDecisionVoter.ACCESS_GRANTED;
                    }
                }
            }

            return AccessDecisionVoter.ACCESS_DENIED;
        }
        else
        {
            return AccessDecisionVoter.ACCESS_ABSTAIN;
        }
    }
}