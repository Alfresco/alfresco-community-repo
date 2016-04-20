package org.alfresco.repo.cache;

/**
 * Describes an entry that is stale in the cache
 * 
 * @author Andy
 *
 */
public class RefreshableCacheRefreshEvent extends AbstractRefreshableCacheEvent
{
    /**
     *
     * @param cacheId String
     * @param tenantId String
     */
    RefreshableCacheRefreshEvent(String cacheId, String tenantId)
    {
        super(cacheId, tenantId);
    }

    /**
     * 
     */
    private static final long serialVersionUID = -8011932788039835334L;

}
