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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeDAO;
import org.alfresco.repo.avm.AVMNodeDAO.ContentUrlHandler;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.domain.ContentUrlDAO;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.node.db.NodeDaoService.NodePropertyHandler;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
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
            extends TransactionListenerAdapter
            implements CopyServicePolicies.OnCopyCompletePolicy,
                       NodeServicePolicies.BeforeDeleteNodePolicy,
                       ContentServicePolicies.OnContentPropertyUpdatePolicy
                       
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
    
    private static Log logger = LogFactory.getLog(ContentStoreCleaner.class);
    
    /** kept to notify the thread that it should quit */
    private static VmShutdownListener vmShutdownListener = new VmShutdownListener("ContentStoreCleaner");
    
    private DictionaryService dictionaryService;
    private PolicyComponent policyComponent;
    private ContentService contentService;
    private NodeDaoService nodeDaoService;
    private AVMNodeDAO avmNodeDAO;
    private ContentUrlDAO contentUrlDAO;
    private TransactionService transactionService;
    private List<ContentStore> stores;
    private boolean eagerOrphanCleanup;
    private List<ContentStoreCleanerListener> listeners;
    private int protectDays;
    
    public ContentStoreCleaner()
    {
        this.stores = new ArrayList<ContentStore>(0);
        this.listeners = new ArrayList<ContentStoreCleanerListener>(0);
        this.protectDays = 7;
    }

    /**
     * @param dictionaryService used to determine which properties are content properties
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param policyComponent   used to register to listen for content updates
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
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
     * @param contentUrlDAO     DAO for recording valid <b>Content URLs</b>
     */
    public void setContentUrlDAO(ContentUrlDAO contentUrlDAO)
    {
        this.contentUrlDAO = contentUrlDAO;
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
     * 
     * @param eagerOrphanCleanup    <tt>true</tt> to clean up content
     */
    public void setEagerOrphanCleanup(boolean eagerOrphanCleanup)
    {
        this.eagerOrphanCleanup = eagerOrphanCleanup;
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
        if (!eagerOrphanCleanup)
        {
            // Don't register for anything because eager cleanup is disabled
            return;
        }
        // Register for the updates
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onContentPropertyUpdate"),
                this,
                new JavaBehaviour(this, "onContentPropertyUpdate"));
        // TODO: This is a RM-specific hack.  Once content properties are separated out, the
        //       following should be accomplished with a trigger to clean up orphaned content.
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
                ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "beforeDeleteNode"));
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCopyComplete"),
                ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "onCopyComplete"));
    }
    
    /**
     * Perform basic checks to ensure that the necessary dependencies were injected.
     */
    private void checkProperties()
    {
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        PropertyCheck.mandatory(this, "contentService", contentService);
        PropertyCheck.mandatory(this, "nodeDaoService", nodeDaoService);
        PropertyCheck.mandatory(this, "avmNodeDAO", avmNodeDAO);
        PropertyCheck.mandatory(this, "contentUrlDAO", contentUrlDAO);
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

    /**
     * Makes sure that copied files get a new, unshared binary.
     */
    public void onCopyComplete(
            QName classRef,
            NodeRef sourceNodeRef,
            NodeRef targetNodeRef,
            boolean copyToNewNode,
            Map<NodeRef, NodeRef> copyMap)
    {
        // Get the cm:content of the source
        ContentReader sourceReader = contentService.getReader(sourceNodeRef, ContentModel.PROP_CONTENT);
        if (sourceReader == null || !sourceReader.exists())
        {
            // No point attempting to duplicate missing content.  We're only interested in cleanin up.
            return;
        }
        // Get the cm:content of the target
        ContentReader targetReader = contentService.getReader(targetNodeRef, ContentModel.PROP_CONTENT);
        if (targetReader == null || !targetReader.exists())
        {
            // The target's content is not present, so we don't copy anything over
            return;
        }
        else if (!targetReader.getContentUrl().equals(sourceReader.getContentUrl()))
        {
            // The target already has a different binary
            return;
        }
        // Create new content for the target node.  This will behave just like an update to the node
        // but occurs in the same txn as the copy process.  Clearly this is a hack and is only
        // able to work when properties are copied with all the proper copy-related policies being
        // triggered.
        ContentWriter targetWriter = contentService.getWriter(targetNodeRef, ContentModel.PROP_CONTENT, true);
        targetWriter.putContent(sourceReader);
        // This will have triggered deletion of the source node's content because the target node
        // is being updated.  Force the source node's content to be protected.
        Set<String> urlsToDelete = TransactionalResourceHelper.getSet(KEY_POST_COMMIT_DELETION_URLS);
        urlsToDelete.remove(sourceReader.getContentUrl());
    }

    /**
     * Records the content URLs of <b>cm:content</b> for post-commit cleanup.
     */
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        // Get the cm:content property
        Pair<Long, NodeRef> nodePair = nodeDaoService.getNodePair(nodeRef);
        if (nodePair == null)
        {
            return;
        }
        ContentData contentData = (ContentData) nodeDaoService.getNodeProperty(
                nodePair.getFirst(), ContentModel.PROP_CONTENT);
        if (contentData == null || !ContentData.hasContent(contentData))
        {
            return;
        }
        String contentUrl = contentData.getContentUrl();
        // Bind it to the list
        Set<String> urlsToDelete = TransactionalResourceHelper.getSet(KEY_POST_COMMIT_DELETION_URLS);
        urlsToDelete.add(contentUrl);
        AlfrescoTransactionSupport.bindListener(this);
    }

    /**
     * Checks for {@link #setEagerOrphanCleanup(boolean) eager cleanup} and pushes the old content URL into
     * a list for post-transaction deletion.
     */
    public void onContentPropertyUpdate(
            NodeRef nodeRef,
            QName propertyQName,
            ContentData beforeValue,
            ContentData afterValue)
    {
        boolean registerListener = false;
        // Bind in URLs to delete when txn commits
        if (beforeValue != null && ContentData.hasContent(beforeValue))
        {
            // Register the URL to delete
            String contentUrl = beforeValue.getContentUrl();
            Set<String> urlsToDelete = TransactionalResourceHelper.getSet(KEY_POST_COMMIT_DELETION_URLS);
            urlsToDelete.add(contentUrl);
            // Register to listen for transaction commit
            registerListener = true;
        }
        // Bind in URLs to delete when txn rolls back
        if (afterValue != null && ContentData.hasContent(afterValue))
        {
            // Register the URL to delete
            String contentUrl = afterValue.getContentUrl();
            Set<String> urlsToDelete = TransactionalResourceHelper.getSet(KEY_POST_ROLLBACK_DELETION_URLS);
            urlsToDelete.add(contentUrl);
            // Register to listen for transaction rollback
            registerListener = true;
        }
        // Register listener
        if (registerListener)
        {
            AlfrescoTransactionSupport.bindListener(this);
        }
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

    private void removeContentUrlsPresentInMetadata()
    {
        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        
        // Remove all the Content URLs for the ADM repository
        // Handler that records the URL
        final NodePropertyHandler nodePropertyHandler = new NodePropertyHandler()
        {
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
                    contentUrlDAO.deleteContentUrl(contentUrl);
                }
            }
        };
        final DataTypeDefinition contentDataType = dictionaryService.getDataType(DataTypeDefinition.CONTENT);
        // execute in READ-WRITE txn
        RetryingTransactionCallback<Object> getUrlsCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                nodeDaoService.getPropertyValuesByActualType(contentDataType, nodePropertyHandler);
                return null;
            };
        };
        txnHelper.doInTransaction(getUrlsCallback);
        
        // Do the same for the AVM repository.
        final ContentUrlHandler handler = new ContentUrlHandler()
        {
            public void handle(String contentUrl)
            {
                if (vmShutdownListener.isVmShuttingDown())
                {
                    throw new VmShutdownException();
                }
                contentUrlDAO.deleteContentUrl(contentUrl);
            }
        };
        // execute in READ-WRITE txn
        RetryingTransactionCallback<Object> getAVMUrlsCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                avmNodeDAO.getContentUrls(handler);
                return null;
            }
        };
        txnHelper.doInTransaction(getAVMUrlsCallback);
    }
    
    private void addContentUrlsPresentInStores()
    {
        org.alfresco.repo.content.ContentStore.ContentUrlHandler handler = new org.alfresco.repo.content.ContentStore.ContentUrlHandler()
        {
            public void handle(String contentUrl)
            {
                if (vmShutdownListener.isVmShuttingDown())
                {
                    throw new VmShutdownException();
                }
                contentUrlDAO.createContentUrl(contentUrl);
            }
        };
        Date checkAllBeforeDate = new Date(System.currentTimeMillis() - (long) protectDays * 3600L * 1000L * 24L);
        for (ContentStore store : stores)
        {
            store.getUrls(null, checkAllBeforeDate, handler);
        }
    }
    
    public void execute()
    {
        checkProperties();
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Starting content store cleanup.");
        }
        // Repeat attempts six times waiting 10 minutes between
        executeInternal(0, 6, 600000);
    }
    
    public void executeInternal(int currentAttempt, int maxAttempts, long waitTime)
    {
        currentAttempt++;
        // This handler removes the URLs from all the stores
        final org.alfresco.repo.domain.ContentUrlDAO.ContentUrlHandler handler = new org.alfresco.repo.domain.ContentUrlDAO.ContentUrlHandler()
        {
            public void handle(String contentUrl)
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
                }
            }
        };
        // execute in READ-WRITE txn
        RetryingTransactionCallback<Object> executeCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                // Delete all the URLs
                contentUrlDAO.deleteAllContentUrls();
                // Populate the URLs from the content stores
                addContentUrlsPresentInStores();
                // Remove URLs present in the metadata
                removeContentUrlsPresentInMetadata();
                // Any remaining URLs are URls present in the stores but not in the metadata
                contentUrlDAO.getAllContentUrls(handler);
                // Delete all the URLs
                contentUrlDAO.deleteAllContentUrls();
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
        catch (Throwable e)
        {
            if (currentAttempt >= maxAttempts)
            {
                throw new AlfrescoRuntimeException("Failed to initiate content store clean", e);
            }
            if (RetryingTransactionHelper.extractRetryCause(e) != null)
            {
                // There are grounds for waiting and retrying
                synchronized(this)
                {
                    try { this.wait(waitTime); } catch (InterruptedException ee) {}
                }
                executeInternal(currentAttempt, maxAttempts, waitTime);
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
