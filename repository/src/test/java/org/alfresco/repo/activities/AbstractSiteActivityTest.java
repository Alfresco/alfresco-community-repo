/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.repo.activities;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.activities.feed.FeedGenerator;
import org.alfresco.repo.activities.feed.local.LocalFeedTaskProcessor;
import org.alfresco.repo.activities.post.lookup.PostLookup;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.activities.FeedControl;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.quartz.Scheduler;
import org.springframework.context.ApplicationContext;

/**
 * Simple Activity Service unit test using site (membership) activities
 * 
 * @author janv
 */
public abstract class AbstractSiteActivityTest
{
    private static Log logger = LogFactory.getLog(AbstractSiteActivityTest.class);
    
    private ApplicationContext applicationContext;
    
    private SiteService siteService;
    private ActivityService activityService;
    private MutableAuthenticationService authenticationService;
    private PersonService personService;
    private PostLookup postLookup;
    private FeedGenerator feedGenerator;
    private NodeArchiveService nodeArchiveService;
    
    //
    // Test config & data
    //
    
    // Location of activity type templates (for site activities)
    private static final String TEST_TEMPLATES_LOCATION = "activities"; // assumes test-resources is on classpath
    
    // Test users
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PW = "admin";
    
    private static String user1 = null;
    private static String user2 = null;
    private static String user3 = null;
    private static String user4 = null;
    
    private static final String USER_PW = "password";
    
    // Test sites
    private static String site1 = null;
    private static String site2 = null;
    private static String site3 = null;
    
    // AppToolId for site membership activities
    private static String appToolId = "siteService"; // refer to SiteService
    
    @Before
    public void setUp() throws Exception
    {
        applicationContext = ApplicationContextHelper.getApplicationContext();
        String testid = "" + System.currentTimeMillis();
        
        // Let's shut down the scheduler so that we aren't competing with the scheduled versions of the post lookup and
        // feed generator jobs
        Scheduler scheduler = (Scheduler) applicationContext.getBean("schedulerFactory");
        scheduler.shutdown();
        
        // Get the required services
        this.activityService = (ActivityService)applicationContext.getBean("activityService");
        this.siteService = (SiteService)applicationContext.getBean("SiteService");
        this.authenticationService = (MutableAuthenticationService)applicationContext.getBean("AuthenticationService");
        this.personService = (PersonService)applicationContext.getBean("PersonService");
        this.nodeArchiveService = (NodeArchiveService)applicationContext.getBean("nodeArchiveService");
        
        LocalFeedTaskProcessor feedProcessor = null;
        
        // alternative: would need to add subsystem context to config location (see above)
        //this.postLookup = (PostLookup)applicationContext.getBean("postLookup");
        //this.feedGenerator = (FeedGenerator)applicationContext.getBean("feedGenerator");
        //feedProcessor = (LocalFeedTaskProcessor)applicationContext.getBean("feedTaskProcessor");
        
        ChildApplicationContextFactory activitiesFeed = (ChildApplicationContextFactory)applicationContext.getBean("ActivitiesFeed");
        ApplicationContext activitiesFeedCtx = activitiesFeed.getApplicationContext();
        this.postLookup = (PostLookup)activitiesFeedCtx.getBean("postLookup");
        this.feedGenerator = (FeedGenerator)activitiesFeedCtx.getBean("feedGenerator");
        feedProcessor = (LocalFeedTaskProcessor)activitiesFeedCtx.getBean("feedTaskProcessor");
        
        
        List<String> templateSearchPaths = new ArrayList<String>(1);
        templateSearchPaths.add(TEST_TEMPLATES_LOCATION);
        feedProcessor.setTemplateSearchPaths(templateSearchPaths);
        feedProcessor.setUseRemoteCallbacks(false);
        
        site1 = "test_site1_" + testid;
        site2 = "test_site2_" + testid;
        site3 = "test_site3_" + testid;
        
        user1 = "Test_User1_" + testid;
        user2 = "Test_User2_" + testid;
        user3 = "Test_User3_" + testid;
        user4 = "Test_User4_" + testid;
        
        
        // create users
        
        login(ADMIN_USER, ADMIN_PW);
        
        createUser(user1, USER_PW);
        createUser(user2, USER_PW);
        createUser(user3, USER_PW);
        createUser(user4, USER_PW);
        
        // create sites
        
        // create public site
        createSite(site1, true);
        
        // create private sites
        createSite(site2, false);
        createSite(site3, false);
    }
    
