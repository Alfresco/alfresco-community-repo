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
package org.alfresco.repo.security.permissions;

import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityType;

/**
 * A basic access control entry 
 * 
 * @author andyh
 *
 */
public class SimpleAccessControlEntry implements AccessControlEntry
{
    /**
     * 
     */
    private static final long serialVersionUID = -3099789485179796034L;

    private AccessStatus accessStatus;
    
    private ACEType aceType;
    
    private String authority;
    
    private AccessControlEntryContext context;
    
    private PermissionReference permission;
    
    private Integer position;
    
    public AccessStatus getAccessStatus()
    {
        return accessStatus;
    }

    public ACEType getAceType()
    {
       return aceType;
    }

    public String getAuthority()
    {
        return authority;
    }

    public AccessControlEntryContext getContext()
    {
       return context;
    }

    public PermissionReference getPermission()
    {
        return permission;
    }

    public Integer getPosition()
    {
        return position;
    }

    /**
     * Set the status
     * @param accessStatus
     */
    public void setAccessStatus(AccessStatus accessStatus)
    {
        this.accessStatus = accessStatus;
    }

    
    /**
     * Set the type
     * @param aceType
     */
    public void setAceType(ACEType aceType)
    {
        this.aceType = aceType;
    }

    /**
     * Set the authority
     * @param authority
     */
    public void setAuthority(String authority)
    {
        this.authority = authority;
    }

    /**
     * Set the context
     * @param context
     */
    public void setContext(AccessControlEntryContext context)
    {
        this.context = context;
    }

    /**
     * Set the permission
     * @param permission
     */
    public void setPermission(PermissionReference permission)
    {
        this.permission = permission;
    }

    /** 
     * Set the position
     * @param position
     */
    public void setPosition(Integer position)
    {
        this.position = position;
    }

    public int compareTo(AccessControlEntry other)
    {
        int diff = this.getPosition() - other.getPosition();
        if(diff == 0)
        {
            diff = (this.getAccessStatus()== AccessStatus.DENIED ? 0 : 1) - (other.getAccessStatus()== AccessStatus.DENIED ? 0 : 1); 
            if(diff == 0)
            {
                return AuthorityType.getAuthorityType(this.getAuthority()).getOrderPosition()  -   AuthorityType.getAuthorityType(other.getAuthority()).getOrderPosition();
            }
            else
            {
                return diff;
            }
        }
        else
        {
            return diff;
        }
    }

    @Override
    public String toString()
    {
       StringBuilder builder = new StringBuilder();
       builder.append("[");
       builder.append(getPermission()).append(", ");
       builder.append(getAuthority()).append(", ");
       builder.append(getAccessStatus()).append(", ");
       builder.append(getAceType()).append(", ");
       builder.append(getPosition()).append(", ");
       builder.append(getContext());
       builder.append("]");
       return builder.toString();
    }

    
}
