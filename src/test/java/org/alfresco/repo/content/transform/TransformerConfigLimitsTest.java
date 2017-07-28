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
package org.alfresco.repo.content.transform;

import static org.alfresco.repo.content.transform.TransformerPropertyNameExtractorTest.mockMimetypes;
import static org.alfresco.repo.content.transform.TransformerPropertyNameExtractorTest.mockProperties;
import static org.junit.Assert.assertEquals;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for TransformerConfigLimits.
 * 
 * @author Alan Davis
 */
public class TransformerConfigLimitsTest
{
    @Mock
    private TransformerProperties transformerProperties;

    @Mock
    private MimetypeService mimetypeService;
    
    private ContentTransformer transformer1;
    private ContentTransformer transformer2;

    private TransformerConfigLimits extractor;
    
    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        
        transformer1 = new DummyContentTransformer("transformer.transformer1");
        transformer2 = new DummyContentTransformer("transformer.transformer2");

        mockMimetypes(mimetypeService,
                "application/pdf", "pdf",
                "image/png",       "png",
                "text/plain",       "txt");
    }

    @Test
    // A value is specified for a transformer and mimetypes
    public void transformerMimetypesTest()
    {
        mockProperties(transformerProperties, "content.transformer.transformer1.extensions.pdf.png.maxSourceSizeKBytes", "10");
        
        extractor = new TransformerConfigLimits(transformerProperties, mimetypeService);
        TransformationOptionLimits limits = extractor.getLimits(transformer1, "application/pdf", "image/png", null);
        assertEquals(10, limits.getMaxSourceSizeKBytes());
    }
    
    @Test
    // A value is specified for a transformer
    public void transformerTest()
    {
        mockProperties(transformerProperties, "content.transformer.transformer1.maxSourceSizeKBytes", "10");
        
        extractor = new TransformerConfigLimits(transformerProperties, mimetypeService);
        TransformationOptionLimits limits = extractor.getLimits(transformer1, "application/pdf", "image/png", null);
        assertEquals(10, limits.getMaxSourceSizeKBytes());
    }
    
    @Test
    // A value is specified as a transformer default with mimetypes
    public void defaultMimetypesTest()
    {
        mockProperties(transformerProperties, "content.transformer.default.extensions.pdf.png.maxSourceSizeKBytes", "10");
        
        extractor = new TransformerConfigLimits(transformerProperties, mimetypeService);
        TransformationOptionLimits limits = extractor.getLimits(transformer1, "application/pdf", "image/png", null);
        assertEquals(10, limits.getMaxSourceSizeKBytes());
    }
    
    @Test
    // A value is specified as a transformer default without mimetypes
    public void defaultTest()
    {
        mockProperties(transformerProperties, "content.transformer.default.maxSourceSizeKBytes", "10");
        
        extractor = new TransformerConfigLimits(transformerProperties, mimetypeService);
        TransformationOptionLimits limits = extractor.getLimits(transformer1, "application/pdf", "image/png", null);
        assertEquals(10, limits.getMaxSourceSizeKBytes());
    }
    
    // ---------------------------------------
    
    @Test
    // Checks that transformer defaults are combined to construct mimetype specific values
    public void transformerDefaultsUsedInMimetypesTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformer1.extensions.pdf.png.maxSourceSizeKBytes", "10",
                "content.transformer.transformer1.timeoutMs", "10000");
        
        extractor = new TransformerConfigLimits(transformerProperties, mimetypeService);
        TransformationOptionLimits limits = extractor.getLimits(transformer1, "application/pdf", "image/png", null);
        assertEquals(10, limits.getMaxSourceSizeKBytes());
        assertEquals(10000L, limits.getTimeoutMs());
    }
    
    @Test
    // Checks that transformer mimetype values override transformer defaults even if unlimited.
    // This was new in 4.2. Prior to this they would have been combined, because of the need to
    // always specify them in the spring configuration, which was removed in 4.2.
    public void dontCombineTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformer1.extensions.pdf.png.maxSourceSizeKBytes", "-1",
                "content.transformer.transformer1.maxSourceSizeKBytes", "20");
        
        extractor = new TransformerConfigLimits(transformerProperties, mimetypeService);
        TransformationOptionLimits limits = extractor.getLimits(transformer1, "application/pdf", "image/png", null);
        assertEquals(-1, limits.getMaxSourceSizeKBytes());
    }
    
    @Test
    // Checks that system wide defaults (and system wide mimetype defaults) have been used to construct
    // transformer specific values.
    public void systemWideDefaultsUsedInTransformersTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformer1.extensions.pdf.png.maxSourceSizeKBytes", "10",
                "content.transformer.transformer1.maxSourceSizeKBytes", "15",
                "content.transformer.default.timeoutMs", "120000",
                "content.transformer.default.extensions.txt.png.pageLimit", "1");
        
        extractor = new TransformerConfigLimits(transformerProperties, mimetypeService);
        TransformationOptionLimits pdfToPngLimits = extractor.getLimits(transformer1, "application/pdf", "image/png", null);
        assertEquals(10, pdfToPngLimits.getMaxSourceSizeKBytes());
        assertEquals(120000L, pdfToPngLimits.getTimeoutMs());
        assertEquals(-1, pdfToPngLimits.getPageLimit());
        
        TransformationOptionLimits txtToPngLimits = extractor.getLimits(transformer1, "text/plain", "image/png", null);
        assertEquals(15, txtToPngLimits.getMaxSourceSizeKBytes());
        assertEquals(120000L, txtToPngLimits.getTimeoutMs());
        assertEquals(1, txtToPngLimits.getPageLimit());
    }
    
    @Test
    // Checks wildcard usage at the transformer level
    public void transformerWildcardTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformer1.extensions.*.png.maxSourceSizeKBytes", "10");
        
        extractor = new TransformerConfigLimits(transformerProperties, mimetypeService);
        TransformationOptionLimits limits = extractor.getLimits(transformer1, "application/pdf", "image/png", null);
        assertEquals(10, limits.getMaxSourceSizeKBytes());
    }
    
    @Test
    // Checks wildcard usage at the system wide level
    public void systemWideWildcardTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformer1.maxSourceSizeKBytes", "15",
                "content.transformer.default.timeoutMs", "120000",
                "content.transformer.default.extensions.txt.*.pageLimit", "1");
        
        extractor = new TransformerConfigLimits(transformerProperties, mimetypeService);
        TransformationOptionLimits txtToPngLimits = extractor.getLimits(transformer1, "text/plain", "image/png", null);
        assertEquals(15, txtToPngLimits.getMaxSourceSizeKBytes());
        assertEquals(120000L, txtToPngLimits.getTimeoutMs());
        assertEquals(1, txtToPngLimits.getPageLimit());
    }
    
    // MNT-14295 With Java 7 the order in which properties were supplied changed from
    // what happen with Java 6 and happens with 8. When combined with a bug to do with
    // always clearing the max value when setting a limit or the limit when setting
    // the max value, the initial map of TransformerConfigLimits would be different.
    // Java 7 was used as the runtime for 4.2 and the 5.0 but Java 8 became the default
    // from 5.0.1.
    // None of the other unit tests in this class failed as a none of them provided
    // both max and limit values.
    @Test
    public void propertyOrderJava7Test()
    {
        mockProperties(transformerProperties,
            "content.transformer.default.timeoutMs", "120000",
            "content.transformer.default.readLimitTimeMs", "-1");
    
        extractor = new TransformerConfigLimits(transformerProperties, mimetypeService);
        TransformationOptionLimits limits = extractor.getLimits(transformer1, "text/plain", "image/png", null);
        assertEquals(120000L, limits.getTimeoutMs());
        assertEquals(-1L, limits.getReadLimitTimeMs());
    }

    @Test
    public void propertyOrderJava8or6Test()
    {
        mockProperties(transformerProperties,
            "content.transformer.default.timeoutMs", "-1",
            "content.transformer.default.readLimitTimeMs", "120000");
    
        extractor = new TransformerConfigLimits(transformerProperties, mimetypeService);
        TransformationOptionLimits limits = extractor.getLimits(transformer1, "text/plain", "image/png", null);
        assertEquals(-1L, limits.getTimeoutMs());
        assertEquals(120000L, limits.getReadLimitTimeMs());
    }

    @Test
    public void bothMaxAndLimitSetIgnoreLimitTest()
    {
        mockProperties(transformerProperties,
            "content.transformer.default.readLimitTimeMs", "990000",
            "content.transformer.default.timeoutMs", "120000");
        
        extractor = new TransformerConfigLimits(transformerProperties, mimetypeService);
        TransformationOptionLimits limits = extractor.getLimits(transformer1, "text/plain", "image/png", null);
        assertEquals(120000L, limits.getTimeoutMs());
        assertEquals(-1L, limits.getReadLimitTimeMs());
    }
    
    @Test
    public void bothMaxAndLimitSetIgnoreMaxTest()
    {
        mockProperties(transformerProperties,
            "content.transformer.default.readLimitTimeMs", "120000",
            "content.transformer.default.timeoutMs", "990000");
        
        extractor = new TransformerConfigLimits(transformerProperties, mimetypeService);
        TransformationOptionLimits limits = extractor.getLimits(transformer1, "text/plain", "image/png", null);
        assertEquals(-1L, limits.getTimeoutMs());
        assertEquals(120000L, limits.getReadLimitTimeMs());
    }
    
    // ---------------------------------------

    @Test
    // A value is specified for a transformer, mimetypes and use
    public void transformerMimetypesUseTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformer1.extensions.pdf.png.maxSourceSizeKBytes",           "10",
                "content.transformer.transformer1.extensions.pdf.png.maxSourceSizeKBytes.use.index", "20");
        
        extractor = new TransformerConfigLimits(transformerProperties, mimetypeService);
        TransformationOptionLimits limits = extractor.getLimits(transformer1, "application/pdf", "image/png", null);
        assertEquals(10, limits.getMaxSourceSizeKBytes());

        limits = extractor.getLimits(transformer1, "application/pdf", "image/png", "index");
        assertEquals(20, limits.getMaxSourceSizeKBytes());
    }
    
    @Test
    // A value is specified for a transformer and use
    public void transformerUseTest()
    {
        mockProperties(transformerProperties,
            "content.transformer.transformer2.maxSourceSizeKBytes",           "10",
            "content.transformer.transformer1.maxSourceSizeKBytes.use.index", "20",
            // The following is ignored when "index" is specified, as the 'use' property is transformer wide.
            "content.transformer.transformer1.maxSourceSizeKBytes",           "30");
        
        extractor = new TransformerConfigLimits(transformerProperties, mimetypeService);
        TransformationOptionLimits limits = extractor.getLimits(transformer1, "application/pdf", "image/png", null);
        assertEquals(30, limits.getMaxSourceSizeKBytes());

        limits = extractor.getLimits(transformer1, "application/pdf", "image/png", "index");
        assertEquals(20, limits.getMaxSourceSizeKBytes());

        limits = extractor.getLimits(transformer2, "application/pdf", "image/png", "index");
        assertEquals(10, limits.getMaxSourceSizeKBytes());
    }
    
    @Test
    // A value is specified as a transformer default with mimetypes and use
    public void defaultMimetypesUseTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.default.extensions.pdf.png.maxSourceSizeKBytes",           "10",
                "content.transformer.default.extensions.pdf.png.maxSourceSizeKBytes.use.index", "20",
                // The following is ignored when "index" is specified, as the 'use' property is system wide.
                "content.transformer.transformer2.maxSourceSizeKBytes", "30");
        
        extractor = new TransformerConfigLimits(transformerProperties, mimetypeService);
        TransformationOptionLimits limits = extractor.getLimits(transformer1, "application/pdf", "image/png", null);
        assertEquals(10, limits.getMaxSourceSizeKBytes());

        limits = extractor.getLimits(transformer2, "application/pdf", "image/png", "doclib");
        assertEquals(30, limits.getMaxSourceSizeKBytes());

        limits = extractor.getLimits(transformer2, "application/pdf", "image/png", "index");
        assertEquals(20, limits.getMaxSourceSizeKBytes());
    }
    
    @Test
    // A value is specified as a transformer default without mimetypes but with a use
    public void defaultUseTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.default.maxSourceSizeKBytes",           "10",
                "content.transformer.default.maxSourceSizeKBytes.use.index", "20");
        
        extractor = new TransformerConfigLimits(transformerProperties, mimetypeService);
        TransformationOptionLimits limits = extractor.getLimits(transformer1, "application/pdf", "image/png", null);
        assertEquals(10, limits.getMaxSourceSizeKBytes());

        limits = extractor.getLimits(transformer1, "application/pdf", "image/png", "index");
        assertEquals(20, limits.getMaxSourceSizeKBytes());
    }
    
    @Test
    // A value is specified as a transformer default without mimetypes but with a use
    public void defaultUseTest2()
    {
        mockProperties(transformerProperties,
                "content.transformer.default.maxSourceSizeKBytes",           "10",
                "content.transformer.default.maxSourceSizeKBytes.use.index", "20",
                // The following is ignored when "index" is specified, as the 'use' property is system wide.
                "content.transformer.transformer2.maxSourceSizeKBytes",      "30");
        
        extractor = new TransformerConfigLimits(transformerProperties, mimetypeService);
        TransformationOptionLimits limits = extractor.getLimits(transformer1, "application/pdf", "image/png", null);
        assertEquals(10, limits.getMaxSourceSizeKBytes());

        limits = extractor.getLimits(transformer2, "application/pdf", "image/png", null);
        assertEquals(30, limits.getMaxSourceSizeKBytes());

        limits = extractor.getLimits(transformer1, "application/pdf", "image/png", "index");
        assertEquals(20, limits.getMaxSourceSizeKBytes());

        limits = extractor.getLimits(transformer2, "application/pdf", "image/png", "index");
        assertEquals(20, limits.getMaxSourceSizeKBytes());
    }

    @Test
    // Checks limit does not change if use param is specified but not specifically set
    public void useUnsetTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformer1.pageLimit", "76");
        
        extractor = new TransformerConfigLimits(transformerProperties, mimetypeService);
        TransformationOptionLimits transformerDefaultLimits = extractor.getLimits(transformer1, null, null, null);
        TransformationOptionLimits transformerDoclibLimits = extractor.getLimits(transformer1, null, null, "doclib");
        assertEquals(76, transformerDefaultLimits.getPageLimit());
        assertEquals(76, transformerDoclibLimits.getPageLimit());
    }
    
    @Test
    // Checks limit does not change if use param is specified but not specifically set
    public void useSetTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformer1.pageLimit.use.doclib", "22",
                "content.transformer.transformer1.pageLimit", "76");
        
        extractor = new TransformerConfigLimits(transformerProperties, mimetypeService);
        TransformationOptionLimits transformerDefaultLimits = extractor.getLimits(transformer1, null, null, null);
        TransformationOptionLimits transformerDoclibLimits = extractor.getLimits(transformer1, null, null, "doclib");
        assertEquals(76, transformerDefaultLimits.getPageLimit());
        assertEquals(22, transformerDoclibLimits.getPageLimit());
    }
    
    @Test
    // Checks wildcard usage at the system wide level
    public void systemWideWildcardUseTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.default.extensions.txt.*.pageLimit", "1",
                "content.transformer.default.extensions.txt.*.pageLimit.use.index", "2",
                "content.transformer.default.extensions.pdf.*.pageLimit.use.index", "3",
                "content.transformer.default.extensions.pdf.txt.pageLimit.use.index", "4");
        
        extractor = new TransformerConfigLimits(transformerProperties, mimetypeService);
        TransformationOptionLimits txtToPngLimits = extractor.getLimits(transformer1, "text/plain", "image/png", null);
        assertEquals(1, txtToPngLimits.getPageLimit());

        txtToPngLimits = extractor.getLimits(transformer1, "text/plain", "image/png", "index");
        assertEquals(2, txtToPngLimits.getPageLimit());

        TransformationOptionLimits pdfToPngLimits = extractor.getLimits(transformer1, "application/pdf", "image/png", "index");
        assertEquals(3, pdfToPngLimits.getPageLimit());

        TransformationOptionLimits pdfToTxtLimits = extractor.getLimits(transformer1, "application/pdf", "text/plain", "index");
        assertEquals(4, pdfToTxtLimits.getPageLimit());
    }
}
