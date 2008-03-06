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

import org.alfresco.repo.security.permissions.ACLCopyMode;
import org.alfresco.repo.security.permissions.ACLType;


/**
 * The interface to support persistence of node access control entries in hibernate
 * 
 * @author andyh
 */
public interface DbAccessControlList
{
    /**
     * Get the long key
     * @return
     */
    public Long getId();

    /**
     * Get the ACL ID
     * @return
     */
    public String getAclId();
    
    /**
     * Get the ACL version
     * @return
     */
    public long getAclVersion();
    
    /**
     * Is this the latest version of the acl identified by the acl id string? 
     * @return
     */
    public boolean isLatest();
    
    /**
     * @return  Returns the version number for optimistic locking
     */
    public Long getVersion();
    
    /**
     * Get inheritance behaviour
     * @return Returns the inheritance status of this list
     */
    public boolean getInherits();
    
    /**
     * Get the ACL from which this one inherits
     * 
     * @return
     */
    public Long getInheritsFrom();
    
    /**
     * Get the type for this ACL
     * 
     * @return
     */
    public ACLType getAclType();
    
    /**
     * Get the ACL inherited from nodes which have this ACL
     * 
     * @return
     */
    public Long getInheritedAclId();
    
    /**
     * Is this ACL versioned - if not there will be no old versions of the ACL 
     * and the long id will remain unchanged.
     * 
     * If an acl is versioned it can not be updated - a new copy has to be created,
     *  
     * @return
     */
    public boolean isVersioned();
    
    /**
     * Set the string ACL ID (not the auto generated long)
     * @param id
     */
    
    public void setAclId(String id);
    
    
    /**
     * Set the ACL version (not the optimistic version used by hibernate)
     * @param version
     */
    public void setAclVersion(long version);
    
    /**
     * Set if this ACL is the latest version of the ACL as identified by getAclId()
     * @param isLatest
     */
    public void setLatest(boolean isLatest);
    
    /**
     * Set inheritance behaviour
     * @param inherits true to set the permissions to inherit
     */
    public void setInherits(boolean inherits);

    /**
     * Set the ACL from which this one inherits
     * @param id
     */
    public void setInheritsFrom(Long id);
    
    /**
     * Set the ACL Type
     * @param type
     */
    public void setAclType(ACLType type);
    
    /**
     * Set the ACL that should be set when inheriting from this one.
     * This ACL does not contain any object specific settings.
     * @param acl
     */
    public void setInheritedAclId(Long acl);
    
    /**
     * Set if this ACL is versioned on write
     * @param isVersioned
     */
    public void setVersioned(boolean isVersioned);
    
    /**
     * Set the change set
     * @param aclChangeSet
     */
    public void setAclChangeSet(DbAccessControlListChangeSet aclChangeSet);
    
    /**
     * Get the change set
     * @return
     */
    public DbAccessControlListChangeSet getAclChangeSet();
    
    // Stuff to fix up in AVM
    
    public DbAccessControlList getCopy(Long parent, ACLCopyMode node);
    
    public void setRequiresVersion(boolean requiresVersion);
    
    public boolean getRequiresVersion();
     
}
