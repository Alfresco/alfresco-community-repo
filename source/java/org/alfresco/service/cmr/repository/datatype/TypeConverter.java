/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.service.cmr.repository.datatype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.util.ParameterCheck;


/**
 * Support for generic conversion between types.
 * 
 * Additional conversions may be added.
 * 
 * Direct conversion and two stage conversions via Number are supported. We do
 * not support conversion by any route at the moment
 */
public class TypeConverter
{

    /**
     * General conversion method to Object types (note it cannot support
     * conversion to primary types due the restrictions of reflection. Use the
     * static conversion methods to primitive types)
     * 
     * @param propertyType - the target property type
     * @param value - the value to be converted
     * @return - the converted value as the correct type
     */
    @SuppressWarnings("unchecked")
    public final Object convert(DataTypeDefinition propertyType, Object value)
    {
        ParameterCheck.mandatory("Property type definition", propertyType);
        
        // Convert property type to java class
        Class javaClass = null;
        String javaClassName = propertyType.getJavaClassName();
        try
        {
            javaClass = Class.forName(javaClassName);
        }
        catch (ClassNotFoundException e)
        {
            throw new DictionaryException("Java class " + javaClassName + " of property type " + propertyType.getName() + " is invalid", e);
        }
        
        return convert(javaClass, value);
    }

    /**
     * General conversion method to Object types (note it cannot support
     * conversion to primary types due the restrictions of reflection. Use the
     * static conversion methods to primitive types)
     * 
     * @param <T> The target type for the result of the conversion
     * @param c - a class for the target type
     * @param value - the value to be converted
     * @return - the converted value as the correct type
     * @throws TypeConversionException if the conversion cannot be performed
     */
    @SuppressWarnings("unchecked")
    public final <T> T convert(Class<T> c, Object value)
    {
        if(value == null)
        {
            return null;
        }

        // Primative types
        if (c.isPrimitive())
        {
            // We can not suport primitive type conversion
            throw new TypeConversionException("Can not convert direct to primitive type " + c.getName());
        }

        // Check if we already have the correct type
        if (c.isInstance(value))
        {
            return c.cast(value);
        }

        // Find the correct conversion - if available and do the converiosn
        Converter converter = getConverter(value, c);
        if (converter == null)
        {
            throw new TypeConversionException(
                    "There is no conversion registered for the value: \n" +
                    "   value class: " + value.getClass().getName() + "\n" +
                    "   to class: " + c.getName() + "\n" +
                    "   value: " + value.toString());
        }
        
        return (T) converter.convert(value);
    }

    /**
     * General conversion method to convert collection contents to the specified
     * type.
     * 
     * @param propertyType - the target property type
     * @param value - the value to be converted
     * @return - the converted value as the correct type
     * @throws DictionaryException if the property type's registered java class is invalid
     * @throws TypeConversionException if the conversion cannot be performed
     */
    @SuppressWarnings("unchecked")
    public final Collection convert(DataTypeDefinition propertyType, Collection values)
    {
        ParameterCheck.mandatory("Property type definition", propertyType);
        
        // Convert property type to java class
        Class javaClass = null;
        String javaClassName = propertyType.getJavaClassName();
        try
        {
            javaClass = Class.forName(javaClassName);
        }
        catch (ClassNotFoundException e)
        {
            throw new DictionaryException("Java class " + javaClassName + " of property type " + propertyType.getName() + " is invalid", e);
        }
        
        return convert(javaClass, values);
    }
    
    /**
     * General conversion method to convert collection contents to the specified
     * type.
     * 
     * @param <T> The target type for the result of the conversion
     * @param c - a class for the target type
     * @param value - the collection to be converted
     * @return - the converted collection
     * @throws TypeConversionException if the conversion cannot be performed
     */
    public final <T> Collection<T> convert(Class<T> c, Collection values)
    {
        if(values == null)
        {
            return null;
        }

        Collection<T> converted = new ArrayList<T>(values.size());
        for (Object value : values)
        {
            converted.add(convert(c, value));
        }

        return converted;
    }
    
    /**
     * Get the boolean value for the value object
     * May have conversion failure
     * 
     * @param value
     * @return
     */
    public final boolean booleanValue(Object value)
    {
        return convert(Boolean.class, value).booleanValue();
    }
    
