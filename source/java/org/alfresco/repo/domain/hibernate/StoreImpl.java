/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.domain.hibernate;

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
public class StoreImpl implements Store
{
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