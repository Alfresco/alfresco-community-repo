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
package org.alfresco.repo.web.scripts.calendar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.NodeRef;
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
import org.json.JSONStringer;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import com.sun.star.lang.IllegalArgumentException;

/**
 * Unit Test to test the Calendaring Web Script API
 * 
 * @author Nick Burch
 */
public class CalendarRestApiTest extends BaseWebScriptTest
{
	@SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(CalendarRestApiTest.class);
	
    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    private SiteService siteService;
    
    private static final String USER_ONE = "UserOneSecondToo";
    private static final String USER_TWO = "UserTwoSecondToo";
    private static final String SITE_SHORT_NAME_CALENDAR = "CalendarSiteShortNameTest";
    
    private static final String EVENT_TITLE_ONE = "TestEventOne";
    private static final String EVENT_TITLE_TWO = "TestEventTwo";
    private static final String EVENT_TITLE_THREE = "TestEventThree";

    private static final String URL_EVENT_BASE = "/calendar/event/" + SITE_SHORT_NAME_CALENDAR + "/"; 
    private static final String URL_EVENTS_LIST = "/calendar/eventList"; 
    private static final String URL_EVENTS_LIST_ICS = "/calendar/eventList-" + SITE_SHORT_NAME_CALENDAR + ".ics"; 
    private static final String URL_EVENT_CREATE = "/calendar/create"; 
    
    private static final String URL_USER_EVENTS_LIST = "/calendar/events/user"; 
    private static final String URL_USER_SITE_EVENTS_LIST = "/calendar/events/" + SITE_SHORT_NAME_CALENDAR + "/user"; 

    
    // General methods

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.authenticationService = (MutableAuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        this.siteService = (SiteService)getServer().getApplicationContext().getBean("SiteService");
        
        // Authenticate as user
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        // Create test site
        // - only create the site if it doesn't already exist
        SiteInfo siteInfo = this.siteService.getSite(SITE_SHORT_NAME_CALENDAR);
        if (siteInfo == null)
        {
            this.siteService.createSite("CalendarSitePreset", SITE_SHORT_NAME_CALENDAR, "CalendarSiteTitle", "BlogSiteDescription", SiteVisibility.PUBLIC);
        }
        
        // Ensure the calendar container is there
        if(!siteService.hasContainer(SITE_SHORT_NAME_CALENDAR, "calendar"))
        {
            siteService.createContainer(SITE_SHORT_NAME_CALENDAR, "calendar", null, null);
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
        
        // delete invite site
        siteService.deleteSite(SITE_SHORT_NAME_CALENDAR);
        
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
            personProps.put(ContentModel.PROP_FIRSTNAME, "FirstName123");
            personProps.put(ContentModel.PROP_LASTNAME, "LastName123");
            personProps.put(ContentModel.PROP_EMAIL, "FirstName123.LastName123@email.com");
            personProps.put(ContentModel.PROP_JOBTITLE, "JobTitle123");
            personProps.put(ContentModel.PROP_JOBTITLE, "Organisation123");
            
            // create person node for user
            this.personService.createPerson(personProps);
        }
        
        // add the user as a member with the given role
        this.siteService.setMembership(SITE_SHORT_NAME_CALENDAR, userName, role);
    }
    
    
    // Test helper methods
    
