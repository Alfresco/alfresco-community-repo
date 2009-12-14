/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.PropertyCheck;
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
     * @see #onContentPropertyUpdate(NodeRef, QName, ContentData, ContentData)
     * @see #afterCommit()
     */
    private static final String KEY_POST_COMMIT_DELETION_URLS = "ContentStoreCleaner.PostCommitDeletionUrls";
    /**
     * Content URLs to delete if the transaction rolls b ack.
     * @see #onContentPropertyUpdate(NodeRef, QName, ContentData, ContentData)
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
        if (!eagerOrphanCleanup)
        {
            return;
        }
        Set<String> urlsToDelete = TransactionalResourceHelper.getSet(KEY_POST_ROLLBACK_DELETION_URLS);
        urlsToDelete.add(contentUrl);
        // Register to listen for transaction rollback
        AlfrescoTransactionSupport.bindListener(this);
    }

    /**
     * Queues orphaned content for post-transaction removal
     */
    public void registerOrphanedContentUrl(String contentUrl)
    {
        if (!eagerOrphanCleanup)
        {
            return;
        }
        Set<String> urlsToDelete = TransactionalResourceHelper.getSet(KEY_POST_COMMIT_DELETION_URLS);
        urlsToDelete.add(contentUrl);
        // Register to listen for transaction commit
        AlfrescoTransactionSupport.bindListener(this);
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
        // Delete
        for (String contentUrl : urlsToDelete)
        {
            for (ContentStore store : stores)
            {
                for (ContentStoreCleanerListener listener : listeners)
                {
                    listener.beforeDelete(store, contentUrl);
                }
                store.delete(contentUrl);
            }
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
                else
                {
                    logger.debug("   Deleting content URL: " + contentUrl);
                }
                count++;
            }
        }
        // Delete, but don't call the listeners - these URLs never did get metadata
        for (String contentUrl : urlsToDelete)
        {
            for (ContentStore store : stores)
            {
                store.delete(contentUrl);
            }
        }
    }
}
