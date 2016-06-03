package org.alfresco.repo.node.getchildren;

import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.query.AbstractQNameAwareCannedQueryFactory;
import org.alfresco.repo.query.NodeWithTargetsEntity;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

/**
 * A {@link CannedQueryFactory} for various queries relating to getting
 * {@link NodeWithTargetsEntity} entires filtering by auditable properties.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class GetChildrenWithTargetAssocsAuditableCannedQueryFactory extends AbstractQNameAwareCannedQueryFactory<NodeWithTargetsEntity>
{
    @Override
    public void afterPropertiesSet() throws Exception
    {
        super.afterPropertiesSet();
    }
    
    @Override
    public CannedQuery<NodeWithTargetsEntity> getCannedQuery(CannedQueryParameters parameters)
    {
        final GetChildrenWithTargetAssocsAuditableCannedQuery cq = new GetChildrenWithTargetAssocsAuditableCannedQuery(
              cannedQueryDAO, methodSecurity, parameters
        );
        
        return (CannedQuery<NodeWithTargetsEntity>) cq;
    }
    
    public CannedQuery<NodeWithTargetsEntity> getCannedQuery(NodeRef parentNodeRef, 
          QName contentType, QName assocType,
          CannedQuerySortDetails sortDetails, PagingRequest pagingReq)
    {
        ParameterCheck.mandatory("parentNodeRef", parentNodeRef);
        ParameterCheck.mandatory("contentType", contentType);
        ParameterCheck.mandatory("pagingReq", pagingReq);
        
        int requestTotalCountMax = pagingReq.getRequestTotalCountMax();
        
        //FIXME Need tenant service like for GetChildren?
        GetChildrenWithTargetAssocsAuditableCannedQueryParams paramBean = new GetChildrenWithTargetAssocsAuditableCannedQueryParams(
              getNodeId(parentNodeRef), 
              getQNameId(ContentModel.PROP_NAME),
              getQNameId(contentType),
              getQNameId(assocType)
        );
        
        CannedQueryPageDetails cqpd = createCQPageDetails(pagingReq);
        
        // create query params holder
        CannedQueryParameters params = new CannedQueryParameters(
              paramBean, cqpd, sortDetails, requestTotalCountMax, pagingReq.getQueryExecutionId());
        
        // return canned query instance
        return getCannedQuery(params);
    }
}
