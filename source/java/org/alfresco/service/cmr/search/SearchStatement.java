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
package org.alfresco.service.cmr.search;

/**
 * A search string and language.
 * 
 * @author Andy Hind
 */
public class SearchStatement
{

    private String language;
    private String query;

    SearchStatement()
    {
        super();
    }
    
    /**
     * A constructor that takes both arguments.
     * 
     * @param language
     * @param query
     */
    SearchStatement(String language, String query)
    {
        this.language = language;
        this.query = query;
    }

    /**
     * Get the query language. 
     * 
     * @return
     */
    public String getLanguage()
    {
        return language;
    }

    /**
     * Get the query.
     * 
     * @return
     */
    public String getQuery()
    {
        return query;
    }

    /**
     * Set the query language.
     * 
     * @param language - the query language.
     */
    public void setLanguage(String language)
    {
        this.language = language;
    }

    /**
     * Set the query string.
     * 
     * @param query - the query string.
     */
    public void setQuery(String query)
    {
        this.query = query;
    }
    
}
