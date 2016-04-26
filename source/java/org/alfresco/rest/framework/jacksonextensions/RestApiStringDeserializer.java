package org.alfresco.rest.framework.jacksonextensions;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.deser.std.StringDeserializer;

/**
 * Rest api string deserializer that ensures that empty strings are treated as null strings.
 * 
 * @author steveglover
 *
 */
public class RestApiStringDeserializer extends StringDeserializer
{
    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
    	String ret = super.deserialize(jp, ctxt);

    	if(ret != null && ret.length() == 0)
    	{
    		ret = null;
    	}

    	return ret;
    }
}
