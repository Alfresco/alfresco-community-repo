/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.page;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.query.ListBackedPagingResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.virtual.page.PageCollator.PagingResultsSource;
import org.alfresco.util.Pair;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.util.comparator.ComparableComparator;

public class PageCollatorTest extends TestCase
{
    private static Log logger = LogFactory.getLog(PageCollatorTest.class);

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    private int[] createMergedPage(int skip, int count, List<Integer> s1, Integer[] s2)
    {
        return createMergedPage(skip,
                                count,
                                s1,
                                ArrayUtils.toPrimitive(s2));
    }

    private int[] createMergedPage(int skip, int count, List<Integer> s1, int[] s2)
    {
        int[] s1Primitive = (s1 == null || s1.isEmpty()) ? new int[] {} : ArrayUtils.toPrimitive((Integer[]) s1
                    .toArray());

        return createMergedPage(skip,
                                count,
                                s1Primitive,
                                s2);
    }

    /**
     * Creates an page-array by concatenating the given arrays than skipping and
     * trimming the resulted array.
     * 
     * @param skip
     * @param count
     * @param s1
     * @param s2
     * @return a page-array obtained by merging <code>s1</code> and
     *         <code>s2</code>, sorting the result than copying
     *         <code>count</code> elements from the concatenation starting with
     *         the element at index <code>skip</code> into the returned array of
     *         size <code>count</code>. If <code>skip</code> value is out of
     *         bounds an empty array is returned.
     */
    private int[] createMergedPage(int skip, int count, int[] s1, int[] s2)
    {
        final int[] m = Arrays.copyOf(s1,
                                      s1.length + s2.length);
        System.arraycopy(s2,
                         0,
                         m,
                         s1.length,
                         s2.length);
        Arrays.sort(m);

        final int zeroCount = (count == 0 ? m.length : count);

        final int len = Math.min(m.length - skip,
                                 zeroCount);
        if (len > 0)
        {
            int[] p = new int[len];

            System.arraycopy(m,
                             skip,
                             p,
                             0,
                             len);

            return p;
        }
        else
        {
            return new int[] {};
        }
    }

    private void assertEqualPages(int[] expected, int[] actual)
    {
        assertEqualPages(Arrays.toString(expected) + " vs " + Arrays.toString(actual),
                         expected,
                         actual);
    }

    private void assertEqualPages(String message, int[] expected, int[] actual)
    {
        assertTrue(message,
                   Arrays.equals(expected,
                                 actual));
    }

    private void assertEqualPages(String message, int[] expected, List<Integer> page)
    {
        assertEqualPages(message,
                         expected,
                         ArrayUtils.toPrimitive((Integer[]) page.toArray(new Integer[] {})));
    }

    @Test
    public void testCreateMergedPage() throws Exception
    {
        // { 0, 1, 2, 3, 4, 7, 8, 9, 10 }
        final int[] s1 = new int[] { 0, 1, 3, 7, 8 };
        final int[] s2 = new int[] { 2, 4, 9, 10 };

        assertEqualPages(new int[] { 2, 3, 4 },
                         createMergedPage(2,
                                          3,
                                          s1,
                                          s2));

        assertEqualPages(new int[] { 9, 10 },
                         createMergedPage(7,
                                          2,
                                          s1,
                                          s2));

        assertEqualPages(new int[] { 0, 1, 2, 3, 4 },
                         createMergedPage(0,
                                          5,
                                          s1,
                                          s2));

        assertEqualPages(new int[] { 0, 1, 2, 3, 4 },
                         createMergedPage(0,
                                          5,
                                          s1,
                                          s2));

        assertEqualPages(new int[] { 4, 7, 8, 9 },
                         createMergedPage(4,
                                          4,
                                          s1,
                                          s2));

        assertEqualPages(new int[] {},
                         createMergedPage(10,
                                          4,
                                          s1,
                                          s2));

        assertEqualPages(new int[] {},
                         createMergedPage(9,
                                          1,
                                          s1,
                                          s2));

        assertEqualPages(new int[] {},
                         createMergedPage(9,
                                          1,
                                          s1,
                                          s2));

        assertEqualPages(new int[] {},
                         createMergedPage(11,
                                          3,
                                          s1,
                                          s2));

        final int[] s3 = new int[] { 2, 3 };
        final int[] s4 = new int[] { 7, 10, 13, 11 };

        assertEqualPages(new int[] { 13 },
                         createMergedPage(5,
                                          2,
                                          s3,
                                          s4));

        final int[] s5 = new int[] { 1, 2, 3 };
        final int[] s6 = new int[] { 5 };

        assertEqualPages(new int[] { 5 },
                         createMergedPage(3,
                                          1,
                                          s5,
                                          s6));

        assertEqualPages(new int[] { 1, 2, 3, 5 },
                         createMergedPage(0,
                                          0,
                                          s5,
                                          s6));
    }

