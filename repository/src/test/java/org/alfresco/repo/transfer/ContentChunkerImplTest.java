/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.transfer;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.alfresco.service.cmr.repository.ContentData;

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

        final Set<ContentData> processedContent = new HashSet<ContentData>();

        chunker.setHandler(
                new ContentChunkProcessor() {
                    public void processChunk(Set<ContentData> data)
                    {
                        processedContent.addAll(data);
                    }
                });

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
        for (int i = 0; i < 11; i++)
        {
            chunker.addContent(new ContentData("url" + i, null, 1, null));
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
        for (int i = 0; i < 100; i++)
        {
            chunker.addContent(new ContentData("url" + i, null, 3, null));
        }
        chunker.flush();
        assertEquals("size 100 not written", processedContent.size(), 100);

    }
}
