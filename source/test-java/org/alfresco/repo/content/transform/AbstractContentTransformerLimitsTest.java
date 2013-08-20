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
package org.alfresco.repo.content.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.content.AbstractContentReader;
import org.alfresco.repo.content.ContentMinimalContextTestSuite;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Test methods that control limits in {@link AbstractContentTransformerLimits}
 */
public class AbstractContentTransformerLimitsTest
{
    private static final String A = MimetypeMap.MIMETYPE_XML;
    private static final String B = MimetypeMap.MIMETYPE_HTML;
    private static final String C = MimetypeMap.MIMETYPE_PDF;
    
    private AbstractContentTransformerLimits transformer;
    private TransformationOptionLimits limits;
    private Map<String, Map<String, TransformationOptionLimits>> mimetypeLimits;
    private TransformationOptions options;
    
    @Before
    public void setUp() throws Exception
    {
        ApplicationContext ctx = ContentMinimalContextTestSuite.getContext();
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        MimetypeService mimetypeService = serviceRegistry.getMimetypeService();
        TransformerDebug transformerDebug = (TransformerDebug) ctx.getBean("transformerDebug");
        TransformerConfig transformerConfig = (TransformerConfig) ctx.getBean("transformerConfig");

        transformer = new AbstractContentTransformer2()
        {
            @Override
            public boolean isTransformableMimetype(String sourceMimetype, String targetMimetype,
                    TransformationOptions options)
            {
                return false;
            }

            @Override
            protected void transformInternal(ContentReader reader, ContentWriter writer,
                    TransformationOptions options) throws Exception
            {
            }
        };
        transformer.setMimetypeService(mimetypeService);
        transformer.setTransformerDebug(transformerDebug);
        transformer.setTransformerConfig(transformerConfig);
        transformer.setBeanName("transformer.test"+System.currentTimeMillis()%100000);
        
        limits = new TransformationOptionLimits();
        options = new TransformationOptions();
    }
    
    private void addMimetypeLimits(String source, String target, TransformationOptionLimits limits)
    {
        if (mimetypeLimits == null)
        {
            mimetypeLimits = new HashMap<String, Map<String, TransformationOptionLimits>>();
        }
        
        Map<String, TransformationOptionLimits> targetLimits = mimetypeLimits.get(source);
        if (targetLimits == null)
        {
            targetLimits = new HashMap<String, TransformationOptionLimits>();
            mimetypeLimits.put(source, targetLimits);
        }
        
        targetLimits.put(target, limits);
    }

    @Test
    public void testTimeoutMs() throws Exception
    {
        long value = 1234;
        transformer.setTimeoutMs(value);
        transformer.register();
        long actual = transformer.getTimeoutMs();
        assertEquals("Getter did not return set value", value, actual);
    }

    @Test
    public void testReadLimitTimeMs() throws Exception
    {
        long value = 1234;
        transformer.setReadLimitTimeMs(value);
        transformer.register();
        long actual = transformer.getReadLimitTimeMs();
        assertEquals("Getter did not return set value", value, actual);
    }

    @Test
    public void testMaxSourceSizeKBytes() throws Exception
    {
        long value = 1234;
        transformer.setMaxSourceSizeKBytes(value);
        transformer.register();
        long actual = transformer.getMaxSourceSizeKBytes();
        assertEquals("Getter did not return set value", value, actual);
    }

    @Test
    public void testReadLimitKBytes() throws Exception
    {
        long value = 1234;
        transformer.setReadLimitKBytes(value);
        transformer.register();
        long actual = transformer.getReadLimitKBytes();
        assertEquals("Getter did not return set value", value, actual);
    }

    @Test
    public void testMaxPages() throws Exception
    {
        int value = 1234;
        transformer.setMaxPages(value);
        transformer.register();
        int actual = transformer.getMaxPages();
        assertEquals("Getter did not return set value", value, actual);
    }

    @Test
    public void testPageLimit() throws Exception
    {
        int value = 1234;
        transformer.setPageLimit(value);
        transformer.register();
        int actual = transformer.getPageLimit();
        assertEquals("Getter did not return set value", value, actual);
    }

