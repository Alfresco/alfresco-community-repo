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
package org.alfresco.repo.node.getchildren;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.query.NodeBackedEntity;
import org.alfresco.repo.query.NodeWithTargetsEntity;
import org.alfresco.repo.query.AbstractQNameAwareCannedQueryFactory.NestedComparator;
import org.alfresco.repo.query.AbstractQNameAwareCannedQueryFactory.NodeBackedEntityComparator;
import org.alfresco.repo.security.permissions.impl.acegi.AbstractCannedQueryPermissions;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides support for {@link CannedQuery canned queries} which
 * filter by Auditable Properties and Target Assocs
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class GetChildrenWithTargetAssocsAuditableCannedQuery extends AbstractCannedQueryPermissions<NodeWithTargetsEntity>
{
    private Log logger = LogFactory.getLog(getClass());
    
    private static final String QUERY_NAMESPACE = "alfresco.query.auditable";
    private static final String QUERY_SELECT_GET_NODES = "select_GetChildrenWithTargetAssocsAuditableCannedQuery";
    
    private final CannedQueryDAO cannedQueryDAO;
    
    public GetChildrenWithTargetAssocsAuditableCannedQuery(
            CannedQueryDAO cannedQueryDAO,
            MethodSecurityBean<NodeWithTargetsEntity> methodSecurity,
            CannedQueryParameters params)
    {
        super(params, methodSecurity);
        this.cannedQueryDAO = cannedQueryDAO;
    }
    
    @Override
    protected List<NodeWithTargetsEntity> queryAndFilter(CannedQueryParameters parameters)
    {
        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);
        
        Object paramBeanObj = parameters.getParameterBean();
        if (paramBeanObj == null)
            throw new NullPointerException("Null GetChildrenWithTargetAssocsAuditable query params");
        
        GetChildrenWithTargetAssocsAuditableCannedQueryParams paramBean = (GetChildrenWithTargetAssocsAuditableCannedQueryParams) paramBeanObj;
        
        // note: refer to SQL for specific DB filtering (eg.parent nodes etc)
        List<NodeWithTargetsEntity> results = cannedQueryDAO.executeQuery(QUERY_NAMESPACE, QUERY_SELECT_GET_NODES, paramBean, 0, Integer.MAX_VALUE);
        
        List<NodeWithTargetsEntity> filtered = new ArrayList<NodeWithTargetsEntity>(results.size());
        for (NodeWithTargetsEntity result : results)
        {
            boolean nextNodeIsAcceptable = true;
            
            // Note - all filtering is currently done in the database
            
            // Did it make the cut
            if (nextNodeIsAcceptable)
            {
                filtered.add(result);
            }
        }
        
        List<Pair<? extends Object, SortOrder>> sortPairs = parameters.getSortDetails().getSortPairs();
        
        // Do the sorting
        if (sortPairs != null && !sortPairs.isEmpty())
        {
            List<Pair<Comparator<NodeBackedEntity>, SortOrder>> comparators =
               new ArrayList<Pair<Comparator<NodeBackedEntity>,SortOrder>>();
            for(Pair<? extends Object, SortOrder> sortPair : sortPairs)
            {
               final QName sortProperty = (QName)sortPair.getFirst();
               final NodeBackedEntityComparator comparator = new NodeBackedEntityComparator(sortProperty);
               comparators.add(new Pair<Comparator<NodeBackedEntity>, SortOrder>(comparator, sortPair.getSecond()));
            }
            NestedComparator<NodeBackedEntity> comparator = new NestedComparator<NodeBackedEntity>(comparators);
            
            // Sort
            Collections.sort(filtered, comparator); 
        }
        
        if (start != null)
        {
            logger.debug("Base query: "+filtered.size()+" in "+(System.currentTimeMillis()-start)+" msecs");
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
