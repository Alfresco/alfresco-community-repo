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

package org.alfresco.repo.doclink;

import java.util.Collections;
import java.util.List;

import org.alfresco.query.AbstractCannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Parameter object for {@link GetDoclinkNodesCannedQuery}.
 * 
 * @author Ramona Popa
 * @since 5.2.1
 */

public class GetDoclinkNodesCannedQuery extends AbstractCannedQuery<Long>
{
    private Log logger = LogFactory.getLog(GetDoclinkNodesCannedQuery.class);

    private static final String QUERY_NAMESPACE = "alfresco.query.doclinks";
    private static final String QUERY_SELECT_GET_DOCLINK_NODES = "select_GetDoclinkNodesCannedQuery";

    private CannedQueryDAO cannedQueryDAO;

    public GetDoclinkNodesCannedQuery(CannedQueryDAO cannedQueryDAO, CannedQueryParameters params)
    {
        super(params);
        this.cannedQueryDAO = cannedQueryDAO;
    }

    @Override
    protected List<Long> queryAndFilter(CannedQueryParameters parameters)
    {
        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);

        Object paramBeanObj = parameters.getParameterBean();
        if (paramBeanObj == null)
        {
            throw new NullPointerException("Null GetDoclinkNodes query params");
        }

        // Get parameters
        GetDoclinkNodesCannedQueryParams paramBean = (GetDoclinkNodesCannedQueryParams) paramBeanObj;

        if (paramBean.getParentNodeStringValue() == null)
        {
            return Collections.emptyList();
        }

        List<Long> nodeIds = cannedQueryDAO.executeQuery(QUERY_NAMESPACE, QUERY_SELECT_GET_DOCLINK_NODES, paramBean, 0, paramBean.getLimit());

        // preload the node for later when we want to get the properties of the node
        if (logger.isDebugEnabled())
        {
            if (start != null)
            {
                logger.debug("Base query: " + nodeIds.size() + " in " + (System.currentTimeMillis() - start) + " msecs");
            }
            if (paramBean.getLimit() == nodeIds.size())
            {
                logger.debug("Node " + paramBean.getParentNodeStringValue()+ " has at least 100,000 link nodes attached. Results have been truncated.");
            }
        }

        return nodeIds;
    }

    @Override
    protected boolean isApplyPostQuerySorting()
    {
        // No post-query sorting
        return false;
    }
}
