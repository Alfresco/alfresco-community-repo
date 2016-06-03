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
