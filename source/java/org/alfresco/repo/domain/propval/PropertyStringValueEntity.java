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

import org.alfresco.repo.domain.CrcHelper;
import org.alfresco.util.EqualsHelper;
import org.springframework.extensions.surf.util.Pair;

/**
 * Entity bean for <b>alf_prop_string_value</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PropertyStringValueEntity
{
    private Long id;
    private String stringValue;
    private String stringEndLower;
    private Long stringCrc;
    
    public PropertyStringValueEntity()
    {
    }
    
    @Override
    public int hashCode()
    {
        return (stringValue == null ? 0 : stringValue.hashCode());
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj != null && obj instanceof PropertyStringValueEntity)
        {
            PropertyStringValueEntity that = (PropertyStringValueEntity) obj;
            return EqualsHelper.nullSafeEquals(this.stringValue, that.stringValue);
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
        sb.append("PropertyStringValueEntity")
          .append("[ ID=").append(id)
          .append(", stringValue=").append(stringValue)
          .append("]");
        return sb.toString();
    }
    
    public Pair<Long, String> getEntityPair()
    {
        return new Pair<Long, String>(id, stringValue);
    }
    
    /**
     * Set the string and string-end values
     */
    public void setValue(String value)
    {
        if (value == null)
        {
            throw new IllegalArgumentException("Null strings cannot be persisted");
        }
        stringValue = value;
        // Calculate the crc value from the original value
        Pair<String, Long> crcPair = CrcHelper.getStringCrcPair(value, 16, false, true);
        stringEndLower = crcPair.getFirst();
        stringCrc = crcPair.getSecond();
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getStringValue()
    {
        return stringValue;
    }

    public void setStringValue(String stringValue)
    {
        this.stringValue = stringValue;
    }
    
    public String getStringEndLower()
    {
        return stringEndLower;
    }

    public void setStringEndLower(String stringEndLower)
    {
        this.stringEndLower = stringEndLower;
    }

    public Long getStringCrc()
    {
        return stringCrc;
    }

    public void setStringCrc(Long stringCrc)
    {
        this.stringCrc = stringCrc;
    }
}
