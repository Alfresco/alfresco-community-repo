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

package org.alfresco.repo.content.caching.test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.alfresco.repo.content.AbstractContentReader;
import org.alfresco.repo.content.AbstractContentStore;
import org.alfresco.repo.content.AbstractContentWriter;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;

/**
 * Package-private class - only for testing the CachingContentStore.
 * 
 * @author Matt Ward
 */
class SlowContentStore extends AbstractContentStore
{
    private ConcurrentMap<String, AtomicLong> urlHits = new ConcurrentHashMap<String, AtomicLong>();
    
    /*
     * @see org.alfresco.repo.content.ContentStore#isWriteSupported()
     */
    @Override
    public boolean isWriteSupported()
    {
        return true;
    }

    /*
     * @see org.alfresco.repo.content.ContentStore#getReader(java.lang.String)
     */
    @Override
    public ContentReader getReader(String contentUrl)
    {
        urlHits.putIfAbsent(contentUrl, new AtomicLong(0));
        urlHits.get(contentUrl).incrementAndGet();
        
        return new SlowReader(contentUrl);
    }

    @Override
    protected ContentWriter getWriterInternal(ContentReader existingContentReader, String newContentUrl)
    {
        if (newContentUrl == null)
            newContentUrl = FileContentStore.createNewFileStoreUrl() + ".slow";
        
        return new SlowWriter(newContentUrl, existingContentReader);
    }
    
    
    @Override
    public boolean exists(String contentUrl)
    {
        return false;
    }

    private class SlowWriter extends AbstractContentWriter
    {
        protected SlowWriter(String contentUrl, ContentReader existingContentReader)
        {
            super(contentUrl, existingContentReader);
        }

        @Override
        public long getSize()
        {
            return 20;
        }

        @Override
        protected ContentReader createReader() throws ContentIOException
        {
            return new SlowReader(getContentUrl());
        }

        @Override
        protected WritableByteChannel getDirectWritableChannel() throws ContentIOException
        {
            return new WritableByteChannel()
            {
                private boolean closed = false;
                private int left = 200;
                
                @Override
                public boolean isOpen()
                {
                    return !closed;
                }
                
                @Override
                public void close() throws IOException
                {
                    closed = true;
                }
                
                @Override
                public int write(ByteBuffer src) throws IOException
                {
                    try
                    {
                        Thread.sleep(50);
                    }
                    catch (InterruptedException error)
                    {
                        throw new RuntimeException(error);
                    }
                    
                    if (left > 0)
                    {                        
                        src.get();
                        left--;
                        return 1;
                    }
                    return 0;
                }
            };
        }
        
    }
    
   
    private class SlowReader extends AbstractContentReader
    {
        protected SlowReader(String contentUrl)
        {
            super(contentUrl);
        }

        @Override
        public boolean exists()
        {
            return true;
        }

        @Override
        public long getLastModified()
        {
            return 0L;
        }

        @Override
        public long getSize()
        {
            return 20;
        }

        @Override
        protected ContentReader createReader() throws ContentIOException
        {
            return new SlowReader(getContentUrl());
        }

        @Override
        protected ReadableByteChannel getDirectReadableChannel() throws ContentIOException
        {
            return new ReadableByteChannel()
            {
                private final byte[] content = "This is the content for my slow ReadableByteChannel".getBytes();
                private int index = 0;
                private boolean closed = false;
                
                @Override
                public boolean isOpen()
                {
                    return !closed;
                }
                
                @Override
                public void close() throws IOException
                {
                    closed = true;
                }
                
                @Override
                public int read(ByteBuffer dst) throws IOException
                {
                    if (index < content.length)
                    {
                        try
                        {
                            Thread.sleep(50);
                        }
                        catch (InterruptedException error)
                        {
                            throw new RuntimeException(error);
                        }
                        dst.put(content[index++]);
                        return 1;
                    }
                    else
                    {
                        return 0;
                    }
                }
            };
        }
        
    }
    
    /**
     * Get statistics for which URLs have been asked for and the frequencies.
     * 
     * @return Map of URL to frequency
     */
    public ConcurrentMap<String, AtomicLong> getUrlHits()
    {
        return this.urlHits;
    }

    public static void main(String[] args)
    {
        SlowContentStore scs = new SlowContentStore();
        
        ContentReader reader = scs.getReader("store://something/bin");
        String content = reader.getContentString();
        System.out.println("Content: " + content);
    }
}
