/*
 * Copyright (C) 2013-2013 Alfresco Software Limited.
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
package org.alfresco.repo.content.replication;

import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.AbstractContentStore;
import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.caching.CachingContentStore;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * </h1><u>Aggregating Content Store</u></h1>
 * <p>
 * A content store implementation that aggregates a set of stores.  Content is not 
 * persisted by this store, but rather it relies on any number of
 * child {@link org.alfresco.repo.content.ContentStore stores} to provide access 
 * to content readers and writers.
 * <p>
 * The order in which the stores appear in the list of stores participating is
 * important.  The first store in the list is known as the <i>primary store</i>.
   <p>
 * Content is written to the primary store only.  The other stores are
 * only used to retrieve content and the primary store is not updated with
 * the content.
 * 
 * @author Derek Hulley
 * @author Mark Rogers
 * @see CachingContentStore
 */
public class AggregatingContentStore extends AbstractContentStore
{    
    private static Log logger = LogFactory.getLog(AggregatingContentStore.class);
    
    private ContentStore primaryStore;
    private List<ContentStore> secondaryStores;
    
    private Lock readLock;
    private Lock writeLock;

    /**
     * Default constructor 
     */
    public AggregatingContentStore()
    {       
        ReadWriteLock storeLock = new ReentrantReadWriteLock();
        readLock = storeLock.readLock();
        writeLock = storeLock.writeLock();
    }
        
    /**
     * Set the primary store that content will be replicated to or from
     * 
     * @param primaryStore the primary content store
     */
    public void setPrimaryStore(ContentStore primaryStore)
    {
        this.primaryStore = primaryStore;
    }

    /**
     * Set the secondary stores that this component will replicate to or from
     * 
     * @param stores a list of stores to replicate to or from
     */
    public void setSecondaryStores(List<ContentStore> secondaryStores)
    {
        this.secondaryStores = secondaryStores;
    }
    
    /**
     * @return      Returns <tt>true</tt> if the primary store supports writing
     */
    public boolean isWriteSupported()
    {
        return primaryStore.isWriteSupported();
    }

    /**
     * @return      Returns <tt>true</tt> if the primary store supports the URL
     */
    @Override
    public boolean isContentUrlSupported(String contentUrl)
    {
        return primaryStore.isContentUrlSupported(contentUrl);
    }
    
    /**
     * @return      Return the primary store root location
     */
    @Override
    public String getRootLocation()
    {
        return primaryStore.getRootLocation();
    }

    /**
     * Forwards the call directly to the first store in the list of stores.
     */
    public ContentReader getReader(String contentUrl) throws ContentIOException
    {
        if (primaryStore == null)
        {
            throw new AlfrescoRuntimeException("ReplicatingContentStore not initialised");
        }
        
        // get a read lock so that we are sure that no replication is underway
        readLock.lock();
        try
        {
            // get a reader from the primary store
            ContentReader primaryReader = primaryStore.getReader(contentUrl);
            
            // give it straight back if the content is there
            if (primaryReader.exists())
            {
                return primaryReader;
            }

            // the content is not in the primary reader so we have to go looking for it
            ContentReader secondaryContentReader = null;
            for (ContentStore store : secondaryStores)
            {
                ContentReader reader = store.getReader(contentUrl);
                if (reader.exists())
                {
                    // found the content in a secondary store
                    return reader;
                }
            }

            return primaryReader;
        }
        finally
        {
            readLock.unlock();
        }     
    }

    public ContentWriter getWriter(ContentContext ctx)
    {
        // get the writer
        ContentWriter writer = primaryStore.getWriter(ctx);
                
        return writer;
    }

    /**
     * Performs a delete on the local store and if outbound replication is on, propogates
     * the delete to the other stores too.
     * 
     * @return Returns the value returned by the delete on the primary store.
     */
    public boolean delete(String contentUrl) throws ContentIOException
    {
        // delete on the primary store
        boolean deleted = primaryStore.delete(contentUrl);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Deleted content for URL: " + contentUrl);
        }
        return deleted;
    }

    /**
     * Iterates over results as given by the primary store and all secondary stores.  It is up to the handler to eliminate
     * duplicates that will occur between the primary and secondary stores.
     */
    @SuppressWarnings("deprecation")
    public void getUrls(Date createdAfter, Date createdBefore, ContentUrlHandler handler) throws ContentIOException
    {
        // add in URLs from primary store
        primaryStore.getUrls(createdAfter, createdBefore, handler);
        
        // add in URLs from secondary stores (they are visible for reads)
        for (ContentStore secondaryStore : secondaryStores)
        {
            secondaryStore.getUrls(createdAfter, createdBefore, handler);
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Iterated over content URLs: \n" +
                    "   created after: " + createdAfter + "\n" +
                    "   created before: " + createdBefore);
        }
    }
}
