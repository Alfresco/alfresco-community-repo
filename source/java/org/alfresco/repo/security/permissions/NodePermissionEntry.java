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
package org.alfresco.repo.security.permissions;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Encapsulate how permissions are globally inherited between nodes.
 * 
 * @author andyh
 */
public interface NodePermissionEntry
{
    /**
     * Get the node ref.
     * 
     * @return
     */
    public NodeRef getNodeRef();
    
    /**
     * Does the node inherit permissions from its primary parent?
     * 
     * @return
     */
    public boolean inheritPermissions();
    
    
    /**
     * Get the permission entries set for this node.
     * 
     * @return
     */
    public Set<? extends PermissionEntry> getPermissionEntries();
}
