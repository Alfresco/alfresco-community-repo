/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.cmis.mapping;

import org.alfresco.cmis.CMISAllowedActionEnum;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * Check-in operation is only allowed for PWC object per CMIS specification
 * 
 * @author Dmitry Velichkevich
 */
public class CanCheckInActionEvaluator extends AbstractActionEvaluator<NodeRef>
{
    private NodeService nodeService;

    private PermissionActionEvaluator permissionEvaluator;

    protected CanCheckInActionEvaluator(ServiceRegistry serviceRegistry)
    {
        super(serviceRegistry, CMISAllowedActionEnum.CAN_CHECKIN);

        permissionEvaluator = new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_CHECKIN, false, PermissionService.CHECK_IN);

        nodeService = serviceRegistry.getNodeService();
    }

    @Override
    public boolean isAllowed(NodeRef object)
    {
        if ((null != object) && nodeService.exists(object))
        {
            return permissionEvaluator.isAllowed(object) && nodeService.hasAspect(object, ContentModel.ASPECT_WORKING_COPY);
        }

        return false;
    }
}
