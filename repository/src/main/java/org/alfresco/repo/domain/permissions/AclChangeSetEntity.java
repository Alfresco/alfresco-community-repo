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

import org.alfresco.util.EqualsHelper;

/**
 * Entity for <b>alf_acl_change_set</b> persistence.
 * 
 * @author janv
 * @since 3.4
 */
public class AclChangeSetEntity implements AclChangeSet
{
    private Long id;
    private Long commitTimeMs;
    
    /**
     * Default constructor
     */
    public AclChangeSetEntity()
    {
    }
    
    public Long getId()
    {
        return id;
    }
    
    public void setId(Long id)
    {
        this.id = id;
    }
    
    public Long getCommitTimeMs()
    {
        return commitTimeMs;
    }

    public void setCommitTimeMs(Long commitTimeMs)
    {
        this.commitTimeMs = commitTimeMs;
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
        else if (obj instanceof AclChangeSetEntity)
        {
            AclChangeSetEntity that = (AclChangeSetEntity)obj;
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
        sb.append("AclChangeSetEntity")
          .append("[ ID=").append(id)
          .append(", commitTimeMs=").append(commitTimeMs)
          .append("]");
        return sb.toString();
    }
}
