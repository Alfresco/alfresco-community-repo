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

import java.io.Serializable;

import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.util.EqualsHelper;

/**
 * Entity for <b>alf_acess_control_list</b> persistence.
 * 
 * @author janv
 * @since 3.4
 */
public class AclEntity implements Acl, Serializable
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long version;
    private String aclId;
    private boolean latest;
    private Long aclVersion;
    private boolean inherits;
    private Long inheritsFrom;
    private Integer type;
    private Long inheritedAcl;
    private boolean isVersioned;
    private boolean requiresVersion;
    private Long aclChangeSet;

    /**
     * Default constructor
     */
    public AclEntity()
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

    public String getAclId()
    {
        return aclId;
    }

    public void setAclId(String aclId)
    {
        this.aclId = aclId;
    }

    public Boolean isLatest()
    {
        return latest;
    }

    public void setLatest(boolean latest)
    {
        this.latest = latest;
    }

    public Long getAclVersion()
    {
        return aclVersion;
    }

    public void setAclVersion(Long aclVersion)
    {
        this.aclVersion = aclVersion;
    }

    public Boolean getInherits()
    {
        return inherits;
    }

    public void setInherits(boolean inherits)
    {
        this.inherits = inherits;
    }

    public Long getInheritsFrom()
    {
        return inheritsFrom;
    }

    public void setInheritsFrom(Long inheritsFrom)
    {
        this.inheritsFrom = inheritsFrom;
    }

    public Integer getType()
    {
        return type;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }

    public Long getInheritedAcl()
    {
        return inheritedAcl;
    }

    public void setInheritedAcl(Long inheritedAcl)
    {
        this.inheritedAcl = inheritedAcl;
    }

    public Boolean isVersioned()
    {
        return isVersioned;
    }

    public void setVersioned(boolean isVersioned)
    {
        this.isVersioned = isVersioned;
    }

    public Boolean getRequiresVersion()
    {
        return requiresVersion;
    }

    public void setRequiresVersion(boolean requiresVersion)
    {
        this.requiresVersion = requiresVersion;
    }

    public Long getAclChangeSetId()
    {
        return aclChangeSet;
    }

    public void setAclChangeSetId(Long aclChangeSet)
    {
        this.aclChangeSet = aclChangeSet;
    }

    public ACLType getAclType()
    {
        return ACLType.getACLTypeFromId(type);
    }

    public void setAclType(ACLType type)
    {
        this.type = type.getId();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof AclEntity)
        {
            AclEntity that = (AclEntity) obj;
            return EqualsHelper.nullSafeEquals(this.id, that.id)
                    && EqualsHelper.nullSafeEquals(this.version, that.version);
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
        sb.append("AclEntity")
                .append("[ ID=").append(id)
                .append(", version=").append(version)
                .append(", aclId=").append(aclId)
                .append(", isLatest=").append(latest)
                .append(", aclVersion=").append(aclVersion)
                .append(", inherits=").append(inherits)
                .append(", inheritsFrom=").append(inheritsFrom)
                .append(", type=").append(type)
                .append(", inheritedAcl=").append(inheritedAcl)
                .append(", isVersioned=").append(isVersioned)
                .append(", requiresVersion=").append(requiresVersion)
                .append(", aclChangeSet=").append(aclChangeSet)
                .append("]");
        return sb.toString();
    }
}
