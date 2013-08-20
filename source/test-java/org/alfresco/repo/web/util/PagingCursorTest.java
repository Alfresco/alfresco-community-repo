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

import junit.framework.TestCase;

import org.alfresco.repo.web.util.PagingCursor.Page;
import org.alfresco.repo.web.util.PagingCursor.Rows;


/**
 * Test Paged and Row Based Cursors
 * 
 * @author davidc
 */
public class PagingCursorTest extends TestCase
{
    protected PagingCursor pageCursor;
    
    @Override
    protected void setUp() throws Exception
    {
        pageCursor = new PagingCursor();
    }

    public void testZeroBasedBooleans()
    {
        assertFalse(pageCursor.isZeroBasedPage());
        pageCursor.setZeroBasedPage(true);
        assertTrue(pageCursor.isZeroBasedPage());
        assertTrue(pageCursor.isZeroBasedRow());
        pageCursor.setZeroBasedRow(false);
        assertFalse(pageCursor.isZeroBasedRow());
    }
    
    public void testZeroRowsPageCursor()
    {
        Page page = pageCursor.createPageCursor(0, 10, 1);
        assertNotNull(page);
        assertEquals(0, page.getTotalRows());
        assertEquals(0, page.getTotalPages());
        assertEquals(10, page.getRowsPerPage());
        assertFalse(page.isInRange());
        assertEquals(1, page.getCurrentPage());
        assertEquals(-1, page.getFirstPage());
        assertEquals(-1, page.getLastPage());
        assertEquals(-1, page.getPreviousPage());
        assertEquals(-1, page.getNextPage());
        assertEquals(-1, page.getStartRow());
        assertEquals(-1, page.getEndRow());
    }

    public void testOutOfBoundsPageCursor()
    {
        Page page1 = pageCursor.createPageCursor(1, 1, 1);
        assertNotNull(page1);
        assertTrue(page1.isInRange());
        Page page2 = pageCursor.createPageCursor(1, 1, 2);
        assertNotNull(page2);
        assertFalse(page2.isInRange());
        Page page3 = pageCursor.createPageCursor(1, 2, 1);
        assertNotNull(page3);
        assertTrue(page3.isInRange());
        Page page4 = pageCursor.createPageCursor(10, 2, 5);
        assertNotNull(page4);
        assertTrue(page4.isInRange());
        Page page5 = pageCursor.createPageCursor(10, 2, 6);
        assertNotNull(page5);
        assertFalse(page5.isInRange());
        Page page6 = pageCursor.createPageCursor(11, 2, 5);
        assertNotNull(page6);
        assertTrue(page6.isInRange());
        Page page7 = pageCursor.createPageCursor(11, 2, 6);
        assertNotNull(page7);
        assertTrue(page7.isInRange());
        Page page8 = pageCursor.createPageCursor(11, 2, 0);
        assertNotNull(page8);
        assertFalse(page8.isInRange());

        pageCursor.setZeroBasedPage(true);
        Page page10 = pageCursor.createPageCursor(1, 1, 0);
        assertNotNull(page10);
        assertTrue(page10.isInRange());
        Page page11 = pageCursor.createPageCursor(1, 1, 1);
        assertNotNull(page11);
        assertFalse(page11.isInRange());
        Page page12 = pageCursor.createPageCursor(1, 2, 0);
        assertNotNull(page12);
        assertTrue(page12.isInRange());
        Page page13 = pageCursor.createPageCursor(10, 2, 4);
        assertNotNull(page13);
        assertTrue(page13.isInRange());
        Page page14 = pageCursor.createPageCursor(10, 2, 5);
        assertNotNull(page14);
        assertFalse(page14.isInRange());
        Page page15 = pageCursor.createPageCursor(11, 2, 4);
        assertNotNull(page15);
        assertTrue(page15.isInRange());
        Page page16 = pageCursor.createPageCursor(11, 2, 5);
        assertNotNull(page16);
        assertTrue(page16.isInRange());
        Page page17 = pageCursor.createPageCursor(11, 2, -1);
        assertNotNull(page17);
        assertFalse(page17.isInRange());
    }

