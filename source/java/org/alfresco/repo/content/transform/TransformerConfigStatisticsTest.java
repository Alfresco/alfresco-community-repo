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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for TransformerConfigStatistics.
 * 
 * @author Alan Davis
 */
public class TransformerConfigStatisticsTest
{
    @Mock
    private TransformerConfigImpl transformerConfig;

    @Mock
    private MimetypeService mimetypeService;
    
    private ContentTransformer transformer1;

    private TransformerConfigStatistics stats;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        stats = new TransformerConfigStatistics(transformerConfig, mimetypeService);
        
        transformer1 = new DummyContentTransformer("transformer.transformer1");

        mockMimetypes(mimetypeService,
                "application/pdf", "pdf",
                "image/png",       "png",
                "text/plain",      "txt");
    }
    
    @Test
    public void createTest()
    {
        TransformerStatistics statistics = stats.getStatistics(transformer1, "application/pdf", "image/png", true);
        assertTrue(statistics != null);
        assertEquals(0L, statistics.getAverageTime());
        assertEquals(0L, statistics.getCount());
        assertEquals(0L, statistics.getErrorCount());
        assertEquals("pdf", statistics.getSourceExt());
        assertEquals("png", statistics.getTargetExt());
        assertEquals("transformer.transformer1", statistics.getTransformerName());
    }

    @Test
    public void createSetAverageTimeTest()
    {
        when(transformerConfig.getInitialAverageTime(transformer1, "application/pdf", "image/png")).thenReturn(12345L);
        when(transformerConfig.getInitialCount(transformer1, "application/pdf", "image/png")).thenReturn(123);

        TransformerStatistics statistics = stats.getStatistics(transformer1, "application/pdf", "image/png", true);
        assertTrue(statistics != null);
        assertEquals(12345L, statistics.getAverageTime());
        assertEquals(123L, statistics.getCount());
    }

    @Test
    public void create0CountOnSetAverageTimeTest()
    {
        // getInitialCount should be defaulted to 0 if the average time is set and there is no default
        when(transformerConfig.getInitialAverageTime(transformer1, "application/pdf", "image/png")).thenReturn(12345L);

        TransformerStatistics statistics = stats.getStatistics(transformer1, "application/pdf", "image/png", true);
        assertTrue(statistics != null);
        assertEquals(12345L, statistics.getAverageTime());
        assertEquals(0L, statistics.getCount());
    }
    
    @Test
    public void createSetErrorTimeTest()
    {
        when(transformerConfig.getErrorTime(transformer1, "application/pdf", "image/png")).thenReturn(12345L);

        TransformerStatistics statistics = stats.getStatistics(transformer1, "application/pdf", "image/png", true);
        statistics.recordError(100L); // 100 does not get used if errorTime is set

        assertEquals(12345L, statistics.getAverageTime());
        assertEquals(1L, statistics.getCount());
        assertEquals(1L, statistics.getErrorCount());
    }
    
    @Test
    public void createNoSetErrorTimeTest()
    {
        TransformerStatistics statistics = stats.getStatistics(transformer1, "application/pdf", "image/png", true);
        statistics.recordError(100L);

        assertEquals(100L, statistics.getAverageTime());
        assertEquals(1L, statistics.getCount());
        assertEquals(1L, statistics.getErrorCount());
    }
    
    @Test
    public void mayCreateButExistsTest()
    {
        TransformerStatistics statistics = stats.getStatistics(transformer1, "application/pdf", "image/png", true);
        statistics.recordTime(100L);

        // Call again (with createNew=true) and then make sure it is the same object returned
        statistics = stats.getStatistics(transformer1, "application/pdf", "image/png", true);
        assertEquals(100L, statistics.getAverageTime());
    }
    
    @Test
    public void mayNotCreateButExistsTest() 
    {
        TransformerStatistics statistics = stats.getStatistics(transformer1, "application/pdf", "image/png", true);
        statistics.recordTime(100L);

        // Call again (with createNew=false) and then make sure it is the same object returned
        statistics = stats.getStatistics(transformer1, "application/pdf", "image/png", false);
        assertEquals(100L, statistics.getAverageTime());
    }
    
    @Test
    public void doesNotExistTest()
    {
        assertEquals(null, stats.getStatistics(transformer1, "application/pdf", "image/png", false));
    }
    
    @Test
    // i.e. the system wide summary
    public void nullTransformerTest()
    {
        TransformerStatistics statistics = stats.getStatistics(null, "application/pdf", "image/png", true);
        assertEquals("SUMMARY", statistics.getTransformerName());
    }
    
    @Test
    public void nullMimetypesTest()
    {
        TransformerStatistics statistics = stats.getStatistics(transformer1, null, null, true);
        assertEquals("transformer.transformer1", statistics.getTransformerName());
        assertEquals("*", statistics.getSourceExt());
        assertEquals("*", statistics.getTargetExt());
    }
    
    @Test
    public void createSummaryTooTest()
    {
        // Same as createTest()
        TransformerStatistics statistics = stats.getStatistics(transformer1, "application/pdf", "image/png", true);

        // The summary for the transformer should also have been created
        statistics = stats.getStatistics(transformer1, "*", "*", false);
        assertTrue(statistics != null);
        assertEquals(0L, statistics.getAverageTime());
        assertEquals(0L, statistics.getCount());
        assertEquals(0L, statistics.getErrorCount());
        assertEquals("*", statistics.getSourceExt()); // pdf in createTest
        assertEquals("*", statistics.getTargetExt()); // png in createTest
        assertEquals("transformer.transformer1", statistics.getTransformerName());
    }
    
    @Test
    // check the transformer summary gets reused
    public void transformerSummaryTest()
    {
        // Create pdf->png and summary from transformer1
        TransformerStatistics pdfToPng = stats.getStatistics(transformer1, "application/pdf", "image/png", true);
        TransformerStatistics summary = stats.getStatistics(transformer1, "*", "*", false);
        
        // Create txt->png for transformer1
        TransformerStatistics txtToPng = stats.getStatistics(transformer1, "text/plain", "image/png", true);
        
        pdfToPng.recordTime(100);
        txtToPng.recordTime(200);
        
        assertEquals(2, summary.getCount());
        assertEquals(150L, summary.getAverageTime());
    }

    @Test
    // check the system wide statistics gather up values
    public void systemWideSummaryTest()
    {
        TransformerStatistics transformer1PdfToPng = stats.getStatistics(transformer1, "application/pdf", "image/png", true);
        TransformerStatistics transformer1TxtToPng = stats.getStatistics(transformer1, "text/plain", "image/png", true);
        
        ContentTransformer transformer2 = new DummyContentTransformer("transformer.transformer2");
        TransformerStatistics transformer2PdfToPng = stats.getStatistics(transformer2, "application/pdf", "image/png", true);
        TransformerStatistics transformer2TxtToPdf = stats.getStatistics(transformer2, "text/plain", "application/pdf", true);

        TransformerStatistics summaryPdfToPng = stats.getStatistics(null, "application/pdf", "image/png", true);
        TransformerStatistics summaryTxtToPng = stats.getStatistics(null, "text/plain", "image/png", true);
        TransformerStatistics summaryTxtToPdf = stats.getStatistics(null, "text/plain", "application/pdf", true);
        TransformerStatistics summary = stats.getStatistics(null, "*", "*", false);
       
        // Run a few transforms
        recordTime(summaryPdfToPng, transformer1PdfToPng, 100);
        recordTime(summaryPdfToPng, transformer1PdfToPng, 100);
        recordTime(summaryPdfToPng, transformer2PdfToPng, 400);

        recordTime(summaryTxtToPng, transformer1TxtToPng, 200);

        recordTime(summaryTxtToPdf, transformer2TxtToPdf, 400);

        // Check summaries
        assertEquals(5, summary.getCount());
        assertEquals(3, summaryPdfToPng.getCount());
        assertEquals(1, summaryTxtToPng.getCount());
        assertEquals(1, summaryTxtToPdf.getCount());

        assertEquals(200L, summaryPdfToPng.getAverageTime());
        assertEquals(200L, summaryTxtToPng.getAverageTime());
        assertEquals(400L, summaryTxtToPdf.getAverageTime());
        assertEquals(240L, summary.getAverageTime()); // 100+100+400+200+400 = 1200
    }
    
    // Calls both transformer and summary recordTime() methods in the same way
    // AbstractContentTransformer2.recordTime(String, String, long) would do for
    // a top level transformation
    private void recordTime(TransformerStatistics summaryAToB, TransformerStatistics transformerAToB, long time)
    {
        transformerAToB.recordTime(time);
        summaryAToB.recordTime(time);
    }
}
