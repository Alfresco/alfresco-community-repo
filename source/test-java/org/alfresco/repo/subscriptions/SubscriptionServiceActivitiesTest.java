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

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.activities.feed.FeedGenerator;
import org.alfresco.repo.activities.feed.local.LocalFeedTaskProcessor;
import org.alfresco.repo.activities.post.lookup.PostLookup;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.subscriptions.SubscriptionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.GUID;
import org.alfresco.util.test.junitrules.AlfrescoPerson;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.TemporarySites;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.RuleChain;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;

@Category(OwnJVMTestsCategory.class)
public class SubscriptionServiceActivitiesTest
{
    private static final Log log = LogFactory.getLog(SubscriptionServiceActivitiesTest.class);
    
    // JUnit Rule to initialise the Alfresco spring configuration
    public static ApplicationContextInit APP_CONTEXT_INIT = new ApplicationContextInit();
    
    // We'll suffix each user id with a unique number to allow for repeated re-runs of this test class.
    // We need to do this as the feed entries for deleted users do not get deleted immediately.
    private final static long NOW = System.currentTimeMillis();
    public static final String USER_ONE_NAME = "UserOne" + NOW;
    public static final String USER_TWO_NAME = "UserTwo" + NOW;
    private static String ADMIN;
    
    // JUnit Rules to create 2 test users.
    public static AlfrescoPerson TEST_USER1 = new AlfrescoPerson(APP_CONTEXT_INIT, USER_ONE_NAME);
    public static AlfrescoPerson TEST_USER2 = new AlfrescoPerson(APP_CONTEXT_INIT, USER_TWO_NAME);
    
    // Tie them together in a static Rule Chain
    @ClassRule public static RuleChain STATIC_RULE_CHAIN = RuleChain.outerRule(APP_CONTEXT_INIT)
                                                            .around(TEST_USER1)
                                                            .around(TEST_USER2);
    
    // A JUnit Rule to manage test nodes use in each test method
    @Rule public TemporarySites testSites = new TemporarySites(APP_CONTEXT_INIT);
    
    // Location of activity type templates (for site activities)
    // assumes test-resources is on classpath
    protected static final String TEST_TEMPLATES_LOCATION = "activities";
    
    protected static SubscriptionService subscriptionService;
    protected static PersonService personService;
    protected static SiteService siteService;
    protected static ActivityService activityService;
    protected static NodeService nodeService;
    protected static ContentService contentService;
    protected static PostLookup postLookup;
    protected static FeedGenerator feedGenerator;
    protected static RetryingTransactionHelper transactionHelper;
    protected static NodeArchiveService nodeArchiveService;
    
    private static Scheduler QUARTZ_SCHEDULER;
    
    // Test Sites - these are all created by USER_ONE & hence USER_ONE is the SiteManager.
    private SiteInfo publicSite,
                     privateSite1, privateSite2,
                     modSite1, modSite2;
    
    @BeforeClass public static void setUp() throws Exception
    {
        final ApplicationContext ctx = APP_CONTEXT_INIT.getApplicationContext();
        
        // Let's shut down the scheduler so that we aren't competing with the
        // scheduled versions of the post lookup and
        // feed generator jobs
        // Note that to ensure this test class can be run within a suite, that we do not want to actually 'shutdown' the scheduler.
        // It may be needed by other test classes and must be restored to life after this class has run.
        QUARTZ_SCHEDULER = ctx.getBean("schedulerFactory", Scheduler.class);
        QUARTZ_SCHEDULER.standby();
        
        // Get the required services
        subscriptionService = (SubscriptionService) ctx.getBean("SubscriptionService");
        personService = (PersonService) ctx.getBean("PersonService");
        siteService = (SiteService) ctx.getBean("SiteService");
        activityService = (ActivityService) ctx.getBean("activityService");
        nodeService = (NodeService) ctx.getBean("NodeService");
        contentService = (ContentService) ctx.getBean("ContentService");
        nodeArchiveService = (NodeArchiveService)ctx.getBean("nodeArchiveService");
        transactionHelper = (RetryingTransactionHelper) ctx.getBean("retryingTransactionHelper");
        
        ChildApplicationContextFactory activitiesFeed = (ChildApplicationContextFactory) ctx.getBean("ActivitiesFeed");
        ApplicationContext activitiesFeedCtx = activitiesFeed.getApplicationContext();
        postLookup = (PostLookup) activitiesFeedCtx.getBean("postLookup");
        feedGenerator = (FeedGenerator) activitiesFeedCtx.getBean("feedGenerator");
        
        LocalFeedTaskProcessor feedProcessor = (LocalFeedTaskProcessor) activitiesFeedCtx.getBean("feedTaskProcessor");
        
        List<String> templateSearchPaths = new ArrayList<String>(1);
        templateSearchPaths.add(TEST_TEMPLATES_LOCATION);
        feedProcessor.setTemplateSearchPaths(templateSearchPaths);
        feedProcessor.setUseRemoteCallbacks(false);
    }
    
