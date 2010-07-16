/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
package org.alfresco.repo.transfer.manifest;

import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer object to represent the access control on a Manifest Node.
 *
 */
public class ManifestAccessControl
{
    private boolean isInherited;
    private List<ManifestPermission> permissions;
    
    public void setInherited(boolean isInherited)
    {
        this.isInherited = isInherited;
    }
    
    public boolean isInherited()
    {
        return isInherited;
    }
    
    public void setPermissions(List<ManifestPermission> permissions)
    {
        this.permissions = permissions;
    }
    
    public List<ManifestPermission> getPermissions()
    {
        return permissions;
    } 
    
    public void addPermission(ManifestPermission permission)
    {
        if(permissions == null)
        {
            permissions = new ArrayList<ManifestPermission>(20);
        }
        permissions.add(permission);
    }
}
