/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Helper Class for outputting Jackson content, makes use of the RestJsonModule (main Jackson config).
 * Default settings : Date format is ISO8601, only serializes non-empty / non-null values.
 *
 * @author Gethin James
 */
public class JacksonHelper implements InitializingBean
{
    private static Log logger = LogFactory.getLog(JacksonHelper.class);  
    
    private Module module;
    private ObjectMapper objectMapper = null;
    private JsonEncoding encoding = JsonEncoding.UTF8;
    public static final String DEFAULT_FILTER_NAME = "defaultFilterName";
    

    /**
     * Sets the Jackson Module to be used.
     * 
     * @param module Module
     */
    public void setModule(Module module)
    {
        this.module = module;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        //Configure the objectMapper ready for use
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.configOverride(java.util.Map.class)
                        .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, null));
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        DateFormat DATE_FORMAT_ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        DATE_FORMAT_ISO8601.setTimeZone(TimeZone.getTimeZone("UTC"));
        objectMapper.setDateFormat(DATE_FORMAT_ISO8601);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }
    
    /**
     * A callback so a JsonGenerator can be used inline but exception are handled here
     * @param outStream OutputStream
     * @param writer The writer interface
     * @throws IOException
     */
    public void withWriter(OutputStream outStream, Writer writer) throws IOException
    {
        try
        {
            JsonGenerator generator = objectMapper.getFactory().createGenerator(outStream, encoding);
            writer.writeContents(generator, objectMapper);
        }
        catch (JsonMappingException error)
        {
            logger.error("Failed to write Json output", error);
        } 
        catch (JsonGenerationException generror)
        {
            logger.error("Failed to write Json output", generror);
        }
    }
       
    /**
     * Constructs the object based on the content.
     * @param content Reader
     * @return T
     */
    public <T> T construct(Reader content, Class<T> requiredType)
    {
        ObjectReader reader = objectMapper.readerFor(requiredType);
        try
        {
            return reader.readValue(content);
        }
        catch (IOException error)
        {
            throw new InvalidArgumentException("Could not read content from HTTP request body: "+error.getMessage());
        }
    }
    
    /**
     * Constructs the object based on the content as a List, the JSON can be an array or just a single value without the [] symbols
     * @param content Reader
     * @return A collection of the specified type
     */
    public <T> List<T> constructList(Reader content, Class<T> requiredType)
    {
        ObjectReader reader = objectMapper.readerFor(TypeFactory.defaultInstance().constructParametricType(List.class, requiredType));
        try
        {
            List<T> toReturn = reader.readValue(content);
            if (toReturn == null || toReturn.isEmpty())
            {
              throw new InvalidArgumentException("Could not read content from HTTP request body, the list is empty");
            }
            return toReturn;
        }
        catch (IOException error)
        {
            throw new InvalidArgumentException("Could not read content from HTTP request body: "+error.getMessage());
        }
    }
    
    /**
     * A callback interface for use with the withWriter() method
     */
    public static interface Writer
    {
        public void writeContents(JsonGenerator generator, ObjectMapper objectMapper) throws JsonGenerationException, JsonMappingException, IOException;
    }

    /*
     * Always returns all properties
     */
    public static class ReturnAllBeanProperties extends SimpleBeanPropertyFilter
    {

    }
}
