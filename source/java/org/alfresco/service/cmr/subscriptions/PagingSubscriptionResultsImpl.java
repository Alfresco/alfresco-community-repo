package org.alfresco.service.cmr.subscriptions;

import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

public class PagingSubscriptionResultsImpl implements PagingSubscriptionResults
{
    private List<NodeRef> page;
    private boolean hasMore;
    private Pair<Integer, Integer> totalCount;

    public PagingSubscriptionResultsImpl(List<NodeRef> page, boolean hasMore, Integer total)
    {
        this.page = page;
        this.hasMore = hasMore;

        if (total != null)
        {
            totalCount = new Pair<Integer, Integer>(total, total);
        }
    }

    @Override
    public List<NodeRef> getPage()
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
