/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.links;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
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
 */
public class LinksRestApiTest extends BaseWebScriptTest
{
	@SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(LinksRestApiTest.class);
	
    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    private NodeService nodeService;
    private SiteService siteService;
    
    private static final String USER_ONE = "UserOneSecondToo";
    private static final String USER_TWO = "UserTwoSecondToo";
    private static final String USERDETAILS_FIRSTNAME = "FirstName123";
    private static final String USERDETAILS_LASTNAME = "LastName123";
    private static final String SITE_SHORT_NAME_LINKS = "LinkSiteShortNameTest";
    
    private static final String LINK_TITLE_ONE   = "TestLinkOne";
    private static final String LINK_TITLE_TWO   = "TestLinkTwo";
    private static final String LINK_TITLE_THREE = "TestLinkThree";
    private static final String LINK_URL_ONE   = "http://google.com/";
    private static final String LINK_URL_TWO   = "http://alfresco.com/";
    private static final String LINK_URL_THREE = "http://share.alfresco.com/";

    private static final String URL_LINKS_BASE = "/api/links/site/" + SITE_SHORT_NAME_LINKS + "/links"; 
    private static final String URL_LINKS_LIST = URL_LINKS_BASE;
    private static final String URL_LINKS_CREATE = URL_LINKS_BASE + "/posts";
    private static final String URL_LINKS_UPDATE = URL_LINKS_BASE + "/"; // plus path
    private static final String URL_LINKS_DELETE = "/api/links/delete/site/" + SITE_SHORT_NAME_LINKS + "/links";
    private static final String URL_LINKS_FETCH = "/api/links/link/site/" + SITE_SHORT_NAME_LINKS + "/links/"; // plus path 
    
    
    // General methods

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.authenticationService = (MutableAuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        this.nodeService = (NodeService)getServer().getApplicationContext().getBean("NodeService");
        this.siteService = (SiteService)getServer().getApplicationContext().getBean("SiteService");
        
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
        if(!siteService.hasContainer(SITE_SHORT_NAME_LINKS, "links"))
        {
            siteService.createContainer(SITE_SHORT_NAME_LINKS, "links", null, null);
        }
        
        // Create users
        createUser(USER_ONE, SiteModel.SITE_COLLABORATOR);
        createUser(USER_TWO, SiteModel.SITE_COLLABORATOR);

        // Do tests as inviter user
        this.authenticationComponent.setCurrentUser(USER_ONE);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        // admin user required to delete user
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        // delete the site
        siteService.deleteSite(SITE_SHORT_NAME_LINKS);
        
        // delete the users
        personService.deletePerson(USER_ONE);
        if(this.authenticationService.authenticationExists(USER_ONE))
        {
           this.authenticationService.deleteAuthentication(USER_ONE);
        }
        
        personService.deletePerson(USER_TWO);
        if(this.authenticationService.authenticationExists(USER_TWO))
        {
           this.authenticationService.deleteAuthentication(USER_TWO);
        }
    }
    
