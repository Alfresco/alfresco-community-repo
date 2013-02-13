/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.util.GUID;

/**
 * A transactionally-safe storage class for singleton objects.  Changes to the singleton
 * are only visibly promoted when the transaction is committed.
 * <p>
 * <code>
 *    private static final TransactionAwareSingleton<Integer> MY_SINGLETON = new TransactionAwareSingleton<Integer>();
 * </code>
 * <p>
 * All modifications to the singleton via {@link #get()} and {@link #put(T)} are made in a 
 * transaction-local manner and promoted to the shared value in a thread-safe manner upon
 * transacton completion.  Transaction-local changes take precedence over the shared value.
 * 
 * @see org.alfresco.repo.transaction.AlfrescoTransactionSupport
 * 
 * @author Derek Hulley
 */
public class TransactionAwareSingleton<T> extends TransactionListenerAdapter
{
    private final String txnKey;
    private final ReadLock singletonReadLock;
    private final WriteLock singletonWriteLock;
    
    private Map<String, Object> tenantSingletonValue = new HashMap<String, Object>(1); // tenant-aware
    
    public TransactionAwareSingleton()
    {
        txnKey = GUID.generate();
        ReentrantReadWriteLock serverReadWriteLock = new ReentrantReadWriteLock();
        singletonReadLock = serverReadWriteLock.readLock();
        singletonWriteLock = serverReadWriteLock.writeLock();
    }
    
    private void setValue(Object value)
    {
        // get a write lock
        singletonWriteLock.lock();
        try
        {
            tenantSingletonValue.put(TenantUtil.getCurrentDomain(), value);
        }
        finally
        {
            singletonWriteLock.unlock();
        }
    }
    
    private Object getValue()
    {
        // get a read lock
        singletonReadLock.lock();
        try
        {
            return tenantSingletonValue.get(TenantUtil.getCurrentDomain());
        }
        finally
        {
            singletonReadLock.unlock();
        }
    }

    /**
     * @return Returns the transaction- and thread-safe wrapped instance
     */
    @SuppressWarnings("unchecked")
    public T get()
    {
        // an in-transaction value overrides the singleton
        TransactionStorage storage = (TransactionStorage) AlfrescoTransactionSupport.getResource(txnKey);
        if (storage != null)
        {
            return (T) storage.newValue;
        }
        else
        {
            return (T) getValue();
        }
    }
    
    /**
     * Store the value in a transaction- and thread-safe manner.  It will only be persisted
     * at the end of the transaction but will be visible to the current transaction from
     * this call onwards.
     * 
     * @param value the value to store
     */
    public void put(T value)
    {
        // the value is changing
        TransactionStorage storage = (TransactionStorage) AlfrescoTransactionSupport.getResource(txnKey);
        if (storage == null)
        {
            // it has not changed before
            storage = new TransactionStorage();
            AlfrescoTransactionSupport.bindResource(txnKey, storage);
            // listen to the transaction
            AlfrescoTransactionSupport.bindListener(this);
        }
        storage.newValue = value;
    }
    
    /**
     * Promotes the storage value to the single value, if required
     */
    public void afterCommit()
    {
        TransactionStorage storage = (TransactionStorage) AlfrescoTransactionSupport.getResource(txnKey);
        if (storage != null)
        {
            // the value was overridden
            setValue(storage.newValue);
        }
    }
    
    /**
     * In-transaction storage of the altered value
     * @author Derek Hulley
     */
    private static class TransactionStorage
    {
        public Object newValue;
    }
}