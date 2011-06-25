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
package org.alfresco.repo.domain.subscriptions.ibatis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.alfresco.util.Pair;
import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;

public class SubscriptionsDAOImpl extends AbstractSubscriptionsDAO
{
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
        Pair<Long, NodeRef> userPair = nodeDAO.getNodePair(userNodeRef);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userNodeId", userPair.getFirst());
        map.put("false", Boolean.FALSE);

        int maxItems = (pagingRequest.getMaxItems() < 0 || pagingRequest.getMaxItems() > Integer.MAX_VALUE - 1 ? Integer.MAX_VALUE - 1
                : pagingRequest.getMaxItems() + 1);

        @SuppressWarnings("unchecked")
        List<SubscriptionNodeEntity> nodeList = (List<SubscriptionNodeEntity>) template.selectList(
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
        Pair<Long, NodeRef> userPair = nodeDAO.getNodePair(userNodeRef);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userNodeId", userPair.getFirst());
        map.put("false", Boolean.FALSE);

        Number count = (Number) template.selectOne("alfresco.subscriptions.select_countSubscriptions", map);

        return count.intValue();
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
        Pair<Long, NodeRef> userPair = nodeDAO.getNodePair(userNodeRef);
        Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(node);

        SubscriptionEntity se = new SubscriptionEntity();
        se.setUserNodeId(userPair.getFirst());
        se.setNodeId(nodePair.getFirst());

        if (((Number) template.selectOne("alfresco.subscriptions.select_hasSubscribed", se)).intValue() == 0)
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
        Pair<Long, NodeRef> userPair = nodeDAO.getNodePair(userNodeRef);
        Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(node);

        SubscriptionEntity se = new SubscriptionEntity();
        se.setUserNodeId(userPair.getFirst());
        se.setNodeId(nodePair.getFirst());

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
        Pair<Long, NodeRef> userPair = nodeDAO.getNodePair(userNodeRef);
        Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(node);

        SubscriptionEntity se = new SubscriptionEntity();
        se.setUserNodeId(userPair.getFirst());
        se.setNodeId(nodePair.getFirst());

        return ((Number) template.selectOne("alfresco.subscriptions.select_hasSubscribed", se)).intValue() == 1;
    }

    @Override
    public PagingFollowingResults selectFollowing(String userId, PagingRequest pagingRequest)
    {
        if (userId == null)
        {
            throw new IllegalArgumentException("User Id may not be null!");
        }

        NodeRef userNodeRef = getUserNodeRef(userId);
        Pair<Long, NodeRef> userPair = nodeDAO.getNodePair(userNodeRef);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userIdQname", qnameDAO.getQName(ContentModel.PROP_USERNAME).getFirst());
        map.put("userNodeId", userPair.getFirst());
        map.put("false", Boolean.FALSE);

        int maxItems = (pagingRequest.getMaxItems() < 0 || pagingRequest.getMaxItems() > Integer.MAX_VALUE - 1 ? Integer.MAX_VALUE - 1
                : pagingRequest.getMaxItems() + 1);

        @SuppressWarnings("unchecked")
        List<String> userList = (List<String>) template.selectList("alfresco.subscriptions.select_Following", map,
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
        Pair<Long, NodeRef> userPair = nodeDAO.getNodePair(userNodeRef);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userIdQname", qnameDAO.getQName(ContentModel.PROP_USERNAME).getFirst());
        map.put("userNodeId", userPair.getFirst());
        map.put("false", Boolean.FALSE);

        int maxItems = (pagingRequest.getMaxItems() < 0 || pagingRequest.getMaxItems() > Integer.MAX_VALUE - 1 ? Integer.MAX_VALUE - 1
                : pagingRequest.getMaxItems() + 1);

        @SuppressWarnings("unchecked")
        List<String> userList = (List<String>) template.selectList("alfresco.subscriptions.select_Followers", map,
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
        Pair<Long, NodeRef> userPair = nodeDAO.getNodePair(userNodeRef);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userNodeId", userPair.getFirst());
        map.put("false", Boolean.FALSE);

        Number count = (Number) template.selectOne("alfresco.subscriptions.select_countFollowers", map);

        return count.intValue();
    }
}
