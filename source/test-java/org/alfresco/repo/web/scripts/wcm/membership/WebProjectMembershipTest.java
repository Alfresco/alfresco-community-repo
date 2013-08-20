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
package org.alfresco.repo.web.scripts.wcm.membership;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyMap;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Unit test to test site Web Project Membership REST API
 * 
 * @author Mark Rogers
 */
public class WebProjectMembershipTest extends BaseWebScriptTest
{    
    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    
    private static final String USER_ONE = "WebProjectTestOne";
    private static final String USER_TWO = "WebProjectTestTwo";
    private static final String USER_THREE = "WebProjectTestThree";
    public static final String ROLE_CONTENT_MANAGER     = "ContentManager";
    public static final String ROLE_CONTENT_PUBLISHER   = "ContentPublisher";
    public static final String ROLE_CONTENT_REVIEWER    = "ContentReviewer";
    public static final String ROLE_CONTENT_CONTRIBUTOR = "ContentContributor";
    
    private static final String URL_WEB_PROJECTS = "/api/wcm/webprojects";
    private static final String URL_MEMBERSHIPS = "/memberships";  
	private static final String BASIC_NAME = "testProj";
	private static final String BASIC_DESCRIPTION = "testDescription";
	private static final String BASIC_TITLE = "testTitle";
	private static final String BASIC_DNSNAME = "testDNSName";
	

    
    private List<String> createdWebProjects = new ArrayList<String>(5);
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.authenticationService = (MutableAuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        // Create users
        createUser(USER_ONE);
        createUser(USER_TWO);
        createUser(USER_THREE);
        
        // Do tests as user one
        this.authenticationComponent.setCurrentUser(USER_ONE);
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
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        // Tidy-up any web projects created during the execution of the test
        for (String webProjectRef : this.createdWebProjects)
        {
        	try 
        	{
        		sendRequest(new DeleteRequest(URL_WEB_PROJECTS + "/" + webProjectRef), 0);
        	} 
        	catch (Exception e)
        	{
        		// ignore exception here
        	}
        }
        
        // Clear the list
        this.createdWebProjects.clear();
    }
    
    /**
     * create a web project
     * @return the webprojectref
     * @throws Exception
     */
    private String createWebProject() throws Exception
    {     
        /**
         * Create a web site
         */
        JSONObject webProj = new JSONObject();
        webProj.put("name", BASIC_NAME);
        webProj.put("description", BASIC_DESCRIPTION);
        webProj.put("title", BASIC_TITLE);
        webProj.put("dnsName", BASIC_DNSNAME); 
        Response response = sendRequest(new PostRequest(URL_WEB_PROJECTS, webProj.toString(), "application/json"), Status.STATUS_OK); 
        
        JSONObject result = new JSONObject(response.getContentAsString());
        JSONObject data = result.getJSONObject("data");
        String webProjectRef = data.getString("webprojectref");
        
        assertNotNull("webproject ref is null", webProjectRef);
        this.createdWebProjects.add(webProjectRef);
        return webProjectRef;
        
     } 
       
