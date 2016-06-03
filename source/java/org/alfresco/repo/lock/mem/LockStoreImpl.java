package org.alfresco.repo.lock.mem;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.alfresco.repo.lock.LockServiceImpl;
import org.alfresco.service.cmr.repository.NodeRef;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

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
     * @param ephemeralTTLSeconds int
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
