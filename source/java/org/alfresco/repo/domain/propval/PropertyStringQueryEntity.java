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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.domain.propval;

import org.alfresco.repo.domain.CrcHelper;
import org.alfresco.util.Pair;

/**
 * Entity bean for querying against the <b>alf_prop_string_value</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PropertyStringQueryEntity
{
    private final Short persistedType;
    private final Long actualTypeId;
    private final String stringValue;
    private final String stringEndLower;
    private final Long stringCrc;
    
    public PropertyStringQueryEntity(Short persistedType, Long actualTypeId, String value)
    {
        this.persistedType = persistedType;
        this.actualTypeId = actualTypeId;
        
        stringValue = value;
        // Calculate the crc value from the original value
        Pair<String, Long> crcPair = CrcHelper.getStringCrcPair(value, 16, false, true);
        stringEndLower = crcPair.getFirst();
        stringCrc = crcPair.getSecond();
    }
    
    public Short getPersistedType()
    {
        return persistedType;
    }

    public Long getActualTypeId()
    {
        return actualTypeId;
    }

    public String getStringValue()
    {
        return stringValue;
    }

    public String getStringEndLower()
    {
        return stringEndLower;
    }

    public Long getStringCrc()
    {
        return stringCrc;
    }
}
