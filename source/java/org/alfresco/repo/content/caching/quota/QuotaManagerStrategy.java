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
