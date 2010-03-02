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
package org.alfresco.repo.web.scripts.discussion;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.PropertyMap;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

/**
 * Unit Test to test Discussions Web Script API
 * 
 * @author mruflin
 */
public class DiscussionServiceTest extends BaseWebScriptTest
{
	private static Log logger = LogFactory.getLog(DiscussionServiceTest.class);
	
    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    private SiteService siteService;
    
    private static final String USER_ONE = "UserOneThird";
    private static final String USER_TWO = "UserTwoThird";
    private static final String SITE_SHORT_NAME_DISCUSSION = "DiscussionSiteShortNameThree";
    private static final String COMPONENT_DISCUSSION = "discussion";

    private static final String URL_FORUM_POST = "/api/forum/post/site/" + SITE_SHORT_NAME_DISCUSSION + "/" + COMPONENT_DISCUSSION + "/";
    private static final String URL_FORUM_POSTS = "/api/forum/site/" + SITE_SHORT_NAME_DISCUSSION + "/" + COMPONENT_DISCUSSION + "/posts";
    
    private List<String> posts = new ArrayList<String>(5);

    
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
        SiteInfo siteInfo = this.siteService.getSite(SITE_SHORT_NAME_DISCUSSION);
        if (siteInfo == null)
        {
            this.siteService.createSite("DiscussionSitePreset", SITE_SHORT_NAME_DISCUSSION, "DiscussionSiteTitle",
                "DiscussionSiteDescription", true);
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
        
        // delete the inviter user
        //personService.deletePerson(USER_ONE);
        //this.authenticationService.deleteAuthentication(USER_ONE);
        //personService.deletePerson(USER_TWO);
        //this.authenticationService.deleteAuthentication(USER_TWO);
        
        // delete invite site
        siteService.deleteSite(SITE_SHORT_NAME_DISCUSSION);
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
        this.siteService.setMembership(SITE_SHORT_NAME_DISCUSSION, userName, role);
    }
    
    
    // Test helper methods 
    
    private JSONObject createPost(String title, String content, int expectedStatus)
    throws Exception
    {
        JSONObject post = new JSONObject();
        post.put("title", title);
        post.put("content", content);
	    Response response = sendRequest(new PostRequest(URL_FORUM_POSTS, post.toString(), "application/json"), expectedStatus);
	    
	    if (expectedStatus != 200)
	    {
	    	return null;
	    }
	    
    	//logger.debug(response.getContentAsString());
    	JSONObject result = new JSONObject(response.getContentAsString());
    	JSONObject item = result.getJSONObject("item");
    	posts.add(item.getString("name"));
    	return item;
    }
    
    private JSONObject updatePost(String nodeRef, String title, String content, int expectedStatus)
    throws Exception
    {
        JSONObject post = new JSONObject();
        post.put("title", title);
        post.put("content", content);
	    Response response = sendRequest(new PutRequest(getPostUrl(nodeRef), post.toString(), "application/json"), expectedStatus);
	    
	    if (expectedStatus != 200)
	    {
	    	return null;
	    }

    	JSONObject result = new JSONObject(response.getContentAsString());
    	return result.getJSONObject("item");
    }
    
    private JSONObject getPost(String name, int expectedStatus)
    throws Exception
    {
    	Response response = sendRequest(new GetRequest(URL_FORUM_POST + name), expectedStatus);
    	if (expectedStatus == 200)
    	{
    		JSONObject result = new JSONObject(response.getContentAsString());
    		return result.getJSONObject("item");
    	}
    	else
    	{
    		return null;
    	}
    }
    
    private String getRepliesUrl(String nodeRef)
    {
    	return "/api/forum/post/node/" + nodeRef.replace("://", "/") + "/replies";
    }
    
    private String getPostUrl(String nodeRef)
    {
    	return "/api/forum/post/node/" + nodeRef.replace("://", "/");
    }
    
    private JSONObject createReply(String nodeRef, String title, String content, int expectedStatus)
    throws Exception
    {
        JSONObject reply = new JSONObject();
        reply.put("title", title);
        reply.put("content", content);
	    Response response = sendRequest(new PostRequest(getRepliesUrl(nodeRef), reply.toString(), "application/json"), expectedStatus);
	    
	    if (expectedStatus != 200)
	    {
	    	return null;
	    }
	    
    	JSONObject result = new JSONObject(response.getContentAsString());
    	return result.getJSONObject("item");
    }
    
    private JSONObject updateComment(String nodeRef, String title, String content, int expectedStatus)
    throws Exception
    {
    	JSONObject comment = new JSONObject();
        comment.put("title", title);
        comment.put("content", content);
	    Response response = sendRequest(new PutRequest(getPostUrl(nodeRef), comment.toString(), "application/json"), expectedStatus);
	    
	    if (expectedStatus != 200)
	    {
	    	return null;
	    }
	    
	    //logger.debug("Comment updated: " + response.getContentAsString());
    	JSONObject result = new JSONObject(response.getContentAsString());
    	return result.getJSONObject("item");
    }
    
