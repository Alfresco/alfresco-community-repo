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

package org.alfresco.repo.workflow;

import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;

/**
 * @since 4.0
 * @author Nick Smith
 *
 */
public class WorkflowAuthorityManager
{
    private final AuthorityDAO authorityDAO;
    
    
    public WorkflowAuthorityManager(AuthorityDAO authorityDAO)
    {
        this.authorityDAO = authorityDAO;
    }

    /**
     * Convert Alfresco authority to user id
     * 
     * @param authority
     * @return actor id
     */
    public String mapAuthorityToName(NodeRef authority)
    {
            return authorityDAO.getAuthorityName(authority);
    }
    
    /**
     * Convert authority name to an Alfresco Authority
     * 
     * @param names
     *            the authority names to convert
     * @return the Alfresco authorities
     */
    public NodeRef mapNameToAuthority(String name)
    {
        NodeRef authority = null;
        if (name != null)
        {
            authority = authorityDAO.getAuthorityNodeRefOrNull(name);
        }
        return authority;
    }
    
    public boolean isUser(String authorityName)
    {
        AuthorityType type = AuthorityType.getAuthorityType(authorityName);
        return type == AuthorityType.USER ||
            type == AuthorityType.ADMIN ||
            type == AuthorityType.GUEST;
    }

    public String getAuthorityName(NodeRef authorityRef)
    {
        return authorityDAO.getAuthorityName(authorityRef);
    }
    
}
