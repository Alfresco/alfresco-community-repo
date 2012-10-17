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
package org.alfresco.repo.security.person;

import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * GetPeople CQ parameters - for query context and filtering
 *
 * @author janv
 * @since 4.1.2
 */
public class GetPeopleCannedQueryParams
{
    private NodeRef parentRef;
    private List<QName> filterProps = Collections.emptyList();
    private String pattern = null;
    
    public GetPeopleCannedQueryParams(
            NodeRef parentRef,
            List<QName> filterProps,
            String pattern)
    {
        this.parentRef = parentRef;
        if (filterProps != null) { this.filterProps = filterProps; }
        
        this.pattern = pattern;
    }
    
    public NodeRef getParentRef()
    {
        return parentRef;
    }
    
    public List<QName> getFilterProps()
    {
        return filterProps;
    }
    
    public String getPattern()
    {
        return pattern;
    }
}
