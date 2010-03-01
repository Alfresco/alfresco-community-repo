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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm;

import java.io.Serializable;

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.namespace.QName;

/**
 * Simple bean to hold properties attached to AVMStores.
 * @author britt
 */
public class AVMStorePropertyImpl implements AVMStoreProperty, Serializable
{
    private static final long serialVersionUID = -5419606158990318723L;

    /**
     * The Primary Key.
     */
    private Long fID;
    
    /**
     * The store that owns this property.
     */
    private AVMStore fStore;
    
    /**
     * The name of the property.
     */
    private QName qname;
    
    /**
     * The actual PropertyValue.
     */
    private PropertyValue fValue;
    
    public AVMStorePropertyImpl()
    {
    }

    /**
     * {@inheritDoc}
     */
    public QName getQname()
    {
        return qname;
    }

    /**
     * {@inheritDoc}
     */
    public void setQname(QName qname)
    {
        this.qname = qname;
    }

    /**
     * Get the store this property belongs to.
     * @return The AVMStore that owns this.
     */
    public AVMStore getStore()
    {
        return fStore;
    }

    /**
     * Set the store that this property belongs to.
     * @param store The AVMStore.
     */
    public void setStore(AVMStore store)
    {
        fStore = store;
    }

    /**
     * Get the actual property value.
     * @return A PropertyValue object.
     */
    public PropertyValue getValue()
    {
        return fValue;
    }

    /**
     * Set the actual property value.
     * @param value The PropertyValue to set.
     */
    public void setValue(PropertyValue value)
    {
        fValue = value;
    }
    
    /**
     * Set the primary key. (For Hibernate)
     * @param id The primary key.
     */
    protected void setId(Long id)
    {
        fID = id;
    }
    
    /**
     * Get the primary key. (For Hibernate)
     * @return The primary key.
     */
    protected Long getId()
    {
        return fID;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof AVMStoreProperty))
        {
            return false;
        }
        AVMStoreProperty o = (AVMStoreProperty)other;
        return fStore.equals(o.getStore()) && qname.equals(o.getQname());
    }
    
    @Override
    public int hashCode()
    {
        return fStore.hashCode() + qname.hashCode();
    }
}

