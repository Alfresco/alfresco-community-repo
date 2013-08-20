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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for some of the regex methods and TransformerPropertyNameExtractor in general.
 * 
 * @author Alan Davis
 */
public class TransformerPropertyNameExtractorTest
{
    @Mock
    private TransformerProperties transformerProperties;

    @Mock
    private MimetypeService mimetypeService;

    private Map<TransformerSourceTargetSuffixKey, TransformerSourceTargetSuffixValue> map;
    
    private TestTransformerPropertyNameExtractor extractor;
    
    private Collection<String> suffixes = Collections.singleton(".suffix");
    
    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        
        extractor = new TestTransformerPropertyNameExtractor();

        mockMimetypes(mimetypeService,
                "application/pdf", "pdf",
                "image/png",       "png");
    }

    /**
     * Mock up the responses from the subsystem so that it returns all the supplied
     * property names and values.
     * @param transformerProperties to mock the return values
     * @param namesAndValues a sequence of property names and values.
     * @throws IllegalStateException if there is not a value for every property
     */
    public static void mockProperties(TransformerProperties transformerProperties, String... namesAndValues)
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
            when(transformerProperties.getProperty(namesAndValues[i])).thenReturn(namesAndValues[i+1]);
        }
        when(transformerProperties.getPropertyNames()).thenReturn(propertyNames);
    }
    
    /**
     * Mock up the responses from the mimetypeService so that it:
     * a) returns all the supplied mimetypes
     * b) returns the extension given the mimetype
     * c) returns the mimetype given the extension.
     * @param mimetypesAndExtensions sequence of mimetypes and extenstions.
     * @param transformerProperties to mock the return values
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
    
    /**
     * Asserts two List<String> are equal after they are sorted.
     * @param msg to be included in the assertEquals call.
     * @param expected list
     * @param actual list
     */
    public void assertEqualsLists(String msg, List<String> expected, List<String> actual)
    {
        if (expected.size() > 1)
        {
            Collections.sort(expected);
        }
        if (actual.size() > 1)
        {
            Collections.sort(actual);
        }
        
        assertEquals(msg, expected, actual);
    }
    
    @Test
    public void textSplitExt()
    {
        String[][] values = new String[][]
        {
                {"AAA.BBB", "AAA", "BBB"},
                {"AA\\.BB.CC\\.DD\\.EE", "AA.BB", "CC.DD.EE"},
        };
        
        for (String[] args: values)
        {
            String input = args[0];
            String expectedSource = args[1];
            String expectedTarget = args[2];
            
            String[] sourceTarget = extractor.splitExt(input);
            assertEquals("length", sourceTarget.length, 2);
            assertEquals("source", expectedSource, sourceTarget[0]);
            assertEquals("target", expectedTarget, sourceTarget[1]);
        }
    }
    
    @Test
    public void testPattern()
    {
        assertTrue( extractor.pattern("ABC").matcher("ABC").matches());
        assertFalse(extractor.pattern("ABC").matcher("x").matches());
        assertFalse(extractor.pattern("ABC").matcher("ABCD").matches());
        assertFalse(extractor.pattern("ABC").matcher("DABC").matches());
        
        assertTrue( extractor.pattern("*B").matcher("B").matches());
        assertTrue( extractor.pattern("*B").matcher("xxB").matches());
        assertFalse(extractor.pattern("*B").matcher("xxBx").matches());
        assertFalse(extractor.pattern("B*").matcher("").matches());
        
        assertTrue( extractor.pattern("C*").matcher("C").matches());
        assertTrue( extractor.pattern("C*").matcher("CxxB").matches());
        assertFalse(extractor.pattern("C*").matcher("xxBx").matches());
        
        assertTrue(extractor.pattern("D*E*F").matcher("DEF").matches());
        assertTrue(extractor.pattern("D*E*F").matcher("DxxExxF").matches());
        assertTrue(extractor.pattern("D*E*F").matcher("D*E*F").matches());
        
        assertTrue( extractor.pattern("A+").matcher("A+").matches());
        assertFalse(extractor.pattern("A+").matcher("AA").matches());
        assertFalse(extractor.pattern("A+").matcher("AAA").matches());
        assertFalse(extractor.pattern("A+").matcher("A+A").matches());
    }
    
    @Test
    public void getMatchingExtensionsFromMimetypesTest()
    {
        mockMimetypes(mimetypeService,
            "mimetypeAx", "aExt",
            "mimetypeBx", "bExt",
            "mimetypeC",  "cExt");

        String[][] data = new String[][]
        {
            {"*C",         "cExt"},
            {"*ypeAx",     "aExt"},
            {"mimetype*x", "aExt bExt"},
            {"mimetypeBx", "bExt"},
            {"*mime*",     "aExt bExt cExt"},
            {"*",          ANY}, // special meaning on its own
            {"",           null},
            {"bad",        null}
        };
        
        for (String[] entry: data)
        {
            List<String> expected = entry[1] == null ? Collections.<String>emptyList() : Arrays.asList(entry[1].split(" "));
            List<String> actual = extractor.getMatchingExtensionsFromMimetypes(entry[0], mimetypeService);
            assertEqualsLists("getMatchingExtensionsFromMimetypes("+entry[0]+")", expected, actual);
            
        }
    }
    
    @Test
    public void getMatchingExtensionsFromExtensionsTest()
    {
        mockMimetypes(mimetypeService,
                "mimetypeAx", "aExt",
                "mimetypeBx", "bExt",
                "mimetypeC",  "cExt");

        String[][] data = new String[][]
        {
            {"c*",   "cExt"},
            {"*a*",  "aExt"},
            {"a*xt", "aExt"},
            {"*Ext", "aExt bExt cExt"},
            {"*t",   "aExt bExt cExt"},
            {"*",    ANY}, // special meaning on its own
            {"",     null},
            {"bad",  null}
        };
        
        for (String[] entry: data)
        {
            List<String> expected = entry[1] == null ? Collections.<String>emptyList() : Arrays.asList(entry[1].split(" "));
            List<String> actual = extractor.getMatchingExtensionsFromExtensions(entry[0], mimetypeService);
            assertEqualsLists("getMatchingExtensionsFromMimetypes("+entry[0]+")", expected, actual);
        }
    }
    
    @Test
    public void transformerSourceTargetSuffixKeyHashCodeEqualsTest()
    {
        // Test data
        String[][] data = new String[][]
        {
            {"transformerName", "sourceExt", "targetExt", ".suffix", null},
            {"XXXXXXXXXXXXXXX", "sourceExt", "targetExt", ".suffix", null},
            {"transformerName", "XXXXXXXXX", "targetExt", ".suffix", null},
            {"transformerName", "sourceExt", "XXXXXXXXX", ".suffix", null},
            {"transformerName", "sourceExt", "targetExt", ".XXXXXX", null},
            {"transformerName", "sourceExt", "targetExt", ".suffix", "index"},
            {"XXXXXXXXXXXXXXX", "sourceExt", "targetExt", ".suffix", "index"},
            {"transformerName", "XXXXXXXXX", "targetExt", ".suffix", "index"},
            {"transformerName", "sourceExt", "XXXXXXXXX", ".suffix", "index"},
            {"transformerName", "sourceExt", "targetExt", ".XXXXXX", "index"}
        };
        
        for (int i=data.length+1; i >= 0; i--)
        {
            // Check properties are set
            Object a = i == data.length+1 ? "" : i == data.length ? null : new TransformerSourceTargetSuffixKey(data[i][0], data[i][1], data[i][2], data[i][3], data[i][4]);
            if (a instanceof TransformerSourceTargetSuffixKey)
            {
                assertEquals(i+" transformerName", ((TransformerSourceTargetSuffixKey)a).transformerName, data[i][0]); 
                assertEquals(i+" sourceExt",       ((TransformerSourceTargetSuffixKey)a).sourceExt,       data[i][1]); 
                assertEquals(i+" targetExt",       ((TransformerSourceTargetSuffixKey)a).targetExt,       data[i][2]); 
                assertEquals(i+" suffix",          ((TransformerSourceTargetSuffixKey)a).suffix,          data[i][3]); 
                assertEquals(i+" use",             ((TransformerSourceTargetSuffixKey)a).use,             data[i][4]); 
            }

            // Try out the hashCode and equals methods
            for (int j=data.length+1; j >= 0; j--)
            {
                Object b = j == data.length+1 ? "" : j == data.length ? null : new TransformerSourceTargetSuffixKey(data[j][0], data[j][1], data[j][2], data[j][3], data[j][4]);
                if (a != null)
                {
                    if (i == j)
                    {
                        assertTrue(    i+" "+j+" equals", a.equals(b));
                        assertEquals(i+" "+j+" hashCode", a.hashCode(), b.hashCode());
                    }
                    else
                    {
                        assertFalse(       i+" "+j+" equals ", a.equals(b));
                        if (b != null)
                        {
                            assertFalse(i+" "+j+" hashCode", a.hashCode() == b.hashCode());
                        }
                    }
                }
            }
        }
    }
    
    @Test
    public void transformerSourceTargetSuffixValueHashCodeEqualsTest()
    {
        mockMimetypes(mimetypeService,
                "sourceMimetype", "sourceExt",
                "sourceXXXXXXXX", "sourceXXX",
                "targetMimetype", "targetExt",
                "targetXXXXXXXX", "targetXXX");

        // Test data
        String[][] data = new String[][]
        {
            {"transformerName", "sourceExt", "targetExt", ".suffix",    null, "value", "sourceMimetype", "targetMimetype"},
            {"XXXXXXXXXXXXXXX", "sourceExt", "targetExt", ".suffix",    null, "value", "sourceMimetype", "targetMimetype"},
            {"transformerName", "sourceXXX", "targetExt", ".suffix",    null, "value", "sourceXXXXXXXX", "targetMimetype"},
            {"transformerName", "sourceExt", "targetXXX", ".suffix",    null, "value", "sourceMimetype", "targetXXXXXXXX"},
            {"transformerName", "sourceExt", "targetExt", ".XXXXXX",    null, "value", "sourceMimetype", "targetMimetype"},
            {"transformerName", "sourceExt", "targetExt", ".suffix",    null, "XXXXX", "sourceMimetype", "targetMimetype"},
            {"transformerName", "sourceExt", "targetExt", ".suffix", "index", "value", "sourceMimetype", "targetMimetype"},
            {"XXXXXXXXXXXXXXX", "sourceExt", "targetExt", ".suffix", "index", "value", "sourceMimetype", "targetMimetype"},
            {"transformerName", "sourceXXX", "targetExt", ".suffix", "index", "value", "sourceXXXXXXXX", "targetMimetype"},
            {"transformerName", "sourceExt", "targetXXX", ".suffix", "index", "value", "sourceMimetype", "targetXXXXXXXX"},
            {"transformerName", "sourceExt", "targetExt", ".XXXXXX", "index", "value", "sourceMimetype", "targetMimetype"},
            {"transformerName", "sourceExt", "targetExt", ".suffix", "index", "XXXXX", "sourceMimetype", "targetMimetype"}
        };
        
        for (int i=data.length+1; i >= 0; i--)
        {
            // Check properties are set
            Object a = i == data.length+1 ? "" : i == data.length ? null : new TransformerSourceTargetSuffixValue(data[i][0], data[i][1], data[i][2], data[i][3], data[i][4], data[i][5], mimetypeService);
            if (a instanceof TransformerSourceTargetSuffixValue)
            {
                assertEquals(i+" transformerName", data[i][0], ((TransformerSourceTargetSuffixValue)a).transformerName); 
                assertEquals(i+" sourceExt",       data[i][1], ((TransformerSourceTargetSuffixValue)a).sourceExt);
                assertEquals(i+" targetExt",       data[i][2], ((TransformerSourceTargetSuffixValue)a).targetExt);
                assertEquals(i+" suffix",          data[i][3], ((TransformerSourceTargetSuffixValue)a).suffix); 
                assertEquals(i+" use",             data[i][4], ((TransformerSourceTargetSuffixValue)a).use); 

                assertEquals(i+" value",           data[i][5], ((TransformerSourceTargetSuffixValue)a).value); 
                assertEquals(i+" sourceMimetype",  data[i][6], ((TransformerSourceTargetSuffixValue)a).sourceMimetype); 
                assertEquals(i+" targetMimetype",  data[i][7], ((TransformerSourceTargetSuffixValue)a).targetMimetype); 

                assertEquals(i+" key",
                        new TransformerSourceTargetSuffixKey(data[i][0], data[i][1], data[i][2], data[i][3], data[i][4]),
                        ((TransformerSourceTargetSuffixValue)a).key()); 
            }

            // Try out the hashCode and equals methods
            for (int j=data.length+1; j >= 0; j--)
            {
                Object b = j == data.length+1 ? "" : j == data.length ? null : new TransformerSourceTargetSuffixValue(data[j][0], data[j][1], data[j][2], data[j][3], data[j][4], data[j][5], mimetypeService);
                if (a != null)
                {
                    if (i == j)
                    {
                        assertTrue(    i+" "+j+" equals", a.equals(b));
                        assertEquals(i+" "+j+" hashCode", a.hashCode(), b.hashCode());
                    }
                    else
                    {
                        assertFalse(       i+" "+j+" equals ", a.equals(b));
                        if (b != null)
                        {
                            assertFalse(i+" "+j+" hashCode", a.hashCode() == b.hashCode());
                        }
                    }
                }
            }
        }
    }
    
    @Test
    public void transformerSourceTargetSuffixValuetoStringTest()
    {
        mockMimetypes(mimetypeService,
                "sourceMimetype", "sourceExt",
                "targetMimetype", "targetExt");

        // Test data
        String[][] data = new String[][]
        {
            {"transformerName", "sourceExt", "targetExt", ".suffix",    null, "value", "transformerName.extensions.sourceExt.targetExt.suffix=value"},
            {"transformerName", "sourceExt", "targetExt", ".suffix", "index", "value", "transformerName.extensions.sourceExt.targetExt.suffix.use.index=value"},
            {"transformerName",  ANY,         ANY,        ".suffix",    null, "value", "transformerName.suffix=value"}
        };
        
        for (int i=data.length-1; i >= 0; i--)
        {
            // Check properties are set
            TransformerSourceTargetSuffixValue a = new TransformerSourceTargetSuffixValue(data[i][0], data[i][1], data[i][2], data[i][3], data[i][4], data[i][5], mimetypeService);
            assertEquals(i+" toString", data[i][6], a.toString());
        }
    }
    
    @Test
    public void getPropertyTest()
    {
        mockMimetypes(mimetypeService,
                "sourceMimetype", "sourceExt",
                "sourceAAAAAAAA", "sourceAAA",
                "targetMimetype", "targetExt");

        // Test data
        String[][] data = new String[][]
        {
            {"transformerName", "sourceExt", "targetExt", ".suffix",    null, "value1"},
            {"transformerName", null,         null,       ".suffix",    null, "value2"},
            {"transformerName",  ANY,         ANY,        ".suffix",    null, "value2"},
            {"transformerName", "sourceAAA", "targetExt", ".suffix",    null, "value3"},
            {"transformerXXXX", null,         null,       ".suffix",    null,     null},
            {"transformerName", "sourceExt", "targetExt", ".suffix", "index", "value4"},
            {"transformerName", null,         null,       ".suffix", "index", "value5"},
            {"transformerName",  ANY,         ANY,        ".suffix", "index", "value5"},
            {"transformerName", "sourceAAA", "targetExt", ".suffix", "index", "value6"},
            {"transformerXXXX", null,         null,       ".suffix", "index",     null}
        };
        
        // Populate the test Map
        Map<TransformerSourceTargetSuffixKey, TransformerSourceTargetSuffixValue> transformerSourceTargetSuffixValues =
                new HashMap<TransformerSourceTargetSuffixKey, TransformerSourceTargetSuffixValue>();
        for (int i=data.length-1; i >= 0; i--)
        {
            TransformerSourceTargetSuffixValue keyValue = new TransformerSourceTargetSuffixValue(data[i][0], data[i][1], data[i][2], data[i][3], data[i][4], data[i][5], mimetypeService);
            transformerSourceTargetSuffixValues.put(keyValue.key(), keyValue);
        }
        
        // Check we can return the values including the last one which was not added so has a null value
        for (int i=data.length-1; i >= 0; i--)
        {
            assertEquals(i+" getProperty", data[i][5], extractor.getProperty(data[i][0], data[i][1], data[i][2], data[i][3], data[i][4], transformerSourceTargetSuffixValues));
        }
    }
    
    @Test
    public void transformerWideTest()
    {
        mockProperties(transformerProperties, "content.transformer.abc.suffix", "value1");
        extractor = new TestTransformerPropertyNameExtractor();
        
        map = extractor.getTransformerSourceTargetValuesMap(suffixes, true, true, false, transformerProperties, mimetypeService);
        
        assertEquals("value1", extractor.getProperty("transformer.abc", null, null, ".suffix", null, map));
    }
    
    @Test
    public void excludeSummaryTest()
    {
        mockProperties(transformerProperties, "content.transformer.abc.suffix", "value1");
        extractor = new TestTransformerPropertyNameExtractor();
        
        map = extractor.getTransformerSourceTargetValuesMap(suffixes, false, true, false, transformerProperties, mimetypeService);
        
        assertEquals(null, extractor.getProperty("transformer.abc", null, null, ".suffix", null, map));
    }
    
    @Test
    public void extensionTest()
    {
        mockProperties(transformerProperties, "content.transformer.abc.extensions.pdf.png.suffix", "value1");
        extractor = new TestTransformerPropertyNameExtractor();
        
        map = extractor.getTransformerSourceTargetValuesMap(suffixes, true, true, false, transformerProperties, mimetypeService);
        
        assertEquals("value1", extractor.getProperty("transformer.abc", "pdf", "png", ".suffix", null, map));
    }
    
    @Test
    public void excludeExtensionsTest()
    {
        mockProperties(transformerProperties, "content.transformer.abc.extensions.pdf.png.suffix", "value1");
        extractor = new TestTransformerPropertyNameExtractor();
        
        map = extractor.getTransformerSourceTargetValuesMap(suffixes, true, false, false, transformerProperties, mimetypeService);
        
        assertEquals(null, extractor.getProperty("transformer.abc", "pdf", "png", ".suffix", null, map));
    }
    
    @Test
    public void mimetypeTest()
    {
        mockProperties(transformerProperties, "content.transformer.abc.mimetypes.application/pdf.image/png.suffix", "value1");
        extractor = new TestTransformerPropertyNameExtractor();
        
        map = extractor.getTransformerSourceTargetValuesMap(suffixes, true, true, false, transformerProperties, mimetypeService);
        
        assertEquals("value1", extractor.getProperty("transformer.abc", "pdf", "png", ".suffix", null, map));
    }

    @Test
    public void multipleSuffix1Test()
    {
        suffixes = Arrays.asList(new String[] {".suffix1", ".suffix2", ".suffix3", ".suffix4"});
        mockProperties(transformerProperties, "content.transformer.abc.suffix1", "value1");
        extractor = new TestTransformerPropertyNameExtractor();
        
        map = extractor.getTransformerSourceTargetValuesMap(suffixes, true, true, false, transformerProperties, mimetypeService);
        
        assertEquals("value1", extractor.getProperty("transformer.abc", null, null, ".suffix1", null, map));
    }
    
    @Test
    public void multipleSuffix4Test()
    {
        suffixes = Arrays.asList(new String[] {".suffix1", ".suffix2", ".suffix3", ".suffix4"});
        mockProperties(transformerProperties, "content.transformer.abc.suffix4", "value1");
        extractor = new TestTransformerPropertyNameExtractor();
        
        map = extractor.getTransformerSourceTargetValuesMap(suffixes, true, true, false, transformerProperties, mimetypeService);
        
        assertEquals("value1", extractor.getProperty("transformer.abc", null, null, ".suffix4", null, map));
    }
    
    @Test
    public void noPrefixTest()
    {
        mockProperties(transformerProperties, "the.cat.sat", "on the mat");
        extractor = new TestTransformerPropertyNameExtractor();
        
        assertEquals(0, extractor.getTransformerSourceTargetValues(suffixes, true, false, transformerProperties, mimetypeService).size());
    }
    
    @Test
    public void multipleValuesTest()
    {
        suffixes = TransformerConfig.LIMIT_SUFFIXES;
        mockProperties(transformerProperties,
                "content.transformer.abc.maxSourceSizeKBytes",                                    "1",
                "content.transformer.abc.timeoutMs",                                              "2",
                "content.transformer.abc.extensions.pdf.png.maxPages",                            "3",
                "content.transformer.x.y.mimetypes.application/pdf.image/png.maxPages",           "4",
                "content.transformer.abc.maxSourceSizeKBytes.use.index",                          "5",
                "content.transformer.abc.timeoutMs.use.index",                                    "6",
                "content.transformer.abc.extensions.pdf.png.maxPages.use.index",                  "7",
                "content.transformer.x.y.mimetypes.application/pdf.image/png.maxPages.use.index", "8");
        
        extractor = new TestTransformerPropertyNameExtractor();
        
        map = extractor.getTransformerSourceTargetValuesMap(suffixes, true, true, false, transformerProperties, mimetypeService);
        
        assertEquals("1", extractor.getProperty("transformer.abc", null, null,   ".maxSourceSizeKBytes",    null, map));
        assertEquals("2", extractor.getProperty("transformer.abc", null, null,   ".timeoutMs",              null, map));
        assertEquals("3", extractor.getProperty("transformer.abc", "pdf", "png", ".maxPages",               null, map));
        assertEquals("4", extractor.getProperty("transformer.x.y", "pdf", "png", ".maxPages",               null, map));
        assertEquals(null, extractor.getProperty("transformer.abc", null, null,   ".maxSourceSizeKBytes", "index", map));
        assertEquals(null, extractor.getProperty("transformer.abc", null, null,   ".timeoutMs",           "index", map));
        assertEquals(null, extractor.getProperty("transformer.abc", "pdf", "png", ".maxPages",            "index", map));
        assertEquals(null, extractor.getProperty("transformer.x.y", "pdf", "png", ".maxPages",            "index", map));

        map = extractor.getTransformerSourceTargetValuesMap(suffixes, true, true, true, transformerProperties, mimetypeService);
        
        assertEquals("1", extractor.getProperty("transformer.abc", null, null,   ".maxSourceSizeKBytes",    null, map));
        assertEquals("2", extractor.getProperty("transformer.abc", null, null,   ".timeoutMs",              null, map));
        assertEquals("3", extractor.getProperty("transformer.abc", "pdf", "png", ".maxPages",               null, map));
        assertEquals("4", extractor.getProperty("transformer.x.y", "pdf", "png", ".maxPages",               null, map));
        assertEquals("5", extractor.getProperty("transformer.abc", null, null,   ".maxSourceSizeKBytes", "index", map));
        assertEquals("6", extractor.getProperty("transformer.abc", null, null,   ".timeoutMs",           "index", map));
        assertEquals("7", extractor.getProperty("transformer.abc", "pdf", "png", ".maxPages",            "index", map));
        assertEquals("8", extractor.getProperty("transformer.x.y", "pdf", "png", ".maxPages",            "index", map));
    }
    
    @Test
    public void wildcardTest()
    {
        suffixes = TransformerConfig.LIMIT_SUFFIXES;
        mockProperties(transformerProperties,
                "content.transformer.abc.extensions.p*.*g.maxPages", "value");
        
        extractor = new TestTransformerPropertyNameExtractor();
        
        map = extractor.getTransformerSourceTargetValuesMap(suffixes, true, true, false, transformerProperties, mimetypeService);
        
        assertEquals("pdf,png to png", 2, map.size());
        assertEquals("value", extractor.getProperty("transformer.abc", "pdf", "png", ".maxPages", null, map));
        assertEquals("value", extractor.getProperty("transformer.abc", "png", "png", ".maxPages", null, map));
    }
    
    @Test
    public void mimetypeWinsOverExtensionFirstTest()
    {
        // Extension property is provided first
        suffixes = TransformerConfig.LIMIT_SUFFIXES;
        mockProperties(transformerProperties,
                "content.transformer.abc.extensions.pdf.png.maxPages",                 "extension",
                "content.transformer.abc.mimetypes.application/pdf.image/png.maxPages", "mimetype");
        
        extractor = new TestTransformerPropertyNameExtractor();
        
        map = extractor.getTransformerSourceTargetValuesMap(suffixes, true, true, false, transformerProperties, mimetypeService);
        
        assertEquals("mimetype", extractor.getProperty("transformer.abc", "pdf", "png", ".maxPages", null, map));
    }
    
    @Test
    public void mimetypeWinsOverExtensionSecondTest()
    {
        // Extension property is provided second
        suffixes = TransformerConfig.LIMIT_SUFFIXES;
        mockProperties(transformerProperties,
                "content.transformer.abc.mimetypes.application/pdf.image/png.maxPages", "mimetype",
                "content.transformer.abc.extensions.pdf.png.maxPages",                 "extension");
        
        extractor = new TestTransformerPropertyNameExtractor();
        
        map = extractor.getTransformerSourceTargetValuesMap(suffixes, true, true, false, transformerProperties, mimetypeService);
        
        assertEquals("mimetype", extractor.getProperty("transformer.abc", "pdf", "png", ".maxPages", null, map));
    }
    
    @Test
    public void callsToHandlePropertyTest()
    {
        final String[][] data = new String[][]
        {    // Must sort the property names
            {"content.transformer.abc.extensions.pdf.png.suffix",                            "value2", "transformer.abc", ".extensions.",             "pdf",       "png", ".suffix", null},
            {"content.transformer.abc.extensions.pdf.png.suffix.use.index",                  "value2", "transformer.abc", ".extensions.",             "pdf",       "png", ".suffix", "index"},

            {"content.transformer.abc.mimetypes.application/pdf.image/png.suffix",           "value2", "transformer.abc",  ".mimetypes.", "application/pdf", "image/png", ".suffix", null},
            {"content.transformer.abc.mimetypes.application/pdf.image/png.suffix.use.index", "value2", "transformer.abc",  ".mimetypes.", "application/pdf", "image/png", ".suffix", "index"},

            {"content.transformer.abc.suffix",                                               "value1", "transformer.abc",           null,              null,        null, ".suffix", null},
            {"content.transformer.abc.suffix.use.index",                                     "value1", "transformer.abc",           null,              null,        null, ".suffix", "index"},

            {"xxxx.transformer.abc.extensions.pdf.png.suffix",                               "value2",              null},
            {"xxxx.transformer.abc.extensions.pdf.png.suffix.use.index",                     "value2",              null}
        };
        
        List<String> properties = new ArrayList<String>();
        final Set<String> suffixes = new HashSet<String>();
        for (String[] entry: data)
        {
            properties.add(entry[0]);
            properties.add(entry[1]);
            if (entry.length == 8)
            {
                suffixes.add(entry[6]);
            }
        }
        mockProperties(transformerProperties, properties.toArray(new String[properties.size()]));
        
        final AtomicInteger i = new AtomicInteger(0);
        extractor = new TestTransformerPropertyNameExtractor()
        {
            protected void handleProperty(String transformerName, String separator,
                    String firstExpression, String secondExpression, String suffix, String use, String value,
                    String propertyName,
                    Map<TransformerSourceTargetSuffixKey, TransformerSourceTargetSuffixValue> transformerSourceTargetSuffixValues, MimetypeService mimetypeService)
            {
                // Ignore non transformer properties
                int j;
                do
                {
                    j = i.getAndIncrement();
                } while(j < data.length && data[j].length != 8);
                
                if (j < data.length)
                {
                    assertEquals(j+" transformerName",  data[j][2], transformerName);
                    assertEquals(j+" separator",        data[j][3], separator);
                    assertEquals(j+" firstExpression",  data[j][4], firstExpression);
                    assertEquals(j+" secondExpression", data[j][5], secondExpression);
                    assertEquals(j+" suffix",           data[j][6], suffix);
                    assertEquals(j+" use",              data[j][7], use);
                    assertEquals(j+" value",            data[j][1], value);
                    assertEquals(j+" propertyName",     data[j][0], propertyName);
                }
                
                while(j+1 < data.length && data[j+1].length < 8)
                {
                    j = i.getAndIncrement();
                }
                
                super.handleProperty(transformerName, separator, firstExpression, secondExpression, suffix, use, value, propertyName, transformerSourceTargetSuffixValues, mimetypeService);
            }
        };
        
        extractor.callGetTransformerSourceTargetValuesMap(suffixes, true, transformerProperties, mimetypeService);
        
        assertEquals("counter", 8, i.get());
    }
}

class TestTransformerPropertyNameExtractor extends TransformerPropertyNameExtractor
{
    public Collection<TransformerSourceTargetSuffixValue> callGetTransformerSourceTargetValues(Collection<String> suffixes,
            boolean includeSummary, TransformerProperties transformerProperties, MimetypeService mimetypeService)
    {
        return getTransformerSourceTargetValues(suffixes, includeSummary, false, transformerProperties, mimetypeService);
    }

    public Map<TransformerSourceTargetSuffixKey, TransformerSourceTargetSuffixValue> callGetTransformerSourceTargetValuesMap(Collection<String> suffixes,
            boolean includeSummary, TransformerProperties transformerProperties, MimetypeService mimetypeService)
    {
        return getTransformerSourceTargetValuesMap(suffixes, includeSummary, true, true, transformerProperties, mimetypeService);
    }
};

