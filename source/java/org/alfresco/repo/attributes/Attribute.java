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

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.repo.domain.schema.SchemaBootstrap;

/**
 * Interface for polymorphic attributes.
 * @author britt
 */
public interface Attribute extends Serializable, Iterable<Attribute>
{
    public static enum Type implements Serializable
    {
        BOOLEAN
        {
            @Override
            public AttributeValue getAttributeValue(Attribute from)
            {
                if (from instanceof BooleanAttribute)
                {
                    return new BooleanAttributeValue((BooleanAttribute)from);
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Conversion to " + this + " not supported for " + from.getType() + "(" + from + ")");
                }
            }

            @Override
            public AttributeImpl getAttributeImpl(Attribute from)
            {
                if (from instanceof BooleanAttribute)
                {
                    return new BooleanAttributeImpl((BooleanAttribute)from);
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Conversion to " + this + " not supported for " + from.getType() + "(" + from + ")");
                }
            }
        },
        BYTE
        {
            @Override
            public AttributeValue getAttributeValue(Attribute from)
            {
                if (from instanceof ByteAttribute)
                {
                    return new ByteAttributeValue((ByteAttribute)from);
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Conversion to " + this + " not supported for " + from.getType() + "(" + from + ")");
                }
            }

            @Override
            public AttributeImpl getAttributeImpl(Attribute from)
            {
                if (from instanceof ByteAttribute)
                {
                    return new ByteAttributeImpl((ByteAttribute)from);
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Conversion to " + this + " not supported for " + from.getType() + "(" + from + ")");
                }
            }
        },
        SHORT
        {
            @Override
            public AttributeValue getAttributeValue(Attribute from)
            {
                if (from instanceof ShortAttribute)
                {
                    return new ShortAttributeValue((ShortAttribute)from);
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Conversion to " + this + " not supported for " + from.getType() + "(" + from + ")");
                }
            }

            @Override
            public AttributeImpl getAttributeImpl(Attribute from)
            {
                if (from instanceof ShortAttribute)
                {
                    return new ShortAttributeImpl((ShortAttribute)from);
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Conversion to " + this + " not supported for " + from.getType() + "(" + from + ")");
                }
            }
        },
        INT
        {
            @Override
            public AttributeValue getAttributeValue(Attribute from)
            {
                if (from instanceof IntAttribute)
                {
                    return new IntAttributeValue((IntAttribute)from);
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Conversion to " + this + " not supported for " + from.getType() + "(" + from + ")");
                }
            }

            @Override
            public AttributeImpl getAttributeImpl(Attribute from)
            {
                if (from instanceof IntAttribute)
                {
                    return new IntAttributeImpl((IntAttribute)from);
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Conversion to " + this + " not supported for " + from.getType() + "(" + from + ")");
                }
            }
        },
        LONG
        {
            @Override
            public AttributeValue getAttributeValue(Attribute from)
            {
                if (from instanceof LongAttribute)
                {
                    return new LongAttributeValue((LongAttribute)from);
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Conversion to " + this + " not supported for " + from.getType() + "(" + from + ")");
                }
            }

            @Override
            public AttributeImpl getAttributeImpl(Attribute from)
            {
                if (from instanceof LongAttribute)
                {
                    return new LongAttributeImpl((LongAttribute)from);
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Conversion to " + this + " not supported for " + from.getType() + "(" + from + ")");
                }
            }
        },
        FLOAT
        {
            @Override
            public AttributeValue getAttributeValue(Attribute from)
            {
                if (from instanceof FloatAttribute)
                {
                    return new FloatAttributeValue((FloatAttribute)from);
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Conversion to " + this + " not supported for " + from.getType() + "(" + from + ")");
                }
            }

            @Override
            public AttributeImpl getAttributeImpl(Attribute from)
            {
                if (from instanceof FloatAttribute)
                {
                    return new FloatAttributeImpl((FloatAttribute)from);
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Conversion to " + this + " not supported for " + from.getType() + "(" + from + ")");
                }
            }
        },
        DOUBLE
        {
            @Override
            public AttributeValue getAttributeValue(Attribute from)
            {
                if (from instanceof DoubleAttribute)
                {
                    return new DoubleAttributeValue((DoubleAttribute)from);
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Conversion to " + this + " not supported for " + from.getType() + "(" + from + ")");
                }
            }

            @Override
            public AttributeImpl getAttributeImpl(Attribute from)
            {
                if (from instanceof DoubleAttribute)
                {
                    return new DoubleAttributeImpl((DoubleAttribute)from);
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Conversion to " + this + " not supported for " + from.getType() + "(" + from + ")");
                }
            }
        },
        STRING
        {
            @Override
            public AttributeValue getAttributeValue(Attribute from)
            {
                if (from instanceof StringAttribute)
                {
                    return new StringAttributeValue((StringAttribute)from);
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Conversion to " + this + " not supported for " + from.getType() + "(" + from + ")");
                }
            }

            @Override
            public AttributeImpl getAttributeImpl(Attribute from)
            {
                if (from instanceof StringAttribute)
                {
                    // We need to check that the String will fit into the database
                    StringAttribute stringAttr = (StringAttribute) from;
                    String stringValue = stringAttr.getStringValue();
                    
                    if (stringValue != null && stringValue.length() > SchemaBootstrap.getMaxStringLength())
                    {
                        // Need to serialize it
                        return new SerializableAttributeImpl(stringValue);
                    }
                    else
                    {
                        return new StringAttributeImpl(stringValue);
                    }
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Conversion to " + this + " not supported for " + from.getType() + "(" + from + ")");
                }
            }
        },
        SERIALIZABLE
        {
            @Override
            public AttributeValue getAttributeValue(Attribute from)
            {
                if (from instanceof SerializableAttribute)
                {
                    return new SerializableAttributeValue((SerializableAttribute)from);
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Conversion to " + this + " not supported for " + from.getType() + "(" + from + ")");
                }
            }

            @Override
            public AttributeImpl getAttributeImpl(Attribute from)
            {
                if (from instanceof SerializableAttribute)
                {
                    return new SerializableAttributeImpl((SerializableAttribute)from);
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Conversion to " + this + " not supported for " + from.getType() + "(" + from + ")");
                }
            }
        },
        MAP
        {
            @Override
            public AttributeValue getAttributeValue(Attribute from)
            {
                if (from instanceof MapAttribute)
                {
                    return new MapAttributeValue((MapAttribute)from);
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Conversion to " + this + " not supported for " + from.getType() + "(" + from + ")");
                }
            }

            @Override
            public AttributeImpl getAttributeImpl(Attribute from)
            {
                if (from instanceof MapAttribute)
                {
                    return new MapAttributeImpl((MapAttribute)from);
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Conversion to " + this + " not supported for " + from.getType() + "(" + from + ")");
                }
            }
        },
        LIST
        {
            @Override
            public AttributeValue getAttributeValue(Attribute from)
            {
                if (from instanceof ListAttribute)
                {
                    return new ListAttributeValue((ListAttribute)from);
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Conversion to " + this + " not supported for " + from.getType() + "(" + from + ")");
                }
            }

            @Override
            public AttributeImpl getAttributeImpl(Attribute from)
            {
                if (from instanceof ListAttribute)
                {
                    return new ListAttributeImpl((ListAttribute)from);
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Conversion to " + this + " not supported for " + from.getType() + "(" + from + ")");
                }
            }
        };
        
