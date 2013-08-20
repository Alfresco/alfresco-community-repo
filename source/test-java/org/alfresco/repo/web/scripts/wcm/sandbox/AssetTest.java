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


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Junit tests of the REST bindings for WCM Assets
 */
public class AssetTest  extends BaseWebScriptTest {
	
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
	
	private static final String WEBAPP_ROOT = "ROOT";
	private static final String WEBAPP_YELLOW = "YELLOW";
	private static final String WEBAPP_GREEN = "GREEN";

	private static final String ROOT_FILE = "index.htm";

	private static final String FIELD_DATA = "data";
    private static final String FIELD_PROPERTIES = "properties";
    private static final String FIELD_CONTENT = "content";

    private static final String PROP_NAME = "cm:name";
    private static final String PROP_TITLE = "cm:title";

    private static final String TEST_CONTENT_ENTRY = "This is test content entry for an Asset";

    // override jbpm.job.executor idleInterval to 5s (was 1.5m) for WCM unit tests
	private static final String SUBMIT_CONFIG_LOCATION = "classpath:wcm/wcm-jbpm-context.xml";
	private static final long SUBMIT_DELAY = 15000L; // (in millis) 15s - to allow time for async submit workflow to complete (as per 5s idleInterval above)
	
	    
    private List<String> createdWebProjects = new ArrayList<String>(5);
    
