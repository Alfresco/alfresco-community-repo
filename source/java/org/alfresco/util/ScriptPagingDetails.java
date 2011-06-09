package org.alfresco.util;

/**
 * A simple paging details wrapper, to hold things like the 
 *  skip count, max items and total items. This is typically
 *  used with Scripts and WebScripts, and feeds into the
 *  Repository level paging support.
 * This class is typically used with {@link ModelUtil}.
 * Note that {@link org.alfresco.repo.web.paging.Paging}
 *  provides an alternate solution for other paging
 *  use cases.
 */
public class ScriptPagingDetails
{
    private int totalItems = -1;
    private int maxItems = -1; // TODO To PagingRequest
    private int skipCount = -1; // TODO To PagingRequest
    private String queryExecutionId = null;  // TODO To PagingRequest
    
    public ScriptPagingDetails() {}
    public ScriptPagingDetails(int maxItems, int skipCount)
    {
        this(maxItems, skipCount, null);
    }
    public ScriptPagingDetails(int maxItems, int skipCount, String queryExecutionId)
    {
        this.maxItems = maxItems;
        this.skipCount = skipCount;
        this.queryExecutionId = queryExecutionId;
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
    
    public String getQueryExecutionId() {
        return queryExecutionId;
    }
    public void setQueryExecutionId(String queryExecutionId) {
        this.queryExecutionId = queryExecutionId;
    }
}