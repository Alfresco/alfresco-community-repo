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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Entity bean for <b>alf_prop_value</b> table.
 * <p>
 * Values here are either simple values that can be stored in a <code>long</code>
 * or will be references to data in other tables.
 * 
 * @author Derek Hulley
 * @since 3.3
 */
public class PropertyValueEntity
{
    public static final Long LONG_ZERO = new Long(0L);
    public static final Long LONG_ONE = new Long(1L);

    public static final Short ORDINAL_NULL = 0;
    public static final Short ORDINAL_LONG = 1;
    public static final Short ORDINAL_DOUBLE = 2;
    public static final Short ORDINAL_STRING = 3;
    public static final Short ORDINAL_SERIALIZABLE = 4;
    public static final Short ORDINAL_MAP = 5;
    public static final Short ORDINAL_COLLECTION = 6;
    
    /**
     * Enumeration of persisted types for <b>alf_prop_value.persisted_type</b>.
     * <p/>
     * This enumeration is a helper for the default implementation of the {@link PropertyTypeConverter}
     * and should not be used in public interfaces.
     * 
     * @author Derek Hulley
     * @since 3.3
     */
    public static enum PersistedType
    {
        NULL
        {
            @Override
            public Short getOrdinalNumber()
            {
                return ORDINAL_NULL;
            }
            @Override
            public Class<?> getAssociatedClass()
            {
                throw new UnsupportedOperationException("NULL is a special case and has no associated class.");
            }
        },
        LONG
        {
            @Override
            public Short getOrdinalNumber()
            {
                return ORDINAL_LONG;
            }
            @Override
            public Class<?> getAssociatedClass()
            {
                return Long.class;
            }
        },
        DOUBLE
        {
            @Override
            public Short getOrdinalNumber()
            {
                return ORDINAL_DOUBLE;
            }
            @Override
            public Class<?> getAssociatedClass()
            {
                return Double.class;
            }
        },
        STRING
        {
            @Override
            public Short getOrdinalNumber()
            {
                return ORDINAL_STRING;
            }
            @Override
            public Class<?> getAssociatedClass()
            {
                return String.class;
            }
        },
        SERIALIZABLE
        {
            @Override
            public Short getOrdinalNumber()
            {
                return ORDINAL_SERIALIZABLE;
            }
            @Override
            public Class<?> getAssociatedClass()
            {
                return Serializable.class;
            }
        },
        MAP
        {
            @Override
            public Short getOrdinalNumber()
            {
                return ORDINAL_MAP;
            }
            @Override
            public Class<?> getAssociatedClass()
            {
                return Map.class;
            }
        },
        COLLECTION
        {
            @Override
            public Short getOrdinalNumber()
            {
                return ORDINAL_COLLECTION;
            }
            @Override
            public Class<?> getAssociatedClass()
            {
                return Collection.class;
            }
        };
        
        /**
         * Fetch the numerical value that will represent the the persisted type.  This is done
         * explicitly to prevent ordering issues if further types are added.
         * 
         * @return              Returns the ordinal number
         */
        public abstract Short getOrdinalNumber();
        
        /**
         * Get the persisted type's class.  This is used for determining the source type when
         * converting from persisted values.
         * 
         * @return              Returns the class associated with the persisted type
         */
        public abstract Class<?> getAssociatedClass();
    }

    public static final Pair<Short, Serializable> PERSISTED_TYPE_NULL;
    /**
     * An unmodifiable map of persisted type enums keyed by their ordinal number
     */
    public static final Map<Short, PersistedType> persistedTypesByOrdinal;
    
    /**
     * An unmodifiable map of persisted type enums keyed by the classes they store
     */
    public static final Map<Class<?>, PersistedType> persistedTypesByClass;
    
    static
    {
        // Create a pair for null values
        PERSISTED_TYPE_NULL = new Pair<Short, Serializable>(PersistedType.NULL.getOrdinalNumber(), new Long(0));
        // Create the map of ordinal-type
        Map<Short, PersistedType> mapOrdinal = new HashMap<Short, PersistedType>(15);
        for (PersistedType persistedType : PersistedType.values())
        {
            mapOrdinal.put(persistedType.getOrdinalNumber(), persistedType);
        }
        persistedTypesByOrdinal = Collections.unmodifiableMap(mapOrdinal);
        // Create the map of class-type
        Map<Class<?>, PersistedType> mapClass = new HashMap<Class<?>, PersistedType>(29);
        mapClass.put(Boolean.class, PersistedType.LONG);
        mapClass.put(Short.class, PersistedType.LONG);
        mapClass.put(Integer.class, PersistedType.LONG);
        mapClass.put(Long.class, PersistedType.LONG);
        mapClass.put(Float.class, PersistedType.DOUBLE);
        mapClass.put(Double.class, PersistedType.DOUBLE);
        mapClass.put(String.class, PersistedType.STRING);
        mapClass.put(Date.class, PersistedType.LONG);
        mapClass.put(Map.class, PersistedType.SERIALIZABLE);            // Will be serialized if encountered
        mapClass.put(Collection.class, PersistedType.SERIALIZABLE);     // Will be serialized if encountered
        persistedTypesByClass = Collections.unmodifiableMap(mapClass);
    }

    private static final Log logger = LogFactory.getLog(PropertyValueEntity.class);
    
