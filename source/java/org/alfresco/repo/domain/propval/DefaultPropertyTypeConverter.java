/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.domain.propval;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.propval.PropertyValueEntity.PersistedType;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Default converter for handling data going to and from the persistence layer.
 * <p/>
 * Properties are stored as a set of well-defined types defined by the enumeration
 * {@link PersistedType}.  Ultimately, data can be persisted as BLOB data, but must
 * be the last resort.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class DefaultPropertyTypeConverter implements PropertyTypeConverter
{
    /**
     * An unmodifiable map of types and how they should be persisted
     */
    protected static final Map<Class<?>, PersistedType> defaultPersistedTypesByClass;
    
    static
    {
        // Create the map of class-type
        Map<Class<?>, PersistedType> mapClass = new HashMap<Class<?>, PersistedType>(29);
        mapClass.put(Boolean.class, PersistedType.LONG);
        mapClass.put(Short.class, PersistedType.LONG);
        mapClass.put(Integer.class, PersistedType.LONG);
        mapClass.put(Long.class, PersistedType.LONG);
        mapClass.put(Date.class, PersistedType.LONG);
        mapClass.put(Float.class, PersistedType.DOUBLE);
        mapClass.put(Double.class, PersistedType.DOUBLE);
        mapClass.put(String.class, PersistedType.STRING);
        mapClass.put(Class.class, PersistedType.STRING);
        mapClass.put(NodeRef.class, PersistedType.STRING);
        mapClass.put(StoreRef.class, PersistedType.STRING);
        mapClass.put(Period.class, PersistedType.STRING);
        mapClass.put(Locale.class, PersistedType.STRING);
        mapClass.put(AssociationRef.class, PersistedType.STRING);
        mapClass.put(ChildAssociationRef.class, PersistedType.STRING);
        // Everything else is just Serializable
        defaultPersistedTypesByClass = Collections.unmodifiableMap(mapClass);
        
    }
    
    private Map<Class<?>, PersistedType> persistenceMapping;
    
    /**
     * Default constructor
     */
    public DefaultPropertyTypeConverter()
    {
        persistenceMapping = new HashMap<Class<?>, PersistedType>(
                DefaultPropertyTypeConverter.defaultPersistedTypesByClass);
    }
    
    /**
     * Allow subclasses to add further type mappings specific to the implementation
     * 
     * @param clazz                 the class to be converted
     * @param targetType            the target persisted type
     */
    protected void addTypeMapping(Class<?> clazz, PersistedType targetType)
    {
        this.persistenceMapping.put(clazz, targetType);
    }
    
    /**
     * Determines if the value can be adequately recreated (to equality) by creating
     * a new instance.  For example, a <b>java.util.HashMap</b> is constructable provided
     * that the map is empty.
     * <p>
     * Subclasses can override this to handle any well-known types, and in conjunction with
     * {@link #constructInstance(String)}, even choose to return <tt>true</tt> if it needs a
     * non-default constructor.
     * 
     * @param value             the value to check
     * @return                  Returns <tt>true</tt> if the value can be reconstructed by
     *                          instantiation using a default constructor
     */
    protected boolean isConstructable(Serializable value)
    {
        // Is it in the set directly
        Class<?> valueClazz = value.getClass();
        // Check for default constructor
        try
        {
            valueClazz.getConstructor();
        }
        catch (NoSuchMethodException e)
        {
            // Can't reconstruct using just a type name
            return false;
        }
        // Maps and Collections
        if (value instanceof Map<?, ?>)
        {
            Map<?, ?> mapValue = (Map<?, ?>) value;
            return mapValue.isEmpty();
        }
        else if (value instanceof Collection<?>)
        {
            Collection<?> collectionValue = (Collection<?>) value;
            return collectionValue.isEmpty();
        }
        else
        {
            // We don't recognise it
            return false;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Serializable constructInstance(String clazzName)
    {
        try
        {
            Class<?> clazz = Class.forName(clazzName);
            Constructor<?> constructor = clazz.getConstructor();
            return (Serializable) constructor.newInstance();
        }
        catch (ClassCastException e)
        {
            throw new AlfrescoRuntimeException("The constructed property is not serializable: " + clazzName);
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Unable to construct property for class: " + clazzName);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public PersistedType getPersistentType(Serializable value)
    {
        ParameterCheck.mandatory("value", value);
        
        Class<?> clazz = value.getClass();
        PersistedType type = persistenceMapping.get(clazz);
        if (type != null)
        {
            return type;
        }
        // Before we give up, check if it is constructable
        if (isConstructable(value))
        {
            // It'll just be given back as a class name i.e. a CONSTRUCTABLE
            return PersistedType.CONSTRUCTABLE;
        }
        else if (value instanceof Enum<?>)
        {
            return PersistedType.ENUM;
        }
        else
        {
            // Check if there are converters to and from well-known types, just in case
            if (DefaultTypeConverter.INSTANCE.getConverter(clazz, Long.class) != null &&
                    DefaultTypeConverter.INSTANCE.getConverter(Long.class, clazz) != null)
            {
                return PersistedType.LONG;
            }
            else if (DefaultTypeConverter.INSTANCE.getConverter(clazz, String.class) != null &&
                    DefaultTypeConverter.INSTANCE.getConverter(String.class, clazz) != null)
            {
                return PersistedType.STRING;
            }
            // No hope of doing anything useful other than storing it
            return PersistedType.SERIALIZABLE;
        }
    }
    
    /**
     * Performs the conversion using {@link DefaultTypeConverter} but also adds
     * special handling for {@link Enum enum types}.
     */
    @SuppressWarnings("unchecked")
    public <T> T convert(Class<T> targetClass, Serializable value)
    {
        if (targetClass.isEnum() && value != null && value instanceof String)
        {
            Class<Enum> enumClazz = (Class<Enum>) targetClass;
            return (T) Enum.valueOf(enumClazz, (String) value);
        }
        else
        {
            return DefaultTypeConverter.INSTANCE.convert(targetClass, value);
        }
    }
}
