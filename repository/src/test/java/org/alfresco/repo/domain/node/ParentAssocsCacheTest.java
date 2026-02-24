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
package org.alfresco.repo.domain.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

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
