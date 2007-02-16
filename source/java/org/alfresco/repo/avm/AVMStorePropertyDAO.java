/*
 * Copyright (C) 2006 Alfresco, Inc.
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

import org.alfresco.service.namespace.QName;

/**
 * The DAO interface for AVMStoreProperties.
 * @author britt
 */
public interface AVMStorePropertyDAO
{
    /**
     * Persist a property.
     * @param prop The AVMStoreProperty to persist.
     */
    public void save(AVMStoreProperty prop);
    
    /**
     * Get a property by store and name.
     * @param store The AVMStore.
     * @param name The QName of the property.
     * @return The given AVMStoreProperty or null if not found.
     */
    public AVMStoreProperty get(AVMStore store, QName name);
    
    /**
     * Get all the properties associated with a store.
     * @param store The AVMStore whose properties should be fetched.
     * @return A List of properties associated with the store.
     */
    public List<AVMStoreProperty> get(AVMStore store);

    /**
     * Query store properties by key pattern.
     * @param store The store.
     * @param keyPattern An sql 'like' pattern wrapped up in a QName
     * @return A List of matching AVMStoreProperties.
     */
    public List<AVMStoreProperty> queryByKeyPattern(AVMStore store, QName keyPattern);

    /**
     * Query all stores' properties by key pattern.
     * @param keyPattern The sql 'like' pattern wrapped up in a QName
     * @return A List of match AVMStoreProperties.
     */
    public List<AVMStoreProperty> queryByKeyPattern(QName keyPattern);
    
    /**
     * Update a modified property.
     * @param prop The AVMStoreProperty to update.
     */
    public void update(AVMStoreProperty prop);
    
    /**
     * Delete a property from a store by name.
     * @param store The AVMStore to delete from.
     * @param name The name of the property.
     */
    public void delete(AVMStore store, QName name);
    
    /**
     * Delete all properties associated with a store.
     * @param store The AVMStore whose properties are to be deleted.
     */
    public void delete(AVMStore store);
}
