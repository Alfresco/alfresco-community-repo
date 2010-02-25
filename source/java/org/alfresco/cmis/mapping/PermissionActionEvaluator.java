/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.cmis.mapping;

import org.alfresco.cmis.CMISAllowedActionEnum;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * Alfresco Permission based Action Evaluator
 * 
 * @author davidc
 */
public class PermissionActionEvaluator extends AbstractActionEvaluator<NodeRef>
{
    private String[] permissions;
    private PermissionService permissionService;

    /**
     * Construct
     * 
     * @param serviceRegistry
     * @param permission
     */
    protected PermissionActionEvaluator(ServiceRegistry serviceRegistry, CMISAllowedActionEnum action, String... permission)
    {
        super(serviceRegistry, action);
        this.permissions = permission;
        this.permissionService = serviceRegistry.getPermissionService();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISActionEvaluator#isAllowed(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isAllowed(NodeRef nodeRef)
    {
        for (String permission : permissions)
        {
            if (permissionService.hasPermission(nodeRef, permission) == AccessStatus.DENIED)
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
