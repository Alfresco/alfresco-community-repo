/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
import java.util.Set;

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
    
    private boolean includeAdministrators;
    private Set<QName> inclusiveAspects;
    private Set<QName> exclusiveAspects;
    
    public GetPeopleCannedQueryParams(
            NodeRef parentRef,
            List<QName> filterProps,
            String pattern,
            Set<QName> inclusiveAspects,
            Set<QName> exclusiveAspects,
            boolean includeAdministrators)
    {
        this.parentRef = parentRef;
        if (filterProps != null) { this.filterProps = filterProps; }
        
        this.pattern = pattern;
        
        this.inclusiveAspects = inclusiveAspects;
        this.exclusiveAspects = exclusiveAspects;
        this.includeAdministrators = includeAdministrators;
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
    
    public boolean getIncludeAdministrators()
    {
        return includeAdministrators;
    }
    
    public Set<QName> getInclusiveAspects()
    {
        return inclusiveAspects;
    }
    
    public Set<QName> getExclusiveAspects()
    {
        return exclusiveAspects;
    }
    
}
