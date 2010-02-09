/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.alfresco.repo.domain.AuditableProperties;
import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.NodePropertyValue;
import org.alfresco.repo.domain.PropertyMapKey;
import org.alfresco.repo.domain.Store;
import org.alfresco.repo.domain.Transaction;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;

/**
 * Bean containing all the persistence data representing a <b>node</b>.
 * <p>
 * This implementation of the {@link org.alfresco.repo.domain.Node Node} interface is
 * Hibernate specific.
 * 
 * @author Derek Hulley
 */
public class NodeImpl extends LifecycleAdapter implements Node, Serializable
{
    private static final long serialVersionUID = -2101330674810283053L;

    private Long id;
    private Long version;
    private Store store;
    private String uuid;
    private Long typeQNameId;
    private Transaction transaction;
    private boolean deleted;
    private DbAccessControlList accessControlList;
    private Set<Long> aspects;
    private Map<PropertyMapKey, NodePropertyValue> properties;
    private AuditableProperties auditableProperties;
    
    private transient ReadLock refReadLock;
    private transient WriteLock refWriteLock;
    private transient NodeRef nodeRef;
    private transient QName typeQName;

    public NodeImpl()
    {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        refReadLock = lock.readLock();
        refWriteLock = lock.writeLock();

        aspects = new HashSet<Long>(5);
        properties = new HashMap<PropertyMapKey, NodePropertyValue>(5);
        // Note auditableProperties starts null, as hibernate maps a component containing nulls to null and this would
        // cause a lot of dirty checks to fail!
    }

    /**
     * Thread-safe caching of the reference is provided
     */
    public NodeRef getNodeRef()
    {
        // first check if it is available
        refReadLock.lock();
        try
        {
            if (nodeRef != null)
            {
                return nodeRef;
            }
        }
        finally
        {
            refReadLock.unlock();
        }
        // get write lock
        refWriteLock.lock();
        try
        {
            // double check
            if (nodeRef == null )
            {
                nodeRef = new NodeRef(getStore().getStoreRef(), getUuid());
            }
            return nodeRef;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }
    
    public QName getTypeQName(QNameDAO qnameDAO)
    {
        refReadLock.lock();
        try
        {
            if (typeQName != null)
            {
                return typeQName;
            }
        }
        finally
        {
            refReadLock.unlock();
        }
        refWriteLock.lock();
        try
        {
            typeQName = qnameDAO.getQName(typeQNameId).getSecond();
            return typeQName;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }

    public void setTypeQName(QNameDAO qnameDAO, QName qname)
    {
        refWriteLock.lock();
        try
        {
            Long typeQNameId = qnameDAO.getOrCreateQName(qname).getFirst();
            setTypeQNameId(typeQNameId);
        }
        finally
        {
            refWriteLock.unlock();
        }
    }

    /**
     * @see #getNodeRef()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(50);
        sb.append("Node")
          .append("[id=").append(id)
          .append(", ref=").append(getNodeRef())
          .append(", txn=").append(transaction)
          .append(", deleted=").append(deleted)
          .append("]");
        return sb.toString();
    }
    
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (obj == this)
        {
            return true;
        }
        else if (!(obj instanceof Node))
        {
            return false;
        }
        Node that = (Node) obj;
        if (EqualsHelper.nullSafeEquals(id, that.getId()))
        {
            return true;
        }
        else
        {
            return (this.getNodeRef().equals(that.getNodeRef()));
        }
    }
    
    public int hashCode()
    {
        return getUuid().hashCode();
    }

    public Long getId()
    {
        return id;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setId(Long id)
    {
        this.id = id;
    }

    public Store getStore()
    {
        return store;
    }

    public void setStore(Store store)
    {
        refWriteLock.lock();
        try
        {
            this.store = store;
            this.nodeRef = null;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid(String uuid)
    {
        refWriteLock.lock();
        try
        {
            this.uuid = uuid;
            this.nodeRef = null;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }

    public Long getVersion()
    {
        return version;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setVersion(Long version)
    {
        this.version = version;
    }

    public Transaction getTransaction()
    {
        return transaction;
    }

    public void setTransaction(Transaction transaction)
    {
        this.transaction = transaction;
    }

    public boolean getDeleted()
    {
        return deleted;
    }

    public void setDeleted(boolean deleted)
    {
        this.deleted = deleted;
    }

    public Long getTypeQNameId()
    {
        return typeQNameId;
    }

    public void setTypeQNameId(Long typeQNameId)
    {
        refWriteLock.lock();
        try
        {
            this.typeQNameId = typeQNameId;
            this.typeQName = null;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }

    public DbAccessControlList getAccessControlList()
    {
        return accessControlList;
    }

    public void setAccessControlList(DbAccessControlList accessControlList)
    {
        this.accessControlList = accessControlList;
    }

    public Set<Long> getAspects()
    {
        return aspects;
    }
    
    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setAspects(Set<Long> aspects)
    {
        this.aspects = aspects;
    }

    public Map<PropertyMapKey, NodePropertyValue> getProperties()
    {
        return properties;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setProperties(Map<PropertyMapKey, NodePropertyValue> properties)
    {
        this.properties = properties;
    }

    public AuditableProperties getAuditableProperties()
    {
        return auditableProperties;
    }

    public void setAuditableProperties(AuditableProperties auditableProperties)
    {
        this.auditableProperties = auditableProperties;
    }
}
