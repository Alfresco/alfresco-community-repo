/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * aLong with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.domain.permissions;


/**
 * Entity for <b>alf_access_control_member</b> persistence.
 * 
 * Relates an ACE to an ACL with a position
 * 
 * @author janv
 * @since 3.4
 */
public interface AclMember
{
    public Long getId();
    
    /**
     * Get the ACL to which the ACE belongs
     * 
     * @return - the acl id
     */
    public Long getAclId();
    
    /**
     * Get the ACE included in the ACL
     * 
     * @return - the ace id
     */
    public Long getAceId();
    
    /**
     * Get the position group for this member in the ACL
     * 
     * 0  - implies the ACE is on the object
     * >0 - that it is inherited in some way
     * 
     * The lower values are checked first so take precedence.
     * 
     * @return - the position of the ace in the acl
     */
    public Integer getPos();
}
