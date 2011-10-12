package org.alfresco.util;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

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
    
    public ScriptPagingDetails(PagingRequest paging)
    {
       super(paging.getSkipCount(), paging.getMaxItems(), paging.getQueryExecutionId());
       setRequestTotalCountMax(paging.getRequestTotalCountMax());
    }
    
    /**
     * Creates a new {@link PagingRequest} object (in the form of
     *  {@link ScriptPagingDetails}), based on the standard URL
     *  parameters for webscript paging.
     *  
     * @param req The request object to extract parameters from
     * @param maxResultCount The maximum results count if none is specified
     */
    public ScriptPagingDetails(WebScriptRequest req, int maxResultCount) throws WebScriptException
    {
       this(buildPagingRequest(req, maxResultCount));
    }
    /**
     * Creates a new {@link PagingRequest} object, based on the standard URL
     *  parameters for webscript paging.
     *  
     * @param req The request object to extract parameters from
     * @param maxResultCount The maximum results count if none is specified
     */
    public static PagingRequest buildPagingRequest(WebScriptRequest req, int maxResultCount) throws WebScriptException
    {
       int pageSize = maxResultCount;
       int startIndex = 0;
       
       String queryId = req.getParameter("queryId");

       String pageSizeS = req.getParameter("pageSize");
       if (pageSizeS != null)
       {
          try
          {
             pageSize = Integer.parseInt(pageSizeS);
          }
          catch (NumberFormatException e)
          {
             throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Paging size parameters invalid");
          }
       }
       
       String startIndexS = req.getParameter("startIndex");
       if (startIndexS != null)
       {
          try
          {
             startIndex = Integer.parseInt(startIndexS);
          }
          catch (NumberFormatException e)
          {
             throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Paging size parameters invalid");
          }
       }
       else
       {
          // No Start Index given, did they supply a Page Number?
          String pageNumberS = req.getParameter("page");
          if(pageNumberS != null)
          {
             try
             {
                int pageNumber = Integer.parseInt(pageNumberS);
                startIndex = (pageNumber-1) * pageSize;
             }
             catch (NumberFormatException e)
             {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Paging size parameters invalid");
             }
          }
       }
       
       PagingRequest paging = new PagingRequest( startIndex, pageSize, queryId );
       
       // The default total count is the higher of page 10, or 2 pages further
       paging.setRequestTotalCountMax( Math.max(10*pageSize,startIndex+2*pageSize) );
       
       // All done
       return paging;
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
        if(results.getTotalResultCount() == null)
        {
           // No count calculated
           this.totalItems = -1;
           this.confidence = ItemsSizeConfidence.UNKNOWN;
        }
        else
        {
           // Get the total count and confidence 
           Integer min = results.getTotalResultCount().getFirst();
           Integer max = results.getTotalResultCount().getSecond();
        
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
    
    public void setQueryExecutionId(String queryExecutionId)
    {
        super.setQueryExecutionId(queryExecutionId);
    }
}