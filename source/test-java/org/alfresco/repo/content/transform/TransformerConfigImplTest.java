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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

/**
 * Test class for TransformerConfigImpl. This class generally calls onto
 * secondary classes to handle the request, so most test simply check that
 * the real method is called.
 * 
 * @author Alan Davis
 */
public class TransformerConfigImplTest
{
    @Mock
    private ApplicationContext applicationContext;
    
    @Mock
    private MimetypeService mimetypeService;
    
    @Mock
    private ContentTransformerRegistry transformerRegistry;

    @Mock
    private TransformerLog transformerLog;
    
    @Mock
    private TransformerDebugLog transformerDebugLog;

    @Mock
    private ChildApplicationContextFactory subsystem;
    
    @Mock
    private TransformerProperties transformerProperties;
    
    @Mock
    private TransformationOptions options;
    
    @Mock
    private
    ContentTransformer transformer1;
    
    private TransformerConfigImpl config;
    
    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        Properties globalProperties = new Properties();
        
        config = new TransformerConfigImpl()
        {
            @Override
            synchronized ChildApplicationContextFactory getSubsystem()
            {
                return subsystem;
            }
        };
        config.setApplicationContext(applicationContext);
        config.setMimetypeService(mimetypeService);
        config.setContentTransformerRegistry(transformerRegistry);
        config.setTransformerLog(transformerLog);
        config.setTransformerDebugLog(transformerDebugLog);
        config.setGlobalProperties(globalProperties);

        mockMimetypes(mimetypeService,
                "application/pdf", "pdf",
                "image/png",       "png");
        
