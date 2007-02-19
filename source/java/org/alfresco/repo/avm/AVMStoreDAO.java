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
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.repo.avm;

import java.util.List;

/**
 * DAO for Repositories.
 * @author britt
 */
public interface AVMStoreDAO
{
    /**
     * Save an AVMStore, never before saved.
     * @param store The AVMStore
     */
    public void save(AVMStore store);
    
    /**
     * Delete the given AVMStore.
     * @param store The AVMStore.
     */
    public void delete(AVMStore store);
    
    /**
     * Get all AVMStores.
     * @return A List of all the AVMStores.
     */
    public List<AVMStore> getAll();

    /**
     * Get an AVMStore by name.
     * @param name The name of the AVMStore.
     * @return The AVMStore or null if not found.
     */
    public AVMStore getByName(String name);
    
    /**
     * Get the AVM Store that has the given root as HEAD.
     * @param root The root to query.
     * @return The matching store or null.
     */
    public AVMStore getByRoot(AVMNode root);
    
    /**
     * Update the given AVMStore record.
     * @param rep The dirty AVMStore.
     */
    public void update(AVMStore rep);
    
    /**
     * Get A store by primary key.
     * @param id The primary key.
     * @return The store.
     */
    public AVMStore getByID(long id);
    
    /**
     * Invalidate the by name lookup cache.
     */
    public void invalidateCache();
}
