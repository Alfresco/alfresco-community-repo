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

/**
 * Realtes an ACE to an ACL with a position
 * 
 * @author andyh
 *
 */
public interface DbAccessControlListMember
{
    /**
     * Get the ID for the membership entry
     * @return - the id
     */
    public Long getId();
    
    /**
     * Get the version for this membership entry
     * @return - the version
     */
    public Long getVersion();
    
    /**
     * Get the ACL to which the ACE belongs.
     * @return - the acl id
     */
    public DbAccessControlList getAccessControlList();
    
    /**
     * Get the ACE included in the ACL
     * @return - the ace id
     */
    public DbAccessControlEntry getAccessControlEntry();
    
    /**
     * Get the position group for this member in the ACL
     * 
     * 0  - implies the ACE is om the object
     * >0 - that it is inhertied in some way
     * 
     * The lower values are checked first so take precidence.
     * 
     * @return - the position of the ace in the acl
     */
    public int getPosition();
    
    /**
     * Set the ACL
     * @param acl
     */
    public void setAccessControlList(DbAccessControlList acl);
    
    /**
     * Set the ACE
     * @param ace
     */
    public void setAccessControlEntry(DbAccessControlEntry ace);
    
    /**
     * Set the position for the ACL-ACE relationship
     * @param position
     */
    public void setPosition(int position);
}
