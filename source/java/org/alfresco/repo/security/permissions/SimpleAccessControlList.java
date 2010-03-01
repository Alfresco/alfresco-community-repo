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
package org.alfresco.repo.security.permissions;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.security.permissions.impl.SimpleNodePermissionEntry;

public class SimpleAccessControlList implements AccessControlList
{
    /**
     * 
     */
    private static final long serialVersionUID = -1859514919998903150L;

    private AccessControlListProperties properties;
    
    private List<AccessControlEntry> entries = new ArrayList<AccessControlEntry>();
    
    private transient SimpleNodePermissionEntry cachedSimpleNodePermissionEntry;
    
    public List<AccessControlEntry> getEntries()
    {
        return entries;
    }

    public AccessControlListProperties getProperties()
    {
       return properties;
    }

    public void setEntries(List<AccessControlEntry> entries)
    {
        this.entries = entries;
    }

    public void setProperties(AccessControlListProperties properties)
    {
        this.properties = properties;
    }

    public synchronized SimpleNodePermissionEntry getCachedSimpleNodePermissionEntry()
    {
        return cachedSimpleNodePermissionEntry;
    }

    public synchronized void setCachedSimpleNodePermissionEntry(SimpleNodePermissionEntry cachedSimpleNodePermissionEntry)
    {
        this.cachedSimpleNodePermissionEntry = cachedSimpleNodePermissionEntry;
    }
    
    

}
