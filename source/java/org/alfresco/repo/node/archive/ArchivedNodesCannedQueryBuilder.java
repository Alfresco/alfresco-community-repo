/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.node.archive;

import org.alfresco.query.PagingRequest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;

/**
 * A simple Immutable POJO to hold the canned query parameters.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 4.2
 */
public class ArchivedNodesCannedQueryBuilder
{
    private final NodeRef archiveRootNodeRef;
    private final PagingRequest pagingRequest;
    private String filter;
    private boolean filterIgnoreCase;
    private boolean sortOrderAscending;

    public static class Builder
    {
        private final NodeRef archiveRootNodeRef;
        private final PagingRequest pagingRequest;
        private String filter;
        private boolean filterIgnoreCase;
        private boolean sortOrderAscending;

        public Builder(NodeRef archiveRootNodeRef, PagingRequest pagingRequest)
        {
            this.archiveRootNodeRef = archiveRootNodeRef;
            this.pagingRequest = pagingRequest;
        }

        public Builder filter(String filter)
        {
            this.filter = filter;
            return this;
        }

        public Builder filterIgnoreCase(boolean filterIgnoreCase)
        {
            this.filterIgnoreCase = filterIgnoreCase;
            return this;
        }

        public Builder sortOrderAscending(boolean sortOrderAscending)
        {
            this.sortOrderAscending = sortOrderAscending;
            return this;
        }

        public ArchivedNodesCannedQueryBuilder build()
        {
            return new ArchivedNodesCannedQueryBuilder(this);
        }
    }

    public ArchivedNodesCannedQueryBuilder(Builder builder)
    {
        ParameterCheck.mandatory("storeRef", (this.archiveRootNodeRef = builder.archiveRootNodeRef));
        ParameterCheck.mandatory("pagingRequest", builder.pagingRequest);        
      
        // Defensive copy
        PagingRequest pr = new PagingRequest(builder.pagingRequest.getSkipCount(),
                    builder.pagingRequest.getMaxItems(),
                    builder.pagingRequest.getQueryExecutionId());
        pr.setRequestTotalCountMax(builder.pagingRequest.getRequestTotalCountMax());
        this.pagingRequest = pr;
        this.filterIgnoreCase = builder.filterIgnoreCase;
        this.filter = builder.filter;        
        this.sortOrderAscending = builder.sortOrderAscending;
    }

    public NodeRef getArchiveRootNodeRef()
    {
        return this.archiveRootNodeRef;
    }


    public PagingRequest getPagingRequest()
    {
        PagingRequest pr = new PagingRequest(this.pagingRequest.getSkipCount(),
                    this.pagingRequest.getMaxItems(), this.pagingRequest.getQueryExecutionId());
        pr.setRequestTotalCountMax(this.pagingRequest.getRequestTotalCountMax());
        
        return pr;
    }

    public String getFilter()
    {
        return this.filter;
    }

    public boolean isFilterIgnoreCase()
    {
        return this.filterIgnoreCase;
    }

    public boolean getSortOrderAscending()
    {
        return this.sortOrderAscending;
    }
}
