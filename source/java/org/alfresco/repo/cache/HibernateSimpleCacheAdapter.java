package org.alfresco.repo.cache;

import java.io.Serializable;
import java.util.Map;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.Timestamper;

/**
 * Adapts a {@link SimpleCache} instance for use as a Hibernate {@link Cache}.
 *
 * @author Matt Ward
 */
public class HibernateSimpleCacheAdapter implements Cache
{
    private final SimpleCache<Serializable, Object> cache;
    private final String regionName;
    
    /**
     * Adapt a 
     * @param cache SimpleCache<Serializable, Object>
     * @param regionName String
     */
    public HibernateSimpleCacheAdapter(SimpleCache<Serializable, Object> cache, String regionName)
    {
        this.cache = cache;
        this.regionName = regionName;
    }

    @Override
    public Object read(Object key) throws CacheException
    {
        return cache.get(serializable(key));
    }

    @Override
    public Object get(Object key) throws CacheException
    {
        return cache.get(serializable(key));
    }

    @Override
    public void put(Object key, Object value) throws CacheException
    {
        cache.put(serializable(key), value);
    }

    @Override
    public void update(Object key, Object value) throws CacheException
    {
        cache.put(serializable(key), value);
    }

    @Override
    public void remove(Object key) throws CacheException
    {
        cache.remove(serializable(key));
    }

    @Override
    public void clear() throws CacheException
    {
        cache.clear();
    }

    @Override
    public void destroy() throws CacheException
    {
        // NoOp
    }

    @Override
    public void lock(Object key) throws CacheException
    {
        // NoOp
    }

    @Override
    public void unlock(Object key) throws CacheException
    {
        // NoOp
    }

    @Override
    public long nextTimestamp()
    {
        return Timestamper.next();
    }

    @Override
    public int getTimeout()
    {
        return Timestamper.ONE_MS * 60000; // 1 minute
    }

    @Override
    public String getRegionName()
    {
        return regionName;
    }

    @Override
    public long getSizeInMemory()
    {
        return -1;
    }

    @Override
    public long getElementCountInMemory()
    {
        return -1;
    }

    @Override
    public long getElementCountOnDisk()
    {
        return 0;
    }

    @Override
    public Map toMap()
    {
        throw new UnsupportedOperationException();
    }

    private Serializable serializable(Object obj)
    {
        if (!(obj instanceof Serializable))
        {
            throw new IllegalArgumentException("Object is not Serializable, class=" + obj.getClass().getName());
        }
        return (Serializable) obj;
    }
    
}
