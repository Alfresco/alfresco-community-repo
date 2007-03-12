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

package org.alfresco.repo.attributes;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Interface for polymorphic attributes.
 * @author britt
 */
public interface Value extends Iterable<Value>
{
    public static enum Type implements Serializable
    {
        BYTE,
        SHORT,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        STRING,
        BLOB,
        SERIALIZABLE,
        LIST,
        MAP
    };
    
    /**
     * Get the value type for this node.
     * @return
     */
    public Type getType();

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
     * Add a Value to a list.
     * @param value The value to add.
     */
    public void add(Value value);
    
    /**
     * Add a Value to a list at the given index.
     * @param index The offset.
     * @param value The value to add. 
     */
    public void add(int index, Value value);
    
    /**
     * Get the value at the given index of a list.
     * @param index The offset.
     * @return The value.
     */
    public Value get(int index);
    
    /**
     * Remove the given entry from a list.
     * @param index The offset to remove.
     */
    public void remove(int index);
    
    /**
     * Clear a list or a map.
     */
    public void clear();
    
    /**
     * Add an entry to a map.
     * @param key The key to the entry.
     * @param value The Value of the entry.
     */
    public void put(String key, Value value);
    
    /**
     * Get the Value for a key in a map.
     * @param key The key.
     * @return The value.
     */
    public Value get(String key);
    
    /**
     * Remove an entry by key from a map.
     * @param key The key of the entry to remove.
     */
    public void remove(String key);
    
    /**
     * Get the entry set for a map.
     * @return The entry set.
     */
    public Set<Map.Entry<String, Value>> entrySet(); 
    
    /**
     * Get the key set for a map.
     * @return The key set.
     */
    public Set<String> keySet();
    
    /**
     * Get the collection of values of a map.
     * @return The values.
     */
    public Collection<Value> values();
    
    /**
     * Get (possibly recursively) the Value as an Object. The returned
     * value is a copy of the Value using standard java Integers, Longs, Doubles, Strings,
     * byte[]s, Maps, and Lists.
     * @return The Object value.
     */
    public Object getAsObject();
}
