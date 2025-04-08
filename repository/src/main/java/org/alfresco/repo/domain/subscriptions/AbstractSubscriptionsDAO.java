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
        }
        catch (NoSuchPersonException nspe)
        {
            return null;
        }
    }
}
