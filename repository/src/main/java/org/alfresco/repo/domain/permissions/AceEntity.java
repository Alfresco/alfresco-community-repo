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

import org.alfresco.repo.security.permissions.ACEType;
import org.alfresco.util.EqualsHelper;

/**
 * Entity for <b>alf_access_control_entry</b> persistence.
 * 
 * @author janv
 * @since 3.4
 */
public class AceEntity implements Ace
{
    private Long id;
    private Long version;
    private Long permissionId;
    private Long authorityId;
    private boolean allowed;
    private Integer applies;
    private Long contextId;

    /**
     * Default constructor
     */
    public AceEntity()
    {}

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }

    public Long getPermissionId()
    {
        return permissionId;
    }

    public void setPermissionId(Long permissionId)
    {
        this.permissionId = permissionId;
    }

    public Long getAuthorityId()
    {
        return authorityId;
    }

    public void setAuthorityId(Long authorityId)
    {
        this.authorityId = authorityId;
    }

    public boolean isAllowed()
    {
        return allowed;
    }

    public void setAllowed(boolean allowed)
    {
        this.allowed = allowed;
    }

    public Integer getApplies()
    {
        return applies;
    }

    public void setApplies(Integer applies)
    {
        this.applies = applies;
    }

    public Long getContextId()
    {
        return contextId;
    }

    public void setContextId(Long contextId)
    {
        this.contextId = contextId;
    }

    public ACEType getAceType()
    {
        return ACEType.getACETypeFromId(getApplies());
    }

    public void setAceType(ACEType aceType)
    {
        setApplies(aceType.getId());
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
        else if (obj instanceof AceEntity)
        {
            AceEntity that = (AceEntity) obj;
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
        sb.append("AceEntity")
                .append("[ ID=").append(id)
                .append(", version=").append(version)
                .append(", permissionId=").append(permissionId)
                .append(", authorityId=").append(authorityId)
                .append(", isAllowed=").append(allowed)
                .append(", applies=").append(applies)
                .append(", contextId=").append(contextId)
                .append("]");
        return sb.toString();
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
}