    @After
    public void tearDown() throws Exception
    {
        login(ADMIN_USER, ADMIN_PW);
        
        deleteUser(user1);
        deleteUser(user2);
        deleteUser(user3);
        deleteUser(user4);
        
        deleteSite(site1); 
        deleteSite(site2);
        deleteSite(site3);
    }
    
    protected void createSite(String siteId, boolean isPublic) throws Exception
    {
        siteService.createSite("myPreset", siteId, "myTitle", "myDescription", (isPublic ? SiteVisibility.PUBLIC : SiteVisibility.PRIVATE));
        
        if (logger.isDebugEnabled())
        {
            logger.debug("createdSite: " + siteId);
        }
    }
    
    protected void deleteSite(String siteId) throws Exception
    {
        SiteInfo siteInfo = siteService.getSite(siteId);
        if (siteInfo != null)
        {
            // delete site (and site's associated groups)
            siteService.deleteSite(siteId);
            nodeArchiveService.purgeArchivedNode(nodeArchiveService.getArchivedNode(siteInfo.getNodeRef()));
        }
    }
    
    @Test
    //MNT-9104 If username contains uppercase letters the action of joining a site will not be displayed in "My activities" 
    public void testUserActivitiesOnSite() throws Exception
    {
        login(ADMIN_USER, ADMIN_PW);
        
        addMembership(site1, user4, SiteModel.SITE_CONSUMER);
        
        generateFeed();
        
        login(user4, USER_PW);
        
        getUserFeed(user4, site1, false, false, true, 1);
    }
    
    @Test
    public void testGetSiteFeedsBefore() throws Exception
    {
        login(ADMIN_USER, ADMIN_PW);
        
        getSiteFeed(site1, 0);
        getSiteFeed(site2, 0); // site 2 is private, but accessible to admins
        getSiteFeed(site3, 0); // site 3 is private, but accessible to admins
        
        login(user4, USER_PW);
        
        getSiteFeed(site1, 0); // site 1 is public, hence site feed is accessible to any user of the system
        
        try
        {
            getSiteFeed(site2, 0); // site 2 is private, hence only accessible to members or admins
            
            fail("Site feed for private site should not be accessible to non-admin / non-member");
        }
        catch (AccessDeniedException ade)
        {
            // ignore
        }
        
        try
        {
            getSiteFeed(site3, 0); // site 3 is private, hence only accessible to members or admins
            
            fail("Site feed for private site should not be accessible to non-admin / non-member");
        }
        catch (AccessDeniedException ade)
        {
            // ignore
        }
    }
    
    protected void getSiteFeed(String siteId, int expectedCount) throws Exception
    {
        assertEquals(expectedCount, activityService.getSiteFeedEntries(siteId).size());
    }
    
    @Test
    public void testGetUserFeedsBefore() throws Exception
    {
        // as admin
        
        login(ADMIN_USER, ADMIN_PW);
        
        getUserFeed(user1, true, 0);
        getUserFeed(user2, true, 0);
        getUserFeed(user3, true, 0);
        getUserFeed(user4, true, 0);
        
        // as user1
        
        login(user1, USER_PW);
        
        getUserFeed(user1, false, 0);
        
        // as user2
        
        login(user2, USER_PW);
        
        try
        {
            getUserFeed(user1, true, 0);
            
            fail("User feed should only be accessible to user or an admin");
        }
        catch (AccessDeniedException ade)
        {
            // ignore
        }
        
        
        // as user1 - with filter args ...
        
        login(user1, USER_PW);
        
        getUserFeed(null, site1, false, false, false, 0);
        getUserFeed(null, site2, false, false, false, 0);
        getUserFeed(null, site3, false, false, false, 0);
        
        getUserFeed(null, null, false, true, false, 0);
        getUserFeed(null, null, false, false, true, 0);
        getUserFeed(null, null, false, true, true, 0);
    }
    
    protected void getUserFeed(String userId, boolean isAdmin, int expectedCount) throws Exception
    {
        getUserFeed(userId, null, isAdmin, false, false, expectedCount);
    }
    
