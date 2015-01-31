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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.alfresco.repo.cache.TransactionStats.OpType;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Simple non-persistent implementation of {@link CacheStatistics}. Statistics
 * are empty at repository startup.
 * 
 * @since 5.0
 * @author Matt Ward
 */
public class InMemoryCacheStatistics implements CacheStatistics, ApplicationContextAware
{
    /** Read/Write locks by cache name */
    private final ConcurrentMap<String, ReentrantReadWriteLock> locks = new ConcurrentHashMap<>();
    private Map<String, Map<OpType, OperationStats>> cacheToStatsMap = new HashMap<>();
    private ApplicationContext applicationContext;
    
    
    @Override
    public long count(String cacheName, OpType opType)
    {
        ReadLock readLock = getReadLock(cacheName);
        readLock.lock();
        try
        {
            Map<OpType, OperationStats> cacheStats = cacheToStatsMap.get(cacheName);
            if (cacheStats == null)
            {
                throw new NoStatsForCache(cacheName);
            }
            OperationStats opStats = cacheStats.get(opType);
            return opStats.getCount();
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public double meanTime(String cacheName, OpType opType)
    {
        ReadLock readLock = getReadLock(cacheName);
        readLock.lock();
        try
        {
            Map<OpType, OperationStats> cacheStats = cacheToStatsMap.get(cacheName);
            if (cacheStats == null)
            {
                throw new NoStatsForCache(cacheName);
            }
            OperationStats opStats = cacheStats.get(opType);
            return opStats.meanTime();
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public void add(String cacheName, TransactionStats txStats)
    {
        boolean registerCacheStats = false;
        WriteLock writeLock = getWriteLock(cacheName);
        writeLock.lock();
        try
        {
            // Are we adding new stats for a previously unseen cache?
            registerCacheStats = !cacheToStatsMap.containsKey(cacheName);
            if (registerCacheStats)
            {
                // There are no statistics yet for this cache. 
                cacheToStatsMap.put(cacheName, new HashMap<OpType, OperationStats>());
            }
            Map<OpType, OperationStats> cacheStats = cacheToStatsMap.get(cacheName);
            
            for (OpType opType : OpType.values())
            {                
                SummaryStatistics txOpSummary = txStats.getTimings(opType);
                long count = txOpSummary.getN();
                double totalTime = txOpSummary.getSum();
                    
                OperationStats oldStats = cacheStats.get(opType);
                OperationStats newStats;
                if (oldStats == null)
                {
                    newStats = new OperationStats(totalTime, count);
                }
                else
                {
                    newStats = new OperationStats(oldStats, totalTime, count);
                }
                cacheStats.put(opType, newStats);
            }
        }
        finally
        {
            writeLock.unlock();
        }
        
        if (registerCacheStats)
        {
            // We've added stats for a previously unseen cache, raise an event
            // so that an MBean for the cache may be registered, for example. 
            applicationContext.publishEvent(new CacheStatisticsCreated(this, cacheName));
        }
    }
    
    @Override
    public double hitMissRatio(String cacheName)
    {
        ReadLock readLock = getReadLock(cacheName);
        readLock.lock();
        try
        {
            Map<OpType, OperationStats> cacheStats = cacheToStatsMap.get(cacheName);
            if (cacheStats == null)
            {
                throw new NoStatsForCache(cacheName);
            }
            long hits = cacheStats.get(OpType.GET_HIT).getCount();
            long misses = cacheStats.get(OpType.GET_MISS).getCount();
            return (double)hits / (hits+misses);
        }
        finally
        {
            readLock.unlock();
        }
    }
    
    @Override
    public long numGets(String cacheName)
    {
        ReadLock readLock = getReadLock(cacheName);
        readLock.lock();
        try
        {
            Map<OpType, OperationStats> cacheStats = cacheToStatsMap.get(cacheName);
            if (cacheStats == null)
            {
                throw new NoStatsForCache(cacheName);
            }
            long hits = cacheStats.get(OpType.GET_HIT).getCount();
            long misses = cacheStats.get(OpType.GET_MISS).getCount();
            return hits+misses;
        }
        finally
        {
            readLock.unlock();
        }
    }
    
    @Override
    public Map<OpType, OperationStats> allStats(String cacheName)
    {
        ReadLock readLock = getReadLock(cacheName);
        readLock.lock();
        try
        {
            Map<OpType, OperationStats> cacheStats = cacheToStatsMap.get(cacheName);
            if (cacheStats == null)
            {
                throw new NoStatsForCache(cacheName);
            }
            return new HashMap<>(cacheStats);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }
    
    
    /**
     * Gets a {@link ReentrantReadWriteLock} for a specific cache, lazily
     * creating the lock if necessary. Locks may be created per cache
     * (rather than hashing to a smaller pool) since the number of
     * caches is not too large.
     * 
     * @param cacheName  Cache name to obtain lock for.
     * @return ReentrantReadWriteLock
     */
    private ReentrantReadWriteLock getLock(String cacheName)
    {
        if (!locks.containsKey(cacheName))
        {
            ReentrantReadWriteLock newLock = new ReentrantReadWriteLock();
            if (locks.putIfAbsent(cacheName, newLock) == null)
            {
                // Lock was successfully added to map.
                return newLock;
            };
        }
        return locks.get(cacheName);
    }
    
    private ReadLock getReadLock(String cacheName)
    {
        ReadLock readLock = getLock(cacheName).readLock();
        return readLock;
    }
    
    private WriteLock getWriteLock(String cacheName)
    {
        WriteLock writeLock = getLock(cacheName).writeLock();
        return writeLock;
    }
}
