package org.alfresco.repo.cache.lookup;

import java.io.Serializable;


/**
 * Value-key-wrapper used to separate cache regions, allowing a single cache to be used for different
 * purposes.<b/>
 * This class is distinct from the region key so that ID-based lookups don't class with value-based lookups. 
 */
public class CacheRegionValueKey implements Serializable
{
    private static final long serialVersionUID = 5838308035326617927L;
    
    private final String cacheRegion;
    private final Serializable cacheValueKey;
    private final int hashCode;
    public CacheRegionValueKey(String cacheRegion, Serializable cacheValueKey)
    {
        this.cacheRegion = cacheRegion;
        this.cacheValueKey = cacheValueKey;
        this.hashCode = cacheRegion.hashCode() + cacheValueKey.hashCode();
    }
    @Override
    public String toString()
    {
        return cacheRegion + "." + cacheValueKey.toString();
    }
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (!(obj instanceof CacheRegionValueKey))
        {
            return false;
        }
        CacheRegionValueKey that = (CacheRegionValueKey) obj;
        return this.cacheRegion.equals(that.cacheRegion) && this.cacheValueKey.equals(that.cacheValueKey);
    }
    @Override
    public int hashCode()
    {
        return hashCode;
    }
}