    @Test
    public void testMimetypeLimit() throws Exception
    {
        long value = 1234;
        limits.setMaxSourceSizeKBytes(value);
        addMimetypeLimits(A, B, limits);

        transformer.setMimetypeLimits(mimetypeLimits);
        transformer.register();
        long actual = transformer.getLimits(A, B, options).getMaxSourceSizeKBytes();
        assertEquals("Getter did not return set value", value, actual);

        actual = transformer.getLimits(A, C, options).getMaxSourceSizeKBytes();
        assertEquals("Other values should not be set", -1, actual);
    }

    @Test
    public void testMimetypeLimitTargetWildcard() throws Exception
    {
        long value = 1234;
        limits.setMaxSourceSizeKBytes(value);
        addMimetypeLimits(A, "*", limits);

        transformer.setMimetypeLimits(mimetypeLimits);
        transformer.register();
        long actual = transformer.getLimits(A, B, options).getMaxSourceSizeKBytes();
        assertEquals("Getter did not return set value", value, actual);

        actual = transformer.getLimits(B, A, options).getMaxSourceSizeKBytes();
        assertEquals("Other values should not be set", -1, actual);
    }

    @Test
    public void testMimetypeLimitSourceWildcard() throws Exception
    {
        long value = 1234;
        limits.setMaxSourceSizeKBytes(value);
        addMimetypeLimits("*", B, limits);

        transformer.setMimetypeLimits(mimetypeLimits);
        transformer.register();
        long actual = transformer.getLimits(A, B, options).getMaxSourceSizeKBytes();
        assertEquals("Getter did not return set value", value, actual);

        actual = transformer.getLimits(B, A, options).getMaxSourceSizeKBytes();
        assertEquals("Other values should not be set", -1, actual);
    }
    
    @Test
    public void testPassedInOptions() throws Exception
    {
        long value = 1234;
        limits.setMaxSourceSizeKBytes(value+1);
        addMimetypeLimits(A, B, limits);

        transformer.setMimetypeLimits(mimetypeLimits);
        transformer.register();
        long actual = transformer.getLimits(A, B, options).getMaxSourceSizeKBytes();
        assertEquals("Getter did not return set value", value+1, actual);

        options.setMaxSourceSizeKBytes(value);
        actual = transformer.getLimits(A, B, options).getMaxSourceSizeKBytes();
        assertEquals("Getter did not return set value", value, actual);
    }

    @Test
    public void testGetMaxSourceSizeKBytes() throws Exception
    {
        long kValue = 12;
        long byteValue = kValue*1024;

        // Not set mimetype limits yet
        assertTrue("No limits so should have been ok",
                transformer.isTransformableSize(A, byteValue+1, B, options));

        // Set limit for A to B mimetypes and test
        limits.setMaxSourceSizeKBytes(kValue);
        addMimetypeLimits(A, B, limits);
        transformer.setMimetypeLimits(mimetypeLimits);
        transformer.register();

        assertEquals("Expected to have set value returned", kValue,
                transformer.getMaxSourceSizeKBytes(A, B, options));

        // With a mimetype that does not have any specific limits
        assertEquals("Expected to have -1 (unlimited) returned", -1,
                transformer.getMaxSourceSizeKBytes(C, B, options));
    }
    
    @Test
    // Combination using just transformer limit to start with
    public void testGetMaxSourceSizeKBytesCombination() throws Exception
    {
        long kValue = 12;
        long byteValue = kValue*1024;

        // Not set mimetype limits yet
        assertTrue("No limits so should have been ok",
                transformer.isTransformableSize(A, byteValue+1, B, options));

        // Check for combinations with transformer limits
        
        transformer.setMaxSourceSizeKBytes(kValue);
        transformer.register();
        assertEquals("Expected to have transformer set value returned", kValue,
                transformer.getMaxSourceSizeKBytes(A, B, options));
    }
    
    @Test
    // Combination where mimetype limit is used
    public void testGetMaxSourceSizeKBytesCombinationMimetypeUsed() throws Exception
    {
        long kValue = 12;
        long byteValue = kValue*1024;

        // Not set mimetype limits yet
        assertTrue("No limits so should have been ok",
                transformer.isTransformableSize(A, byteValue+1, B, options));

        transformer.setMaxSourceSizeKBytes(kValue+1);
        limits.setMaxSourceSizeKBytes(kValue);
        addMimetypeLimits(A, B, limits);
        transformer.setMimetypeLimits(mimetypeLimits);
        transformer.register();
        assertEquals("Expected to have transformer set value returned", kValue,
                transformer.getMaxSourceSizeKBytes(A, B, options));
    }

