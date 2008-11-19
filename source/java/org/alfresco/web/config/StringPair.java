/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.config;

/**
 * This immutable class represents a pair of Strings, such as a Name-Value pair. A null
 * value is allowed, but a null name is not.
 * 
 * @author Neil McErlean.
 */
public class StringPair implements Comparable<StringPair>
{
    private final String name;
    private final String value;
    
    public StringPair(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    public String getName()
    {
        return this.name;
    }
    
    public String getValue()
    {
        return this.value;
    }
    
    public int compareTo(StringPair otherStringPair)
    {
        if (otherStringPair == null)
        {
            throw new NullPointerException("Cannot compareTo null.");
        }
        if (this.equals(otherStringPair))
        {
            return 0;
        }
        return this.toString().compareTo(otherStringPair.toString());
    }
    
    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == null || !otherObj.getClass().equals(this.getClass()))
        {
            return false;
        }
        StringPair otherStringPair = (StringPair)otherObj;
        
        // These String.valueOf calls will protect us from NPEs in the case of
        // null values.
        return this.name.equals(otherStringPair.name)
               && String.valueOf(this.value).equals(String.valueOf(otherStringPair.value));
    }

    @Override
    public int hashCode()
    {
        int valueHashCode = 0;
        if (value != null)
        {
            valueHashCode = value.hashCode();
        }
        return name.hashCode() + 7 * valueHashCode;
    }
    
    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append(name).append("=").append(value);
        return result.toString();
    }
}
