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
package org.alfresco.repo.activities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.repo.domain.activities.ActivityPostDAO;
import org.alfresco.repo.domain.activities.ActivityPostEntity;
import org.alfresco.repo.jscript.ClasspathScriptLocation;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.activities.FeedControl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

/**
 * Activity Service Implementation unit test
 * 
 * @author janv
 * @since 3.0
 */
@Category(OwnJVMTestsCategory.class)
public class ActivityServiceImplTest extends TestCase 
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private ActivityService activityService;
    private ScriptService scriptService;
    private MutableAuthenticationService authenticationService;
    private SiteService siteService;
    private TransactionService transactionService;
    private ActivityPostDAO postDAO;
    private NodeArchiveService nodeArchiveService;
    
    private static final String ADMIN_PW = "admin";
    
    private static final String USER_UN = "bob";
    private static final String USER_PW = "bob";
    
    private static final String TEST_RUN_ID = ""+System.currentTimeMillis();
    
    @Override
    protected void setUp() throws Exception
    {
        activityService = (ActivityService)ctx.getBean("activityService");
        scriptService = (ScriptService)ctx.getBean("ScriptService");
        siteService = (SiteService)ctx.getBean("SiteService");
        nodeArchiveService = (NodeArchiveService)ctx.getBean("nodeArchiveService");
        transactionService = (TransactionService)ctx.getBean("TransactionService");
        
        postDAO = (ActivityPostDAO)ctx.getBean("postDAO");
        
        authenticationService = (MutableAuthenticationService)ctx.getBean("AuthenticationService");
        
        authenticationService.authenticate(AuthenticationUtil.getAdminUserName(), ADMIN_PW.toCharArray());
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        authenticationService.clearCurrentSecurityContext();
    }
    
    public void testPostValidActivities() throws Exception
    {
        activityService.postActivity("org.alfresco.testActivityType1", null, null, "");
        
        activityService.postActivity("org.alfresco.testActivityType2", "", "", "");
        
        activityService.postActivity("org.alfresco.testActivityType3", "site1", "appToolA", "{ \"var1\" : \"val1\" }");
    }
    
    public void testPostInvalidActivities() throws Exception
    {
        try
        {
            activityService.postActivity("", "", "",(NodeRef) null, "");
            fail("invalid post activity");
        }
        catch (IllegalArgumentException iae)
        {
            assertTrue(iae.getMessage().contains("nodeRef is a mandatory parameter"));
        }
        
        try
        {
            activityService.postActivity("", "", "", "");
            fail("invalid post activity");
        }
        catch (IllegalArgumentException iae)
        {
            assertTrue(iae.getMessage().contains("activityType is a mandatory parameter"));
        }
        
        try
        {
            activityService.postActivity("org.alfresco.testActivityType1", "", "", "{ \"nodeRef\" : \"notfound\" }");
            fail("invalid post activity: bad nodeRef");
        }
        catch (IllegalArgumentException iae)
        {
            assertTrue(iae.getMessage().contains("Invalid node ref: notfound"));
        }
    }
    
    public void testGetEmptySiteFeed() throws Exception
    {
        if(! authenticationService.authenticationExists(USER_UN))
        {
            authenticationService.createAuthentication(USER_UN, USER_PW.toCharArray());
        }
        
        authenticationService.clearCurrentSecurityContext();
        
        authenticationService.authenticate(USER_UN, USER_PW.toCharArray());
        
        final String siteId = "emptySite-"+TEST_RUN_ID;
        siteService.createSite("mypreset", siteId, "empty site title", "empty site description", SiteVisibility.PUBLIC);

        RetryingTransactionCallback<List<String>> getEntriesCallback = new RetryingTransactionCallback<List<String>>()
        {
            @Override
            public List<String> execute() throws Throwable
            {
                return activityService.getSiteFeedEntries(siteId);
            }
        };
        List<String> siteFeedEntries = transactionService.getRetryingTransactionHelper().doInTransaction(getEntriesCallback);
        
        assertNotNull(siteFeedEntries);
        assertTrue(siteFeedEntries.isEmpty());
        SiteInfo siteInfo = siteService.getSite(siteId);
        siteService.deleteSite(siteId);
        nodeArchiveService.purgeArchivedNode(nodeArchiveService.getArchivedNode(siteInfo.getNodeRef()));
    }
    
    public void testGetEmptyUserFeed() throws Exception
    {
        List<String> userFeedEntries = activityService.getUserFeedEntries("unknown user", null);
        
        assertNotNull(userFeedEntries);
        assertTrue(userFeedEntries.isEmpty());
        
        userFeedEntries = activityService.getUserFeedEntries("unknown user", "some site");
        
        assertNotNull(userFeedEntries);
        assertTrue(userFeedEntries.isEmpty());
        
        userFeedEntries = activityService.getUserFeedEntries("unknown user", "some site", true, false, null, null);
        
        assertNotNull(userFeedEntries);
        assertTrue(userFeedEntries.isEmpty());
        
        userFeedEntries = activityService.getUserFeedEntries("unknown user", "some site", false, true, null, null);
        
        assertNotNull(userFeedEntries);
        assertTrue(userFeedEntries.isEmpty());
        
        userFeedEntries = activityService.getUserFeedEntries("unknown user", "some site", true, true, null, null);
        
        assertNotNull(userFeedEntries);
        assertTrue(userFeedEntries.isEmpty());
    }
    
    public void testJSAPI() throws Exception
    {
        ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/activities/script/test_activityService.js");
        String result = (String)this.scriptService.executeScript(location, new HashMap<String, Object>(0));
        
        // Check the result and fail if message returned
        if (result != null && result.length() != 0)
        {
            fail("The activity service test JS script failed: " + result);
        }
    }
    
    public void testFeedControls() throws Exception
    {
        List<FeedControl> feedControls = activityService.getFeedControls(USER_UN);
        assertNotNull(feedControls);
        assertTrue(feedControls.isEmpty());
        
        if(! authenticationService.authenticationExists(USER_UN))
        {
            authenticationService.createAuthentication(USER_UN, USER_PW.toCharArray());
        }
        
        authenticationService.clearCurrentSecurityContext();
        
        authenticationService.authenticate(USER_UN, USER_PW.toCharArray());
        
        feedControls = activityService.getFeedControls();
        assertNotNull(feedControls);
        assertTrue(feedControls.isEmpty());
        
        assertFalse(activityService.existsFeedControl(new FeedControl("mySite1", "appTool1")));
        
        activityService.setFeedControl(new FeedControl("mySite1", null));
        activityService.setFeedControl(new FeedControl("mySite1", "appTool1"));
        activityService.setFeedControl(new FeedControl(null, "appTool2"));
        
        feedControls = activityService.getFeedControls();
        assertEquals(3, feedControls.size());
        
        feedControls = activityService.getFeedControls(USER_UN);
        assertEquals(3, feedControls.size());
        
        assertTrue(activityService.existsFeedControl(new FeedControl("mySite1", "appTool1")));
        
        activityService.unsetFeedControl(new FeedControl("mySite1", "appTool1"));
        
        assertFalse(activityService.existsFeedControl(new FeedControl("mySite1", "appTool1")));
        
        feedControls = activityService.getFeedControls();
        assertEquals(2, feedControls.size());
        
        activityService.unsetFeedControl(new FeedControl("mySite1", null));
        activityService.unsetFeedControl(new FeedControl(null, "appTool2"));
        
        feedControls = activityService.getFeedControls();
        assertEquals(0, feedControls.size());
    }
    
    public void testLongName_ALF_10362() throws Exception
    { 
        byte [] namePattern = new byte[1024]; 
        Arrays.fill(namePattern, (byte) 'A');
        
        ActivityPostEntity params = new ActivityPostEntity();
        params.setStatus(ActivityPostEntity.STATUS.PENDING.toString());
        
        int cnt = postDAO.selectPosts(params, -1).size();
        
        activityService.postActivity("org.alfresco.testActivityType4", "site2", "appToolA", "{\"title\":\"" + new String(namePattern, "UTF-8") + "\"}");
        
        assertEquals(cnt+1, postDAO.selectPosts(params, -1).size());
    }
}
