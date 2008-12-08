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

package org.alfresco.repo.web.scripts.wcm.sandbox;


import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.PropertyMap;
import org.alfresco.web.scripts.Status;
import org.alfresco.web.scripts.TestWebScriptServer.DeleteRequest;
import org.alfresco.web.scripts.TestWebScriptServer.GetRequest;
import org.alfresco.web.scripts.TestWebScriptServer.PostRequest;
import org.alfresco.web.scripts.TestWebScriptServer.PutRequest;
import org.alfresco.web.scripts.TestWebScriptServer.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Junit tests of the REST bindings for WCM Sandbox and WCM Sandboxes
 * @author mrogers
 *
 */
public class AssetTest  extends BaseWebScriptTest {
	
    private AuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    
    // TODO - Replace the use of these two services as and when the REST API is available
    private AVMService avmNonLockingAwareService;
    private AVMService avmLockingAwareService;
    private char AVM_STORE_SEPARATOR = ':';
    
    private static final String USER_ONE = "WebProjectTestOne";
    private static final String USER_TWO = "WebProjectTestTwo";
    private static final String USER_THREE = "WebProjectTestThree";
    private static final String USER_FOUR = "WebProjectTestFour";
    private static final String USER_ADMIN = "admin";
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
	
	    
    private List<String> createdWebProjects = new ArrayList<String>(5);
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.authenticationService = (AuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        
        // TODO - Replace the use of these two services as and when the REST API is available
        this.avmNonLockingAwareService = (AVMService)getServer().getApplicationContext().getBean("AVMService");
        this.avmLockingAwareService = (AVMService)getServer().getApplicationContext().getBean("AVMLockingAwareService");
        
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
        JSONObject data = result.getJSONObject("data");
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
    		JSONObject data = result.getJSONObject("data");
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
    	JSONArray lookupResult = result.getJSONArray("data");    
        assertTrue("sandbox is not empty", lookupResult.length() == 0); 
	}
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        this.authenticationComponent.setCurrentUser("admin");
        
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
        this.authenticationComponent.setCurrentUser("admin");
    	String webprojref = createWebProject();
    	createMembership(webprojref, USER_ONE, ROLE_CONTENT_MANAGER);
    	String sandboxref = createSandbox(webprojref, USER_ONE);
   	
    	//TODO REPLACE THIS IMPLEMENTATION WITH THE REST API ONCE AVAILABLE
        String bodgeRootPath = sandboxref + AVM_STORE_SEPARATOR + "/www/avm_webapps/" + WEBAPP_ROOT;
        String bodgeYellowPath = sandboxref + AVM_STORE_SEPARATOR + "/www/avm_webapps/" + WEBAPP_YELLOW;
        
        avmLockingAwareService.createDirectory(sandboxref + AVM_STORE_SEPARATOR + "/www/avm_webapps", WEBAPP_YELLOW);
        String submitterURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/submitter";
        JSONObject submitForm = new JSONObject();
        submitForm.put("label", "the label");
        submitForm.put("comment", "the comment");
        submitForm.put("all", true);
        Response response = sendRequest(new PostRequest(submitterURL, submitForm.toString(), "application/json"), Status.STATUS_OK);
        
        
        avmLockingAwareService.createFile(bodgeRootPath, "rootFile1");
        avmLockingAwareService.createFile(bodgeYellowPath, "yellowFile1");
        
