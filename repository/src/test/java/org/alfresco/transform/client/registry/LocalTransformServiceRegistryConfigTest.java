/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.transform.client.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractLocalTransform;
import org.alfresco.repo.content.transform.LocalPipelineTransform;
import org.alfresco.repo.content.transform.LocalTransformImpl;
import org.alfresco.repo.content.transform.LocalTransformServiceRegistry;
import org.alfresco.repo.content.transform.TransformerDebug;
import org.alfresco.transform.client.model.config.SupportedSourceAndTarget;
import org.alfresco.transform.client.model.config.TransformConfig;
import org.alfresco.transform.client.model.config.TransformOption;
import org.alfresco.transform.client.model.config.TransformOptionGroup;
import org.alfresco.transform.client.model.config.TransformOptionValue;
import org.alfresco.transform.client.model.config.Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.CronExpression;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Testing LocalTransformServiceRegistry.
 */
public class LocalTransformServiceRegistryConfigTest extends TransformRegistryTest
{
    public static final String HARD_CODED_VALUE = "hard coded value";

    private class TestLocalTransformServiceRegistry extends LocalTransformServiceRegistry
    {
        private boolean mockSuccessReadingConfig = true;
        LocalData dummyData = new LocalData();
        private List<String> errorsLogged = new ArrayList<>();
        private boolean resetBaseUrl = true;
        private int tEngineCount = 0;

        public synchronized boolean getMockSuccessReadingConfig()
        {
            return mockSuccessReadingConfig;
        }

        public synchronized void setMockSuccessReadingConfig(boolean mockSuccessReadingConfig)
        {
            System.out.println("\n"+getMs()+": set next mock read to "+(mockSuccessReadingConfig ? "success" : "failure"));
            this.mockSuccessReadingConfig = mockSuccessReadingConfig;
        }

        public void setResetBaseUrl(boolean resetBaseUrl)
        {
            this.resetBaseUrl = resetBaseUrl;
        }

        @Override
        // As we are mocking, baseUrl is always null, so this method sets it so we don't get errors.
        // It looks for the alfresco global property or system property with a localTransform. prefix,
        // just like the normal code.
        // If setResetBaseUrl(false) is called, the baseUrl remains null
        // If we are using transforms called "t-engine" only the first one has its baseUrl set - does not use properties.
        public String getBaseUrlIfTesting(String name, String baseUrl)
        {
            boolean isTEngine = "t-engine".equals(name);
            tEngineCount += isTEngine ? 1 : 0;
            return baseUrl == null && resetBaseUrl && !isTEngine
                    ? getProperty(LOCAL_TRANSFORM +name+URL, null)
                    : isTEngine && tEngineCount == 1
                    ? HARD_CODED_VALUE
                    : baseUrl;
        }

        @Override
        public boolean readConfig() throws IOException
        {
            readConfigCount++;
            dummyData = new LocalData();
            boolean mockSuccessReadingRemoteConfig = getMockSuccessReadingConfig();
            System.out.println(getMs() + "readConfig() success="+mockSuccessReadingRemoteConfig+" reads="+readConfigCount);
            return mockSuccessReadingRemoteConfig;
        }

        @Override
        public LocalData getData()
         {
             return dummyData;
         }

        @Override
        protected void logError(String msg)
        {
            errorsLogged.add(msg);
            super.logError(msg);
        }

        @Override
        protected void logWarn(String msg)
        {
            logError(msg);
        }

        public Data assertDataChanged(Data prevData, String msg)
        {
            // If the data changes, there has been a read
            System.out.println(getMs()+msg);
            Data data = getData();
            assertNotEquals("The configuration data should have changed: "+msg, data, prevData);
            return data;
        }

        public Data assertDataUnchanged(Data data, String msg)
        {
            // If the data changes, there has been a read
            System.out.println(getMs()+msg);
            assertEquals("The configuration data should be the same: "+msg, getData(), data);
            return getData();
        }

        public void assertErrorLogged(String pattern)
        {
            Pattern p = Pattern.compile(pattern);
            for (String msg : errorsLogged)
            {
                Matcher matcher = p.matcher(msg);
                if (matcher.matches())
                {
                    return;
                }
            }
            fail("Did not find error message that matches "+pattern);
        }
    }

    private static Log log = LogFactory.getLog(LocalTransformServiceRegistry.class);

    public static final String PNG = "image/png";
    public static final String TIFF = "image/tiff";

    public static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();

    private static final String LOCAL_TRANSFORM_SERVICE_CONFIG = "alfresco/local-transform-service-config-test.json";
    private static final String LOCAL_TRANSFORM_SERVICE_CONFIG_PIPELINE = "alfresco/local-transform-service-config-pipeline-test.json";

    private static final String LOCAL_TRANSFORM = "localTransform.";
    private static final String URL = ".url";

