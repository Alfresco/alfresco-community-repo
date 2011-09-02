/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.content.caching;

import java.util.Date;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.springframework.beans.factory.annotation.Required;

/**
 * Implementation of ContentStore that wraps any other ContentStore (the backing store)
 * transparently providing caching of content in that backing store.
 * <p>
 * CachingContentStore should only be used to wrap content stores that are significantly
 * slower that FileContentStore - otherwise performance may actually degrade from its use.
 * <p>
 * It is important that cacheOnInbound is set to true for exceptionally slow backing stores,
 * e.g. {@link org.alfresco.enterprise.repo.content.xam.XAMContentStore}
 * 
 * @author Matt Ward
 */
public class CachingContentStore implements ContentStore
{
    // NUM_LOCKS absolutely must be a power of 2 for the use of locks to be evenly balanced
    private final static int numLocks = 32;
    private final static ReentrantReadWriteLock[] locks; 
    private ContentStore backingStore;
    private ContentCache cache;
    private boolean cacheOnInbound;
    private int maxCacheTries = 2;
    
    static
    {
        locks = new ReentrantReadWriteLock[numLocks];
        for (int i = 0; i < numLocks; i++)
        {
            locks[i] = new ReentrantReadWriteLock();
        }
    }

    public CachingContentStore()
    {
    }
    
    public CachingContentStore(ContentStore backingStore, ContentCache cache, boolean cacheOnInbound)
    {
        this.backingStore = backingStore;
        this.cache = cache;
        this.cacheOnInbound = cacheOnInbound;
    }

    
    /*
     * @see org.alfresco.repo.content.ContentStore#isContentUrlSupported(java.lang.String)
     */
    @Override
    public boolean isContentUrlSupported(String contentUrl)
    {
        return backingStore.isContentUrlSupported(contentUrl);
    }

    /*
     * @see org.alfresco.repo.content.ContentStore#isWriteSupported()
     */
    @Override
    public boolean isWriteSupported()
    {
        return backingStore.isWriteSupported();
    }

    /*
     * @see org.alfresco.repo.content.ContentStore#getTotalSize()
     */
    @Override
    public long getTotalSize()
    {
        return backingStore.getTotalSize();
    }

    /*
     * @see org.alfresco.repo.content.ContentStore#getSpaceUsed()
     */
    @Override
    public long getSpaceUsed()
    {
        return backingStore.getSpaceUsed();
    }

    /*
     * @see org.alfresco.repo.content.ContentStore#getSpaceFree()
     */
    @Override
    public long getSpaceFree()
    {
        return backingStore.getSpaceFree();
    }

    /*
     * @see org.alfresco.repo.content.ContentStore#getSpaceTotal()
     */
    @Override
    public long getSpaceTotal()
    {
        return backingStore.getSpaceTotal();
    }

    /*
     * @see org.alfresco.repo.content.ContentStore#getRootLocation()
     */
    @Override
    public String getRootLocation()
    {
        return backingStore.getRootLocation();
    }

    /*
     * @see org.alfresco.repo.content.ContentStore#exists(java.lang.String)
     */
    @Override
    public boolean exists(String contentUrl)
    {
        return backingStore.exists(contentUrl);
    }

