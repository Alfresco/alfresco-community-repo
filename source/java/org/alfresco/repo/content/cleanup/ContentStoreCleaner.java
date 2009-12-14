/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.content.cleanup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.avm.AVMNodeDAO;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.domain.contentclean.ContentCleanDAO;
import org.alfresco.repo.domain.contentclean.ContentCleanDAO.ContentUrlBatchProcessor;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.node.db.NodeDaoService.NodePropertyHandler;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.extensions.surf.util.PropertyCheck;
import org.alfresco.util.VmShutdownListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This component is responsible cleaning up orphaned content.
 * <p/>
 * Clean-up happens at two levels.<p/>
 * <u><b>Eager cleanup:</b></u> (since 3.2)<p/>
 * If {@link #setEagerOrphanCleanup(boolean) eager cleanup} is activated, then this
 * component listens to all content property change events and recorded for post-transaction
 * processing.  All orphaned content is deleted from the registered store(s).  Note that
 * any {@link #setListeners(List) listeners} are called as normal; backup or scrubbing
 * procedures should be plugged in as listeners if this is required.
 * <p/>
 * <u><b>Lazy cleanup:</b></u><p/>
 * This is triggered by means of a {@link ContentStoreCleanupJob Quartz job}.  This is
 * a heavy-weight process that effectively compares the database metadata with the
 * content URLs controlled by the various stores.  Once again, the listeners are called
 * appropriately.
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
    private static Log logger = LogFactory.getLog(ContentStoreCleaner.class);
    
    /** kept to notify the thread that it should quit */
    private static VmShutdownListener vmShutdownListener = new VmShutdownListener("ContentStoreCleaner");
    
    private JobLockService jobLockService;
    private ContentCleanDAO contentCleanDAO;
    private ContentDataDAO contentDataDAO;
    private DictionaryService dictionaryService;
    private ContentService contentService;
    private NodeDaoService nodeDaoService;
    private AVMNodeDAO avmNodeDAO;
    private TransactionService transactionService;
    private List<ContentStore> stores;
    private List<ContentStoreCleanerListener> listeners;
    private int protectDays;
    
    public ContentStoreCleaner()
    {
        this.stores = new ArrayList<ContentStore>(0);
        this.listeners = new ArrayList<ContentStoreCleanerListener>(0);
        this.protectDays = 7;
    }

    /**
     * @param jobLockService        service used to ensure that cleanup runs are not duplicated
     */
    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    /**
     * @param contentCleanDAO       DAO used for manipulating content URLs
     */
    public void setContentCleanDAO(ContentCleanDAO contentCleanDAO)
    {
        this.contentCleanDAO = contentCleanDAO;
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
     * @param nodeDaoService used to get the property values
     */
    public void setNodeDaoService(NodeDaoService nodeDaoService)
    {
        this.nodeDaoService = nodeDaoService;
    }

    /**
     * @param avmNodeDAO The AVM Node DAO to get urls with.
     */
    public void setAvmNodeDAO(AVMNodeDAO avmNodeDAO)
    {
        this.avmNodeDAO = avmNodeDAO;
    }
    
    /**
     * @param transactionService the component to ensure proper transactional wrapping
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * @param stores the content stores to clean
     */
    public void setStores(List<ContentStore> stores)
    {
        this.stores = stores;
    }

    /**
     * @param listeners the listeners that can react to deletions
     */
    public void setListeners(List<ContentStoreCleanerListener> listeners)
    {
        this.listeners = listeners;
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
     * Initializes the cleaner based on the {@link #setEagerOrphanCleanup(boolean) eagerCleanup} flag.
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
        PropertyCheck.mandatory(this, "contentCleanerDAO", contentCleanDAO);
        PropertyCheck.mandatory(this, "contentDataDAO", contentDataDAO);
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "contentService", contentService);
        PropertyCheck.mandatory(this, "nodeDaoService", nodeDaoService);
        PropertyCheck.mandatory(this, "avmNodeDAO", avmNodeDAO);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "listeners", listeners);
        
        // check the protect days
        if (protectDays < 0)
        {
            throw new AlfrescoRuntimeException("Property 'protectDays' must be 0 or greater (0 is not recommended)");
        }
        else if (protectDays == 0)
        {
            logger.warn(
                    "Property 'protectDays' is set to 0.  " +
                    "It is possible that in-transaction content will be deleted.");
        }
    }
    
    private void removeContentUrlsPresentInMetadata(final ContentUrlBatchProcessor urlRemover)
    {
        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        
        // Remove all the Content URLs for the ADM repository
        // Handlers that record the URLs
        final ContentDataDAO.ContentUrlHandler contentUrlHandler = new ContentDataDAO.ContentUrlHandler()
        {
            long lastLock = 0L;
            public void handle(String contentUrl)
            {
                if (vmShutdownListener.isVmShuttingDown())
                {
                    throw new VmShutdownException();
                }
                urlRemover.processContentUrl(contentUrl);
                // Check lock
                long now = System.currentTimeMillis();
                if (now - lastLock > (long)(LOCK_TTL/2L))
                {
                    jobLockService.getTransactionalLock(LOCK_QNAME, LOCK_TTL);
                    lastLock = now;
                }
            }
        };
        final NodePropertyHandler nodePropertyHandler = new NodePropertyHandler()
        {
            long lastLock = 0L;
            public void handle(NodeRef nodeRef, QName nodeTypeQName, QName propertyQName, Serializable value)
            {
                if (vmShutdownListener.isVmShuttingDown())
                {
                    throw new VmShutdownException();
                }
                // Convert the values to ContentData and extract the URLs
                ContentData contentData = DefaultTypeConverter.INSTANCE.convert(ContentData.class, value);
                String contentUrl = contentData.getContentUrl();
                if (contentUrl != null)
                {
                    urlRemover.processContentUrl(contentUrl);
                }
                // Check lock
                long now = System.currentTimeMillis();
                if (now - lastLock > (long)(LOCK_TTL/2L))
                {
                    jobLockService.getTransactionalLock(LOCK_QNAME, LOCK_TTL);
                    lastLock = now;
                }
            }
        };
        final DataTypeDefinition contentDataType = dictionaryService.getDataType(DataTypeDefinition.CONTENT);
        // execute in READ-WRITE txn
        RetryingTransactionCallback<Void> getUrlsCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Exception
            {
                contentDataDAO.getAllContentUrls(contentUrlHandler);
                nodeDaoService.getPropertyValuesByActualType(contentDataType, nodePropertyHandler);
                return null;
            };
        };
        txnHelper.doInTransaction(getUrlsCallback);
        
        // Do the same for the AVM repository.
        final AVMNodeDAO.ContentUrlHandler handler = new AVMNodeDAO.ContentUrlHandler()
        {
            long lastLock = 0L;
            public void handle(String contentUrl)
            {
                if (vmShutdownListener.isVmShuttingDown())
                {
                    throw new VmShutdownException();
                }
                urlRemover.processContentUrl(contentUrl);
                // Check lock
                long now = System.currentTimeMillis();
                if (now - lastLock > (long)(LOCK_TTL/2L))
                {
                    jobLockService.getTransactionalLock(LOCK_QNAME, LOCK_TTL);
                    lastLock = now;
                }
            }
        };
        // execute in READ-WRITE txn
        RetryingTransactionCallback<Void> getAVMUrlsCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Exception
            {
                avmNodeDAO.getContentUrls(handler);
                return null;
            }
        };
        txnHelper.doInTransaction(getAVMUrlsCallback);
    }
    
    private void addContentUrlsPresentInStores(final ContentUrlBatchProcessor urlInserter)
    {
        org.alfresco.repo.content.ContentStore.ContentUrlHandler handler = new org.alfresco.repo.content.ContentStore.ContentUrlHandler()
        {
            long lastLock = 0L;
            public void handle(String contentUrl)
            {
                if (vmShutdownListener.isVmShuttingDown())
                {
                    throw new VmShutdownException();
                }
                urlInserter.processContentUrl(contentUrl);
                // Check lock
                long now = System.currentTimeMillis();
                if (now - lastLock > (long)(LOCK_TTL/2L))
                {
                    jobLockService.getTransactionalLock(LOCK_QNAME, LOCK_TTL);
                    lastLock = now;
                }
            }
        };
        Date checkAllBeforeDate = new Date(System.currentTimeMillis() - (long) protectDays * 3600L * 1000L * 24L);
        for (ContentStore store : stores)
        {
            store.getUrls(null, checkAllBeforeDate, handler);
        }
    }
    
    private static final QName LOCK_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ContentStoreCleaner"); 
    private static final long LOCK_TTL = 30000L;
    public void execute()
    {
        checkProperties();

        RetryingTransactionCallback<Void> executeCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Exception
            {
                logger.debug("Content store cleanup started.");
                // Get the lock without any waiting
                // The lock will be refreshed, but the first lock starts the process
                jobLockService.getTransactionalLock(LOCK_QNAME, LOCK_TTL);
                executeInternal();
                return null;
            }
        };
        try
        {
            RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
            txnHelper.setMaxRetries(0);
            txnHelper.doInTransaction(executeCallback);
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug("   Content store cleanup completed.");
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
    }
    
    public void executeInternal()
    {
        final ContentUrlBatchProcessor storeUrlDeleteHandler = new ContentUrlBatchProcessor()
        {
            long lastLock = 0L;
            public void start()
            {
            }
            public void processContentUrl(String contentUrl)
            {
                for (ContentStore store : stores)
                {
                    if (vmShutdownListener.isVmShuttingDown())
                    {
                        throw new VmShutdownException();
                    }
                    if (logger.isDebugEnabled())
                    {
                        if (store.isWriteSupported())
                        {
                            logger.debug("   Deleting content URL: " + contentUrl);
                        }
                    }
                    for (ContentStoreCleanerListener listener : listeners)
                    {
                        listener.beforeDelete(store, contentUrl);
                    }
                    // Delete
                    store.delete(contentUrl);
                    // Check lock
                    long now = System.currentTimeMillis();
                    if (now - lastLock > (long)(LOCK_TTL/2L))
                    {
                        jobLockService.getTransactionalLock(LOCK_QNAME, LOCK_TTL);
                        lastLock = now;
                    }
                }
            }
            public void end()
            {
            }
        };
        // execute in READ-WRITE txn
        RetryingTransactionCallback<Void> executeCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Exception
            {
                // Clean up
                contentCleanDAO.cleanUp();
                // Push all store URLs in
                ContentUrlBatchProcessor urlInserter = contentCleanDAO.getUrlInserter();
                try
                {
                    urlInserter.start();
                    addContentUrlsPresentInStores(urlInserter);
                }
                finally
                {
                    urlInserter.end();
                }
                // Delete all content URLs
                ContentUrlBatchProcessor urlRemover = contentCleanDAO.getUrlRemover();
                try
                {
                    urlRemover.start();
                    removeContentUrlsPresentInMetadata(urlRemover);
                }
                finally
                {
                    urlRemover.end();
                }
                // Any remaining URLs are URls present in the stores but not in the metadata
                contentCleanDAO.listAllUrls(storeUrlDeleteHandler);
                // Clean up
                contentCleanDAO.cleanUp();
                return null;
            };
        };
        try
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(executeCallback);
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug("   Content store cleanup completed.");
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
    }

    /**
     * Message carrier to break out of loops using the callback.
     * 
     * @author Derek Hulley
     * @since 2.1.3
     */
    private class VmShutdownException extends RuntimeException
    {
        private static final long serialVersionUID = -5876107469054587072L;
    }
}
