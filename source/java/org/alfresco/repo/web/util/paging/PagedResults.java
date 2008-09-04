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
package org.alfresco.repo.web.util.paging;

import java.io.Serializable;


/**
 * A Paged Result Set
 * 
 * @author davidc
 */
public class PagedResults implements Serializable
{
    private static final long serialVersionUID = 5905699888354619269L;
    
    private Object result;
    private Object[] results;
    private Cursor cursor;
    

    /**
     * Construct
     *  
     * @param results  results for the page within cursor
     * @param cursor  the cursor
     */
    /*Package*/ PagedResults(Object[] results, Cursor cursor)
    {
        this.result = results;
        this.results = results;
        this.cursor = cursor;
    }

    /**
     * Construct
     *  
     * @param results  results for the page within cursor
     * @param cursor  the cursor
     */
    /*Package*/ PagedResults(Object result, Cursor cursor)
    {
        this.result = result;
        this.results = null;
        this.cursor = cursor;
    }

    /**
     * Get Results
     * 
     * @return  results
     */
    public Object[] getResults()
    {
        if (results == null)
        {
            if (result != null)
            {
                results = new Object[] {result};
            }
        }
        return results;
    }

    /**
     * Get Result
     * 
     * @return  result
     */
    public Object getResult()
    {
        return result;
    }

    /**
     * Get Cursor
     * 
     * @return  cursor
     */
    public Cursor getCursor()
    {
        return cursor;
    }
    
}