    	/**
    	 * Get the modified asset and verify its format
    	 */
    	{
    	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified?webApp=" + WEBAPP_YELLOW;
       	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
       	    JSONObject result = new JSONObject(list.getContentAsString());
    	    JSONArray lookupResult = result.getJSONArray("data");
    	    
    	    assertTrue("testListUserSandbox", lookupResult.length() == 1);
    	    
    	    // Now check the contents..
    	    JSONObject x = lookupResult.getJSONObject(0);
    		String name = x.getString("name");
    		String path = x.getString("path");
    		String creator = x.getString("creator");
    		boolean isFile = x.getBoolean("isFile");
    		boolean isDeleted = x.getBoolean("isDeleted");
    		boolean isDirectory = x.getBoolean("isDirectory");
    		
    		assertNotNull("name is null", name);
    		assertEquals("name is wrong", "yellowFile1", name);
    		assertEquals("creator is wrong", "admin", creator);
    		assertTrue("not isFile", isFile);
    		assertFalse("not isDirectory", isDirectory);
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
        this.authenticationComponent.setCurrentUser("admin");
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
    	    JSONArray lookupResult = result.getJSONArray("data");
    	    
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
    	//TODO REPLACE THIS IMPLEMENTATION WITH THE REST API ONCE AVAILABLE
        String bodgePath = sandboxref + AVM_STORE_SEPARATOR + "/www/avm_webapps/ROOT";
        avmLockingAwareService.createFile(bodgePath, "myFile1");
 
    	/**
    	 * Get the modified asset and verify its format
    	 */
    	{
    	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
       	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
       	    JSONObject result = new JSONObject(list.getContentAsString());
    	    JSONArray lookupResult = result.getJSONArray("data");
    	    
    	    assertTrue("testListUserSandbox", lookupResult.length() == 1);
    	    
    	    // Now check the contents..
    	    JSONObject x = lookupResult.getJSONObject(0);
    		String name = x.getString("name");
    		String path = x.getString("path");
    		String creator = x.getString("creator");
    		boolean isFile = x.getBoolean("isFile");
    		boolean isDeleted = x.getBoolean("isDeleted");
    		boolean isDirectory = x.getBoolean("isDirectory");
    		
    		assertNotNull("name is null", name);
    		assertEquals("name is wrong", "myFile1", name);
    		assertEquals("creator is wrong", "admin", creator);
    		assertTrue("not isFile", isFile);
    		assertFalse("not isDirectory", isDirectory);
    		assertFalse("not isDeleted", isDeleted);
    		
    		assertNotNull("path is null", path);
    		assertEquals("path of MyFile1 is not correct", path, "/www/avm_webapps/ROOT/myFile1");
    		
    	}
    	
    	/**
    	 * Add some more assets, a dir and a file
    	 */
        avmLockingAwareService.createDirectory(bodgePath, "fileA");
    		
    	/**
    	 * Get the modified assets - should be 2 (filex) is in a new dir
    	 */
    	
    	/**
    	 * Add a new dir containing assets
    	 */
        avmLockingAwareService.createDirectory(bodgePath, "dir1");
        avmLockingAwareService.createFile(bodgePath + "/dir1", "filex");
        avmLockingAwareService.createFile(bodgePath + "/dir1", "filey");
    	
    	/**
    	 * Get the modified assets should be myFile1, fileA, dir1 
    	 */
    	{
    	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
       	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
       	    JSONObject result = new JSONObject(list.getContentAsString());
    	    JSONArray lookupResult = result.getJSONArray("data");
    	    
    	    assertTrue("testListUserSandbox", lookupResult.length() == 3);
    	}    	
    }
    
    /**
     * test the modified assets methods
     * @throws Exception
     */
    public void testSubmitAssetsTest() throws Exception
    { 
        this.authenticationComponent.setCurrentUser("admin");
    	String webprojref = createWebProject();
    	createMembership(webprojref, USER_ONE, ROLE_CONTENT_MANAGER);
    	String sandboxref = createSandbox(webprojref, USER_ONE);
    	
    	//TODO REPLACE THIS IMPLEMENTATION WITH THE REST API ONCE AVAILABLE
        String bodgePath = sandboxref + AVM_STORE_SEPARATOR + "/www/avm_webapps/ROOT";
        avmLockingAwareService.createFile(bodgePath, "myFile1");
        
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
            avmLockingAwareService.createFile(bodgePath, "myFile2");
            
        	{
        	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
           	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
           	    JSONObject result = new JSONObject(list.getContentAsString());
        	    JSONArray lookupResult = result.getJSONArray("data");
        	    
        	    assertTrue("testListUserSandbox", lookupResult.length() > 0);
        	} 
            
            JSONObject submitForm = new JSONObject();
            submitForm.put("label", "the label");
            submitForm.put("comment", "the comment");
            submitForm.put("all", true);
            Response response = sendRequest(new PostRequest(submitterURL, submitForm.toString(), "application/json"), Status.STATUS_OK);
            //TODO Nothing in the response now.
            
        	checkSandboxEmpty(webprojref, sandboxref);

        }
        
