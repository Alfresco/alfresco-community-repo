/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import org.alfresco.repo.solr.Acl;

/**
 * Interface for SOLR changeset objects.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class AclEntity implements Acl
{
    private Long id;
    private Long inheritedId;
    private Long aclChangeSetId;

    @Override
    public String toString()
    {
        return "AclEntity [id=" + id + ", inheritedId=" + inheritedId + ", aclChangeSetId=" + aclChangeSetId + "]";
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
    public Long getAclChangeSetId()
    {
        return aclChangeSetId;
    }
    public void setAclChangeSetId(Long aclChangeSetId)
    {
        this.aclChangeSetId = aclChangeSetId;
    }

    @Override
    public Long getInheritedId()
    {
        return inheritedId;
    }

    public void setInheritedId(Long inheritedId)
    {
        this.inheritedId = inheritedId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aclChangeSetId == null) ? 0 : aclChangeSetId.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((inheritedId == null) ? 0 : inheritedId.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AclEntity other = (AclEntity) obj;
        if (aclChangeSetId == null)
        {
            if (other.aclChangeSetId != null)
                return false;
        }
        else if (!aclChangeSetId.equals(other.aclChangeSetId))
            return false;
        if (id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!id.equals(other.id))
            return false;
        if (inheritedId == null)
        {
            if (other.inheritedId != null)
                return false;
        }
        else if (!inheritedId.equals(other.inheritedId))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Acl o)
    {
        return this.getId().compareTo(o.getId());
    }
    
    
}
