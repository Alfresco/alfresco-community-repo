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

import org.alfresco.util.EqualsHelper;
import org.springframework.extensions.surf.util.Pair;

/**
 * Entity bean for <b>alf_prop_numeric_value</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PropertyDoubleValueEntity
{
    private Long id;
    private Double doubleValue;
    
    public PropertyDoubleValueEntity()
    {
    }
    
    @Override
    public int hashCode()
    {
        return (doubleValue == null ? 0 : doubleValue.hashCode()); 
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj != null && obj instanceof PropertyDoubleValueEntity)
        {
            PropertyDoubleValueEntity that = (PropertyDoubleValueEntity) obj;
            return EqualsHelper.nullSafeEquals(this.doubleValue, that.doubleValue);
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
        sb.append("PropertyNumericValueEntity")
          .append("[ ID=").append(id)
          .append(", value=").append(doubleValue)
          .append("]");
        return sb.toString();
    }
    
    /**
     * @return          Returns the ID-value pair
     */
    public Pair<Long, Double> getEntityPair()
    {
        return new Pair<Long, Double>(id, doubleValue);
    }
    
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public double getDoubleValue()
    {
        return doubleValue;
    }

    public void setDoubleValue(Double doubleValue)
    {
        this.doubleValue = doubleValue;
    }
}
