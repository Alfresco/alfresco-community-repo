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
package org.alfresco.repo.domain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
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

/**
 * Immutable property value storage class.
 * <p>
 * <b>As of 2.2.1, this class is only used by the AVM persistence layers.</b>
 * 
 * @author Derek Hulley
 */
public class PropertyValue implements Cloneable, Serializable
{
    private static final long serialVersionUID = -497902497351493075L;

    /** used to take care of empty strings being converted to nulls by the database */
    private static final String STRING_EMPTY = "";
    
    private static Log logger = LogFactory.getLog(PropertyValue.class);
    private static Log loggerOracle = LogFactory.getLog(PropertyValue.class.getName() + ".oracle");

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
                return DefaultTypeConverter.INSTANCE.convert(Long.class, value);
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
             * Strings longer than the maximum of {@link PropertyValue#DEFAULT_MAX_STRING_LENGTH}
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
        /**
         * @deprecated          column FK to alf_global_attributes has been removed (3.4)
         */
        DB_ATTRIBUTE
        {
            @Override
            public Integer getOrdinalNumber()
            {
                return Integer.valueOf(8);
            }

            @Override
            Serializable convert(Serializable value)
            {
                return null;
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
                // NOTE: since 2.2.1, PropertyValue is only used by AVM (which does not natively support MLText, other than single/default string)
                return ValueType.STRING;
            }

            @Override
            Serializable convert(Serializable value)
            {
                // NOTE: since 2.2.1, PropertyValue is only used by AVM (which does not natively support MLText, other than single/default string)
                MLText mlText = DefaultTypeConverter.INSTANCE.convert(MLText.class, value);
                if (mlText.size() > 1)
                {
                    throw new UnsupportedOperationException("PropertyValue MLText is not supported for AVM");
                }
                return DefaultTypeConverter.INSTANCE.convert(String.class, mlText);
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
                return ValueType.STRING;
            }

            @Override
            Serializable convert(Serializable value)
            {
                return DefaultTypeConverter.INSTANCE.convert(ContentData.class, value);
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
        };
        
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
        
        protected ArrayList<Serializable> convert(Collection<?> collection)
        {
            ArrayList<Serializable> arrayList = new ArrayList<Serializable>(collection.size());
            for (Object object : collection)
            {
                Serializable newValue = null;
                if (object != null)
                {
                    if (!(object instanceof Serializable))
                    {
                        throw new AlfrescoRuntimeException("Collection values must contain Serializable instances: \n" +
                                "   value type: " + this + "\n" +
                                "   collection: " + collection + "\n" +
                                "   value: " + object);
                    }
                    Serializable value = (Serializable) object;
                    newValue = convert(value);
                }
                arrayList.add(newValue);
            }
            // done
            return arrayList;
        }
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
        else if ((value instanceof Integer) || (value instanceof Long))
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
        else if (value instanceof ContentData)
        {
            return ValueType.CONTENT;
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
        else if (value instanceof MLText)
        {
            return ValueType.MLTEXT;
        }
        else if (value instanceof Period)
        {
            return ValueType.PERIOD;
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
        valueTypesByPropertyType.put(DataTypeDefinition.ENCRYPTED, ValueType.SERIALIZABLE);
        valueTypesByPropertyType.put(DataTypeDefinition.BOOLEAN, ValueType.BOOLEAN);
        valueTypesByPropertyType.put(DataTypeDefinition.INT, ValueType.INTEGER);
        valueTypesByPropertyType.put(DataTypeDefinition.LONG, ValueType.LONG);
        valueTypesByPropertyType.put(DataTypeDefinition.DOUBLE, ValueType.DOUBLE);
        valueTypesByPropertyType.put(DataTypeDefinition.FLOAT, ValueType.FLOAT);
        valueTypesByPropertyType.put(DataTypeDefinition.DATE, ValueType.DATE);
        valueTypesByPropertyType.put(DataTypeDefinition.DATETIME, ValueType.DATE);
        valueTypesByPropertyType.put(DataTypeDefinition.CATEGORY, ValueType.NODEREF);
        valueTypesByPropertyType.put(DataTypeDefinition.CONTENT, ValueType.CONTENT);
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

    /** the type of the property, prior to serialization persistence */
    private ValueType actualType;
    /** true if the property values are contained in a collection */
    private boolean isMultiValued;
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
    public PropertyValue()
    {
    }
    
    /**
     * Construct a new property value.
     * 
     * @param typeQName the dictionary-defined property type to store the property as.   
     * May be null in which case the type will be determined from the value parameter.
     * @param value the value to store.  This will be converted into a format compatible
     *      with the type given
     * 
     * @throws java.lang.UnsupportedOperationException if the value cannot be converted to the
     *      type given
     */
    public PropertyValue(QName typeQName, Serializable value)
    {
        this.actualType = PropertyValue.getActualType(value);
        if (value == null)
        {
            setPersistedValue(ValueType.NULL, null);
            setMultiValued(false);
        }
        else if (value instanceof Collection<?>)
        {
            if(typeQName != null)
            {  
                Collection<?> collection = (Collection<?>) value;
                ValueType collectionValueType = makeValueType(typeQName);
                // convert the collection values - we need to do this to ensure that the
                // values provided conform to the given type
            
                ArrayList<Serializable> convertedCollection = collectionValueType.convert(collection);
                // the persisted type is, nonetheless, a serializable
                setPersistedValue(ValueType.SERIALIZABLE, convertedCollection);
            }
            else
            {
                setPersistedValue(ValueType.SERIALIZABLE, value);
            }
             

            setMultiValued(true);
        }
        else
        {
            // Does the client consider the type to be important?
            if (typeQName != null)
            {
                // Convert the value to the type required.  This ensures that any type conversion issues
                // are caught early and prevent the scenario where the data in the DB cannot be given
                // back out because it is unconvertable.
                ValueType valueType = makeValueType(typeQName);
                value = valueType.convert(value);
            }
            // get the persisted type
            ValueType persistedValueType = this.actualType.getPersistedType(value);
            // convert to the persistent type
            value = persistedValueType.convert(value);
            setPersistedValue(persistedValueType, value);
            setMultiValued(false);
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
        if (obj instanceof PropertyValue)
        {
            PropertyValue that = (PropertyValue) obj;
            return (this.actualType.equals(that.actualType) &&
                    EqualsHelper.nullSafeEquals(this.booleanValue, that.booleanValue) &&
                    EqualsHelper.nullSafeEquals(this.longValue, that.longValue) &&
                    EqualsHelper.nullSafeEquals(this.floatValue, that.floatValue) &&
                    EqualsHelper.nullSafeEquals(this.doubleValue, that.doubleValue) &&
                    EqualsHelper.nullSafeEquals(this.stringValue, that.stringValue) &&
                    EqualsHelper.nullSafeEquals(this.serializableValue, that.serializableValue)
                    );
            
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
          .append(", multi-valued=").append(isMultiValued)
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
        ValueType type = PropertyValue.valueTypesByOrdinalNumber.get(actualType);
        if (type == null)
        {
            logger.error("Unknown property actual type ordinal number: " + actualType);
        }
        this.actualType = type;
    }

    public boolean isMultiValued()
    {
        return isMultiValued;
    }

    public void setMultiValued(boolean isMultiValued)
    {
        this.isMultiValued = isMultiValued;
    }

    public Integer getPersistedType()
    {
        return persistedType == null ? null : persistedType.getOrdinalNumber();
    }
    public void setPersistedType(Integer persistedType)
    {
        ValueType type = PropertyValue.valueTypesByOrdinalNumber.get(persistedType);
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
            case DB_ATTRIBUTE:
                throw new IllegalArgumentException("DB_ATTRIBUTE is no longer supported.");
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
                    return PropertyValue.STRING_EMPTY;
                }
                else
                {
                    return this.stringValue;
                }
            case DB_ATTRIBUTE:
                return null;
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
        if (persistedType == ValueType.NULL)
        {
            ret = null;
        }
        else if (this.isMultiValued)
        {
            // collections are always stored
            Collection<?> collection = (Collection<?>) this.serializableValue;
            // convert the collection values - we need to do this to ensure that the
            // values provided conform to the given type
            ArrayList<Serializable> convertedCollection = requiredType.convert(collection);
            ret = convertedCollection;
        }
        else
        {
            Serializable persistedValue = getPersistedValue();
            // convert the type
            ret = requiredType.convert(persistedValue);
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
        if (value instanceof Collection<?>)
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
