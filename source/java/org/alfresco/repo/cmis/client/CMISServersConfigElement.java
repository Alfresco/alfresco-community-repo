/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.client;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.element.ConfigElementAdapter;

/**
 * CMIS server configuration element.
 */
public class CMISServersConfigElement extends ConfigElementAdapter
{
    private static final long serialVersionUID = 1L;
    private static final String CONFIG_ELEMENT_ID = "cmis-servers";

    private final Map<String, CMISServer> serverDefintions = new HashMap<String, CMISServer>();

    @SuppressWarnings("unchecked")
    public CMISServersConfigElement(Element element)
    {
        super(CONFIG_ELEMENT_ID);

        for (Element childElement : ((List<Element>) element.elements("server")))
        {
            Map<String, String> parameters = new LinkedHashMap<String, String>();
            String name = null;
            String description = null;

            for (Element parameterElement : ((List<Element>) childElement.elements("parameter")))
            {
                String key = parameterElement.attributeValue("key");
                String value = parameterElement.attributeValue("value");
                if (key != null && value != null)
                {
                    if (key.equals("name"))
                    {
                        name = value;
                    } else if (key.equals("description"))
                    {
                        description = value;
                    } else
                    {
                        parameters.put(key, value);
                    }
                }
            }

            if (name != null)
            {
                serverDefintions.put(name, new CMISServerImpl(name, description, parameters));
            }
        }
    }

    @Override
    public ConfigElement combine(ConfigElement configElement)
    {
        if (configElement instanceof CMISServersConfigElement)
        {
            serverDefintions.putAll(((CMISServersConfigElement) configElement).getServerDefinitions());
        }

        return this;
    }

    public Map<String, CMISServer> getServerDefinitions()
    {
        return serverDefintions;
    }
}
