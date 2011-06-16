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
package org.alfresco.repo.subscriptions;

import java.io.Serializable;
import java.util.Collections;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.domain.subscriptions.SubscriptionsDAO;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.subscriptions.PagingFollowingResults;
import org.alfresco.service.cmr.subscriptions.PagingFollowingResultsImpl;
import org.alfresco.service.cmr.subscriptions.PagingSubscriptionResults;
import org.alfresco.service.cmr.subscriptions.PagingSubscriptionResultsImpl;
import org.alfresco.service.cmr.subscriptions.PrivateSubscriptionListException;
import org.alfresco.service.cmr.subscriptions.SubscriptionItemTypeEnum;
import org.alfresco.service.cmr.subscriptions.SubscriptionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

public class SubscriptionServiceImpl implements SubscriptionService
{
    /** Logger */
    private static Log logger = LogFactory.getLog(SubscriptionServiceImpl.class);

    /** Activity tool */
    private static final String ACTIVITY_TOOL = "subscriptionService";

    /** Activity values */
    private static final String SUB_USER = "user";
    private static final String SUB_USER_TO_FOLLOW = "userToFollow";
    private static final String SUB_NODE = "node";

    protected SubscriptionsDAO subscriptionsDAO;
    protected NodeService nodeService;
    protected PersonService personService;
    protected ActivityService activityService;

    /**
     * Sets the subscriptions DAO.
     */
    public void setSubscriptionsDAO(SubscriptionsDAO subscriptionsDAO)
    {
        this.subscriptionsDAO = subscriptionsDAO;
    }

