/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.repo.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cache.DefaultSimpleCache;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;

/**
 * Unit tests for {@link AbstractRoutingContentStore#getReader(String)} / {@link AbstractRoutingContentStore#exists(String)} store selection, including the MNT-25951 regression where a slow {@link ContentStore#exists(String)} call (e.g. an S3 HEAD on a stuck socket) was made while holding the routing store lock and so blocked every other content read and write on the node.
 */
public class AbstractRoutingContentStoreTest
{
    private static final String URL_1 = "test://url-1";
    private static final String URL_2 = "test://url-2";
    private static final String NEW_URL = "test://new-url";
    private static final long TIMEOUT_SECONDS = 5;

    private ContentStore storeA;
    private ContentStore storeB;
    private ContentStore writeStore;
    private TestRoutingContentStore routingStore;
    private ExecutorService executor;

    /** Released at the end of every test so that no thread stays parked inside a blocking exists() stub. */
    private final CountDownLatch release = new CountDownLatch(1);

    @Before
    public void setUp()
    {
        storeA = mock(ContentStore.class);
        storeB = mock(ContentStore.class);
        writeStore = mock(ContentStore.class);
        when(storeA.toString()).thenReturn("storeA");
        when(storeB.toString()).thenReturn("storeB");

        routingStore = new TestRoutingContentStore(List.of(storeA, storeB), writeStore);
        executor = Executors.newCachedThreadPool();
    }

    @After
    public void tearDown()
    {
        release.countDown();
        executor.shutdownNow();
    }

    // ---------------------------------------------------------------------------------------------
    // Store selection behaviour
    // ---------------------------------------------------------------------------------------------

    @Test
    public void cacheMissSearchesStoresAndCachesTheOneThatHasTheContent()
    {
        when(storeA.exists(URL_1)).thenReturn(false);
        when(storeB.exists(URL_1)).thenReturn(true);

        assertTrue(routingStore.exists(URL_1));
        verify(storeA).exists(URL_1);
        verify(storeB).exists(URL_1);

        // Second lookup is served from the cache: only the cached store is checked
        assertTrue(routingStore.exists(URL_1));
        verify(storeA, times(1)).exists(URL_1);
        verify(storeB, times(2)).exists(URL_1);
    }

    @Test
    public void searchStopsAtTheFirstStoreThatHasTheContent()
    {
        when(storeA.exists(URL_1)).thenReturn(true);

        assertTrue(routingStore.exists(URL_1));
        verify(storeB, never()).exists(anyString());
    }

    @Test
    public void getReaderUsesTheSelectedStore()
    {
        ContentReader reader = mock(ContentReader.class);
        when(storeA.exists(URL_1)).thenReturn(false);
        when(storeB.exists(URL_1)).thenReturn(true);
        when(storeB.getReader(URL_1)).thenReturn(reader);

        assertSame(reader, routingStore.getReader(URL_1));
        verify(storeA, never()).getReader(anyString());
    }

    @Test
    public void contentMovedFromCachedStoreIsFoundInAnotherStoreAndCacheIsUpdated()
    {
        // Initially in A
        when(storeA.exists(URL_1)).thenReturn(true);
        assertTrue(routingStore.exists(URL_1));

        // Then moved to B
        when(storeA.exists(URL_1)).thenReturn(false);
        when(storeB.exists(URL_1)).thenReturn(true);
        assertTrue(routingStore.exists(URL_1));

        // B is now the cached store, so A is not checked again
        assertTrue(routingStore.exists(URL_1));
        verify(storeA, times(3)).exists(URL_1); // initial search, cached check, re-search
        verify(storeB, times(2)).exists(URL_1); // re-search, cached check
    }

    @Test
    public void missingContentReturnsEmptyReaderAndIsNotCached()
    {
        when(storeA.exists(URL_1)).thenReturn(false);
        when(storeB.exists(URL_1)).thenReturn(false);

        assertFalse(routingStore.exists(URL_1));
        ContentReader reader = routingStore.getReader(URL_1);
        assertTrue(reader instanceof EmptyContentReader);
        assertFalse(reader.exists());

        // Nothing cached: getWriter for the same URL must not complain that the content exists
        when(writeStore.isWriteSupported()).thenReturn(true);
        ContentWriter writer = mock(ContentWriter.class);
        when(writer.getContentUrl()).thenReturn(URL_1);
        when(writeStore.getWriter(any(ContentContext.class))).thenReturn(writer);
        assertSame(writer, routingStore.getWriter(new ContentContext(null, URL_1)));
    }

