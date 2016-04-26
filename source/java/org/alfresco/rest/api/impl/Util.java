package org.alfresco.rest.api.impl;

import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.rest.framework.resource.parameters.Paging;

public class Util
{
	public static PagingRequest getPagingRequest(Paging paging)
    {
    	PagingRequest pagingRequest = new PagingRequest(paging.getSkipCount(), paging.getMaxItems());
    	pagingRequest.setRequestTotalCountMax(CannedQueryPageDetails.DEFAULT_PAGE_SIZE);
    	return pagingRequest;
    }
}
