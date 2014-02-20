package org.alfresco.repo.search.impl.solr;

import org.alfresco.repo.domain.node.AuditablePropertiesEntity;
import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.node.NodeVersionKey;
import org.alfresco.repo.domain.node.StoreEntity;
import org.alfresco.repo.domain.node.TransactionEntity;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.alfresco.util.Pair;

class TestNode implements Node
{
    NodeRef nodeRef;
    
    TestNode(String id)
    {
        nodeRef = new NodeRef("test://store/" + id);
    }
    
    @Override
    public Long getId()
    {
        return null;
    }

    @Override
    public Long getAclId()
    {
        return null;
    }

    @Override
    public NodeVersionKey getNodeVersionKey()
    {
        return null;
    }

    @Override
    public void lock()
    {
    }

    @Override
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    @Override
    public Status getNodeStatus(QNameDAO qnameDAO)
    {
        return null;
    }

    @Override
    public Pair<Long, NodeRef> getNodePair()
    {
        return null;
    }

    @Override
    public boolean getDeleted(QNameDAO qnameDAO)
    {
        return false;
    }

    @Override
    public Long getVersion()
    {
        return null;
    }

    @Override
    public StoreEntity getStore()
    {
        return null;
    }

    @Override
    public String getUuid()
    {
        return null;
    }

    @Override
    public Long getTypeQNameId()
    {
        return null;
    }

    @Override
    public Long getLocaleId()
    {
        return null;
    }

    @Override
    public TransactionEntity getTransaction()
    {
        return null;
    }

    @Override
    public AuditablePropertiesEntity getAuditableProperties()
    {
        return null;
    }

}
