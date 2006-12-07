/**
 * 
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
