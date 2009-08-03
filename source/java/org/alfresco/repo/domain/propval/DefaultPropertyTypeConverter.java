/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.domain.propval;

import java.io.Serializable;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.propval.PropertyValueEntity.PersistedType;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default converter for handling data going to and from the persistence layer.
 * <p>
 * This implementation uses an explicit mapping for each Java class supported.  If
 * an unsupported class is found, then the value is just serialized.  Conversions
 * are done using the {@link DefaultTypeConverter}.
 * 
 * @author Derek Hulley
 * @since 3.3
 */
public class DefaultPropertyTypeConverter implements PropertyTypeConverter
{
    private static final Log logger = LogFactory.getLog(DefaultPropertyTypeConverter.class);
    
    /**
     * Default constructor
     */
    public DefaultPropertyTypeConverter()
    {
    }
    
    /**
     * {@inheritDoc}
     * <p/>
     * This converter looks up a {@link PersistedType} using the <code>class</code> of the
     * given value.  <tt>null</tt> values are handled specially.  If there is no match, then
     * the {@link PersistedType#SERIALIZABLE} type is used.
     */
    public Pair<Short, Serializable> convertToPersistedType(Serializable value)
    {
        if (value == null)
        {
            return PropertyValueEntity.PERSISTED_TYPE_NULL;
        }
        // Look up the type in the class map
        Class<?> clazz = value.getClass();
        PersistedType type = PropertyValueEntity.persistedTypesByClass.get(clazz);
        if (type == null)
        {
            return new Pair<Short, Serializable>(PersistedType.SERIALIZABLE.getOrdinalNumber(), value);
        }
        else
        {
            // Convert the value
            Class<?> toClazz = type.getAssociatedClass();
            try
            {
                Serializable converted = (Serializable) DefaultTypeConverter.INSTANCE.convert(toClazz, value);
                return new Pair<Short, Serializable>(type.getOrdinalNumber(), converted);
            }
            catch (TypeConversionException e)
            {
                throw new AlfrescoRuntimeException(
                        "Failed to convert to persistable value: \n" +
                        "   Value:        " + value.getClass() + "\n" +
                        "   Target type:  " + type + "\n" +
                        "   Target class: " + toClazz,
                        e);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     * <p/>
     * Looks up the {@link #persistedTypesByOrdinal persisted type} and uses the {@link DefaultTypeConverter} to
     * convert back to an external value.
     */
    public Serializable convertFromPersistedType(Short persistedType, Class<?> actualType, Serializable persistedValue)
    {
        if (persistedValue == null)
        {
            throw new IllegalArgumentException("A persisted value can never be null");
        }
        PersistedType type = PropertyValueEntity.persistedTypesByOrdinal.get(persistedType);
        if (type == null)
        {
            // Not recognised!  This is probably a data issue
            logger.warn("Persisted type of '" + persistedType + "' not recognised.");
            return persistedValue;
        }
        else if (type == PersistedType.SERIALIZABLE)
        {
            // No conversion necessary
            return persistedValue;
        }
        // Convert the value
        try
        {
            return (Serializable) DefaultTypeConverter.INSTANCE.convert(actualType, persistedValue);
        }
        catch (TypeConversionException e)
        {
            throw new AlfrescoRuntimeException(
                    "Failed to convert from persisted value: \n" +
                    "   Value:        " + persistedValue.getClass() + "\n" +
                    "   Source type:  " + type + "\n" +
                    "   Target class: " + actualType,
                    e);
        }
    }
}
