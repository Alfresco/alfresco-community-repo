/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.domain;

import java.io.Serializable;

import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;

/**
 * Compound key for persistence of {@link org.alfresco.repo.domain.DbPermission}.
 * 
 * @author Derek Hulley
 */
public class DbPermissionKey implements Serializable
{
    private static final long serialVersionUID = -1667797216480779296L;

    private QName typeQname;
    private String name;

    public DbPermissionKey()
    {
    }
    
    public DbPermissionKey(QName typeQname, String name)
    {
        this.typeQname = typeQname;
        this.name = name;
    }
	
	public String toString()
	{
		return ("DbPermissionKey" +
				"[ type=" + typeQname +
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
        return (EqualsHelper.nullSafeEquals(this.typeQname, that.typeQname)
                && EqualsHelper.nullSafeEquals(this.name, that.name)
                );
    }
    
    public QName getTypeQname()
    {
        return typeQname;
    }
    
    /**
     * Tamper-proof method only to be used by introspectors
     */
    @SuppressWarnings("unused")
    private void setTypeQname(QName typeQname)
    {
        this.typeQname = typeQname;
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
