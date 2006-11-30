/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */

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