    public void testTotalPageCursor()
    {
        Page page1 = pageCursor.createPageCursor(10, 1, 1);
        assertEquals(10, page1.getTotalRows());
        assertEquals(10, page1.getTotalPages());
        Page page2 = pageCursor.createPageCursor(10, 10, 1);
        assertEquals(10, page2.getTotalRows());
        assertEquals(1, page2.getTotalPages());
        Page page3 = pageCursor.createPageCursor(9, 10, 1);
        assertEquals(9, page3.getTotalRows());
        assertEquals(1, page3.getTotalPages());
        Page page4 = pageCursor.createPageCursor(11, 10, 1);
        assertEquals(11, page4.getTotalRows());
        assertEquals(2, page4.getTotalPages());
        Page page5 = pageCursor.createPageCursor(20, 10, 1);
        assertEquals(20, page5.getTotalRows());
        assertEquals(2, page5.getTotalPages());
    }

    public void testPagingPageCursor()
    {
        Page page1 = pageCursor.createPageCursor(10, 1, 1);
        assertEquals(1, page1.getCurrentPage());
        assertEquals(1, page1.getFirstPage());
        assertEquals(10, page1.getLastPage());
        assertEquals(-1, page1.getPreviousPage());
        assertEquals(2, page1.getNextPage());
        assertEquals(0, page1.getStartRow());
        assertEquals(0, page1.getEndRow());
        Page page2 = pageCursor.createPageCursor(10, 1, 2);
        assertEquals(2, page2.getCurrentPage());
        assertEquals(1, page2.getFirstPage());
        assertEquals(10, page2.getLastPage());
        assertEquals(1, page2.getPreviousPage());
        assertEquals(3, page2.getNextPage());
        assertEquals(1, page2.getStartRow());
        assertEquals(1, page2.getEndRow());
        Page page3 = pageCursor.createPageCursor(10, 10, 1);
        assertEquals(1, page3.getCurrentPage());
        assertEquals(1, page3.getFirstPage());
        assertEquals(1, page3.getLastPage());
        assertEquals(-1, page3.getPreviousPage());
        assertEquals(-1, page3.getNextPage());
        assertEquals(0, page3.getStartRow());
        assertEquals(9, page3.getEndRow());
        Page page4 = pageCursor.createPageCursor(9, 10, 1);
        assertEquals(1, page4.getCurrentPage());
        assertEquals(1, page4.getFirstPage());
        assertEquals(1, page4.getLastPage());
        assertEquals(-1, page4.getPreviousPage());
        assertEquals(-1, page4.getNextPage());
        assertEquals(0, page4.getStartRow());
        assertEquals(8, page4.getEndRow());
        Page page5 = pageCursor.createPageCursor(11, 10, 1);
        assertEquals(1, page5.getCurrentPage());
        assertEquals(1, page5.getFirstPage());
        assertEquals(2, page5.getLastPage());
        assertEquals(-1, page5.getPreviousPage());
        assertEquals(2, page5.getNextPage());
        assertEquals(0, page5.getStartRow());
        assertEquals(9, page5.getEndRow());
        Page page6 = pageCursor.createPageCursor(20, 10, 1);
        assertEquals(1, page6.getCurrentPage());
        assertEquals(1, page6.getFirstPage());
        assertEquals(2, page6.getLastPage());
        assertEquals(-1, page6.getPreviousPage());
        assertEquals(2, page6.getNextPage());
        assertEquals(0, page6.getStartRow());
        assertEquals(9, page6.getEndRow());
        Page page7 = pageCursor.createPageCursor(20, 10, 2);
        assertEquals(2, page7.getCurrentPage());
        assertEquals(1, page7.getFirstPage());
        assertEquals(2, page7.getLastPage());
        assertEquals(1, page7.getPreviousPage());
        assertEquals(-1, page7.getNextPage());
        assertEquals(10, page7.getStartRow());
        assertEquals(19, page7.getEndRow());
        Page page8 = pageCursor.createPageCursor(11, 10, 2);
        assertEquals(2, page8.getCurrentPage());
        assertEquals(1, page8.getFirstPage());
        assertEquals(2, page8.getLastPage());
        assertEquals(1, page8.getPreviousPage());
        assertEquals(-1, page8.getNextPage());
        assertEquals(10, page8.getStartRow());
        assertEquals(10, page8.getEndRow());
    }

