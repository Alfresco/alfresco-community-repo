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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.content.cleanup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.avm.AVMNodeDAO;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
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
    
    private static VmShutdownListener vmShutdownListener = new VmShutdownListener(ContentStoreCleaner.class.getName());
    
    private DictionaryService dictionaryService;
    private NodeDaoService nodeDaoService;
    private TransactionService transactionService;
    private AVMNodeDAO avmNodeDAO;
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
     * Setter for Spring.
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
     * Perform basic checks to ensure that the necessary dependencies were injected.
     */
    private void checkProperties()
    {
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "nodeDaoService", nodeDaoService);
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
    
    private Set<String> getValidUrls()
    {
        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        
        final DataTypeDefinition contentDataType = dictionaryService.getDataType(DataTypeDefinition.CONTENT);
        // wrap to make the request in a transaction
        RetryingTransactionCallback<List<Serializable>> getUrlsCallback = new RetryingTransactionCallback<List<Serializable>>()
        {
            public List<Serializable> execute() throws Throwable
            {
                return nodeDaoService.getPropertyValuesByActualType(contentDataType);
            }
        };
        // execute in READ-ONLY txn
        List<Serializable> values = txnHelper.doInTransaction(getUrlsCallback, true);
        
        // Do the same for the AVM repository.
        RetryingTransactionCallback<List<String>> getAVMUrlsCallback = new RetryingTransactionCallback<List<String>>()
        {
            public List<String> execute() throws Exception
            {
                return avmNodeDAO.getContentUrls();
            }
        };
        // execute in READ-ONLY txn
        List<String> avmContentUrls = txnHelper.doInTransaction(getAVMUrlsCallback, true);
        
        // get all valid URLs
        Set<String> validUrls = new HashSet<String>(values.size());
        // convert the strings to objects and extract the URL
        for (Serializable value : values)
        {
            ContentData contentData = (ContentData) value;
            if (contentData.getContentUrl() != null)
            {
                // a URL was present
                validUrls.add(contentData.getContentUrl());
            }
        }
        // put all the avm urls into validUrls.
        for (String url : avmContentUrls)
        {
            validUrls.add(url);
        }
        
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Found " + validUrls.size() + " valid URLs in metadata");
        }
        return validUrls;
    }
    
    public void execute()
    {
        checkProperties();
        try
        {
            Set<String> validUrls = getValidUrls();
            // now clean each store in turn
            for (ContentStore store : stores)
            {
                try
                {
                    clean(validUrls, store);
                }
                catch (UnsupportedOperationException e)
                {
                    throw new ContentIOException(
                            "Unable to clean store as the necessary operations are not supported: " + store,
                            e);
                }
            }
        }
        catch (ContentIOException e)
        {
            throw e;
        }
        catch (Throwable e)
        {
            // If the VM is shutting down, then ignore
            if (vmShutdownListener.isVmShuttingDown())
            {
                // Ignore
            }
            else
            {
                logger.error("Exception during cleanup of content", e);
            }
        }
    }
    
    private void clean(Set<String> validUrls, ContentStore store)
    {
        Date checkAllBeforeDate = new Date(System.currentTimeMillis() - (long) protectDays * 3600L * 1000L * 24L);
        // get the store's URLs
        Set<String> storeUrls = store.getUrls(null, checkAllBeforeDate);
        // remove all URLs that occur in the validUrls
        storeUrls.removeAll(validUrls);
        // now clean the store
        for (String url : storeUrls)
        {
            ContentReader sourceReader = store.getReader(url);
            // announce this to the listeners
            for (ContentStoreCleanerListener listener : listeners)
            {
                // get a fresh reader
                ContentReader listenerReader = sourceReader.getReader();
                // call it
                listener.beforeDelete(listenerReader);
            }
            // delete it
            store.delete(url);
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Removed URL from store: \n" +
                        "   Store: " + store + "\n" +
                        "   URL: " + url);
            }
        }
    }
}
