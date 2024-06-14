/*
 * Copyright (C) 2023 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */

package org.alfresco.httpclient;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.ConnectionConfig.Builder;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;


public class HttpClient4Factory
{
    protected static final String TLS_PROTOCOL = "TLS";
    protected static final String HTTPS_PROTOCOL = "https";
    protected static final String HTTP_TARGET_HOST = "http.target_host";
    protected static final String TLS_V_1_2 = "TLSv1.2";
    protected static final String TLS_V_1_3 = "TLSv1.3";

    private static SSLContext createSSLContext(HttpClientConfig config)
    {
        KeyManager[] keyManagers = config.getKeyStore().createKeyManagers();
        TrustManager[] trustManagers = config.getTrustStore().createTrustManagers();

        try
        {
            SSLContext sslcontext = SSLContext.getInstance(TLS_PROTOCOL);
            sslcontext.init(keyManagers, trustManagers, null);
            return sslcontext;
        }
        catch(Throwable e)
        {
            throw new AlfrescoRuntimeException("Unable to create SSL context", e);
        }
    }

    public static CloseableHttpClient createHttpClient(HttpClientConfig config)
    {
        return createHttpClient(config, null);
    }

    public static CloseableHttpClient createHttpClient(HttpClientConfig config, HttpClientConnectionManager connectionManager)
    {
        HttpClientBuilder clientBuilder = HttpClients.custom();
        PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create();

        if(config.isMTLSEnabled())
        {
            clientBuilder.addRequestInterceptorFirst((request, details, context) -> {
                if (!((HttpHost) context.getAttribute(HTTP_TARGET_HOST)).getSchemeName().equals(HTTPS_PROTOCOL))
                {
                    String msg = "mTLS is enabled but provided URL does not use a secured protocol";
                    throw new HttpClientException(msg);
                }
            });
            connectionManagerBuilder.setSSLSocketFactory(getSslConnectionSocketFactory(config));
        }

        if (connectionManager != null)
        {
            clientBuilder.setConnectionManager(connectionManager);
        }
        else
        {
            //Setting a connectionManager overrides these properties
            config.getMaxTotalConnections().ifPresent(connectionManagerBuilder::setMaxConnTotal);
            config.getMaxHostConnections().ifPresent(connectionManagerBuilder::setMaxConnPerRoute);
            clientBuilder.setConnectionManager(connectionManagerBuilder.build());
        }

        Builder connectionConfigBuilder = ConnectionConfig.custom();
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        config.getConnectionTimeout().ifPresent(v -> connectionConfigBuilder.setConnectTimeout(Timeout.ofSeconds(v)));
        config.getConnectionRequestTimeout().ifPresent(v -> requestConfigBuilder.setConnectionRequestTimeout(Timeout.ofSeconds(v)));
        config.getSocketTimeout().ifPresent(v -> connectionConfigBuilder.setSocketTimeout(Timeout.ofSeconds(v)));

        clientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());

        clientBuilder.setRetryStrategy(new DefaultHttpRequestRetryStrategy(5, TimeValue.ofSeconds(1L)));

        return clientBuilder.build();
    }

    private static SSLConnectionSocketFactory getSslConnectionSocketFactory(HttpClientConfig config)
    {
        return new SSLConnectionSocketFactory(
                createSSLContext(config),
                new String[] { TLS_V_1_2, TLS_V_1_3 },
                null,
                config.isHostnameVerificationDisabled() ? new NoopHostnameVerifier() : new DefaultHostnameVerifier());
    }

    public static PoolingHttpClientConnectionManager createPoolingConnectionManager(HttpClientConfig config)
    {
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;
        if(config.isMTLSEnabled())
        {
            poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(
                    RegistryBuilder.<ConnectionSocketFactory>create()
                                   .register("https", getSslConnectionSocketFactory(config))
                                   .build());
        }
        else
        {
            poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(
                    RegistryBuilder.<ConnectionSocketFactory>create()
                   .register("http", PlainConnectionSocketFactory.getSocketFactory())
                   .build());
        }
        config.getMaxTotalConnections().ifPresent(poolingHttpClientConnectionManager::setMaxTotal);
        config.getMaxHostConnections().ifPresent(poolingHttpClientConnectionManager::setDefaultMaxPerRoute);

        return poolingHttpClientConnectionManager;
    }
}
