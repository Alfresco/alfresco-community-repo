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
import java.util.Date;
import java.util.List;

import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.query.NodeBackedEntity;
import org.alfresco.repo.query.AbstractQNameAwareCannedQueryFactory.NestedComparator;
import org.alfresco.repo.query.AbstractQNameAwareCannedQueryFactory.NodeBackedEntityComparator;
import org.alfresco.repo.security.permissions.impl.acegi.AbstractCannedQueryPermissions;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides support for {@link CannedQuery canned queries} which
 * filter by Auditable Properties
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class GetChildrenAuditableCannedQuery extends AbstractCannedQueryPermissions<NodeBackedEntity>
{
    private Log logger = LogFactory.getLog(getClass());
    
    private static final String QUERY_NAMESPACE = "alfresco.query.auditable";
    private static final String QUERY_SELECT_GET_NODES = "select_GetChildrenAuditableCannedQuery";
    
    private final CannedQueryDAO cannedQueryDAO;
    
    public GetChildrenAuditableCannedQuery(
            CannedQueryDAO cannedQueryDAO,
            MethodSecurityBean<NodeBackedEntity> methodSecurity,
            CannedQueryParameters params)
    {
        super(params, methodSecurity);
        this.cannedQueryDAO = cannedQueryDAO;
    }
    
    @Override
    protected List<NodeBackedEntity> queryAndFilter(CannedQueryParameters parameters)
    {
        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);
        
        Object paramBeanObj = parameters.getParameterBean();
        if (paramBeanObj == null)
            throw new NullPointerException("Null GetChildrenAuditable query params");
        
        GetChildrenAuditableCannedQueryParams paramBean = (GetChildrenAuditableCannedQueryParams) paramBeanObj;
        
        boolean filterByCreator = (paramBean.getCreatorFilter() != null);
        boolean filterByModifier = (paramBean.getModifierFilter() != null);
        boolean filterByCreatedDate = (paramBean.getCreatedBefore() != null && paramBean.getCreatedAfter() != null);
        boolean filterByModifiedDate = (paramBean.getModifiedBefore() != null && paramBean.getModifiedAfter() != null);
        
        // note: refer to SQL for specific DB filtering (eg.parent nodes etc)
        List<NodeBackedEntity> results = cannedQueryDAO.executeQuery(QUERY_NAMESPACE, QUERY_SELECT_GET_NODES, paramBean, 0, Integer.MAX_VALUE);
        
        // TODO Should this be case insensitive?
        List<NodeBackedEntity> filtered = new ArrayList<NodeBackedEntity>(results.size());
        for (NodeBackedEntity result : results)
        {
            boolean nextNodeIsAcceptable = true;
            
            // Creator/Modifier filtering
            if(filterByCreator || filterByModifier)
            {
               String creator = result.getCreator();
               String modifier = result.getModifier();
               if(modifier == null)
               {
                  modifier = creator;
               }
               
               if(filterByCreator)
               {
                  if(! paramBean.getCreatorFilter().equals(creator))
                  {
                     nextNodeIsAcceptable = false;
                  }
               }
               if(filterByModifier)
               {
                  if(! paramBean.getModifierFilter().equals(modifier))
                  {
                     nextNodeIsAcceptable = false;
                  }
               }
            }
            
            // Date filtering
            if(filterByCreatedDate || filterByModifiedDate)
            {
               Date createdDate = DefaultTypeConverter.INSTANCE.convert(Date.class, result.getCreatedDate()); 
               Date modifiedDate = DefaultTypeConverter.INSTANCE.convert(Date.class, result.getModifiedDate());
               if(modifiedDate == null)
               {
                  modifiedDate = createdDate;
               }
               
               if(filterByCreatedDate)
               {
                  if(createdDate.before(paramBean.getCreatedAfter()) ||
                     createdDate.after(paramBean.getCreatedBefore()))
                  {
                     // Outside period
                     nextNodeIsAcceptable = false;
                  }
               }
               if(filterByModifiedDate)
               {
                  if(modifiedDate.before(paramBean.getModifiedAfter()) ||
                     modifiedDate.after(paramBean.getModifiedBefore()))
                  {
                     // Outside period
                     nextNodeIsAcceptable = false;
                  }
               }
            }
            
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