        /**
         * Get the unpersisted attribute value implementation of the {@link Attribute} given an existing attribute.
         * The <tt>from</tt> attribute may be a persistable entity or not but a new instance will
         * be created.
         * <p>
         * No assumptions should be made about the return type.  The raw type might not match the persisted type.
         * 
         * @param from      the instance supplying the data
         * @return          Returns a value object based on the provided data
         */
        public abstract AttributeValue getAttributeValue(Attribute from);
        
        /**
         * Get a persistable implementation of the {@link Attribute} given an existing attribute.
         * The <tt>from</tt> attribute may be a persistable entity or not but a new instance will
         * be created.
         * <p>
         * No assumptions should be made about the return type.  It is possible that the data will
         * be converted to a different persistable type.
         * 
         * @param from      the instance supplying the data
         * @return          Returns a persistable entity based on the provided data
         */
        public abstract AttributeImpl getAttributeImpl(Attribute from);
    };
    
    /**
     * Set the ACL on this Attribute.
     * @param acl The ACL.
     */
    public void setAcl(DbAccessControlList acl);
    
    /**
     * Get the (possibly null ACL) on this Attribute.
     * @return The ACL or null.
     */
    public DbAccessControlList getAcl();
    
    /**
     * @return      the enumerated type
     */
    public Type getType();
    
