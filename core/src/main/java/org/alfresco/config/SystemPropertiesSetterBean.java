/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Takes a set of properties and pushes them into the Java environment.  Usually, VM properties
 * are required by the system (see {@link SystemPropertiesFactoryBean} and
 * Spring's <tt>PropertyPlaceholderConfigurer</tt>); sometimes it is necessary to take properties
 * available to Spring and push them onto the VM.
 * <p>
 * For simplicity, the system property, if present, will NOT be modified.  Also, property placeholders
 * (<b>${...}</b>), empty values and null values will be ignored.
 * <p>
 * Use the {@link #init()} method to push the properties.
 * 
 * @author Derek Hulley
 * @since V3.1
 */
public class SystemPropertiesSetterBean
{
    private static Log logger = LogFactory.getLog(SystemPropertiesSetterBean.class);
    
    private Map<String, String> propertyMap;
    
    SystemPropertiesSetterBean()
    {
        propertyMap = new HashMap<String, String>(3);
    }
    
    /**
     * Set the properties that will be pushed onto the JVM.
     * 
     * @param propertyMap           a map of <b>property name</b> to <b>property value</b>
     */
    public void setPropertyMap(Map<String, String> propertyMap)
    {
        this.propertyMap = propertyMap;
    }
    
    public void init()
    {
        for (Map.Entry<String, String> entry : propertyMap.entrySet())
        {
            String name = entry.getKey();
            String value = entry.getValue();
            // Some values can be ignored
            if (value == null || value.length() == 0)
            {
                continue;
            }
            if (value.startsWith("${") && value.endsWith("}"))
            {
                continue;
            }
            // Check the system properties
            if (System.getProperty(name) != null)
            {
                // It was already there
                if (logger.isDebugEnabled())
                {
                    logger.debug("\n" +
                            "Not pushing up system property: \n" +
                            "   Property:              " + name + "\n" +
                            "   Value already present: " + System.getProperty(name) + "\n" +
                            "   Value provided:        " + value);
                }
                continue;
            }
            System.setProperty(name, value);
        }
    }
}
