/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.domain.avm;

import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.namespace.QName;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * DAO services for 
 *     <b>avm_stores</b>,
 *     <b>avm_store_properties</b>
 * tables
 *
 * @author janv
 * @since 3.2
 */
public interface AVMStoreDAO
{
    //
    // AVM Stores
    //
    
    /**
     * Get an existing AVM store by Store ID
     * 
     * @param id            the unique ID of the store entity
     * @return              the store (never null)
     * @throws              AlfrescoRuntimeException if the ID provided is invalid
     */
    public AVMStoreEntity getStore(long id);
    
    /**
     * Get an existing AVM store by Root Node ID
     * 
     * @param rootNodeId    the unique ID of the root node entity
     * @return              the store (never null)
     * @throws              AlfrescoRuntimeException if the ID provided is invalid
     */
    public AVMStoreEntity getStoreByRoot(long rootNodeId);
    
    /**
     * Get an existing AVM store by name
     * 
     * @param name          the name to query for
     * @return              the store or <tt>null</tt> if it doesn't exist
     */
    public AVMStoreEntity getStore(String name);
    
    /**
     * Get all AVM stores
     * 
     * @return              list of stores or <tt>empty</tt> if no stores (never null)
     */
    public List<AVMStoreEntity> getAllStores();
    
    /**
     * Create a new AVM store
     * 
     * @param name          the name
     * @return              the store (never null)
     * @throws              ConcurrencyFailureException if the name already exists
     */
    public AVMStoreEntity createStore(String name);
    
    /**
     * Update an existing AVM store
     * 
     * @param storeEntity   the store
     */
    public void updateStore(AVMStoreEntity storeEntity);
    
    /**
     * Delete an existing AVM store
     * 
     * @param storeEntity   the store
     * @throws              ConcurrencyFailureException if the store does not exist
     */
    public void deleteStore(long storeId);
    
    public void clearStoreEntityCache();
    
    // 
    // AVM Store Properties
    // 
    
    public void createOrUpdateStoreProperty(long storeId, QName qname, PropertyValue value);
    
    public PropertyValue getStoreProperty(long storeId, QName qname);
    
    public Map<QName, PropertyValue> getStoreProperties(long storeId);
    
    public Map<String, Map<QName, PropertyValue>> getStorePropertiesByKeyPattern(String uri, String localName);
    
    public Map<QName, PropertyValue> getStorePropertiesByStoreAndKeyPattern(long storeId, String uri, String localName);
    
    public void deleteStoreProperty(long storeId, QName qname);
    
    public void deleteStoreProperties(long storeId);
}
