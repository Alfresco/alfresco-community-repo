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
package org.alfresco.util;

import java.util.Date;

import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

public class JMXUtils
{
    public static OpenType<?> getOpenType(Object o)
    {
        if(o instanceof Long)
        {
            return SimpleType.LONG;
        }
        else if(o instanceof String)
        {
            return SimpleType.STRING;
        }
        else if(o instanceof Date)
        {
            return SimpleType.DATE;
        }
        else if(o instanceof Integer)
        {
            return SimpleType.INTEGER;
        }
        else if(o instanceof Boolean)
        {
            return SimpleType.BOOLEAN;
        }
        else if(o instanceof Double)
        {
            return SimpleType.DOUBLE;
        }
        else if(o instanceof Float)
        {
            return SimpleType.FLOAT;
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }
}
