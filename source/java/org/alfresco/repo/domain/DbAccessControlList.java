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

import java.util.Set;

import org.alfresco.repo.domain.hibernate.DbAccessControlEntryImpl;


/**
 * The interface to support persistence of node access control entries in hibernate
 * 
 * @author andyh
 */
public interface DbAccessControlList
{
    public long getId();

    /**
     * 
     * @return Returns the access control entries for this access control list 
     */
    public Set<DbAccessControlEntry> getEntries();
    
    /**
     * Get inheritance behaviour
     * @return Returns the inheritance status of this list
     */
    public boolean getInherits();
    
    /**
     * Set inheritance behaviour
     * @param inherits true to set the permissions to inherit
     */
    public void setInherits(boolean inherits);

    public int deleteEntriesForAuthority(String authorityKey);
    
    public int deleteEntriesForPermission(DbPermissionKey permissionKey);

    public int deleteEntry(String authorityKey, DbPermissionKey permissionKey);
    
    /**
     * Delete the entries related to this access control list
     * 
     * @return Returns the number of entries deleted
     */
    public int deleteEntries();
    
    public DbAccessControlEntry getEntry(String authorityKey, DbPermissionKey permissionKey);
    
    /**
     * Factory method to create an entry and wire it up.
     * Note that the returned value may still be transient.  Saving it should  be fine, but
     * is not required.
     * 
     * @param permission the mandatory permission association with this entry
     * @param authority the mandatory authority.  Must not be transient.
     * @param allowed allowed or disallowed.  Must not be transient.
     * @return Returns the new entry
     */
    public DbAccessControlEntryImpl newEntry(DbPermission permission, DbAuthority authority, boolean allowed);
    
    /**
     * Make a copy of this ACL (persistently)
     * @return The copy.
     */
    public DbAccessControlList getCopy();
}
