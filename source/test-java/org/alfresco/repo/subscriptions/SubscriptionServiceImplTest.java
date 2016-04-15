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
package org.alfresco.repo.subscriptions;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.subscriptions.PagingFollowingResults;
import org.alfresco.service.cmr.subscriptions.PrivateSubscriptionListException;
import org.alfresco.service.cmr.subscriptions.SubscriptionItemTypeEnum;
import org.alfresco.service.cmr.subscriptions.SubscriptionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

@Category(OwnJVMTestsCategory.class)
public class SubscriptionServiceImplTest extends TestCase
{
    public static final String[] CONTEXTS = new String[] { "classpath:alfresco/application-context.xml", "classpath:test/alfresco/test-subscriptions-context.xml" };

    public static final String USER_BOB = "bob" + GUID.generate();
    public static final String USER_TOM = "tom" + GUID.generate();
    public static final String USER_LISA = "lisa" + GUID.generate();

    public static final String FOLLOWED_NODE_NAME = "Followed.txt";

    public static final QName ASPECT_ARCHIVE = QName.createQName("http://www.alfresco.org/model/testsubscriptionsmodel/1.0", "archive");

    private UserTransaction txn;

    protected ApplicationContext ctx = ApplicationContextHelper.getApplicationContext(CONTEXTS);
    protected TransactionService transactionService;
    protected SubscriptionService subscriptionService;
    protected PersonService personService;
    protected NodeService nodeService;
    protected SearchService searchService;

    @Override
    public void setUp() throws Exception
    {
        // Get the required services
        transactionService = (TransactionService) ctx.getBean("TransactionService");
        subscriptionService = (SubscriptionService) ctx.getBean("SubscriptionService");
        personService = (PersonService) ctx.getBean("PersonService");
        nodeService = (NodeService) ctx.getBean("NodeService");
        searchService = (SearchService) ctx.getBean("SearchService");

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

    public void testSubscriptionsRemovalOnArchivingNodesForAlf12358() throws Exception
    {
        NodeRef companyHome = null;
        ResultSet resultSet = null;
        try
        {
            resultSet = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, "PATH:\"/app\\:company_home\"");
            assertNotNull("Can't find Company Home NodeRef!", resultSet);
            assertEquals("Found too many Company Home nodes!", 1, resultSet.length());

            companyHome = resultSet.getNodeRef(0);
            assertNotNull("Company Home NodeRef is invalid!", companyHome);
        }
        finally
        {
            if (null != resultSet)
            {
                try
                {
                    resultSet.close();
                }
                catch (Exception e)
                {
                    // Doing nothing
                }
            }
        }

        ChildAssociationRef followed = null;

        int initialCount = subscriptionService.getSubscriptionCount(USER_TOM, SubscriptionItemTypeEnum.USER);

        try
        {
            followed = nodeService.createNode(companyHome, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName
                    .createValidLocalName(FOLLOWED_NODE_NAME)), ContentModel.TYPE_USER);
            nodeService.addAspect(followed.getChildRef(), ASPECT_ARCHIVE, null);
            assertTrue("Fake User node MUST BE archival!", nodeService.hasAspect(followed.getChildRef(), ASPECT_ARCHIVE));

            subscriptionService.subscribe(USER_TOM, followed.getChildRef());
            assertEquals("Initial subscriptions count MUST BE lesser by 1 after adding new subscription!", 1, (subscriptionService.getSubscriptionCount(USER_TOM,
                    SubscriptionItemTypeEnum.USER) - initialCount));

            nodeService.deleteNode(followed.getChildRef());
            assertEquals("Archiving of node MUST cause removal all the subscriptions created against it!", initialCount, subscriptionService.getSubscriptionCount(USER_TOM,
                    SubscriptionItemTypeEnum.USER));
        }
        finally
        {
            if ((null != followed) && nodeService.exists(followed.getChildRef()))
            {
                try
                {
                    nodeService.deleteNode(followed.getChildRef());
                }
                catch (Exception e)
                {
                    // Doing nothing
                }
            }
        }
    }
}
