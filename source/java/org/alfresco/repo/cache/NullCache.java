package org.alfresco.repo.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * A cache that does nothing - always.
 * <P/>
 * There are conditions under which code that expects to be caching, should not be.  Using this
 * cache, it becomes possible to configure a valid cache in whilst still ensuring that the
 * actual caching is not performed.
 * 
 * @author Derek Hulley
 */
public class NullCache<K extends Serializable, V extends Object> implements SimpleCache<K, V>
{
    /** Singleton for retrieval via {@link #getInstance() } */
    private static final NullCache<Serializable, Object> INSTANCE = new NullCache<Serializable, Object>();
    
    /**
     * @return          Returns a singleton that can be used in any way - all operations are stateless
     */
    @SuppressWarnings("unchecked")
    public static final <K extends Serializable, V extends Object> NullCache<K, V> getInstance()
    {
        return (NullCache<K, V>) INSTANCE;
    }
    
    public NullCache()
    {
    }

    /** NO-OP */
    public boolean contains(K key)
    {
        return false;
    }

    public Collection<K> getKeys()
    {
        return Collections.<K>emptyList();
    }

    /** NO-OP */
    public V get(K key)
    {
        return null;
    }

    /** NO-OP */
    public void put(K key, V value)
    {
        return;
    }

    /** NO-OP */
    public void remove(K key)
    {
        return;
    }

    /** NO-OP */
    public void clear()
    {
        return;
    }
}
