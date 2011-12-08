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
package org.alfresco.repo.content.caching.cleanup;

import java.io.File;
import java.util.Date;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.repo.content.caching.CacheFileProps;
import org.alfresco.repo.content.caching.ContentCacheImpl;
import org.alfresco.repo.content.caching.FileHandler;
import org.alfresco.repo.content.caching.quota.UsageTracker;
import org.alfresco.util.Deleter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

/**
 * Cleans up redundant cache files from the cached content file store. Once references to cache files are
 * no longer in the in-memory cache, the binary content files can be removed.
 * 
 * @author Matt Ward
 */
public class CachedContentCleaner extends Thread implements FileHandler, ApplicationEventPublisherAware
{
    private static final Log log = LogFactory.getLog(CachedContentCleaner.class);
    private ContentCacheImpl cache;   // impl specific functionality required
    private long minFileAgeMillis = 0;
    private Integer maxDeleteWatchCount = 1;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private boolean running;
    private UsageTracker usageTracker;
    private long newDiskUsage;
    private long numFilesSeen;
    private long numFilesDeleted;
    private long sizeFilesDeleted;
    private long numFilesMarked;
    private Date timeStarted;
    private Date timeFinished;
    private ApplicationEventPublisher eventPublisher;
    private long targetReductionBytes;
    private boolean cleanRequested;
    private String reasonMessage;
   
    
    public CachedContentCleaner()
    {        
        setName(getClass().getSimpleName());
        setDaemon(true);
    }
    
    /**
     * This method MUST be called after the cleaner has been fully constructed
     * to notify interested parties that the cleaner exists and to start the actual cleaner thread.
     */
    public void init()
    {
        eventPublisher.publishEvent(new CachedContentCleanerCreatedEvent(this));    
        start();
    }
    
    
    @Override
    public void run()
    {
        while (true)
        {
            doClean();
        }
    }

    public synchronized void execute()
    {
        execute("none specified");
    }
    
    public synchronized void executeAggressive(String reason, long targetReductionBytes)
    {
        this.targetReductionBytes = targetReductionBytes;
        execute(reason);
    }
    
    private synchronized void doClean()
    {
        while (running || (!cleanRequested))
        {
            try
            {
                wait();
            }
            catch (InterruptedException error)
            {
                // Nothing to do.
            }
        }
        
        running = true;
        if (log.isInfoEnabled())
        {
            log.info("Starting cleaner, reason: " + reasonMessage);
        }
        resetStats();
        timeStarted = new Date();
        cache.processFiles(this);
        timeFinished = new Date(); 
        
        if (usageTracker != null)
        {
            usageTracker.setCurrentUsageBytes(newDiskUsage);
        }
        
        if (log.isInfoEnabled())
        {
            log.info("Finished, duration: " + getDurationSeconds() + "s, seen: " + numFilesSeen +
                        ", marked: " + numFilesMarked +
                        ", deleted: " + numFilesDeleted +
                        " (" + String.format("%.2f", getSizeFilesDeletedMB()) + "MB, " +
                        sizeFilesDeleted + " bytes)" +
                        ", target: " + targetReductionBytes + " bytes");
        }
        
        cleanRequested = false;
        this.targetReductionBytes = 0;
        running = false;
        
        notifyAll();
    }
    
    
    public synchronized void execute(String reasonMessage)
    {
        this.reasonMessage = reasonMessage;
        cleanRequested = true;
        notifyAll();
    }
    
    
    private void resetStats()
    {
        newDiskUsage = 0;
        numFilesSeen = 0;
        numFilesDeleted = 0;
        sizeFilesDeleted = 0;
        numFilesMarked = 0;
    }


