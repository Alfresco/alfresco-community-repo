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

import org.alfresco.util.EqualsHelper;


/**
 * Entity for <b>alf_access_control_member</b> persistence.
 * 
 * Relates an ACE to an ACL with a position
 * 
 * @author janv
 * @since 3.4
 */
public class AclMemberEntity implements AclMember
{
    private Long id;
    private Long version;
    private Long aclId;
    private Long aceId;
    private Integer pos;
    
    /**
     * Default constructor
     */
    public AclMemberEntity()
    {
    }
    
    public AclMemberEntity(long aclId, long aceId, int pos)
    {
        this.aclId = aclId;
        this.aceId = aceId;
        this.pos = pos;
    }
    
    /**
     * Get the ID for this ACL member
     * 
     * @return - the id
     */
    public Long getId()
    {
        return id;
    }
    
    public void setId(Long id)
    {
        this.id = id;
    }
    
    /**
     * Get the version for this ACL member - for optimistic locking
     * 
     * @return - the version
     */
    public Long getVersion()
    {
        return version;
    }
    
    public void setVersion(Long version)
    {
        this.version = version;
    }
    
    public void incrementVersion()
    {
        if (this.version >= Long.MAX_VALUE)
        {
            this.version = 0L;
        }
        else
        {
            this.version++;
        }
    }
    
    /**
     * Get the ACL to which the ACE belongs
     * 
     * @return - the acl id
     */
    public Long getAclId()
    {
        return aclId;
    }
    
    public void setAclId(Long aclId)
    {
        this.aclId = aclId;
    }
    
    /**
     * Get the ACE included in the ACL
     * 
     * @return - the ace id
     */
    public Long getAceId()
    {
        return aceId;
    }
    
    public void setAceId(Long aceId)
    {
        this.aceId = aceId;
    }
    
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
    public Integer getPos()
    {
        return pos;
    }
    
    /**
     * Set the position for the ACL-ACE relationship
     * 
     * @param position
     */
    public void setPos(Integer pos)
    {
        this.pos = pos;
    }
    
    @Override
    public int hashCode()
    {
        return (id == null ? 0 : id.hashCode());
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof AclMemberEntity)
        {
            AclMemberEntity that = (AclMemberEntity)obj;
            return (EqualsHelper.nullSafeEquals(this.id, that.id));
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("AclMemberEntity")
          .append("[ ID=").append(id)
          .append(", version=").append(version)
          .append(", aclId=").append(aclId)
          .append(", aceId=").append(aceId)
          .append(", pos=").append(pos)
          .append("]");
        return sb.toString();
    }
}
