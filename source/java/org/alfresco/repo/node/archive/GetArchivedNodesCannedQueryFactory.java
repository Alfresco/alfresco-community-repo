
package org.alfresco.repo.node.archive;

import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.query.AbstractQNameAwareCannedQueryFactory;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

/**
 * Canned query factory for getting archived nodes.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 4.2
 */
public class GetArchivedNodesCannedQueryFactory extends AbstractQNameAwareCannedQueryFactory<ArchivedNodeEntity>
{
    @Override
    public CannedQuery<ArchivedNodeEntity> getCannedQuery(CannedQueryParameters parameters)
    {
        return (CannedQuery<ArchivedNodeEntity>) new GetArchivedNodesCannedQuery(cannedQueryDAO,
                    nodeDAO, methodSecurity, parameters);
    }

    /**
     * @param archiveStoreRootNodeRef NodeRef
     * @param assocTypeQName QName
     * @param filter String
     * @param filterIgnoreCase boolean
     * @param pagingRequest PagingRequest
     * @param sortOrderAscending boolean
     * @return an implementation that will execute the query
     */
    public CannedQuery<ArchivedNodeEntity> getCannedQuery(NodeRef archiveStoreRootNodeRef, QName assocTypeQName,
                String filter, boolean filterIgnoreCase, PagingRequest pagingRequest,
                boolean sortOrderAscending)
    {
        ParameterCheck.mandatory("pagingRequest", pagingRequest);
        Long nodeId = (archiveStoreRootNodeRef == null) ? -1 : getNodeId(archiveStoreRootNodeRef);
        Long qnameId = (assocTypeQName == null) ? -1 : getQNameId(assocTypeQName);

        int requestTotalCountMax = pagingRequest.getRequestTotalCountMax();

        GetArchivedNodesCannedQueryParams paramBean = new GetArchivedNodesCannedQueryParams(nodeId,
                    qnameId, filter, filterIgnoreCase, getQNameId(ContentModel.PROP_NAME),
                    sortOrderAscending);

        // page details
        CannedQueryPageDetails cqpd = new CannedQueryPageDetails(pagingRequest.getSkipCount(),
                    pagingRequest.getMaxItems(), CannedQueryPageDetails.DEFAULT_PAGE_NUMBER,
                    CannedQueryPageDetails.DEFAULT_PAGE_COUNT);

        // create query params holder
        CannedQueryParameters params = new CannedQueryParameters(paramBean, cqpd, null,
                    requestTotalCountMax, pagingRequest.getQueryExecutionId());

        // return canned query instance
        return getCannedQuery(params);
    }
}
