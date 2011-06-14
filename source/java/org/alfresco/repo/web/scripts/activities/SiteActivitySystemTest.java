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
package org.alfresco.repo.web.scripts.activities;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.TestCase;

import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.web.scripts.activities.feed.UserFeedRetrieverWebScript;
import org.springframework.extensions.surf.util.Base64;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Simple Activity Service system test (requires remote repo to be running) using site (membership) activities
 * 
 * @author janv
 */
public class SiteActivitySystemTest extends TestCase
{
    private static Log logger = LogFactory.getLog(SiteActivitySystemTest.class);
    
    // TODO - use test property file
    private static final String REPO = "http://localhost:8080/alfresco";
    
    // web script (REST)
    private static final String WEBSCRIPT_ENDPOINT  = REPO + "/service";
    
    // Site Service part-URLs
    private static final String URL_SITES = "/api/sites";
    private static final String URL_MEMBERSHIPS = "/memberships";
    
    // Person Service part-URLs
    private static final String URL_PEOPLE = "/api/people";
    
    // Activity Service part-URLs
    private static final String URL_ACTIVITIES = "/api/activities";
    private static final String URL_SITE_FEED = "/feed/site";
    private static final String URL_USER_FEED = "/feed/user";
    private static final String URL_USER_FEED_CTRL = "/feed/control";
    
    // Test users & passwords
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
    
    private static int DELAY_MSECS = 120000; // 2 mins
    
    private static boolean setup = false;
    private static boolean sitesCreated = false;
    private static boolean membersAddedUpdated = false;
    private static boolean membersRemoved = false;
    private static boolean controlsCreated = false;
    
    
    public SiteActivitySystemTest()
    {
    }
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        if (! setup)
        {
            String testid = ""+System.currentTimeMillis();
            
            site1 = "test_site1_" + testid;
            site2 = "test_site2_" + testid;
            site3 = "test_site3_" + testid;
            
            user1 = "test_user1_" + testid;
            user2 = "test_user2_" + testid;
            user3 = "test_user3_" + testid;
            user4 = "test_user4_" + testid;
            
            // pre-create users
            
            String ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, ADMIN_USER, ADMIN_PW);
            assertNotNull(ticket);
            
            createUser(ticket, user1, USER_PW);
            createUser(ticket, user2, USER_PW);
            createUser(ticket, user3, USER_PW);
            createUser(ticket, user4, USER_PW);
            
