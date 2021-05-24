/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.repo.content.cleanup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.repo.domain.contentdata.ContentDataDAO.ContentUrlHandler;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.JobLockService.JobLockRefreshCallback;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.VmShutdownListener;
import org.alfresco.util.VmShutdownListener.VmShutdownException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This component is responsible cleaning up orphaned content.
 * 
 * Clean-up happens at two levels.<p/>
 * <u><b>Eager cleanup:</b></u> (since 3.2)<p/>
 * If  eager cleanup is activated, then this
 * component listens to all content property change events and recorded for post-transaction
 * processing.  All orphaned content is deleted from the registered store(s).  Note that
 * any listeners are called as normal; backup or scrubbing
 * procedures should be plugged in as listeners if this is required.
 * <p/>
 * <u><b>Lazy cleanup:</b></u><p/>
 * This is triggered by means of a {@link ContentStoreCleanupJob Quartz job}.  This process
 * gets content URLs that have been marked as orphaned and cleans up the various stores.
 * Once again, the listeners are called appropriately.
 * <p/>
 * <u><b>How backup policies are affected:</b></u><p/>
 * When restoring the system from a backup, the type of restore required is dictated by
 * the cleanup policy being enforced.  If eager cleanup is active, the system must<br/>
 * (a) have a listeners configured to backup the deleted content
 *     e.g. {@link DeletedContentBackupCleanerListener}, or <br/>
 * (b) ensure consistent backups across the database and content stores: backup
 *     when the system is not running; use a DB-based content store.  This is the
 *     recommended route when running with eager cleanup.
 * <p/>
 * Lazy cleanup protects the content for a given period (e.g. 7 days) giving plenty of
 * time for a backup to be taken; this allows hot backup without needing metadata-content
 * consistency to be enforced.
 * 
 * @author Derek Hulley
 */
public class ContentStoreCleaner
{
    /*
     * TODO: Use the ScheduledJobLockExecuter, which borrows (and fixes) some of the code use here
     */
    
    /**
     * Enumeration of actions to take in the even that an orphaned binary fails to get deleted.
     * Most stores are able to delete orphaned content, but it is possible that stores have
     * protection against binary deletion that is outside of the Alfresco server's control.
     * 
     * @author Derek Hulley
     * @since 3.3.5
     */
    public enum DeleteFailureAction
    {
        /**
         * Failure to clean up a binary is logged, but the URL is discarded for good i.e.
         * there will be no further attempt to clean up the binary or any remaining record
         * of its existence.
         */
        IGNORE,
        /**
         * Failure to clean up the binary is logged and then a URL record is created with a
         * orphan time of 0; there will be no further attempts to delete the URL binary, but
         * the record will also not be destroyed.
         */
        KEEP_URL;
    }
    
    private static final QName LOCK_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ContentStoreCleaner"); 
    private static final long LOCK_TTL = 30000L;
    
    private static Log logger = LogFactory.getLog(ContentStoreCleaner.class);
    
    /** kept to notify the thread that it should quit */
    private static VmShutdownListener vmShutdownListener = new VmShutdownListener("ContentStoreCleaner");
    
    private EagerContentStoreCleaner eagerContentStoreCleaner;
    private JobLockService jobLockService;
    private ContentDataDAO contentDataDAO;
    private DictionaryService dictionaryService;
    private ContentService contentService;
    private TransactionService transactionService;
    private int protectDays;
    private int batchSize;
    private DeleteFailureAction deletionFailureAction;
    
    public ContentStoreCleaner()
    {
        this.batchSize = 1000;
        this.protectDays = 7;
        this.deletionFailureAction = DeleteFailureAction.IGNORE;
    }

    /**
     * Set the component that will do the physical deleting
     */
    public void setEagerContentStoreCleaner(EagerContentStoreCleaner eagerContentStoreCleaner)
    {
        this.eagerContentStoreCleaner = eagerContentStoreCleaner;
    }

    /**
     * @param jobLockService        service used to ensure that cleanup runs are not duplicated
     */
    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    /**
     * @param contentDataDAO        DAO used for enumerating DM content URLs
     */
    public void setContentDataDAO(ContentDataDAO contentDataDAO)
    {
        this.contentDataDAO = contentDataDAO;
    }

    /**
     * @param dictionaryService used to determine which properties are content properties
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param contentService    service to copy content binaries
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * @param transactionService the component to ensure proper transactional wrapping
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Set the minimum number of days old that orphaned content must be
     *      before deletion is possible.  The default is 7 days.
     * 
     * @param protectDays minimum age (in days) of deleted content
     */
    public void setProtectDays(int protectDays)
    {
        this.protectDays = protectDays;
    }

    /**
     * Set the batch size used by the cleaning jobs. The default is 1000
     * 
     * @param batchSize     batch size for each cleanup job
     */
    public void setBatchSize(int batchSize)
    {
        this.batchSize = batchSize;
    }

    /**
     * Set the action to take in the event that an orphaned binary failed to get deleted.
     * The default is {@link DeleteFailureAction#IGNORE}.
     * 
     * @param deletionFailureAction     the action to take when deletes fail
     */
    public void setDeletionFailureAction(DeleteFailureAction deletionFailureAction)
    {
        this.deletionFailureAction = deletionFailureAction;
    }

    /**
     * Initializes the cleaner.
     */
    public void init()
    {
        checkProperties();
    }
    
