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
package org.alfresco.module.org_alfresco_module_rm.capability.policy;

import java.util.Arrays;
import java.util.List;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.aopalliance.intercept.MethodInvocation;

public class UpdatePropertiesPolicy extends AbstractBasePolicy
{
    @SuppressWarnings("rawtypes")
	public int evaluate(
            MethodInvocation invocation,
            Class[] params,
            ConfigAttributeDefinition cad)
    {
        NodeRef nodeRef = getTestNode(invocation, params, cad.getParameters().get(0), cad.isParent());
        int result = getCapabilityService().getCapability("UpdateProperties").evaluate(nodeRef);

        if (AccessDecisionVoter.ACCESS_GRANTED != result)
        {
            if (checkEligablePermissions(nodeRef))
            {
                result = AccessDecisionVoter.ACCESS_GRANTED;
            }
        }

        return result;
    }

    private boolean checkEligablePermissions(NodeRef nodeRef)
    {
        boolean result = false;
        List<String> permissions = Arrays.asList(
                RMPermissionModel.CREATE_RECORDS,
                RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS,
                RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA
        );

        NodeRef filePlan = getFilePlanService().getFilePlan(nodeRef);
        for (String permission : permissions)
        {
            if (permissionService.hasPermission(filePlan, permission) == AccessStatus.ALLOWED)
            {
                result = true;
                break;
            }
        }

        return result;
    }
}