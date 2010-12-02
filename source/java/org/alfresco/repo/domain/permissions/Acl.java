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
package org.alfresco.repo.domain.permissions;

import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.AccessControlListProperties;


/**
 * Entity for <b>alf_access_control_list</b> persistence.
 * 
 * @author janv
 * @since 3.4
 */
public interface Acl extends AccessControlListProperties
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
    public Long getAclVersion();
    
    /**
     * Is this the latest version of the acl identified by the acl id string? 
     * @return
     */
    public Boolean isLatest();
    
    /**
     * Get inheritance behaviour
     * @return Returns the inheritance status of this list
     */
    public Boolean getInherits();
    
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
    public Long getInheritedAcl();
    
    /**
     * Is this ACL versioned - if not there will be no old versions of the ACL 
     * and the long id will remain unchanged.
     * 
     * If an acl is versioned it can not be updated - a new copy has to be created,
     *  
     * @return
     */
    public Boolean isVersioned();
    
    public Boolean getRequiresVersion();
    
    public Long getAclChangeSetId();
}
