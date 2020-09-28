/*
 * #%L
 * Alfresco Repository
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

package org.alfresco.repo.virtual.ref;

/**
 * Generic value of type <code>V</code> holder parameter.
 * 
 * @param <V>
 */
public abstract class ValueParameter<V> implements Parameter
{
    private V value;

    public ValueParameter(V value)
    {
        super();
        this.value = value;
    }

    public V getValue()
    {
        return value;
    }

    @Override
    public int hashCode()
    {
        return this.value != null ? this.value.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj == null)
        {
            return false;
        }
        else if (!(getClass().equals(obj.getClass())))
        {
            return false;
        }

        if (obj instanceof ValueParameter<?>)
        {
            ValueParameter<?> other = (ValueParameter<?>) obj;

            if (value == null)
            {
                return other.value == null;
            }
            else
            {
                return this.value.equals(other.value);
            }
        }
        else
        {
            return false;
        }
    }
}
