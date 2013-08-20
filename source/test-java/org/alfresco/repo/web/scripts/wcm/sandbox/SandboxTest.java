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

package org.alfresco.repo.web.scripts.wcm.sandbox;


import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.alfresco.util.PropertyMap;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Junit tests of the REST bindings for WCM Sandbox and WCM Sandboxes
 * @author mrogers
 *
 */
public class SandboxTest  extends BaseWebScriptTest {
	
    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    
    private static final String USER_ONE = "WebProjectTestOne";
    private static final String USER_TWO = "WebProjectTestTwo";
    private static final String USER_THREE = "WebProjectTestThree";
    private static final String USER_FOUR = "WebProjectTestFour";
    public static final String ROLE_CONTENT_MANAGER     = "ContentManager";
    public static final String ROLE_CONTENT_PUBLISHER   = "ContentPublisher";
    public static final String ROLE_CONTENT_REVIEWER    = "ContentReviewer";
    public static final String ROLE_CONTENT_CONTRIBUTOR = "ContentContributor";
    
    private static final String URL_WEB_PROJECT = "/api/wcm/webprojects";
    private static final String URI_MEMBERSHIPS = "/memberships"; 
    private static final String URI_SANDBOXES = "/sandboxes"; 
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
        createUser(USER_FOUR);
        
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
    
    /**
     * Create a new membership
     */
    private void createMembership(String webProjectRef, String userName, String role) throws Exception
    {
        String validURL = URL_WEB_PROJECT + "/" + webProjectRef + "/memberships";
    	JSONObject membership = new JSONObject();
    	membership.put("role", ROLE_CONTENT_MANAGER);
    	JSONObject person = new JSONObject();
    	person.put("userName", USER_TWO);
    	membership.put("person", person);
    
    	sendRequest(new PostRequest(validURL, membership.toString(), "application/json"), Status.STATUS_OK);
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
        		sendRequest(new DeleteRequest(URL_WEB_PROJECT + "/" + webProjectRef), 0);
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
        Response response = sendRequest(new PostRequest(URL_WEB_PROJECT, webProj.toString(), "application/json"), Status.STATUS_OK); 
        
        JSONObject result = new JSONObject(response.getContentAsString());
        JSONObject data = result.getJSONObject("data");
        String webProjectRef = data.getString("webprojectref");
        
        assertNotNull("webproject ref is null", webProjectRef);
        this.createdWebProjects.add(webProjectRef);
        return webProjectRef;
        
     } 


    /**
     * CRUD Sandbox
     * Create a sandbox, get it, and delete it
     * Is update supported? - There are no read-write attributes yet.
     */
    public void testCreateSandbox() throws Exception
    {
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
    	String webprojref = createWebProject();
    	   	
    	createMembership(webprojref, USER_ONE, ROLE_CONTENT_MANAGER);
    	
    	String sandboxref ;
    	
    	/** 
    	 * Create a sandbox
    	 */
        {
        	JSONObject box = new JSONObject();
        	box.put("userName", USER_ONE);
        	String validURL = "/api/wcm/webprojects/" + webprojref + "/sandboxes";
        	Response response = sendRequest(new PostRequest(validURL, box.toString(), "application/json"), Status.STATUS_OK); 
        
        	JSONObject result = new JSONObject(response.getContentAsString());
        	JSONObject data = result.getJSONObject("data");
        	
        	sandboxref = data.getString("sandboxref");
        	String url = data.getString("url");
        	String name = data.getString("name");
        	JSONObject createdDate = data.getJSONObject("createdDate");
        	String createdOn = createdDate.getString("iso8601");
        	String createdBy = data.getString("creator");
        	boolean isAuthorSandbox = data.getBoolean("isAuthorSandbox");
        	boolean isStagingSandbox = data.getBoolean("isStagingSandbox");
        	assertNotNull("created date is null", createdOn );
        	assertNotNull("created by is null", createdBy );
        	assertNotNull("sandboxref is null", sandboxref);
        	assertNotNull("url is null", url);
        	assertNotNull("name is null", name);
         	assertTrue("not author sandbox", isAuthorSandbox);
          	assertFalse("is staging sandbox", isStagingSandbox);
        	
        	// check created date - throws exception if format invalid
        	@SuppressWarnings("unused")
			java.util.Date d = ISO8601DateFormat.parse(createdOn);
        	
        	// lookup url returned
        	sendRequest(new GetRequest(url), Status.STATUS_OK);
        }
        String sandboxURL = "/api/wcm/webprojects/" + webprojref + "/sandboxes/" + sandboxref;
       
        /**
         * Get the sandbox
         */
   	    sendRequest(new GetRequest(sandboxURL), Status.STATUS_OK);
        
   	
    	/**
    	 * Delete the sandbox
    	 */
		sendRequest(new DeleteRequest(sandboxURL), Status.STATUS_OK);
		
    	/** 
    	 * Create a sandbox - negative test - no userName
    	 */
		{
			JSONObject box = new JSONObject();
			String validURL = "/api/wcm/webprojects/" + webprojref + "/sandboxes";
			sendRequest(new PostRequest(validURL, box.toString(), "application/json"), Status.STATUS_BAD_REQUEST); 
		}
         
    }
    
