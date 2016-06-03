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
