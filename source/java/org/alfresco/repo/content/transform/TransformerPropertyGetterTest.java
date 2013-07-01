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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for TransformerPropertyGetter.
 * 
 * @author Alan Davis
 */
public class TransformerPropertyGetterTest
{
    @Mock
    private TransformerProperties transformerProperties;

    @Mock
    private MimetypeService mimetypeService;
    
    @Mock
    ContentTransformerRegistry transformerRegistry;
    
    @Mock
    TransformerLog transformerLog;
    
    @Mock
    TransformerDebugLog transformerDebugLog;

    private ContentTransformer transformer1;
    private ContentTransformer transformer2;
    private ContentTransformer transformer3;

    private class DummyContentTransformer2 extends AbstractContentTransformer2 {
        private final String name;
        
        DummyContentTransformer2(String name)
        {
            this.name = name;
        }
        
        @Override
        public String getName()
        {
            return name;
        }

        @Override
        protected void transformInternal(ContentReader reader, ContentWriter writer,
                TransformationOptions options) throws Exception
        {
        }};

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        
        transformer1 = new DummyContentTransformer2("transformer.transformer1");
        transformer2 = new DummyContentTransformer2("transformer.transformer2");
        transformer3 = new DummyContentTransformer2("transformer.transformer3");
        
        when(transformerLog.getPropertyName()).thenReturn("transformer.log.entries");
        when(transformerDebugLog.getPropertyName()).thenReturn("transformer.debug.entries");
        
        when(transformerProperties.getDefaultProperties()).thenReturn(new Properties());

