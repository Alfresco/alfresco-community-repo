/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.util.Timeout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.alfresco.encryption.KeyResourceLoader;
import org.alfresco.encryption.KeyStoreParameters;
import org.alfresco.encryption.ssl.SSLEncryptionParameters;

/**
 * JUnit Test for the {@link ElasticsearchHttpClientFactory} class.
 **/
public class ElasticsearchHttpClientFactoryTest
{

    protected static final Logger ELASTICSEARCH_LOGGER = LogManager.getLogger(ElasticsearchHttpClientFactory.class);
    private ElasticsearchHttpClientFactory elasticsearchHttpClientFactory;

    @Before
    public void setUp()
    {
        elasticsearchHttpClientFactory = new ElasticsearchHttpClientFactory();
        configureClientFactory(elasticsearchHttpClientFactory);
    }

    @After
    public void tearDown()
    {
        Configurator.setLevel(ELASTICSEARCH_LOGGER, null);
    }

    @Test
    public void testGetElasticsearchClient()
    {
        assertNotNull("ElasticsearchClient should not be null",
                elasticsearchHttpClientFactory.getElasticsearchClient());
    }

    @Test
    public void testResponseTimeoutProperty()
    {
        int customResponseTimeout = 6000;
        configureClientFactory(elasticsearchHttpClientFactory);

        // Set the response timeout — equivalent to Spring injecting the property value via setResponseTimeout()
        elasticsearchHttpClientFactory.setResponseTimeout(customResponseTimeout);

        // Trigger client creation — this builds and caches the RequestConfig.
        elasticsearchHttpClientFactory.getElasticsearchClient();

        // Assert that the cached RequestConfig still holds the customResponseTimeout value of 6000ms
        assertThat(elasticsearchHttpClientFactory.getRequestConfig())
                .isNotNull()
                .extracting(RequestConfig::getResponseTimeout)
                .isEqualTo(Timeout.ofMilliseconds(customResponseTimeout));
    }

    @Test
    public void testGetElasticsearchServerUrlWithNone()
    {
        elasticsearchHttpClientFactory.setSecureComms("none");
        assertThat(elasticsearchHttpClientFactory.getElasticsearchServerUrl()).startsWith("http://");
    }

    @Test
    public void testGetElasticsearchServerUrlWithHttps()
    {
        elasticsearchHttpClientFactory.setSecureComms("https");
        assertThat(elasticsearchHttpClientFactory.getElasticsearchServerUrl()).startsWith("https://");
    }

    @Test
    public void testGetElasticsearchClientWithHttps()
    {
        ElasticsearchHttpClientFactory factory = createFactoryWithSsl("https");
        assertNotNull("Elasticsearch client should not be null with https", factory.getElasticsearchClient());
    }

    private ElasticsearchHttpClientFactory createFactoryWithSsl(String secureComms)
    {
        KeyStoreParameters keyStoreParams = mock(KeyStoreParameters.class);
        when(keyStoreParams.getLocation()).thenReturn("classpath:alfresco/keystore/ssl.keystore");
        when(keyStoreParams.getType()).thenReturn("JCEKS");
        when(keyStoreParams.getProvider()).thenReturn(null);

        KeyStoreParameters trustStoreParams = mock(KeyStoreParameters.class);
        when(trustStoreParams.getLocation()).thenReturn("classpath:alfresco/keystore/ssl.truststore");
        when(trustStoreParams.getType()).thenReturn("JCEKS");
        when(trustStoreParams.getProvider()).thenReturn(null);

        SSLEncryptionParameters sslParams = new SSLEncryptionParameters(keyStoreParams, trustStoreParams);
        KeyResourceLoader keyResourceLoader = mock(KeyResourceLoader.class);

        ElasticsearchHttpClientFactory factory = new ElasticsearchHttpClientFactory();
        factory.setHost("localhost");
        factory.setPort(9200);
        factory.setBaseUrl("/");
        factory.setSecureComms(secureComms);
        factory.setHostNameVerification(false);
        factory.setSslEncryptionParameters(sslParams);
        factory.setKeyResourceLoader(keyResourceLoader);
        factory.init();

        return factory;
    }

    protected static void configureClientFactory(ElasticsearchHttpClientFactory elasticsearchHttpClientFactory)
    {
        elasticsearchHttpClientFactory.setSecureComms("none");
        elasticsearchHttpClientFactory.setHost("localhost");
        elasticsearchHttpClientFactory.setPort(9200);
        elasticsearchHttpClientFactory.setBaseUrl("/");
    }

}
