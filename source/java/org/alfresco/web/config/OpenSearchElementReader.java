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

import java.util.Iterator;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.xml.elementreader.ConfigElementReader;
import org.alfresco.web.config.OpenSearchConfigElement.EngineConfig;
import org.dom4j.Element;


/**
 * Custom element reader to parse config for the open search
 * 
 * @author davidc
 */
public class OpenSearchElementReader implements ConfigElementReader
{
    public static final String ELEMENT_OPENSEARCH = "opensearch";
    public static final String ELEMENT_ENGINES = "engines";
    public static final String ELEMENT_ENGINE = "engine";
    public static final String ELEMENT_URL = "url";
    public static final String ATTR_TYPE = "type";
    public static final String ATTR_LABEL = "label";
    public static final String ATTR_LABEL_ID = "label-id";

    
    /**
     * @see org.alfresco.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
     */
    @SuppressWarnings("unchecked")
    public ConfigElement parse(Element element)
    {
        OpenSearchConfigElement configElement = null;

        if (element != null)
        {
            String elementName = element.getName();
            if (elementName.equals(ELEMENT_OPENSEARCH) == false)
            {
                throw new ConfigException("OpenSearchElementReader can only parse " + ELEMENT_OPENSEARCH
                        + "elements, the element passed was '" + elementName + "'");
            }

            // go through the registered engines
            configElement = new OpenSearchConfigElement();
            Element pluginsElem = element.element(ELEMENT_ENGINES);
            if (pluginsElem != null)
            {
                Iterator<Element> engines = pluginsElem.elementIterator(ELEMENT_ENGINE);
                while(engines.hasNext())
                {
                    // construct engine
                    Element engineElem = engines.next();
                    String label = engineElem.attributeValue(ATTR_LABEL);
                    String labelId = engineElem.attributeValue(ATTR_LABEL_ID);
                    EngineConfig engineCfg = new EngineConfig(label, labelId);
                
                    // construct urls for engine
                    Iterator<Element> urlsConfig = engineElem.elementIterator(ELEMENT_URL);
                    while (urlsConfig.hasNext())
                    {
                        Element urlConfig = urlsConfig.next();
                        String type = urlConfig.attributeValue(ATTR_TYPE);
                        String url = urlConfig.getTextTrim();
                        engineCfg.addUrl(type, url);
                    }

                    // register engine config
                    configElement.addEngine(engineCfg);
                }
            }
        }

        return configElement;
    }
}
