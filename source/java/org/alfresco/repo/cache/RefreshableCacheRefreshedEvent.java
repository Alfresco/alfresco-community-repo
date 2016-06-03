package org.alfresco.repo.cache;

/**
 * Describes a new entry has been inserted in the cache.
 * 
 * @author Andy
 *
 */
public class RefreshableCacheRefreshedEvent extends AbstractRefreshableCacheEvent
{

    /**
     * 
     */
    private static final long serialVersionUID = 2352511592269578075L;

    /**
     * @param cacheId String
     * @param tenantId String
     */
    RefreshableCacheRefreshedEvent(String cacheId, String tenantId)
    {
        super(cacheId, tenantId);
    }

}
