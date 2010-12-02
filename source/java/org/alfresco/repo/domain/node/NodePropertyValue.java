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
package org.alfresco.repo.domain.node;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.schema.SchemaBootstrap;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.VersionNumber;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Immutable property value storage class.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class NodePropertyValue implements Cloneable, Serializable
{
    private static final long serialVersionUID = -497902497351493075L;

    /** used to take care of empty strings being converted to nulls by the database */
    private static final String STRING_EMPTY = "";
    /** used to provide empty collection values in and out */
    public static final Serializable EMPTY_COLLECTION_VALUE = (Serializable) Collections.emptyList();
    
    private static Log logger = LogFactory.getLog(NodePropertyValue.class);
    private static Log loggerOracle = LogFactory.getLog(NodePropertyValue.class.getName() + ".oracle");

    /** potential value types */
    private static enum ValueType
    {
        NULL
        {
            @Override
            public Integer getOrdinalNumber()
            {
                return Integer.valueOf(0);
            }

            @Override
            Serializable convert(Serializable value)
            {
                return null;
            }
        },
        BOOLEAN
        {
            @Override
            public Integer getOrdinalNumber()
            {
                return Integer.valueOf(1);
            }

            @Override
            Serializable convert(Serializable value)
            {
                return DefaultTypeConverter.INSTANCE.convert(Boolean.class, value);
            }
        },
        INTEGER
        {
            @Override
            public Integer getOrdinalNumber()
            {
                return Integer.valueOf(2);
            }

            @Override
            protected ValueType getPersistedType(Serializable value)
            {
                return ValueType.LONG;
            }

            @Override
            Serializable convert(Serializable value)
            {
                return DefaultTypeConverter.INSTANCE.convert(Integer.class, value);
            }
        },
        LONG
        {
            @Override
            public Integer getOrdinalNumber()
            {
                return Integer.valueOf(3);
            }

            @Override
            Serializable convert(Serializable value)
            {
                if (value == null)
                {
                    return null;
                }
                else if (value instanceof ContentDataId)
                {
                    return ((ContentDataId)value).getId();
                }
                else if (value instanceof ContentDataWithId)
                {
                    return ((ContentDataWithId)value).getId();
                }
                else
                {
                    return DefaultTypeConverter.INSTANCE.convert(Long.class, value);
                }
            }
        },
        FLOAT
        {
            @Override
            public Integer getOrdinalNumber()
            {
                return Integer.valueOf(4);
            }

            @Override
            Serializable convert(Serializable value)
            {
                return DefaultTypeConverter.INSTANCE.convert(Float.class, value);
            }
        },
        DOUBLE
        {
            @Override
            public Integer getOrdinalNumber()
            {
                return Integer.valueOf(5);
            }

            @Override
            Serializable convert(Serializable value)
            {
                return DefaultTypeConverter.INSTANCE.convert(Double.class, value);
            }
        },
        STRING
        {
            @Override
            public Integer getOrdinalNumber()
            {
                return Integer.valueOf(6);
            }

            /**
             * Strings longer than the maximum of {@link NodePropertyValue#DEFAULT_MAX_STRING_LENGTH}
             * characters will be serialized.
             */
            @Override
            protected ValueType getPersistedType(Serializable value)
            {
                if (value instanceof String)
                {
                    String valueStr = (String) value;
                    // Check how long the String can be
                    if (valueStr.length() > SchemaBootstrap.getMaxStringLength())
                    {
                        return ValueType.SERIALIZABLE;
                    }
                }
                return ValueType.STRING;
            }

            @Override
            Serializable convert(Serializable value)
            {
                return DefaultTypeConverter.INSTANCE.convert(String.class, value);
            }
        },
        DATE
        {
            @Override
            public Integer getOrdinalNumber()
            {
                return Integer.valueOf(7);
            }

            @Override
            protected ValueType getPersistedType(Serializable value)
            {
                return ValueType.STRING;
            }

            @Override
            Serializable convert(Serializable value)
            {
                return DefaultTypeConverter.INSTANCE.convert(Date.class, value);
            }
        },
        SERIALIZABLE
        {
            @Override
            public Integer getOrdinalNumber()
            {
                return Integer.valueOf(9);
            }

            @Override
            Serializable convert(Serializable value)
            {
                return value;
            }
        },
        MLTEXT
        {
            @Override
            public Integer getOrdinalNumber()
            {
                return Integer.valueOf(10);
            }

            @Override
            protected ValueType getPersistedType(Serializable value)
            {
                if (value instanceof MLText)
                {
                    throw new IllegalArgumentException("MLText must be split up before persistence.");
                }
                return ValueType.STRING;
            }

            @Override
            Serializable convert(Serializable value)
            {
                return DefaultTypeConverter.INSTANCE.convert(String.class, value);
            }
        },
        CONTENT
        {
            @Override
            public Integer getOrdinalNumber()
            {
                return Integer.valueOf(11);
            }

            @Override
            protected ValueType getPersistedType(Serializable value)
            {
                if (value instanceof ContentData)
                {
                    return ValueType.SERIALIZABLE;
                }
                else
                {
                    throw new RuntimeException("ContentData persistence must be by ContentDataId.");
                }
            }

            @Override
            Serializable convert(Serializable value)
            {
                if (value instanceof Long)
                {
                    return value;
                }
                else if (value instanceof String)
                {
                    logger.warn("Content URL converter has not run to completion: " + value);
                    return DefaultTypeConverter.INSTANCE.convert(ContentData.class, value);
                }
                else
                {
                    return DefaultTypeConverter.INSTANCE.convert(ContentData.class, value);
                }
            }
        },
        NODEREF
        {
            @Override
            public Integer getOrdinalNumber()
            {
                return Integer.valueOf(12);
            }

            @Override
            protected ValueType getPersistedType(Serializable value)
            {
                return ValueType.STRING;
            }

            @Override
            Serializable convert(Serializable value)
            {
                return DefaultTypeConverter.INSTANCE.convert(NodeRef.class, value);
            }
        },
        CHILD_ASSOC_REF
        {
            @Override
            public Integer getOrdinalNumber()
            {
                return Integer.valueOf(13);
            }

            @Override
            protected ValueType getPersistedType(Serializable value)
            {
                return ValueType.STRING;
            }

            @Override
            Serializable convert(Serializable value)
            {
                return DefaultTypeConverter.INSTANCE.convert(ChildAssociationRef.class, value);
            }
        },
        ASSOC_REF
        {
            @Override
            public Integer getOrdinalNumber()
            {
                return Integer.valueOf(14);
            }

            @Override
            protected ValueType getPersistedType(Serializable value)
            {
                return ValueType.STRING;
            }

            @Override
            Serializable convert(Serializable value)
            {
                return DefaultTypeConverter.INSTANCE.convert(AssociationRef.class, value);
            }
        },
        QNAME
        {
            @Override
            public Integer getOrdinalNumber()
            {
                return Integer.valueOf(15);
            }

            @Override
            protected ValueType getPersistedType(Serializable value)
            {
                return ValueType.STRING;
            }

            @Override
            Serializable convert(Serializable value)
            {
                return DefaultTypeConverter.INSTANCE.convert(QName.class, value);
            }
        },
        PATH
        {
            @Override
            public Integer getOrdinalNumber()
            {
                return Integer.valueOf(16);
            }

            @Override
            protected ValueType getPersistedType(Serializable value)
            {
                return ValueType.SERIALIZABLE;
            }

            @Override
            Serializable convert(Serializable value)
            {
                return DefaultTypeConverter.INSTANCE.convert(Path.class, value);
            }
        },
        LOCALE
        {
            @Override
            public Integer getOrdinalNumber()
            {
                return Integer.valueOf(17);
            }

            @Override
            protected ValueType getPersistedType(Serializable value)
            {
                return ValueType.STRING;
            }

            @Override
            Serializable convert(Serializable value)
            {
                return DefaultTypeConverter.INSTANCE.convert(Locale.class, value);
            }
        },
        VERSION_NUMBER
        {
            @Override
            public Integer getOrdinalNumber()
            {
                return Integer.valueOf(18);
            }

            @Override
            protected ValueType getPersistedType(Serializable value)
            {
                return ValueType.STRING;
            }

            @Override
            Serializable convert(Serializable value)
            {
                return DefaultTypeConverter.INSTANCE.convert(VersionNumber.class, value);
            }
        },
        COLLECTION
        {
            @Override
            public Integer getOrdinalNumber()
            {
                return Integer.valueOf(19);
            }

            /**
             * @return      Returns and empty <tt>Collection</tt> if the value is null
             *              otherwise it just returns the original value
             */
            @Override
            Serializable convert(Serializable value)
            {
                if (value == null)
                {
                    return (Serializable) Collections.emptyList();
                }
                else
                {
                    return value;
                }
            }
        },
        PERIOD
        {
            @Override
            public Integer getOrdinalNumber()
            {
                return Integer.valueOf(20);
            }
            
            @Override
            protected ValueType getPersistedType(Serializable value)
            {
                return ValueType.STRING;
            }

            @Override
            Serializable convert(Serializable value)
            {
                return DefaultTypeConverter.INSTANCE.convert(Period.class, value);
            }
        },
        CONTENT_DATA_ID
        {
            @Override
            public Integer getOrdinalNumber()
            {
                return Integer.valueOf(21);
            }
            
            @Override
            protected ValueType getPersistedType(Serializable value)
            {
                return ValueType.LONG;
            }

            @Override
            Serializable convert(Serializable value)
            {
                if (value == null)
                {
                    return null;
                }
                else if (value instanceof Long)
                {
                    return value;
                }
                else if (value instanceof ContentDataId)
                {
                    return ((ContentDataId)value).getId();
                }
                else
                {
                    return DefaultTypeConverter.INSTANCE.convert(ContentData.class, value);
                }
            }
        },
        ;
        
        /**
         * @return      Returns the manually-maintained ordinal number for the value
         */
        public abstract Integer getOrdinalNumber();
        
        /**
         * Override if the type gets persisted in a different format.
         * 
         * @param value the actual value that is to be persisted.  May not be null.
         */
        protected ValueType getPersistedType(Serializable value)
        {
            return this;
        }
        
        /**
         * Converts a value to this type.  The implementation must be able to cope with any legitimate
         * source value.
         * 
         * @see DefaultTypeConverter.INSTANCE#convert(Class, Object)
         */
        abstract Serializable convert(Serializable value);
    }
    
    /**
     * Determine the actual value type to aid in more concise persistence.
     * 
     * @param value the value that is to be persisted
     * @return Returns the value type equivalent of the 
     */
    private static ValueType getActualType(Serializable value)
    {
        if (value == null)
        {
            return ValueType.NULL;
        }
        else if (value instanceof Boolean)
        {
            return ValueType.BOOLEAN;
        }
        else if (value instanceof Integer)
        {
            return ValueType.INTEGER;
        }
        else if (value instanceof Long)
        {
            return ValueType.LONG;
        }
        else if (value instanceof Float)
        {
            return ValueType.FLOAT;
        }
        else if (value instanceof Double)
        {
            return ValueType.DOUBLE;
        }
        else if (value instanceof String)
        {
            return ValueType.STRING;
        }
        else if (value instanceof Date)
        {
            return ValueType.DATE;
        }
        else if (value instanceof NodeRef)
        {
            return ValueType.NODEREF;
        }
        else if (value instanceof ChildAssociationRef)
        {
            return ValueType.CHILD_ASSOC_REF;
        }
        else if (value instanceof AssociationRef)
        {
            return ValueType.ASSOC_REF;
        }
        else if (value instanceof QName)
        {
            return ValueType.QNAME;
        }
        else if (value instanceof Path)
        {
            return ValueType.PATH;
        }
        else if (value instanceof Locale)
        {
            return ValueType.LOCALE;
        }
        else if (value instanceof VersionNumber)
        {
            return ValueType.VERSION_NUMBER;
        }
        else if (value instanceof Period)
        {
            return ValueType.PERIOD;
        }
        else if (value instanceof ContentDataId)
        {
            return ValueType.CONTENT_DATA_ID;
        }
        else if (value instanceof ContentDataWithId)
        {
            return ValueType.CONTENT_DATA_ID;
        }
        else if (value instanceof ContentData)
        {
            return ValueType.CONTENT;
        }
        else
        {
            // type is not recognised as belonging to any particular slot
            return ValueType.SERIALIZABLE;
        }
    }
    
    /** a mapping from a property type <code>QName</code> to the corresponding value type */
    private static Map<QName, ValueType> valueTypesByPropertyType;
    /**
     * a mapping of {@link ValueType} ordinal number to the enum.  This is manually maintained
     * and <b>MUST NOT BE CHANGED FOR EXISTING VALUES</b>.
     */
    private static Map<Integer, ValueType> valueTypesByOrdinalNumber;
    static
    {
        valueTypesByPropertyType = new HashMap<QName, ValueType>(37);
        valueTypesByPropertyType.put(DataTypeDefinition.ANY, ValueType.SERIALIZABLE);
        valueTypesByPropertyType.put(DataTypeDefinition.BOOLEAN, ValueType.BOOLEAN);
        valueTypesByPropertyType.put(DataTypeDefinition.INT, ValueType.INTEGER);
        valueTypesByPropertyType.put(DataTypeDefinition.LONG, ValueType.LONG);
        valueTypesByPropertyType.put(DataTypeDefinition.DOUBLE, ValueType.DOUBLE);
        valueTypesByPropertyType.put(DataTypeDefinition.FLOAT, ValueType.FLOAT);
        valueTypesByPropertyType.put(DataTypeDefinition.DATE, ValueType.DATE);
        valueTypesByPropertyType.put(DataTypeDefinition.DATETIME, ValueType.DATE);
        valueTypesByPropertyType.put(DataTypeDefinition.CATEGORY, ValueType.NODEREF);
        valueTypesByPropertyType.put(DataTypeDefinition.CONTENT, ValueType.CONTENT_DATA_ID);
        valueTypesByPropertyType.put(DataTypeDefinition.TEXT, ValueType.STRING);
        valueTypesByPropertyType.put(DataTypeDefinition.MLTEXT, ValueType.MLTEXT);
        valueTypesByPropertyType.put(DataTypeDefinition.NODE_REF, ValueType.NODEREF);
        valueTypesByPropertyType.put(DataTypeDefinition.CHILD_ASSOC_REF, ValueType.CHILD_ASSOC_REF);
        valueTypesByPropertyType.put(DataTypeDefinition.ASSOC_REF, ValueType.ASSOC_REF);
        valueTypesByPropertyType.put(DataTypeDefinition.PATH, ValueType.PATH);
        valueTypesByPropertyType.put(DataTypeDefinition.QNAME, ValueType.QNAME);
        valueTypesByPropertyType.put(DataTypeDefinition.LOCALE, ValueType.LOCALE);
        valueTypesByPropertyType.put(DataTypeDefinition.PERIOD, ValueType.PERIOD);
        
        valueTypesByOrdinalNumber = new HashMap<Integer, ValueType>(37);
        for (ValueType valueType : ValueType.values())
        {
            Integer ordinalNumber = valueType.getOrdinalNumber();
            if (valueTypesByOrdinalNumber.containsKey(ordinalNumber))
            {
                throw new RuntimeException("ValueType has duplicate ordinal number: " + valueType);
            }
            else if (ordinalNumber.intValue() == -1)
            {
                throw new RuntimeException("ValueType doesn't have an ordinal number: " + valueType);
            }
            valueTypesByOrdinalNumber.put(ordinalNumber, valueType);
        }
    }
    
    /**
     * Helper method to convert the type <code>QName</code> into a <code>ValueType</code>
     * 
     * @return Returns the <code>ValueType</code>  - never null
     */
    private static ValueType makeValueType(QName typeQName)
    {
        ValueType valueType = valueTypesByPropertyType.get(typeQName);
        if (valueType == null)
        {
            throw new AlfrescoRuntimeException(
                    "Property type not recognised: \n" +
                    "   type: " + typeQName);
        }
        return valueType;
    }
    
    /**
     * Given an actual type qualified name, returns the <tt>int</tt> ordinal number
     * that represents it in the database.
     * 
     * @param typeQName the type qualified name
     * @return Returns the <tt>int</tt> representation of the type,
     *      e.g. <b>CONTENT.getOrdinalNumber()</b> for type <b>d:content</b>.
     */
    public static int convertToTypeOrdinal(QName typeQName)
    {
        ValueType valueType = makeValueType(typeQName);
        return valueType.getOrdinalNumber();
    }
    
    /** the type of the property, prior to serialization persistence */
    private ValueType actualType;
    /** the type of persistence used */
    private ValueType persistedType;
    
    private Boolean booleanValue;
    private Long longValue;
    private Float floatValue;
    private Double doubleValue;
    private String stringValue;
    private Serializable serializableValue;
    
    /**
     * default constructor
     */
    public NodePropertyValue()
    {
    }
    
    /**
     * Construct a new property value.
     * 
     * @param typeQName         the dictionary-defined property type to store the property as
     * @param value             the value to store.  This will be converted into a format compatible
     *                          with the type given
     * 
     * @throws java.lang.UnsupportedOperationException if the value cannot be converted to the type given
     */
    public NodePropertyValue(QName typeQName, Serializable value)
    {
        ParameterCheck.mandatory("typeQName", typeQName);
        
        if (value == null)
        {
            this.actualType = NodePropertyValue.getActualType(value);
            setPersistedValue(ValueType.NULL, null);
        }
        else
        {
            // Convert the value to the type required.  This ensures that any type conversion issues
            // are caught early and prevent the scenario where the data in the DB cannot be given
            // back out because it is unconvertable.
            ValueType valueType = makeValueType(typeQName);
            value = valueType.convert(value);

            this.actualType = NodePropertyValue.getActualType(value);
            // get the persisted type
            ValueType persistedValueType = this.actualType.getPersistedType(value);
            // convert to the persistent type
            value = persistedValueType.convert(value);
            setPersistedValue(persistedValueType, value);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (obj instanceof NodePropertyValue)
        {
            NodePropertyValue that = (NodePropertyValue) obj;
            return (this.actualType.equals(that.actualType) &&
                    EqualsHelper.nullSafeEquals(this.getPersistedValue(), that.getPersistedValue()));
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public int hashCode()
    {
        int h = 0;
        if (actualType != null)
            h = actualType.hashCode();
        Serializable persistedValue = getPersistedValue();
        if (persistedValue != null)
            h += 17 * persistedValue.hashCode();
        return h;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("PropertyValue")
          .append("[actual-type=").append(actualType)
          .append(", value-type=").append(persistedType)
          .append(", value=").append(getPersistedValue())
          .append("]");
        return sb.toString();
    }

    public Integer getActualType()
    {
        return actualType == null ? null : actualType.getOrdinalNumber();
    }

    /**
     * @return          Returns the actual type's String representation
     */
    public String getActualTypeString()
    {
        return actualType == null ? null : actualType.toString();
    }

    public void setActualType(Integer actualType)
    {
        ValueType type = NodePropertyValue.valueTypesByOrdinalNumber.get(actualType);
        if (type == null)
        {
            logger.error("Unknown property actual type ordinal number: " + actualType);
        }
        this.actualType = type;
    }

    public Integer getPersistedType()
    {
        return persistedType == null ? null : persistedType.getOrdinalNumber();
    }
    public void setPersistedType(Integer persistedType)
    {
        ValueType type = NodePropertyValue.valueTypesByOrdinalNumber.get(persistedType);
        if (type == null)
        {
            logger.error("Unknown property persisted type ordinal number: " + persistedType);
        }
        this.persistedType = type;
    }
    
    /**
     * Stores the value in the correct slot based on the type of persistence requested.
     * No conversion is done.
     * 
     * @param persistedType the value type
     * @param value the value - it may only be null if the persisted type is {@link ValueType#NULL}
     */
    public void setPersistedValue(ValueType persistedType, Serializable value)
    {
        switch (persistedType)
        {
            case NULL:
                if (value != null)
                {
                    throw new AlfrescoRuntimeException("Value must be null for persisted type: " + persistedType);
                }
                break;
            case BOOLEAN:
                this.booleanValue = (Boolean) value;
                break;
            case LONG:
                this.longValue = (Long) value;
                break;
            case FLOAT:
                this.floatValue = (Float) value;
                break;
            case DOUBLE:
                this.doubleValue = (Double) value;
                break;
            case STRING:
                this.stringValue = (String) value;
                break;
            case SERIALIZABLE:
                this.serializableValue = cloneSerializable(value);
                break;
            default:
                throw new AlfrescoRuntimeException("Unrecognised value type: " + persistedType);
        }
        // we store the type that we persisted as
        this.persistedType = persistedType;
    }
    
    /**
     * Clones a serializable object to disconnect the original instance from the persisted instance.
     * 
     * @param original          the original object
     * @return                  the new cloned object
     */
    private Serializable cloneSerializable(Serializable original)
    {
       ObjectOutputStream objectOut = null;
       ByteArrayOutputStream byteOut = null;
       ObjectInputStream objectIn = null;
        try
        {
           // Write the object out to a byte array
           byteOut = new ByteArrayOutputStream();
           objectOut = new ObjectOutputStream(byteOut);
           objectOut.writeObject(original);
           objectOut.flush();

           objectIn = new ObjectInputStream(new ByteArrayInputStream(byteOut.toByteArray()));
           Object target = objectIn.readObject();
           // Done
           return (Serializable) target;
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Failed to clone serializable object: " + original, e);
        }
        finally
        {
           if (objectOut != null)
           {
              try { objectOut.close(); } catch (Throwable e) {}
           }
           if (byteOut != null)
           {
              try { byteOut.close(); } catch (Throwable e) {}
           }
           if (objectIn != null)
           {
              try { objectIn.close(); } catch (Throwable e) {}
           }
        }
    }

    /**
     * @return Returns the persisted value, keying off the persisted value type
     */
    private Serializable getPersistedValue()
    {
        switch (persistedType)
        {
            case NULL:
                return null;
            case BOOLEAN:
                return this.booleanValue;
            case LONG:
                return this.longValue;
            case FLOAT:
                return this.floatValue;
            case DOUBLE:
                return this.doubleValue;
            case STRING:
                // Oracle stores empty strings as 'null'...
                if (this.stringValue == null)
                {
                    // We know that we stored a non-null string, but now it is null.
                    // It can only mean one thing - Oracle
                    if (loggerOracle.isDebugEnabled())
                    {
                        logger.debug("string_value is 'null'.  Forcing to empty String");
                    }
                    return NodePropertyValue.STRING_EMPTY;
                }
                else
                {
                    return this.stringValue;
                }
            case SERIALIZABLE:
                return this.serializableValue;
            default:
                throw new AlfrescoRuntimeException("Unrecognised value type: " + persistedType);
        }
    }

    /**
     * Fetches the value as a desired type.  Collections (i.e. multi-valued properties)
     * will be converted as a whole to ensure that all the values returned within the
     * collection match the given type.
     * 
     * @param typeQName the type required for the return value
     * @return Returns the value of this property as the desired type, or a <code>Collection</code>
     *      of values of the required type
     * 
     * @throws AlfrescoRuntimeException
     *      if the type given is not recognized
     * @throws org.alfresco.service.cmr.repository.datatype.TypeConversionException
     *      if the conversion to the required type fails
     * 
     * @see DataTypeDefinition#ANY The static qualified names for the types
     */
    public Serializable getValue(QName typeQName)
    {
        // first check for null
        ValueType requiredType = makeValueType(typeQName);
        if (requiredType == ValueType.SERIALIZABLE)
        {
            // the required type must be the actual type
            requiredType = this.actualType;
        }
        
        // we need to convert
        Serializable ret = null;
        if (actualType == ValueType.COLLECTION && persistedType == ValueType.NULL)
        {
            // This is a special case of an empty collection
            ret = (Serializable) Collections.emptyList();
        }
        else if (persistedType == ValueType.NULL)
        {
            ret = null;
        }
        else
        {
            Serializable persistedValue = getPersistedValue();
            // convert the type
            // In order to cope with historical data, where collections were serialized
            // regardless of type.
            if (persistedValue instanceof Collection<?>)
            {
                // We assume that the collection contained the correct type values.  They would
                // have been converted on the way in.
                ret = (Serializable) persistedValue;
            }
            else
            {
                ret = requiredType.convert(persistedValue);
            }
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Fetched value: \n" +
                    "   property value: " + this + "\n" +
                    "   requested type: " + requiredType + "\n" +
                    "   result: " + ret);
        }
        return ret;
    }
    
    /**
     * Gets the value or values as a guaranteed collection.
     * 
     * @see #getValue(QName)
     */
    @SuppressWarnings("unchecked")
    public Collection<Serializable> getCollection(QName typeQName)
    {
        Serializable value = getValue(typeQName);
        if (value instanceof Collection)
        {
            return (Collection<Serializable>) value;
        }
        else
        {
            return Collections.singletonList(value);
        }
    }
    
    public boolean getBooleanValue()
    {
        if (booleanValue == null)
            return false;
        else
            return booleanValue.booleanValue();
    }
    public void setBooleanValue(boolean value)
    {
        this.booleanValue = Boolean.valueOf(value);
    }
    
    public long getLongValue()
    {
        if (longValue == null)
            return 0;
        else
            return longValue.longValue();
    }
    public void setLongValue(long value)
    {
        this.longValue = Long.valueOf(value);
    }
    
    public float getFloatValue()
    {
        if (floatValue == null)
            return 0.0F;
        else
            return floatValue.floatValue();
    }
    public void setFloatValue(float value)
    {
        this.floatValue = Float.valueOf(value);
    }
    
    public double getDoubleValue()
    {
        if (doubleValue == null)
            return 0.0;
        else
            return doubleValue.doubleValue();
    }
    public void setDoubleValue(double value)
    {
        this.doubleValue = Double.valueOf(value);
    }
    
    public String getStringValue()
    {
        return stringValue;
    }
    public void setStringValue(String value)
    {
        this.stringValue = value;
    }
    
    public Serializable getSerializableValue()
    {
        return serializableValue;
    }
    public void setSerializableValue(Serializable value)
    {
        this.serializableValue = value;
    }
}