    /**
     * Sets the node service.
     */
    public final void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Sets the person service.
     */
    public final void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    /**
     * Sets the activity service.
     */
    public final void setActivityService(ActivityService activictyService)
    {
        this.activityService = activictyService;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PagingSubscriptionResults getSubscriptions(String userId, SubscriptionItemTypeEnum type,
            PagingRequest pagingRequest)
    {
        if (!subscriptionsEnabled())
        {
            return new PagingSubscriptionResultsImpl(Collections.EMPTY_LIST, false, 0);
        }

        checkRead(userId);
        return subscriptionsDAO.selectSubscriptions(userId, type, pagingRequest);
    }

    @Override
    public int getSubscriptionCount(String userId, SubscriptionItemTypeEnum type)
    {
        if (!subscriptionsEnabled())
        {
            return 0;
        }

        return subscriptionsDAO.countSubscriptions(userId, type);
    }

    @Override
    public void subscribe(String userId, NodeRef node)
    {
        if (!subscriptionsEnabled())
        {
            return;
        }

        checkWrite(userId);
        checkUserNode(node);
        subscriptionsDAO.insertSubscription(userId, node);

        if (userId.equalsIgnoreCase(AuthenticationUtil.getRunAsUser()))
        {
            String activityDataJSON = null;
            try
            {
                JSONObject activityData = new JSONObject();
                activityData.put(SUB_USER, userId);
                activityData.put(SUB_NODE, node.toString());
                activityDataJSON = activityData.toString();
            } catch (JSONException je)
            {
                // log error, subsume exception
                logger.error("Failed to get activity data: " + je);
            }

            activityService.postActivity(ActivityType.SUBSCRIPTIONS_SUBSCRIBE, null, ACTIVITY_TOOL, activityDataJSON);
        }
    }

    @Override
    public void unsubscribe(String userId, NodeRef node)
    {
        if (!subscriptionsEnabled())
        {
            return;
        }

        checkWrite(userId);
        subscriptionsDAO.deleteSubscription(userId, node);
    }

    @Override
    public boolean hasSubscribed(String userId, NodeRef node)
    {
        if (!subscriptionsEnabled())
        {
            return false;
        }

        checkRead(userId);
        return subscriptionsDAO.hasSubscribed(userId, node);
    }

    @SuppressWarnings("unchecked")
    @Override
    public PagingFollowingResults getFollowing(String userId, PagingRequest pagingRequest)
    {
        if (!subscriptionsEnabled())
        {
            return new PagingFollowingResultsImpl(Collections.EMPTY_LIST, false, 0);
        }

        checkRead(userId);
        return subscriptionsDAO.selectFollowing(userId, pagingRequest);
    }

    @SuppressWarnings("unchecked")
    @Override
    public PagingFollowingResults getFollowers(String userId, PagingRequest pagingRequest)
    {
        if (!subscriptionsEnabled())
        {
            return new PagingFollowingResultsImpl(Collections.EMPTY_LIST, false, 0);
        }

        if (userId == null)
        {
            throw new IllegalArgumentException("User Id may not be null!");
        }

        return subscriptionsDAO.selectFollowers(userId, pagingRequest);
    }

    @Override
    public int getFollowersCount(String userId)
    {
        if (!subscriptionsEnabled())
        {
            return 0;
        }

        return subscriptionsDAO.countFollowers(userId);
    }

    @Override
    public int getFollowingCount(String userId)
    {
        if (!subscriptionsEnabled())
        {
            return 0;
        }

        return getSubscriptionCount(userId, SubscriptionItemTypeEnum.USER);
    }

    @Override
    public void follow(String userId, String userToFollow)
    {
        if (!subscriptionsEnabled())
        {
            return;
        }

        checkWrite(userId);
        subscriptionsDAO.insertSubscription(userId, getUserNodeRef(userToFollow));

        if (userId.equalsIgnoreCase(AuthenticationUtil.getRunAsUser()))
        {
            String activityDataJSON = null;
            try
            {
                JSONObject activityData = new JSONObject();
                activityData.put(SUB_USER, userId);
                activityData.put(SUB_USER_TO_FOLLOW, userToFollow);
                activityDataJSON = activityData.toString();
            } catch (JSONException je)
            {
                // log error, subsume exception
                logger.error("Failed to get activity data: " + je);
            }

            activityService.postActivity(ActivityType.SUBSCRIPTIONS_FOLLOW, null, ACTIVITY_TOOL, activityDataJSON);
        }
    }

    @Override
    public void unfollow(String userId, String userToUnfollow)
    {
        if (!subscriptionsEnabled())
        {
            return;
        }

        checkWrite(userId);
        subscriptionsDAO.deleteSubscription(userId, getUserNodeRef(userToUnfollow));
    }

    @Override
    public boolean follows(String userId, String userToFollow)
    {
        if (!subscriptionsEnabled())
        {
            return false;
        }

        checkRead(userId);
        return subscriptionsDAO.hasSubscribed(userId, getUserNodeRef(userToFollow));
    }

    @Override
    public void setSubscriptionListPrivate(String userId, boolean isPrivate)
    {
        checkWrite(userId);
        nodeService.setProperty(getUserNodeRef(userId), ContentModel.PROP_SUBSCRIPTIONS_PRIVATE, isPrivate);
    }

    @Override
    public boolean isSubscriptionListPrivate(String userId)
    {
        if (userId == null)
        {
            throw new IllegalArgumentException("User Id may not be null!");
        }

        Serializable privateList = nodeService.getProperty(getUserNodeRef(userId),
                ContentModel.PROP_SUBSCRIPTIONS_PRIVATE);
        if (privateList == null)
        {
            return false;
        }

        if (privateList instanceof Boolean && !((Boolean) privateList).booleanValue())
        {
            return false;
        }

        return true;
    }

    protected boolean subscriptionsEnabled()
    {
        return true;
    }

    /**
     * Checks if the current user is allowed to get subscription data.
     */
    protected void checkRead(String userId)
    {
        if (userId == null)
        {
            throw new IllegalArgumentException("User Id may not be null!");
        }

        String currentUser = AuthenticationUtil.getRunAsUser();
        if (currentUser == null)
        {
            throw new IllegalArgumentException("No current user!");
        }

        if (currentUser.equalsIgnoreCase(userId) || currentUser.equalsIgnoreCase(AuthenticationUtil.getAdminUserName())
                || AuthenticationUtil.isRunAsUserTheSystemUser() || !isSubscriptionListPrivate(userId))
        {
            return;
        }

        throw new PrivateSubscriptionListException("subscription_service.err.private-list");
    }

    /**
     * Checks if the current user is allowed to get change data.
     */
    protected void checkWrite(String userId)
    {
        if (userId == null)
        {
            throw new IllegalArgumentException("User Id may not be null!");
        }

        String currentUser = AuthenticationUtil.getRunAsUser();
        if (currentUser == null)
        {
            throw new IllegalArgumentException("No current user!");
        }

        if (currentUser.equalsIgnoreCase(userId) || currentUser.equalsIgnoreCase(AuthenticationUtil.getAdminUserName())
                || AuthenticationUtil.isRunAsUserTheSystemUser())
        {
            return;
        }

        throw new AccessDeniedException("subscription_service.err.write-denied");
    }

    /**
     * Gets the user node ref from the user id.
     */
    protected NodeRef getUserNodeRef(String userId)
    {
        return personService.getPerson(userId, false);
    }

    /**
     * Checks if the node is a user node and throws an exception if it id not.
     */
    protected void checkUserNode(NodeRef nodeRef)
    {
        // we only support user-to-user subscriptions in this release
        if (!ContentModel.TYPE_USER.equals(nodeService.getType(nodeRef)))
        {
            throw new IllegalArgumentException("Only user nodes supported!");
        }
    }
}
