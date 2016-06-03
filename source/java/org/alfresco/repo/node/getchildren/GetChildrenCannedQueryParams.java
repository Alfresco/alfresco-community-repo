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
