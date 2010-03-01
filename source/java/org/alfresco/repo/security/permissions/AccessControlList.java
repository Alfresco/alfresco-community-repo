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

import java.io.Serializable;
import java.util.List;

import org.alfresco.repo.security.permissions.impl.SimpleNodePermissionEntry;

public interface AccessControlList extends Serializable
{
    /**
     * Get the properties
     * @return
     */
    public AccessControlListProperties getProperties();
    
    /**
     * Get the members of the ACL in order
     * Ordered by:
     * position, 
     * then deny followed by allow, 
     * then by authority type 
     * then ....
     * 
     * To make permission evaluation faster for the common cases
     * 
     * @return
     */
    public List<AccessControlEntry> getEntries();
    
    public SimpleNodePermissionEntry getCachedSimpleNodePermissionEntry();
    
    public void setCachedSimpleNodePermissionEntry(SimpleNodePermissionEntry cachedSimpleNodePermissionEntry);
}
