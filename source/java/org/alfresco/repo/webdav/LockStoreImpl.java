/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.webdav;

import java.util.concurrent.ConcurrentMap;

import org.alfresco.service.cmr.repository.NodeRef;


/**
 * The default {@link LockStore} implementation. This is based upon a ConcurrentMap intended to be supplied by
 * Hazelcast (or a similar, distributed data structure library).
 * 
 * @see LockStore
 * @author Matt Ward
 */
public class LockStoreImpl implements LockStore
{
    private final ConcurrentMap<NodeRef, LockInfo> lockInfoMap;
    
    public LockStoreImpl(ConcurrentMap<NodeRef, LockInfo> lockInfoMap)
    {
        this.lockInfoMap = lockInfoMap;
    }
    
    @Override
    public void put(NodeRef nodeToLock, LockInfo lockInfo)
    {
        lockInfoMap.put(nodeToLock, lockInfo);
    }

    @Override
    public LockInfo get(NodeRef nodeRef)
    {
        return lockInfoMap.get(nodeRef);
    }

    @Override
    public void remove(NodeRef nodeRef)
    {
        lockInfoMap.remove(nodeRef);
    }
}
