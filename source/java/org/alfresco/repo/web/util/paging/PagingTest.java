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

import junit.framework.TestCase;


/**
 * Test Paged and Window Based Cursors
 * 
 * @author davidc
 */
public class PagingTest extends TestCase
{
    protected Paging paging;
    
    @Override
    protected void setUp() throws Exception
    {
        paging = new Paging();
    }

    public void testZeroBasedBooleans()
    {
        assertFalse(paging.isZeroBasedPage());
        paging.setZeroBasedPage(true);
        assertTrue(paging.isZeroBasedPage());
        assertTrue(paging.isZeroBasedRow());
        paging.setZeroBasedRow(false);
        assertFalse(paging.isZeroBasedRow());
    }

    public void testCreatePage()
    {
        Page page = paging.createPage(1, 10);
        assertNotNull(page);
        assertEquals(Paging.PageType.PAGE, page.getType());
        assertFalse(page.isZeroBasedIdx());
        assertEquals(1, page.getNumber());
        assertEquals(10, page.getSize());
        
        Page window = paging.createWindow(1, 10);
        assertNotNull(window);
        assertEquals(Paging.PageType.WINDOW, window.getType());
        assertTrue(window.isZeroBasedIdx());
        assertEquals(1, window.getNumber());
        assertEquals(10, window.getSize());
    }

    public void testZeroRowsPage()
    {
        Cursor cursor = paging.createCursor(0, paging.createPage(1, 10));
        assertNotNull(cursor);
        assertEquals(0, cursor.getTotalRows());
        assertEquals(0, cursor.getTotalPages());
        assertEquals(10, cursor.getPageSize());
        assertFalse(cursor.isInRange());
        assertEquals(1, cursor.getCurrentPage());
        assertEquals(-1, cursor.getFirstPage());
        assertEquals(-1, cursor.getLastPage());
        assertEquals(-1, cursor.getPrevPage());
        assertEquals(-1, cursor.getNextPage());
        assertEquals(0, cursor.getStartRow());
        assertEquals(-1, cursor.getEndRow());
        assertEquals(0, cursor.getRowCount());
    }

    public void testOutOfBoundsPage()
    {
        Cursor cursor1 = paging.createCursor(1, paging.createPage(1, 1));
        assertNotNull(cursor1);
        assertTrue(cursor1.isInRange());
        Cursor cursor2 = paging.createCursor(1, paging.createPage(2, 1));
        assertNotNull(cursor2);
        assertFalse(cursor2.isInRange());
        Cursor cursor3 = paging.createCursor(1, paging.createPage(1, 2));
        assertNotNull(cursor3);
        assertTrue(cursor3.isInRange());
        Cursor cursor4 = paging.createCursor(10, paging.createPage(5, 2));
        assertNotNull(cursor4);
        assertTrue(cursor4.isInRange());
        Cursor cursor5 = paging.createCursor(10, paging.createPage(6, 2));
        assertNotNull(cursor5);
        assertFalse(cursor5.isInRange());
        Cursor cursor6 = paging.createCursor(11, paging.createPage(5, 2));
        assertNotNull(cursor6);
        assertTrue(cursor6.isInRange());
        Cursor cursor7 = paging.createCursor(11, paging.createPage(6, 2));
        assertNotNull(cursor7);
        assertTrue(cursor7.isInRange());
        Cursor cursor8 = paging.createCursor(11, paging.createPage(0, 2));
        assertNotNull(cursor8);
        assertFalse(cursor8.isInRange());

        paging.setZeroBasedPage(true);
        Cursor cursor10 = paging.createCursor(1, paging.createPage(0, 1));
        assertNotNull(cursor10);
        assertTrue(cursor10.isInRange());
        Cursor cursor11 = paging.createCursor(1, paging.createPage(1, 1));
        assertNotNull(cursor11);
        assertFalse(cursor11.isInRange());
        Cursor cursor12 = paging.createCursor(1, paging.createPage(0, 2));
        assertNotNull(cursor12);
        assertTrue(cursor12.isInRange());
        Cursor cursor13 = paging.createCursor(10, paging.createPage(4, 2));
        assertNotNull(cursor13);
        assertTrue(cursor13.isInRange());
        Cursor cursor14 = paging.createCursor(10, paging.createPage(5, 2));
        assertNotNull(cursor14);
        assertFalse(cursor14.isInRange());
        Cursor cursor15 = paging.createCursor(11, paging.createPage(4, 2));
        assertNotNull(cursor15);
        assertTrue(cursor15.isInRange());
        Cursor cursor16 = paging.createCursor(11, paging.createPage(5, 2));
        assertNotNull(cursor16);
        assertTrue(cursor16.isInRange());
        Cursor cursor17 = paging.createCursor(11, paging.createPage(-1, 2));
        assertNotNull(cursor17);
        assertFalse(cursor17.isInRange());
    }

