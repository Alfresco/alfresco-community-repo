package org.alfresco.repo.blog.cannedqueries;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;

/**
 * A {@link CannedQueryFactory} for the creation of {@link DraftsAndPublishedBlogPostsCannedQuery}s.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 */
public class DraftsAndPublishedBlogPostsCannedQueryFactory extends AbstractBlogPostsCannedQueryFactory
{
    @Override
    public CannedQuery<BlogEntity> getCannedQuery(CannedQueryParameters parameters)
    {
        final DraftsAndPublishedBlogPostsCannedQuery cq = new DraftsAndPublishedBlogPostsCannedQuery(
                cannedQueryDAO,
                methodSecurity,
                parameters);
        return (CannedQuery<BlogEntity>) cq;
    }
    
    public CannedQuery<BlogEntity> getCannedQuery(NodeRef blogContainerNode, Date fromDate, Date toDate, String byUser, PagingRequest pagingReq)
    {
        ParameterCheck.mandatory("blogContainerNode", blogContainerNode);
        ParameterCheck.mandatory("pagingReq", pagingReq);
        
        int requestTotalCountMax = pagingReq.getRequestTotalCountMax();
        
        //FIXME Need tenant service like for GetChildren?
        DraftsAndPublishedBlogPostsCannedQueryParams paramBean = new DraftsAndPublishedBlogPostsCannedQueryParams(
                                                                                    getNodeId(blogContainerNode),
                                                                                    getQNameId(ContentModel.PROP_NAME),
                                                                                    getQNameId(ContentModel.PROP_PUBLISHED),
                                                                                    getQNameId(ContentModel.TYPE_CONTENT),
                                                                                    byUser,
                                                                                    fromDate, toDate);
        
        CannedQueryPageDetails cqpd = createCQPageDetails(pagingReq);
        
        List<Pair<QName, Boolean>> sortPairs = new ArrayList<Pair<QName, Boolean>>(2);
        
        // Sort by created then published. We want a list of all published (most recently published first),
        //                                 followed by all unpublished (most recently created first)
        sortPairs.add(new Pair<QName, Boolean>(ContentModel.PROP_CREATED, Boolean.FALSE));
        sortPairs.add(new Pair<QName, Boolean>(ContentModel.PROP_PUBLISHED, Boolean.FALSE));
        
        CannedQuerySortDetails cqsd = createCQSortDetails(sortPairs);
        
        // create query params holder
        CannedQueryParameters params = new CannedQueryParameters(paramBean, cqpd, cqsd, requestTotalCountMax, pagingReq.getQueryExecutionId());
        
        // return canned query instance
        return getCannedQuery(params);
    }
}
