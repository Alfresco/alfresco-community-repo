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
