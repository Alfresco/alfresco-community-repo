/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
import java.util.Properties;

/**
 * Abstract {@link CacheFactory} implementation containing common functionality
 * such as cache configuration properties support.
 * 
 * @author Matt Ward
 */
public abstract class AbstractCacheFactory<K extends Serializable, V> implements CacheFactory<K, V> 
{
    private static final String PROP_SEPERATOR = ".";
    private Properties properties;
    
    public String getProperty(String cacheName, String propName, final String defaultValue)
    {
        final String fqPropName = cacheName + PROP_SEPERATOR + propName;
        String value = properties.getProperty(fqPropName);
        if (value != null)
        {
            value = value.trim();
        }
        if (value == null || value.isEmpty())
        {
            value = defaultValue;
        }
        return value;
    }
    
    /**
     * Provide properties to parameterize cache creation. Cache properties are prefixed
     * with the cacheName supplied when invoking {@link DefaultCacheFactory#createCache(String)}.
     * For example, for a cache named cache.ticketsCache the property cache.ticketsCache.maxItems
     * will determine the capacity of the cache.
     * 
     * @param properties
     */
    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }
}
