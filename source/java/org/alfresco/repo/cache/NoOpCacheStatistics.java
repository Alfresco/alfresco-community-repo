/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.cache;

import org.alfresco.repo.cache.TransactionStats.OpType;

/**
 * A do-nothing implementation of {@link CacheStatistics}. Used
 * by the {@link TransactionalCache} if it is not explicitly
 * injected with an implementation.
 * 
 * @since 5.0
 * @author Matt Ward
 */
public class NoOpCacheStatistics implements CacheStatistics
{
    @Override
    public void add(String cacheName, TransactionStats stats)
    {
        // Does nothing
    }

    @Override
    public long count(String cacheName, OpType opType)
    {
        return 0;
    }

    @Override
    public double meanTime(String cacheName, OpType opType)
    {
        return Double.NaN;
    }

    @Override
    public double hitMissRatio(String cacheName)
    {
        return Double.NaN;
    }
}
