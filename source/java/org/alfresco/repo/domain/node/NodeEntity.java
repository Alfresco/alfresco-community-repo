/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.domain.node;

import org.alfresco.repo.security.permissions.PermissionCheckValue;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * Bean to convey <b>alf_node</b> data.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class NodeEntity implements Node, PermissionCheckValue
{
    private boolean locked;
    
    private Long id;
    private Long version;
    private StoreEntity store;
    private String uuid;
    private Long typeQNameId;
    private Long localeId;
    private Long aclId;
    private Boolean deleted;
    private TransactionEntity transaction;
    private AuditablePropertiesEntity auditableProperties;
    
    /**
     * Required default constructor
     */
    public NodeEntity()
    {
        locked = false;
    }
    
    /**
     * Helper constructor to build the necessary elements to fulfill the {@link #getNodeRef()} query
     */
    /* package */ NodeEntity(NodeRef nodeRef)
    {
        this();
        this.store = new StoreEntity();
        this.store.setProtocol(nodeRef.getStoreRef().getProtocol());
        this.store.setIdentifier(nodeRef.getStoreRef().getIdentifier());
        this.uuid = nodeRef.getId();
    }
    
    /**
     * Helper copy constructor
     */
    /* package */ NodeEntity(Node node)
    {
        this.id = node.getId();
        this.version = node.getVersion();
        this.store = node.getStore();
        this.uuid = node.getUuid();
        this.typeQNameId = node.getTypeQNameId();
        this.localeId = node.getLocaleId();
        this.aclId = node.getAclId();
        this.deleted = node.getDeleted();
        this.transaction = node.getTransaction();
        this.auditableProperties = node.getAuditableProperties();
    }
        
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("NodeEntity")
          .append("[ ID=").append(id)
          .append(", version=").append(version);
        if (store != null)
        {
            sb.append(", store=").append(store.getProtocol()).append("://").append(store.getIdentifier());
        }
        else
        {
            sb.append(", store=").append("null");
        }
        sb.append(", uuid=").append(uuid)
          .append(", typeQNameId=").append(typeQNameId)
          .append(", localeId=").append(localeId)
          .append(", aclId=").append(aclId)
          .append(", deleted=").append(deleted)
          .append(", transaction=").append(transaction)
          .append(", auditProps=").append(auditableProperties)
          .append("]");
        return sb.toString();
    }
    
    @Override
    // TODO: Must cache the key
    public NodeVersionKey getNodeVersionKey()
    {
        if (id == null || version == null)
        {
            throw new IllegalStateException("The NodeEntity has not be filled: " + this);
        }
        return new NodeVersionKey(id, version);
    }
    
    /**
     * Lock the entity against further updates to prevent accidental modification
     */
    public synchronized void lock()
    {
        locked = true;
        if (auditableProperties != null)
        {
            auditableProperties.lock();
        }
    }
    
    private synchronized final void checkLock()
    {
        if (locked)
        {
            throw new IllegalStateException("The entity is locked against updates: " + this);
        }
    }
    
    public synchronized void incrementVersion()
    {
        checkLock();
        if (version >= Short.MAX_VALUE)
        {
            this.version = 0L;
        }
        else
        {
            this.version++;
        }
    }
    
    @Override
    public NodeRef getNodeRef()
    {
        return new NodeRef(store.getStoreRef(), uuid);
    }
    
    @Override
    public NodeRef.Status getNodeStatus()
    {
        NodeRef nodeRef = new NodeRef(store.getStoreRef(), uuid);
        return new NodeRef.Status(nodeRef, transaction.getChangeTxnId(), transaction.getId(), deleted);
    }
    
    @Override
    public Pair<Long, NodeRef> getNodePair()
    {
        return new Pair<Long, NodeRef>(id, getNodeRef());
    }

    @Override
    public Long getId()
    {
        return id;
    }

    public synchronized void setId(Long id)
    {
        checkLock();
        this.id = id;
    }

    @Override
    public Long getVersion()
    {
        return version;
    }

    public synchronized void setVersion(Long version)
    {
        checkLock();
        this.version = version;
    }

    @Override
    public StoreEntity getStore()
    {
        return store;
    }

    public synchronized void setStore(StoreEntity store)
    {
        checkLock();
        this.store = store;
    }

    @Override
    public String getUuid()
    {
        return uuid;
    }

    public synchronized void setUuid(String uuid)
    {
        checkLock();
        this.uuid = uuid;
    }

    @Override
    public Long getTypeQNameId()
    {
        return typeQNameId;
    }

    public synchronized void setTypeQNameId(Long typeQNameId)
    {
        checkLock();
        this.typeQNameId = typeQNameId;
    }

    @Override
    public Long getLocaleId()
    {
        return localeId;
    }

    public synchronized void setLocaleId(Long localeId)
    {
        this.localeId = localeId;
    }

    @Override
    public Long getAclId()
    {
        return aclId;
    }

    public synchronized void setAclId(Long aclId)
    {
        checkLock();
        this.aclId = aclId;
    }

    @Override
    public Boolean getDeleted()
    {
        return deleted;
    }

    public synchronized void setDeleted(Boolean deleted)
    {
        checkLock();
        this.deleted = deleted;
    }

    @Override
    public TransactionEntity getTransaction()
    {
        return transaction;
    }

    public synchronized void setTransaction(TransactionEntity transaction)
    {
        checkLock();
        this.transaction = transaction;
    }

    @Override
    public AuditablePropertiesEntity getAuditableProperties()
    {
        return auditableProperties;
    }

    public synchronized void setAuditableProperties(AuditablePropertiesEntity auditableProperties)
    {
        checkLock();
        this.auditableProperties = auditableProperties;
    }
}