    @Test
    // Check no limit when page limit set on a transformer that does not support page limit
    // maxSourceSizeKbytes value should be ignored if a page limit is in use
    public void testGetMaxSourceSizeKBytesPageSupportsNot() throws Exception
    {
        long kValue = 12;
        long byteValue = kValue*1024;

        // Not set mimetype limits yet
        assertTrue("No limits so should have been ok",
                transformer.isTransformableSize(A, byteValue+1, B, options));

        transformer.setPageLimitsSupported(false);
        transformer.setMaxSourceSizeKBytes(kValue);
        limits.setMaxSourceSizeKBytes(kValue+1);
        limits.setPageLimit(1);
        addMimetypeLimits(A, B, limits);
        transformer.setMimetypeLimits(mimetypeLimits);
        transformer.register();
        assertEquals("Expected to ignore the page limit as the transformer does not support it", kValue+1,
                transformer.getMaxSourceSizeKBytes(A, B, options));
    }
    
    @Test
    // Check no limit when page limit set on a transformer that does support page limit
    // maxSourceSizeKbytes value should be ignored if a page limit is in use
    public void testGetMaxSourceSizeKBytesPageSupports() throws Exception
    {
        long kValue = 12;
        long byteValue = kValue*1024;

        // Not set mimetype limits yet
        assertTrue("No limits so should have been ok",
                transformer.isTransformableSize(A, byteValue+1, B, options));

        transformer.setPageLimitsSupported(true);
        transformer.setMaxSourceSizeKBytes(kValue);
        limits.setMaxSourceSizeKBytes(kValue+1);
        limits.setPageLimit(1);
        addMimetypeLimits(A, B, limits);
        transformer.setMimetypeLimits(mimetypeLimits);
        transformer.register();
        assertEquals("Expected to have -1 (unlimited) returned when there are page limits", -1,
                transformer.getMaxSourceSizeKBytes(A, B, options));
    }

    @Test
    // Using limit on a mimetype
    public void testIsTransformableSizeMimetype() throws Exception
    {
        long kValue = 12;
        long byteValue = kValue*1024;

        // Not set mimetype limits yet
        assertTrue("No limits so should have been ok",
                transformer.isTransformableSize(A, byteValue+1, B, options));

        // Set limit for A to B mimetypes and test
        limits.setMaxSourceSizeKBytes(kValue);
        addMimetypeLimits(A, B, limits);
        transformer.setMimetypeLimits(mimetypeLimits);
        transformer.register();

        assertTrue("Size is less than limit so should have been ok",
                transformer.isTransformableSize(A, byteValue-1, B, options));
        assertTrue("Size is equal to limit so should have been ok",
                transformer.isTransformableSize(A, byteValue, B, options));
        assertFalse("Size is greater than limit so should have failed",
                transformer.isTransformableSize(A, byteValue+1, B, options));

        // With a mimetype that does not have any specific limits
        assertTrue("No limits so should have been ok",
                transformer.isTransformableSize(C, byteValue+1, B, options));
        assertTrue("No limits so should have been ok",
                transformer.isTransformableSize(A, byteValue+1, C, options));
        assertTrue("No limits so should have been ok",
                transformer.isTransformableSize(C, byteValue+1, C, options));
    }
    
    @Test
    // Using limit on transformer as a whole
    public void testIsTransformableSizeTrans() throws Exception
    {
        long kValue = 12;
        long byteValue = kValue*1024;

        // Not set mimetype limits yet
        assertTrue("No limits so should have been ok",
                transformer.isTransformableSize(A, byteValue+1, B, options));

        transformer.setMaxSourceSizeKBytes(kValue);
        transformer.register();
        assertTrue("Size is equal to limit so should have been ok",
                transformer.isTransformableSize(A, byteValue, B, options));
        assertFalse("Size is greater than limit so should have failed",
                transformer.isTransformableSize(A, byteValue+1, B, options));
    }
        
