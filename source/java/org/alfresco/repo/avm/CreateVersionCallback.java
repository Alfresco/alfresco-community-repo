/**
 * 
 */
package org.alfresco.repo.avm;

/**
 * Callback interface for those interested in new version creation.
 * @author britt
 */
public interface CreateVersionCallback 
{
    /**
     * A version of a store has been created.
     * @param storeName The name of the store in which a new version has been created.
     * @param versionID The version id of the new version.
     */
    public void versionCreated(String storeName, int versionID);
}
