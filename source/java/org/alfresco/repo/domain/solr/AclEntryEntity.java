/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.domain.solr;

import org.alfresco.repo.solr.AclEntry;

/**
 * Interface for SOLR changeset objects.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class AclEntryEntity implements AclEntry
{
    private Long id;
    private Long aclId;
    private Long aclPermissionId;
    private String aclAuthority;

    @Override
    public String toString()
    {
        return "AclEntryEntity " +
        		"[id=" + id +
                ", aclId=" + aclId +
        		", aclPermissionId=" + aclPermissionId +
                ", aclAuthority=" + aclAuthority +
        		"]";
    }

    @Override
    public Long getId()
    {
        return id;
    }
    public void setId(Long id)
    {
        this.id = id;
    }

    @Override
    public Long getAclId()
    {
        return aclId;
    }

    public void setAclId(Long aclId)
    {
        this.aclId = aclId;
    }

    @Override
    public Long getAclPermissionId()
    {
        return aclPermissionId;
    }
    public void setAclPermissionId(Long aclPermissionId)
    {
        this.aclPermissionId = aclPermissionId;
    }

    @Override
    public String getAclAuthority()
    {
        return aclAuthority;
    }
    public void setAclAuthority(String aclAuthority)
    {
        this.aclAuthority = aclAuthority;
    }
}
