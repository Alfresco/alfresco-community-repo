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
package org.alfresco.repo.content.caching;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.GUID;

/**
 * The one and only implementation of the ContentCache class. Binary content data itself is stored on disk in the location specified by {@link #cacheRoot}.
 * <p>
 * The in-memory lookup table is provided by a SimpleCache implementation.
 * 
 * @author Matt Ward
 */
public class ContentCacheImpl implements ContentCache
{
    private static final Log log = LogFactory.getLog(ContentCacheImpl.class);
    private static final String CACHE_FILE_EXT = ".bin";
    private static final String CACHE_FILE_TEMP_EXT = ".tmp";
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
     *            Key
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
     *            Key
     * @param value
     *            String
     */
    public void putIntoLookup(Key key, String value)
    {
        memoryStore.put(key, value);
    }

    /**
     * Get the path of a cache file for the given content URL - will return null if there is no entry in the cache for the specified URL.
     * 
     * @param contentUrl
     *            String
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
     *            File
     * @return String
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

            // Getting the path for a URL from the memoryStore will reset the timeToIdle for
            // that URL. It is important to perform a reverse lookup as well to ensure that the
            // cache file path to URL mapping is also kept in the cache.
            memoryStore.get(Key.forCacheFile(path));

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
        File tempFile = createCacheFile();

        // Copy the content from the source into a cache file
        if (source.getSize() > 0L)
        {
            source.getContent(tempFile);
            File cacheFile = renameTempToActive(tempFile);
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
     * @return File
     */
    private File createCacheFile()
    {
        File file = new File(cacheRoot, createNewTempCacheFilePath());
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

    /**
     * Remove all items from the lookup table. Cached content files are not removed.
     */
    public void removeAll()
    {
        memoryStore.clear();
    }

    @Override
    public void deleteFile(String url)
    {
        File cacheFile = new File(getCacheFilePath(url));
        cacheFile.delete();
    }

    @Override
    public ContentWriter getWriter(final String url)
    {
        // Get a writer to a cache file.
        final File tempFile = createCacheFile();
        final CacheWriter writer = new CacheWriter(tempFile, url);

        // Attach a listener to populate the in-memory store when done writing.
        writer.addListener(new ContentStreamListener() {
            @Override
            public void contentStreamClosed() throws ContentIOException
            {
                final File cacheFile = renameTempToActive(tempFile);
                writer.setCacheFile(cacheFile);
                recordCacheEntries(url, cacheFile);
            }
        });

        return writer;
    }

    private File renameTempToActive(File tempFile)
    {
        String fullPath = tempFile.getPath();
        int extIndex = fullPath.lastIndexOf(CACHE_FILE_TEMP_EXT);

        if (extIndex > 0)
        {
            String prefix = fullPath.substring(0, extIndex);
            File dest = new File(prefix + CACHE_FILE_EXT);

            boolean renamed = tempFile.renameTo(dest);

            if (renamed)
            {
                return dest;
            }
            else
            {
                throw new IllegalStateException(
                        "Temp file couldn't be renamed to active cache file, temp=" +
                                tempFile.getPath() +
                                ", dest=" +
                                dest.getPath());
            }
        }
        else
        {
            throw new IllegalArgumentException("Invalid temp file name: " + tempFile.getPath());
        }
    }

    /**
     * Creates a relative path for a new cache file. The path is based upon the current date/time: year/month/day/hour/minute/guid.bin
     * <p>
     * e.g. 2011/12/3/13/55/27d56416-bf9f-4d89-8f9e-e0a52de0a59e.bin
     * 
     * @return The relative path for the new cache file.
     */
    public static String createNewCacheFilePath()
    {
        return createNewCacheFilePath(false);
    }

    private static String createNewTempCacheFilePath()
    {
        return createNewCacheFilePath(true);
    }

    private static String createNewCacheFilePath(boolean tempFile)
    {
        Calendar calendar = new GregorianCalendar();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // 0-based
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
                .append(GUID.generate());

        if (tempFile)
        {
            sb.append(CACHE_FILE_TEMP_EXT);
        }
        else
        {
            sb.append(CACHE_FILE_EXT);
        }

        return sb.toString();
    }

    /**
     * Configure ContentCache with a memory store.
     * 
     * @param memoryStore
     *            the memoryStore to set
     */
    public void setMemoryStore(SimpleCache<Key, String> memoryStore)
    {
        this.memoryStore = memoryStore;
    }

    /**
     * Specify the directory where cache files will be written.
     * 
     * @param cacheRoot
     *            File
     */
    public void setCacheRoot(File cacheRoot)
    {
        if (cacheRoot == null)
        {
            throw new IllegalArgumentException("cacheRoot cannot be null.");
        }
        if (!cacheRoot.exists())
        {
            cacheRoot.mkdirs();
        }
        this.cacheRoot = cacheRoot;
    }

    /**
     * Returns the directory where cache files will be written (cacheRoot).
     * 
     * @return cacheRoot
     */
    @Override
    public File getCacheRoot()
    {
        return this.cacheRoot;
    }

    /**
     * Ask the ContentCacheImpl to visit all the content files in the cache.
     * 
     * @param handler
     *            FileHandler
     */
    public void processFiles(FileHandler handler)
    {
        handleDir(cacheRoot, handler);
    }

    /**
     * Recurse into a directory handling cache files (*.bin) with the supplied {@link FileHandler}.
     * 
     * @param dir
     *            File
     * @param handler
     *            FileHandler
     */
    private void handleDir(File dir, FileHandler handler)
    {
        if (dir.isDirectory())
        {
            File[] files = sortFiles(dir);

            for (File file : files)
            {
                if (file.isDirectory())
                {
                    handleDir(file, handler);
                }
                else
                {
                    if (file.getName().endsWith(CACHE_FILE_EXT))
                        handler.handle(file);
                }
            }
        }
        else
        {
            throw new IllegalArgumentException("handleDir() called with non-directory: " + dir.getAbsolutePath());
        }
    }

    /**
     * Sort files ready for a FileHandler to visit them. This sorts them based on the structure created by the {@link #createNewCacheFilePath()} method. Knowing that the directories are all numeric date/time components, if they are sorted in ascending order then the oldest directories will be visited first.
     * <p>
     * The returned array contains the (numerically sorted) directories first followed by the (unsorted) plain files.
     * 
     * @param dir
     *            File
     * @return File[]
     */
    private File[] sortFiles(File dir)
    {
        List<File> dirs = new ArrayList<File>();
        List<File> files = new ArrayList<File>();

        for (File item : dir.listFiles())
        {
            if (item.isDirectory())
            {
                dirs.add(item);
            }
            else
            {
                files.add(item);
            }
        }

        // Sort directories as numbers - as for structure produced by ContentCacheImpl
        Collections.sort(dirs, new NumericFileNameComparator());

        // Concatenation of elements in dirs followed by elements in files
        List<File> all = new ArrayList<File>();
        all.addAll(dirs);
        all.addAll(files);

        return all.toArray(new File[]{});
    }

    protected static class NumericFileNameComparator implements Comparator<File>
    {
        @Override
        public int compare(File o1, File o2)
        {
            Integer n1 = parse(o1.getName());
            Integer n2 = parse(o2.getName());
            return n1.compareTo(n2);
        }

        /**
         * If unable to parse a String numerically then Integer.MAX_VALUE is returned. This results in unexpected directories or files in the structure appearing after the expected directories - so the files we know ought to be older will appear first in a sorted collection.
         * 
         * @param s
         *            String to parse
         * @return Numeric form of s
         */
        private int parse(String s)
        {
            try
            {
                return Integer.parseInt(s);
            }
            catch (NumberFormatException e)
            {
                return Integer.MAX_VALUE;
            }
        }
    }

    /**
     * This FileContentWriter subclass allows for the temp cache file to be renamed to a cache file proper, e.g filename.tmp becomes filename.bin
     * 
     * @author Matt Ward
     */
    public static class CacheWriter extends FileContentWriter
    {
        private File cacheFile = null;

        public CacheWriter(File file, String url)
        {
            super(file, url, null);
        }

        public void setCacheFile(File file)
        {
            cacheFile = file;
        }

        @Override
        public File getFile()
        {
            if (cacheFile != null)
            {
                return cacheFile;
            }
            return super.getFile();
        }

        @Override
        protected ContentReader createReader() throws ContentIOException
        {
            FileContentReader reader = new FileContentReader(getFile(), getContentUrl());
            // TODO: what about reader.setAllowRandomAccess(this.allowRandomAccess); ?
            return reader;
        }

        @Override
        public long getSize()
        {
            File file = getFile();

            if (file == null)
                return 0L;
            else if (!file.exists())
                return 0L;
            else
                return file.length();
        }
    }
}
