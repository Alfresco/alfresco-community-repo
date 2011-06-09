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

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.chemistry.opencmis.commons.enums.Action;

/**
 * Alfresco Permission based Action Evaluator
 * 
 * @author davidc
 */
public class CanCheckInActionEvaluator extends AbstractActionEvaluator<NodeRef>
{
    private PermissionActionEvaluator permissionEvaluator;
    private NodeService nodeService;

    /**
     * Construct
     * 
     * @param serviceRegistry
     * @param permission
     */
    protected CanCheckInActionEvaluator(ServiceRegistry serviceRegistry)
    {
        super(serviceRegistry, Action.CAN_CHECK_IN);
        permissionEvaluator = new PermissionActionEvaluator(serviceRegistry, Action.CAN_CHECK_IN,
                PermissionService.CHECK_IN);
        nodeService = serviceRegistry.getNodeService();
    }

    public boolean isAllowed(NodeRef nodeRef)
    {
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY))
        {
            return permissionEvaluator.isAllowed(nodeRef);
        }
        return false;
    }
}
