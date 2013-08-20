package org.alfresco.rest.framework.jacksonextensions;

import org.alfresco.rest.framework.resource.UniqueId;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.BeanPropertyWriter;
import org.codehaus.jackson.map.ser.impl.PropertySerializerMap;

/**
 * A property writer for writing the unique id of a object
 *
 * @author Gethin James
 */
public class UniqueIdPropertyWriter extends BeanPropertyWriter
{

    protected UniqueIdPropertyWriter(BeanPropertyWriter base)
    {
        super(base);
    }
    
    protected UniqueIdPropertyWriter(BeanPropertyWriter base, JsonSerializer<Object> ser)
    {
        super(base, ser);
    }

    @Override
    public void serializeAsField(Object bean, JsonGenerator jgen, SerializerProvider prov)
                throws Exception
    {
        // _name is final so we have to do a little bit of trickery to change the field name.
        Object value = get(bean);
        // Null handling is bit different, check that first
        if (value == null) {
            if (!_suppressNulls) {
                jgen.writeFieldName(_name);
                prov.defaultSerializeNull(jgen);
            }
            return;
        }
        // For non-nulls, first: simple check for direct cycles
        if (value == bean) {
            _reportSelfReference(bean);
        }
        if (_suppressableValue != null && _suppressableValue.equals(value)) {
            return;
        }

        JsonSerializer<Object> ser = _serializer;
        if (ser == null) {
            Class<?> cls = value.getClass();
            PropertySerializerMap map = _dynamicSerializers;
            ser = map.serializerFor(cls);
            if (ser == null) {
                ser = _findAndAddDynamic(map, cls, prov);
            }
        }
        jgen.writeFieldName(UniqueId.UNIQUE_NAME);
        if (_typeSerializer == null) {
            ser.serialize(value, jgen, prov);
        } else {
            ser.serializeWithType(value, jgen, prov, _typeSerializer);
        }
        
    }


    /**
     * We have to override this!
     */
    @Override
    public BeanPropertyWriter withSerializer(JsonSerializer<Object> ser)
    {
        return new UniqueIdPropertyWriter(this, ser);
    }

}
