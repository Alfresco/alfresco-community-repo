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
    public enum ItemsSizeConfidence {
        EXACT,     
        RANGE,
        AT_LEAST,
        UNKNOWN
    };
   
    private int skipCount = -1; // TODO To PagingRequest
    private String queryExecutionId = null;  // TODO To PagingRequest
    private int maxItems = -1; // TODO To PagingRequest
    
    private int totalItems = -1;
    private int totalItemsRangeMax = -1;
    private ItemsSizeConfidence confidence = ItemsSizeConfidence.UNKNOWN;
    
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
    
    public ItemsSizeConfidence getConfidence()
    {
        return confidence;
    }
    
    /**
     * Get the total number of items. See {@link #getConfidence()} for an idea 
     *  of the accuracy/confidence on this value. 
     */
    public int getTotalItems()
    {
        return totalItems;
    }
    public void setTotalItems(int totalItems)
    {
        this.totalItems = totalItems;
        
        if(totalItems >= 0)
        {
           this.confidence = ItemsSizeConfidence.EXACT;
        }
        else
        {
           this.confidence = ItemsSizeConfidence.UNKNOWN;
        }
    }
    
    /**
     * Where the confidence is {@link ItemsSizeConfidence#RANGE}, returns
     *  the upper bound of the range.
     */
    public int getTotalItemsRangeMax()
    {
        return totalItemsRangeMax;
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