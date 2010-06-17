/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.domain.node;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * Bean to convey <b>alf_node</b> data.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class NodeEntity implements Node
{
    private boolean locked;
    
    private Long id;
    private Long version;
    private StoreEntity store;
    private String uuid;
    private Long typeQNameId;
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
          .append("[ ID=").append(id);
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
          .append(", aclId=").append(aclId)
          .append(", deleted=").append(deleted)
          .append(", transaction=").append(transaction)
          .append(", auditProps=").append(auditableProperties)
          .append("]");
        return sb.toString();
    }
    
    /**
     * Lock the entity against further updates to prevent accidental modification
     */
    public void lock()
    {
        locked = true;
    }
    
    private final void checkLock()
    {
        if (locked)
        {
            throw new IllegalStateException("The entity is locked against updates: " + this);
        }
    }
    
    public void incrementVersion()
    {
        if (version >= Short.MAX_VALUE)
        {
            this.version = 0L;
        }
        else
        {
            this.version++;
        }
    }
    
    public NodeRef getNodeRef()
    {
        return new NodeRef(store.getStoreRef(), uuid);
    }
    
    public Pair<Long, NodeRef> getNodePair()
    {
        return new Pair<Long, NodeRef>(id, getNodeRef());
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        checkLock();
        this.id = id;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        checkLock();
        this.version = version;
    }

    public StoreEntity getStore()
    {
        return store;
    }

    public void setStore(StoreEntity store)
    {
        checkLock();
        this.store = store;
    }

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid(String uuid)
    {
        checkLock();
        this.uuid = uuid;
    }

    public Long getTypeQNameId()
    {
        return typeQNameId;
    }

    public void setTypeQNameId(Long typeQNameId)
    {
        checkLock();
        this.typeQNameId = typeQNameId;
    }

    public Long getAclId()
    {
        return aclId;
    }

    public void setAclId(Long aclId)
    {
        checkLock();
        this.aclId = aclId;
    }

    public Boolean getDeleted()
    {
        return deleted;
    }

    public void setDeleted(Boolean deleted)
    {
        checkLock();
        this.deleted = deleted;
    }

    public TransactionEntity getTransaction()
    {
        return transaction;
    }

    public void setTransaction(TransactionEntity transaction)
    {
        checkLock();
        this.transaction = transaction;
    }

    public AuditablePropertiesEntity getAuditableProperties()
    {
        return auditableProperties;
    }

    public void setAuditableProperties(AuditablePropertiesEntity auditableProperties)
    {
        checkLock();
        this.auditableProperties = auditableProperties;
    }
}
