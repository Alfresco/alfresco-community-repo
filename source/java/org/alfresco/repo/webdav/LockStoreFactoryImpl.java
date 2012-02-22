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

import com.hazelcast.core.HazelcastInstance;


/**
 * Default implementation of the {@link LockStoreFactory} interface. Creates {@link LockStore}s
 * backed by a Hazelcast distributed {@link java.util.Map Map}.
 * 
 * @see LockStoreFactory
 * @see LockStoreImpl
 * @author Matt Ward
 */
public class LockStoreFactoryImpl implements LockStoreFactory
{
    private HazelcastInstance hazelcast;
    
    @Override
    public LockStore getLockStore()
    {
        ConcurrentMap<NodeRef, LockInfo> map = hazelcast.getMap("webdav-locks");
        return new LockStoreImpl(map);
    }

    /**
     * @param hazelcast the hazelcast to set
     */
    public void setHazelcast(HazelcastInstance hazelcast)
    {
        this.hazelcast = hazelcast;
    }
}