    public void testListMemberships() throws Exception
    {
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        // Create a site
    	String webProjectRef = createWebProject();
    	
		String validURL = URL_WEB_PROJECTS + "/" + webProjectRef + "/memberships";
        
		/**
		 * A newly created web project has 1 users (admin is a special case)
		 */
    	{
    		Response response = sendRequest(new GetRequest(validURL), Status.STATUS_OK);
    		JSONObject result = new JSONObject(response.getContentAsString()); 
    	    JSONArray data = result.getJSONArray("data");
       
    		assertNotNull(data);
    		assertEquals(1, data.length());
    	}
        
        /**
         *  Add USER_ONE (CONTENT_MANAGER)to the project and list it again
         */
        {
        	JSONObject membership = new JSONObject();
        	membership.put("role", ROLE_CONTENT_MANAGER);
        	JSONObject person = new JSONObject();
        	person.put("userName", USER_ONE);
        	membership.put("person", person);
        	sendRequest(new PostRequest(validURL, membership.toString(), "application/json"), Status.STATUS_OK);

    		Response response = sendRequest(new GetRequest(validURL), Status.STATUS_OK); 
        	JSONObject result = new JSONObject(response.getContentAsString());
    	    JSONArray data = result.getJSONArray("data");
        	assertNotNull(data);
        	assertEquals(2, data.length());
        	
        	boolean foundUser = false;
        	for(int i = 0; i < data.length(); i++)
        	{
            	JSONObject obj = data.getJSONObject(i);
            	if(USER_ONE.equals(obj.getJSONObject("person").get("userName"))) 
            	{
            		assertEquals(ROLE_CONTENT_MANAGER, obj.get("role"));
            		foundUser = true;
            	}
        		
        	}
        	assertTrue("user one not found", foundUser);
        	 
        }
        
        /**
         *  Add USER_TWO (CONTENT_REVIEWER) to the project and list it again
         */
        {
        	JSONObject membership = new JSONObject();
        	membership.put("role", ROLE_CONTENT_REVIEWER);
        	JSONObject person = new JSONObject();
        	person.put("userName", USER_TWO);
        	membership.put("person", person);
        	sendRequest(new PostRequest(validURL, membership.toString(), "application/json"), Status.STATUS_OK);

    		Response response = sendRequest(new GetRequest(validURL), Status.STATUS_OK); 
        	JSONObject result = new JSONObject(response.getContentAsString());
    	    JSONArray data = result.getJSONArray("data");
        	assertNotNull(data);
        	assertEquals(3, data.length());
        	
        	boolean foundUser = false;
        	for(int i = 0; i < data.length(); i++)
        	{
            	JSONObject obj = data.getJSONObject(i);
            	if(USER_TWO.equals(obj.getJSONObject("person").get("userName"))) 
            	{
            		assertEquals(ROLE_CONTENT_REVIEWER, obj.get("role"));
            		foundUser = true;
            	}
        		
        	}
        	assertTrue("user one not found", foundUser);
        	
        }
        
        /**
         * List the web sites with a user name = USER_TWO (should find 1)
         */
        {
            String stepURL = "/api/wcm/webprojects?userName=" + USER_TWO;
        	Response list = sendRequest(new GetRequest(stepURL), Status.STATUS_OK);
        	JSONObject response = new JSONObject(list.getContentAsString());
    	    JSONArray data = response.getJSONArray("data");
        	assertTrue(data.length() == 1);
        }
        
		/**
		 * Negative test 
		 * Project not found
		 */
    	{
    		String invalidURL = URL_WEB_PROJECTS + "/" + "NotExist" + "/memberships";
    		sendRequest(new GetRequest(invalidURL), Status.STATUS_NOT_FOUND);
    	}

    }
    
