/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */

package org.alfresco.repo.avm;

import java.io.Serializable;

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.namespace.QName;

/**
 * Simple bean to hold properties attached to AVMStores.
 * @author britt
 */
class AVMStorePropertyImpl implements AVMStoreProperty, Serializable
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
    private QName fName;
    
    /**
     * The actual PropertyValue.
     */
    private PropertyValue fValue;
    
    public AVMStorePropertyImpl()
    {
    }

    /**
     * Get the name of the property.
     * @return The QName for the property.
     */
    public QName getName()
    {
        return fName;
    }

    /**
     * Set the name of the property.
     * @param name The QName of the property.
     */
    public void setName(QName name)
    {
        fName = name;
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
        return fStore.equals(o.getStore()) && fName.equals(o.getName());
    }
    
    @Override
    public int hashCode()
    {
        return fStore.hashCode() + fName.hashCode();
    }
}

