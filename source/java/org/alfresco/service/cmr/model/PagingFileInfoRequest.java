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

import org.alfresco.query.PagingRequest;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * Request for page of FileInfo results
 * 
 * @author janv
 * @since 4.0
 */
public class PagingFileInfoRequest implements PagingRequest
{
    private int skipCount = 0;
    private int maxItems;
    private int requestTotalCountMax = 0; // request total count up to a given max (0 => do not request total count)
    private List<Pair<QName, Boolean>> sortProps;
    private String queryExecutionId;
    
    public PagingFileInfoRequest(int maxItems, String queryExecutionId)
    {
        this.maxItems = maxItems;
        this.queryExecutionId = queryExecutionId;
    }
    
    public PagingFileInfoRequest(int skipCount, int maxItems, List<Pair<QName, Boolean>> sortProps, String queryExecutionId)
    {
        this.skipCount = skipCount;
        this.maxItems = maxItems;
        this.sortProps = sortProps;
        this.queryExecutionId = queryExecutionId;
    }
    
    @Override
    public int getSkipCount()
    {
        return skipCount;
    }
    
    @Override
    public int getMaxItems()
    {
        return maxItems;
    }
    
    @Override
    public int getRequestTotalCountMax()
    {
        return requestTotalCountMax;
    }
    
    public void setRequestTotalCountMax(int requestTotalCountMax)
    {
        this.requestTotalCountMax = requestTotalCountMax;
    }
    
    public List<Pair<QName, Boolean>> getSortProps()
    {
        return sortProps;
    }
    
    @Override
    public String getQueryExecutionId()
    {
        return queryExecutionId;
    }
}
