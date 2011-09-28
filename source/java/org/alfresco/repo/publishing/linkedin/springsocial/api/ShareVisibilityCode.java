/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.publishing.linkedin.springsocial.api;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * 
 * @author Brian
 * @since 4.0
 */
@XmlEnum
public enum ShareVisibilityCode
{

    @XmlEnumValue("anyone")
    ANYONE("anyone"), 
    
    @XmlEnumValue("connections-only")
    CONNECTIONS_ONLY("connections-only");
    
    private final String value;

    ShareVisibilityCode(String value)
    {
        this.value = value;
    }

    public String value()
    {
        return value;
    }

    public static ShareVisibilityCode fromValue(String value)
    {
        for (ShareVisibilityCode validCode : ShareVisibilityCode.values())
        {
            if (validCode.value.equals(value))
            {
                return validCode;
            }
        }
        throw new IllegalArgumentException(value);
    }

}
