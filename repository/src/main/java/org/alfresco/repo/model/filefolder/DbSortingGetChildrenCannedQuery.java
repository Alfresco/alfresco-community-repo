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
import java.util.HashSet;
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

/**
 * GetChildren canned query that performs sorting in the database.
 * <p>
 * This query handles the specific case of default sorting (folders first descending, name ascending).
 */
public class DbSortingGetChildrenCannedQuery extends GetChildrenCannedQuery
{
    private static final Log LOG = LogFactory.getLog(DbSortingGetChildrenCannedQuery.class);
    private static final String QUERY_COUNT_GET_CHILDREN_WITH_PROPS_SORTED = "count_GetChildrenCannedQueryWithPropsSorted";
    private static final String QUERY_SELECT_GET_CHILDREN_WITH_PROPS_SORTED = "select_GetChildrenCannedQueryWithPropsSorted";
    private final DictionaryService dictionaryService;

    private int totalCount;

    public DbSortingGetChildrenCannedQuery(NodeDAO nodeDAO, QNameDAO qnameDAO, CannedQueryDAO cannedQueryDAO, NodePropertyHelper nodePropertyHelper, TenantService tenantService, NodeService nodeService, MethodSecurityBean<NodeRef> methodSecurity, CannedQueryParameters params, HiddenAspect hiddenAspect, DictionaryService dictionaryService, Set<QName> ignoreAspectQNames)
    {
        super(nodeDAO, qnameDAO, cannedQueryDAO, nodePropertyHelper, tenantService, nodeService, methodSecurity, params, hiddenAspect, dictionaryService, ignoreAspectQNames);
        this.dictionaryService = dictionaryService;
    }

    @Override
    protected List<NodeRef> executeQuery(List<FilterProp> filterProps, List<Pair<QName, CannedQuerySortDetails.SortOrder>> sortPairs, FilterSortNodeEntity params, GetChildrenCannedQueryParams paramBean, int filterSortPropCnt)
    {
        addFolderTypes(params);
        LOG.trace("Executing DB sorting get children canned query for " + params.getParentNodeId() + " with default sorting");
        fetchTotalCount(params);
        List<FilterSortNode> children = fetchPagedChildren(params);
        return toNodeRefs(children);
    }

    private void addFolderTypes(FilterSortNodeEntity params)
    {
        Set<QName> folderQNames = new HashSet<>(50);
        folderQNames.addAll(dictionaryService.getSubTypes(ContentModel.TYPE_FOLDER, true));
        folderQNames.add(ContentModel.TYPE_FOLDER);
        Set<Long> folderTypeQNameIds = qnameDAO.convertQNamesToIds(folderQNames, false);
        params.setFolderTypeQNameIds(folderTypeQNameIds);
    }

    private void fetchTotalCount(FilterSortNodeEntity params)
    {
        totalCount = cannedQueryDAO.executeCountQuery(QUERY_NAMESPACE, QUERY_COUNT_GET_CHILDREN_WITH_PROPS_SORTED, params).intValue();
        LOG.trace("Total children count for " + params.getParentNodeId() + ": " + totalCount);
    }

    private List<FilterSortNode> fetchPagedChildren(FilterSortNodeEntity params)
    {
        CannedQueryPageDetails pageDetails = parameters.getPageDetails();
        final List<FilterSortNode> children = new ArrayList<>(100);
        int requestedCount = pageDetails.getPageSize();
        final PagedFilterSortChildQueryCallback callback = new PagedFilterSortChildQueryCallback(children, requestedCount);
        FilterSortResultHandler resultHandler = new FilterSortResultHandler(callback);
        int skipResults = pageDetails.getSkipResults();
        cannedQueryDAO.executeQuery(QUERY_NAMESPACE, QUERY_SELECT_GET_CHILDREN_WITH_PROPS_SORTED, params, skipResults, Integer.MAX_VALUE, resultHandler);
        resultHandler.done();
        LOG.trace(children.size() + " children found for " + params.getParentNodeId() + " total count: " + totalCount);
        return children;
    }

    private List<NodeRef> toNodeRefs(List<FilterSortNode> children)
    {
        List<NodeRef> result = new ArrayList<>(children.size());
        for (FilterSortNode child : children)
        {
            result.add(tenantService.getBaseName(child.getNodeRef()));
        }
        return result;
    }

    @Override
    protected boolean isApplyPostQueryPermissions()
    {
        return true;
    }

    @Override
    protected boolean isApplyPostQueryPaging()
    {
        return false;
    }

    @Override
    protected CannedQueryResults<NodeRef> createCannedQueryResults(List<List<NodeRef>> finalPages, List<NodeRef> rawResults)
    {
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
                    return new Pair<>(totalCount, totalCount);
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
                return rawResults.get(0);
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
                CannedQueryPageDetails pageDetails = parameters.getPageDetails();
                return totalCount > pageDetails.getSkipResults() + pageDetails.getPageSize();
            }
        };
    }

    protected class PagedFilterSortChildQueryCallback implements FilterSortChildQueryCallback
    {
        private final List<FilterSortNode> children;
        private final int requiredCount;

        public PagedFilterSortChildQueryCallback(List<FilterSortNode> children, int requiredCount)
        {
            this.children = children;
            this.requiredCount = requiredCount;
        }

        @Override
        public void handle(FilterSortNode node)
        {
            if (needsMore() && includeImpl(true, node.getNodeRef()))
            {
                children.add(node);
            }
        }

        @Override
        public int remainingNeeded()
        {
            return requiredCount - children.size();
        }
    }
}
