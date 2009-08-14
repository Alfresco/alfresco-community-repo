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

/**
 * Entity bean for <b>alf_prop_collections_link</b> table.
 * 
 * @author Derek Hulley
 * @since 3.3
 */
public class PropertyCollectionLinkEntity
{
    private long rootCollectionPropId;
    private long currentCollectionPropId;
    private long keyPropId;
    private long valuePropId;
    
    public PropertyCollectionLinkEntity()
    {
    }
    
    @Override
    public int hashCode()
    {
        return (int) rootCollectionPropId + (int) valuePropId;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof PropertyCollectionLinkEntity)
        {
            PropertyCollectionLinkEntity that = (PropertyCollectionLinkEntity) obj;
            return
                    this.rootCollectionPropId == that.rootCollectionPropId &&
                    this.currentCollectionPropId == that.currentCollectionPropId &&
                    this.keyPropId == that.keyPropId &&
                    this.valuePropId == that.valuePropId;
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("PropertyCollectionsLinkEntity")
          .append("[ currentCollectionPropId=").append(currentCollectionPropId)
          .append(", keyPropId=").append(keyPropId)
          .append(", valuePropId=").append(valuePropId)
          .append("]");
        return sb.toString();
    }

    public long getRootCollectionPropId()
    {
        return rootCollectionPropId;
    }

    public void setRootCollectionPropId(long rootCollectionPropId)
    {
        this.rootCollectionPropId = rootCollectionPropId;
    }

    public long getCurrentCollectionPropId()
    {
        return currentCollectionPropId;
    }

    public void setCurrentCollectionPropId(long currentCollectionPropId)
    {
        this.currentCollectionPropId = currentCollectionPropId;
    }

    public long getKeyPropId()
    {
        return keyPropId;
    }

    public void setKeyPropId(long keyPropId)
    {
        this.keyPropId = keyPropId;
    }

    public long getValuePropId()
    {
        return valuePropId;
    }

    public void setValuePropId(long valuePropId)
    {
        this.valuePropId = valuePropId;
    }
}
