/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
     * @param contentUrl
     * @return true if the URL exists in the in-memory cache. It <em>may</em> therefore be cached on disk.
     */
    boolean contains(String contentUrl);

    /**
     * Retrieve a ContentReader for the cached content specified by URL.
     * 
     * @param contentUrl
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
     * @param contentUrl
     * @param reader
     * @return true if the content was cached, false otherwise.
     */
    boolean put(String contentUrl, ContentReader reader);

    /**
     * Remove a cached item from the in-memory lookup table. Implementation should not remove
     * the actual cached content (file) - this should be left to the clean-up process or can
     * be deleted with {@link #deleteFile(String)}.
     * 
     * @param contentUrl
     */
    void remove(String contentUrl);

    /**
     * Deletes the cached content file for the specified URL. To remove the item from the
     * lookup table also, use {@link #remove(String)} after calling this method.
     * 
     * @param url
     */
    void deleteFile(String url);

    /**
     * Retrieve a ContentWriter to write content to a cache file. Upon closing the stream
     * a listener will add the new content file to the in-memory lookup table.
     * 
     * @param context
     * @return ContentWriter
     */
    ContentWriter getWriter(String url);
}
