/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.domain;



/**
 * The interface against which permission entries are persisted
 * 
 * @author andyh
 */

public interface DbAccessControlEntry
{
    /**
     * @return Returns the identifier for this object
     */
    public long getId();
    
    /**
     * @return Returns the containing access control list
     */
    public DbAccessControlList getAccessControlList();
    
    /**
     * @param acl the accession control list to which entry belongs
     */
    public void setAccessControlList(DbAccessControlList acl);
    
    /**
     * @return Returns the permission to which this entry applies
     */
    public DbPermission getPermission();
    
    /**
     * @param permission the permission to which the entry applies
     */
    public void setPermission(DbPermission permission);
    
    /**
     * @return Returns the authority to which this entry applies
     */
    public DbAuthority getAuthority();
    
    /**
     * @param authority the authority to which this entry applies
     */
    public void setAuthority(DbAuthority authority);
    
    /**
     * @return Returns <tt>true</tt> if this permission is allowed 
     */
    public boolean isAllowed();
    
    /**
     * Set if this permission is allowed, otherwise it is denied.
     * 
     * @param allowed
     */
    public void setAllowed(boolean allowed);
    
    /**
     * Helper method to delete the instance and make sure that all
     * inverse associations are properly maintained.
     */
    public void delete();
}
