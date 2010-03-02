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
package org.alfresco.repo.web.scripts.activities.feed.control;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyMap;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Unit test the Activity Service's User Feed Control Web Script API
 * 
 * @author janv
 */
public class FeedControlTest extends BaseWebScriptTest
{    
    private static Log logger = LogFactory.getLog(FeedControlTest.class);
    
    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    
    private static final String TEST_USER = "my user";
    
    private static final String TEST_SITE_ID = "my site";
    private static final String TEST_APP_TOOL_ID = "my app tool";
    
    private static final String URL_CONTROLS = "/api/activities/feed/controls";
    private static final String URL_CONTROL = "/api/activities/feed/control";

    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.authenticationService = (MutableAuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
    
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        // Create users
        createUser(TEST_USER);
        
        // Do tests as user one
        this.authenticationComponent.setCurrentUser(TEST_USER);
    }
    
    private void createUser(String userName)
    {
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            this.authenticationService.createAuthentication(userName, "PWD".toCharArray());
            
            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");
            
            this.personService.createPerson(ppOne);
        }        
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        this.authenticationComponent.clearCurrentSecurityContext();
    }
    
    public void testCreateFeedControls() throws Exception
    {
        createFeedControl(TEST_SITE_ID, null);
        createFeedControl(null, TEST_APP_TOOL_ID);
        createFeedControl(TEST_SITE_ID, TEST_APP_TOOL_ID);
    }
    
    protected void createFeedControl(String siteId, String appToolId) throws Exception
    {
        // Set (create) feed control
        JSONObject feedControl = new JSONObject();
        feedControl.put("siteId", siteId);
        feedControl.put("appToolId", appToolId);
        
        int expectedStatus = 200;
        Response response = sendRequest(new PostRequest(URL_CONTROL, feedControl.toString(), "application/json"), expectedStatus); 
    
        if (logger.isDebugEnabled())
        {
            logger.debug(response);
        }
    }
    
    public void testRetrieveFeedControls() throws Exception
    {
        // Get (retrieve) feed controls
        int expectedStatus = 200;
        Response response = sendRequest(new GetRequest(URL_CONTROLS), expectedStatus);        
        JSONArray result = new JSONArray(response.getContentAsString());
        
        if (logger.isDebugEnabled())
        {
            logger.debug(result);
        }
        
        assertNotNull(result);
        assertEquals(3, result.length());
    }
    
    public void testDeleteFeedControls() throws Exception
    {
        deleteFeedControl(TEST_SITE_ID, null);
        deleteFeedControl(null, TEST_APP_TOOL_ID);
        deleteFeedControl(TEST_SITE_ID, TEST_APP_TOOL_ID);
    }
    
    protected void deleteFeedControl(String siteId, String appToolId) throws Exception
    {
        // Unset (delete) feed control
        int expectedStatus = 200;
        Response response = sendRequest(new DeleteRequest(URL_CONTROL + "?s=" + TEST_SITE_ID + "&a=" + TEST_APP_TOOL_ID), expectedStatus);
    
        if (logger.isDebugEnabled())
        {
            logger.debug(response);
        }
    }
}
