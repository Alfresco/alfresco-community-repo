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

import static org.junit.Assert.assertEquals;
import static org.alfresco.repo.content.transform.TransformerPropertyNameExtractorTest.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests TransformerPropertySetter.
 * 
 * @author Alan Davis
 */
public class TransformerPropertySetterTest
{
    @Mock
    private TransformerProperties transformerProperties;

    @Mock
    private MimetypeService mimetypeService;

    @Mock
    private ContentTransformerRegistry transformerRegistry;
    
    private TransformerPropertySetter setter;
    private Properties properties;
    
    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        mockAllTransformers(transformerRegistry, "abc", "complex.PDF.Image", "transformer1", "transformer2", "transformer3");
        mockMimetypes(mimetypeService,
                "application/pdf", "pdf",
                "image/png",       "png");
        setter = new TransformerPropertySetter(transformerProperties, mimetypeService, transformerRegistry);
        
        properties = new Properties();
        when(transformerProperties.getDefaultProperties()).thenReturn(properties);
    }
    
    public static void mockAllTransformers(ContentTransformerRegistry transformerRegistry, String... shortTransformerNames)
    {
        List<ContentTransformer> allTransformers = new ArrayList<ContentTransformer>();
        for (String shortTransformerName: shortTransformerNames)
        {
            allTransformers.add(new DummyContentTransformer(TransformerConfig.TRANSFORMER+shortTransformerName));
        }
        when(transformerRegistry.getAllTransformers()).thenReturn(allTransformers);
    }

    private Map<String, String> expectedProperties(String... namesAndValues)
    {
        Map<String, String> expected = new HashMap<String, String>();
        String name = null;
        for (String string: namesAndValues)
        {
            if (name == null)
            {
                name = string;
            }
            else
            {
                expected.put(name, string);
                name = null;
            }
        }
        return expected;
    }

    private Collection<String> expectedNames(String... names)
    {
        Set<String> expected = new HashSet<String>();
        expected.addAll(Arrays.asList(names));
        return expected;
    }

    @Test
    public void simpleTest()
    {
        int count = setter.setProperties("transformer.log.entries=12");

        verify(transformerProperties).setProperties(expectedProperties("transformer.log.entries", "12"));
        assertEquals("Added count", 1, count);
    }

    @Test
    public void debugLineSplitTest()
    {
        int count = setter.setProperties(
                "transformer.log.entries=12\n" +
                "transformer.debug.entries=23");
        
        verify(transformerProperties).setProperties(expectedProperties(
                "transformer.debug.entries", "23",
                "transformer.log.entries", "12"));
        assertEquals("Added count", 2, count);
    }

    @Test
    public void mixedCaseTest()
    {
        setter.setProperties("Transformer.LOG.entries=12");

        verify(transformerProperties).setProperties(expectedProperties("Transformer.LOG.entries", "12"));
    }

    @Test
    public void debugLogSizeTest()
    {
        setter.setProperties(
                "transformer.log.entries=12\n" +
                "transformer.debug.entries=-1");
        
        verify(transformerProperties).setProperties(expectedProperties(
                "transformer.debug.entries", "-1",
                "transformer.log.entries", "12"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void badPropertyNameTest()
    {
        setter.setProperties("trans.bad=11");
    }

    @Test(expected=IllegalArgumentException.class)
    public void duplicateNameTest()
    {
        setter.setProperties(
                "transformer.log.entries=12\n" +
                "transformer.log.entries=-1");
    }

    @Test(expected=IllegalArgumentException.class)
    public void badIntegerTest()
    {
        setter.setProperties("transformer.debug.entries=--1");
    }

    @Test(expected=IllegalArgumentException.class)
    public void badInteger2Test()
    {
        setter.setProperties("transformer.debug.entries=1a");
    }

    @Test(expected=IllegalArgumentException.class)
    public void noValueEqualsTest()
    {
        // setProiperties must have an =
        setter.setProperties("transformer.debug.entries");
    }

    @Test
    public void booleanTest()
    {
        setter.setProperties("content.transformer.complex.PDF.Image.extensions.pdf.png.supported=TruE");
        verify(transformerProperties).setProperties(expectedProperties(
                "content.transformer.complex.PDF.Image.extensions.pdf.png.supported", "TruE"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void badBooleanTest()
    {
        setter.setProperties("content.transformer.complex.PDF.Image.extensions.pdf.png.supported=yes");
    }

    @Test
    public void defaultTransformerTest()
    {
        setter.setProperties(
                "content.transformer.default.priority=100\n"+
                "content.transformer.default.thresholdCount=3\n"+
                "content.transformer.default.time=0\n"+
                "content.transformer.default.count=100000\n"+
                "content.transformer.default.errorTime=120000\n"+
                "content.transformer.default.timeoutMs=120000\n"+
                "content.transformer.default.readLimitTimeMs=-1\n"+
                "content.transformer.default.maxSourceSizeKBytes=-1\n"+
                "content.transformer.default.readLimitKBytes=-1\n"+
                "content.transformer.default.pageLimit=-1\n"+
                "content.transformer.default.maxPages=-1");
        
        verify(transformerProperties).setProperties(expectedProperties(
                "content.transformer.default.priority", "100",
                "content.transformer.default.thresholdCount", "3",
                "content.transformer.default.time", "0",
                "content.transformer.default.count", "100000",
                "content.transformer.default.errorTime", "120000",
                "content.transformer.default.timeoutMs", "120000",
                "content.transformer.default.readLimitTimeMs", "-1",
                "content.transformer.default.maxSourceSizeKBytes", "-1",
                "content.transformer.default.readLimitKBytes", "-1",
                "content.transformer.default.pageLimit", "-1",
                "content.transformer.default.maxPages", "-1"));
    }

    @Test
    public void commentAndWhiteSpaceTest()
    {
        setter.setProperties(
                "# hi mum\n"+
                "content.transformer.default.priority=100\n"+
                "  content.transformer.default.thresholdCount=3   \n"+
                "\t  content.transformer.default.time=0\n"+
                "\t\t \t content.transformer.default.count=100000   # another comment\n"+
                "#\n"+
                "   \n"+
                "\n"+
                "content.transformer.default.errorTime=120000\n");
        
        verify(transformerProperties).setProperties(expectedProperties(
                "content.transformer.default.priority", "100",
                "content.transformer.default.thresholdCount", "3",
                "content.transformer.default.time", "0",
                "content.transformer.default.count", "100000",
                "content.transformer.default.errorTime", "120000"));
    }

    @Test
    public void fixJConsolesMissingNewlinesTest()
    {
        // Variable names based on what JConsole does to newlines and what fixJConsolesMissingNewlines does to spaces
        String nl2nl = "@";
        String nl2space = "~";
        String space2nl = "%";
        
        String data =
                "transformer.log.entries=12"+nl2nl+
                "transformer.log.entries=-1"+nl2space+
                "# a line of comment"+nl2nl+
                "content.transformer.default.priority=100"+nl2space+
                "#"+nl2space+  // Will discard the next property, but this is still best we can do
                "content.transformer.default.thresholdCount=3 "+nl2nl+
                "content.transformer.default.time=0    # end of line comment "+nl2nl+
                "content.transformer.default.count=100000 "+nl2space+
                "   "+space2nl+"content.transformer.default.errorTime=120000 "+nl2nl+
                "content.transformer.default.timeoutMs=120000    "+nl2nl+
                "content.transformer.default.readLimitTimeMs=-1";

        // What we paste into JConsole
        String original = data.replace(nl2nl, "\n").replace(nl2space, "\n").replace(space2nl, " ");

        // What we get from JConsole
        String missingNl = data.replace(nl2nl, " ").replace(nl2space, " ").replace(space2nl, " ");

        // What we expect to recover as a result of this method
        String expected = data.replace(nl2nl, "\n").replace(nl2space, " ").replace(space2nl, "\n");

        // What we do get from fixJConsolesMissingNewlines
        String actual = setter.fixJConsolesMissingNewlines(missingNl);

        String indentOriginal = ("\n"+original).replaceAll("\n", "\n        ");
        assertEquals("Having entered:"+indentOriginal+"\n", expected, actual);
    }

    @Test(expected=IllegalArgumentException.class)
    public void midNameWhiteSpaceTest()
    {
        setter.setProperties("transformer.   log.entries=12");
    }

    @Test(expected=IllegalArgumentException.class)
    public void prefixValueWhiteSpaceTest()
    {
        setter.setProperties("transformer.log.entries= 12");
    }

    @Test
    public void namedTransformerTest()
    {
        setter.setProperties(
                "content.transformer.abc.priority=100\n"+
                "content.transformer.abc.thresholdCount=3\n"+
                "content.transformer.abc.time=0\n"+
                "content.transformer.abc.count=100000\n"+
                "content.transformer.abc.errorTime=120000\n"+
                "content.transformer.abc.timeoutMs=120000\n"+
                "content.transformer.abc.readLimitTimeMs=-1\n"+
                "content.transformer.abc.maxSourceSizeKBytes=-1\n"+
                "content.transformer.abc.readLimitKBytes=-1\n"+
                "content.transformer.abc.pageLimit=-1\n"+
                "content.transformer.abc.maxPages=-1\n"+
                "content.transformer.abc.failover=transformer1|transformer2|transformer3\n"+
                "content.transformer.abc.pipeline=transformer1|pdf|transformer2");
        
        verify(transformerProperties).setProperties(expectedProperties(
                "content.transformer.abc.priority", "100",
                "content.transformer.abc.thresholdCount", "3",
                "content.transformer.abc.time", "0",
                "content.transformer.abc.count", "100000",
                "content.transformer.abc.errorTime", "120000",
                "content.transformer.abc.timeoutMs", "120000",
                "content.transformer.abc.readLimitTimeMs", "-1",
                "content.transformer.abc.maxSourceSizeKBytes", "-1",
                "content.transformer.abc.readLimitKBytes", "-1",
                "content.transformer.abc.pageLimit", "-1",
                "content.transformer.abc.maxPages", "-1",
                "content.transformer.abc.failover", "transformer1|transformer2|transformer3",
                "content.transformer.abc.pipeline", "transformer1|pdf|transformer2"));
    }

    @Test
    public void simpleExtensionsTest()
    {
        setter.setProperties("content.transformer.abc.extensions.pdf.png.maxPages=-1");
    }

    @Test
    public void namedTransformerExtensionTest()
    {
        setter.setProperties(
                "content.transformer.abc.extensions.pdf.png.priority=100\n"+
                "content.transformer.abc.extensions.pdf.png.thresholdCount=3\n"+
                "content.transformer.abc.extensions.pdf.png.time=0\n"+
                "content.transformer.abc.extensions.pdf.png.count=100000\n"+
                "content.transformer.abc.extensions.pdf.png.errorTime=120000\n"+
                "content.transformer.abc.extensions.pdf.png.timeoutMs=120000\n"+
                "content.transformer.abc.extensions.pdf.png.readLimitTimeMs=-1\n"+
                "content.transformer.abc.extensions.pdf.png.maxSourceSizeKBytes=-1\n"+
                "content.transformer.abc.extensions.pdf.png.readLimitKBytes=-1\n"+
                "content.transformer.abc.extensions.pdf.png.pageLimit=-1\n"+
                "content.transformer.abc.extensions.pdf.png.maxPages=-1\n"+
                "content.transformer.abc.extensions.pdf.png.supported=true");
        
        verify(transformerProperties).setProperties(expectedProperties(
                "content.transformer.abc.extensions.pdf.png.priority", "100",
                "content.transformer.abc.extensions.pdf.png.thresholdCount", "3",
                "content.transformer.abc.extensions.pdf.png.time", "0",
                "content.transformer.abc.extensions.pdf.png.count", "100000",
                "content.transformer.abc.extensions.pdf.png.errorTime", "120000",
                "content.transformer.abc.extensions.pdf.png.timeoutMs", "120000",
                "content.transformer.abc.extensions.pdf.png.readLimitTimeMs", "-1",
                "content.transformer.abc.extensions.pdf.png.maxSourceSizeKBytes", "-1",
                "content.transformer.abc.extensions.pdf.png.readLimitKBytes", "-1",
                "content.transformer.abc.extensions.pdf.png.pageLimit", "-1",
                "content.transformer.abc.extensions.pdf.png.maxPages", "-1",
                "content.transformer.abc.extensions.pdf.png.supported", "true"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void badSourceExtensionsTest()
    {
        setter.setProperties("content.transformer.abc.extensions.bad.png.maxPages=-1");
    }

    @Test(expected=IllegalArgumentException.class)
    public void badTargetExtensionsTest()
    {
        setter.setProperties("content.transformer.abc.extensions.pdf.bad.maxPages=-1");
    }

    @Test
    public void simpleMimetypeTest()
    {
        setter.setProperties("content.transformer.abc.mimetypes.application/pdf.image/png.maxPages=-1");
    }

    @Test
    public void namedTransformerMimetypeTest()
    {
        setter.setProperties(
                "content.transformer.abc.mimetypes.application/pdf.image/png.priority=100\n"+
                "content.transformer.abc.mimetypes.application/pdf.image/png.thresholdCount=3\n"+
                "content.transformer.abc.mimetypes.application/pdf.image/png.time=0\n"+
                "content.transformer.abc.mimetypes.application/pdf.image/png.count=100000\n"+
                "content.transformer.abc.mimetypes.application/pdf.image/png.errorTime=120000\n"+
                "content.transformer.abc.mimetypes.application/pdf.image/png.timeoutMs=120000\n"+
                "content.transformer.abc.mimetypes.application/pdf.image/png.readLimitTimeMs=-1\n"+
                "content.transformer.abc.mimetypes.application/pdf.image/png.maxSourceSizeKBytes=-1\n"+
                "content.transformer.abc.mimetypes.application/pdf.image/png.readLimitKBytes=-1\n"+
                "content.transformer.abc.mimetypes.application/pdf.image/png.pageLimit=-1\n"+
                "content.transformer.abc.mimetypes.application/pdf.image/png.maxPages=-1\n"+
                "content.transformer.abc.mimetypes.application/pdf.image/png.supported=true");
        
        verify(transformerProperties).setProperties(expectedProperties(
                "content.transformer.abc.mimetypes.application/pdf.image/png.priority", "100",
                "content.transformer.abc.mimetypes.application/pdf.image/png.thresholdCount", "3",
                "content.transformer.abc.mimetypes.application/pdf.image/png.time", "0",
                "content.transformer.abc.mimetypes.application/pdf.image/png.count", "100000",
                "content.transformer.abc.mimetypes.application/pdf.image/png.errorTime", "120000",
                "content.transformer.abc.mimetypes.application/pdf.image/png.timeoutMs", "120000",
                "content.transformer.abc.mimetypes.application/pdf.image/png.readLimitTimeMs", "-1",
                "content.transformer.abc.mimetypes.application/pdf.image/png.maxSourceSizeKBytes", "-1",
                "content.transformer.abc.mimetypes.application/pdf.image/png.readLimitKBytes", "-1",
                "content.transformer.abc.mimetypes.application/pdf.image/png.pageLimit", "-1",
                "content.transformer.abc.mimetypes.application/pdf.image/png.maxPages", "-1",
                "content.transformer.abc.mimetypes.application/pdf.image/png.supported", "true"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void badSourceMimetypeTest()
    {
        setter.setProperties("content.transformer.abc.mimetypes.bad.image/png.maxPages=-1");
    }

    @Test(expected=IllegalArgumentException.class)
    public void badTargetMimetypeTest()
    {
        setter.setProperties("content.transformer.abc.mimetypes.application/pdf.bad.maxPages=-1");
    }

    public void pipelineTest()
    {
        setter.setProperties("content.transformer.abc.pipeline=transformer1|pdf|transformer2");
    }

    @Test(expected=IllegalArgumentException.class)
    public void pipelineWithExtensionsTest()
    {
        setter.setProperties("content.transformer.abc.extensions.pdf.png.pipeline=transformer1|pdf|transformer2");
    }

    @Test(expected=IllegalArgumentException.class)
    public void pipelineWithMimetypesTest()
    {
        setter.setProperties("content.transformer.abc.mimetypes.application/pdf.image/png.pipeline=transformer1|application/pdf|transformer2");
    }

    @Test(expected=IllegalArgumentException.class)
    public void pipelineTooFewTest()
    {
        setter.setProperties("content.transformer.abc.pipeline=transformer1");
    }

    @Test(expected=IllegalArgumentException.class)
    public void pipelineMissingTransformerTest()
    {
        setter.setProperties("content.transformer.abc.pipeline=transformer1|pdf|transformer2|png");
    }

    @Test(expected=IllegalArgumentException.class)
    public void pipelineBadTransformerTest()
    {
        setter.setProperties("content.transformer.abc.pipeline=transformer1|pdf|bad");
    }

    @Test(expected=IllegalArgumentException.class)
    public void pipelineBadExtensionTest()
    {
        setter.setProperties("content.transformer.abc.pipeline=transformer1|bad|transformer2");
    }

    public void failoverTest()
    {
        setter.setProperties("content.transformer.abc.failover=transformer1|transformer2");
    }

    @Test(expected=IllegalArgumentException.class)
    public void failoverWithExtensionsTest()
    {
        setter.setProperties("content.transformer.abc.extensions.pdf.png.pipeline=transformer1|pdf|transformer2");
    }

    @Test(expected=IllegalArgumentException.class)
    public void failoverWithMimetypeTest()
    {
        setter.setProperties("content.transformer.abc.mimetypes.application/pdf.image/png.failover=transformer1|transformer2");
    }

    @Test(expected=IllegalArgumentException.class)
    public void failoverTooFewTest()
    {
        setter.setProperties("content.transformer.abc.failover=transformer1");
    }

    @Test(expected=IllegalArgumentException.class)
    public void failoverBadTransformerTest()
    {
        setter.setProperties("content.transformer.abc.failover=transformer1|bad|transformer2");
    }

    @Test(expected=IllegalArgumentException.class)
    public void removeUnsetPropetyTest()
    {
        setter.removeProperties("transformer.log.entries");
    }
    
    @Test
    public void removeSimplePropetyTest()
    {
        mockProperties(transformerProperties, "transformer.log.entries", "12");
        
        int count = setter.removeProperties("transformer.log.entries");
        
        verify(transformerProperties).removeProperties(expectedNames("transformer.log.entries"));
        assertEquals("Removed count", 1, count);
    }

    @Test
    // removeProperties optionally has a value
    public void removeOptionalValueTest()
    {
        mockProperties(transformerProperties, "transformer.log.entries", "12");
        
        setter.removeProperties("transformer.log.entries=12");
    }
    
    @Test(expected=IllegalArgumentException.class)
    // removeProperties on a default property
    public void removeDefaultValueTest()
    {
        mockProperties(transformerProperties, "transformer.log.entries", "12");

        properties.setProperty("transformer.log.entries", "10");
        
        setter.removeProperties("transformer.log.entries=12");
    }
    
    @Test
    public void dynamicTransformerReferenceTest()
    {
        setter.setProperties(
                "content.transformer.abc.failover=transformer1|xyz\n"+              // Reference the transformer xyz
                "content.transformer.xyz.pipeline=transformer1|pdf|transformer2");  // Create the transformer xyz
    }

    @Test
    public void removeMultiplePropetiesTest()
    {
        mockProperties(transformerProperties,
            "content.transformer.default.priority",     "100",
            "content.transformer.default.thresholdCount", "3",
            "content.transformer.default.time",           "0",
            "content.transformer.default.count",     "100000",
            "content.transformer.default.errorTime", "120000");
        
        int count = setter.removeProperties(
                "# hi mum\n"+
                "content.transformer.default.priority=100\n"+
                "  content.transformer.default.thresholdCount=3   \n"+
                "\t  content.transformer.default.time=0\n"+
                "\t\t \t content.transformer.default.count=100000   # another comment\n"+
                "#\n"+
                "   \n"+
                "\n"+
                "content.transformer.default.errorTime=120000\n");

        verify(transformerProperties).removeProperties(expectedNames(
                "content.transformer.default.priority",
                "content.transformer.default.thresholdCount",
                "content.transformer.default.time",
                "content.transformer.default.count",
                "content.transformer.default.errorTime"));
        assertEquals("Removed count", 5, count);
    }
}

class DummyContentTransformer implements ContentTransformer
{
    private final String name;
    
    DummyContentTransformer(String name)
    {
        this.name = name;
    }
    
    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public boolean isTransformable(String sourceMimetype, String targetMimetype,
            TransformationOptions options)
    {
        return false;
    }

    @Override
    public boolean isTransformable(String sourceMimetype, long sourceSize,
            String targetMimetype, TransformationOptions options)
    {
        return false;
    }

    @Override
    public boolean isTransformableMimetype(String sourceMimetype, String targetMimetype,
            TransformationOptions options)
    {
        return false;
    }

    @Override
    public boolean isTransformableSize(String sourceMimetype, long sourceSize,
            String targetMimetype, TransformationOptions options)
    {
        return false;
    }

    @Override
    public String getComments(boolean available)
    {
        return "";
    }

    @Override
    public long getMaxSourceSizeKBytes(String sourceMimetype, String targetMimetype,
            TransformationOptions options)
    {
        return 0;
    }

    @Override
    public boolean isExplicitTransformation(String sourceMimetype, String targetMimetype,
            TransformationOptions options)
    {
        return false;
    }

    @Override
    public long getTransformationTime()
    {
        return 0;
    }

    @Override
    public long getTransformationTime(String sourceMimetype, String targetMimetype)
    {
        return 0;
    }

    @Override
    public void transform(ContentReader reader, ContentWriter writer) throws ContentIOException
    {
    }

    @Override
    public void transform(ContentReader reader, ContentWriter writer,
            Map<String, Object> options) throws ContentIOException
    {
    }

    @Override
    public void transform(ContentReader reader, ContentWriter contentWriter,
            TransformationOptions options) throws ContentIOException
    {
    }
};
