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

    private SSLEncryptionParameters sslEncryptionParameters;
    private KeyResourceLoader keyResourceLoader;
    private Boolean mTLSEnabled;

    private int maxTotalConnections = 40;
    private int maxHostConnections = 40;
    private Integer socketTimeout = null;
    private int connectionTimeout = 0;

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

    public void setMaxTotalConnections(int maxTotalConnections)
    {
        this.maxTotalConnections = maxTotalConnections;
    }

    public void setMaxHostConnections(int maxHostConnections)
    {
        this.maxHostConnections = maxHostConnections;
    }

    public void setSocketTimeout(Integer socketTimeout)
    {
        this.socketTimeout = socketTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout)
    {
        this.connectionTimeout = connectionTimeout;
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
    };

    public CloseableHttpClient createHttpClient()
    {
        HttpClientBuilder clientBuilder = HttpClients.custom();

        if(ismTLSEnabled())
        {
            clientBuilder.setSSLContext(createSSLContext());
        }

        clientBuilder.setMaxConnTotal(maxTotalConnections);
        clientBuilder.setMaxConnPerRoute(maxHostConnections);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectionTimeout)
                .setSocketTimeout(socketTimeout)
                .build();
        clientBuilder.setDefaultRequestConfig(requestConfig);


        return clientBuilder.build();
    };
    
}
