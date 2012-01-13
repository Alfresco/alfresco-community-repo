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
package org.alfresco.repo.content.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.content.AbstractContentReader;
import org.alfresco.repo.content.ContentMinimalContextTestSuite;
import org.alfresco.repo.content.AbstractContentReaderLimitTest.DummyAbstractContentReader;
import org.alfresco.service.ServiceRegistry;
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
    private static final String A = "a";
    private static final String B = "b";
    private static final String C = "c";
    
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

        transformer = new AbstractContentTransformer2()
        {
            @Override
            public boolean isTransformable(String sourceMimetype, String targetMimetype,
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
        long actual = transformer.getTimeoutMs();
        assertEquals("Getter did not return set value", value, actual);
    }

    @Test
    public void testReadLimitTimeMs() throws Exception
    {
        long value = 1234;
        transformer.setReadLimitTimeMs(value);
        long actual = transformer.getReadLimitTimeMs();
        assertEquals("Getter did not return set value", value, actual);
    }

    @Test
    public void testMaxSourceSizeKBytes() throws Exception
    {
        long value = 1234;
        transformer.setMaxSourceSizeKBytes(value);
        long actual = transformer.getMaxSourceSizeKBytes();
        assertEquals("Getter did not return set value", value, actual);
    }

    @Test
    public void testReadLimitKBytes() throws Exception
    {
        long value = 1234;
        transformer.setReadLimitKBytes(value);
        long actual = transformer.getReadLimitKBytes();
        assertEquals("Getter did not return set value", value, actual);
    }

    @Test
    public void testMaxPages() throws Exception
    {
        int value = 1234;
        transformer.setMaxPages(value);
        int actual = transformer.getMaxPages();
        assertEquals("Getter did not return set value", value, actual);
    }

    @Test
    public void testPageLimit() throws Exception
    {
        int value = 1234;
        transformer.setPageLimit(value);
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
        long actual = transformer.getLimits(A, B, options).getMaxSourceSizeKBytes();
        assertEquals("Getter did not return set value", value+1, actual);

        options.setMaxSourceSizeKBytes(value);
        actual = transformer.getLimits(A, B, options).getMaxSourceSizeKBytes();
        assertEquals("Getter did not return set value", value, actual);
    }

    @Test
    public void testIsTransformableSize() throws Exception
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

        assertTrue("Size is less than limit so should have been ok",
                transformer.isTransformableSize(A, byteValue-1, B, options));
        assertTrue("Size is equal to limit so should have been ok",
                transformer.isTransformableSize(A, byteValue, B, options));
        assertFalse("Size is greater than limit so should not have failed",
                transformer.isTransformableSize(A, byteValue+1, B, options));

        // With a mimetype that does not have any specific limits
        assertTrue("No limits so should have been ok",
                transformer.isTransformableSize(C, byteValue+1, B, options));
        assertTrue("No limits so should have been ok",
                transformer.isTransformableSize(A, byteValue+1, C, options));
        assertTrue("No limits so should have been ok",
                transformer.isTransformableSize(C, byteValue+1, C, options));
        
        // Clear the mimetype limits and double check
        limits.setMaxSourceSizeKBytes(-1);
      
        assertTrue("No limits so should have been ok",
                transformer.isTransformableSize(A, byteValue+1, B, options));

        // Check for combinations with transformer limits
        
        // a) Using just transformer limit to start with
        transformer.setMaxSourceSizeKBytes(kValue);
        assertTrue("Size is equal to limit so should have been ok",
                transformer.isTransformableSize(A, byteValue, B, options));
        assertFalse("Size is greater than limit so should not have failed",
                transformer.isTransformableSize(A, byteValue+1, B, options));
    
        // b) combination where transformer limit is used
        transformer.setMaxSourceSizeKBytes(kValue+1);
        limits.setMaxSourceSizeKBytes(kValue);
        assertTrue("Size is equal to limit so should have been ok",
                transformer.isTransformableSize(A, byteValue, B, options));
        assertFalse("Size is greater than limit so should not have failed",
                transformer.isTransformableSize(A, byteValue+1, B, options));
        
        // c) combination where mimetype limit is used
        transformer.setMaxSourceSizeKBytes(kValue);
        limits.setMaxSourceSizeKBytes(kValue+1);
        assertTrue("Size is equal to limit so should have been ok",
                transformer.isTransformableSize(A, byteValue, B, options));
        assertFalse("Size is greater than limit so should not have failed",
                transformer.isTransformableSize(A, byteValue+1, B, options));
    }
    
    @Test
    public void testIsTransformableSizeWithPageLimit() throws Exception
    {
        long kValue = 12;
        long byteValue = kValue*1024;
        
        transformer.setMaxSourceSizeKBytes(kValue);
        transformer.setPageLimitsSuported(true);

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
        
        assertEquals("Limit should not have been set in the reader", null, reader.getLimits());
        
        transformer.setReaderLimits(reader, null, options);
        assertEquals("Limit should have been set in the reader", value, reader.getLimits().getTimeoutMs());
        
        options.setTimeoutMs(--value);
        transformer.setReaderLimits(reader, null, options);
        assertEquals("Limit should have been set in the reader", value, reader.getLimits().getTimeoutMs());
    }
}
