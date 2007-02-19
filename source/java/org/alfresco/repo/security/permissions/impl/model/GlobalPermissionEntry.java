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
package org.alfresco.repo.security.permissions.impl.model;

import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.dom4j.Attribute;
import org.dom4j.Element;

public class GlobalPermissionEntry implements XMLModelInitialisable, PermissionEntry
{
    private static final String AUTHORITY = "authority";
    
    private static final String PERMISSION = "permission";
    
    private String authority;
    
    private PermissionReference permissionReference;
    
    public GlobalPermissionEntry()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    public void initialise(Element element, NamespacePrefixResolver nspr, PermissionModel permissionModel)
    {
        Attribute authorityAttribute = element.attribute(AUTHORITY);
        if(authorityAttribute != null)
        {
            authority = authorityAttribute.getStringValue();
        }
        Attribute permissionAttribute = element.attribute(PERMISSION);
        if(permissionAttribute != null)
        {
            permissionReference = permissionModel.getPermissionReference(null, permissionAttribute.getStringValue());
        }

    }
    
    public String getAuthority()
    {
        return authority;
    }
    
    public PermissionReference getPermissionReference()
    {
        return permissionReference;
    }

    public NodeRef getNodeRef()
    {
        return null;
    }

    public boolean isDenied()
    {
        return false;
    }

    public boolean isAllowed()
    {
        return true;
    }

    public AccessStatus getAccessStatus()
    {
        return AccessStatus.ALLOWED;
    }

}
