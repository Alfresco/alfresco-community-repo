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
package org.alfresco.util.cache;

/**
 * A generic event with the cache id and affected tenant
 * 
 * @author Andy
 */
public abstract class AbstractRefreshableCacheEvent implements RefreshableCacheEvent
{
    private static final long serialVersionUID = 1324638640132648062L;

    private String cacheId;
    private String key;

    AbstractRefreshableCacheEvent(String cacheId, String key)
    {
        this.cacheId = cacheId;
        this.key = key;
    }

    @Override
    public String getCacheId()
    {
        return cacheId;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    @Override
    public String toString()
    {
        return "AbstractRefreshableCacheEvent [cacheId=" + cacheId + ", tenantId=" + key + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cacheId == null) ? 0 : cacheId.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AbstractRefreshableCacheEvent other = (AbstractRefreshableCacheEvent) obj;
        if (cacheId == null)
        {
            if (other.cacheId != null) return false;
        }
        else if (!cacheId.equals(other.cacheId)) return false;
        if (key == null)
        {
            if (other.key != null) return false;
        }
        else if (!key.equals(other.key)) return false;
        return true;
    }
}