    /**
     * Perform basic checks to ensure that the necessary dependencies were injected.
     */
    private void checkProperties()
    {
        PropertyCheck.mandatory(this, "jobLockService", jobLockService);
        PropertyCheck.mandatory(this, "contentDataDAO", contentDataDAO);
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "contentService", contentService);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "eagerContentStoreCleaner", eagerContentStoreCleaner);
        
        // check the protect days
        if (protectDays < 0)
        {
            throw new AlfrescoRuntimeException("Property 'protectDays' must be 0 or greater (0 is not recommended)");
        }
        else if (protectDays == 0)
        {
            logger.warn(
                    "Property 'protectDays' is set to 0.  " +
                    "Please ensure that your backup strategy is appropriate for this setting.");
        }
    }
    
    /**
     * Acquire the job lock
     */
    private String acquireLock(JobLockRefreshCallback lockCallback)
    {
        // Get lock
        String lockToken = jobLockService.getLock(LOCK_QNAME, LOCK_TTL);

        // Register the refresh callback which will keep the lock alive
        jobLockService.refreshLock(lockToken, LOCK_QNAME, LOCK_TTL, lockCallback);

        if (logger.isDebugEnabled())
        {
            logger.debug("lock acquired: " + LOCK_QNAME + ": " + lockToken);
        }

        return lockToken;
    }
    
    /**
     * Release the lock after the job completes
     */
    private void releaseLock(LockCallback lockCallback, String lockToken)
    {
        if (lockCallback != null)
        {
            lockCallback.running.set(false);
        }

        if (lockToken != null)
        {
            jobLockService.releaseLock(lockToken, LOCK_QNAME);
            if (logger.isDebugEnabled())
            {
                logger.debug("Lock released: " + LOCK_QNAME + ": " + lockToken);
            }
        }
    }
    
    public void execute()
    {
        checkProperties();
        
        // Bypass if the system is in read-only mode
        if (transactionService.isReadOnly())
        {
            logger.debug("Content store cleanup bypassed; the system is read-only.");
            return;
        }

        LockCallback lockCallback = new LockCallback();
        String lockToken = null;
        try
        {
            logger.debug("Content store cleanup started.");
            lockToken = acquireLock(lockCallback);
            executeInternal();
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug("   Content store cleanup completed.");
            }
        }
        catch (LockAcquisitionException e)
        {
            // Job being done by another process
            if (logger.isDebugEnabled())
            {
                logger.debug("   Content store cleanup already underway.");
            }
        }
        catch (VmShutdownException e)
        {
            // Aborted
            if (logger.isDebugEnabled())
            {
                logger.debug("   Content store cleanup aborted.");
            }
        }
        finally
        {
            releaseLock(lockCallback, lockToken);
        }
    }
    
    private void executeInternal()
    {
        final long maxOrphanTime = System.currentTimeMillis() - (protectDays * 24 * 3600 * 1000L);
        // execute in READ-WRITE txn
        RetryingTransactionCallback<Long> getAndDeleteWork = new RetryingTransactionCallback<Long>()
        {
            public Long execute() throws Exception
            {
                return cleanBatch(maxOrphanTime, batchSize);
            };
        };
        while (true)
        {
            Long lastProcessedOrphanId = transactionService.getRetryingTransactionHelper().doInTransaction(getAndDeleteWork);
            if (vmShutdownListener.isVmShuttingDown())
            {
                throw new VmShutdownException();
            }
            if (lastProcessedOrphanId == null)
            {
                // There is no more to process
                break;
            }
            // There is still more to delete, so continue
            if (logger.isDebugEnabled())
            {
                logger.debug("   Removed orphaned content URLs up orphan time " + new Date(lastProcessedOrphanId));
            }
        }
        // Done
    }
    
    private class LockCallback implements JobLockRefreshCallback
    {
        final AtomicBoolean running = new AtomicBoolean(true);
        
        @Override
        public boolean isActive()
        {
            return running.get();
        }
        
        @Override
        public void lockReleased()
        {
            running.set(false);
            if (logger.isDebugEnabled())
            {
                logger.debug("Lock release notification: " + LOCK_QNAME);
            }
        }
    }
    
    /**
     * 
     * @param maxTimeExclusive      the max orphan time (exclusive)
     * @param batchSize             the maximum number of orphans to process
     * @return                      Returns the last processed orphan ID or <tt>null</tt> if nothing was processed
     */
    private Long cleanBatch(final long maxTimeExclusive, final int batchSize)
    {
        // Get a bunch of cleanable URLs
        final TreeMap<Long, String> urlsById = new TreeMap<Long, String>();
        ContentUrlHandler contentUrlHandler = new ContentUrlHandler()
        {
            @Override
            public void handle(Long id, String contentUrl, Long orphanTime)
            {
                urlsById.put(id, contentUrl);
            }
        };
        // Get a bunch of cleanable URLs
        contentDataDAO.getContentUrlsOrphaned(contentUrlHandler, maxTimeExclusive, batchSize);
        
        // Shortcut, if necessary
        if (urlsById.size() == 0)
        {
            return null;
        }
        
        // Compile list of IDs and do a mass delete, recording the IDs to find the largest
        Long lastId = urlsById.lastKey();
        List<Long> ids = new ArrayList<Long>(urlsById.keySet());
        contentDataDAO.deleteContentUrls(ids);
        // No problems, so far (ALF-1998: contentStoreCleanerJob leads to foreign key exception)

        // Now attempt to physically delete the URLs
        for (Long id : ids)
        {
            String contentUrl = urlsById.get(id);
            // Handle failures
            boolean deleted = eagerContentStoreCleaner.deleteFromStores(contentUrl);
            if (!deleted)
            {
                switch (deletionFailureAction)
                {
                    case KEEP_URL:
                        // Keep the URL, but with an orphan time of 0 so that it is recorded
                        contentDataDAO.createContentUrlOrphaned(contentUrl, new Date(0L));
                    case IGNORE:
                        break;
                    default:
                        throw new IllegalStateException("Unknown deletion failure action: " + deletionFailureAction);
                }
            }
        }
        
        // Done
        return lastId;
    }
}