    /**
     * Get the char value for the value object
     * May have conversion failure
     * 
     * @param value
     * @return
     */
    public final char charValue(Object value)
    {
        return convert(Character.class, value).charValue();
    }
    
    /**
     * Get the byte value for the value object
     * May have conversion failure
     * 
     * @param value
     * @return
     */
    public final byte byteValue(Object value)
    {
        if (value instanceof Number)
        {
            return ((Number) value).byteValue();
        }
        return convert(Byte.class, value).byteValue();
    }

    /**
     * Get the short value for the value object
     * May have conversion failure
     * 
     * @param value
     * @return
     */
    public final short shortValue(Object value)
    {
        if (value instanceof Number)
        {
            return ((Number) value).shortValue();
        }
        return convert(Short.class, value).shortValue();
    }
    
    /**
     * Get the int value for the value object
     * May have conversion failure
     * 
     * @param value
     * @return
     */
    public final int intValue(Object value)
    {
        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        return convert(Integer.class, value).intValue();
    }
    
    /**
     * Get the long value for the value object
     * May have conversion failure
     * 
     * @param value
     * @return
     */
    public final long longValue(Object value)
    {
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        return convert(Long.class, value).longValue();
    }

    /**
     * Get the bollean value for the value object
     * May have conversion failure
     * 
     * @param float
     * @return
     */
    public final float floatValue(Object value)
    {
        if (value instanceof Number)
        {
            return ((Number) value).floatValue();
        }
        return convert(Float.class, value).floatValue();
    }
    
    /**
     * Get the bollean value for the value object
     * May have conversion failure
     * 
     * @param double
     * @return
     */
    public final double doubleValue(Object value)
    {
        if (value instanceof Number)
        {
            return ((Number) value).doubleValue();
        }
        return convert(Double.class, value).doubleValue();
    }

    /**
     * Is the value multi valued
     * 
     * @param value
     * @return true - if the underlyinf is a collection of values and not a singole value
     */
    public final boolean isMultiValued(Object value)
    {
        return (value instanceof Collection);
    }

    /**
     * Get the number of values represented
     * 
     * @param value
     * @return 1 for normal values and the size of the collection for MVPs
     */
    public final int size(Object value)
    {
        if (value instanceof Collection)
        {
            return ((Collection) value).size();
        }
        else
        {
            return 1;
        }
    }

    /**
     * Get a collection for the passed value
     * 
     * @param value
     * @return
     */
    private final Collection createCollection(Object value)
    {
        Collection coll;
        if (isMultiValued(value))
        {
            coll = (Collection) value;
        }
        else
        {
            ArrayList<Object> list = new ArrayList<Object>(1);
            list.add(value);
            coll = list;
        }
        return coll;
    }

    /**
     * Get a collection for the passed value converted to the specified type
     * 
     * @param c
     * @param value
     * @return
     */
    public final <T> Collection<T> getCollection(Class<T> c, Object value)
    {
        Collection coll = createCollection(value);
        return convert(c, coll);
    }
        
    /**
     * Add a converter to the list of those available
     * 
     * @param <F>       
     * @param <T>
     * @param source
     * @param destination
     * @param converter
     */
    public final <F, T> void addConverter(Class<F> source, Class<T> destination, Converter<F, T> converter)
    {
        Map<Class, Converter> map = conversions.get(source);
        if (map == null)
        {
            map = new HashMap<Class, Converter>();
            conversions.put(source, map);
        }
        map.put(destination, converter);
    }

    /**
     * Add a dynamic two stage converter
     * @param <F>  from
     * @param <I>  intermediate
     * @param <T>  to
     * @param source
     * @param intermediate
     * @param destination
     */
    public final <F, I, T> Converter<F, T> addDynamicTwoStageConverter(Class<F> source, Class<I> intermediate, Class<T> destination)
    {
        Converter<F, T> converter = new TypeConverter.DynamicTwoStageConverter<F, I, T>(source, intermediate, destination);
        addConverter(source, destination, converter);
        return converter;
    }

