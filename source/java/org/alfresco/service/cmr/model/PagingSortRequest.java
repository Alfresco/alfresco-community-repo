/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.service.cmr.model;

import java.util.List;

import org.alfresco.service.cmr.repository.PagingSortProp;


/**
 * TEMP
 * 
 * @deprecated for review (API is subject to change)
 */
public class PagingSortRequest
{
    private int skipCount;
    private int maxItems;
    private boolean requestTotalCount;
    private List<PagingSortProp> sortProps;
    
    public PagingSortRequest(int skipCount, int maxItems, boolean requestTotalCount, List<PagingSortProp> sortProps)
    {
        this.skipCount = skipCount;
        this.maxItems = maxItems;
        this.requestTotalCount = requestTotalCount;
        this.sortProps = sortProps;
    }

    public int getSkipCount()
    {
        return skipCount;
    }

    public int getMaxItems()
    {
        return maxItems;
    }

    public boolean requestTotalCount()
    {
        return requestTotalCount;
    }

    public List<PagingSortProp> getSortProps()
    {
        return sortProps;
    }
}
