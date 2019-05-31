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

import org.alfresco.repo.content.transform.TransformerDebug;
import org.alfresco.repo.content.transform.LocalTransformServiceRegistry;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Properties;

/**
 * Extends the {@link TransformServiceRegistryImplTest} (used to test the config received from the Transform Service)
 * so that configuration for the local transformations may be tested. This includes pipelines and options specific
 * transform steps.
 */
public class LocalTransformServiceRegistryTest extends TransformServiceRegistryImplTest
{
    protected LocalTransformServiceRegistry registry;
    private Properties properties;
    @Mock
    private TransformerDebug transformerDebug;

    public static final String TRANSFORM_SERVICE_CONFIG = "alfresco/transformers";

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        properties = new Properties();
        properties.setProperty(        "name.url", "dummy");
        properties.setProperty("transformer1.url", "dummy");
        properties.setProperty("transformer2.url", "dummy");
        properties.setProperty("transformer3.url", "dummy");
        properties.setProperty("transformer4.url", "dummy");
        properties.setProperty("transformer5.url", "dummy");
        properties.setProperty( "libreoffice.url", "dummy");
        properties.setProperty(        "tika.url", "dummy");
        properties.setProperty( "pdfrenderer.url", "dummy");
        properties.setProperty( "imagemagick.url", "dummy");

        super.setUp();
    }

    protected TransformServiceRegistryImpl buildTransformServiceRegistryImpl()
    {
        registry = new LocalTransformServiceRegistry();
        registry.setJsonObjectMapper(JSON_OBJECT_MAPPER);
        registry.setProperties(properties);
        registry.setTransformerDebug(transformerDebug);
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

    // TODO test pipeline

    // TODO test strict mimetype check

    // TODO test retry transform on different mimetype

    // TODO test using system properties and alfresco-global.properties

    // TODO test the JsonConverter
}
