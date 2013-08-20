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
package org.alfresco.repo.domain.subscriptions;

import junit.framework.TestCase;

import org.alfresco.query.PagingRequest;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.subscriptions.PagingFollowingResults;
import org.alfresco.service.cmr.subscriptions.PagingSubscriptionResults;
import org.alfresco.service.cmr.subscriptions.SubscriptionItemTypeEnum;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

public class SubscriptionDAOTest extends TestCase
{
    private ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private PersonService personService;

    private SubscriptionsDAO subscriptionsDAO;

    protected NodeRef getUserNodeRef(final String userId)
    {
        final PersonService ps = personService;

        return AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
        {
            @Override
            public NodeRef doWork() throws Exception
            {
                return ps.getPerson(userId);
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    protected void insert(final String userId, final NodeRef node) throws Exception
    {
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                subscriptionsDAO.insertSubscription(userId, node);
                return null;
            }
        };
        txnHelper.doInTransaction(callback, false, false);
    }

    protected void delete(final String userId, final NodeRef node) throws Exception
    {
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                subscriptionsDAO.deleteSubscription(userId, node);
                return null;
            }
        };
        txnHelper.doInTransaction(callback, false, false);
    }

    protected int count(final String userId) throws Exception
    {
        RetryingTransactionCallback<Integer> callback = new RetryingTransactionCallback<Integer>()
        {
            public Integer execute() throws Throwable
            {
                return subscriptionsDAO.countSubscriptions(userId, SubscriptionItemTypeEnum.USER);
            }
        };

        return txnHelper.doInTransaction(callback, false, false);
    }

    protected boolean hasSubscribed(final String userId, final NodeRef node) throws Exception
    {
        RetryingTransactionCallback<Boolean> callback = new RetryingTransactionCallback<Boolean>()
        {
            public Boolean execute() throws Throwable
            {
                return subscriptionsDAO.hasSubscribed(userId, node);
            }
        };

        return txnHelper.doInTransaction(callback, false, false);
    }

    protected PagingSubscriptionResults select(final String userId) throws Exception
    {
        RetryingTransactionCallback<PagingSubscriptionResults> callback = new RetryingTransactionCallback<PagingSubscriptionResults>()
        {
            public PagingSubscriptionResults execute() throws Throwable
            {
                return subscriptionsDAO.selectSubscriptions(userId, SubscriptionItemTypeEnum.USER, new PagingRequest(
                        100000, null));
            }
        };

        return txnHelper.doInTransaction(callback, false, false);
    }

    protected int countFollowers(final String userId) throws Exception
    {
        RetryingTransactionCallback<Integer> callback = new RetryingTransactionCallback<Integer>()
        {
            public Integer execute() throws Throwable
            {
                return subscriptionsDAO.countFollowers(userId);
            }
        };

        return txnHelper.doInTransaction(callback, false, false);
    }

    protected PagingFollowingResults selectFollowing(final String userId) throws Exception
    {
        RetryingTransactionCallback<PagingFollowingResults> callback = new RetryingTransactionCallback<PagingFollowingResults>()
        {
            public PagingFollowingResults execute() throws Throwable
            {
                return subscriptionsDAO.selectFollowing(userId, new PagingRequest(100000, null));
            }
        };

        return txnHelper.doInTransaction(callback, false, false);
    }

    protected PagingFollowingResults selectFollowers(final String userId) throws Exception
    {
        RetryingTransactionCallback<PagingFollowingResults> callback = new RetryingTransactionCallback<PagingFollowingResults>()
        {
            public PagingFollowingResults execute() throws Throwable
            {
                return subscriptionsDAO.selectFollowers(userId, new PagingRequest(100000, null));
            }
        };

        return txnHelper.doInTransaction(callback, false, false);
    }

    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();

        personService = serviceRegistry.getPersonService();

        subscriptionsDAO = (SubscriptionsDAO) ctx.getBean("subscriptionsDAO");
    }

    public void testInsertAndDelete() throws Exception
    {
        String userId = "admin";
        String userId2 = "guest";
        NodeRef nodeRef = getUserNodeRef(userId2);

        // check subscription first
        if (hasSubscribed(userId, nodeRef))
        {
            delete(userId, nodeRef);
        }
        boolean hasSubscribed = hasSubscribed(userId, nodeRef);
        assertFalse(hasSubscribed);

        // count subscriptions
        int count = count(userId);
        assertTrue(count >= 0);

        // insert
        insert(userId, nodeRef);
        insert(userId, nodeRef);
        assertEquals(count + 1, count(userId));
        assertTrue(hasSubscribed(userId, nodeRef));

        // select
        PagingSubscriptionResults psr = select(userId);
        assertNotNull(psr);
        assertNotNull(psr.getPage());
        assertTrue(psr.getPage().contains(nodeRef));

        PagingFollowingResults following = selectFollowing(userId);
        assertNotNull(following);
        assertNotNull(following.getPage());
        assertTrue(following.getPage().contains(userId2));

        assertEquals(psr.getPage().size(), following.getPage().size());

        // count followers
        int followerCount = countFollowers(userId2);
        assertTrue(followerCount >= 0);

        // select followers
        PagingFollowingResults followers = selectFollowers(userId2);
        assertNotNull(followers);
        assertNotNull(followers.getPage());
        assertTrue(followers.getPage().contains(userId));

        // delete
        delete(userId, nodeRef);
        assertEquals(count, count(userId));
        assertFalse(hasSubscribed(userId, nodeRef));
    }
}
