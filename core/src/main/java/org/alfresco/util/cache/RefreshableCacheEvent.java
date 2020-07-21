package org.alfresco.util.cache;

import java.io.Serializable;

public interface RefreshableCacheEvent extends Serializable
{
    /**
     * Get the cache id
     */
    public String getCacheId();
    
        
    /**
     * Get the affected key/tenant id
     */
    public String getKey();
    
}