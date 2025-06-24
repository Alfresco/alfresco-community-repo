/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.web.scripts.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigException;
import org.springframework.extensions.config.element.ConfigElementAdapter;

/**
 * Custom config element that represents the config data for open search
 * 
 * @author davidc
 */
public class OpenSearchConfigElement extends ConfigElementAdapter
{
    public static final String CONFIG_ELEMENT_ID = "opensearch";

    private ProxyConfig proxy;
    private Set<EngineConfig> engines = new HashSet<EngineConfig>(8, 10f);
    private Map<String, EngineConfig> enginesByProxy = new HashMap<String, EngineConfig>();

    /**
     * Default constructor
     */
    public OpenSearchConfigElement()
    {
        super(CONFIG_ELEMENT_ID);
    }

    /**
     * Constructor
     * 
     * @param name
     *            Name of the element this config element represents
     */
    public OpenSearchConfigElement(String name)
    {
        super(name);
    }

    /**
     * @see ConfigElement#getChildren()
     */
    public List<ConfigElement> getChildren()
    {
        throw new ConfigException("Reading the open search config via the generic interfaces is not supported");
    }

    /**
     * @see ConfigElement#combine(ConfigElement)
     */
    public ConfigElement combine(ConfigElement configElement)
    {
        OpenSearchConfigElement newElement = (OpenSearchConfigElement) configElement;
        OpenSearchConfigElement combinedElement = new OpenSearchConfigElement();

        // add all the plugins from this element
        for (EngineConfig plugin : this.getEngines())
        {
            combinedElement.addEngine(plugin);
        }

        // add all the plugins from the given element
        for (EngineConfig plugin : newElement.getEngines())
        {
            combinedElement.addEngine(plugin);
        }

        // set the proxy configuration
        ProxyConfig proxyConfig = this.getProxy();
        if (proxyConfig != null)
        {
            combinedElement.setProxy(proxyConfig);
        }

        return combinedElement;
    }

    /**
     * Sets the proxy configuration
     * 
     * @param proxyConfig
     *            ProxyConfig
     */
    /* package */ void setProxy(ProxyConfig proxyConfig)
    {
        this.proxy = proxyConfig;
    }

    /**
     * Gets the proxy configuration
     * 
     * @return The proxy configuration
     */
    public ProxyConfig getProxy()
    {
        return this.proxy;
    }

    /**
     * @return Returns a set of the engines
     */
    public Set<EngineConfig> getEngines()
    {
        return this.engines;
    }

    /**
     * @param proxy
     *            name of engine proxy
     * @return associated engine config (or null, if none registered against proxy)
     */
    public EngineConfig getEngine(String proxy)
    {
        return this.enginesByProxy.get(proxy);
    }

    /**
     * Adds an engine
     * 
     * @param engineConfig
     *            A pre-configured engine config object
     */
    /* package */ void addEngine(EngineConfig engineConfig)
    {
        this.engines.add(engineConfig);
        String proxy = engineConfig.getProxy();
        if (proxy != null && proxy.length() > 0)
        {
            this.enginesByProxy.put(proxy, engineConfig);
        }
    }

    /**
     * Inner class representing the configuration of an OpenSearch engine
     * 
     * @author davidc
     */
    public static class EngineConfig
    {
        protected String label;
        protected String labelId;
        protected String proxy;
        protected Map<String, String> urls = new HashMap<String, String>(8, 10f);

        /**
         * Construct
         * 
         * @param label
         *            String
         * @param labelId
         *            String
         */
        public EngineConfig(String label, String labelId)
        {
            if ((label == null || label.length() == 0) && (labelId == null || labelId.length() == 0))
            {
                throw new IllegalArgumentException("'label' or 'label-id' must be specified");
            }
            this.label = label;
            this.labelId = labelId;
        }

        /**
         * Construct
         * 
         * @param label
         *            String
         * @param labelId
         *            String
         * @param proxy
         *            String
         */
        public EngineConfig(String label, String labelId, String proxy)
        {
            this(label, labelId);
            this.proxy = proxy;
        }

        /**
         * @return I18N label id
         */
        public String getLabelId()
        {
            return labelId;
        }

        /**
         * @return label
         */
        public String getLabel()
        {
            return label;
        }

        /**
         * @return proxy
         */
        public String getProxy()
        {
            return proxy;
        }

        /**
         * Gets the urls supported by this engine
         * 
         * @return urls
         */
        public Map<String, String> getUrls()
        {
            return urls;
        }

        /**
         * Adds a url
         * 
         * @param mimetype
         *            mime type
         * @param uri
         *            uri
         */
        /* package */ void addUrl(String mimetype, String uri)
        {
            this.urls.put(mimetype, uri);
        }

    }

    /**
     * Inner class representing the configuration of the OpenSearch proxy
     * 
     * @author davidc
     */
    public static class ProxyConfig
    {
        protected String url;

        /**
         * Construct
         * 
         * @param url
         *            String
         */
        public ProxyConfig(String url)
        {
            if (url == null || url.length() == 0)
            {
                throw new IllegalArgumentException("'url' must be specified");
            }
            this.url = url;
        }

        /**
         * @return url
         */
        public String getUrl()
        {
            return url;
        }
    }

}
