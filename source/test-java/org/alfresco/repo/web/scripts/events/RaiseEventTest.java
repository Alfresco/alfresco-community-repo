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
package org.alfresco.repo.web.scripts.events;

import java.util.List;

import org.alfresco.events.types.BrowserEvent;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.events.EventPublisherForTestingOnly;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.PropertyMap;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;

/**
 * Tests the {@link RaiseBrowserEventPost} endpoint
 */
public class RaiseEventTest extends BaseWebScriptTest
{
    
    private static final String USER_ONE = "RaiseEventTestuser1";
    private static final String SITE_EVENTS_TEST = "Site_Raise_Event_Test";
    private static final String POST_URL = "/api/events/";
    
    private EventPublisherForTestingOnly eventPublisher;
    private AuthenticationComponent authenticationComponent;
    private SiteService siteService;   
    private MutableAuthenticationService authenticationService;
    private PersonService personService;
   
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.siteService = (SiteService)getServer().getApplicationContext().getBean("SiteService");
        this.eventPublisher = (EventPublisherForTestingOnly)getServer().getApplicationContext().getBean("eventPublisher");
        this.authenticationService = (MutableAuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        // Create test site
        // - only create the site if it doesn't already exist
        SiteInfo siteInfo = this.siteService.getSite(SITE_EVENTS_TEST);
        if (siteInfo == null)
        {
            this.siteService.createSite("RaiseEventSitePreset", SITE_EVENTS_TEST, SITE_EVENTS_TEST+" Title", "SiteDescription", SiteVisibility.PUBLIC);
        }
        
        createUser(USER_ONE, SiteModel.SITE_COLLABORATOR);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        // admin user required to delete user
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        SiteInfo siteInfo = this.siteService.getSite(SITE_EVENTS_TEST);
        if (siteInfo != null)
        {
            // delete invite site
            siteService.deleteSite(SITE_EVENTS_TEST);
         }
        
        // delete the users
        deleteUser(USER_ONE);
    }
    
    /**
     * Tests POST requests to create browser events.
     * 
     * @throws Exception
     */
    public void testRaiseEvent() throws Exception
    {
       sendRequest(new PostRequest(POST_URL+"mypage/edit", "", "application/json"), Status.STATUS_OK);
       sendRequest(new PostRequest(POST_URL, "", "application/json"), Status.STATUS_NOT_FOUND);
       sendRequest(new PostRequest(POST_URL+"mypage", "", "application/json"), Status.STATUS_NOT_FOUND);
       sendRequest(new GetRequest(POST_URL+SITE_EVENTS_TEST+"/sitepage/view"), Status.STATUS_METHOD_NOT_ALLOWED);
       sendRequest(new PostRequest(POST_URL+SITE_EVENTS_TEST+"/sitepage/view", "", "application/json"), Status.STATUS_OK);
       sendRequest(new PostRequest(POST_URL+SITE_EVENTS_TEST+"/sitepage/view", "rubbish", "application/json"), Status.STATUS_BAD_REQUEST);
       sendRequest(new PostRequest(POST_URL+SITE_EVENTS_TEST+"/specialpage/view", "{\"source\": \"bob\", \"target\": \"hope\"}", "application/json"), Status.STATUS_OK);
       
       List<BrowserEvent> browserEvents = eventPublisher.getQueueByType(BrowserEvent.class);
       int found = 0;
       for (BrowserEvent be : browserEvents)
       {
          if ("mypage".equals(be.getComponent()))
          {
              found ++;
              assertEquals("edit", be.getAction());
              continue;
          }
          
          if ("sitepage".equals(be.getComponent()))
          {
              found ++;
              assertEquals(SITE_EVENTS_TEST, be.getSiteId());
              assertEquals("view", be.getAction());
              continue;
          }
          
          if ("specialpage".equals(be.getComponent()))
          {
              found ++;
              assertEquals(SITE_EVENTS_TEST, be.getSiteId());
              assertEquals("view", be.getAction());
              assertEquals("{\"source\": \"bob\", \"target\": \"hope\"}", be.getAttributes());
              continue;
          } 
       }
       assertEquals(3, found); //Found and validated 3 events.
   
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
        this.siteService.setMembership(SITE_EVENTS_TEST, userName, role);
    }
    private void deleteUser(String userName)
    {
       personService.deletePerson(userName);
       if (this.authenticationService.authenticationExists(userName))
       {
          this.authenticationService.deleteAuthentication(userName);
       }
    }

}
