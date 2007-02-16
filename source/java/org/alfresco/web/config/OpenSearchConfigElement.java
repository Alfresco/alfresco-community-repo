/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.element.ConfigElementAdapter;


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
     * @param name  Name of the element this config element represents
     */
    public OpenSearchConfigElement(String name)
    {
        super(name);
    }

    /**
     * @see org.alfresco.config.ConfigElement#getChildren()
     */
    public List<ConfigElement> getChildren()
    {
        throw new ConfigException("Reading the open search config via the generic interfaces is not supported");
    }

    /**
     * @see org.alfresco.config.ConfigElement#combine(org.alfresco.config.ConfigElement)
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
     */
    /*package*/ void setProxy(ProxyConfig proxyConfig)
    {
        this.proxy = proxyConfig;
    }
    
    /**
     * Gets the proxy configuration
     * 
     * @return
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
     * @param proxy  name of engine proxy
     * @return  associated engine config (or null, if none registered against proxy)
     */
    public EngineConfig getEngine(String proxy)
    {
       return this.enginesByProxy.get(proxy); 
    }

    /**
     * Adds an engine
     * 
     * @param pluginConfig  A pre-configured engine config object
     */
    /*package*/ void addEngine(EngineConfig engineConfig)
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
         * @param labelId
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
         * @param labelId
         * @param proxy
         */
        public EngineConfig(String label, String labelId, String proxy)
        {
            this(label, labelId);
            this.proxy = proxy;
        }

        /**
         * @return  I18N label id
         */
        public String getLabelId()
        {
            return labelId;
        }

        /**
         * @return  label
         */
        public String getLabel()
        {
            return label;
        }

        /**
         * @return  proxy
         */
        public String getProxy()
        {
            return proxy;
        }
        
        /**
         * Gets the urls supported by this engine
         * 
         * @return  urls
         */
        public Map<String, String> getUrls()
        {
            return urls;
        }

        /**
         * Adds a url
         * 
         * @param pluginConfig  A pre-configured plugin config object
         */
        /*package*/ void addUrl(String mimetype, String uri)
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
         * @return  url
         */
        public String getUrl()
        {
            return url;
        }
    }
    
}
