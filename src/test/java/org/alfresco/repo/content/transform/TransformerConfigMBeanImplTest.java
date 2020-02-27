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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for TransformerConfigMBeanImpl.
 * 
 * @author Alan Davis
 *
 * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
public class TransformerConfigMBeanImplTest
{
    @Mock
    private ContentTransformerRegistry transformerRegistry;

    @Mock
    private LegacyTransformerDebug transformerDebug;

    @Mock
    private TransformerConfig transformerConfig;

    @Mock
    private MimetypeService mimetypeService;

    // Would like to use mockito, see getTransformationLogTest(),
    // but getting the error WrongTypeOfReturnValue: String[] cannot be returned by getUpperMaxEntries()
    // looks like the wrong method is being stubbed, when stubbing getEntries()
    private TransformerLog transformerLog = new TransformerLog()
    {
        @Override
        public String[] getEntries(int n)
        {
            return logEntries.toArray(new String[logEntries.size()]);
        }
    };
    
    // Would like to use mockito, see transformerLog comment above
    private TransformerDebugLog transformerDebugLog = new TransformerDebugLog()
    {
        @Override
        public String[] getEntries(int n)
        {
            return logEntries.toArray(new String[logEntries.size()]);
        }
    };

    private final List<String> logEntries = new ArrayList<String>(); 
    
    private TransformerConfigMBeanImpl mbean;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        mbean = new TransformerConfigMBeanImpl();
        mbean.setContentTransformerRegistry(transformerRegistry);
        mbean.setTransformerDebug(transformerDebug);
        mbean.setTransformerConfig(transformerConfig);
        mbean.setMimetypeService(mimetypeService);
        mbean.setTransformerLog(transformerLog);
        mbean.setTransformerDebugLog(transformerDebugLog);

