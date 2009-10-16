/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.domain.avm;

import org.alfresco.util.EqualsHelper;

/**
 * Entity bean for <b>avm_stores</b> table.
 * <p>
 * 
 * @author janv
 * @since 3.2
 */
public class AVMStoreEntity
{
    private Long id;
    private Long nextVersion;
    private String name;
    private Long aclId;
    private Long rootNodeId;
    private Long vers; // for concurrency control
    
    
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
        return nextVersion;
    }
    
    public void setVersion(Long version)
    {
        this.nextVersion = version;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public Long getRootNodeId()
    {
        return rootNodeId;
    }
    
    public void setRootNodeId(Long rootNodeId)
    {
        this.rootNodeId = rootNodeId;
    }
    
    public Long getAclId()
    {
        return aclId;
    }
    
    public void setAclId(Long aclId)
    {
        this.aclId = aclId;
    }
    
    public Long getVers()
    {
        return vers;
    }
    
    public void setVers(Long vers)
    {
        this.vers = vers;
    }
    
    public void incrementVers()
    {
        if (this.vers >= Long.MAX_VALUE)
        {
            this.vers = 0L;
        }
        else
        {
            this.vers++;
        }
    }
    
    @Override
    public int hashCode()
    {
        return (name == null ? 0 : name.hashCode());
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof AVMStoreEntity)
        {
            AVMStoreEntity that = (AVMStoreEntity) obj;
            return EqualsHelper.nullSafeEquals(this.name, that.name);
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
        sb.append("AVMStoreEntity")
          .append("[ ID=").append(id)
          .append(", name=").append(name)
          .append(", nextVersion=").append(nextVersion)
          .append(", rootNodeId=").append(rootNodeId)
          .append(", aclId=").append(aclId)
          .append("]");
        return sb.toString();
    }
}
