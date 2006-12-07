/**
 * 
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
