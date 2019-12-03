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
    /**
     * {code}totalItems{code} is optional and some endpoints don't include it e.g. GET sites/{siteId}/members
     * See the section entitled "The list object" in https://ts.alfresco.com/share/page/site/prodman/document-details?nodeRef=workspace://SpacesStore/17eacc65-28e5-40bb-8113-edb8c21d57a5
     */
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

    /**
     * Get the totalItems.
     *
     * @return The total number of items, or null if it was not included in the response.
     */
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
      return new ModelAssertion<>(this);
    }

    @Override
    public ModelAssertion<RestPaginationModel> and()
    {
      return assertThat();
    }
}    