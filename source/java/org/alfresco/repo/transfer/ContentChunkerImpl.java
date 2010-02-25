/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.transfer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.alfresco.repo.site.SiteServiceImpl;
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
        Iterator iter = buffer.iterator();
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
