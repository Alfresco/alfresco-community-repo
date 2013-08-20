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
package org.alfresco.repo.web.scripts.wcm;

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
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Unit test to test site Web Project REST API
 * 
 * @author Mark Rogers
 */
public class WebProjectTest extends BaseWebScriptTest
{    
    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    
    private static final String USER_ONE = "WebProjectTestOne";
    private static final String USER_TWO = "WebProjectTestTwo";
    private static final String USER_THREE = "WebProjectTestThree";
    
    private static final String URL_WEB_PROJECTS = "/api/wcm/webprojects";
    
	private static final String BASIC_NAME = "testProj";
	private static final String BASIC_UPDATED_NAME = "updatedName";
	private static final String BASIC_DESCRIPTION = "testDescription";
	private static final String BASIC_UPDATED_DESCRIPTION = "updatedDescription";
	private static final String BASIC_TITLE = "testTitle";
	private static final String BASIC_UPDATED_TITLE = "updatedTitle";
	private static final String BASIC_DNSNAME = "testDNSName";
	
	private static final String BAD_PROJECT_REF="doesNotExist";
    
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
    
    public void testBasicCRUDWebProject() throws Exception
    {     
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
    	sendRequest(new DeleteRequest(URL_WEB_PROJECTS + "/" + BASIC_DNSNAME),0 );  
        
        /**
         * Create a web site
         */
        JSONObject webProj = new JSONObject();
        webProj.put("name", BASIC_NAME);
        webProj.put("description", BASIC_DESCRIPTION);
        webProj.put("title", BASIC_TITLE);
        webProj.put("dnsName", BASIC_DNSNAME); 
        webProj.put("isTemplate", true);
        
        Response response = sendRequest(new PostRequest(URL_WEB_PROJECTS, webProj.toString(), "application/json"), Status.STATUS_OK); 
        
        JSONObject top = new JSONObject(response.getContentAsString());
        JSONObject result = top.getJSONObject("data");
        String webProjectRef = result.getString("webprojectref");
        
        assertNotNull("webproject ref is null", webProjectRef);
        this.createdWebProjects.add(webProjectRef);
        
    	assertEquals(BASIC_NAME, result.get("name"));
       	assertEquals(BASIC_DESCRIPTION, result.get("description"));
       	assertEquals(BASIC_TITLE, result.get("title"));
     	// not yet implemented
       	//assertEquals(true, result.get("isTemplate"));
       	
       	/**
       	 *  Read the web site we created above
       	 */
        Response lookup = sendRequest(new GetRequest(URL_WEB_PROJECTS + "/" + webProjectRef), Status.STATUS_OK);
        {
        	JSONObject lookupResult = new JSONObject(lookup.getContentAsString());
        	JSONObject data = lookupResult.getJSONObject("data");
        	assertEquals(BASIC_NAME, data.get("name"));
        	assertEquals(BASIC_DESCRIPTION, data.get("description"));
        	assertEquals(BASIC_TITLE, data.get("title"));
        	String url = data.getString("url");
        	assertNotNull("url is null", url);
        	sendRequest(new GetRequest(url), Status.STATUS_OK);
        }
        
       	/**
       	 *  Update the name property on the web site we created above
       	 */
        {
        	JSONObject update = new JSONObject();
        	update.put("name", BASIC_UPDATED_NAME);
        	Response updateResponse = sendRequest(new PutRequest(URL_WEB_PROJECTS + "/" + webProjectRef, update.toString(), "application/json"), Status.STATUS_OK);
        	JSONObject updateResult = new JSONObject(updateResponse.getContentAsString());        	
        	JSONObject data = updateResult.getJSONObject("data");  
        	assertEquals(BASIC_UPDATED_NAME, data.get("name"));
        	assertEquals(BASIC_DESCRIPTION, data.get("description"));
        	assertEquals(BASIC_TITLE, data.get("title"));
        }
          	
        /**
         * Delete the web site we created above
         */
    	sendRequest(new DeleteRequest(URL_WEB_PROJECTS + "/" + webProjectRef), Status.STATUS_OK);      	 
    } // END testBasicCRUDWebProject
    
