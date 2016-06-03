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
