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

import net.sf.acegisecurity.vote.AccessDecisionVoter;
import org.alfresco.module.org_alfresco_module_rm.capability.impl.ViewRecordsCapability;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.aopalliance.intercept.MethodInvocation;

public class AssocPolicy extends AbstractBasePolicy
{

    @SuppressWarnings("rawtypes")
	public int evaluate(
            MethodInvocation invocation,
            Class[] params,
            ConfigAttributeDefinition cad)
    {
        NodeRef source = null;
        if (cad.getParameters().get(0) > -1)
        {
            source = getTestNode(invocation, params, cad.getParameters().get(0), cad.isParent());
        }

        NodeRef target = null;
        if (cad.getParameters().get(1) > -1)
        {
            target = getTestNode(invocation, params, cad.getParameters().get(1), cad.isParent());
        }

        if (source != null && target != null)
        {
            // check the source node ref is a file plan component
            if (nodeService.hasAspect(source, RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT))
            {
                return getCapabilityService().getCapability(ViewRecordsCapability.NAME).evaluate(source);
            }
            else
            {
                final boolean isFilePlanComponent = nodeService.hasAspect(target, RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT);
                final boolean hasViewRecordCapability = getCapabilityService().hasCapability(target, ViewRecordsCapability.NAME);
                // allow association between a source non rm node and an rm node if the user
                // has ViewRecordsCapability on the RM target node and  write properties on the dm node
                if ( isFilePlanComponent &&
                        hasViewRecordCapability &&
                        permissionService.hasPermission(source, PermissionService.WRITE_PROPERTIES).equals(AccessStatus.ALLOWED))
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

