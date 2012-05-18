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

import org.alfresco.repo.cluster.HazelcastInstanceFactory;
import org.alfresco.service.cmr.repository.NodeRef;

import com.hazelcast.core.HazelcastInstance;


/**
 * Default implementation of the {@link LockStoreFactory} interface. Creates {@link LockStore}s
 * backed by a Hazelcast distributed Map if clustering is enabled,
 * otherwise it creates a non-clustered {@link SimpleLockStore}.
 * 
 * @see LockStoreFactory
 * @see LockStoreImpl
 * @author Matt Ward
 */
public class LockStoreFactoryImpl implements LockStoreFactory
{
    private static final String HAZELCAST_MAP_NAME = "webdav-locks";
    private HazelcastInstanceFactory hazelcastInstanceFactory;
    
    /**
     * This method should be used sparingly and the created {@link LockStore}s should be
     * retained (this factory does not cache instances of them).
     */
    @Override
    public synchronized LockStore createLockStore()
    {
        if (!hazelcastInstanceFactory.isClusteringEnabled())
        {
            return new SimpleLockStore();
        }
        else
        {
            HazelcastInstance instance = hazelcastInstanceFactory.getInstance();
            ConcurrentMap<NodeRef, LockInfo> map = instance.getMap(HAZELCAST_MAP_NAME);
            return new LockStoreImpl(map);
        }
    }

    /**
     * @param hazelcastInstanceFactory the factory that will create a HazelcastInstance if required.
     */
    public synchronized void setHazelcastInstanceFactory(HazelcastInstanceFactory hazelcastInstanceFactory)
    {
        this.hazelcastInstanceFactory = hazelcastInstanceFactory;
    }
}
