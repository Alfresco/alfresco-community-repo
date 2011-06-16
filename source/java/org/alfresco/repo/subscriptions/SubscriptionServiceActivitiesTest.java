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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.activities.feed.FeedGenerator;
import org.alfresco.repo.activities.feed.local.LocalFeedTaskProcessor;
import org.alfresco.repo.activities.post.lookup.PostLookup;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.subscriptions.SubscriptionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.quartz.Scheduler;
import org.springframework.context.ApplicationContext;

public class SubscriptionServiceActivitiesTest extends TestCase
{
    // Location of activity type templates (for site activities)
    // assumes test-resources is on classpath
    protected static final String TEST_TEMPLATES_LOCATION = "activities";

    protected ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    protected SubscriptionService subscriptionService;
    protected PersonService personService;
    protected PostLookup postLookup;
    protected FeedGenerator feedGenerator;

    @Override
    public void setUp() throws Exception
    {
        // Let's shut down the scheduler so that we aren't competing with the
        // scheduled versions of the post lookup and
        // feed generator jobs
        Scheduler scheduler = (Scheduler) ctx.getBean("schedulerFactory");
        scheduler.shutdown();

        // Get the required services
        subscriptionService = (SubscriptionService) ctx.getBean("SubscriptionService");
        personService = (PersonService) ctx.getBean("PersonService");

        ChildApplicationContextFactory activitiesFeed = (ChildApplicationContextFactory) ctx.getBean("ActivitiesFeed");
        ApplicationContext activitiesFeedCtx = activitiesFeed.getApplicationContext();
        postLookup = (PostLookup) activitiesFeedCtx.getBean("postLookup");
        feedGenerator = (FeedGenerator) activitiesFeedCtx.getBean("feedGenerator");

        LocalFeedTaskProcessor feedProcessor = (LocalFeedTaskProcessor) activitiesFeedCtx.getBean("feedTaskProcessor");

        List<String> templateSearchPaths = new ArrayList<String>(1);
        templateSearchPaths.add(TEST_TEMPLATES_LOCATION);
        feedProcessor.setTemplateSearchPaths(templateSearchPaths);
        feedProcessor.setUseRemoteCallbacks(false);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
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
        properties.put(ContentModel.PROP_EMAIL, userId + "@email.com");

        return personService.createPerson(properties);
    }

    protected void generateFeed() throws Exception
    {
        postLookup.execute();
        feedGenerator.execute();
    }

    public void testFollowingActivity() throws Exception
    {
        final String userId1 = "bob";
        final String userId2 = "tom";

        createPerson(userId1);
        createPerson(userId2);

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            @Override
            public Object doWork() throws Exception
            {
                subscriptionService.follow(userId1, userId2);
                return null;
            }
        }, userId1);

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            @Override
            public Object doWork() throws Exception
            {
                subscriptionService.follow(userId2, userId1);
                return null;
            }
        }, userId2);

        generateFeed();

        deletePerson(userId1);
        deletePerson(userId2);
    }
}
