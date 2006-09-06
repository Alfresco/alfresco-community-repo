/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
