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
package org.alfresco.repo.domain.subscriptions.ibatis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.subscriptions.AbstractSubscriptionsDAO;
import org.alfresco.repo.domain.subscriptions.SubscriptionEntity;
import org.alfresco.repo.domain.subscriptions.SubscriptionNodeEntity;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.subscriptions.PagingFollowingResults;
import org.alfresco.service.cmr.subscriptions.PagingFollowingResultsImpl;
import org.alfresco.service.cmr.subscriptions.PagingSubscriptionResults;
import org.alfresco.service.cmr.subscriptions.PagingSubscriptionResultsImpl;
import org.alfresco.service.cmr.subscriptions.SubscriptionItemTypeEnum;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

public class SubscriptionsDAOImpl extends AbstractSubscriptionsDAO
{
    private static final QName PROP_SYS_NODE_DBID = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "node-dbid");

    private SqlSessionTemplate template;
    private QNameDAO qnameDAO;

    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate)
    {
        this.template = sqlSessionTemplate;
    }

    public final void setQNameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    @Override
    public PagingSubscriptionResults selectSubscriptions(String userId, SubscriptionItemTypeEnum type,
            PagingRequest pagingRequest)
    {
        if (userId == null)
        {
            throw new IllegalArgumentException("User Id may not be null!");
        }

        if (type == null)
        {
            throw new IllegalArgumentException("Type may not be null!");
        }

        NodeRef userNodeRef = getUserNodeRef(userId);
        if (userNodeRef == null)
        {
            throw new IllegalArgumentException("User does not exist!");
        }

        Long dbid = (Long) nodeService.getProperty(userNodeRef, PROP_SYS_NODE_DBID);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userNodeId", dbid);

        int maxItems = (pagingRequest.getMaxItems() < 0 || pagingRequest.getMaxItems() > Integer.MAX_VALUE - 1 ? Integer.MAX_VALUE - 1
                : pagingRequest.getMaxItems() + 1);

        @SuppressWarnings("unchecked")
        List<SubscriptionNodeEntity> nodeList = template.selectList(
                "alfresco.subscriptions.select_Subscriptions", map, new RowBounds(pagingRequest.getSkipCount(),
                        maxItems + 1));

        boolean hasMore = nodeList.size() > maxItems;

        List<NodeRef> result = new ArrayList<NodeRef>(nodeList.size());
        for (SubscriptionNodeEntity sne : nodeList)
        {
            result.add(sne.getNodeRef());
            if (result.size() == pagingRequest.getMaxItems())
            {
                break;
            }
        }

        Integer totalCount = null;
        if (pagingRequest.getRequestTotalCountMax() > 0)
        {
            totalCount = countSubscriptions(userId, type);
        }

        return new PagingSubscriptionResultsImpl(result, hasMore, totalCount);
    }

    @Override
    public int countSubscriptions(String userId, SubscriptionItemTypeEnum type)
    {
        if (userId == null)
        {
            throw new IllegalArgumentException("User Id may not be null!");
        }

        if (type == null)
        {
            throw new IllegalArgumentException("Type may not be null!");
        }

        NodeRef userNodeRef = getUserNodeRef(userId);
        if (userNodeRef == null)
        {
            return 0;
        }

        Long dbid = (Long) nodeService.getProperty(userNodeRef, PROP_SYS_NODE_DBID);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userNodeId", dbid);

        Number count = template.selectOne("alfresco.subscriptions.select_countSubscriptions", map);
        return count == null ? 0 : count.intValue();
    }

    @Override
    public void insertSubscription(String userId, NodeRef node)
    {
        if (userId == null)
        {
            throw new IllegalArgumentException("User Id may not be null!");
        }

        if (node == null)
        {
            throw new IllegalArgumentException("Node may not be null!");
        }

        NodeRef userNodeRef = getUserNodeRef(userId);
        if (userNodeRef == null)
        {
            throw new IllegalArgumentException("User does not exist!");
        }

        Long dbid = (Long) nodeService.getProperty(userNodeRef, PROP_SYS_NODE_DBID);
        Long nodedbid = (Long) nodeService.getProperty(node, PROP_SYS_NODE_DBID);

        SubscriptionEntity se = new SubscriptionEntity();
        se.setUserNodeId(dbid);
        se.setNodeId(nodedbid);

        Number count = template.selectOne("alfresco.subscriptions.select_hasSubscribed", se);
        if (count == null || count.intValue() == 0)
        {
            template.insert("alfresco.subscriptions.insert_Subscription", se);
        }
    }

    @Override
    public void deleteSubscription(String userId, NodeRef node)
    {
        if (userId == null)
        {
            throw new IllegalArgumentException("User Id may not be null!");
        }

        if (node == null)
        {
            throw new IllegalArgumentException("Node may not be null!");
        }

        NodeRef userNodeRef = getUserNodeRef(userId);
        if (userNodeRef == null)
        {
            throw new IllegalArgumentException("User does not exist!");
        }

        Long dbid = (Long) nodeService.getProperty(userNodeRef, PROP_SYS_NODE_DBID);
        Long nodedbid = (Long) nodeService.getProperty(node, PROP_SYS_NODE_DBID);

        SubscriptionEntity se = new SubscriptionEntity();
        se.setUserNodeId(dbid);
        se.setNodeId(nodedbid);

        template.delete("alfresco.subscriptions.delete_Subscription", se);
    }

    @Override
    public boolean hasSubscribed(String userId, NodeRef node)
    {
        if (userId == null)
        {
            throw new IllegalArgumentException("User Id may not be null!");
        }

        if (node == null)
        {
            throw new IllegalArgumentException("Node may not be null!");
        }

        NodeRef userNodeRef = getUserNodeRef(userId);
        if (userNodeRef == null)
        {
            throw new IllegalArgumentException("User does not exist!");
        }

        Long dbid = (Long) nodeService.getProperty(userNodeRef, PROP_SYS_NODE_DBID);
        Long nodedbid = (Long) nodeService.getProperty(node, PROP_SYS_NODE_DBID);

        SubscriptionEntity se = new SubscriptionEntity();
        se.setUserNodeId(dbid);
        se.setNodeId(nodedbid);

        Number count = template.selectOne("alfresco.subscriptions.select_hasSubscribed", se);
        return count == null ? false : count.intValue() > 0;
    }

    @Override
    public PagingFollowingResults selectFollowing(String userId, PagingRequest pagingRequest)
    {
        if (userId == null)
        {
            throw new IllegalArgumentException("User Id may not be null!");
        }

        NodeRef userNodeRef = getUserNodeRef(userId);
        if (userNodeRef == null)
        {
            throw new IllegalArgumentException("User does not exist!");
        }

        Long dbid = (Long) nodeService.getProperty(userNodeRef, PROP_SYS_NODE_DBID);

        Map<String, Object> map = new HashMap<String, Object>();

        Pair<Long, QName> qNamePair = qnameDAO.getQName(ContentModel.PROP_USERNAME);
        if (null != qNamePair)
        {
            map.put("userIdQname", qNamePair.getFirst());
        }

        map.put("userNodeId", dbid);

        int maxItems = (pagingRequest.getMaxItems() < 0 || pagingRequest.getMaxItems() > Integer.MAX_VALUE - 1 ? Integer.MAX_VALUE - 1
                : pagingRequest.getMaxItems() + 1);

        @SuppressWarnings("unchecked")
        List<String> userList = template.selectList("alfresco.subscriptions.select_Following", map,
                new RowBounds(pagingRequest.getSkipCount(), maxItems + 1));

        boolean hasMore = userList.size() > maxItems;
        if (hasMore && userList.size() > 0)
        {
            userList.remove(userList.size() - 1);
        }

        Integer totalCount = null;
        if (pagingRequest.getRequestTotalCountMax() > 0)
        {
            totalCount = countSubscriptions(userId, SubscriptionItemTypeEnum.USER);
        }

        return new PagingFollowingResultsImpl(userList, hasMore, totalCount);
    }

    @Override
    public PagingFollowingResults selectFollowers(String userId, PagingRequest pagingRequest)
    {
        if (userId == null)
        {
            throw new IllegalArgumentException("User Id may not be null!");
        }

        NodeRef userNodeRef = getUserNodeRef(userId);
        if (userNodeRef == null)
        {
            throw new IllegalArgumentException("User does not exist!");
        }

        Long dbid = (Long) nodeService.getProperty(userNodeRef, PROP_SYS_NODE_DBID);

        Map<String, Object> map = new HashMap<String, Object>();

        Pair<Long, QName> qNamePair = qnameDAO.getQName(ContentModel.PROP_USERNAME);
        if (null != qNamePair)
        {
            map.put("userIdQname", qNamePair.getFirst());
        }

        map.put("userNodeId", dbid);

        int maxItems = (pagingRequest.getMaxItems() < 0 || pagingRequest.getMaxItems() > Integer.MAX_VALUE - 1 ? Integer.MAX_VALUE - 1
                : pagingRequest.getMaxItems() + 1);

        @SuppressWarnings("unchecked")
        List<String> userList = template.selectList("alfresco.subscriptions.select_Followers", map,
                new RowBounds(pagingRequest.getSkipCount(), maxItems + 1));

        boolean hasMore = userList.size() > maxItems;
        if (hasMore && userList.size() > 0)
        {
            userList.remove(userList.size() - 1);
        }

        Integer totalCount = null;
        if (pagingRequest.getRequestTotalCountMax() > 0)
        {
            totalCount = countFollowers(userId);
        }

        return new PagingFollowingResultsImpl(userList, hasMore, totalCount);
    }

    @Override
    public int countFollowers(String userId)
    {
        if (userId == null)
        {
            throw new IllegalArgumentException("User Id may not be null!");
        }

        NodeRef userNodeRef = getUserNodeRef(userId);
        if (userNodeRef == null)
        {
            return 0;
        }

        Long dbid = (Long) nodeService.getProperty(userNodeRef, PROP_SYS_NODE_DBID);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userNodeId", dbid);

        Number count = template.selectOne("alfresco.subscriptions.select_countFollowers", map);
        return count == null ? 0 : count.intValue();
    }
}
