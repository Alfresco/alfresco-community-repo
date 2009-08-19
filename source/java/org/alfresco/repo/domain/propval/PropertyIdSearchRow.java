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
package org.alfresco.repo.domain.propval;

import java.io.Serializable;

/**
 * Entity bean search results from <b>alf_prop_collections_link</b> and <b>alf_prop_value</b> tables.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PropertyIdSearchRow
{
    private final PropertyLinkEntity linkEntity;
    private final PropertyValueEntity valueEntity;
    
    public PropertyIdSearchRow()
    {
        linkEntity = new PropertyLinkEntity();
        valueEntity = new PropertyValueEntity();
    }
    
//    @Override
//    public int hashCode()
//    {
//        return (int) rootCollectionPropId + (int) valuePropId;
//    }
//    
//    @Override
//    public boolean equals(Object obj)
//    {
//        if (this == obj)
//        {
//            return true;
//        }
//        else if (obj instanceof PropertyCollectionLinkEntity)
//        {
//            PropertyCollectionLinkEntity that = (PropertyCollectionLinkEntity) obj;
//            return
//                    this.rootCollectionPropId == that.rootCollectionPropId &&
//                    this.currentCollectionPropId == that.currentCollectionPropId &&
//                    this.valuePropId == that.valuePropId &&
//                    this.keyPropId == that.keyPropId;
//        }
//        else
//        {
//            return false;
//        }
//    }
//    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("PropertyIdSearchRow")
          .append("[ ").append(linkEntity)
          .append(", ").append(valueEntity)
          .append("]");
        return sb.toString();
    }

    public PropertyLinkEntity getLinkEntity()
    {
        return linkEntity;
    }

    public PropertyValueEntity getValueEntity()
    {
        return valueEntity;
    }

    public void setRootPropId(long rootPropId)
    {
        linkEntity.setRootPropId(rootPropId);
    }

    public void setCurrentPropId(long currentPropId)
    {
        linkEntity.setCurrentPropId(currentPropId);
    }

    public void setValuePropId(long valuePropId)
    {
        linkEntity.setValuePropId(valuePropId);
    }

    public void setKeyPropId(long keyPropId)
    {
        linkEntity.setKeyPropId(keyPropId);
    }

    public void setActualTypeId(Long actualTypeId)
    {
        valueEntity.setActualTypeId(actualTypeId);
    }

    public void setPersistedType(Short persistedType)
    {
        valueEntity.setPersistedType(persistedType);
    }

    public void setLongValue(Long longValue)
    {
        valueEntity.setLongValue(longValue);
    }

    public void setStringValue(String stringValue)
    {
        valueEntity.setStringValue(stringValue);
    }

    public void setDoubleValue(Double doubleValue)
    {
        valueEntity.setDoubleValue(doubleValue);
    }

    public void setSerializableValue(Serializable serializableValue)
    {
        valueEntity.setSerializableValue(serializableValue);
    }
}
