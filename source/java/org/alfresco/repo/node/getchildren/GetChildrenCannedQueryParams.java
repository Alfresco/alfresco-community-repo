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
package org.alfresco.repo.node.getchildren;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * GetChildren CQ parameters - for query context and filtering
 *
 * @author janv
 * @since 4.0
 */
public class GetChildrenCannedQueryParams
{
    private NodeRef parentRef;
    
    private Set<QName> childTypeQNames = Collections.emptySet();
    private List<FilterProp> filterProps = Collections.emptyList();
    private Set<QName> assocTypeQNames = null;
    private String pattern = null;
    private Set<QName> inclusiveAspects = null;
    private Set<QName> exclusiveAspects = null;
    
    public GetChildrenCannedQueryParams(
            NodeRef parentRef,
            Set<QName> assocTypeQNames,
            Set<QName> childTypeQNames,
            Set<QName> inclusiveAspects,
            Set<QName> exclusiveAspects,
            List<FilterProp> filterProps,
            String pattern)
    {
        this.parentRef = parentRef;
        this.assocTypeQNames = assocTypeQNames;

        if (childTypeQNames != null) { this.childTypeQNames = childTypeQNames; }
        this.inclusiveAspects = inclusiveAspects;
        this.exclusiveAspects = exclusiveAspects;
        if (filterProps != null) { this.filterProps = filterProps; }
        if (pattern != null)
        {
        	this.pattern = pattern;
        } 
    }
    
    public NodeRef getParentRef()
    {
        return parentRef;
    }
    
    public Set<QName> getChildTypeQNames()
    {
        return childTypeQNames;
    }
    
    public Set<QName> getAssocTypeQNames()
    {
		return assocTypeQNames;
	}

	public List<FilterProp>  getFilterProps()
    {
        return filterProps;
    }

	public String getPattern()
	{
		return pattern;
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
