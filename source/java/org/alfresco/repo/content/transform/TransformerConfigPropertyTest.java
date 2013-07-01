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

import static org.alfresco.repo.content.transform.TransformerConfig.ANY;
import static org.alfresco.repo.content.transform.TransformerConfig.PRIORITY;
import static org.alfresco.repo.content.transform.TransformerPropertyNameExtractorTest.mockMimetypes;
import static org.alfresco.repo.content.transform.TransformerPropertyNameExtractorTest.mockProperties;
import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for TransformerConfigProperty.
 * 
 * @author Alan Davis
 */
public class TransformerConfigPropertyTest
{
    @Mock
    private TransformerProperties transformerProperties;

    @Mock
    private MimetypeService mimetypeService;

    private TransformerConfigProperty extractor;
    
    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        
        mockMimetypes(mimetypeService,
                "application/pdf", "pdf",
                "image/png",       "png");
    }

    @Test
    public void simpleTest()
    {
        mockProperties(transformerProperties, "content.transformer.abc.xyz.priority", "87");
        
        extractor = new TransformerConfigProperty(transformerProperties, mimetypeService, PRIORITY, "55");
        int actual = extractor.getInt((ContentTransformer) new DummyContentTransformer("transformer.abc.xyz"), null, null);
        assertEquals("transformer default priority", 87, actual);
    }

    @Test
    public void getSystemWideDefaultTest()
    {
        mockProperties(transformerProperties, "content.transformer.default.priority", "87");
        
        extractor = new TransformerConfigProperty(transformerProperties, mimetypeService, PRIORITY, "55");
        int actual = extractor.getInt(null, null, null);
        assertEquals("default priority", 87, actual);
    }

    @Test
    public void getSystemWideDefaultWithAnyTest()
    {
        // Same as getSystemWideDefaultTest except getInt uses ANY rather than null
        mockProperties(transformerProperties, "content.transformer.default.priority", "87");
        
        extractor = new TransformerConfigProperty(transformerProperties, mimetypeService, PRIORITY, "55");
        int actual = extractor.getInt(null, ANY, ANY);
        assertEquals("default priority", 87, actual);
    }
    
    @Test
    public void useSystemWideDefaultTest()
    {
        mockProperties(transformerProperties, "content.transformer.default.priority", "87");
        
        extractor = new TransformerConfigProperty(transformerProperties, mimetypeService, PRIORITY, "55");
        int actual = extractor.getInt((ContentTransformer) new DummyContentTransformer("transformer.abc.xyz"), null, null);
        assertEquals("default priority", 87, actual);
    }

    @Test
    public void useNonExistentSystemWideDefaultTest()
    {
        mockProperties(transformerProperties);
        
        extractor = new TransformerConfigProperty(transformerProperties, mimetypeService, PRIORITY, "55");
        int actual = extractor.getInt((ContentTransformer) new DummyContentTransformer("transformer.abc.xyz"), null, null);
        assertEquals("default priority", 55, actual);
    }

    @Test
    public void mimetypesTest()
    {
        mockProperties(transformerProperties, "content.transformer.abc.xyz.mimetypes.application/pdf.image/png.priority", "87");
        
        extractor = new TransformerConfigProperty(transformerProperties, mimetypeService, PRIORITY, "55");
        int actual = extractor.getInt((ContentTransformer) new DummyContentTransformer("transformer.abc.xyz"), "application/pdf", "image/png");
        assertEquals("transformer default priority", 87, actual);
    }

    @Test
    public void extensionsTest()
    {
        mockProperties(transformerProperties, "content.transformer.abc.xyz.extensions.pdf.png.priority", "87");
        
        extractor = new TransformerConfigProperty(transformerProperties, mimetypeService, PRIORITY, "55");
        int actual = extractor.getInt((ContentTransformer) new DummyContentTransformer("transformer.abc.xyz"), "application/pdf", "image/png");
        assertEquals("transformer default priority", 87, actual);
    }

    @Test
    public void multiplePropertiesTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.default.priority", "11",
                "content.transformer.abc.extensions.pdf.png.priority", "22",
                "content.transformer.abc.xyz.extensions.pdf.png.priority", "33");
        
        extractor = new TransformerConfigProperty(transformerProperties, mimetypeService, PRIORITY, "55");

        int actual = extractor.getInt((ContentTransformer) new DummyContentTransformer("transformer.qaz"), "application/pdf", "image/png");
        assertEquals("default", 11, actual);
        actual = extractor.getInt((ContentTransformer) new DummyContentTransformer("transformer.abc"), "application/pdf", "image/png");
        assertEquals("abc", 22, actual);
        actual = extractor.getInt((ContentTransformer) new DummyContentTransformer("transformer.abc.xyz"), "application/pdf", "image/png");
        assertEquals("abc.xyz", 33, actual);
    }

    @Test
    public void longTest()
    {
        mockProperties(transformerProperties, "content.transformer.abc.xyz.extensions.pdf.png.priority", "1234567890");
        
        extractor = new TransformerConfigProperty(transformerProperties, mimetypeService, PRIORITY, "55");
        long actual = extractor.getInt((ContentTransformer) new DummyContentTransformer("transformer.abc.xyz"), "application/pdf", "image/png");
        assertEquals("transformer default priority", 1234567890L, actual);
    }

    @Test(expected=NumberFormatException.class)
    public void badIntTest()
    {
        mockProperties(transformerProperties, "content.transformer.abc.xyz.extensions.pdf.png.priority",
                Long.toString(((long)Integer.MAX_VALUE)+1));
        
        extractor = new TransformerConfigProperty(transformerProperties, mimetypeService, PRIORITY, "55");
        extractor.getInt((ContentTransformer) new DummyContentTransformer("transformer.abc.xyz"), "application/pdf", "image/png");
    }

    @Test(expected=NumberFormatException.class)
    public void badIntAbcTest()
    {
        mockProperties(transformerProperties, "content.transformer.abc.xyz.extensions.pdf.png.priority", "abc");
        
        extractor = new TransformerConfigProperty(transformerProperties, mimetypeService, PRIORITY, "55");
        extractor.getInt((ContentTransformer) new DummyContentTransformer("transformer.abc.xyz"), "application/pdf", "image/png");
    }

    @Test(expected=NumberFormatException.class)
    public void badLongTest()
    {
        mockProperties(transformerProperties, "content.transformer.abc.xyz.extensions.pdf.png.priority",
                new BigInteger(""+Long.MAX_VALUE).add(BigInteger.ONE).toString());
        
        extractor = new TransformerConfigProperty(transformerProperties, mimetypeService, PRIORITY, "55");
        extractor.getLong((ContentTransformer) new DummyContentTransformer("transformer.abc.xyz"), "application/pdf", "image/png");
    }

    @Test(expected=NumberFormatException.class)
    public void badLongAbcTest()
    {
        mockProperties(transformerProperties, "content.transformer.abc.xyz.extensions.pdf.png.priority", "abc");
        
        extractor = new TransformerConfigProperty(transformerProperties, mimetypeService, PRIORITY, "55");
        extractor.getLong((ContentTransformer) new DummyContentTransformer("transformer.abc.xyz"), "application/pdf", "image/png");
    }
}