    public void testCreateMemberships() throws Exception
    {
    	this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
    
    	String webProjectRef = createWebProject();
    	
        String validURL = URL_WEB_PROJECTS + "/" + webProjectRef + "/memberships";
        
        /**
         * Create a new membership
         */
        {
        	JSONObject membership = new JSONObject();
        	membership.put("role", ROLE_CONTENT_MANAGER);
        	JSONObject person = new JSONObject();
        	person.put("userName", USER_TWO);
        	membership.put("person", person);
        
        	Response response = sendRequest(new PostRequest(validURL, membership.toString(), "application/json"), Status.STATUS_OK);
        	JSONObject result = new JSONObject(response.getContentAsString());
    	    JSONObject data = result.getJSONObject("data");
        
        	// Check the result
        	assertEquals(ROLE_CONTENT_MANAGER, data.get("role"));
        	assertEquals(USER_TWO, data.getJSONObject("person").get("userName")); 
        }
        
        /**
         * Get the membership
         */
        {
            String validGetURL = URL_WEB_PROJECTS + "/" + webProjectRef + "/memberships/" + USER_TWO;
        	Response response = sendRequest(new GetRequest(validGetURL), Status.STATUS_OK);
        	JSONObject result = new JSONObject(response.getContentAsString());
    	    JSONObject data = result.getJSONObject("data");
        
        	// Check the result
        	assertEquals(ROLE_CONTENT_MANAGER, data.get("role"));
        	assertEquals(USER_TWO, data.getJSONObject("person").get("userName"));
        }
        
        /**
         * Negative test  -- umm  -- this passes is this correct?
         * Create User two that already exists
         */
        {
        	JSONObject membership = new JSONObject();
        	membership.put("role", ROLE_CONTENT_MANAGER);
        	JSONObject person = new JSONObject();
        	person.put("userName", USER_TWO);
        	membership.put("person", person);
        
        	Response response = sendRequest(new PostRequest(validURL, membership.toString(), "application/json"), Status.STATUS_OK);
        	JSONObject result = new JSONObject(response.getContentAsString());
    	    JSONObject data = result.getJSONObject("data");
    	    
        	// Check the result
        	assertEquals(ROLE_CONTENT_MANAGER, data.get("role"));
        	assertEquals(USER_TWO, data.getJSONObject("person").get("userName")); 
        }
        
        /**
         * Negative test missing role
         */
        {
        	JSONObject membership = new JSONObject();

        	JSONObject person = new JSONObject();
        	person.put("userName", USER_TWO);
        	membership.put("person", person);
        	
           	sendRequest(new PostRequest(validURL, membership.toString(), "application/json"), Status.STATUS_BAD_REQUEST);
        }
        
        /**
         * Negative test missing person
         */
        {
        	JSONObject membership = new JSONObject();
        	membership.put("role", ROLE_CONTENT_MANAGER);
        	JSONObject person = new JSONObject();
        	person.put("userName", USER_TWO);
          	
           	sendRequest(new PostRequest(validURL, membership.toString(), "application/json"), Status.STATUS_BAD_REQUEST);
        }
    }
  
 
    public void testGetMembership() throws Exception
    {
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        // Create a site
    	String webProjectRef = createWebProject();
    	
        // Test error conditions
        sendRequest(new GetRequest(URL_WEB_PROJECTS + "/badsite" + URL_MEMBERSHIPS + "/" + USER_ONE), Status.STATUS_NOT_FOUND);
        String validURL = URL_WEB_PROJECTS + "/" + webProjectRef + URL_MEMBERSHIPS ;
        
        // User not found
        sendRequest(new GetRequest(validURL + "baduser"), Status.STATUS_NOT_FOUND);
        
        /**
         * Now lookup the admin user and check they are a content manager
         */
        Response response = sendRequest(new GetRequest(validURL + "/" + AuthenticationUtil.getAdminUserName()), Status.STATUS_OK);
        JSONObject result = new JSONObject(response.getContentAsString());
	    JSONObject data = result.getJSONObject("data");
	    
        // Check the result
        assertEquals(ROLE_CONTENT_MANAGER, data.get("role"));
        assertEquals(AuthenticationUtil.getAdminUserName(), data.getJSONObject("person").get("userName")); 
    }

//    Update Not yet implemented
    
//    public void testUpdateMembership() throws Exception
//    {
//    	
//       	this.authenticationComponent.setCurrentUser(USER_ADMIN);
//        
//    	String webProjectRef = createWebProject();
//    	
//        String validURL = URL_WEB_PROJECT + "/" + webProjectRef + "/memberships";
//        
//        String validUserTwoURL = URL_WEB_PROJECT + "/" + webProjectRef + "/membership/" + USER_TWO;
//        
//        
//        /**
//         * Negative test wrong project
//         */
//        {	
//            String invalidProject = URL_WEB_PROJECT + "/" + "I no exist" + "/membership/" + USER_TWO;
//        	JSONObject membership = new JSONObject();
//        	membership.put("role", ROLE_CONTENT_MANAGER);          	
//           	sendRequest(new PutRequest( invalidProject, membership.toString(), "application/json"), Status.STATUS_NOT_FOUND);
//        }
//             
//        /**
//         * Negative test wrong user
//         */
//        {
//            String invalidUser = URL_WEB_PROJECT + "/" + webProjectRef + "/membership/" + "Dr Nobody";
//        	JSONObject membership = new JSONObject();
//        	membership.put("role", ROLE_CONTENT_MANAGER);          	
//           	sendRequest(new PutRequest( invalidUser, membership.toString(), "application/json"), Status.STATUS_NOT_FOUND);
//        }
//
//        
//        /**
//         * Create a new membership USER_TWO with CONTENT_MANAGER
//         */
//        {
//        	JSONObject membership = new JSONObject();
//        	membership.put("role", ROLE_CONTENT_MANAGER);
//        	JSONObject person = new JSONObject();
//        	person.put("userName", USER_TWO);
//        	membership.put("person", person);
//        	sendRequest(new PostRequest(validURL, membership.toString(), "application/json"), Status.STATUS_OK);       
//        }
//        
//        /**
//         * Negative test missing role
//         */
//        {
//        	JSONObject membership = new JSONObject();        	
//           	sendRequest(new PutRequest(validUserTwoURL, membership.toString(), "application/json"), Status.STATUS_BAD_REQUEST);
//        }
//
//        
//        /**
//         * Now change USER_TWO to a  ROLE_CONTENT_CONTRIBUTOR
//         */
//        {
//        	JSONObject membership = new JSONObject();
//        	membership.put("role", ROLE_CONTENT_CONTRIBUTOR);
//        	Response response = sendRequest(new PutRequest(validUserTwoURL, membership.toString(),"application/json"), Status.STATUS_OK);
//        	JSONObject result = new JSONObject(response.getContentAsString());
//        
//        	// Check the result
//        	assertEquals(ROLE_CONTENT_CONTRIBUTOR, result.get("role"));
//        	assertEquals(USER_TWO, result.getJSONObject("person").get("userName"));
//        }
//        
//        /**
//         * Go back and get the role
//         */
//        {
//        	Response response = sendRequest(new GetRequest(validUserTwoURL), Status.STATUS_OK);
//        	JSONObject result = new JSONObject(response.getContentAsString());
//        
//        	// Check the result
//        	assertEquals(ROLE_CONTENT_CONTRIBUTOR, result.get("role"));
//        	assertEquals(USER_TWO, result.getJSONObject("person").get("userName"));
//        }
//
//
//    	
//    }
  
