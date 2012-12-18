/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.security.authority;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;

/**
 * Authority Info - used by GetAuthorities CQ
 *
 * @author janv
 * @since 4.0
 */
public class AuthorityInfo
{
    private Long nodeId;
    
    private String authorityDisplayName; // eg. My Group, My Role
    private String authorityName;        // eg. GROUP_my1, ROLE_myA
    
    public AuthorityInfo(Long nodeId, String authorityDisplayName, String authorityName)
    {
        this.nodeId = nodeId;
        this.authorityDisplayName = authorityDisplayName;
        this.authorityName = authorityName;
    }
    
    public Long getNodeId()
    {
        return nodeId;
    }
    
    public String getAuthorityDisplayName()
    {
        return authorityDisplayName;
    }
    
    public String getAuthorityName()
    {
        return authorityName;
    }

    public String getShortName()
    {
        AuthorityType type = AuthorityType.getAuthorityType(authorityName);
        if (type.isFixedString())
        {
            return "";
        }
        else if (type.isPrefixed())
        {
            return authorityName.substring(type.getPrefixString().length());
        }
        else
        {
            return authorityName;
        }
    }
}
