/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.content.caching.quota;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicLong;

import org.alfresco.repo.content.caching.ContentCacheImpl;
import org.alfresco.repo.content.caching.cleanup.CachedContentCleaner;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Quota manager for the CachingContentStore that has the following characteristics:
 * <p>
 * When a cache file has been written that results in cleanThresholdPct (default 80%) of maxUsageBytes
 * being exceeded then the cached content cleaner is invoked (if not already running) in a new thread.
 * <p>
 * When the CachingContentStore is about to write a cache file but the disk usage is in excess of panicThresholdPct
 * (default 90%) then the cache file is not written and the cleaner is started (if not already running) in a new thread.
 * <p>
 * This quota manager works in conjunction with the cleaner to update disk usage levels in memory. When the quota
 * manager shuts down the current disk usage is saved to disk in {ContentCacheImpl.cacheRoot}/cache-usage.ser
 * <p>
 * Upon startup, if the cache-usage.ser file exists then the current usage is seeded with that value and the cleaner
 * is invoked in a new thread so that the value can be updated more accurately (perhaps some files were deleted
 * manually after shutdown for example).
 * 
 * @author Matt Ward
 */
public class StandardQuotaStrategy implements QuotaManagerStrategy, UsageTracker
{
    private static final String CACHE_USAGE_FILENAME = "cache-usage.ser";
    private final static Log log = LogFactory.getLog(StandardQuotaStrategy.class);
    private static final long DEFAULT_DISK_USAGE_ESTIMATE = 0L;
    private int panicThresholdPct = 90;
    private int cleanThresholdPct = 80;
    private int targetUsagePct = 70;
    private long maxUsageBytes = 0;
    private AtomicLong currentUsageBytes = new AtomicLong(0);
    private CachedContentCleaner cleaner;
    private ContentCacheImpl cache;   // impl specific functionality required
    private int maxFileSizeMB = 0;
    
    /**
     * Lifecycle method. Should be called immediately after constructing objects of this type (e.g. by the
     * Spring framework's application context).
     */
    public void init()
    {        
        if (log.isDebugEnabled())
        {
            log.debug("Starting quota strategy.");
        }
        PropertyCheck.mandatory(this, "cleaner", cleaner);
        PropertyCheck.mandatory(this, "cache", cache);
        
        if (maxUsageBytes < (10 * FileUtils.ONE_MB))
        {
            if (log.isWarnEnabled())
            {
                log.warn("Low maxUsageBytes of " + maxUsageBytes + "bytes - did you mean to specify in MB?");
            }
        }
        
        loadDiskUsage();
        // Run the cleaner thread so that it can update the disk usage more accurately.
        runCleanerThread("quota (init)");
    }
    
    
    /**
     * Lifecycle method. Should be called when finished using an object of this type and before the application
     * container is shutdown (e.g. using a Spring framework destroy method).
     */
    public void shutdown()
    {
        if (log.isDebugEnabled())
        {
            log.debug("Shutting down quota strategy.");
        }
        saveDiskUsage();
    }
    
    
    private void loadDiskUsage()
    {
        // Load the last known disk usage value.
        try
        {
            FileInputStream fis = new FileInputStream(new File(cache.getCacheRoot(), CACHE_USAGE_FILENAME));
            ObjectInputStream ois = new ObjectInputStream(fis);
            currentUsageBytes.set(ois.readLong());
            ois.close();
            if (log.isInfoEnabled())
            {
                log.info("Using last known disk usage estimate: " + getCurrentUsageBytes());
            }
        }
        catch (Throwable e)
        {
            // Assume disk usage
            setCurrentUsageBytes(DEFAULT_DISK_USAGE_ESTIMATE);
            
            if (log.isInfoEnabled())
            {
                log.info("Unable to load last known disk usage estimate so assuming: " + getCurrentUsageBytes());
            }
        }        
    }


    private void saveDiskUsage()
    {
        // Persist the last known disk usage value.
        try
        {
            FileOutputStream fos = new FileOutputStream(new File(cache.getCacheRoot(), CACHE_USAGE_FILENAME));
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(currentUsageBytes);
            out.close();
        }
        catch (Throwable e)
        {
            throw new RuntimeException("Unable to save content cache disk usage statistics.", e);
        }
    }
    
    
    @Override
    public boolean beforeWritingCacheFile(long contentSizeBytes)
    {
        long maxFileSizeBytes = getMaxFileSizeBytes();
        if (maxFileSizeBytes > 0 && contentSizeBytes > maxFileSizeBytes)
        {
            if (log.isDebugEnabled())
            {
                log.debug("File too large (" + contentSizeBytes + " bytes, max allowed is " + 
                            getMaxFileSizeBytes() + ") - vetoing disk write.");
            }
            return false;
        }
        else if (usageWillReach(panicThresholdPct, contentSizeBytes))
        {
            if (log.isDebugEnabled())
            {
                log.debug("Panic threshold reached (" + panicThresholdPct +
                            "%) - vetoing disk write and starting cached content cleaner.");
            }
            runCleanerThread("quota (panic threshold)");
            return false;
        }
        
        return true;
    }


