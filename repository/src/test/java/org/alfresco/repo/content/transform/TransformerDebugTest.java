/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

import com.google.common.collect.ImmutableMap;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.LogAdapter;
import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

public class TransformerDebugTest
{
    public static final String RENDITION_2B = "renditionName1";
    public static final String TRANSFORMER_A2B = "transformerA2B";
    public static final String TRANSFORMER_A2C = "transformerA2C";
    public static final String TRANSFORMER_C2B = "transformerC2B";

    private TransformerDebug transformerDebug;

    public static final String MIMETYPE_A = "mimetypeA";
    public static final String MIMETYPE_A_EXT = "a";
    public static final String MIMETYPE_B = "mimetypeB";
    public static final String MIMETYPE_B_EXT = "bbb";
    public static final String MIMETYPE_C = "mimetypeC";
    public static final String MIMETYPE_C_EXT = "ccc";
    public static final String MIMETYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String MIMETYPE_DOCX_EXT = "docx";
    public static final String MIMETYPE_LONGER = "mimetypeLonger";
    public static final String MIMETYPE_LONGER_EXT = "longer";

    private static final Map<String, String> testMimetypeService = ImmutableMap.of(
            MIMETYPE_A, MIMETYPE_A_EXT,
            MIMETYPE_B, MIMETYPE_B_EXT,
            MIMETYPE_C, MIMETYPE_C_EXT,
            MIMETYPE_DOCX, MIMETYPE_DOCX_EXT,
            MIMETYPE_LONGER, MIMETYPE_LONGER_EXT);

    private StringBuilder singleLine;
    private StringBuilder multiLine;
    private AtomicReference<LogLevel> singleLineLogLevel = new AtomicReference<>();
    private AtomicReference<LogLevel> multiLineLogLevel = new AtomicReference<>();
    private Map<String, String> options;

