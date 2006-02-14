/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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

import org.alfresco.error.AlfrescoRuntimeException;
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
            if (logger.isDebugEnabled())
            {
                execute();
            }
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
        long totalSize = 0L;
        // get all the caches
        String[] cacheNames = cacheManager.getCacheNames();
        logger.debug("Dumping EHCache info:");
        for (String cacheName : cacheNames)
        {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null)  // perhaps a temporary cache
            {
                continue;
            }
            // dump
            CacheAnalysis analysis = new CacheAnalysis(cache);
            logger.debug(analysis);
            // get the size
            totalSize += analysis.getSize();
        }
        // check the size
        double sizePercentage = (double)totalSize / (double)maxHeapSize * 100.0;
        String msg = String.format(
                "EHCaches currently consume %5.2f MB or %3.2f percent of system VM size",
                (double)totalSize / 1024.0 / 1024.0,
                sizePercentage);
        logger.debug(msg);
    }
    
    private static class CacheAnalysis
    {
        private Cache cache;
        private long size = 0L;
        
        public CacheAnalysis(Cache cache) throws CacheException
        {
            this.cache = cache;
            calculateSize();
        }
        
        public synchronized long getSize()
        {
            return size;
        }
        
        @SuppressWarnings("unchecked")
        private synchronized void calculateSize() throws CacheException
        {
            // calculate the cache deep size - EHCache 1.1 is always returning 0L
            List<Serializable> keys = cache.getKeys();
            for (Serializable key : keys)
            {
                Element element = cache.get(key);
                size += getSize(element);
            }
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
        
        public String getStatusStr()
        {
            switch (cache.getStatus())
            {
                case Cache.STATUS_ALIVE:
                    return "ALIVE";
                case Cache.STATUS_DISPOSED:
                    return "DISPOSED";
                case Cache.STATUS_UNINITIALISED:
                    return "UNINITIALIZED";
                default:
                    throw new AlfrescoRuntimeException("Unknown cache status: " + cache.getStatus());
            }
        }
        
        public String toString()
        {
            double sizeMB = (double)getSize()/1024.0/1024.0;
            long maxSize = cache.getMaxElementsInMemory();
            long currentSize = cache.getMemoryStoreSize();
            double percentageFull = (double)currentSize / (double)maxSize * 100.0;
            double estMaxSize = sizeMB / (double) currentSize * (double) maxSize;
            
            StringBuilder sb = new StringBuilder(512);
            sb.append("   Analyzing EHCache: \n")
              .append("===>  ").append(cache).append("\n")
              .append("      Deep Size:              ").append(String.format("%5.2f MB", sizeMB)).append("\n")
              .append("      Percentage used:        ").append(String.format("%5.2f percent", percentageFull)).append("\n")
              .append("      Estimated maximum size: ").append(String.format("%5.2f MB", estMaxSize));
            return sb.toString();
        }
    }
}
