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

import org.alfresco.error.AlfrescoRuntimeException;
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
     * @return          Returns the store most appropriate for the given context and
     *                  <b>never <tt>null</tt></b>
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
            if (store != null)
            {
                // We found a store that was previously used
                try
                {
                    // It is possible for content to be removed from a store and
                    // it might have moved into another store.
                    if (store.exists(contentUrl))
                    {
                        // We found a store and can use it
                        return store;
                    }
                }
                catch (UnsupportedContentUrlException e)
                {
                    // This is odd.  The store that previously supported the content URL
                    // no longer does so.  I can't think of a reason why that would be.
                    throw new AlfrescoRuntimeException(
                            "Found a content store that previously supported a URL, but no longer does: \n" +
                            "   Store:       " + store + "\n" +
                            "   Content URL: " + contentUrl);
                }
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
            else
            {
                store = null;
            }
            // It isn't, so search all the stores
            List<ContentStore> stores = getAllStores();
            // Keep track of the unsupported state of the content URL - it might be a rubbish URL
            boolean contentUrlSupported = false;
            for (ContentStore storeInList : stores)
            {
                boolean exists = false;
                try
                {
                    exists = storeInList.exists(contentUrl);
                    // At least the content URL was supported
                    contentUrlSupported = true;
                }
                catch (UnsupportedContentUrlException e)
                {
                    // The store can't handle the content URL
                }
                if (!exists)
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
            // Check if the content URL was supported
            if (!contentUrlSupported)
            {
                throw new UnsupportedContentUrlException(this, contentUrl);
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
     * @return      Returns <tt>true</tt> if the URL is supported by any of the stores.
     */
    public boolean isContentUrlSupported(String contentUrl)
    {
        List<ContentStore> stores = getAllStores();
        boolean supported = false;
        for (ContentStore store : stores)
        {
            if (store.isContentUrlSupported(contentUrl))
            {
                supported = true;
                break;
            }
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("The url " + (supported ? "is" : "is not") + " supported by at least one store.");
        }
        return supported;
    }

    /**
     * @return      Returns <tt>true</tt> if write is supported by any of the stores.
     */
    public boolean isWriteSupported()
    {
        List<ContentStore> stores = getAllStores();
        boolean supported = false;
        for (ContentStore store : stores)
        {
            if (store.isWriteSupported())
            {
                supported = true;
                break;
            }
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Writing " + (supported ? "is" : "is not") + " supported by at least one store.");
        }
        return supported;
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
     * Selects a store for the given context and caches store that was used.
     * 
     * @see #selectWriteStore(ContentContext)
     */
    public ContentWriter getWriter(ContentContext context) throws ContentIOException
    {
        String contentUrl = context.getContentUrl();
        if (contentUrl != null)
        {
            // Check to see if it is in the cache
            storesCacheReadLock.lock();
            try
            {
                // Check if the store is in the cache
                ContentStore store = storesByContentUrl.get(contentUrl);
                if (store != null)
                {
                    throw new ContentExistsException(this, contentUrl);
                }
                /*
                 * We could go further and check each store for the existence of the URL,
                 * but that would be overkill.  The main problem we need to prevent is
                 * the simultaneous access of the same store.  The router represents
                 * a single store and therefore if the URL is present in any of the stores,
                 * it is effectively present in all of them.
                 */
            }
            finally
            {
                storesCacheReadLock.unlock();
            }
        }
        // Select the store for writing
        ContentStore store = selectWriteStore(context);
        // Check that we were given a valid store
        if (store == null)
        {
            throw new NullPointerException(
                    "Unable to find a writer.  'selectWriteStore' may not return null: \n" +
                    "   Router: " + this + "\n" +
                    "   Chose:  " + store);
        }
        else if (!store.isWriteSupported())
        {
            throw new AlfrescoRuntimeException(
                    "A write store was chosen that doesn't support writes: \n" +
                    "   Router: " + this + "\n" +
                    "   Chose:  " + store);
        }
        ContentWriter writer = store.getWriter(context);
        // Cache the store against the URL
        storesCacheWriteLock.lock();
        try
        {
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
     * This operation has to be performed on all the stores in order to maintain the
     * {@link ContentStore#exists(String)} contract.
     */
    public boolean delete(String contentUrl) throws ContentIOException
    {
        boolean deleted = true;
        List<ContentStore> stores = getAllStores();
        for (ContentStore store : stores)
        {
            if (store.isWriteSupported())
            {
                deleted &= store.delete(contentUrl);
            }
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
}
