package org.alfresco.filesys.alfresco;

/**
 * Does this NetworkFile have reference counting?
 */
public interface NetworkFileLegacyReferenceCount
{
    /**
     * Increment the file open count, first open = 1;
     * 
     * @return the current open count
     */
    public int incrementLegacyOpenCount();
    
    /**
     * Decrement the file open count
     * 
     * @return the current open count
     */
    public int decrementLagacyOpenCount();
    
    /**
     * Return the open file count
     * 
     * @return the current open count
     */
    public int getLegacyOpenCount(); 

}