    @AfterClass public static void restartQuartzScheduler() throws SchedulerException
    {
        // We put the scheduler in standby mode BeforeClass. Now we must restore it.
        QUARTZ_SCHEDULER.start();
        AuthenticationUtil.clearCurrentSecurityContext();
    }
    
    @Before public void createTestSites() throws Exception
    {
        ADMIN = AuthenticationUtil.getAdminUserName();
        
        final String guid = GUID.generate();
        
        // admin creates the test sites. This is how this test case was before refactoring. TODO Probably better to have a non-admin user create the sites.
        publicSite   = testSites.createSite("sitePreset", "pub" + guid,   "", "", SiteVisibility.PUBLIC,  ADMIN);
        privateSite1 = testSites.createSite("sitePreset", "priv1" + guid, "", "", SiteVisibility.PRIVATE, ADMIN);
        privateSite2 = testSites.createSite("sitePreset", "priv2" + guid, "", "", SiteVisibility.PRIVATE, ADMIN);
        modSite1     = testSites.createSite("sitePreset", "mod1" + guid,  "", "", SiteVisibility.MODERATED, ADMIN);
        modSite2     = testSites.createSite("sitePreset", "mod2" + guid, "", "",  SiteVisibility.MODERATED, ADMIN);
        log.debug("Created some test sites...");
        
        AuthenticationUtil.clearCurrentSecurityContext();
        // test site cleanup is handled automatically by the JUnit Rule.
    }
    
    protected NodeRef addTextContent(String siteId, String name)
    {
        String textData = name;
        String mimeType = MimetypeMap.MIMETYPE_TEXT_PLAIN;
                
        Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
        contentProps.put(ContentModel.PROP_NAME, name);
        
        // ensure that the Document Library folder is pre-created so that test code can start creating content straight away.
        // At the time of writing V4.1 does not create this folder automatically, but Thor does
        NodeRef parentRef = siteService.getContainer(siteId, SiteService.DOCUMENT_LIBRARY);
        if (parentRef == null)
        {
            parentRef = siteService.createContainer(siteId, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);
        }
        
        ChildAssociationRef association = nodeService.createNode(parentRef,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                ContentModel.TYPE_CONTENT,
                contentProps);
        
        NodeRef content = association.getChildRef();
        
        // add titled aspect (for Web Client display)
        Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>();
        titledProps.put(ContentModel.PROP_TITLE, name);
        titledProps.put(ContentModel.PROP_DESCRIPTION, name);
        nodeService.addAspect(content, ContentModel.ASPECT_TITLED, titledProps);
        
        ContentWriter writer = contentService.getWriter(content, ContentModel.PROP_CONTENT, true);
        
        writer.setMimetype(mimeType);
        writer.setEncoding("UTF-8");
        
        writer.putContent(textData);
        
        activityService.postActivity("org.alfresco.documentlibrary.file-added", siteId, "documentlibrary", content, name, ContentModel.PROP_CONTENT, parentRef);
        
        return content;
    }
    
    protected void generateFeed() throws Exception
    {
        postLookup.execute();
        feedGenerator.execute();
    }
    
