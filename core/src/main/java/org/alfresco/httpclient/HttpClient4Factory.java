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

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;


public class HttpClient4Factory
{
    protected static final String TLS_PROTOCOL = "TLS";
    protected static final String HTTPCLIENT_CONFIG = "httpclient.config.";

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
        HttpClientBuilder clientBuilder = HttpClients.custom();

        if(Boolean.parseBoolean(config.getConfig().get("mTLSEnabled")))
        {
            clientBuilder.setSSLContext(createSSLContext(config));
        }

        clientBuilder.setMaxConnTotal(Integer.parseInt(config.getConfig().get("maxTotalConnections")));
        clientBuilder.setMaxConnPerRoute(Integer.parseInt(config.getConfig().get("maxHostConnections")));

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Integer.parseInt(config.getConfig().get("connectionTimeout")))
                .setSocketTimeout(Integer.parseInt(config.getConfig().get("socketTimeout")))
                .build();
        clientBuilder.setDefaultRequestConfig(requestConfig);

        return clientBuilder.build();
    }

//    private String getPropertyValue(String propertyName, String defaultValue)
//    {
//        return Optional.ofNullable(config.get(propertyName)).orElse(defaultValue);
//    }

}
