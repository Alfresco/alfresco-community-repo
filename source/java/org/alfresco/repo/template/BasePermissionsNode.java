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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * Base class for Template API objects that support permissions.
 * 
 * @author Kevin Roast
 */
public abstract class BasePermissionsNode extends BaseContentNode implements TemplatePermissions
{
    private List<String> permissions = null;
    
    // ------------------------------------------------------------------------------
    // Security API 
    
    /**
     * @return List of permissions applied to this Node.
     *         Strings returned are of the format [ALLOWED|DENIED];[USERNAME|GROUPNAME];PERMISSION for example
     *         ALLOWED;kevinr;Consumer so can be easily tokenized on the ';' character.
     */
    public List<String> getPermissions()
    {
        if (this.permissions == null)
        {
            String userName = this.services.getAuthenticationService().getCurrentUserName();
            this.permissions = new ArrayList<String>(4);
            Set<AccessPermission> acls = this.services.getPermissionService().getAllSetPermissions(getNodeRef());
            for (AccessPermission permission : acls)
            {
                StringBuilder buf = new StringBuilder(64);
                buf.append(permission.getAccessStatus())
                .append(';')
                .append(permission.getAuthority())
                .append(';')
                .append(permission.getPermission());
                this.permissions.add(buf.toString());
            }
        }
        return this.permissions;
    }
    
    /**
     * @return true if this node inherits permissions from its parent node, false otherwise.
     */
    public boolean getInheritsPermissions()
    {
        return this.services.getPermissionService().getInheritParentPermissions(getNodeRef());
    }
    
    /**
     * @param permission        Permission name to test
     * 
     * @return true if the current user is granted the specified permission on the node
     */
    public boolean hasPermission(String permission)
    {
        return (this.services.getPermissionService().hasPermission(getNodeRef(), permission) == AccessStatus.ALLOWED);
    }
}
