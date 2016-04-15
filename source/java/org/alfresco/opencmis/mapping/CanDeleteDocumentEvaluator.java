/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.opencmis.mapping;

import org.alfresco.opencmis.dictionary.CMISActionEvaluator;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.chemistry.opencmis.commons.enums.Action;

public class CanDeleteDocumentEvaluator extends AbstractActionEvaluator
{
    private CMISActionEvaluator currentVersionEvaluator;

    /**
     * Construct
     * 
     * @param serviceRegistry ServiceRegistry
     */
    protected CanDeleteDocumentEvaluator(ServiceRegistry serviceRegistry)
    {
        super(serviceRegistry, Action.CAN_DELETE_OBJECT);
        this.currentVersionEvaluator = new PermissionActionEvaluator(serviceRegistry,
                Action.CAN_DELETE_OBJECT, PermissionService.DELETE_NODE);
    }

    public boolean isAllowed(CMISNodeInfo nodeInfo)
    {
    	boolean isAllowed = true;

        if(!nodeInfo.isCurrentVersion() || nodeInfo.hasPWC())
        {
        	// not allowed if not current version or is checked out
            isAllowed = false;
        }
        else
        {
        	isAllowed = currentVersionEvaluator.isAllowed(nodeInfo);
        }

        return isAllowed;
    }

}
