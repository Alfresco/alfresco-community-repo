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
/*
 * Copyright (C) 2017 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.rest.search;

import org.alfresco.utility.model.TestModel;

/**
 * Search response sample.
 *  "pagination": {
 *     "maxItems": 100,
 *     "hasMoreItems": false,
 *     "totalItems": 0,
 *     "count": 0,
 *     "skipCount": 0
 *  },
 **/
public class Pagination extends TestModel
{
    
    private Integer maxItems;
    private Integer totalItems;
    private Integer count;
    private Integer skipCount;
    private boolean hasMoreItems;
    public Integer getMaxItems()
    {
        return maxItems;
    }
    public void setMaxItems(Integer maxItems)
    {
        this.maxItems = maxItems;
    }
    public Integer getTotalItems()
    {
        return totalItems;
    }
    public void setTotalItems(Integer totalItems)
    {
        this.totalItems = totalItems;
    }
    public Integer getCount()
    {
        return count;
    }
    public void setCount(Integer count)
    {
        this.count = count;
    }
    public Integer getSkipCount()
    {
        return skipCount;
    }
    public void setSkipCount(Integer skipCount)
    {
        this.skipCount = skipCount;
    }
    public boolean isHasMoreItems()
    {
        return hasMoreItems;
    }
    public void setHasMoreItems(boolean hasMoreItems)
    {
        this.hasMoreItems = hasMoreItems;
    }
    
}
