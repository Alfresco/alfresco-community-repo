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
package org.alfresco.repo.domain.avm;

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.util.EqualsHelper;

/**
 * Entity bean for <b>avm_store_properties</b> table.
 * <p>
 * 
 * @author janv
 * @since 3.2
 */
public class AVMStorePropertyEntity extends PropertyValue
{
    private static final long serialVersionUID = -3125922581571555965L;
    
    private Long id;
    private Long avmStoreId;
    private Long qnameId;
    
    public AVMStorePropertyEntity()
    {
        // default constructor
    }
    
    public AVMStorePropertyEntity(long storeId, Long qnameId, PropertyValue value)
    {
        setAvmStoreId(storeId);
        setQnameId(qnameId);
        
        this.setActualType(value.getActualType());
        this.setAttributeValue(value.getAttributeValue());
        this.setBooleanValue(value.getBooleanValue());
        this.setDoubleValue(value.getDoubleValue());
        this.setFloatValue(value.getFloatValue());
        this.setLongValue(value.getLongValue());
        this.setMultiValued(value.isMultiValued());
        this.setPersistedType(value.getPersistedType());
        this.setSerializableValue(value.getSerializableValue());
        this.setStringValue(value.getStringValue());
    }
    
    public Long getId()
    {
        return id;
    }
    public void setId(Long id)
    {
        this.id = id;
    }
    public Long getAvmStoreId()
    {
        return avmStoreId;
    }
    public void setAvmStoreId(Long avmStoreId)
    {
        this.avmStoreId = avmStoreId;
    }
    public Long getQnameId()
    {
        return qnameId;
    }
    public void setQnameId(Long qnameId)
    {
        this.qnameId = qnameId;
    }
    
    @Override
    public int hashCode()
    {
        return ((avmStoreId == null ? 0 : avmStoreId.hashCode()) +
                (qnameId == null ? 0 : qnameId.hashCode()));
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof AVMStorePropertyEntity)
        {
            AVMStorePropertyEntity that = (AVMStorePropertyEntity) obj;
            return (EqualsHelper.nullSafeEquals(this.avmStoreId, that.avmStoreId) &&
                    EqualsHelper.nullSafeEquals(this.qnameId, that.qnameId));
        }
        else
        {
            return false;
        }
    }
}
