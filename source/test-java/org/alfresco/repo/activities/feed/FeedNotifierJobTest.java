/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.activities.feed;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.activities.post.lookup.PostLookup;
import org.alfresco.repo.domain.activities.ActivityPostDAO;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyMap;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;

/**
 * Tests for the {@link FeedNotifierJob} class.
 * 
 * @author alex.mukha
 */
@RunWith(MockitoJUnitRunner.class)
public class FeedNotifierJobTest
{
    private static ApplicationContext ctx = null;
    
    private FeedNotifierJob feedNotifierJob;
    private @Mock JobExecutionContext jobCtx;
    private TenantAdminService tenantAdminService;
    private TransactionService transactionService;
    private FeedNotifierImpl feedNotifier;
    private JobDetail jobDetail;
    private ActivityService activityService;
    private PersonService personService;
    private FeedGenerator feedGenerator;
    private PostLookup postLookup;
    private ActivityPostDAO postDAO;
    private RegisterErrorUserFeedNotifier userNotifier;
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private SiteService siteService;
    private RepoAdminService repoAdminService;
    private ActionService actionService;
    private AuthenticationContext authenticationContext;
    
    private NodeRef failingPersonNodeRef;
    private NodeRef personNodeRef;
    private String userName1 = "user1." + GUID.generate();
    private String userName2 = "user2." + GUID.generate();
    
    @BeforeClass
    public static void init()
    {
        ApplicationContextHelper.setUseLazyLoading(false);
        ApplicationContextHelper.setNoAutoStart(true);

        ctx = ApplicationContextHelper.getApplicationContext();
    }
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception
    {
        ChildApplicationContextFactory activitiesFeed = (ChildApplicationContextFactory) ctx.getBean("ActivitiesFeed");
        ApplicationContext activitiesFeedCtx = activitiesFeed.getApplicationContext();
        feedNotifier = (FeedNotifierImpl) activitiesFeedCtx.getBean("feedNotifier");
        activityService = (ActivityService) activitiesFeedCtx.getBean("activityService");
        feedGenerator = (FeedGenerator) activitiesFeedCtx.getBean("feedGenerator");
        postLookup = (PostLookup) activitiesFeedCtx.getBean("postLookup");
        ObjectFactory<ActivitiesFeedModelBuilder> feedModelBuilderFactory = (ObjectFactory<ActivitiesFeedModelBuilder>) activitiesFeedCtx.getBean("feedModelBuilderFactory");
        EmailUserNotifier emailUserNotifier = (EmailUserNotifier) activitiesFeedCtx.getBean("emailUserNotifier");
        
        tenantAdminService = (TenantAdminService) ctx.getBean("tenantAdminService");
        transactionService = (TransactionService) ctx.getBean("transactionService");
        personService = (PersonService) ctx.getBean("personService");
        postDAO = (ActivityPostDAO) ctx.getBean("postDAO");
        nodeService = (NodeService) ctx.getBean("nodeService");
        namespaceService = (NamespaceService) ctx.getBean("namespaceService");
        siteService = (SiteService) ctx.getBean("siteService");
        repoAdminService = (RepoAdminService) ctx.getBean("repoAdminService");
        actionService = (ActionService) ctx.getBean("ActionService");
        authenticationContext = (AuthenticationContext) ctx.getBean("authenticationContext");
        
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        // create some users
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            @SuppressWarnings("synthetic-access")
            public Void execute() throws Throwable
            {
                personNodeRef = createUser(userName1);
                failingPersonNodeRef = createUser(userName2);
                return null;
            }
        }, false, true);
        
        // use our own user notifier for testing purposes
        userNotifier = new RegisterErrorUserFeedNotifier();
        userNotifier.setNodeService(nodeService);
        userNotifier.setNamespaceService(namespaceService);
        userNotifier.setSiteService(siteService);
        userNotifier.setActivityService(activityService);
        userNotifier.setRepoAdminService(repoAdminService);
        userNotifier.setActionService(actionService);
        userNotifier.setActivitiesFeedModelBuilderFactory(feedModelBuilderFactory);
        userNotifier.setAuthenticationContext(authenticationContext);
        userNotifier.setExcludedEmailSuffixes(emailUserNotifier.getExcludedEmailSuffixes());
        
        feedNotifier.setUserNotifier(userNotifier);
        
        jobDetail = new JobDetail("feedNotifier", FeedNotifierJob.class);
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        jobDataMap.put("tenantAdminService", tenantAdminService);
        jobDataMap.put("feedNotifier", feedNotifier);
        feedNotifierJob = new FeedNotifierJob();
        
        when(jobCtx.getJobDetail()).thenReturn(jobDetail);
    }

    private NodeRef createUser(String userName)
    {
        PropertyMap personProps = new PropertyMap();
        personProps.put(ContentModel.PROP_USERNAME, userName);
        personProps.put(ContentModel.PROP_FIRSTNAME, userName);
        personProps.put(ContentModel.PROP_LASTNAME, userName);
        personProps.put(ContentModel.PROP_EMAIL, userName + "@email.com");
        return personService.createPerson(personProps);
    }

    private void generateActivities() throws Exception
    {
        // generate the activities
        postLookup.execute();

        Long maxSequence = postDAO.getMaxActivitySeq();
        while (maxSequence != null)
        {
            feedGenerator.execute();

            maxSequence = postDAO.getMaxActivitySeq();
        }
    }
    
    @After
    public void cleanUp()
    {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            @SuppressWarnings("synthetic-access")
            public Void execute() throws Throwable
            {
                personService.deletePerson(failingPersonNodeRef);
                personService.deletePerson(personNodeRef);
                return null;
            }
        }, false, true);
        AuthenticationUtil.clearCurrentSecurityContext();
    }
    
    /**
     * Test for MNT-12398
     * @throws JobExecutionException 
     */
    @Test
    public void testAuthentication() throws Exception
    {
        final String activityType = "org.alfresco.profile.status-changed";
        final String siteId = null;
        final String appTool = "profile";
        // Status update
        final String jsonActivityData = "{\"status\":\"test\"}";
        
        // and activity for userName2
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            @SuppressWarnings("synthetic-access")
            public Void execute() throws Throwable
            {
                AuthenticationUtil.pushAuthentication();
                AuthenticationUtil.setFullyAuthenticatedUser(userName1);

                activityService.postActivity(activityType, siteId, appTool, jsonActivityData);

                AuthenticationUtil.popAuthentication();

                return null;
            }
        }, false, true);
        
        generateActivities();
        // Start feed notifier as user2 (the generation should be run as system internally)
        // We should not get the "Unable to get user feed entries for 'user1' - currently logged in as 'user2'"
        AuthenticationUtil.setFullyAuthenticatedUser(userName2);
        feedNotifierJob.execute(jobCtx);
        
        assertNull("The notification failed with error " + userNotifier.getError(), userNotifier.getError());
    }

    private class RegisterErrorUserFeedNotifier extends EmailUserNotifier
    {
        private Exception error = null;
        
        public Exception getError()
        {
            return error;
        }
        
        @Override
        public Pair<Integer, Long> notifyUser(final NodeRef personNodeRef, String subject, Object[] subjectParams, Map<String, String> siteNames,
                String shareUrl, int repeatIntervalMins, String templateNodeRef)
        {
            try
            {
                return super.notifyUser(personNodeRef, subject, subjectParams, siteNames, shareUrl, repeatIntervalMins, templateNodeRef);
            }
            catch (AccessDeniedException e)
            {
                error = e;
                throw e;
            }
        }
        
    }
    
}
