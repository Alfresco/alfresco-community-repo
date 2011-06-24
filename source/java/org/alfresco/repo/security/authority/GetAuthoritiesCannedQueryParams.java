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
package org.alfresco.repo.security.authority;

import org.alfresco.service.cmr.security.AuthorityType;


/**
 * GetAuthorities CQ parameters - for query context and filtering
 *
 * @author janv
 * @since 4.0
 */
public class GetAuthoritiesCannedQueryParams extends AuthorityInfoEntity
{
    private String displayNameFilter; // startsWith / ignoreCase (note: trailing * is implied)
    private AuthorityType type;
    
    public GetAuthoritiesCannedQueryParams(AuthorityType type, Long parentNodeId, Long authorityDisplayNameQNameId, String displayNameFilter)
    {
        super(parentNodeId, authorityDisplayNameQNameId);
        
        if ((displayNameFilter == null) || (displayNameFilter.equals("")) || (displayNameFilter.equals("*")))
        {
            // The wildcard means no filtering is needed on this property
            this.displayNameFilter = null;
        }
        else
        {
            if (displayNameFilter.endsWith("*"))
            {
                // The trailing * is implicit
                displayNameFilter = displayNameFilter.substring(0, displayNameFilter.length()-1);
            }
            
            this.displayNameFilter = displayNameFilter.toLowerCase();
        }
        
        this.type = type;
    }
    
    public String getDisplayNameFilter()
    {
        return displayNameFilter;
    }
    
    public AuthorityType getType()
    {
        return type;
    }
}