    /*
     * @see org.alfresco.repo.content.ContentStore#getReader(java.lang.String)
     */
    @Override
    public ContentReader getReader(String contentUrl)
    {
        // Use pool of locks - which one is determined by a hash of the URL.
        // This will stop the content from being read/cached multiple times from the backing store
        // when it should only be read once - cached versions should be returned after that.
        ReadLock readLock = readWriteLock(contentUrl).readLock();
        readLock.lock();
        try
        {
            if (cache.contains(contentUrl))
            {
                return cache.getReader(contentUrl);
            }
        }
        catch(CacheMissException e)
        {
            // Fall through to cacheAndRead(url);
        }
        finally
        {
            readLock.unlock();
        }
        
        return cacheAndRead(contentUrl);
    }    
    
    
    private ContentReader cacheAndRead(String url)
    {
        WriteLock writeLock = readWriteLock(url).writeLock();
        writeLock.lock();
        try
        {
            for (int i = 0; i < maxCacheTries; i++)
            {
                ContentReader reader = attemptCacheAndRead(url);
                if (reader != null)
                {
                    return reader;
                }
            }
            // Have tried multiple times to cache the item and read it back from the cache
            // but there is a recurring problem - give up and return the item from the backing store.
            return backingStore.getReader(url);
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    private ContentReader attemptCacheAndRead(String url)
    {
        ContentReader reader = null;
        try
        {
            if (!cache.contains(url))
            {
                if (cache.put(url, backingStore.getReader(url)))
                {
                    reader = cache.getReader(url);
                }
            }
            else
            {
                reader = cache.getReader(url);
            }
        }
        catch(CacheMissException e)
        {
            cache.remove(url);
        }
        
        return reader;
    }
    
    /*
     * @see org.alfresco.repo.content.ContentStore#getWriter(org.alfresco.repo.content.ContentContext)
     */
    @Override
    public ContentWriter getWriter(final ContentContext context)
    {
        if (cacheOnInbound)
        {
            final ContentWriter bsWriter = backingStore.getWriter(context);
                        
            // write to cache
            final ContentWriter cacheWriter = cache.getWriter(bsWriter.getContentUrl());
            
            cacheWriter.addListener(new ContentStreamListener()
            {
                @Override
                public void contentStreamClosed() throws ContentIOException
                {
                    // Finished writing to the cache, so copy to the backing store -
                    // ensuring that the encoding attributes are set to the same as for the cache writer.
                    bsWriter.setEncoding(cacheWriter.getEncoding());
                    bsWriter.setLocale(cacheWriter.getLocale());
                    bsWriter.setMimetype(cacheWriter.getMimetype());
                    bsWriter.putContent(cacheWriter.getReader());
                }
            });
            
            return cacheWriter;
        }
        else
        {
            // No need to invalidate the cache for this content URL, since a content URL
            // is only ever written to once.
            return backingStore.getWriter(context);
        }
    }

    /*
     * @see org.alfresco.repo.content.ContentStore#getWriter(org.alfresco.service.cmr.repository.ContentReader, java.lang.String)
     */
    @Override
    public ContentWriter getWriter(ContentReader existingContentReader, String newContentUrl)
    {
        ContentContext ctx = new ContentContext(existingContentReader, newContentUrl);
        return getWriter(ctx);
    }

    /*
     * @see org.alfresco.repo.content.ContentStore#getUrls(org.alfresco.repo.content.ContentStore.ContentUrlHandler)
     */
    @Override
    public void getUrls(ContentUrlHandler handler) throws ContentIOException
    {
        backingStore.getUrls(handler);
    }

    /*
     * @see org.alfresco.repo.content.ContentStore#getUrls(java.util.Date, java.util.Date, org.alfresco.repo.content.ContentStore.ContentUrlHandler)
     */
    @Override
    public void getUrls(Date createdAfter, Date createdBefore, ContentUrlHandler handler)
                throws ContentIOException
    {
        backingStore.getUrls(createdAfter, createdBefore, handler);
    }

    /*
     * @see org.alfresco.repo.content.ContentStore#delete(java.lang.String)
     */
    @Override
    public boolean delete(String contentUrl)
    {
        ReentrantReadWriteLock readWriteLock = readWriteLock(contentUrl);
        ReadLock readLock = readWriteLock.readLock();
        readLock.lock();
        try
        {
            if (!cache.contains(contentUrl))
            {
                // The item isn't in the cache, so simply delete from the backing store
                return backingStore.delete(contentUrl);
            }
        }
        finally
        {
            readLock.unlock();
        }
        
        WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try
        {
            // Double check the content still exists in the cache
            if (cache.contains(contentUrl))
            {
                // The item is in the cache, so remove.
                cache.remove(contentUrl);
                
            }
            // Whether the item was in the cache or not, it must still be deleted from the backing store.
            return backingStore.delete(contentUrl);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    /**
     * Get a ReentrantReadWriteLock for a given URL. The lock is from a pool rather than
     * per URL, so some contention is expected.
     *  
     * @param url
     * @return
     */
    public ReentrantReadWriteLock readWriteLock(String url)
    {
        return locks[lockIndex(url)];
    }
    
    private int lockIndex(String url)
    {
        return url.hashCode() & (numLocks - 1);
    }
    
    @Required
    public void setBackingStore(ContentStore backingStore)
    {
        this.backingStore = backingStore;
    }

    @Required
    public void setCache(ContentCache cache)
    {
        this.cache = cache;
    }

    public void setCacheOnInbound(boolean cacheOnInbound)
    {
        this.cacheOnInbound = cacheOnInbound;
    }
}
