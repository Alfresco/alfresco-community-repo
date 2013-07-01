/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.content.transform;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides access to transformer properties which come from the Transformer sub system AND
 * the those that start with "content.transformer." in the parent context.<p>
 * 
 * By default a subsystem only provides properties defined within itself and only those
 * properties may be overridden by alfresco.global.properties. New properties may not be added.
 * As this class allows this to happen for the Transformers subsystem.
 * 
 * @author Alan Davis
 */
public class TransformerProperties
{
    private static final String TRANSFORMERS_PROPERTIES = "alfresco/subsystems/Transformers/default/transformers.properties";

    private static Log logger = LogFactory.getLog(TransformerProperties.class);
    
    private final ChildApplicationContextFactory subsystem;
    
    private final Properties globalProperties;
    
    TransformerProperties(ChildApplicationContextFactory subsystem, Properties globalProperties)
    {
        this.subsystem = subsystem;
        this.globalProperties = globalProperties;
    }

    public String getProperty(String name)
    {
        String value = subsystem.getProperty(name);
        if (value == null)
        {
            value = globalProperties.getProperty(name);
        }
        return value;
    }

    /**
     * Returns the default properties from the transformers.properties file. These may be overridden by customers in
     * other property files and JMX. 
     */
    public Properties getDefaultProperties()
    {
        Properties defaultProperties = new Properties();
        InputStream propertiesStream = getClass().getClassLoader().getResourceAsStream(TRANSFORMERS_PROPERTIES);
        if (propertiesStream != null)
        {
            try
            {
                defaultProperties.load(propertiesStream);
            }
            catch (IOException e)
            {
                logger.error("Could not read "+TRANSFORMERS_PROPERTIES+" so all properties will appear to be overridden by the customer", e);
            }
        }
        else
        {
            logger.error("Could not find "+TRANSFORMERS_PROPERTIES+" so all properties will appear to be overridden by the customer");
        }
        return defaultProperties;
    }

    public Set<String> getPropertyNames()
    {
        Set<String> propertyNames = new HashSet<String>(subsystem.getPropertyNames());
        for (String name: globalProperties.stringPropertyNames())
        {
            if (name.startsWith(TransformerConfig.PREFIX) && !propertyNames.contains(name))
            {
                propertyNames.add(name);
            }
        }

        return propertyNames;
    }

    public void setProperties(Map<String, String> map)
    {
        subsystem.setProperties(map);
    }

    public void removeProperties(Collection<String> propertyNames)
    {
        subsystem.removeProperties(propertyNames);
    }
}