    // Tests
    
    public void testCreateForumPost() throws Exception
    {
    	String title = "test";
    	String content = "test";
    	JSONObject item = createPost(title, content, 200);
    	
    	// check that the values
    	assertEquals(title, item.get("title"));
    	assertEquals(content, item.get("content"));
    	
    	// fetch the post
    	getPost(item.getString("name"), 200);
    }
    
    public void testUpdateForumPost() throws Exception
    {
    	String title = "test";
    	String content = "test";
    	JSONObject item = createPost(title, content, 200);
    	
    	// check that the values
    	assertEquals(title, item.get("title"));
    	assertEquals(content, item.get("content"));
    	assertEquals(false, item.getBoolean("isUpdated"));
    	
    	// fetch the post
    	getPost(item.getString("name"), 200);
    	
    	String title2 = "test";
    	String content2 = "test";
    	item = updatePost(item.getString("nodeRef"), title2, content2, 200);
    	
    	// check that the values
    	assertEquals(title2, item.get("title"));
    	assertEquals(content2, item.get("content"));
    	assertEquals(true, item.getBoolean("isUpdated"));
    }
    
    public void testGetAll() throws Exception
    {
    	String url = URL_FORUM_POSTS;
    	Response response = sendRequest(new GetRequest(url), 200);
    	JSONObject result = new JSONObject(response.getContentAsString());
    	
    	// we should have posts.size + drafts.size together
    	assertEquals(this.posts.size(), result.getInt("total"));
    }
    
    public void testDeleteToplevelPost() throws Exception
    {
    	// create a post
    	JSONObject item = createPost("test", "test", 200);
    	String name = item.getString("name");
    	
    	// delete the post
    	Response response = sendRequest(new DeleteRequest(URL_FORUM_POST + name), 200);
    	
    	// try to fetch it again
    	sendRequest(new GetRequest(URL_FORUM_POST + name), 404);
    }

    public void testAddReply() throws Exception
    {
    	// create a root post
    	JSONObject item = createPost("test", "test", 200);
    	String postName = item.getString("name");
    	String postNodeRef = item.getString("nodeRef");
    	
    	// add a reply
    	JSONObject reply = createReply(postNodeRef, "test", "test", 200);
    	String replyNodeRef = reply.getString("nodeRef");
    	assertEquals("test", reply.getString("title"));
    	assertEquals("test", reply.getString("content"));
    	
    	// add a reply to the reply
    	JSONObject reply2 = createReply(replyNodeRef, "test2", "test2", 200);
    	assertEquals("test2", reply2.getString("title"));
    	assertEquals("test2", reply2.getString("content"));
    	
    	// fetch all replies for the post
    	Response response = sendRequest(new GetRequest(getRepliesUrl(postNodeRef)), 200);
    	logger.debug(response.getContentAsString());
    	JSONObject result = new JSONObject(response.getContentAsString());
    	// check the number of replies
    	assertEquals(1, result.getJSONArray("items").length());
    	
    	// fetch again the top level post
    	item = getPost(postName, 200);
    	assertEquals(2, item.getInt("totalReplyCount"));
    	assertEquals(1, item.getInt("replyCount"));
    }

    public void testUpdateReply() throws Exception
    {
    	// create a root post
    	JSONObject item = createPost("test", "test", 200);
    	String postName = item.getString("name");
    	String postNodeRef = item.getString("nodeRef");
    	
    	// add a reply
    	JSONObject reply = createReply(postNodeRef, "test", "test", 200);
    	String replyNodeRef = reply.getString("nodeRef");
    	assertEquals("test", reply.getString("title"));
    	assertEquals("test", reply.getString("content"));
    	assertEquals(false, reply.getBoolean("isUpdated"));
    	
    	// now update it
    	JSONObject reply2 = updatePost(reply.getString("nodeRef"), "test2", "test2", 200);
    	assertEquals("test2", reply2.getString("title"));
    	assertEquals("test2", reply2.getString("content"));
    	assertEquals(true, reply2.getBoolean("isUpdated"));
    }
    
    /*
    public void testDeleteReplyPost() throws Exception
    {
    	// create a root post
    	JSONObject item = createPost("test", "test", 200);
    	String postName = item.getString("name");
    	String postNodeRef = item.getString("nodeRef");
    	
    	// add a reply
    	JSONObject reply = createReply(postNodeRef, "test", "test", 200);
    	String replyNodeRef = reply.getString("nodeRef");
    	assertEquals("test", reply.getString("title"));
    	assertEquals("test", reply.getString("content"));
    	
    	// delete the reply
    	deleteRequest(getPostUrl(replyNodeRef), 200);

    	// fetch again the top level post
    	item = getPost(postName, 200);
    	assertEquals(0, item.getInt("totalReplyCount"));
    	assertEquals(0, item.getInt("replyCount"));
    }
    */
}