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
import org.alfresco.util.Pair;

/**
 * Transaction listener that fires purge version events.
 * @author britt
 */
public class PurgeVersionTxnListener extends TransactionListenerAdapter 
{
    /**
     * Storage for versions purged in a transaction.
     */
    private ThreadLocal<List<Pair<String, Integer>>> fPurgedVersions;

    /**
     * Callbacks to invoke on commit.
     */
    private List<PurgeVersionCallback> fCallbacks;
    
    /**
     * Default constructor.
     */
    public PurgeVersionTxnListener()
    {
        fPurgedVersions = new ThreadLocal<List<Pair<String, Integer>>>();
        fCallbacks = new ArrayList<PurgeVersionCallback>();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListenerAdapter#afterCommit()
     */
    @Override
    public void afterCommit() 
    {
        List<Pair<String, Integer>> created = fPurgedVersions.get();
        if (created != null)
        {
            for (Pair<String, Integer> version : created)
            {
                synchronized (this)
                {
                    for (PurgeVersionCallback cb : fCallbacks)
                    {
                        cb.versionPurged(version.getFirst(), version.getSecond());
                    }
                }
            }
            fPurgedVersions.set(null);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListenerAdapter#afterRollback()
     */
    @Override
    public void afterRollback() 
    {
        fPurgedVersions.set(null);
    }
    
    /**
     * During the transaction somebody is responsible for 
     * calling this.
     * @param storeName The name of the store that just created a new version
     * @param versionID The id of the new version.
     */
    public void versionPurged(String storeName, int versionID)
    {
        List<Pair<String, Integer>> purged = fPurgedVersions.get();
        if (purged == null)
        {
            purged = new ArrayList<Pair<String, Integer>>();
            fPurgedVersions.set(purged);
        }
        purged.add(new Pair<String, Integer>(storeName, versionID));
    }
    
    /**
     * Register a callback.
     * @param cb The callback.
     */
    public synchronized void addCallback(PurgeVersionCallback cb)
    {
        fCallbacks.add(cb);
    }
}
