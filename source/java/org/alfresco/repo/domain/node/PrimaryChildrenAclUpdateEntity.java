package org.alfresco.repo.domain.node;

/**
 * Carry bulk acl update info.
 * 
 * @author andyh
 * @since 3.4
 *
 */
public class PrimaryChildrenAclUpdateEntity
{
    Long txnId;
    Long primaryParentNodeId; 
    Long optionalOldSharedAclIdInAdditionToNull;
    Long newSharedAclId;
    
    public PrimaryChildrenAclUpdateEntity()
    {
    }

    public Long getTxnId()
    {
        return txnId;
    }

    public void setTxnId(Long txnId)
    {
        this.txnId = txnId;
    }

    public Long getPrimaryParentNodeId()
    {
        return primaryParentNodeId;
    }

    public void setPrimaryParentNodeId(Long primaryParentNodeId)
    {
        this.primaryParentNodeId = primaryParentNodeId;
    }

    public Long getOptionalOldSharedAclIdInAdditionToNull()
    {
        return optionalOldSharedAclIdInAdditionToNull;
    }

    public void setOptionalOldSharedAclIdInAdditionToNull(Long optionalOldSharedAclIdInAdditionToNull)
    {
        this.optionalOldSharedAclIdInAdditionToNull = optionalOldSharedAclIdInAdditionToNull;
    }

    public Long getNewSharedAclId()
    {
        return newSharedAclId;
    }

    public void setNewSharedAclId(Long newSharedAclId)
    {
        this.newSharedAclId = newSharedAclId;
    }
    
    public boolean getIsPrimary()
    {
        return true;
    }
    
}
