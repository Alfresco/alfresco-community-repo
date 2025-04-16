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
package org.alfresco.repo.content.caching.quota;

import static org.junit.Assert.assertEquals;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.SizeFileComparator;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.caching.CachingContentStore;
import org.alfresco.repo.content.caching.ContentCacheImpl;
import org.alfresco.repo.content.caching.cleanup.CachedContentCleaner;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.testing.category.LuceneTests;

/**
 * Tests for the StandardQuotaStrategy.
 * 
 * @author Matt Ward
 */
@Category(LuceneTests.class)
public class StandardQuotaStrategyTest
{
    private static ApplicationContext ctx;
    private CachingContentStore store;
    private static byte[] aKB;
    private ContentCacheImpl cache;
    private File cacheRoot;
    private StandardQuotaStrategy quota;
    private CachedContentCleaner cleaner;

    @BeforeClass
    public static void beforeClass()
    {
        ctx = ApplicationContextHelper.getApplicationContext(new String[]{
                "classpath:cachingstore/test-std-quota-context.xml"
        });

        aKB = new byte[1024];
        Arrays.fill(aKB, (byte) 36);
    }

    @AfterClass
    public static void afterClass()
    {
        ApplicationContextHelper.closeApplicationContext();
    }

    @Before
    public void setUp() throws Exception
    {
        store = (CachingContentStore) ctx.getBean("cachingContentStore");
        store.setCacheOnInbound(true);
        cache = (ContentCacheImpl) ctx.getBean("contentCache");
        cacheRoot = cache.getCacheRoot();
        quota = (StandardQuotaStrategy) ctx.getBean("quotaManager");
        quota.setCurrentUsageBytes(0);
        // No file size limit
        quota.setMaxFileSizeMB(0);
        cleaner = (CachedContentCleaner) ctx.getBean("cachedContentCleaner");
        // Empty the in-memory cache
        cache.removeAll();

        FileUtils.cleanDirectory(cacheRoot);
    }

    @Test
    public void cleanerWillTriggerAtCorrectThreshold() throws IOException, InterruptedException
    {
        // Write 15 x 1MB files. This will not trigger any quota related actions.
        // Quota is 20MB. The quota manager will...
        // * start the cleaner at 16MB (80% of 20MB)
        // * refuse to cache any more files at 18MB (90% of 20MB)
        List<String> contentURLs = new ArrayList<String>();
        for (int i = 0; i < 15; i++)
        {
            String url = writeSingleFileInMB(1);
            contentURLs.add(url);
        }
        // All 15 should be retained.
        assertEquals(15, findCacheFiles().size());

        // Simulate eviction from the in-memory cache. We'll evict 10, so that 6 of the
        // eventual 16 created files have details remaining in the cache.
        //
        // Note, we're simulating eviction here, rather than imposing, for example, a maxItems on the
        // cache (i.e. the "cachingContentStoreCache" bean) since cache behaviour isn't easily deterministic
        // in this respect - the google guava cache in particular, evicts items as the cache size *approaches*
        // the size limit.
        for (int evictCount = 0; evictCount < 10; evictCount++)
        {
            cache.remove(contentURLs.get(evictCount));
        }

        // Writing one more file should trigger a clean.
        writeSingleFileInMB(1);

        Thread.sleep(200);
        while (cleaner.isRunning())
        {
            Thread.sleep(50);
        }

        // Since 6 of the 16 total had details in the in-memory cache at the time the cleaner
        // ran, then only 6 files should remain on disk - the rest should have been removed by the cleaner.
        assertEquals(6, findCacheFiles().size());
    }

    @Test
    public void cachingIsDisabledAtCorrectThreshold() throws IOException
    {
        // Write 4 x 6MB files.
        for (int i = 0; i < 4; i++)
        {
            writeSingleFileInMB(6);
        }

        // Only the first 3 are cached - caching is disabled after that as
        // the panic threshold has been reached.
        assertEquals(3, findCacheFiles().size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void largeContentCacheFilesAreNotKeptOnDisk() throws IOException
    {
        quota.setMaxFileSizeMB(3);
        writeSingleFileInMB(1);
        writeSingleFileInMB(2);
        writeSingleFileInMB(3);
        writeSingleFileInMB(4);

        List<File> files = new ArrayList<File>(findCacheFiles());
        assertEquals(3, files.size());
        Collections.sort(files, SizeFileComparator.SIZE_COMPARATOR);
        assertEquals(1, files.get(0).length() / FileUtils.ONE_MB);
        assertEquals(2, files.get(1).length() / FileUtils.ONE_MB);
        assertEquals(3, files.get(2).length() / FileUtils.ONE_MB);
    }

    private String writeSingleFileInMB(int sizeInMb) throws IOException
    {
        ContentWriter writer = store.getWriter(ContentContext.NULL_CONTEXT);
        File content = createFileOfSize(sizeInMb * 1024);
        writer.putContent(content);
        return writer.getContentUrl();
    }

    private File createFileOfSize(long sizeInKB) throws IOException
    {
        File file = new File(TempFileProvider.getSystemTempDir(), GUID.generate() + ".generated");
        file.deleteOnExit();
        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
        for (long i = 0; i < sizeInKB; i++)
        {
            os.write(aKB);
        }
        os.close();

        return file;
    }

    @SuppressWarnings("unchecked")
    private Collection<File> findCacheFiles()
    {
        return FileUtils.listFiles(cacheRoot, new SuffixFileFilter(".bin"), TrueFileFilter.INSTANCE);
    }

    /**
     * Not a unit test, but useful to fire up a lot of writers that will push the CachingContentStore's StandardQuotaStrategy beyond the panic threshold. The behaviour can then be monitored with, for example, a profiler.
     * 
     * @throws Exception
     */
    private void concurrencySmokeTest() throws Exception
    {
        StandardQuotaStrategyTest.beforeClass();
        setUp();
        // Need to set maxDeleteWatch count to > 0
        // (0 is useful in unit tests, but for real usage must not be used)
        cleaner.setMaxDeleteWatchCount(1);

        final int numThreads = 100;
        Thread[] writers = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++)
        {
            final String threadName = "WriterThread[" + i + "]";
            Runnable runnable = new Runnable() {
                @Override
                public void run()
                {
                    while (true)
                    {
                        writeFile();
                        pause();
                    }
                }

                private void writeFile()
                {
                    try
                    {
                        writeSingleFileInMB(1);
                    }
                    catch (IOException error)
                    {
                        throw new RuntimeException(threadName + " couldn't write file.", error);
                    }
                }

                private void pause()
                {
                    long pauseTimeMillis = Math.round(Math.random() * 2000);
                    try
                    {
                        Thread.sleep(pauseTimeMillis);
                    }
                    catch (InterruptedException error)
                    {
                        // Swallow the exception and carry on.
                        System.out.println(threadName + " InterruptedException.");
                    }
                }
            };
            Thread writerThread = new Thread(runnable);
            writerThread.setName(threadName);
            writers[i] = writerThread;

            writerThread.start();
        }
    }

    public static void main(String[] args) throws Exception
    {
        StandardQuotaStrategyTest test = new StandardQuotaStrategyTest();
        test.concurrencySmokeTest();
    }

}
