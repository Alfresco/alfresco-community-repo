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
     * @param result  results for the page within cursor
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