    @Test
    public void storesThatDoNotSupportTheUrlAreSkipped()
    {
        when(storeA.exists(URL_1)).thenThrow(new UnsupportedContentUrlException(storeA, URL_1));
        when(storeB.exists(URL_1)).thenReturn(true);

        assertTrue(routingStore.exists(URL_1));
    }

    @Test
    public void urlNotSupportedByAnyStoreThrowsUnsupportedContentUrlException()
    {
        when(storeA.exists(URL_1)).thenThrow(new UnsupportedContentUrlException(storeA, URL_1));
        when(storeB.exists(URL_1)).thenThrow(new UnsupportedContentUrlException(storeB, URL_1));

        assertThrows(UnsupportedContentUrlException.class, () -> routingStore.exists(URL_1));
    }

    @Test
    public void cachedStoreThatNoLongerSupportsTheUrlThrowsAndKeepsTheCause()
    {
        when(storeA.exists(URL_1)).thenReturn(true);
        assertTrue(routingStore.exists(URL_1));

        UnsupportedContentUrlException cause = new UnsupportedContentUrlException(storeA, URL_1);
        when(storeA.exists(URL_1)).thenThrow(cause);

        AlfrescoRuntimeException e = assertThrows(AlfrescoRuntimeException.class, () -> routingStore.exists(URL_1));
        assertSame(cause, e.getCause());
    }

    @Test
    public void getWriterCachesTheStoreForTheNewUrl()
    {
        ContentWriter writer = mock(ContentWriter.class);
        when(writer.getContentUrl()).thenReturn(NEW_URL);
        when(writeStore.isWriteSupported()).thenReturn(true);
        when(writeStore.getWriter(any(ContentContext.class))).thenReturn(writer);
        when(writeStore.exists(NEW_URL)).thenReturn(true);

        routingStore.getWriter(new ContentContext(null, null));

        // The read for the new URL goes straight to the store that wrote it
        assertTrue(routingStore.exists(NEW_URL));
        verify(storeA, never()).exists(anyString());
        verify(storeB, never()).exists(anyString());
        // and a second write to the same URL is rejected
        assertThrows(ContentExistsException.class, () -> routingStore.getWriter(new ContentContext(null, NEW_URL)));
    }

    // ---------------------------------------------------------------------------------------------
    // MNT-25951: exists() must not be called while holding the routing store lock
    // ---------------------------------------------------------------------------------------------

    /**
     * A thread blocked in exists() on the cached store (cache-hit path) used to hold the read lock. Any thread wanting the write lock, such as {@link AbstractRoutingContentStore#getWriter(ContentContext)} caching a new URL, then queued behind it and, because the lock is non-fair, every new reader queued behind that writer.
     */
    @Test
    public void slowExistsOnCachedStoreDoesNotBlockWriters() throws Exception
    {
        // Put URL_1 -> storeA in the cache
        when(storeA.exists(URL_1)).thenReturn(true);
        assertTrue(routingStore.exists(URL_1));

        // Make the next exists() on storeA hang; when it returns, the content is found to have moved to storeB
        when(storeB.exists(URL_1)).thenReturn(true);
        CountDownLatch entered = blockExists(storeA, URL_1);
        Future<Boolean> slowRead = executor.submit(() -> routingStore.exists(URL_1));
        awaitEntered(entered);

        // A writer must be able to cache its new URL meanwhile
        ContentWriter writer = mock(ContentWriter.class);
        when(writer.getContentUrl()).thenReturn(NEW_URL);
        when(writeStore.isWriteSupported()).thenReturn(true);
        when(writeStore.getWriter(any(ContentContext.class))).thenReturn(writer);
        Future<ContentWriter> write = executor.submit(() -> routingStore.getWriter(new ContentContext(null, null)));
        assertSame("getWriter was blocked by a slow exists() on the cached store", writer, getWithin(write));

        // And readers of other URLs too
        when(storeB.exists(URL_2)).thenReturn(true);
        Future<Boolean> otherRead = executor.submit(() -> routingStore.exists(URL_2));
        assertTrue("A read of another URL was blocked by a slow exists() on the cached store", getWithin(otherRead));

        release.countDown();
        assertTrue(getWithin(slowRead));
    }

