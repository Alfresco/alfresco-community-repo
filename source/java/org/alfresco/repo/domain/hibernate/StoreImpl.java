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
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.Store;
import org.alfresco.repo.domain.StoreKey;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * Hibernate-specific implementation of the domain entity <b>store</b>.
 * 
 * @author Derek Hulley
 */
public class StoreImpl implements Store, Serializable
{
    private static final long serialVersionUID = -6135740209100885890L;

    private StoreKey key;
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
                storeRef = new StoreRef(getKey().getProtocol(), getKey().getIdentifier());
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
        return (this.getKey().equals(that.getKey()));
    }
    
    /**
     * @see #getKey()
     */
    public int hashCode()
    {
        return getKey().hashCode();
    }
    
    public StoreKey getKey()
    {
        return key;
    }
    
    public void setKey(StoreKey key)
    {
        refWriteLock.lock();
        try
        {
            this.key = key;
            this.storeRef = null;
        }
        finally
        {
            refWriteLock.unlock();
        }
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