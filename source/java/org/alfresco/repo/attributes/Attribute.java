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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.DbAccessControlList;

/**
 * Interface for polymorphic attributes.
 * @author britt
 */
public interface Attribute extends Serializable, Iterable<Attribute>
{
    public static enum Type implements Serializable
    {
        BOOLEAN,
        BYTE,
        SHORT,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        STRING,
        SERIALIZABLE,
        MAP,
        LIST
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
}
