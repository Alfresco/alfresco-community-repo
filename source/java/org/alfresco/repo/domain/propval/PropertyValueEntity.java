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
import java.util.Collections;
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
 * @since 3.2
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
    public static final Short ORDINAL_CONSTRUCTABLE = 5;
    public static final Short ORDINAL_ENUM = 6;
    
    /**
     * Enumeration of persisted types for <b>alf_prop_value.persisted_type</b>.
     * <p/>
     * This enumeration is a helper for the default implementation of the {@link PropertyTypeConverter}
     * and should not be used in public interfaces.
     * 
     * @author Derek Hulley
     * @since 3.2
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
        CONSTRUCTABLE
        {
            @Override
            public Short getOrdinalNumber()
            {
                return ORDINAL_CONSTRUCTABLE;
            }
            @Override
            public Class<?> getAssociatedClass()
            {
                return Class.class;
            }
        },
        ENUM
        {
            @Override
            public Short getOrdinalNumber()
            {
                return ORDINAL_ENUM;
            }
            @Override
            public Class<?> getAssociatedClass()
            {
                return Enum.class;
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
     * Helper method to get the value based on the persisted type. 
     * 
     * @param actualType        the type to convert to
     * @param converter         the data converter to use
     * @return                  Returns the converted value
     */
    public Serializable getValue(Class<Serializable> actualType, PropertyTypeConverter converter)
    {
        switch (persistedTypeEnum)
        {
        case NULL:
            return null;
        case LONG:
            return converter.convert(actualType, Long.valueOf(longValue));
        case DOUBLE:
            return converter.convert(actualType, Double.valueOf(doubleValue));
        case STRING:
            if (stringValue != null && stringValue.equals(PropertyStringValueEntity.EMPTY_STRING_REPLACEMENT))
            {
                return converter.convert(actualType, PropertyStringValueEntity.EMPTY_STRING);
            }
            else
            {
                return converter.convert(actualType, stringValue);
            }
        case SERIALIZABLE:
            return converter.convert(actualType, serializableValue);
        case CONSTRUCTABLE:
            // Construct an instance using the converter (it knows best!)
            return converter.constructInstance(stringValue);
        case ENUM:
            // The converter should handle enumeration types
            return converter.convert(actualType, stringValue);
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
     */
    public void setValue(Serializable value, PropertyTypeConverter converter)
    {
        if (value == null)
        {
            this.persistedType = ORDINAL_NULL;
            this.persistedTypeEnum = PersistedType.NULL;
            this.longValue = LONG_ZERO;
        }
        else
        {
            // The converter will be responsible for deserializing, so let it choose
            // how the data is to be stored.
            persistedTypeEnum = converter.getPersistentType(value);
            persistedType = persistedTypeEnum.getOrdinalNumber();
            // Get the class to persist as
            switch (persistedTypeEnum)
            {
                case LONG:
                    longValue = converter.convert(Long.class, value);
                    break;
                case DOUBLE:
                    doubleValue = converter.convert(Double.class, value);
                    break;
                case STRING:
                    stringValue = converter.convert(String.class, value);
                    if (stringValue.equals(PropertyStringValueEntity.EMPTY_STRING))
                    {
                        // Oracle: We can't insert empty strings into the column.
                        stringValue = PropertyStringValueEntity.EMPTY_STRING_REPLACEMENT;
                    }
                    break;
                case CONSTRUCTABLE:
                    // A special case.  There is no conversion, so just Store the name of the class.
                    stringValue = value.getClass().getName();
                    break;
                case ENUM:
                    // A special case.  Store the string-equivalent representation
                    stringValue = converter.convert(String.class, value);
                    break;
                case SERIALIZABLE:
                    serializableValue = value;
                    break;
                default:
                    throw new IllegalStateException(
                            "PropertyTypeConverter.convertToPersistentType returned illegal type: " +
                            "   Converter:      " + converter + "\n" +
                            "   Type Returned:  " + persistedTypeEnum + "\n" +
                            "   From Value:     " + value);
            }
        }
    }
    
    /**
     * Helper method to determine how the given value will be stored.
     * 
     * @param value         the value to check
     * @param converter     the type converter
     * @return              Returns the persisted type
     * 
     * @see PropertyTypeConverter#getPersistentType(Serializable)
     */
    public static PersistedType getPersistedTypeEnum(Serializable value, PropertyTypeConverter converter)
    {
        PersistedType persistedTypeEnum;
        if (value == null)
        {
            persistedTypeEnum = PersistedType.NULL;
        }
        else
        {
            persistedTypeEnum = converter.getPersistentType(value);
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
        if (stringValue == null)
        {
            // Oracle!  It pulls nulls out in place of empty strings.
            //  Since we don't put nulls into the DB (the column doesn't allow it)
            //  we can be sure that this is an Oracle empty string
            stringValue = "";
        }
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
