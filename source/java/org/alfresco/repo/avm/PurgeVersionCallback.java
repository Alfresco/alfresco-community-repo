/**
 * 
 */
package org.alfresco.repo.avm;

/**
 * A Callback interface for those interested in versions being purged.
 * @author britt
 */
public interface PurgeVersionCallback
{
    /**
     * A version was purged from a store.
     * @param storeName The name of the store from which a version was purged.
     * @param versionID The id of the purged version.
     */
    public void versionPurged(String storeName, int versionID);
}
