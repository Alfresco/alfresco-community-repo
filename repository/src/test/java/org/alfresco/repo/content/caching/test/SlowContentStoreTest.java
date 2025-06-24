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
package org.alfresco.repo.content.caching.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.caching.CachingContentStore;
import org.alfresco.repo.content.caching.ContentCacheImpl;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.testing.category.LuceneTests;

/**
 * Tests that exercise the CachingContentStore in conjunction with a backing store that runs deliberately slowly.
 * 
 * @author Matt Ward
 */
@Category(LuceneTests.class)
public class SlowContentStoreTest
{
    private static ClassPathXmlApplicationContext ctx;
    private CachingContentStore cachingStore;
    private static final Log logger = LogFactory.getLog(SlowContentStoreTest.class);

    @BeforeClass
    public static void setUpClass()
    {
        String slowconf = "classpath:cachingstore/test-slow-context.xml";
        ctx = (ClassPathXmlApplicationContext) ApplicationContextHelper.getApplicationContext(new String[]{slowconf});
    }

    @Before
    public void setUp()
    {
        cachingStore = (CachingContentStore) ctx.getBean("cachingContentStore");
        cachingStore.setCacheOnInbound(false);

        // Clear the cache before each test case.
        ContentCacheImpl cache = (ContentCacheImpl) ctx.getBean("contentCache");
        cache.removeAll();
    }

    @Test
    public void readsAreFasterFromCache()
    {
        // First read will hit the SLOW backing store
        TimedStoreReader storeReader = new TimedStoreReader();
        storeReader.execute();
        assertTrue("First read should take a while", storeReader.timeTakenMillis() > 1000);
        logger.debug(String.format("First read took %ds", storeReader.timeTakenMillis()));
        // The content came from the slow backing store...
        assertEquals("This is the content for my slow ReadableByteChannel", storeReader.content);

        // Subsequent reads will hit the cache
        for (int i = 0; i < 5; i++)
        {
            storeReader = new TimedStoreReader();
            storeReader.execute();
            assertTrue("Subsequent reads should be fast", storeReader.timeTakenMillis() < 100);
            logger.debug(String.format("Cache read took %ds", storeReader.timeTakenMillis()));
            // The content came from the slow backing store, but was cached...
            assertEquals("This is the content for my slow ReadableByteChannel", storeReader.content);
        }
    }

    @Test
    public void writeThroughCacheResultsInFastReadFirstTime()
    {
        cachingStore.setCacheOnInbound(true);

        // This content will be cached on the way in
        cachingStore.getWriter(new ContentContext(null, "any-url")).putContent("Content written from " + getClass().getSimpleName());

        // First read will hit cache
        TimedStoreReader storeReader = new TimedStoreReader();
        storeReader.execute();
        assertTrue("First read should be fast", storeReader.timeTakenMillis() < 100);
        logger.debug(String.format("First read took %ds", storeReader.timeTakenMillis()));
        assertEquals("Content written from " + getClass().getSimpleName(), storeReader.content);

        // Subsequent reads will also hit the cache
        for (int i = 0; i < 5; i++)
        {
            storeReader = new TimedStoreReader();
            storeReader.execute();
            assertTrue("Subsequent reads should be fast", storeReader.timeTakenMillis() < 100);
            logger.debug(String.format("Cache read took %ds", storeReader.timeTakenMillis()));
            // The original cached content, still cached...
            assertEquals("Content written from " + getClass().getSimpleName(), storeReader.content);
        }
    }

    private class TimedStoreReader extends TimedExecutor
    {
        String content;

        @Override
        protected void doExecute()
        {
            content = cachingStore.getReader("any-url").getContentString();
            logger.debug("Read content: " + content);
        }
    }

    private abstract class TimedExecutor
    {
        private long start;
        private long finish;

        public void execute()
        {
            start = System.currentTimeMillis();
            doExecute();
            finish = System.currentTimeMillis();
        }

        public long timeTakenMillis()
        {
            return finish - start;
        }

        protected abstract void doExecute();
    }
}
