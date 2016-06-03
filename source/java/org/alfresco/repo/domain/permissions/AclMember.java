/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
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