    protected void getUserFeed(String userId, String siteId, boolean isAdmin, boolean excludeThisUser, boolean excludeOtherUsers, int expectedCount) throws Exception
    {
        if (userId == null)
        {
            userId = AuthenticationUtil.getFullyAuthenticatedUser();
        }
        assertEquals(expectedCount, activityService.getUserFeedEntries(userId, siteId, excludeThisUser, excludeOtherUsers, null, null).size());
    }

    public void configureFeedControls() throws Exception
    {
        // user 1 opts out of all activities for site 1
        login(user1, USER_PW);
        addFeedControl(site1, null);

        // user 2 opts out of site membership activities (across all sites)
        login(user2, USER_PW);
        addFeedControl(null, appToolId);

        // user 3 opts out of site membership activities for site 1 only
        login(user3, USER_PW);
        addFeedControl(site1, appToolId);

        // TODO add more here, once we have more appToolIds
    }

    public void addAndUpdateMemberships() throws Exception
    {
        login(ADMIN_USER, ADMIN_PW);

        addAndUpdateMemberships(site1, true);  // public site, include all users
        addAndUpdateMemberships(site2, true);  // private site, include all users
        addAndUpdateMemberships(site3, false); // private site, do not include user 4

        generateFeed();
    }
    
    @Test
    public void testGetSiteFeedsAfterAddAndUpdateMemberships() throws Exception
    {
        addAndUpdateMemberships();
        
        login(ADMIN_USER, ADMIN_PW);
        
        getSiteFeed(site1, 8); // 8 = 4 users, each with 1 join, 1 role change
        getSiteFeed(site2, 8); // 8 = 4 users, each with 1 join, 1 role change
        getSiteFeed(site3, 6); // 6 = 3 users, each with 1 join, 1 role change (not user 4)
        
        login(user4, USER_PW);
        
        getSiteFeed(site1, 8);
        getSiteFeed(site2, 8); // site 2 is private, user 4 is a member
        
        try
        {
            getSiteFeed(site3, 0); // site 3 is private, user 4 is not a member
            
            fail("Site feed for private site should not be accessible to non-admin / non-member");
        }
        catch (AccessDeniedException ade)
        {
            // ignore
        }
    }

    public void removeMemberships() throws Exception
    {
            login(ADMIN_USER, ADMIN_PW);
            
            removeMemberships(site1, true);
            removeMemberships(site2, true);
            removeMemberships(site3, false);
            
            generateFeed();
    }
    
    protected void addAndUpdateMemberships(String siteId, boolean includeUser4) throws Exception
    {
        // add member -> join site
        addMembership(siteId, user1, SiteModel.SITE_CONSUMER);
        addMembership(siteId, user2, SiteModel.SITE_MANAGER);
        addMembership(siteId, user3, SiteModel.SITE_COLLABORATOR);
        
        if (includeUser4) { addMembership(siteId, user4, SiteModel.SITE_CONSUMER); }
        
        // update member -> change role
        updateMembership(siteId, user1, SiteModel.SITE_MANAGER);
        updateMembership(siteId, user2, SiteModel.SITE_COLLABORATOR);
        updateMembership(siteId, user3, SiteModel.SITE_CONSUMER);
        
        if (includeUser4) { updateMembership(siteId, user4, SiteModel.SITE_COLLABORATOR); }
    }
    
    protected void removeMemberships(String siteId, boolean includeUser4) throws Exception
    {
        // remove member -> leave site
        removeMembership(siteId, user1);
        removeMembership(siteId, user2);
        removeMembership(siteId, user3);
        
        if (includeUser4) { removeMembership(siteId, user4); }
    }
    
    private void addFeedControl(String siteId, String appToolId) throws Exception
    {
        // set feed control for current user
        activityService.setFeedControl(new FeedControl(siteId, appToolId));
    }
    
