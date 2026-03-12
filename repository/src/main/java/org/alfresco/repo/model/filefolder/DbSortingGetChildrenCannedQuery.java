/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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
package org.alfresco.repo.model.filefolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQueryResults;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodePropertyHelper;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.repo.node.getchildren.FilterSortNodeEntity;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQueryParams;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

public class DbSortingGetChildrenCannedQuery extends GetChildrenCannedQuery
{

    private static final Log LOG = LogFactory.getLog(DbSortingGetChildrenCannedQuery.class);
    private static final String QUERY_SELECT_GET_CHILDREN_WITH_PROPS_SORTED = "select_GetChildrenCannedQueryWithPropsSorted";

    boolean wasUsed = false;
    int totalSeenCount;
    int skipped;
    int seenAfterRequiredCountSatisfied = 0;

    public DbSortingGetChildrenCannedQuery(NodeDAO nodeDAO, QNameDAO qnameDAO, CannedQueryDAO cannedQueryDAO, NodePropertyHelper nodePropertyHelper, TenantService tenantService, NodeService nodeService, MethodSecurityBean<NodeRef> methodSecurity, CannedQueryParameters params, HiddenAspect hiddenAspect, DictionaryService dictionaryService, Set<QName> ignoreAspectQNames)
    {
        super(nodeDAO, qnameDAO, cannedQueryDAO, nodePropertyHelper, tenantService, nodeService, methodSecurity, params, hiddenAspect, dictionaryService, ignoreAspectQNames);
    }

    @Override
    protected List<NodeRef> executeQuery(List<FilterProp> filterProps, List<Pair<QName, CannedQuerySortDetails.SortOrder>> sortPairs, FilterSortNodeEntity params, GetChildrenCannedQueryParams paramBean)
    {
        if (filterProps.isEmpty() && isDefaultSorting(sortPairs))
        {
            wasUsed = true;
            LOG.info("Executing DB sorting get children canned query for " + params.getParentNodeId() + " with default sorting and no filters");
            CannedQueryPageDetails pageDetails = parameters.getPageDetails();
            int requestedCount = pageDetails.getPageSize();

            Set<QName> folderQNames = nodePropertyHelper.buildFolderTypes();
            Set<Long> folderTypeQNameIds = qnameDAO.convertQNamesToIds(folderQNames, false);
            params.setFolderTypeQNameIds(folderTypeQNameIds);

            final List<FilterSortNode> children = new ArrayList<>(requestedCount);
            final PagedFilterSortChildQueryCallback callback = new PagedFilterSortChildQueryCallback(children, pageDetails);
            FilterSortResultHandler resultHandler = new FilterSortResultHandler(callback);
            cannedQueryDAO.executeQuery(QUERY_NAMESPACE, QUERY_SELECT_GET_CHILDREN_WITH_PROPS_SORTED, params, 0, Integer.MAX_VALUE, resultHandler);
            resultHandler.done();

            List<NodeRef> result = new ArrayList<>(children.size());
            for (FilterSortNode child : children)
            {
                result.add(tenantService.getBaseName(child.getNodeRef()));
            }

            LOG.info(children.size() + " children found for " + params.getParentNodeId() + " total seen: " + totalSeenCount + " skipped: " + skipped + " seen after required count satisfied: " + seenAfterRequiredCountSatisfied);
            return result;
        }
        else
        {
            LOG.info("Fallback to super executeQuery");
            return super.executeQuery(filterProps, sortPairs, params, paramBean);
        }
    }

    @Override
    protected boolean isApplyPostQueryPermissions()
    {
        if (wasUsed)
        {
            return true;
        }
        return super.isApplyPostQueryPermissions();

    }

    @Override
    protected boolean isApplyPostQueryPaging()
    {
        if (wasUsed)
        {
            return false;
        }
        return super.isApplyPostQueryPaging();
    }

    @Override
    protected CannedQueryResults<NodeRef> createCannedQueryResults(List<List<NodeRef>> finalPages, List<NodeRef> rawResults)
    {
        if (!wasUsed)
        {
            return super.createCannedQueryResults(finalPages, rawResults);
        }
        return new CannedQueryResults<>() {
            @Override
            public CannedQuery<NodeRef> getOriginatingQuery()
            {
                return DbSortingGetChildrenCannedQuery.this;
            }

            @Override
            public String getQueryExecutionId()
            {
                return queryExecutionId;
            }

            @Override
            public Pair<Integer, Integer> getTotalResultCount()
            {
                if (parameters.getTotalResultCountMax() > 0)
                {
                    return new Pair<>(totalSeenCount, totalSeenCount);
                }
                else
                {
                    throw new IllegalStateException("Total results were not requested in parameters.");
                }
            }

            @Override
            public int getPagedResultCount()
            {
                return rawResults.size();
            }

            @Override
            public int getPageCount()
            {
                return 1;
            }

            @Override
            public NodeRef getSingleResult()
            {
                if (rawResults.size() != 1)
                {
                    throw new IllegalStateException("There must be exactly one page of one result available.");
                }
                return rawResults.getFirst();
            }

            @Override
            public List<NodeRef> getPage()
            {
                return rawResults;
            }

            @Override
            public List<List<NodeRef>> getPages()
            {
                return finalPages;
            }

            @Override
            public boolean hasMoreItems()
            {
                return seenAfterRequiredCountSatisfied > 0;
            }
        };
    }

    /**
     * See: NodesImpl.getListChildrenSortPropsDefault()
     */
    private boolean isDefaultSorting(List<Pair<QName, CannedQuerySortDetails.SortOrder>> sortPairs)
    {
        return sortPairs.size() == 2
                && sortPairs.contains(new Pair<>(org.alfresco.repo.node.getchildren.GetChildrenCannedQuery.SORT_QNAME_NODE_IS_FOLDER, CannedQuerySortDetails.SortOrder.DESCENDING))
                && sortPairs.contains(new Pair<>(ContentModel.PROP_NAME, CannedQuerySortDetails.SortOrder.ASCENDING));
    }

    protected class PagedFilterSortChildQueryCallback implements FilterSortChildQueryCallback
    {
        private final List<FilterSortNode> children;
        private final int skipResults;
        private final int pageSize;

        public PagedFilterSortChildQueryCallback(List<FilterSortNode> children, CannedQueryPageDetails pageDetails)
        {
            this.children = children;
            this.skipResults = pageDetails.getSkipResults();
            this.pageSize = pageDetails.getPageSize();
        }

        @Override
        public boolean handle(FilterSortNode node)
        {
            if (includeImpl(true, node.getNodeRef()))
            {
                totalSeenCount++;
                if (skipped < skipResults)
                {
                    skipped++;
                    return true;
                }
                if (children.size() < pageSize)
                {
                    children.add(node);
                }
                else
                {
                    seenAfterRequiredCountSatisfied++;
                }
            }
            return true;
        }
    }
}
