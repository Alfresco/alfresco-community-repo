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
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.Store;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.EqualsHelper;

/**
 * Hibernate-specific implementation of the domain entity <b>store</b>.
 * 
 * @author Derek Hulley
 */
public class StoreImpl implements Store, Serializable
{
    private static final long serialVersionUID = -5501292033972362796L;
    
    private Long id;
    private String protocol;
    private String identifier;
    private Long version;
    private Node rootNode;

    private transient ReadLock refReadLock;
    private transient WriteLock refWriteLock;
    private transient StoreRef storeRef;

    public StoreImpl()
    {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        refReadLock = lock.readLock();
        refWriteLock = lock.writeLock();
    }
    
    /**
     * Lazily constructs <code>StoreRef</code> instance referencing this entity
     */
    public StoreRef getStoreRef()
    {
        // first check if it is available
        refReadLock.lock();
        try
        {
            if (storeRef != null)
            {
                return storeRef;
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
            if (storeRef == null )
            {
                storeRef = new StoreRef(protocol, identifier);
            }
            return storeRef;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }
    
    /**
     * @see #getStoreRef()()
     */
    public String toString()
    {
        return getStoreRef().toString();
    }
    
    /**
     * @see #getKey()
     */
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
        else if (!(obj instanceof Store))
        {
            return false;
        }
        Store that = (Store) obj;
        return EqualsHelper.nullSafeEquals(this.getStoreRef(), that.getStoreRef());
    }
    
    /**
     * @see #getKey()
     */
    public int hashCode()
    {
        return protocol.hashCode() + identifier.hashCode();
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
    
    public String getProtocol()
    {
        return protocol;
    }
    
    public void setProtocol(String protocol)
    {
        refWriteLock.lock();
        try
        {
            this.protocol = protocol;
            this.storeRef = null;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }
    
    public String getIdentifier()
    {
        return identifier;
    }
    
    public void setIdentifier(String identifier)
    {
        refWriteLock.lock();
        try
        {
            this.identifier = identifier;
            this.storeRef = null;
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

    public Node getRootNode()
    {
        return rootNode;
    }
    
    public void setRootNode(Node rootNode)
    {
        this.rootNode = rootNode;
    }
}