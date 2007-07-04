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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.alfresco.repo.domain.ChildAssoc;
import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.domain.Store;
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
    private QName typeQName;
    private Set<QName> aspects;
    private Collection<ChildAssoc> parentAssocs;
    private Map<QName, PropertyValue> properties;
    private DbAccessControlList accessControlList;
    
    private transient ReadLock refReadLock;
    private transient WriteLock refWriteLock;
    private transient NodeRef nodeRef;

    public NodeImpl()
    {
        aspects = new HashSet<QName>(5);
        parentAssocs = new HashSet<ChildAssoc>(5);
        properties = new HashMap<QName, PropertyValue>(5);
        
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        refReadLock = lock.readLock();
        refWriteLock = lock.writeLock();
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
    
    /**
     * @see #getNodeRef()
     */
    public String toString()
    {
        return getNodeRef().toString();
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

    public QName getTypeQName()
    {
        return typeQName;
    }

    public void setTypeQName(QName typeQName)
    {
        this.typeQName = typeQName;
    }

    public Set<QName> getAspects()
    {
        return aspects;
    }
    
    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setAspects(Set<QName> aspects)
    {
        this.aspects = aspects;
    }

    public Collection<ChildAssoc> getParentAssocs()
    {
        return parentAssocs;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setParentAssocs(Collection<ChildAssoc> parentAssocs)
    {
        this.parentAssocs = parentAssocs;
    }

    public Map<QName, PropertyValue> getProperties()
    {
        return properties;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setProperties(Map<QName, PropertyValue> properties)
    {
        this.properties = properties;
    }

    public DbAccessControlList getAccessControlList()
    {
        return accessControlList;
    }

    public void setAccessControlList(DbAccessControlList accessControlList)
    {
        this.accessControlList = accessControlList;
    }
}
