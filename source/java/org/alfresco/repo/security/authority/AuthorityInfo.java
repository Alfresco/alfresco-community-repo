package org.alfresco.repo.security.authority;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;

/**
 * Authority Info - used by GetAuthorities CQ
 *
 * @author janv
 * @since 4.0
 */
@AlfrescoPublicApi
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
