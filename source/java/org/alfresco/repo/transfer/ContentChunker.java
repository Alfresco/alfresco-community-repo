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

import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.transfer.TransferException;

/**
 * The Content Chunker Splits Content into "Chunks" of a given size.
 * 
 * So, for example, if the chunk size is 10MB and there are 6 files of 2MB then 
 * there will be one chunk containing 5 chunks and the remaining 2MB will remain.
 * <p>
 * Call the addContent method to add ContentData to the chunker.
 * <p>
 * Call the setHandler method to set the handler to process chunks of content.
 * <p>
 * Call the flush() method after the last call to addContent to flush the remaining 
 * buffered content.
 * @author Mark
 */
public interface ContentChunker
{
    /**
     * add content data to the chunker
     */
    public void addContent(ContentData data) throws TransferException;
    
    /**
     * flush any remaining content data
     */
    public void flush() throws TransferException;

    /**
     * 
     * @param chunkSize
     */
    public void setChunkSize(long chunkSize);

    /**
     * 
     * @return
     */
    public long getChunkSize();

    /**
     * 
     * @param handler
     */
    public void setHandler(ContentChunkProcessor handler);

}
