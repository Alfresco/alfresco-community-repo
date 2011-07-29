package org.alfresco.filesys.alfresco;

/**
 * Cache for alfresco "save shuffles" which are used by some applications
 * to compensate for a most computer filesystem being non atomic. 
 * 
 * <p>
 * Overlays an Alfresco repository with temporary files being created and 
 * soft deleted from folders that are likely to have save shuffles going on.
 * <p>
 * Implementations must be thread safe 
 */
public interface ShuffleCache
{
    
    /**
     * Add a new temporary file to the "shuffle cache".   Content is not persisted 
     * in the alfresco repo until either a rename occurs or after a time delay.
     * 
     * @return content writer?
     */
    public void createTemporaryFile(String path);
    
    /**
     * Soft delete a file. The file may be re-instated later or the delete made
     * permenant after a time delay.
     */
    public void softDelete(String path);
    
    /**
     * Takes the contents of a temporary file and applies it to the new path.
     * <p>
     * If the new path has been soft deleted then the soft delete is removed.
     * <p>
     * After the contents of the temporary file have been written the it may may be made 
     * available for garbage collection.
     * 
     * @param oldPath the location of the temporaryFile
     * @param newPath the location of the new file.
     */
    public void renameTemporaryFile(String oldPath, String newPath);
    
    /**
     * Does the specified directory contain a shuffled temporary file
     * @param dir
     * @return
     */
    boolean isShuffleDirectory(String dir);
    
    /**
     * Has the path been "soft deleted"
     */
    boolean isDeleted(String path);
    
    /**
     * Has the path been "soft created"
     * @param path
     * @return
     */
    boolean isCreated(String path);
}
