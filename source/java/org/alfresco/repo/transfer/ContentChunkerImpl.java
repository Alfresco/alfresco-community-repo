/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
package org.alfresco.repo.transfer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.transfer.TransferException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Content Chunker Splits Content into "Chunks" of a given size.
 *
 * @author Mark
 */
public class ContentChunkerImpl implements ContentChunker
{
    private static Log logger = LogFactory.getLog(ContentChunkerImpl.class);
    
    /**
     * The chunk size.
     */
    private long chunkSize = 1000000;
    
    /**
     * The handler to recieve the "chunks"
     */
    private ContentChunkProcessor handler;
    
    /**
     * The internal buffer
     */
    private Set<ContentData> buffer = new HashSet<ContentData>();
    
    /**
     * 
     */
    public void addContent(ContentData data) throws TransferException
    {
        logger.debug("add content size:" + data.getSize());
        buffer.add(data);
        
        /**
         * work out whether the buffer has filled up and needs to be flushed
         */
        Iterator<ContentData> iter = buffer.iterator();      
        long totalContentSize = 0;
        
        while (iter.hasNext())
        {
            ContentData x = (ContentData)iter.next();
            totalContentSize += x.getSize();
        }
        if(logger.isDebugEnabled())
        {
            logger.debug("elements " + buffer.size() + ", totalContentSize:" + totalContentSize);
        }
        if(totalContentSize >= chunkSize)
        {
            flush();
        }
    }
    
    /**
     * 
     */
    public void flush() throws TransferException
    {
        logger.debug("flush number of contents:" + buffer.size());
        if(buffer.size() > 0)
        {
            handler.processChunk(buffer);
        }
        buffer.clear();
        logger.debug("buffer empty");
    }

    public void setChunkSize(long chunkSize)
    {
        this.chunkSize = chunkSize;
    }

    public long getChunkSize()
    {
        return chunkSize;
    }

    public void setHandler(ContentChunkProcessor handler)
    {
        this.handler = handler;
    }

    public ContentChunkProcessor getHandler()
    {
        return handler;
    }
}