    public void testTotalPage()
    {
        Cursor cursor1 = paging.createCursor(10, paging.createPage(1, 1));
        assertEquals(10, cursor1.getTotalRows());
        assertEquals(10, cursor1.getTotalPages());
        Cursor cursor2 = paging.createCursor(10, paging.createPage(1, 10));
        assertEquals(10, cursor2.getTotalRows());
        assertEquals(1, cursor2.getTotalPages());
        Cursor cursor3 = paging.createCursor(9, paging.createPage(1, 10));
        assertEquals(9, cursor3.getTotalRows());
        assertEquals(1, cursor3.getTotalPages());
        Cursor cursor4 = paging.createCursor(11, paging.createPage(1, 10));
        assertEquals(11, cursor4.getTotalRows());
        assertEquals(2, cursor4.getTotalPages());
        Cursor cursor5 = paging.createCursor(20, paging.createPage(1, 10));
        assertEquals(20, cursor5.getTotalRows());
        assertEquals(2, cursor5.getTotalPages());
    }

    public void testCursorPage()
    {
        Cursor cursor1 = paging.createCursor(10, paging.createPage(1, 1));
        assertEquals(1, cursor1.getCurrentPage());
        assertEquals(1, cursor1.getFirstPage());
        assertEquals(10, cursor1.getLastPage());
        assertEquals(-1, cursor1.getPrevPage());
        assertEquals(2, cursor1.getNextPage());
        assertEquals(0, cursor1.getStartRow());
        assertEquals(0, cursor1.getEndRow());
        assertEquals(1, cursor1.getRowCount());
        Cursor cursor2 = paging.createCursor(10, paging.createPage(2, 1));
        assertEquals(2, cursor2.getCurrentPage());
        assertEquals(1, cursor2.getFirstPage());
        assertEquals(10, cursor2.getLastPage());
        assertEquals(1, cursor2.getPrevPage());
        assertEquals(3, cursor2.getNextPage());
        assertEquals(1, cursor2.getStartRow());
        assertEquals(1, cursor2.getEndRow());
        assertEquals(1, cursor2.getRowCount());
        Cursor cursor3 = paging.createCursor(10, paging.createPage(1, 10));
        assertEquals(1, cursor3.getCurrentPage());
        assertEquals(1, cursor3.getFirstPage());
        assertEquals(1, cursor3.getLastPage());
        assertEquals(-1, cursor3.getPrevPage());
        assertEquals(-1, cursor3.getNextPage());
        assertEquals(0, cursor3.getStartRow());
        assertEquals(9, cursor3.getEndRow());
        assertEquals(10, cursor3.getRowCount());
        Cursor cursor4 = paging.createCursor(9, paging.createPage(1, 10));
        assertEquals(1, cursor4.getCurrentPage());
        assertEquals(1, cursor4.getFirstPage());
        assertEquals(1, cursor4.getLastPage());
        assertEquals(-1, cursor4.getPrevPage());
        assertEquals(-1, cursor4.getNextPage());
        assertEquals(0, cursor4.getStartRow());
        assertEquals(8, cursor4.getEndRow());
        assertEquals(9, cursor4.getRowCount());
        Cursor cursor5 = paging.createCursor(11, paging.createPage(1, 10));
        assertEquals(1, cursor5.getCurrentPage());
        assertEquals(1, cursor5.getFirstPage());
        assertEquals(2, cursor5.getLastPage());
        assertEquals(-1, cursor5.getPrevPage());
        assertEquals(2, cursor5.getNextPage());
        assertEquals(0, cursor5.getStartRow());
        assertEquals(9, cursor5.getEndRow());
        assertEquals(10, cursor5.getRowCount());
        Cursor cursor6 = paging.createCursor(20, paging.createPage(1, 10));
        assertEquals(1, cursor6.getCurrentPage());
        assertEquals(1, cursor6.getFirstPage());
        assertEquals(2, cursor6.getLastPage());
        assertEquals(-1, cursor6.getPrevPage());
        assertEquals(2, cursor6.getNextPage());
        assertEquals(0, cursor6.getStartRow());
        assertEquals(9, cursor6.getEndRow());
        assertEquals(10, cursor6.getRowCount());
        Cursor cursor7 = paging.createCursor(20, paging.createPage(2, 10));
        assertEquals(2, cursor7.getCurrentPage());
        assertEquals(1, cursor7.getFirstPage());
        assertEquals(2, cursor7.getLastPage());
        assertEquals(1, cursor7.getPrevPage());
        assertEquals(-1, cursor7.getNextPage());
        assertEquals(10, cursor7.getStartRow());
        assertEquals(19, cursor7.getEndRow());
        assertEquals(10, cursor7.getRowCount());
        Cursor cursor8 = paging.createCursor(11, paging.createPage(2, 10));
        assertEquals(2, cursor8.getCurrentPage());
        assertEquals(1, cursor8.getFirstPage());
        assertEquals(2, cursor8.getLastPage());
        assertEquals(1, cursor8.getPrevPage());
        assertEquals(-1, cursor8.getNextPage());
        assertEquals(10, cursor8.getStartRow());
        assertEquals(10, cursor8.getEndRow());
        assertEquals(1, cursor8.getRowCount());
    }