    public void testListWebSites() throws Exception
    {
    	int LOOP_COUNT = 5;
    	        
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());

        for(int i = 0; i < LOOP_COUNT; i++)
        {
        	/**
        	 * Create a web site
        	 */
        	JSONObject webProj = new JSONObject();
        	webProj.put("name", BASIC_NAME + i);
        	webProj.put("description", BASIC_DESCRIPTION + i);
        	webProj.put("title", BASIC_TITLE + i);
        	webProj.put("dnsName", BASIC_DNSNAME + i); 
        	Response response = sendRequest(new PostRequest(URL_WEB_PROJECTS, webProj.toString(), "application/json"), Status.STATUS_OK); 
        
        	JSONObject result = new JSONObject(response.getContentAsString());
        	JSONObject data = result.getJSONObject("data");  
        	String webProjectRef = data.getString("webprojectref");
            this.createdWebProjects.add(webProjectRef);
        }
        
        /**
         * List the web sites
         */
        {
        	Response list = sendRequest(new GetRequest(URL_WEB_PROJECTS), Status.STATUS_OK);
        
        	JSONObject lookupResult = new JSONObject(list.getContentAsString());
        	JSONArray data = lookupResult.getJSONArray("data");  
        	assertTrue(data.length() >= LOOP_COUNT);
        	
        	/**
        	 * Now check that the list contains the sites created above
        	 */
        	int foundCount = 0;
        	
        	for(int i = 0; i < data.length(); i++)
        	{
        		JSONObject obj = data.getJSONObject(i);
        		String name = obj.getString("name");
        		if(name.contains(BASIC_NAME))
        		{
        			foundCount++;
        		}
        	}
        	assertTrue (foundCount >= LOOP_COUNT);
        }
        
