/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.attributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;

/**
 * Base class for handling basic type conversions.
 * 
 * @author Derek Hulley
 * @since V2.1.2
 */
public abstract class AbstractAttribute implements Attribute
{
    public final AttributeImpl getAttributeImpl()
    {
        if (this instanceof AttributeImpl)
        {
            // No conversion necessary
            return (AttributeImpl) this;
        }
        else
        {
            // Use Type's factory method
            return getType().getAttributeImpl(this);
        }
    }

    public final AttributeValue getAttributeValue()
    {
        if (this instanceof AttributeValue)
        {
            // No conversion necessary
            return (AttributeValue) this;
        }
        else
        {
            // Use Type's factory method
            return getType().getAttributeValue(this);
        }
    }
    
    /**
     * {@link ListAttributeValue}-specific method.
     */
    public void add(Attribute attr)
    {
        throw new AttributeMethodNotImplemented("Not a List.");
    }

    /**
     * {@link ListAttributeValue}-specific method.
     */
    public void add(int index, Attribute attr)
    {
        throw new AttributeMethodNotImplemented("Not a List.");
    }

    /**
     * {@link ListAttributeValue}-specific method.
     */
    public Iterator<Attribute> iterator()
    {
        throw new AttributeMethodNotImplemented("Not a List.");
    }

    /**
     * {@link ListAttributeValue} or {@link MapAttributeValue}-specific method.
     */
    public int size()
    {
        throw new AttributeMethodNotImplemented("Not a List or Map.");
    }

    /**
     * {@link ListAttributeValue}-specific method.
     */
    public Attribute get(int index)
    {
        throw new AttributeMethodNotImplemented("Not a List.");
    }

    /**
     * {@link ListAttributeValue}-specific method.
     */
    public void remove(int index)
    {
        throw new AttributeMethodNotImplemented("Not a List.");
    }

    /**
     * {@link ListAttributeValue}-specific method.
     */
    public void set(int index, Attribute value)
    {
        throw new AttributeMethodNotImplemented("Not a List.");
    }
    
    /**
     * {@link MapAttributeValue}-specific method.
     */
    public void clear()
    {
        throw new AttributeMethodNotImplemented("Not a Map.");
    }

    /**
     * {@link MapAttributeValue}-specific method.
     */
    public Set<Entry<String, Attribute>> entrySet()
    {
        throw new AttributeMethodNotImplemented("Not a Map.");
    }

    /**
     * {@link MapAttributeValue}-specific method.
     */
    public Set<String> keySet()
    {
        throw new AttributeMethodNotImplemented("Not a map.");
    }

    /**
     * {@link MapAttributeValue}-specific method.
     */
    public Collection<Attribute> values()
    {
        throw new AttributeMethodNotImplemented("Not a map.");
    }

    /**
     * {@link MapAttributeValue}-specific method.
     */
    public void put(String key, Attribute value)
    {
        throw new AttributeMethodNotImplemented("Not a map.");
    }

    /**
     * {@link MapAttributeValue}-specific method.
     */
    public void remove(String key)
    {
        throw new AttributeMethodNotImplemented("Not a map.");
    }

    /**
     * {@link MapAttributeValue}-specific method.
     */
    public Attribute get(String key)
    {
        throw new AttributeMethodNotImplemented("Not a Map.");
    }

    public byte[] getBlobValue()
    {
        Serializable raw = getRawValue();
        // Just serialize it
        try
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(raw);
            byte[] bytes = bos.toByteArray();
            return bytes;
        }
        catch (IOException e)
        {
            throw new TypeConversionException("Unable to get blob value: " + this);
        }
    }

    public boolean getBooleanValue()
    {
        Serializable raw = getRawValue();
        try
        {
            Boolean obj = DefaultTypeConverter.INSTANCE.convert(Boolean.class, raw);
            return obj;
        }
        catch (TypeConversionException e)
        {
            throw new AttributeMethodNotImplemented("Unable to convert to Boolean value: " + this);
        }
    }

    public byte getByteValue()
    {
        Serializable raw = getRawValue();
        try
        {
            Byte obj = DefaultTypeConverter.INSTANCE.convert(Byte.class, raw);
            return obj;
        }
        catch (TypeConversionException e)
        {
            throw new AttributeMethodNotImplemented("Unable to convert to Byte value: " + this);
        }
    }

    public short getShortValue()
    {
        Serializable raw = getRawValue();
        try
        {
            Short obj = DefaultTypeConverter.INSTANCE.convert(Short.class, raw);
            return obj;
        }
        catch (TypeConversionException e)
        {
            throw new AttributeMethodNotImplemented("Unable to convert to Short value: " + this);
        }
    }

    public int getIntValue()
    {
        Serializable raw = getRawValue();
        try
        {
            Integer obj = DefaultTypeConverter.INSTANCE.convert(Integer.class, raw);
            return obj;
        }
        catch (TypeConversionException e)
        {
            throw new AttributeMethodNotImplemented("Unable to convert to Integer value: " + this);
        }
    }

    public long getLongValue()
    {
        Serializable raw = getRawValue();
        try
        {
            Long obj = DefaultTypeConverter.INSTANCE.convert(Long.class, raw);
            return obj;
        }
        catch (TypeConversionException e)
        {
            throw new AttributeMethodNotImplemented("Unable to convert to Long value: " + this);
        }
    }

    public double getDoubleValue()
    {
        Serializable raw = getRawValue();
        try
        {
            Double obj = DefaultTypeConverter.INSTANCE.convert(Double.class, raw);
            return obj;
        }
        catch (TypeConversionException e)
        {
            throw new AttributeMethodNotImplemented("Unable to convert to Double value: " + this);
        }
    }

    public float getFloatValue()
    {
        Serializable raw = getRawValue();
        try
        {
            Float obj = DefaultTypeConverter.INSTANCE.convert(Float.class, raw);
            return obj;
        }
        catch (TypeConversionException e)
        {
            throw new AttributeMethodNotImplemented("Unable to convert to Float value: " + this);
        }
    }

    public String getStringValue()
    {
        Serializable raw = getRawValue();
        try
        {
            String obj = DefaultTypeConverter.INSTANCE.convert(String.class, raw);
            return obj;
        }
        catch (TypeConversionException e)
        {
            throw new AttributeMethodNotImplemented("Unable to convert to String value: " + this);
        }
    }

    public Serializable getSerializableValue()
    {
        // This can always be fulfilled by the raw value
        return getRawValue();
    }

    public void setBlobValue(byte[] value)
    {
        throw new AttributeMethodNotImplemented("Not a Blob.");
    }

    public void setBooleanValue(boolean value)
    {
        throw new AttributeMethodNotImplemented("Not a boolean.");
    }

    public void setByteValue(byte value)
    {
        throw new AttributeMethodNotImplemented("Not a byte.");
    }

    public void setShortValue(short value)
    {
        throw new AttributeMethodNotImplemented("Not a short.");
    }

    public void setIntValue(int value)
    {
        throw new AttributeMethodNotImplemented("Not an int.");
    }

    public void setLongValue(long value)
    {
        throw new AttributeMethodNotImplemented("Not a long.");
    }

    public void setDoubleValue(double value)
    {
        throw new AttributeMethodNotImplemented("Not a double.");
    }

    public void setFloatValue(float value)
    {
        throw new AttributeMethodNotImplemented("Not a float.");
    }

    public void setStringValue(String value)
    {
        throw new AttributeMethodNotImplemented("Not a String.");
    }

    public void setSerializableValue(Serializable value)
    {
        throw new AttributeMethodNotImplemented("Not a Serializable.");
    }
}
