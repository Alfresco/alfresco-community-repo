package org.alfresco.repo.search;

/**
 * How to carry out the index request ...
 * 
 * @author andyh
 *
 */
public enum IndexMode
{
    /**
     * Synchronously 
     */
    SYNCHRONOUS, 
    
    /**
     * Asynchronously
     */
    ASYNCHRONOUS,
    
    /**
     * Unindexed
     */
    UNINDEXED;
}
