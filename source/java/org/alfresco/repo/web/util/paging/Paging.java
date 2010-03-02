/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.web.util.paging;

import java.util.Map;

/**
 * Paging.  A utility for maintaining paged indexes for a collection of N items.
 * 
 * There are two types of cursor:
 * 
 * a) Paged
 * 
 * This type of cursor is driven from a page number and page size.  Random access within
 * the collection is possible by jumping straight to a page.  A simple scroll through
 * the collection is supported by iterating through each next page.  
 * 
 * b) Windowed
 * 
 * This type of cursor is driven from a skip row count and maximum number of rows.  Random
 * access is not supported.  The collection of items is simply scrolled through from
 * start to end by iterating through each next set of rows.
 * 
 * In either case, a paging cursor provides a start row and end row which may be used
 * to extract the items for the page from the collection of N items.
 * 
 * A zero (or less) page size or row maximum means "unlimited". 
 * 
 * Zero or one based Page and Rows indexes are supported.  By default, Pages are 1 based and
 * Rows are 0 based.
 *
 * At any time, -1 is returned to represent "out of range" i.e. for next, previous, last page.
 * 
 * Pseudo-code for traversing through a collection of N items (10 at a time):
 * 
 * Paging paging = new Paging();
 * Cursor page = paging.createCursor(N, paging.createPage(1, 10));
 * while (page.isInRange())
 * {
 *    for (long i = page.getStartRow(); i <= page.getEndRow(); i++)
 *    {
 *       ...collection[i]...
 *    }
 *    page = paging.createCursor(N, paging.createPage(page.getNextPage(), page.getPageSize());
 * }
 * 
 * Cursor window = paging.createCursor(N, paging.createWindow(0, 10));
 * while (window.isInRange())
 * {
 *    for (long i = window.getStartRow(); i <= window.getEndRow(); i++)
 *    {
 *       ...collection[i]...
 *    }
 *    window = paging.createCursor(N, paging.createWindow(window.getNextPage(), window.getPageSize());   
 * }
 * 
 * @author davidc
 */
public class Paging
{
    public enum PageType
    {
        PAGE,
        WINDOW
    };
    
    boolean zeroBasedPage = false;
    boolean zeroBasedRow = true;

    /**
     * Sets zero based page index
     * 
     * Note: scoped to this paging cursor instance
     * 
     * @param zeroBasedPage  true => 0 based, false => 1 based
     */
    public void setZeroBasedPage(boolean zeroBasedPage)
    {
        this.zeroBasedPage = zeroBasedPage;
    }

    /**
     * Is zero based page index?
     * 
     * Note: scoped to this paging cursor instance
     *
     * @return true => 0 based, false => 1 based
     */
    public boolean isZeroBasedPage()
    {
        return zeroBasedPage;
    }
    
    /**
     * Sets zero based row index
     * 
     * Note: scoped to this paging cursor instance
     * 
     * @param zeroBasedRow  true => 0 based, false => 1 based
     */
    public void setZeroBasedRow(boolean zeroBasedRow)
    {
        this.zeroBasedRow = zeroBasedRow;
    }

    /**
     * Is zero based row index?
     * 
     * Note: scoped to this paging cursor instance
     *
     * @return true => 0 based, false => 1 based
     */
    public boolean isZeroBasedRow()
    {
        return zeroBasedRow;
    }

