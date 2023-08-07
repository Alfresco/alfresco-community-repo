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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
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

        config = retrieveConfig();
        checkUnsupportedProperties(config);
    }

    /**
     * Method used for retrieving HttpClient config from Global Properties
     * that can also have values provided/overridden through System Properties
     *
     * @return map of properties
     */
    private Map<String, String> retrieveConfig()
    {
        Map<String, String> resultProperties = getHttpClientPropertiesForService(properties);
        Map<String, String> systemProperties = getHttpClientPropertiesForService(System.getProperties());

        systemProperties.forEach((k, v) -> resultProperties.put(k, v)); //Override/Add to Global Properties results with values from System Properties

        return resultProperties;
    }

    private Map<String, String> getHttpClientPropertiesForService(Properties properties) {
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
              .filter(propertyName -> !HttpClientPropertiesEnum.isPropertyNameSupported(propertyName))
              .forEach(propertyName -> LOGGER.warn(String.format("For service [%s], an unsupported property [%s] is set", serviceName, propertyName)));
    }

    private Optional<Integer> getIntegerProperty(HttpClientPropertiesEnum property)
    {
        Optional<String> optionalProperty = extractValueFromConfig(property);

        return optionalProperty.isPresent() ? Optional.of(Integer.parseInt(optionalProperty.get())) : Optional.empty();
    }

    private Optional<Boolean> getBooleanProperty(HttpClientPropertiesEnum property)
    {
        Optional<String> optionalProperty = extractValueFromConfig(property);

        return optionalProperty.isPresent() ? Optional.of(Boolean.parseBoolean(optionalProperty.get())) : Optional.empty();
    }

    private Optional<String> extractValueFromConfig(HttpClientPropertiesEnum property)
    {
        return Optional.ofNullable(config.get(property.name));
    }

    public Optional<Integer> getConnectionTimeout()
    {
        return getIntegerProperty(HttpClientPropertiesEnum.CONNECTION_REQUEST_TIMEOUT);
    }

    public Optional<Integer> getSocketTimeout()
    {
        return getIntegerProperty(HttpClientPropertiesEnum.SOCKET_TIMEOUT);
    }

    public Optional<Integer> getConnectionRequestTimeout()
    {
        return getIntegerProperty(HttpClientPropertiesEnum.CONNECTION_REQUEST_TIMEOUT);
    }

    public Optional<Integer> getMaxTotalConnections()
    {
        return getIntegerProperty(HttpClientPropertiesEnum.MAX_TOTAL_CONNECTIONS);
    }

    public Optional<Integer> getMaxHostConnections()
    {
        return getIntegerProperty(HttpClientPropertiesEnum.MAX_HOST_CONNECTIONS);
    }

    public boolean isMTLSEnabled()
    {
        return getBooleanProperty(HttpClientPropertiesEnum.MTLS_ENABLED).orElse(Boolean.FALSE);
    }

    public boolean isHostnameVerificationDisabled()
    {
        return getBooleanProperty(HttpClientPropertiesEnum.HOSTNAME_VERIFICATION_DISABLED).orElse(Boolean.FALSE);
    }

    private enum HttpClientPropertiesEnum
    {
        CONNECTION_TIMEOUT("connectionTimeout"),
        SOCKET_TIMEOUT("socketTimeout"),
        CONNECTION_REQUEST_TIMEOUT("connectionRequestTimeout"),
        MAX_TOTAL_CONNECTIONS("maxTotalConnections"),
        MAX_HOST_CONNECTIONS("maxHostConnections"),
        HOSTNAME_VERIFICATION_DISABLED("hostnameVerificationDisabled"),
        MTLS_ENABLED("mTLSEnabled");

        private final String name;

        HttpClientPropertiesEnum(String propertyName)
        {
            this.name = propertyName;
        }

        private static final List<String> supportedProperties = new ArrayList<>();

        static {
            for (HttpClientPropertiesEnum property : HttpClientPropertiesEnum.values()) {
                supportedProperties.add(property.name);
            }
        }

        public static boolean isPropertyNameSupported(String propertyName) {
            return supportedProperties.contains(propertyName);
        }
    }
}
