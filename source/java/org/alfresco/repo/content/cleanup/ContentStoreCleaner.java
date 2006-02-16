/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.content.cleanup;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.transaction.TransactionService;
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
    
    private DictionaryService dictionaryService;
    private NodeDaoService nodeDaoService;
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
        if (dictionaryService == null)
        {
            throw new AlfrescoRuntimeException("Property 'dictionaryService' not set");
        }
        if (nodeDaoService == null)
        {
            throw new AlfrescoRuntimeException("Property 'nodeDaoService' not set");
        }
        if (transactionService == null)
        {
            throw new AlfrescoRuntimeException("Property 'transactionService' not set");
        }
        if (stores == null || stores.size() == 0)
        {
            throw new AlfrescoRuntimeException("Property 'stores' not set");
        }
        if (listeners == null)
        {
            throw new AlfrescoRuntimeException("Property 'listeners' not set");
        }
    }
    
    private Set<String> getValidUrls()
    {
        // wrap to make the request in a transaction
        TransactionWork<List<String>> getUrlsWork = new TransactionWork<List<String>>()
        {
            public List<String> doWork() throws Exception
            {
                return nodeDaoService.getContentDataStrings();
            };
        };
        // execute in READ-ONLY txn
        List<String> contentDataStrings = TransactionUtil.executeInUserTransaction(
                transactionService,
                getUrlsWork,
                true);
        
        // get all valid URLs
        Set<String> validUrls = new HashSet<String>(contentDataStrings.size());
        // convert the strings to objects and extract the URL
        for (String contentDataString : contentDataStrings)
        {
            ContentData contentData = ContentData.createContentProperty(contentDataString);
            if (contentData.getContentUrl() != null)
            {
                // a URL was present
                validUrls.add(contentData.getContentUrl());
            }
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
        Set<String> validUrls = getValidUrls();
        // now clean each store in turn
        for (ContentStore store : stores)
        {
            clean(validUrls, store);
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
