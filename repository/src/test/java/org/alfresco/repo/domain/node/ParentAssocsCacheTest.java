/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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
package org.alfresco.repo.domain.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.Test;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.Pair;

public class ParentAssocsCacheTest
{
    @Test
    public void testGetReturnsNullWhenAbsent()
    {
        ParentAssocsCache cache = new ParentAssocsCache(10, 2, 1);
        Pair<Long, String> key = new Pair<>(1L, "store");

        assertNull(cache.get(key));
    }

    @Test
    public void testGetWithLoaderCachesValue()
    {
        ParentAssocsCache cache = new ParentAssocsCache(10, 2, 1);
        Pair<Long, String> key = new Pair<>(2L, "store");
        ParentAssocsInfo info = parentAssocsInfo(10L, true);
        AtomicInteger calls = new AtomicInteger();
        Callable<ParentAssocsInfo> loader = () -> {
            calls.incrementAndGet();
            return info;
        };

        ParentAssocsInfo first = cache.get(key, loader);
        ParentAssocsInfo second = cache.get(key, loader);

        assertSame(info, first);
        assertSame(info, second);
        assertSame(info, cache.get(key));
        assertEquals(1, calls.get());
    }

    @Test
    public void testGetWrapsExecutionException()
    {
        ParentAssocsCache cache = new ParentAssocsCache(10, 2, 1);
        Pair<Long, String> key = new Pair<>(3L, "store");
        RuntimeException root = new RuntimeException("boom");
        Callable<ParentAssocsInfo> loader = () -> {
            throw root;
        };

        AlfrescoRuntimeException ex = assertThrows(AlfrescoRuntimeException.class, () -> cache.get(key, loader));

        assertNotNull(ex.getCause());
    }

    @Test
    public void testPutAndRemoveReturnOld()
    {
        ParentAssocsCache cache = new ParentAssocsCache(10, 2, 1);
        Pair<Long, String> key = new Pair<>(4L, "store");
        ParentAssocsInfo info = parentAssocsInfo(20L, false);

        cache.put(key, info);
        ParentAssocsInfo removed = cache.remove(key);

        assertSame(info, removed);
        assertNull(cache.get(key));
    }

    @Test
    public void testClearInvalidatesAll()
    {
        ParentAssocsCache cache = new ParentAssocsCache(10, 2, 1);
        Pair<Long, String> key1 = new Pair<>(5L, "store");
        Pair<Long, String> key2 = new Pair<>(6L, "store");

        cache.put(key1, parentAssocsInfo(30L, true));
        cache.put(key2, parentAssocsInfo(31L, false));
        cache.clear();

        assertNull(cache.get(key1));
        assertNull(cache.get(key2));
    }

    @Test
    public void testConcurrentGetWithLoaderOnlyLoadsOnce() throws Exception
    {
        ParentAssocsCache cache = new ParentAssocsCache(10, 2, 1);
        Pair<Long, String> key = new Pair<>(7L, "store");
        ParentAssocsInfo info = parentAssocsInfo(40L, true);
        AtomicInteger calls = new AtomicInteger();
        Callable<ParentAssocsInfo> loader = () -> {
            calls.incrementAndGet();
            return info;
        };
        int threads = 24;
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try
        {
            List<Future<ParentAssocsInfo>> futures = new ArrayList<>(threads);
            for (int i = 0; i < threads; i++)
            {
                futures.add(executor.submit(() -> {
                    start.await();
                    return cache.get(key, loader);
                }));
            }
            start.countDown();
            for (Future<ParentAssocsInfo> future : futures)
            {
                assertSame(info, future.get(5, TimeUnit.SECONDS));
            }
            assertEquals(1, calls.get());
        }
        finally
        {
            executor.shutdownNow();
        }
    }

    @Test
    public void testConcurrentGetDifferentKeysLoadsEachOnce() throws Exception
    {
        ParentAssocsCache cache = new ParentAssocsCache(50, 2, 1);
        int keys = 16;
        List<Pair<Long, String>> keyList = IntStream.range(0, keys)
                .mapToObj(index -> new Pair<>((long) index + 100, "store"))
                .toList();
        List<AtomicInteger> counters = IntStream.range(0, keys)
                .mapToObj(index -> new AtomicInteger())
                .toList();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try
        {
            List<Future<ParentAssocsInfo>> futures = new ArrayList<>(keys);
            for (int i = 0; i < keys; i++)
            {
                int index = i;
                Pair<Long, String> key = keyList.get(i);
                ParentAssocsInfo info = parentAssocsInfo((long) index + 200, index % 2 == 0);
                Callable<ParentAssocsInfo> loader = () -> {
                    counters.get(index).incrementAndGet();
                    return info;
                };
                futures.add(executor.submit(() -> {
                    start.await();
                    return cache.get(key, loader);
                }));
            }
            start.countDown();
            for (int i = 0; i < keys; i++)
            {
                ParentAssocsInfo value = futures.get(i).get(5, TimeUnit.SECONDS);
                assertSame(cache.get(keyList.get(i)), value);
            }
            assertTrue(counters.stream().allMatch(counter -> counter.get() == 1));
        }
        finally
        {
            executor.shutdownNow();
        }
    }

    private ParentAssocsInfo parentAssocsInfo(Long assocId, boolean isPrimary)
    {
        return new ParentAssocsInfo(false, false, parentAssocEntities(assocId, isPrimary));
    }

    private List<ChildAssocEntity> parentAssocEntities(Long assocId, boolean isPrimary)
    {
        ChildAssocEntity assoc = new ChildAssocEntity();
        assoc.setId(assocId);
        assoc.setPrimary(isPrimary);
        return Arrays.asList(assoc);
    }
}