    @Override
    public boolean afterWritingCacheFile(long contentSizeBytes)
    {
        boolean keepNewFile = true;
        
        long maxFileSizeBytes = getMaxFileSizeBytes();
        if (maxFileSizeBytes > 0 && contentSizeBytes > maxFileSizeBytes)
        {
            keepNewFile = false;
        }
        else
        {
            // The file has just been written so update the usage stats.
            addUsageBytes(contentSizeBytes);
        }
        
        if (getCurrentUsageBytes() >= maxUsageBytes)
        {
            // Reached quota limit - time to aggressively recover some space to make sure that
            // new requests to cache a file are likely to be honoured.
            if (log.isDebugEnabled())
            {
                log.debug("Usage has reached or exceeded quota limit, limit: " + maxUsageBytes +
                            " bytes, current usage: " + getCurrentUsageBytes() + " bytes.");
            }
            runAggressiveCleanerThread("quota (limit reached)");
        }
        else if (usageHasReached(cleanThresholdPct))
        {
            // If usage has reached the clean threshold, start the cleaner
            if (log.isDebugEnabled())
            {
                log.debug("Usage has reached " + cleanThresholdPct + "% - starting cached content cleaner.");
            }
            
            runCleanerThread("quota (clean threshold)");
        }
        
        return keepNewFile;
    }


    /**
     * Run the cleaner in a new thread.
     */
    private void runCleanerThread(final String reason, final boolean aggressive)
    {
        Runnable cleanerRunner = new Runnable()
        {    
            @Override
            public void run()
            {
                if (aggressive)
                {
                    long targetReductionBytes = (long) (((double) targetUsagePct / 100) * maxUsageBytes);
                    cleaner.executeAggressive(reason, targetReductionBytes);                    
                }
                else
                {
                    cleaner.execute(reason);
                }
            }
        };
        Thread cleanerThread = new Thread(cleanerRunner, getClass().getSimpleName() + " cleaner");
        cleanerThread.run();
    }
    
    /**
     * Run a non-aggressive clean up job in a new thread.
     * 
     * @param reason
     */
    private void runCleanerThread(final String reason)
    {
        runCleanerThread(reason, false);
    }
    
    /**
     * Run an aggressive clean up job in a new thread.
     * 
     * @param reason
     */
    private void runAggressiveCleanerThread(final String reason)
    {
       runCleanerThread(reason, true); 
    }

    
    /**
     * Will an increase in disk usage of <code>contentSize</code> bytes result in the specified
     * <code>threshold</code> (percentage of maximum allowed usage) being reached or exceeded?
     * 
     * @param threshold
     * @param contentSize
     * @return true if additional content will reach <code>threshold</code>.
     */
    private boolean usageWillReach(int threshold, long contentSize)
    {
        long potentialUsage = getCurrentUsageBytes() + contentSize;
        double pctOfMaxAllowed = ((double) potentialUsage / maxUsageBytes) * 100;        
        return pctOfMaxAllowed >= threshold;
    }
    
    private boolean usageHasReached(int threshold)
    {
        return usageWillReach(threshold, 0);
    }
    
    
    public void setMaxUsageMB(long maxUsageMB)
    {
        setMaxUsageBytes(maxUsageMB * FileUtils.ONE_MB);
    }
    
    public void setMaxUsageBytes(long maxUsageBytes)
    {
        this.maxUsageBytes = maxUsageBytes;
    }
    

    public void setPanicThresholdPct(int panicThresholdPct)
    {
        this.panicThresholdPct = panicThresholdPct;
    }


    public void setCleanThresholdPct(int cleanThresholdPct)
    {
        this.cleanThresholdPct = cleanThresholdPct;
    }


    @Required
    public void setCache(ContentCacheImpl cache)
    {
        this.cache = cache;
    }


    @Required
    public void setCleaner(CachedContentCleaner cleaner)
    {
        this.cleaner = cleaner;
    }


    @Override
    public long getCurrentUsageBytes()
    {
        return currentUsageBytes.get();
    }
    
    
    public double getCurrentUsageMB()
    {
        return (double) getCurrentUsageBytes() / FileUtils.ONE_MB;
    }

    public long getMaxUsageBytes()
    {
        return maxUsageBytes;
    }
    
    public long getMaxUsageMB()
    {
        return maxUsageBytes / FileUtils.ONE_MB;
    }
    
    public int getMaxFileSizeMB()
    {
        return this.maxFileSizeMB;
    }

    protected long getMaxFileSizeBytes()
    {
        return maxFileSizeMB * FileUtils.ONE_MB;
    }
    
    public void setMaxFileSizeMB(int maxFileSizeMB)
    {
        this.maxFileSizeMB = maxFileSizeMB;
    }


    @Override
    public long addUsageBytes(long sizeDelta)
    {
        long newUsage = currentUsageBytes.addAndGet(sizeDelta);
        if (log.isDebugEnabled())
        {
            log.debug(String.format("Disk usage changed by %d to %d bytes", sizeDelta, newUsage));
        }
        return newUsage;
    }


    @Override
    public void setCurrentUsageBytes(long newDiskUsage)
    {
        if (log.isInfoEnabled())
        {
            log.info(String.format("Setting disk usage to %d bytes", newDiskUsage));
        }
        currentUsageBytes.set(newDiskUsage);
    }
}
