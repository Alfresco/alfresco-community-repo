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
package org.alfresco.repo.web.util;

/**
 * Paging cursor.  A utility for maintaining paged indexes for a collection of N items.
 * 
 * There are two types of cursor:
 * 
 * a) Paged
 * 
 * This type of cursor is driven from a page number and page size.  Random access within
 * the collection is possible by jumping straight to a page.  A simple scroll through
 * the collection is supported by iterating through each next page.  
 * 
 * b) Rows
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
 * At any time, -1 is returned to represent "out of range" i.e. for next, previous, last page
 * and next skip count.
 * 
 * Pseudo-code for traversing through a collection of N items (10 at a time):
 * 
 * PagingCursor cursor = new PagingCursor();
 * Page page = cursor.createPageCursor(N, 10, 1);
 * while (page.isInRange())
 * {
 *    for (long i = page.getStartRow(); i <= page.getEndRow(); i++)
 *    {
 *       ...collection[i]...
 *    }
 *    page = cursor.createPageCursor(N, 10, page.getNextPage());
 * }
 * 
 * Rows rows = cursor.createRowsCursor(N, 10, 0);
 * while (rows.isInRange())
 * {
 *    for (long i = page.getStartRow(); i <= page.getEndRow(); i++)
 *    {
 *       ...collection[i]...
 *    }
 *    rows = cursor.createRowsCursor(N, 10, rows.getNextSkipRows());   
 * }
 * 
 * @author davidc
 */
public class PagingCursor
{
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
     * Create a Page based Cursor
     * 
     * @param totalRows  total rows in collection
     * @param rowsPerPage  page size
     * @param page  page number (0 or 1 based)
     * @return  Page Cursor
     */
    public Page createPageCursor(long totalRows, int rowsPerPage, int page)
    {
        return new Page(totalRows, rowsPerPage, page, zeroBasedPage, zeroBasedRow);
    }
    
    /**
     * Create a Page based Cursor
     * 
     * @param totalRows  total rows in collection
     * @param rowsPerPage  page size
     * @param page  page number (0 or 1 based)
     * @param zeroBasedPage  true => 0 based, false => 1 based
     * @param zeroBasedRow  true => 0 based, false => 1 based
     * @return  Page Cursor
     */
    public Page createPageCursor(long totalRows, int rowsPerPage, int page, boolean zeroBasedPage, boolean zeroBasedRow)
    {
        return new Page(totalRows, rowsPerPage, page, zeroBasedPage, zeroBasedRow);
    }

    /**
     * Create a Rows based Cursor
     * 
     * @param totalRows  total rows in collection
     * @param maxRows  maximum number of rows in page
     * @param skipRows  number of rows to skip (0 - none)
     * @return  Rows Cursor
     */
    public Rows createRowsCursor(long totalRows, long maxRows, long skipRows)
    {
        return new Rows(totalRows, maxRows, skipRows, zeroBasedRow);
    }
    
    /**
     * Create a Rows based Cursor
     * 
     * @param totalRows  total rows in collection
     * @param maxRows  maximum number of rows in page
     * @param skipRows  number of rows to skip (0 - none)
     * @param zeroBasedRow  true => 0 based, false => 1 based
     * @return  Rows Cursor
     */
    public Rows createRowsCursor(long totalRows, long maxRows, long skipRows, boolean zeroBasedRow)
    {
        return new Rows(totalRows, maxRows, skipRows, zeroBasedRow);
    }
    

    /**
     * Page based Cursor
     */
    public static class Page
    {
        boolean zeroBasedPage;
        boolean zeroBasedRow;
        long totalRows;
        int rowsPerPage;
        long pageSize;
        int currentPage;
        int currentRow;
        
        /**
         * Create a Page based Cursor
         * 
         * @param totalRows  total rows in collection
         * @param rowsPerPage  page size
         * @param page  page number (0 or 1 based)
         * @param zeroBasedPage  true => 0 based, false => 1 based
         * @param zeroBasedRow  true => 0 based, false => 1 based
         */
        public Page(long totalRows, int rowsPerPage, int page, boolean zeroBasedPage, boolean zeroBasedRow)
        {
            this.zeroBasedPage = zeroBasedPage;
            this.zeroBasedRow = zeroBasedRow;
            this.totalRows = totalRows;
            this.rowsPerPage = rowsPerPage;
            this.pageSize = (rowsPerPage <=0) ? totalRows : rowsPerPage;
            this.currentPage = (zeroBasedPage) ? page : page - 1;
        }

        /**
         * Gets total rows
         * 
         * @return  total rows
         */
        public long getTotalRows()
        {
            return totalRows;
        }
        
