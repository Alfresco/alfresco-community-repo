package org.alfresco.repo.content.caching;

import java.io.File;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;

/**
 * A cache designed to operate on content and split between memory and disk.
 * The binary content data itself is stored on disk but the references to
 * those files are stored in memory.
 * 
 * @author Matt Ward
 */
public interface ContentCache
{
    /**
     * Returns the location where cache files will be written (cacheRoot) - implementation
     * dependant and may be null.
     * 
     * @return cacheRoot
     */
    public File getCacheRoot();
    
    /**
     * Check to see if the content - specified by URL - exists in the cache.
     * <p>
     * Note that just because the in-memory cache has a record of the content item having been placed
     * into the cache, it does not mean that the disk item is guaranteed to be there. The temp file
     * clean-up process, for example, may have removed it.
     * <p>
     * @param contentUrl String
     * @return true if the URL exists in the in-memory cache. It <em>may</em> therefore be cached on disk.
     */
    boolean contains(String contentUrl);

    /**
     * Retrieve a ContentReader for the cached content specified by URL.
     * 
     * @param contentUrl String
     * @return ContentReader
     * @throws org.alfresco.repo.content.caching.CacheMissException
     *         If the cache does not contain the specified content.
     */
    ContentReader getReader(String contentUrl);
    
    /**
     * Put an item into cache - this will populate both a disk file (with content) and
     * the in-memory lookup table (with the URL and cache file location).
     * 
     * Empty content will NOT be cached - in which case false is returned.
     * 
     * @param contentUrl String
     * @param reader ContentReader
     * @return true if the content was cached, false otherwise.
     */
    boolean put(String contentUrl, ContentReader reader);

    /**
     * Remove a cached item from the in-memory lookup table. Implementation should not remove
     * the actual cached content (file) - this should be left to the clean-up process or can
     * be deleted with {@link #deleteFile(String)}.
     * 
     * @param contentUrl String
     */
    void remove(String contentUrl);

    /**
     * Deletes the cached content file for the specified URL. To remove the item from the
     * lookup table also, use {@link #remove(String)} after calling this method.
     * 
     * @param url String
     */
    void deleteFile(String url);

    /**
     * Retrieve a ContentWriter to write content to a cache file. Upon closing the stream
     * a listener will add the new content file to the in-memory lookup table.
     * 
     * @param url url
     * @return ContentWriter
     */
    ContentWriter getWriter(String url);
}
