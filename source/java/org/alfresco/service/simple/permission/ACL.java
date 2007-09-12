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

package org.alfresco.service.simple.permission;

import java.io.Serializable;
import java.util.Set;

/**
 * Interface for ACLs. ACLs express the capabilities granted to 
 * different authorities (users, groups, or roles (one hopes that roles can go away as they are 
 * operationally just another name for a group)).  ACLs contain explicit entries made of
 * a capability and a list of agents plus an indication of whether the entry denies or allows
 * the capability. Entries that deny override any entries that allow. 
 * @author britt
 */
public interface ACL extends Serializable
{
    /**
     * Insert an allow entry.
     * Removes any denials explicitly for the authorities and capability given.
     * @param capability The capability to grant.
     * @param authorities The authorities granted the capability.
     */
    public void allow(String capability, String ... authorities);
    
    /**
     * Insert a deny entry.
     * Removes any allows explicitly for the authorities and capability given.
     * @param capability The capability to deny.
     * @param authorities The authorities to deny.
     */
    public void deny(String capability, String ... authorities);
    
    /**
     * Does the given authority have the given capability
     * @param authority The authority (user)
     * @param isOwner Is the authority the owner of the controlled entity.
     * @param capability The capability.
     * @return Whether the authority can.
     */
    public boolean can(String authority, boolean isOwner, String capability);
    
    /**
     * Get the capabilities for the given authority.
     * @param authority The authority.
     * @param isOwner is the authority the owner of the controlled entity.
     * @return A set of capabilities.
     */
    public Set<String> getCapabilities(String authority, boolean isOwner);
    
    /**
     * Get the authorities with the given capability.
     * @param capability The capability under consideration.
     * @return The set of authorities.
     */
    public Set<String> getAllowed(String capability);
    
    /**
     * Get a string representation of this ACL, suitable for persistence.
     * @return The string representation.
     */
    public String getStringRepresentation();
    
    /**
     * Should this ACL be inherited.
     * @return Whether it should.
     */
    public boolean inherits();
}