    /**
     * A page source that uses a generic array for the entire possible page
     * range.
     * 
     * @author Bogdan Horje
     * @param <R>
     */
    class ArrayPageSource<R> implements PagingResultsSource<R>
    {
        private R[] array;

        private boolean boundsError;

        public ArrayPageSource(R[] array)
        {
            this(array,
                 true);
        }

        public ArrayPageSource(R[] array, boolean boundsError)
        {
            super();
            this.array = array;
            this.boundsError = boundsError;
        }

        @Override
        public PagingResults<R> retrieve(PagingRequest pr) throws PageCollationException
        {
            final int skip = pr.getSkipCount();
            final int pageSize = pr.getMaxItems();
            if (skip < 0 || pageSize < 0)
            {
                throw new PageCollationException("Invalid page!");
            }

            if (boundsError && (skip >= array.length))
            {
                throw new InvalidPageBounds("Out of bounds " + skip + ">=" + array.length);
            }

            return new ListBackedPagingResults<R>(Arrays.asList(array),
                                                  pr);
        }

    }

    public void assertCollate(List<Integer> results, Integer[] source, int skip, int pageSize) throws Exception
    {
        assertCollate(results,
                      source,
                      skip,
                      pageSize,
                      false);
    }

    public void assertCollate(List<Integer> results, Integer[] source, int skip, int pageSize, boolean boundsError)
                throws Exception
    {
        Arrays.sort(source);
        Collections.sort(results);
        int[] expected = createMergedPage(skip,
                                          pageSize,
                                          results,
                                          source);
        PagingResults<Integer> actualResults = new PageCollator<Integer>().collate(results,
                                                                                   new ArrayPageSource(source),
                                                                                   new PagingRequest(skip,
                                                                                                     pageSize),
                                                                                   new ComparableComparator<Integer>());
        List<Integer> actualPage = actualResults.getPage();

        final String message = "[" + results + " + " + Arrays.toString(source) + " ] -> " + Arrays.toString(expected)
                    + " != " + actualPage;
        assertEqualPages(message,
                         expected,
                         actualPage);
        assertEquals("Invalid moreItems info!",
                     (pageSize != 0) && (skip + pageSize < results.size() + source.length),
                     actualResults.hasMoreItems());
        assertTrue(message,
                   (pageSize == 0) || actualPage.size() <= pageSize);
        final int expectedTotal = source.length + results.size();
        if (boundsError && !new Pair<Integer, Integer>(null,
                                                       null).equals(actualResults.getTotalResultCount()))
        {
            assertEquals("Invalid total info",
                         new Pair<Integer, Integer>(expectedTotal,
                                                    expectedTotal),
                         actualResults.getTotalResultCount());
        }
        logger.info(actualPage);
    }

    @Test
    public void testCollate() throws Exception
    {
        final List<Integer> s1 = Arrays.asList(2,
                                               4,
                                               9,
                                               10);
        final Integer[] s2 = new Integer[] { 0, 1, 3, 5, 7, 8 };

        assertCollate(s1,
                      s2,
                      0,
                      3);
        assertCollate(s1,
                      s2,
                      2,
                      3);

        final List<Integer> s3 = Arrays.asList(2,
                                               4,
                                               9,
                                               10,
                                               12,
                                               15,
                                               17,
                                               18);
        final Integer[] s4 = new Integer[] { 0, 1, 3, 8, 16, 19 };

        assertCollate(s3,
                      s4,
                      0,
                      3);
        assertCollate(s3,
                      s4,
                      2,
                      3);
        assertCollate(s3,
                      s4,
                      6,
                      3);
        assertCollate(s3,
                      s4,
                      7,
                      3);

        final List<Integer> s5 = Arrays.asList(2,
                                               3,
                                               6,
                                               8);
        final Integer[] s6 = new Integer[] { 7, 10, 13, 11, 17, 19 };

        assertCollate(s5,
                      s6,
                      0,
                      1);
        assertCollate(s5,
                      s6,
                      1,
                      2);

    }

