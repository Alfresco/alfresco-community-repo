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
 * Entity bean for <b>alf_prop_link</b> table.
 * 
 * @author Derek Hulley
 * @since 3.3
 */
public class PropertyLinkEntity
{
    private long rootPropId;
    private long currentPropId;
    private long valuePropId;
    private long keyPropId;
    
    public PropertyLinkEntity()
    {
    }
    
    @Override
    public int hashCode()
    {
        return (int) rootPropId + (int) valuePropId;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof PropertyLinkEntity)
        {
            PropertyLinkEntity that = (PropertyLinkEntity) obj;
            return
                    this.rootPropId == that.rootPropId &&
                    this.currentPropId == that.currentPropId &&
                    this.valuePropId == that.valuePropId &&
                    this.keyPropId == that.keyPropId;
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
        sb.append("PropertyLinkEntity")
          .append("[ rootPropId=").append(rootPropId)
          .append(", currentPropId=").append(currentPropId)
          .append(", valuePropId=").append(valuePropId)
          .append(", keyPropId=").append(keyPropId)
          .append("]");
        return sb.toString();
    }

    public long getRootPropId()
    {
        return rootPropId;
    }

    public void setRootPropId(long rootPropId)
    {
        this.rootPropId = rootPropId;
    }

    public long getCurrentPropId()
    {
        return currentPropId;
    }

    public void setCurrentPropId(long currentPropId)
    {
        this.currentPropId = currentPropId;
    }

    public long getValuePropId()
    {
        return valuePropId;
    }

    public void setValuePropId(long valuePropId)
    {
        this.valuePropId = valuePropId;
    }

    public long getKeyPropId()
    {
        return keyPropId;
    }

    public void setKeyPropId(long keyPropId)
    {
        this.keyPropId = keyPropId;
    }
}
