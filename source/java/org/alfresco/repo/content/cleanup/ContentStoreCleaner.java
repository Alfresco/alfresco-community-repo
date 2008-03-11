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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.avm.AVMNodeDAO;
import org.alfresco.repo.avm.AVMNodeDAO.ContentUrlHandler;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.domain.ContentUrlDAO;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.node.db.NodeDaoService.NodePropertyHandler;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.VmShutdownListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This component is responsible for finding orphaned content in a given
 * content store or stores.  Deletion handlers can be provided to ensure
 * that the content is moved to another location prior to being removed
 * from the store(s) being cleaned.
 * 
 * @author Derek Hulley
 */
public class ContentStoreCleaner
{
    private static Log logger = LogFactory.getLog(ContentStoreCleaner.class);
    
    /** kept to notify the thread that it should quit */
    private static VmShutdownListener vmShutdownListener = new VmShutdownListener("ContentStoreCleaner");
    
    private DictionaryService dictionaryService;
    private NodeDaoService nodeDaoService;
    private AVMNodeDAO avmNodeDAO;
    private ContentUrlDAO contentUrlDAO;
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
     * @param dictionaryService used to determine which properties are content properties
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
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
     * Perform basic checks to ensure that the necessary dependencies were injected.
     */
    private void checkProperties()
    {
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
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
    
    private void removeContentUrlsPresentInMetadata()
    {
        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        
        // Remove all the Content URLs for the ADM repository
        // Handler that records the URL
        final NodePropertyHandler nodePropertyHandler = new NodePropertyHandler()
        {
            public void handle(Node node, Serializable value)
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
