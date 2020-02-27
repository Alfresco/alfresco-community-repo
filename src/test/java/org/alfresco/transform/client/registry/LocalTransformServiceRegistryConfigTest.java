/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2019 Alfresco Software Limited
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
import org.alfresco.repo.content.transform.LocalTransformServiceRegistry;
import org.alfresco.repo.content.transform.TransformerDebug;
import org.alfresco.transform.client.model.config.SupportedSourceAndTarget;
import org.alfresco.transform.client.model.config.TransformOption;
import org.alfresco.transform.client.model.config.Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.CronExpression;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Extends the {@link TransformServiceRegistryConfigTest} (used to test the config received from the Transform Service)
 * so that configuration for the local transformations may be tested. This includes pipelines and options specific
 * transform steps.
 */
public class LocalTransformServiceRegistryConfigTest extends TransformServiceRegistryConfigTest
{
    private class TestLocalTransformServiceRegistry extends LocalTransformServiceRegistry
    {
        private boolean mockSuccessReadingConfig = true;
        LocalData dummyData = new LocalData();

        public synchronized boolean getMockSuccessReadingConfig()
        {
            return mockSuccessReadingConfig;
        }

        public synchronized void setMockSuccessReadingConfig(boolean mockSuccessReadingConfig)
        {
            System.out.println("\n"+getMs()+": set next mock read to "+(mockSuccessReadingConfig ? "success" : "failure"));
            this.mockSuccessReadingConfig = mockSuccessReadingConfig;
        }

        @Override
        protected String getBaseUrlIfTesting(String name, String baseUrl)
        {
            return baseUrl == null
                    ? getProperty(LOCAL_TRANSFORM +name+URL, null)
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
    }

    private static Log log = LogFactory.getLog(LocalTransformServiceRegistry.class);

    private static final String LOCAL_TRANSFORM_SERVICE_CONFIG = "alfresco/local-transform-service-config-test.json";
    private static final String LOCAL_TRANSFORM_SERVICE_CONFIG_PIPELINE = "alfresco/local-transform-service-config-pipeline-test.json";

    private static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();
    private static final String LOCAL_TRANSFORM = "localTransform.";
    private static final String URL = ".url";

    private Map<String, Set<TransformOption>> mapOfTransformOptions;
    private List<CombinedConfig.TransformAndItsOrigin> transformerList;

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
    }

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

    @Override
    protected String getTransformServiceConfig()
    {
        return LOCAL_TRANSFORM_SERVICE_CONFIG;
    }

    @Override
    protected String getTransformServiceConfigPipeline()
    {
        return LOCAL_TRANSFORM_SERVICE_CONFIG_PIPELINE;
    }

