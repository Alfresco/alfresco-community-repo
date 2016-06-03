package org.alfresco.repo.security.sync;

public enum SyncStatus 
{
    /**
     * A sync has been requested but has not yet started
     */
    WAITING,
    /**
     * A sync is in progress 
     */
    IN_PROGRESS,
    
    /**
     * A sync is finished with no errors
     */
    COMPLETE,
    
    /**
     * A sync finished with at least 1 error
     */
    COMPLETE_ERROR
}
