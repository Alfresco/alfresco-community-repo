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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.repo.discussion.cannedqueries.NodeWithChildrenEntity.NameAndCreatedAt;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.security.permissions.impl.acegi.AbstractCannedQueryPermissions;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides support for {@link CannedQuery canned queries} which
 * filter topics by their posts
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class GetDiscussionTopcisWithPostsCannedQuery extends AbstractCannedQueryPermissions<NodeWithChildrenEntity>
{
    private Log logger = LogFactory.getLog(getClass());
    
    private static final String QUERY_NAMESPACE = "alfresco.query.discussion";
    private static final String QUERY_SELECT_GET_NODES = "select_GetDiscussionTopcisWithPosts";
    
    private final CannedQueryDAO cannedQueryDAO;
    
    public GetDiscussionTopcisWithPostsCannedQuery(
            CannedQueryDAO cannedQueryDAO,
            MethodSecurityBean<NodeWithChildrenEntity> methodSecurity,
            CannedQueryParameters params)
    {
        super(params, methodSecurity);
        this.cannedQueryDAO = cannedQueryDAO;
    }
    
    @Override
    protected List<NodeWithChildrenEntity> queryAndFilter(CannedQueryParameters parameters)
    {
        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);
        
        Object paramBeanObj = parameters.getParameterBean();
        if (paramBeanObj == null)
            throw new NullPointerException("Null GetChildrenAuditable query params");
        
        GetDiscussionTopcisWithPostsCannedQueryParams paramBean = (GetDiscussionTopcisWithPostsCannedQueryParams) paramBeanObj;
        
        boolean filterByTopicCreatedDate = (paramBean.getTopicCreatedAfter() != null);
        boolean filterByPostCreatedDate = (paramBean.getPostCreatedAfter() != null);
        
        // note: refer to SQL for specific DB filtering (eg.parent nodes etc)
        List<NodeWithChildrenEntity> results = cannedQueryDAO.executeQuery(QUERY_NAMESPACE, QUERY_SELECT_GET_NODES, paramBean, 0, Integer.MAX_VALUE);
        
        // Filter
        List<NodeWithChildrenEntity> filtered = new ArrayList<NodeWithChildrenEntity>(results.size());
        for (NodeWithChildrenEntity result : results)
        {
            // Filter by topic date
           if (filterByTopicCreatedDate)
           {
               Date createdDate = DefaultTypeConverter.INSTANCE.convert(Date.class, result.getCreatedDate());
               if (createdDate.before(paramBean.getTopicCreatedAfter()))
               {
                  // Created too early
                  continue;
               }
           }
           
           // Filter by post date
           if (filterByPostCreatedDate)
           {
              List<NameAndCreatedAt> wantedPosts = new ArrayList<NameAndCreatedAt>();
              for (NameAndCreatedAt post : result.getChildren())
              {
                 Date createdDate = DefaultTypeConverter.INSTANCE.convert(Date.class, post.getCreatedAt());
                 if (createdDate.before(paramBean.getPostCreatedAfter()))
                 {
                    // Created too early
                    continue;
                 }
                 else
                 {
                    wantedPosts.add(post);
                 }
              }
              result.setChildren(wantedPosts);
           }
           
           // If required, filter out the primary post
           if (paramBean.getExcludePrimaryPost())
           {
              List<NameAndCreatedAt> wantedPosts = new ArrayList<NameAndCreatedAt>();
              for (NameAndCreatedAt post : result.getChildren())
              {
                 if (post.getName().equals( result.getName() ))
                 {
                    // Primary post, skip
                    continue;
                 }
                 else
                 {
                    wantedPosts.add(post);
                 }
              }
              result.setChildren(wantedPosts);
           }
           
           // Ignore any topic with no posts
           if (result.getChildren().size() == 0)
           {
              // No valid posts
              continue;
           }
           
           // If we get here, the topic is of interest
           filtered.add(result);
        }
        
        // Sort by the result count, and then the created date
        Collections.sort(filtered, new Comparator<NodeWithChildrenEntity>() {
           @Override
           public int compare(NodeWithChildrenEntity o1, NodeWithChildrenEntity o2) 
           {
              int res = o2.getChildren().size() - o1.getChildren().size();
              if (res == 0)
              {
                 res = o2.getCreatedDate().compareTo(o1.getCreatedDate());
              }
              return res;
           }
        });

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
