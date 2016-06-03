package org.alfresco.repo.domain.subscriptions;

import org.alfresco.query.PagingRequest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.subscriptions.PagingFollowingResults;
import org.alfresco.service.cmr.subscriptions.PagingSubscriptionResults;
import org.alfresco.service.cmr.subscriptions.SubscriptionItemTypeEnum;

public abstract class AbstractSubscriptionsDAO implements SubscriptionsDAO
{
    protected NodeService nodeService;
    protected PersonService personService;

    public final void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public final void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    @Override
    public abstract PagingSubscriptionResults selectSubscriptions(String userId, SubscriptionItemTypeEnum type,
            PagingRequest pagingRequest);

    @Override
    public abstract int countSubscriptions(String userId, SubscriptionItemTypeEnum type);

    @Override
    public abstract void insertSubscription(String userId, NodeRef node);

    @Override
    public abstract void deleteSubscription(String userId, NodeRef node);

    @Override
    public abstract boolean hasSubscribed(String userId, NodeRef node);

    @Override
    public abstract PagingFollowingResults selectFollowers(String userId, PagingRequest pagingRequest);

    @Override
    public abstract int countFollowers(String userId);

    protected NodeRef getUserNodeRef(String userId)
    {
        try
        {
            return personService.getPerson(userId, false);
        } catch (NoSuchPersonException nspe)
        {
            return null;
        }
    }
}
