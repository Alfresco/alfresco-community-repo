package org.alfresco.filesys.alfresco;

import java.util.Map;

/**
 * Cache for alfresco "CIFS shuffles"
 * 
 *
 */
public class ShuffleCacheImpl implements ShuffleCache
{    
    /**
     *  time in ms that temporary files should live in the cache before 
     *  being persisted.
     */
    private long timeBeforePersist  = 5 * 60000L; // 5 minutes default
    
    /**
     * Is the cache caseSensitive?
     */
    private boolean caseSensitive;
    
    /**
     * The shuffle folder cache keyed by path.
     * <path> <shuffleFolderInfo>
     */
    private Map<String, ShuffleFolderInfo> folderCache;
    

    /**
     * The information held for each folder that has a "shuffle" 
     * in progress.
     * @author mrogers
     */
    private class ShuffleFolderInfo
    {

    }

    @Override
    public void createTemporaryFile(String path)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void softDelete(String path)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void renameTemporaryFile(String oldPath, String newPath)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isShuffleDirectory(String dir)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDeleted(String path)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCreated(String path)
    {
        // TODO Auto-generated method stub
        return false;
    }

    void setTimeBeforePersist(long timeBeforePersist)
    {
        this.timeBeforePersist = timeBeforePersist;
    }

    long getTimeBeforePersist()
    {
        return timeBeforePersist;
    }

    public void setCaseSensitive(boolean caseSensitive)
    {
        this.caseSensitive = caseSensitive;
    }

    public boolean isCaseSensitive()
    {
        return caseSensitive;
    }
}
