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
import java.io.IOException;
import java.io.PrintWriter;

import org.alfresco.repo.content.caching.CacheFileProps;
import org.alfresco.repo.content.caching.CachingContentStore;
import org.alfresco.repo.content.caching.ContentCacheImpl;
import org.alfresco.repo.content.caching.Key;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
    private static ApplicationContext ctx;
    private CachingContentStore cachingStore;
    private ContentCacheImpl cache;
    private File cacheRoot;
    private CachedContentCleaner cleaner;
    
    
    @BeforeClass
    public static void beforeClass()
    {
        String conf = "classpath:cachingstore/test-context.xml";
        String cleanerConf = "classpath:cachingstore/test-cleaner-context.xml";
        ctx = ApplicationContextHelper.getApplicationContext(new String[] { conf, cleanerConf });
    }
    
    
    @Before
    public void setUp() throws IOException
    {
        cachingStore = (CachingContentStore) ctx.getBean("cachingContentStore");
        cache = (ContentCacheImpl) ctx.getBean("contentCache");
        cacheRoot = cache.getCacheRoot();
        cleaner = (CachedContentCleaner) ctx.getBean("cachedContentCleaner");
        cleaner.setMinFileAgeMillis(0);
        cleaner.setMaxDeleteWatchCount(0);

        // Clear the cache from disk and memory
        cache.removeAll();
        FileUtils.cleanDirectory(cacheRoot);
    }

    
    @Test
    public void filesNotInCacheAreDeleted()
    {
        cleaner.setMaxDeleteWatchCount(0);
        int numFiles = 300; // Must be a multiple of number of UrlSource types being tested
        long totalSize = 0; // what is the total size of the sample files?
        File[] files = new File[numFiles];
        for (int i = 0; i < numFiles; i++)
        {
            // Testing with a number of files. The cached file cleaner will be able to determine the 'original'
            // content URL for each file by either retrieving from the companion properties file, or performing
            // a 'reverse lookup' in the cache (i.e. cache.contains(Key.forCacheFile(...))), or there will be no
            // URL determinable for the file.
            UrlSource urlSource = UrlSource.values()[i % UrlSource.values().length];
            File cacheFile = createCacheFile(urlSource, i, false);
            files[i] = cacheFile;
            totalSize += cacheFile.length();
        }

        // Run cleaner
        cleaner.execute();
        
        // check all files deleted
        for (File file : files)
        {
            assertFalse("File should have been deleted: " + file, file.exists());
        }
        
        assertEquals("Incorrect number of deleted files", numFiles, cleaner.getNumFilesDeleted());
        assertEquals("Incorrect total size of files deleted", totalSize, cleaner.getSizeFilesDeleted());
    }
    
    
    @Test
    public void filesNewerThanMinFileAgeMillisAreNotDeleted() throws InterruptedException
    {
        final long minFileAge = 1000;
        cleaner.setMinFileAgeMillis(minFileAge);
        cleaner.setMaxDeleteWatchCount(0);
        int numFiles = 10;
        
        File[] oldFiles = new File[numFiles];
        for (int i = 0; i < numFiles; i++)
        {
            oldFiles[i] = createCacheFile(UrlSource.REVERSE_CACHE_LOOKUP, i, false);
        }
        
        // Sleep to make sure 'old' files really are older than minFileAgeMillis
        Thread.sleep(minFileAge);
        
        File[] newFiles = new File[numFiles];
        long newFilesTotalSize = 0;
        for (int i = 0; i < numFiles; i++)
        {
            newFiles[i] = createCacheFile(UrlSource.REVERSE_CACHE_LOOKUP, i, false);
            newFilesTotalSize += newFiles[i].length();
        }


        // The cleaner must finish before any of the newFiles are older than minFileAge. If the files are too
        // old the test will fail and it will be necessary to rethink how to test this.
        cleaner.execute();
        
        // check all 'old' files deleted
        for (File file : oldFiles)
        {
            assertFalse("File should have been deleted: " + file, file.exists());
        }
        // check all 'new' files still present
        for (File file : newFiles)
        {
            assertTrue("File should not have been deleted: " + file, file.exists());
        }
        
        assertEquals("Incorrect number of deleted files", newFiles.length, cleaner.getNumFilesDeleted());
        assertEquals("Incorrect total size of files deleted", newFilesTotalSize, cleaner.getSizeFilesDeleted());
    }

    @Test
    public void aggressiveCleanReclaimsTargetSpace() throws InterruptedException
    {
        int numFiles = 30;
        File[] files = new File[numFiles];
        for (int i = 0; i < numFiles; i++)
        {
            // Make sure it's in the cache - all the files will be in the cache, so the
            // cleaner won't clean any up once it has finished aggressively reclaiming space.
            files[i] = createCacheFile(UrlSource.REVERSE_CACHE_LOOKUP, i, true);
        }

        // How much space to reclaim - seven files worth (all files are same size)
        long fileSize = files[0].length();
        long sevenFilesSize = 7 * fileSize;
        
        // We'll get it to clean seven files worth aggressively and then it will continue non-aggressively.
        // It will delete the older files aggressively (i.e. the ones prior to the two second sleep) and
        // then will examine the new files for potential deletion.
        // Since some of the newer files are not in the cache, it will delete those.
        cleaner.executeAggressive("aggressiveCleanReclaimsTargetSpace()", sevenFilesSize);
        
        int numDeleted = 0;
        
        for (File f : files)
        {
            if (!f.exists())
            {
                numDeleted++;
            }
        }
        // How many were definitely deleted?
        assertEquals("Wrong number of files deleted", 7 , numDeleted);
        
        // The cleaner should have recorded the correct number of deletions
        assertEquals("Incorrect number of deleted files", 7, cleaner.getNumFilesDeleted());
        assertEquals("Incorrect total size of files deleted", sevenFilesSize, cleaner.getSizeFilesDeleted());
    }
    
    @Ignore()
    @Test
    public void standardCleanAfterAggressiveFinished() throws InterruptedException
    {
        int numFiles = 30;
        int newerFilesIndex = 14;
        File[] files = new File[numFiles];
        
        for (int i = 0; i < numFiles; i++)
        {
            if (i == newerFilesIndex)
            {
                // Files after this sleep will definitely be in 'newer' directories.
                Thread.sleep(2000);
            }
                        
            if (i >= 21 && i <= 24)
            {
                // 21 to 24 will be deleted after the aggressive deletions (once the cleaner has returned
                // to normal cleaning), because they are not in the cache.
                files[i] = createCacheFile(UrlSource.NOT_PRESENT, i, false);
            }
            else
            {
                // All other files will be in the cache
                files[i] = createCacheFile(UrlSource.REVERSE_CACHE_LOOKUP, i, true);
            }
        }
        
        // How much space to reclaim - seven files worth (all files are same size)
        long fileSize = files[0].length();
        long sevenFilesSize = 7 * fileSize;
        
        // We'll get it to clean seven files worth aggressively and then it will continue non-aggressively.
        // It will delete the older files aggressively (i.e. the ones prior to the two second sleep) and
        // then will examine the new files for potential deletion.
        // Since some of the newer files are not in the cache, it will delete those.
        cleaner.executeAggressive("standardCleanAfterAggressiveFinished()", sevenFilesSize);
        
        for (int i = 0; i < numFiles; i++)
        {
            File f = files[i];
            String newerOrOlder = ((i >= newerFilesIndex) ? "newer" : "older");
            System.out.println("files[" + i + "] = " + newerOrOlder + " file, exists=" + f.exists());
        }
        
        int numOlderFilesDeleted = 0;
        for (int i = 0; i < newerFilesIndex; i++)
        {
            if (!files[i].exists())
            {
                numOlderFilesDeleted++;
            }
        }
        assertEquals("Wrong number of older files deleted", 7, numOlderFilesDeleted);
        
        int numNewerFilesDeleted = 0;
        for (int i = newerFilesIndex; i < numFiles; i++)
        {
            if (!files[i].exists())
            {
                numNewerFilesDeleted++;
            }
        }
        assertEquals("Wrong number of newer files deleted", 4, numNewerFilesDeleted);
        
        // The cleaner should have recorded the correct number of deletions
        assertEquals("Incorrect number of deleted files", 11, cleaner.getNumFilesDeleted());
        assertEquals("Incorrect total size of files deleted", (11*fileSize), cleaner.getSizeFilesDeleted());
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
        File file = createCacheFile(UrlSource.NOT_PRESENT, 0, false);
        
        cleaner.handle(file);
        checkFilesDeleted(file);
        
        // Anticipated to be the most common setting: maxDeleteWatchCount of 1.
        cleaner.setMaxDeleteWatchCount(1);
        file = createCacheFile(UrlSource.NOT_PRESENT, 0, false);
        
        cleaner.handle(file);
        checkWatchCountForCacheFile(file, 1);
        
        cleaner.handle(file);
        checkFilesDeleted(file);
        
        // Check that some other arbitrary figure for maxDeleteWatchCount works correctly.
        cleaner.setMaxDeleteWatchCount(3);
        file = createCacheFile(UrlSource.NOT_PRESENT, 0, false);
        
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
        String url = makeContentUrl();
        int numFiles = 50;
        for (int i = 0; i < numFiles; i++)
        {
            ContentReader reader = cachingStore.getReader(url);
            reader.getContentString();
        }
        
        cleaner.execute();
        
        for (int i = 0; i < numFiles; i++)
        {
            File cacheFile = new File(cache.getCacheFilePath(url));
            assertTrue("File should exist", cacheFile.exists());
        }
    }
    
    
    private File createCacheFile(UrlSource urlSource, int fileNum, boolean putInCache)
    {
        File file = new File(cacheRoot, ContentCacheImpl.createNewCacheFilePath());
        file.getParentFile().mkdirs();
        writeSampleContent(file);
        String contentUrl = makeContentUrl();
        
        if (putInCache)
        {
            cache.putIntoLookup(Key.forUrl(contentUrl), file.getAbsolutePath());
        }
        
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


    private String makeContentUrl()
    {
        return "protocol://some/made/up/url/" + GUID.generate();
    }


    private void writeSampleContent(File file)
    {
        try
        {
            PrintWriter writer = new PrintWriter(file);
            writer.println("Content for sample file in " + getClass().getName());
            writer.close();
        }
        catch (Throwable e)
        {
            throw new RuntimeException("Couldn't write file: " + file, e);
        }
    }
}