      public void testDeleteMembership() throws Exception
      {
      	this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
    	String webProjectRef = createWebProject();
    	
        String validURL = URL_WEB_PROJECTS + "/" + webProjectRef + "/memberships";
        
        /**
         * Create a new membership
         */
        {
        	JSONObject membership = new JSONObject();
        	membership.put("role", ROLE_CONTENT_MANAGER);
        	JSONObject person = new JSONObject();
        	person.put("userName", USER_TWO);
        	membership.put("person", person);
        
        	Response response = sendRequest(new PostRequest(validURL, membership.toString(), "application/json"), Status.STATUS_OK);
        	JSONObject result = new JSONObject(response.getContentAsString());
    	    JSONObject data = result.getJSONObject("data");
    	    
        	// Check the result
        	assertEquals(ROLE_CONTENT_MANAGER, data.get("role"));
        	assertEquals(USER_TWO, data.getJSONObject("person").get("userName")); 
        }
        
        String validGetURL = URL_WEB_PROJECTS + "/" + webProjectRef + URL_MEMBERSHIPS + "/" + USER_TWO;
        
        {
        	sendRequest(new GetRequest(validGetURL), Status.STATUS_OK);
        }
        /**
         * Delete the membership for USER_TWO
         */
        {
            sendRequest(new DeleteRequest(validGetURL), Status.STATUS_OK);
        }
        
        /**
         * lookup should now fail
         */
        {
        	sendRequest(new GetRequest(validGetURL), Status.STATUS_NOT_FOUND);
        }
        
        /**
         * Negative test - delete user two again - should not exist
         * Delete the membership
         */
        {
            sendRequest(new DeleteRequest(validGetURL), Status.STATUS_NOT_FOUND);
        }
        
        
        
      }
    
}
