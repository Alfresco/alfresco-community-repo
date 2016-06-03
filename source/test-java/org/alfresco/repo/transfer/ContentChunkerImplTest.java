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
        for(int i = 0; i < 100 ; i++)
        {
            chunker.addContent(new ContentData("url" + i, null, 3, null));
        }
        chunker.flush();
        assertEquals("size 100 not written", processedContent.size(), 100);
               
    }
}
