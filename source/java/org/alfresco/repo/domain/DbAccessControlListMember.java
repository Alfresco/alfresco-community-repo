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
