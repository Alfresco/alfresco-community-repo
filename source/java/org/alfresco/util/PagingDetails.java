package org.alfresco.util;

/**
 * A simple paging details wrapper, to hold things like
 *  the skip count, max items and total items.
 * This is typically used with {@link ModelUtil}.
 * Note that {@link org.alfresco.repo.web.paging.Paging}
 *  provides an alternate solution for other paging
 *  use cases.
 */
public class PagingDetails
{
    private int totalItems = -1;
    private int maxItems = -1;
    private int skipCount = -1;
    
    public PagingDetails() {}
    public PagingDetails(int maxItems, int skipCount)
    {
        this.maxItems = maxItems;
        this.skipCount = skipCount;
    }
    
    public int getTotalItems()
    {
        return totalItems;
    }
    public void setTotalItems(int totalItems)
    {
        this.totalItems = totalItems;
    }
    
    public int getMaxItems()
    {
        return maxItems;
    }
    public void setMaxItems(int maxItems)
    {
        this.maxItems = maxItems;
    }
    
    public int getSkipCount()
    {
        return skipCount;
    }
    public void setSkipCount(int skipCount)
    {
        this.skipCount = skipCount;
    }
}