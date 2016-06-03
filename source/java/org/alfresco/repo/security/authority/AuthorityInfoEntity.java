package org.alfresco.repo.security.authority;

import org.alfresco.repo.domain.node.NodeEntity;

/**
 * Authority Info Entity - used by GetAuthorities CQ
 *
 * @author jan
 * @since 4.0
 */
public class AuthorityInfoEntity
{
    private Long id; // node id
    
    private NodeEntity node;
    
    private String authorityDisplayName;
    private String authorityName;
    
    // Supplemental query-related parameters
    private Long parentNodeId;
    private Long authorityDisplayNameQNameId;
    
    /**
     * Default constructor
     */
    public AuthorityInfoEntity()
    {
    }
    
    public AuthorityInfoEntity(Long parentNodeId, Long authorityDisplayNameQNameId)
    {
        this.parentNodeId = parentNodeId;
        this.authorityDisplayNameQNameId = authorityDisplayNameQNameId;
    }
    
    public Long getId()
    {
        return id;
    }
    
    public void setId(Long id)
    {
        this.id = id;
    }
    
    public String getAuthorityDisplayName()
    {
        return authorityDisplayName;
    }
    
    public void setAuthorityDisplayName(String authorityDisplayName)
    {
        this.authorityDisplayName = authorityDisplayName;
    }
    
    public String getAuthorityName()
    {
        return authorityName;
    }
    
    public void setAuthorityName(String authorityName)
    {
        this.authorityName = authorityName;
    }
    
    public NodeEntity getNode()
    {
        return node;
    }
    
    public void setNode(NodeEntity childNode)
    {
        this.node = childNode;
    }
    
    // Supplemental query-related parameters
    
    public Long getParentNodeId()
    {
        return parentNodeId;
    }
    
    public Long getAuthorityDisplayNameQNameId()
    {
        return authorityDisplayNameQNameId;
    }
}