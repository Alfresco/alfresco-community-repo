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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A generic utility for arranging a {@link List} of objects of type
 * <code>R</code> into a <code>R</code> objects page obtained from a
 * {@link PagingResultsSource} considering a given {@link PagingRequest} for the
 * resulted {@link PagingResults}.
 * 
 * @author Bogdan Horje
 * @param <R>
 */
public class PageCollator<R>
{
    private static Log logger = LogFactory.getLog(PageCollator.class);

    /**
     * Implementors are paging request capable objects - i.e. can provide
     * data-paged results of type <code>R</code> considering a given
     * {@link PagingRequest}.
     *
     * @param <R>
     */
    public interface PagingResultsSource<R>
    {
        PagingResults<R> retrieve(PagingRequest pr) throws PageCollationException;
    }

    /**
     * @param objects
     * @param objectPageSurce
     * @param pagingRequest
     * @param comparator
     * @return a {@link PagingResults} R objects obtained from merging a
     *         collection of R objects with a paged result obtained from a
     *         {@link PagingResultsSource} considering the a merged result
     *         {@link PagingRequest}
     * @throws PageCollationException
     */
    public PagingResults<R> collate(List<R> objects, PagingResultsSource<R> objectPageSurce,
                PagingRequest pagingRequest, Comparator<R> comparator) throws PageCollationException
    {
        final int skip = pagingRequest.getSkipCount();
        final int pageSize = pagingRequest.getMaxItems();

        if (skip < 0 || pageSize < 0)
        {
            throw new InvalidPageBounds("Negative page skip index and/or bounds.");
        }

        int preemptiveSkip = Math.max(0,
                                      skip - objects.size());
        int pageSkip = skip - preemptiveSkip;
        int preemptiveSize = pageSize + pageSkip;
        PagingResults<R> pageResults = null;
        try
        {
            PagingRequest preemptiveRequest = new PagingRequest(preemptiveSkip,
                                                                preemptiveSize,
                                                                pagingRequest.getQueryExecutionId());
            preemptiveRequest.setRequestTotalCountMax(pagingRequest.getRequestTotalCountMax());
            pageResults = objectPageSurce.retrieve(preemptiveRequest);
        }
        catch (InvalidPageBounds e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(e);
            }
            pageResults = new PagingResults<R>()
            {

                @Override
                public List<R> getPage()
                {
                    return Collections.emptyList();
                }

                @Override
                public boolean hasMoreItems()
                {
                    return false;
                }

                @Override
                public Pair<Integer, Integer> getTotalResultCount()
                {
                    return new Pair<Integer, Integer>(null,
                                                      null);
                }

                @Override
                public String getQueryExecutionId()
                {
                    return null;
                }
            };
        }

        return collate(objects,
                       pageResults,
                       pageSkip,
                       pagingRequest,
                       comparator);
    }

    private PagingResults<R> collate(List<R> objects, final PagingResults<R> objectPageSurce, int pageSkip,
                final PagingRequest pagingRequest, Comparator<R> comparator)
    {
        final int pageSize = pagingRequest.getMaxItems();
        final List<R> inPageList = objectPageSurce.getPage();
        final List<R> collatedPageList = new LinkedList<>();
        final boolean endOfCollation = collate(objects,
                                               inPageList,
                                               pageSkip,
                                               pageSize,
                                               comparator,
                                               collatedPageList);
        final int resultsSize = objects.size();

        final Pair<Integer, Integer> pageTotal = objectPageSurce.getTotalResultCount();
        Integer pageTotalFirst = null;
        Integer pageTotalSecond = null;

        if (pageTotal != null)
        {
            pageTotalFirst = pageTotal.getFirst();
            pageTotalSecond = pageTotal.getSecond();
        }

        final Pair<Integer, Integer> total = new Pair<>(pageTotalFirst == null ? null : pageTotalFirst + resultsSize,
                                                        pageTotalSecond == null ? null : pageTotalSecond + resultsSize);

        final boolean hasMoreItems = objectPageSurce.hasMoreItems() || !endOfCollation;

        return new PagingResults<R>()
        {

            @Override
            public List<R> getPage()
            {
                return collatedPageList;
            }

            @Override
            public boolean hasMoreItems()
            {
                return hasMoreItems;
            }

            @Override
            public Pair<Integer, Integer> getTotalResultCount()
            {
                return total;
            }

            @Override
            public String getQueryExecutionId()
            {
                return pagingRequest.getQueryExecutionId();
            }

        };
    }

    private boolean collate(List<R> objects, List<R> pageObjects, int pageSkip, int pageSize, Comparator<R> comparator,
                List<R> collatedResult)
    {

        final int resultsSize = objects.size();
        final int inPageSize = pageObjects.size();
        if (pageSkip >= resultsSize + inPageSize)
        {
            return true;
        }

        List<R> collation = new ArrayList<>(objects.size() + pageObjects.size());
        collation.addAll(pageObjects);

        for (int i = 0; i < resultsSize; i++)
        {
            final int collationSize = collation.size();
            final R result = objects.get(i);
            int j = 0;

            if (comparator != null)
            {
                for (; j < collationSize; j++)
                {
                    final R collated = collation.get(j);
                    if (comparator.compare(result,
                                           collated) <= 0)
                    {
                        break;
                    }
                }
            }

            collation.add(j,
                          result);
        }

        final R[] collationArray = (R[]) collation.toArray();
        final int zeroPageSize = (pageSize == 0 ? collationArray.length - pageSkip : pageSize);
        final int to = Math.min(pageSkip + zeroPageSize,
                                collationArray.length);

        collatedResult.addAll(Arrays.asList(Arrays.copyOfRange(collationArray,
                                                               pageSkip,
                                                               to)));

        return to == collationArray.length;
    }
}
