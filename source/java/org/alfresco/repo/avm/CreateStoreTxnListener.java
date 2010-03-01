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
 * Transaction listener for firing create store events.
 * @author britt
 */
public class CreateStoreTxnListener extends TransactionListenerAdapter 
{
    /**
     * Storage for stores created in a transaction.
     */
    private ThreadLocal<List<String>> fCreatedStores;

    /**
     * Callbacks to invoke on commit.
     */
    private List<CreateStoreCallback> fCallbacks;
    
    /**
     * Default constructor.
     */
    public CreateStoreTxnListener()
    {
        fCreatedStores = new ThreadLocal<List<String>>();
        fCallbacks = new ArrayList<CreateStoreCallback>();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListenerAdapter#afterCommit()
     */
    @Override
    public void afterCommit() 
    {
        List<String> created = fCreatedStores.get();
        if (created != null)
        {
            for (String name : created)
            {
                synchronized (this)
                {
                    for (CreateStoreCallback cb : fCallbacks)
                    {
                        cb.storeCreated(name);
                    }
                }
            }
            fCreatedStores.set(null);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListenerAdapter#afterRollback()
     */
    @Override
    public void afterRollback() 
    {
        fCreatedStores.set(null);
    }
    
    /**
     * During the transaction somebody is responsible for 
     * calling this.
     * @param storeName The name of the store that has been created.
     */
    public void storeCreated(String storeName)
    {
        List<String> created = fCreatedStores.get();
        if (created == null)
        {
            created = new ArrayList<String>();
            fCreatedStores.set(created);
        }
        created.add(storeName);
    }
    
    /**
     * Register a callback.
     * @param cb The callback.
     */
    public synchronized void addCallback(CreateStoreCallback cb)
    {
        fCallbacks.add(cb);
    }
}