    @Mock
    private NodeService nodeService;
    private NodeRef sourceNodeRef = new NodeRef("workspace://SpacesStore/parent");

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        TransformerDebug.Frame.reset();
        singleLine = new StringBuilder();
        multiLine = new StringBuilder();
        singleLineLogLevel.set(LogLevel.DEBUG);
        multiLineLogLevel.set(LogLevel.DEBUG);
        transformerDebug = new TransformerDebug();
        transformerDebug.setExtensionLookup(mimetype -> testMimetypeService.get(mimetype));
        transformerDebug.setTransformerDebugLog(new DummyLog(multiLine, multiLineLogLevel));
        transformerDebug.setTransformerLog(new DummyLog(singleLine, singleLineLogLevel));
        transformerDebug.setNodeService(nodeService);
        options = ImmutableMap.of(
                "option1", "value1",
                "option2", "value2");
        doReturn("filename1.a").when(nodeService).getProperty(sourceNodeRef, ContentModel.PROP_NAME);
    }

    private static String getSanitisedActual(StringBuilder debug)
    {
        return debug.toString()
                .replaceAll(" [\\d,]+ ms", " nn ms"); // the time can change
    }

    @Test
    public void testSingleLevelTransformDebug()
    {
        String expectedSingleLine =
                "DEBUG 0 a    bbb  INFO filename1.a 1 KB nn ms transformerA2B -- renditionName1 -- ";
        String expectedMultiLine =
                "DEBUG 0               a    bbb  filename1.a 1 KB -- renditionName1 -- transformerA2B\n" +
                "DEBUG 0                 option1=\"value1\"\n" +
                "DEBUG 0                 option2=\"value2\"\n" +
                "DEBUG 0               workspace://SpacesStore/parent\n" +
                "DEBUG 0               Finished in nn ms\n";

        testSingleLevelTransform(expectedSingleLine, expectedMultiLine);
    }

    @Test
    public void testSingleLevelTransformTrace()
    {
        singleLineLogLevel.set(LogLevel.TRACE);
        multiLineLogLevel.set(LogLevel.TRACE);

        String expectedSingleLine =
                "DEBUG 0 a    bbb  INFO filename1.a 1 KB nn ms transformerA2B -- renditionName1 -- ";
        String expectedMultiLine =
                "TRACE 0               fromUrl1\n" +
                "TRACE 0               mimetypeA mimetypeB\n" +
                "DEBUG 0               a    bbb  filename1.a 1 KB -- renditionName1 -- transformerA2B\n" +
                "DEBUG 0                 option1=\"value1\"\n" +
                "DEBUG 0                 option2=\"value2\"\n" +
                "DEBUG 0               workspace://SpacesStore/parent\n" +
                "DEBUG 0               Finished in nn ms\n";

        testSingleLevelTransform(expectedSingleLine, expectedMultiLine);
    }

    @Test
    public void testSingleLevelTransformInfo()
    {
        singleLineLogLevel.set(LogLevel.INFO);
        multiLineLogLevel.set(LogLevel.INFO);

        String expectedSingleLine = "";
        String expectedMultiLine = "";

        testSingleLevelTransform(expectedSingleLine, expectedMultiLine);
    }

    private void testSingleLevelTransform(String expectedSingleLine, String expectedMultiLine)
    {
        transformerDebug.pushTransform(TRANSFORMER_A2B, "fromUrl1", MIMETYPE_A, MIMETYPE_B,
                1024, options, RENDITION_2B, sourceNodeRef);
        transformerDebug.popTransform();

        assertEquals(expectedSingleLine, getSanitisedActual(singleLine));
        assertEquals(expectedMultiLine, getSanitisedActual(multiLine));
    }

    @Test
    public void testMultiLevelTransformDebug()
    {
        String expectedSingleLine =
                "DEBUG 0 a    bbb  INFO filename1.a 1 KB nn ms transformerA2B -- renditionName1 -- ";
        String expectedMultiLine =
                "DEBUG 0               a    bbb  filename1.a 1 KB -- renditionName1 -- transformerA2B\n" +
                "DEBUG 0                 option1=\"value1\"\n" +
                "DEBUG 0                 option2=\"value2\"\n" +
                "DEBUG 0               workspace://SpacesStore/parent\n" +
                "DEBUG 0.1             a    ccc  filename1.a 1 KB transformerA2C\n" +
                "DEBUG 0.2             ccc  bbb  filename1.a 1 KB transformerC2B\n" +
                "DEBUG 0               Finished in nn ms\n";

        testMultiLevelTransform(expectedSingleLine, expectedMultiLine);
    }

    @Test
    public void testMultiLevelTransformTrace()
    {
        singleLineLogLevel.set(LogLevel.TRACE);
        multiLineLogLevel.set(LogLevel.TRACE);

        String expectedSingleLine =
                "TRACE 0.1             a    ccc  INFO filename1.a 1 KB nn ms transformerA2C -- renditionName1 -- \n" +
                "TRACE 0.2             ccc  bbb  INFO filename1.a 1 KB nn ms transformerC2B -- renditionName1 -- \n" +
                "DEBUG 0 a    bbb  INFO filename1.a 1 KB nn ms transformerA2B -- renditionName1 -- ";
        String expectedMultiLine =
                "TRACE 0               fromUrl1\n" +
                "TRACE 0               mimetypeA mimetypeB\n" +
                "DEBUG 0               a    bbb  filename1.a 1 KB -- renditionName1 -- transformerA2B\n" +
                "DEBUG 0                 option1=\"value1\"\n" +
                "DEBUG 0                 option2=\"value2\"\n" +
                "DEBUG 0               workspace://SpacesStore/parent\n" +
                "TRACE 0.1             fromUrl1\n" +
                "TRACE 0.1             mimetypeA mimetypeC\n" +
                "DEBUG 0.1             a    ccc  filename1.a 1 KB transformerA2C\n" +
                "TRACE 0.1             Finished in nn ms\n" +
                "TRACE 0.2             fromUrl1\n" +
                "TRACE 0.2             mimetypeC mimetypeB\n" +
                "DEBUG 0.2             ccc  bbb  filename1.a 1 KB transformerC2B\n" +
                "TRACE 0.2             Finished in nn ms\n" +
                "DEBUG 0               Finished in nn ms\n";

        testMultiLevelTransform(expectedSingleLine, expectedMultiLine);
    }

    @Test
    public void testMultiLevelTransformInfo()
    {
        singleLineLogLevel.set(LogLevel.INFO);
        multiLineLogLevel.set(LogLevel.INFO);

        String expectedSingleLine = "";
        String expectedMultiLine = "";

        testMultiLevelTransform(expectedSingleLine, expectedMultiLine);
    }

    private void testMultiLevelTransform(String expectedSingleLine, String expectedMultiLine)
    {
        transformerDebug.pushTransform(TRANSFORMER_A2B, "fromUrl1", MIMETYPE_A, MIMETYPE_B,
                1024, options, RENDITION_2B, sourceNodeRef);
        transformerDebug.pushTransform(TRANSFORMER_A2C, "fromUrl1", MIMETYPE_A, MIMETYPE_C,
                1024, options, RENDITION_2B, sourceNodeRef);
        transformerDebug.popTransform();
        transformerDebug.pushTransform(TRANSFORMER_C2B, "fromUrl1", MIMETYPE_C, MIMETYPE_B,
                1024, options, RENDITION_2B, sourceNodeRef);
        transformerDebug.popTransform();
        transformerDebug.popTransform();

        assertEquals(expectedSingleLine, getSanitisedActual(singleLine));
        assertEquals(expectedMultiLine, getSanitisedActual(multiLine));
    }

    @Test
    public void testReplaceWithMetadataExtensionIfEmbedOrExtract()
    {
        assertEquals("json", transformerDebug.replaceWithMetadataExtensionIfEmbedOrExtract(
                "alfresco-metadata-extract", "jpeg", "targetExtension"));
        assertEquals("jpeg", transformerDebug.replaceWithMetadataExtensionIfEmbedOrExtract(
                "alfresco-metadata-embed", "jpeg", "targetExtension"));
        assertEquals(MIMETYPE_DOCX_EXT, transformerDebug.replaceWithMetadataExtensionIfEmbedOrExtract(
                MIMETYPE_DOCX, "jpeg", MIMETYPE_DOCX_EXT));
    }

    @Test
    public void testGetMimetypeExt()
    {
        assertEquals(MIMETYPE_A_EXT  +"    ", transformerDebug.getMimetypeExt(MIMETYPE_A));
        assertEquals(MIMETYPE_B_EXT    +"  ", transformerDebug.getMimetypeExt(MIMETYPE_B));
        assertEquals(MIMETYPE_DOCX_EXT  +" ", transformerDebug.getMimetypeExt(MIMETYPE_DOCX));
        assertEquals(MIMETYPE_LONGER_EXT+" ", transformerDebug.getMimetypeExt(MIMETYPE_LONGER)); // not padded to 4 spaces

        assertEquals("unknown"          +" ", transformerDebug.getMimetypeExt("unknown"));       // not padded
        assertEquals("x"                +" ", transformerDebug.getMimetypeExt("x"));             // not padded
    }

    @Test
    public void testSpaces()
    {
        assertEquals("",        transformerDebug.spaces(-1));
        assertEquals("",        transformerDebug.spaces(0));
        assertEquals(" ",       transformerDebug.spaces(1));
        assertEquals("   ",     transformerDebug.spaces(3));
        assertEquals("     ",   transformerDebug.spaces(5));
    }

    @Test
    public void testMs()
    {
        assertEquals("-1 ms",        transformerDebug.ms(-1));
        assertEquals("0 ms",         transformerDebug.ms(0));
        assertEquals("1 ms",         transformerDebug.ms(1));
        assertEquals("2 ms",         transformerDebug.ms(2));
        assertEquals("123 ms",       transformerDebug.ms(123));
        assertEquals("1,234 ms",     transformerDebug.ms(1234));
        assertEquals("3,600,000 ms", transformerDebug.ms(3600000));
    }

    @Test
    public void testFileSize()
    {
        assertEquals("unlimited", transformerDebug.fileSize(-1));
        assertEquals("0 bytes",   transformerDebug.fileSize(0));
        assertEquals("1 byte",    transformerDebug.fileSize(1));
        assertEquals("2 bytes",   transformerDebug.fileSize(2));
        assertEquals("2 KB",      transformerDebug.fileSize(2L*1024));
        assertEquals("3 MB",      transformerDebug.fileSize(3L*1024*1024));
        assertEquals("4 GB",      transformerDebug.fileSize(4L*1024*1024*1024));
        assertEquals("5 TB",      transformerDebug.fileSize(5L*1024*1024*1024*1024));

        assertEquals("1.4 KB",    transformerDebug.fileSize(1L*1024 + 511));
        assertEquals("1.5 KB",    transformerDebug.fileSize(1L*1024 + 512));
        assertEquals("1.9 KB",    transformerDebug.fileSize(2L*1024 - 1));

        assertEquals("2.9 MB",      transformerDebug.fileSize(3L*1024*1024           - 1));
        assertEquals("3.9 GB",      transformerDebug.fileSize(4L*1024*1024*1024      - 1));
        assertEquals("4.9 TB",      transformerDebug.fileSize(5L*1024*1024*1024*1024 - 1));
    }

    @Test
    public void testGetRenditionName()
    {
        assertEquals("",                                 transformerDebug.getRenditionName(null));
        assertEquals("-- doclib -- ",                    transformerDebug.getRenditionName("doclib"));
        assertEquals("-- metadataExtract -- ",           transformerDebug.getRenditionName("transform:alfresco-metadata-extract"));
        assertEquals("-- metadataEmbed -- ",             transformerDebug.getRenditionName("transform:alfresco-metadata-embed"));
        assertEquals("-- transform:customTransform -- ", transformerDebug.getRenditionName("transform:customTransform"));
    }

    @Test
    public void testGetTransformName()
    {
        assertEquals(null,                        transformerDebug.getTransformName(null));
        assertEquals(null,                        transformerDebug.getTransformName("doclib"));
        assertEquals("alfresco-metadata-extract", transformerDebug.getTransformName("transform:alfresco-metadata-extract"));
        assertEquals("alfresco-metadata-embed",   transformerDebug.getTransformName("transform:alfresco-metadata-embed"));
        assertEquals("customTransform",           transformerDebug.getTransformName("transform:customTransform"));
    }

    private enum LogLevel
    {
        INFO, DEBUG, TRACE;

        boolean isSet(LogLevel levelSet)
        {
            return levelSet != null && this.compareTo(levelSet) <= 0;
        }
    }

    private class DummyLog extends LogAdapter
    {
        private final StringBuilder stringBuilder;
        private final AtomicReference<LogLevel> logLevel;

        public DummyLog(StringBuilder stringBuilder, AtomicReference<LogLevel> logLevel)
        {
            super(null);
            this.stringBuilder = stringBuilder;
            this.logLevel = logLevel;
        }

        @Override
        public boolean isDebugEnabled()
        {
            return LogLevel.DEBUG.isSet(logLevel.get());
        }

        @Override
        public boolean isTraceEnabled()
        {
            return LogLevel.TRACE.isSet(logLevel.get());
        }

        @Override
        public void debug(Object message, Throwable throwable)
        {
            if (isDebugEnabled())
            {
                append(message, throwable, "DEBUG ");
            }
        }

        @Override
        public void trace(Object message, Throwable throwable)
        {
            if (isTraceEnabled())
            {
                append(message, throwable, "TRACE ");
            }
        }

        private void append(Object message, Throwable throwable, String prefix)
        {
            if (stringBuilder.length() > 0)
            {
                stringBuilder.append('\n');
            }
            stringBuilder.append(prefix).append(message);
            if (throwable != null)
            {
                stringBuilder.append("\n      ").append(throwable.getMessage());
            }
        }
    }
}