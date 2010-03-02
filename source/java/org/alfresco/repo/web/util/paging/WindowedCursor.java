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
 * Cursor implementation based on notion of a Window.
 * 
 * @author davidc
 */
public class WindowedCursor implements Cursor, Serializable
{
    private static final long serialVersionUID = 521131539938276413L;

    private boolean zeroBasedRow;
    private int totalRows;
    private int skipRows;
    private int maxRows;
    private int rowsPerPage;
    
    /**
     * Construct
     * 
     * @param zeroBasedRow  true => 0 based, false => 1 based
     * @param totalRows  total rows in collection
     * @param skipRows  number of rows to skip (0 - none)
     * @param maxRows  maximum number of rows in window
     */
    WindowedCursor(boolean zeroBasedRow, int totalRows, int skipRows, int maxRows)
    {
        this.zeroBasedRow = zeroBasedRow;
        this.totalRows = totalRows;
        this.skipRows = skipRows;
        this.maxRows = maxRows;
        this.rowsPerPage = (maxRows <= 0) ? totalRows - skipRows : maxRows;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#getPageType()
     */
    public String getPageType()
    {
        return PageType.WINDOW.toString();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#getPageSize()
     */
    public int getPageSize()
    {
        return maxRows;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#getTotalPages()
     */
    public int getTotalPages()
    {
        return -1;
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
        return skipRows;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#getFirstPage()
     */
    public int getFirstPage()
    {
        if (totalRows <=0)
            return -1;
        
        return zeroBasedRow ? 0 : 1;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#getLastPage()
     */
    public int getLastPage()
    {
        return -1;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#getNextPage()
     */
    public int getNextPage()
    {
        return (skipRows + rowsPerPage < totalRows) ? skipRows + maxRows : -1;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#getPrevPage()
     */
    public int getPrevPage()
    {
        return (skipRows > 0) ? Math.max(0, skipRows - maxRows) : -1;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#isInRange()
     */
    public boolean isInRange()
    {
        return skipRows >= 0 && skipRows < totalRows;
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

        return skipRows + (zeroBasedRow ? 0 : 1);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.util.paging.Cursor#getEndRow()
     */
    public int getEndRow()
    {
        if (totalRows <= 0)
            return -1;
        
        return getStartRow() + Math.min(rowsPerPage, totalRows - skipRows) - 1;
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
