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
package org.alfresco.rest.framework.jacksonextensions;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.module.SimpleModule;
import org.springframework.beans.factory.InitializingBean;

/**
 * This is the main Jackson configuration, it configures the Rest Json settings.
 * It is possible to add custom serializers and deserializers using
 * the Spring bean config.
 *
 * @author Gethin James
 */
public class RestJsonModule extends SimpleModule implements InitializingBean
{
    private static Log logger = LogFactory.getLog(RestJsonModule.class);  
    private final static String NAME = "AlfrescoRestJsonModule";
     
    @SuppressWarnings("rawtypes")
    private List<JsonSerializer> jsonSerializers;
    @SuppressWarnings("rawtypes")
    private Map<String,JsonDeserializer> jsonDeserializers;
    
    public RestJsonModule()
    {
        super(NAME, new Version(1, 0, 0, null));
    }
    
    @Override
    public void setupModule(SetupContext context)
    {
        super.setupModule(context);
        context.insertAnnotationIntrospector(new CustomAnnotationIntrospector());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void afterPropertiesSet() throws Exception
    {
        if (jsonSerializers != null) 
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Setting up Json Module serializers");
            }
            for (JsonSerializer aSerializer : jsonSerializers)
            {
                addSerializer(aSerializer);
            }
        }
        if (jsonDeserializers != null) 
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Setting up Json Module deserializers");
            }
            for (Entry<String, JsonDeserializer> aDeserializer : jsonDeserializers.entrySet())
            {
                Class theDeserializer = Class.forName(aDeserializer.getKey());
                addDeserializer(theDeserializer, aDeserializer.getValue());
            }
        }
    }

    public void setJsonSerializers(@SuppressWarnings("rawtypes") List<JsonSerializer> jsonSerializers)
    {
        this.jsonSerializers = jsonSerializers;
    }

    public void setJsonDeserializers(@SuppressWarnings("rawtypes") Map<String, JsonDeserializer> jsonDeserializers)
    {
        this.jsonDeserializers = jsonDeserializers;
    }
}
