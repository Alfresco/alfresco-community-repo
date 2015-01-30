/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * Base class for FillingOnChildrenCapabilityCondition and ReadOnChildrenCapabilityCondition
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public abstract class ChildPermissionCapabilityCondition extends AbstractCapabilityCondition
{
    protected boolean evaluateImpl(NodeRef nodeRef, String permission)
    {
        boolean result = true;

        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(nodeRef);
        for (ChildAssociationRef childAssociationRef : childAssocs)
        {
            NodeRef childRef = childAssociationRef.getChildRef();
            if (permissionService.hasPermission(childRef, permission) == AccessStatus.DENIED)
            {
                result = false;
                break;
            }
        }

        return result;
    }
}