    /**
     * Find conversion for the specified object
     * 
     * Note: Takes into account the class of the object and any interfaces it may
     *       also support.
     * 
     * @param <F>
     * @param <T>
     * @param source
     * @param dest
     * @return
     */
    @SuppressWarnings("unchecked")
    public final <T> Converter getConverter(Object value, Class<T> dest)
    {
        Converter converter = null;    
        if (value == null)
        {
            return null;
        }

        // find via class of value
        Class valueClass = value.getClass();
        converter = getConverter(valueClass, dest);
        if (converter != null)
        {
            return converter;
        }
        
        // find via supported interfaces of value
        do
        {
            Class[] ifClasses = valueClass.getInterfaces();
            for (Class ifClass : ifClasses)
            {
                converter = getConverter(ifClass, dest);
                if (converter != null)
                {
                    return converter;
                }
            }
            valueClass = valueClass.getSuperclass();
        }
        while (valueClass != null);
        
        return null;
    }

    /**
     * Find a conversion for a specific Class 
     * 
     * @param <F>
     * @param <T>
     * @param source
     * @param dest
     * @return
     */
    public <F, T> Converter getConverter(Class<F> source, Class<T> dest)
    {
        Converter<?, ?> converter = null;
        Class clazz = source;
        do
        {
            Map<Class, Converter> map = conversions.get(clazz);
            if (map == null)
            {
                continue;
            }
            converter = map.get(dest);
            
            if (converter == null)
            {
                // attempt to establish converter from source to dest via Number
                Converter<?, ?> first = map.get(Number.class);
                Converter<?, ?> second = null;
                if (first != null)
                {
                    map = conversions.get(Number.class);
                    if (map != null)
                    {
                        second = map.get(dest);
                    }
                }
                if (second != null)
                {
                    converter = new TwoStageConverter<F, Number, T>(first, second);
                }
            }
        }
        while ((converter == null) && ((clazz = clazz.getSuperclass()) != null));

        return converter;
    }

    /**
     * Map of conversion
     */
    private Map<Class, Map<Class, Converter>> conversions = new HashMap<Class, Map<Class, Converter>>();


    // Support for pluggable conversions
    
    /**
     * Conversion interface
     * 
     * @author andyh
     *
     * @param <F> From type
     * @param <T> To type
     */
    public interface Converter<F, T>
    {
        public T convert(F source);
    }

    /**
     * Support for chaining conversions
     * 
     * @author andyh
     *
     * @param <F> From Type
     * @param <I> Intermediate type
     * @param <T> To Type
     */
    public static class TwoStageConverter<F, I, T> implements Converter<F, T>
    {
        Converter first;

        Converter second;

        TwoStageConverter(Converter first, Converter second)
        {
            this.first = first;
            this.second = second;
        }

        @SuppressWarnings("unchecked")
        public T convert(F source)
        {
            return (T) second.convert((I) first.convert(source));
        }
    }
    
    /**
     * Support for chaining conversions
     * 
     * @author David Caruana
     *
     * @param <F> From Type
     * @param <I> Intermediate type
     * @param <T> To Type
     */
    protected class DynamicTwoStageConverter<F, I, T> implements Converter<F, T>
    {
        Class<F> from;
        Class<I> intermediate;
        Class<T> to;
        
        DynamicTwoStageConverter(Class<F> from, Class<I> intermediate, Class<T> to)
        {
            this.from = from;
            this.intermediate = intermediate;
            this.to = to;
        }
        
        /**
         * @return  from class
         */
        public Class<F> getFrom()
        {
            return from;
        }
        
        /**
         * @return  intermediate class
         */
        public Class<I> getIntermediate()
        {
            return intermediate;
        }
        
        /**
         * @return  to class
         */
        public Class<T> getTo()
        {
            return to;
        }
        
        @SuppressWarnings("unchecked")
        public T convert(F source)
        {
            Converter iConverter = TypeConverter.this.getConverter(from, intermediate);
            Converter tConverter = TypeConverter.this.getConverter(intermediate, to);
            if (iConverter == null || tConverter == null)
            {
                throw new TypeConversionException("Cannot convert from " + from.getName() + " to " + to.getName());
            }
            
            Object iValue = iConverter.convert(source);
            Object tValue = tConverter.convert(iValue);
            return (T)tValue;
        }
    }
    
}