        /**
         * Submit paths
         */
        {
        	avmLockingAwareService.createFile(bodgePath, "myFile3");
            JSONObject submitForm = new JSONObject();
            submitForm.put("label", "the label");
            submitForm.put("comment", "the comment");
            
            JSONArray paths = new JSONArray();
            paths.put("/www/avm_webapps/ROOT/myFile3");
            submitForm.put("paths", paths);
            Response response = sendRequest(new PostRequest(submitterURL, submitForm.toString(), "application/json"), Status.STATUS_OK); 
         	checkSandboxEmpty(webprojref, sandboxref);
        }
        
        /**
         * Submit assets - get a list of modified assets and submit them back
         */
        {
            avmLockingAwareService.createFile(bodgePath, "myFile4");
            
        	{
        	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
           	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
           	    JSONObject result = new JSONObject(list.getContentAsString());
        	    JSONArray lookupResult = result.getJSONArray("data");
        	    
        	    assertTrue("testListUserSandbox", lookupResult.length() > 0);
        	    
                JSONObject submitForm = new JSONObject();
                submitForm.put("label", "the label");
                submitForm.put("comment", "the comment");
                submitForm.put("assets", lookupResult);
                Response response = sendRequest(new PostRequest(submitterURL, submitForm.toString(), "application/json"), Status.STATUS_OK); 

        	}
        	checkSandboxEmpty(webprojref, sandboxref);
        	    	
        	
         }
        
        /**
         * Submit assets more complex example - get a list of modified assets and submit them back
         * Also has a delete to process
         */
        {
            avmLockingAwareService.removeNode(sandboxref + AVM_STORE_SEPARATOR + "/www/avm_webapps/ROOT/myFile3");
            avmLockingAwareService.createFile(bodgePath, "buffy.jpg");
            avmLockingAwareService.createDirectory(bodgePath, "vampires");
            avmLockingAwareService.createFile(bodgePath + "/vampires", "master");
            avmLockingAwareService.createFile(bodgePath + "/vampires", "drusilla");
            avmLockingAwareService.createDirectory(bodgePath, "humans");
            avmLockingAwareService.createFile(bodgePath + "/humans", "willow");
            avmLockingAwareService.createFile(bodgePath + "/humans", "xander");
            
        	{
        	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
           	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
           	    JSONObject result = new JSONObject(list.getContentAsString());
        	    JSONArray lookupResult = result.getJSONArray("data");
        	    
        	    assertTrue("testListUserSandbox", lookupResult.length() > 0);
        	    
                JSONObject submitForm = new JSONObject();
                submitForm.put("label", "the label");
                submitForm.put("comment", "the comment");
                submitForm.put("assets", lookupResult);
                Response response = sendRequest(new PostRequest(submitterURL, submitForm.toString(), "application/json"), Status.STATUS_OK); 

        	}
        	checkSandboxEmpty(webprojref, sandboxref);
        	    	    	
         }
        

        /**
         * Now finally, a big complicated submission of assets and paths.
         */
        {
        	// single file in existing dir
            avmLockingAwareService.createFile(bodgePath + "/vampires", "angel");
            
            //delete from an existing dir
            avmLockingAwareService.removeNode(sandboxref + AVM_STORE_SEPARATOR + "/www/avm_webapps/ROOT/vampires/drusilla");
            // multiple file in existing dir
            avmLockingAwareService.createFile(bodgePath + "/humans", "giles");
            avmLockingAwareService.createFile(bodgePath + "/humans", "dawn");
            avmLockingAwareService.createFile(bodgePath + "/humans", "anya");
            // new directory
            avmLockingAwareService.createDirectory(bodgePath, "cast");
            avmLockingAwareService.createFile(bodgePath + "/cast", "Anthony Head");
            avmLockingAwareService.createFile(bodgePath + "/cast", "James Marsters");

            
        	{
        	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
           	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
           	    JSONObject result = new JSONObject(list.getContentAsString());
        	    JSONArray lookupResult = result.getJSONArray("data");
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
                
           	    Response listTwo = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
           	    JSONObject resultTwo = new JSONObject(listTwo.getContentAsString());
        	    JSONArray lookupResultTwo = resultTwo.getJSONArray("data");
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
        	checkSandboxEmpty(webprojref, sandboxref);
        	    	    	
         }
    }
    
