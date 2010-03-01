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
package org.alfresco.repo.avm;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.transaction.TransactionListenerAdapter;

/**
 * Transaction listener for firing purge store events.
 * @author britt
 */
public class PurgeStoreTxnListener extends TransactionListenerAdapter 
{
    /**
     * Storage for stores purged in a transaction.
     */
    private ThreadLocal<List<String>> fPurgedStores;

    /**
     * Callbacks to invoke on commit.
     */
    private List<PurgeStoreCallback> fCallbacks;
    
    /**
     * Default constructor.
     */
    public PurgeStoreTxnListener()
    {
        fPurgedStores = new ThreadLocal<List<String>>();
        fCallbacks = new ArrayList<PurgeStoreCallback>();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListenerAdapter#afterCommit()
     */
    @Override
    public void afterCommit() 
    {
        List<String> created = fPurgedStores.get();
        if (created != null)
        {
            for (String name : created)
            {
                synchronized (this)
                {
                    for (PurgeStoreCallback cb : fCallbacks)
                    {
                        cb.storePurged(name);
                    }
                }
            }
            fPurgedStores.set(null);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListenerAdapter#afterRollback()
     */
    @Override
    public void afterRollback() 
    {
        fPurgedStores.set(null);
    }
    
    /**
     * During the transaction somebody is responsible for 
     * calling this.
     * @param storeName The name of the store that has been purged.
     */
    public void storePurged(String storeName)
    {
        List<String> purged = fPurgedStores.get();
        if (purged == null)
        {
            purged = new ArrayList<String>();
            fPurgedStores.set(purged);
        }
        purged.add(storeName);
    }
    
    /**
     * Register a callback.
     * @param cb The callback.
     */
    public synchronized void addCallback(PurgeStoreCallback cb)
    {
        fCallbacks.add(cb);
    }
}