    @Override
    protected void setUp() throws Exception
    {
        setCustomContext(SUBMIT_CONFIG_LOCATION);
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
        JSONObject data = result.getJSONObject(FIELD_DATA);
        String webProjectRef = data.getString("webprojectref");
        
        assertNotNull("webproject ref is null", webProjectRef);
        this.createdWebProjects.add(webProjectRef);
        return webProjectRef;
        
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
    
    /**
     * 
     * @param user
     * @return sandboxref
     */
    private String createSandbox(String webprojref, String userName) throws org.json.JSONException, java.io.IOException
    {
    	String sandboxref = null;
    	{
    		JSONObject box = new JSONObject();
    		box.put("userName", userName);
    		String validURL = "/api/wcm/webprojects/" + webprojref + "/sandboxes";
    		Response response = sendRequest(new PostRequest(validURL, box.toString(), "application/json"), Status.STATUS_OK); 
    		JSONObject result = new JSONObject(response.getContentAsString());
    		JSONObject data = result.getJSONObject(FIELD_DATA);
    		sandboxref = data.getString("sandboxref");
    		assertNotNull("sandboxref is null", sandboxref);
    	}
    	return sandboxref;
    	
    }
    
    private void checkSandboxEmpty(String webprojref, String sandboxref) throws Exception
    {
    	String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
    	Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
    	JSONObject result = new JSONObject(list.getContentAsString());
    	JSONArray lookupResult = result.getJSONArray(FIELD_DATA);    
        assertTrue("sandbox is not empty", lookupResult.length() == 0); 
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
     * Test the modified assets (Web App) methods
     * @throws Exception
     */
    public void testModifiedAssetsWebAppTest() throws Exception
    {
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
    	String webprojref = createWebProject();
    	createMembership(webprojref, USER_ONE, ROLE_CONTENT_MANAGER);
    	String sandboxref = createSandbox(webprojref, USER_ONE);
    	createFolder(webprojref, sandboxref, "/www/avm_webapps", WEBAPP_YELLOW );
        String submitterURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/submitter";
        createFile(webprojref, sandboxref, WEBAPP_ROOT, "/", "rootFile1" );

        JSONObject submitForm = new JSONObject();
        submitForm.put("label", "the label");
        submitForm.put("comment", "the comment");
        submitForm.put("all", true);
        sendRequest(new PostRequest(submitterURL, submitForm.toString(), "application/json"), Status.STATUS_OK);
        
        Thread.sleep(SUBMIT_DELAY);
        
        /*
         * Background set up now create a new file which is our test
         */
        createFile(webprojref, sandboxref, "/www/avm_webapps/" + WEBAPP_YELLOW, "yellowFile1" );
        
    	/**
    	 * Get the modified asset and verify its format
    	 */
    	{
    	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified?webApp=" + WEBAPP_YELLOW;
       	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
       	    JSONObject result = new JSONObject(list.getContentAsString());
       	    System.out.println(list.getContentAsString());
    	    JSONArray lookupResult = result.getJSONArray(FIELD_DATA);
    	    
    	    assertTrue("testListUserSandbox", lookupResult.length() == 1);
    	    
    	    // Now check the contents..
    	    JSONObject x = lookupResult.getJSONObject(0);
    		String name = x.getString("name");
    		String path = x.getString("path");
    		String creator = x.getString("creator");
    		boolean isFile = x.getBoolean("isFile");
    		boolean isDeleted = x.getBoolean("isDeleted");
    		boolean isFolder = x.getBoolean("isFolder");
    		
    		assertNotNull("name is null", name);
    		assertEquals("name is wrong", "yellowFile1", name);
    		assertEquals("creator is wrong", AuthenticationUtil.getAdminUserName(), creator);
    		assertTrue("not isFile", isFile);
    		assertFalse("not isFolder", isFolder);
    		assertFalse("not isDeleted", isDeleted);
    		
    		assertNotNull("path is null", path);
    		assertEquals("path of MyFile1 is not correct", path, "/www/avm_webapps/YELLOW/yellowFile1");
    	}
    }
    
    /**
     * test the modified assets with a webapp methods
     * @throws Exception
     */
    public void testModifiedAssetsTest() throws Exception
    {
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
    	String webprojref = createWebProject();
    	createMembership(webprojref, USER_ONE, ROLE_CONTENT_MANAGER);
    	String sandboxref = createSandbox(webprojref, USER_ONE);
   	
    	/**
    	 * Get the modified assets within that sandbox   (should return nothing)
    	 */
    	{
    	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
       	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
       	    JSONObject result = new JSONObject(list.getContentAsString());
    	    JSONArray lookupResult = result.getJSONArray(FIELD_DATA);
    	    
    	    assertTrue("testListUserSandbox", lookupResult.length() == 0);
    	}
    	
    	/**
    	 * Negative test - Get the modified assets within that sandbox with an invalid web project - should get a 404
    	 */
    	{
    	    String sandboxesURL = URL_WEB_PROJECT + "/" + "crap" + URI_SANDBOXES + "/" + sandboxref + "/modified";
       	    sendRequest(new GetRequest(sandboxesURL), Status.STATUS_NOT_FOUND);
    	}
    	
    	/**
    	 * Negative test - Get the modified assets within that sandbox with an invalid sandbox - should get a 404
    	 */
    	{
    	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref  + URI_SANDBOXES + "/" + "crap" + "/modified";
       	    sendRequest(new GetRequest(sandboxesURL), Status.STATUS_NOT_FOUND);
    	}
    	
    	/**
    	 * add a single asset
    	 */
    	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/", "myFile1");
 
    	/**
    	 * Get the modified asset and verify its format
    	 */
    	{
    	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
       	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
       	    JSONObject result = new JSONObject(list.getContentAsString());
    	    JSONArray lookupResult = result.getJSONArray(FIELD_DATA);
    	    
    	    assertTrue("testListUserSandbox", lookupResult.length() == 1);
    	    
    	    // Now check the contents..
    	    JSONObject x = lookupResult.getJSONObject(0);
    		String name = x.getString("name");
    		String path = x.getString("path");
    		String creator = x.getString("creator");
    		boolean isFile = x.getBoolean("isFile");
    		boolean isDeleted = x.getBoolean("isDeleted");
    		boolean isFolder = x.getBoolean("isFolder");
    		
    		assertNotNull("name is null", name);
    		assertEquals("name is wrong", "myFile1", name);
    		assertEquals("creator is wrong", AuthenticationUtil.getAdminUserName(), creator);
    		assertTrue("not isFile", isFile);
    		assertFalse("not isDirectory", isFolder);
    		assertFalse("not isDeleted", isDeleted);
    		
    		assertNotNull("path is null", path);
    		assertEquals("path of MyFile1 is not correct", path, "/www/avm_webapps/ROOT/myFile1");
    		
    	}
    	
    	/**
    	 * Add a second asset
    	 */
    	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/", "fileA");
    		
    	/**
    	 * Get the modified assets should be myFile1, fileA
    	 */
    	{
    	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
       	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
       	    JSONObject result = new JSONObject(list.getContentAsString());
    	    JSONArray lookupResult = result.getJSONArray(FIELD_DATA);
    	    
    	    assertTrue("testListUserSandbox", lookupResult.length() == 2);
    	}    
    	
    	/**
    	 * Add a new dir containing assets
    	 */
    	createFolder(webprojref, sandboxref, WEBAPP_ROOT, "/", "dir1");
    	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/dir1", "filex");
    	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/dir1", "filey");

    	
    	/**
    	 * Get the modified assets should be myFile1, fileA, dir1 
    	 */
    	{
    	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
       	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
       	    JSONObject result = new JSONObject(list.getContentAsString());
    	    JSONArray lookupResult = result.getJSONArray(FIELD_DATA);
    	    
    	    assertTrue("testListUserSandbox", lookupResult.length() == 3);
    	}    	
    }
    
    /**
     * test the modified assets methods
     * @throws Exception
     */
    public void testSubmitAssetsTest() throws Exception
    { 
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
    	String webprojref = createWebProject();
    	createMembership(webprojref, USER_ONE, ROLE_CONTENT_MANAGER);
    	String sandboxref = createSandbox(webprojref, USER_ONE);
    	
    	/**
    	 * add a single asset
    	 */
    	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/", "myFile1");
    	          
        String submitterURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/submitter";

        /**
         * Submit all Negative tests - missing label
         */
        {
            JSONObject submitForm = new JSONObject();
            submitForm.put("comment", "the comment");
            submitForm.put("all", true);
            sendRequest(new PostRequest(submitterURL, submitForm.toString(), "application/json"), Status.STATUS_BAD_REQUEST);
        }
        
        /**
         * Submit all Negative tests - missing comment
         */
        {
            JSONObject submitForm = new JSONObject();
            submitForm.put("label", "the label");
            submitForm.put("all", true);
            sendRequest(new PostRequest(submitterURL, submitForm.toString(), "application/json"), Status.STATUS_BAD_REQUEST);
        }
   
        /**
         * Submit all Negative test - invalid project
         */
        {
            
            String crapURL = URL_WEB_PROJECT + "/" + "crap" + URI_SANDBOXES + "/" + sandboxref + "/submitter";
     
            JSONObject submitForm = new JSONObject();
            submitForm.put("label", "the label");
            submitForm.put("comment", "the comment");
            submitForm.put("all", true);
            sendRequest(new PostRequest(crapURL, submitForm.toString(), "application/json"), Status.STATUS_NOT_FOUND); 
        }
        
        /**
         * Submit all Negative test - invalid sandbox
         */
        {
            
            String crapURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + "crap" + "/submitter";
     
            JSONObject submitForm = new JSONObject();
            submitForm.put("label", "the label");
            submitForm.put("comment", "the comment");
            submitForm.put("all", true);
            sendRequest(new PostRequest(crapURL, submitForm.toString(), "application/json"), Status.STATUS_NOT_FOUND); 
        }
        
        /**
         * Submit all Negative test - none of all, assets or paths.
         */
        {
            JSONObject submitForm = new JSONObject();
            submitForm.put("label", "the label");
            submitForm.put("comment", "the comment");
            submitForm.put("all", false);
            sendRequest(new PostRequest(submitterURL, submitForm.toString(), "application/json"), Status.STATUS_BAD_REQUEST); 
        }
        
        /**
         * Positive test - Submit all
         */
        {
        	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/", "myFile2");
            
        	{
        	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
           	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
           	    JSONObject result = new JSONObject(list.getContentAsString());
        	    JSONArray lookupResult = result.getJSONArray(FIELD_DATA);
        	    
        	    assertTrue("testListUserSandbox", lookupResult.length() > 0);
        	} 
            
            JSONObject submitForm = new JSONObject();
            submitForm.put("label", "the label");
            submitForm.put("comment", "the comment");
            submitForm.put("all", true);
            sendRequest(new PostRequest(submitterURL, submitForm.toString(), "application/json"), Status.STATUS_OK);
            
            Thread.sleep(SUBMIT_DELAY);
            
        	checkSandboxEmpty(webprojref, sandboxref);

        }
        
        /**
         * Submit paths
         */
        {
        	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/", "myFile3");
            JSONObject submitForm = new JSONObject();
            submitForm.put("label", "the label");
            submitForm.put("comment", "the comment");
            
            JSONArray paths = new JSONArray();
            paths.put("/www/avm_webapps/ROOT/myFile3");
            submitForm.put("paths", paths);
            Response response = sendRequest(new PostRequest(submitterURL, submitForm.toString(), "application/json"), Status.STATUS_OK); 
         	
            Thread.sleep(SUBMIT_DELAY);
            
            checkSandboxEmpty(webprojref, sandboxref);
        }
        
        /**
         * Submit assets - get a list of modified assets and submit them back
         */
        {
        	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/", "myFile4");
            
        	{
        	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
           	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
           	    JSONObject result = new JSONObject(list.getContentAsString());
        	    JSONArray lookupResult = result.getJSONArray(FIELD_DATA);
        	    
        	    assertTrue("testListUserSandbox", lookupResult.length() > 0);
        	    
                JSONObject submitForm = new JSONObject();
                submitForm.put("label", "the label");
                submitForm.put("comment", "the comment");
                submitForm.put("assets", lookupResult);
                Response response = sendRequest(new PostRequest(submitterURL, submitForm.toString(), "application/json"), Status.STATUS_OK); 

        	}
        	
        	Thread.sleep(SUBMIT_DELAY);
        	
        	checkSandboxEmpty(webprojref, sandboxref);
        	    	
        	
         }
        
        /**
         * Submit assets more complex example - get a list of modified assets and submit them back
         * Also has a delete to process
         */
        {
            deleteFile(webprojref, sandboxref, "/www/avm_webapps/ROOT/myFile3");
        	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/", "buffy.jpg");
        	createFolder(webprojref, sandboxref, WEBAPP_ROOT, "/", "vampires");
        	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/vampires", "master");
        	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/vampires", "drusilla");
         	createFolder(webprojref, sandboxref, WEBAPP_ROOT, "/", "humans");
         	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/humans", "willow");
         	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/humans", "xander");
            
        	{
        	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
           	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
           	    JSONObject result = new JSONObject(list.getContentAsString());
        	    JSONArray lookupResult = result.getJSONArray(FIELD_DATA);
        	    
        	    assertTrue("testListUserSandbox", lookupResult.length() > 0);
        	    
                JSONObject submitForm = new JSONObject();
                submitForm.put("label", "the label");
                submitForm.put("comment", "the comment");
                submitForm.put("assets", lookupResult);
                Response response = sendRequest(new PostRequest(submitterURL, submitForm.toString(), "application/json"), Status.STATUS_OK); 

        	}
        	
        	Thread.sleep(SUBMIT_DELAY);
        	
        	checkSandboxEmpty(webprojref, sandboxref);
        	    	    	
         }
        

        /**
         * Now finally, a big complicated submission of assets and paths.
         */
        {
        	// single file in existing dir
        	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/vampires", "angel");
            
            //delete from an existing dir
        	deleteFile(webprojref, sandboxref, "/www/avm_webapps/ROOT/vampires/drusilla");
            
            // multiple file in existing dir
            createFile(webprojref, sandboxref, WEBAPP_ROOT, "/humans", "giles");
            createFile(webprojref, sandboxref, WEBAPP_ROOT, "/humans", "dawn");
            createFile(webprojref, sandboxref, WEBAPP_ROOT, "/humans", "anya");

            // new directory
            createFolder(webprojref, sandboxref, WEBAPP_ROOT, "/", "cast");
            createFile(webprojref, sandboxref, WEBAPP_ROOT, "/cast", "Anthony Head");
            createFile(webprojref, sandboxref, WEBAPP_ROOT, "/cast", "James Marsters");
            
        	{
        	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
           	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
           	    JSONObject result = new JSONObject(list.getContentAsString());
        	    JSONArray lookupResult = result.getJSONArray(FIELD_DATA);
        	    JSONArray assets = new JSONArray();
                JSONArray paths = new JSONArray();
                JSONArray omitted = new JSONArray();
        	    assertTrue("testListUserSandbox", lookupResult.length() > 4);
        	    
        	    /**
        	     * chop off 3 items from the modified list.   First 2 go into path which should leave 1 unsubmitted.
        	     */
        	    for(int i = 0; i < lookupResult.length(); i++)
        	    {
        	    	if (i < 2) 
        	    	{
        	    	    // do nothing	
        	    		omitted.put(lookupResult.getJSONObject(i).get("path"));
        	    	} 
        	    	else if ( i < 4)
        	    	{
        	    		// copy into paths
        	    		paths.put(lookupResult.getJSONObject(i).get("path"));
        	    	} 
        	    	else 
        	    	{
        	    		// copy into assets
        	    		assets.put(lookupResult.getJSONObject(i));
        	    	}
        	    }
        	    
                JSONObject submitForm = new JSONObject();
                submitForm.put("label", "the label");
                submitForm.put("comment", "the comment");
                submitForm.put("assets", assets);
                submitForm.put("paths", paths);
                
                Response response = sendRequest(new PostRequest(submitterURL, submitForm.toString(), "application/json"), Status.STATUS_OK); 
                
                Thread.sleep(SUBMIT_DELAY);
                
           	    Response listTwo = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
           	    JSONObject resultTwo = new JSONObject(listTwo.getContentAsString());
        	    JSONArray lookupResultTwo = resultTwo.getJSONArray(FIELD_DATA);
        	    assertTrue("testListUserSandbox", lookupResultTwo.length() == 2);
                
                /**
                 * Now submit the omitted two files                
                 */
                JSONObject submitOmitted = new JSONObject();
                submitOmitted.put("label", "the label");
                submitOmitted.put("comment", "the comment");
                submitOmitted.put("paths", omitted);
                sendRequest(new PostRequest(submitterURL, submitOmitted.toString(), "application/json"), Status.STATUS_OK); 
        	}
        	
        	Thread.sleep(SUBMIT_DELAY);
        	
        	checkSandboxEmpty(webprojref, sandboxref);
        	    	    	
         }
    }
    
    /**
     * Test the submit assets (Web App) methods
     * @throws Exception
     */
    public void testSubmitAssetsWebAppTest() throws Exception
    {
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
    	String webprojref = createWebProject();
    	createMembership(webprojref, USER_ONE, ROLE_CONTENT_MANAGER);
    	String sandboxref = createSandbox(webprojref, USER_ONE);
   	
    	createFolder(webprojref, sandboxref, "/www/avm_webapps", WEBAPP_YELLOW );
         
        String submitterURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/submitter";
        JSONObject submitForm = new JSONObject();
        submitForm.put("label", "the label");
        submitForm.put("comment", "the comment");
        submitForm.put("all", true);
        sendRequest(new PostRequest(submitterURL, submitForm.toString(), "application/json"), Status.STATUS_OK);
    
        /**
         * Now we can set up our test data
         */
    	createFile(webprojref, sandboxref, "/www/avm_webapps/" + WEBAPP_ROOT, "rootFile1" );
    	createFile(webprojref, sandboxref, "/www/avm_webapps/" + WEBAPP_YELLOW, "yellowFile1" );
        
        /** 
         * Submit YELLOW - Should leave root alone
         */       
        submitForm.put("label", "yellow submit");
        submitForm.put("comment", "yellow submit");
        submitForm.put("all", true);
        sendRequest(new PostRequest(submitterURL + "?webApp=" + WEBAPP_YELLOW, submitForm.toString(), "application/json"), Status.STATUS_OK);
        
        Thread.sleep(SUBMIT_DELAY);
        
    	/**
    	 * Get the modified asset (yellow should have been submitted leaving root
    	 */
    	{
    	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
       	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
       	    JSONObject result = new JSONObject(list.getContentAsString());
    	    JSONArray lookupResult = result.getJSONArray(FIELD_DATA);
    	    
    	    assertEquals("testListUserSandbox", lookupResult.length(), 1);
    	    
    	    // Now check the contents..
    	    JSONObject x = lookupResult.getJSONObject(0);
    		String name = x.getString("name");
    		
    		assertNotNull("name is null", name);
    		assertEquals("name is wrong", "rootFile1", name);
    	}
    }
    
    /**
     * test the revert assets methods
     * @throws Exception
     */
    public void testRevertAssetsTest() throws Exception
    {
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
    	String webprojref = createWebProject();
    	createMembership(webprojref, USER_ONE, ROLE_CONTENT_MANAGER);
    	String sandboxref = createSandbox(webprojref, USER_ONE);
    	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/", "myFile1");
        
        String reverterURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/reverter";

        /**
         * Revert all Negative test - invalid project
         */
        {
            
            String crapURL = URL_WEB_PROJECT + "/" + "crap" + URI_SANDBOXES + "/" + sandboxref + "/reverter";
     
            JSONObject submitForm = new JSONObject();
            submitForm.put("all", true);
            sendRequest(new PostRequest(crapURL, submitForm.toString(), "application/json"), Status.STATUS_NOT_FOUND); 
        }
        
        /**
         * Submit all Negative test - invalid sandbox
         */
        {
            
            String crapURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + "crap" + "/reverter";
     
            JSONObject submitForm = new JSONObject();
            submitForm.put("all", true);
            sendRequest(new PostRequest(crapURL, submitForm.toString(), "application/json"), Status.STATUS_NOT_FOUND); 
        }
        
        /**
         * Submit all Negative test - none of all, assets or paths.
         */
        {
            JSONObject submitForm = new JSONObject();
            submitForm.put("all", false);
            sendRequest(new PostRequest(reverterURL, submitForm.toString(), "application/json"), Status.STATUS_BAD_REQUEST); 
        }
        
        /**
         * Positive test - Revert all
         */
        {
        	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/", "myFile2");
            
        	{
        	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
           	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
           	    JSONObject result = new JSONObject(list.getContentAsString());
        	    JSONArray lookupResult = result.getJSONArray(FIELD_DATA);
        	    
        	    assertTrue("testListUserSandbox", lookupResult.length() > 0);
        	} 
            
            JSONObject submitForm = new JSONObject();
            submitForm.put("all", true);
            sendRequest(new PostRequest(reverterURL, submitForm.toString(), "application/json"), Status.STATUS_OK);
            
        	checkSandboxEmpty(webprojref, sandboxref);

        }
        
        /**
         * Revert via paths
         */
        {
        	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/", "myFile3");
            JSONObject submitForm = new JSONObject();
            
            JSONArray paths = new JSONArray();
            paths.put("/www/avm_webapps/ROOT/myFile3");
            submitForm.put("paths", paths);
            Response response = sendRequest(new PostRequest(reverterURL, submitForm.toString(), "application/json"), Status.STATUS_OK); 
         	checkSandboxEmpty(webprojref, sandboxref);
        }
        
        /**
         * Revert assets - get a list of modified assets and revert them back
         */
        {
        	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/", "myFile4");
            
        	{
        	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
           	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
           	    JSONObject result = new JSONObject(list.getContentAsString());
        	    JSONArray lookupResult = result.getJSONArray(FIELD_DATA);
        	    
        	    assertTrue("testListUserSandbox", lookupResult.length() > 0);
        	    
                JSONObject submitForm = new JSONObject();
                submitForm.put("assets", lookupResult);
                Response response = sendRequest(new PostRequest(reverterURL, submitForm.toString(), "application/json"), Status.STATUS_OK); 

        	}
        	checkSandboxEmpty(webprojref, sandboxref);
        	    
         }
        
        /**
         * Revert assets more complex example - get a list of modified assets and submit them back
         * Also has a delete to revert
         */
        {
        	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/", "buffy.jpg");
        	createFolder(webprojref, sandboxref, WEBAPP_ROOT, "/", "vampires");
        	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/vampires", "master");
        	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/vampires", "drusilla");
         	createFolder(webprojref, sandboxref, WEBAPP_ROOT, "/", "humans");
         	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/humans", "willow");
         	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/humans", "xander");
            
        	{
        	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
           	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
           	    JSONObject result = new JSONObject(list.getContentAsString());
        	    JSONArray lookupResult = result.getJSONArray(FIELD_DATA);
        	    
        	    assertTrue("testListUserSandbox", lookupResult.length() > 0);
        	    
                JSONObject submitForm = new JSONObject();
                submitForm.put("assets", lookupResult);
                Response response = sendRequest(new PostRequest(reverterURL, submitForm.toString(), "application/json"), Status.STATUS_OK); 

        	}
        	checkSandboxEmpty(webprojref, sandboxref);
        	    	    	
         }
        

        /**
         * Now finally, a big complicated reversion of assets and paths.
         */
        {
        	// First submit a chunk of data
        	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/", "buffy.jpg");
        	createFolder(webprojref, sandboxref, WEBAPP_ROOT, "/", "vampires");
        	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/vampires", "master");
        	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/vampires", "drusilla");
         	createFolder(webprojref, sandboxref, WEBAPP_ROOT, "/", "humans");
         	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/humans", "willow");
         	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/humans", "xander");

            JSONObject submitForm = new JSONObject();
            submitForm.put("label", "the label");
            submitForm.put("comment", "the comment");
            submitForm.put("all", true);
            String submitterURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/submitter";
            sendRequest(new PostRequest(submitterURL, submitForm.toString(), "application/json"), Status.STATUS_OK);
            
            Thread.sleep(SUBMIT_DELAY);
            
            // Now we can set up the data that will get reverted
            
        	// single file in existing dir
        	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/vampires", "angel");
            
            //delete from an existing dir
         	deleteFile(webprojref, sandboxref, "/www/avm_webapps/ROOT/vampires/drusilla");
           
            // multiple file in existing dir
          	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/humans", "giles");
          	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/humans", "dawn");
          	createFile(webprojref, sandboxref, WEBAPP_ROOT, "/humans", "anya");

            // new directory
        	createFolder(webprojref, sandboxref, WEBAPP_ROOT, "/", "cast");
            createFile(webprojref, sandboxref, WEBAPP_ROOT, "/cast", "Anthony Head");
            createFile(webprojref, sandboxref, WEBAPP_ROOT, "/cast", "James Marsters");

            
        	{
        	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
           	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
           	    JSONObject result = new JSONObject(list.getContentAsString());
        	    JSONArray lookupResult = result.getJSONArray(FIELD_DATA);
        	    JSONArray assets = new JSONArray();
                JSONArray paths = new JSONArray();
                JSONArray omitted = new JSONArray();
        	    assertTrue("testListUserSandbox", lookupResult.length() > 4);
        	    
        	    /**
        	     * chop off 3 items from the modified list.   First 2 go into path which should leave 1 unsubmitted.
        	     */
        	    for(int i = 0; i < lookupResult.length(); i++)
        	    {
        	    	if (i < 2) 
        	    	{
        	    	    // do nothing	
        	    		omitted.put(lookupResult.getJSONObject(i).get("path"));
        	    	} 
        	    	else if ( i < 4)
        	    	{
        	    		// copy into paths
        	    		paths.put(lookupResult.getJSONObject(i).get("path"));
        	    	} 
        	    	else 
        	    	{
        	    		// copy into assets
        	    		assets.put(lookupResult.getJSONObject(i));
        	    	}
        	    }
        	    
                JSONObject revertForm = new JSONObject();
                revertForm.put("assets", assets);
                revertForm.put("paths", paths);
                
                sendRequest(new PostRequest(reverterURL, revertForm.toString(), "application/json"), Status.STATUS_OK); 
                
           	    Response listTwo = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
           	    JSONObject resultTwo = new JSONObject(listTwo.getContentAsString());
        	    JSONArray lookupResultTwo = resultTwo.getJSONArray(FIELD_DATA);
        	    assertTrue("testListUserSandbox", lookupResultTwo.length() == 2);
                
                /**
                 * Now revert the omitted two files                
                 */
                JSONObject submitOmitted = new JSONObject();
                submitOmitted.put("paths", omitted);
                sendRequest(new PostRequest(reverterURL, submitOmitted.toString(), "application/json"), Status.STATUS_OK); 
        	}
        	checkSandboxEmpty(webprojref, sandboxref);
        	    	    	
         }
    }
    
    /**
     * Test the revert assets (Web App) methods
     * @throws Exception
     */
    public void testRevertAssetsWebAppTest() throws Exception
    {
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
    	String webprojref = createWebProject();
    	createMembership(webprojref, USER_ONE, ROLE_CONTENT_MANAGER);
    	String sandboxref = createSandbox(webprojref, USER_ONE);
   	
        createFolder(webprojref, sandboxref, "/www/avm_webapps",  WEBAPP_YELLOW);
        
        String submitterURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/submitter";
        JSONObject submitForm = new JSONObject();
        submitForm.put("label", "the label");
        submitForm.put("comment", "the comment");
        submitForm.put("all", true);
        sendRequest(new PostRequest(submitterURL, submitForm.toString(), "application/json"), Status.STATUS_OK);
        
        Thread.sleep(SUBMIT_DELAY);
    
        /**
         * Now we can set up our test data
         */
        createFile(webprojref, sandboxref, WEBAPP_ROOT, "/", "rootFile1");
        createFile(webprojref, sandboxref, WEBAPP_YELLOW, "/", "yellowFile1");
        
        /** 
         * Revert YELLOW - Should leave root alone
         */
        
        String reverterURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/reverter?webApp=" + WEBAPP_YELLOW;
        JSONObject revertForm = new JSONObject();
        revertForm.put("all", true);
        sendRequest(new PostRequest(reverterURL, revertForm.toString(), "application/json"), Status.STATUS_OK);
        
    	/**
    	 * Get the modified asset (yellow should have been reverted leaving root
    	 */
    	{
    	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
       	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
       	    JSONObject result = new JSONObject(list.getContentAsString());
    	    JSONArray lookupResult = result.getJSONArray(FIELD_DATA);
    	    
    	    assertTrue("testListUserSandbox", lookupResult.length() == 1);
    	    
    	    // Now check the contents..
    	    JSONObject x = lookupResult.getJSONObject(0);
    		String name = x.getString("name");
    		
    		assertNotNull("name is null", name);
    		assertEquals("name is wrong", "rootFile1", name);
    	}
    }  // End of testRevertAssetsWebAppTest
    
    public void testGetAsset() throws Exception
    {
    	final String YELLOW_FILE = "YellowFile.xyz";
    	
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
    	String webprojref = createWebProject();
    	createMembership(webprojref, USER_ONE, ROLE_CONTENT_MANAGER);
    	String sandboxref = createSandbox(webprojref, USER_ONE);
   	    
    	// Set up a file/folder to read
        createFolder(webprojref, sandboxref, "/www/avm_webapps",  WEBAPP_YELLOW);
        createFile(webprojref, sandboxref, WEBAPP_ROOT, "/", ROOT_FILE);
        createFolder(webprojref, sandboxref,  WEBAPP_ROOT, "/", "characters");
        createFile(webprojref, sandboxref, WEBAPP_ROOT, "/characters", "Buffy Ann Summers.jpg");
        createFile(webprojref, sandboxref, WEBAPP_ROOT, "/characters", "Willow Rosenberg.png");
        createFile(webprojref, sandboxref, WEBAPP_ROOT, "/characters", "Joyce Summers.jpg");
        createFile(webprojref, sandboxref, WEBAPP_ROOT, "/characters", "Cordelia Chase.jpg");
        createFolder(webprojref, sandboxref,  WEBAPP_ROOT, "/characters", "out");
        createFile(webprojref, sandboxref,  WEBAPP_ROOT, "/characters/out", "Giles");
        
        createFile(webprojref, sandboxref, WEBAPP_YELLOW, "/", YELLOW_FILE);
        
        /**
         * Positive test - read ROOT folder
         */
        {
        	String rootURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets" + "/www/avm_webapps/" + WEBAPP_ROOT ;
        	Response root = sendRequest(new GetRequest(rootURL), Status.STATUS_OK);
        	JSONObject result = new JSONObject(root.getContentAsString());
        	System.out.println(root.getContentAsString());
        	JSONObject rootDir = result.getJSONObject(FIELD_DATA); 
        	String name = rootDir.getString("name");
        	JSONArray children = rootDir.getJSONArray("children");
        	assertEquals("name is wrong", WEBAPP_ROOT, name);
        	assertEquals("too many children", children.length(), 2);
        }
        
        /**
         * Positive test - read yellowFile file absolute
         */
        {
        	String yellowURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets" + "/www/avm_webapps/" + WEBAPP_YELLOW + "/" + YELLOW_FILE ;
        	Response yellow = sendRequest(new GetRequest(yellowURL), Status.STATUS_OK);
        	JSONObject result = new JSONObject(yellow.getContentAsString());
        	JSONObject yellowFile = result.getJSONObject(FIELD_DATA);  
        	String name = yellowFile.getString("name");
        	long version = yellowFile.getLong("version");
        	long fileSize = yellowFile.getLong("fileSize");
        	assertEquals("name is wrong", YELLOW_FILE, name);
        }
        
        /**
         * Positive test - read yellowFile file relative to webApp
         */
        {
        	String yellowURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets" + "/" + YELLOW_FILE +"?webApp="+ WEBAPP_YELLOW;
        	Response yellow = sendRequest(new GetRequest(yellowURL), Status.STATUS_OK);
        	JSONObject result = new JSONObject(yellow.getContentAsString());
        	JSONObject yellowFile = result.getJSONObject(FIELD_DATA);  
        	String name = yellowFile.getString("name");
        	long version = yellowFile.getLong("version");
        	long fileSize = yellowFile.getLong("fileSize");
        	assertEquals("name is wrong", YELLOW_FILE, name);
        }
        
        /**
         * Negative test - read file that does not exist
         */
        {
        	String yellowURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets" + "/www/avm_webapps/" + WEBAPP_YELLOW + "/crap" ;
        	sendRequest(new GetRequest(yellowURL), Status.STATUS_NOT_FOUND);
 
        }
        
        /**
         * Negative test - missing web project 
         */
        {
         	String yellowURL = URL_WEB_PROJECT + "/" + "crap" + URI_SANDBOXES + "/" + sandboxref + "/assets" + "/www/avm_webapps/" + WEBAPP_YELLOW + "/" + YELLOW_FILE ;
        	sendRequest(new GetRequest(yellowURL), Status.STATUS_NOT_FOUND);
 
        }
        
        /**
         * Negative test - missing sandbox  
         */
        {
         	String yellowURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + "crap" + "/assets" + "/www/avm_webapps/" + WEBAPP_YELLOW + "/" + YELLOW_FILE ;
        	sendRequest(new GetRequest(yellowURL), Status.STATUS_NOT_FOUND);
 
        }
        
        
        /**
         * Positive test - read children
         */
        
        
    }
    
    public void testDeleteAsset() throws Exception
    {
    	final String YELLOW_FILE = "YellowFile.xyz";
    	final String YELLOW_FILE2 = "Buffy.jpg";
    	
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
    	String webprojref = createWebProject();
    	createMembership(webprojref, USER_ONE, ROLE_CONTENT_MANAGER);
    	String sandboxref = createSandbox(webprojref, USER_ONE);
   	
    	createFolder(webprojref, sandboxref, "/www/avm_webapps", WEBAPP_YELLOW);
        createFile(webprojref, sandboxref, WEBAPP_ROOT, "/", ROOT_FILE);
        createFile(webprojref, sandboxref, WEBAPP_YELLOW, "/", YELLOW_FILE);
        createFile(webprojref, sandboxref, WEBAPP_YELLOW, "/", YELLOW_FILE2);

        
        /**
         * Positive test 
         * 
         * Read ROOT folder
         * 
         * Delete ROOT folder
         * 
         * Fail to read root folder
         */
        {
        	String rootURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets" + "/www/avm_webapps/" + WEBAPP_ROOT ;
        	sendRequest(new GetRequest(rootURL), Status.STATUS_OK);
        	
        	sendRequest(new DeleteRequest(rootURL), Status.STATUS_OK);
        
        	sendRequest(new GetRequest(rootURL), Status.STATUS_NOT_FOUND);
        }
        
        /**
         * Positive test - delete yellowFile file with absolute path
         */
        {
        	String yellowURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets" + "/www/avm_webapps/" + WEBAPP_YELLOW + "/" + YELLOW_FILE ;
        	sendRequest(new GetRequest(yellowURL), Status.STATUS_OK);
        	
        	sendRequest(new DeleteRequest(yellowURL), Status.STATUS_OK);
        	
        	sendRequest(new GetRequest(yellowURL), Status.STATUS_NOT_FOUND);
        
        	/**
        	 * Part 2 Negative test - fail delete file that does not exist
        	 */
        	sendRequest(new DeleteRequest(yellowURL), Status.STATUS_NOT_FOUND);
 
        }
        
        /**
         * Positive test - delete yellowFile file with relative path
         */
        {
        	String yellowURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets" + "/" + YELLOW_FILE2 + "?webApp="+ WEBAPP_YELLOW ;
        	sendRequest(new GetRequest(yellowURL), Status.STATUS_OK);
        	
        	sendRequest(new DeleteRequest(yellowURL), Status.STATUS_OK);
        	
        	sendRequest(new GetRequest(yellowURL), Status.STATUS_NOT_FOUND);
        }
        
        /**
         * Negative test - delete missing web project 
         */
        {
         	String yellowURL = URL_WEB_PROJECT + "/" + "crap" + URI_SANDBOXES + "/" + sandboxref + "/assets" + "/www/avm_webapps/" + WEBAPP_YELLOW + "/" + YELLOW_FILE ;
        	sendRequest(new DeleteRequest(yellowURL), Status.STATUS_NOT_FOUND);
 
        }
        
        /**
         * Negative test - missing sandbox  
         */
        {
         	String yellowURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + "crap" + "/assets" + "/www/avm_webapps/" + WEBAPP_YELLOW + "/" + YELLOW_FILE ;
        	sendRequest(new DeleteRequest(yellowURL), Status.STATUS_NOT_FOUND);
 
        }
    }
    
    /**
     * Create Asset
     * @throws Exception
     */
    public void testCreateAsset() throws Exception
    {
    	final String YELLOW_FILE = "YellowFile.xyz";
    	
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
    	String webprojref = createWebProject();
    	createMembership(webprojref, USER_ONE, ROLE_CONTENT_MANAGER);
    	String sandboxref = createSandbox(webprojref, USER_ONE);
   	
        /**
         * Positive test - create a Yellow webapp with an absolute path
         */
        {
        	String rootURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets" + "/www/avm_webapps";
        	JSONObject submitForm = new JSONObject();
        	submitForm.put("name", WEBAPP_YELLOW);
        	submitForm.put("type", "folder");
        
        	Response response = sendRequest(new PostRequest(rootURL, submitForm.toString(), "application/json"), Status.STATUS_CREATED);
       	    JSONObject result = new JSONObject(response.getContentAsString());
    	    JSONObject lookupResult = result.getJSONObject(FIELD_DATA);
        	String name = lookupResult.getString("name");
        	boolean isFolder = lookupResult.getBoolean("isFolder");
        	boolean isFile = lookupResult.getBoolean("isFile");

        	assertEquals("name is wrong", WEBAPP_YELLOW, name);
        	assertTrue("folder not true", isFolder);
        	assertFalse("file not false", isFile);
        
        }
     
        /**
         * Positive test - create a file in the root webapp with a little content
         */
        {
        	String rootURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets" + "/www/avm_webapps/" + WEBAPP_ROOT;
        	JSONObject submitForm = new JSONObject();
        	submitForm.put("name", ROOT_FILE);
        	submitForm.put("type", "file");
        	submitForm.put(FIELD_CONTENT, "Hello World");
        	Response response = sendRequest(new PostRequest(rootURL, submitForm.toString(), "application/json"), Status.STATUS_CREATED);
            JSONObject result = new JSONObject(response.getContentAsString());
        	JSONObject lookupResult = result.getJSONObject(FIELD_DATA);
        	String name = lookupResult.getString("name");
        	long fileSize = lookupResult.getLong("fileSize");
        	assertEquals("name is wrong", ROOT_FILE, name);
        	boolean isFolder = lookupResult.getBoolean("isFolder");
        	boolean isFile = lookupResult.getBoolean("isFile");
        	assertTrue("file not true", isFile);
        	assertFalse("folder not false", isFolder);
        	assertTrue("file is empty", fileSize > 0);
        }
        
        /**
         * Positive test - create a file in the new Yellow webapp dir
         */
        {
        	String rootURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets" + "/www/avm_webapps/" + WEBAPP_YELLOW;
        	JSONObject submitForm = new JSONObject();
        	submitForm.put("name", YELLOW_FILE);
        	submitForm.put("type", "file");
        	sendRequest(new PostRequest(rootURL, submitForm.toString(), "application/json"), Status.STATUS_CREATED);
        }   
        
        /**
         * Positive test - create a file in the new Yellow webapp dir with a relative path
         */
        {
        	String rootURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets/" + "?webApp=" + WEBAPP_YELLOW;
        	JSONObject submitForm = new JSONObject();
        	submitForm.put("name", "willow.jpg");
        	submitForm.put("type", "file");
        	sendRequest(new PostRequest(rootURL, submitForm.toString(), "application/json"), Status.STATUS_CREATED);
        }   
        
        /**
         * Positive test - create a file in the new Yellow webapp dir with a relative path with some depth
         */
        {
        	createFolder(webprojref, sandboxref, WEBAPP_YELLOW, "/", "humans" );
        	String rootURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets/humans" + "?webApp=" + WEBAPP_YELLOW;
        	JSONObject submitForm = new JSONObject();
        	submitForm.put("name", "dawn.jpg");
        	submitForm.put("type", "file");
        	Response response = sendRequest(new PostRequest(rootURL, submitForm.toString(), "application/json"), Status.STATUS_CREATED);
        	JSONObject result = new JSONObject(response.getContentAsString());
         	JSONObject lookupResult = result.getJSONObject(FIELD_DATA);
         	String name = lookupResult.getString("name");
        	String path = lookupResult.getString("path");
         	assertEquals("name not correct", name, "dawn.jpg");
         	assertEquals("path not correct", path, "/www/avm_webapps/" + WEBAPP_YELLOW + "/humans/dawn.jpg");
         
        }   


     }
    
    /**
     * Test rename asset
     * @throws Exception
     */
    public void testRenameAsset() throws Exception
    {
    	final String YELLOW_FILE = "buffy.jpg";
    	final String PURPLE_FILE = "buffy.htm";
    	final String PURPLE_FILE2 = "willow.htm";
    	final String ROOT_MOVED_FILE = "smashing.htm";
    	
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
    	String webprojref = createWebProject();
    	createMembership(webprojref, USER_ONE, ROLE_CONTENT_MANAGER);
    	String sandboxref = createSandbox(webprojref, USER_ONE);

        /**
         * Positive test - create a Yellow webapp with some content, rename it to green
         */
        {
        	createFolder(webprojref, sandboxref, "/www/avm_webapps", WEBAPP_YELLOW );
        	createFile(webprojref, sandboxref, WEBAPP_YELLOW, "/", YELLOW_FILE);
        	
          	String yellowURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets" + "/www/avm_webapps/" + WEBAPP_YELLOW;
        	JSONObject submitForm = new JSONObject();
        	submitForm.put("name", WEBAPP_GREEN);
        	Response response = sendRequest(new PutRequest(yellowURL, submitForm.toString(), "application/json"), Status.STATUS_OK);
        	JSONObject result = new JSONObject(response.getContentAsString());
    	    JSONObject lookupResult = result.getJSONObject(FIELD_DATA);
        	String name = lookupResult.getString("name");
        	assertEquals("name is wrong", WEBAPP_GREEN, name);
        }
        
       /**
         * rename a file - absolute
         */
    	{
    		createFile(webprojref, sandboxref, WEBAPP_ROOT, "/", ROOT_FILE);
      	
        	String yellowURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets" + "/www/avm_webapps/" + WEBAPP_ROOT + "/" + ROOT_FILE;
        	JSONObject submitForm = new JSONObject();
        	submitForm.put("name", ROOT_MOVED_FILE);
        	Response response = sendRequest(new PutRequest(yellowURL, submitForm.toString(), "application/json"), Status.STATUS_OK);
        	JSONObject result = new JSONObject(response.getContentAsString());
        	JSONObject lookupResult = result.getJSONObject(FIELD_DATA);
        	String name = lookupResult.getString("name");
        	assertEquals("name is wrong", ROOT_MOVED_FILE, name);
        
        	/**
        	 * Part 2 Negative test - rename a file that should no longer exist
        	 */
        	sendRequest(new PutRequest(yellowURL, submitForm.toString(), "application/json"), Status.STATUS_NOT_FOUND);
        }
    	
        /**
         * rename a file - relative
         */
    	{
    		createFile(webprojref, sandboxref, WEBAPP_ROOT, "/", PURPLE_FILE);
      	
        	String purpleURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets/" + PURPLE_FILE +"?webApp="+WEBAPP_ROOT;
        	JSONObject submitForm = new JSONObject();
        	submitForm.put("name", PURPLE_FILE2);
        	Response response = sendRequest(new PutRequest(purpleURL, submitForm.toString(), "application/json"), Status.STATUS_OK);
        	JSONObject result = new JSONObject(response.getContentAsString());
        	JSONObject lookupResult = result.getJSONObject(FIELD_DATA);
        	String name = lookupResult.getString("name");
        	assertEquals("name is wrong", PURPLE_FILE2, name);
    	}

    	
        /**
         * Negative test - missing sandbox
         */
    	{
         	String yellowURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + "crap" + "/assets" + "/www/avm_webapps/" + WEBAPP_ROOT + "/" + ROOT_MOVED_FILE;
        	JSONObject submitForm = new JSONObject();
        	submitForm.put("name", ROOT_MOVED_FILE);
        	sendRequest(new PutRequest(yellowURL, submitForm.toString(), "application/json"), Status.STATUS_NOT_FOUND);
    	}
    }
    
    /**
     * Test rename asset
     * @throws Exception
     */
    public void testMoveAsset() throws Exception
    {
    	final String YELLOW_FILE = "buffy.jpg";
    	
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
    	String webprojref = createWebProject();
    	createMembership(webprojref, USER_ONE, ROLE_CONTENT_MANAGER);
    	String sandboxref = createSandbox(webprojref, USER_ONE);
     	
        /**
         * move a file
         */
    	{
    		createFile(webprojref, sandboxref, WEBAPP_ROOT, "/", ROOT_FILE);
    		createFolder(webprojref, sandboxref, WEBAPP_ROOT, "/", "actors");
    		createFolder(webprojref, sandboxref, WEBAPP_ROOT, "/actors", "humans");
      	
        	String myURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets" + "/www/avm_webapps/" + WEBAPP_ROOT + "/" + ROOT_FILE;
        	JSONObject submitForm = new JSONObject();
        	submitForm.put("path", "/www/avm_webapps/ROOT/actors/humans");
        	Response response = sendRequest(new PutRequest(myURL, submitForm.toString(), "application/json"), Status.STATUS_OK);
        	System.out.println(response.getContentAsString());
        	JSONObject result = new JSONObject(response.getContentAsString());
        	JSONObject lookupResult = result.getJSONObject(FIELD_DATA);
        	String name = lookupResult.getString("name");
        	assertEquals("name is wrong", ROOT_FILE, name);
        
        	/**
        	 * Part 2 Negative test - rename a file that should no longer exist
        	 */
        	sendRequest(new PutRequest(myURL, submitForm.toString(), "application/json"), Status.STATUS_NOT_FOUND);
        }	
    }

    /**
     * Tests updating properties of an Asset
     * 
     * @throws Exception
     */
    public void testUpdateAssetProperties() throws Exception
    {
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        String webprojref = createWebProject();
        createMembership(webprojref, USER_ONE, ROLE_CONTENT_MANAGER);
        String sandboxref = createSandbox(webprojref, AuthenticationUtil.getAdminUserName());

        // Update properties for the File object
        createFile(webprojref, sandboxref, WEBAPP_ROOT, "/", ROOT_FILE);
        updatePropertiesAndAssert(webprojref, sandboxref, ROOT_FILE, ("Renamed_" + ROOT_FILE));

        // Update properties for the Folder object
        createFolder(webprojref, sandboxref, WEBAPP_ROOT, "/", WEBAPP_GREEN);
        updatePropertiesAndAssert(webprojref, sandboxref, WEBAPP_GREEN, ("Renamed_" + WEBAPP_GREEN));
    }

    private void updatePropertiesAndAssert(String webprojref, String sandboxref, String oldName, String updatedName) throws JSONException, IOException,
            UnsupportedEncodingException
    {
        String propertiesUrl = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets/properties/www/avm_webapps/" + WEBAPP_ROOT + "/" + oldName;
        JSONObject properties = new JSONObject();
        properties.put(ContentModel.PROP_TITLE.getLocalName(), updatedName);
        properties.put(ContentModel.PROP_NAME.getLocalName(), updatedName);
        JSONObject submitForm = new JSONObject();
        submitForm.put(FIELD_PROPERTIES, properties);
        Response response = sendRequest(new PostRequest(propertiesUrl, submitForm.toString(), "application/json"), Status.STATUS_ACCEPTED);
        assertUpdatedProperties(updatedName, response);
        // Check whether the updated File is allowable
        String assetRequestUrl = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets/www/avm_webapps/" + WEBAPP_ROOT + "/" + updatedName;
        response = sendRequest(new GetRequest(assetRequestUrl), Status.STATUS_OK);
        assertUpdatedProperties(updatedName, response);
        // Negative test
        assetRequestUrl = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets/www/avm_webapps" + WEBAPP_ROOT + "/" + oldName;
        response = sendRequest(new GetRequest(assetRequestUrl), Status.STATUS_NOT_FOUND);
    }

    private void assertUpdatedProperties(String updatedName, Response response) throws JSONException, UnsupportedEncodingException
    {
        JSONObject result = new JSONObject(response.getContentAsString());
        JSONObject properties = (JSONObject) result.get(FIELD_DATA);
        properties = (JSONObject) properties.get(FIELD_PROPERTIES);
        assertEquals(updatedName, properties.get(PROP_TITLE));
        assertEquals(updatedName, properties.get(PROP_NAME));
    }

    /**
     * Tests updating of Content of an File Asset
     * 
     * @throws Exception
     */
    public void testGetAndUpdateAssetContent() throws Exception
    {
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        String webprojref = createWebProject();
        createMembership(webprojref, USER_ONE, ROLE_CONTENT_MANAGER);
        String sandboxref = createSandbox(webprojref, USER_ONE);

        createFile(webprojref, sandboxref, WEBAPP_ROOT, "/", ROOT_FILE);
        String contentRequestUrl = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets/content/www/avm_webapps/" + WEBAPP_ROOT + "/" + ROOT_FILE;
        sendRequest(new GetRequest(contentRequestUrl), Status.STATUS_INTERNAL_SERVER_ERROR);

        String propertiesRequestUrl = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets/properties/www/avm_webapps/" + WEBAPP_ROOT + "/" + ROOT_FILE;
        JSONObject contentEntry = new JSONObject();
        String content = TEST_CONTENT_ENTRY;
        contentEntry.put(FIELD_CONTENT, content);
        sendRequest(new PostRequest(propertiesRequestUrl, contentEntry.toString(), "application/json"), Status.STATUS_ACCEPTED);

        Response response = sendRequest(new GetRequest(contentRequestUrl), Status.STATUS_OK);
        contentEntry = new JSONObject(response.getContentAsString());
        assertEquals(content, contentEntry.get(FIELD_CONTENT));
    }

    /**
     * Utility method to create a folder
     * @param webprojref
     * @param sandboxref
     * @param parent
     * @param name
     * @throws Exception
     */
    
    private void createFolder(String webprojref, String sandboxref, String parent, String name) throws Exception
    {
    	String rootURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets" + parent;
    	JSONObject submitForm = new JSONObject();
    	submitForm.put("name", name);
    	submitForm.put("type", "folder");
    	sendRequest(new PostRequest(rootURL, submitForm.toString(), "application/json"), Status.STATUS_CREATED);
    }
    
    /**
     * Utility method to create a folder in a web app
     * @param webprojref
     * @param sandboxref
     * @param parent
     * @param name
     * @throws Exception
     */
    private void createFolder(String webprojref, String sandboxref, String webApp, String parent, String name) throws Exception
    {
        String rootURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets" + parent + "?webApp="+webApp;
    	JSONObject submitForm = new JSONObject();
    	submitForm.put("name", name);
    	submitForm.put("type", "folder");
    	sendRequest(new PostRequest(rootURL, submitForm.toString(), "application/json"), Status.STATUS_CREATED);
    }
    
    /**
     * Utility method to create a file
     * @param webprojref
     * @param sandboxref
     * @param parent
     * @param name

     */
    private void createFile(String webprojref, String sandboxref, String parent, String name) throws Exception
    {
    	String rootURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets" + parent;
    	JSONObject submitForm = new JSONObject();
    	submitForm.put("name", name);
    	submitForm.put("type", "file");
    	sendRequest(new PostRequest(rootURL, submitForm.toString(), "application/json"), Status.STATUS_CREATED);    
    }
    /**
     * Utility method to create a file in a web app
     * @param webprojref
     * @param sandboxref
     * @param parent
     * @param name
     */
    
    private void createFile(String webprojref, String sandboxref, String webApp, String parent, String name) throws Exception
    {
    	String rootURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets" + parent+ "?webApp="+webApp;
       	JSONObject submitForm = new JSONObject();
    	submitForm.put("name", name);
    	submitForm.put("type", "file");
    	sendRequest(new PostRequest(rootURL, submitForm.toString(), "application/json"), Status.STATUS_CREATED);    
    }
    
    private void deleteFile(String webprojref, String sandboxref, String path) throws Exception
    {
       String deleteURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/assets" + path ;
       sendRequest(new DeleteRequest(deleteURL), Status.STATUS_OK); 	
    }
} // End of AssetTest
