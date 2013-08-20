package org.alfresco.rest.framework.jacksonextensions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.rest.framework.core.ResourceInspectorUtil;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.BeanDescription;
import org.codehaus.jackson.map.BeanPropertyDefinition;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.std.SerializerBase;
import org.codehaus.jackson.type.JavaType;

/**
 * Serializes ExecutionResult into the correct response format
 * 
 * @author Gethin James
 */
public class SerializerOfExecutionResult extends SerializerBase<ExecutionResult>
{

    protected SerializerOfExecutionResult()
    {
        super(ExecutionResult.class);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void serialize(ExecutionResult value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonGenerationException
    {
        
        SerializationConfig config = provider.getConfig();
        Object rootObj = value.getRoot();
        if (rootObj == null)
        {
            provider.getNullValueSerializer().serialize(null, jgen, provider);
        }
        else
        {
            Class<?> cls = rootObj.getClass();
            Map toBeSerialized = new HashMap(); //create an untyped map, add the contents of the root + the embeds.
            BeanPropertiesFilter filter = value.getFilter();
            if (filter == null) filter = BeanPropertiesFilter.ALLOW_ALL;
            
            if (Map.class.isAssignableFrom(cls))
            {
                // Its a map so 
                Map rootAsaMap = (Map) rootObj;
                toBeSerialized.putAll(rootAsaMap);
            }
            else
            {
                JavaType classType = config.constructType(cls);
                BeanDescription beanDesc = provider.getConfig().introspect(classType);
                List<BeanPropertyDefinition> props = beanDesc.findProperties();
                for (BeanPropertyDefinition beanProperty : props)
                {
                    if (beanProperty.couldSerialize() && filter.isAllowed(beanProperty.getName()))
                    {
                        Object propertyValue = ResourceInspectorUtil.invokeMethod(beanProperty.getGetter().getAnnotated(), rootObj);
                        if (propertyValue != null)
                        {
                            if((propertyValue instanceof String))
                            {
                            	if(((String)propertyValue).trim().length() > 0)
                            	{
                            		toBeSerialized.put(beanProperty.getName(), propertyValue);
                            	}
                            }
                            else
                            {
                            	toBeSerialized.put(beanProperty.getName(), propertyValue);
                            }
                        }
                    }
                }
            }
            
            //Add embedded
            for (Entry<String, Object> embedded : value.getEmbedded().entrySet())
            {
                if (filter == null || filter.isAllowed(embedded.getKey()))
                {
                  toBeSerialized.put(embedded.getKey(),embedded.getValue());
                }
            }

            //if its an embedded entity then render the properties (not as an "entry:")
            if (value.isAnEmbeddedEntity())
            {
                jgen.writeObject(toBeSerialized);
            }
            else
            {
                jgen.writeStartObject();
                jgen.writeObjectField("entry", toBeSerialized);
                if (value.getRelated() != null && !value.getRelated().isEmpty())
                {
                  jgen.writeObjectField("relations", value.getRelated());
                }
                jgen.writeEndObject();
            }

        }

    }

}