    private Map<String, Set<TransformOption>> mapOfTransformOptions;
    private List<Transformer> transformerList;

    protected TestLocalTransformServiceRegistry registry;

    private Properties properties = new Properties();

    @Mock private TransformerDebug transformerDebug;
    @Mock private MimetypeMap mimetypeMap;

    private Map<String, List<String>> imagemagickSupportedTransformation;
    private Map<String, List<String>> tikaSupportedTransformation;
    private Map<String, List<String>> pdfRendererSupportedTransformation;
    private Map<String, List<String>> libreofficeSupportedTransformation;
    private Map<String, List<String>> officeToImageViaPdfSupportedTransformation;

    private int readConfigCount;
    private long startMs = System.currentTimeMillis();

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        initTestData();

        super.setUp();
        LogManager.getLogger(LocalTransformServiceRegistryConfigTest.class).setLevel(Level.DEBUG);
    }

    @Override
    protected LocalTransformServiceRegistry buildTransformServiceRegistryImpl() throws Exception
    {
        registry = new TestLocalTransformServiceRegistry();
        registry.setJsonObjectMapper(JSON_OBJECT_MAPPER);
        registry.setProperties(properties);
        registry.setTransformerDebug(transformerDebug);
        registry.setMimetypeService(mimetypeMap);
        registry.setPipelineConfigDir("");
        registry.setCronExpression(null); // just read it once
        registry.afterPropertiesSet();
        return registry;
    }

    private String getMs()
    {
        return (System.currentTimeMillis() - startMs) + "ms: ";
    }

    protected int getExpectedTransformsForTestJsonPipeline()
    {
// imagemagick
//        {"sourceMediaType": "image/jpeg", "targetMediaType": "image/jpeg"},
//        {"sourceMediaType": "image/jpeg", "targetMediaType": "image/png"},
//        {"sourceMediaType": "image/jpeg", "targetMediaType": "image/bmp"}
// pdfrendere
//        {"sourceMediaType": "application/pdf", "targetMediaType": "image/png" }
// libreoffice
//         {"sourceMediaType": "application/vnd.ms-outlook", "targetMediaType": "application/pdf"}
// officeToImageViaPdf
//        {"sourceMediaType": "application/msword",  "targetMediaType": "image/gif" },
//        {"sourceMediaType": "application/msword",  "targetMediaType": "image/jpeg"},
//        {"sourceMediaType": "application/msword",  "targetMediaType": "image/png" },
//        {"sourceMediaType": "application/msword",  "targetMediaType": "image/tiff"}
        return 3 + 1 + 1 + 4;   // 9
    }

    /**
     * Loads localTransforms from the LOCAL_TRANSFORM_SERVICE_CONFIG config file.
     * @param path
     */
    private void retrieveLocalTransformList(String path)
    {
        CombinedConfig combinedConfig = new CombinedConfig(log, registry);
        combinedConfig.addLocalConfig(path);
        combinedConfig.register(registry);

        TransformConfig transformConfig = combinedConfig.buildTransformConfig();
        mapOfTransformOptions = transformConfig.getTransformOptions();
        transformerList = transformConfig.getTransformers();
    }

    /**
     * Initialize source and target test data for each transformer
     */
    private void initTestData()
    {
        // Add JVM properties
        System.setProperty(LOCAL_TRANSFORM + "pdfrenderer" + URL, "http://localhost:8090/");
        System.setProperty(LOCAL_TRANSFORM + "imagemagick" + URL, "http://localhost:8091/");
        System.setProperty(LOCAL_TRANSFORM + "libreoffice" + URL, "http://localhost:8092/");
        System.setProperty(LOCAL_TRANSFORM + "tika" + URL, "http://localhost:8093/");

        // Add alfresco-global properties
        properties.setProperty(LOCAL_TRANSFORM + "pdfrenderer" + URL, "http://localhost:8090/");
        properties.setProperty(LOCAL_TRANSFORM + "imagemagick" + URL, "http://localhost:8091/");
        properties.setProperty(LOCAL_TRANSFORM + "libreoffice" + URL, "http://localhost:8092/");
        properties.setProperty(LOCAL_TRANSFORM + "tika" + URL, "http://localhost:8093/");

        // ImageMagick supported Source and Target List:
        imagemagickSupportedTransformation = new HashMap<>();
        List<String> targetMimetype = new ArrayList<>();
        targetMimetype.add("image/gif");
        targetMimetype.add("image/tiff");
        imagemagickSupportedTransformation.put("image/tiff", targetMimetype);
        targetMimetype = new ArrayList<>(targetMimetype);
        targetMimetype.add("image/png");
        targetMimetype.add("image/jpeg");
        imagemagickSupportedTransformation.put("image/gif", targetMimetype);
        imagemagickSupportedTransformation.put("image/jpeg", targetMimetype);
        targetMimetype = new ArrayList<>(targetMimetype);
        targetMimetype.add("alfresco-metadata-extract"); // Metadata extract and embed types should be excluded from pipeline cartesian products
        imagemagickSupportedTransformation.put("image/png", targetMimetype);
        targetMimetype = new ArrayList<>();
        targetMimetype.add("target1");
        targetMimetype.add("target2");
        targetMimetype.add("target3");
        imagemagickSupportedTransformation.put("source", targetMimetype);

        // Tika Supported Source and Target List:
        targetMimetype = new ArrayList<>();
        tikaSupportedTransformation = new HashMap<>();
        targetMimetype.add("text/plain");
        tikaSupportedTransformation.put("application/pdf", targetMimetype);
        tikaSupportedTransformation.put("application/msword", targetMimetype);
        tikaSupportedTransformation.put("application/vnd.ms-excel", targetMimetype);
        tikaSupportedTransformation.put("application/vnd.ms-powerpoint", targetMimetype);
        tikaSupportedTransformation.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", targetMimetype);
        tikaSupportedTransformation.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", targetMimetype);
        tikaSupportedTransformation.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", targetMimetype);
        tikaSupportedTransformation.put("application/vnd.ms-outlook", targetMimetype);
        targetMimetype = new ArrayList<>();
        targetMimetype.add("target1");
        targetMimetype.add("target2");
        targetMimetype.add("target3");
        tikaSupportedTransformation.put("source", targetMimetype);

        // Libre Office Source and Target List:
        targetMimetype = new ArrayList<>();
        libreofficeSupportedTransformation = new HashMap<>();
        targetMimetype.add("application/pdf");
        libreofficeSupportedTransformation.put("application/vnd.ms-excel", targetMimetype);
        libreofficeSupportedTransformation.put("application/vnd.ms-powerpoint", targetMimetype);
        libreofficeSupportedTransformation.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", targetMimetype);
        libreofficeSupportedTransformation.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", targetMimetype);
        libreofficeSupportedTransformation.put("application/vnd.ms-outlook", targetMimetype);
        targetMimetype.add("application/msword");
        libreofficeSupportedTransformation.put("application/msword", targetMimetype);
        libreofficeSupportedTransformation.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", targetMimetype);

        // Pdf Renderer Source and Target List:
        targetMimetype = new ArrayList<>();
        pdfRendererSupportedTransformation = new HashMap<>();
        targetMimetype.add("image/png");
        pdfRendererSupportedTransformation.put("application/pdf", targetMimetype);
        targetMimetype = new ArrayList<>();
        targetMimetype.add("target1");
        targetMimetype.add("target2");
        targetMimetype.add("target3");
        pdfRendererSupportedTransformation.put("source", targetMimetype);

        // Office to Image via Pdf Pipeline Transformer Source and Target List:
        targetMimetype = new ArrayList<>();
        officeToImageViaPdfSupportedTransformation = new HashMap<>();
        targetMimetype.add("image/gif");
        targetMimetype.add("image/tiff");
        targetMimetype.add("image/png");
        targetMimetype.add("image/jpeg");
        officeToImageViaPdfSupportedTransformation.put("application/msword", targetMimetype);
        officeToImageViaPdfSupportedTransformation.put("application/vnd.ms-excel", targetMimetype);
        officeToImageViaPdfSupportedTransformation.put("application/vnd.ms-powerpoint", targetMimetype);
        officeToImageViaPdfSupportedTransformation.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", targetMimetype);
        officeToImageViaPdfSupportedTransformation.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", targetMimetype);
        officeToImageViaPdfSupportedTransformation.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", targetMimetype);
        officeToImageViaPdfSupportedTransformation.put("application/vnd.ms-outlook", targetMimetype);
    }

    @Override
    protected String getBaseUrl(Transformer transformer)
    {
        return LOCAL_TRANSFORM+transformer.getTransformerName()+".url";
    }

    private int countTopLevelOptions(Set<String> transformOptionNames)
    {
        int i = 0;
        for (String name: transformOptionNames)
        {
            Set<TransformOption> transformOptions = mapOfTransformOptions.get(name);
            i += transformOptions.size();
        }
        return i;
    }

    private void register(String path) throws IOException
    {
        CombinedConfig combinedConfig = new CombinedConfig(log, registry);
        combinedConfig.addLocalConfig(path);
        combinedConfig.register((TransformServiceRegistryImpl)registry);
    }

    @Test
    public void testJsonConfig() throws IOException
    {
        register(LOCAL_TRANSFORM_SERVICE_CONFIG);

        // Check the count of transforms supported
        assertEquals("The number of UNIQUE source to target mimetypes transforms has changed. Config change?",
                64, countSupportedTransforms(true));
        assertEquals("The number of source to target mimetypes transforms has changed. " +
                        "There may be multiple transformers for the same combination. Config change?",
                70, countSupportedTransforms(false));

        // Check a supported transform for each transformer.
        assertSupported(DOC, 1234, PDF, emptyMap(), null, ""); // libreoffice
        assertSupported(DOC, 1234, PDF, emptyMap(), null, ""); // libreoffice
        assertSupported(PDF, 1234, PNG, emptyMap(), null, ""); // pdfrenderer
        assertSupported(JPEG,1234, GIF, emptyMap(), null, ""); // imagemagick
        assertSupported(MSG, 1234, TXT, emptyMap(), null, ""); // tika
        assertSupported(MSG, 1234, GIF, emptyMap(), null, ""); // officeToImageViaPdf

        Map<String, String> invalidPdfOptions = new HashMap<>();
        invalidPdfOptions.put("allowEnlargement", "false");
        assertSupported(DOC, 1234, PDF, invalidPdfOptions, null, "Invalid as there is a extra option");
    }

    @Test
    public void testJsonPipeline() throws IOException
    {
        register(LOCAL_TRANSFORM_SERVICE_CONFIG_PIPELINE);

        // Check the count of transforms supported
        int expectedTransforms = getExpectedTransformsForTestJsonPipeline();
        assertEquals("The number of UNIQUE source to target mimetypes transforms has changed. Config change?",
                expectedTransforms, countSupportedTransforms(true));
        assertEquals("The number of source to target mimetypes transforms has changed. " +
                        "There may be multiple transformers for the same combination. Config change?",
                expectedTransforms, countSupportedTransforms(false));

        // Check required and optional default correctly
        Map<String, List<SupportedTransform>> transformsToWord =
                registry.getData().getTransforms().get(DOC);
        List<SupportedTransform> supportedTransforms = transformsToWord.get(GIF);
        SupportedTransform supportedTransform = supportedTransforms.get(0);

        Set<TransformOption> transformOptionsSet = supportedTransform.getTransformOptions().getTransformOptions();

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

        assertTrue("The holding group should be required", supportedTransform.getTransformOptions().isRequired());
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
        assertSupported(DOC,1234, GIF,  emptyMap(), null, "");
        assertSupported(DOC,1234, PNG,  emptyMap(), null, "");
        assertSupported(DOC,1234, JPEG, emptyMap(), null, "");
        assertSupported(DOC,1234, TIFF, emptyMap(), null, "");

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
        return retrieveTransformOptionByPropertyName(transformOptionGroup, propertyName, "TransformOptionValue") != null;
    }

    private int countSupportedTransforms(boolean unique)
    {
        int count = 0;
        int uniqueCount = 0;
        for (Map<String, List<SupportedTransform>> targetMap : registry.getData().getTransforms().values())
        {
            for (List<SupportedTransform> supportedTransforms : targetMap.values())
            {
                uniqueCount++;
                count += supportedTransforms.size();
            }
        }
        return unique ? uniqueCount : count;
    }

    @Test
    public void testReadJsonConfig()
    {
        retrieveLocalTransformList(LOCAL_TRANSFORM_SERVICE_CONFIG);

        // Assert expected size of the transformers.
        assertNotNull("Transformer list is null.", transformerList);
        assertEquals("Unexpected number of transformers retrieved", 5, transformerList.size());

        // Assert proper transformers are loaded
        List<String> listOfExpectedTransformersName= new ArrayList<>();
        listOfExpectedTransformersName.add("imagemagick");
        listOfExpectedTransformersName.add("tika");
        listOfExpectedTransformersName.add("pdfrenderer");
        listOfExpectedTransformersName.add("libreoffice");
        listOfExpectedTransformersName.add("officeToImageViaPdf");

        for (Transformer transformer : transformerList)
        {
            assertTrue(transformer.getTransformerName() + " should be an expected local transformer.", listOfExpectedTransformersName.contains(transformer.getTransformerName()));
            listOfExpectedTransformersName.remove(transformer.getTransformerName());

            switch (transformer.getTransformerName())
            {
                case "imagemagick":
                    assertEquals(transformer.getTransformerName() + " incorrect number of supported transform", 18, transformer.getSupportedSourceAndTargetList().size());
                    assertEquals( transformer.getTransformerName() + "incorrect number of transform option names", 1, transformer.getTransformOptions().size());
                    assertEquals( transformer.getTransformerName() + "incorrect number of transform options", 6, countTopLevelOptions(transformer.getTransformOptions()));
                    assertEquals(transformer.getTransformerName() + " expected to not be a transformer pipeline", transformer.getTransformerPipeline().size(), 0);
                    assertEquals(transformer.getTransformerName() + " expected to not be a failover pipeline", transformer.getTransformerFailover().size(), 0);

                    //Test supportedSourceAndTargetList
                    for ( SupportedSourceAndTarget ssat: transformer.getSupportedSourceAndTargetList())
                    {
                        assertTrue(ssat.getSourceMediaType() + " not expected to be a supported transform source.", imagemagickSupportedTransformation.containsKey(ssat.getSourceMediaType()));
                        assertTrue(ssat.getTargetMediaType() + " not expected to be a supported transform target for " + ssat.getSourceMediaType(), imagemagickSupportedTransformation.get(ssat.getSourceMediaType()).contains(ssat.getTargetMediaType()));
                    }
                    break;

                case "tika":
                    assertEquals(transformer.getTransformerName() + " incorrect number of supported transform", 11, transformer.getSupportedSourceAndTargetList().size());
                    assertEquals( transformer.getTransformerName() + "incorrect number of transform option names", 1, transformer.getTransformOptions().size());
                    assertEquals( transformer.getTransformerName() + "incorrect number of transform options", 5, countTopLevelOptions(transformer.getTransformOptions()));
                    assertEquals(transformer.getTransformerName() + " expected to not be a transformer pipeline", transformer.getTransformerPipeline().size(), 0);
                    assertEquals(transformer.getTransformerName() + " expected to not be a failover pipeline", transformer.getTransformerFailover().size(), 0);

                    //Test supportedSourceAndTargetList
                    for ( SupportedSourceAndTarget ssat: transformer.getSupportedSourceAndTargetList())
                    {
                        assertTrue(ssat.getSourceMediaType() + " not expected to be a supported transform source.", tikaSupportedTransformation.containsKey(ssat.getSourceMediaType()));
                        assertTrue(ssat.getTargetMediaType() + " not expected to be a supported transform target for " + ssat.getSourceMediaType(), tikaSupportedTransformation.get(ssat.getSourceMediaType()).contains(ssat.getTargetMediaType()));
                    }
                    break;

                case "pdfrenderer":
                    assertEquals(transformer.getTransformerName() + " incorrect number of supported transform", 4, transformer.getSupportedSourceAndTargetList().size());
                    assertEquals( transformer.getTransformerName() + "incorrect number of transform option names", 1, transformer.getTransformOptions().size());
                    assertEquals( transformer.getTransformerName() + "incorrect number of transform options", 5, countTopLevelOptions(transformer.getTransformOptions()));
                    assertEquals(transformer.getTransformerName() + " expected to not be a transformer pipeline", transformer.getTransformerPipeline().size(), 0);
                    assertEquals(transformer.getTransformerName() + " expected to not be a failover pipeline", transformer.getTransformerFailover().size(), 0);

                    //Test supportedSourceAndTargetList
                    for ( SupportedSourceAndTarget ssat: transformer.getSupportedSourceAndTargetList())
                    {
                        assertTrue(ssat.getSourceMediaType() + " not expected to be a supported transform source.", pdfRendererSupportedTransformation.containsKey(ssat.getSourceMediaType()));
                        assertTrue(ssat.getTargetMediaType() + " not expected to be a supported transform target for " + ssat.getSourceMediaType(), pdfRendererSupportedTransformation.get(ssat.getSourceMediaType()).contains(ssat.getTargetMediaType()));
                    }
                    break;

                case "libreoffice":
                    assertEquals(transformer.getTransformerName() + " incorrect number of supported transform", 9, transformer.getSupportedSourceAndTargetList().size());
                    assertEquals( transformer.getTransformerName() + "incorrect number of transform option names", 0, transformer.getTransformOptions().size());
                    assertEquals( transformer.getTransformerName() + "incorrect number of transform options", 0, countTopLevelOptions(transformer.getTransformOptions()));
                    assertEquals(transformer.getTransformerName() + " expected to not be a transformer pipeline", transformer.getTransformerPipeline().size(), 0);
                    assertEquals(transformer.getTransformerName() + " expected to not be a failover pipeline", transformer.getTransformerFailover().size(), 0);

                    //Test supportedSourceAndTargetList
                    for ( SupportedSourceAndTarget ssat: transformer.getSupportedSourceAndTargetList())
                    {
                        assertTrue(ssat.getSourceMediaType() + " not expected to be a supported transform source.", libreofficeSupportedTransformation.containsKey(ssat.getSourceMediaType()));
                        assertTrue(ssat.getTargetMediaType() + " not expected to be a supported transform target for " + ssat.getSourceMediaType(), libreofficeSupportedTransformation.get(ssat.getSourceMediaType()).contains(ssat.getTargetMediaType()));
                    }
                    break;

                case "officeToImageViaPdf":
                    // Note we will get 35 entries in getSupportedSourceAndTargetList() if the metadata transforms are not excluded
                    assertEquals(transformer.getTransformerName() + " incorrect number of supported transform", 28, transformer.getSupportedSourceAndTargetList().size());
                    assertEquals( transformer.getTransformerName() + "incorrect number of transform option names", 2, transformer.getTransformOptions().size());
                    assertEquals( transformer.getTransformerName() + "incorrect number of transform options", 11, countTopLevelOptions(transformer.getTransformOptions()));
                    assertEquals(transformer.getTransformerName() + " expected to be a transformer pipeline", transformer.getTransformerPipeline().size(), 3);

                    //Test supportedSourceAndTargetList
                    for ( SupportedSourceAndTarget ssat: transformer.getSupportedSourceAndTargetList())
                    {
                        assertTrue(ssat.getSourceMediaType() + " not expected to be a supported transform source.", officeToImageViaPdfSupportedTransformation.containsKey(ssat.getSourceMediaType()));
                        assertTrue(ssat.getTargetMediaType() + " not expected to be a supported transform target for " + ssat.getSourceMediaType(), officeToImageViaPdfSupportedTransformation.get(ssat.getSourceMediaType()).contains(ssat.getTargetMediaType()));
                    }
                    break;
            }
        }
        assertEquals("Transformer expected but not found in config file", 0, listOfExpectedTransformersName.size());
    }

    @Test
    public void testReadTransformProperties()
    {
        retrieveLocalTransformList(LOCAL_TRANSFORM_SERVICE_CONFIG);

        assertNotNull("Transformer list is null.", transformerList);
        for (Transformer transformer : transformerList)
        {
            if (transformer.getTransformerPipeline() == null)
            {
                assertNotNull(transformer.getTransformerName()+ " JVM property not set.", System.getProperty(LOCAL_TRANSFORM + transformer.getTransformerName() + URL));
            }
        }
        assertEquals("Unexpected pdfrenderer JVM property value", "http://localhost:8090/", System.getProperty(LOCAL_TRANSFORM + "pdfrenderer" + URL));
        assertEquals("Unexpected imagemagick JVM property value", "http://localhost:8091/", System.getProperty(LOCAL_TRANSFORM + "imagemagick" + URL));
        assertEquals("Unexpected libreoffice JVM property value", "http://localhost:8092/", System.getProperty(LOCAL_TRANSFORM + "libreoffice" + URL));
        assertEquals("Unexpected tika JVM property value", "http://localhost:8093/", System.getProperty(LOCAL_TRANSFORM + "tika" + URL));

        for (Transformer transformer : transformerList)
        {
            if(transformer.getTransformerPipeline() == null)
            {
                assertNotNull(transformer.getTransformerName()+ " alfresco-global property not set.", properties.getProperty(LOCAL_TRANSFORM + transformer.getTransformerName() + URL));
            }
        }
        assertEquals("Unexpected pdfrenderer alfresco-global property value", "http://localhost:8090/", properties.getProperty(LOCAL_TRANSFORM + "pdfrenderer" + URL));
        assertEquals("Unexpected imagemagick alfresco-global property value", "http://localhost:8091/", properties.getProperty(LOCAL_TRANSFORM + "imagemagick" + URL));
        assertEquals("Unexpected libreoffice alfresco-global property value", "http://localhost:8092/", properties.getProperty(LOCAL_TRANSFORM + "libreoffice" + URL));
        assertEquals("Unexpected tika alfresco-global property value", "http://localhost:8093/", properties.getProperty(LOCAL_TRANSFORM + "tika" + URL));
    }

    @Test
    // Simulates the reading of config which changes over time and sometimes T-Engines fail to reply.
    public void testAdditionAndRemovalOfTEngines() throws Exception
    {
        CronExpression origCronExpression = registry.getCronExpression();
        CronExpression origInitialAndOnErrorCronExpression = registry.getInitialAndOnErrorCronExpression();
        String origPipelineConfigDir = registry.getPipelineConfigDir();

        try
        {
            readConfigCount = 0;

            registry.setInitialAndOnErrorCronExpression(new CronExpression(("0/2 * * ? * * *"))); // every 2 seconds rather than 10 seconds
            registry.setCronExpression(new CronExpression(("0/4 * * ? * * *"))); // every 4 seconds rather than every hour

            // Sleep until a 6 second boundary, in order to make testing clearer.
            // It avoids having to work out schedule offsets and extra quick runs that can otherwise take place.
            Thread.sleep(4000-System.currentTimeMillis()%4000);
            startMs = System.currentTimeMillis();
            registry.setMockSuccessReadingConfig(false);
            registry.afterPropertiesSet();
            TransformServiceRegistryImpl.Data data = registry.getData();

            Thread.sleep(1000); // 1 seconds
            data = registry.assertDataChanged(data, "There should have been a read after a few milliseconds that fails");

            Thread.sleep(2000); // 3 seconds
            data = registry.assertDataChanged(data, "There should have been a read after 2 seconds that fails");

            Thread.sleep(2000); // 5 seconds
            data = registry.assertDataChanged(data, "There should have been a read after 4 seconds that fails");

            Thread.sleep(2000); // 7 seconds
            data = registry.assertDataChanged(data, "There should have been a read after 6 seconds that fails");

            // Should switch to normal 4s schedule after the next read, so the read at 12 seconds will be on that schedule.
            // It is always possible that another quick one gets scheduled almost straight away after the next read.
            registry.setMockSuccessReadingConfig(true);
            Thread.sleep(2000); // 9 seconds
            data = registry.assertDataChanged(data, "There should have been a read after 8 seconds that succeeds");

            Thread.sleep(2000); // 11 seconds
            data = registry.assertDataUnchanged(data, "There really should not have been a read until 12 seconds");

            Thread.sleep(2000); // 13 seconds
            data = registry.assertDataChanged(data, "There should have been a read after 12 seconds that succeeds");

            // Should switch back to initial/error schedule after failure
            registry.setMockSuccessReadingConfig(false);
            Thread.sleep(4000); // 17 seconds
            data = registry.assertDataChanged(data, "There should have been a read after 16 seconds that fails");

            Thread.sleep(2000); // 19 seconds
            data = registry.assertDataChanged(data, "There should have been a read after 18 seconds");
        }
        finally
        {
            registry.setMockSuccessReadingConfig(true);
            registry.setCronExpression(origCronExpression);
            registry.setInitialAndOnErrorCronExpression(origInitialAndOnErrorCronExpression);
            registry.setPipelineConfigDir(origPipelineConfigDir);
        }
    }

    @Test
    // Checks that only options expected by individual transforms in the pipeline are actually passed to the transform.
    public void testStripExtraOptions()
    {
        retrieveLocalTransformList(LOCAL_TRANSFORM_SERVICE_CONFIG);

        Map<String, String> actualOptions = Map.of(
                "autoOrient", "true",
                "width", "100",
                "height", "50");
        LocalPipelineTransform officeToImageViaPdf =
                (LocalPipelineTransform)((LocalTransformServiceRegistry)registry).getLocalTransform(
                        "application/msword", -1, "image/gif", actualOptions, null);
        assertEquals("Original number of options officeToImageViaPdf", 21, officeToImageViaPdf.getTransformsTransformOptionNames().size());

        AbstractLocalTransform libreoffice = (AbstractLocalTransform) officeToImageViaPdf.getIntermediateTransformer(0);
        assertEquals("libreoffice options", 0, libreoffice.getTransformsTransformOptionNames().size());
        assertEquals("libreoffice actual options", 0, libreoffice.getStrippedTransformOptions(actualOptions).size());

        AbstractLocalTransform pdfrenderer = (AbstractLocalTransform) officeToImageViaPdf.getIntermediateTransformer(1);
        assertEquals("pdfrenderer options", 5, pdfrenderer.getTransformsTransformOptionNames().size());
        assertEquals("pdfrenderer actual options", 2, pdfrenderer.getStrippedTransformOptions(actualOptions).size()); // width, height

        AbstractLocalTransform imagemagick = (AbstractLocalTransform) officeToImageViaPdf.getIntermediateTransformer(2);
        assertEquals("imagemagick options", 16, imagemagick.getTransformsTransformOptionNames().size());
        assertEquals("imagemagick actual options", 1, imagemagick.getStrippedTransformOptions(actualOptions).size()); // autoOrient
    }

    @Test
    // Checks that the correct transformer is selected based on priority
    public void testPriority()
    {
        retrieveLocalTransformList(LOCAL_TRANSFORM_SERVICE_CONFIG);

        assertEquals("pdfrenderer",
                ((AbstractLocalTransform)registry.getLocalTransform("source", -1,
                        "target1", Collections.emptyMap(), null)).getName());

        assertEquals("imagemagick",
                ((AbstractLocalTransform)registry.getLocalTransform("source", -1,
                        "target2", Collections.emptyMap(), null)).getName());

        assertEquals("tika",
                ((AbstractLocalTransform)registry.getLocalTransform("source", -1,
                        "target3", Collections.emptyMap(), null)).getName());
    }

    @Test
    public void testNoName()
    {
        retrieveLocalTransformList("alfresco/local-transform-service-config-no-name-test.json");
        registry.assertErrorLogged("Transformer names may not be null.*no-name-test.*");
    }

    @Test
    public void testPipelineAndFailover()
    {
        retrieveLocalTransformList("alfresco/local-transform-service-config-pipeline-and-failover-test.json");
        registry.assertErrorLogged("Transformer .* cannot have pipeline and failover sections.*pipeline-and-failover.*");
    }

    @Test
    public void testTEngineDuplicateNames()
    {
        retrieveLocalTransformList("alfresco/local-transform-service-config-dup-name-test.json");
        registry.assertErrorLogged("Transformer \"pdfrenderer\" must be a unique name.*dup-name.*");
    }

    @Test
    public void testTEngineNoBaseUrls()
    {
        registry.setResetBaseUrl(false);
        retrieveLocalTransformList("alfresco/local-transform-service-config-no-base-url-test.json");
        registry.assertErrorLogged("Single step transformers \\(such as \"pdfrenderer\"\\) must be defined in a " +
                "T-Engine rather than in a pipeline file, unless they are overriding an existing single step definition.*no-base-url.*");
    }

    @Test
    public void testPipelineMissingStepTransform()
    {
        retrieveLocalTransformList("alfresco/transform-service-config-pipeline-missing-step-test.json");
        registry.assertErrorLogged("Transformer \"missingPdfrenderer\" ignored as step transforms \\(\"pdfrenderer\"\\) do not exist.*pipeline-missing-step-test.*");
    }

    @Test
    public void testFailoverMissingStepTransform()
    {
        retrieveLocalTransformList("alfresco/transform-service-config-failover-missing-step-test.json");
        registry.assertErrorLogged("Transformer \"missingPdfrenderer\" ignored as step transforms \\(\"pdfrenderer\"\\) do not exist.*failover-missing-step-test.*");
    }

    @Test
    public void testOverrideTEngine()
    {
        // This json file contains two transformers with the same name.
        // * The second one should override the first which has different supported source mimetypes, target mimetypes
        //   and max sizes. It also has different transform options.
        // * There is special code in getBaseUrlIfTesting mocking up the baseUrl just for the first one. It should be
        //   copied to the second one.
        retrieveLocalTransformList("alfresco/local-transform-service-config-override-test.json");

        assertNotNull("Should still be supported",
                ((AbstractLocalTransform)registry.getLocalTransform("text/csv", 1000,
                        "text/html", Collections.emptyMap(), null)));

        assertNotNull("Increased max size be supported",
                ((AbstractLocalTransform)registry.getLocalTransform("text/csv", 2000,
                        "text/html", Collections.emptyMap(), null)));

        assertNull("Increased max size is now 2000",
                ((AbstractLocalTransform)registry.getLocalTransform("text/csv", 3000,
                        "text/html", Collections.emptyMap(), null)));

        assertNotNull("Should have been added",
                ((AbstractLocalTransform)registry.getLocalTransform("text/csv", -1,
                        "application/pdf", Collections.emptyMap(), null)));

        assertNull("Should have been removed",
                ((AbstractLocalTransform)registry.getLocalTransform("text/csv", -1,
                        "text/tab-separated-values", Collections.emptyMap(), null)));

        assertNotNull("options1 should still exist, even if not used", mapOfTransformOptions.get("options1"));

        assertNotNull("options2 should exist", mapOfTransformOptions.get("options2"));

        Map<String, String> actualOptions = Map.of(
                "width", "100",
                "height", "50");
        assertNull("width from options1 is no longer used, so should find no transformer",
                ((AbstractLocalTransform)registry.getLocalTransform("text/csv", -1,
                        "application/pdf", actualOptions, null)));

        actualOptions = Map.of(
                "page", "100",
                "height", "50");
        assertNotNull("Both options are in options2, so should we should find the transformer",
                ((AbstractLocalTransform)registry.getLocalTransform("text/csv", -1,
                        "application/pdf", actualOptions, null)));

        LocalTransformImpl localTransform = (LocalTransformImpl)
                registry.getLocalTransform("text/csv", -1,
                "application/pdf", Collections.emptyMap(), null);
        assertEquals("Should only have 2 options", 2,
                localTransform.getTransformsTransformOptionNames().size());
        assertTrue("The baseUrl should have been copied", localTransform.remoteTransformerClientConfigured());
    }

    @Test
    public void testOverrideTEngineWithPipeline()
    {
        // This json file contains two transformers with the same name, plus two others libreoffice and pdfrenderer
        // * The first transform which should be overridden has a baseUrl as it talks to a T-Engine and has 5
        //   supported source to target mimetypes.
        // * The second one should override the first with a pipeline of libreoffice and pdfrenderer but only has 1
        //   supported source to target mimetype. It should not have a baseUrl as it will not talk to a T-Engine.
        //   THIS IS WHAT IS BASICALLY DIFFERENT TO testOverrideTEngine, resulting in a LocalPipelineTransform rather
        //   rather than a LocalTransformImpl.
        // * There is special code in getBaseUrlIfTesting mocking up the baseUrl just for the first one. It should be
        //   copied to the second one.
        retrieveLocalTransformList("alfresco/local-transform-service-config-override-with-pipeline-test.json");

        LocalPipelineTransform pipelineTransform = (LocalPipelineTransform) registry.getLocalTransform("text/csv",
                -1,"image/png", Collections.emptyMap(), null);
        assertNotNull("Should supported csv to png", pipelineTransform);
    }
}