    /**
     * Method to return the underlying raw data for possible conversion to the descired type.
     * @return          Returns a raw data value
     */
    public Serializable getRawValue();

    /**
     * @see Type#getAttributeValue(Attribute)
     */
    public abstract AttributeValue getAttributeValue();
    
    /**
     * @see Type#getAttributeImpl(Attribute)
     */
    public abstract AttributeImpl getAttributeImpl();

    /**
     * Set a boolean value.
     * @param value The value.
     */
    public void setBooleanValue(boolean value);
    
    /**
     * Get the value of a BooleanValue.
     * @return The value.
     */
    public boolean getBooleanValue();
    
    /**
     * Set a byte value.
     * @param value The value to set.
     */
    public void setByteValue(byte value);
    
    /**
     * Get the value of a ByteValue.
     * @return The value.
     */
    public byte getByteValue();
    
    /**
     * Set a short value.
     * @param value The value to set.
     */
    public void setShortValue(short value);
    
    /**
     * Get the value of a ShortValue.
     * @return The value.
     */
    public short getShortValue();
    
    /**
     * Set an integer value.
     * @param value The value to set.
     */
    public void setIntValue(int value);
    
    /**
     * Get the integer value of an IntValue.
     * @return The value.
     */
    public int getIntValue();
    
    /**
     * Set a long value.
     * @param value The value to set.
     */
    public void setLongValue(long value);
    
    /**
     * Get the long value of a LongValue.
     * @return The value.
     */
    public long getLongValue();
    
    /**
     * Set a float value.
     * @param value The value to set.
     */
    public void setFloatValue(float value);
    
    /**
     * Get the value of a FloatValue.
     * @return The value.
     */
    public float getFloatValue();
    
    /**
     * Set a double value.
     * @param value The value to set.
     */
    public void setDoubleValue(double value);
    
    /**
     * Get a double value from a DoubleValue.
     * @return The value.
     */
    public double getDoubleValue();
    
    /**
     * Set a String value.
     * @param value The value to set.
     */
    public void setStringValue(String value);
    
    /**
     * Get a String value from a StringValue.
     * @return The value.
     */
    public String getStringValue();
    
    /**
     * Set a Blob value.
     * @param value The value to set.
     */
    public void setBlobValue(byte[] value);
    
    /**
     * Get a Blob value from a BlobValue
     * @return The value.
     */
    public byte[] getBlobValue();
    
    /**
     * Set a Serializable value.
     * @param value
     */
    public void setSerializableValue(Serializable value);
    
    /**
     * Get a Seriailizable value from a SerializableValue
     * @return The value.
     */
    public Serializable getSerializableValue();
    
    /**
     * Clear a map.
     */
    public void clear();
    
    /**
     * Add an entry to a map.
     * @param key The key to the entry.
     * @param value The Value of the entry.
     */
    public void put(String key, Attribute value);
    
    /**
     * Get the Value for a key in a map.
     * @param key The key.
     * @return The value.
     */
    public Attribute get(String key);
    
    /**
     * Remove an entry by key from a map.
     * @param key The key of the entry to remove.
     */
    public void remove(String key);
    
    /**
     * Get the entry set for a map.
     * @return The entry set.
     */
    public Set<Map.Entry<String, Attribute>> entrySet(); 
    
    /**
     * Get the key set for a map.
     * @return The key set.
     */
    public Set<String> keySet();
    
    /**
     * Get the collection of values of a map.
     * @return The values.
     */
    public Collection<Attribute> values();
    
    /**
     * Add an attribute to a list attribute.
     * @param attr
     */
    public void add(Attribute attr);
    
    /**
     * Add an attribute to a list attribute at a given position.
     * @param index The offset.
     * @param attr The attribute.
     */
    public void add(int index, Attribute attr);
    
    /**
     * Get the size of a List of a Map.
     * @return
     */
    public int size();
    
    /**
     * Get an iterator over a list's entries.
     * @return
     */
    public Iterator<Attribute> iterator();
    
    /**
     * Get an Attribute from a List.
     * @param index The offset.
     * @return The Attribute or null.
     */
    public Attribute get(int index);
    
    /**
     * Remove an entry from a list.
     * @param index The entry to remove.
     */
    public void remove(int index);
    
    /**
     * Set an attribute in a list.
     * @param index The index to set.
     * @param value The attribute to set.
     */
    public void set(int index, Attribute value);
}
