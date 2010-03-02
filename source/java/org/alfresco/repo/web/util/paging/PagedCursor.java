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

import java.io.Serializable;

import org.alfresco.repo.web.util.paging.Paging.PageType;


/**
 * Implementation of cursor based on notion of a Page.
 *  
 * @author davidc
 */
public class PagedCursor implements Cursor, Serializable
{
    private static final long serialVersionUID = -1041155610387669590L;
    
    private boolean zeroBasedPage;
    private boolean zeroBasedRow;
    private int totalRows;
    private int pageSize;
    private int rowsPerPage;
    private int page;
    
    
    /**
     * Construct
     * 
     * @param zeroBasedRow   true => row index starts at zero
     * @param totalRows  total number of rows (-1 for don't know)
     * @param zeroBasedPage  true => page number starts at zero
     * @param page  page number
     * @param pageSize  page size
     */
    /*package*/ PagedCursor(boolean zeroBasedRow, int totalRows, boolean zeroBasedPage, int page, int pageSize)
    {
        this.zeroBasedRow = zeroBasedRow;
        this.totalRows = totalRows;
        this.zeroBasedPage = zeroBasedPage;
        this.page = (zeroBasedPage) ? page : page - 1;
        this.pageSize = pageSize;
        this.rowsPerPage = (pageSize <=0) ? totalRows : pageSize;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#getPageType()
     */
    public String getPageType()
    {
        return PageType.PAGE.toString();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#getPageSize()
     */
    public int getPageSize()
    {
        return pageSize;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#getTotalPages()
     */
    public int getTotalPages()
    {
        if (totalRows <= 0)
            return 0;
        
        int totalPages = (int)(totalRows / rowsPerPage);
        totalPages += (totalRows % rowsPerPage != 0) ? 1 : 0;
        return totalPages;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#getTotalRows()
     */
    public int getTotalRows()
    {
        return totalRows;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#getCurrentPage()
     */
    public int getCurrentPage()
    {
        return page + (zeroBasedPage ? 0 : 1);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#getFirstPage()
     */
    public int getFirstPage()
    {
        if (totalRows <= 0)
            return -1;
        
        return zeroBasedPage ? 0 : 1;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#getLastPage()
     */
    public int getLastPage()
    {
        if (totalRows <= 0)
            return -1;
        
        return getTotalPages() - (zeroBasedPage ? 1 : 0);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#getNextPage()
     */
    public int getNextPage()
    {
        return getCurrentPage() < getLastPage() ? getCurrentPage() + 1 : - 1;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#getPrevPage()
     */
    public int getPrevPage()
    {
        return page > 0 ? getCurrentPage() - 1 : - 1;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#isInRange()
     */
    public boolean isInRange()
    {
        return page >= 0 && getCurrentPage() <= getLastPage();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#hasFirstPage()
     */
    public boolean getHasFirstPage()
    {
        return getFirstPage() != -1;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#hasLastPage()
     */
    public boolean getHasLastPage()
    {
        return getLastPage() != -1;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#hasNextPage()
     */
    public boolean getHasNextPage()
    {
        return getNextPage() != -1;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#hasPrevPage()
     */
    public boolean getHasPrevPage()
    {
        return getPrevPage() != -1;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#getStartRow()
     */
    public int getStartRow()
    {
        if (totalRows <= 0)
            return 0;
        
        return (page * rowsPerPage) + (zeroBasedRow ? 0 : 1);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#getEndRow()
     */
    public int getEndRow()
    {
        if (totalRows <= 0)
            return -1;
        
        return getStartRow() + Math.min(rowsPerPage, totalRows - (page * rowsPerPage)) - 1;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#getRowCount()
     */
    public int getRowCount()
    {
        if (totalRows <= 0)
            return 0;
        
        return getEndRow() - getStartRow() + 1;
    }

}
