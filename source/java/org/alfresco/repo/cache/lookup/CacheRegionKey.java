package org.alfresco.repo.cache.lookup;

import java.io.Serializable;

/**
 * Key-wrapper used to separate cache regions, allowing a single cache to be used for different
 * purposes.<b/>
 * This class is distinct from the ID key so that ID-based lookups don't class with value-based lookups. 
 */
public class CacheRegionKey implements Serializable
{
    private static final long serialVersionUID = -213050301938804468L;

    private final String cacheRegion;
    private final Serializable cacheKey;
    private final int hashCode;
    public CacheRegionKey(String cacheRegion, Serializable cacheKey)
    {
        this.cacheRegion = cacheRegion;
        this.cacheKey = cacheKey;
        this.hashCode = cacheRegion.hashCode() + cacheKey.hashCode();
    }
    @Override
    public String toString()
    {
        return cacheRegion + "." + cacheKey.toString();
    }
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (!(obj instanceof CacheRegionKey))
        {
            return false;
        }
        CacheRegionKey that = (CacheRegionKey) obj;
        return this.cacheRegion.equals(that.cacheRegion) && this.cacheKey.equals(that.cacheKey);
    }
    @Override
    public int hashCode()
    {
        return hashCode;
    }
}