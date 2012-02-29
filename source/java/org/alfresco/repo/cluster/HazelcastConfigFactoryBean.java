/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.cluster;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryXmlConfig;

/**
 * FactoryBean used to create Hazelcast {@link Config} objects. A configuration file is supplied
 * in the form of a Spring {@link Resource} and a set of {@link Properties} can also be provided. The
 * XML file is processed so that property placeholders of the form ${property.name} are substitued for
 * the corresponding property value before the XML is parsed into the Hazelcast configuration object.
 *  
 * @author Matt Ward
 */
public class HazelcastConfigFactoryBean implements InitializingBean, FactoryBean<Config>
{
    private static final String PLACEHOLDER_END = "}";
    private static final String PLACEHOLDER_START = "${";
    private Resource configFile;
    private Config config;
    private Properties properties;
    
    
    /**
     * Set the Hazelcast XML configuration file to use. This will be merged with the supplied
     * Properties and parsed to produce a final {@link Config} object. 
     * @param configFile the configFile to set
     */
    public void setConfigFile(Resource configFile)
    {
        this.configFile = configFile;
    }
    
    /**
     * Used to supply the set of Properties that the configuration file can reference.
     * 
     * @param properties the properties to set
     */
    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    /**
     * Spring {@link InitializingBean} lifecycle method. Substitutes property placeholders for their
     * corresponding values and creates a {@link Config Hazelcast configuration} with the post-processed
     * XML file - ready for the {@link #getObject()} factory method to be used to retrieve it.
     */
    @Override
    public void afterPropertiesSet() throws Exception
    {
        if (configFile == null)
        {
            throw new IllegalArgumentException("No configuration file specified.");
        }        
        if (properties == null)
        {
            properties = new Properties();
        }
        
        // These configXML strings will be large and are therefore intended
        // to be thrown away. We only want to keep the final Config object.
        String rawConfigXML = getConfigFileContents();
        String configXML = substituteProperties(rawConfigXML);
        config = new InMemoryXmlConfig(configXML);
    }

    /**
     * For the method parameter <code>text</code>, replaces all occurrences of placeholders having
     * the form ${property.name} with the value of the property having the key "property.name". The
     * properties are supplied using {@link #setProperties(Properties)}.
     * 
     * @param text The String to apply property substitutions to.
     * @return String after substitutions have been applied.
     */
    private String substituteProperties(String text)
    {
        for (String propName : properties.stringPropertyNames())
        {
            String propValue = properties.getProperty(propName);
            String quotedPropName = Pattern.quote(PLACEHOLDER_START + propName + PLACEHOLDER_END);
            text = text.replaceAll(quotedPropName, propValue);
        }
        
        return text;
    }

    /**
     * Opens the configFile {@link Resource} and reads the contents into a String.
     * 
     * @return the contents of the configFile resource.
     */
    private String getConfigFileContents()
    {
        StringWriter writer = new StringWriter();
        InputStream inputStream = null;
        try
        {
            inputStream = configFile.getInputStream();
            IOUtils.copy(inputStream, writer, "UTF-8");
            return writer.toString();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Couldn't read configuration: " + configFile, e);
        }
        finally
        {    
            try
            {
                if (inputStream != null)
                {
                    inputStream.close();
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException("Couldn't close stream", e);
            }
        }
    }

    /**
     * FactoryBean's factory method. Returns the config with the property key/value
     * substitutions in place.
     */
    @Override
    public Config getObject() throws Exception
    {
        return config;
    }

    @Override
    public Class<?> getObjectType()
    {
        return Config.class;
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }
}
