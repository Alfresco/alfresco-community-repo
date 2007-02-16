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

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * A single permission entry defined against a node.
 * 
 * @author andyh
 */
public interface PermissionEntry
{
    /**
     * Get the permission definition.
     * 
     * This may be null. Null implies that the settings apply to all permissions
     * 
     * @return
     */
    public PermissionReference getPermissionReference();

    /**
     * Get the authority to which this entry applies This could be the string
     * value of a username, group, role or any other authority assigned to the
     * authorisation.
     * 
     * If null then this applies to all.
     * 
     * @return
     */
    public String getAuthority();

    /**
     * Get the node ref for the node to which this permission applies.
     * 
     * This can only be null for a global permission 
     * 
     * @return
     */
    public NodeRef getNodeRef();

    /**
     * Is permissions denied?
     *
     */
    public boolean isDenied();

    /**
     * Is permission allowed?
     *
     */
    public boolean isAllowed();
    
    /**
     * Get the Access enum value
     * 
     * @return
     */
    public AccessStatus getAccessStatus();
}
