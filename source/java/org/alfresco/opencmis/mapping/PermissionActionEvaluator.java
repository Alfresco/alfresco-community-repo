/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.opencmis.mapping;

import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.chemistry.opencmis.commons.enums.Action;

/**
 * Alfresco Permission based Action Evaluator
 * 
 * @author davidc
 */
public class PermissionActionEvaluator extends AbstractActionEvaluator
{
    private String[] permissions;
    private PermissionService permissionService;

    /**
     * Construct
     * 
     * @param serviceRegistry
     * @param permission
     */
    protected PermissionActionEvaluator(ServiceRegistry serviceRegistry, Action action, String... permission)
    {
        super(serviceRegistry, action);
        this.permissions = permission;
        this.permissionService = serviceRegistry.getPermissionService();
    }

    public boolean isAllowed(CMISNodeInfo nodeInfo)
    {
        for (String permission : permissions)
        {
            if (permissionService.hasPermission(nodeInfo.getNodeRef(), permission) == AccessStatus.DENIED)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("PermissionActionEvaluator[action=").append(getAction());
        builder.append(", permissions=");
        for (String permission : permissions)
        {
            builder.append(permission).append(",");
        }
        builder.append("]");
        return builder.toString();
    }

}
