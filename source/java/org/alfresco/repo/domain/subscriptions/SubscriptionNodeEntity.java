package org.alfresco.repo.domain.subscriptions;

import org.alfresco.service.cmr.repository.NodeRef;

public class SubscriptionNodeEntity
{
    private String protocol;
    private String identifier;
    private String id;

    public String getProtocol()
    {
        return protocol;
    }

    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public NodeRef getNodeRef()
    {
        return new NodeRef(protocol, identifier, id);
    }
}
