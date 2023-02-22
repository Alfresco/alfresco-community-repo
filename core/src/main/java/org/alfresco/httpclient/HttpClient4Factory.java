/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2023 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.httpclient;


import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.alfresco.encryption.AlfrescoKeyStore;
import org.alfresco.encryption.AlfrescoKeyStoreImpl;
import org.alfresco.encryption.KeyResourceLoader;
import org.alfresco.encryption.ssl.SSLEncryptionParameters;
import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;


public class HttpClient4Factory
{
    protected static final String TLS_PROTOCOL = "TLS";
    protected static final String HTTPCLIENT_CONFIG = "httpclient.config.";

    private SSLEncryptionParameters sslEncryptionParameters;
    private KeyResourceLoader keyResourceLoader;
    private Properties properties;

    private Boolean mTLSEnabled;

    private AlfrescoKeyStore keyStore;
    private AlfrescoKeyStore trustStore;

    public void init()
    {
        this.keyStore = new AlfrescoKeyStoreImpl(sslEncryptionParameters.getKeyStoreParameters(),  keyResourceLoader);
        this.trustStore = new AlfrescoKeyStoreImpl(sslEncryptionParameters.getTrustStoreParameters(), keyResourceLoader);
    }

    public void setSslEncryptionParameters(SSLEncryptionParameters sslEncryptionParameters)
    {
        this.sslEncryptionParameters = sslEncryptionParameters;
    }

    public void setKeyResourceLoader(KeyResourceLoader keyResourceLoader)
    {
        this.keyResourceLoader = keyResourceLoader;
    }

    /**
     * The Alfresco global properties.
     */
    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    public void setmTLSEnabled(String mTLSEnabled)
    {
        System.out.println(mTLSEnabled);
        this.mTLSEnabled = Boolean.parseBoolean(mTLSEnabled);
    }

    public Boolean ismTLSEnabled()
    {
        return mTLSEnabled;
    }

    private SSLContext createSSLContext()
    {
        KeyManager[] keyManagers = keyStore.createKeyManagers();
        TrustManager[] trustManagers = trustStore.createTrustManagers();

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

    public CloseableHttpClient createHttpClient(String serviceName)
    {
        HttpClientBuilder clientBuilder = HttpClients.custom();

        HttpClientConfig config = new HttpClientConfig(serviceName);

        if(config.mTLSEnabled)
        {
            clientBuilder.setSSLContext(createSSLContext());
        }

        clientBuilder.setMaxConnTotal(config.maxTotalConnections);
        clientBuilder.setMaxConnPerRoute(config.maxHostConnections);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(config.connectionTimeout)
                .setSocketTimeout(config.socketTimeout)
                .build();
        clientBuilder.setDefaultRequestConfig(requestConfig);

        return clientBuilder.build();
    }

    class HttpClientConfig {
        Boolean mTLSEnabled;
        int maxTotalConnections;
        int maxHostConnections;
        Integer socketTimeout;
        int connectionTimeout;

        private final Map<String, String> config;

        HttpClientConfig(String serviceName) {
            config = retrieveConfig(serviceName);

            maxTotalConnections = Integer.parseInt(getPropertyValue("maxTotalConnections", "40"));
            maxHostConnections = Integer.parseInt(getPropertyValue("maxHostConnections", "40"));
            socketTimeout = Integer.parseInt(getPropertyValue("socketTimeout", "0"));
            connectionTimeout = Integer.parseInt(getPropertyValue("connectionTimeout", "0"));
            mTLSEnabled = Boolean.parseBoolean(getPropertyValue("mTLSEnabled", "false"));
        }

        private Map<String, String> retrieveConfig(String serviceName)
        {
            return properties.keySet().stream()
                             .filter(key -> key instanceof String)
                             .map(Object::toString)
                             .filter(key -> key.startsWith(HTTPCLIENT_CONFIG + serviceName))
                             .collect(Collectors.toMap(
                                     key -> key.replace(HTTPCLIENT_CONFIG + serviceName + ".", ""),
                                     key -> properties.getProperty(key, null)));
        }

        private String getPropertyValue(String propertyName, String defaultValue)
        {
            return Optional.ofNullable(config.get(propertyName)).orElse(defaultValue);
        }
    }
}
