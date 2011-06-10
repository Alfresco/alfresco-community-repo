package org.alfresco.util;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;

/**
 * A simple paging details wrapper, to hold things like the 
 *  skip count, max items and total items. This is typically
 *  used with Scripts and WebScripts, and feeds into the
 *  Repository level paging support.
 * This class is typically used with {@link ModelUtil}.
 * Note that {@link org.alfresco.repo.web.paging.Paging}
 *  provides an alternate solution for other paging
 *  use cases.
 * 
 * TODO Set a value for {@link #setRequestTotalCountMax(int)}
 */
public class ScriptPagingDetails extends PagingRequest
{
    public enum ItemsSizeConfidence {
        EXACT,     
        RANGE,
        AT_LEAST,
        UNKNOWN
    };
   
    private int totalItems = -1;
    private int totalItemsRangeMax = -1;
    private ItemsSizeConfidence confidence = ItemsSizeConfidence.UNKNOWN;
    
    public ScriptPagingDetails() 
    {
        super(-1, null);
    }
    public ScriptPagingDetails(int maxItems, int skipCount)
    {
        this(maxItems, skipCount, null);
    }
    public ScriptPagingDetails(int maxItems, int skipCount, String queryExecutionId)
    {
        super(skipCount, maxItems, queryExecutionId);
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
    
    /**
     * Records the total number of items that were found. If the value is -1,
     *  then the confidence is set to {@link ItemsSizeConfidence#UNKNOWN}, otherwise
     *  the confidence is {@link ItemsSizeConfidence#EXACT}
     * @param totalItems The total number of items the search found
     */
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
     * Records the total number of results found, and the confidence
     *  in this, from the Paging Results 
     * @param results The PagingResults to extract the information from
     */
    public <R> void setTotalItems(PagingResults<R> results)
    {
        Integer min = results.getTotalResultCount().getFirst();
        Integer max = results.getTotalResultCount().getSecond();
     
        // Get the total count and confidence 
        if(min == null)
        {
           this.totalItems = -1;
           this.confidence = ItemsSizeConfidence.UNKNOWN;
        }
        else if(max == null)
        {
           this.totalItems = min;
           this.confidence = ItemsSizeConfidence.AT_LEAST;
        }
        else if(min == max)
        {
           this.totalItems = min;
           this.confidence = ItemsSizeConfidence.EXACT;
        }
        else
        {
           this.totalItems = min;
           this.totalItemsRangeMax = max;
           this.confidence = ItemsSizeConfidence.RANGE;
        }
        
        // Finally record the query execution ID
        setQueryExecutionId(results.getQueryExecutionId());
    }
    
    /**
     * Where the confidence is {@link ItemsSizeConfidence#RANGE}, returns
     *  the upper bound of the range.
     */
    public int getTotalItemsRangeMax()
    {
        return totalItemsRangeMax;
    }
    
    public void setMaxItems(int maxItems)
    {
        super.setMaxItems(maxItems);
    }
    
    public void setSkipCount(int skipCount)
    {
        super.setSkipCount(skipCount);
    }
    
    public void setQueryExecutionId(String queryExecutionId) {
        super.setQueryExecutionId(queryExecutionId);
    }
}