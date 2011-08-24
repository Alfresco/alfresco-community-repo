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
package org.alfresco.repo.discussion.cannedqueries;

import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.query.AbstractQNameAwareCannedQueryFactory;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;

/**
 * A {@link CannedQueryFactory} for various queries relating to getting
 * Topics with some information on their Posts
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class GetDiscussionTopcisWithPostsCannedQueryFactory extends AbstractQNameAwareCannedQueryFactory<NodeWithChildrenEntity>
{
    @Override
    public void afterPropertiesSet() throws Exception
    {
        super.afterPropertiesSet();
    }
    
    @Override
    public CannedQuery<NodeWithChildrenEntity> getCannedQuery(CannedQueryParameters parameters)
    {
        final GetDiscussionTopcisWithPostsCannedQuery cq = new GetDiscussionTopcisWithPostsCannedQuery(
              cannedQueryDAO, methodSecurity, parameters
        );
        
        return (CannedQuery<NodeWithChildrenEntity>) cq;
    }
    
    public CannedQuery<NodeWithChildrenEntity> getCannedQuery(NodeRef parentNodeRef, 
          Date topicCreatedFrom, Date postCreatedFrom, boolean excludePrimaryPosts,
          CannedQuerySortDetails sortDetails, PagingRequest pagingReq)
    {
        ParameterCheck.mandatory("parentNodeRef", parentNodeRef);
        ParameterCheck.mandatory("pagingReq", pagingReq);
        
        int requestTotalCountMax = pagingReq.getRequestTotalCountMax();
        
        //FIXME Need tenant service like for GetChildren?
        GetDiscussionTopcisWithPostsCannedQueryParams paramBean = new GetDiscussionTopcisWithPostsCannedQueryParams(
              getNodeId(parentNodeRef), 
              getQNameId(ContentModel.PROP_NAME),
              getQNameId(ForumModel.TYPE_TOPIC),
              getQNameId(ForumModel.TYPE_POST),
              topicCreatedFrom, postCreatedFrom,
              excludePrimaryPosts
        );
        
        CannedQueryPageDetails cqpd = createCQPageDetails(pagingReq);
        
        // create query params holder
        CannedQueryParameters params = new CannedQueryParameters(
              paramBean, cqpd, sortDetails, requestTotalCountMax, pagingReq.getQueryExecutionId());
        
        // return canned query instance
        return getCannedQuery(params);
    }
}
