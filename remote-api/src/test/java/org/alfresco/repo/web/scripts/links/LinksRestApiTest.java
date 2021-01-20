/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.repo.web.scripts.links;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.activities.feed.FeedGenerator;
import org.alfresco.repo.activities.post.lookup.PostLookup;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Unit Test to test the Links Web Script API
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class LinksRestApiTest extends BaseWebScriptTest
{
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(LinksRestApiTest.class);

    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private TransactionService transactionService;
    private BehaviourFilter policyBehaviourFilter;
    private PersonService personService;
    private NodeService nodeService;
    private NodeService internalNodeService;
    private SiteService siteService;
    private NodeArchiveService nodeArchiveService;
    private ActivityService activityService;
    private FeedGenerator feedGenerator;
    private PostLookup postLookup;
    
    private static final String USER_ONE = "UserOneSecondToo";
    private static final String USER_TWO = "UserTwoSecondToo";
    private static final String USERDETAILS_FIRSTNAME = "FirstName123";
    private static final String USERDETAILS_LASTNAME = "LastName123";
    private static final String SITE_SHORT_NAME_LINKS = "LinkSiteShortNameTest";
    
    private static final String LINK_TITLE_ONE   = "TestLinkOne";
    private static final String LINK_TITLE_TWO   = "TestLinkTwo";
    private static final String LINK_TITLE_THREE = "StillTestLinkThree";
    private static final String LINK_URL_ONE   = "http://google.com/";
    private static final String LINK_URL_TWO   = "http://alfresco.com/";
    private static final String LINK_URL_THREE = "http://share.alfresco.com/";

    private static final String URL_LINKS_BASE = "/api/links/site/" + SITE_SHORT_NAME_LINKS + "/links"; 
    private static final String URL_LINKS_LIST = URL_LINKS_BASE;
    private static final String URL_LINKS_CREATE = URL_LINKS_BASE + "/posts";
    private static final String URL_LINKS_UPDATE = URL_LINKS_BASE + "/"; // plus path
    private static final String URL_LINKS_DELETE = "/api/links/delete/site/" + SITE_SHORT_NAME_LINKS + "/links";
    private static final String URL_LINKS_FETCH = "/api/links/link/site/" + SITE_SHORT_NAME_LINKS + "/links/"; // plus path

    private static final String URL_DELETE_COMMENT = "api/comment/node/{0}/{1}/{2}?site={3}&itemtitle={4}&page={5}";
    
    
    // General methods

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.authenticationService = (MutableAuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.policyBehaviourFilter = (BehaviourFilter)getServer().getApplicationContext().getBean("policyBehaviourFilter");
        this.transactionService = (TransactionService)getServer().getApplicationContext().getBean("transactionService");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        this.nodeService = (NodeService)getServer().getApplicationContext().getBean("NodeService");
        this.siteService = (SiteService)getServer().getApplicationContext().getBean("SiteService");
        this.internalNodeService = (NodeService)getServer().getApplicationContext().getBean("nodeService");
        this.nodeArchiveService = (NodeArchiveService)getServer().getApplicationContext().getBean("nodeArchiveService");
        this.activityService = (ActivityService)getServer().getApplicationContext().getBean("activityService");
        ChildApplicationContextFactory activitiesFeed = (ChildApplicationContextFactory)getServer().getApplicationContext().getBean("ActivitiesFeed");
        ApplicationContext activitiesFeedCtx = activitiesFeed.getApplicationContext();
        this.feedGenerator = (FeedGenerator)activitiesFeedCtx.getBean("feedGenerator");
        this.postLookup = (PostLookup)activitiesFeedCtx.getBean("postLookup");


        // Authenticate as user
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        // Create test site
        // - only create the site if it doesn't already exist
        SiteInfo siteInfo = this.siteService.getSite(SITE_SHORT_NAME_LINKS);
        if (siteInfo == null)
        {
            this.siteService.createSite("CalendarSitePreset", SITE_SHORT_NAME_LINKS, "LinksSiteTitle", "TestDescription", SiteVisibility.PUBLIC);
        }
        
        // Ensure the links container is there
        if (!siteService.hasContainer(SITE_SHORT_NAME_LINKS, "links"))
        {
            siteService.createContainer(SITE_SHORT_NAME_LINKS, "links", null, null);
        }
        
        // Create users
        createUser(USER_ONE, SiteModel.SITE_COLLABORATOR, SITE_SHORT_NAME_LINKS);
        createUser(USER_TWO, SiteModel.SITE_COLLABORATOR, SITE_SHORT_NAME_LINKS);

        // Do tests as inviter user
        this.authenticationComponent.setCurrentUser(USER_ONE);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        // admin user required to delete user
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        SiteInfo siteInfo = this.siteService.getSite(SITE_SHORT_NAME_LINKS);
        if (siteInfo != null)
        {
            // delete the site
            siteService.deleteSite(SITE_SHORT_NAME_LINKS);
            nodeArchiveService.purgeArchivedNode(nodeArchiveService.getArchivedNode(siteInfo.getNodeRef()));
        }
        
        // delete the users
        personService.deletePerson(USER_ONE);
        if(this.authenticationService.authenticationExists(USER_ONE))
        {
           this.authenticationService.deleteAuthentication(USER_ONE);
        }
        
        personService.deletePerson(USER_TWO);
        if (this.authenticationService.authenticationExists(USER_TWO))
        {
           this.authenticationService.deleteAuthentication(USER_TWO);
        }
    }
    
    private void createUser(String userName, String role, String siteName)
    {
        // if user with given user name doesn't already exist then create user
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            // create user
            this.authenticationService.createAuthentication(userName, "password".toCharArray());
            
            // create person properties
            PropertyMap personProps = new PropertyMap();
            personProps.put(ContentModel.PROP_USERNAME, userName);
            personProps.put(ContentModel.PROP_FIRSTNAME, USERDETAILS_FIRSTNAME);
            personProps.put(ContentModel.PROP_LASTNAME, USERDETAILS_LASTNAME);
            personProps.put(ContentModel.PROP_EMAIL, "FirstName123.LastName123@email.com");
            personProps.put(ContentModel.PROP_JOBTITLE, "JobTitle123");
            personProps.put(ContentModel.PROP_JOBTITLE, "Organisation123");
            
            // create person node for user
            this.personService.createPerson(personProps);
        }
        
        // add the user as a member with the given role
        this.siteService.setMembership(siteName, userName, role);
    }
    
    
    // Test helper methods
    
    private JSONObject getLinks(String filter, String username) throws Exception
    {
       String origUser = this.authenticationComponent.getCurrentUserName();
       if (username != null)
       {
          this.authenticationComponent.setCurrentUser(username);
          filter = "user";
       }
       
       String url = URL_LINKS_LIST;
       if (filter == null)
       {
          filter = "all";
       }
       url += "?filter=" + filter;
       url += "&startIndex=0&page=1&pageSize=4";
       
       Response response = sendRequest(new GetRequest(url), 200);
       JSONObject result = new JSONObject(response.getContentAsString());
       
       if (username != null)
       {
          this.authenticationComponent.setCurrentUser(origUser);
       }
       
       return result;
    }
    
    private JSONObject getLink(String name, int expectedStatus) throws Exception
    {
       Response response = sendRequest(new GetRequest(URL_LINKS_FETCH + name), expectedStatus);
       if (expectedStatus == Status.STATUS_OK)
       {
          JSONObject result = new JSONObject(response.getContentAsString());
          if (result.has("item"))
          {
             return result.getJSONObject("item");
          }
          return result;
       }
       else
       {
          return null;
       }
    }
    
    /**
     * Creates a single link based on the supplied details
     */
    private JSONObject createLink(String title, String description, String url,
          boolean internal, int expectedStatus) throws Exception
    {
       JSONObject json = new JSONObject();
       json.put("site", SITE_SHORT_NAME_LINKS);
       json.put("title", title);
       json.put("description", description);
       json.put("url", url);
       json.put("tags", "");
       if (internal)
       {
          json.put("internal", "true");
       }
       json.put("page", "links-view"); // TODO Is this really needed?
       
       Response response = sendRequest(new PostRequest(URL_LINKS_CREATE, json.toString(), "application/json"), expectedStatus);
       if (expectedStatus == Status.STATUS_OK)
       {
          JSONObject result = new JSONObject(response.getContentAsString());
          if (result.has("link"))
          {
             return result.getJSONObject("link");
          }
          return result;
       }
       else
       {
          return null;
       }
    }
    
    /**
     * Updates the link with the new details
     */
    private JSONObject updateLink(String name, String title, String description, String url,
          boolean internal, int expectedStatus) throws Exception
    {
       JSONObject json = new JSONObject();
       json.put("site", SITE_SHORT_NAME_LINKS);
       json.put("title", title);
       json.put("description", description);
       json.put("url", url);
       json.put("tags", "");
       json.put("internal", Boolean.toString(internal).toLowerCase());
       json.put("page", "links-view"); // TODO Is this really needed?
       
       Response response = sendRequest(new PutRequest(URL_LINKS_UPDATE + name, json.toString(), "application/json"), expectedStatus);
       if (expectedStatus == Status.STATUS_OK)
       {
          JSONObject result = new JSONObject(response.getContentAsString());
          if (result.has("links"))
          {
             return result.getJSONObject("links");
          }
          return result;
       }
       else
       {
          return null;
       }
    }
    
    /**
     * Deletes the link
     */
    private JSONObject deleteLink(String name, int expectedStatus) throws Exception
    {
       Response response = sendRequest(new DeleteRequest(URL_LINKS_FETCH+name), expectedStatus);
       if (expectedStatus == Status.STATUS_OK)
       {
          JSONObject result = new JSONObject(response.getContentAsString());
          return result;
       }
       else
       {
          return null;
       }
    }
    
    /**
     * Deletes the links
     */
    private JSONObject deleteLinks(List<String> names, int expectedStatus) throws Exception
    {
       JSONArray items = new JSONArray();
       for (String name : names)
       {
          items.put(name);
       }
       
       JSONObject json = new JSONObject();
       json.put("items", items);
       
       Response response = sendRequest(new PostRequest(URL_LINKS_DELETE, json.toString(), "application/json"), expectedStatus);
       if (expectedStatus == Status.STATUS_OK)
       {
          JSONObject result = new JSONObject(response.getContentAsString());
          return result;
       }
       else
       {
          return null;
       }
    }
    
    /**
     * Monkeys with the created date on a link
     */
    private void pushLinkCreatedDateBack(String name, int daysAgo) throws Exception
    {
       NodeRef container = siteService.getContainer(SITE_SHORT_NAME_LINKS, "links");
       NodeRef node = nodeService.getChildByName(container, ContentModel.ASSOC_CONTAINS, name);
       
       Date created = (Date)nodeService.getProperty(node, ContentModel.PROP_CREATED);
       Date newCreated = new Date(created.getTime() - daysAgo*24*60*60*1000);
       
       UserTransaction txn = transactionService.getUserTransaction();
       txn.begin();

       this.policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
       internalNodeService.setProperty(node, ContentModel.PROP_CREATED, newCreated);
       this.policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
       
       txn.commit();
       
       // Now chance something else on the node to have it re-indexed
       nodeService.setProperty(node, ContentModel.PROP_CREATED, newCreated);
       nodeService.setProperty(node, ContentModel.PROP_DESCRIPTION, "Forced change");
    }
    
    /**
     * Gets the link name (link- timestamp based) from a returned link
     */
    private String getNameFromLink(JSONObject link) throws Exception
    {
       if (! link.has("name"))
       {
          throw new IllegalArgumentException("No name in " + link.toString());
       }
       
       return link.getString("name");
    }
    
    
    // Tests
    
    /**
     * Creating, editing, fetching and deleting a link
     */
    public void testCreateEditDeleteEntry() throws Exception
    {
       JSONObject link;
       JSONObject author;
       JSONObject permissions;
       String name;
       
       
       // None to start with
       link = getLinks(null, null);
       assertEquals("Incorrect JSON: " + link.toString(), true, link.has("total"));
       assertEquals(0, link.getInt("total"));
       
       
       // Won't be there to start with
       link = getLink(LINK_TITLE_ONE, Status.STATUS_NOT_FOUND);
       
       
       // Create
       // (We don't get much info back)
       link = createLink(LINK_TITLE_ONE, "Thing 1", LINK_URL_ONE, false, Status.STATUS_OK);
       assertEquals("Incorrect JSON: " + link.toString(), true, link.has("name"));
       name = getNameFromLink(link);
       
       assertEquals(name, link.getString("name"));
       assertEquals(name, link.getString("message"));

       
       // Fetch
       link = getLink(name, Status.STATUS_OK);
       
       assertEquals("Error found " + link.toString(), false, link.has("error"));
       assertEquals(LINK_TITLE_ONE, link.getString("title"));
       assertEquals("Thing 1", link.getString("description"));
       assertEquals(LINK_URL_ONE, link.getString("url"));
       assertEquals(false, link.getBoolean("internal"));
       assertEquals(0, link.getJSONArray("tags").length());
       
       assertEquals(true, link.has("author"));
       author = link.getJSONObject("author");
       assertEquals(USER_ONE, author.getString("username"));
       assertEquals(USERDETAILS_FIRSTNAME, author.getString("firstName"));
       assertEquals(USERDETAILS_LASTNAME, author.getString("lastName"));
       
       // Check the permissions
       assertEquals(true, link.has("permissions"));
       permissions = link.getJSONObject("permissions");
       assertEquals(true, permissions.getBoolean("edit"));
       assertEquals(true, permissions.getBoolean("delete"));
       
       // Check the noderef
       NodeRef nodeRef = new NodeRef(link.getString("nodeRef"));
       assertEquals(true, nodeService.exists(nodeRef));
       assertEquals(name, nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
       
       // Check the comments url
       assertEquals(
             "/node/workspace/" + nodeRef.getStoreRef().getIdentifier() + "/" + nodeRef.getId() + "/comments",
             link.getString("commentsUrl"));
       
       // Check the created date: compare two java.util.Date objects.
       assertEquals(
             nodeService.getProperty(nodeRef, ContentModel.PROP_CREATED),
             ISO8601DateFormat.parse(link.getJSONObject("createdOnDate").getString("iso8601")));
       
       // Edit
       // We should get a simple message
       link = updateLink(name, LINK_TITLE_ONE, "More Thing 1", LINK_URL_ONE, true, Status.STATUS_OK);
       assertEquals(
             "Incorrect JSON: " + link.toString(), 
             true, link.has("message"));
       assertEquals(
             "Incorrect JSON: " + link.toString(), 
             true, link.getString("message").contains("updated"));
       
       
       // Fetch
       link = getLink(name, Status.STATUS_OK);
       
       assertEquals("Error found " + link.toString(), false, link.has("error"));
       assertEquals(LINK_TITLE_ONE, link.getString("title"));
       assertEquals("More Thing 1", link.getString("description"));
       assertEquals(LINK_URL_ONE, link.getString("url"));
       assertEquals(true, link.getBoolean("internal"));
       assertEquals(0, link.getJSONArray("tags").length());
       
       assertEquals(true, link.has("author"));
       author = link.getJSONObject("author");
       assertEquals(USER_ONE, author.getString("username"));
       assertEquals(USERDETAILS_FIRSTNAME, author.getString("firstName"));
       assertEquals(USERDETAILS_LASTNAME, author.getString("lastName"));
       
       
       // Fetch as a different user, permissions different
       this.authenticationComponent.setCurrentUser(USER_TWO);
       link = getLink(name, Status.STATUS_OK);
       
       // Check the basics
       assertEquals(LINK_TITLE_ONE, link.getString("title"));
       assertEquals("More Thing 1", link.getString("description"));
       assertEquals(LINK_URL_ONE, link.getString("url"));
       assertEquals(true, link.getBoolean("internal"));
       assertEquals(0, link.getJSONArray("tags").length());
       
       // Different user in the site, can edit but not delete
       assertEquals(true, link.has("permissions"));
       permissions = link.getJSONObject("permissions");
       assertEquals(true, permissions.getBoolean("edit"));
       assertEquals(false, permissions.getBoolean("delete"));
       
       this.authenticationComponent.setCurrentUser(USER_ONE);

       
       // Delete
       link = deleteLinks(Arrays.asList(new String[]{name}), Status.STATUS_OK);
       assertEquals(
             "Incorrect JSON: " + link.toString(), 
             true, link.has("message"));
       
       assertEquals(
             "Incorrect JSON: " + link.toString(), 
             true, link.getString("message").contains("deleted"));

       
       // Fetch, will have gone
       link = getLink(name, Status.STATUS_NOT_FOUND);
       
       
       // Can't delete again
       deleteLinks(Arrays.asList(new String[]{name}), Status.STATUS_NOT_FOUND);
       
       
       // Can't edit it when it's deleted
       sendRequest(new PutRequest(URL_LINKS_UPDATE + name, "{}", "application/json"), Status.STATUS_NOT_FOUND);
       
       
       // Do a single delete
       link = createLink(LINK_TITLE_ONE, "Thing 1", LINK_URL_ONE, false, Status.STATUS_OK);
       name = getNameFromLink(link);
       
       getLink(name, Status.STATUS_OK);
       deleteLink(name, Status.STATUS_NO_CONTENT);
       getLink(name, Status.STATUS_NOT_FOUND);
       deleteLink(name, Status.STATUS_NOT_FOUND);
    }

    /**
     * MNT-13456 Check for XSS attack via update of link
     * @throws Exception
     */
    public void testXssLinks() throws Exception
    {
        String LINK_TITLE = "lnk" + System.currentTimeMillis();
        String LINK_URL = "http://alfresco.com";

        HashMap<String, Integer> mapForCheck = new HashMap<String, Integer>();
        mapForCheck.put("http:javasc\\ript:alert('mail.ru')", Status.STATUS_BAD_REQUEST);
        mapForCheck.put("javas\\0cr\\ip\\00t:alert('dd')", Status.STATUS_BAD_REQUEST);
        mapForCheck.put("alfresco.my", Status.STATUS_OK);
        mapForCheck.put("javascript:alert('http://somedata.html')", Status.STATUS_BAD_REQUEST);
        mapForCheck.put("http://alfresco.org", Status.STATUS_OK);
        mapForCheck.put("localhost:8080", Status.STATUS_OK);
        mapForCheck.put("localhost:8080/share", Status.STATUS_OK);
        mapForCheck.put("localhost:80A80/share", Status.STATUS_BAD_REQUEST);
        mapForCheck.put("http:java\\00script:alert('XSS')", Status.STATUS_BAD_REQUEST);
        mapForCheck.put("http:javas\\0cript:alert('XSS')", Status.STATUS_BAD_REQUEST);
        mapForCheck.put("http: &#14;  javascript:alert('XSS')", Status.STATUS_BAD_REQUEST);
        mapForCheck.put("<SCRIPT/XSS SRC='http://ha.ckers.org/xss.js'></SCRIPT>", Status.STATUS_BAD_REQUEST);
        mapForCheck.put("<iframe src=http://ha.ckers.org/scriptlet.html <", Status.STATUS_BAD_REQUEST);
        mapForCheck.put("html:vbscript:msgbox(\"XSS\")", Status.STATUS_BAD_REQUEST);
        mapForCheck.put("<STYLE>@im\\port'\\ja\\vasc\\ript:alert(\"XSS\")';</STYLE>", Status.STATUS_BAD_REQUEST);
        mapForCheck.put("<IMG SRC= onmouseover=\"alert('xxs')\">", Status.STATUS_BAD_REQUEST);
        mapForCheck.put("BODY onload!#$%&()*~+-_.,:;?@[/|\\]^`=alert(\"XSS\")>", Status.STATUS_BAD_REQUEST);
        mapForCheck.put("onload54(dd)fg`=df", Status.STATUS_BAD_REQUEST);

        JSONObject link;

        link = createLink(LINK_TITLE, "Link desc", LINK_URL, false, Status.STATUS_OK);
        String name = getNameFromLink(link);

        for (String url : mapForCheck.keySet())
        {
            int expStatus = mapForCheck.get(url);
            updateLink(name, LINK_TITLE, "Link desc", url, false, expStatus);
        }
    }
    
    /**
     * Listing
     */
    public void testOverallListing() throws Exception
    {
       JSONObject links;
       JSONArray entries;
       
       // Initially, there are no events
       links = getLinks(null, null);
       assertEquals("Incorrect JSON: " + links.toString(), true, links.has("total"));
       assertEquals(0, links.getInt("total"));
       assertEquals(0, links.getInt("itemCount"));
       
       
       // Add two links to get started with
       createLink(LINK_TITLE_ONE, "Thing 1", LINK_URL_ONE, false, Status.STATUS_OK);
       createLink(LINK_TITLE_TWO, "Thing 2", LINK_URL_TWO, false, Status.STATUS_OK);
       
       // Check again
       links = getLinks(null, null);
       
       // Should have two links
       assertEquals("Incorrect JSON: " + links.toString(), true, links.has("total"));
       assertEquals(2, links.getInt("total"));
       assertEquals(2, links.getInt("itemCount"));
     
       entries = links.getJSONArray("items");
       assertEquals(2, entries.length());
       // Sorted by newest created first
       assertEquals(LINK_TITLE_TWO, entries.getJSONObject(0).getString("title"));
       assertEquals(LINK_TITLE_ONE, entries.getJSONObject(1).getString("title"));
       
       
       // Add a third, which is internal, and created by the other user
       this.authenticationComponent.setCurrentUser(USER_TWO);
       JSONObject link3 = createLink(LINK_TITLE_THREE, "Thing 3", LINK_URL_THREE, true, Status.STATUS_OK);
       String name3 = getNameFromLink(link3);
       updateLink(name3, LINK_TITLE_THREE, "More Where 3", LINK_URL_THREE, false, Status.STATUS_OK);
       this.authenticationComponent.setCurrentUser(USER_ONE);
       
       
       // Check now, should have three links
       links = getLinks(null, null);
       assertEquals(3, links.getInt("total"));
       assertEquals(3, links.getInt("itemCount"));
       
       entries = links.getJSONArray("items");
       assertEquals(3, entries.length());
       assertEquals(LINK_TITLE_THREE, entries.getJSONObject(0).getString("title"));
       assertEquals(LINK_TITLE_TWO, entries.getJSONObject(1).getString("title"));
       assertEquals(LINK_TITLE_ONE, entries.getJSONObject(2).getString("title"));
       
       
       // Ask for filtering by user
       links = getLinks(null, USER_ONE);
       assertEquals(2, links.getInt("total"));
       assertEquals(2, links.getInt("itemCount"));
       
       entries = links.getJSONArray("items");
       assertEquals(2, entries.length());
       assertEquals(LINK_TITLE_TWO, entries.getJSONObject(0).getString("title"));
       assertEquals(LINK_TITLE_ONE, entries.getJSONObject(1).getString("title"));
       
       links = getLinks(null, USER_TWO);
       assertEquals(1, links.getInt("total"));
       assertEquals(1, links.getInt("itemCount"));
       
       entries = links.getJSONArray("items");
       assertEquals(1, entries.length());
       assertEquals(LINK_TITLE_THREE, entries.getJSONObject(0).getString("title"));

       
       // Ask for filtering by recent docs
       links = getLinks("recent", null);
       assertEquals(3, links.getInt("total"));
       assertEquals(3, links.getInt("itemCount"));
       
       entries = links.getJSONArray("items");
       assertEquals(3, entries.length());
       assertEquals(LINK_TITLE_THREE, entries.getJSONObject(0).getString("title"));
       assertEquals(LINK_TITLE_TWO, entries.getJSONObject(1).getString("title"));
       assertEquals(LINK_TITLE_ONE, entries.getJSONObject(2).getString("title"));
       
       
       // Push the 3rd event back, it'll fall off
       pushLinkCreatedDateBack(name3, 10);
       
       links = getLinks("recent", null);
       assertEquals(2, links.getInt("total"));
       assertEquals(2, links.getInt("itemCount"));
       
       entries = links.getJSONArray("items");
       assertEquals(2, entries.length());
       assertEquals(LINK_TITLE_TWO, entries.getJSONObject(0).getString("title"));
       assertEquals(LINK_TITLE_ONE, entries.getJSONObject(1).getString("title"));
       
       
       
       // Trigger the paging, by going over our page size of 4
       createLink(LINK_TITLE_THREE+"a", "Thing 4", LINK_URL_THREE, true, Status.STATUS_OK);
       createLink(LINK_TITLE_THREE+"z", "Thing 5", LINK_URL_THREE, true, Status.STATUS_OK);
       
       links = getLinks(null, null);
       assertEquals(5, links.getInt("total"));
       assertEquals(4, links.getInt("itemCount"));
       
       entries = links.getJSONArray("items");
       assertEquals(4, entries.length());
       assertEquals(LINK_TITLE_THREE+"z", entries.getJSONObject(0).getString("title"));
       assertEquals(LINK_TITLE_THREE+"a", entries.getJSONObject(1).getString("title"));
       assertEquals(LINK_TITLE_TWO, entries.getJSONObject(2).getString("title"));
       assertEquals(LINK_TITLE_ONE, entries.getJSONObject(3).getString("title"));
       // THREE is now the oldest, as we pushed it back in time, so it's on page two
       
       
       // Now hide the site, and remove the user from it, won't be allowed to see it
       this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
       SiteInfo site = siteService.getSite(SITE_SHORT_NAME_LINKS);
       site.setVisibility(SiteVisibility.PRIVATE);
       siteService.updateSite(site);
       siteService.removeMembership(SITE_SHORT_NAME_LINKS, USER_ONE);
       this.authenticationComponent.setCurrentUser(USER_ONE);
       
       sendRequest(new GetRequest(URL_LINKS_LIST), Status.STATUS_NOT_FOUND);
    }
    
    /**
     * Test for <a href=https://issues.alfresco.com/jira/browse/MNT-11964>MNT-11964</a>
     * @throws Exception 
     */
    public void testCreateLinkPermission() throws Exception
    {
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        String siteName = SITE_SHORT_NAME_LINKS + GUID.generate();
        this.siteService.createSite("LinkSitePreset", siteName, "SiteTitle", "SiteDescription", SiteVisibility.PUBLIC);
        
        String userName = USER_ONE + GUID.generate();
        createUser(userName, SiteModel.SITE_COLLABORATOR, siteName);

        // Check permissions for admin
        checkLinkPermissions(siteName);
        
        // Check permissions for user
        this.authenticationComponent.setCurrentUser(userName);
        checkLinkPermissions(siteName);

        // Cleanup
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        this.siteService.deleteSite(siteName);
        
        // Create a new site as user
        this.authenticationComponent.setCurrentUser(userName);
        siteName = SITE_SHORT_NAME_LINKS + GUID.generate();
        this.siteService.createSite("LinkSitePreset", siteName, "SiteTitle", "SiteDescription", SiteVisibility.PUBLIC);
        
        // Check permissions for user
        checkLinkPermissions(siteName);
        
        // Check permissions for admin
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        checkLinkPermissions(siteName);
        
        // Cleanup
        this.siteService.deleteSite(siteName);
        this.personService.deletePerson(userName);
    }
    
    private void checkLinkPermissions(String siteName) throws Exception
    {
        String url = "/api/links/site/" + siteName + "/links";
        url += "?filter=" + "all";
        url += "&startIndex=0&page=1&pageSize=4";
        Response response = sendRequest(new GetRequest(url), 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        
        assertTrue("The user sould have permission to create a new link.", result.getJSONObject("metadata").getJSONObject("linkPermissions").getBoolean("create"));
    }

    public void testCommentLink() throws Exception
    {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        JSONObject link = createLink(LINK_TITLE_ONE, "commented link", LINK_URL_ONE, false, Status.STATUS_OK);
        postLookup.execute();
        feedGenerator.execute();
        int activityNumStart = activityService.getSiteFeedEntries(SITE_SHORT_NAME_LINKS).size();
        String name = getNameFromLink(link);
        link = getLink(name, Status.STATUS_OK);
        String nodeRef = link.getString("nodeRef");
        JSONObject commentOne = createComment(nodeRef, "comment", "content", 200);
        postLookup.execute();
        feedGenerator.execute();
        int activityNumNext = activityService.getSiteFeedEntries(SITE_SHORT_NAME_LINKS).size();
        assertEquals("The activity feeds were not generated after adding a comment", activityNumStart + 1, activityNumNext);
        activityNumStart = activityNumNext;
        NodeRef commentNodeRef = new NodeRef(commentOne.getString("nodeRef"));
        sendRequest(new DeleteRequest(getDeleteCommentUrl(commentNodeRef)), 200);
        postLookup.execute();
        feedGenerator.execute();
        activityNumNext = activityService.getSiteFeedEntries(SITE_SHORT_NAME_LINKS).size();
        assertEquals("The activity feeds were not generated after deleting a comment", activityNumStart + 1, activityNumNext);
    }

    private JSONObject createComment(String nodeRef, String title, String content, int expectedStatus)
            throws Exception
    {
        JSONObject comment = new JSONObject();
        comment.put("title", title);
        comment.put("content", content);
        comment.put("site", SITE_SHORT_NAME_LINKS);
        Response response = sendRequest(new PostRequest(getCommentsUrl(nodeRef), comment.toString(), "application/json"), expectedStatus);

        if (expectedStatus != 200)
        {
            return null;
        }

        //logger.debug("Comment created: " + response.getContentAsString());
        JSONObject result = new JSONObject(response.getContentAsString());
        return result.getJSONObject("item");
    }

    private String getCommentsUrl(String nodeRef)
    {
        return "/api/node/" + nodeRef.replace("://", "/") + "/comments";
    }

    private String getCommentUrl(String nodeRef)
    {
        return "/api/comment/node/" + nodeRef.replace("://", "/");
    }

    private String getDeleteCommentUrl(NodeRef commentNodeRef)
    {
        String itemTitle = "Test Title";
        String page = "document-details";

        String URL = MessageFormat.format(URL_DELETE_COMMENT, new Object[] { commentNodeRef.getStoreRef().getProtocol(),
                commentNodeRef.getStoreRef().getIdentifier(), commentNodeRef.getId(), SITE_SHORT_NAME_LINKS, itemTitle, page});
        return URL;
    }
}
