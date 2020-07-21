/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.transform.client.model.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TransformServiceRegistryImplTest
{
    public static final String GIF = "gif";
    public static final String JPEG = "jpeg";
    public static final String PNG = "png";
    public static final String TIFF = "tiff";
    public static final String PDF = "pdf";
    public static final String DOC = "doc";
    public static final String XLS = "xls";
    public static final String PPT = "ppt";
    public static final String DOCX = "docx";
    public static final String XLSX = "xlsx";
    public static final String PPTX = "pptx";
    public static final String MSG = "msg";
    public static final String TXT = "txt";

    public static final String GIF_MIMETYPE = "image/gif";
    public static final String JPEG_MIMETYPE = "image/jpeg";
    public static final String PNG_MIMETYPE = "image/png";
    public static final String TIFF_MIMETYPE = "image/tiff";
    public static final String PDF_MIMETYPE = "appliction/pdf";
    public static final String DOC_MIMETYPE = "application/msword";
    public static final String XLS_MIMETYPE = "application/vnd.ms-excel";
    public static final String PPT_MIMETYPE = "application/vnd.ms-powerpoint";
    public static final String DOCX_MIMETYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String XLSX_MIMETYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String PPTX_MIMETYPE = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
    public static final String MSG_MIMETYPE = "application/vnd.ms-outlook";
    public static final String TXT_MIMETYPE = "text/plain";

    public static final String TRANSFORM_SERVICE_CONFIG = "alfresco/transform-service-config.json";
    public static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();

    private TransformServiceRegistryImpl registry;
    private TransformBuilder builder;
    private Transformer transformer;
    private ExtensionMap extensionMap;

    @Before
    public void setUp() throws Exception
    {
        extensionMap = new ExtensionMap()
        {
            private Map<String, String> map = new HashMap<>();

            {
                map.put(GIF, GIF_MIMETYPE);
                map.put(JPEG, JPEG_MIMETYPE);
                map.put(PNG, PNG_MIMETYPE);
                map.put(TIFF, TIFF_MIMETYPE);
                map.put(PDF, PDF_MIMETYPE);
                map.put(DOC, DOC_MIMETYPE);
                map.put(XLS, XLS_MIMETYPE);
                map.put(PPT, PPT_MIMETYPE);
                map.put(DOCX, DOCX_MIMETYPE);
                map.put(XLSX, XLSX_MIMETYPE);
                map.put(PPTX, PPTX_MIMETYPE);
                map.put(MSG, MSG_MIMETYPE);
                map.put(TXT, TXT_MIMETYPE);
            }

            @Override
            public String toMimetype(String extension)
            {
                return map.get(extension);
            }
        };

        registry = buildTransformServiceRegistryImpl();

        builder = new TransformBuilder();
    }

    private TransformServiceRegistryImpl buildTransformServiceRegistryImpl()
    {
        TransformServiceRegistryImpl registry = new TransformServiceRegistryImpl();
        registry.setExtensionMap(extensionMap);
        registry.setJsonObjectMapper(JSON_OBJECT_MAPPER);
        return registry;
    }

    @After
    public void tearDown()
    {
        // shut down
    }

    private void assertAddToPossibleOptions(TransformOptionGroup transformOptionGroup, String actualOptionNames, String expectedNames, String expectedRequired)
    {
        Map<String, String> actualOptions = buildActualOptions(actualOptionNames);
        Set<String> expectedNameSet = expectedNames == null || expectedNames.isEmpty() ? Collections.EMPTY_SET : new HashSet(Arrays.asList(expectedNames.split(", ")));
        Set<String> expectedRequiredSet = expectedRequired == null || expectedRequired.isEmpty() ? Collections.EMPTY_SET : new HashSet(Arrays.asList(expectedRequired.split(", ")));

        Map<String, Boolean> possibleTransformOptions = new HashMap<>();

        registry.addToPossibleTransformOptions(possibleTransformOptions, transformOptionGroup, true, actualOptions);

        assertEquals("The expected options don't match", expectedNameSet, possibleTransformOptions.keySet());
        for (String name: possibleTransformOptions.keySet())
        {
            Boolean required = possibleTransformOptions.get(name);
            if (required)
            {
                assertTrue(name+" should be REQUIRED", expectedRequiredSet.contains(name));
            }
            else
            {
                assertFalse(name+" should be OPTIONAL", expectedRequiredSet.contains(name));
            }
        }
    }

    // transformOptionNames are upper case if required.
    private void assertIsSupported(String actualOptionNames, String transformOptionNames, String unsupportedMsg)
    {
        Map<String, String> actualOptions = buildActualOptions(actualOptionNames);

        Map<String, Boolean> transformOptions = new HashMap<>();
        Set<String> transformOptionNameSet = transformOptionNames == null || transformOptionNames.isEmpty() ? Collections.EMPTY_SET : new HashSet(Arrays.asList(transformOptionNames.split(", ")));
        for (String name : transformOptionNameSet)
        {
            Boolean required = name.toUpperCase().equals(name);
            transformOptions.put(name, required);
        }

        boolean supported = registry.isSupported(transformOptions, actualOptions);
        if (unsupportedMsg == null || unsupportedMsg.isEmpty())
        {
            assertTrue("Expected these options to be SUPPORTED", supported);
        }
        else
        {
            assertFalse("Expected these options NOT to be supported, because "+unsupportedMsg, supported);
        }
    }

    private void assertTransformOptions(List<TransformOption> transformOptions)
    {
        transformer = new Transformer("name", "1",
                transformOptions,
                Arrays.asList(
                        new SupportedSourceAndTarget(DOC, TXT, -1),
                        new SupportedSourceAndTarget(XLS, TXT, 1024000)));

        registry = buildTransformServiceRegistryImpl();
        registry.register(transformer);

        assertTrue(registry.isSupported(XLS_MIMETYPE, 1024, TXT_MIMETYPE, Collections.emptyMap(), null));
        assertTrue(registry.isSupported(XLS_MIMETYPE, 1024000, TXT_MIMETYPE, null, null));
        assertFalse(registry.isSupported(XLS_MIMETYPE, 1024001, TXT_MIMETYPE, Collections.emptyMap(), null));
        assertTrue(registry.isSupported(DOC_MIMETYPE, 1024001, TXT_MIMETYPE, null, null));
    }

    private void assertSupported(String sourceExt, long sourceSizeInBytes, String targetExt,
                                 Map<String, String> actualOptions, String unsupportedMsg)
    {
        assertSupported(sourceExt, sourceSizeInBytes, targetExt, actualOptions, unsupportedMsg, transformer);
    }

    private void assertSupported(String sourceExt, long sourceSizeInBytes, String targetExt,
                                 Map<String, String> actualOptions, String unsupportedMsg,
                                 Transformer... transformers)
    {
        registry = buildTransformServiceRegistryImpl();
        for (Transformer transformer : transformers)
        {
            registry.register(transformer);
        }
        assertSupported(sourceExt, sourceSizeInBytes, targetExt, actualOptions, null, unsupportedMsg);
    }

    private void assertSupported(String sourceExt, long sourceSizeInBytes, String targetExt,
                                 Map<String, String> actualOptions, String transformName,
                                 String unsupportedMsg)
    {
        String sourceMimetype = extensionMap.toMimetype(sourceExt);
        String targetMimetype = extensionMap.toMimetype(targetExt);
        boolean supported = registry.isSupported(sourceMimetype, sourceSizeInBytes, targetMimetype, actualOptions, transformName);
        if (unsupportedMsg == null || unsupportedMsg.isEmpty())
        {
            assertTrue(sourceExt+" to "+targetExt+" should be SUPPORTED", supported);
        }
        else
        {
            assertFalse(sourceExt+" to "+targetExt+" should NOT be supported", supported);
        }
    }

    private Map<String, String> buildActualOptions(String actualOptionNames)
    {
        Map<String, String> actualOptions = new HashMap<>();
        Set<String> actualOptionNamesSet = actualOptionNames == null || actualOptionNames.isEmpty() ? Collections.EMPTY_SET : new HashSet(Arrays.asList(actualOptionNames.split(", ")));
        for (String name : actualOptionNamesSet)
        {
            actualOptions.put(name, "value for " + name);
        }
        return actualOptions;
    }

    @Test
    public void testReadWriteJson() throws IOException
    {
        Transformer libreOffice = new Transformer("libreOffice", "1",
                null, // there are no options
                Arrays.asList(
                        new SupportedSourceAndTarget(DOC, PDF, -1),
                        new SupportedSourceAndTarget(XLS, PDF, 1024000),
                        new SupportedSourceAndTarget(PPT, PDF, -1),
                        new SupportedSourceAndTarget(MSG, PDF, -1)));

        Transformer pdfRenderer = new Transformer("pdfRenderer", "1",
                Arrays.asList(
                        new TransformOptionValue(false, "page"),
                        new TransformOptionValue(false, "width"),
                        new TransformOptionValue(false, "height"),
                        new TransformOptionValue(false, "allowPdfEnlargement"),
                        new TransformOptionValue(false, "maintainPdfAspectRatio")),
                Arrays.asList(
                        new SupportedSourceAndTarget(PDF, PNG, -1)));

        Transformer tika = new Transformer("tika", "1",
                Arrays.asList(
                        new TransformOptionValue(false, "transform"),
                        new TransformOptionValue(false, "includeContents"),
                        new TransformOptionValue(false, "notExtractBookmarksText"),
                        new TransformOptionValue(false, "targetMimetype"),
                        new TransformOptionValue(false, "targetEncoding")),
                Arrays.asList(
                        new SupportedSourceAndTarget(PDF, TXT, -1),
                        new SupportedSourceAndTarget(DOC, TXT, -1),
                        new SupportedSourceAndTarget(XLS, TXT, 1024000),
                        new SupportedSourceAndTarget(PPT, TXT, -1),
                        new SupportedSourceAndTarget(MSG, TXT, -1)));

        Transformer imageMagick = new Transformer("imageMagick", "1",
                Arrays.asList(
                        new TransformOptionValue(false, "alphaRemove"),
                        new TransformOptionValue(false, "autoOrient"),
                        new TransformOptionGroup(false, Arrays.asList(
                                new TransformOptionValue(false, "cropGravity"),
                                new TransformOptionValue(false, "cropWidth"),
                                new TransformOptionValue(false, "cropHeight"),
                                new TransformOptionValue(false, "cropPercentage"),
                                new TransformOptionValue(false, "cropXOffset"),
                                new TransformOptionValue(false, "cropYOffset"))),
                        new TransformOptionGroup(false, Arrays.asList(
                                new TransformOptionValue(false, "thumbnail"),
                                new TransformOptionValue(false, "resizeHeight"),
                                new TransformOptionValue(false, "resizeWidth"),
                                new TransformOptionValue(false, "resizePercentage"),
                                new TransformOptionValue(false, "maintainAspectRatio")))),
                Arrays.asList(
                        new SupportedSourceAndTarget(GIF, GIF, -1),
                        new SupportedSourceAndTarget(GIF, JPEG, -1),
                        new SupportedSourceAndTarget(GIF, PNG, -1),
                        new SupportedSourceAndTarget(GIF, TIFF, -1),

                        new SupportedSourceAndTarget(JPEG, GIF, -1),
                        new SupportedSourceAndTarget(JPEG, JPEG, -1),
                        new SupportedSourceAndTarget(JPEG, PNG, -1),
                        new SupportedSourceAndTarget(JPEG, TIFF, -1),

                        new SupportedSourceAndTarget(PNG, GIF, -1),
                        new SupportedSourceAndTarget(PNG, JPEG, -1),
                        new SupportedSourceAndTarget(PNG, PNG, -1),
                        new SupportedSourceAndTarget(PNG, TIFF, -1),

                        new SupportedSourceAndTarget(TIFF, GIF, -1),
                        new SupportedSourceAndTarget(TIFF, JPEG, -1),
                        new SupportedSourceAndTarget(TIFF, PNG, -1),
                        new SupportedSourceAndTarget(TIFF, TIFF, -1)));

        Transformer officeToImage = builder.buildPipeLine("officeToImageViaPdf", "1",
                Arrays.asList(
                        new SupportedSourceAndTarget(DOC, GIF, -1),
                        new SupportedSourceAndTarget(DOC, JPEG, -1),
                        new SupportedSourceAndTarget(DOC, PNG, -1),
                        new SupportedSourceAndTarget(DOC, TIFF, -1),
                        new SupportedSourceAndTarget(XLS, GIF, -1),
                        new SupportedSourceAndTarget(XLS, JPEG, -1),
                        new SupportedSourceAndTarget(XLS, PNG, -1),
                        new SupportedSourceAndTarget(XLS, TIFF, -1),
                        new SupportedSourceAndTarget(PPT, GIF, -1),
                        new SupportedSourceAndTarget(PPT, JPEG, -1),
                        new SupportedSourceAndTarget(PPT, PNG, -1),
                        new SupportedSourceAndTarget(PPT, TIFF, -1),
                        new SupportedSourceAndTarget(MSG, GIF, -1),
                        new SupportedSourceAndTarget(MSG, JPEG, -1),
                        new SupportedSourceAndTarget(MSG, PNG, -1),
                        new SupportedSourceAndTarget(MSG, TIFF, -1)),
                Arrays.asList(
                        new ChildTransformer(false, libreOffice),  // to pdf
                        new ChildTransformer(false, pdfRenderer),  // to png
                        new ChildTransformer(true, imageMagick))); // to other image formats

        List<Transformer> transformers1 = Arrays.asList(libreOffice, tika, pdfRenderer, imageMagick, officeToImage);

        File tempFile = File.createTempFile("test", ".json");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new FileWriter(tempFile), transformers1);

        try (Reader reader = new BufferedReader(new FileReader(tempFile)))
        {
            registry.register(reader);
            // Check the count of transforms supported
            assertEquals("The number of UNIQUE source to target mimetypes transforms has changed. Config change?",
                    42, countSupportedTransforms(true));
            assertEquals("The number of source to target mimetypes transforms has changed. " +
                            "There may be multiple transformers for the same combination. Config change?",
                    42, countSupportedTransforms(false));

            // Check a supported transform for each transformer.
            assertSupported(DOC, 1234, PDF, null, null, ""); // libreoffice
            assertSupported(DOC, 1234, PDF, null, null, ""); // libreoffice
            assertSupported(PDF, 1234, PNG, null, null, ""); // pdfrenderer
            assertSupported(JPEG,1234, GIF, null, null, ""); // imagemagick
            assertSupported(MSG, 1234, TXT, null, null, ""); // tika
            assertSupported(MSG, 1234, GIF, null, null, ""); // officeToImageViaPdf
            assertSupported(DOC, 1234, PNG, null, null, ""); // officeToImageViaPdf
        }
    }

    @Test
    public void testJsonConfig() throws IOException
    {
        try (Reader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().
                getResourceAsStream(TRANSFORM_SERVICE_CONFIG))))
        {
            registry.register(reader);

            // Check the count of transforms supported
            assertEquals("The number of UNIQUE source to target mimetypes transforms has changed. Config change?",
                    63, countSupportedTransforms(true));
            assertEquals("The number of source to target mimetypes transforms has changed. " +
                            "There may be multiple transformers for the same combination. Config change?",
                    63, countSupportedTransforms(false));

            // Check a supported transform for each transformer.
            assertSupported(DOC, 1234, PDF, null, null, ""); // libreoffice
            assertSupported(DOC, 1234, PDF, null, null, ""); // libreoffice
            assertSupported(PDF, 1234, PNG, null, null, ""); // pdfrenderer
            assertSupported(JPEG,1234, GIF, null, null, ""); // imagemagick
            assertSupported(MSG, 1234, TXT, null, null, ""); // tika
            assertSupported(MSG, 1234, GIF, null, null, ""); // officeToImageViaPdf

            Map<String, String> invalidPdfOptions = new HashMap<>();
            invalidPdfOptions.put("allowEnlargement", "false");
            assertSupported(DOC, 1234, PDF, invalidPdfOptions, null, "Invalid as there is a extra option");
        }
    }

    @Test
    public void testJsonPipeline() throws IOException
    {
        try (Reader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().
                getResourceAsStream("alfresco/transform-service-config-test1.json"))))
        {
            registry.register(reader);

            // Check the count of transforms supported
            assertEquals("The number of UNIQUE source to target mimetypes transforms has changed. Config change?",
                    4, countSupportedTransforms(true));
            assertEquals("The number of source to target mimetypes transforms has changed. " +
                            "There may be multiple transformers for the same combination. Config change?",
                    4, countSupportedTransforms(false));

            ConcurrentMap<String, List<TransformServiceRegistryImpl.SupportedTransform>> transformer =
                    registry.transformers.get("officeToImageViaPdf");

            // Check required and optional default correctly
            ConcurrentMap<String, List<TransformServiceRegistryImpl.SupportedTransform>> transformsToWord =
                    registry.transformers.get("application/msword");
            List<TransformServiceRegistryImpl.SupportedTransform> supportedTransforms = transformsToWord.get("image/gif");
            TransformServiceRegistryImpl.SupportedTransform supportedTransform = supportedTransforms.get(0);

            TransformOptionGroup imageMagick = (TransformOptionGroup)supportedTransform.transformOptions.transformOptions.get(0);
            TransformOptionGroup pdf         = (TransformOptionGroup)supportedTransform.transformOptions.transformOptions.get(1);

            TransformOptionValue alphaRemove = (TransformOptionValue)imageMagick.transformOptions.get(0);
            TransformOptionGroup crop = (TransformOptionGroup)imageMagick.transformOptions.get(4);
            TransformOptionValue cropGravity = (TransformOptionValue)crop.transformOptions.get(0);
            TransformOptionValue cropWidth = (TransformOptionValue)crop.transformOptions.get(1);

            assertTrue("The holding group should be required", supportedTransform.transformOptions.isRequired());
            assertTrue("imageMagick should be required as it is set", imageMagick.isRequired());
            assertFalse("pdf should be optional as required is not set", pdf.isRequired());
            assertEquals("alphaRemove", alphaRemove.getName());
            assertEquals("cropGravity", cropGravity.getName());
            assertEquals("cropWidth", cropWidth.getName());
            assertFalse("alphaRemove should be optional as required is not set", alphaRemove.isRequired());
            assertFalse("crop should be optional as required is not set", crop.isRequired());
            assertTrue("cropGravity should be required as it is set", cropGravity.isRequired());
            assertFalse("cropWidth should be optional as required is not set", cropWidth.isRequired());

            // Check a supported transform for each transformer.
            assertSupported(DOC,1234, GIF,  null, null, "");
            assertSupported(DOC,1234, PNG,  null, null, "");
            assertSupported(DOC,1234, JPEG, null, null, "");
            assertSupported(DOC,1234, TIFF, null, null, "");

            Map<String, String> actualOptions = new HashMap<>();
            actualOptions.put("thumbnail", "true");
            actualOptions.put("resizeWidth", "100");
            actualOptions.put("resizeHeight", "100");
            actualOptions.put("allowEnlargement", "false");
            actualOptions.put("maintainAspectRatio", "true");
            assertSupported(DOC,1234, PNG, actualOptions, null, "");
        }
    }

    private int countSupportedTransforms(boolean unique)
    {
        int count = 0;
        int uniqueCount = 0;
        for (ConcurrentMap<String, List<TransformServiceRegistryImpl.SupportedTransform>> targetMap : registry.transformers.values())
        {
            for (List<TransformServiceRegistryImpl.SupportedTransform> supportedTransforms : targetMap.values())
            {
                uniqueCount++;
                count += supportedTransforms.size();
            }
        }
        return unique ? uniqueCount : count;
    }

    @Test
    public void testOptionalGroups()
    {
        TransformOptionGroup transformOptionGroup =
                new TransformOptionGroup(true, Arrays.asList(
                        new TransformOptionValue(false, "1"),
                        new TransformOptionValue(true, "2"),
                        new TransformOptionGroup(false, Arrays.asList(
                                new TransformOptionValue(false, "3.1"),
                                new TransformOptionValue(false, "3.2"),
                                new TransformOptionValue(false, "3.3"))),
                        new TransformOptionGroup(false, Arrays.asList( // OPTIONAL
                                new TransformOptionValue(false, "4.1"),
                                new TransformOptionValue(true, "4.2"),
                                new TransformOptionValue(false, "4.3")))));

        assertAddToPossibleOptions(transformOptionGroup, "",  "1, 2", "2");
        assertAddToPossibleOptions(transformOptionGroup, "1", "1, 2", "2");
        assertAddToPossibleOptions(transformOptionGroup, "2", "1, 2", "2");
        assertAddToPossibleOptions(transformOptionGroup, "2, 3.2", "1, 2, 3.1, 3.2, 3.3", "2");
        assertAddToPossibleOptions(transformOptionGroup, "2, 4.1", "1, 2, 4.1, 4.2, 4.3", "2, 4.2");
        assertAddToPossibleOptions(transformOptionGroup, "2, 4.2", "1, 2, 4.1, 4.2, 4.3", "2, 4.2");
    }

    @Test
    public void testRequiredGroup()
    {
        TransformOptionGroup transformOptionGroup =
                new TransformOptionGroup(true, Arrays.asList(
                        new TransformOptionValue(false, "1"),
                        new TransformOptionValue(true, "2"),
                        new TransformOptionGroup(false, Arrays.asList(
                                new TransformOptionValue(false, "3.1"),
                                new TransformOptionValue(false, "3.2"),
                                new TransformOptionValue(false, "3.3"))),
                        new TransformOptionGroup(true, Arrays.asList( // REQUIRED
                                new TransformOptionValue(false, "4.1"),
                                new TransformOptionValue(true, "4.2"),
                                new TransformOptionValue(false, "4.3")))));

        assertAddToPossibleOptions(transformOptionGroup, "",  "1, 2, 4.1, 4.2, 4.3", "2, 4.2");
        assertAddToPossibleOptions(transformOptionGroup, "1", "1, 2, 4.1, 4.2, 4.3", "2, 4.2");
        assertAddToPossibleOptions(transformOptionGroup, "2, 3.2", "1, 2, 3.1, 3.2, 3.3, 4.1, 4.2, 4.3", "2, 4.2");
        assertAddToPossibleOptions(transformOptionGroup, "2, 4.1", "1, 2, 4.1, 4.2, 4.3", "2, 4.2");
        assertAddToPossibleOptions(transformOptionGroup, "2, 4.2", "1, 2, 4.1, 4.2, 4.3", "2, 4.2");
    }

    @Test
    public void testNesstedGrpups()
    {
        TransformOptionGroup transformOptionGroup =
                new TransformOptionGroup(false, Arrays.asList(
                        new TransformOptionGroup(false, Arrays.asList(
                                new TransformOptionValue(false, "1"),
                                new TransformOptionGroup(false, Arrays.asList(
                                        new TransformOptionValue(false, "1.2"),
                                        new TransformOptionGroup(false, Arrays.asList(
                                                new TransformOptionValue(false, "1.2.3"))))))),
                        new TransformOptionGroup(false, Arrays.asList(
                                new TransformOptionValue(false, "2"),
                                new TransformOptionGroup(false, Arrays.asList(
                                        new TransformOptionValue(false, "2.2"),
                                        new TransformOptionGroup(false, Arrays.asList(
                                                new TransformOptionGroup(false, Arrays.asList(
                                                        new TransformOptionValue(false, "2.2.1.2"))))))))),
                        new TransformOptionGroup(false, Arrays.asList(
                                new TransformOptionValue(true, "3"), // REQUIRED
                                new TransformOptionGroup(false, Arrays.asList(
                                        new TransformOptionGroup(false, Arrays.asList(
                                                new TransformOptionGroup(false, Arrays.asList(
                                                        new TransformOptionValue(false, "3.1.1.2"))))))))),
                        new TransformOptionGroup(false, Arrays.asList(
                                new TransformOptionValue(false, "4"),
                                new TransformOptionGroup(true, Arrays.asList( // REQUIRED
                                        new TransformOptionGroup(false, Arrays.asList(
                                                new TransformOptionGroup(false, Arrays.asList(
                                                        new TransformOptionValue(false, "4.1.1.2"))))))))),
                        new TransformOptionGroup(false, Arrays.asList(
                                new TransformOptionValue(false, "5"),
                                new TransformOptionGroup(false, Arrays.asList(
                                        new TransformOptionGroup(true, Arrays.asList( // REQUIRED
                                                new TransformOptionGroup(false, Arrays.asList(
                                                        new TransformOptionValue(false, "5.1.1.2"))))))))),
                        new TransformOptionGroup(false, Arrays.asList(
                                new TransformOptionValue(false, "6"),
                                new TransformOptionGroup(false, Arrays.asList(
                                        new TransformOptionGroup(false, Arrays.asList(
                                                new TransformOptionGroup(true, Arrays.asList( // REQUIRED
                                                        new TransformOptionValue(false, "6.1.1.2"))))))))),
                        new TransformOptionGroup(false, Arrays.asList(
                                new TransformOptionValue(false, "7"),
                                new TransformOptionGroup(false, Arrays.asList(
                                        new TransformOptionGroup(false, Arrays.asList(
                                                new TransformOptionGroup(false, Arrays.asList(
                                                        new TransformOptionValue(true, "7.1.1.2"))))))))) // REQUIRED
                ));

        assertAddToPossibleOptions(transformOptionGroup, "", "", "");
        assertAddToPossibleOptions(transformOptionGroup, "1", "1", "");
        assertAddToPossibleOptions(transformOptionGroup, "1, 7", "1, 7", "");
        assertAddToPossibleOptions(transformOptionGroup, "1, 7.1.1.2", "1, 7, 7.1.1.2", "7.1.1.2");
        assertAddToPossibleOptions(transformOptionGroup, "1, 6", "1, 6", "");
        assertAddToPossibleOptions(transformOptionGroup, "1, 6.1.1.2", "1, 6, 6.1.1.2", "");
        assertAddToPossibleOptions(transformOptionGroup, "1, 5", "1, 5", "");
        assertAddToPossibleOptions(transformOptionGroup, "1, 5.1.1.2", "1, 5, 5.1.1.2", "");
        assertAddToPossibleOptions(transformOptionGroup, "1, 4", "1, 4", "");
        assertAddToPossibleOptions(transformOptionGroup, "1, 4.1.1.2", "1, 4, 4.1.1.2", "");
        assertAddToPossibleOptions(transformOptionGroup, "1, 3", "1, 3", "3");
        assertAddToPossibleOptions(transformOptionGroup, "1, 3.1.1.2", "1, 3, 3.1.1.2", "3");

        assertAddToPossibleOptions(transformOptionGroup, "2",       "2", "");
        assertAddToPossibleOptions(transformOptionGroup, "2, 2.2",  "2, 2.2", "");
        assertAddToPossibleOptions(transformOptionGroup, "3",       "3",          "3");
        assertAddToPossibleOptions(transformOptionGroup, "3.1.1.2", "3, 3.1.1.2", "3");
    }

    @Test
    public void testSupportedOptions()
    {
        assertIsSupported("a", "a, B, c", "required option B is missing");
        assertIsSupported("",  "a, B, c", "required option B is missing");
        assertIsSupported("B", "a, B, c", null);
        assertIsSupported("B, c", "a, B, c", null);
        assertIsSupported("B, a, c", "a, B, c", null);

        assertIsSupported("B, d",    "a, B, c", "there is an extra option d");
        assertIsSupported("B, c, d", "a, B, c", "there is an extra option d");
        assertIsSupported("d", "a, B, c", "required option B is missing and there is an extra option d");

        assertIsSupported("a", "a, b, c", null);
        assertIsSupported("", "a, b, c", null);
        assertIsSupported("a, b, c", "a, b, c", null);
    }

    @Test
    public void testNoActualOptions()
    {
        assertTransformOptions(Arrays.asList(
                new TransformOptionValue(false, "option1"),
                new TransformOptionValue(false, "option2")));
    }

    @Test
    public void testNoTrasformOptions()
    {
        assertTransformOptions(Collections.emptyList());
        assertTransformOptions(null);
    }

    @Test
    public void testSupported()
    {
        transformer = new Transformer("name", "1",
                Arrays.asList(
                        new TransformOptionValue(false, "page"),
                        new TransformOptionValue(false, "width"),
                        new TransformOptionValue(false, "height")),
                Arrays.asList(
                        new SupportedSourceAndTarget(DOC, GIF, 102400),
                        new SupportedSourceAndTarget(DOC, JPEG, -1),
                        new SupportedSourceAndTarget(MSG, GIF, -1)));

        assertSupported(DOC, 1024, GIF, null, null);
        assertSupported(DOC, 102400, GIF, null, null);
        assertSupported(DOC, 102401, GIF, null, "source is too large");
        assertSupported(DOC, 1024, JPEG, null, null);
        assertSupported(GIF, 1024, DOC, null, GIF+" is not a source of this transformer");
        assertSupported(MSG, 1024, GIF, null, null);
        assertSupported(MSG, 1024, JPEG, null, MSG+" to "+JPEG+" is not supported by this transformer");

        assertSupported(DOC, 1024, GIF, buildActualOptions("page, width"), null);
        assertSupported(DOC, 1024, GIF, buildActualOptions("page, width, startPage"), "startPage is not an option");
    }

    @Test
    public void testCache()
    {
        // Note: transformNames are an alias for a set of actualOptions and the target mimetpe. The source mimetype may change.
        transformer = new Transformer("name", "1",
                Arrays.asList(
                        new TransformOptionValue(false, "page"),
                        new TransformOptionValue(false, "width"),
                        new TransformOptionValue(false, "height")),
                Arrays.asList(
                        new SupportedSourceAndTarget(DOC, GIF, 102400),
                        new SupportedSourceAndTarget(MSG, GIF, -1)));

        registry.register(transformer);

        assertSupported(DOC, 1024, GIF, null, "doclib", "");
        assertSupported(MSG, 1024, GIF, null, "doclib", "");

        assertEquals(102400L, registry.getMaxSize(DOC_MIMETYPE, GIF_MIMETYPE, null, "doclib"));
        assertEquals(-1L, registry.getMaxSize(MSG_MIMETYPE, GIF_MIMETYPE, null, "doclib"));

        // Change the cached value and try and check we are now using the cached value.
        registry.cachedMaxSizes.get("doclib").put(DOC_MIMETYPE, 1234L);
        assertEquals(1234L, registry.getMaxSize(DOC_MIMETYPE, GIF_MIMETYPE, null, "doclib"));
    }

    @Test
    public void testMultipleTransformers()
    {
        Transformer transformer1 = new Transformer("transformer1", "1",
                Arrays.asList(
                        new TransformOptionValue(false, "page"),
                        new TransformOptionValue(false, "width"),
                        new TransformOptionValue(false, "height")),
                Arrays.asList(
                        new SupportedSourceAndTarget(DOC, GIF, 102400),
                        new SupportedSourceAndTarget(DOC, JPEG, -1),
                        new SupportedSourceAndTarget(MSG, GIF, -1)));

        Transformer transformer2 = new Transformer("transformer2", "1",
                Arrays.asList(
                        new TransformOptionValue(false, "opt1"),
                        new TransformOptionValue(false, "opt2")),
                Arrays.asList(
                        new SupportedSourceAndTarget(PDF, GIF, -1),
                        new SupportedSourceAndTarget(PPT, JPEG, -1)));

        Transformer transformer3 = new Transformer("transformer3", "1",
                Arrays.asList(
                        new TransformOptionValue(false, "opt1")),
                Arrays.asList(
                        new SupportedSourceAndTarget(DOC, GIF, -1)));

        Map<String, String> actualOptions = null;

        assertSupported(DOC, 1024, GIF, actualOptions, null, transformer1);
        assertSupported(DOC, 1024, GIF, actualOptions, null, transformer1, transformer2);
        assertSupported(DOC, 1024, GIF, actualOptions, null, transformer1, transformer2, transformer3);

        assertSupported(DOC, 102401, GIF, null, "source is too large", transformer1);
        assertSupported(DOC, 102401, GIF, null, null, transformer1, transformer3);

        assertSupported(PDF, 1024, GIF, actualOptions, "Only transformer2 supports these mmetypes", transformer1);
        assertSupported(PDF, 1024, GIF, actualOptions, null, transformer1, transformer2);
        assertSupported(PDF, 1024, GIF, actualOptions, null, transformer1, transformer2, transformer3);

        actualOptions = buildActualOptions("opt1");
        assertSupported(PDF, 1024, GIF, actualOptions, "Only transformer2/4 supports these options", transformer1);
        assertSupported(PDF, 1024, GIF, actualOptions, null, transformer1, transformer2);
        assertSupported(PDF, 1024, GIF, actualOptions, null, transformer1, transformer2, transformer3);
        assertSupported(PDF, 1024, GIF, actualOptions, "transformer4 supports opt1 but not the source mimetype ", transformer1, transformer3);
    }

    @Test
    public void testPipeline()
    {
        Transformer transformer1 = new Transformer("transformer1", "1",
                null, // there are no options
                Arrays.asList(
                        new SupportedSourceAndTarget(DOC, PDF, -1),
                        new SupportedSourceAndTarget(MSG, PDF, -1)));

        Transformer transformer2 = new Transformer("transformer2", "1",
                Arrays.asList(
                        new TransformOptionValue(false, "page"),
                        new TransformOptionValue(false, "width"),
                        new TransformOptionValue(false, "height")),
                Arrays.asList(
                        new SupportedSourceAndTarget(PDF, GIF, -1),
                        new SupportedSourceAndTarget(PDF, JPEG, -1)));

        buildPipelineTransformer(transformer1, transformer2);

        assertSupported(DOC, 1024, GIF, null, null);
        assertSupported(DOC, 1024, JPEG, null, null);
        assertSupported(GIF, 1024, DOC, null, GIF+" is not a source of this transformer");
        assertSupported(MSG, 1024, GIF, null, null);
        assertSupported(MSG, 1024, JPEG, null, MSG+" to "+JPEG+" is not supported by this transformer");

        // Now try the options
        assertSupported(DOC, 1024, GIF, buildActualOptions("page, width"), null);
        assertSupported(DOC, 1024, GIF, buildActualOptions("page, width, startPage"), "startPage is not an option");

        // Add options to the first transformer
        transformer1.setTransformOptions(Arrays.asList(
                new TransformOptionValue(false, "startPage"),
                new TransformOptionValue(false, "endPage")));
        buildPipelineTransformer(transformer1, transformer2);

        assertSupported(DOC, 1024, GIF, buildActualOptions("page, width"), null);
        assertSupported(DOC, 1024, GIF, buildActualOptions("page, width, startPage"), null);
    }

    private void buildPipelineTransformer(Transformer transformer1, Transformer transformer2)
    {
        transformer = builder.buildPipeLine("officeToImage", "1",
                Arrays.asList(
                        new SupportedSourceAndTarget(DOC, GIF, -1),
                        new SupportedSourceAndTarget(DOC, JPEG, -1),
                        new SupportedSourceAndTarget(MSG, GIF, -1)),
                Arrays.asList(
                        new ChildTransformer(false, transformer1),
                        new ChildTransformer(true, transformer2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void textMissingMimetype()
    {
        transformer = new Transformer("name", "1",
                null, // there are no options
                Arrays.asList(
                        new SupportedSourceAndTarget("rubbish", PDF, -1)));
        registry.register(transformer);
    }
}