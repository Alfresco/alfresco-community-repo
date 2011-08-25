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
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The one and only implementation of the ContentCache class. Binary content data itself
 * is stored on disk in the location specified by {@link cacheRoot}.
 * <p>
 * The in-memory lookup table is provided by Ehcache.
 * 
 * @author Matt Ward
 */
public class ContentCacheImpl implements ContentCache
{
    private static final Log log = LogFactory.getLog(ContentCacheImpl.class);
    private static final String CACHE_FILE_EXT = ".bin";
    private File cacheRoot;
    private SimpleCache<Key, String> memoryStore;
    
    
    @Override
    public boolean contains(String contentUrl)
    {
        return memoryStore.contains(Key.forUrl(contentUrl));
    }

    /**
     * Allows caller to perform lookup using a {@link Key}.
     * 
     * @param key
     * @return true if the cache contains, false otherwise.
     */
    public boolean contains(Key key)
    {
        return memoryStore.contains(key);
    }
    
    /**
     * Put an item in the lookup table.
     * 
     * @param key
     * @param value
     */
    public void putIntoLookup(Key key, String value)
    {
        memoryStore.put(key, value);
    }
    
    /**
     * Get the path of a cache file for the given content URL - will return null if there is no entry
     * in the cache for the specified URL.
     * 
     * @param contentUrl
     * @return cache file path
     */
    public String getCacheFilePath(String contentUrl)
    {
        return memoryStore.get(Key.forUrl(contentUrl));
    }
    
    /**
     * Get a content URL from the cache - keyed by File.
     * 
     * @param file
     * @return
     */
    public String getContentUrl(File file)
    {
        return memoryStore.get(Key.forCacheFile(file));
    }
    
    @Override
    public ContentReader getReader(String contentUrl)
    {
        Key url = Key.forUrl(contentUrl);
        if (memoryStore.contains(url))
        {
            String path = memoryStore.get(url);
            File cacheFile = new File(path);
            if (cacheFile.exists())
            {
                return new FileContentReader(cacheFile, contentUrl);
            }
        }
        
        throw new CacheMissException(contentUrl);
    }
    
    @Override
    public boolean put(String contentUrl, ContentReader source)
    {
        File cacheFile = createCacheFile();
        
        // Copy the content from the source into a cache file
        if (source.getSize() > 0L)
        {
            source.getContent(cacheFile);
            // Add a record of the cached file to the in-memory cache.
            recordCacheEntries(contentUrl, cacheFile);
            return true;
        }

        return false;
    }

    private void recordCacheEntries(String contentUrl, File cacheFile)
    {
        memoryStore.put(Key.forUrl(contentUrl), cacheFile.getAbsolutePath());
        memoryStore.put(Key.forCacheFile(cacheFile), contentUrl);
    }
    
    /**
     * Create a File object and makes any intermediate directories in the path.
     * 
     * @param contentUrl
     * @return File
     */
    private File createCacheFile()
    {
        File file = new File(cacheRoot, createNewCacheFilePath());
        File parentDir = file.getParentFile();
        parentDir.mkdirs();
        return file;
    }
    
    @Override
    public void remove(String contentUrl)
    {
        // Remove from the in-memory cache, but not from disk. Let the clean-up process do this asynchronously.
        String path = getCacheFilePath(contentUrl);
        memoryStore.remove(Key.forUrl(contentUrl));
        memoryStore.remove(Key.forCacheFile(path));
    }

    @Override
    public ContentWriter getWriter(final String url)
    {
        // Get a writer to a cache file.
        final File cacheFile = createCacheFile();
        ContentWriter writer = new FileContentWriter(cacheFile, url, null);
        
        // Attach a listener to populate the in-memory store when done writing.
        writer.addListener(new ContentStreamListener()
        {
            @Override
            public void contentStreamClosed() throws ContentIOException
            {
                recordCacheEntries(url, cacheFile);
            }
        });
        
        return writer;
    }
    
    /**
     * Creates a relative path for a new cache file. The path is based
     * upon the current date/time: year/month/day/hour/minute/guid.bin
     * <p>
     * e.g. 2011/12/3/13/55/27d56416-bf9f-4d89-8f9e-e0a52de0a59e.bin
     * @return The relative path for the new cache file.
     */
    public static String createNewCacheFilePath()
    {
        Calendar calendar = new GregorianCalendar();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;  // 0-based
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        // create the URL
        StringBuilder sb = new StringBuilder(20);
        sb.append(year).append('/')
          .append(month).append('/')
          .append(day).append('/')
          .append(hour).append('/')
          .append(minute).append('/')
          .append(GUID.generate()).append(CACHE_FILE_EXT);
        return sb.toString();
    }

    /**
     * Configure ContentCache with a memory store - an EhCacheAdapter.
     * 
     * @param memoryStore the memoryStore to set
     */
    public void setMemoryStore(SimpleCache<Key, String> memoryStore)
    {
        this.memoryStore = memoryStore;
    }
    
    /**
     * Specify the directory where cache files will be written.
     * 
     * @param cacheRoot
     */
    public void setCacheRoot(File cacheRoot)
    {
        this.cacheRoot = cacheRoot;
    }
    
    /**
     * Returns the directory where cache files will be written (cacheRoot).
     * 
     * @return cacheRoot
     */
    public File getCacheRoot()
    {
        return this.cacheRoot;
    }

    // Not part of the ContentCache interface as this breaks encapsulation.
    // Handy method for tests though, since it allows us to find out where
    // the content was cached.
    protected String cacheFileLocation(String url)
    {
        return memoryStore.get(Key.forUrl(url));
    }

    /**
     * @param cachedContentCleaner
     */
    public void processFiles(FileHandler handler)
    {
        handleDir(cacheRoot, handler);
    }

    /**
     * Recurse into a directory handling cache files (*.bin) with the supplied
     * {@link FileHandler}.
     * 
     * @param dir
     * @param handler
     */
    private void handleDir(File dir, FileHandler handler)
    {
        if (dir.isDirectory())
        {
            File[] files = dir.listFiles();
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    handleDir(file, handler);
                }
                else
                {
                    if (file.getName().endsWith(CACHE_FILE_EXT)) handler.handle(file);
                }
            }
        }
        else
        {
            throw new IllegalArgumentException("handleDir() called with non-directory: " + dir.getAbsolutePath());
        }
    }
}
