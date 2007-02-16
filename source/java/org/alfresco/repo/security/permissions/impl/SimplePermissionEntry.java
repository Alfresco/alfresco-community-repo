/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.security.permissions.impl;

import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * A simple object representation of a permission entry.
 *  
 * @author andyh
 */
public final class SimplePermissionEntry extends AbstractPermissionEntry
{
    
    /*
     * The node ref to which the permissoin applies
     */
    private NodeRef nodeRef;
    
    /*
     * The permission reference - as a simple permission reference
     */
    private PermissionReference permissionReference;
    
    /*
     * The authority to which the permission aplies
     */
    private String authority;
    
    /*
     * The access mode for the permission
     */
    private AccessStatus accessStatus;
    
    
    
    public SimplePermissionEntry(NodeRef nodeRef, PermissionReference permissionReference, String authority, AccessStatus accessStatus)
    {
        super();
        this.nodeRef = nodeRef;
        this.permissionReference = permissionReference;
        this.authority = authority;
        this.accessStatus = accessStatus;
    }

    public PermissionReference getPermissionReference()
    {
        return permissionReference;
    }

    public String getAuthority()
    {
       return authority;
    }

    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    public boolean isDenied()
    {
        return accessStatus == AccessStatus.DENIED;
    }

    public boolean isAllowed()
    {
        return accessStatus == AccessStatus.ALLOWED;
    }

    public AccessStatus getAccessStatus()
    {
        return accessStatus;
    }

}
