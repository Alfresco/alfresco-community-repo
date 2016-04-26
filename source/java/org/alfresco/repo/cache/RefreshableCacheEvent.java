package org.alfresco.repo.cache;

import java.io.Serializable;

/**
 * A cache event
 * 
 * @author Andy
 *
 */
public interface RefreshableCacheEvent extends Serializable
{
    /**
     * Get the cache id
     */
    public String getCacheId();
    
    /**
     * Get the affected tenant id
     */
    public String getTenantId();
}
