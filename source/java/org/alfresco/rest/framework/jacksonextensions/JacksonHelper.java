package org.alfresco.rest.framework.jacksonextensions;

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
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.Module;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.map.ser.BeanPropertyFilter;
import org.codehaus.jackson.map.ser.BeanPropertyWriter;
import org.codehaus.jackson.map.type.TypeFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Helper Class for outputting Jackson content, makes use of the
 * RestJsonModule (main Jackson config).
 * Default settings : Date format is ISO8601, only serializes
 * non-empty / non-null values.
 *
 * @author Gethin James
 */
public class JacksonHelper implements InitializingBean
{
    private static Log logger = LogFactory.getLog(JacksonHelper.class);  
    
    Module module;
    private ObjectMapper objectMapper = null;
    private JsonEncoding encoding = JsonEncoding.UTF8;
    public static final String DEFAULT_FILTER_NAME = "defaultFilterName";
    

    /**
     * Sets the Jackson Module to be used.
     * 
     * @param module
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
        objectMapper.setSerializationInclusion(Inclusion.NON_EMPTY);  //or NON_EMPTY?
        objectMapper.configure(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES, false);
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        DateFormat DATE_FORMAT_ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        DATE_FORMAT_ISO8601.setTimeZone(TimeZone.getTimeZone("UTC"));
        objectMapper.setDateFormat(DATE_FORMAT_ISO8601);
        objectMapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        
    }
    
    /**
     * A callback so a JsonGenerator can be used inline but exception are handled here
     * @param outStream
     * @param writer The writer interface
     * @throws IOException
     */
    public void withWriter(OutputStream outStream, Writer writer) throws IOException
    {
        try
        {
            JsonGenerator generator = objectMapper.getJsonFactory().createJsonGenerator(outStream, encoding);     
            writer.writeContents(generator, objectMapper);
        }
        catch (JsonMappingException error)
        {
            logger.error("Failed to write Json output",error);
        } 
        catch (JsonGenerationException generror)
        {
            logger.error("Failed to write Json output",generror);
        }
    }
       
    /**
     * Constructs the object based on the content.
     * @param content
     * @param requiredType
     * @return
     * @throws IOException
     */
    public <T> T construct(Reader content, Class<T> requiredType) throws IOException, JsonMappingException, JsonParseException
    {
            ObjectReader reader = objectMapper.reader(requiredType);
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
     * @param content
     * @param requiredType
     * @return A collection of the specified type
     * @throws IOException
     */
    public <T> List<T> constructList(Reader content, Class<T> requiredType) throws IOException, JsonMappingException, JsonParseException
    {
        ObjectReader reader = objectMapper.reader(TypeFactory.defaultInstance().constructParametricType(List.class, requiredType));
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
    public static class ReturnAllBeanProperties implements BeanPropertyFilter
    {

        @Override
        public void serializeAsField(Object bean, JsonGenerator jgen, SerializerProvider provider, BeanPropertyWriter writer) throws Exception
        {
            writer.serializeAsField(bean, jgen, provider);
        }

    }
}
