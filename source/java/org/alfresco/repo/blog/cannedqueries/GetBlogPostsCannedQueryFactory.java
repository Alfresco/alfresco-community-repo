package org.alfresco.repo.blog.cannedqueries;

import java.util.Date;

import org.alfresco.model.BlogIntegrationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.service.cmr.blog.BlogPostInfo;
import org.alfresco.service.cmr.blog.BlogService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;

/**
 * A {@link CannedQueryFactory} for various queries relating to {@link BlogPostInfo blog-posts}.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 * 
 * @see BlogService#getDrafts(NodeRef, String, PagingRequest)
 * @see BlogService#getPublished(NodeRef, Date, Date, String, PagingRequest)
 */
public class GetBlogPostsCannedQueryFactory extends AbstractBlogPostsCannedQueryFactory
{
    @Override
    public CannedQuery<BlogEntity> getCannedQuery(CannedQueryParameters parameters)
    {
        final GetBlogPostsCannedQuery cq = new GetBlogPostsCannedQuery(cannedQueryDAO, methodSecurity, parameters);
        
        return (CannedQuery<BlogEntity>) cq;
    }
    
    public CannedQuery<BlogEntity> getGetDraftsCannedQuery(NodeRef blogContainerNode, String username, PagingRequest pagingReq)
    {
        ParameterCheck.mandatory("blogContainerNode", blogContainerNode);
        ParameterCheck.mandatory("pagingReq", pagingReq);
        
        int requestTotalCountMax = pagingReq.getRequestTotalCountMax();
        
        //FIXME Need tenant service like for GetChildren?
        boolean isPublished = false;
        GetBlogPostsCannedQueryParams paramBean = new GetBlogPostsCannedQueryParams(getNodeId(blogContainerNode),
                                                                                    getQNameId(ContentModel.PROP_NAME),
                                                                                    getQNameId(ContentModel.PROP_PUBLISHED),
                                                                                    getQNameId(ContentModel.TYPE_CONTENT),
                                                                                    username,
                                                                                    isPublished,
                                                                                    null, null,
                                                                                    null, null);
        
        CannedQueryPageDetails cqpd = createCQPageDetails(pagingReq);
        CannedQuerySortDetails cqsd = createCQSortDetails(ContentModel.PROP_CREATED, SortOrder.DESCENDING);
        
        // create query params holder
        CannedQueryParameters params = new CannedQueryParameters(paramBean, cqpd, cqsd, requestTotalCountMax, pagingReq.getQueryExecutionId());
        
        // return canned query instance
        return getCannedQuery(params);
    }
    
    public CannedQuery<BlogEntity> getGetPublishedExternallyCannedQuery(NodeRef blogContainerNode, PagingRequest pagingReq)
    {
        ParameterCheck.mandatory("blogContainerNode", blogContainerNode);
        ParameterCheck.mandatory("pagingReq", pagingReq);
        
        int requestTotalCountMax = pagingReq.getRequestTotalCountMax();
        
        boolean isPublished = true;
        
        Long blogIntAspectQNameId = getQNameId(BlogIntegrationModel.ASPECT_BLOG_POST);
        if (blogIntAspectQNameId == null)
        {
            // possible if no blogs have ever been published externally
            blogIntAspectQNameId = -1L; // run the query but should return empty results
        }
        
        // published externally if it has the BLOG_POST aspect
        GetBlogPostsCannedQueryParams paramBean = new GetBlogPostsCannedQueryParams(getNodeId(blogContainerNode),
                                                                                    getQNameId(ContentModel.PROP_NAME),
                                                                                    getQNameId(ContentModel.PROP_PUBLISHED),
                                                                                    getQNameId(ContentModel.TYPE_CONTENT),
                                                                                    null,
                                                                                    isPublished,
                                                                                    null, null,
                                                                                    blogIntAspectQNameId,
                                                                                    getQNameId(BlogIntegrationModel.PROP_POSTED));
        
        CannedQueryPageDetails cqpd = createCQPageDetails(pagingReq);
        CannedQuerySortDetails cqsd = createCQSortDetails(BlogIntegrationModel.PROP_POSTED, SortOrder.DESCENDING);
        
        // create query params holder
        CannedQueryParameters params = new CannedQueryParameters(paramBean, cqpd, cqsd, requestTotalCountMax, pagingReq.getQueryExecutionId());
        
        // return canned query instance
        return getCannedQuery(params);
    }
    
    public CannedQuery<BlogEntity> getGetPublishedCannedQuery(NodeRef blogContainerNode, Date fromDate, Date toDate, String byUser, PagingRequest pagingReq)
    {
        ParameterCheck.mandatory("blogContainerNode", blogContainerNode);
        ParameterCheck.mandatory("pagingReq", pagingReq);
        
        int requestTotalCountMax = pagingReq.getRequestTotalCountMax();
        
        boolean isPublished = true;
        GetBlogPostsCannedQueryParams paramBean = new GetBlogPostsCannedQueryParams(getNodeId(blogContainerNode),
                                                                                    getQNameId(ContentModel.PROP_NAME),
                                                                                    getQNameId(ContentModel.PROP_PUBLISHED),
                                                                                    getQNameId(ContentModel.TYPE_CONTENT),
                                                                                    byUser,
                                                                                    isPublished,
                                                                                    fromDate, toDate,
                                                                                    null, null);
        
        CannedQueryPageDetails cqpd = createCQPageDetails(pagingReq);
        CannedQuerySortDetails cqsd = createCQSortDetails(ContentModel.PROP_PUBLISHED, SortOrder.DESCENDING);
        
        // create query params holder
        CannedQueryParameters params = new CannedQueryParameters(paramBean, cqpd, cqsd, requestTotalCountMax, pagingReq.getQueryExecutionId());
        
        // return canned query instance
        return getCannedQuery(params);
    }
}
