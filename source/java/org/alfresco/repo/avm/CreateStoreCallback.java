/**
 * 
 */
package org.alfresco.repo.avm;

/**
 * Interface for listeners to new store events.
 * @author britt
 */
public interface CreateStoreCallback 
{
    /**
     * A new store has been created.
     * @param storeName The name of the new store.
     */
    public void storeCreated(String storeName);
}

