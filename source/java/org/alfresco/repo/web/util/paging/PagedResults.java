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
