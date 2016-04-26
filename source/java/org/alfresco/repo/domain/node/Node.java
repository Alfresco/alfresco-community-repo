package org.alfresco.repo.domain.node;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * Interface for beans carrying general information for <b>alf_node</b> data.
 * 
 * @author andyh
 */
public interface Node extends NodeIdAndAclId
{
    /**
     * Helper method to get a key that includes the node and its current version number
     */
    public NodeVersionKey getNodeVersionKey();
    
    /**
     * Helper method to force the instance to be read-only
     */
    public void lock();
    
    public abstract NodeRef getNodeRef();

    public NodeRef.Status getNodeStatus(QNameDAO qnameDAO);
    
    public abstract Pair<Long, NodeRef> getNodePair();
    
    /**
     * Checks the {@link #getTypeQNameId() type} of the node to determine if the node is deleted
     * @param qnameDAO          DAO to work out type IDs
     * @return                  <tt>true</tt> if the node is {@link ContentModel#TYPE_DELETED}
     */
    public abstract boolean getDeleted(QNameDAO qnameDAO);

    public abstract Long getVersion();

    public abstract StoreEntity getStore();

    public abstract String getUuid();

    public abstract Long getTypeQNameId();
    
    public abstract Long getLocaleId();

    public abstract TransactionEntity getTransaction();

    public abstract AuditablePropertiesEntity getAuditableProperties();

}