    @Test public void testFollowingActivity() throws Exception
    {
        // We'll change things in the system in order to cause the generation of activity events and compare the feeds with these totals as we go.
        // Initially, both users have zero activity feed entries.
        // Java's requirement for final modifiers on variables accessed within inner classes means we can't use simple ints here.
        
        doWorkAs(ADMIN, new RetryingTransactionCallback<Void>()
                {
                    @Override public Void execute() throws Throwable
                    {
                        for (SiteInfo s : new SiteInfo[] { publicSite, privateSite1, privateSite2, modSite1, modSite2 })
                        {
                            siteService.setMembership(s.getShortName(), USER_ONE_NAME, SiteModel.SITE_MANAGER);
                        }
                        return null;
                    }
                });
        log.debug("Made user '" + USER_ONE_NAME + "' a SiteManager in each test site.");
        
        
        doWorkAs(ADMIN, new RetryingTransactionCallback<Void>()
                {
                    @Override public Void execute() throws Throwable
                    {
                        log.debug("Now to check if the activity tables have the correct number of entries for our test users");
                        
                        List<String> feed = activityService.getUserFeedEntries(USER_ONE_NAME, null, false, false, null, null);
                        assertEquals(USER_ONE_NAME + " had wrong feed size.", 0, feed.size());
                        
                        feed = activityService.getUserFeedEntries(USER_TWO_NAME, null, false, false, null, null);
                        assertEquals(USER_TWO_NAME + " had wrong feed size.", 0, feed.size());
                        return null;
                    }
                });
        
        
        // userId1 + 5, userId2 + 0
        generateFeed();
        doWorkAs(ADMIN, new RetryingTransactionCallback<Void>()
                {
                    @Override public Void execute() throws Throwable
                    {
                        List<String> feed = activityService.getUserFeedEntries(USER_ONE_NAME, null, false, false, null, null);
                        log.debug(USER_ONE_NAME + "'s feed: " + prettyJson(feed));
                        assertEquals(USER_ONE_NAME + " had wrong feed size", 5, feed.size());
                        
                        feed = activityService.getUserFeedEntries(USER_TWO_NAME, null, false, false, null, null);
                        log.debug(USER_TWO_NAME + "'s feed: " + prettyJson(feed));
                        assertEquals(USER_TWO_NAME + " had wrong feed size", 0, feed.size());
                        return null;
                    }
                });
        
        
        doWorkAs(USER_ONE_NAME, new RetryingTransactionCallback<Void>()
                {
                    @Override public Void execute() throws Throwable
                    {
                        subscriptionService.follow(USER_ONE_NAME, USER_TWO_NAME);
                        return null;
                    }
                });
        log.debug(USER_ONE_NAME + " is now following " + USER_TWO_NAME);
        
        
        doWorkAs(USER_TWO_NAME, new RetryingTransactionCallback<Void>()
                {
                    @Override public Void execute() throws Throwable
                    {
                        subscriptionService.follow(USER_TWO_NAME, USER_ONE_NAME);
                        return null;
                    }
                });
        log.debug("And " + USER_TWO_NAME + " is now following " + USER_ONE_NAME);
        
        
        // userId1 + 2, userId2 + 2
        generateFeed();
        doWorkAs(ADMIN, new RetryingTransactionCallback<Void>()
                {
                    @Override public Void execute() throws Throwable
                    {
                        List<String> feed = activityService.getUserFeedEntries(USER_ONE_NAME, null, false, false, null, null);
                        log.debug(USER_ONE_NAME + "'s feed: " + prettyJson(feed));
                        assertEquals(USER_ONE_NAME + "'s feed was wrong size", 7, feed.size());
                        
                        feed = activityService.getUserFeedEntries(USER_TWO_NAME, null, false, false, null, null);
                        log.debug(USER_TWO_NAME + "'s feed: " + prettyJson(feed));
                        assertEquals(USER_TWO_NAME + "'s feed was wrong size", 2, feed.size());
                        return null;
                    }
                });
        
        
        doWorkAs(USER_ONE_NAME, new RetryingTransactionCallback<Void>()
                {
                    @Override public Void execute() throws Throwable
                    {
                        addTextContent(publicSite.getShortName(),   USER_ONE_NAME+"pub-a");
                        addTextContent(privateSite1.getShortName(), USER_ONE_NAME+"priv1-a");
                        addTextContent(privateSite2.getShortName(), USER_ONE_NAME+"priv2-a");
                        addTextContent(modSite1.getShortName(),     USER_ONE_NAME+"mod1-a");
                        addTextContent(modSite2.getShortName(),     USER_ONE_NAME+"mod2-a");
                        return null;
                    }
                });
        log.debug(USER_ONE_NAME + " added some content across the sites.");
        
        
        // userId1 + 5, userId2 + 1
        generateFeed();
        doWorkAs(ADMIN, new RetryingTransactionCallback<Void>()
                {
                    @Override public Void execute() throws Throwable
                    {
                        List <String> feed = activityService.getUserFeedEntries(USER_ONE_NAME, null, false, false, null, null);
                        log.debug(USER_ONE_NAME + "'s feed: " + prettyJson(feed));
                        assertEquals(USER_ONE_NAME + "'s feed was wrong size", 12, feed.size());
                        
                        // note: userId2 should not see activities from followers in moderated sites that they do not belong do (ALF-16460)
                        feed = activityService.getUserFeedEntries(USER_TWO_NAME, null, false, false, null, null);
                        log.debug(USER_TWO_NAME + "'s feed: " + prettyJson(feed));
                        assertEquals(USER_TWO_NAME + "'s feed was wrong size", 3, feed.size());
        
                        siteService.setMembership(privateSite2.getShortName(), USER_TWO_NAME, SiteModel.SITE_CONSUMER);
                        siteService.setMembership(modSite2.getShortName(),     USER_TWO_NAME, SiteModel.SITE_MANAGER);
                        
                        log.debug(USER_TWO_NAME + "'s role changed on some sites.");
                        return null;
                    }
                });
        
        
        // userId1 + 2, userId2 + 2
        generateFeed();
        doWorkAs(ADMIN, new RetryingTransactionCallback<Void>()
                {
                    @Override public Void execute() throws Throwable
                    {
                        List <String> feed = activityService.getUserFeedEntries(USER_ONE_NAME, null, false, false, null, null);
                        log.debug(USER_ONE_NAME + "'s feed: " + prettyJson(feed));
                        assertEquals(USER_ONE_NAME + "'s feed was wrong size", 14, feed.size());
                        
                        // note: userId2 should not see activities from followers in moderated sites that they do not belong do (ALF-16460)
                        feed = activityService.getUserFeedEntries(USER_TWO_NAME, null, false, false, null, null);
                        log.debug(USER_TWO_NAME + "'s feed: " + prettyJson(feed));
                        assertEquals(USER_TWO_NAME + "'s feed was wrong size", 5, feed.size());
                        return null;
                    }
                });
        
        
        doWorkAs(USER_ONE_NAME, new RetryingTransactionCallback<Void>()
                {
                    @Override public Void execute() throws Throwable
                    {
                        addTextContent(publicSite.getShortName(),   USER_ONE_NAME+"pub-b");
                        addTextContent(privateSite1.getShortName(), USER_ONE_NAME+"priv1-b");
                        addTextContent(privateSite2.getShortName(), USER_ONE_NAME+"priv2-b");
                        addTextContent(modSite1.getShortName(),     USER_ONE_NAME+"mod1-b");
                        addTextContent(modSite2.getShortName(),     USER_ONE_NAME+"mod2-b");
                        return null;
                    }
                });
        log.debug(USER_ONE_NAME + " has added some more content...");
        
        
        // userId1 + 5, userId2 + 3
        generateFeed();
        doWorkAs(ADMIN, new RetryingTransactionCallback<Void>()
                {
                    @Override public Void execute() throws Throwable
                    {
                        List<String> feed = activityService.getUserFeedEntries(USER_ONE_NAME, null, false, false, null, null);
                        assertEquals("User's feed was wrong size", 19, feed.size());
                        
                        // note: userId2 should not see activities from followers in moderated sites that they do not belong to (ALF-16460)
                        feed = activityService.getUserFeedEntries(USER_TWO_NAME, null, false, false, null, null);
                        assertEquals("User's feed was wrong size", 8, feed.size());
                        
                        return null;
                    }
                });
        
        
        log.debug("Now to delete the test sites...");
        doWorkAs(ADMIN, new RetryingTransactionCallback<Void>()
                {
                    @Override public Void execute() throws Throwable
                    {
                        for (SiteInfo s : new SiteInfo[] { publicSite, privateSite1, privateSite2, modSite1, modSite2 })
                        {
                            deleteSite(s.getShortName());
                        }
                        log.debug("Deleted all the test sites.");
                        
                        return null;
                    }
                });
        
        doWorkAs(ADMIN, new RetryingTransactionCallback<Void>()
                {
                    @Override public Void execute() throws Throwable
                    {
                        // Feeds should now be reduced to 'follow' events only - see FeedCleaner's behaviours/policies/transaction listeners.
                        List<String> feed = activityService.getUserFeedEntries(USER_ONE_NAME, null, false, false, null, null);
                        log.debug(USER_ONE_NAME + "'s feed:\n" + prettyJson(feed));
                        assertEquals("User's feed was wrong size", 2, feed.size());
                        
                        feed = activityService.getUserFeedEntries(USER_TWO_NAME, null, false, false, null, null);
                        assertEquals("User's feed was wrong size", 2, feed.size());
                        
                        return null;
                    }
                });
        
        
        log.debug("Now to delete the users...");
        doWorkAs(ADMIN, new RetryingTransactionCallback<Void>()
                {
                    @Override public Void execute() throws Throwable
                    {
                        for (String user : new String[] { USER_ONE_NAME, USER_TWO_NAME })
                        {
                            deletePerson(user);
                        }
                        log.debug("Deleted the test people.");
                        
                        return null;
                    }
                });
        
        
        doWorkAs(ADMIN, new RetryingTransactionCallback<Void>()
                {
                    @Override public Void execute() throws Throwable
                    {
                        // Now both users should be reduced to having no events.
                        // FIXME So shouldn't these numbers be down to 0 now?
                        // Use log4j.logger.org.alfresco.repo.activities.feed.cleanup.FeedCleaner=trace to see the FeedCleaner's work
                        
                        List<String> feed = activityService.getUserFeedEntries(USER_ONE_NAME, null, false, false, null, null);
                        log.debug("User1's feed: " + prettyJson(feed));
                        
                        assertEquals("User's feed was wrong size", 0, feed.size());
                        
                        feed = activityService.getUserFeedEntries(USER_TWO_NAME, null, false, false, null, null);
                        assertEquals("User's feed was wrong size", 0, feed.size());
                        
                        return null;
                    }
                });
    }
    
