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

    private AuthorityType authorityType;

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

    public AuthorityType getAuthorityType()
    {
        return authorityType;
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
     * 
     * @param accessStatus
     *            AccessStatus
     */
    public void setAccessStatus(AccessStatus accessStatus)
    {
        this.accessStatus = accessStatus;
    }

    /**
     * Set the type
     * 
     * @param aceType
     *            ACEType
     */
    public void setAceType(ACEType aceType)
    {
        this.aceType = aceType;
    }

    /**
     * Set the authority
     * 
     * @param authority
     *            String
     */
    public void setAuthority(String authority)
    {
        this.authority = authority;
        this.authorityType = AuthorityType.getAuthorityType(authority);
    }

    /**
     * Set the context
     * 
     * @param context
     *            AccessControlEntryContext
     */
    public void setContext(AccessControlEntryContext context)
    {
        this.context = context;
    }

    /**
     * Set the permission
     * 
     * @param permission
     *            PermissionReference
     */
    public void setPermission(PermissionReference permission)
    {
        this.permission = permission;
    }

    /**
     * Set the position
     * 
     * @param position
     *            Integer
     */
    public void setPosition(Integer position)
    {
        this.position = position;
    }

    public int compareTo(AccessControlEntry other)
    {
        int diff = this.getPosition() - other.getPosition();
        if (diff == 0)
        {
            diff = (this.getAccessStatus() == AccessStatus.DENIED ? 0 : 1) - (other.getAccessStatus() == AccessStatus.DENIED ? 0 : 1);
            if (diff == 0)
            {
                return getAuthorityType().getOrderPosition() - other.getAuthorityType().getOrderPosition();
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
