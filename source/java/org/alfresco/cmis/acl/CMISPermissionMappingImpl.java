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

import java.util.List;

import org.alfresco.cmis.CMISPermissionMapping;

/**
 * @author andyh
 *
 */
public class CMISPermissionMappingImpl implements CMISPermissionMapping
{
    String key;
    
    List<String> permissions;
    
    CMISPermissionMappingImpl(String key, List<String> permissions)
    {
        this.key = key;
        this.permissions = permissions;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.cmis.CMISPermissionMapping#getKey()
     */
    public String getKey()
    {
        return key;
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.CMISPermissionMapping#getPermissions()
     */
    public List<String> getPermissions()
    {
        return permissions;
    }

}
