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

package org.alfresco.rest.framework.webscripts;

import org.alfresco.rest.framework.resource.content.ContentInfo;
import org.springframework.extensions.webscripts.Cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Values to be set on the response at the appropriate time.
 *
 * It should be ok to set these variables multiple times but only the latest values are used.
 *
 * @author Gethin James
 */
public class WithResponse
{
    private ContentInfo contentInfo;
    private int status;
    private Map<String, List<String>> headers;
    private Cache cache;

    public WithResponse(int status, ContentInfo contentInfo, Cache cache)
    {
        this.contentInfo = contentInfo;
        this.status = status;
        this.headers = new HashMap<>();
        this.cache = cache;
    }

    /**
     * Sets the information about the content: mimetype, encoding, locale, length
     *
     * @param contentInfo
     */
    public void setContentInfo(ContentInfo contentInfo)
    {
        this.contentInfo = contentInfo;
    }

    /**
     * Sets the Response Status
     *
     * @param status int
     */
    public void setStatus(int status)
    {
        this.status = status;
    }

    /**
     * Set a response header with the given name and value.  If the header has
     * already been set, the new value overwrites the previous one.
     *
     * @param name  header name
     * @param value  header value
     */
    public void setHeader(String name, String value)
    {
        headers.put(name, new ArrayList<String>(Arrays.asList(value)));
    }

    /**
     * Adds a response header with the given name and value.  This method
     * allows a response header to have multiple values.
     *
     * @param name  header name
     * @param value  header value
     */
    public void addHeader(String name, String value)
    {
        List<String> existing = headers.get(name);
        if (existing != null)
        {
            existing.add(value);
        }
        else
        {
            setHeader(name, value);
        }
    }

    /**
     * Sets the Cache control
     *
     * @param  cache  cache control
     */
    public void setCache(Cache cache)
    {
        this.cache = cache;
    }

    public ContentInfo getContentInfo()
    {
        return contentInfo;
    }

    public int getStatus()
    {
        return status;
    }

    public Cache getCache()
    {
        return cache;
    }

    public Map<String, List<String>> getHeaders()
    {
        return headers;
    }

}
