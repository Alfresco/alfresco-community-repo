package org.alfresco.repo.domain.subscriptions;

import org.alfresco.query.PagingRequest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.subscriptions.PagingFollowingResults;
import org.alfresco.service.cmr.subscriptions.PagingSubscriptionResults;
import org.alfresco.service.cmr.subscriptions.SubscriptionItemTypeEnum;

public interface SubscriptionsDAO
{
    PagingSubscriptionResults selectSubscriptions(String userId, SubscriptionItemTypeEnum type,
            PagingRequest pagingRequest);

    int countSubscriptions(String userId, SubscriptionItemTypeEnum type);

    void insertSubscription(String userId, NodeRef node);

    void deleteSubscription(String userId, NodeRef node);

    boolean hasSubscribed(String userId, NodeRef node);

    PagingFollowingResults selectFollowing(String userId, PagingRequest pagingRequest);

    PagingFollowingResults selectFollowers(String userId, PagingRequest pagingRequest);

    int countFollowers(String userId);
}
