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

    public static final Short ORDINAL_NULL = new Short((short)0);
    public static final Short ORDINAL_BOOLEAN = new Short((short)1);
    public static final Short ORDINAL_LONG = new Short((short)2);
    public static final Short ORDINAL_DOUBLE = new Short((short)3);
    public static final Short ORDINAL_STRING = new Short((short)4);
    public static final Short ORDINAL_SERIALIZABLE = new Short((short)5);
    
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
        BOOLEAN
        {
            @Override
            public Short getOrdinalNumber()
            {
                return ORDINAL_BOOLEAN;
            }
            @Override
            public Class<?> getAssociatedClass()
            {
                return Boolean.class;
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
        mapClass.put(Boolean.class, PersistedType.BOOLEAN);
        mapClass.put(Short.class, PersistedType.LONG);
        mapClass.put(Integer.class, PersistedType.LONG);
        mapClass.put(Long.class, PersistedType.LONG);
        mapClass.put(Float.class, PersistedType.DOUBLE);
        mapClass.put(Double.class, PersistedType.DOUBLE);
        mapClass.put(String.class, PersistedType.STRING);
        mapClass.put(Date.class, PersistedType.LONG);
        persistedTypesByClass = Collections.unmodifiableMap(mapClass);
    }

    private static final Log logger = LogFactory.getLog(PropertyValueEntity.class);
    
    private Long id;
    private Short persistedType;
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
        return (persistedType == null ? 0 : persistedType.intValue()) + (longValue == null ? 0 : longValue.intValue());
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
            return EqualsHelper.nullSafeEquals(this.persistedType, that.persistedType) &&
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
          .append(", type=").append(persistedType)
          .append(", value=").append(longValue)
          .append("]");
        return sb.toString();
    }
    
    /**
     * @return          Returns the ID-value pair
     */
    public Pair<Long, Serializable> getEntityPair()
    {
        Serializable value = getValue();
        return new Pair<Long, Serializable>(id, value);
    }
    
    /**
     * Gets the value based on the persisted type.
     * Note that this is the value <b>as persisted</b> and not the original, client-required
     * value.
     * @return          Returns the persisted value
     */
    public Serializable getValue()
    {
        if (persistedType.equals(PersistedType.NULL.getOrdinalNumber()))
        {
            return null;
        }
        else if (persistedType.equals(PersistedType.BOOLEAN.getOrdinalNumber()))
        {
            return (longValue.longValue() > 0 ? Boolean.TRUE : Boolean.FALSE);
        }
        else if (persistedType.equals(PersistedType.LONG.getOrdinalNumber()))
        {
            return longValue;
        }
        else if (persistedType.equals(PersistedType.DOUBLE.getOrdinalNumber()))
        {
            return doubleValue;
        }
        else if (persistedType.equals(PersistedType.STRING.getOrdinalNumber()))
        {
            return stringValue;
        }
        else if (persistedType.equals(PersistedType.SERIALIZABLE.getOrdinalNumber()))
        {
            return serializableValue;
        }
        else
        {
            logger.warn("Persisted type code not recognised: " + this.persistedType);
            // Return any non-null value and hope it works
            if (serializableValue != null)
            {
                return serializableValue;
            }
            else if (doubleValue != null)
            {
                return doubleValue;
            }
            else if (stringValue != null)
            {
                return stringValue;
            }
            else
            {
                return longValue;
            }
        }
    }
    
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Short getPersistedType()
    {
        return persistedType;
    }

    public void setPersistedType(Short persistedType)
    {
        this.persistedType = persistedType;
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
