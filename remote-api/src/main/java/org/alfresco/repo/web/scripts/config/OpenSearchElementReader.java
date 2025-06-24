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

import java.util.Iterator;

import org.dom4j.Element;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigException;
import org.springframework.extensions.config.xml.elementreader.ConfigElementReader;

import org.alfresco.repo.web.scripts.config.OpenSearchConfigElement.EngineConfig;
import org.alfresco.repo.web.scripts.config.OpenSearchConfigElement.ProxyConfig;

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
    public static final String ELEMENT_PROXY = "proxy";
    public static final String ATTR_TYPE = "type";
    public static final String ATTR_LABEL = "label";
    public static final String ATTR_LABEL_ID = "label-id";
    public static final String ATTR_PROXY = "proxy";

    /**
     * @see org.springframework.extensions.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
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
                while (engines.hasNext())
                {
                    // construct engine
                    Element engineElem = engines.next();
                    String label = engineElem.attributeValue(ATTR_LABEL);
                    String labelId = engineElem.attributeValue(ATTR_LABEL_ID);
                    String proxy = engineElem.attributeValue(ATTR_PROXY);
                    EngineConfig engineCfg = new EngineConfig(label, labelId, proxy);

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

            // extract proxy configuration
            String url = null;
            Element proxyElem = element.element(ELEMENT_PROXY);
            if (proxyElem != null)
            {
                Element urlElem = proxyElem.element(ELEMENT_URL);
                if (urlElem != null)
                {
                    url = urlElem.getTextTrim();
                    ProxyConfig proxyCfg = new ProxyConfig(url);
                    configElement.setProxy(proxyCfg);
                }
            }
        }

        return configElement;
    }
}
