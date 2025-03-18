/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.lock.mem;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.alfresco.repo.lock.LockServiceImpl;
import org.alfresco.service.cmr.repository.NodeRef;

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
     *            int
     */
    public LockStoreImpl(int ephemeralTTLSeconds)
    {
        super(createMap(ephemeralTTLSeconds, TimeUnit.SECONDS));
    }

    private static ConcurrentMap<NodeRef, LockState> createMap(long expiry, TimeUnit timeUnit)
    {
        Cache<NodeRef, LockState> cache = CacheBuilder.newBuilder()
                .concurrencyLevel(32)
                .expireAfterWrite(expiry, timeUnit)
                .build();
        return cache.asMap();
    }
}