    @Override
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
     */
    private void retrieveLocalTransformList()
    {
        CombinedConfig combinedConfig = new CombinedConfig(log);
        combinedConfig.addLocalConfig(LOCAL_TRANSFORM_SERVICE_CONFIG);
        combinedConfig.register(registry);
        mapOfTransformOptions = combinedConfig.combinedTransformOptions;
        transformerList = combinedConfig.combinedTransformers;
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
        targetMimetype.add("image/png");
        targetMimetype.add("image/jpeg");
        imagemagickSupportedTransformation.put("image/gif", targetMimetype);
        imagemagickSupportedTransformation.put("image/jpeg", targetMimetype);
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

    @Test
    @Override
    public void testJsonConfig() throws IOException
    {
        // Not 60, 60 as we have added source->target1..3 to three transformers
        internalTestJsonConfig(63, 69);
    }

    @Test
    public void testReadWriteJson() throws IOException
    {
        // Override super method so it passes, as there is nothing more to be gained for LocalTransforms.
    }

    @Test
    public void testReadJsonConfig()
    {
        retrieveLocalTransformList();

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

        for (CombinedConfig.TransformAndItsOrigin t : transformerList)
        {
            assertTrue(t.transformer.getTransformerName() + " should be an expected local transformer.", listOfExpectedTransformersName.contains(t.transformer.getTransformerName()));
            listOfExpectedTransformersName.remove(t.transformer.getTransformerName());

            switch (t.transformer.getTransformerName())
            {
                case "imagemagick":
                    assertEquals(t.transformer.getTransformerName() + " incorrect number of supported transform", 17, t.transformer.getSupportedSourceAndTargetList().size());
                    assertEquals( t.transformer.getTransformerName() + "incorrect number of transform option names", 1, t.transformer.getTransformOptions().size());
                    assertEquals( t.transformer.getTransformerName() + "incorrect number of transform options", 6, countTopLevelOptions(t.transformer.getTransformOptions()));
                    assertEquals(t.transformer.getTransformerName() + " expected to not be a transformer pipeline", t.transformer.getTransformerPipeline().size(), 0);
                    assertEquals(t.transformer.getTransformerName() + " expected to not be a failover pipeline", t.transformer.getTransformerFailover().size(), 0);

                    //Test supportedSourceAndTargetList
                    for ( SupportedSourceAndTarget ssat: t.transformer.getSupportedSourceAndTargetList())
                    {
                        assertTrue(ssat.getSourceMediaType() + " not expected to be a supported transform source.", imagemagickSupportedTransformation.containsKey(ssat.getSourceMediaType()));
                        assertTrue(ssat.getTargetMediaType() + " not expected to be a supported transform target for " + ssat.getSourceMediaType(), imagemagickSupportedTransformation.get(ssat.getSourceMediaType()).contains(ssat.getTargetMediaType()));
                    }
                    break;

                case "tika":
                    assertEquals(t.transformer.getTransformerName() + " incorrect number of supported transform", 11, t.transformer.getSupportedSourceAndTargetList().size());
                    assertEquals( t.transformer.getTransformerName() + "incorrect number of transform option names", 1, t.transformer.getTransformOptions().size());
                    assertEquals( t.transformer.getTransformerName() + "incorrect number of transform options", 5, countTopLevelOptions(t.transformer.getTransformOptions()));
                    assertEquals(t.transformer.getTransformerName() + " expected to not be a transformer pipeline", t.transformer.getTransformerPipeline().size(), 0);
                    assertEquals(t.transformer.getTransformerName() + " expected to not be a failover pipeline", t.transformer.getTransformerFailover().size(), 0);

                    //Test supportedSourceAndTargetList
                    for ( SupportedSourceAndTarget ssat: t.transformer.getSupportedSourceAndTargetList())
                    {
                        assertTrue(ssat.getSourceMediaType() + " not expected to be a supported transform source.", tikaSupportedTransformation.containsKey(ssat.getSourceMediaType()));
                        assertTrue(ssat.getTargetMediaType() + " not expected to be a supported transform target for " + ssat.getSourceMediaType(), tikaSupportedTransformation.get(ssat.getSourceMediaType()).contains(ssat.getTargetMediaType()));
                    }
                    break;

                case "pdfrenderer":
                    assertEquals(t.transformer.getTransformerName() + " incorrect number of supported transform", 4, t.transformer.getSupportedSourceAndTargetList().size());
                    assertEquals( t.transformer.getTransformerName() + "incorrect number of transform option names", 1, t.transformer.getTransformOptions().size());
                    assertEquals( t.transformer.getTransformerName() + "incorrect number of transform options", 5, countTopLevelOptions(t.transformer.getTransformOptions()));
                    assertEquals(t.transformer.getTransformerName() + " expected to not be a transformer pipeline", t.transformer.getTransformerPipeline().size(), 0);
                    assertEquals(t.transformer.getTransformerName() + " expected to not be a failover pipeline", t.transformer.getTransformerFailover().size(), 0);

                    //Test supportedSourceAndTargetList
                    for ( SupportedSourceAndTarget ssat: t.transformer.getSupportedSourceAndTargetList())
                    {
                        assertTrue(ssat.getSourceMediaType() + " not expected to be a supported transform source.", pdfRendererSupportedTransformation.containsKey(ssat.getSourceMediaType()));
                        assertTrue(ssat.getTargetMediaType() + " not expected to be a supported transform target for " + ssat.getSourceMediaType(), pdfRendererSupportedTransformation.get(ssat.getSourceMediaType()).contains(ssat.getTargetMediaType()));
                    }
                    break;

                case "libreoffice":
                    assertEquals(t.transformer.getTransformerName() + " incorrect number of supported transform", 9, t.transformer.getSupportedSourceAndTargetList().size());
                    assertEquals( t.transformer.getTransformerName() + "incorrect number of transform option names", 0, t.transformer.getTransformOptions().size());
                    assertEquals( t.transformer.getTransformerName() + "incorrect number of transform options", 0, countTopLevelOptions(t.transformer.getTransformOptions()));
                    assertEquals(t.transformer.getTransformerName() + " expected to not be a transformer pipeline", t.transformer.getTransformerPipeline().size(), 0);
                    assertEquals(t.transformer.getTransformerName() + " expected to not be a failover pipeline", t.transformer.getTransformerFailover().size(), 0);

                    //Test supportedSourceAndTargetList
                    for ( SupportedSourceAndTarget ssat: t.transformer.getSupportedSourceAndTargetList())
                    {
                        assertTrue(ssat.getSourceMediaType() + " not expected to be a supported transform source.", libreofficeSupportedTransformation.containsKey(ssat.getSourceMediaType()));
                        assertTrue(ssat.getTargetMediaType() + " not expected to be a supported transform target for " + ssat.getSourceMediaType(), libreofficeSupportedTransformation.get(ssat.getSourceMediaType()).contains(ssat.getTargetMediaType()));
                    }
                    break;

                case "officeToImageViaPdf":
                    assertEquals(t.transformer.getTransformerName() + " incorrect number of supported transform", 28, t.transformer.getSupportedSourceAndTargetList().size());
                    assertEquals( t.transformer.getTransformerName() + "incorrect number of transform option names", 2, t.transformer.getTransformOptions().size());
                    assertEquals( t.transformer.getTransformerName() + "incorrect number of transform options", 11, countTopLevelOptions(t.transformer.getTransformOptions()));
                    assertEquals(t.transformer.getTransformerName() + " expected to be a transformer pipeline", t.transformer.getTransformerPipeline().size(), 3);

                    //Test supportedSourceAndTargetList
                    for ( SupportedSourceAndTarget ssat: t.transformer.getSupportedSourceAndTargetList())
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
        retrieveLocalTransformList();

        assertNotNull("Transformer list is null.", transformerList);
        for (CombinedConfig.TransformAndItsOrigin t : transformerList)
        {
            if(t.transformer.getTransformerPipeline() == null)
            {
                assertNotNull(t.transformer.getTransformerName()+ " JVM property not set.", System.getProperty(LOCAL_TRANSFORM + t.transformer.getTransformerName() + URL));
            }
        }
        assertEquals("Unexpected pdfrenderer JVM property value", "http://localhost:8090/", System.getProperty(LOCAL_TRANSFORM + "pdfrenderer" + URL));
        assertEquals("Unexpected imagemagick JVM property value", "http://localhost:8091/", System.getProperty(LOCAL_TRANSFORM + "imagemagick" + URL));
        assertEquals("Unexpected libreoffice JVM property value", "http://localhost:8092/", System.getProperty(LOCAL_TRANSFORM + "libreoffice" + URL));
        assertEquals("Unexpected tika JVM property value", "http://localhost:8093/", System.getProperty(LOCAL_TRANSFORM + "tika" + URL));

        for (CombinedConfig.TransformAndItsOrigin t : transformerList)
        {
            if(t.transformer.getTransformerPipeline() == null)
            {
                assertNotNull(t.transformer.getTransformerName()+ " alfresco-global property not set.", properties.getProperty(LOCAL_TRANSFORM + t.transformer.getTransformerName() + URL));
            }
        }
        assertEquals("Unexpected pdfrenderer alfresco-global property value", "http://localhost:8090/", properties.getProperty(LOCAL_TRANSFORM + "pdfrenderer" + URL));
        assertEquals("Unexpected imagemagick alfresco-global property value", "http://localhost:8091/", properties.getProperty(LOCAL_TRANSFORM + "imagemagick" + URL));
        assertEquals("Unexpected libreoffice alfresco-global property value", "http://localhost:8092/", properties.getProperty(LOCAL_TRANSFORM + "libreoffice" + URL));
        assertEquals("Unexpected tika alfresco-global property value", "http://localhost:8093/", properties.getProperty(LOCAL_TRANSFORM + "tika" + URL));
    }

    @Test
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
    public void testStripExtraOptions()
    {
        retrieveLocalTransformList();

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
    public void testPriority()
    {
        retrieveLocalTransformList();

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
}
