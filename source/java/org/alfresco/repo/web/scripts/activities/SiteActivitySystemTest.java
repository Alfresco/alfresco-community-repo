/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
import java.util.Date;

import junit.framework.TestCase;

import org.alfresco.repo.site.SiteModel;
import org.alfresco.util.Base64;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
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
    
    // web service (SOAP) - temporary (see below)
    private static final String WEBSERVICE_ENDPOINT = REPO + "/api";
    
    private static final String URL_AUTH = "/AuthenticationService";
    private static final String URL_ADMIN = "/AdministrationService";
    
    // web script (REST)
    private static final String WEBSCRIPT_ENDPOINT  = REPO + "/service";
    
    // Site Service part-URLs
    private static final String URL_SITES = "/api/sites";
    private static final String URL_MEMBERSHIPS = "/memberships";
    
    // Activity Service part-URLs
    private static final String URL_ACTIVITIES = "/api/activities";
    private static final String URL_SITE_FEED = "/feed/site";
    private static final String URL_USER_FEED = "/feed/user";
    private static final String URL_USER_FEED_CTRL = "/feed/user/control";
    
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
            
            createUser(user1, USER_PW);
            createUser(user2, USER_PW);
            createUser(user3, USER_PW);
            createUser(user4, USER_PW);
            
            setup = true;
        }
       
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
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
        
        String ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, ADMIN_USER, ADMIN_PW);
        
        getUserFeed(user1, ticket, true, 0);
        getUserFeed(user2, ticket, true, 0);
        getUserFeed(user3, ticket, true, 0);
        getUserFeed(user4, ticket, true, 0);
        
        ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, user1, USER_PW);
        
        getUserFeed(user1, ticket, false, 0);
        
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
    }
    
    protected void getUserFeed(String userId, String ticket, boolean isAdmin, int expectedCount) throws Exception
    {
        String url = WEBSCRIPT_ENDPOINT + URL_ACTIVITIES + URL_USER_FEED + (isAdmin ? "/" + userId : "") + "?format=json";
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
            Thread.sleep(90000); // 1 min
            
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
            Thread.sleep(60000); // 1 min
            
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
        
        String ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, ADMIN_USER, ADMIN_PW);
        
        // 2 sites, with 4 users, each with 1 join and 1 role change = 8x2
        // 1 site,  with 3 users, each with 1 join and 1 role change = 6x1
        
        getUserFeed(user1, ticket, true, 14);  // 8 = due to feed control - exclude site 1
        getUserFeed(user2, ticket, true, 0);   // 0 = due to feed control - exclude site membership activities (across all sites)
        getUserFeed(user3, ticket, true, 14);  // 8 = due to feed control - exclude site membership activities for site 1
        getUserFeed(user4, ticket, true, 16);  // 16 = no feed control
        
        ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, user1, USER_PW);
        
        getUserFeed(user1, ticket, false, 14);
        
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
        
        String url = WEBSCRIPT_ENDPOINT + URL_SITES + "/" + siteId + URL_MEMBERSHIPS + "/" + userName;
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
    
    // TODO - replace with Create Person REST API when it becomes available
    
    protected void createUser(String username, String password) throws MalformedURLException, URISyntaxException, IOException
    {
        String ticket = webServiceStartSession(ADMIN_USER, ADMIN_PW);
        webServiceCreateUser(username, password, ticket);
        webServiceEndSession(ticket);
    }
    
    private String webServiceStartSession(String username, String password) throws MalformedURLException, URISyntaxException, IOException
    {
        String soapCall = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"+
            "<soapenv:Body><startSession xmlns=\"http://www.alfresco.org/ws/service/authentication/1.0\">"+
            "<username>"+username+"</username>"+
            "<password>"+password+"</password>"+
            "</startSession></soapenv:Body></soapenv:Envelope>";
        
        String response = callInOutWeb(WEBSERVICE_ENDPOINT + URL_AUTH, "POST", null, soapCall, "text/xml; charset=utf-8", "\"http://www.alfresco.org/ws/service/authentication/1.0/startSession\"");
        
        String ticket = null;
        
        if (response != null)
        {
            int idx1 = response.indexOf("<ticket>");
            if (idx1 != -1)
            {
                int idx2 = response.indexOf("</ticket>");
                if (idx2 != -1)
                {
                    ticket = response.substring(idx1+"<ticket>".length(), idx2);
                }
            }
        }
        
        return ticket;
    }
    
    private void webServiceCreateUser(String username, String password, String ticket) throws MalformedURLException, URISyntaxException, IOException
    {
        Date now = new Date();
        String startTime = ISO8601DateFormat.format(now);
        String expireTime = ISO8601DateFormat.format(new Date(now.getTime() + 5*60*1000));
        
        String soapCall = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"+
            "<soapenv:Header>"+
            "<wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" soapenv:mustUnderstand=\"1\">"+
            "<wsu:Timestamp xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\"><wsu:Created>"+startTime+"</wsu:Created><wsu:Expires>"+expireTime+"</wsu:Expires></wsu:Timestamp>"+
            "<wsse:UsernameToken><wsse:Username>ticket</wsse:Username><wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">"+ticket+"</wsse:Password></wsse:UsernameToken></wsse:Security></soapenv:Header>"+
            "<soapenv:Body><createUsers xmlns=\"http://www.alfresco.org/ws/service/administration/1.0\">"+
            "<newUsers><userName>"+username+"</userName><password>"+password+"</password><properties>"+
            "<ns1:name xmlns:ns1=\"http://www.alfresco.org/ws/model/content/1.0\">{http://www.alfresco.org/model/content/1.0}homeFolder</ns1:name>"+
            "<ns2:isMultiValue xmlns:ns2=\"http://www.alfresco.org/ws/model/content/1.0\">false</ns2:isMultiValue>"+
            "<ns3:value xmlns:ns3=\"http://www.alfresco.org/ws/model/content/1.0\">workspace:////SpacesStore</ns3:value></properties><properties>" +
            "<ns4:name xmlns:ns4=\"http://www.alfresco.org/ws/model/content/1.0\">{http://www.alfresco.org/model/content/1.0}firstName</ns4:name>"+
            "<ns5:isMultiValue xmlns:ns5=\"http://www.alfresco.org/ws/model/content/1.0\">false</ns5:isMultiValue>"+
            "<ns6:value xmlns:ns6=\"http://www.alfresco.org/ws/model/content/1.0\">"+"FN_"+username+"</ns6:value></properties><properties>"+
            "<ns7:name xmlns:ns7=\"http://www.alfresco.org/ws/model/content/1.0\">{http://www.alfresco.org/model/content/1.0}middleName</ns7:name>"+
            "<ns8:isMultiValue xmlns:ns8=\"http://www.alfresco.org/ws/model/content/1.0\">false</ns8:isMultiValue>"+
            "<ns9:value xmlns:ns9=\"http://www.alfresco.org/ws/model/content/1.0\"></ns9:value></properties><properties>"+
            "<ns10:name xmlns:ns10=\"http://www.alfresco.org/ws/model/content/1.0\">{http://www.alfresco.org/model/content/1.0}lastName</ns10:name>"+
            "<ns11:isMultiValue xmlns:ns11=\"http://www.alfresco.org/ws/model/content/1.0\">false</ns11:isMultiValue>" +
            "<ns12:value xmlns:ns12=\"http://www.alfresco.org/ws/model/content/1.0\">"+"LN_"+username+"</ns12:value></properties><properties>" +
            "<ns13:name xmlns:ns13=\"http://www.alfresco.org/ws/model/content/1.0\">{http://www.alfresco.org/model/content/1.0}email</ns13:name>"+
            "<ns14:isMultiValue xmlns:ns14=\"http://www.alfresco.org/ws/model/content/1.0\">false</ns14:isMultiValue>"+
            "<ns15:value xmlns:ns15=\"http://www.alfresco.org/ws/model/content/1.0\">email1210929178773</ns15:value></properties><properties>"+
            "<ns16:name xmlns:ns16=\"http://www.alfresco.org/ws/model/content/1.0\">{http://www.alfresco.org/model/content/1.0}organizationId</ns16:name>"+
            "<ns17:isMultiValue xmlns:ns17=\"http://www.alfresco.org/ws/model/content/1.0\">false</ns17:isMultiValue>"+
            "<ns18:value xmlns:ns18=\"http://www.alfresco.org/ws/model/content/1.0\">org1210929178773</ns18:value></properties></newUsers></createUsers></soapenv:Body></soapenv:Envelope>";
        
        callInOutWeb(WEBSERVICE_ENDPOINT + URL_ADMIN, "POST", null, soapCall, "text/xml; charset=utf-8", "\"http://www.alfresco.org/ws/service/administration/1.0/createUsers\"");  // ignore response
    }
    
    private void webServiceEndSession(String ticket) throws MalformedURLException, URISyntaxException, IOException
    {
        String soapCall = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"+
            "<soapenv:Body><endSession xmlns=\"http://www.alfresco.org/ws/service/authentication/1.0\">"+
            "<ticket>"+ticket+"</ticket>"+
            "</endSession></soapenv:Body></soapenv:Envelope>";
        
        callInOutWeb(WEBSERVICE_ENDPOINT + URL_AUTH, "POST", null, soapCall, "text/xml; charset=utf-8", "\"http://www.alfresco.org/ws/service/authentication/1.0/endSession\""); // ignore response
    }
}
