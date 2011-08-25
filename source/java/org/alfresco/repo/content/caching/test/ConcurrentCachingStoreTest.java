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
package org.alfresco.repo.content.caching.test;


import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.alfresco.repo.content.caching.CachingContentStore;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Tests to ensure that the CachingContentStore works as expected under highly concurrent load.
 * 
 * @author Matt Ward
 */
public class ConcurrentCachingStoreTest
{
    private static final Log log = LogFactory.getLog(ConcurrentCachingStoreTest.class);
    // NUM_THREADS must be at least 2 x NUM_URLS to ensure each URLs is accessed by more than one thread.
    private static final int NUM_THREADS = 2000;
    private static final int NUM_URLS = 40;
    private ApplicationContext ctx;
    private CachingContentStore store;
    private SlowContentStore backingStore;

    @Before
    public void setUp()
    {
        String conf = "classpath:cachingstore/test-context.xml";
        String slowconf = "classpath:cachingstore/test-slow-context.xml";
        ctx = ApplicationContextHelper.getApplicationContext(new String[] { conf, slowconf });
        
        store = (CachingContentStore) ctx.getBean("cachingContentStore");
        store.setCacheOnInbound(false);
        
        backingStore = (SlowContentStore) ctx.getBean("backingStore");
    }

    
    @Test
    public void concurrentReadsWillReadCacheOncePerURL() throws InterruptedException
    {
        // Attack with multiple threads
        Thread[] threads = new Thread[NUM_THREADS];
        for (int i = 0; i < NUM_THREADS; i++)
        {
            CacheReaderThread t = new CacheReaderThread(i, NUM_URLS);
            threads[i] = t;
            t.start();
        }
        
        for (int i = 0; i < threads.length; i++)
            threads[i].join();
        
        
        log.debug("\nResults:");
        
        // Check how many times the backing store was read from
        int failedURLs = 0;
        
        for (Map.Entry<String, AtomicLong> entry : backingStore.getUrlHits().entrySet())
        {
            String url = entry.getKey();
            long numHits = entry.getValue().get();
            log.debug("URL: " + url + ", hits: " + numHits);
            
            if (numHits > 1) failedURLs++;
        }
        
        
        // If any of the URLs were accessed more than once, then the test will fail.
        if (failedURLs > 0)
            Assert.fail(failedURLs + " URLs were requested more than once.");
    }
    

    
    private class CacheReaderThread extends Thread
    {
        private final int threadNum;
        private final int numUrls;
        private int reads = 50;
        
        CacheReaderThread(int threadNum, int numUrls) {
            super(CacheReaderThread.class.getSimpleName() + "-" + threadNum);
            this.threadNum = threadNum;
            this.numUrls = numUrls;
        }
        
        @Override
        public void run()
        {
            while (reads > 0)
            {
                String url = generateUrlToRead();
                ContentReader reader = store.getReader(url);
                String content = reader.getContentString();
                log.debug("Thread: " + getName() + ", URL: " + url + ", content: " + content);
                reads--;
            }
        }

        private String generateUrlToRead()
        {
            int urlNum = threadNum % numUrls;
            return "store://2010/11/5/17/33/" + urlNum + ".bin";
        }   
    }
}
