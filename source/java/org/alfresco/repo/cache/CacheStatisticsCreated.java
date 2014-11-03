/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import org.springframework.context.ApplicationEvent;

/**
 * Signal that cache statistics have been registered for the given cache.
 * 
 * @since 5.0
 * @author Matt Ward
 */
public class CacheStatisticsCreated extends ApplicationEvent
{
    private static final long serialVersionUID = 1L;
    private final CacheStatistics cacheStats;
    private final String cacheName;
    
    public CacheStatisticsCreated(CacheStatistics cacheStats, String cacheName)
    {
        super(cacheStats);
        this.cacheStats = cacheStats;
        this.cacheName = cacheName;
    }

    public CacheStatistics getCacheStats()
    {
        return this.cacheStats;
    }

    public String getCacheName()
    {
        return this.cacheName;
    }
}