        mockMimetypes(mimetypeService,
                "application/pdf", "pdf",
                "image/png",       "png",
                "text/plain",       "txt");
    }

    public static void mockAllTransformers(ContentTransformerRegistry transformerRegistry, ContentTransformer... transformers)
    {
        List<ContentTransformer> allTransformers = new ArrayList<ContentTransformer>();
        for (ContentTransformer transformer: transformers)
        {
            allTransformers.add(transformer);
        }
        when(transformerRegistry.getAllTransformers()).thenReturn(allTransformers);
    }

    @Test
    public void logEntriesTest()
    {
        String actual = new TransformerPropertyGetter(false, transformerProperties,
                mimetypeService, transformerRegistry, transformerLog, transformerDebugLog).toString();
        
        assertEquals("# LOG and DEBUG history sizes\n" +
                "# ===========================\n" +
                "# Use small values as these logs are held in memory. 0 to disable.\n" +
                "# transformer.log.entries=0\n" +
                "# transformer.debug.entries=0\n", actual);
    }

    @Test
    public void defaultTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.default.priority",          "100",
                "content.transformer.default.count",               "0",
                "content.transformer.default.maxPages.use.XXX",   "88");

        String actual = new TransformerPropertyGetter(false, transformerProperties,
                mimetypeService, transformerRegistry, transformerLog, transformerDebugLog).toString();
        
        assertEquals("# LOG and DEBUG history sizes\n" +
                "# ===========================\n" +
                "# Use small values as these logs are held in memory. 0 to disable.\n" +
                "# transformer.log.entries=0\n" +
                "# transformer.debug.entries=0\n" +
                "\n" +
                "# Default transformer settings\n" +
                "# ============================\n" +
                "content.transformer.default.count=0\n" +
                "content.transformer.default.maxPages.use.XXX=88\n"+
                "content.transformer.default.priority=100\n", actual);
    }

    @Test
    public void defaultWithOverridesTest()
    {
        defaultWithOverridesTest(false,
                "# LOG and DEBUG history sizes\n" +
                "# ===========================\n" +
                "# Use small values as these logs are held in memory. 0 to disable.\n" +
                "# transformer.log.entries=0\n" +
                "# transformer.debug.entries=0\n" +
                "\n" +
                "# Default transformer settings\n" +
                "# ============================\n" +
                "# content.transformer.default.count=0\n" +
                "content.transformer.default.maxPages.use.XXX=88  # default=77\n"+
                "content.transformer.default.priority=100  # default=111\n");
    }
    
    @Test
    public void defaultWithOnlyOverridesTest()
    {
        defaultWithOverridesTest(true,
                "# Default transformer settings\n" +
                "# ============================\n" +
                "content.transformer.default.maxPages.use.XXX=88  # default=77\n"+
                "content.transformer.default.priority=100  # default=111\n");
    }

    private void defaultWithOverridesTest(boolean onlyChanges, String expected)
    {
        Properties defaultProperties = new Properties();
        defaultProperties.setProperty("content.transformer.default.priority",          "111");
        defaultProperties.setProperty("content.transformer.default.count",               "0");
        defaultProperties.setProperty("content.transformer.default.maxPages.use.XXX",   "77");
        when(transformerProperties.getDefaultProperties()).thenReturn(defaultProperties);

        mockProperties(transformerProperties,
                "content.transformer.default.priority",          "100",
                "content.transformer.default.count",               "0",
                "content.transformer.default.maxPages.use.XXX",   "88");

        String actual = new TransformerPropertyGetter(onlyChanges, transformerProperties,
                mimetypeService, transformerRegistry, transformerLog, transformerDebugLog).toString();
        
        assertEquals(expected, actual);
    }

    @Test
    public void allSuffixesTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformer1.timeoutMs",                          "1",
                "content.transformer.transformer1.extensions.pdf.png.readLimitTimeMs", "2",
                "content.transformer.transformer1.maxSourceSizeKBytes",                "3",
                "content.transformer.transformer1.extensions.pdf.png.readLimitKBytes", "4",
                "content.transformer.transformer1.maxPages",                           "5",
                "content.transformer.transformer1.extensions.pdf.png.pageLimit",       "6",
                "content.transformer.transformer1.maxPages.use.index",                 "12",
                "content.transformer.transformer1.maxPages.use.webpreview",            "13",
                
                "content.transformer.transformer1.extensions.pdf.png.supported",       "true",
                "content.transformer.transformer1.priority",                           "7",
                "content.transformer.transformer1.errorTime",                          "8",
                "content.transformer.transformer2.time",                               "9",
                "content.transformer.transformer2.count",                              "10",
                "content.transformer.transformer1.thresholdCount",                     "11",
                "content.transformer.transformer1.failover",                           "qwe|qaz",
                "content.transformer.transformer1.pipeline",                           "qwe|pdf|qaz");
        
        when(transformerRegistry.getTransformer("transformer.transformer1")).thenReturn(transformer1);
        when(transformerRegistry.getTransformer("transformer.transformer2")).thenReturn(transformer2);
        when(transformerRegistry.getTransformers()).thenReturn(Arrays.asList(new ContentTransformer[] {transformer1, transformer2}));
        
        String actual = new TransformerPropertyGetter(false, transformerProperties,
                mimetypeService, transformerRegistry, transformerLog, transformerDebugLog).toString();

        assertEquals("# LOG and DEBUG history sizes\n" +
                "# ===========================\n" +
                "# Use small values as these logs are held in memory. 0 to disable.\n" +
                "# transformer.log.entries=0\n" +
                "# transformer.debug.entries=0\n" +
                "\n" +
                "# Transformers with configuration settings\n" +
                "# ========================================\n" +
                "# Commented out settings are hard coded values for information purposes\n" +
                "\n" +
                "# transformer1\n" +
                "# ------------\n" +
                "content.transformer.transformer1.pipeline=qwe|pdf|qaz\n" +
                "content.transformer.transformer1.errorTime=8\n" +
                "content.transformer.transformer1.failover=qwe|qaz\n" +
                "content.transformer.transformer1.maxPages=5\n" +
                "content.transformer.transformer1.maxPages.use.index=12\n"+
                "content.transformer.transformer1.maxPages.use.webpreview=13\n"+
                "content.transformer.transformer1.maxSourceSizeKBytes=3\n" +
                "content.transformer.transformer1.priority=7\n" +
                "content.transformer.transformer1.thresholdCount=11\n" +
                "content.transformer.transformer1.timeoutMs=1\n" +
                "content.transformer.transformer1.extensions.pdf.png.pageLimit=6\n" +
                "content.transformer.transformer1.extensions.pdf.png.readLimitKBytes=4\n" +
                "content.transformer.transformer1.extensions.pdf.png.readLimitTimeMs=2\n" +
                "content.transformer.transformer1.extensions.pdf.png.supported=true\n" +
                "\n" +
                "# transformer2\n"+
                "# ------------\n" +
                "content.transformer.transformer2.count=10\n"+
                "content.transformer.transformer2.time=9\n", actual);
    }
    
    @Test
    public void normalTest()
    {
        normalTest(true);
    }

    @Test
    // Output for a transformer that may only be used as part of another transformer
    public void normalComponentTest()
    {
        normalTest(false);
    }

    private void normalTest(boolean isAvailable)
    {
        AbstractContentTransformer2 simple = new AbstractContentTransformer2() {
            @Override
            protected void transformInternal(ContentReader reader, ContentWriter writer,
                    TransformationOptions options) throws Exception
            {
            }};
        simple.setBeanName("transformer.exampleSimple");
        
        when(transformerRegistry.getAllTransformers()).thenReturn(Arrays.asList(new ContentTransformer[] {(ContentTransformer)simple}));
        if (isAvailable)
        {
            when(transformerRegistry.getTransformers()).thenReturn(Arrays.asList(new ContentTransformer[] {(ContentTransformer)simple}));
        }

        String actual = new TransformerPropertyGetter(false, transformerProperties,
                mimetypeService, transformerRegistry, transformerLog, transformerDebugLog).toString();
        
        assertEquals("# LOG and DEBUG history sizes\n" +
                "# ===========================\n" +
                "# Use small values as these logs are held in memory. 0 to disable.\n" +
                "# transformer.log.entries=0\n" +
                "# transformer.debug.entries=0\n" +
                "\n" +
                "# Transformers without extra configuration settings\n" +
                "# =================================================\n" +
                "\n" +
                "# exampleSimple\n" +
                "# -------------\n" +
                (isAvailable ? "" : "# content.transformer.exampleSimple.available=false\n"), actual);
    }
    
    @Test
    public void complexTest()
    {
        complexTest(true);
    }

    @Test
    // Output for a transformer that may only be used as part of another transformer
    public void complexComponentTest()
    {
        complexTest(false);
    }

    private void complexTest(boolean isAvailable)
    {
        ComplexContentTransformer complex = new ComplexContentTransformer();
        complex.setTransformers(Arrays.asList(new ContentTransformer[] {transformer1, transformer2, transformer3}));
        complex.setIntermediateMimetypes(Arrays.asList(new String[] {"application/pdf", null}));
        complex.setBeanName("transformer.examplePipeline");
        complex.setMimetypeService(mimetypeService);
        
        if (isAvailable)
        {
            when(transformerRegistry.getTransformers()).thenReturn(Arrays.asList(new ContentTransformer[] {complex, transformer1, transformer2, transformer3}));
        }
        else
        {
            when(transformerRegistry.getTransformers()).thenReturn(Arrays.asList(new ContentTransformer[] {transformer1, transformer2, transformer3}));
        }
        when(transformerRegistry.getAllTransformers()).thenReturn(Arrays.asList(new ContentTransformer[] {complex, transformer1, transformer2, transformer3}));

        String actual = new TransformerPropertyGetter(false, transformerProperties,
                mimetypeService, transformerRegistry, transformerLog, transformerDebugLog).toString();
        
        assertEquals("# LOG and DEBUG history sizes\n" +
                "# ===========================\n" +
                "# Use small values as these logs are held in memory. 0 to disable.\n" +
                "# transformer.log.entries=0\n" +
                "# transformer.debug.entries=0\n" +
                "\n" +
                "# Transformers without extra configuration settings\n" +
                "# =================================================\n" +
                "\n" +
                "# examplePipeline\n" +
                "# ---------------\n" +
                (isAvailable ? "" : "# content.transformer.examplePipeline.available=false\n") +
                "# content.transformer.examplePipeline.pipeline=transformer1|pdf|transformer2||transformer3\n"+                
                "\n" +
                "# transformer1\n" +
                "# ------------\n" +
                "\n" +
                "# transformer2\n" +
                "# ------------\n" +
                "\n" +
                "# transformer3\n" +
                "# ------------\n", actual);
    }
    
    @Test
    public void failoverTest()
    {
        failoverTest(true);
    }

    @Test
    // Output for a failover transformer that may only be used as part of another transformer
    public void failoverComponentTest()
    {
        failoverTest(false);
    }

    private void failoverTest(boolean isAvailable)
    {
        FailoverContentTransformer failover = new FailoverContentTransformer();
        failover.setTransformers(Arrays.asList(new ContentTransformer[] {transformer1, transformer2}));
        failover.setBeanName("transformer.exampleFailover");
        
        if (isAvailable)
        {
            when(transformerRegistry.getTransformers()).thenReturn(Arrays.asList(new ContentTransformer[] {failover, transformer1, transformer2}));
        }
        else
        {
            when(transformerRegistry.getTransformers()).thenReturn(Arrays.asList(new ContentTransformer[] {transformer1, transformer2}));
        }
        when(transformerRegistry.getAllTransformers()).thenReturn(Arrays.asList(new ContentTransformer[] {failover, transformer1, transformer2}));

        String actual = new TransformerPropertyGetter(false, transformerProperties,
                mimetypeService, transformerRegistry, transformerLog, transformerDebugLog).toString();
        
        assertEquals("# LOG and DEBUG history sizes\n" +
                "# ===========================\n" +
                "# Use small values as these logs are held in memory. 0 to disable.\n" +
                "# transformer.log.entries=0\n" +
                "# transformer.debug.entries=0\n" +
                "\n" +
                "# Transformers without extra configuration settings\n" +
                "# =================================================\n" +
                "\n" +
                "# exampleFailover\n" +
                "# ---------------\n" +
                (isAvailable ? "" : "# content.transformer.exampleFailover.available=false\n") +
                "# content.transformer.exampleFailover.failover=transformer1|transformer2\n" +
                "\n" +
                "# transformer1\n" +
                "# ------------\n" +
                "\n" +
                "# transformer2\n" +
                "# ------------\n", actual);
    }
}
