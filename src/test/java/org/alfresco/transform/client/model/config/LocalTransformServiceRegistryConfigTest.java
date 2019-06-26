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
package org.alfresco.transform.client.model.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.LocalTransformServiceRegistry;
import org.alfresco.repo.content.transform.TransformerDebug;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.CronExpression;
import org.quartz.Scheduler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Extends the {@link TransformServiceRegistryConfigTest} (used to test the config received from the Transform Service)
 * so that configuration for the local transformations may be tested. This includes pipelines and options specific
 * transform steps.
 */
public class LocalTransformServiceRegistryConfigTest extends TransformServiceRegistryConfigTest
{
    private class TestLocalTransformServiceRegistry extends LocalTransformServiceRegistry
    {
        private boolean lastReadSucceed = false;
        private boolean mockSuccessReadingRemoteConfig = true;

        public synchronized boolean getMockSuccessReadingRemoteConfig()
        {
            return mockSuccessReadingRemoteConfig;
        }

        public synchronized void setMockSuccessReadingRemoteConfig(boolean mockSuccessReadingRemoteConfig)
        {
            System.out.println("\n"+getMs()+": set next mock read to "+(mockSuccessReadingRemoteConfig ? "success" : "failure"));
            this.mockSuccessReadingRemoteConfig = mockSuccessReadingRemoteConfig;
        }

        @Override
        protected String getBaseUrlIfTesting(String name, String baseUrl)
        {
            return baseUrl == null
                    ? getProperty(LOCAL_TRANSFORM +name+URL, null)
                    : baseUrl;
        }

        @Override
        protected TransformServiceRegistryImpl.Data readConfig() throws IOException
        {
            readConfigCount++;
            data = createData();
            boolean mockSuccessReadingRemoteConfig = getMockSuccessReadingRemoteConfig();
            lastReadSucceed = mockSuccessReadingRemoteConfig;
            setSuccessReadingRemoteConfig(data, mockSuccessReadingRemoteConfig);
            System.out.println(getMs() + "readConfig() success="+mockSuccessReadingRemoteConfig+" reads="+readConfigCount);
            return data;
        }

        public Data assertDataChanged(Data data, String msg)
        {
            // If the data changes, there has been a read
            System.out.println(getMs()+msg);
            assertNotEquals("The configuration data should have changed: "+msg, this.data, data);
            return this.data;
        }

        public Data assertDataUnchanged(Data data, String msg)
        {
            // If the data changes, there has been a read
            System.out.println(getMs()+msg);
            assertEquals("The configuration data should be the same: "+msg, this.data, data);
            return this.data;
        }
    }

    protected TestLocalTransformServiceRegistry registry;

    private Properties properties = new Properties();

    @Mock private TransformerDebug transformerDebug;
    @Mock private MimetypeMap mimetypeMap;

    private static final String LOCAL_TRANSFORM_SERVICE_CONFIG = "alfresco/local-transform-service-config-test.json";
    private static final String LOCAL_TRANSFORM_SERVICE_CONFIG_PIPELINE = "alfresco/local-transform-service-config-pipeline-test.json";

    private static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();
    private static final String LOCAL_TRANSFORM = "localTransform.";
    private static final String URL = ".url";

    private static Log log = LogFactory.getLog(LocalTransformServiceRegistry.class);

    private Map<String, List<String>> imagemagickSupportedTransformation;
    private Map<String, List<String>> tikaSupportedTransformation;
    private Map<String, List<String>> pdfRendererSupportedTransformation;
    private Map<String, List<String>> libreofficeSupportedTransformation;
    private Map<String, List<String>> officeToImageViaPdfSupportedTransformation;

