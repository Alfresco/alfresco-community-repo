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

import org.alfresco.module.org_alfresco_module_rm.capability.impl.CreateCapability;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInvocation;

public class CreatePolicy extends AbstractBasePolicy
{
    @SuppressWarnings("rawtypes")
	public int evaluate(
            MethodInvocation invocation,
            Class[] params,
            ConfigAttributeDefinition cad)
    {
        NodeRef linkee = null;
        QName assocType = null;

        // get the destination node
        NodeRef destination = getTestNode(invocation, params, cad.getParameters().get(0), cad.isParent());

        if (cad.getParameters().size() > 1)
        {
            // get the linkee when present
            linkee = getTestNode(invocation, params, cad.getParameters().get(1), cad.isParent());

            // get the assoc type
            if(cad.getParameters().size() > 2)
            {
                assocType = getType(invocation, params, cad.getParameters().get(2), cad.isParent());
            }
        }

        return ((CreateCapability) getCapabilityService().getCapability("Create")).evaluate(destination, linkee, assocType);
    }

}