    @Test
    // Combination where mimetype limit is used
    public void testIsTransformableSizeCombinationMimetypeUsed() throws Exception
    {
        long kValue = 12;
        long byteValue = kValue*1024;

        // Not set mimetype limits yet
        assertTrue("No limits so should have been ok",
                transformer.isTransformableSize(A, byteValue+1, B, options));

        transformer.setMaxSourceSizeKBytes(kValue+1);
        limits.setMaxSourceSizeKBytes(kValue);
        addMimetypeLimits(A, B, limits);
        transformer.setMimetypeLimits(mimetypeLimits);
        transformer.register();
        assertTrue("Size is equal to limit so should have been ok",
                transformer.isTransformableSize(A, byteValue, B, options));
        assertFalse("Size is greater than limit so should have failed",
                transformer.isTransformableSize(A, byteValue+1, B, options));
    }
    
    @Test
    public void testIsTransformableSizeWithPageLimit() throws Exception
    {
        long kValue = 12;
        long byteValue = kValue*1024;
        
        transformer.setMaxSourceSizeKBytes(kValue);
        transformer.setPageLimitsSupported(true);
        transformer.register();

        // Test works as normal before setting the pageLimit
        assertTrue("Size is less than limit so should have been ok",
                transformer.isTransformableSize(A, byteValue-1, B, options));
        assertTrue("Size is equal to limit so should have been ok",
                transformer.isTransformableSize(A, byteValue, B, options));
        assertFalse("Size is greater than limit so should not have failed",
                transformer.isTransformableSize(A, byteValue+1, B, options));

        // test with pageLimit set
        options.getLimits().setPageLimit(1);
        assertTrue("Size is greater than limit BUT pageLimit is set so should have been ok",
                transformer.isTransformableSize(A, byteValue+1, B, options));
    }
    
    @Test
    public void testSetReaderLimits() throws Exception
    {
        AbstractContentReader reader = new DummyAbstractContentReader(0, 0);
        
        long value = 1234;
        transformer.setTimeoutMs(value);
        transformer.register();
        
        assertEquals("Limit should not have been set in the reader", null, reader.getLimits());
        
        transformer.setReaderLimits(reader, null, options);
        assertEquals("Limit should have been set in the reader", value, reader.getLimits().getTimeoutMs());
        
        options.setTimeoutMs(--value);
        transformer.setReaderLimits(reader, null, options);
        assertEquals("Limit should have been set in the reader", value, reader.getLimits().getTimeoutMs());
    }

    /**
     * A dummy AbstractContentReader that returns a given number of bytes
     * (all 'a') very slowly. There is a configurable delay returning each byte.
     * Used to test timeouts and read limits.
     */
    private static class DummyAbstractContentReader extends AbstractContentReader
    {
        final long size;
        final long msPerByte;
        
        /**
         * @param size of the dummy data
         * @param msPerByte milliseconds between byte reads
         */
        public DummyAbstractContentReader(long size, long msPerByte)
        {
            super("a");
            this.size = size;
            this.msPerByte = msPerByte;
        }
        
        /**
         * @return  Returns an instance of the this class
         */
        @Override
        protected ContentReader createReader() throws ContentIOException
        {
            return new DummyAbstractContentReader(size, msPerByte);
        }

        @Override
        protected ReadableByteChannel getDirectReadableChannel() throws ContentIOException
        {
            InputStream is = new InputStream()
            {
                long read = 0;
                long start = 0;
                
                @Override
                public int read() throws IOException
                {
                    if (read >= size)
                        return -1;
                    
                    read++; 

                    if (msPerByte > 0)
                    {
                        long elapse = System.currentTimeMillis() - start;
                        if (read == 1)
                        {
                            start = elapse;
                        }
                        else
                        {
                            // On Windows it is possible to just wait 1 ms per byte but this
                            // does not work on linux hence (end up with a full read taking
                            // 40 seconds rather than 5) the need to wait if elapse time
                            // is too fast.
                            long delay = (read * msPerByte) - elapse;
                            if (delay > 0)
                            {
                                try
                                {
                                    Thread.sleep(delay);
                                }
                                catch (InterruptedException e)
                                {
                                    // ignore
                                }
                            }
                        }
                    }
                    
                    return 'a';
                }

                // Just a way to tell AbstractContentReader not to wrap the ChannelInputStream
                // in a BufferedInputStream
                @Override
                public boolean markSupported()
                {
                    return true;
                }
            };
            return Channels.newChannel(is);
        }

        public boolean exists()
        {
            return true;
        }

        public long getLastModified()
        {
            return 0L;
        }

        public long getSize()
        {
            return size;
        }
    };
}
