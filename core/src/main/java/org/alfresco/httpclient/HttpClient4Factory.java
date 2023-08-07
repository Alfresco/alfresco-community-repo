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
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;


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

        if(config.isMTLSEnabled())
        {
            clientBuilder.addInterceptorFirst((HttpRequestInterceptor) (request, context) -> {
                if (!((HttpHost) context.getAttribute(HTTP_TARGET_HOST)).getSchemeName().equals(HTTPS_PROTOCOL))
                {
                    String msg = "mTLS is enabled but provided URL does not use a secured protocol";
                    throw new HttpClientException(msg);
                }
            });
            clientBuilder.setSSLSocketFactory(getSslConnectionSocketFactory(config));
        }

        if (connectionManager != null)
        {
            clientBuilder.setConnectionManager(connectionManager);
        }
        else
        {
            //Setting a connectionManager overrides these properties
            config.getMaxTotalConnections().ifPresent(v -> clientBuilder.setMaxConnTotal(v));
            config.getMaxHostConnections().ifPresent(v -> clientBuilder.setMaxConnPerRoute(v));
        }

        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        config.getConnectionTimeout().ifPresent(v -> requestConfigBuilder.setConnectTimeout(v));
        config.getConnectionRequestTimeout().ifPresent(v -> requestConfigBuilder.setConnectionRequestTimeout(v));
        config.getSocketTimeout().ifPresent(v -> requestConfigBuilder.setSocketTimeout(v));

        RequestConfig requestConfig = requestConfigBuilder.build();

        clientBuilder.setDefaultRequestConfig(requestConfig);

        clientBuilder.setRetryHandler(new StandardHttpRequestRetryHandler(5, false));

        return clientBuilder.build();
    }

    private static SSLConnectionSocketFactory getSslConnectionSocketFactory(HttpClientConfig config)
    {
        return new SSLConnectionSocketFactory(
                createSSLContext(config),
                new String[] { TLS_V_1_2, TLS_V_1_3 },
                null,
                config.isHostnameVerificationDisabled() ? new NoopHostnameVerifier() : SSLConnectionSocketFactory.getDefaultHostnameVerifier());
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
        config.getMaxTotalConnections().ifPresent(v -> poolingHttpClientConnectionManager.setMaxTotal(v));
        config.getMaxHostConnections().ifPresent(v -> poolingHttpClientConnectionManager.setDefaultMaxPerRoute(v));

        return poolingHttpClientConnectionManager;
    }
}
