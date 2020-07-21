package org.alfresco.util.cache;

public interface AsynchronouslyRefreshedCache <T> extends RefreshableCache <T>
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
     * @param key tennant id
     * @return          <tt>true</tt> if the cache is not currently refreshing itself
     */
    boolean isUpToDate(String key);

}
