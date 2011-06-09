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
package org.alfresco.repo.security.permissions.impl;

import org.alfresco.repo.security.permissions.PermissionReference;

/**
 * This class provides common support for hash code and equality.
 * 
 * @author andyh
 */
@SuppressWarnings("serial")
public abstract class AbstractPermissionReference implements PermissionReference
{
    private int hashcode = 0;
    private String str = null;
    
    protected AbstractPermissionReference()
    {
        super();
    }

    @Override
    public final boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(!(o instanceof AbstractPermissionReference))
        {
            return false;
        }
        AbstractPermissionReference other = (AbstractPermissionReference)o;
        if(other.hashCode() != this.hashCode())
        {
            return false;
        }
        return this.getName().equals(other.getName()) && this.getQName().equals(other.getQName());
    }

    @Override
    public final int hashCode()
    {
        if (hashcode == 0)
        {
           hashcode = (getName().hashCode() * 1000003) + getQName().hashCode();
        }
        return hashcode;
    }

    @Override
    public String toString()
    {
        if (str == null)
        {
            str = getQName() + "." + getName();
        }
        return str;
    }
}
