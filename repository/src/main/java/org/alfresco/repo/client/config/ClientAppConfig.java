/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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

package org.alfresco.repo.client.config;

import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.UrlUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * This class picks up all the loaded properties passed to it and uses a naming
 * convention to isolate the client's name and the related values.
 * <p>
 * The naming convention must confirm to the following:
 * <p>
 * <i>repo.client-app.{@literal <client-name>.<propertyName>}</i>
 * <p>
 * Also, the client-name or property name ({@literal <propertyName>}) must not contain a dot {@literal ('.')}
 * <p>
 * Note also, that any property without a value is ignored and the client will not be registered
 * if all the properties of that client have no values.
 * <p>
 * So, if a new client (e.g. MyClientName) is required to send a shared-link email and the service or the API requires,
 * for example, <i>sharedLinkBaseUrl</i> and <i>templateAssetsUrl</i> properties, then the following
 * needs to be put into a properties file.
 * <ul>
 * <li>{@code repo.client-app.MyClientName.sharedLinkBaseUrl=http://localhost:8080/MyClientName/s}</li>
 * <li>{@code repo.client-app.MyClientName.templateAssetsUrl=http://localhost:8080/MyClientName/assets}</li>
 * </ul>
 * The default property file is <b>alfresco/client/config/repo-clients-apps.properties</b> which
 * could be overridden (or add new clients) by <b>alfresco-global</b> properties file.
 * <p>
 * <b>Note:</b> The {@literal <clientName>Url} is a special property which can be used as a placeholder.
 * For example,
 * <ul>
 * <li>{@code repo.client-app.MyClientName.MyClientNameUrl=${repoBaseUrl}/entrypoint}</li>
 * <li>{@code repo.client-app.MyClientName.somePropName=${MyClientNameUrl}/some-page}</li>
 * </ul>
 * </p>
 *
 * @author Jamal Kaabi-Mofrad
 */
public class ClientAppConfig extends AbstractLifecycleBean
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientAppConfig.class);

    public static final String PREFIX = "repo.client-app.";
    public static final String PROP_TEMPLATE_ASSETS_URL = "templateAssetsUrl";

    public static final String SHARE_PLACEHOLDER = "${shareUrl}";

    private Properties defaultProperties;
    private Properties globalProperties;

    private final ConcurrentMap<String, ClientApp> clients = new ConcurrentHashMap<>();

    public ClientAppConfig()
    {
    }

    public void setDefaultProperties(Properties defaultProperties)
    {
        this.defaultProperties = defaultProperties;
    }

    public void setGlobalProperties(Properties globalProperties)
    {
        this.globalProperties = globalProperties;
    }

    public void init()
    {
        PropertyCheck.mandatory(this, "defaultProperties", defaultProperties);
        PropertyCheck.mandatory(this, "globalProperties", globalProperties);
    }

    /**
     * Returns an unmodifiable view of the clients map. Never null.
     */
    public Map<String, ClientApp> getClients()
    {
        return Collections.unmodifiableMap(clients);
    }

    /**
     * Returns the named client or null if no client exists with the given name.
     *
     * @param name the name of the client to retrieve
     */
    public ClientApp getClient(String name)
    {
        return clients.get(name);
    }

    /**
     * Checks whether a client with the given name exists or not.
     *
     * @param name the name of the client to check
     * @return true if the client with the given name exists; false otherwise
     */
    public boolean exists(String name)
    {
        return clients.containsKey(name);
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        Map<String, String> mergedProperties = getAndMergeProperties();

        Set<String> clientsNames = new HashSet<>();
        Set<String> propsNames = new HashSet<>();
        processPropertyKeys(mergedProperties, clientsNames, propsNames);
        clients.putAll(processClients(clientsNames, propsNames, mergedProperties));

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("All bootstrapped repo clients apps: " + clients);
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // nothing to do
    }

    /**
     * Processes the property's key and extracts the clients' names.
     *
     * @param allProps the merged properties
     * @param  clientsNames a set of strings which will be populated with client names
     * @param  propsNames a set of strings which will be populated with properties names (i.e.the property after the client name)
     */
    protected void processPropertyKeys(Map<String, String> allProps, Set<String> clientsNames, Set<String> propsNames)
    {
        for (String key : allProps.keySet())
        {
            String propKey = key;
            if (propKey.startsWith(PREFIX))
            {
                propKey = propKey.substring(PREFIX.length());
                // Find the client name
                int clientNameControlDot = propKey.indexOf('.');
                if (clientNameControlDot < 1)
                {
                    logMalformedPropertyKey(key);
                    continue;
                }

                int propNameLength = (propKey.length() - clientNameControlDot) - 1; // Length of characters between dots
                if (propNameLength < 1)
                {
                    logMalformedPropertyKey(key);
                    continue;
                }
                String clientName = propKey.substring(0, clientNameControlDot);
                String propName = propKey.substring((clientNameControlDot + 1));
                // the property name (the property after the client name) must not contain a '.'
                if (propName.indexOf('.') == -1)
                {
                    clientsNames.add(clientName);
                    propsNames.add(propName);
                }
                else
                {
                    logMalformedPropertyKey(key, "The property name " + propName + " must not contain a '.'");
                }
            }
            else
            {
                logMalformedPropertyKey(propKey);
            }
        }
    }

    /**
     * Processes the given properties and if the properties' values are valid, creates
     * a map of {@code ClientApp} with the client's name as the key.
     *
     * @param clientsNames the processed clients' names
     * @param propsNames the processed properties names
     * @param allProps     the merged properties
     * @return a map of {@code ClientApp} with the client's name as the key.
     */
    protected Map<String, ClientApp> processClients(Set<String> clientsNames, Set<String> propsNames, Map<String, String> allProps)
    {
        Map<String, ClientApp> clientApps = new HashMap<>(clientsNames.size());
        for (String clientName : clientsNames)
        {
            Map<String, String> config = new HashMap<>();
            String templateAssetsUrl = null;
            for (String propName : propsNames)
            {
                String propKey = getPropertyKey(clientName, propName);
                if (!allProps.containsKey(propKey))
                {
                    // if the constructed property key does not exist, skip this iteration.
                    continue;
                }

                String propValue = allProps.get(propKey);

                if (StringUtils.isEmpty(propValue))
                {
                    logInvalidPropertyValue(propKey, propValue);
                    continue;
                }

                if (PROP_TEMPLATE_ASSETS_URL.equals(propName))
                {
                    templateAssetsUrl = propValue;
                }
                else
                {
                    config.put(propName, propValue);
                }
            }
            if (StringUtils.isEmpty(templateAssetsUrl) && config.isEmpty())
            {
                LOGGER.warn("Client-app [" + clientName + "] can not be registered as it needs at least one property with a valid value.");
                continue;
            }
            // As the required values are valid, create the client data
            ClientApp client = new ClientApp(clientName, templateAssetsUrl, config);
            clientApps.put(clientName, client);
        }
        return clientApps;
    }

    /**
     * Converts and merges the given Java properties into a {@code java.util.Map}.
     */
    protected Map<String, String> getAndMergeProperties()
    {
        Map<String, String> allProperties = new HashMap<>();
        for (String propKey : defaultProperties.stringPropertyNames())
        {
            allProperties.put(propKey, defaultProperties.getProperty(propKey));
        }

        // Add new clients or override the default values from other properties files
        for (String propKey : globalProperties.stringPropertyNames())
        {
            if (propKey.startsWith(PREFIX))
            {
                String value = globalProperties.getProperty(propKey);
                // before overriding the key, validate the property value
                if (StringUtils.isEmpty(value))
                {
                    logInvalidPropertyValue(propKey, value);
                    continue;
                }
                allProperties.put(propKey, value);
            }
        }

        return allProperties;
    }

    private void logMalformedPropertyKey(String propName, String reason)
    {
        reason = (StringUtils.isBlank(reason)) ? "" : " " + reason;
        LOGGER.warn("Ignoring client app config (malformed property key) [" + propName + "]." + reason);
    }

    private void logMalformedPropertyKey(String propName)
    {
        logMalformedPropertyKey(propName, null);
    }

    private void logInvalidPropertyValue(String propName, String propValue)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Ignoring client app config (invalid value) [" + propValue + "] for the property:" + propName);
        }
    }

    private String getPropertyKey(String clientName, String clientProp)
    {
        return PREFIX + clientName + '.' + clientProp;
    }

    public static class ClientApp
    {
        private static final Logger LOGGER = LoggerFactory.getLogger(ClientApp.class);
        private final String name;
        private final String clientUrlPropKey;
        private final Pattern clientUrlPlaceholderPattern;
        private final String templateAssetsUrl;
        private final Map<String, String> properties;

        public ClientApp(String name, String templateAssetsUrl, Map<String, String> properties)
        {
            this.name = name;
            this.clientUrlPropKey = name + "Url"; // E.g. 'workspaceUrl' in 'repo.client-app.workspace.workspaceUrl'
            this.clientUrlPlaceholderPattern = Pattern.compile("\\$\\{" + clientUrlPropKey + '}'); // E.g. ${workspaceUrl}
            this.templateAssetsUrl = templateAssetsUrl;
            this.properties = new HashMap<>(properties);
        }

        public String getName()
        {
            return name;
        }

        public String getClientUrlPropKey()
        {
            return clientUrlPropKey;
        }

        public String getClientUrl()
        {
            String url = properties.get(getClientUrlPropKey());
            if (StringUtils.isEmpty(url) && "share".equals(name))
            {
                return SHARE_PLACEHOLDER;
            }
            return url;
        }

        /**
         * Resolves the client URL that has one of the following defined URL placeholders:
         * <ul>
         *     <li>{@literal ${repoBaseUrl}}</li>
         *     <li>{@literal ${shareUrl}}</li>
         * </ul>
         *
         * @param sysAdminParams SysAdminParams object to retrieve configurable system parameters
         * @return the resolved property value if applicable
         */
        public String getResolvedClientUrl(SysAdminParams sysAdminParams)
        {
            String resolvedUrl;
            String clientUrl = getClientUrl();
            if (!StringUtils.isEmpty(clientUrl) && clientUrl.contains(SHARE_PLACEHOLDER))
            {
                resolvedUrl = UrlUtil.replaceShareUrlPlaceholder(clientUrl, sysAdminParams);
            }
            else
            {
                resolvedUrl = UrlUtil.replaceRepoBaseUrlPlaceholder(clientUrl, sysAdminParams);
            }

            LOGGER.debug("Resolved clientUrl [" + clientUrl + "] to [" + resolvedUrl + "] for the client: " + name);
            return resolvedUrl;
        }

        public Pattern getClientUrlPlaceholderPattern()
        {
            return clientUrlPlaceholderPattern;
        }

        public String getTemplateAssetsUrl()
        {
            return templateAssetsUrl;
        }

        /**
         * Resolves the template assets URL that has the following defined URL placeholder:
         * <ul>
         *     <li>{@literal ${<clientName>Url}}</li>
         * </ul>
         *
         * @param sysAdminParams SysAdminParams object to retrieve configurable system parameters
         * @return the resolved property value if applicable
         */
        public String getResolvedTemplateAssetsUrl(SysAdminParams sysAdminParams)
        {
            String resolvedUrl = UrlUtil.replaceUrlPlaceholder(getClientUrlPlaceholderPattern(), getTemplateAssetsUrl(),
                                                               getResolvedClientUrl(sysAdminParams));

            LOGGER.debug("Resolved template assets [" + getTemplateAssetsUrl() + "] URL to [" + resolvedUrl
                                     + "] for the client: " + name);
            return resolvedUrl;
        }

        public Map<String, String> getProperties()
        {
            return Collections.unmodifiableMap(properties);
        }

        public String getProperty(String propName)
        {
            return properties.get(propName);
        }

        /**
         * Resolves the property that has the following defined URL placeholder:
         * <ul>
         *     <li>{@literal ${<clientName>Url}}</li>
         * </ul>
         *
         * @param propName       the property name to search for
         * @param sysAdminParams SysAdminParams object to retrieve configurable system parameters
         * @return the resolved property value if applicable
         */
        public String getResolvedProperty(String propName, SysAdminParams sysAdminParams)
        {
            String property = getProperty(propName);
            String resolvedProp = UrlUtil.replaceUrlPlaceholder(getClientUrlPlaceholderPattern(), property,
                                                                getResolvedClientUrl(sysAdminParams));

            LOGGER.debug("Resolved [" + property + "] URL to [" + resolvedProp + "] for the [" + propName
                                     + "] property of the client: " + name);
            return resolvedProp;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof ClientApp))
            {
                return false;
            }

            ClientApp clientApp = (ClientApp) o;
            return getName().equals(clientApp.getName());
        }

        @Override
        public int hashCode()
        {
            return getName().hashCode();
        }

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder(250);
            sb.append("ClientApp [name=").append(name)
                        .append(", clientUrlPropKey=").append(clientUrlPropKey)
                        .append(", clientUrlPlaceholderPattern=").append(clientUrlPlaceholderPattern.pattern())
                        .append(", templateAssetsUrl=").append(templateAssetsUrl)
                        .append(", properties=").append(properties)
                        .append(']');
            return sb.toString();
        }
    }
}