    private Long id;
    private Long actualTypeId;
    private Short persistedType;
    private PersistedType persistedTypeEnum;            // Derived
    private Long longValue;
    private String stringValue;
    private Double doubleValue;
    private Serializable serializableValue;
    
    public PropertyValueEntity()
    {
        this.persistedType = PersistedType.NULL.getOrdinalNumber();
        this.longValue = LONG_ZERO;
    }
    
    @Override
    public int hashCode()
    {
        return (actualTypeId == null ? 0 : actualTypeId.intValue()) + (longValue == null ? 0 : longValue.intValue());
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj != null && obj instanceof PropertyValueEntity)
        {
            PropertyValueEntity that = (PropertyValueEntity) obj;
            return EqualsHelper.nullSafeEquals(this.actualTypeId, that.actualTypeId) &&
                   EqualsHelper.nullSafeEquals(this.longValue, that.longValue);
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("PropertyValueEntity")
          .append("[ ID=").append(id)
          .append(", actualTypeId=").append(actualTypeId)
          .append(", persistedType=").append(persistedType)
          .append(", value=").append(longValue)
          .append("]");
        return sb.toString();
    }
    
    /**
     * Gets the value based on the persisted type.
     * Note that this is the value <b>as persisted</b> and not the original, client-required
     * value.
     * @return          Returns the persisted value
     */
    public Serializable getPersistedValue()
    {
        switch (persistedTypeEnum)
        {
        case MAP:
        case COLLECTION:
        case NULL:
            return null;
        case LONG:
            return longValue;
        case DOUBLE:
            return doubleValue;
        case STRING:
            return stringValue;
        case SERIALIZABLE:
            return serializableValue;
        default:
            throw new IllegalStateException("Should not be able to get through switch");
        }
    }
    
    /**
     * Shortcut method to set the value.  It will be converted as required and the necessary fields
     * will be populated.
     * 
     * @param value         the value to persist (may be <tt>null</tt>)
     * @param converter     the converter that will perform and type conversion
     * @return              Returns the persisted type value
     */
    public Serializable setValue(Serializable value, PropertyTypeConverter converter)
    {
        if (value == null)
        {
            this.persistedType = ORDINAL_NULL;
            this.persistedTypeEnum = PersistedType.NULL;
            this.longValue = LONG_ZERO;
            return longValue;
        }
        else
        {
            Class<?> valueClazz = value.getClass();
            persistedTypeEnum = persistedTypesByClass.get(valueClazz);
            if (persistedTypeEnum == null)
            {
                persistedTypeEnum = PersistedType.SERIALIZABLE;
            }
            persistedType = persistedTypeEnum.getOrdinalNumber();
            // Get the class to persist as
            switch (persistedTypeEnum)
            {
                case LONG:
                    longValue = converter.convert(Long.class, value);
                    return longValue;
                case DOUBLE:
                    doubleValue = converter.convert(Double.class, value);
                    return doubleValue;
                case STRING:
                    stringValue = converter.convert(String.class, value);
                    return stringValue;
                case SERIALIZABLE:
                    serializableValue = value;
                    return serializableValue;
                default:
                    throw new IllegalStateException("Should not be able to get through switch");
            }
        }
    }
    
    /**
     * Helper method to determine how the given value will be stored.
     * 
     * @param value         the value to check
     * @return              Returns the persisted type
     */
    public static PersistedType getPersistedTypeEnum(Serializable value)
    {
        PersistedType persistedTypeEnum;
        if (value == null)
        {
            persistedTypeEnum = PersistedType.NULL;
        }
        else
        {
            Class<?> valueClazz = value.getClass();
            persistedTypeEnum = persistedTypesByClass.get(valueClazz);
            if (persistedTypeEnum == null)
            {
                persistedTypeEnum = PersistedType.SERIALIZABLE;
            }
        }
        return persistedTypeEnum;
    }
    
    public PersistedType getPersistedTypeEnum()
    {
        return persistedTypeEnum;
    }
    
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getActualTypeId()
    {
        return actualTypeId;
    }

    public void setActualTypeId(Long actualTypeId)
    {
        this.actualTypeId = actualTypeId;
    }

    public Short getPersistedType()
    {
        return persistedType;
    }

    public void setPersistedType(Short persistedType)
    {
        this.persistedType = persistedType;
        this.persistedTypeEnum = persistedTypesByOrdinal.get(persistedType);
        if (persistedTypeEnum == null)
        {
            logger.error("Persisted type '" + persistedType + "' is not recognised.");
            this.persistedTypeEnum = PersistedType.LONG;
        }
    }

    public Long getLongValue()
    {
        return longValue;
    }

    public void setLongValue(Long longValue)
    {
        this.longValue = longValue;
    }

    public String getStringValue()
    {
        return stringValue;
    }

    public void setStringValue(String stringValue)
    {
        this.stringValue = stringValue;
    }

    public Double getDoubleValue()
    {
        return doubleValue;
    }

    public void setDoubleValue(Double doubleValue)
    {
        this.doubleValue = doubleValue;
    }

    public Serializable getSerializableValue()
    {
        return serializableValue;
    }

    public void setSerializableValue(Serializable serializableValue)
    {
        this.serializableValue = serializableValue;
    }
}
