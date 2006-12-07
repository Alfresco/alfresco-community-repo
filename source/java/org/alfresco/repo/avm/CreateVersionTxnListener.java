/**
 * 
 */
package org.alfresco.repo.avm;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.util.Pair;

/**
 * Transaction listener that fires create version events.
 * @author britt
 */
public class CreateVersionTxnListener extends TransactionListenerAdapter 
{
    /**
     * Storage for versions created in a transaction.
     */
    private ThreadLocal<List<Pair<String, Integer>>> fCreatedVersions;

    /**
     * Callbacks to invoke on commit.
     */
    private List<CreateVersionCallback> fCallbacks;
    
    /**
     * Default constructor.
     */
    public CreateVersionTxnListener()
    {
        fCreatedVersions = new ThreadLocal<List<Pair<String, Integer>>>();
        fCallbacks = new ArrayList<CreateVersionCallback>();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListenerAdapter#afterCommit()
     */
    @Override
    public void afterCommit() 
    {
        List<Pair<String, Integer>> created = fCreatedVersions.get();
        for (Pair<String, Integer> version : created)
        {
            synchronized (this)
            {
                for (CreateVersionCallback cb : fCallbacks)
                {
                    cb.versionCreated(version.getFirst(), version.getSecond());
                }
            }
        }
        fCreatedVersions.set(null);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListenerAdapter#afterRollback()
     */
    @Override
    public void afterRollback() 
    {
        fCreatedVersions.set(null);
    }
    
    /**
     * During the transaction somebody is responsible for 
     * calling this.
     * @param storeName The name of the store that just created a new version
     * @param versionID The id of the new version.
     */
    public void versionCreated(String storeName, int versionID)
    {
        List<Pair<String, Integer>> created = fCreatedVersions.get();
        if (created == null)
        {
            created = new ArrayList<Pair<String, Integer>>();
            fCreatedVersions.set(created);
        }
        created.add(new Pair<String, Integer>(storeName, versionID));
    }
    
    /**
     * Register a callback.
     * @param cb The callback.
     */
    public synchronized void addCallback(CreateVersionCallback cb)
    {
        fCallbacks.add(cb);
    }
}