    /**
     * Test the submit assets (Web App) methods
     * @throws Exception
     */
    public void testSubmitAssetsWebAppTest() throws Exception
    {
        this.authenticationComponent.setCurrentUser("admin");
    	String webprojref = createWebProject();
    	createMembership(webprojref, USER_ONE, ROLE_CONTENT_MANAGER);
    	String sandboxref = createSandbox(webprojref, USER_ONE);
   	
    	//TODO REPLACE THIS IMPLEMENTATION WITH THE REST API ONCE AVAILABLE
        String bodgeRootPath = sandboxref + AVM_STORE_SEPARATOR + "/www/avm_webapps/" + WEBAPP_ROOT;
        String bodgeYellowPath = sandboxref + AVM_STORE_SEPARATOR + "/www/avm_webapps/" + WEBAPP_YELLOW;  
        avmLockingAwareService.createDirectory(sandboxref + AVM_STORE_SEPARATOR + "/www/avm_webapps", WEBAPP_YELLOW);
        
        String submitterURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/submitter";
        JSONObject submitForm = new JSONObject();
        submitForm.put("label", "the label");
        submitForm.put("comment", "the comment");
        submitForm.put("all", true);
        sendRequest(new PostRequest(submitterURL, submitForm.toString(), "application/json"), Status.STATUS_OK);
    
        /**
         * Now we can set up our test data
         */
        avmLockingAwareService.createFile(bodgeRootPath, "rootFile1");
        avmLockingAwareService.createFile(bodgeYellowPath, "yellowFile1");
        
        /** 
         * Submit YELLOW - Should leave root alone
         */       
        submitForm.put("label", "yellow submit");
        submitForm.put("comment", "yellow submit");
        submitForm.put("all", true);
        sendRequest(new PostRequest(submitterURL + "?webApp=" + WEBAPP_YELLOW, submitForm.toString(), "application/json"), Status.STATUS_OK);
        
    	/**
    	 * Get the modified asset (yellow should have been submitted leaving root
    	 */
    	{
    	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
       	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
       	    JSONObject result = new JSONObject(list.getContentAsString());
    	    JSONArray lookupResult = result.getJSONArray("data");
    	    
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
        this.authenticationComponent.setCurrentUser("admin");
    	String webprojref = createWebProject();
    	createMembership(webprojref, USER_ONE, ROLE_CONTENT_MANAGER);
    	String sandboxref = createSandbox(webprojref, USER_ONE);
    	
    	//TODO REPLACE THIS IMPLEMENTATION WITH THE REST API ONCE AVAILABLE
        String bodgePath = sandboxref + AVM_STORE_SEPARATOR + "/www/avm_webapps/ROOT";
        avmLockingAwareService.createFile(bodgePath, "myFile1");
        
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
            avmLockingAwareService.createFile(bodgePath, "myFile2");
            
        	{
        	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
           	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
           	    JSONObject result = new JSONObject(list.getContentAsString());
        	    JSONArray lookupResult = result.getJSONArray("data");
        	    
        	    assertTrue("testListUserSandbox", lookupResult.length() > 0);
        	} 
            
            JSONObject submitForm = new JSONObject();
            submitForm.put("all", true);
            Response response = sendRequest(new PostRequest(reverterURL, submitForm.toString(), "application/json"), Status.STATUS_OK);
            //TODO Nothing in the response now.
            
        	checkSandboxEmpty(webprojref, sandboxref);

        }
        
        /**
         * Revert via paths
         */
        {
        	avmLockingAwareService.createFile(bodgePath, "myFile3");
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
            avmLockingAwareService.createFile(bodgePath, "myFile4");
            
        	{
        	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
           	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
           	    JSONObject result = new JSONObject(list.getContentAsString());
        	    JSONArray lookupResult = result.getJSONArray("data");
        	    
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
            avmLockingAwareService.createFile(bodgePath, "buffy.jpg");
            avmLockingAwareService.createDirectory(bodgePath, "vampires");
            avmLockingAwareService.createFile(bodgePath + "/vampires", "master");
            avmLockingAwareService.createFile(bodgePath + "/vampires", "drusilla");
            avmLockingAwareService.createDirectory(bodgePath, "humans");
            avmLockingAwareService.createFile(bodgePath + "/humans", "willow");
            avmLockingAwareService.createFile(bodgePath + "/humans", "xander");
            
        	{
        	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
           	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
           	    JSONObject result = new JSONObject(list.getContentAsString());
        	    JSONArray lookupResult = result.getJSONArray("data");
        	    
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
            avmLockingAwareService.createFile(bodgePath, "buffy.jpg");
            avmLockingAwareService.createDirectory(bodgePath, "vampires");
            avmLockingAwareService.createFile(bodgePath + "/vampires", "master");
            avmLockingAwareService.createFile(bodgePath + "/vampires", "drusilla");
            avmLockingAwareService.createDirectory(bodgePath, "humans");
            avmLockingAwareService.createFile(bodgePath + "/humans", "willow");
            avmLockingAwareService.createFile(bodgePath + "/humans", "xander");

            JSONObject submitForm = new JSONObject();
            submitForm.put("label", "the label");
            submitForm.put("comment", "the comment");
            submitForm.put("all", true);
            String submitterURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/submitter";
            sendRequest(new PostRequest(submitterURL, submitForm.toString(), "application/json"), Status.STATUS_OK);
            
            // Now we can set up the data that will get reverted
            
        	// single file in existing dir
            avmLockingAwareService.createFile(bodgePath + "/vampires", "angel");
            
            //delete from an existing dir
            avmLockingAwareService.removeNode(sandboxref + AVM_STORE_SEPARATOR + "/www/avm_webapps/ROOT/vampires/drusilla");
            // multiple file in existing dir
            avmLockingAwareService.createFile(bodgePath + "/humans", "giles");
            avmLockingAwareService.createFile(bodgePath + "/humans", "dawn");
            avmLockingAwareService.createFile(bodgePath + "/humans", "anya");
            // new directory
            avmLockingAwareService.createDirectory(bodgePath, "cast");
            avmLockingAwareService.createFile(bodgePath + "/cast", "Anthony Head");
            avmLockingAwareService.createFile(bodgePath + "/cast", "James Marsters");

            
        	{
        	    String sandboxesURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/modified";
           	    Response list = sendRequest(new GetRequest(sandboxesURL), Status.STATUS_OK);
           	    JSONObject result = new JSONObject(list.getContentAsString());
        	    JSONArray lookupResult = result.getJSONArray("data");
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
        	    JSONArray lookupResultTwo = resultTwo.getJSONArray("data");
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
        this.authenticationComponent.setCurrentUser("admin");
    	String webprojref = createWebProject();
    	createMembership(webprojref, USER_ONE, ROLE_CONTENT_MANAGER);
    	String sandboxref = createSandbox(webprojref, USER_ONE);
   	
    	//TODO REPLACE THIS IMPLEMENTATION WITH THE REST API ONCE AVAILABLE
        String bodgeRootPath = sandboxref + AVM_STORE_SEPARATOR + "/www/avm_webapps/" + WEBAPP_ROOT;
        String bodgeYellowPath = sandboxref + AVM_STORE_SEPARATOR + "/www/avm_webapps/" + WEBAPP_YELLOW;  
        avmLockingAwareService.createDirectory(sandboxref + AVM_STORE_SEPARATOR + "/www/avm_webapps", WEBAPP_YELLOW);
        
        String submitterURL = URL_WEB_PROJECT + "/" + webprojref + URI_SANDBOXES + "/" + sandboxref + "/submitter";
        JSONObject submitForm = new JSONObject();
        submitForm.put("label", "the label");
        submitForm.put("comment", "the comment");
        submitForm.put("all", true);
        sendRequest(new PostRequest(submitterURL, submitForm.toString(), "application/json"), Status.STATUS_OK);
    
        /**
         * Now we can set up our test data
         */
        avmLockingAwareService.createFile(bodgeRootPath, "rootFile1");
        avmLockingAwareService.createFile(bodgeYellowPath, "yellowFile1");
        
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
    	    JSONArray lookupResult = result.getJSONArray("data");
    	    
    	    assertTrue("testListUserSandbox", lookupResult.length() == 1);
    	    
    	    // Now check the contents..
    	    JSONObject x = lookupResult.getJSONObject(0);
    		String name = x.getString("name");
    		
    		assertNotNull("name is null", name);
    		assertEquals("name is wrong", "rootFile1", name);
    	}
    }  // End of testRevertAssetsWebAppTest
} // End of AssetTest
