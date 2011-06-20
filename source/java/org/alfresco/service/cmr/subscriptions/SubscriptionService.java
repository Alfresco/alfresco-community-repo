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
package org.alfresco.service.cmr.subscriptions;

import org.alfresco.query.PagingRequest;
import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Subscription Service.
 * 
 * @author Florian Mueller
 * @since 4.0
 */
public interface SubscriptionService
{
    // --- subscription ---

    /**
     * Returns the nodes a user has subscribed to.
     * 
     * @param userId
     *            the id of the user
     * @param type
     *            the type of the nodes
     * @param pagingRequest
     *            paging details
     * 
     * @throws PrivateSubscriptionListException
     *             if the subscription list is private and the calling user is
     *             not allowed to see it
     */
    @NotAuditable
    PagingSubscriptionResults getSubscriptions(String userId, SubscriptionItemTypeEnum type, PagingRequest pagingRequest);

    /**
     * Returns how many nodes the given user has subscribed to.
     * 
     * @param userId
     *            the id of the user
     * @param type
     *            the type of the nodes
     */
    @NotAuditable
    int getSubscriptionCount(String userId, SubscriptionItemTypeEnum type);

    /**
     * Subscribes to a node.
     * 
     * @param userId
     *            id of the user
     * @param node
     *            the node
     */
    @Auditable(parameters = { "userId", "node" })
    void subscribe(String userId, NodeRef node);

    /**
     * Unsubscribes from a node.
     * 
     * @param userId
     *            id of the user
     * @param node
     *            the node
     */
    @Auditable(parameters = { "userId", "node" })
    void unsubscribe(String userId, NodeRef node);

    /**
     * Returns if the user has subscribed to the given node.
     * 
     * @param userId
     *            id of the user
     * @param node
     *            the node
     */
    @NotAuditable
    boolean hasSubscribed(String userId, NodeRef node);

    // --- follow ---

    /**
     * Returns a list of users that the given user follows.
     * 
     * @param userId
     *            id of the user
     * @param pagingRequest
     *            paging details
     * @throws PrivateSubscriptionListException
     *             if the subscription list is private and the calling user is
     *             not allowed to see it
     */
    @NotAuditable
    PagingFollowingResults getFollowing(String userId, PagingRequest pagingRequest);

    /**
     * Returns a list of users that follow the given user.
     * 
     * @param userId
     *            id of the user
     * @param pagingRequest
     *            paging details
     */
    @NotAuditable
    PagingFollowingResults getFollowers(String userId, PagingRequest pagingRequest);

    /**
     * Returns how many users the given user follows.
     * 
     * @param userId
     *            the id of the user
     * @param type
     *            the type of the nodes
     */
    @NotAuditable
    int getFollowingCount(String userId);

    /**
     * Returns how many users follow the given user.
     * 
     * @param userId
     *            the id of the user
     * @param type
     *            the type of the nodes
     */
    @NotAuditable
    int getFollowersCount(String userId);

    /**
     * Follows another
     * 
     * @param userId
     *            the id of the user
     * @param userToFollow
     *            the id of the user to follow
     */
    @Auditable(parameters = { "userId", "userToFollow" })
    void follow(String userId, String userToFollow);

    @Auditable(parameters = { "userId", "userToUnfollow" })
    void unfollow(String userId, String userToUnfollow);

    /**
     * Returns if the user follows to the given other user.
     * 
     * @param userId
     *            id of the user
     * @param userToFollow
     *            the id of the other user
     */
    @NotAuditable
    boolean follows(String userId, String userToFollow);

    // --- privacy settings ---

    /**
     * Sets or unsets the subscription list of the given user to private.
     * 
     * @param userId
     *            the id of the user
     * @param isPrivate
     *            <code>true</code> - set list private,
     *            <code>false<code> - set list public
     * 
     */
    @Auditable(parameters = { "userId", "isPrivate" })
    void setSubscriptionListPrivate(String userId, boolean isPrivate);

    /**
     * Returns if the subscription list of the given user is set to private.
     * 
     * @param userId
     *            the id of the user
     */
    @NotAuditable
    boolean isSubscriptionListPrivate(String userId);
}
