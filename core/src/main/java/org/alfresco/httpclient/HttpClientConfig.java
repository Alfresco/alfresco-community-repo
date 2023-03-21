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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.encryption.AlfrescoKeyStore;
import org.alfresco.encryption.AlfrescoKeyStoreImpl;
import org.alfresco.encryption.KeyResourceLoader;
import org.alfresco.encryption.ssl.SSLEncryptionParameters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HttpClientConfig
{
    private static final String HTTPCLIENT_CONFIG = "httpclient.config.";

    protected static final Log LOGGER = LogFactory.getLog(HttpClientConfig.class);

    private Properties properties;
    private String serviceName;

    private SSLEncryptionParameters sslEncryptionParameters;
    private KeyResourceLoader keyResourceLoader;

    private AlfrescoKeyStore keyStore;
    private AlfrescoKeyStore trustStore;

    private Map<String, String> config;

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

    public void init()
    {
        this.keyStore = new AlfrescoKeyStoreImpl(sslEncryptionParameters.getKeyStoreParameters(),  keyResourceLoader);
        this.trustStore = new AlfrescoKeyStoreImpl(sslEncryptionParameters.getTrustStoreParameters(), keyResourceLoader);

        config = retrieveConfig(serviceName);
        checkUnsupportedProperties(config);
    }

    /**
     * Method used for retrieving HttpClient config from Global Properties
     * @param serviceName name of used service
     * @return map of properties
     */
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

    private void checkUnsupportedProperties(Map<String, String> config)
    {
        config.keySet().stream()
              .filter(propertyName -> !HttpClientProperties.isPropertyNameSupported(propertyName))
              .forEach(propertyName -> LOGGER.warn(String.format("For service [%s], an unsupported property [%s] is set", serviceName, propertyName)));
    }

    private Integer getIntegerProperty(String propertyName)
    {
        String keyValue = Optional.ofNullable(config.get(propertyName)).orElseThrow(() -> {
            String msg = String.format("Required property: '%s' is empty.", propertyName);
            LOGGER.error(msg);
            throw new HttpClientException(msg);
        });
            return Integer.parseInt(keyValue);
    }

    private Boolean getBooleanProperty(String propertyName)
    {
        String keyValue = Optional.ofNullable(config.get(propertyName)).orElseThrow(() -> {
            String msg = String.format("Required property: '%s' is empty.", propertyName);
            LOGGER.error(msg);
            throw new HttpClientException(msg);
        });
        return Boolean.parseBoolean(keyValue);
    }

    public Integer getConnectionTimeout()
    {
        return getIntegerProperty(HttpClientProperties.CONNECTION_REQUEST_TIMEOUT.propertyName);
    }

    public Integer getSocketTimeout()
    {
        return getIntegerProperty(HttpClientProperties.SOCKET_TIMEOUT.propertyName);
    }

    public Integer getConnectionRequestTimeout()
    {
        return getIntegerProperty(HttpClientProperties.CONNECTION_REQUEST_TIMEOUT.propertyName);
    }

    public Integer getMaxTotalConnections()
    {
        return getIntegerProperty(HttpClientProperties.MAX_TOTAL_CONNECTIONS.propertyName);
    }

    public Integer getMaxHostConnections()
    {
        return getIntegerProperty(HttpClientProperties.MAX_HOST_CONNECTIONS.propertyName);
    }

    public Boolean isMTLSEnabled()
    {
        return getBooleanProperty(HttpClientProperties.MTLS_ENABLED.propertyName);
    }

    private enum HttpClientProperties
    {
        CONNECTION_TIMEOUT("connectionTimeout"),
        SOCKET_TIMEOUT("socketTimeout"),
        CONNECTION_REQUEST_TIMEOUT("connectionRequestTimeout"),
        MAX_TOTAL_CONNECTIONS("maxTotalConnections"),
        MAX_HOST_CONNECTIONS("maxHostConnections"),
        MTLS_ENABLED("mTLSEnabled");

        private final String propertyName;

        HttpClientProperties(String propertyName)
        {
            this.propertyName = propertyName;
        }

        public String getPropertyName() {
            return this.propertyName;
        }

        private static final List<String> supportedProperties = new ArrayList<>();

        static {
            for (HttpClientProperties property : HttpClientProperties.values()) {
                supportedProperties.add(property.getPropertyName());
            }
        }

        public static boolean isPropertyNameSupported(String propertyName) {
            return supportedProperties.contains(propertyName);
        }
    }
}
