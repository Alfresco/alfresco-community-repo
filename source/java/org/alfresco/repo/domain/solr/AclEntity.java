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
    private Long aclChangeSetId;

    @Override
    public String toString()
    {
        return "AclEntity " +
        		"[id=" + id +
                ", aclChangeSetId=" + aclChangeSetId +
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
    public Long getAclChangeSetId()
    {
        return aclChangeSetId;
    }
    public void setAclChangeSetId(Long aclChangeSetId)
    {
        this.aclChangeSetId = aclChangeSetId;
    }
}
