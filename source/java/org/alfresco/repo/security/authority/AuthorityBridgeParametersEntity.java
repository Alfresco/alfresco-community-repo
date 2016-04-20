package org.alfresco.repo.security.authority;

/**
 * @author Andy
 *
 */
public class AuthorityBridgeParametersEntity
{
    Long typeQNameId;
    
    Long storeId;
    
    Long childAssocTypeQNameId;
    
    Long authorityNameQNameId;
    
    Long nodeId;
    
    public AuthorityBridgeParametersEntity()
    {
        
    }
    
    public AuthorityBridgeParametersEntity(Long typeQNameId, Long childAssocTypeQNameId, Long authorityNameQNameId, Long storeId)
    {
        this.typeQNameId = typeQNameId;
        this.childAssocTypeQNameId = childAssocTypeQNameId;
        this.storeId = storeId;
        this.authorityNameQNameId = authorityNameQNameId;
    }
    
    public AuthorityBridgeParametersEntity(Long typeQNameId, Long childAssocTypeQNameId, Long authorityNameQNameId, Long storeId, Long nodeId)
    {
        this(typeQNameId, childAssocTypeQNameId, authorityNameQNameId, storeId);
        this.nodeId = nodeId;
    }

    /**
     * @return the typeQNameId
     */
    public Long getTypeQNameId()
    {
        return typeQNameId;
    }

    /**
     * @param typeQNameId the typeQNameId to set
     */
    public void setTypeQNameId(Long typeQNameId)
    {
        this.typeQNameId = typeQNameId;
    }

    /**
     * @return the storeId
     */
    public Long getStoreId()
    {
        return storeId;
    }

    /**
     * @param storeId the storeId to set
     */
    public void setStoreId(Long storeId)
    {
        this.storeId = storeId;
    }

    /**
     * @return the childAssocTypeQNameId
     */
    public Long getChildAssocTypeQNameId()
    {
        return childAssocTypeQNameId;
    }

    /**
     * @param childAssocTypeQNameId the childAssocTypeQNameId to set
     */
    public void setChildAssocTypeQNameId(Long childAssocTypeQNameId)
    {
        this.childAssocTypeQNameId = childAssocTypeQNameId;
    }

    /**
     * @return the authorityNameQNameId
     */
    public Long getAuthorityNameQNameId()
    {
        return authorityNameQNameId;
    }

    /**
     * @param authorityNameQNameId the authorityNameQNameId to set
     */
    public void setAuthorityNameQNameId(Long authorityNameQNameId)
    {
        this.authorityNameQNameId = authorityNameQNameId;
    }

    /**
     * @return the childName
     */
    public Long getNodeId()
    {
        return nodeId;
    }

    /**
     * @param nodeId the node id to set
     */
    public void setNodeId(Long nodeId)
    {
        this.nodeId = nodeId;
    }

    
}