    public void testUnlimitedPage()
    {
        Cursor cursor1 = paging.createCursor(100, paging.createPage(1, -1));
        assertTrue(cursor1.isInRange());
        assertEquals(1, cursor1.getCurrentPage());
        assertEquals(1, cursor1.getFirstPage());
        assertEquals(1, cursor1.getLastPage());
        assertEquals(-1, cursor1.getPrevPage());
        assertEquals(-1, cursor1.getNextPage());
        assertEquals(0, cursor1.getStartRow());
        assertEquals(99, cursor1.getEndRow());
        Cursor cursor2 = paging.createCursor(100, paging.createPage(2, -1));
        assertFalse(cursor2.isInRange());
    }

    public void testScrollPage()
    {
        int count = 0;
        long[] coll = new long[100];

        Cursor cursor = paging.createCursor(coll.length, paging.createPage(1, 10));
        while (cursor.isInRange())
        {
           for (long i = cursor.getStartRow(); i <= cursor.getEndRow(); i++)
           {
              coll[(int)i] = i;
              count++;
           }
           cursor = paging.createCursor(coll.length, paging.createPage(cursor.getNextPage(), cursor.getPageSize()));
        }
        
        assertEquals(100, count);
        for (int test = 0; test < count; test++)
        {
            assertEquals(test, coll[test]);
        }
    }

    public void testZeroRowsWindow()
    {
        Cursor rows = paging.createCursor(0, paging.createWindow(0, 10));
        assertNotNull(rows);
        assertEquals(0, rows.getTotalRows());
        assertFalse(rows.isInRange());
        assertEquals(10, rows.getPageSize());
        assertEquals(0, rows.getStartRow());
        assertEquals(-1, rows.getEndRow());
        assertEquals(0, rows.getRowCount());
        assertEquals(-1, rows.getNextPage());
    }

    public void testOutOfBoundsWindow()
    {
        Cursor cursor1 = paging.createCursor(1, paging.createWindow(0, 1));
        assertNotNull(cursor1);
        assertTrue(cursor1.isInRange());
        Cursor cursor2 = paging.createCursor(1, paging.createWindow(1, 1));
        assertNotNull(cursor2);
        assertFalse(cursor2.isInRange());
        Cursor cursor3 = paging.createCursor(1, paging.createWindow(0, -1));
        assertNotNull(cursor3);
        assertTrue(cursor3.isInRange());
    }

    public void testTotalWindow()
    {
        Cursor cursor1 = paging.createCursor(10, paging.createWindow(1, 1));
        assertEquals(10, cursor1.getTotalRows());
    }

    public void testCursorWindow()
    {
        Cursor cursor1 = paging.createCursor(10, paging.createWindow(0, 1));
        assertEquals(0, cursor1.getStartRow());
        assertEquals(0, cursor1.getEndRow());
        assertEquals(1, cursor1.getRowCount());
        assertEquals(1, cursor1.getNextPage());
        Cursor cursor2 = paging.createCursor(10, paging.createWindow(0, 7));
        assertEquals(0, cursor2.getStartRow());
        assertEquals(6, cursor2.getEndRow());
        assertEquals(7, cursor2.getRowCount());
        assertEquals(7, cursor2.getNextPage());
        Cursor cursor3 = paging.createCursor(10, paging.createWindow(7, 7));
        assertEquals(7, cursor3.getStartRow());
        assertEquals(9, cursor3.getEndRow());
        assertEquals(3, cursor3.getRowCount());
        assertEquals(-1, cursor3.getNextPage());
        Cursor cursor4 = paging.createCursor(10, paging.createWindow(0, 10));
        assertEquals(0, cursor4.getStartRow());
        assertEquals(9, cursor4.getEndRow());
        assertEquals(10, cursor4.getRowCount());
        assertEquals(-1, cursor4.getNextPage());
        Cursor cursor5 = paging.createCursor(10, paging.createWindow(0, 11));
        assertEquals(0, cursor5.getStartRow());
        assertEquals(9, cursor5.getEndRow());
        assertEquals(10, cursor5.getRowCount());
        assertEquals(-1, cursor5.getNextPage());
    }

    public void testUnlimitedWindow()
    {
        Cursor cursor1 = paging.createCursor(100, paging.createWindow(0, -1));
        assertTrue(cursor1.isInRange());
        assertEquals(0, cursor1.getStartRow());
        assertEquals(99, cursor1.getEndRow());
        assertEquals(100, cursor1.getRowCount());
        assertEquals(-1, cursor1.getNextPage());
    }

    public void testScrollWindow()
    {
        int count = 0;
        long[] coll = new long[100];

        Cursor cursor = paging.createCursor(coll.length, paging.createWindow(0, 10));
        while (cursor.isInRange())
        {
           for (long i = cursor.getStartRow(); i <= cursor.getEndRow(); i++)
           {
              coll[(int)i] = i;
              count++;
           }
           cursor = paging.createCursor(coll.length, paging.createWindow(cursor.getNextPage(), cursor.getPageSize()));
        }
        
        assertEquals(100, count);
        for (int test = 0; test < count; test++)
        {
            assertEquals(test, coll[test]);
        }
    }

}
