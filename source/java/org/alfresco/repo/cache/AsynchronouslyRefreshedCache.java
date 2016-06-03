package org.alfresco.repo.cache;

/**
 * Implementation details in addition to the exposed interface.
 * 
 * @author Andy
 * @since 4.1.3
 */
public interface AsynchronouslyRefreshedCache<T> extends RefreshableCache<T>
{
    /**
     * Get the cache id
     * 
     * @return          the cache ID
     */
    String getCacheId();
   
    /**
     * Determine if the cache is up to date
     * 
     * @return          <tt>true</tt> if the cache is not currently refreshing itself
     */
    boolean isUpToDate();
}