    private JSONObject getEntry(String name, int expectedStatus) throws Exception
    {
       Response response = sendRequest(new GetRequest(URL_EVENT_BASE + name), expectedStatus);
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
    
    private JSONObject createEntry(String name, String where, String description, 
          int expectedStatus)
    throws Exception
    {
       JSONObject json = new JSONObject();
       json.put("site", SITE_SHORT_NAME_CALENDAR);
       json.put("what", name);
       json.put("where", where);
       json.put("desc", description);
       json.put("from", "2011/06/29"); // TODO
       json.put("to", "2011/06/29"); // TODO
       json.put("fromdate", "Wednesday, 29 June 2011"); // TODO
       json.put("todate", "Wednesday, 29 June 2011"); // TODO
       json.put("start", "12:00"); // TODO
       json.put("end", "13:00"); // TODO
       json.put("tags", "");
       json.put("docfolder", "");
       json.put("page", "calendar");
       
       Response response = sendRequest(new PostRequest(URL_EVENT_CREATE, json.toString(), "application/json"), expectedStatus);
       if (expectedStatus == Status.STATUS_OK)
       {
          JSONObject result = new JSONObject(response.getContentAsString());
          if(result.has("event"))
          {
             return result.getJSONObject("event");
          }
          return result;
       }
       else
       {
          return null;
       }
    }
    
    public JSONObject updateEntry(String name, String what, String where, String description, 
          int expectedStatus) throws Exception
    {
       JSONObject json = new JSONObject();
       json.put("what", what);
       json.put("where", where);
       json.put("desc", description);
       json.put("from", "2011/06/29"); // TODO
       json.put("to", "2011/06/29"); // TODO
       json.put("fromdate", "Wednesday, 29 June 2011"); // TODO
       json.put("todate", "Wednesday, 29 June 2011"); // TODO
       json.put("start", "12:00"); // TODO
       json.put("end", "13:00"); // TODO
       json.put("tags", "");
       json.put("docfolder", "");
       json.put("page", "calendar");
       
       Response response = sendRequest(new PutRequest(URL_EVENT_BASE + name, json.toString(), "application/json"), expectedStatus);
       if (expectedStatus == Status.STATUS_OK)
       {
          JSONObject result = new JSONObject(response.getContentAsString());
          if(result.has("event"))
          {
             return result.getJSONObject("event");
          }
          if(result.has("data"))
          {
             return result.getJSONObject("data");
          }
          return result;
       }
       else
       {
          return null;
       }
    }
    
    /**
     * Gets the event name (ics timestamp based) from an entry
     */
    private String getNameFromEntry(JSONObject entry) throws Exception
    {
       if(! entry.has("uri"))
       {
          throw new IllegalArgumentException("No uri in " + entry.toString());
       }
    
       String uri = entry.getString("uri");
       String name = uri.substring( 
             uri.indexOf(SITE_SHORT_NAME_CALENDAR) + SITE_SHORT_NAME_CALENDAR.length() + 1
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
     * Creating, editing, fetching and deleting an entry
     */
    public void testCreateEditDeleteEntry() throws Exception
    {
       JSONObject entry;
       String name;
       
       
       // Won't be there to start with
       entry = getEntry(EVENT_TITLE_ONE, Status.STATUS_OK);
       assertEquals(true, entry.has("error"));
       
       
       // Create
       entry = createEntry(EVENT_TITLE_ONE, "Where", "Thing", Status.STATUS_OK);
       name = getNameFromEntry(entry);
       
       assertEquals(EVENT_TITLE_ONE, entry.getString("name"));
       assertEquals("Where", entry.getString("where"));
       // TODO Check more
       
       
       // Fetch
       entry = getEntry(name, Status.STATUS_OK);
       
       assertEquals("Error found " + entry.toString(), false, entry.has("error"));
       assertEquals(EVENT_TITLE_ONE, entry.getString("what"));
       assertEquals(name, entry.getString("name"));
       assertEquals("Where", entry.getString("location")); // Not where...
       assertEquals("Thing", entry.getString("description"));
       // TODO Check more
       
       
       // Edit
       entry = updateEntry(name, EVENT_TITLE_ONE, "More Where", "More Thing", Status.STATUS_OK);
       assertEquals("Error found " + entry.toString(), false, entry.has("error"));
       assertEquals(EVENT_TITLE_ONE, entry.getString("summary"));
       assertEquals("More Where", entry.getString("location"));
       assertEquals("More Thing", entry.getString("description"));
       // TODO Check more
       
       
       // Fetch
       entry = getEntry(name, Status.STATUS_OK);
       
       assertEquals("Error found " + entry.toString(), false, entry.has("error"));
       assertEquals(EVENT_TITLE_ONE, entry.getString("what"));
       assertEquals(name, entry.getString("name"));
       assertEquals("More Where", entry.getString("location")); // Not where...
       assertEquals("More Thing", entry.getString("description"));
       // TODO Check more

       
       // Delete
       sendRequest(new DeleteRequest(URL_EVENT_BASE + name), Status.STATUS_NO_CONTENT);
       
       
       // Fetch, will have gone
       entry = getEntry(EVENT_TITLE_ONE, Status.STATUS_OK);
       assertEquals(true, entry.has("error"));
       
       // Can't delete again
       sendRequest(new DeleteRequest(URL_EVENT_BASE + name), Status.STATUS_NOT_FOUND);
    }
    
    /**
     * Listing
     */
    public void testOverallListing() throws Exception
    {
       
    }
    
    /**
     * Listing for a user
     */
    public void testUserListing() throws Exception
    {
       
    }
    
}