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

/**
 * A generic event with the cache id and affected tenant
 * 
 * @author Andy
 */
public abstract class AbstractRefreshableCacheEvent implements RefreshableCacheEvent
{
    /**
     * 
     */
    private static final long serialVersionUID = 1324638640132648062L;

    private String cacheId;

    private String tenantId;

    AbstractRefreshableCacheEvent(String cacheId, String tenantId)
    {
        this.cacheId = cacheId;
        this.tenantId = tenantId;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.cache.RefreshableCacheEvent#getCacheId()
     */
    @Override
    public String getCacheId()
    {
        return cacheId;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.cache.RefreshableCacheEvent#getTenantId()
     */
    @Override
    public String getTenantId()
    {
        return tenantId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "AbstractRefreshableCacheEvent [cacheId=" + cacheId + ", tenantId=" + tenantId + "]";
    }

    
}