    @Test
    public void testGetSiteFeedsAfterRemoveMemberships() throws Exception
    {
        addAndUpdateMemberships();
        removeMemberships();
        
        login(ADMIN_USER, ADMIN_PW);
        
        getSiteFeed(site1, 12); // 12 = 4 users, each with 1 join, 1 role change, 1 leave
        getSiteFeed(site2, 12); // 12 = 4 users, each with 1 join, 1 role change, 1 leave
        getSiteFeed(site3, 9);  //  9 = 3 users, each with 1 join, 1 role change, 1 leave (not user 4)
        
        login(user4, USER_PW);
        
        getSiteFeed(site1, 12);
        
        try
        {
            getSiteFeed(site2, 0); // site 2 is private, user 4 is no longer a member
            
            fail("Site feed for private site should not be accessible to non-admin / non-member");
        }
        catch (AccessDeniedException ade)
        {
            // ignore
        }
        
        try
        {
            getSiteFeed(site3, 0); // site 3 is private, user 4 was never a member
            
            fail("Site feed for private site should not be accessible to non-admin / non-member");
        }
        catch (AccessDeniedException ade)
        {
            // ignore
        }
    }
    
    @Test
    public void testGetUserFeedsAfterApplyingFeedControls() throws Exception
    {
        configureFeedControls();
        addAndUpdateMemberships();
        removeMemberships();
        
        // as admin
        
        login(ADMIN_USER, ADMIN_PW);
        
        // site 1, with 4 users, each with 1 join, 1 role change = 4x2 = 8
        // site 2, with 4 users, each with 1 join, 1 role change = 4x2 = 8
        // site 3, with 3 users, each with 1 join, 1 role change = 3x2 = 6
        // Every user is removed from sites so in its feed it sees the removal for each site
        
        // user 1 belongs to 3 sites and is removed from 3 sites  = (2x8)+(1x6)+3 = 25
        // user 2 belongs to 3 sites and is removed from 3 sites = (2x8)+(1x6)+3 = 25
        // user 3 belongs to 3 sites and is removed from 3 sites = (2x8)+(1x6)+3 = 25
        // user 4 belongs to 2 sites and is removed from 3 sites = (2x8)+2 = 18

        getUserFeed(user1, true, 16);  // 16 = (25 - 9) due to feed control - exclude site 1 (4 joins, 4 changes, 1 removal)
        getUserFeed(user2, true, 0);   // 0 = due to feed control - exclude site membership activities (across all sites)
        getUserFeed(user3, true, 16);  // 16 = (25 - 9) due to feed control - exclude site membership activities for site 1
        getUserFeed(user4, true, 18);  // 18 = no feed control
        
        // as user1
        
        login(user1, USER_PW);
        
        getUserFeed(user1, false, 16);
        
        // as user2
        
        login(user2, USER_PW);
        
        try
        {
            getUserFeed(user1, true, 16);
            
            fail("User feed should only be accessible to user or an admin");
        }
        catch (AccessDeniedException ade)
        {
            // ignore
        }
        
        // as user1 - with filter args ...
        
        login(user1, USER_PW);
        
        getUserFeed(null, site1, false, false, false, 0);
        getUserFeed(null, site2, false, false, false, 9);
        getUserFeed(null, site3, false, false, false, 7);
        
        getUserFeed(null, null, false, false, false, 16); // no filter
        getUserFeed(null, null, false, true, false, 12);  // exclude any from user1
        getUserFeed(null, null, false, false, true, 4);   // exclude all except user1
        getUserFeed(null, null, false, true, true, 0);    // exclude all (NOOP)
        
        // TODO - add more (eg. other non-admin user activities)
    }
    
    private void addMembership(String siteId, String userName, String role) throws Exception
    {  
        updateMembership(siteId, userName, role);
    }
    
    private void updateMembership(String siteId, String userName, String role) throws Exception
    {  
        siteService.setMembership(siteId, userName, role);
    }
    
    private void removeMembership(String siteId, String userName) throws Exception
    {
        siteService.removeMembership(siteId, userName);
    }
    
    protected void createUser(String userName, String password)
    {
        if (authenticationService.authenticationExists(userName) == false)
        {
            authenticationService.createAuthentication(userName, password.toCharArray());
            
            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");
            
            personService.createPerson(ppOne);
        }
    }
    
    protected void deleteUser(String userName)
    {
        personService.deletePerson(userName);
    }
    
    private void login(String username, String password)
    {
        AuthenticationUtil.setFullyAuthenticatedUser(username);
    }
    
    private void generateFeed() throws Exception
    {
        postLookup.execute();
        feedGenerator.execute();
    }
    
    @AfterClass
    // remove system "user.name.caseSensitive" property
    public static void afterClass()
    {
        System.clearProperty("user.name.caseSensitive");
    }
}
