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
    private ContentStore backingStore;
    private ContentCache cache;
    private boolean cacheOnInbound;
    

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
        if (!cache.contains(contentUrl))
        {
            ContentReader bsReader = backingStore.getReader(contentUrl);
            if (!cache.put(contentUrl, bsReader))
            {
                // Content wasn't put into cache successfully.
                return bsReader.getReader();
            }
        }
        
        // TODO: what if, in the meantime this item has been deleted from the disk cache?
        return cache.getReader(contentUrl);
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
        if (cache.contains(contentUrl))
            cache.remove(contentUrl);
        
        return backingStore.delete(contentUrl);
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
