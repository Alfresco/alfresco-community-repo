/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

package org.alfresco.rest.api.model;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;

/**
 *
 *
 * @author Jamal Kaabi-Mofrad
 */
public class CustomModelNamedValue implements Comparable<CustomModelNamedValue>
{
    private String name;
    private String simpleValue = null;
    private List<String> listValue = null;

    public CustomModelNamedValue()
    {
    }

    public CustomModelNamedValue(String name, Object value)
    {
        this.name = name;
        if (value instanceof List<?>)
        {
            List<?> values = (List<?>) value;
            listValue = new ArrayList<>(values.size());
            for(Object val : values)
            {
                listValue.add(convertToString(val));
            }
        }
        else
        {
            simpleValue = convertToString(value);
        }
    }

    private String convertToString(Object value)
    {
        try
        {
            return DefaultTypeConverter.INSTANCE.convert(String.class, value);
        }
        catch (TypeConversionException e)
        {
            throw new InvalidArgumentException("Cannot convert to string '" + value + "'.");
        }
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getSimpleValue()
    {
        return this.simpleValue;
    }

    public void setSimpleValue(String simpleValue)
    {
        this.simpleValue = simpleValue;
    }

    public List<String> getListValue()
    {
        return this.listValue;
    }

    public void setListValue(List<String> listValue)
    {
        this.listValue = listValue;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof CustomModelNamedValue))
        {
            return false;
        }
        CustomModelNamedValue other = (CustomModelNamedValue) obj;
        if (this.name == null)
        {
            if (other.name != null)
            {
                return false;
            }
        }
        else if (!this.name.equals(other.name))
        {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(CustomModelNamedValue other)
    {
        return name.compareTo(other.getName());
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(120);
        builder.append("CustomModelNamedValue [name=").append(this.name)
                    .append(", simpleValue=").append(this.simpleValue)
                    .append(", listValue=").append(this.listValue)
                    .append(']');
        return builder.toString();
    }
}
