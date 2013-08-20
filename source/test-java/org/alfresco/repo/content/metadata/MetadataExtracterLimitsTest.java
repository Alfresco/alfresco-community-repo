/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.content.metadata;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 * Tests a mock delayed metadata extractor for proper timeout handling.
 *
 * @author Ray Gauss II
 */
public class MetadataExtracterLimitsTest
{
    private MockDelayedMetadataExtracter extracter;

    @Before
    public void setUp() throws Exception
    {
        extracter = new MockDelayedMetadataExtracter(1500);
        extracter.init();
    }

    protected MetadataExtracter getExtracter()
    {
        return extracter;
    }

    protected Map<QName, Serializable> extractFromFile(File sourceFile, String mimetype)
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        // construct a reader onto the source file
        ContentReader sourceReader = new FileContentReader(sourceFile);
        sourceReader.setMimetype(mimetype);
        getExtracter().extract(sourceReader, properties);
        return properties;
    }
    
    /**
     * Tests that delayed metadata extraction completes properly for no mimetype-specific limits.
     *
     * @throws Exception
     */
    @Test
    public void testNoTimeout() throws Exception
    {
        File file = AbstractContentTransformerTest.loadNamedQuickTestFile("quick.txt");

        Map<QName, Serializable> properties = extractFromFile(file, MimetypeMap.MIMETYPE_TEXT_PLAIN);

        assertEquals("value1", properties.get(QName.createQName("http://DummyMappingMetadataExtracter", "a1")));
    }

    /**
     * Tests that delayed metadata extraction times out properly for wildcard mimetype limits.
     *
     * @throws Exception
     */
    @Test
    public void testWildcardTimeout() throws Exception
    {
        long timeoutMs = 1000;

        MetadataExtracterLimits limits = new MetadataExtracterLimits();
        limits.setTimeoutMs(timeoutMs);
        HashMap<String, MetadataExtracterLimits> mimetypeLimits =
                new HashMap<String, MetadataExtracterLimits>(1);
        mimetypeLimits.put("*", limits);
        ((MockDelayedMetadataExtracter) getExtracter()).setMimetypeLimits(mimetypeLimits);

        File file = AbstractContentTransformerTest.loadNamedQuickTestFile("quick.txt");

        long startTime = (new Date()).getTime();
        extractFromFile(file, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        long extractionTime = (new Date()).getTime() - startTime;

        assertTrue("Metadata extraction took (" + extractionTime + "ms) " +
                "but should have failed with a timeout at " + timeoutMs + "ms",
                extractionTime < (timeoutMs + 50)); // bit of wiggle room for logging, cleanup, etc.
    }
    
    /**
     * Tests that delayed metadata extraction times out properly for mimetype-specific limits.
     *
     * @throws Exception
     */
    @Test
    public void testMimetypeSpecificTimeout() throws Exception
    {
        long timeoutMs = 1000;

        MetadataExtracterLimits limits = new MetadataExtracterLimits();
        limits.setTimeoutMs(timeoutMs);
        HashMap<String, MetadataExtracterLimits> mimetypeLimits =
                new HashMap<String, MetadataExtracterLimits>(1);
        mimetypeLimits.put(MimetypeMap.MIMETYPE_TEXT_PLAIN, limits);
        ((MockDelayedMetadataExtracter) getExtracter()).setMimetypeLimits(mimetypeLimits);

        File file = AbstractContentTransformerTest.loadNamedQuickTestFile("quick.txt");

        long startTime = (new Date()).getTime();
        extractFromFile(file, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        long extractionTime = (new Date()).getTime() - startTime;

        assertTrue("Metadata extraction took (" + extractionTime + "ms) " +
                "but should have failed with a timeout at " + timeoutMs + "ms",
                extractionTime < (timeoutMs + 50)); // bit of wiggle room for logging, cleanup, etc.
    }
    
    /**
     * Tests that delayed metadata extraction stops gracefully when interrupted.
     *
     * @throws Exception
     */
    @Test
    public void testInterrupted() throws Exception
    {
        long timeoutMs = 5000;
        long interruptMs = 500;

        MetadataExtracterLimits limits = new MetadataExtracterLimits();
        limits.setTimeoutMs(timeoutMs);
        HashMap<String, MetadataExtracterLimits> mimetypeLimits =
                new HashMap<String, MetadataExtracterLimits>(1);
        mimetypeLimits.put(MimetypeMap.MIMETYPE_TEXT_PLAIN, limits);
        ((MockDelayedMetadataExtracter) getExtracter()).setMimetypeLimits(mimetypeLimits);

        final File file = AbstractContentTransformerTest.loadNamedQuickTestFile("quick.txt");

        long startTime = (new Date()).getTime();
        
        Thread extractThread = new Thread(new Runnable()
        {
            public void run()
            {
                extractFromFile(file, MimetypeMap.MIMETYPE_TEXT_PLAIN);
            }
        });
        extractThread.start();
        Thread.sleep(interruptMs);
        extractThread.interrupt();
        long extractionTime = (new Date()).getTime() - startTime;

        assertTrue("Metadata extraction took (" + extractionTime + "ms) " +
                "but should have been interrupted at " + interruptMs + "ms",
                extractionTime < (interruptMs + 50)); // bit of wiggle room for logging, cleanup, etc.
    }
    
    /**
     * Tests that delayed metadata extraction completes properly for unmatched mimetype-specific limits.
     *
     * @throws Exception
     */
    @Test
    public void testUnmatchedMimetypeSpecificTimeout() throws Exception
    {
        long timeoutMs = 1000;

        MetadataExtracterLimits limits = new MetadataExtracterLimits();
        limits.setTimeoutMs(timeoutMs);
        HashMap<String, MetadataExtracterLimits> mimetypeLimits =
                new HashMap<String, MetadataExtracterLimits>(1);
        mimetypeLimits.put(MimetypeMap.MIMETYPE_IMAGE_JPEG, limits);
        ((MockDelayedMetadataExtracter) getExtracter()).setMimetypeLimits(mimetypeLimits);

        File file = AbstractContentTransformerTest.loadNamedQuickTestFile("quick.txt");

        Map<QName, Serializable> properties = extractFromFile(file, MimetypeMap.MIMETYPE_TEXT_PLAIN);

        assertEquals("value1", properties.get(QName.createQName("http://DummyMappingMetadataExtracter", "a1")));
    }
    
    /**
     * Tests that delayed metadata extraction completes properly for unlimited timeout.
     *
     * @throws Exception
     */
    @Test
    public void testUnlimitedTimeout() throws Exception
    {
        long timeoutMs = -1;

        MetadataExtracterLimits limits = new MetadataExtracterLimits();
        limits.setTimeoutMs(timeoutMs);
        HashMap<String, MetadataExtracterLimits> mimetypeLimits =
                new HashMap<String, MetadataExtracterLimits>(1);
        mimetypeLimits.put(MimetypeMap.MIMETYPE_IMAGE_JPEG, limits);
        ((MockDelayedMetadataExtracter) getExtracter()).setMimetypeLimits(mimetypeLimits);

        File file = AbstractContentTransformerTest.loadNamedQuickTestFile("quick.txt");

        Map<QName, Serializable> properties = extractFromFile(file, MimetypeMap.MIMETYPE_TEXT_PLAIN);

        assertEquals("value1", properties.get(QName.createQName("http://DummyMappingMetadataExtracter", "a1")));
    }

    /**
     * Mock metadata extracter that simply delays by the time specified in
     * its constructor and returns default properties regardless of the content
     * reader its exctracting from.
     */
    private class MockDelayedMetadataExtracter extends AbstractMappingMetadataExtracter
    {
        private long delay;

        public MockDelayedMetadataExtracter(long delay)
        {
            this.delay = delay;
        }

        @Override
        public boolean isSupported(String sourceMimetype)
        {
            return true;
        }

        @Override
        protected Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
        {
            Map<String, Serializable> properties = new HashMap<String, Serializable>(10);
            long startTime = (new Date()).getTime();
            boolean done = false;
            int i = 0;
            try
            {
                while(!done)
                {
                    Thread.sleep(50); // working hard
                    long extractTime = (new Date()).getTime() - startTime;
                    properties.put("key" + i, extractTime);
                    i++;
                    done = extractTime > delay;
                }
                properties.put("a", "value1");
            }
            catch (InterruptedException e)
            {
                // Asked to stop
                return null;
            }
            return properties;
        }
    }
}
