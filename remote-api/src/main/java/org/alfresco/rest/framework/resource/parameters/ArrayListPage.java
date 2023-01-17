/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.framework.resource.parameters;

import org.alfresco.rest.api.search.context.SearchContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArrayListPage<E> extends ArrayList<E> implements ListPage<E>
{
    private final Paging paging;
    private final int totalItems;
    private final boolean hasMore;

    public ArrayListPage(final List<? extends E> list)
    {
        super(list != null ? list : Collections.emptyList());
        this.paging = null;
        this.totalItems = this.size();
        this.hasMore = false;
    }

    public ArrayListPage(final List<? extends E> list, final Paging paging)
    {
        super(sublistFrom(list, paging));
        this.paging = paging;
        if (list != null)
        {
            this.totalItems = list.size();
            if (paging != null)
            {
                final int start = paging.getSkipCount();
                final int end = (paging.getMaxItems() == 0) ? list.size() : Math.min(list.size(), start + paging.getMaxItems());
                this.hasMore = !(list.size() == end);
            } else {
                this.hasMore = false;
            }
        } else {
            this.totalItems = 0;
            this.hasMore = false;
        }
    }

    @Override
    public Paging getPaging()
    {
        return paging;
    }

    @Override
    public Integer getTotalItems()
    {
        return totalItems;
    }

    @Override
    public boolean hasMoreItems()
    {
        return hasMore;
    }

    @Override
    public String getQueryExecutionId()
    {
        return null;
    }

    @Override
    public Object getSourceEntity()
    {
        return null;
    }

    @Override
    public SearchContext getContext()
    {
        return null;
    }

    private static <E> List<? extends E> sublistFrom(final List<? extends E> list, final Paging paging)
    {
        if (list == null)
        {
            return Collections.emptyList();
        }
        if (paging == null)
        {
            return list;
        }

        final int start = paging.getSkipCount();
        final int end = (paging.getMaxItems() == 0)? list.size() : Math.min(list.size(), start + paging.getMaxItems());
        return list.subList(Math.min(start, end), end);
    }
}