    public void testUnlimitedPageCursor()
    {
        Page page1 = pageCursor.createPageCursor(100, 0, 1);
        assertTrue(page1.isInRange());
        assertEquals(1, page1.getCurrentPage());
        assertEquals(1, page1.getFirstPage());
        assertEquals(1, page1.getLastPage());
        assertEquals(-1, page1.getPreviousPage());
        assertEquals(-1, page1.getNextPage());
        assertEquals(0, page1.getStartRow());
        assertEquals(99, page1.getEndRow());
        Page page2 = pageCursor.createPageCursor(100, 0, 2);
        assertFalse(page2.isInRange());
    }

    public void testScrollPageCursor()
    {
        int count = 0;
        long[] coll = new long[100];

        Page page = pageCursor.createPageCursor(100, 10, 1);
        while (page.isInRange())
        {
           for (long i = page.getStartRow(); i <= page.getEndRow(); i++)
           {
              coll[(int)i] = i;
              count++;
           }
           page = pageCursor.createPageCursor(100, 10, page.getNextPage());
        }
        
        assertEquals(100, count);
        for (int test = 0; test < count; test++)
        {
            assertEquals(test, coll[test]);
        }
    }
    
    public void testZeroRowsIndexCursor()
    {
        Rows rows = pageCursor.createRowsCursor(0, 10, 0);
        assertNotNull(rows);
        assertEquals(0, rows.getTotalRows());
        assertFalse(rows.isInRange());
        assertEquals(10, rows.getMaxRows());
        assertEquals(-1, rows.getStartRow());
        assertEquals(-1, rows.getEndRow());
        assertEquals(-1, rows.getNextSkipRows());
    }

    public void testOutOfBoundsRowsCursor()
    {
        Rows rows1 = pageCursor.createRowsCursor(1, 1, 0);
        assertNotNull(rows1);
        assertTrue(rows1.isInRange());
        Rows rows2 = pageCursor.createRowsCursor(1, 1, 1);
        assertNotNull(rows2);
        assertFalse(rows2.isInRange());
        Rows rows3 = pageCursor.createRowsCursor(1, -1, 0);
        assertNotNull(rows3);
        assertTrue(rows3.isInRange());
    }

    public void testTotalRowsCursor()
    {
        Rows rows1 = pageCursor.createRowsCursor(10, 1, 1);
        assertEquals(10, rows1.getTotalRows());
    }

    public void testPagingRowsCursor()
    {
        Rows rows1 = pageCursor.createRowsCursor(10, 1, 0);
        assertEquals(0, rows1.getStartRow());
        assertEquals(0, rows1.getEndRow());
        assertEquals(1, rows1.getNextSkipRows());
        Rows rows2 = pageCursor.createRowsCursor(10, 7, 0);
        assertEquals(0, rows2.getStartRow());
        assertEquals(6, rows2.getEndRow());
        assertEquals(7, rows2.getNextSkipRows());
        Rows rows3 = pageCursor.createRowsCursor(10, 7, 7);
        assertEquals(7, rows3.getStartRow());
        assertEquals(9, rows3.getEndRow());
        assertEquals(-1, rows3.getNextSkipRows());
        Rows rows4 = pageCursor.createRowsCursor(10, 10, 0);
        assertEquals(0, rows4.getStartRow());
        assertEquals(9, rows4.getEndRow());
        assertEquals(-1, rows4.getNextSkipRows());
        Rows rows5 = pageCursor.createRowsCursor(10, 11, 0);
        assertEquals(0, rows5.getStartRow());
        assertEquals(9, rows5.getEndRow());
        assertEquals(-1, rows5.getNextSkipRows());
    }

    public void testUnlimitedRowsCursor()
    {
        Rows rows1 = pageCursor.createRowsCursor(100, 0, 0);
        assertTrue(rows1.isInRange());
        assertEquals(0, rows1.getStartRow());
        assertEquals(99, rows1.getEndRow());
        assertEquals(-1, rows1.getNextSkipRows());
    }

    public void testScrollRowsCursor()
    {
        int count = 0;
        long[] coll = new long[100];

        Rows rows = pageCursor.createRowsCursor(100, 10, 0);
        while (rows.isInRange())
        {
           for (long i = rows.getStartRow(); i <= rows.getEndRow(); i++)
           {
              coll[(int)i] = i;
              count++;
           }
           rows = pageCursor.createRowsCursor(100, 10, rows.getNextSkipRows());
        }
        
        assertEquals(100, count);
        for (int test = 0; test < count; test++)
        {
            assertEquals(test, coll[test]);
        }
    }
    
}
