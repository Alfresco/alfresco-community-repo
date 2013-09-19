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

import static org.alfresco.repo.content.transform.TransformerPropertyNameExtractorTest.mockMimetypes;
import static org.alfresco.repo.content.transform.TransformerPropertyNameExtractorTest.mockProperties;
import static org.junit.Assert.assertEquals;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for TransformerConfigSupported.
 * 
 * @author Alan Davis
 */
public class TransformerConfigSupportedTest
{
    @Mock
    private TransformerProperties transformerProperties;

    @Mock
    private MimetypeService mimetypeService;
    
    @Mock
    private TransformationOptions options;

    private TransformerConfigSupported extractor;
    
    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        
        mockMimetypes(mimetypeService,
                "application/pdf", "pdf",
                "image/png",       "png",
                "text/xml",        "xml");
    }

    @Test
    public void supportedTest()
    {
        mockProperties(transformerProperties, "content.transformer.abc.extensions.pdf.png.supported", "true");
        
        extractor = new TransformerConfigSupported(transformerProperties, mimetypeService);
        boolean supported = extractor.isSupportedTransformation((ContentTransformer) new DummyContentTransformer("transformer.abc"), "application/pdf", "image/png", options);
        assertEquals("supported", true, supported);
    }
    
    @Test
    public void unsupportedTest()
    {
        mockProperties(transformerProperties, "content.transformer.abc.extensions.pdf.png.supported", "false");
        
        extractor = new TransformerConfigSupported(transformerProperties, mimetypeService);
        boolean supported = extractor.isSupportedTransformation((ContentTransformer) new DummyContentTransformer("transformer.abc"), "application/pdf", "image/png", options);
        assertEquals("supported", false, supported);
    }
    
    @Test
    public void mixedCaseTrueTest()
    {
        mockProperties(transformerProperties, "content.transformer.abc.extensions.pdf.png.supported", "tRUE");
        
        extractor = new TransformerConfigSupported(transformerProperties, mimetypeService);
        boolean supported = extractor.isSupportedTransformation((ContentTransformer) new DummyContentTransformer("transformer.abc"), "application/pdf", "image/png", options);
        assertEquals("supported", true, supported);
    }
    
    @Test
    public void unsupportedMimetypesTest()
    {
        mockProperties(transformerProperties, "content.transformer.abc.mimetypes.application/pdf.image/png.supported", "false");
        
        extractor = new TransformerConfigSupported(transformerProperties, mimetypeService);
        boolean supported = extractor.isSupportedTransformation((ContentTransformer) new DummyContentTransformer("transformer.abc"), "application/pdf", "image/png", options);
        assertEquals("supported", false, supported);
    }
    
    @Test
    public void badValueIsUnsupportedTest()
    {
        mockProperties(transformerProperties, "content.transformer.abc.extensions.pdf.png.supported", "bad");
        
        extractor = new TransformerConfigSupported(transformerProperties, mimetypeService);
        boolean supported = extractor.isSupportedTransformation((ContentTransformer) new DummyContentTransformer("transformer.abc"), "application/pdf", "image/png", options);
        assertEquals("supported", false, supported);
    }
    
    @Test
    public void otherMimetypesTest()
    {
        mockProperties(transformerProperties, "content.transformer.abc.extensions.png.pdf.supported", "false");
        
        extractor = new TransformerConfigSupported(transformerProperties, mimetypeService);
        boolean supported = extractor.isSupportedTransformation((ContentTransformer) new DummyContentTransformer("transformer.abc"), "application/pdf", "image/png", options);
        assertEquals("supported", true, supported);
    }
    
    @Test
    public void supportedByDefaultTest()
    {
        mockProperties(transformerProperties);
        
        extractor = new TransformerConfigSupported(transformerProperties, mimetypeService);
        boolean supported = extractor.isSupportedTransformation((ContentTransformer) new DummyContentTransformer("transformer.abc"), "application/pdf", "image/png", options);
        assertEquals("supported", true, supported);
    }
    
    @Test
    public void multipleTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.abc.extensions.pdf.png.supported", "false",
                "content.transformer.def.extensions.pdf.png.supported", "true",
                "content.transformer.xyz.extensions.pdf.png.supported", "false");
        
        extractor = new TransformerConfigSupported(transformerProperties, mimetypeService);
        boolean supported = extractor.isSupportedTransformation((ContentTransformer) new DummyContentTransformer("transformer.abc"), "application/pdf", "image/png", options);
        assertEquals("abc supported", false, supported);
        supported = extractor.isSupportedTransformation((ContentTransformer) new DummyContentTransformer("transformer.def"), "application/pdf", "image/png", options);
        assertEquals("def supported", true, supported);
        supported = extractor.isSupportedTransformation((ContentTransformer) new DummyContentTransformer("transformer.xyz"), "application/pdf", "image/png", options);
        assertEquals("xyz supported", false, supported);
    }
    
    @Test
    public void withoutDefaultTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.abc.extensions.pdf.*.supported", "false",
                "content.transformer.abc.extensions.pdf.png.supported", "true");
        
        extractor = new TransformerConfigSupported(transformerProperties, mimetypeService);
        boolean supported = extractor.isSupportedTransformation((ContentTransformer) new DummyContentTransformer("transformer.abc"), "application/pdf", "application/pdf", options);
        assertEquals("abc supported", false, supported);
        supported = extractor.isSupportedTransformation((ContentTransformer) new DummyContentTransformer("transformer.abc"), "application/pdf", "image/png", options);
        assertEquals("def supported", true, supported);
        supported = extractor.isSupportedTransformation((ContentTransformer) new DummyContentTransformer("transformer.abc"), "image/png", "text/xml", options);
        assertEquals("xyz supported", false, supported);  // << not the same in withDefaultTest
    }
    
    @Test
    public void withDefaultTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.abc.extensions.*.*.supported", "true",  // << not the same in withoutDefaultTest
                "content.transformer.abc.extensions.pdf.*.supported", "false",
                "content.transformer.abc.extensions.pdf.png.supported", "true");
        
        extractor = new TransformerConfigSupported(transformerProperties, mimetypeService);
        boolean supported = extractor.isSupportedTransformation((ContentTransformer) new DummyContentTransformer("transformer.abc"), "application/pdf", "application/pdf", options);
        assertEquals("abc supported", false, supported);
        supported = extractor.isSupportedTransformation((ContentTransformer) new DummyContentTransformer("transformer.abc"), "application/pdf", "image/png", options);
        assertEquals("def supported", true, supported);
        supported = extractor.isSupportedTransformation((ContentTransformer) new DummyContentTransformer("transformer.abc"), "image/png", "text/xml", options);
        assertEquals("xyz supported", true, supported);  // << not the same in withoutDefaultTest
    }
    
    @Test
    public void withoutDefaultNegatedTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.abc.extensions.pdf.*.supported", "true",
                "content.transformer.abc.extensions.pdf.png.supported", "false");
        
        extractor = new TransformerConfigSupported(transformerProperties, mimetypeService);
        boolean supported = extractor.isSupportedTransformation((ContentTransformer) new DummyContentTransformer("transformer.abc"), "application/pdf", "application/pdf", options);
        assertEquals("abc supported", true, supported);
        supported = extractor.isSupportedTransformation((ContentTransformer) new DummyContentTransformer("transformer.abc"), "application/pdf", "image/png", options);
        assertEquals("def supported", false, supported);
        supported = extractor.isSupportedTransformation((ContentTransformer) new DummyContentTransformer("transformer.abc"), "image/png", "text/xml", options);
        assertEquals("xyz supported", true, supported);
    }
    
    @Test
    public void withDefaultNegatedTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.abc.extensions.*.*.supported", "false",
                "content.transformer.abc.extensions.pdf.*.supported", "true",
                "content.transformer.abc.extensions.pdf.png.supported", "false");
        
        extractor = new TransformerConfigSupported(transformerProperties, mimetypeService);
        boolean supported = extractor.isSupportedTransformation((ContentTransformer) new DummyContentTransformer("transformer.abc"), "application/pdf", "application/pdf", options);
        assertEquals("abc supported", true, supported);
        supported = extractor.isSupportedTransformation((ContentTransformer) new DummyContentTransformer("transformer.abc"), "application/pdf", "image/png", options);
        assertEquals("def supported", false, supported);
        supported = extractor.isSupportedTransformation((ContentTransformer) new DummyContentTransformer("transformer.abc"), "image/png", "text/xml", options);
        assertEquals("xyz supported", false, supported);
    }
    
    @Test
    public void bothUnsupportedAndSupportedTest()
    {
        // mimetypes should win if both are supplied
        mockProperties(transformerProperties,
                "content.transformer.abc.extensions.pdf.png.supported", "false",
                "content.transformer.abc.mimetypes.application/pdf.image/png.supported", "true");
        
        extractor = new TransformerConfigSupported(transformerProperties, mimetypeService);
        boolean supported = extractor.isSupportedTransformation((ContentTransformer) new DummyContentTransformer("transformer.abc"), "application/pdf", "image/png", options);
        assertEquals("supported", true, supported);
    }
}
