/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.xml.elementreader.ConfigElementReader;
import org.alfresco.web.config.DefaultControlsConfigElement.ControlParam;
import org.dom4j.Element;

/**
 * This class is a custom element reader to parse the config file for
 * &lt;default-controls&gt; elements.
 * 
 * @author Neil McErlean.
 */
public class DefaultControlsElementReader implements ConfigElementReader
{
    public static final String ELEMENT_DEFAULT_CONTROLS = "default-controls";
    public static final String ELEMENT_CONTROL_PARAM = "control-param";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_TEMPLATE = "template";

    /**
     * @see org.alfresco.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
     */
    @SuppressWarnings("unchecked")
    public ConfigElement parse(Element element)
    {
        DefaultControlsConfigElement result = null;
        if (element == null)
        {
            return null;
        }

        String name = element.getName();
        if (!name.equals(ELEMENT_DEFAULT_CONTROLS))
        {
            throw new ConfigException(this.getClass().getName()
                    + " can only parse " + ELEMENT_DEFAULT_CONTROLS
                    + " elements, the element passed was '" + name + "'");
        }

        result = new DefaultControlsConfigElement();

        Iterator<Element> xmlNodes = element.elementIterator();
        while (xmlNodes.hasNext())
        {
            Element nextNode = xmlNodes.next();
            String typeName = nextNode.attributeValue(ATTR_NAME);
            String templatePath = nextNode.attributeValue(ATTR_TEMPLATE);

            List<Element> controlParamNode = nextNode.elements(ELEMENT_CONTROL_PARAM);
            ControlParam param = null;
            // If the optional control-param tags are present
            List<ControlParam> params = new ArrayList<ControlParam>();

            for (Element nextControlParam : controlParamNode)
            {
                String paramName = nextControlParam.attributeValue(ATTR_NAME);
                String elementValue = nextControlParam.getTextTrim();
                // This impl assumes a String value within the control-param tags.
                // Cannot handle a value as XML attribute.
                param = new ControlParam(paramName, elementValue);
                params.add(param);
            }

            result.addDataMapping(typeName, templatePath, params);
        }

        return result;
    }
}
