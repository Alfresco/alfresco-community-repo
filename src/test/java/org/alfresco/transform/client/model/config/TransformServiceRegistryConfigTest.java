/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.quartz.CronExpression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test the config received from the Transform Service about what it supports.
 */
public class TransformServiceRegistryConfigTest
{
    private static Log log = LogFactory.getLog(TransformServiceRegistryConfigTest.class);

    public static final String GIF = "image/gif";
    public static final String JPEG = "image/jpeg";
    public static final String PNG = "image/png";
    public static final String TIFF = "image/tiff";
    public static final String PDF = "application/pdf";
    public static final String DOC = "application/msword";
    public static final String XLS = "application/vnd.ms-excel";
    public static final String PPT = "application/vnd.ms-powerpoint";
    public static final String MSG = "application/vnd.ms-outlook";
    public static final String TXT = "text/plain";

    private static final String TRANSFORM_SERVICE_CONFIG = "alfresco/transform-service-config-test.json";
    private static final String TRANSFORM_SERVICE_CONFIG_PIPELINE = "alfresco/transform-service-config-pipeline-test.json";

    public static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();

    private TransformServiceRegistryImpl registry;
    protected TransformBuilder builder;
    protected InlineTransformer transformer;

    @Before
    public void setUp() throws Exception
    {
        registry = buildTransformServiceRegistryImpl();
        builder = new TransformBuilder();
        LogManager.getLogger(TransformServiceRegistryConfigTest.class).setLevel(Level.DEBUG);
    }

    protected TransformServiceRegistryImpl buildTransformServiceRegistryImpl() throws Exception
    {
        TransformServiceRegistryImpl registry = new TransformServiceRegistryImpl()
        {
            @Override
            public boolean readConfig() throws IOException
            {
                return true;
            }

            @Override
            protected Log getLog()
            {
                return log;
            }
        };
        registry.setJsonObjectMapper(JSON_OBJECT_MAPPER);
        registry.setCronExpression(null); // just read once
        registry.afterPropertiesSet();
        return registry;
    }

    @After
    public void tearDown()
    {
        // shut down
    }

    protected String getTransformServiceConfig()
    {
        return TRANSFORM_SERVICE_CONFIG;
    }