    /**
     * Test the list sandbox method
     */
    public void testListSandbox() throws Exception
    {
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
    	String webprojref = createWebProject();
    	
    	/**
    	 * Call the list sandboxes method
    	 */
    	{
    	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES;
       	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
       	    JSONObject result = new JSONObject(list.getContentAsString());
    	    JSONArray lookupResult = result.getJSONArray("data");
    	        	    
    	    // By default there should be a staging sandbox
    	    assertTrue("list of sandboxes is empty", lookupResult.length() > 0);
    	}
    	
    	createMembership(webprojref, USER_ONE, ROLE_CONTENT_MANAGER);
    	createMembership(webprojref, USER_TWO, ROLE_CONTENT_REVIEWER);
    	createMembership(webprojref, USER_THREE, ROLE_CONTENT_CONTRIBUTOR);
    	String validURL = "/api/wcm/webprojects/" + webprojref + "/sandboxes";
    	JSONObject box = new JSONObject();
    	box.put("userName", USER_ONE);
    	sendRequest(new PostRequest(validURL, box.toString(), "application/json"), Status.STATUS_OK); 
    	box.put("userName", USER_TWO);
    	sendRequest(new PostRequest(validURL, box.toString(), "application/json"), Status.STATUS_OK); 
    	box.put("userName", USER_THREE);
    	sendRequest(new PostRequest(validURL, box.toString(), "application/json"), Status.STATUS_OK); 
    	
    	/**
    	 * List the sandboxes belonging to USER_ONE
    	 */
    	{
    	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "?userName=" + USER_ONE;
       	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
       	    JSONObject result = new JSONObject(list.getContentAsString());
    	    JSONArray lookupResult = result.getJSONArray("data");
    	    
    	    assertTrue("testListUserSandbox", lookupResult.length() == 1);
        	JSONObject obj1 = lookupResult.getJSONObject(0);
    	    String url = obj1.getString("url");
        	String name = obj1.getString("name");
        	assertNotNull("url is null", url);
           	assertNotNull("name is null", name);
           	
           	/**
           	 * Should be able to lookup the url returned
           	 */
       	    sendRequest(new GetRequest(url), Status.STATUS_OK);
    	}
    	
    	/**
    	 * List the sandboxes belonging to USER_TWO
    	 */
    	{
    	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "?userName=" + USER_TWO;
       	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
       	    JSONObject result = new JSONObject(list.getContentAsString());
    	    JSONArray lookupResult = result.getJSONArray("data");
    	    
    	    assertTrue("testListUserSandbox", lookupResult.length() == 1);
    	}
    	
    	/**
    	 * Call the list sandboxes method
    	 */
    	{
    	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES;
       	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
       	    JSONObject result = new JSONObject(list.getContentAsString());
    	    JSONArray lookupResult = result.getJSONArray("data");
    	        	    
    	    // There have been 3 creates above
    	    assertTrue("list of sandboxes is empty", lookupResult.length() > 3);
    	}
    	
    	/**
    	 * Negative test
    	 * Call the list sandbox method for a web project that does not exist
    	 */
    	{
    		String sandboxesURL = URL_WEB_PROJECT + "/" + "twaddle" + URI_SANDBOXES;
    		sendRequest(new GetRequest(sandboxesURL), Status.STATUS_NOT_FOUND);
    	}
    	
    	/**
    	 * Negative test
    	 * Call the list sandbox method for a user that does not have a sandbox project that does not exist
    	 */
    	{
    		String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref  + URI_SANDBOXES + "?userName=" + USER_FOUR;
    	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
       	    JSONObject result = new JSONObject(list.getContentAsString());
    	    JSONArray lookupResult = result.getJSONArray("data");
    	    assertTrue("lookup user 4 (not existing) found a sandbox", lookupResult.length() == 0);
    	}
    	  	
    }
    

