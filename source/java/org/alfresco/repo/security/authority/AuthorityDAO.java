/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.security.authority;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;

public interface AuthorityDAO
{
    /**
     * Add an authority to another.
     * 
     * @param parentName
     * @param childName
     */
    void addAuthority(String parentName, String childName);

    /**
     * Create an authority.
     * 
     * @param name
     * @param authorityDisplayName
     * @param authorityZone
     */
    void createAuthority(String name, String authorityDisplayName, String authorityZone);

    /**
     * Delete an authority.
     * 
     * @param name
     */
    void deleteAuthority(String name);

    /**
     * Get all root authorities.
     * 
     * @param type
     * @return
     */
    Set<String> getAllRootAuthorities(AuthorityType type);

    /**
     * Get contained authorities.
     * 
     * @param type
     * @param name
     * @param immediate
     * @return
     */
    Set<String> getContainedAuthorities(AuthorityType type, String name, boolean immediate);

    /**
     * Remove an authority.
     * 
     * @param parentName
     * @param childName
     */
    void removeAuthority(String parentName, String childName);

    /**
     * Get the authorities that contain the one given.
     * 
     * @param type
     * @param name
     * @param immediate
     * @return
     */
    Set<String> getContainingAuthorities(AuthorityType type, String name, boolean immediate);

    /**
     * Get all authorities by type
     * 
     * @param type
     * @return
     */
    Set<String> getAllAuthorities(AuthorityType type);
    
    /**
     * Test if an authority already exists.
     * 
     * @param name
     * @return
     */
    boolean authorityExists(String name);
    
    /**
     * Get a node ref for the authority if one exists
     * 
     * @param name
     * @return
     */
    NodeRef getAuthorityNodeRefOrNull(String name);

    /**
     * Gets the name for the given authority node
     * 
     * @param authorityRef  authority node
     * @return  name
     */
    public String getAuthorityName(NodeRef authorityRef);

    /**
     * Get the display name for an authority
     * 
     * @param authorityName
     * @return the display name
     */
    String getAuthorityDisplayName(String authorityName);

    /**
     * Set the display name for an authority
     * 
     * @param authorityName
     * @param authorityDisplayName
     */
    void setAuthorityDisplayName(String authorityName, String authorityDisplayName);

    /**
     * Find authorities by pattern.
     * 
     * @param type
     * @param namePattern
     * @return
     */
    public Set<String> findAuthorities(AuthorityType type, String namePattern);

    /**
     * Gets or creates an authority zone node with the specified name
     * 
     * @param zoneName
     *            the zone name
     * @return reference to the zone node
     */
    public NodeRef getOrCreateZone(String zoneName);
    
    /**
     * Gets the name of the zone containing the specified authority.
     * 
     * @param name
     *            the authority long name
     * @return the the name of the zone containing the specified authority, {@link AuthorityService#DEFAULT_ZONE} if the
     *         authority exists but has no zone, or <code>null</code> if the authority does not exist.
     */
    public String getAuthorityZone(String name);
    
    /**
     * Gets the names of all authorities in a zone, optionally filtered by type.
     * 
     * @param zoneName
     *            the zone name
     * @param type
     *            the authority type to filter by or <code>null</code> for all authority types
     * @return the names of all authorities in a zone, optionally filtered by type
     */
    public Set<String> getAllAuthoritiesInZone(String zoneName, AuthorityType type);
}
