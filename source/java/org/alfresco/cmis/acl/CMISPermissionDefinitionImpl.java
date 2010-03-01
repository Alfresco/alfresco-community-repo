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
package org.alfresco.cmis.acl;

import org.alfresco.cmis.CMISPermissionDefinition;

/**
 * Implementation class for a simple CMIS permission definition.
 * 
 * @author andyh
 *
 */
public class CMISPermissionDefinitionImpl implements CMISPermissionDefinition
{
    private String permission;
    
    private String description;

    /*package*/ CMISPermissionDefinitionImpl(String permission)
    {
        this.permission = permission;
    }
    
    /*package*/ CMISPermissionDefinitionImpl(String permission, String description)
    {
        this(permission);
        this.description = description;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.cmis.CMISPermissionDefinition#getDescription()
     */
    public String getDescription()
    {
        if(description != null)
        {
            return description;
        }
        else
        {
            return permission;
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.CMISPermissionDefinition#getPermission()
     */
    public String getPermission()
    {
        return permission;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((permission == null) ? 0 : permission.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final CMISPermissionDefinitionImpl other = (CMISPermissionDefinitionImpl) obj;
        if (permission == null)
        {
            if (other.permission != null)
                return false;
        }
        else if (!permission.equals(other.permission))
            return false;
        return true;
    }

    
}
