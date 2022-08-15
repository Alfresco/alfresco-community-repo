/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.model;

import org.alfresco.rest.core.assertion.IModelAssertion;
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
}
