/**
 * 
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
