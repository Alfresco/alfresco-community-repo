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
package org.alfresco.util.schemacomp.model;

import org.alfresco.util.schemacomp.Differences;
import org.alfresco.util.schemacomp.SchemaUtils;
import org.springframework.util.StringUtils;

/**
 * Useful base class for many, if not all the {@link DbObject} implementations.
 * 
 * @author Matt Ward
 */
public abstract class AbstractDbObject implements DbObject
{
    private String name;

    /**
     * Default constructor
     */
    public AbstractDbObject()
    {
    }

    /**
     * Instantiate, giving the object a name.
     * 
     * @param name
     */
    public AbstractDbObject(String name)
    {
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    @Override
    public boolean sameAs(DbObject other)
    {
        if (getName() != null && other != null && other.getName() != null)
        {
            return getName().equals(other.getName());
        }
        else
        {
            // Only other way we can know if they are the same is if they are
            // the exact same object reference.
            return this == other;
        }
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
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AbstractDbObject other = (AbstractDbObject) obj;
        if (this.name == null)
        {
            if (other.name != null) return false;
        }
        else if (!this.name.equals(other.name)) return false;
        return true;
    }
    
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getSimpleName());
        sb.append("[name=");
        
        if (getName() != null)
        {
            sb.append(getName());
        }
        else
        {
            sb.append("null");
        }
        
        sb.append("]");
        
        return sb.toString();
    }

    /**
     * Provides an implementation of {@link DbObject#diff(DbObject, Differences)}. The template
     * method {@link #doDiff(DbObject, Differences)} provides the subclass specific diffing logic,
     * whilst this method handles the workflow required in most cases: set the path's prefix that will be
     * used to explain where differences occur; compare the name fields of the two objects; delegate to the
     * subclass specific diffing (if any); remove the last path addition ready for the next object to perform
     * its diff correctly.
     */
    @Override
    public void diff(DbObject right, Differences differences)
    {
        if (name != null && StringUtils.hasText(name))
        {
            differences.pushPath(name);
        }
        else
        {
            differences.pushPath("<" + getClass().getSimpleName() + ">");
        }
        SchemaUtils.compareSimple(name, right.getName(), differences);
        doDiff(right, differences);
        differences.popPath();
    }


    /**
     * Override this method to provide subclass specific diffing logic.
     * 
     * @param right
     * @param differences
     */
    protected void doDiff(DbObject right, Differences differences)
    {
    }
}
