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
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.chemistry.opencmis.commons.enums.Action;

/**
 * Alfresco Permission based Action Evaluator
 * 
 * @author florian.mueller
 */
public class CanCancelCheckOutActionEvaluator extends AbstractActionEvaluator
{
    private PermissionActionEvaluator permissionEvaluator;

    /**
     * Construct
     * 
     * @param serviceRegistry
     * @param permission
     */
    protected CanCancelCheckOutActionEvaluator(ServiceRegistry serviceRegistry)
    {
        super(serviceRegistry, Action.CAN_CANCEL_CHECK_OUT);
        permissionEvaluator = new PermissionActionEvaluator(serviceRegistry, Action.CAN_CANCEL_CHECK_OUT,
                PermissionService.CANCEL_CHECK_OUT);
    }

    public boolean isAllowed(CMISNodeInfo nodeInfo)
    {
        if (nodeInfo.isPWC())
        {
            return permissionEvaluator.isAllowed(nodeInfo);
        }
        
        return false;
    }
}