    private void deleteSite(String siteShortName)
    {
        SiteInfo siteInfo = siteService.getSite(siteShortName);
        if (siteInfo != null)
        {
            log.debug("Deleting site: " + siteShortName);
            siteService.deleteSite(siteShortName);
            nodeArchiveService.purgeArchivedNode(nodeArchiveService.getArchivedNode(siteInfo.getNodeRef()));
        }
        else
        {
            log.debug("Not deleting site: " + siteShortName + ", as it doesn't appear to exist");
        }
    }
    
    private void deletePerson(String userName)
    {
        if (personService.personExists(userName))
        {
            log.debug("Deleting person: " + userName);
            personService.deletePerson(userName);
        }
        else
        {
            log.debug("Not deleting person: " + userName + ", as they don't appear to exist");
        }
    }
    
    // Just adding a little helper method to make the above code more readable. Oh Java 8, how we need you! (Or Groovy, or Scala...)
    private <T> T doWorkAs(final String userName, final RetryingTransactionCallback<T> work)
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<T>()
        {
            @Override public T doWork() throws Exception
            {
                return transactionHelper.doInTransaction(work);
            }
        }, userName);
    }
    
    private String prettyJson(List<String> jsonStrings)
    {
        StringBuilder result = new StringBuilder();
        for (String jsonString : jsonStrings)
        {
            result.append(prettyJson(jsonString));
            result.append("\n");
        }
        return result.toString();
    }
    
    private String prettyJson(String jsonString)
    {
        String result = jsonString;
        try
        {
            JSONObject json = new JSONObject(new JSONTokener(jsonString));
            result = json.toString(2);
        } catch (JSONException ignored)
        {
            // Intentionally empty
        }
        return result;
    }
}