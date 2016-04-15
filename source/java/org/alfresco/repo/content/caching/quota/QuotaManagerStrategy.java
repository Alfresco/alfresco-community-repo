package org.alfresco.repo.content.caching.quota;


/**
 * Disk quota managers for the CachingContentStore must implement this interface.
 * 
 * @author Matt Ward
 */
public interface QuotaManagerStrategy
{
    /**
     * Called immediately before writing a cache file or (when cacheOnInBound is set to true
     * for the CachingContentStore) before handing a ContentWriter to a content producer.
     * <p>
     * In the latter case, the contentSize will be unknown (0), since the content
     * length hasn't been established yet.
     * 
     * @param contentSize The size of the content that will be written or 0 if not known.
     * @return true to allow the cache file to be written, false to veto.
     */
    boolean beforeWritingCacheFile(long contentSize);
    
    
    /**
     * Called immediately after writing a cache file - specifying the size of the file that was written.
     * The return value allows implementations control over whether the new cache file is kept (true) or
     * immediately removed (false).
     * 
     * @param contentSize The size of the content that was written.
     * @return true to allow the cache file to remain, false to immediately delete.
     */
    boolean afterWritingCacheFile(long contentSize);
}
