/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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

    private Set<EngineConfig> engines = new HashSet<EngineConfig>(8, 10f);

    
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

        return combinedElement;
    }

    /**
     * @return Returns a set of the engines
     */
    public Set<EngineConfig> getEngines()
    {
        return this.engines;
    }

    /**
     * Adds an engine
     * 
     * @param pluginConfig  A pre-configured engine config object
     */
    /*package*/ void addEngine(EngineConfig engineConfig)
    {
        this.engines.add(engineConfig);
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
        
        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            StringBuilder buffer = new StringBuilder(super.toString());
            buffer.append(" {label=").append(this.label);
            buffer.append(" labelId=").append(this.labelId).append(")");
            return buffer.toString();
        }        
    }
    
}
