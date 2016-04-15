/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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