    protected String getTransformServiceConfigPipeline()
    {
        return TRANSFORM_SERVICE_CONFIG_PIPELINE;
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

    private void assertTransformOptions(Set<TransformOption> transformOptions) throws Exception
    {
        transformer = new InlineTransformer("name",
                transformOptions,
                Set.of(
                        new SupportedSourceAndTarget(DOC, TXT, -1),
                        new SupportedSourceAndTarget(XLS, TXT, 1024000)));

        registry = buildTransformServiceRegistryImpl();
        registry.register(transformer, getBaseUrl(transformer), getClass().getName());

        assertTrue(registry.isSupported(XLS, 1024, TXT, Collections.emptyMap(), null));
        assertTrue(registry.isSupported(XLS, 1024000, TXT, null, null));
        assertFalse(registry.isSupported(XLS, 1024001, TXT, Collections.emptyMap(), null));
        assertTrue(registry.isSupported(DOC, 1024001, TXT, null, null));
    }

    protected String getBaseUrl(InlineTransformer transformer)
    {
        return null;
    }

    private void assertTransformerName(String sourceMimetype, long sourceSizeInBytes, String targetMimetype,
                                       Map<String, String> actualOptions, String expectedTransformerName,
                                       InlineTransformer... transformers) throws Exception
    {
        buildAndPopulateRegistry(transformers);
        String transformerName = registry.getTransformerName(sourceMimetype, sourceSizeInBytes, targetMimetype, actualOptions, null);
        assertEquals(sourceMimetype+" to "+targetMimetype+" should have returned "+expectedTransformerName, expectedTransformerName, transformerName);
    }

    private void assertSupported(String sourceMimetype, long sourceSizeInBytes, String targetMimetype,
                                 Map<String, String> actualOptions, String unsupportedMsg)  throws Exception
    {
        assertSupported(sourceMimetype, sourceSizeInBytes, targetMimetype, actualOptions, unsupportedMsg, transformer);
    }

    private void assertSupported(String sourceMimetype, long sourceSizeInBytes, String targetMimetype,
                                 Map<String, String> actualOptions, String unsupportedMsg,
                                 InlineTransformer... transformers) throws Exception
    {
        buildAndPopulateRegistry(transformers);
        assertSupported(sourceMimetype, sourceSizeInBytes, targetMimetype, actualOptions, null, unsupportedMsg);
    }

    private void buildAndPopulateRegistry(InlineTransformer[] transformers)  throws Exception
    {
        registry = buildTransformServiceRegistryImpl();
        for (InlineTransformer transformer : transformers)
        {
            registry.register(transformer, getBaseUrl(transformer), getClass().getName());
        }
    }

    private void assertSupported(String sourceMimetype, long sourceSizeInBytes, String targetMimetype,
                                 Map<String, String> actualOptions, String renditionName,
                                 String unsupportedMsg)
    {
        boolean supported = registry.isSupported(sourceMimetype, sourceSizeInBytes, targetMimetype, actualOptions, renditionName);
        if (unsupportedMsg == null || unsupportedMsg.isEmpty())
        {
            assertTrue(sourceMimetype+" to "+targetMimetype+" should be SUPPORTED", supported);
        }
        else
        {
            assertFalse(sourceMimetype+" to "+targetMimetype+" should NOT be supported", supported);
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

    private void register(String path) throws IOException
    {
        CombinedConfig combinedConfig = new CombinedConfig(log);
        combinedConfig.addLocalConfig(path);
        combinedConfig.register(registry);
    }

    @Test
    public void testReadWriteJson() throws IOException
    {
        InlineTransformer libreoffice = new InlineTransformer("libreoffice",
                null, // there are no options
                Set.of(
                        new SupportedSourceAndTarget(DOC, PDF, -1),
                        new SupportedSourceAndTarget(XLS, PDF, 1024000),
                        new SupportedSourceAndTarget(PPT, PDF, -1),
                        new SupportedSourceAndTarget(MSG, PDF, -1)));

        InlineTransformer pdfrenderer = new InlineTransformer("pdfrenderer",
                Set.of(
                        new TransformOptionValue(false, "page"),
                        new TransformOptionValue(false, "width"),
                        new TransformOptionValue(false, "height"),
                        new TransformOptionValue(false, "allowPdfEnlargement"),
                        new TransformOptionValue(false, "maintainPdfAspectRatio")),
                Set.of(
                        new SupportedSourceAndTarget(PDF, PNG, -1)));

        InlineTransformer tika = new InlineTransformer("tika",
                Set.of(
                        new TransformOptionValue(false, "transform"),
                        new TransformOptionValue(false, "includeContents"),
                        new TransformOptionValue(false, "notExtractBookmarksText"),
                        new TransformOptionValue(false, "targetMimetype"),
                        new TransformOptionValue(false, "targetEncoding")),
                Set.of(
                        new SupportedSourceAndTarget(PDF, TXT, -1),
                        new SupportedSourceAndTarget(DOC, TXT, -1),
                        new SupportedSourceAndTarget(XLS, TXT, 1024000),
                        new SupportedSourceAndTarget(PPT, TXT, -1),
                        new SupportedSourceAndTarget(MSG, TXT, -1)));

        InlineTransformer imagemagick = new InlineTransformer("imagemagick",
                Set.of(
                        new TransformOptionValue(false, "alphaRemove"),
                        new TransformOptionValue(false, "autoOrient"),
                        new TransformOptionGroup(false, Set.of(
                                new TransformOptionValue(false, "cropGravity"),
                                new TransformOptionValue(false, "cropWidth"),
                                new TransformOptionValue(false, "cropHeight"),
                                new TransformOptionValue(false, "cropPercentage"),
                                new TransformOptionValue(false, "cropXOffset"),
                                new TransformOptionValue(false, "cropYOffset"))),
                        new TransformOptionGroup(false, Set.of(
                                new TransformOptionValue(false, "thumbnail"),
                                new TransformOptionValue(false, "resizeHeight"),
                                new TransformOptionValue(false, "resizeWidth"),
                                new TransformOptionValue(false, "resizePercentage"),
                                new TransformOptionValue(false, "maintainAspectRatio")))),
                Set.of(
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

        InlineTransformer officeToImage = builder.buildPipeLine("transformer1",
                Set.of(
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
                        new ChildTransformer(false, libreoffice),  // to pdf
                        new ChildTransformer(false, pdfrenderer),  // to png
                        new ChildTransformer(true, imagemagick))); // to other image formats

        List<InlineTransformer> transformers1 = Arrays.asList(libreoffice, tika, pdfrenderer, imagemagick, officeToImage);

        File tempFile = File.createTempFile("test", ".json");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new FileWriter(tempFile), transformers1);

        try (Reader reader = new BufferedReader(new FileReader(tempFile)))
        {
            registry.register(reader, getClass().getName());
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
            assertSupported(MSG, 1234, GIF, null, null, ""); // transformer1 (officeToImageViaPdf)
            assertSupported(DOC, 1234, PNG, null, null, ""); // transformer1 (officeToImageViaPdf)
        }
    }

    @Test
    public void testJsonConfig() throws IOException
    {
        register(getTransformServiceConfig());

        // Check the count of transforms supported
        assertEquals("The number of UNIQUE source to target mimetypes transforms has changed. Config change?",
                60, countSupportedTransforms(true));
        assertEquals("The number of source to target mimetypes transforms has changed. " +
                        "There may be multiple transformers for the same combination. Config change?",
                60, countSupportedTransforms(false));

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

    @Test
    public void testJsonPipeline() throws IOException
    {
        register(getTransformServiceConfigPipeline());

        // Check the count of transforms supported
        int expectedTransforms = getExpectedTransformsForTestJsonPipeline();
        assertEquals("The number of UNIQUE source to target mimetypes transforms has changed. Config change?",
                expectedTransforms, countSupportedTransforms(true));
        assertEquals("The number of source to target mimetypes transforms has changed. " +
                        "There may be multiple transformers for the same combination. Config change?",
                expectedTransforms, countSupportedTransforms(false));

        ConcurrentMap<String, List<TransformServiceRegistryImpl.SupportedTransform>> transformer =
                registry.getData().transformers.get("officeToImageViaPdf");

        // Check required and optional default correctly
        ConcurrentMap<String, List<TransformServiceRegistryImpl.SupportedTransform>> transformsToWord =
                registry.getData().transformers.get(DOC);
        List<TransformServiceRegistryImpl.SupportedTransform> supportedTransforms = transformsToWord.get(GIF);
        TransformServiceRegistryImpl.SupportedTransform supportedTransform = supportedTransforms.get(0);

        Set<TransformOption> transformOptionsSet = supportedTransform.transformOptions.getTransformOptions();
        System.out.println("Nothing");

        Iterator<TransformOption> iterator = transformOptionsSet.iterator();
        assertTrue("Expected transform values", iterator.hasNext());
        // Because Set is unordered we don't know which TransformOptionGroup we retrieve
        TransformOptionGroup transformOptions1 = (TransformOptionGroup)iterator.next();

        assertTrue("Expected transform values", iterator.hasNext());
        TransformOptionGroup transformOptions2 = (TransformOptionGroup)iterator.next();

        TransformOptionGroup imagemagick;
        TransformOptionGroup pdf;

        if(containsTransformOptionValueName(transformOptions1, "alphaRemove"))
        {
            imagemagick = transformOptions1;
            pdf = transformOptions2;
        }
        else
        {
            imagemagick = transformOptions2;
            pdf = transformOptions1;
        }

        TransformOptionValue alphaRemove = (TransformOptionValue)retrieveTransformOptionByPropertyName(imagemagick, "alphaRemove", "TransformOptionValue");
        TransformOptionGroup crop = (TransformOptionGroup)retrieveTransformOptionByPropertyName(imagemagick, "crop", "TransformOptionGroup");
        TransformOptionValue cropGravity = (TransformOptionValue)retrieveTransformOptionByPropertyName(crop, "cropGravity", "TransformOptionValue");
        TransformOptionValue cropWidth = (TransformOptionValue)retrieveTransformOptionByPropertyName(crop, "cropWidth", "TransformOptionValue");

        assertTrue("The holding group should be required", supportedTransform.transformOptions.isRequired());
        assertFalse("imagemagick should be optional as it is not set", imagemagick.isRequired());
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

    private TransformOption retrieveTransformOptionByPropertyName (TransformOptionGroup transformOptionGroup, String propertyName, String propertyType)
    {
        Iterator<TransformOption> iterator = transformOptionGroup.getTransformOptions().iterator();

        List<TransformOption> transformOptionsList = new ArrayList<>();
        while(iterator.hasNext())
        {
            transformOptionsList.add(iterator.next());
        }

        for (TransformOption t : transformOptionsList)
        {
            if (t instanceof TransformOptionValue)
            {
                TransformOptionValue value = (TransformOptionValue) t;
                if (propertyType.equalsIgnoreCase("TransformOptionValue"))
                {
                    if (value.getName().equalsIgnoreCase(propertyName))
                        return value;
                }
                else
                {
                    if (value.getName().contains(propertyName))
                        return transformOptionGroup;
                }
            }
            else
            {
                TransformOption result = retrieveTransformOptionByPropertyName((TransformOptionGroup)t, propertyName, propertyType);
                if (result != null)
                    return result;
            }
        }
        return null;
    }

    private boolean containsTransformOptionValueName (TransformOptionGroup transformOptionGroup, String propertyName)
    {
        if (retrieveTransformOptionByPropertyName(transformOptionGroup, propertyName, "TransformOptionValue") != null)
            return true;
        return false;
    }

    private boolean containsTransformOptionGroupeName (TransformOptionGroup transformOptionGroup, String propertyName)
    {
        if (retrieveTransformOptionByPropertyName(transformOptionGroup, propertyName, "TransformOptionGroup") != null)
            return true;
        return false;
    }

    protected int getExpectedTransformsForTestJsonPipeline()
    {
        return 4;
    }

    private int countSupportedTransforms(boolean unique)
    {
        int count = 0;
        int uniqueCount = 0;
        for (ConcurrentMap<String, List<TransformServiceRegistryImpl.SupportedTransform>> targetMap : registry.getData().transformers.values())
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
                new TransformOptionGroup(true, Set.of(
                        new TransformOptionValue(false, "1"),
                        new TransformOptionValue(true, "2"),
                        new TransformOptionGroup(false, Set.of(
                                new TransformOptionValue(false, "3.1"),
                                new TransformOptionValue(false, "3.2"),
                                new TransformOptionValue(false, "3.3"))),
                        new TransformOptionGroup(false, Set.of( // OPTIONAL
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
                new TransformOptionGroup(true, Set.of(
                        new TransformOptionValue(false, "1"),
                        new TransformOptionValue(true, "2"),
                        new TransformOptionGroup(false, Set.of(
                                new TransformOptionValue(false, "3.1"),
                                new TransformOptionValue(false, "3.2"),
                                new TransformOptionValue(false, "3.3"))),
                        new TransformOptionGroup(true, Set.of( // REQUIRED
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
                new TransformOptionGroup(false, Set.of(
                        new TransformOptionGroup(false, Set.of(
                                new TransformOptionValue(false, "1"),
                                new TransformOptionGroup(false, Set.of(
                                        new TransformOptionValue(false, "1.2"),
                                        new TransformOptionGroup(false, Set.of(
                                                new TransformOptionValue(false, "1.2.3"))))))),
                        new TransformOptionGroup(false, Set.of(
                                new TransformOptionValue(false, "2"),
                                new TransformOptionGroup(false, Set.of(
                                        new TransformOptionValue(false, "2.2"),
                                        new TransformOptionGroup(false, Set.of(
                                                new TransformOptionGroup(false, Set.of(
                                                        new TransformOptionValue(false, "2.2.1.2"))))))))),
                        new TransformOptionGroup(false, Set.of(
                                new TransformOptionValue(true, "3"), // REQUIRED
                                new TransformOptionGroup(false, Set.of(
                                        new TransformOptionGroup(false, Set.of(
                                                new TransformOptionGroup(false, Set.of(
                                                        new TransformOptionValue(false, "3.1.1.2"))))))))),
                        new TransformOptionGroup(false, Set.of(
                                new TransformOptionValue(false, "4"),
                                new TransformOptionGroup(true, Set.of( // REQUIRED
                                        new TransformOptionGroup(false, Set.of(
                                                new TransformOptionGroup(false, Set.of(
                                                        new TransformOptionValue(false, "4.1.1.2"))))))))),
                        new TransformOptionGroup(false, Set.of(
                                new TransformOptionValue(false, "5"),
                                new TransformOptionGroup(false, Set.of(
                                        new TransformOptionGroup(true, Set.of( // REQUIRED
                                                new TransformOptionGroup(false, Set.of(
                                                        new TransformOptionValue(false, "5.1.1.2"))))))))),
                        new TransformOptionGroup(false, Set.of(
                                new TransformOptionValue(false, "6"),
                                new TransformOptionGroup(false, Set.of(
                                        new TransformOptionGroup(false, Set.of(
                                                new TransformOptionGroup(true, Set.of( // REQUIRED
                                                        new TransformOptionValue(false, "6.1.1.2"))))))))),
                        new TransformOptionGroup(false, Set.of(
                                new TransformOptionValue(false, "7"),
                                new TransformOptionGroup(false, Set.of(
                                        new TransformOptionGroup(false, Set.of(
                                                new TransformOptionGroup(false, Set.of(
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
    public void testNoActualOptions()  throws Exception
    {
        assertTransformOptions(Set.of(
                new TransformOptionValue(false, "option1"),
                new TransformOptionValue(false, "option2")));
    }

    @Test
    public void testNoTrasformOptions()  throws Exception
    {
        assertTransformOptions(Collections.emptySet());
        assertTransformOptions(null);
    }

    @Test
    public void testSupported() throws Exception
    {
        transformer = new InlineTransformer("name",
                Set.of(
                        new TransformOptionValue(false, "page"),
                        new TransformOptionValue(false, "width"),
                        new TransformOptionValue(false, "height")),
                Set.of(
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
        transformer = new InlineTransformer("name",
                Set.of(
                        new TransformOptionValue(false, "page"),
                        new TransformOptionValue(false, "width"),
                        new TransformOptionValue(false, "height")),
                Set.of(
                        new SupportedSourceAndTarget(DOC, GIF, 102400),
                        new SupportedSourceAndTarget(MSG, GIF, -1)));

        registry.register(transformer, getBaseUrl(transformer), getClass().getName());

        assertSupported(DOC, 1024, GIF, null, "doclib", "");
        assertSupported(MSG, 1024, GIF, null, "doclib", "");

        assertEquals(102400L, registry.getMaxSize(DOC, GIF, null, "doclib"));
        assertEquals(-1L, registry.getMaxSize(MSG, GIF, null, "doclib"));

        // Change the cached value and try and check we are now using the cached value.
        List<TransformServiceRegistryImpl.SupportedTransform> supportedTransforms = registry.getData().cachedSupportedTransformList.get("doclib").get(DOC);
        supportedTransforms.get(0).maxSourceSizeBytes = 1234L;
        assertEquals(1234L, registry.getMaxSize(DOC, GIF, null, "doclib"));
    }

    @Test
    public void testGetTransformerName() throws Exception
    {
       InlineTransformer t1 = new InlineTransformer("transformer1", null,
                Set.of(new SupportedSourceAndTarget(MSG, GIF, 100, 50)));
       InlineTransformer t2 = new InlineTransformer("transformer2", null,
                Set.of(new SupportedSourceAndTarget(MSG, GIF, 200, 60)));
       InlineTransformer t3 = new InlineTransformer("transformer3", null,
                Set.of(new SupportedSourceAndTarget(MSG, GIF, 200, 40)));
       InlineTransformer t4 = new InlineTransformer("transformer4", null,
                Set.of(new SupportedSourceAndTarget(MSG, GIF, -1, 100)));
       InlineTransformer t5 = new InlineTransformer("transformer5", null,
                Set.of(new SupportedSourceAndTarget(MSG, GIF, -1, 80)));

        Map<String, String> actualOptions = null;

        // Select on size - priority is ignored
        assertTransformerName(MSG, 100, GIF, actualOptions, "transformer1", t1, t2);
        assertTransformerName(MSG, 150, GIF, actualOptions, "transformer2", t1, t2);
        assertTransformerName(MSG, 250, GIF, actualOptions, null, t1, t2);
        // Select on priority - t1, t2 and t4 are discarded.
        //                      t3 is a higher priority and has a larger size than t1 and t2.
        //                      Similar story fo t4 with t5.
        assertTransformerName(MSG, 100, GIF, actualOptions, "transformer3", t1, t2, t3, t4, t5);
        assertTransformerName(MSG, 200, GIF, actualOptions, "transformer3", t1, t2, t3, t4, t5);
        // Select on size and priority, t1 and t2 discarded
        assertTransformerName(MSG, 200, GIF, actualOptions, "transformer3", t1, t2, t3, t4);
        assertTransformerName(MSG, 300, GIF, actualOptions, "transformer4", t1, t2, t3, t4);
        assertTransformerName(MSG, 300, GIF, actualOptions, "transformer5", t1, t2, t3, t4, t5);
    }

    @Test
    public void testMultipleTransformers() throws Exception
    {
       InlineTransformer transformer1 = new InlineTransformer("transformer1",
                Set.of(
                        new TransformOptionValue(false, "page"),
                        new TransformOptionValue(false, "width"),
                        new TransformOptionValue(false, "height")),
                Set.of(
                        new SupportedSourceAndTarget(DOC, GIF, 102400),
                        new SupportedSourceAndTarget(DOC, JPEG, -1),
                        new SupportedSourceAndTarget(MSG, GIF, -1)));

       InlineTransformer transformer2 = new InlineTransformer("transformer2",
                Set.of(
                        new TransformOptionValue(false, "opt1"),
                        new TransformOptionValue(false, "opt2")),
                Set.of(
                        new SupportedSourceAndTarget(PDF, GIF, -1),
                        new SupportedSourceAndTarget(PPT, JPEG, -1)));

       InlineTransformer transformer3 = new InlineTransformer("transformer3",
                Set.of(
                        new TransformOptionValue(false, "opt1")),
                Set.of(
                        new SupportedSourceAndTarget(DOC, GIF, -1)));

        Map<String, String> actualOptions = null;

        assertSupported(DOC, 1024, GIF, actualOptions, null, transformer1);
        assertSupported(DOC, 1024, GIF, actualOptions, null, transformer1, transformer2);
        assertSupported(DOC, 1024, GIF, actualOptions, null, transformer1, transformer2, transformer3);

        assertSupported(DOC, 102401, GIF, null, "source is too large", transformer1);
        assertSupported(DOC, 102401, GIF, null, null, transformer1, transformer3);

        assertSupported(PDF, 1024, GIF, actualOptions, "Only transformer2 supports these mimetypes", transformer1);
        assertSupported(PDF, 1024, GIF, actualOptions, null, transformer1, transformer2);
        assertSupported(PDF, 1024, GIF, actualOptions, null, transformer1, transformer2, transformer3);

        actualOptions = buildActualOptions("opt1");
        assertSupported(PDF, 1024, GIF, actualOptions, "Only transformer2/4 supports these options", transformer1);
        assertSupported(PDF, 1024, GIF, actualOptions, null, transformer1, transformer2);
        assertSupported(PDF, 1024, GIF, actualOptions, null, transformer1, transformer2, transformer3);
        assertSupported(PDF, 1024, GIF, actualOptions, "transformer4 supports opt1 but not the source mimetype ", transformer1, transformer3);
    }

    @Test
    public void testPipeline() throws Exception
    {
        InlineTransformer transformer1 = new InlineTransformer("transformer1",
                null, // there are no options
                Set.of(
                        new SupportedSourceAndTarget(DOC, PDF, -1),
                        new SupportedSourceAndTarget(MSG, PDF, -1)));

        InlineTransformer transformer2 = new InlineTransformer("transformer2",
                Set.of(
                        new TransformOptionValue(false, "page"),
                        new TransformOptionValue(false, "width"),
                        new TransformOptionValue(false, "height")),
                Set.of(
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
        transformer1.setTransformOptions(Set.of(
                new TransformOptionValue(false, "startPage"),
                new TransformOptionValue(false, "endPage")));
        buildPipelineTransformer(transformer1, transformer2);

        assertSupported(DOC, 1024, GIF, buildActualOptions("page, width"), null);
        assertSupported(DOC, 1024, GIF, buildActualOptions("page, width, startPage"), null);
    }

    private void buildPipelineTransformer(InlineTransformer transformer1, InlineTransformer transformer2)
    {
        transformer = builder.buildPipeLine("transformer1",
                Set.of(
                        new SupportedSourceAndTarget(DOC, GIF, -1),
                        new SupportedSourceAndTarget(DOC, JPEG, -1),
                        new SupportedSourceAndTarget(MSG, GIF, -1)),
                Arrays.asList(
                        new ChildTransformer(false, transformer1),
                        new ChildTransformer(true, transformer2)));
    }
}