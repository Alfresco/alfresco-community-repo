/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.content.caching.cleanup;

import java.io.File;

import org.alfresco.repo.content.caching.CacheFileProps;
import org.alfresco.repo.content.caching.ContentCacheImpl;
import org.alfresco.repo.content.caching.FileHandler;
import org.alfresco.util.Deleter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Cleans up redundant cache files from the cached content file store. Once references to cache files are
 * no longer in the in-memory cache, the binary content files can be removed.
 * 
 * @author Matt Ward
 */
public class CachedContentCleaner implements FileHandler
{
    private static final Log log = LogFactory.getLog(CachedContentCleaner.class);
    private ContentCacheImpl cache;   // impl specific functionality required
    private Integer maxDeleteWatchCount = 1;
    
    public void execute()
    {   
        cache.processFiles(this);
    }
    
    @Override
    public void handle(File cachedContentFile)
    {
        if (log.isDebugEnabled())
        {
            log.debug("handle file: " + cachedContentFile);
        }
        
        CacheFileProps props = null; // don't load unless required        
        String url = cache.getContentUrl(cachedContentFile);
        if (url == null)
        {
            // Not in the cache, check the properties file
            props = new CacheFileProps(cachedContentFile);
            props.load();
            url = props.getContentUrl();
        }   
        
        if (url != null && !cache.contains(url))
        {
            if (props == null)
            {
                props = new CacheFileProps(cachedContentFile);
                props.load();
            }
            markOrDelete(cachedContentFile, props);
        }
        else if (url == null)
        {
            // It might still be in the cache, but we were unable to determine it from the reverse lookup
            // or the properties file. Delete the file as it is most likely orphaned. If for some reason it is
            // still in the cache, cache.getReader(url) must re-cache it.
            markOrDelete(cachedContentFile, props);
        }    
    }

    

    /**
     * Marks a file for deletion by a future run of the CachedContentCleaner. Each time a file is observed
     * by the cleaner as being ready for deletion, the deleteWatchCount is incremented until it reaches
     * maxDeleteWatchCount - in which case the next run of cleaner will really delete it.
     * <p>
     * For maxDeleteWatchCount of 1 for example, the first cleaner run will mark the file for deletion and the second
     * run will really delete it.
     * <p>
     * This offers a degree of protection over the fairly unlikely event that a reader will be obtained for a file that
     * is in the cache but gets removed from the cache and is then deleted by the cleaner before
     * the reader was consumed. A maxDeleteWatchCount of 1 should normally be fine (recommended), whilst
     * 0 would result in immediate deletion the first time the cleaner sees it as a candidate
     * for deletion (not recommended).
     * 
     * @param file
     * @param props
     */
    private void markOrDelete(File file, CacheFileProps props)
    {
        Integer deleteWatchCount = props.getDeleteWatchCount();

        // Just in case the value has been corrupted somehow.
        if (deleteWatchCount < 0)
            deleteWatchCount = 0;
        
        if (deleteWatchCount < maxDeleteWatchCount)
        {
            deleteWatchCount++;
            props.setDeleteWatchCount(deleteWatchCount);
            props.store();
        }
        else
        {
            deleteFilesNow(file);
        }
    }

    /**
     * Deletes both the cached content file and its peer properties file that contains the
     * original content URL and deletion marker information.
     *  
     * @param cacheFile Location of cached content file.
     */
    private void deleteFilesNow(File cacheFile)
    {
        CacheFileProps props = new CacheFileProps(cacheFile);
        props.delete();
        cacheFile.delete();
        Deleter.deleteEmptyParents(cacheFile, cache.getCacheRoot());
    }

    

    @Required
    public void setCache(ContentCacheImpl cache)
    {
        this.cache = cache;
    }

    public void setMaxDeleteWatchCount(Integer maxDeleteWatchCount)
    {
        if (maxDeleteWatchCount < 0)
        {
            throw new IllegalArgumentException("maxDeleteWatchCount cannot be negative [value=" + maxDeleteWatchCount + "]");
        }
        this.maxDeleteWatchCount = maxDeleteWatchCount;
    }
}
