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


/**
 * Properties for an access control list
 * 
 * @author andyh
 *
 */
public interface AccessControlListProperties
{
    /**
     * Get the ACL ID
     * @return the acl id
     */
    
    public String getAclId();
    
    /**
     * Get the ACL version
     * @return the acl version
     */
    public Long getAclVersion();
    
    /**
     * Is this the latest version of the acl identified by the acl id string? 
     * @return - true if the acl is the latest version
     */
    public Boolean isLatest();
    
    /**
     * Get inheritance behaviour
     * @return Returns the inheritance status of this list
     */
    public Boolean getInherits();
    
    /**
     * Get the type for this ACL
     * 
     * @return the acl type
     */
    public ACLType getAclType();
    
    /**
     * Is this ACL versioned - if not there will be no old versions of the ACL 
     * and the long id will remain unchanged.
     * 
     * If an acl is versioned it can not be updated - a new copy has to be created,
     *  
     * @return if the acl is verioned
     */
    public Boolean isVersioned();
    
    /**
     * The ACL DB id
     * 
     * @return the id
     */
    public Long getId();
    
    /**
     * Get the acl change set
     * @return - the id of the change set
     */
    public Long getAclChangeSetId();
}