    public void testCollateBoundary1() throws Exception
    {
        final List<Integer> s1 = Arrays.asList(2,
                                               3);
        final Integer[] s2 = new Integer[] { 7, 10, 13, 11 };

        assertCollate(s1,
                      s2,
                      6,
                      1,
                      true);
    }

    public void testCollateBoundary2() throws Exception
    {
        final List<Integer> s1 = Arrays.asList(2,
                                               3);
        final Integer[] s2 = new Integer[] { 7, 10, 13, 11 };

        assertCollate(s1,
                      s2,
                      5,
                      2,
                      true);
    }

    public void testCollateBoundary3() throws Exception
    {
        final List<Integer> s1 = Arrays.asList(1,
                                               2,
                                               3);
        final Integer[] s2 = new Integer[] { 5, 6, 7, 8 };

        assertCollate(s1,
                      s2,
                      6,
                      1,
                      true);
    }

    public void testCollateBoundary4() throws Exception
    {
        final List<Integer> s1 = Arrays.asList(1,
                                               2,
                                               3);
        final Integer[] s2 = new Integer[] { 5 };

        assertCollate(s1,
                      s2,
                      3,
                      1,
                      false);
    }

    public void testCollateBoundary5() throws Exception
    {
        final List<Integer> s1 = Arrays.asList(2,
                                               3);
        final Integer[] s2 = new Integer[] { 7, 10, 13 };

        assertCollate(s1,
                      s2,
                      10,
                      2,
                      true);
    }

    public void testCollateBoundary6() throws Exception
    {
        final List<Integer> s1 = Arrays.asList(2,
                                               3);
        final Integer[] s2 = new Integer[] { 7, 10, 13 };

        assertCollate(s1,
                      s2,
                      0,
                      0,
                      true);
    }

    public void testCollateBoundary7() throws Exception
    {
        final List<Integer> s1 = Arrays.asList(10);
        final Integer[] s2 = new Integer[] { 1, 2, 3 };

        assertCollate(s1,
                      s2,
                      6,
                      1,
                      true);
    }

    public void testCollateBoundary8() throws Exception
    {
        final List<Integer> s1 = Arrays.asList(1);
        final Integer[] s2 = new Integer[] { 5, 6, 7 };

        assertCollate(s1,
                      s2,
                      5,
                      1,
                      true);
    }

    public void testCollateBoundary9() throws Exception
    {
        final List<Integer> s1 = Arrays.asList(1);
        final Integer[] s2 = new Integer[] { 5 };

        assertCollate(s1,
                      s2,
                      0,
                      2,
                      true);
    }

    public void testCollateBoundary10() throws Exception
    {
        final List<Integer> s1 = Arrays.asList();
        final Integer[] s2 = new Integer[] { 5 };

        assertCollate(s1,
                      s2,
                      0,
                      2,
                      true);
    }

    public void testCollateBoundary11() throws Exception
    {
        final List<Integer> s1 = Arrays.asList(1);
        final Integer[] s2 = new Integer[] {};

        assertCollate(s1,
                      s2,
                      0,
                      1,
                      false);
    }

    public void testCollateBoundary12() throws Exception
    {
        final List<Integer> s1 = Arrays.asList();
        final Integer[] s2 = new Integer[] {};

        assertCollate(s1,
                      s2,
                      0,
                      1,
                      false);
    }

    public void testCollateBoundary13() throws Exception
    {
        final List<Integer> s1 = Arrays.asList(6,
                                               7,
                                               8,
                                               9,
                                               10,
                                               11,
                                               12,
                                               13,
                                               14);
        final Integer[] s2 = new Integer[] { 1 };

        assertCollate(s1,
                      s2,
                      3,
                      5,
                      false);
    }

    public void testCollateFalsePositive1() throws Exception
    {
        final List<Integer> s1 = Arrays.asList(2,
                                               3);
        final Integer[] s2 = new Integer[] { 7, 10, 13 };

        try
        {
            new PageCollator<Integer>().collate(s1,
                                                new ArrayPageSource(s2),
                                                new PagingRequest(-1,
                                                                  1),
                                                new ComparableComparator<Integer>());
            fail("Invalid page data.");
        }
        catch (PageCollationException e)
        {
            logger.info(e.getMessage());
        }

        try
        {
            new PageCollator<Integer>().collate(s1,
                                                new ArrayPageSource(s2),
                                                new PagingRequest(1,
                                                                  -1),
                                                new ComparableComparator<Integer>());
            fail("Invalid page data.");
        }
        catch (PageCollationException e)
        {
            logger.info(e.getMessage());
        }
    }
}