    /**
     * A thread blocked in exists() while searching all stores (cache-miss path) used to hold the write lock, so every other read, even one served from the cache, and every write blocked behind it.
     */
    @Test
    public void slowExistsDuringStoreSearchDoesNotBlockOtherReadsOrWrites() throws Exception
    {
        // Put URL_2 -> storeB in the cache
        when(storeA.exists(URL_2)).thenReturn(false);
        when(storeB.exists(URL_2)).thenReturn(true);
        assertTrue(routingStore.exists(URL_2));

        // URL_1 is not cached and the first store probed hangs (e.g. an S3 HEAD on a stuck socket)
        when(storeB.exists(URL_1)).thenReturn(true);
        CountDownLatch entered = blockExists(storeA, URL_1);
        Future<Boolean> slowRead = executor.submit(() -> routingStore.exists(URL_1));
        awaitEntered(entered);

        // A cached read must not wait
        Future<Boolean> cachedRead = executor.submit(() -> routingStore.exists(URL_2));
        assertTrue("A cached read was blocked by a slow exists() during the store search", getWithin(cachedRead));

        // Nor must a writer
        ContentWriter writer = mock(ContentWriter.class);
        when(writer.getContentUrl()).thenReturn(NEW_URL);
        when(writeStore.isWriteSupported()).thenReturn(true);
        when(writeStore.getWriter(any(ContentContext.class))).thenReturn(writer);
        Future<ContentWriter> write = executor.submit(() -> routingStore.getWriter(new ContentContext(null, null)));
        assertSame("getWriter was blocked by a slow exists() during the store search", writer, getWithin(write));

        release.countDown();
        assertTrue(getWithin(slowRead));
        // The search result was still cached once the slow call returned
        assertTrue(routingStore.exists(URL_1));
        verify(storeA, times(1)).exists(URL_1);
    }

    /**
     * Two threads missing the cache for different URLs search the stores concurrently instead of one after the other.
     */
    @Test
    public void concurrentCacheMissesForDifferentUrlsDoNotSerialise() throws Exception
    {
        when(storeB.exists(URL_1)).thenReturn(true);
        when(storeA.exists(URL_2)).thenReturn(true);
        CountDownLatch entered = blockExists(storeA, URL_1);
        Future<Boolean> slowRead = executor.submit(() -> routingStore.exists(URL_1));
        awaitEntered(entered);

        Future<Boolean> otherMiss = executor.submit(() -> routingStore.exists(URL_2));
        assertTrue("A cache miss for another URL was blocked by a slow exists()", getWithin(otherMiss));

        release.countDown();
        assertTrue(getWithin(slowRead));
    }

    /**
     * Two threads missing the cache for the same URL at the same time both find, and cache, the same store.
     */
    @Test
    public void concurrentCacheMissesForTheSameUrlResolveToTheSameStore() throws Exception
    {
        int threads = 8;
        CountDownLatch allInside = new CountDownLatch(threads);
        doAnswer(inv -> {
            allInside.countDown();
            allInside.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return false;
        }).when(storeA).exists(URL_1);
        when(storeB.exists(URL_1)).thenReturn(true);
        ContentReader reader = mock(ContentReader.class);
        when(storeB.getReader(URL_1)).thenReturn(reader);

        List<Future<ContentReader>> reads = new ArrayList<>();
        for (int i = 0; i < threads; i++)
        {
            reads.add(executor.submit(() -> routingStore.getReader(URL_1)));
        }
        for (Future<ContentReader> read : reads)
        {
            assertSame(reader, getWithin(read));
        }
        // All threads were inside storeA.exists() at the same time, i.e. none was blocked by the lock
        assertEquals(0, allInside.getCount());
    }

    // ---------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------

    /**
     * Makes {@code store.exists(url)} block until {@link #release} is counted down, then return {@code false}.
     *
     * @return a latch that is counted down when a thread enters the blocking call
     */
    private CountDownLatch blockExists(ContentStore store, String url)
    {
        CountDownLatch entered = new CountDownLatch(1);
        doAnswer(inv -> {
            entered.countDown();
            release.await();
            return false;
        }).when(store).exists(url);
        return entered;
    }

    private static void awaitEntered(CountDownLatch entered) throws InterruptedException
    {
        assertTrue("Slow exists() was never called", entered.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    private static <T> T getWithin(Future<T> future) throws Exception
    {
        try
        {
            return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
        catch (TimeoutException e)
        {
            future.cancel(true);
            throw new AssertionError("Operation did not complete within " + TIMEOUT_SECONDS + "s - it is blocked on the routing store lock", e);
        }
    }

    private static class TestRoutingContentStore extends AbstractRoutingContentStore
    {
        private final List<ContentStore> stores;
        private final ContentStore writeStore;

        TestRoutingContentStore(List<ContentStore> stores, ContentStore writeStore)
        {
            this.stores = stores;
            this.writeStore = writeStore;
            setStoresCache(new DefaultSimpleCache<>(100, getClass().getName()));
        }

        @Override
        protected List<ContentStore> getAllStores()
        {
            return stores;
        }

        @Override
        protected ContentStore selectWriteStore(ContentContext ctx)
        {
            return writeStore;
        }
    }
}
