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

import java.io.Serializable;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.capability.impl.UpdateCapability;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInvocation;

public class UpdatePolicy extends AbstractBasePolicy
{

    @SuppressWarnings("unchecked")
	public int evaluate( 
            MethodInvocation invocation, 
            Class[] params, 
            ConfigAttributeDefinition cad)
    {
        NodeRef updatee = getTestNode(invocation, params, cad.getParameters().get(0), cad.isParent());
        QName aspectQName = null;
        if (cad.getParameters().size() > 1)
        {
            if (cad.getParameters().get(1) > -1)
            {
                aspectQName = getQName(invocation, params, cad.getParameters().get(1));
            }
        }
        Map<QName, Serializable> properties = null;
        if (cad.getParameters().size() > 2)
        {
            if (cad.getParameters().get(2) > -1)
            {
                properties = getProperties(invocation, params, cad.getParameters().get(2));
            }
        }
        
        UpdateCapability updateCapability = (UpdateCapability)capabilityService.getCapability("Update");
        return updateCapability.evaluate(updatee, aspectQName, properties);
    }

}