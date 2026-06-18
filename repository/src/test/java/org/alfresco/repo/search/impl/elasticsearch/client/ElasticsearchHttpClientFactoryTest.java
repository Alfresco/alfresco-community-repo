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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.util.Timeout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;

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

    @Test
    public void testDestroyClosesTransportAndNullsClient() throws IOException
    {
        // Given a factory with a mock client whose transport we can verify
        OpenSearchTransport mockTransport = mock(OpenSearchTransport.class);
        OpenSearchClient mockClient = mock(OpenSearchClient.class);
        when(mockClient._transport()).thenReturn(mockTransport);

        elasticsearchHttpClientFactory.setClient(mockClient);
        assertNotNull("Client should be set before destroy", elasticsearchHttpClientFactory.getClient());

        // When
        elasticsearchHttpClientFactory.destroy();

        // Then
        verify(mockTransport).close();
        assertNull("Client should be null after destroy", elasticsearchHttpClientFactory.getClient());
    }

    @Test
    public void testDestroyWhenClientIsNullIsNoOp()
    {
        // Given no client has been created
        assertNull("Client should initially be null", elasticsearchHttpClientFactory.getClient());

        // When / Then destroy should not throw
        elasticsearchHttpClientFactory.destroy();
        assertNull("Client should still be null after destroy", elasticsearchHttpClientFactory.getClient());
    }

    @Test
    public void testDestroyHandlesIOExceptionGracefully() throws IOException
    {
        // Given a mock transport that throws IOException on close
        OpenSearchTransport mockTransport = mock(OpenSearchTransport.class);
        doThrow(new IOException("Simulated close failure")).when(mockTransport).close();
        OpenSearchClient mockClient = mock(OpenSearchClient.class);
        when(mockClient._transport()).thenReturn(mockTransport);

        elasticsearchHttpClientFactory.setClient(mockClient);

        // When / Then destroy should not propagate the exception and should still null out the client
        elasticsearchHttpClientFactory.destroy();
        assertNull("Client should be null even after IOException during close", elasticsearchHttpClientFactory.getClient());
    }

    @Test
    public void testGetElasticsearchClientAfterDestroyCreatesNewClient()
    {
        // Given we create a client and then destroy it
        OpenSearchClient firstClient = elasticsearchHttpClientFactory.getElasticsearchClient();
        assertNotNull("First client should not be null", firstClient);

        elasticsearchHttpClientFactory.destroy();
        assertNull("Client should be null after destroy", elasticsearchHttpClientFactory.getClient());

        // When requesting the client again
        OpenSearchClient secondClient = elasticsearchHttpClientFactory.getElasticsearchClient();

        // Then a new client instance should be created
        assertNotNull("Second client should not be null", secondClient);
        assertThat(secondClient).isNotSameAs(firstClient);
    }

    @Test
    public void testGetElasticsearchClientIsThreadSafe() throws Exception
    {
        int threadCount = 10;
        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount))
        {
            CountDownLatch startLatch = new CountDownLatch(1);

            List<Future<OpenSearchClient>> futures = IntStream.range(0, threadCount)
                    .mapToObj(i -> executor.submit(() -> {
                        startLatch.await();
                        return elasticsearchHttpClientFactory.getElasticsearchClient();
                    }))
                    .toList();

            startLatch.countDown();

            Set<OpenSearchClient> clients = ConcurrentHashMap.newKeySet();
            for (Future<OpenSearchClient> future : futures)
            {
                clients.add(future.get());
            }

            assertThat(clients).hasSize(1);
            assertSame("All threads should get the same client instance",
                    clients.iterator().next(), elasticsearchHttpClientFactory.getClient());
        }
    }

}
