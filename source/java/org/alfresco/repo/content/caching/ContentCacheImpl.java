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
package org.alfresco.repo.content.caching;

import java.io.File;

import net.sf.ehcache.CacheManager;

import org.alfresco.repo.cache.EhCacheAdapter;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.TempFileProvider;

/**
 * The one and only implementation of the ContentCache class.
 * <p>
 * Binary content data itself is stored on disk in temporary files managed
 * by Alfresco (see {@link org.alfresco.util.TempFileProvider}).
 * <p>
 * The in-memory lookup table is provided by Ehcache.
 * 
 * @author Matt Ward
 */
public class ContentCacheImpl implements ContentCache
{
    private static final String CACHE_DIR = "caching_cs";
    private static final String TMP_FILE_EXTENSION = ".tmp";
    private static final String EHCACHE_NAME = "contentStoreCache";
    private static final long T24_HOURS = 86400;
    private final File cacheRoot = TempFileProvider.getLongLifeTempDir(CACHE_DIR);
    private EhCacheAdapter<String, String> memoryStore;
    
    public ContentCacheImpl()
    {
        // TODO: Configuration to be moved out into Spring
        memoryStore = new EhCacheAdapter<String, String>();
        configureMemoryStore();   
    }

    private void configureMemoryStore()
    {
        CacheManager manager = CacheManager.getInstance();
        
        // Create the cache if it hasn't already been created.
        if (!manager.cacheExists(EHCACHE_NAME))
        {
            net.sf.ehcache.Cache memoryOnlyCache = 
                new net.sf.ehcache.Cache(EHCACHE_NAME, 10000, false, false, T24_HOURS, T24_HOURS);
            
            manager.addCache(memoryOnlyCache);
        }
        
        memoryStore.setCache(manager.getCache(EHCACHE_NAME));
    }
    
    
    @Override
    public boolean contains(String contentUrl)
    {
        return memoryStore.contains(contentUrl);
    }

    
    
    @Override
    public ContentReader getReader(String contentUrl)
    {
        if (memoryStore.contains(contentUrl))
        {
            String path = memoryStore.get(contentUrl);
            return new FileContentReader(new File(path), contentUrl);
        }
        
        throw new CacheMissException(contentUrl);
    }

    
    
    @Override
    public boolean put(String contentUrl, ContentReader source)
    {
        File cacheFile = createCacheFile(contentUrl);
        
        // Copy the content from the source into a cache file
        if (source.getSize() > 0L)
        {
            source.getContent(cacheFile);
            // Add a record of the cached file to the in-memory cache.
            memoryStore.put(contentUrl, cacheFile.getAbsolutePath());
            return true;
        }

        return false;
    }

    
    /**
     * Create a File object and makes any intermediate directories in the path.
     * 
     * @param contentUrl
     * @return File
     */
    private File createCacheFile(String contentUrl)
    {
        File path = new File(cacheRoot, pathFromUrl(contentUrl));
        File parentDir = path.getParentFile();
        
        parentDir.mkdirs();
        
        File cacheFile = TempFileProvider.createTempFile(path.getName(), TMP_FILE_EXTENSION, parentDir);
        return cacheFile;
    }
    
    
    
    
    /*
     * @see org.alfresco.repo.content.caching.ContentCache#remove(java.lang.String)
     */
    @Override
    public void remove(String contentUrl)
    {
        // Remove from the in-memory cache, but not from disk. Let the clean-up process do this asynchronously.
        memoryStore.remove(contentUrl);
    }

    
    /*
     * @see org.alfresco.repo.content.caching.ContentCache#getWriter(org.alfresco.repo.content.ContentContext)
     */
    @Override
    public ContentWriter getWriter(final String url)
    {
        // Get a writer to a cache file.
        final File cacheFile = createCacheFile(url);
        ContentWriter writer = new FileContentWriter(cacheFile);
        
        // Attach a listener to populate the in-memory store when done writing.
        writer.addListener(new ContentStreamListener()
        {
            @Override
            public void contentStreamClosed() throws ContentIOException
            {
                memoryStore.put(url, cacheFile.getAbsolutePath());
            }
        });
        
        return writer;
    }

    
    /**
     * Converts a content URL to a relative path name where the protocol will
     * be the name of a subdirectory. For example:
     * <p>
     * store://2011/8/5/15/4/386595e0-3b52-4d5c-a32d-df9d0b9fd56e.bin
     * <p>
     * will become:
     * <p>
     * store/2011/8/5/15/4/386595e0-3b52-4d5c-a32d-df9d0b9fd56e.bin
     * 
     * @param contentUrl
     * @return String representation of relative path to file.
     */
    private String pathFromUrl(String contentUrl)
    {
        return contentUrl.replaceFirst("://", "/");
    }
}
