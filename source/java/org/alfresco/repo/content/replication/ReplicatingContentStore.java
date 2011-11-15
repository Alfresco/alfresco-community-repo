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
package org.alfresco.repo.content.replication;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.AbstractContentStore;
import org.alfresco.repo.content.AbstractContentStreamListener;
import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentExistsException;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.caching.CachingContentStore;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * </h1><u>Replicating Content Store</u></h1>
 * <p>
 * A content store implementation that is able to replicate content between stores.
 * Content is not persisted by this store, but rather it relies on any number of
 * child {@link org.alfresco.repo.content.ContentStore stores} to provide access to
 * content readers and writers.
 * <p>
 * The order in which the stores appear in the list of stores participating is
 * important.  The first store in the list is known as the <i>primary store</i>.
 * When the replicator goes to fetch content, the stores are searched
 * from first to last.  The stores should therefore be arranged in order of
 * speed.
 * <p>
 * It supports the notion of inbound and/or outbound replication, both of which can be
 * operational at the same time.
 * 
 * </h2><u>Outbound Replication</u></h2>
 * <p>
 * When this is enabled, then the primary store is used for writes.  When the
 * content write completes (i.e. the write channel is closed) then the content
 * is synchronously copied to all other stores.  The write is therefore slowed
 * down, but the content replication will occur <i>in-transaction</i>.
 * <p>
 * The {@link #setOutboundThreadPoolExecutor(boolean) outboundThreadPoolExecutor }
 * property to enable asynchronous replication.<br>
 * With asynchronous replication, there is always a risk that a failure
 * occurs during the replication.  Depending on the configuration of the server,
 * further action may need to be taken to rectify the problem manually.
 *  
 * </h2><u>Inbound Replication</u></h2>
 * <p>
 * This can be used to lazily replicate content onto the primary store.  When
 * content can't be found in the primary store, the other stores are checked
 * in order.  If content is found, then it is copied into the local store
 * before being returned.  Subsequent accesses will use the primary store.<br>
 * This should be used where the secondary stores are much slower, such as in
 * the case of a store against some kind of archival mechanism.
 * 
 * <h2><u>No Replication</u></h2>
 * <p>
 * Content is written to the primary store only.  The other stores are
 * only used to retrieve content and the primary store is not updated with
 * the content.
 * 
 * @author Derek Hulley
 * @see CachingContentStore
 */
public class ReplicatingContentStore extends AbstractContentStore
{
    /*
     * The replication process uses thread synchronization as it can
     * decide to write content to specific URLs during requests for
     * a reader.
     * While this won't help the underlying stores if there are
     * multiple replications on top of them, it will prevent repeated
     * work from multiple threads entering an instance of this component
     * looking for the same content at the same time.
     */
    
    private static Log logger = LogFactory.getLog(ReplicatingContentStore.class);
    
    private RetryingTransactionHelper transactionHelper;
    private ContentStore primaryStore;
    private List<ContentStore> secondaryStores;
    private boolean inbound;
    private boolean outbound;
    private ThreadPoolExecutor outboundThreadPoolExecutor;
    
    private Lock readLock;
    private Lock writeLock;

    /**
     * Default constructor set <code>inbound = false</code> and <code>outbound = true</code>;
     */
    public ReplicatingContentStore()
    {
        inbound = false;
        outbound = true;
        
        ReadWriteLock storeLock = new ReentrantReadWriteLock();
        readLock = storeLock.readLock();
        writeLock = storeLock.writeLock();
    }
    
    /**
     * @deprecated  Replaced with {@link #setRetryingTransactionHelper(RetryingTransactionHelper)}
     */
    public void setTransactionService(TransactionService transactionService)
    {
        logger.warn("Property 'transactionService' has been replaced with 'retryingTransactionHelper'.");
    }

    /**
     * Set the retrying transaction helper.
     * 
     * @since 2.0
     */
    public void setRetryingTransactionHelper(RetryingTransactionHelper helper)
    {
        this.transactionHelper = helper;
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
     * Set whether or not this component should replicate content to the
     * primary store if not found.
     *  
     * @param inbound true to pull content onto the primary store when found
     *      on one of the other stores
     */
    public void setInbound(boolean inbound)
    {
        this.inbound = inbound;
    }
    
    /**
     * Set whether or not this component should replicate content to all stores
     * as it is written.
     *  
     * @param outbound true to enable synchronous replication to all stores
     */
    public void setOutbound(boolean outbound)
    {
        this.outbound = outbound;
    }

    /**
     * Set the thread pool executer
     * 
     * @param outboundThreadPoolExecutor set this to have the synchronization occur in a separate
     *      thread
     */
    public void setOutboundThreadPoolExecutor(ThreadPoolExecutor outboundThreadPoolExecutor)
    {
        this.outboundThreadPoolExecutor = outboundThreadPoolExecutor;
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
     * Forwards the call directly to the first store in the list of stores.
     */
    public ContentReader getReader(String contentUrl) throws ContentIOException
    {
        if (primaryStore == null)
        {
            throw new AlfrescoRuntimeException("ReplicatingContentStore not initialised");
        }
        
        // get a read lock so that we are sure that no replication is underway
        ContentReader existingContentReader = null;
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
                    secondaryContentReader = reader;
                    break;
                }
            }
            // we already know that the primary has nothing
            // drop out if no content was found
            if (secondaryContentReader == null)
            {
                return primaryReader;
            }
            // secondary content was found
            // return it if we are not doing inbound
            if (!inbound)
            {
                return secondaryContentReader;
            }
            
            // we have to replicate inbound
            existingContentReader = secondaryContentReader;
        }
        finally
        {
            readLock.unlock();
        }
        
        // -- a small gap for concurrent threads to get through --
        
        // do inbound replication
        writeLock.lock();
        try
        {
            // double check the primary
            ContentReader primaryContentReader = primaryStore.getReader(contentUrl);
            if (primaryContentReader.exists())
            {
                // we were beaten to it
                return primaryContentReader;
            }
            // get a writer
            ContentContext ctx = new ContentContext(existingContentReader, contentUrl);
            ContentWriter primaryContentWriter = primaryStore.getWriter(ctx);
            // copy it over
            primaryContentWriter.putContent(existingContentReader);
            // get a writer to the new content
            primaryContentReader = primaryContentWriter.getReader();
            // done
            return primaryContentReader;
        }
        finally
        {
            writeLock.unlock();
        }
    }

    public ContentWriter getWriter(ContentContext ctx)
    {
        // get the writer
        ContentWriter writer = primaryStore.getWriter(ctx);
        
        // attach a replicating listener if outbound replication is on
        if (outbound)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Attaching " + (outboundThreadPoolExecutor == null ? "" : "a") + "synchronous " +
                                "replicating listener to local writer: \n" +
                        "   primary store: " + primaryStore + "\n" +
                        "   writer: " + writer);
            }
            // attach the listener
            ReplicatingWriteListener listener = new ReplicatingWriteListener(secondaryStores, writer, outboundThreadPoolExecutor);
            listener.setRetryingTransactionHelper(transactionHelper);   // mandatory when listeners are added
            writer.addListener(listener);
          
        }
        
        // done
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
        
        // propogate outbound deletions
        if (outbound)
        {
            for (ContentStore store : secondaryStores)
            {
                // Ignore read-only stores
                if (!store.isWriteSupported())
                {
                    continue;
                }
                store.delete(contentUrl);
            }
            // log
            if (logger.isDebugEnabled())
            {
                logger.debug("Propagated content delete to " + secondaryStores.size() + " stores:" + contentUrl);
            }
        }
        // done
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

    /**
     * Replicates the content upon stream closure.  If the thread pool is available,
     * then the process will be asynchronous.
     * <p>
    
     * 
     * @author Derek Hulley
     */
    public static class ReplicatingWriteListener extends AbstractContentStreamListener
    {
        private List<ContentStore> stores;
        private ContentWriter writer;
        private ThreadPoolExecutor threadPoolExecutor;
        
        public ReplicatingWriteListener(
                List<ContentStore> stores,
                ContentWriter writer,
                ThreadPoolExecutor threadPoolExecutor)
        {
            this.stores = stores;
            this.writer = writer;
            this.threadPoolExecutor = threadPoolExecutor;
        }
        
        public void contentStreamClosedImpl() throws ContentIOException
        {
            Runnable runnable = new ReplicateOnCloseRunnable();
            if (threadPoolExecutor == null)
            {
                // execute direct
                runnable.run();
            }
            else
            {
                threadPoolExecutor.execute(runnable);
            }
        }
        
        /**
         * Performs the actual replication work.
         * 
         * @author Derek Hulley
         */
        private class ReplicateOnCloseRunnable implements Runnable
        {
            public void run()
            {
                for (int i = 0; i < stores.size(); i++)
                {
                    ContentStore store = stores.get(i);
                    // Bypass read-only stores
                    if (!store.isWriteSupported())
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Ignoring read-only store for content replication: \n" +
                                    "   Content: " + writer + "\n" +
                                    "   Store:   " + store + "\n" +
                                    "   Number:  " + i);
                        }
                        continue;
                    }
                    
                    try
                    {
                        // replicate the content to the store - we know the URL that we want to write to
                        ContentReader reader = writer.getReader();
                        String contentUrl = reader.getContentUrl();
                        // in order to replicate, we have to specify the URL that we are going to write to
                        ContentContext ctx = new ContentContext(null, contentUrl);
                        ContentWriter replicatedWriter = store.getWriter(ctx);
                        // write it
                        replicatedWriter.putContent(reader);
                        
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Replicated content to store: \n" +
                                    "   Content: " + writer + "\n" +
                                    "   Store:   " + store + "\n" +
                                    "   Number:  " + i);
                        }
                    }
                    catch (UnsupportedOperationException e)
                    {
                        throw new ContentIOException(
                                "Unable to replicate content.  The target store doesn't support replication: \n" +
                                "   Content: " + writer + "\n" +
                                "   Store:   " + store + "\n" +
                                "   Number:  " + i,
                                e);
                    }
                    catch (ContentExistsException e)
                    {
                        throw new ContentIOException(
                                "Content replication failed.  " +
                                "The content URL already exists in the target (secondary) store: \n" +
                                "   Content: " + writer + "\n" +
                                "   Store:   " + store + "\n" +
                                "   Number:  " + i);
                    }
                    catch (Throwable e)
                    {
                        throw new ContentIOException(
                                "Content replication failed: \n" +
                                "   Content: " + writer + "\n" +
                                "   Store:   " + store + "\n" +
                                "   Number:  " + i,
                                e);
                    }
                }
            }
        }
    }
}
