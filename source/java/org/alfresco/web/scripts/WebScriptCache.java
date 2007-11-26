/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.scripts;

import java.util.Date;


/**
 * Web Script Cache
 *
 * Records the desired cache requirements for the Web Script
 * 
 * @author davidc
 */
public class WebScriptCache implements WebScriptDescription.RequiredCache
{
    private boolean neverCache = true;
    private boolean isPublic = false;
    private boolean mustRevalidate = true;
    private Date lastModified = null;
    private String eTag = null;
    private Long maxAge = null;

    
    /**
     * Construct
     */
    public WebScriptCache()
    {
    }
    
    /**
     * Construct
     * 
     * @param requiredCache
     */
    public WebScriptCache(WebScriptDescription.RequiredCache requiredCache)
    {
        neverCache = requiredCache.getNeverCache();
        isPublic = requiredCache.getIsPublic();
        mustRevalidate = requiredCache.getMustRevalidate();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptDescription.RequiredCache#getNeverCache()
     */
    public boolean getNeverCache()
    {
        return neverCache;
    }
    
    /**
     * @param neverCache
     */
    public void setNeverCache(boolean neverCache)
    {
        this.neverCache = neverCache;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptDescription.RequiredCache#getIsPublic()
     */
    public boolean getIsPublic()
    {
        return isPublic;
    }
    
    /**
     * @param isPublic
     */
    public void setIsPublic(boolean isPublic)
    {
        this.isPublic = isPublic;
    }

    /**
     * @return last modified
     */
    public Date getLastModified()
    {
        return lastModified;
    }
    
    /**
     * @param lastModified
     */
    public void setLastModified(Date lastModified)
    {
        this.lastModified = lastModified;
    }
    
    /**
     * @return  ETag
     */
    public String getETag()
    {
        return eTag;
    }
    
    /**
     * @param tag  ETag
     */
    public void setETag(String tag)
    {
        eTag = tag;
    }
    
    /**
     * @return  Max Age (seconds)
     */
    public Long getMaxAge()
    {
        return maxAge;
    }
    
    /**
     * @param maxAge  Max Age (seconds)
     */
    public void setMaxAge(Long maxAge)
    {
        this.maxAge = maxAge;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptDescription.RequiredCache#getMustRevalidate()
     */
    public boolean getMustRevalidate()
    {
        return mustRevalidate;
    }
    
    /**
     * @param mustRevalidate
     */
    public void setMustRevalidate(boolean mustRevalidate)
    {
        this.mustRevalidate = mustRevalidate;
    }
    
}
