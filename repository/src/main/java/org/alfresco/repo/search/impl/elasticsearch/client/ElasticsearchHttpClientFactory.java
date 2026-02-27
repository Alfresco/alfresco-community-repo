/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.encryption.AlfrescoKeyStore;
import org.alfresco.encryption.AlfrescoKeyStoreImpl;
import org.alfresco.encryption.KeyResourceLoader;
import org.alfresco.encryption.ssl.SSLEncryptionParameters;
import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Singleton factory for Elasticsearch Http Client. This class is providing an Elastic RestHighLevelClient instance, that maintains a pool of RestLowLevelClient instances.
 */
public class ElasticsearchHttpClientFactory
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchHttpClientFactory.class);

    // Elasticsearch Http Client connection pool
    private OpenSearchClient client;

    // Basic parameters for Elasticsearch server endpoint
    private String host;
    private String baseUrl;
    private int port;

    // SSL parameters for Elasticsearch server endpoint
    private String secureComms;
    private AlfrescoKeyStore sslTrustStore;
    private SSLEncryptionParameters sslEncryptionParameters;
    private KeyResourceLoader keyResourceLoader;
    private boolean hostNameVerification;

    // Http Basic Authentication credential parameters for Elasticsearch server endpoint
    private String user;
    private String password;

    // Elasticsearch index details
    private String indexName;
    private String archiveIndexName;

    // Connection pool size
    private int maxTotalConnections;
    private int maxHostConnections;
    private int threadCount;

    // Connection and request timeout
    private int connectionTimeout;
    private int socketTimeout;

    /**
     * Initialize SSL Truststore for https connections using "encryption.ssl.truststore.*" properties
     */
    public void init()
    {
        this.sslTrustStore = new AlfrescoKeyStoreImpl(sslEncryptionParameters.getTrustStoreParameters(), keyResourceLoader);
    }

    /**
     * Singleton method returning the Elasticsearch client. The client is only built if it's not already created.
     *
     * @return Elasticsearch client
     */
    public OpenSearchClient getElasticsearchClient()
    {
        if (client == null)
        {
            LOGGER.debug("Creating Elasticsearch client for {}", (secureComms.equals("https") ? "https" : "http") + "://" + host + ":" + port + baseUrl);
            client = getElasticsearchClient(secureComms.equals("https") ? "https" : "http", port);
        }
        return client;
    }

    /**
     * Gets Elasticsearch server URL
     *
     * @return Elasticsearch server URL
     */
    public String getElasticsearchServerUrl()
    {
        return (secureComms.equals("https") ? "https" : "http") + "://" + host + ":" + port + baseUrl;
    }

    /**
     * Creates an Elasticsearch client applying parameters from properties file
     *
     * @param protocol
     *            Http protocol: http or https
     * @param port
     *            Port number
     * @return Elasticsearch client ready to be used
     */
    private OpenSearchClient getElasticsearchClient(String protocol, int port)
    {
        OpenSearchTransport transport = ApacheHttpClient5TransportBuilder.builder(new HttpHost(protocol, host, port))
                .setHttpClientConfigCallback(this::getHttpAsyncClientBuilder)
                .setPathPrefix(baseUrl)
                .setMapper(new JacksonJsonpMapper())
                .build();

        return new OpenSearchClient(transport);
    }

    /**
     * Apply pooling options, credentials and SSL settings to Elasticsearch client
     *
     * @param httpClientBuilder
     *            Existing HttpClientBuilder instance
     * @return httpClientBuilder including required settings
     */
    private HttpAsyncClientBuilder getHttpAsyncClientBuilder(HttpAsyncClientBuilder httpClientBuilder)
    {
        final var connectionBuilder = PoolingAsyncClientConnectionManagerBuilder.create()
                .setMaxConnTotal(maxTotalConnections)
                .setMaxConnPerRoute(maxHostConnections)
                .setDefaultConnectionConfig(
                        ConnectionConfig.custom()
                                .setConnectTimeout(Timeout.ofMilliseconds(connectionTimeout))
                                .setSocketTimeout(Timeout.ofMilliseconds(socketTimeout))
                                .build());

        // Credentials have been given, so pass them wrapped in a CredentialsProvider
        if (user != null && !user.isEmpty())
        {
            httpClientBuilder.setDefaultCredentialsProvider(getCredentialsProvider());
        }

        // Secure http mode has been selected, so build the SSLContext with the right truststore
        if (secureComms.equals("https"))
        {
            var tlsStrategyBuilder = ClientTlsStrategyBuilder.create().setSslContext(getSSLContext());
            if (!hostNameVerification)
            {
                tlsStrategyBuilder.setHostnameVerifier(NoopHostnameVerifier.INSTANCE);
            }
            connectionBuilder.setTlsStrategy(tlsStrategyBuilder.build());
        }
        // Override the default thread count unless it's either undefined or invalid
        if (threadCount > 0)
        {
            httpClientBuilder.setIOReactorConfig(IOReactorConfig.custom().setIoThreadCount(threadCount).build());
        }
        else
        {
            LOGGER.debug("Using default ioThreadCount for Elasticsearch HTTP Client since the specified value was {}.", threadCount);
        }

        httpClientBuilder.setUserAgent(StringUtils.EMPTY).setConnectionManager(connectionBuilder.build());

        return httpClientBuilder;
    }

    /**
     * Build CredentialsProvider instance with user and password values from properties file
     *
     * @return CredentialsProvider instance
     */
    private CredentialsProvider getCredentialsProvider()
    {
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        AuthScope anyHostPortScope = new AuthScope(null, -1);
        credentialsProvider.setCredentials(anyHostPortScope, new UsernamePasswordCredentials(user, password.toCharArray()));
        return credentialsProvider;
    }

    /**
     * Build SSLContext instance with truststore that must include Elasticsearch server public certificate in order to be trusted for this https connection.
     *
     * @return SSLContext instnce
     */
    private SSLContext getSSLContext()
    {
        TrustManager[] trustmanagers = sslTrustStore.createTrustManagers();

        try
        {
            SSLContext sslcontext = SSLContext.getInstance("TLSv1.2");
            sslcontext.init(null, trustmanagers, null);
            return sslcontext;
        }
        catch (Exception e)
        {
            throw new AlfrescoRuntimeException("Unable to create SSL context", e);
        }
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public void setSecureComms(String secureComms)
    {
        this.secureComms = secureComms;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setMaxTotalConnections(int maxTotalConnections)
    {
        this.maxTotalConnections = maxTotalConnections;
    }

    public void setMaxHostConnections(int maxHostConnections)
    {
        this.maxHostConnections = maxHostConnections;
    }

    public void setConnectionTimeout(int connectionTimeout)
    {
        this.connectionTimeout = connectionTimeout;
    }

    public void setSocketTimeout(int socketTimeout)
    {
        this.socketTimeout = socketTimeout;
    }

    public void setSslEncryptionParameters(SSLEncryptionParameters sslEncryptionParameters)
    {
        this.sslEncryptionParameters = sslEncryptionParameters;
    }

    public void setKeyResourceLoader(KeyResourceLoader keyResourceLoader)
    {
        this.keyResourceLoader = keyResourceLoader;
    }

    public void setHostNameVerification(boolean hostNameVerification)
    {
        this.hostNameVerification = hostNameVerification;
    }

    public void setIndexName(String indexName)
    {
        this.indexName = indexName;
    }

    public void setArchiveIndexName(String archiveIndexName)
    {
        this.archiveIndexName = archiveIndexName;
    }

    public String getArchiveIndexName()
    {
        return archiveIndexName;
    }

    public String getIndexName()
    {
        return indexName;
    }

    public void setThreadCount(int threadCount)
    {
        this.threadCount = threadCount;
    }

}
