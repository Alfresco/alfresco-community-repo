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
package org.alfresco.repo.model.filefolder;

import java.util.List;

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.PagingFileInfoResults;

/**
 * TEMP
 * 
 * @deprecated for review (API is subject to change)
 */
/* package */ class PagingFileInfoResultsImpl implements PagingFileInfoResults
{
    private List<FileInfo> nodeInfos;
    private Boolean hasMore; // null => unknown
    private Long totalCount; // null => not requested (or unknown)
    
    public PagingFileInfoResultsImpl(List<FileInfo> nodeInfos, Boolean hasMore, Long totalCount)
    {
        this.nodeInfos = nodeInfos;
        this.hasMore = hasMore;
        this.totalCount= totalCount;
    }
    
    public List<FileInfo> getResultsForPage()
    {
        return nodeInfos;
    }
    
    public Boolean hasMore()
    {
        return hasMore;
    }
    public Long getTotalCount()
    {
        return totalCount;
    }
}