        /**
         * List the web sites with a user name (should find 0)
         */
        {
            String stepURL = "/api/wcm/webprojects?userName=Freddy";
        	Response listResponse = sendRequest(new GetRequest(stepURL), Status.STATUS_OK);
        	JSONObject lookupResult = new JSONObject(listResponse.getContentAsString());
         	JSONArray data = lookupResult.getJSONArray("data");   
        	assertTrue(data.length() == 0);
        }
    } // testListWebSites
    
    public void testUpdateWebProject() throws Exception
    {
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        /**
         * Create a web site
         */
        JSONObject webProj = new JSONObject();
        webProj.put("name", BASIC_NAME);
        webProj.put("description", BASIC_DESCRIPTION);
        webProj.put("title", BASIC_TITLE);
        webProj.put("dnsName", BASIC_DNSNAME); 
        Response response = sendRequest(new PostRequest(URL_WEB_PROJECTS, webProj.toString(), "application/json"), Status.STATUS_OK); 
        JSONObject top = new JSONObject(response.getContentAsString());
        JSONObject result = top.getJSONObject("data");
        String webProjectRef = result.getString("webprojectref");
        
        
        assertNotNull("webproject ref is null", webProjectRef);
        this.createdWebProjects.add(webProjectRef);
        
       	/**
       	 *  Read the web site we created above to double check it created correctly
       	 */
        {
        	Response lookup = sendRequest(new GetRequest(URL_WEB_PROJECTS + "/" + webProjectRef), Status.STATUS_OK);
        	JSONObject lookupResult = new JSONObject(lookup.getContentAsString());    
            JSONObject data = lookupResult.getJSONObject("data");
        	assertEquals(BASIC_NAME, data.get("name"));
        	assertEquals(BASIC_DESCRIPTION, data.get("description"));
        	assertEquals(BASIC_TITLE, data.get("title"));
        }

        /*
         * Update description only
         */
        {
        	JSONObject update = new JSONObject();
        	update.put("description", BASIC_UPDATED_DESCRIPTION);
        	Response updateResponse = sendRequest(new PutRequest(URL_WEB_PROJECTS + "/" + webProjectRef, update.toString(), "application/json"), Status.STATUS_OK);
        	JSONObject updateResult = new JSONObject(updateResponse.getContentAsString());    

        	/*
        	 * Read the result, check description updated and other properties remain unchanged
        	 */
            Response lookup = sendRequest(new GetRequest(URL_WEB_PROJECTS + "/" + webProjectRef), Status.STATUS_OK);
            JSONObject lookupResult = new JSONObject(lookup.getContentAsString());    
            JSONObject data = lookupResult.getJSONObject("data");
            assertEquals(BASIC_NAME, data.get("name"));
            assertEquals(BASIC_UPDATED_DESCRIPTION, data.get("description"));
            assertEquals(BASIC_TITLE, data.get("title"));
        }
        
        /*
         * Update title only, description (from previous step) and title should be updated
         */
        {
        	JSONObject update = new JSONObject();
        	update.put("title", BASIC_UPDATED_TITLE);
        	Response updateResponse = sendRequest(new PutRequest(URL_WEB_PROJECTS + "/" + webProjectRef, update.toString(), "application/json"), Status.STATUS_OK);
        	new JSONObject(updateResponse.getContentAsString());    

        	/*
        	 * Read the result, check description updated and other properties unchanged
        	 */
            Response lookup = sendRequest(new GetRequest(URL_WEB_PROJECTS + "/" + webProjectRef), Status.STATUS_OK);
            JSONObject lookupResult = new JSONObject(lookup.getContentAsString());    
            JSONObject data = lookupResult.getJSONObject("data");
            assertEquals(BASIC_NAME, data.get("name"));
            assertEquals(BASIC_UPDATED_DESCRIPTION, data.get("description"));
            assertEquals(BASIC_UPDATED_TITLE, data.get("title"));
        }

        /**
         * Update name only (description and title should remain)
         */
        {
        	JSONObject update = new JSONObject();
        	update.put("name", BASIC_UPDATED_NAME);
        	Response updateResponse = sendRequest(new PutRequest(URL_WEB_PROJECTS + "/" + webProjectRef, update.toString(), "application/json"), Status.STATUS_OK);
        	JSONObject updateResult = new JSONObject(updateResponse.getContentAsString());    

        	/*
        	 * Read the result, check description updated and other properties unchanged
        	 */
            Response lookup = sendRequest(new GetRequest(URL_WEB_PROJECTS + "/" + webProjectRef), Status.STATUS_OK);
            JSONObject lookupResult = new JSONObject(lookup.getContentAsString());    
            JSONObject data = lookupResult.getJSONObject("data");
            assertEquals(BASIC_UPDATED_NAME, data.get("name"));
            assertEquals(BASIC_UPDATED_DESCRIPTION, data.get("description"));
            assertEquals(BASIC_UPDATED_TITLE, data.get("title"));
       	
        }
        
        /**
         * Negative test.
         * 
         * attempt to update a web project that does not exist - should give a 404.
         */
        {
        	JSONObject update = new JSONObject();
        	update.put("name", BASIC_UPDATED_NAME);
        	sendRequest(new PutRequest(URL_WEB_PROJECTS + "/" + BAD_PROJECT_REF, update.toString(), "application/json"), Status.STATUS_NOT_FOUND);
        }
    	
    } // end testUpdateWebProject
    
    public void testDeleteWebProject() throws Exception
    {
    	/**
    	 * Negative test
    	 * 
    	 * Delete a project that does not exist
    	 */
        {
        	sendRequest(new DeleteRequest(URL_WEB_PROJECTS + "/" + BAD_PROJECT_REF), Status.STATUS_NOT_FOUND);      	 
        }
    
    }  // end testDeleteWebproject
    
}
