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

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.subscriptions.PagingFollowingResults;
import org.alfresco.service.cmr.subscriptions.PrivateSubscriptionListException;
import org.alfresco.service.cmr.subscriptions.SubscriptionService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.springframework.context.ApplicationContext;

public class SubscriptionServiceImplTest extends TestCase
{
    public static final String USER_BOB = "bob";
    public static final String USER_TOM = "tom";
    public static final String USER_LISA = "lisa";

    private UserTransaction txn;

    protected ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    protected TransactionService transactionService;
    protected SubscriptionService subscriptionService;
    protected PersonService personService;

    @Override
    public void setUp() throws Exception
    {
        // Get the required services
        transactionService = (TransactionService) ctx.getBean("TransactionService");
        subscriptionService = (SubscriptionService) ctx.getBean("SubscriptionService");
        personService = (PersonService) ctx.getBean("PersonService");

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        txn = transactionService.getNonPropagatingUserTransaction(false);
        txn.begin();

        createPerson(USER_BOB);
        createPerson(USER_TOM);
        createPerson(USER_LISA);
    }

    @Override
    protected void tearDown() throws Exception
    {
        deletePerson(USER_BOB);
        deletePerson(USER_TOM);
        deletePerson(USER_LISA);

        if (txn != null)
        {
            if (txn.getStatus() == Status.STATUS_MARKED_ROLLBACK)
            {
                txn.rollback();
            } else
            {
                txn.commit();
            }
            txn = null;
        }
    }

    protected void deletePerson(String userId)
    {
        personService.deletePerson(userId);
    }

    protected NodeRef createPerson(String userId)
    {
        deletePerson(userId);

        PropertyMap properties = new PropertyMap(5);
        properties.put(ContentModel.PROP_USERNAME, userId);
        properties.put(ContentModel.PROP_FIRSTNAME, userId);
        properties.put(ContentModel.PROP_LASTNAME, "Test");
        properties.put(ContentModel.PROP_EMAIL, userId + "@test.demo.alfresco.com");

        return personService.createPerson(properties);
    }

    public void testFollow() throws Exception
    {
        String userId1 = USER_BOB;
        String userId2 = USER_TOM;
        String userId3 = USER_LISA;

        // check follows first
        if (subscriptionService.follows(userId1, userId2))
        {
            subscriptionService.unfollow(userId1, userId2);
        }
        assertFalse(subscriptionService.follows(userId1, userId2));

        // count the people user 1 is following
        int count = subscriptionService.getFollowingCount(userId1);
        assertTrue(count >= 0);

        // user 1 follows user 2 -- twice (the second follow request should be
        // ignored)
        subscriptionService.follow(userId1, userId2);
        subscriptionService.follow(userId1, userId2);
        assertEquals(count + 1, subscriptionService.getFollowingCount(userId1));
        assertTrue(subscriptionService.follows(userId1, userId2));

        // user 1 follows user 3
        subscriptionService.follow(userId1, userId3);
        assertEquals(count + 2, subscriptionService.getFollowingCount(userId1));
        assertTrue(subscriptionService.follows(userId1, userId3));

        // get following list of user 1
        PagingFollowingResults following = subscriptionService.getFollowing(userId1, new PagingRequest(100000, null));
        assertNotNull(following);
        assertNotNull(following.getPage());
        assertTrue(following.getPage().contains(userId2));
        assertTrue(following.getPage().contains(userId3));

        // count followers of user 2
        int followerCount = subscriptionService.getFollowersCount(userId2);
        assertTrue(followerCount > 0);

        // get followers of user 2
        PagingFollowingResults followers = subscriptionService.getFollowers(userId2, new PagingRequest(100000, null));
        assertNotNull(followers);
        assertNotNull(followers.getPage());
        assertTrue(followers.getPage().contains(userId1));

        // unfollow
        subscriptionService.unfollow(userId1, userId2);
        assertEquals(count + 1, subscriptionService.getFollowingCount(userId1));
        assertFalse(subscriptionService.follows(userId1, userId2));
        assertTrue(subscriptionService.follows(userId1, userId3));

        subscriptionService.unfollow(userId1, userId3);
        assertEquals(count, subscriptionService.getFollowingCount(userId1));
        assertFalse(subscriptionService.follows(userId1, userId3));
    }

    public void testDeletePerson() throws Exception
    {
        String userId1 = USER_BOB;
        String userId2 = "subscription-temp-user";

        createPerson(userId2);

        subscriptionService.follow(userId1, userId2);
        assertTrue(subscriptionService.follows(userId1, userId2));

        deletePerson(userId2);

        PagingFollowingResults following = subscriptionService.getFollowing(userId1, new PagingRequest(100000, null));
        assertNotNull(following);
        assertNotNull(following.getPage());
        assertFalse(following.getPage().contains(userId2));
    }

    public void testPrivateList() throws Exception
    {
        final String userId1 = USER_BOB;
        final String userId2 = USER_TOM;

        assertFalse(subscriptionService.isSubscriptionListPrivate(userId1));

        subscriptionService.setSubscriptionListPrivate(userId1, false);
        assertFalse(subscriptionService.isSubscriptionListPrivate(userId1));

        subscriptionService.setSubscriptionListPrivate(userId1, true);
        assertTrue(subscriptionService.isSubscriptionListPrivate(userId1));

        subscriptionService.setSubscriptionListPrivate(userId1, false);
        assertFalse(subscriptionService.isSubscriptionListPrivate(userId1));

        subscriptionService.setSubscriptionListPrivate(userId1, true);
        assertTrue(subscriptionService.isSubscriptionListPrivate(userId1));

        subscriptionService.follow(userId1, userId2);

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            @Override
            public Object doWork() throws Exception
            {
                assertNotNull(subscriptionService.getFollowing(userId1, new PagingRequest(100000, null)));
                return null;
            }
        }, userId1);

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            @Override
            public Object doWork() throws Exception
            {
                assertNotNull(subscriptionService.getFollowing(userId1, new PagingRequest(100000, null)));
                return null;
            }
        }, AuthenticationUtil.getAdminUserName());

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            @Override
            public Object doWork() throws Exception
            {
                try
                {
                    subscriptionService.getFollowing(userId1, new PagingRequest(100000, null));
                    fail("Expected PrivateSubscriptionListException!");
                } catch (PrivateSubscriptionListException psle)
                {
                    // expected
                }
                return null;
            }
        }, userId2);
    }
}
