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
 * http://www.alfresco.com/legal/licensing
 */

package org.alfresco.repo.attributes;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.alfresco.repo.domain.DbAccessControlList;

/**
 * Value based non-persistent implementation of Attribute.
 * @author britt
 */
public abstract class AttributeValue implements Attribute
{
    /**
     * ACL for this Attribute
     */
    private DbAccessControlList fACL;
    
    public AttributeValue()
    {
    }
    
    /**
     * Helper for copy constructors.
     */
    public AttributeValue(DbAccessControlList acl)
    {
        fACL = acl;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#clear()
     */
    public void clear()
    {
        throw new AttributeMethodNotImplemented("Not a Map.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#entrySet()
     */
    public Set<Entry<String, Attribute>> entrySet()
    {
        throw new AttributeMethodNotImplemented("Not a Map.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#get(java.lang.String)
     */
    public Attribute get(String key)
    {
        throw new AttributeMethodNotImplemented("Not a Map.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#getBlobValue()
     */
    public byte[] getBlobValue()
    {
        throw new AttributeMethodNotImplemented("Not a Blob.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#getBooleanValue()
     */
    public boolean getBooleanValue()
    {
        throw new AttributeMethodNotImplemented("Not a boolean.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#getByteValue()
     */
    public byte getByteValue()
    {
        throw new AttributeMethodNotImplemented("Not a byte.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#getDoubleValue()
     */
    public double getDoubleValue()
    {
        throw new AttributeMethodNotImplemented("Not a double.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#getFloatValue()
     */
    public float getFloatValue()
    {
        throw new AttributeMethodNotImplemented("Not a float.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#getIntValue()
     */
    public int getIntValue()
    {
        throw new AttributeMethodNotImplemented("Not an int.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#getLongValue()
     */
    public long getLongValue()
    {
        throw new AttributeMethodNotImplemented("Not a long.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#getSerializableValue()
     */
    public Serializable getSerializableValue()
    {
        throw new AttributeMethodNotImplemented("Not a Serializable.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#getShortValue()
     */
    public short getShortValue()
    {
        throw new AttributeMethodNotImplemented("Not a short.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#getStringValue()
     */
    public String getStringValue()
    {
        throw new AttributeMethodNotImplemented("Not a String.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#keySet()
     */
    public Set<String> keySet()
    {
        throw new AttributeMethodNotImplemented("Not a map.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#put(java.lang.String, org.alfresco.repo.attributes.Attribute)
     */
    public void put(String key, Attribute value)
    {
        throw new AttributeMethodNotImplemented("Not a map.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#remove(java.lang.String)
     */
    public void remove(String key)
    {
        throw new AttributeMethodNotImplemented("Not a map.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#setBlobValue(byte[])
     */
    public void setBlobValue(byte[] value)
    {
        throw new AttributeMethodNotImplemented("Not a Blob.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#setBooleanValue(boolean)
     */
    public void setBooleanValue(boolean value)
    {
        throw new AttributeMethodNotImplemented("Not a boolean.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#setByteValue(byte)
     */
    public void setByteValue(byte value)
    {
        throw new AttributeMethodNotImplemented("Not a byte.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#setDoubleValue(double)
     */
    public void setDoubleValue(double value)
    {
        throw new AttributeMethodNotImplemented("Not a double.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#setFloatValue(float)
     */
    public void setFloatValue(float value)
    {
        throw new AttributeMethodNotImplemented("Not a float.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#setIntValue(int)
     */
    public void setIntValue(int value)
    {
        throw new AttributeMethodNotImplemented("Not an int.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#setLongValue(long)
     */
    public void setLongValue(long value)
    {
        throw new AttributeMethodNotImplemented("Not a long.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#setSerializableValue(java.io.Serializable)
     */
    public void setSerializableValue(Serializable value)
    {
        throw new AttributeMethodNotImplemented("Not a Serializable.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#setShortValue(short)
     */
    public void setShortValue(short value)
    {
        throw new AttributeMethodNotImplemented("Not a short.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#setStringValue(java.lang.String)
     */
    public void setStringValue(String value)
    {
        throw new AttributeMethodNotImplemented("Not a String.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#values()
     */
    public Collection<Attribute> values()
    {
        throw new AttributeMethodNotImplemented("Not a map.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#getAcl()
     */
    public DbAccessControlList getAcl()
    {
        return fACL;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#setAcl(org.alfresco.repo.domain.DbAccessControlList)
     */
    public void setAcl(DbAccessControlList acl)
    {
        fACL = acl;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#add(org.alfresco.repo.attributes.Attribute)
     */
    public void add(Attribute attr)
    {
        throw new AttributeMethodNotImplemented("Not a List.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#add(int, org.alfresco.repo.attributes.Attribute)
     */
    public void add(int index, Attribute attr)
    {
        throw new AttributeMethodNotImplemented("Not a List.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#iterator()
     */
    public Iterator<Attribute> iterator()
    {
        throw new AttributeMethodNotImplemented("Not a List.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#size()
     */
    public int size()
    {
        throw new AttributeMethodNotImplemented("Not a List or Map.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#get(int)
     */
    public Attribute get(int index)
    {
        throw new AttributeMethodNotImplemented("Not a List.");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#remove(int)
     */
    public void remove(int index)
    {
        throw new AttributeMethodNotImplemented("Not a List.");
    }
}
