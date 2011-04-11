/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.content.cleanup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.util.PropertyCheck;
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
public class EagerContentStoreCleaner extends TransactionListenerAdapter
{
    /**
     * Content URLs to delete once the transaction commits.
     * @see #afterCommit()
     */
    private static final String KEY_POST_COMMIT_DELETION_URLS = "ContentStoreCleaner.PostCommitDeletionUrls";
    /**
     * Content URLs to delete if the transaction rolls back.
     * @see #afterRollback()
     */
    private static final String KEY_POST_ROLLBACK_DELETION_URLS = "ContentStoreCleaner.PostRollbackDeletionUrls";
    
    private static Log logger = LogFactory.getLog(EagerContentStoreCleaner.class);
    
    private boolean eagerOrphanCleanup;
    private List<ContentStore> stores;
    private List<ContentStoreCleanerListener> listeners;
    
    public EagerContentStoreCleaner()
    {
        this.stores = new ArrayList<ContentStore>(0);
        this.listeners = new ArrayList<ContentStoreCleanerListener>(0);
    }

    /**
     * @param eagerOrphanCleanup    <tt>true</tt> to enable this component, otherwise <tt>false</tt>
     */
    public void setEagerOrphanCleanup(boolean eagerOrphanCleanup)
    {
        this.eagerOrphanCleanup = eagerOrphanCleanup;
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
        PropertyCheck.mandatory(this, "listeners", listeners);
    }
    
    /**
     * Queues orphaned content for post-transaction removal
     */
    public void registerNewContentUrl(String contentUrl)
    {
        Set<String> urlsToDelete = TransactionalResourceHelper.getSet(KEY_POST_ROLLBACK_DELETION_URLS);
        urlsToDelete.add(contentUrl);
        // Register to listen for transaction rollback
        AlfrescoTransactionSupport.bindListener(this);
    }

    /**
     * Queues orphaned content for post-transaction removal
     * <p/>
     * <b>NB: </b>Any content registered <u>will</u> be deleted if the current transaction
     *            commits and if 'eager' cleanup is turned on.
     * 
     * @return  Returns <tt>true</tt> if the content was scheduled for post-transaction deletion.
     *          If the return value is <tt>true</tt> then the calling code <b>must</b> delete
     *          the row entry for the content URL provided <b>BEFORE THE TRANSACTION COMMITS!</b>
     */
    public boolean registerOrphanedContentUrl(String contentUrl)
    {
        return registerOrphanedContentUrl(contentUrl, false);
    }

    /**
     * Queues orphaned content for post-transaction removal
     * <p/>
     * <b>NB: </b>Any content registered <u>will</u> be deleted if the current transaction
     *            commits and if 'eager' cleanup is turned on.
     * 
     * @param force         <tt>true</tt> for force the post-commit URL deletion
     *                      regardless of the setting {@link #setEagerOrphanCleanup(boolean)}.
     * @return  Returns <tt>true</tt> if the content was scheduled for post-transaction deletion.
     *          If the return value is <tt>true</tt> then the calling code <b>must</b> delete
     *          the row entry for the content URL provided <b>BEFORE THE TRANSACTION COMMITS!</b>
     */
    public boolean registerOrphanedContentUrl(String contentUrl, boolean force)
    {
        if (!eagerOrphanCleanup && !force)
        {
            return false;
        }
        Set<String> urlsToDelete = TransactionalResourceHelper.getSet(KEY_POST_COMMIT_DELETION_URLS);
        urlsToDelete.add(contentUrl);
        // Register to listen for transaction commit
        AlfrescoTransactionSupport.bindListener(this);
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Scheduled content for post-commit eager cleanup: " + contentUrl);
        }
        return true;
    }

    /**
     * Cleans up all newly-orphaned content
     */
    @Override
    public void afterCommit()
    {
        Set<String> urlsToDelete = TransactionalResourceHelper.getSet(KEY_POST_COMMIT_DELETION_URLS);
        // Debug
        if (logger.isDebugEnabled())
        {
            logger.debug("Post-commit deletion of old content URLs: ");
            int count = 0;
            for (String contentUrl : urlsToDelete)
            {
                if (count == 10)
                {
                    logger.debug("   " + (urlsToDelete.size() - 10) + " more ...");
                }
                else
                {
                    logger.debug("   Deleting content URL: " + contentUrl);
                }
                count++;
            }
        }
        // Delete, including calling listeners
        for (String contentUrl : urlsToDelete)
        {
            deleteFromStores(contentUrl, false);
        }
    }

    @Override
    public void afterRollback()
    {
        Set<String> urlsToDelete = TransactionalResourceHelper.getSet(KEY_POST_ROLLBACK_DELETION_URLS);
        // Debug
        if (logger.isDebugEnabled())
        {
            logger.debug("Post-rollback deletion of new content URLs: ");
            int count = 0;
            for (String contentUrl : urlsToDelete)
            {
                if (count == 10)
                {
                    logger.debug("   " + (urlsToDelete.size() - 10) + " more ...");
                }
                else if (count < 10)
                {
                    logger.debug("   Deleting content URL: " + contentUrl);
                }
                count++;
            }
        }
        // Delete, but don't call the listeners - these URLs never did get metadata
        for (String contentUrl : urlsToDelete)
        {
            deleteFromStores(contentUrl, false);
        }
    }
    
    /**
     * Delete the content URL from all stores
     * 
     * @param contentUrl                the URL to delete
     * @return                          Returns <tt>true</tt> if all deletes were successful
     */
    public boolean deleteFromStores(String contentUrl)
    {
        return deleteFromStores(contentUrl, true);
    }
    
    private boolean deleteFromStores(String contentUrl, boolean callListeners)
    {
        int deleted = 0;
        for (ContentStore store : stores)
        {
            // Bypass if the store is read-only or doesn't support the URL
            if (!store.isWriteSupported() || !store.isContentUrlSupported(contentUrl))
            {
                continue;
            }
            if (callListeners)
            {
                // Call listeners
                for (ContentStoreCleanerListener listener : listeners)
                {
                    try
                    {
                        // Since we are in post-commit, we do best-effort
                        listener.beforeDelete(store, contentUrl);
                    }
                    catch (Throwable e)
                    {
                        logger.error(
                                "Content deletion listener failed: \n" +
                                "   URL:    " + contentUrl + "\n" +
                                "   Source: " + store,
                                e);
                    }
                }
            }
            // Delete
            if (deleteFromStore(contentUrl, store))
            {
                deleted++;
            }
        }
        // Did we delete from all stores (non-existence is a delete, too)
        return deleted == stores.size();
    }
    
    /**
     * Attempts to delete the URL from the store, catching and reporing errors.
     */
    private boolean deleteFromStore(String contentUrl, ContentStore store)
    {
        try
        {
            // Since we are in post-commit, we do best-effort
            if (!store.delete(contentUrl))
            {
                logger.error(
                        "Content deletion failed (no exception): \n" +
                        "   URL:    " + contentUrl + "\n" +
                        "   Source: " + store);
                return false;
            }
            else
            {
                return true;
            }
        }
        catch (Throwable e)
        {
            logger.error(
                    "Content deletion failed: \n" +
                    "   URL:    " + contentUrl + "\n" +
                    "   Source: " + store,
                    e);
            return false;
        }
    }
}
