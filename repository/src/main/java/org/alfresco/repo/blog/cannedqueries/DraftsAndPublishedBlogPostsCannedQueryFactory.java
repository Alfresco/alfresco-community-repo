/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
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

        // FIXME Need tenant service like for GetChildren?
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
        // followed by all unpublished (most recently created first)
        sortPairs.add(new Pair<QName, Boolean>(ContentModel.PROP_CREATED, Boolean.FALSE));
        sortPairs.add(new Pair<QName, Boolean>(ContentModel.PROP_PUBLISHED, Boolean.FALSE));

        CannedQuerySortDetails cqsd = createCQSortDetails(sortPairs);

        // create query params holder
        CannedQueryParameters params = new CannedQueryParameters(paramBean, cqpd, cqsd, requestTotalCountMax, pagingReq.getQueryExecutionId());

        // return canned query instance
        return getCannedQuery(params);
    }
}
