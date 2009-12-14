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
package org.alfresco.cmis;

import java.util.Locale;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.search.impl.querymodel.QueryOptions;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * The options for a CMIS query
 * 
 * @author andyh
 */
public class CMISQueryOptions extends QueryOptions
{
    public enum CMISQueryMode
    {
        CMS_STRICT, CMS_WITH_ALFRESCO_EXTENSIONS ;
    }
    
   
    private CMISQueryMode queryMode = CMISQueryMode.CMS_STRICT;

    /**
     * Create a CMISQueryOptions instance with the default options other than the query and store ref.
     * The query will be run using the locale returned by I18NUtil.getLocale()
     * 
     * @param query - the query to run
     * @param storeRef - the store against which to run the query
     */
    public CMISQueryOptions(String query, StoreRef storeRef)
    {
        this(query, storeRef, I18NUtil.getLocale());
    }
    
    /**
     * Create a CMISQueryOptions instance with the default options other than the query, store ref and locale.
     * 
     * @param query - the query to run
     * @param storeRef - the store against which to run the query
     */
    public CMISQueryOptions(String query, StoreRef storeRef, Locale locale)
    {
        super(query, storeRef, locale);
    }

    

    
    /**
     * Get the query mode.
     * 
     * @return the queryMode
     */
    public CMISQueryMode getQueryMode()
    {
        return queryMode;
    }

    /**
     * Set the query mode.
     * 
     * @param queryMode the queryMode to set
     */
    public void setQueryMode(CMISQueryMode queryMode)
    {
        this.queryMode = queryMode;
    }
}


