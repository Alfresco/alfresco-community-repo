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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.transaction;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

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
    private static final String TRANSACTION_KEY = "TransactionAwareSingleton.storage";
    
    private final ReadLock singletonReadLock;
    private final WriteLock singletonWriteLock;
    private Object singletonValue;
    
    public TransactionAwareSingleton()
    {
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
            singletonValue = value;
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
            return singletonValue;
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
        TransactionStorage storage = (TransactionStorage) AlfrescoTransactionSupport.getResource(TRANSACTION_KEY);
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
        TransactionStorage storage = (TransactionStorage) AlfrescoTransactionSupport.getResource(TRANSACTION_KEY);
        if (storage == null)
        {
            // it has not changed before
            storage = new TransactionStorage();
            AlfrescoTransactionSupport.bindResource(TRANSACTION_KEY, storage);
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
        TransactionStorage storage = (TransactionStorage) AlfrescoTransactionSupport.getResource(TRANSACTION_KEY);
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
