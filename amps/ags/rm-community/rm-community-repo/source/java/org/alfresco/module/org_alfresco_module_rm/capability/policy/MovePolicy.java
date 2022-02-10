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

package org.alfresco.module.org_alfresco_module_rm.capability.policy;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.aopalliance.intercept.MethodInvocation;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

public class MovePolicy extends AbstractBasePolicy
{

    @SuppressWarnings("rawtypes")
	public int evaluate(
            MethodInvocation invocation,
            Class[] params,
            ConfigAttributeDefinition cad)
    {

        NodeRef movee = null;
        if (cad.getParameters().get(0) > -1)
        {
            movee = getTestNode(invocation, params, cad.getParameters().get(0), cad.isParent());
        }

        NodeRef destination = null;
        if (cad.getParameters().get(1) > -1)
        {
            destination = getTestNode(invocation, params, cad.getParameters().get(1), cad.isParent());
        }

        if ((movee != null) && (destination != null))
        {
            // check that we aren't trying to move something from the DM into RM
            if (nodeService.hasAspect(movee, RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT))
            {
                return getCapabilityService().getCapability("Move").evaluate(movee, destination);
            }
            else
            {
                if (nodeService.hasAspect(destination, RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT) &&
                    permissionService.hasPermission(destination, RMPermissionModel.FILING).equals(AccessStatus.ALLOWED))
                {
                    return AccessDecisionVoter.ACCESS_GRANTED;
                }
                else
                {
                    return AccessDecisionVoter.ACCESS_DENIED;
                }
            }
        }
        else
        {
            return AccessDecisionVoter.ACCESS_DENIED;
        }

    }
}