        finishSetup();
    }
    
    private void finishSetup()
    {
        when(transformer1.getName()).thenReturn("transformer.abc");
        when(transformerRegistry.getTransformer("transformer.abc")).thenReturn(transformer1);
        when(transformer1.getComments(false)).thenReturn("");

        config.initialise();
    }

    /**
     * Mock up the responses from the subsystem so that it returns all the supplied
     * property names and values.
     * @param subsystem to mock the return values
     * @param namesAndValues a sequence of property names and values.
     * @throws IllegalStateException if there is not a value for every property
     */
    public static void mockProperties(ChildApplicationContextFactory subsystem, String... namesAndValues)
    {
        if (namesAndValues.length % 2 != 0)
        {
            // Not using IllegalArgumentException as this is thrown by classes under test
            throw new java.lang.IllegalStateException("There should be a value for every property");
        }
        
        final Set<String> propertyNames = new TreeSet<String>();
        for (int i=0; i < namesAndValues.length; i+=2)
        {
            propertyNames.add(namesAndValues[i]);
            when(subsystem.getProperty(namesAndValues[i])).thenReturn(namesAndValues[i+1]);
        }
        when(subsystem.getPropertyNames()).thenReturn(propertyNames);
    }
    
    @Test
    public void getPropertyTest()
    {
        when(subsystem.getProperty("abc")).thenReturn("xyz");
        String actual = config.getProperty("abc");
        assertEquals("xyz", actual);
    }

    @Test
    public void getPropertiesTest()
    {
        when(transformerLog.getPropertyName()).thenReturn("transformer.log.entries");
        when(transformerDebugLog.getPropertyName()).thenReturn("transformer.debug.entries");
        
        String actual = config.getProperties(false);
        assertEquals("# LOG and DEBUG history sizes\n" +
                "# ===========================\n" +
                "# Use small values as these logs are held in memory. 0 to disable.\n" +
                "transformer.log.entries=0  # default=50\n" +
                "# transformer.debug.entries=0\n", actual);
    }
    
    @Test
    public void setPropertiesTest()
    {
        config.setProperties("transformer.debug.entries=56\ntransformer.log.entries=76");
        Map<String, String> expected = new HashMap<String, String>();
        expected.put("transformer.debug.entries", "56");
        expected.put("transformer.log.entries", "76");
        verify(subsystem).setProperties(expected);
    }
    
    @Test
    public void removePropertiesTest()
    {
        mockProperties(subsystem, "content.transformer.abc.extensions.pdf.png.maxPages", "23");
        finishSetup();

        config.removeProperties("content.transformer.abc.extensions.pdf.png.maxPages");
        Set<String> expected = new HashSet<String>();
        expected.add("content.transformer.abc.extensions.pdf.png.maxPages");
        verify(subsystem).removeProperties(expected);
    }
    
    @Test
    public void getStatisticsTest()
    {
        TransformerStatistics actual = config.getStatistics(transformer1, "application/pdf", "image/png", true);
        actual.recordTime(100);
        actual.recordTime(200);
        actual = config.getStatistics(transformer1, "application/pdf", "image/png", false);
        assertEquals(150, actual.getAverageTime());
    }
    
    @Test
    public void getLimitsTest()
    {
        mockProperties(subsystem, "content.transformer.abc.extensions.pdf.png.maxPages", "23");
        finishSetup();

        TransformationOptionLimits actual = config.getLimits(transformer1, "application/pdf", "image/png", null);
        assertEquals(23, actual.getMaxPages());
    }
    
    @Test
    public void isSupportedTransformationTest()
    {
        mockProperties(subsystem, "content.transformer.abc.extensions.pdf.png.suppoprted", "true");
        finishSetup();

        assertTrue(config.isSupportedTransformation(transformer1, "application/pdf", "image/png", options));
    }

    @Test
    public void getPriorityTest()
    {
        mockProperties(subsystem,
                "content.transformer.default.priority",                "22",
                "content.transformer.abc.extensions.pdf.png.priority", "67");
        finishSetup();

        assertEquals(67, config.getPriority(transformer1, "application/pdf", "image/png"));
    }

    @Test
    public void getPriorityBadTest()
    {
        mockProperties(subsystem,
                "content.transformer.default.priority",                "22",
                "content.transformer.abc.extensions.pdf.png.priority", "bad");
        finishSetup();

        assertEquals(22, config.getPriority(transformer1, "application/pdf", "image/png"));
    }

    @Test
    public void getThresholdCountTest()
    {
        mockProperties(subsystem,
                "content.transformer.default.thresholdCount",                "22",
                "content.transformer.abc.extensions.pdf.png.thresholdCount", "67");
        finishSetup();

        assertEquals(67, config.getThresholdCount(transformer1, "application/pdf", "image/png"));
    }

    @Test
    public void getThresholdCountBadTest()
    {
        mockProperties(subsystem,
                "content.transformer.default.thresholdCount",                "22",
                "content.transformer.abc.extensions.pdf.png.thresholdCount", "bad");
        finishSetup();

        assertEquals(22, config.getThresholdCount(transformer1, "application/pdf", "image/png"));
    }

    @Test
    public void getErrorTimeTest()
    {
        mockProperties(subsystem,
                "content.transformer.default.errorTime",                "22",
                "content.transformer.abc.extensions.pdf.png.errorTime", "67");
        finishSetup();

        assertEquals(67, config.getErrorTime(transformer1, "application/pdf", "image/png"));
    }

    @Test
    public void getErrorTimeBadTest()
    {
        mockProperties(subsystem,
                "content.transformer.default.errorTime",                "22",
                "content.transformer.abc.extensions.pdf.png.errorTime", "bad");
        finishSetup();

        assertEquals(22, config.getErrorTime(transformer1, "application/pdf", "image/png"));
    }

    @Test
    public void getInitialAverageTimeTest()
    {
        mockProperties(subsystem,
                "content.transformer.default.time",                "22",
                "content.transformer.abc.extensions.pdf.png.time", "67");
        finishSetup();

        assertEquals(67, config.getInitialAverageTime(transformer1, "application/pdf", "image/png"));
    }

    @Test
    public void getInitialAverageTimeBadTest()
    {
        mockProperties(subsystem,
                "content.transformer.default.time",                "22",
                "content.transformer.abc.extensions.pdf.png.time", "bad");
        finishSetup();

        assertEquals(22, config.getInitialAverageTime(transformer1, "application/pdf", "image/png"));
    }

    @Test
    public void getInitialCountTest()
    {
        mockProperties(subsystem,
                "content.transformer.default.count",                "22",
                "content.transformer.abc.extensions.pdf.png.count", "67");
        finishSetup();

        assertEquals(67, config.getInitialCount(transformer1, "application/pdf", "image/png"));
    }

    @Test
    public void getInitialCountBadTest()
    {
        mockProperties(subsystem,
                "content.transformer.default.count",                "22",
                "content.transformer.abc.extensions.pdf.png.count", "bad");
        finishSetup();

        assertEquals(22, config.getInitialCount(transformer1, "application/pdf", "image/png"));
    }
}
