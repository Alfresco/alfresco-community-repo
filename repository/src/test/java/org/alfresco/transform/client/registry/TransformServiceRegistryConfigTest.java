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
package org.alfresco.transform.client.registry;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.transform.client.model.config.TransformOption;
import org.alfresco.transform.client.model.config.TransformOptionGroup;
import org.alfresco.transform.client.model.config.TransformOptionValue;
import org.alfresco.transform.client.registry.SupportedTransform;
import org.alfresco.transform.client.registry.TransformRegistryTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test the config received from the Transform Service about what it supports.
 *
 * @author adavis
 */
public class TransformServiceRegistryConfigTest extends TransformRegistryTest
{
    private static Log log = LogFactory.getLog(TransformServiceRegistryConfigTest.class);

    public static final String PNG = "image/png";
    public static final String TIFF = "image/tiff";

    private static final String TRANSFORM_SERVICE_CONFIG = "alfresco/transform-service-config-test.json";
    private static final String TRANSFORM_SERVICE_CONFIG_PIPELINE = "alfresco/transform-service-config-pipeline-test.json";

    public static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();

    @Before
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        LogManager.getLogger(TransformServiceRegistryConfigTest.class).setLevel(Level.DEBUG);
    }

    @Override
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

    private void register(String path) throws IOException
    {
        CombinedConfig combinedConfig = new CombinedConfig(log);
        combinedConfig.addLocalConfig(path);
        combinedConfig.register((TransformServiceRegistryImpl)registry);
    }

    @Test
    public void testJsonConfig() throws IOException
    {
        internalTestJsonConfig(60, 60);
    }

    protected void internalTestJsonConfig(int expectedSourceTargetUniqueCount, int expectedSourceTargetCount)  throws IOException
    {
        register(getTransformServiceConfig());

        // Check the count of transforms supported
        assertEquals("The number of UNIQUE source to target mimetypes transforms has changed. Config change?",
                expectedSourceTargetUniqueCount, countSupportedTransforms(true));
        assertEquals("The number of source to target mimetypes transforms has changed. " +
                        "There may be multiple transformers for the same combination. Config change?",
                expectedSourceTargetCount, countSupportedTransforms(false));

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
        register(getTransformServiceConfigPipeline());

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

    protected int getExpectedTransformsForTestJsonPipeline()
    {
//        {"sourceMediaType": "application/msword",  "targetMediaType": "image/gif" },
//        {"sourceMediaType": "application/msword",  "targetMediaType": "image/jpeg"},
//        {"sourceMediaType": "application/msword",  "targetMediaType": "image/png" },
//        {"sourceMediaType": "application/msword",  "targetMediaType": "image/tiff"}
        return 4;
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
}