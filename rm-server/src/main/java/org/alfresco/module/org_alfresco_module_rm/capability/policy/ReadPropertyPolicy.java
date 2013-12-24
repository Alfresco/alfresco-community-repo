/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

import net.sf.acegisecurity.vote.AccessDecisionVoter;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInvocation;

public class ReadPropertyPolicy extends AbstractBasePolicy
{

    @SuppressWarnings("rawtypes")
	public int evaluate( 
            MethodInvocation invocation, 
            Class[] params, 
            ConfigAttributeDefinition cad)
    {
        NodeRef nodeRef = getTestNode(invocation, params, cad.getParameters().get(0), cad.isParent());
        QName propertyQName = getQName(invocation, params, cad.getParameters().get(1));
        if(propertyQName.equals(RecordsManagementModel.PROP_HOLD_REASON))
        {
            return capabilityService.getCapability(RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE).evaluate(nodeRef);
        }
        else
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
    }

}