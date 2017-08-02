/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rm.rest.api.properties;

/**
 * POJO representing a property key/value in the alfresco-global.properties file
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
public class Property
{
    /**
     * Property key
     */
    private String key = null;

    /**
     * Property value
     */
    private Object value = null;

    /**
     * Empty constructor needed for the REST API
     */
    public Property()
    {
    }

    /**
     * Constructor
     */
    public Property(String key, Object value)
    {
        this.key = key;
        this.value = value;
    }

    /**
     * Gets the property key
     *
     * @return The property key
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Sets the property key
     *
     * @param key The property key to set
     */
    public void setKey(String key)
    {
        this.key = key;
    }

    /**
     * Get the property value
     *
     * @return The property value
     */
    public Object getValue()
    {
        return value;
    }

    /**
     * Sets the property value
     *
     * @param value The property value
     */
    public void setValue(Object value)
    {
        this.value = value;
    }

    /**
     * Equals implementation for the property
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Property property = (Property) o;

        if (!key.equals(property.key))
        {
            return false;
        }
        return value.equals(property.value);
    }

    /**
     * hashCode implementation for the property
     */
    @Override
    public int hashCode()
    {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    /**
     * toString implementation for the property
     */
    @Override
    public String toString()
    {
        return "Property{" + "key='" + key + '\'' + ", value=" + value + '}';
    }
}
