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

import java.util.Set;

/**
 * Interface for a registry of capabilities.
 * @author britt
 */
public interface AuthorityCapabilityRegistry
{
    /**
     * Get all known capabilities.
     * @return A list of all the capabilities.
     */
    public Set<String> getAllCapabilities();
    
    /**
     * Get all authorities know to the system.
     * @return
     */
    public Set<String> getAllAuthorities();
    
    /**
     * Get the integer id corresponding to the given capability.
     * @return The id.
     */
    public int getCapabilityID(String capability);
    
    /**
     * Get the name of a capability from it's unique id.
     * @param id
     * @return The capability name or null if the id is invalid.
     */
    public String getCapabilityName(int id);
    
    /**
     * Add a capability.
     * @param capability
     */
    public void addCapability(String capability);
    
    /**
     * Get the id for an authority.
     * @param authority
     * @return The id for the authority.
     */
    public int getAuthorityID(String authority);
    
    /**
     * Get the name from an authority id.
     * @param id The authority id.
     * @return The authority name.
     */
    public String getAuthorityName(int id);
    
    /**
     * Add a new authority.
     * @param authority The authority name.
     * @param parent The parent authority. May be null.
     */
    public void addAuthority(String authority, String parent);
    
    /**
     * Remove an authority completely from the system.
     * @param authority The authority to move.
     */
    public void removeAuthority(String authority);
    
    /**
     * Remove a containment relationship.
     * @param parent The parent.
     * @param child The child.
     */
    public void removeAuthorityChild(String parent, String child);
    
    /**
     * Get all authorities which are contained directly or transitively by the given authority.
     * @param authority The authority to check.
     * @return The contained authorities.
     */
    public Set<String> getContainedAuthorities(String authority);
    
    /**
     * Get all authorities which directly or indirectly contain the given authority.
     * @param authority The authority to check.
     * @return The container authorities.
     */
    public Set<String> getContainerAuthorities(String authority);
    
    /**
     * Get the case normalized version of authority.
     * @param authority The authority.
     * @return The case normalized version.
     */
    public String normalizeAuthority(String authority);
}
