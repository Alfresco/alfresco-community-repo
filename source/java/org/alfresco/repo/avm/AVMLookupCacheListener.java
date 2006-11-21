/**
 * 
 */
package org.alfresco.repo.avm;

import org.alfresco.repo.transaction.TransactionListenerAdapter;

/**
 * This is the listener that cleans up the lookup cache on transaction
 * rollback.
 * @author britt
 */
public class AVMLookupCacheListener extends TransactionListenerAdapter 
{
    /**
     * The lookup cache.
     */
    private LookupCache fLookupCache;

    /**
     * A default constructor.
     */
    public AVMLookupCacheListener()
    {
    }
    
    /**
     * Set the Lookup Cache.
     * @param lookupCache
     */
    public void setLookupCache(LookupCache lookupCache)
    {
        fLookupCache = lookupCache;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListenerAdapter#afterRollback()
     */
    @Override
    public void afterRollback() 
    {
        fLookupCache.onRollback();
    }
}
