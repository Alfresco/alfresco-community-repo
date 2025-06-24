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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.repo.blog.cannedqueries.AbstractBlogPostsCannedQueryFactory.BlogEntityComparator;
import org.alfresco.repo.domain.node.AuditablePropertiesEntity;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.security.permissions.impl.acegi.AbstractCannedQueryPermissions;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.service.cmr.blog.BlogService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * This class provides support for several {@link CannedQuery canned queries} used by the {@link BlogService}.
 * 
 * @author Neil Mc Erlean, janv
 * @since 4.0
 */
public class GetBlogPostsCannedQuery extends AbstractCannedQueryPermissions<BlogEntity>
{
    private Log logger = LogFactory.getLog(getClass());

    private static final String QUERY_NAMESPACE = "alfresco.query.blogs";
    private static final String QUERY_SELECT_GET_BLOGS = "select_GetBlogsCannedQuery";

    private final CannedQueryDAO cannedQueryDAO;

    public GetBlogPostsCannedQuery(
            CannedQueryDAO cannedQueryDAO,
            MethodSecurityBean<BlogEntity> methodSecurity,
            CannedQueryParameters params)
    {
        super(params, methodSecurity);
        this.cannedQueryDAO = cannedQueryDAO;
    }

    @Override
    protected List<BlogEntity> queryAndFilter(CannedQueryParameters parameters)
    {
        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);

        Object paramBeanObj = parameters.getParameterBean();
        if (paramBeanObj == null)
            throw new NullPointerException("Null GetBlogPosts query params");

        GetBlogPostsCannedQueryParams paramBean = (GetBlogPostsCannedQueryParams) paramBeanObj;
        String requestedCreator = paramBean.getCmCreator();
        boolean isPublished = paramBean.getIsPublished();
        Date publishedFromDate = paramBean.getPublishedFromDate();
        Date publishedToDate = paramBean.getPublishedToDate();

        // note: refer to SQL for specific DB filtering (eg.parent node and optionally blog integration aspect, etc)
        List<BlogEntity> results = cannedQueryDAO.executeQuery(QUERY_NAMESPACE, QUERY_SELECT_GET_BLOGS, paramBean, 0, Integer.MAX_VALUE);

        List<BlogEntity> filtered = new ArrayList<BlogEntity>(results.size());
        for (BlogEntity result : results)
        {
            boolean nextNodeIsAcceptable = true;

            Date actualPublishedDate = DefaultTypeConverter.INSTANCE.convert(Date.class, result.getPublishedDate());

            // Only return blog-posts whose cm:published status matches that requested (ie. a blog "is published" when published date is not null)
            boolean blogIsPublished = (actualPublishedDate != null);
            if (blogIsPublished != isPublished)
            {
                nextNodeIsAcceptable = false;
            }

            // Only return blog posts whose creator matches the given username, if there is one.
            if (requestedCreator != null)
            {
                AuditablePropertiesEntity auditProps = result.getNode().getAuditableProperties();
                if ((auditProps == null) || (!requestedCreator.equals(auditProps.getAuditCreator())))
                {
                    nextNodeIsAcceptable = false;
                }
            }

            // Only return blogs published within the specified dates
            if ((publishedFromDate != null) || (publishedToDate != null))
            {
                if (actualPublishedDate != null)
                {
                    if (publishedFromDate != null && actualPublishedDate.before(publishedFromDate))
                    {
                        nextNodeIsAcceptable = false;
                    }
                    if (publishedToDate != null && actualPublishedDate.after(publishedToDate))
                    {
                        nextNodeIsAcceptable = false;
                    }
                }
                else
                {
                    nextNodeIsAcceptable = false;
                }
            }

            if (nextNodeIsAcceptable)
            {
                filtered.add(result);
            }
        }

        List<Pair<? extends Object, SortOrder>> sortPairs = parameters.getSortDetails().getSortPairs();

        // For now, the BlogService only sorts by a single property.
        if (sortPairs != null && !sortPairs.isEmpty())
        {
            Pair<? extends Object, SortOrder> sortPair = sortPairs.get(0);

            QName sortProperty = (QName) sortPair.getFirst();
            Comparator<BlogEntity> comparator = new BlogEntityComparator(sortProperty);

            if (sortPair.getSecond() == SortOrder.DESCENDING)
            {
                comparator = Collections.reverseOrder(comparator);
            }
            Collections.sort(filtered, comparator);
        }

        if (start != null)
        {
            logger.debug("Base query: " + filtered.size() + " in " + (System.currentTimeMillis() - start) + " msecs");
        }

        return filtered;
    }

    @Override
    protected boolean isApplyPostQuerySorting()
    {
        // No post-query sorting. It's done within the queryAndFilter() method above.
        return false;
    }
}
