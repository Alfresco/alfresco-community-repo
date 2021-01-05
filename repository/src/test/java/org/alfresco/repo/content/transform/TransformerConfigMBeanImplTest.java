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

import org.alfresco.service.cmr.repository.MimetypeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Test class for TransformerConfigMBeanImpl.
 * 
 * @author Alan Davis
 */
public class TransformerConfigMBeanImplTest
{
    @Mock
    private AdminUiTransformerDebug transformerDebug;

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
        mbean.setTransformerDebug(transformerDebug);
        mbean.setMimetypeService(mimetypeService);
        mbean.setTransformerLog(transformerLog);
        mbean.setTransformerDebugLog(transformerDebugLog);

        mockMimetypes(mimetypeService,
                "application/pdf", "pdf",
                "image/png",       "png",
                "text/plain",      "txt");
    }

    /**
     * Mock up the responses from the mimetypeService so that it:
     * a) returns all the supplied mimetypes
     * b) returns the extension given the mimetype
     * c) returns the mimetype given the extension.
     * @param mimetypeService mimetype service
     * @param mimetypesAndExtensions sequence of mimetypes and extenstions.
     * @throws IllegalStateException if there is not an extension for every mimetype
     */
    public static void mockMimetypes(MimetypeService mimetypeService, String... mimetypesAndExtensions)
    {
        if (mimetypesAndExtensions.length % 2 != 0)
        {
            // Not using IllegalArgumentException as this is thrown by classes under test
            throw new java.lang.IllegalStateException("There should be an extension for every mimetype");
        }

        final Set<String> allMimetypes = new HashSet<String>();
        for (int i=0; i < mimetypesAndExtensions.length; i+=2)
        {
            allMimetypes.add(mimetypesAndExtensions[i]);
            when(mimetypeService.getExtension(mimetypesAndExtensions[i])).thenReturn(mimetypesAndExtensions[i+1]);
            when(mimetypeService.getMimetype(mimetypesAndExtensions[i+1])).thenReturn(mimetypesAndExtensions[i]);
        }
        when(mimetypeService.getMimetypes()).thenReturn(new ArrayList<String>(allMimetypes));
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
        assertEquals("One result", mbean.getTransformationsByExtension("pdf", "png"));
    }
    
    @Test
    public void getTransformationsByExtensionUpperCaseTest()
    {
        setupForGetTransformationsByExtension();
        assertEquals("One result", mbean.getTransformationsByExtension("PDF", "PNG"));
    }
    
    @Test
    public void getTransformationsByExtensionNullSourceTest()
    {
        setupForGetTransformationsByExtension();
        assertEquals("Lots of results to png", mbean.getTransformationsByExtension(null, "PNG"));
    }
    
    @Test
    public void getTransformationsByExtensionNullTargetTest()
    {
        setupForGetTransformationsByExtension();
        assertEquals("Lots of results from pdf", mbean.getTransformationsByExtension("pdf", null));
    }
    
    private void setupForGetTransformationsByExtension()
    {
        when(transformerDebug.transformationsByExtension("pdf", "png", true)).thenReturn("One result");
        when(transformerDebug.transformationsByExtension(null, "png", true)).thenReturn("Lots of results to png");
        when(transformerDebug.transformationsByExtension("pdf", null, true)).thenReturn("Lots of results from pdf");
    }
    
    @Test
    public void getTransformationLogTest()
    {
        logEntries.add("test message 1");
        logEntries.add("test message 2");
        assertArrayEquals(new String[] {"test message 1", "test message 2"}, mbean.getTransformationLog(5));
    }
    
    @Test
    public void getTransformationLogZeroTest()
    {
        assertArrayEquals(new String[] {"No transformations to report"}, mbean.getTransformationLog(5));
    }
    
    @Test
    public void getTransformationDebugLogTest()
    {
        logEntries.add("test message 1");
        logEntries.add("test message 2");
        assertArrayEquals(new String[] {"test message 1", "test message 2"}, mbean.getTransformationDebugLog(5));
    }
    
    @Test
    public void getTransformationDebugLogZeroTest()
    {
        assertArrayEquals(new String[] {"No transformations to report"}, mbean.getTransformationDebugLog(5));
    }
    
    @Test
    public void testTransformAnyTransformerTest()
    {
        when(transformerDebug.testTransform("pdf", "png")).thenReturn("debug output");
        assertEquals("debug output", mbean.testTransform("pdf", "png"));
    }
    
    @Test
    public void testTransformAnyTransformerBadExtensionTest()
    {
        when(transformerDebug.testTransform("bad", "png")).thenThrow(new IllegalArgumentException("Unknown source extension: bad"));
        assertEquals("Unknown source extension: bad", mbean.testTransform("bad", "png"));
    }
}
