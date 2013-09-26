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
package org.alfresco.repo.lock.mem;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.alfresco.repo.lock.LockServiceImpl;
import org.alfresco.service.cmr.repository.NodeRef;

import com.google.common.collect.MapMaker;

/**
 * {@link LockStore} implementation backed by a Google {@link ConcurrentMap}.
 * 
 * @author Matt Ward
 */
public class LockStoreImpl extends AbstractLockStore<ConcurrentMap<NodeRef, LockState>>
{
    /**
     * Default constructor.
     */
    public LockStoreImpl()
    {
        super(createMap(LockServiceImpl.MAX_EPHEMERAL_LOCK_SECONDS, TimeUnit.SECONDS));
    }
    
    /**
     * Constructor allowing specification of TTLs.
     * 
     * @param ephemeralTTLSeconds
     */
    public LockStoreImpl(int ephemeralTTLSeconds)
    {
        super(createMap(ephemeralTTLSeconds, TimeUnit.SECONDS));
    }
    
    private static ConcurrentMap<NodeRef, LockState> createMap(long expiry, TimeUnit timeUnit)
    {
        ConcurrentMap<NodeRef, LockState> map = new MapMaker()
                    .concurrencyLevel(32)
                    .expiration(expiry, timeUnit)
                    .makeMap();
        return map;
    }
}