    @Override
    public void handle(File cachedContentFile)
    {
        if (log.isDebugEnabled())
        {
            log.debug("handle file: " + cachedContentFile + " (target reduction: " + targetReductionBytes + " bytes)");
        }
        numFilesSeen++;
        CacheFileProps props = null;
        boolean deleted = false;
        
        if (targetReductionBytes > 0 && sizeFilesDeleted < targetReductionBytes)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Target reduction " + targetReductionBytes +
                           " bytes not yet reached. Deleted so far: " + sizeFilesDeleted);
            }
            // Aggressive clean mode, delete file straight away.
            deleted = deleteFilesNow(cachedContentFile);
        }
        else
        {
            if (oldEnoughForCleanup(cachedContentFile))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("File is older than " + minFileAgeMillis + 
                                "ms - considering for cleanup: " + cachedContentFile);
                }
                props = new CacheFileProps(cachedContentFile);        
                String url = cache.getContentUrl(cachedContentFile);
                if (url == null)
                {
                    // Not in the cache, check the properties file 
                    props.load();
                    url = props.getContentUrl();
                }   
                
                if (url == null || !cache.contains(url))
                {
                    // If the url is null, it might still be in the cache, but we were unable to determine it
                    // from the reverse lookup or the properties file. Delete the file as it is most likely orphaned.
                    // If for some reason it is still in the cache, cache.getReader(url) must re-cache it.
                    deleted = markOrDelete(cachedContentFile, props);
                }
            }
            else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("File too young for cleanup - ignoring " + cachedContentFile);
                }
            }
        }
        
        if (!deleted)
        {
            if (props == null)
            {
                props = new CacheFileProps(cachedContentFile);
            }
            long size = cachedContentFile.length() + props.fileSize();
            newDiskUsage += size;
        }
    }

    

    /**
     * Is the file old enough to be considered for cleanup/deletion? The file must be older than minFileAgeMillis
     * to be considered for deletion - the state of the cache and the file's associated properties file will not
     * be examined unless the file is old enough.
     *  
     * @return true if the file is older than minFileAgeMillis, false otherwise.
     */
    private boolean oldEnoughForCleanup(File file)
    {
        if (minFileAgeMillis == 0)
        {
            return true;
        }
        else
        {
            long now = System.currentTimeMillis();
            return (file.lastModified() < (now - minFileAgeMillis));
        }
    }


    /**
     * Marks a file for deletion by a future run of the CachedContentCleaner. Each time a file is observed
     * by the cleaner as being ready for deletion, the deleteWatchCount is incremented until it reaches
     * maxDeleteWatchCount - in which case the next run of cleaner will really delete it.
     * <p>
     * For maxDeleteWatchCount of 1 for example, the first cleaner run will mark the file for deletion and the second
     * run will really delete it.
     * <p>
     * This offers a degree of protection over the fairly unlikely event that a reader will be obtained for a file that
     * is in the cache but gets removed from the cache and is then deleted by the cleaner before
     * the reader was consumed. A maxDeleteWatchCount of 1 should normally be fine (recommended), whilst
     * 0 would result in immediate deletion the first time the cleaner sees it as a candidate
     * for deletion (not recommended).
     * 
     * @param file
     * @param props
     * @return true if the content file was deleted, false otherwise.
     */
    private boolean markOrDelete(File file, CacheFileProps props)
    {
        Integer deleteWatchCount = props.getDeleteWatchCount();

        // Just in case the value has been corrupted somehow.
        if (deleteWatchCount < 0)
            deleteWatchCount = 0;
        
        boolean deleted = false;
        
        if (deleteWatchCount < maxDeleteWatchCount)
        {
            deleteWatchCount++;
            
            if (log.isDebugEnabled())
            {
                log.debug("Marking file for deletion, deleteWatchCount=" + deleteWatchCount + ", file: "+ file);
            }
            props.setDeleteWatchCount(deleteWatchCount);
            props.store();
            numFilesMarked++;
        }
        else
        {
            if (log.isDebugEnabled())
            {
                log.debug("Deleting cache file " + file);
            }
            deleted = deleteFilesNow(file);
        }
        
        return deleted;
    }

    /**
     * Deletes both the cached content file and its peer properties file that contains the
     * original content URL and deletion marker information.
     *  
     * @param cacheFile Location of cached content file.
     * @return true if the content file was deleted, false otherwise.
     */
    private boolean deleteFilesNow(File cacheFile)
    {
        CacheFileProps props = new CacheFileProps(cacheFile);
        props.delete();
        long fileSize = cacheFile.length();
        boolean deleted = cacheFile.delete();
        if (deleted)
        {
            if (log.isTraceEnabled())
            {
                log.trace("Deleted cache file: " + cacheFile);
            }
            numFilesDeleted++;
            sizeFilesDeleted += fileSize;
            Deleter.deleteEmptyParents(cacheFile, cache.getCacheRoot());
        }
        else
        {
            if (log.isWarnEnabled())
            {
                log.warn("Failed to delete cache file: " + cacheFile);
            }
        }
        
        return deleted;
    }

    

    @Required
    public void setCache(ContentCacheImpl cache)
    {
        this.cache = cache;
    }

    
    /**
     * Sets the minimum age of a cache file before it will be considered for deletion.
     * @see #oldEnoughForCleanup(File)
     * @param minFileAgeMillis
     */
    public void setMinFileAgeMillis(long minFileAgeMillis)
    {
        this.minFileAgeMillis = minFileAgeMillis;
    }


    /**
     * Sets the maxDeleteWatchCount value.
     * 
     * @see #markOrDelete(File, CacheFileProps)
     * @param maxDeleteWatchCount
     */
    public void setMaxDeleteWatchCount(Integer maxDeleteWatchCount)
    {
        if (maxDeleteWatchCount < 0)
        {
            throw new IllegalArgumentException("maxDeleteWatchCount cannot be negative [value=" + maxDeleteWatchCount + "]");
        }
        this.maxDeleteWatchCount = maxDeleteWatchCount;
    }


    /**
     * @param usageTracker the usageTracker to set
     */
    public void setUsageTracker(UsageTracker usageTracker)
    {
        this.usageTracker = usageTracker;
    }

    public boolean isRunning()
    {
        lock.readLock().lock();
        try
        {
            return running;
        }
        finally
        {
            lock.readLock().unlock();
        }
    }
    
    public long getNumFilesSeen()
    {
        return this.numFilesSeen;
    }
    
    public long getNumFilesDeleted()
    {
        return this.numFilesDeleted;
    }

    public long getSizeFilesDeleted()
    {
        return this.sizeFilesDeleted;
    }
        
    public double getSizeFilesDeletedMB()
    {
        return (double) getSizeFilesDeleted() / FileUtils.ONE_MB;
    }

    public long getNumFilesMarked()
    {
        return numFilesMarked;
    }
    
    public Date getTimeStarted()
    {
        return this.timeStarted;
    }

    public Date getTimeFinished()
    {
        return this.timeFinished;
    }
    
    public long getDurationSeconds()
    {
        return getDurationMillis() / 1000;
    }
    
    public long getDurationMillis()
    {
        return timeFinished.getTime() - timeStarted.getTime();
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Returns the cacheRoot that this cleaner is responsible for.
     * @return File
     */
    public File getCacheRoot()
    {
        return cache.getCacheRoot();
    }
}