    /**
     * Test the get sandbox method 
     */
    public void testGetSandbox() throws Exception
    {
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
    	String webprojref = createWebProject();
    	
    	createMembership(webprojref, USER_ONE, ROLE_CONTENT_MANAGER);
    	
    	String sandboxref ;
    	
    	/** 
    	 * Create a sandbox
    	 */
        {
        	JSONObject box = new JSONObject();
        	box.put("userName", USER_ONE);
        	String validURL = "/api/wcm/webprojects/" + webprojref + "/sandboxes";
        	Response response = sendRequest(new PostRequest(validURL, box.toString(), "application/json"), Status.STATUS_OK); 
       	    JSONObject result = new JSONObject(response.getContentAsString());
         	JSONObject data = result.getJSONObject("data");
        	sandboxref = data.getString("sandboxref");
        	assertNotNull("sandboxref is null", sandboxref);

        }
        String sandboxURL = "/api/wcm/webprojects/" + webprojref + "/sandboxes/" + sandboxref;
    	
    	/**
    	 * Call the get sandbox method for a web project 
    	 */
    	{
    		Response response = sendRequest(new GetRequest(sandboxURL), Status.STATUS_OK);
        	JSONObject result = new JSONObject(response.getContentAsString());
         	JSONObject data = result.getJSONObject("data");
        	sandboxref = data.getString("sandboxref");
        	String url = data.getString("url");
        	String name = data.getString("name");
        	assertNotNull("sandboxref is null", sandboxref);
        	assertNotNull("url is null", url);
        	assertNotNull("name is null", name);
        	
        	JSONObject createdDate = data.getJSONObject("createdDate");
        	String createdOn = createdDate.getString("iso8601");
        	String createdBy = data.getString("creator");
        	assertNotNull("created date is null", createdOn );
        	assertNotNull("created by is null", createdBy );
    	}
    	
    	/**
    	 * Negative test
    	 * Call the list sandbox method for a web project that does not exist
    	 */
       	{
            String invalidWebprojURL = "/api/wcm/webprojects/" + "twaddle" + "/sandboxes/" + sandboxref;
       	    sendRequest(new GetRequest(invalidWebprojURL), Status.STATUS_NOT_FOUND);
       	}
       	
    	/**
    	 * Negative test
    	 * Call the list sandbox method for a web project that does exist and a sandbox that doesn't
    	 */
       	{
            String invalidboxURL = "/api/wcm/webprojects/" + webprojref + "/sandboxes/" + "twaddle";
       	    sendRequest(new GetRequest(invalidboxURL), Status.STATUS_NOT_FOUND);
       	}
    }
    
    /**
     * Test the delete sandbox method
     */
    public void testDeleteSandbox() throws Exception
    {
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
    	String webprojref = createWebProject();
    	
    	createMembership(webprojref, USER_ONE, ROLE_CONTENT_MANAGER);
    	
      	/** 
    	 * Create a sandbox
    	 */
        JSONObject box = new JSONObject();
        box.put("userName", USER_ONE);
        String validURL = "/api/wcm/webprojects/" + webprojref + "/sandboxes";
        Response response = sendRequest(new PostRequest(validURL, box.toString(), "application/json"), Status.STATUS_OK); 
        JSONObject result = new JSONObject(response.getContentAsString());
     	JSONObject data = result.getJSONObject("data");
        String sandboxref = data.getString("sandboxref");
        assertNotNull("sandboxref is null", sandboxref);
        
        String sandboxURL = "/api/wcm/webprojects/" + webprojref + "/sandboxes/" + sandboxref;
        
        /**
         * Negative test - web project not exist
         */
   	    {
   	        String invalidProject = "/api/wcm/webprojects/" + "silly" + "/sandboxes/" + sandboxref;
   	    	sendRequest(new DeleteRequest(invalidProject), Status.STATUS_NOT_FOUND);
   	    }
   	    
   	    /**
   	     * Negative test - user sandbox not exist
   	     */
   	    {
   	        String invalidSandbox = "/api/wcm/webprojects/"  + webprojref + "/sandboxes/" + "silly";
   	    	sendRequest(new DeleteRequest(invalidSandbox), Status.STATUS_NOT_FOUND);
   	    }
         	
    	/**
    	 * Delete the sandbox - positive test
    	 */
   	    {
   	    	sendRequest(new DeleteRequest(sandboxURL), Status.STATUS_OK);
   	    }
   	    
    	/**
    	 * Negative test
    	 * Delete the sandbox that has already been deleted
    	 */
		
   	    {
   	    	sendRequest(new DeleteRequest(sandboxURL), Status.STATUS_NOT_FOUND);
   	    }	
    }
}
