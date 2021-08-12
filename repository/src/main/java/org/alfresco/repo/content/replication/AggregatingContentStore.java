/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.content.replication;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.AbstractContentStore;
import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.UnsupportedContentUrlException;
import org.alfresco.repo.content.caching.CachingContentStore;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.DirectAccessUrl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <h1><u>Aggregating Content Store</u></h1>
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
    private static final Log logger = LogFactory.getLog(AggregatingContentStore.class);
    
    private ContentStore primaryStore;
    private List<ContentStore> secondaryStores;
    
    private Lock readLock;

    /**
     * Default constructor 
     */
    public AggregatingContentStore()
    {
        ReadWriteLock storeLock = new ReentrantReadWriteLock();
        readLock = storeLock.readLock();
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
     * @param secondaryStores a list of stores to replicate to or from
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

    public boolean exists(String contentUrl)
    {
        if (primaryStore == null)
        {
            throw new AlfrescoRuntimeException("ReplicatingContentStore not initialised");
        }

        // get a read lock so that we are sure that no replication is underway
        readLock.lock();
        try
        {
            // Keep track of the unsupported state of the content URL - it might be a rubbish URL
            boolean contentUrlSupported = false;

            boolean contentUrlExists = false;

            // Check the primary store
            try
            {
                contentUrlExists = primaryStore.exists(contentUrl);

                // At least the content URL was supported
                contentUrlSupported = true;
            }
            catch (UnsupportedContentUrlException e)
            {
                // The store can't handle the content URL
            }

            if (contentUrlExists)
            {
                return true;
            }

            // the content is not in the primary store so we have to go looking for it
            for (ContentStore store : secondaryStores)
            {
                contentUrlExists = false;
                try
                {
                    contentUrlExists = store.exists(contentUrl);

                    // At least the content URL was supported
                    contentUrlSupported = true;
                }
                catch (UnsupportedContentUrlException e)
                {
                    // The store can't handle the content URL
                }

                if (contentUrlExists)
                {
                    break;
                }
            }

            // Check if the content URL was supported
            if (!contentUrlSupported)
            {
                throw new UnsupportedContentUrlException(this, contentUrl);
            }

            return contentUrlExists;
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
     * @return Returns {@code true} if at least one store supports direct access URLs
     */
    public boolean isContentDirectUrlEnabled()
    {
        // Check the primary store
        boolean isContentDirectUrlEnabled = primaryStore.isContentDirectUrlEnabled();

        if (!isContentDirectUrlEnabled)
        {
            // Direct access is not supported by the primary store so we have to check the
            // other stores
            for (ContentStore store : secondaryStores)
            {
                isContentDirectUrlEnabled = store.isContentDirectUrlEnabled();

                if (isContentDirectUrlEnabled)
                {
                    break;
                }
            }
        }

        return isContentDirectUrlEnabled;
    }

    /**
     * @return Returns {@code true} if at least one store supports direct access URL for node
     */
    public boolean isContentDirectUrlEnabled(NodeRef nodeRef)
    {
        // Check the primary store
        boolean isContentDirectUrlEnabled = primaryStore.isContentDirectUrlEnabled(nodeRef);

        if (!isContentDirectUrlEnabled)
        {
            // Direct access is not supported by the primary store so we have to check the
            // other stores
            for (ContentStore store : secondaryStores)
            {
                isContentDirectUrlEnabled = store.isContentDirectUrlEnabled(nodeRef);

                if (isContentDirectUrlEnabled)
                {
                    break;
                }
            }
        }

        return isContentDirectUrlEnabled;
    }

    public DirectAccessUrl requestContentDirectUrl(String contentUrl, boolean attachment, String fileName, Long validFor)
    {
        if (primaryStore == null)
        {
            throw new AlfrescoRuntimeException("ReplicatingContentStore not initialised");
        }

        // get a read lock so that we are sure that no replication is underway
        readLock.lock();
        try
        {
            // Keep track of the unsupported state of the content URL - it might be a rubbish URL
            boolean contentUrlSupported = true;
            boolean directAccessUrlSupported = true;

            DirectAccessUrl directAccessUrl = null;

            // Check the primary store
            try
            {
                directAccessUrl = primaryStore.requestContentDirectUrl(contentUrl, attachment, fileName, validFor);
            }
            catch (UnsupportedOperationException e)
            {
                // The store does not support direct access URL
                directAccessUrlSupported = false;
            }
            catch (UnsupportedContentUrlException e)
            {
                // The store can't handle the content URL
                contentUrlSupported = false;
            }

            if (directAccessUrl != null)
            {
                return directAccessUrl;
            }

            // the content is not in the primary store so we have to go looking for it
            for (ContentStore store : secondaryStores)
            {
                try
                {
                    directAccessUrl = store.requestContentDirectUrl(contentUrl, attachment, fileName, validFor);
                }
                catch (UnsupportedOperationException e)
                {
                    // The store does not support direct access URL
                    directAccessUrlSupported = false;
                }
                catch (UnsupportedContentUrlException e)
                {
                    // The store can't handle the content URL
                    contentUrlSupported = false;
                }

                if (directAccessUrl != null)
                {
                    break;
                }
            }

            if (directAccessUrl == null)
            {
                if (!directAccessUrlSupported)
                {
                    // The direct access URL was not supported
                    throw new UnsupportedOperationException("Retrieving direct access URLs is not supported by this content store.");
                }
                else if (!contentUrlSupported)
                {
                    // The content URL was not supported
                    throw new UnsupportedContentUrlException(this, contentUrl);
                }
            }

            return directAccessUrl;
        }
        finally
        {
            readLock.unlock();
        }
    }
}