            setup = true;
        }
       
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    public void testLogin() throws Exception
    {
        String ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, ADMIN_USER, ADMIN_PW);
        assertNotNull(ticket);
    }
    
    public void testCreateSites() throws Exception
    {
        if (! sitesCreated)
        {
            String ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, ADMIN_USER, ADMIN_PW);
            
            // create public site
            createSite(site1, true, ticket);
            
            // create private sites
            createSite(site2, false, ticket);
            createSite(site3, false, ticket);
            
            sitesCreated = true;
        }
    }
    
    protected void createSite(String siteId, boolean isPublic, String ticket) throws Exception
    {
        JSONObject site = new JSONObject();
        site.put("sitePreset", "myPreset");
        site.put("shortName", siteId);
        site.put("title", "myTitle");
        site.put("description", "myDescription");
        site.put("isPublic", isPublic);
        
        String url = WEBSCRIPT_ENDPOINT + URL_SITES;
        String response = callPostWebScript(url, ticket, site.toString());
        
        if (logger.isDebugEnabled())
        {
            logger.debug("createSite: " + siteId);
            logger.debug("----------");
            logger.debug(url);
            logger.debug(response);
        }
    }
    
    public void testGetSites() throws Exception
    {
        testCreateSites();
        
        String ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, ADMIN_USER, ADMIN_PW);
        
        getSite(site1, ticket);
        getSite(site2, ticket);
        getSite(site3, ticket);
    }
    
    protected void getSite(String siteId, String ticket) throws Exception
    {
        String url = WEBSCRIPT_ENDPOINT + URL_SITES + "/" + siteId;
        String response = callGetWebScript(url, ticket);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("getSite:" + siteId);
            logger.debug("-------");
            logger.debug(url);
            logger.debug(response);
        }
    }
    
    public void testGetSiteFeedsBefore() throws Exception
    {
        testCreateSites();
        
        String ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, ADMIN_USER, ADMIN_PW);
        
        getSiteFeed(site1, ticket, 0);
        getSiteFeed(site2, ticket, 0); // site 2 is private, but accessible to admins
        getSiteFeed(site3, ticket, 0); // site 3 is private, but accessible to admins
        
        ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, user4, USER_PW);
        
        getSiteFeed(site1, ticket, 0); // site 1 is public, hence site feed is accessible to any user of the system
        
        try
        {
            getSiteFeed(site2, ticket, 0); // site 2 is private, hence only accessible to members or admins
            
            fail("Site feed for private site should not be accessible to non-admin / non-member");
        }
        catch (IOException ioe)
        {
            assertTrue(ioe.getMessage().contains("HTTP response code: 401"));
        }
        
        try
        {
            getSiteFeed(site3, ticket, 0); // site 3 is private, hence only accessible to members or admins
            
            fail("Site feed for private site should not be accessible to non-admin / non-member");
        }
        catch (IOException ioe)
        {
            assertTrue(ioe.getMessage().contains("HTTP response code: 401"));
        }
    }
    
    protected void getSiteFeed(String siteId, String ticket, int expectedCount) throws Exception
    {
        String url = WEBSCRIPT_ENDPOINT + URL_ACTIVITIES + URL_SITE_FEED + "/" + siteId + "?format=json";
        String jsonArrayResult = callGetWebScript(url, ticket);
        
        if (jsonArrayResult != null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("getSiteFeed:" + siteId);
                logger.debug("-----------");
                logger.debug(url);
                logger.debug(jsonArrayResult);
            }
            
            JSONArray ja = new JSONArray(jsonArrayResult);
            assertEquals(expectedCount, ja.length());
        }
        else
        {
            fail("Error getting site feed");
        }
    }
    
    public void testGetUserFeedsBefore() throws Exception
    {
        testCreateSites();
        
        // as admin
        
        String ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, ADMIN_USER, ADMIN_PW);
        
        getUserFeed(user1, ticket, true, 0);
        getUserFeed(user2, ticket, true, 0);
        getUserFeed(user3, ticket, true, 0);
        getUserFeed(user4, ticket, true, 0);
        
        // as user1
        
        ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, user1, USER_PW);
        
        getUserFeed(user1, ticket, false, 0);
        
        // as user2
        
        ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, user2, USER_PW);
        
        try
        {
            getUserFeed(user1, ticket, true, 0);
            
            fail("User feed should only be accessible to user or an admin");
        }
        catch (IOException ioe)
        {
            assertTrue(ioe.getMessage().contains("HTTP response code: 401"));
        }
        
        
        // as user1 - with filter args ...
        
        ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, user1, USER_PW);
        
        getUserFeed(null, site1, ticket, false, false, false, 0);
        getUserFeed(null, site2, ticket, false, false, false, 0);
        getUserFeed(null, site3, ticket, false, false, false, 0);
        
        getUserFeed(null, null, ticket, false, true, false, 0);
        getUserFeed(null, null, ticket, false, false, true, 0);
        getUserFeed(null, null, ticket, false, true, true, 0);
    }
    
    protected void getUserFeed(String userId, String ticket, boolean isAdmin, int expectedCount) throws Exception
    {
        getUserFeed(userId, null, ticket, isAdmin, false, false, expectedCount);
    }
    
    protected void getUserFeed(String userId, String siteId, String ticket, boolean isAdmin, boolean excludeThisUser, boolean excludeOtherUsers, int expectedCount) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append(WEBSCRIPT_ENDPOINT).
           append(URL_ACTIVITIES).
           append(URL_USER_FEED).
           append(isAdmin ? "/" + userId : ""). // optional
           append("?").
           append((siteId != null) ? UserFeedRetrieverWebScript.PARAM_SITE_ID + "=" + siteId + "&": "").     // optional
           append(excludeThisUser ? UserFeedRetrieverWebScript.PARAM_EXCLUDE_THIS_USER + "=true&" : "").     // optional
           append(excludeOtherUsers ? UserFeedRetrieverWebScript.PARAM_EXCLUDE_OTHER_USERS + "=true&" : ""). // optional
           append("format=json");
        
        String url = sb.toString();
        String jsonArrayResult = callGetWebScript(url, ticket);
        
        if (jsonArrayResult != null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("getUserFeed:" + userId + (isAdmin ? "(as admin)" : ""));
                logger.debug("-----------");
                logger.debug(url);
                logger.debug(jsonArrayResult);
            }
            
            JSONArray ja = new JSONArray(jsonArrayResult);
            assertEquals(expectedCount, ja.length());
        }
        else
        {
            fail("Error getting user feed");
        }
    }
    
    public void testUserFeedControls() throws Exception
    {
        if (! controlsCreated)
        {
            // user 1 opts out of all activities for site 1
            String ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, user1, USER_PW);
            addFeedControl(user1, site1, null, ticket);
            
            // user 2 opts out of site membership activities (across all sites)
            ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, user2, USER_PW);
            addFeedControl(user2, null, appToolId, ticket);
            
            // user 3 opts out of site membership activities for site 1 only
            ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, user3, USER_PW);
            addFeedControl(user3, site1, appToolId, ticket);
            
            // TODO add more here, once we have more appToolIds
            
            controlsCreated = true;
        }
    }
    
    public void testAddAndUpdateMembershipsWithPause() throws Exception
    {
        if (! membersAddedUpdated)
        {
            testCreateSites();
            
            String ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, ADMIN_USER, ADMIN_PW);
           
            addAndUpdateMemberships(site1, ticket, true);  // public site, include all users
            addAndUpdateMemberships(site2, ticket, true);  // private site, include all users
            addAndUpdateMemberships(site3, ticket, false); // private site, do not include user 4
            
            // add pause - otherwise, activity service will not generate feed entries (since they will have already left the site)
            Thread.sleep(DELAY_MSECS);
            
            membersAddedUpdated = true;
        }
    }
    
    public void testGetSiteFeedsAfterAddAndUpdateMemberships() throws Exception
    {
        testCreateSites();
        testAddAndUpdateMembershipsWithPause();
        
        String ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, ADMIN_USER, ADMIN_PW);
        
        getSiteFeed(site1, ticket, 8); // 8 = 4 users, each with 1 join, 1 role change
        getSiteFeed(site2, ticket, 8); // 8 = 4 users, each with 1 join, 1 role change
        getSiteFeed(site3, ticket, 6); // 6 = 3 users, each with 1 join, 1 role change (not user 4)
        
        ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, user4, USER_PW);
        
        getSiteFeed(site1, ticket, 8);
        getSiteFeed(site2, ticket, 8); // site 2 is private, user 4 is a member
        
        try
        {
            getSiteFeed(site3, ticket, 0); // site 3 is private, user 4 is not a member
            
            fail("Site feed for private site should not be accessible to non-admin / non-member");
        }
        catch (IOException ioe)
        {
            assertTrue(ioe.getMessage().contains("HTTP response code: 401"));
        }
    }
    public void testRemoveMembershipsWithPause() throws Exception
    {
        if (! membersRemoved)
        {
            testCreateSites();
            testAddAndUpdateMembershipsWithPause();
            
            String ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, ADMIN_USER, ADMIN_PW);
            
            removeMemberships(site1, ticket, true);
            removeMemberships(site2, ticket, true);
            removeMemberships(site3, ticket, false);
            
            // add pause
            Thread.sleep(DELAY_MSECS);
            
            membersRemoved = true;
        }
    }
    
    protected void addAndUpdateMemberships(String siteId, String ticket, boolean includeUser4) throws Exception
    {
        // add member -> join site
        addMembership(siteId, user1, ticket, SiteModel.SITE_CONSUMER);
        addMembership(siteId, user2, ticket, SiteModel.SITE_MANAGER);
        addMembership(siteId, user3, ticket, SiteModel.SITE_COLLABORATOR);
        
        if (includeUser4) { addMembership(siteId, user4, ticket, SiteModel.SITE_CONSUMER); }
        
        // update member -> change role
        updateMembership(siteId, user1, ticket, SiteModel.SITE_MANAGER);
        updateMembership(siteId, user2, ticket, SiteModel.SITE_COLLABORATOR);
        updateMembership(siteId, user3, ticket, SiteModel.SITE_CONSUMER);
        
        if (includeUser4) { updateMembership(siteId, user4, ticket, SiteModel.SITE_COLLABORATOR); }
    }
    
    protected void removeMemberships(String siteId, String ticket, boolean includeUser4) throws Exception
    {
        // remove member -> leave site
        removeMembership(siteId, user1, ticket);
        removeMembership(siteId, user2, ticket);
        removeMembership(siteId, user3, ticket);
        
        if (includeUser4) { removeMembership(siteId, user4, ticket); }
    }
    
    public void testGetSiteFeedsAfterRemoveMemberships() throws Exception
    {
        testCreateSites();
        testAddAndUpdateMembershipsWithPause();
        testRemoveMembershipsWithPause();
        
        String ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, ADMIN_USER, ADMIN_PW);
        
        getSiteFeed(site1, ticket, 12); // 12 = 4 users, each with 1 join, 1 role change, 1 leave
        getSiteFeed(site2, ticket, 12); // 12 = 4 users, each with 1 join, 1 role change, 1 leave
        getSiteFeed(site3, ticket, 9);  //  9 = 3 users, each with 1 join, 1 role change, 1 leave (not user 4)
        
        ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, user4, USER_PW);
        
        getSiteFeed(site1, ticket, 12);
        
        try
        {
            getSiteFeed(site2, ticket, 0); // site 2 is private, user 4 is no longer a member
            
            fail("Site feed for private site should not be accessible to non-admin / non-member");
        }
        catch (IOException ioe)
        {
            assertTrue(ioe.getMessage().contains("HTTP response code: 401"));
        }
        
        try
        {
            getSiteFeed(site3, ticket, 0); // site 3 is private, user 4 was never a member
            
            fail("Site feed for private site should not be accessible to non-admin / non-member");
        }
        catch (IOException ioe)
        {
            assertTrue(ioe.getMessage().contains("HTTP response code: 401"));
        }
    }
    
    public void testGetUserFeedsAfter() throws Exception
    {
        testCreateSites();
        testAddAndUpdateMembershipsWithPause();
        testRemoveMembershipsWithPause();
        testUserFeedControls();
        
        // as admin
        
        String ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, ADMIN_USER, ADMIN_PW);
        
        // site 1, with 4 users, each with 1 join, 1 role change = 4x2 = 8
        // site 2, with 4 users, each with 1 join, 1 role change = 4x2 = 8
        // site 3, with 3 users, each with 1 join, 1 role change = 3x2 = 6
        
        // user 1 belongs to 3 sites = (2x8)+(1x6) = 22
        // user 2 belongs to 3 sites = (2x8)+(1x6) = 22
        // user 3 belongs to 3 sites = (2x8)+(1x6) = 22
        // user 4 belongs to 2 sites = (2x8) = 16
        
        getUserFeed(user1, ticket, true, 14);  // 14 = (22 - 8) due to feed control - exclude site 1
        getUserFeed(user2, ticket, true, 0);   // 0 = due to feed control - exclude site membership activities (across all sites)
        getUserFeed(user3, ticket, true, 14);  // 14 = (22 - 8) due to feed control - exclude site membership activities for site 1
        getUserFeed(user4, ticket, true, 16);  // 16 = no feed control
        
        // as user1
        
        ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, user1, USER_PW);
        
        getUserFeed(user1, ticket, false, 14);
        
        // as user2
        
        ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, user2, USER_PW);
        
        try
        {
            getUserFeed(user1, ticket, true, 14);
            
            fail("User feed should only be accessible to user or an admin");
        }
        catch (IOException ioe)
        {
            assertTrue(ioe.getMessage().contains("HTTP response code: 401"));
        }
        
        // as user1 - with filter args ...
        
        ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, user1, USER_PW);
        
        getUserFeed(null, site1, ticket, false, false, false, 0);
        getUserFeed(null, site2, ticket, false, false, false, 8);
        getUserFeed(null, site3, ticket, false, false, false, 6);
        
        getUserFeed(null, null, ticket, false, false, false, 14); // no filter
        getUserFeed(null, null, ticket, false, true, false, 14);  // exclude any from user1
        getUserFeed(null, null, ticket, false, false, true, 0);   // exclude all except user1
        getUserFeed(null, null, ticket, false, true, true, 0);    // exclude all (NOOP)
        
        // TODO - add more (eg. other non-admin user activities)
    }
    
    private void addMembership(String siteId, String userName, String ticket, String role) throws Exception
    {  
        // Build the JSON membership object
        JSONObject membership = new JSONObject();
        membership.put("role", role);
        JSONObject person = new JSONObject();
        person.put("userName", userName);
        membership.put("person", person);
        
        String url = WEBSCRIPT_ENDPOINT + URL_SITES + "/" + siteId + URL_MEMBERSHIPS;
        String response = callPostWebScript(url, ticket, membership.toString());
        
        if (logger.isDebugEnabled())
        {
            logger.debug("addMembership: " + siteId + " - " + userName);
            logger.debug("--------------");
            logger.debug(url);
            logger.debug(response);
        }
    }
    
    private void updateMembership(String siteId, String userName, String ticket, String role) throws Exception
    {  
        // Build the JSON membership object
        JSONObject membership = new JSONObject();
        membership.put("role", role);
        JSONObject person = new JSONObject();
        person.put("userName", userName);
        membership.put("person", person);
        
        String url = WEBSCRIPT_ENDPOINT + URL_SITES + "/" + siteId + URL_MEMBERSHIPS;
        String response = callPutWebScript(url, ticket, membership.toString());
        
        if (logger.isDebugEnabled())
        {
            logger.debug("updateMembership: " + siteId + " - " + userName);
            logger.debug("-----------------");
            logger.debug(url);
            logger.debug(response);
        }
    }
    
    private void removeMembership(String siteId, String userName, String ticket) throws Exception
    {  
        String url = WEBSCRIPT_ENDPOINT + URL_SITES + "/" + siteId + URL_MEMBERSHIPS + "/" + userName;
        String response = callDeleteWebScript(url, ticket);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("removeMembership: " + siteId + " - " + userName);
            logger.debug("-----------------");
            logger.debug(url);
            logger.debug(response);
        }
    }
    
    private void addFeedControl(String userName, String siteId, String appToolId, String ticket) throws Exception
    {  
        // Build the JSON feedControl object
        JSONObject feedControl = new JSONObject();
        feedControl.put("siteId", siteId);
        feedControl.put("appToolId", appToolId);
        
        String url = WEBSCRIPT_ENDPOINT + URL_ACTIVITIES + URL_USER_FEED_CTRL;
        String response = callPostWebScript(url, ticket, feedControl.toString());
        
        if (logger.isDebugEnabled())
        {
            logger.debug("addFeedControl: " + userName);
            logger.debug("--------------");
            logger.debug(url);
            logger.debug(response);
        }
    }
    
    protected String callGetWebScript(String urlString, String ticket) throws MalformedURLException, URISyntaxException, IOException
    {
        return callOutWebScript(urlString, "GET", ticket);
    }
    
    protected String callDeleteWebScript(String urlString, String ticket) throws MalformedURLException, URISyntaxException, IOException
    {
        return callOutWebScript(urlString, "DELETE", ticket);
    }
    
    protected String callPostWebScript(String urlString, String ticket, String data) throws MalformedURLException, URISyntaxException, IOException
    {
        return callInOutWebScript(urlString, "POST", ticket, data);
    }
    
    protected String callPutWebScript(String urlString, String ticket, String data) throws MalformedURLException, URISyntaxException, IOException
    {
        return callInOutWebScript(urlString, "PUT", ticket, data);
    }
    
    private String callOutWebScript(String urlString, String method, String ticket) throws MalformedURLException, URISyntaxException, IOException
    {
        URL url = new URL(urlString);
        
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod(method);
        
        if (ticket != null)
        {
            // add Base64 encoded authorization header
            // refer to: http://wiki.alfresco.com/wiki/Web_Scripts_Framework#HTTP_Basic_Authentication
            conn.addRequestProperty("Authorization", "Basic " + Base64.encodeBytes(ticket.getBytes()));
        }
        
        String result = null;
        InputStream is = null;
        BufferedReader br = null;
        
        try
        {
            is = conn.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            
            String line = null;
            StringBuffer sb = new StringBuffer();
            while(((line = br.readLine()) !=null))  {
                sb.append(line);
            }
            
            result = sb.toString();
        }
        finally
        {
            if (br != null) { br.close(); };
            if (is != null) { is.close(); };
        }
        
        return result;
    }
    
    private String callInOutWebScript(String urlString, String method, String ticket, String data) throws MalformedURLException, URISyntaxException, IOException
    {
        return callInOutWeb(urlString, method, ticket, data, "application/json", null);
    }
    
    private String callInOutWeb(String urlString, String method, String ticket, String data, String contentType, String soapAction) throws MalformedURLException, URISyntaxException, IOException
    {
        URL url = new URL(urlString);
        
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod(method);
        
        conn.setRequestProperty("Content-type", contentType);
        
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches (false);
        
        if (soapAction != null)
        {
            conn.setRequestProperty("SOAPAction", soapAction);
        }
        
        if (ticket != null)
        {
            // add Base64 encoded authorization header
            // refer to: http://wiki.alfresco.com/wiki/Web_Scripts_Framework#HTTP_Basic_Authentication
            conn.addRequestProperty("Authorization", "Basic " + Base64.encodeBytes(ticket.getBytes()));
        }
        
        String result = null;
        BufferedReader br = null;
        DataOutputStream wr = null;
        OutputStream os = null;
        InputStream is = null;
        
        try
        {
            os = conn.getOutputStream();
            wr = new DataOutputStream(os);
            wr.write(data.getBytes());
            wr.flush();
        }
        finally
        {
            if (wr != null) { wr.close(); };
            if (os != null) { os.close(); };
        }
        
        try
        {
            is = conn.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            
            String line = null;
            StringBuffer sb = new StringBuffer();
            while(((line = br.readLine()) !=null)) 
            {
                sb.append(line);
            }
            
            result = sb.toString();
        }
        finally
        {
            if (br != null) { br.close(); };
            if (is != null) { is.close(); };
        }
        
        return result;
    }
    
    protected String callLoginWebScript(String serviceUrl, String username, String password) throws MalformedURLException, URISyntaxException, IOException
    {
        // Refer to: http://wiki.alfresco.com/wiki/Web_Scripts_Framework#HTTP_Basic_Authentication  
        String ticketResult = callGetWebScript(serviceUrl+"/api/login?u="+username+"&pw="+password, null);
        
        if (ticketResult != null)
        {
            int startTag = ticketResult.indexOf("<ticket>");
            int endTag = ticketResult.indexOf("</ticket>");
            if ((startTag != -1) && (endTag != -1))
            {
                ticketResult = ticketResult.substring(startTag+("<ticket>".length()), endTag);
            }
        }
        
        return ticketResult;
    }
    
    protected void createUser(String ticket, String username, String password) throws JSONException, IOException, URISyntaxException
    {
        // Build the JSON person object
        JSONObject person = new JSONObject();
        person.put("userName", username);
        person.put("firstName", "first");
        person.put("lastName", "last");
        person.put("email", "email@email.com");
        person.put("password", password);
        
        String url = WEBSCRIPT_ENDPOINT + URL_PEOPLE;
        String response = callPostWebScript(url, ticket, person.toString());
        
        if (logger.isDebugEnabled())
        {
            logger.debug("addPerson: " + username);
            logger.debug("--------------");
            logger.debug(url);
            logger.debug(response);
        }
    }
}
