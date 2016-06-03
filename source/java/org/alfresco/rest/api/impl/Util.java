package org.alfresco.rest.api.impl;

import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.PageDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author steveglover
 * @author Jamal Kaabi-Mofrad
 */
public class Util
{
    public static PagingRequest getPagingRequest(Paging paging)
    {
        PagingRequest pagingRequest = new PagingRequest(paging.getSkipCount(), paging.getMaxItems());
        pagingRequest.setRequestTotalCountMax(CannedQueryPageDetails.DEFAULT_PAGE_SIZE);
        return pagingRequest;
    }

    public static <T> PagingResults<T> wrapPagingResults(Paging paging, Collection<T> result)
    {
        if (paging == null)
        {
            throw new InvalidArgumentException("paging object can't be null.");
        }
        if (result == null)
        {
            result = Collections.emptyList();
        }

        PagingRequest pagingRequest = getPagingRequest(paging);

        final int totalSize = result.size();
        final PageDetails pageDetails = PageDetails.getPageDetails(pagingRequest, totalSize);

        final List<T> page = new ArrayList<>(pageDetails.getPageSize());
        Iterator<T> it = result.iterator();
        for (int counter = 0, end = pageDetails.getEnd(); counter < end && it.hasNext(); counter++)
        {
            T element = it.next();
            if (counter < pageDetails.getSkipCount())
            {
                continue;
            }
            if (counter > end - 1)
            {
                break;
            }
            page.add(element);
        }

        return new PagingResults<T>()
        {
            @Override
            public List<T> getPage()
            {
                return page;
            }

            @Override
            public boolean hasMoreItems()
            {
                return pageDetails.hasMoreItems();
            }

            @Override
            public Pair<Integer, Integer> getTotalResultCount()
            {
                Integer total = totalSize;
                return new Pair<>(total, total);
            }

            @Override
            public String getQueryExecutionId()
            {
                return null;
            }
        };
    }
}
