/**
 * 
 */
package org.alfresco.repo.avm;

/**
 * Callback interface for those interested in store purges.
 * @author britt
 */
public interface PurgeStoreCallback 
{
    /**
     * A store has been purged.
     * @param storeName The name of the purged store.
     */
    public void storePurged(String storeName);
}
