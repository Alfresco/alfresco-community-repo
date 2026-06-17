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

import java.time.Duration;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
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
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;

import org.alfresco.encryption.AlfrescoKeyStore;
import org.alfresco.encryption.AlfrescoKeyStoreImpl;
import org.alfresco.encryption.KeyResourceLoader;
import org.alfresco.encryption.ssl.SSLEncryptionParameters;
import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Singleton factory for Elasticsearch Http Client. This class is providing an Elastic RestHighLevelClient instance, that maintains a pool of RestLowLevelClient instances.
 */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyFields"})
public class ElasticsearchHttpClientFactory
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchHttpClientFactory.class);

    protected static final String TLS = "TLS";
    protected static final String TLS_V_1_2 = "TLSv1.2";
    protected static final String TLS_V_1_3 = "TLSv1.3";
    protected static final String SECURE_COMMS_HTTPS = "https";

    // Elasticsearch Http Client connection pool
    private volatile OpenSearchClient client;
    private volatile OpenSearchTransport transport;
    private volatile AutoCloseable managedAwsHttpClient;

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

    // Authentication mode parameters for Elasticsearch server endpoint
    private String authMode = AuthMode.BASIC.value;
    private String awsRegion;
    private String awsService = "es";

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
    private int responseTimeout;

    // Built RequestConfig instance (set once during client creation)
    private RequestConfig builtRequestConfig;

    /**
     * Initialize SSL Truststore for https connections using "encryption.ssl.truststore.*" properties
     */
    public void init()
    {
        this.sslTrustStore = new AlfrescoKeyStoreImpl(sslEncryptionParameters.getTrustStoreParameters(), keyResourceLoader);
    }

    protected String getSecureComms()
    {
        return secureComms;
    }

    protected String getHost()
    {
        return host;
    }

    protected String getBaseUrl()
    {
        return baseUrl;
    }

    protected int getPort()
    {
        return port;
    }

    protected SSLEncryptionParameters getSslEncryptionParameters()
    {
        return sslEncryptionParameters;
    }

    protected KeyResourceLoader getKeyResourceLoader()
    {
        return keyResourceLoader;
    }

    protected AlfrescoKeyStore getSslTrustStore()
    {
        return sslTrustStore;
    }

    protected boolean isHostNameVerification()
    {
        return hostNameVerification;
    }

    protected String getUser()
    {
        return user;
    }

    protected int getMaxTotalConnections()
    {
        return maxTotalConnections;
    }

    protected int getMaxHostConnections()
    {
        return maxHostConnections;
    }

    protected int getThreadCount()
    {
        return threadCount;
    }

    protected int getConnectionTimeout()
    {
        return connectionTimeout;
    }

    protected int getSocketTimeout()
    {
        return socketTimeout;
    }

    protected int getResponseTimeout()
    {
        return responseTimeout;
    }

    protected OpenSearchClient getClient()
    {
        return client;
    }

    public synchronized void destroy()
    {
        closeManagedResources();
    }

    /**
     * Singleton method returning the Elasticsearch client. The client is only built if it's not already created.
     *
     * @return Elasticsearch client
     */
    @SuppressWarnings({"PMD.AvoidSynchronizedStatement", "PMD.AvoidDeeplyNestedIfStmts"})
    public OpenSearchClient getElasticsearchClient()
    {
        if (client == null)
        {
            synchronized (this)
            {
                if (client == null)
                {
                    String protocol = getProtocol();
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Creating Elasticsearch client for {}://{}:{}{} (secureComms={}, isSecure={}, authMode={})",
                                protocol, host, port, baseUrl, secureComms, isSecure(), getEffectiveAuthMode().value);
                    }
                    client = getElasticsearchClient(protocol, port);
                }
            }
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
    protected OpenSearchClient getElasticsearchClient(String protocol, int port)
    {
        // Keep the legacy basic-auth transport untouched and route IAM through the AWS signer transport.
        return switch (getEffectiveAuthMode())
        {
        case BASIC -> createBasicAuthClient(protocol, port);
        case AWS_IAM -> createAwsIamClient(protocol, port);
        };
    }

    private OpenSearchClient createBasicAuthClient(String protocol, int port)
    {
        OpenSearchTransport transport = ApacheHttpClient5TransportBuilder.builder(new HttpHost(protocol, host, port))
                .setHttpClientConfigCallback(this::getHttpAsyncClientBuilder)
                .setPathPrefix(baseUrl)
                .setMapper(new JacksonJsonpMapper())
                .build();

        this.transport = transport;
        this.managedAwsHttpClient = null;

        return new OpenSearchClient(transport);
    }

    private OpenSearchClient createAwsIamClient(String protocol, int port)
    {
        validateAwsIamConfiguration();

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Creating AWS IAM OpenSearch client for {} using signing service {} in region {}",
                    protocol + "://" + host + ":" + port + baseUrl, awsService, awsRegion);
        }

        SdkAsyncHttpClient awsHttpClient = NettyNioAsyncHttpClient.builder()
                .connectionTimeout(Duration.ofMillis(connectionTimeout))
                .readTimeout(Duration.ofMillis(socketTimeout))
                .build();

        OpenSearchTransport transport = new AwsSdk2Transport(
                awsHttpClient,
                host,
                awsService,
                Region.of(awsRegion),
                AwsSdk2TransportOptions.builder()
                        .setMapper(new JacksonJsonpMapper())
                        .build());

        this.transport = transport;
        this.managedAwsHttpClient = awsHttpClient;

        return new OpenSearchClient(transport);
    }

    private AuthMode getEffectiveAuthMode()
    {
        return AuthMode.fromProperty(authMode);
    }

    /**
     * @return {@code true} when the configured {@code secureComms} value indicates an HTTPS endpoint. Subclasses may broaden this (for example, an mTLS-aware enterprise factory).
     */
    protected boolean isSecure()
    {
        return SECURE_COMMS_HTTPS.equals(secureComms);
    }

    /**
     * @return the wire protocol ("https" or "http") derived from {@link #isSecure()}.
     */
    protected String getProtocol()
    {
        return isSecure() ? "https" : "http";
    }

    private void validateAwsIamConfiguration()
    {
        if (StringUtils.isBlank(awsRegion))
        {
            throw new IllegalStateException("Property elasticsearch.aws.region is required when elasticsearch.auth.mode=aws-iam");
        }

        if (StringUtils.isBlank(awsService))
        {
            throw new IllegalStateException("Property elasticsearch.aws.service must not be blank when elasticsearch.auth.mode=aws-iam");
        }

        // AwsSdk2Transport hardcodes the "https://" scheme, ignores the configured port, and has no path-prefix support; AWS-managed OpenSearch is the only supported IAM target.
        if (!SECURE_COMMS_HTTPS.equals(secureComms))
        {
            throw new IllegalStateException(
                    "elasticsearch.secureComms must be 'https' when elasticsearch.auth.mode=aws-iam");
        }

        if (port != 443)
        {
            throw new IllegalStateException(
                    "elasticsearch.port must be 443 when elasticsearch.auth.mode=aws-iam");
        }

        if (!"/".equals(StringUtils.trimToNull(baseUrl)))
        {
            throw new IllegalStateException(
                    "elasticsearch.baseUrl must be '/' when elasticsearch.auth.mode=aws-iam");
        }
    }

    private void closeManagedResources()
    {
        Exception primary = null;

        OpenSearchTransport transportToClose = transport;
        if (transportToClose == null && client != null)
        {
            try
            {
                transportToClose = client._transport();
            }
            catch (Exception exception)
            {
                LOGGER.warn("Unable to retrieve Elasticsearch transport for shutdown", exception);
            }
        }

        if (transportToClose != null)
        {
            try
            {
                transportToClose.close();
            }
            catch (Exception exception)
            {
                primary = exception;
            }
        }

        if (managedAwsHttpClient != null)
        {
            try
            {
                managedAwsHttpClient.close();
            }
            catch (Exception exception)
            {
                if (primary == null)
                {
                    primary = exception;
                }
                else
                {
                    primary.addSuppressed(exception);
                }
            }
        }

        transport = null;
        managedAwsHttpClient = null;
        client = null;

        if (primary != null)
        {
            LOGGER.warn("Unable to close Elasticsearch HTTP client resources", primary);
        }
    }

    /**
     * Builds and caches the RequestConfig with the configured response timeout.
     */
    protected RequestConfig buildRequestConfig()
    {
        this.builtRequestConfig = RequestConfig.custom()
                .setResponseTimeout(Timeout.ofMilliseconds(responseTimeout))
                .build();
        return this.builtRequestConfig;
    }

    /**
     * Returns the built RequestConfig instance, or null if not built yet.
     */
    RequestConfig getRequestConfig()
    {
        return builtRequestConfig;
    }

    /**
     * Apply pooling options, credentials and SSL settings to Elasticsearch client
     *
     * @param httpClientBuilder
     *            Existing HttpClientBuilder instance
     * @return httpClientBuilder including required settings
     */
    protected HttpAsyncClientBuilder getHttpAsyncClientBuilder(HttpAsyncClientBuilder httpClientBuilder)
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
        if (secureComms.equals(SECURE_COMMS_HTTPS))
        {
            LOGGER.debug("Configuring TLS strategy for Elasticsearch client (secureComms={}, hostNameVerification={})", secureComms, hostNameVerification);
            var tlsStrategyBuilder = ClientTlsStrategyBuilder.create().setSslContext(getSSLContext()).setTlsVersions(TLS_V_1_2, TLS_V_1_3);
            if (!hostNameVerification)
            {
                tlsStrategyBuilder.setHostnameVerifier(NoopHostnameVerifier.INSTANCE);
            }
            connectionBuilder.setTlsStrategy(tlsStrategyBuilder.buildAsync());
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

        // Build and set the HTTP/2 response timeout using the configured responseTimeout property
        httpClientBuilder.setDefaultRequestConfig(buildRequestConfig());
        return httpClientBuilder;
    }

    /**
     * Build CredentialsProvider instance with user and password values from properties file
     *
     * @return CredentialsProvider instance
     */
    protected CredentialsProvider getCredentialsProvider()
    {
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        AuthScope anyHostPortScope = new AuthScope(null, -1);
        credentialsProvider.setCredentials(anyHostPortScope, new UsernamePasswordCredentials(user, password.toCharArray()));
        return credentialsProvider;
    }

    /**
     * Build SSLContext instance with truststore that must include Elasticsearch server public certificate in order to be trusted for this https connection.
     *
     * @return SSLContext instance
     */
    protected SSLContext getSSLContext()
    {
        TrustManager[] trustmanagers = sslTrustStore.createTrustManagers();

        try
        {
            SSLContext sslcontext = SSLContext.getInstance(TLS);
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

    public void setAuthMode(String authMode)
    {
        this.authMode = authMode;
    }

    public void setAwsRegion(String awsRegion)
    {
        this.awsRegion = awsRegion;
    }

    public void setAwsService(String awsService)
    {
        this.awsService = awsService;
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

    public void setResponseTimeout(int responseTimeout)
    {
        this.responseTimeout = responseTimeout;
    }

    protected void setClient(OpenSearchClient client)
    {
        this.client = client;
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

    private enum AuthMode
    {
        BASIC("basic"), AWS_IAM("aws-iam");

        private final String value;

        AuthMode(String value)
        {
            this.value = value;
        }

        private static AuthMode fromProperty(String value)
        {
            if (StringUtils.isBlank(value))
            {
                return BASIC;
            }

            for (AuthMode mode : values())
            {
                if (mode.value.equalsIgnoreCase(value.trim()))
                {
                    return mode;
                }
            }

            throw new IllegalArgumentException("Unsupported elasticsearch.auth.mode: " + value);
        }
    }

}
