/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.repo.content.caching.quota;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import org.alfresco.repo.content.caching.ContentCacheImpl;
import org.alfresco.repo.content.caching.cleanup.CachedContentCleaner;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private static final String CACHE_USAGE_FILENAME = "cache-usage.txt";
    private final static Log log = LogFactory.getLog(StandardQuotaStrategy.class);
    private static final long DEFAULT_DISK_USAGE_ESTIMATE = 0L;
    private int panicThresholdPct = 90;
    private int cleanThresholdPct = 80;
    private int targetUsagePct = 70;
    private long maxUsageBytes = 0;
    /* Threshold in seconds indicating a minimal gap between normal cleanup starts */
    private long normalCleanThresholdSec = 0;
    private AtomicLong currentUsageBytes = new AtomicLong(0);
    private AtomicLong lastCleanupStart = new AtomicLong(0);
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
        // Set the time to start the normal clean
        lastCleanupStart.set(System.currentTimeMillis() - normalCleanThresholdSec);
        // Run the cleaner thread so that it can update the disk usage more accurately.
        signalCleanerStart("quota (init)");
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
        File usageFile = new File(cache.getCacheRoot(), CACHE_USAGE_FILENAME);
        
        if (!usageFile.exists())
        {
            setCurrentUsageBytes(DEFAULT_DISK_USAGE_ESTIMATE);
            
            if (log.isInfoEnabled())
            {
                log.info("No previous usage file found (" + usageFile + ") so assuming: " +
                            getCurrentUsageBytes() + " bytes.");
            }
        }
        else
        {
            FileContentReader reader = new FileContentReader(usageFile); 
            String usageStr = reader.getContentString();
            long usage = Long.parseLong(usageStr);
            currentUsageBytes.set(usage);
            if (log.isInfoEnabled())
            {
                log.info("Using last known disk usage estimate: " + getCurrentUsageBytes());
            }
        }    
    }


    private void saveDiskUsage()
    {
        File usageFile = new File(cache.getCacheRoot(), CACHE_USAGE_FILENAME);
        FileContentWriter writer = new FileContentWriter(usageFile);
        writer.putContent(currentUsageBytes.toString());
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
            signalCleanerStart("quota (panic threshold)");
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
            signalAggressiveCleanerStart("quota (limit reached)");
        }
        else if (usageHasReached(cleanThresholdPct))
        {
            // If usage has reached the clean threshold, start the cleaner
            if (log.isDebugEnabled())
            {
                log.debug("Usage has reached " + cleanThresholdPct + "% - starting cached content cleaner.");
            }
            
            signalCleanerStart("quota (clean threshold)");
        }
        
        return keepNewFile;
    }


    /**
     * Run the cleaner in a new thread.
     */
    private void signalCleanerStart(final String reason, final boolean aggressive)
    {
        if (aggressive)
        {
            long targetReductionBytes = (long) (((double) targetUsagePct / 100) * maxUsageBytes);
            cleaner.executeAggressive(reason, targetReductionBytes);                    
        }
        else
        {
            long timePassedFromLastClean = System.currentTimeMillis() - lastCleanupStart.get();
            if (timePassedFromLastClean < normalCleanThresholdSec * 1000)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Skipping a normal clean as it is too soon. The last cleanup was run " + timePassedFromLastClean/1000f + " seconds ago.");
                }
            }
            else
            {
                lastCleanupStart.set(System.currentTimeMillis());
                cleaner.execute(reason);
            }
        }
    }
    
    /**
     * Run a non-aggressive clean up job in a new thread.
     * 
     * @param reason String
     */
    private void signalCleanerStart(final String reason)
    {
        signalCleanerStart(reason, false);
    }
    
    /**
     * Run an aggressive clean up job in a new thread.
     * 
     * @param reason String
     */
    private void signalAggressiveCleanerStart(final String reason)
    {
       signalCleanerStart(reason, true); 
    }

    
    /**
     * Will an increase in disk usage of <code>contentSize</code> bytes result in the specified
     * <code>threshold</code> (percentage of maximum allowed usage) being reached or exceeded?
     * 
     * @param threshold int
     * @param contentSize long
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

    public void setTargetUsagePct(int targetUsagePct)
    {
        this.targetUsagePct = targetUsagePct;
    }

    public void setNormalCleanThresholdSec(long normalCleanThresholdSec)
    {
        this.normalCleanThresholdSec = normalCleanThresholdSec;
    }

    public void setCache(ContentCacheImpl cache)
    {
        this.cache = cache;
    }

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
