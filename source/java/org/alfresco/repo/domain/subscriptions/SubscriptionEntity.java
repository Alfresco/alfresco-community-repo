package org.alfresco.repo.domain.subscriptions;

public class SubscriptionEntity
{
    private Long userNodeId;
    private Long nodeId;
    private Long deletedTypeQNameId;

    public Long getUserNodeId()
    {
        return userNodeId;
    }

    public void setUserNodeId(Long userNodeId)
    {
        this.userNodeId = userNodeId;
    }

    public Long getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(Long nodeId)
    {
        this.nodeId = nodeId;
    }

    public Long getDeletedTypeQNameId()
    {
        return deletedTypeQNameId;
    }

    public void setDeletedTypeQNameId(Long deletedTypeQNameId)
    {
        this.deletedTypeQNameId = deletedTypeQNameId;
    }
}
