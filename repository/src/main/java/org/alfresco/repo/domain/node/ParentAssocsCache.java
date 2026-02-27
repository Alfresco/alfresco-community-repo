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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Weigher;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.Pair;

/**
 * A Map-like class for storing ParentAssocsInfos, backed by a Google {@link Cache} implementation.
 */
class ParentAssocsCache
{
    private final Cache<Pair<Long, String>, ParentAssocsInfo> cache;

    /**
     * @param size
     *            int
     * @param limitFactor
     *            int
     * @param concurrencyLevel
     *            int
     */
    ParentAssocsCache(int size, int limitFactor, int concurrencyLevel)
    {
        final int maxParentCount = size * limitFactor;

        Weigher<Pair<Long, String>, ParentAssocsInfo> weigher = (key, value) -> {
            var parentAssocsSize = Optional.ofNullable(value)
                    .map(ParentAssocsInfo::getParentAssocs)
                    .map(Map::size)
                    .orElse(1);
            return Math.max(1, parentAssocsSize);
        };

        this.cache = CacheBuilder.newBuilder()
                .maximumWeight(maxParentCount)
                .concurrencyLevel(concurrencyLevel)
                .weigher(weigher)
                .build();
    }

    ParentAssocsInfo get(Pair<Long, String> cacheKey)
    {
        return cache.getIfPresent(cacheKey);
    }

    ParentAssocsInfo get(Pair<Long, String> cacheKey, Callable<ParentAssocsInfo> valueLoader)
    {
        try
        {
            return cache.get(cacheKey, valueLoader);
        }
        catch (Exception e)
        {
            throw new AlfrescoRuntimeException("Failed to load parent associations", e);
        }
    }

    void put(Pair<Long, String> cacheKey, ParentAssocsInfo parentAssocs)
    {
        cache.put(cacheKey, parentAssocs);
    }

    ParentAssocsInfo remove(Pair<Long, String> cacheKey)
    {
        ParentAssocsInfo old = cache.getIfPresent(cacheKey);
        if (old != null)
        {
            cache.invalidate(cacheKey);
        }
        return old;
    }

    void clear()
    {
        cache.invalidateAll();
    }
}