    private void createUser(String userName, String role)
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
        this.siteService.setMembership(SITE_SHORT_NAME_LINKS, userName, role);
    }
    
    
    // Test helper methods
    
    private JSONObject getLinks(String filter, String username, String from) throws Exception
    {
       String url = URL_LINKS_LIST;
       if(filter == null)
       {
          filter = "all";
       }
       url += "?filter=" + filter;
       
       if(username != null)
       {
          url += "&user=" + username;
       }
       if(from != null)
       {
          url += "from=" + from;
       }
       
       Response response = sendRequest(new GetRequest(url), 200);
       JSONObject result = new JSONObject(response.getContentAsString());
       return result;
    }
    
    private JSONObject getLink(String name, int expectedStatus) throws Exception
    {
       Response response = sendRequest(new GetRequest(URL_LINKS_FETCH + name), expectedStatus);
       if (expectedStatus == Status.STATUS_OK)
       {
          JSONObject result = new JSONObject(response.getContentAsString());
          if(result.has("item"))
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
          boolean internal, int expectedStatus)
    throws Exception
    {
       JSONObject json = new JSONObject();
       json.put("site", SITE_SHORT_NAME_LINKS);
       json.put("title", title);
       json.put("description", description);
       json.put("url", url);
       json.put("tags", "");
       if(internal)
       {
          json.put("internal", "true");
       }
       json.put("page", "links-view"); // TODO Is this really needed?
       
       Response response = sendRequest(new PostRequest(URL_LINKS_CREATE, json.toString(), "application/json"), expectedStatus);
       if (expectedStatus == Status.STATUS_OK)
       {
          JSONObject result = new JSONObject(response.getContentAsString());
          if(result.has("link"))
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
          if(result.has("links"))
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
       JSONArray items = new JSONArray();
       items.put(name);
       
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
       // TODO
    }
    
    /**
     * Gets the link name (link- timestamp based) from a returned link
     */
    private String getNameFromLink(JSONObject link) throws Exception
    {
       if(link.has("name"))
       {
          return link.getString("name");
       }
       
       if(! link.has("uri"))
       {
          throw new IllegalArgumentException("No uri in " + link.toString());
       }
    
// TODO
       String uri = link.getString("uri");
       String name = uri.substring( 
             uri.indexOf(SITE_SHORT_NAME_LINKS) + SITE_SHORT_NAME_LINKS.length() + 1
       );
       
       if(name.indexOf('?') > 0)
       {
          return name.substring(0, name.indexOf('?'));
       }
       else
       {
          return name;
       }
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
       link = getLinks(null, null, null);
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
       
       // Check the noderef, comments url, created on
       // TODO
//       "commentsUrl": "/node/workspace\/SpacesStore\/7a8ea18e-8ff0-4337-b5af-b732d9e8d6e9/comments",
//       "nodeRef": "workspace://SpacesStore/7a8ea18e-8ff0-4337-b5af-b732d9e8d6e9",
//       "createdOn": "Jul 28 2011 17:23:20 GMT+0100 (BST)",

       
       
       // Edit
       // We should get a simple message
       link = updateLink(name, LINK_TITLE_ONE, "More Thing 1", LINK_URL_ONE, true, Status.STATUS_OK);
       assertEquals(
             "Incorrect JSON: " + link.toString(), 
             true, link.has("message")
       );
       assertEquals(
             "Incorrect JSON: " + link.toString(), 
             true, link.getString("message").contains("updated")
       );
       
       
       
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
       link = deleteLink(name, Status.STATUS_OK);
       assertEquals(
             "Incorrect JSON: " + link.toString(), 
             true, link.has("message")
       );
       assertEquals(
             "Incorrect JSON: " + link.toString(), 
             true, link.getString("message").contains("deleted")
       );
       
       
       // Fetch, will have gone
       link = getLink(name, Status.STATUS_NOT_FOUND);
       
       
       // Can't delete again
       deleteLink(name, Status.STATUS_NOT_FOUND);
       
       
       // Can't edit it when it's deleted
       sendRequest(new PutRequest(URL_LINKS_UPDATE + name, "{}", "application/json"), Status.STATUS_NOT_FOUND);
    }
    
    /**
     * Listing
     */
    public void testOverallListing() throws Exception
    {
       JSONObject dates;
       JSONArray entries;
if(1!=0) { return; } // TODO Finish       
       
       // Initially, there are no events
       dates = getLinks(null, null, null);
       assertEquals("Incorrect JSON: " + dates.toString(), true, dates.has("total"));
       assertEquals(0, dates.getInt("total"));
       
       
       // Add two links to get started with
       createLink(LINK_TITLE_ONE, "Thing 1", LINK_URL_ONE, false, Status.STATUS_OK);
       createLink(LINK_TITLE_TWO, "Thing 2", LINK_URL_TWO, false, Status.STATUS_OK);
       
       // Check again
       dates = getLinks(null, null, null);
       
       // Should have two links
       assertEquals("Incorrect JSON: " + dates.toString(), true, dates.has("total"));
System.err.println(dates.toString());       
       assertEquals(2, dates.getInt("total"));
     
       entries = dates.getJSONArray("6/29/2011");
       assertEquals(2, entries.length());
       assertEquals(LINK_TITLE_ONE, entries.getJSONObject(0).getString("name"));
       assertEquals(LINK_TITLE_TWO, entries.getJSONObject(1).getString("name"));
       
       
       // Add a third, which is internal
       JSONObject link3 = createLink(LINK_TITLE_THREE, "Thing 3", LINK_URL_THREE, true, Status.STATUS_OK);
       String name3 = getNameFromLink(link3);
       updateLink(name3, LINK_TITLE_THREE, "More Where 3", LINK_URL_THREE, false, Status.STATUS_OK);
       
       
       // Check now, should have three links
       dates = getLinks(null, null, null);
       assertEquals(2, dates.length());
       assertEquals("6/29/2011", dates.names().getString(0));
       assertEquals("6/28/2011", dates.names().getString(1));
       
       entries = dates.getJSONArray("6/29/2011");
       assertEquals(2, entries.length());
       assertEquals(LINK_TITLE_ONE, entries.getJSONObject(0).getString("name"));
       assertEquals(LINK_TITLE_TWO, entries.getJSONObject(1).getString("name"));
       assertEquals(LINK_TITLE_THREE, entries.getJSONObject(1).getString("name"));
       
       
       // Ask for filtering
       // TODO
       
       
       // Ask for paging
       // TODO
       
       
       // By date
       // TODO
    }
    
    /**
     * Listing for a user
     */
    public void testUserListing() throws Exception
    {
       JSONObject result;
       JSONArray events;
if(1!=0) { return; } // TODO Finish       
       
       // Initially, there are no events
       result = getLinks(null, null, null);
       assertEquals(0, result.length());
       
       result = getLinks(null,"admin", null);
       events = result.getJSONArray("events");
       assertEquals(0, events.length());

       
       // Add two links to start with
       createLink(LINK_TITLE_ONE, "Thing 1", LINK_URL_ONE, false, Status.STATUS_OK);
       createLink(LINK_TITLE_TWO, "Thing 2", LINK_URL_TWO, false, Status.STATUS_OK);
       
       // Check again, should see both
       result = getLinks(null,"admin", null);
       assertEquals(1, result.length());
       
       result = getLinks(null, "admin", "2000/01/01"); // With a from date
       events = result.getJSONArray("events");
       assertEquals(2, events.length());
       assertEquals(LINK_TITLE_ONE, events.getJSONObject(0).getString("title"));
       assertEquals(LINK_TITLE_TWO, events.getJSONObject(1).getString("title"));
       
       result = getLinks(null, "admin", null); // Without a from date
       events = result.getJSONArray("events");
       assertEquals(2, events.length());
       assertEquals(LINK_TITLE_ONE, events.getJSONObject(0).getString("title"));
       assertEquals(LINK_TITLE_TWO, events.getJSONObject(1).getString("title"));
       
       
       // Add a third, on the previous day
       JSONObject link3 = createLink(LINK_TITLE_THREE, "Thing 3", LINK_URL_THREE, true, Status.STATUS_OK);
       String name3 = getNameFromLink(link3);
       pushLinkCreatedDateBack(name3, 2);
       
       
       // Check getting all of them
       result = getLinks(null,"admin", null);
       events = result.getJSONArray("events");
       assertEquals(3, events.length());
       assertEquals(LINK_TITLE_THREE, events.getJSONObject(0).getString("title"));
       assertEquals(LINK_TITLE_ONE, events.getJSONObject(1).getString("title"));
       assertEquals(LINK_TITLE_TWO, events.getJSONObject(2).getString("title"));
       assertEquals(SITE_SHORT_NAME_LINKS, events.getJSONObject(0).getString("site"));
       assertEquals(SITE_SHORT_NAME_LINKS, events.getJSONObject(1).getString("site"));
       assertEquals(SITE_SHORT_NAME_LINKS, events.getJSONObject(2).getString("site"));
       

       // Now set a date filter to constrain
       result = getLinks(null, "admin", "2011/06/29");
       events = result.getJSONArray("events");
       assertEquals(2, events.length());
       assertEquals(LINK_TITLE_ONE, events.getJSONObject(0).getString("title"));
       assertEquals(LINK_TITLE_TWO, events.getJSONObject(1).getString("title"));
       
       result = getLinks(null, "admin", "2999/01/01"); // Future
       events = result.getJSONArray("events");
       assertEquals(0, events.length());
       
       
       // Try for a different user
       result = getLinks(null, USER_ONE, "2011/06/29");
       events = result.getJSONArray("events");
       assertEquals(0, events.length());
       
       result = getLinks(null, USER_ONE, null);
       events = result.getJSONArray("events");
       assertEquals(0, events.length());
       
       
       // Now hide the site, and remove the user from it, events will go
       this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
       SiteInfo site = siteService.getSite(SITE_SHORT_NAME_LINKS);
       site.setVisibility(SiteVisibility.PRIVATE);
       siteService.updateSite(site);
       siteService.removeMembership(SITE_SHORT_NAME_LINKS, USER_ONE);
       this.authenticationComponent.setCurrentUser(USER_ONE);
       
       result = getLinks(null, "admin", null);
       assertEquals(0, events.length());
    }
}