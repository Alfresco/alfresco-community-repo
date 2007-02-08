/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.cache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Analyzes the size of EHCache caches used.
 * <p>
 * To activate this class, call the {@link #init()} method. 
 * 
 * @author Derek Hulley
 */
public class EhCacheTracerJob implements Job
{
    private static Log logger = LogFactory.getLog(EhCacheTracerJob.class);
    
    private CacheManager cacheManager;

    /**
     * Set the cache manager to analyze.  The default cache manager will be analyzed
     * if this property is not set.
     * 
     * @param cacheManager optional cache manager to analyze
     */
    public void setCacheManager(CacheManager cacheManager)
    {
        this.cacheManager = cacheManager;
    }

    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        try
        {
            execute();
        }
        catch (Throwable e)
        {
            logger.error("Exception during execution of job", e);
        }
    }
    
    private void execute() throws Exception
    {
        if (cacheManager == null)
        {
            cacheManager = CacheManager.getInstance();
        }
        
        long maxHeapSize = Runtime.getRuntime().maxMemory();
        long allCachesTotalSize = 0L;
        double estimatedMaxSize = 0L;
        // get all the caches
        String[] cacheNames = cacheManager.getCacheNames();
        logger.debug("Dumping EHCache info:");
        boolean analyzeAll = true;
        for (String cacheName : cacheNames)
        {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null)  // perhaps a temporary cache
            {
                continue;
            }
            Log cacheLogger = LogFactory.getLog(this.getClass().getName() + "." + cacheName);
            // log each cache to its own logger
            // dump
            if (cacheLogger.isDebugEnabled())
            {
                CacheAnalysis analysis = new CacheAnalysis(cache);
                cacheLogger.debug(analysis);
                // get the size
                allCachesTotalSize += analysis.getSize();
                double cacheEstimatedMaxSize = analysis.getEstimatedMaxSize();
                estimatedMaxSize += (Double.isNaN(cacheEstimatedMaxSize) || Double.isInfinite(cacheEstimatedMaxSize))
                                    ? 0.0
                                    : cacheEstimatedMaxSize;
            }
            else
            {
                analyzeAll = false;
            }
        }
        if (analyzeAll)
        {
            // check the size
            double sizePercentage = (double)allCachesTotalSize / (double)maxHeapSize * 100.0;
            double maxSizePercentage = estimatedMaxSize / (double)maxHeapSize * 100.0;
            String msg = String.format(
                    "EHCaches currently consume %5.2f MB or %3.2f percent of system VM size. \n" +
                    "The estimated maximum size is %5.2f MB or %3.2f percent of system VM size.",
                    (double)allCachesTotalSize / 1024.0 / 1024.0,
                    sizePercentage,
                    estimatedMaxSize / 1024.0 / 1024.0,
                    maxSizePercentage);
            logger.debug(msg);
        }
    }
    
    private static class CacheAnalysis
    {
        private Cache cache;
        private long size = 0L;
        double sizeMB;
        long maxSize;
        long currentSize;
        long hitCount;
        long missCount;
        double percentageFull;
        double estMaxSize;
        
        public CacheAnalysis(Cache cache) throws CacheException
        {
            this.cache = cache;
            if (this.cache.getStatus().equals(Status.STATUS_ALIVE))
            {
                try
                {
                    calculateSize();
                }
                catch (Throwable e)
                {
                    // just ignore
                }
            }
        }
        
        public synchronized long getSize()
        {
            return size;
        }
        
        public synchronized double getEstimatedMaxSize()
        {
            return estMaxSize;
        }
        
        @SuppressWarnings("unchecked")
        private synchronized void calculateSize() throws CacheException
        {
            // calculate the cache deep size - EHCache 1.1 is always returning 0L
            List<Serializable> keys = cache.getKeys();
            // only count a maximum of 1000 entities
            int count = 0;
            for (Serializable key : keys)
            {
                Element element = cache.get(key);
                size += getSize(element);
                count++;
                if (count >= 50)
                {
                    break;
                }
            }
            
            // the size must be multiplied by the ratio of the count to actual size
            size = count > 0 ? (long) ((double)size * ((double)keys.size()/(double)count)) : 0L;
            
            sizeMB = (double)size/1024.0/1024.0;
            maxSize = cache.getMaxElementsInMemory();
            currentSize = cache.getMemoryStoreSize();
            hitCount = cache.getHitCount();
            missCount = cache.getMissCountNotFound();
            percentageFull = (double)currentSize / (double)maxSize * 100.0;
            estMaxSize = size / (double) currentSize * (double) maxSize;
        }
        
        private long getSize(Serializable obj)
        {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
            ObjectOutputStream oos = null;
            try
            {
                oos = new ObjectOutputStream(bout);
                oos.writeObject(obj);
                return bout.size();
            }
            catch (IOException e)
            {
                logger.warn("Deep size calculation failed for cache: \n" + cache);
                return 0L;
            }
            finally
            {
                try { oos.close(); } catch (IOException e) {}
            }
        }
        
        public String toString()
        {
            double sizeMB = (double)getSize()/1024.0/1024.0;
            long maxSize = cache.getMaxElementsInMemory();
            long currentSize = cache.getMemoryStoreSize();
            long hitCount = cache.getHitCount();
            long totalMissCount = cache.getMissCountNotFound() + cache.getMissCountExpired();
            double hitRatio = (double)hitCount / (double)(totalMissCount + hitCount) * 100.0;
            double percentageFull = (double)currentSize / (double)maxSize * 100.0;
            double estMaxSize = sizeMB / (double) currentSize * (double) maxSize;
            
            StringBuilder sb = new StringBuilder(512);
            sb.append("\n")
              .append("===>  EHCache: ").append(cache).append("\n")
              .append("      Hit Ratio:              ").append(String.format("%10.2f percent  ", hitRatio      ))
              .append("   |         Hit Count:     ").append(String.format("%10d hits     ", hitCount     ))
              .append("   |         Miss Count:    ").append(String.format("%10d misses   ", totalMissCount     )).append("\n")
              .append("      Deep Size:              ").append(String.format("%10.2f MB     ", sizeMB        ))
              .append("     |         Current Count: ").append(String.format("%10d entries  ", currentSize   )).append("\n")
              .append("      Percentage used:        ").append(String.format("%10.2f percent", percentageFull))
              .append("     |         Max Count:     ").append(String.format("%10d entries  ", maxSize       )).append("\n")
              .append("      Estimated maximum size: ").append(String.format("%10.2f MB     ", estMaxSize    ));
            return sb.toString();
        }
    }
}
