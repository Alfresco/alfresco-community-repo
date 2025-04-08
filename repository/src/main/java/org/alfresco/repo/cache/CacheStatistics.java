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
package org.alfresco.repo.cache;

import java.util.Map;

import org.alfresco.repo.cache.TransactionStats.OpType;

/**
 * Centralised cache statistics service. Transactional caches participating in statistical collection will provide their data to this service using the {@link #add(String, TransactionStats)} method. The data is then aggregated so that, for example, the hit ratio for a particular cache may be retrieved.
 * 
 * @since 5.0
 * @author Matt Ward
 */
public interface CacheStatistics
{
    /**
     * Add new details to the system wide cache statistics.
     */
    void add(String cacheName, TransactionStats stats);

    /**
     * Get the number of occurrences of the given operation type, retrieve the number of cache hits that have happened to the cache.
     * 
     * @param cacheName
     *            Name of the cache.
     * @param opType
     *            Type of cache operation.
     * @return long count
     */
    long count(String cacheName, OpType opType);

    /**
     * The mean time in nanoseconds for all operations of the given type.
     * 
     * @param cacheName
     *            The cache name.
     * @param opType
     *            Type of operation, e.g. cache hits.
     * @return Time in nanos (double) or NaN if not available yet.
     */
    double meanTime(String cacheName, OpType opType);

    /**
     * The hit ratio for the given cache, where 1.0 is the maximum possible value and 0.0 represents a cache that has never successfully returned a previously cached value.
     * 
     * @param cacheName
     *            The cache name.
     * @return ratio (double)
     */
    double hitMissRatio(String cacheName);

    /**
     * Retrieve the total number of get operations invoked on the cache (i.e. sum of hits and misses).
     * 
     * @param cacheName
     *            The cache name.
     * @return Count of get operations.
     */
    long numGets(String cacheName);

    /**
     * Retrieve a map containing a snapshot of all of the raw stats (e.g. counts, mean operation times etc.). Since this is a snapshot it is unaffected by future modifications to the statistics - by using {@link CacheStatistics#add(String, TransactionStats)} for example.
     * 
     * @param cacheName
     *            The cache name.
     * @return Map of OpType to OperationStats
     */
    Map<OpType, OperationStats> allStats(String cacheName);
}