    private TransformServiceRegistryImpl.Data data;
    private int readConfigCount;
    private long startMs;

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
        registry.setCronExpression(new CronExpression("* * * * * ? 2099")); // not for a long time.
        registry.setInitialAndOnErrorCronExpression(new CronExpression("* * * * * ? 2099")); // not for a long time.
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
        // Need to have at least one supportedSourceAndTargetList element per transformer and there are 3.
        return 4+3;
    }

    /**
     * Reads and loads localTransforms from LOCAL_TRANSFORM_SERVICE_CONFIG config file.
     * @return List<Transformer> list of local transformers.
     */
    private List<CombinedConfig.TransformAndItsOrigin> retrieveLocalTransformList()
    {
        try {
            CombinedConfig combinedConfig = new CombinedConfig(log);
            combinedConfig.addLocalConfig(LOCAL_TRANSFORM_SERVICE_CONFIG);
            return combinedConfig.getTransforms();
        } catch (IOException e) {
            log.error("Could not read LocalTransform config file");
            fail();
        }
        return null;
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

    protected String getBaseUrl(Transformer transformer)
    {
        return LOCAL_TRANSFORM+transformer.getTransformerName()+".url";
    }

    @Test
    public void testReadWriteJson() throws IOException
    {
        // Override super method so it passes, as there is nothing more to be gained for LocalTransforms.
    }

    @Test
    public void testReadJsonConfig()
    {
        List<CombinedConfig.TransformAndItsOrigin> transformerList = retrieveLocalTransformList();
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
            assertTrue(t.transform.getTransformerName() + " should be an expected local transformer.", listOfExpectedTransformersName.contains(t.transform.getTransformerName()));
            listOfExpectedTransformersName.remove(t.transform.getTransformerName());

            switch (t.transform.getTransformerName())
            {
                case "imagemagick":
                    assertEquals(t.transform.getTransformerName() + " incorrect number of supported transform", 14, t.transform.getSupportedSourceAndTargetList().size());
                    assertEquals( t.transform.getTransformerName() + "incorrect number of transform options", 6, t.transform.getTransformOptions().size());
                    assertNull(t.transform.getTransformerName() + " expected to not be a transformer pipeline", t.transform.getTransformerPipeline());

                    //Test supportedSourceAndTargetList
                    for ( SupportedSourceAndTarget ssat: t.transform.getSupportedSourceAndTargetList())
                    {
                        assertTrue(ssat.getSourceMediaType() + " not expected to be a supported transform source.", imagemagickSupportedTransformation.containsKey(ssat.getSourceMediaType()));
                        assertTrue(ssat.getTargetMediaType() + " not expected to be a supported transform target for " + ssat.getSourceMediaType(), imagemagickSupportedTransformation.get(ssat.getSourceMediaType()).contains(ssat.getTargetMediaType()));
                    }
                    break;

                case "tika":
                    assertEquals(t.transform.getTransformerName() + " incorrect number of supported transform", 8, t.transform.getSupportedSourceAndTargetList().size());
                    assertEquals( t.transform.getTransformerName() + "incorrect number of transform options", 5, t.transform.getTransformOptions().size());
                    assertNull(t.transform.getTransformerName() + " expected to not be a transformer pipeline", t.transform.getTransformerPipeline());

                    //Test supportedSourceAndTargetList
                    for ( SupportedSourceAndTarget ssat: t.transform.getSupportedSourceAndTargetList())
                    {
                        assertTrue(ssat.getSourceMediaType() + " not expected to be a supported transform source.", tikaSupportedTransformation.containsKey(ssat.getSourceMediaType()));
                        assertTrue(ssat.getTargetMediaType() + " not expected to be a supported transform target for " + ssat.getSourceMediaType(), tikaSupportedTransformation.get(ssat.getSourceMediaType()).contains(ssat.getTargetMediaType()));
                    }
                    break;

                case "pdfrenderer":
                    assertEquals(t.transform.getTransformerName() + " incorrect number of supported transform", 1, t.transform.getSupportedSourceAndTargetList().size());
                    assertEquals( t.transform.getTransformerName() + "incorrect number of transform options", 5, t.transform.getTransformOptions().size());
                    assertNull(t.transform.getTransformerName() + " expected to not be a transformer pipeline", t.transform.getTransformerPipeline());

                    //Test supportedSourceAndTargetList
                    for ( SupportedSourceAndTarget ssat: t.transform.getSupportedSourceAndTargetList())
                    {
                        assertTrue(ssat.getSourceMediaType() + " not expected to be a supported transform source.", pdfRendererSupportedTransformation.containsKey(ssat.getSourceMediaType()));
                        assertTrue(ssat.getTargetMediaType() + " not expected to be a supported transform target for " + ssat.getSourceMediaType(), pdfRendererSupportedTransformation.get(ssat.getSourceMediaType()).contains(ssat.getTargetMediaType()));
                    }
                    break;

                case "libreoffice":
                    assertEquals(t.transform.getTransformerName() + " incorrect number of supported transform", 9, t.transform.getSupportedSourceAndTargetList().size());
                    assertNull( t.transform.getTransformerName() + "incorrect number of transform options", t.transform.getTransformOptions());
                    assertNull(t.transform.getTransformerName() + " expected to not be a transformer pipeline", t.transform.getTransformerPipeline());

                    //Test supportedSourceAndTargetList
                    for ( SupportedSourceAndTarget ssat: t.transform.getSupportedSourceAndTargetList())
                    {
                        assertTrue(ssat.getSourceMediaType() + " not expected to be a supported transform source.", libreofficeSupportedTransformation.containsKey(ssat.getSourceMediaType()));
                        assertTrue(ssat.getTargetMediaType() + " not expected to be a supported transform target for " + ssat.getSourceMediaType(), libreofficeSupportedTransformation.get(ssat.getSourceMediaType()).contains(ssat.getTargetMediaType()));
                    }
                    break;

                case "officeToImageViaPdf":
                    assertEquals(t.transform.getTransformerName() + " incorrect number of supported transform", 28, t.transform.getSupportedSourceAndTargetList().size());
                    assertEquals( t.transform.getTransformerName() + "incorrect number of transform options", 2, t.transform.getTransformOptions().size());
                    assertNotNull(t.transform.getTransformerName() + " expected to be a transformer pipeline", t.transform.getTransformerPipeline());

                    //Test supportedSourceAndTargetList
                    for ( SupportedSourceAndTarget ssat: t.transform.getSupportedSourceAndTargetList())
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
        List<CombinedConfig.TransformAndItsOrigin> transformerList = retrieveLocalTransformList();
        assertNotNull("Transformer list is null.", transformerList);
        for (CombinedConfig.TransformAndItsOrigin t : transformerList)
        {
            if(t.transform.getTransformerPipeline() == null)
            {
                assertNotNull(t.transform.getTransformerName()+ " JVM property not set.", System.getProperty(LOCAL_TRANSFORM + t.transform.getTransformerName() + URL));
            }
        }
        assertEquals("Unexpected pdfrenderer JVM property value", "http://localhost:8090/", System.getProperty(LOCAL_TRANSFORM + "pdfrenderer" + URL));
        assertEquals("Unexpected imagemagick JVM property value", "http://localhost:8091/", System.getProperty(LOCAL_TRANSFORM + "imagemagick" + URL));
        assertEquals("Unexpected libreoffice JVM property value", "http://localhost:8092/", System.getProperty(LOCAL_TRANSFORM + "libreoffice" + URL));
        assertEquals("Unexpected tika JVM property value", "http://localhost:8093/", System.getProperty(LOCAL_TRANSFORM + "tika" + URL));

        for (CombinedConfig.TransformAndItsOrigin t : transformerList)
        {
            if(t.transform.getTransformerPipeline() == null)
            {
                assertNotNull(t.transform.getTransformerName()+ " alfresco-global property not set.", properties.getProperty(LOCAL_TRANSFORM + t.transform.getTransformerName() + URL));
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
        Scheduler origScheduler = registry.getScheduler();

        if (origScheduler != null)
        {
            origScheduler.clear();
        }

        try
        {
            TransformServiceRegistryImpl.Data prevData;
            data = null;
            readConfigCount = 0;

            registry.setScheduler(null);
            registry.setInitialAndOnErrorCronExpression(new CronExpression(("0/2 * * ? * * *"))); // every 2 seconds rather than 10 seconds
            registry.setCronExpression(new CronExpression(("0/4 * * ? * * *"))); // every 4 seconds rather than 10 mins

            // Sleep until a 6 second boundary, in order to make testing clearer.
            // It avoids having to work out schedule offsets and extra quick runs that can otherwise take place.
            Thread.sleep(4000-System.currentTimeMillis()%4000);
            startMs = System.currentTimeMillis();
            registry.setMockSuccessReadingRemoteConfig(false);
            registry.afterPropertiesSet();

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
            registry.setMockSuccessReadingRemoteConfig(true);
            Thread.sleep(2000); // 9 seconds
            data = registry.assertDataChanged(data, "There should have been a read after 8 seconds that succeeds");

            Thread.sleep(2000); // 11 seconds
            data = registry.assertDataUnchanged(data, "There really should not have been a read until 12 seconds");

            Thread.sleep(2000); // 13 seconds
            data = registry.assertDataChanged(data, "There should have been a read after 12 seconds that succeeds");

            // Should switch back to initial/error schedule after failure
            registry.setMockSuccessReadingRemoteConfig(false);
            Thread.sleep(4000); // 17 seconds
            data = registry.assertDataChanged(data, "There should have been a read after 16 seconds that fails");

            Thread.sleep(2000); // 19 seconds
            data = registry.assertDataChanged(data, "There should have been a read after 18 seconds");
        }
        finally
        {
            registry.setMockSuccessReadingRemoteConfig(true);

            // Reset scheduler properties just in case another tests needs them in future.
            // We don't start the scheduler with registry.afterPropertiesSet() as this is
            // really just mocked up version of the registry.
            registry.setCronExpression(origCronExpression);
            registry.setInitialAndOnErrorCronExpression(origInitialAndOnErrorCronExpression);
            registry.setPipelineConfigDir(origPipelineConfigDir);
            registry.setScheduler(null);
        }
    }
}
