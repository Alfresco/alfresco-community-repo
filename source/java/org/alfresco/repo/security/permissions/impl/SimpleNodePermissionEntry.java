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

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A simple object representation of a node permission entry
 * 
 * @author andyh
 */
public final class SimpleNodePermissionEntry extends AbstractNodePermissionEntry implements Serializable
{
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 8157870444595023347L;

    /*
     * The node 
     */
    private NodeRef nodeRef;
    
    /*
     * Are permissions inherited?
     */
    private boolean inheritPermissions;
    
    /*
     * The set of permission entries.
     */
    private List<? extends PermissionEntry> permissionEntries;
    
    
    public SimpleNodePermissionEntry(NodeRef nodeRef, boolean inheritPermissions, List<? extends PermissionEntry> permissionEntries)
    {
        super();
        this.nodeRef = nodeRef;
        this.inheritPermissions = inheritPermissions;
        this.permissionEntries = permissionEntries;
    }

    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    public boolean inheritPermissions()
    {
        return inheritPermissions;
    }

    public List<? extends PermissionEntry> getPermissionEntries()
    {
       return permissionEntries;
    }

}
