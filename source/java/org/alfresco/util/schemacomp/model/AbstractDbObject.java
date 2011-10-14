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

/**
 * TODO: comment me!
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
    public Object getIdentifier()
    {
        return getName();
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
}
