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
package org.alfresco.repo.content;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A store providing support for content store implementations that provide
 * routing of content read and write requests based on context.
 * 
 * @see ContentContext
 * 
 * @since 2.1
 * @author Derek Hulley
 */
public abstract class AbstractRoutingContentStore implements ContentStore
{
    private static Log logger = LogFactory.getLog(AbstractRoutingContentStore.class);
    
    private SimpleCache<String, ContentStore> storesByContentUrl;
    private ReadLock storesCacheReadLock;
    private WriteLock storesCacheWriteLock;
    
    protected AbstractRoutingContentStore()
    {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        storesCacheReadLock = lock.readLock();
        storesCacheWriteLock = lock.writeLock();
    }

    /**
     * @param storesCache       cache of stores used to access URLs 
     */
    public void setStoresCache(SimpleCache<String, ContentStore> storesCache)
    {
        this.storesByContentUrl = storesCache;
    }
    
    /**
     * @return          Returns a list of all possible stores available for reading or writing
     */
    protected abstract List<ContentStore> getAllStores();
    
    /**
     * Get a content store based on the context provided.  The applicability of the
     * context and even the types of context allowed are up to the implementation, but
     * normally there should be a fallback case for when the parameters are not adequate
     * to make a decision.
     * 
     * @param ctx       the context to use to make the choice
     * @return          Returns the store most appropriate for the given context
     */
    protected abstract ContentStore selectWriteStore(ContentContext ctx);
    
    /**
     * Checks the cache for the store and ensures that the URL is in the store.
     * 
     * @param contentUrl    the content URL to search for
     * @return              Returns the store matching the content URL
     */
    private ContentStore selectReadStore(String contentUrl)
    {
        storesCacheReadLock.lock();
        try
        {
            // Check if the store is in the cache
            ContentStore store = storesByContentUrl.get(contentUrl);
            if (store != null && store.exists(contentUrl))
            {
                // We found a store and can use it
                return store;
            }
        }
        finally
        {
            storesCacheReadLock.unlock();
        }
        // Get the write lock and double check
        storesCacheWriteLock.lock();
        try
        {
            // Double check
            ContentStore store = storesByContentUrl.get(contentUrl);
            if (store != null && store.exists(contentUrl))
            {
                // We found a store and can use it
                if (logger.isDebugEnabled())
                {
                    logger.debug(
                            "Found mapped store for content URL: \n" +
                            "   Content URL: " + contentUrl + "\n" +
                            "   Store:       " + store);
                }
                return store;
            }
            // It isn't, so search all the stores
            List<ContentStore> stores = getAllStores();
            for (ContentStore storeInList : stores)
            {
                if (!store.exists(contentUrl))
                {
                    // It is not in the store
                    continue;
                }
                // We found one
                store = storeInList;
                // Put the value in the cache
                storesByContentUrl.put(contentUrl, store);
                break;
            }
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Mapped content URL to store for reading: \n" +
                        "   Content URL: " + contentUrl + "\n" +
                        "   Store:       " + store);
            }
            return store;
        }
        finally
        {
            storesCacheWriteLock.unlock();
        }
    }

    /**
     * This operation has to be performed on all the stores in order to maintain the
     * {@link ContentStore#exists(String)} contract.
     */
    public boolean delete(String contentUrl) throws ContentIOException
    {
        boolean deleted = true;
        List<ContentStore> stores = getAllStores();
        for (ContentStore store : stores)
        {
            deleted &= store.delete(contentUrl);
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Deleted content URL from stores: \n" +
                    "   Stores:  " + stores.size() + "\n" +
                    "   Deleted: " + deleted);
        }
        return deleted;
    }

    /**
     * @see #selectReadStore(String)
     */
    public boolean exists(String contentUrl) throws ContentIOException
    {
        ContentStore store = selectReadStore(contentUrl);
        return (store != null);
    }

    /**
     * @return  Returns a valid reader from one of the stores otherwise
     *          a {@link EmptyContentReader} is returned.
     */
    public ContentReader getReader(String contentUrl) throws ContentIOException
    {
        ContentStore store = selectReadStore(contentUrl);
        if (store != null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Getting reader from store: \n" +
                        "   Content URL: " + contentUrl + "\n" +
                        "   Store:       " + store);
            }
            return store.getReader(contentUrl);
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Getting empty reader for content URL: " + contentUrl);
            }
            return new EmptyContentReader(contentUrl);
        }
    }

    /**
     * Compile a set of URLs from all stores.
     */
    public Set<String> getUrls() throws ContentIOException
    {
        Set<String> urls = new HashSet<String>(1139);
        List<ContentStore> stores = getAllStores();
        for (ContentStore store : stores)
        {
            Set<String> storeUrls = store.getUrls();
            urls.addAll(storeUrls);
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("Found " + urls.size() + " URLs from " + stores.size() + " stores");
        }
        return urls;
    }

    /**
     * Compile a set of URLs from all stores given the date range.
     */
    public Set<String> getUrls(Date createdAfter, Date createdBefore) throws ContentIOException
    {
        Set<String> urls = new HashSet<String>(1139);
        List<ContentStore> stores = getAllStores();
        for (ContentStore store : stores)
        {
            Set<String> storeUrls = store.getUrls(createdAfter, createdBefore);
            urls.addAll(storeUrls);
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("Found " + urls.size() + " URLs from " + stores.size() + " stores");
        }
        return urls;
    }

    /**
     * Selects a store for the given context and caches store that was used.
     * 
     * @see #selectWriteStore(ContentContext)
     */
    public ContentWriter getWriter(ContentContext context) throws ContentIOException
    {
        // Select the store for writing
        ContentStore store = selectWriteStore(context);
        ContentWriter writer = store.getWriter(context);
        // Cache the store against the URL
        storesCacheWriteLock.lock();
        try
        {
            String contentUrl = writer.getContentUrl();
            storesByContentUrl.put(contentUrl, store);
        }
        finally
        {
            storesCacheWriteLock.unlock();
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Got writer and cache URL from store: \n" +
                    "   Context: " + context + "\n" +
                    "   Writer:  " + writer + "\n" +
                    "   Store:   " + store);
        }
        return writer;
    }

    /**
     * @see 
     */
    public ContentWriter getWriter(ContentReader existingContentReader, String newContentUrl) throws ContentIOException
    {
        return getWriter(new ContentContext(existingContentReader, newContentUrl));
    }
}
