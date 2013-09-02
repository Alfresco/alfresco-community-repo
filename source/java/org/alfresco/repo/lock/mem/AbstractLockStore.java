/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.lock.mem;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Base class for LockStore implementations that use a ConcurrentMap as storage.
 * 
 * @author Matt Ward
 */
public abstract class AbstractLockStore<T extends ConcurrentMap<NodeRef, LockState>> implements LockStore
{
    protected long maxTryLockMillis = 100;
    protected T map;
    
    public AbstractLockStore(T map)
    {
        this.map = map;
    }
    
    /**
     * Set the maximum time a lock store should wait while trying to acquire a concurrency lock.
     * 
     * @see #acquireConcurrencyLock(NodeRef)
     * @param maxTryLockMillis
     */
    @Override
    public void setMaxTryLockMillis(long maxTryLockMillis)
    {
        this.maxTryLockMillis = maxTryLockMillis;
    }



    @Override
    public LockState get(NodeRef nodeRef)
    {
        // Always lock on NodeRef related LockStore operations. Otherwise it is possible
        // to, for example, get LockState data that is not consistent with the props in persistent storage.
        acquireConcurrencyLock(nodeRef);
        try
        {
            return map.get(nodeRef);
        }
        finally
        {
            releaseConcurrencyLock(nodeRef);
        }
    }

    @Override
    public boolean contains(NodeRef nodeRef)
    {
        acquireConcurrencyLock(nodeRef);
        try
        {
            return map.containsKey(nodeRef);
        }
        finally
        {
            releaseConcurrencyLock(nodeRef);
        }
    }

    @Override
    public void set(NodeRef nodeRef, LockState lockState)
    {
        acquireConcurrencyLock(nodeRef);
        try
        {
            doSet(nodeRef, lockState);
        }
        finally
        {
            releaseConcurrencyLock(nodeRef);
        }
    }

    protected abstract void doSet(NodeRef nodeRef, LockState lockState);

    @Override
    public void clear()
    {
        // TODO: lock whole map?
        map.clear();
    }

    @Override
    public abstract void acquireConcurrencyLock(NodeRef nodeRef);

    @Override
    public abstract void releaseConcurrencyLock(NodeRef nodeRef);

    @Override
    public Set<NodeRef> getNodes()
    {
        // TODO: lock whole map?
        return map.keySet();
    }
}
