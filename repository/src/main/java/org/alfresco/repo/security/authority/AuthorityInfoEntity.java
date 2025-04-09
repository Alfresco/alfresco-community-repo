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
package org.alfresco.repo.security.authority;

import org.alfresco.repo.domain.node.NodeEntity;

/**
 * Authority Info Entity - used by GetAuthorities CQ
 *
 * @author jan
 * @since 4.0
 */
public class AuthorityInfoEntity
{
    private Long id; // node id

    private NodeEntity node;

    private String authorityDisplayName;
    private String authorityName;

    // Supplemental query-related parameters
    private Long parentNodeId;
    private Long authorityDisplayNameQNameId;
    // Authority type
    private Long typeQNameId;

    /**
     * Default constructor
     */
    public AuthorityInfoEntity()
    {}

    public AuthorityInfoEntity(Long parentNodeId, Long authorityDisplayNameQNameId, Long typeQNameId)
    {
        this.parentNodeId = parentNodeId;
        this.authorityDisplayNameQNameId = authorityDisplayNameQNameId;
        this.typeQNameId = typeQNameId;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getAuthorityDisplayName()
    {
        return authorityDisplayName;
    }

    public void setAuthorityDisplayName(String authorityDisplayName)
    {
        this.authorityDisplayName = authorityDisplayName;
    }

    public String getAuthorityName()
    {
        return authorityName;
    }

    public void setAuthorityName(String authorityName)
    {
        this.authorityName = authorityName;
    }

    public NodeEntity getNode()
    {
        return node;
    }

    public void setNode(NodeEntity childNode)
    {
        this.node = childNode;
    }

    // Supplemental query-related parameters

    public Long getParentNodeId()
    {
        return parentNodeId;
    }

    public Long getAuthorityDisplayNameQNameId()
    {
        return authorityDisplayNameQNameId;
    }

    public Long getTypeQNameId()
    {
        return typeQNameId;
    }
}
