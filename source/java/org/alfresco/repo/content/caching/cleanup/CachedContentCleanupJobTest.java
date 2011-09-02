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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.alfresco.repo.content.caching.CacheFileProps;
import org.alfresco.repo.content.caching.CachingContentStore;
import org.alfresco.repo.content.caching.ContentCacheImpl;
import org.alfresco.repo.content.caching.Key;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Tests for the CachedContentCleanupJob
 * 
 * @author Matt Ward
 */
public class CachedContentCleanupJobTest
{
    private enum UrlSource { PROPS_FILE, REVERSE_CACHE_LOOKUP, NOT_PRESENT };
    private ApplicationContext ctx;
    private CachingContentStore cachingStore;
    private ContentCacheImpl cache;
    private File cacheRoot;
    private CachedContentCleaner cleaner;
    
    @Before
    public void setUp()
    {
        String conf = "classpath:cachingstore/test-context.xml";
        String cleanerConf = "classpath:cachingstore/test-cleaner-context.xml";
        ctx = ApplicationContextHelper.getApplicationContext(new String[] { conf, cleanerConf });
        
        cachingStore = (CachingContentStore) ctx.getBean("cachingContentStore");
        
        cache = (ContentCacheImpl) ctx.getBean("contentCache");
        cacheRoot = cache.getCacheRoot();
        
        cleaner = (CachedContentCleaner) ctx.getBean("cachedContentCleaner");
    }

    
    @Test
    public void filesNotInCacheAreDeleted()
    {
        cleaner.setMaxDeleteWatchCount(0);
        int numFiles = 300; // Must be a multiple of number of UrlSource types being tested
        File[] files = new File[300];
        for (int i = 0; i < numFiles; i++)
        {
            // Testing with a number of files. The cached file cleaner will be able to determine the 'original'
            // content URL for each file by either retrieving from the companion properties file, or performing
            // a 'reverse lookup' in the cache (i.e. cache.contains(Key.forCacheFile(...))), or there will be no
            // URL determinable for the file.
            UrlSource urlSource = UrlSource.values()[i % UrlSource.values().length];
            File cacheFile = createCacheFile(urlSource, i);
            files[i] = cacheFile;
        }

        // Run cleaner
        cleaner.execute();
        
        // check all files deleted
        for (File file : files)
        {
            assertFalse("File should have been deleted: " + file, file.exists());
        }
    }

    @Test
    public void emptyParentDirectoriesAreDeleted() throws FileNotFoundException
    {
        cleaner.setMaxDeleteWatchCount(0);
        File file = new File(cacheRoot, "243235984/a/b/c/d.bin");
        file.getParentFile().mkdirs();
        PrintWriter writer = new PrintWriter(file);
        writer.println("Content for emptyParentDirectoriesAreDeleted");
        writer.close();
        assertTrue("Directory should exist", new File(cacheRoot, "243235984/a/b/c").exists());
        
        cleaner.handle(file);
        
        assertFalse("Directory should have been deleted", new File(cacheRoot, "243235984").exists());
    }
    
    @Test
    public void markedFilesHaveDeletionDeferredUntilCorrectPassOfCleaner()
    {
        // A non-advisable setting but useful for testing, maxDeleteWatchCount of zero
        // which should result in immediate deletion upon discovery of content no longer in the cache.
        cleaner.setMaxDeleteWatchCount(0);
        File file = createCacheFile(UrlSource.NOT_PRESENT, 0);
        
        cleaner.handle(file);
        checkFilesDeleted(file);
        
        // Anticipated to be the most common setting: maxDeleteWatchCount of 1.
        cleaner.setMaxDeleteWatchCount(1);
        file = createCacheFile(UrlSource.NOT_PRESENT, 0);
        
        cleaner.handle(file);
        checkWatchCountForCacheFile(file, 1);
        
        cleaner.handle(file);
        checkFilesDeleted(file);
        
        // Check that some other arbitrary figure for maxDeleteWatchCount works correctly.
        cleaner.setMaxDeleteWatchCount(3);
        file = createCacheFile(UrlSource.NOT_PRESENT, 0);
        
        cleaner.handle(file);
        checkWatchCountForCacheFile(file, 1);
        
        cleaner.handle(file);
        checkWatchCountForCacheFile(file, 2);
        
        cleaner.handle(file);
        checkWatchCountForCacheFile(file, 3);
        
        cleaner.handle(file);
        checkFilesDeleted(file);
    }


    private void checkFilesDeleted(File file)
    {
        assertFalse("File should have been deleted: " + file, file.exists());
        CacheFileProps props = new CacheFileProps(file);
        assertFalse("Properties file should have been deleted, cache file: " + file, props.exists());
    }


    private void checkWatchCountForCacheFile(File file, Integer expectedWatchCount)
    {
        assertTrue("File should still exist: " + file, file.exists());
        CacheFileProps props = new CacheFileProps(file);
        props.load();
        assertEquals("File should contain correct deleteWatchCount", expectedWatchCount, props.getDeleteWatchCount());
    }
    
    
    @Test
    public void filesInCacheAreNotDeleted()
    {
        cleaner.setMaxDeleteWatchCount(0);
        
        // The SlowContentStore will always give out content when asked,
        // so asking for any content will cause something to be cached.
        int numFiles = 50;
        for (int i = 0; i < numFiles; i++)
        {
            ContentReader reader = cachingStore.getReader(String.format("store://caching/store/url-%03d.bin", i));
            reader.getContentString();
        }
        
        cleaner.execute();
        
        for (int i = 0; i < numFiles; i++)
        {
            File cacheFile = new File(cache.getCacheFilePath(String.format("store://caching/store/url-%03d.bin", i)));
            assertTrue("File should exist", cacheFile.exists());
        }
    }
    
    
    private File createCacheFile(UrlSource urlSource, int fileNum)
    {
        File file = new File(cacheRoot, ContentCacheImpl.createNewCacheFilePath());
        file.getParentFile().mkdirs();
        writeSampleContent(file);
        String contentUrl = String.format("protocol://some/made/up/url-%03d.bin", fileNum);
        
        switch(urlSource)
        {
            case NOT_PRESENT:
                // cache won't be able to determine original content URL for the file
                break;
            case PROPS_FILE:
                // file with content URL in properties file
                CacheFileProps props = new CacheFileProps(file);
                props.setContentUrl(contentUrl);
                props.store();
                break;
            case REVERSE_CACHE_LOOKUP:
                // file with content URL in reverse lookup cache - but not 'in the cache' (forward lookup).
                cache.putIntoLookup(Key.forCacheFile(file), contentUrl);
        }
        assertTrue("File should exist", file.exists());
        return file;
    }


    private void writeSampleContent(File file)
    {
        try
        {
            PrintWriter writer = new PrintWriter(file);
            writer.println("Content for sample file in " + getClass().getName());
        }
        catch (Throwable e)
        {
            throw new RuntimeException("Couldn't write file: " + file, e);
        }
    }
}