    /**
     * Create a Page or Window from standardised request arguments
     *
     * For Paged based index (take precedence over window based index, if both are specified):
     * 
     * - request args
     *     pageNo  => page number index 
     *     pageSize  => size of page
     * 
     * For Window based index (as defined by CMIS):
     * 
     * - request args  (take precedence over header values if both are specified)
     *     skipCount  => row number start index
     *     maxItems  => size of page
     * 
     * @param args  request args
     * @return  page (if pageNumber driven) or window (if skipCount driven)
     */
    public Page createPageOrWindow(Map<String, String> args)
    {
        // page number
        Integer pageNo = null;
        String strPageNo = args.get("pageNo");
        if (strPageNo != null)
        {
            try
            {
                pageNo = new Integer(strPageNo);
            }
            catch(NumberFormatException e) {};
        }
        
        // page size
        Integer pageSize = null;
        String strPageSize = args.get("pageSize");
        if (strPageSize != null)
        {
            try
            {
                pageSize = new Integer(strPageSize);
            }
            catch(NumberFormatException e) {};
        }
        
        // skip count
        Integer skipCount = null;
        String strSkipCount = args.get("skipCount");
        if (strSkipCount != null)
        {
            try
            {
                skipCount = new Integer(strSkipCount);
            }
            catch(NumberFormatException e) {};
        }

        // max items
        Integer maxItems = null;
        String strMaxItems = args.get("maxItems");
        if (strMaxItems != null)
        {
            try
            {
                maxItems = new Integer(strMaxItems);
            }
            catch(NumberFormatException e) {};
        }

        return createPageOrWindow(pageNo, pageSize, skipCount, maxItems);
    }
    
    /**
     * Create a Page or Window
     * 
     * @param pageNumber  page number (optional and paired with pageSize)
     * @param pageSize   page size (optional and paired with pageNumber)
     * @param skipCount  skipCount (optional and paired with maxItems)
     * @param maxItems  maxItems (optional and paired with skipCount)
     * @return  page (if pageNumber driven) or window (if skipCount driven)
     */
    public Page createPageOrWindow(Integer pageNumber, Integer pageSize, Integer skipCount, Integer maxItems)
    {
        if (pageNumber != null || pageSize != null)
        {
            return createPage(pageNumber == null ? isZeroBasedPage() ? 0 : 1 : pageNumber, pageSize == null ? -1 : pageSize);
        }
        else if (skipCount != null || maxItems != null)
        {
            return createWindow(skipCount == null ? isZeroBasedRow() ? 0 : 1 : skipCount, maxItems == null ? -1 : maxItems);
        }
        return createUnlimitedPage();
    }
    
    /**
     * Create a Page
     * 
     * @param pageNumber  page number
     * @param pageSize  page size
     * @return  the page
     */
    public Page createPage(int pageNumber, int pageSize)
    {
        return new Page(PageType.PAGE, zeroBasedPage, pageNumber, pageSize);
    }
    
    /**
     * Create an unlimited Page
     * 
     * @return  page (single Page starting at first page of unlimited page size)
     */
    public Page createUnlimitedPage()
    {
        return new Page(PageType.PAGE, zeroBasedPage, zeroBasedPage ? 0 : 1, -1);
    }
    
    /**
     * Create a Window
     * @param skipRows  number of rows to skip
     * @param maxRows  maximum number of rows in window
     * @return  the window
     */
    public Page createWindow(int skipRows, int maxRows)
    {
        return new Page(PageType.WINDOW, zeroBasedRow, skipRows, maxRows);
    }
    
    /**
     * Create a Cursor
     * 
     * @param totalRows  total number of rows in cursor (< 0 for don't know)
     * @param page  the page / window within cursor
     * @return  the cursor
     */
    public Cursor createCursor(int totalRows, Page page)
    {
        if (page.getType() == PageType.PAGE)
        {
            return new PagedCursor(zeroBasedRow, totalRows, page.zeroBasedIdx, page.startIdx, page.pageSize);
        }
        else if (page.getType() == PageType.WINDOW)
        {
            return new WindowedCursor(zeroBasedRow, totalRows, page.startIdx, page.pageSize);
        }
        return null;
    }
    
    /**
     * Create a Paged Result Set
     * 
     * @param results  the results for the page within the cursor
     * @param cursor  the cursor
     * @return  the paged result set
     */
    public PagedResults createPagedResults(Object[] results, Cursor cursor)
    {
        return new PagedResults(results, cursor);
    }

    /**
     * Create a Paged Result Set
     * 
     * @param results  the results for the page within the cursor
     * @param cursor  the cursor
     * @return  the paged result set
     */
    public PagedResults createPagedResult(Object result, Cursor cursor)
    {
        return new PagedResults(result, cursor);
    }

}
