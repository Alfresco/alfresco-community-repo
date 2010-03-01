/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
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
