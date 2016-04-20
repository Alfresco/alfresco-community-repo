/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.web.scripts.solr;

/**
 * Represents a property value to be used by Freemarker
 * 
 * @since 4.0
 */
class PropertyValue
{
    // is value actually a string or a JSON object or array
    // if true, enclose the value in double quotes (to represent a JSON string)
    // when converting to a string.
    private boolean isString = true;
    
    private String value;
    
    public PropertyValue(boolean isString, String value)
    {
        super();
        this.isString = isString;
        this.value = value;
    }
    public boolean isString()
    {
        return isString;
    }
    public String getValue()
    {
        return value;
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if(isString)
        {
            sb.append("\""); // for json strings
        }
        sb.append(value);
        if(isString)
        {
            sb.append("\""); // for json strings
        }
        return sb.toString();
    }
}
