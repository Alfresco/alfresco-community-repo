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

package org.alfresco.repo.simple.permission;

import java.util.List;

/**
 * DAO interface for Authority Entries.
 * @author britt
 */
public interface AuthorityEntryDAO
{
    /**
     * Save one. Recursive.
     * @param entry The one to save.
     */
    public void save(AuthorityEntry entry);
    
    /**
     * Get all the entries.
     * @return What you asked for.
     */
    public List<AuthorityEntry> get();
    
    /**
     * Get the parents of an authority.
     * @param entry The child.
     * @return The parents.
     */
    public List<AuthorityEntry> getParents(AuthorityEntry entry);

    /**
     * Get one by name.
     * @param name The authority name.
     * @return The entry or null if not found.
     */
    public AuthorityEntry get(String name);
    
    /**
     * Get one by primary key.
     * @param id
     * @return The entry or null if not found.
     */
    public AuthorityEntry get(int id);
    
    /**
     * Delete an authority.
     * @param entry The authority.
     */
    public void delete(AuthorityEntry entry);
}
