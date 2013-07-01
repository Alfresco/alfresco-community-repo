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

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.service.cmr.repository.TransformationOptions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for TransformerSelectorImpl.
 * 
 * @author Alan Davis
 */
public class TransformerSelectorImplTest
{
    private static final String PNG = "image/png";

    private static final String PDF = "application/pdf";

    @Mock
    private TransformerConfig transformerConfig;
    
    @Mock
    private ContentTransformerRegistry contentTransformerRegistry;
    
    @Mock
    private TransformationOptions options;
    
    @Mock
    private DummyContentTransformer transformer1;

    @Mock
    private DummyContentTransformer transformer2;

    @Mock
    private DummyContentTransformer transformer3;

    @Mock
    private DummyContentTransformer transformer4;
    
    private List<ContentTransformer> allTransformers;
    
    private TransformerSelectorImpl selector;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        selector = new TransformerSelectorImpl();
        selector.setTransformerConfig(transformerConfig);
        selector.setContentTransformerRegistry(contentTransformerRegistry);

        when(transformer1.getName()).thenReturn("transformer.1");
        when(transformer2.getName()).thenReturn("transformer.2");
        when(transformer3.getName()).thenReturn("transformer.3");
        when(transformer4.getName()).thenReturn("transformer.4");

        allTransformers = new ArrayList<ContentTransformer>();
        when(contentTransformerRegistry.getTransformers()).thenReturn(allTransformers);
    }

    private void mockTransformer(DummyContentTransformer transformer, int priority, String sourceMimetype, String targetMimetype)
    {
        when(transformerConfig.getPriority(transformer, sourceMimetype, targetMimetype)).thenReturn(priority);
        allTransformers.add(transformer);
    }
    
    private void mockTransformer(DummyContentTransformer transformer, int priority, String sourceMimetype, String targetMimetype,
            int count, int averageTime, int threshold)
    {
        mockTransformer(transformer, priority, sourceMimetype, targetMimetype);
        when(transformer.isTransformable(sourceMimetype, -1, targetMimetype, options)).thenReturn(true);
        when(transformerConfig.getStatistics(transformer, sourceMimetype, targetMimetype, true)).thenReturn(new DummyTransformerStatistics(count, averageTime));
        when(transformerConfig.getThresholdCount(transformer, sourceMimetype, targetMimetype)).thenReturn(threshold);

    }

    private void assertTransformers(String context, List<ContentTransformer> expected,
            List<ContentTransformer> actual)
    {
        String expectedNames = getNames(expected);
        String actualNames = getNames(actual);
        assertEquals(context, expectedNames, actualNames);
    }
    
    private String getNames(List<ContentTransformer> transformers)
    {
        StringBuilder sb = new StringBuilder();
        
        for (ContentTransformer transformer: transformers)
        {
            if (sb.length() > 0)
            {
                sb.append(' ');
            }
            sb.append(transformer.getName());
        }
        return sb.toString();
    }

    private void runMultipleSelections(int[][] data) throws Exception
    {
        // Run a sequence of selections up to the thresholds and beyond
        List<ContentTransformer> expected = null;
        for (int i=0; i<data.length; i++)
        {
            int[] test = data[i];
            setUp();
            
            mockTransformer(transformer1, 100, PDF, PNG, test[0], test[1], test[2]);
            mockTransformer(transformer2, 100, PDF, PNG);
            mockTransformer(transformer3, 100, PDF, PNG);
            mockTransformer(transformer4, 100, PDF, PNG, test[3], test[4], test[5]);
            
            expected = test[6] == 1
                    ? Arrays.asList(new ContentTransformer[] {transformer1, transformer4})
                    : Arrays.asList(new ContentTransformer[] {transformer4, transformer1});
            List<ContentTransformer> actual = selector.selectTransformers(PDF, -1, PNG, options);
            assertTransformers(i+" multiple", expected, actual);
        }
    }

    @Test
    public void firstRunUnderThresholdTest()
    {
        // 1 and 4 can do PDF->PNG, same priority (100) and neither have not been run 
        mockTransformer(transformer1, 100, PDF, PNG, 0,  0, 3);
        mockTransformer(transformer2, 100, PDF, PNG);
        mockTransformer(transformer3, 100, PDF, PNG);
        mockTransformer(transformer4, 100, PDF, PNG, 0,  0, 3);
        
        List<ContentTransformer> expected = Arrays.asList(new ContentTransformer[] {transformer1, transformer4});
        List<ContentTransformer> actual = selector.selectTransformers(PDF, -1, PNG, options);
        
        assertTransformers("", expected, actual);
    }

    @Test
    public void secondRunUnderThresholdOneRunTest()
    {
        // 1 and 4 can do PDF->PNG, same priority (100) and but 1 has been run 
        // Should continue to round robin to 2
        mockTransformer(transformer1, 100, PDF, PNG, 1,  23, 3);
        mockTransformer(transformer2, 100, PDF, PNG);
        mockTransformer(transformer3, 100, PDF, PNG);
        mockTransformer(transformer4, 100, PDF, PNG, 0,  0, 3);
        
        List<ContentTransformer> expected = Arrays.asList(new ContentTransformer[] {transformer4, transformer1});
        List<ContentTransformer> actual = selector.selectTransformers(PDF, -1, PNG, options);
        
        assertTransformers("", expected, actual);
    }

    @Test
    public void thirdRunUnderThresholdTwoRunTest()
    {
        // 1 and 4 can do PDF->PNG, same priority (100) and both have been run, but still under threshold 
        // Note: If the threshold was not taken into account 4 would be selected
        // Should continue to round robin
        mockTransformer(transformer1, 100, PDF, PNG, 1,  23, 3);
        mockTransformer(transformer2, 100, PDF, PNG);
        mockTransformer(transformer3, 100, PDF, PNG);
        mockTransformer(transformer4, 100, PDF, PNG, 1,  16, 3);
        
        List<ContentTransformer> expected = Arrays.asList(new ContentTransformer[] {transformer1, transformer4});
        List<ContentTransformer> actual = selector.selectTransformers(PDF, -1, PNG, options);
        
        assertTransformers("", expected, actual);
    }

    @Test
    public void multipleTestSameThreshold() throws Exception
    {
        int[][] data = new int[][]
        {
                { 0,  0,  3,    0,  0,  3,   1}, // same as firstRunUnderThresholdTest
                { 1, 23,  3,    0,  0,  3,   4}, // same as secondRunUnderThresholdTest
                { 1, 23,  3,    1, 16,  3,   1}, // same as thirdRunUnderThresholdTwoRunTest
                { 2, 25,  3,    1, 16,  3,   4},
                { 2, 25,  3,    2, 27,  3,   1},
                { 3, 24,  3,    2, 27,  3,   4},
                { 3, 24,  3,    3, 27,  3,   1},
                
                // Threshold past for both transformers - 1 was faster
                { 4, 25,  3,    3, 27,  3,   1},
                { 5, 22,  3,    3, 27,  3,   1},
                { 6, 21,  3,    3, 27,  3,   1},
                
                // 1 is slower than 4 now, so it switches
                { 7, 28,  3,    3, 27,  3,   4},
                { 7, 28,  3,    4, 26,  3,   4},
        };
        
        runMultipleSelections(data);
    }

    @Test
    public void multipleTestDifferentThreshold() throws Exception
    {
        int[][] data = new int[][]
        {
                { 0,  0,  2,    0,  0,  4,   1},
                { 1, 23,  2,    0,  0,  4,   4},
                { 1, 23,  2,    1, 16,  4,   1},
                
                // Threshold past for 1 but not 4
                { 2, 25,  2,    1, 16,  4,   4},
                { 2, 25,  2,    2, 27,  4,   4},
                { 2, 25,  2,    3, 22,  4,   4},
                
                // Threshold past for both transformers
                { 2, 25,  2,    4, 22,  4,   4},
                { 2, 25,  2,    5, 29,  4,   1},
                { 3, 28,  2,    5, 29,  4,   1},

                // 1 is slower than 4 now, so it switches
                { 4, 30,  2,    5, 29,  4,   4},
        };
        
        runMultipleSelections(data);
    }

    @Test
    public void priorityTest()
    {
        // 1 and 4 can do PDF->PNG, but 4 is higher priority, even though 1 appears faster 
        mockTransformer(transformer1, 100, PDF, PNG, 10, 22, 3);
        mockTransformer(transformer2, 100, PDF, PNG);
        mockTransformer(transformer3, 100, PDF, PNG);
        mockTransformer(transformer4,  50, PDF, PNG, 10, 44, 3);
        
        List<ContentTransformer> expected = Arrays.asList(new ContentTransformer[] {transformer4, transformer1});
        List<ContentTransformer> actual = selector.selectTransformers(PDF, -1, PNG, options);
        
        assertTransformers("", expected, actual);
    }
    
    @Test
    public void priorityAndTimeTest()
    {
        // 1, 2 and 4 can do PDF->PNG, but 4 is higher priority, even though 1 and 2 appear faster 
        mockTransformer(transformer1, 100, PDF, PNG, 10, 22, 3);
        mockTransformer(transformer2, 100, PDF, PNG, 10, 23, 3);
        mockTransformer(transformer3, 100, PDF, PNG);
        mockTransformer(transformer4,  50, PDF, PNG, 10, 44, 3);
        
        List<ContentTransformer> expected = Arrays.asList(new ContentTransformer[] {transformer4, transformer1, transformer2});
        List<ContentTransformer> actual = selector.selectTransformers(PDF, -1, PNG, options);
        
        assertTransformers("", expected, actual);
    }
    
    @Test
    public void priorityAndThresholdTest()
    {
        // 1, 2 and 4 can do PDF->PNG, but 4 is higher priority, even though 1 and 2 have not reached the threshold 
        mockTransformer(transformer1, 100, PDF, PNG, 0,  0, 3);
        mockTransformer(transformer2, 100, PDF, PNG, 0,  0, 3);
        mockTransformer(transformer3, 100, PDF, PNG);
        mockTransformer(transformer4,  50, PDF, PNG, 10, 44, 3);
        
        List<ContentTransformer> expected = Arrays.asList(new ContentTransformer[] {transformer4, transformer1, transformer2});
        List<ContentTransformer> actual = selector.selectTransformers(PDF, -1, PNG, options);
        
        assertTransformers("", expected, actual);
    }
}

class DummyTransformerStatistics extends TransformerStatisticsImpl
{
    private final long count;
    private final long averageTime;
    
    public DummyTransformerStatistics(long count, long averageTime)
    {
        super(null, null, null, null, null, 0L, 0L, 0L);
        this.count = count;
        this.averageTime = averageTime;
    }

    @Override
    public long getCount()
    {
        return count;
    }

    @Override
    public long getAverageTime()
    {
        return averageTime;
    }
}