        mockMimetypes(mimetypeService,
                "application/pdf", "pdf",
                "image/png",       "png",
                "text/plain",      "txt");
    }

    @Test
    public void getExtensionsAndMimetypesTest()
    {
        when(mimetypeService.getMimetypes(null)).thenReturn(Arrays.asList(new String[] { "application/pdf", "image/png" }));
        when(mimetypeService.getExtension("application/pdf")).thenReturn("pdf");
        when(mimetypeService.getExtension("image/png")).thenReturn("png");
        
        String[] actual = mbean.getExtensionsAndMimetypes();
        String[] expected = new String[] { "pdf - application/pdf", "png - image/png" };
        assertArrayEquals(expected, actual);
    }
    
    @Test
    public void getTransformationsByExtensionTest()
    {
        setupForGetTransformationsByExtension();
        assertEquals("One result", mbean.getTransformationsByExtension("pdf", "png", null));
    }
    
    @Test
    public void getTransformationsByExtensionUpperCaseTest()
    {
        setupForGetTransformationsByExtension();
        assertEquals("One result", mbean.getTransformationsByExtension("PDF", "PNG", null));
    }
    
    @Test
    public void getTransformationsByExtensionNullSourceTest()
    {
        setupForGetTransformationsByExtension();
        assertEquals("Lots of results to png", mbean.getTransformationsByExtension(null, "PNG", null));
    }
    
    @Test
    public void getTransformationsByExtensionNullTargetTest()
    {
        setupForGetTransformationsByExtension();
        assertEquals("Lots of results from pdf", mbean.getTransformationsByExtension("pdf", null, null));
    }
    
    private void setupForGetTransformationsByExtension()
    {
        when(transformerDebug.transformationsByExtension("pdf", "png", true, true, false, null)).thenReturn("One result");
        when(transformerDebug.transformationsByExtension(null, "png", true, true, false, null)).thenReturn("Lots of results to png");
        when(transformerDebug.transformationsByExtension("pdf", null, true, true, false, null)).thenReturn("Lots of results from pdf");
    }
    
    @Test
    public void getTransformationStatisticsTransformer1FromToTest()
    {
        setupForGetTransformationStatistics();
        // Should not be a transformer summary as there might be other transforms and the
        // totals would not add up
        assertEquals(
                "transformer.transformer1 pdf png count=10 errors=0 averageTime=200 ms",
                mbean.getTransformationStatistics("transformer1", "pdf", "png"));
    }
    
    @Test
    public void getTransformationStatisticsTransformer1AllTest()
    {
        setupForGetTransformationStatistics();
        // Should be transformer summaries as all transforms were requested and the
        // totals will add up
        assertEquals(
                "transformer.transformer1 * * count=30 errors=0 averageTime=133 ms\n" +
                "transformer.transformer1 pdf png count=10 errors=0 averageTime=200 ms\n" +
                "transformer.transformer1 txt png count=20 errors=0 averageTime=100 ms",
                mbean.getTransformationStatistics("transformer1", null, null));
    }
    
    @Test
    public void getTransformationStatisticsFromToTest()
    {
        setupForGetTransformationStatistics();
        // Should be an overall summary as the transformer is not specified
        // Should not be a transformer summary as there 'might' be other transforms and the
        // totals would not add up
        assertEquals(
                "SUMMARY pdf png count=10 errors=0 averageTime=200 ms\n" +
                "SUMMARY txt png count=24 errors=0 averageTime=234 ms\n" +
                "\n" +
                "transformer.transformer1 pdf png count=10 errors=0 averageTime=200 ms\n" +
                "transformer.transformer1 txt png count=20 errors=0 averageTime=100 ms\n" +
                "\n" +
                "transformer.transformer2 txt png count=4 errors=0 averageTime=654 ms",
                mbean.getTransformationStatistics(null, null, "png"));
    }
    
    @Test
    public void getTransformationStatisticsAllTest()
    {
        setupForGetTransformationStatistics();
        // Should be an overall summary as the transformer is not specified
        // Should be a transformer1 summary but not for transformer2 as it only has done txt->png
        assertEquals(
                "SUMMARY * * count=34 errors=0 averageTime=222 ms\n" +
                "SUMMARY pdf png count=10 errors=0 averageTime=200 ms\n" +
                "SUMMARY txt png count=24 errors=0 averageTime=234 ms\n" +
                "\n" +
                "transformer.transformer1 * * count=30 errors=0 averageTime=133 ms\n" +
                "transformer.transformer1 pdf png count=10 errors=0 averageTime=200 ms\n" +
                "transformer.transformer1 txt png count=20 errors=0 averageTime=100 ms\n" +
                "\n" +
                "transformer.transformer2 txt png count=4 errors=0 averageTime=654 ms",
                mbean.getTransformationStatistics(null, null, null));
    }
    
    @Test
    public void getTransformationStatisticsNoneTest()
    {
        setupForGetTransformationStatistics();
        assertEquals(
                "No transformations to report",
                mbean.getTransformationStatistics("transformer1", "png", "pdf"));
    }
    
    @SuppressWarnings("unchecked")
    private void setupForGetTransformationStatistics()
    {
        ContentTransformer transformer1 = (ContentTransformer) new DummyContentTransformer("transformer.transformer1");
        ContentTransformer transformer2 = (ContentTransformer) new DummyContentTransformer("transformer.transformer2");

        Collection<ContentTransformer> transformerList1=Arrays.asList(new ContentTransformer[] {transformer1});
        when(transformerDebug.sortTransformersByName("transformer.transformer1")).thenReturn(
                    transformerList1);
        Collection<ContentTransformer> transformerList2=Arrays.asList(new ContentTransformer[] {transformer1, transformer2});
        when(transformerDebug.sortTransformersByName(null)).thenReturn(
                    transformerList2);

        when(transformerDebug.getSourceMimetypes("pdf")).thenReturn(Collections.singletonList("application/pdf"));
        when(transformerDebug.getSourceMimetypes("png")).thenReturn(Collections.singletonList("image/png"));
        when(transformerDebug.getSourceMimetypes("txt")).thenReturn(Collections.singletonList("text/plain"));
        when(transformerDebug.getSourceMimetypes(null)).thenReturn(Arrays.asList(new String[] {"application/pdf", "image/png", "text/plain"}));

        when(transformerDebug.getTargetMimetypes(any(), eq("pdf"), (Collection<String>) any())).thenReturn(Collections.singletonList("application/pdf"));
        when(transformerDebug.getTargetMimetypes(any(), eq("png"), (Collection<String>) any())).thenReturn(Collections.singletonList("image/png"));
        when(transformerDebug.getTargetMimetypes(any(), eq("txt"), (Collection<String>) any())).thenReturn(Collections.singletonList("text/plain"));
        when(transformerDebug.getTargetMimetypes(any(), (String)eq(null), (Collection<String>) any())).thenReturn(Arrays.asList(new String[] {"application/pdf", "image/png", "text/plain"}));
        
        when(transformerConfig.getStatistics(null, null, null, false)).thenReturn(
                new TransformerStatisticsImpl(mimetypeService, "*", "*", null, null, 130000, 222, 34));
        when(transformerConfig.getStatistics(null, "application/pdf", "image/png", false)).thenReturn(
                new TransformerStatisticsImpl(mimetypeService, "application/pdf", "image/png", null, null, 130001, 200, 10));
        when(transformerConfig.getStatistics(null, "text/plain", "image/png", false)).thenReturn(
                new TransformerStatisticsImpl(mimetypeService, "text/plain", "image/png", null, null, 130002, 234, 24));

        when(transformerConfig.getStatistics(transformer1, "application/pdf", "image/png", false)).thenReturn(
                new TransformerStatisticsImpl(mimetypeService, "application/pdf", "image/png", transformer1, null, 120000, 200, 10));
        when(transformerConfig.getStatistics(transformer1, "text/plain", "image/png", false)).thenReturn(
                new TransformerStatisticsImpl(mimetypeService, "text/plain", "image/png", transformer1, null, 120001, 100, 20));
        when(transformerConfig.getStatistics(transformer1, null, null, false)).thenReturn(
                new TransformerStatisticsImpl(mimetypeService, "*", "*", transformer1, null, 120002, 133, 30));

        when(transformerConfig.getStatistics(transformer2, "text/plain", "image/png", false)).thenReturn(
                new TransformerStatisticsImpl(mimetypeService, "text/plain", "image/png", transformer2, null, 120003, 654, 4));
        when(transformerConfig.getStatistics(transformer2, null, null, false)).thenReturn(
                new TransformerStatisticsImpl(mimetypeService, "*", "*", transformer2, null, 120004, 654, 4));
    }
    
    @Test
    public void getTransformationLogTest()
    {
//      when(transformerLog.getEntries(5)).thenReturn(new String[] {"test message 1", "test message 2"});
        logEntries.add("test message 1");
        logEntries.add("test message 2");
        assertArrayEquals(new String[] {"test message 1", "test message 2"}, mbean.getTransformationLog(5));
    }
    
    @Test
    public void getTransformationLogZeroTest()
    {
//      when(transformerLog.getEntries(5)).thenReturn(new String[0]);
        assertArrayEquals(new String[] {"No transformations to report"}, mbean.getTransformationLog(5));
    }
    
    @Test
    public void getTransformationDebugLogTest()
    {
//      when(transformerDebugLog.getEntries(5)).thenReturn(new String[] {"test message 1", "test message 2"});
        logEntries.add("test message 1");
        logEntries.add("test message 2");
        assertArrayEquals(new String[] {"test message 1", "test message 2"}, mbean.getTransformationDebugLog(5));
    }
    
    @Test
    public void getTransformationDebugLogZeroTest()
    {
//      when(transformerDebugLog.getEntries(5)).thenReturn(new String[0]);
        assertArrayEquals(new String[] {"No transformations to report"}, mbean.getTransformationDebugLog(5));
    }
    
    @Test
    public void getPropertiesTest()
    {
        when(transformerConfig.getProperties(false)).thenReturn("some properties");
        assertEquals("some properties", mbean.getProperties(true));
    }
    
    @Test
    public void setPropertiesTest()
    {
        when(transformerConfig.setProperties("abc")).thenReturn(12);
        assertEquals("Properties added or changed: 12", mbean.setProperties("abc"));
    }
    
    @Test
    public void setPropertiesDataProblemTest()
    {
        when(transformerConfig.setProperties("abc=12\nabc=1")).thenThrow(new IllegalArgumentException("abc has been specified more than once"));
        assertEquals("abc has been specified more than once", mbean.setProperties("abc=12\nabc=1"));
    }
    
    @Test
    public void removePropertiesTest()
    {
        when(transformerConfig.removeProperties("abc")).thenReturn(1);
        assertEquals("Properties removed: 1", mbean.removeProperties("abc"));
    }
    
    @Test
    public void removePropertiesDataProblemTest()
    {
        when(transformerConfig.removeProperties("abc")).thenThrow(new IllegalArgumentException("Unexpected property: abc Does not exist"));
        assertEquals("Unexpected property: abc Does not exist", mbean.removeProperties("abc"));
    }

    @Test
    public void testTransformAnyTransformerTest()
    {
        when(transformerDebug.testTransform("pdf", "png", null)).thenReturn("debug output");
        assertEquals("debug output", mbean.testTransform("String", "pdf", "png", null));
    }
    
    @Test
    public void testTransformAnyTransformerBadExtensionTest()
    {
        when(transformerDebug.testTransform("bad", "png", null)).thenThrow(new IllegalArgumentException("Unknown source extension: bad"));
        assertEquals("Unknown source extension: bad", mbean.testTransform(null, "bad", "png", null));
    }
}
