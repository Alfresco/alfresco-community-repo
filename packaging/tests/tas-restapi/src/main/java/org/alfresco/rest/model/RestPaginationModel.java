package org.alfresco.rest.model;

import org.alfresco.rest.core.assertion.IModelAssertion;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

/**
 * Handles Pagination JSON
 *
 * Example:
 *  "pagination": {
      "count": 100,
      "hasMoreItems": true,
      "totalItems": 269,
      "skipCount": 0,
      "maxItems": 100
    },
 */
public class RestPaginationModel extends TestModel implements IModelAssertion<RestPaginationModel>
{
    private int count;
    private boolean hasMoreItems;
    private Integer totalItems;
    private int skipCount;
    private int maxItems;

    public int getCount()
    {
        return count;
    }

    public void setCount(int count)
    {
        this.count = count;
    }

    public boolean isHasMoreItems()
    {
        return hasMoreItems;
    }

    public void setHasMoreItems(boolean hasMoreItems)
    {
        this.hasMoreItems = hasMoreItems;
    }

    public Integer getTotalItems()
    {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems)
    {
        this.totalItems = totalItems;
    }

    public int getSkipCount()
    {
        return skipCount;
    }

    public void setSkipCount(int skipCount)
    {
        this.skipCount = skipCount;
    }

    public int getMaxItems()
    {
        return maxItems;
    }

    public void setMaxItems(int maxItems)
    {
        this.maxItems = maxItems;
    }

    @Override
    public ModelAssertion<RestPaginationModel> assertThat() 
    {
      return new ModelAssertion<RestPaginationModel>(this);
    }       
    
    @Override
    public ModelAssertion<RestPaginationModel> and() 
    {
      return assertThat();
    }       
}    