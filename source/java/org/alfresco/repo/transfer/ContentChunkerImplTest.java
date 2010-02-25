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
import java.util.Set;

import org.alfresco.service.cmr.repository.ContentData;

import junit.framework.TestCase;

/**
 * Unit test of the Content Chunker
 *
 * @author Mark Rogers
 */
public class ContentChunkerImplTest extends TestCase
{
    public void testContentChunkerImpl() throws Exception
    {
        ContentChunker chunker = new ContentChunkerImpl();
        
        final Set<ContentData>processedContent = new HashSet<ContentData>();
        
        chunker.setHandler(
                new ContentChunkProcessor(){
                public void processChunk(Set<ContentData> data)
                {
                   processedContent.addAll(data);
                }
            }
        );
        
        /**
         * Set a chunk size of 10 bytes
         */
        chunker.setChunkSize(10);
        
        /**
         * add one contet of size 20, should flush immediatly
         */
        chunker.addContent(new ContentData(null, null, 20, null));
        assertTrue("size 20 not written immediatley", processedContent.size() == 1);
        
        /**
         * add one content of size 1 - should remain buffered in chunker
         */
        processedContent.clear();
        chunker.addContent(new ContentData(null, null, 1, null));
        assertTrue("size 1 not buffered", processedContent.size() == 0);
        
        /**
         * flush should write it out
         */
        chunker.flush();
        assertTrue("size 1 not flushed", processedContent.size() == 1);
        
        /**
         * Now test the boundary condition over the threashold
         */
        processedContent.clear();
        for(int i = 0; i < 11 ; i++)
        {
            chunker.addContent(new ContentData(null, null, 1, null));
        }
        assertEquals("size 10 not buffered", processedContent.size(), 10);
        
        /**
         * flush should write it out
         */
        chunker.flush();
        assertTrue("size 1 not flushed", processedContent.size() == 11);
        
        
        /**
         * Now Just whack some load through
         */
        processedContent.clear();
        for(int i = 0; i < 100 ; i++)
        {
            chunker.addContent(new ContentData(null, null, 3, null));
        }
        chunker.flush();
        assertEquals("size 100 not written", processedContent.size(), 100);
               
    }
}
