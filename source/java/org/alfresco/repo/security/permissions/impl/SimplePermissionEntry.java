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
    
    private int position;
    
    public SimplePermissionEntry(NodeRef nodeRef, PermissionReference permissionReference, String authority, AccessStatus accessStatus)
    {
       this(nodeRef, permissionReference, authority, accessStatus, 0);
    }
    
    public SimplePermissionEntry(NodeRef nodeRef, PermissionReference permissionReference, String authority, AccessStatus accessStatus, int position)
    {
        super();
        this.nodeRef = nodeRef;
        this.permissionReference = permissionReference;
        this.authority = authority;
        this.accessStatus = accessStatus;
        this.position = position;
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

    public int getPosition()
    {
        return position;
    }

    public boolean isInherited()
    {
        return position > 0;
    }

}
