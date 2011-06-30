/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.service.cmr.subscriptions;

import java.util.Collections;
import java.util.List;

import org.alfresco.util.Pair;

public class PagingFollowingResultsImpl implements PagingFollowingResults
{
    private List<String> page;
    private boolean hasMore;
    private Pair<Integer, Integer> totalCount;

    public PagingFollowingResultsImpl(List<String> page, boolean hasMore, Integer total)
    {
        this.page = page;
        this.hasMore = hasMore;

        if (total != null)
        {
            totalCount = new Pair<Integer, Integer>(total, total);
        }
    }

    @Override
    public List<String> getPage()
    {
        return Collections.unmodifiableList(page);
    }

    @Override
    public boolean hasMoreItems()
    {
        return hasMore;
    }

    @Override
    public Pair<Integer, Integer> getTotalResultCount()
    {
        return totalCount;
    }

    @Override
    public String getQueryExecutionId()
    {
        return null;
    }
}
