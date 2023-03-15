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

import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.alfresco.encryption.AlfrescoKeyStore;
import org.alfresco.encryption.AlfrescoKeyStoreImpl;
import org.alfresco.encryption.KeyResourceLoader;
import org.alfresco.encryption.ssl.SSLEncryptionParameters;

public class HttpClientConfig
{
    private static final String HTTPCLIENT_CONFIG = "httpclient.config.";

    private Properties properties;
    private String serviceName;

    private SSLEncryptionParameters sslEncryptionParameters;
    private KeyResourceLoader keyResourceLoader;

    private AlfrescoKeyStore keyStore;
    private AlfrescoKeyStore trustStore;

    private Map<String, String> config;

    public void init()
    {
        this.keyStore = new AlfrescoKeyStoreImpl(sslEncryptionParameters.getKeyStoreParameters(),  keyResourceLoader);
        this.trustStore = new AlfrescoKeyStoreImpl(sslEncryptionParameters.getTrustStoreParameters(), keyResourceLoader);

        config = retrieveConfig(serviceName);
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

    /**
     * @return HttpClientConfig instance with default values for testing purpose.
     */
    public static HttpClientConfig getNonMtlsDefaultInstance()
    {
        HttpClientConfig httpClientConfig = new HttpClientConfig();
        httpClientConfig.getConfig().put("mTLSEnabled", "false");
        httpClientConfig.getConfig().put("maxTotalConnections", "40");
        httpClientConfig.getConfig().put("maxHostConnections", "40");
        httpClientConfig.getConfig().put("socketTimeout", "0");
        httpClientConfig.getConfig().put("connectionTimeout", "0");

        return new HttpClientConfig();
    }

    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    public void setServiceName(String serviceName)
    {
        this.serviceName = serviceName;
    }

    public void setSslEncryptionParameters(SSLEncryptionParameters sslEncryptionParameters)
    {
        this.sslEncryptionParameters = sslEncryptionParameters;
    }

    public void setKeyResourceLoader(KeyResourceLoader keyResourceLoader)
    {
        this.keyResourceLoader = keyResourceLoader;
    }

    public AlfrescoKeyStore getKeyStore()
    {
        return keyStore;
    }

    public AlfrescoKeyStore getTrustStore()
    {
        return trustStore;
    }

    public Map<String, String> getConfig()
    {
        return config;
    }

    public Integer getIntegerProperty(String propertyName)
    {
        return Integer.parseInt(config.get(propertyName));
    }

    public Boolean getBooleanProperty(String propertyName)
    {
        return Boolean.parseBoolean(config.get(propertyName));
    }
}
