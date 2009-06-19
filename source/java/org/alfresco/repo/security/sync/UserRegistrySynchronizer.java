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
package org.alfresco.repo.security.sync;

/**
 * A <code>UserRegistrySynchronizer</code> is responsible for synchronizing Alfresco's local user (person) and group
 * (authority) information with one or more external sources (most typically LDAP directories).
 * 
 * @author dward
 */
public interface UserRegistrySynchronizer
{    
    /**
     * Creates a person object for a successfully authenticated user who does not yet have a person object, if allowed
     * to by configuration. Depending on configuration, may trigger a partial synchronize and/or create a new person
     * with default settings.
     * 
     * @param username
     *            the user name
     * @return true, if a person is created
     */
    public boolean createMissingPerson(String username);
    
    /**
     * Retrieves timestamped user and group information from configured external sources and compares it with the local
     * users and groups last retrieved from the same sources. Any updates and additions made to those users and groups
     * are applied to the local Alfresco copies.
     * 
     * @param force
     *            Should a complete or partial set of information be queried from the external sources? When
     *            <code>true</code> then <i>all</i> users and groups are queried. With this complete set of information,
     *            the implementation is able to identify which users and groups have been deleted, so it will delete
     *            users and groups as well as update and create them. When <code>false</code> then each source is only
     *            queried for those users and groups modified since the most recent modification date of all the objects
     *            last queried from that same source. In this mode, local users and groups are created and updated, but
     *            not deleted.
     */
    public void synchronize(boolean force);
}