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

/**
 * CacheMissException will be thrown if an attempt is made to read
 * content from the ContentCache when it is not in the cache.
 * 
 * @author Matt Ward
 */
public class CacheMissException extends RuntimeException
{
    private static final long serialVersionUID = -410818899455752655L; 

    /**
     * @param contentUrl URL of content that was attempted to be retrieved.
     */
    public CacheMissException(String contentUrl)
    {
        super("Content not found in cache [URL=" + contentUrl + "]");
    }
}
