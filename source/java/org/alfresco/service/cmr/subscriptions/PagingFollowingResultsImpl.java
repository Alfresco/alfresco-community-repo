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
