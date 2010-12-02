/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.domain.permissions;

import org.alfresco.util.EqualsHelper;


/**
 * Entity for <b>alf_authority_alias</b> persistence.
 * 
 * @author janv
 * @since 3.4
 */
public class AuthorityAliasEntity implements AuthorityAlias
{
    private Long id;
    private Long version;
    private Long authId;
    private Long aliasId;
    
    /**
     * Default constructor
     */
    public AuthorityAliasEntity()
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
    
    public Long getVersion()
    {
        return version;
    }
    
    public void setVersion(Long version)
    {
        this.version = version;
    }
    
    public Long getAuthId()
    {
        return authId;
    }
    
    public void setAuthId(Long authId)
    {
        this.authId = authId;
    }
    
    public Long getAliasId()
    {
        return aliasId;
    }
    
    public void setAliasId(Long aliasId)
    {
        this.aliasId = aliasId;
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
        else if (obj instanceof AuthorityAliasEntity)
        {
            AuthorityAliasEntity that = (AuthorityAliasEntity)obj;
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
        sb.append("AuthorityAliasEntity")
          .append("[ ID=").append(id)
          .append(", version=").append(version)
          .append(", authId=").append(authId)
          .append(", aliasId=").append(aliasId)
          .append("]");
        return sb.toString();
    }
}
