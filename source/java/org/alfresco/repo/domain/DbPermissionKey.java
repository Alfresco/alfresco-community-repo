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
package org.alfresco.repo.domain;

import java.io.Serializable;

import org.alfresco.util.EqualsHelper;

/**
 * Compound key for persistence of {@link org.alfresco.repo.domain.DbPermission}.
 * 
 * @author Derek Hulley
 */
public class DbPermissionKey implements Serializable
{
    private static final long serialVersionUID = -1667797216480779296L;

    private Long typeQNameId;
    private String name;

    public DbPermissionKey()
    {
    }
    
    public DbPermissionKey(Long typeQNameId, String name)
    {
        this.typeQNameId = typeQNameId;
        this.name = name;
    }
	
	public String toString()
	{
		return ("DbPermissionKey" +
				"[ type=" + typeQNameId +
				", name=" + name +
				"]");
	}
    
    public int hashCode()
    {
        return this.name.hashCode();
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (!(obj instanceof DbPermissionKey))
        {
            return false;
        }
        DbPermissionKey that = (DbPermissionKey) obj;
        return (EqualsHelper.nullSafeEquals(this.typeQNameId, that.typeQNameId)
                && EqualsHelper.nullSafeEquals(this.name, that.name)
                );
    }
    
    public Long getTypeQNameId()
    {
        return typeQNameId;
    }
    
    /**
     * Tamper-proof method only to be used by introspectors
     */
    @SuppressWarnings("unused")
    private void setTypeQName(Long typeQNameId)
    {
        this.typeQNameId = typeQNameId;
    }
    
    public String getName()
    {
        return name;
    }
    
    /**
     * Tamper-proof method only to be used by introspectors
     */
    @SuppressWarnings("unused")
    private void setName(String name)
    {
        this.name = name;
    }
}
