/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.rest.framework.resource.content;

import java.util.Date;

/**
 * An immutable builder for setting the HTTP cache.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class CacheDirective
{
    private final boolean neverCache;
    private final boolean isPublic;
    private final boolean mustRevalidate;
    private final Date lastModified;
    private final String eTag;
    private final Long maxAge;

    private CacheDirective(Builder builder)
    {
        this.neverCache = builder.neverCache;
        this.isPublic = builder.isPublic;
        this.mustRevalidate = builder.mustRevalidate;
        this.lastModified = builder.lastModified == null ? null : new Date(builder.lastModified.getTime());
        this.eTag = builder.eTag;
        this.maxAge = builder.maxAge;
    }

    public boolean isNeverCache()
    {
        return neverCache;
    }

    public boolean isPublic()
    {
        return isPublic;
    }

    public boolean isMustRevalidate()
    {
        return mustRevalidate;
    }

    public Date getLastModified()
    {
        if (lastModified != null)
        {
            return new Date(lastModified.getTime());
        }
        return null;
    }

    public String getETag()
    {
        return eTag;
    }

    public Long getMaxAge()
    {
        return maxAge;
    }

    public static class Builder
    {
        // The default values are the same as the org.springframework.extensions.webscripts.Cache
        private boolean neverCache = true;
        private boolean isPublic = false;
        private boolean mustRevalidate = true;
        private Date lastModified = null;
        private String eTag = null;
        private Long maxAge = null;

        public Builder setNeverCache(boolean neverCache)
        {
            this.neverCache = neverCache;
            return this;
        }

        public Builder setPublic(boolean aPublic)
        {
            isPublic = aPublic;
            return this;
        }

        public Builder setMustRevalidate(boolean mustRevalidate)
        {
            this.mustRevalidate = mustRevalidate;
            return this;
        }

        public Builder setLastModified(Date lastModified)
        {
            this.lastModified = lastModified;
            return this;
        }

        public Builder setETag(String eTag)
        {
            this.eTag = eTag;
            return this;
        }

        public Builder setMaxAge(Long maxAge)
        {
            this.maxAge = maxAge;
            return this;
        }

        public CacheDirective build()
        {
            return new CacheDirective(this);
        }
    }
}
