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
import java.util.Set;
import java.util.Map.Entry;

/**
 * The base class of the implementation of Values.
 * @author britt
 */
public abstract class ValueImpl implements Value 
{
    /**
     * The primary key.
     */
    private long fID;
    
    /**
     * Base constructor.
     */
    protected ValueImpl()
    {
    }
    
    public void setId(long id)
    {
        fID = id;
    }
    
    public long getId()
    {
        return fID;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#add(org.alfresco.repo.attributes.Value)
     */
    public void add(Value value) 
    {
        throw new ValueMethodNotImplementedException("Not ListValue");        
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#add(int, org.alfresco.repo.attributes.Value)
     */
    public void add(int index, Value value) 
    {
        throw new ValueMethodNotImplementedException("Not ListValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#clear()
     */
    public void clear() 
    {
        throw new ValueMethodNotImplementedException("Not ListValue or MapValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#entrySet()
     */
    public Set<Entry<String, Value>> entrySet() 
    {
        throw new ValueMethodNotImplementedException("Not MapValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#get(int)
     */
    public Value get(int index) 
    {
        throw new ValueMethodNotImplementedException("Not ListValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#get(java.lang.String)
     */
    public Value get(String key) 
    {
        throw new ValueMethodNotImplementedException("Not MapValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#getAsObject()
     */
    public Object getAsObject() 
    {
        throw new ValueMethodNotImplementedException("Not implemented in base class");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#getBlobValue()
     */
    public byte[] getBlobValue() 
    {
        throw new ValueMethodNotImplementedException("Not BlobValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#getDoubleValue()
     */
    public double getDoubleValue() 
    {
        throw new ValueMethodNotImplementedException("Not DoubleValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#getIntValue()
     */
    public int getIntValue() 
    {
        throw new ValueMethodNotImplementedException("Not IntValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#getLongValue()
     */
    public long getLongValue() 
    {
        throw new ValueMethodNotImplementedException("Not LongValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#getSerializableValue()
     */
    public Serializable getSerializableValue() 
    {
        throw new ValueMethodNotImplementedException("Not SerializableValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#getStringValue()
     */
    public String getStringValue() 
    {
        throw new ValueMethodNotImplementedException("Not StringValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#keySet()
     */
    public Set<String> keySet() 
    {
        throw new ValueMethodNotImplementedException("Not MapValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#put(java.lang.String, org.alfresco.repo.attributes.Value)
     */
    public void put(String key, Value value) 
    {
        throw new ValueMethodNotImplementedException("Not MapValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#remove(int)
     */
    public void remove(int index) 
    {
        throw new ValueMethodNotImplementedException("Not ListValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#remove(java.lang.String)
     */
    public void remove(String key) 
    {
        throw new ValueMethodNotImplementedException("Not MapValue");    
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#setBlobValue(byte[])
     */
    public void setBlobValue(byte[] value) 
    {
        throw new ValueMethodNotImplementedException("Not BlobValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#setDoubleValue(double)
     */
    public void setDoubleValue(double value) 
    {
        throw new ValueMethodNotImplementedException("Not DoubleValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#setIntValue(int)
     */
    public void setIntValue(int value) 
    {
        throw new ValueMethodNotImplementedException("Not IntValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#setLongValue(long)
     */
    public void setLongValue(long value) 
    {
        throw new ValueMethodNotImplementedException("Not LongValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#setSerializableValue(java.io.Serializable)
     */
    public void setSerializableValue(Serializable value) 
    {
        throw new ValueMethodNotImplementedException("Not SerializableValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#setStringValue(java.lang.String)
     */
    public void setStringValue(String value) 
    {
        throw new ValueMethodNotImplementedException("Not StringValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#values()
     */
    public Collection<Value> values() 
    {
        throw new ValueMethodNotImplementedException("Not MapValue");
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<Value> iterator() 
    {
        throw new ValueMethodNotImplementedException("Not ListValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#getBooleanValue()
     */
    public boolean getBooleanValue() 
    {
        throw new ValueMethodNotImplementedException("Not BooleanValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#getByteValue()
     */
    public byte getByteValue() 
    {
        throw new ValueMethodNotImplementedException("Not ByteValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#getFloatValue()
     */
    public float getFloatValue() 
    {
        throw new ValueMethodNotImplementedException("Not FloatValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#getShortValue()
     */
    public short getShortValue() 
    {
        throw new ValueMethodNotImplementedException("Not ShortValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#setBooleanValue(boolean)
     */
    public void setBooleanValue(boolean value) 
    {
        throw new ValueMethodNotImplementedException("Not BooleanValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#setByteValue(byte)
     */
    public void setByteValue(byte value) 
    {
        throw new ValueMethodNotImplementedException("Not ByteValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#setFloatValue(float)
     */
    public void setFloatValue(float value) 
    {
        throw new ValueMethodNotImplementedException("Not FloatValue");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Value#setShortValue(short)
     */
    public void setShortValue(short value) 
    {
        throw new ValueMethodNotImplementedException("Not ShortValue");
    }
}
