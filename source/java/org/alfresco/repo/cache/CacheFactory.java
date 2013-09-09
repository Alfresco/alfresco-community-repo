/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.cache;

import java.io.Serializable;

/**
 * Cache factory interface. Implementing classes create {@link SimpleCache} objects
 * for a given cache name. It is the responsibility of the implementation to lookup
 * specific cache configuration details using the supplied name.
 *  
 * @author Matt Ward
 */
public interface CacheFactory<K extends Serializable, V>
{
    /**
     * Creates a cache. The type of cache (e.g. localised, clustered etc.) produced is
     * dependant on the factory implementation, and will vary at runtime.
     * 
     * @param cacheName
     * @return SimpleCache
     */
    SimpleCache<K, V> createCache(String cacheName);
}