        /**
         * Gets total number of pages
         * 
         * @return  total number of pages
         */
        public int getTotalPages()
        {
            if (totalRows == 0)
                return 0;
            
            int totalPages = (int)(totalRows / pageSize);
            totalPages += (totalRows % pageSize != 0) ? 1 : 0;
            return totalPages;
        }
        
        /**
         * Gets page size
         * 
         * @return  page size
         */
        public int getRowsPerPage()
        {
            return rowsPerPage;
        }
        
        /**
         * Is the cursor within range of the total number of rows
         * 
         * @return  true => within range of total rows
         */
        public boolean isInRange()
        {
            return currentPage >= 0 && getCurrentPage() <= getLastPage();
        }
        
        /**
         * Gets the current page number
         * 
         * @return  current page number
         */
        public int getCurrentPage()
        {
            return currentPage + (zeroBasedPage ? 0 : 1);
        }

        /**
         * Gets the next page number
         * 
         * @return  next page number (-1 if no more pages)
         */
        public int getNextPage()
        {
            return getCurrentPage() < getLastPage() ? getCurrentPage() + 1 : - 1;
        }
        
        /**
         * Gets the previous page number
         * 
         * @return  previous page number (-1 if no previous pages)
         */
        public int getPreviousPage()
        {
            return currentPage > 0 ? getCurrentPage() - 1 : - 1;
        }

        /**
         * Gets the first page number
         * 
         * @return  first page number
         */
        public int getFirstPage()
        {
            if (totalRows == 0)
                return -1;
            
            return zeroBasedPage ? 0 : 1;
        }

        /**
         * Gets the last page number
         * 
         * @return  last page number
         */
        public int getLastPage()
        {
            if (totalRows == 0)
                return -1;
            
            return getTotalPages() - (zeroBasedPage ? 1 : 0);
        }

        /**
         * Gets the start row within collection for this page
         * 
         * @return  start row index
         */
        public long getStartRow()
        {
            if (totalRows == 0)
                return -1;
            
            return (currentPage * pageSize) + (zeroBasedRow ? 0 : 1);
        }
        
        /**
         * Gets the end row within collection for this page
         * 
         * @return  end row index
         */
        public long getEndRow()
        {
            if (totalRows == 0)
                return -1;
            
            return getStartRow() + Math.min(pageSize, totalRows - (currentPage * pageSize)) - 1;
        }
    }

    /**
     * Rows based Cursor
     */
    public static class Rows
    {
        boolean zeroBasedRow;
        long totalRows;
        long skipRows;
        long maxRows;
        long pageSize;
        
        /**
         * Create a Rows based Cursor
         * 
         * @param totalRows  total rows in collection
         * @param maxRows  maximum number of rows in page
         * @param skipRows  number of rows to skip (0 - none)
         * @param zeroBasedRow  true => 0 based, false => 1 based
         */
        public Rows(long totalRows, long maxRows, long skipRows, boolean zeroBasedRow)
        {
            this.zeroBasedRow = zeroBasedRow;
            this.totalRows = totalRows;
            this.maxRows = maxRows;
            this.skipRows = skipRows;
            this.pageSize = (maxRows <= 0) ? totalRows - skipRows : maxRows;
        }

        /**
         * Gets the total number of rows
         * 
         * @return  total rows
         */
        public long getTotalRows()
        {
            return totalRows;
        }
        
        /**
         * Gets the number rows skipped
         * 
         * @return  skipped row count
         */
        public long getSkipRows()
        {
            return skipRows;
        }

        /**
         * Gets the maximum number of rows to include in this page
         *
         * @return  maximum of numbers
         */
        public long getMaxRows()
        {
            return maxRows;
        }

        /**
         * Is the cursor within range of the total number of rows
         * 
         * @return  true => within range of total rows
         */
        public boolean isInRange()
        {
            return skipRows >= 0 && skipRows < totalRows;
        }
        
        /**
         * Gets the start row within collection for this page
         * 
         * @return  start row index
         */
        public long getStartRow()
        {
            if (totalRows == 0)
                return -1;

            return skipRows + (zeroBasedRow ? 0 : 1);
        }
        
        /**
         * Gets the end row within collection for this page
         * 
         * @return  end row index
         */
        public long getEndRow()
        {
            if (totalRows == 0)
                return -1;
            
            return getStartRow() + Math.min(pageSize, totalRows - skipRows) - 1;
        }

        /**
         * Gets the next skip count
         * 
         * @return  next skip row
         */
        public long getNextSkipRows()
        {
            return (skipRows + pageSize < totalRows) ? skipRows + pageSize : -1;
        }
    }
    
}
