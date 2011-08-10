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
import org.alfresco.model.ForumModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.PropertyMap;
import org.springframework.extensions.webscripts.Status;
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
    private static final String DELETED_REPLY_POST_MARKER = "[[deleted]]";
	
    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    private SiteService siteService;
    private NodeService nodeService;
    
    private static final String USER_ONE = "UserOneThird";
    private static final String USER_TWO = "UserTwoThird";
    private static final String SITE_SHORT_NAME_DISCUSSION = "DiscussionSiteShortNameThree";
    private static final String COMPONENT_DISCUSSION = "discussion";

    private static final String URL_FORUM_SITE_POST = "/api/forum/post/site/" + SITE_SHORT_NAME_DISCUSSION + "/" + COMPONENT_DISCUSSION + "/";
    private static final String URL_FORUM_SITE_POSTS = "/api/forum/site/" + SITE_SHORT_NAME_DISCUSSION + "/" + COMPONENT_DISCUSSION + "/posts";
    private static final String URL_FORUM_NODE_POST_BASE = "/api/forum/post/node/"; // Plus node id
    private static final String URL_FORUM_NODE_POSTS_BASE = "/api/forum/node/"; // Plus node id + /posts 
    
    private List<String> posts = new ArrayList<String>(5);
    private NodeRef FORUM_NODE;

    
    // General methods

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.authenticationService = (MutableAuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        this.siteService = (SiteService)getServer().getApplicationContext().getBean("SiteService");
        this.nodeService = (NodeService)getServer().getApplicationContext().getBean("NodeService");
        
        // Authenticate as user
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        // Create test site
        // - only create the site if it doesn't already exist
        SiteInfo siteInfo = this.siteService.getSite(SITE_SHORT_NAME_DISCUSSION);
        if (siteInfo == null)
        {
            siteInfo = this.siteService.createSite("DiscussionSitePreset", SITE_SHORT_NAME_DISCUSSION, 
                  "DiscussionSiteTitle", "DiscussionSiteDescription", SiteVisibility.PUBLIC);
        }
        
        // Create the forum
        String forumNodeName = "TestForum";
        FORUM_NODE = nodeService.getChildByName(siteInfo.getNodeRef(), ContentModel.ASSOC_CONTAINS, forumNodeName);
        if(FORUM_NODE == null)
        {
           FORUM_NODE = nodeService.createNode(
                 siteInfo.getNodeRef(), ContentModel.ASSOC_CONTAINS,
                 QName.createQName(forumNodeName), ForumModel.TYPE_FORUM
           ).getChildRef();
           nodeService.setProperty(FORUM_NODE, ContentModel.PROP_NAME, forumNodeName); 
           nodeService.setProperty(FORUM_NODE, ContentModel.PROP_TITLE, forumNodeName); 
        }
        
        // Create users
        createUser(USER_ONE, SiteModel.SITE_COLLABORATOR);
        createUser(USER_TWO, SiteModel.SITE_CONTRIBUTOR);
        
        // Do tests as inviter user
        this.authenticationComponent.setCurrentUser(USER_ONE);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        // admin user required to delete user
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        // delete the discussions users
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
        
        // delete discussions test site
        siteService.deleteSite(SITE_SHORT_NAME_DISCUSSION);
    }
    
    private void createUser(String userName, String role)
    {
        // if user with given user name doesn't already exist then create user
        if (!this.authenticationService.authenticationExists(userName))
        {
            // create user
            this.authenticationService.createAuthentication(userName, "password".toCharArray());
        }
         
        if (!this.personService.personExists(userName))
        {
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
    
    
    // -----------------------------------------------------
    //     Test helper methods
    // -----------------------------------------------------
    
    /**
     * Creates a new topic+post in the test site
     */
    private JSONObject createSitePost(String title, String content, int expectedStatus)
    throws Exception
    {
       return doCreatePost(URL_FORUM_SITE_POSTS, title, content, expectedStatus);
    }
    /**
     * Creates a new topic+post under the given node
     */
    private JSONObject createNodePost(NodeRef nodeRef, String title, String content, 
          int expectedStatus) throws Exception
    {
       return doCreatePost(getPostsUrl(nodeRef), title, content, expectedStatus);
    }
    private JSONObject doCreatePost(String url, String title, String content, 
          int expectedStatus) throws Exception
    {
       JSONObject post = new JSONObject();
       post.put("title", title);
       post.put("content", content);
       Response response = sendRequest(new PostRequest(url, post.toString(), "application/json"), expectedStatus);

       if (expectedStatus != Status.STATUS_OK)
       {
          return null;
       }

       JSONObject result = new JSONObject(response.getContentAsString());
       JSONObject item = result.getJSONObject("item");
       posts.add(item.getString("name"));
       return item;
    }

    private JSONObject updatePost(NodeRef nodeRef, String title, String content, 
          int expectedStatus) throws Exception
    {
       return doUpdatePost(getPostUrl(nodeRef), title, content, expectedStatus);
    }
    private JSONObject updatePost(String name, String title, String content, 
          int expectedStatus) throws Exception
    {
       return doUpdatePost(URL_FORUM_SITE_POST + name, title, content, expectedStatus);
    }
    private JSONObject doUpdatePost(String url, String title, String content, 
          int expectedStatus) throws Exception
    {
       JSONObject post = new JSONObject();
       post.put("title", title);
       post.put("content", content);
       Response response = sendRequest(new PutRequest(url, post.toString(), "application/json"), expectedStatus);

       if (expectedStatus != Status.STATUS_OK)
       {
          return null;
       }

       JSONObject result = new JSONObject(response.getContentAsString());
       return result.getJSONObject("item");
    }
    
    private JSONObject getPost(String name, int expectedStatus) throws Exception
    {
       return doGetPost(URL_FORUM_SITE_POST + name, expectedStatus);
    }
    private JSONObject getPost(NodeRef nodeRef, int expectedStatus) throws Exception
    {
       return doGetPost(getPostUrl(nodeRef), expectedStatus);
    }
    private JSONObject doGetPost(String url, int expectedStatus) throws Exception
    {
       Response response = sendRequest(new GetRequest(url), expectedStatus);
       if (expectedStatus == Status.STATUS_OK)
       {
          JSONObject result = new JSONObject(response.getContentAsString());
          return result.getJSONObject("item");
       }
       else
       {
          return null;
       }
    }
    
    private JSONObject deletePost(String name, int expectedStatus) throws Exception
    {
       return doDeletePost(URL_FORUM_SITE_POST + name, expectedStatus);
    }
    private JSONObject deletePost(NodeRef nodeRef, int expectedStatus) throws Exception
    {
       return doDeletePost(getPostUrl(nodeRef), expectedStatus);
    }
    private JSONObject doDeletePost(String url, int expectedStatus) throws Exception
    {
       Response response = sendRequest(new DeleteRequest(url), Status.STATUS_OK);
       if (expectedStatus == Status.STATUS_OK)
       {
          return new JSONObject(response.getContentAsString());
       }
       else
       {
          return null;
       }
    }

    // TODO Method to get replies
    
    private String getRepliesUrl(NodeRef nodeRef)
    {
       return getPostUrl(nodeRef) + "/replies";
    }
    private String getRepliesUrl(String postName)
    {
       return URL_FORUM_SITE_POST + postName + "/replies";
    }
    
    private String getPostUrl(NodeRef nodeRef)
    {
       return URL_FORUM_NODE_POST_BASE + nodeRef.toString().replace("://", "/");
    }
    private String getPostsUrl(NodeRef nodeRef)
    {
       return URL_FORUM_NODE_POSTS_BASE + nodeRef.toString().replace("://", "/") + "/posts";
    }
    
    private JSONObject createReply(NodeRef nodeRef, String title, String content, int expectedStatus)
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
    
    // TODO Non NodeRef version
    private JSONObject updateComment(NodeRef nodeRef, String title, String content, 
          int expectedStatus) throws Exception
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

    
    // -----------------------------------------------------
    //     Tests
    // -----------------------------------------------------
    
    public void testCreateForumPost() throws Exception
    {
    	String title = "test";
    	String content = "test";
    	JSONObject item = createSitePost(title, content, Status.STATUS_OK);
    	
    	// Check that the values in the response are correct
    	assertEquals(title, item.get("title"));
    	assertEquals(content, item.get("content"));
      assertEquals(0, item.get("replyCount"));
    	assertEquals("Invalid JSON " + item, true, item.has("createdOn"));
      assertEquals("Invalid JSON " + item, true, item.has("modifiedOn"));
      assertEquals("Invalid JSON " + item, true, item.has("author"));
      assertEquals("Invalid JSON " + item, true, item.has("permissions"));
      assertEquals("Invalid JSON " + item, true, item.has("url"));
      assertEquals("Invalid JSON " + item, true, item.has("repliesUrl"));
      assertEquals("Invalid JSON " + item, true, item.has("nodeRef"));
      
      // Save some details
      String name = item.getString("name");
      NodeRef nodeRef = new NodeRef(item.getString("nodeRef"));

      
      // Fetch the post by name and check
    	item = getPost(name, Status.STATUS_OK);
    	
      assertEquals(title, item.get("title"));
      assertEquals(content, item.get("content"));
      assertEquals(0, item.get("replyCount"));
      assertEquals("Invalid JSON " + item, true, item.has("createdOn"));
      assertEquals("Invalid JSON " + item, true, item.has("modifiedOn"));
      assertEquals("Invalid JSON " + item, true, item.has("author"));
      assertEquals("Invalid JSON " + item, true, item.has("permissions"));
      assertEquals("Invalid JSON " + item, true, item.has("url"));
      assertEquals("Invalid JSON " + item, true, item.has("repliesUrl"));
      assertEquals("Invalid JSON " + item, true, item.has("nodeRef"));
    	
    	
    	// Fetch the post by noderef and check
      item = getPost(nodeRef, Status.STATUS_OK);
      
      assertEquals(title, item.get("title"));
      assertEquals(content, item.get("content"));
      assertEquals(0, item.get("replyCount"));
      assertEquals("Invalid JSON " + item, true, item.has("createdOn"));
      assertEquals("Invalid JSON " + item, true, item.has("modifiedOn"));
      assertEquals("Invalid JSON " + item, true, item.has("author"));
      assertEquals("Invalid JSON " + item, true, item.has("permissions"));
      assertEquals("Invalid JSON " + item, true, item.has("url"));
      assertEquals("Invalid JSON " + item, true, item.has("repliesUrl"));
      assertEquals("Invalid JSON " + item, true, item.has("nodeRef"));
    	
      
    	// Create another post, this time by noderef
      title = "By Node Title";
      content = "By Node Content";
      item = createNodePost(FORUM_NODE, title, content, Status.STATUS_OK);
      
      assertEquals(title, item.get("title"));
      assertEquals(content, item.get("content"));
      assertEquals(0, item.get("replyCount"));
      
      // Check it by noderef
      nodeRef = new NodeRef(item.getString("nodeRef"));
      item = getPost(nodeRef, Status.STATUS_OK);
      
      assertEquals(title, item.get("title"));
      assertEquals(content, item.get("content"));
      assertEquals(0, item.get("replyCount"));
    }
    
    public void testUpdateForumPost() throws Exception
    {
    	String title = "test";
    	String content = "test";
    	JSONObject item = createSitePost(title, content, 200);
    	
    	// check that the values
    	assertEquals(title, item.get("title"));
    	assertEquals(content, item.get("content"));
    	assertEquals(false, item.getBoolean("isUpdated"));
    	
      assertEquals(true, item.has("name"));
      String name = item.getString("name");
      assertEquals(true, item.has("nodeRef"));
    	NodeRef nodeRef = new NodeRef(item.getString("nodeRef"));
    	
    	// fetch the post by name
    	item = getPost(item.getString("name"), 200);
      assertEquals(title, item.get("title"));
      assertEquals(content, item.get("content"));
      assertEquals(false, item.getBoolean("isUpdated"));

      // Fetch the post by noderef
      item = getPost(nodeRef, 200);
      assertEquals(title, item.get("title"));
      assertEquals(content, item.get("content"));
      assertEquals(false, item.getBoolean("isUpdated"));

      
    	// Update it by name
      String title2 = "updated test";
      String content2 = "test updated";
      item = updatePost(name, title2, content2, 200);
      
      // Check the response
      assertEquals(title2, item.get("title"));
      assertEquals(content2, item.get("content"));
      assertEquals(name, item.get("name"));
      assertEquals(nodeRef.toString(), item.get("nodeRef"));
      assertEquals(true, item.getBoolean("isUpdated"));
      
      // Fetch and check
      item = getPost(nodeRef, 200);
      assertEquals(title2, item.get("title"));
      assertEquals(content2, item.get("content"));
      assertEquals(name, item.get("name"));
      assertEquals(nodeRef.toString(), item.get("nodeRef"));
      assertEquals(true, item.getBoolean("isUpdated"));
      
      
      // Update it again, this time by noderef
      String title3 = "updated 3 test";
      String content3 = "test 3 updated";
    	item = updatePost(nodeRef, title3, content3, 200);
    	
    	// Check that the values returned are correct
      assertEquals(title3, item.get("title"));
      assertEquals(content3, item.get("content"));
      assertEquals(name, item.get("name"));
      assertEquals(nodeRef.toString(), item.get("nodeRef"));
      assertEquals(true, item.getBoolean("isUpdated"));
      
      // Fetch and re-check
      item = getPost(nodeRef, 200);
      assertEquals(title3, item.get("title"));
      assertEquals(content3, item.get("content"));
      assertEquals(name, item.get("name"));
      assertEquals(nodeRef.toString(), item.get("nodeRef"));
      assertEquals(true, item.getBoolean("isUpdated"));
    }
    
    /**
     * Tests that the permissions details included with topics and
     *  posts are correct
     */
    public void testPermissions() throws Exception
    {
       // Create a post, and check the details on it
       JSONObject item = createSitePost("test", "test", Status.STATUS_OK);
       String name = item.getString("name");
       
       JSONObject perms = item.getJSONObject("permissions");
       assertEquals(true, perms.getBoolean("edit"));
       assertEquals(true, perms.getBoolean("reply"));
       assertEquals(true, perms.getBoolean("delete"));
       
       // Check on a fetch too
       item = getPost(name, Status.STATUS_OK);
       perms = item.getJSONObject("permissions");
       assertEquals(true, perms.getBoolean("edit"));
       assertEquals(true, perms.getBoolean("reply"));
       assertEquals(true, perms.getBoolean("delete"));
       
       
       // Switch to another user, see what they see
       this.authenticationComponent.setCurrentUser(USER_TWO);
       
       item = getPost(name, Status.STATUS_OK);
       perms = item.getJSONObject("permissions");
       assertEquals(false, perms.getBoolean("edit"));
       assertEquals(true, perms.getBoolean("reply"));
       assertEquals(false, perms.getBoolean("delete"));
       
       
       // Remove the user from the site, see the change
       this.siteService.removeMembership(SITE_SHORT_NAME_DISCUSSION, USER_TWO);
       
       item = getPost(name, Status.STATUS_OK);
       perms = item.getJSONObject("permissions");
       assertEquals(false, perms.getBoolean("edit"));
       assertEquals(false, perms.getBoolean("reply"));
       assertEquals(false, perms.getBoolean("delete"));
       
       
       // Make the site private, will vanish
       SiteInfo siteInfo = siteService.getSite(SITE_SHORT_NAME_DISCUSSION);
       siteInfo.setVisibility(SiteVisibility.PRIVATE);
       this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
       this.siteService.updateSite(siteInfo);
       this.authenticationComponent.setCurrentUser(USER_TWO);
       
       // On a private site we're not a member of, shouldn't be visable at all
       getPost(name, Status.STATUS_NOT_FOUND);
    }
    
    public void testAddReply() throws Exception
    {
    	// Create a root post
    	JSONObject item = createSitePost("test", "test", Status.STATUS_OK);
    	String postName = item.getString("name");
    	NodeRef topicNodeRef = new NodeRef(item.getString("nodeRef"));
    	
    	// Add a reply
    	JSONObject reply = createReply(topicNodeRef, "test", "test", Status.STATUS_OK);
    	NodeRef replyNodeRef = new NodeRef(reply.getString("nodeRef"));
    	assertEquals("test", reply.getString("title"));
    	assertEquals("test", reply.getString("content"));
    	
    	// Add a reply to the reply
    	JSONObject reply2 = createReply(replyNodeRef, "test2", "test2", 200);
      NodeRef reply2NodeRef = new NodeRef(reply2.getString("nodeRef"));
    	assertEquals("test2", reply2.getString("title"));
    	assertEquals("test2", reply2.getString("content"));
    	
    	
    	// Check things were correctly setup. These should all be siblings
    	//  of each other, with relations between the replies
      assertEquals(ForumModel.TYPE_TOPIC, nodeService.getType(topicNodeRef));
      assertEquals(ForumModel.TYPE_POST, nodeService.getType(replyNodeRef));
      assertEquals(ForumModel.TYPE_POST, nodeService.getType(reply2NodeRef));
      assertEquals(topicNodeRef, nodeService.getPrimaryParent(replyNodeRef).getParentRef());
      assertEquals(topicNodeRef, nodeService.getPrimaryParent(reply2NodeRef).getParentRef());

      // Reply 2 should have an assoc to Reply 1
      assertEquals(0, nodeService.getSourceAssocs(reply2NodeRef, RegexQNamePattern.MATCH_ALL).size());
      assertEquals(1, nodeService.getTargetAssocs(reply2NodeRef, RegexQNamePattern.MATCH_ALL).size());
      assertEquals(replyNodeRef, nodeService.getTargetAssocs(reply2NodeRef, RegexQNamePattern.MATCH_ALL).get(0).getTargetRef());
      
      assertEquals(1, nodeService.getSourceAssocs(replyNodeRef, RegexQNamePattern.MATCH_ALL).size());
      assertEquals(1, nodeService.getTargetAssocs(replyNodeRef, RegexQNamePattern.MATCH_ALL).size());
      assertEquals(reply2NodeRef, nodeService.getSourceAssocs(replyNodeRef, RegexQNamePattern.MATCH_ALL).get(0).getSourceRef());

      
    	// Fetch all replies for the post
    	Response response = sendRequest(new GetRequest(getRepliesUrl(topicNodeRef)), 200);
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
       // Create a root post
       JSONObject item = createSitePost("test", "test", Status.STATUS_OK);
       String postName = item.getString("name");
       NodeRef postNodeRef = new NodeRef(item.getString("nodeRef"));
       assertEquals("test", item.getString("title"));
       assertEquals("test", item.getString("content"));
       assertEquals(false, item.getBoolean("isUpdated"));


       // Add a reply to it
       JSONObject reply = createReply(postNodeRef, "rtest", "rtest", Status.STATUS_OK);
       NodeRef replyNodeRef = new NodeRef(reply.getString("nodeRef"));
       assertEquals("rtest", reply.getString("title"));
       assertEquals("rtest", reply.getString("content"));
       assertEquals(false, reply.getBoolean("isUpdated"));


       // Now update the reply
       JSONObject reply2 = updatePost(replyNodeRef, "test2", "test2", Status.STATUS_OK);
       assertEquals("test2", reply2.getString("title"));
       assertEquals("test2", reply2.getString("content"));
       assertEquals(true, reply2.getBoolean("isUpdated"));

       // Fetch it to check
       reply2 = getPost(replyNodeRef, Status.STATUS_OK);
       assertEquals("test2", reply2.getString("title"));
       assertEquals("test2", reply2.getString("content"));
       assertEquals(true, reply2.getBoolean("isUpdated"));


       // Ensure the original post wasn't changed
       item = getPost(postName, Status.STATUS_OK);
       assertEquals("test", item.getString("title"));
       assertEquals("test", item.getString("content"));
       assertEquals(false, item.getBoolean("isUpdated"));
    }
    
    public void testDeleteToplevelPost() throws Exception
    {
       // Create two posts
       JSONObject item1 = createSitePost("test1", "test1", Status.STATUS_OK);
       JSONObject item2 = createSitePost("test2", "test2", Status.STATUS_OK);
       String name1 = item1.getString("name");
       NodeRef nodeRef1 = new NodeRef(item1.getString("nodeRef"));
       NodeRef nodeRef2 = new NodeRef(item2.getString("nodeRef"));

       // The node references returned correspond to the topics
       assertEquals(ForumModel.TYPE_TOPIC, nodeService.getType(nodeRef1));
       assertEquals(ForumModel.TYPE_TOPIC, nodeService.getType(nodeRef2));


       // Delete one post by name
       deletePost(name1, Status.STATUS_OK);

       // Check it went
       getPost(name1, Status.STATUS_NOT_FOUND);


       // Delete the other post by noderef
       deletePost(nodeRef2, Status.STATUS_OK);

       // Check it went
       getPost(nodeRef2, Status.STATUS_NOT_FOUND);


       // Check all the nodes have gone
       assertEquals(false, nodeService.exists(nodeRef1));
       assertEquals(false, nodeService.exists(nodeRef2));
    }
    
    public void testDeleteReplyPost() throws Exception
    {
      // Create a root post
      JSONObject item = createSitePost("test", "test", Status.STATUS_OK);
      String postName = item.getString("name");
      NodeRef postNodeRef = new NodeRef(item.getString("nodeRef"));
      
      // It doesn't have any replies yet
      assertEquals(0, item.getInt("totalReplyCount"));
      assertEquals(0, item.getInt("replyCount"));
      
      
      // Add a reply
      JSONObject reply = createReply(postNodeRef, "testR", "testR", Status.STATUS_OK);
      NodeRef replyNodeRef = new NodeRef(reply.getString("nodeRef"));
      String replyName = reply.getString("name");
      assertEquals("testR", reply.getString("title"));
      assertEquals("testR", reply.getString("content"));
      
      // Fetch the reply and check
      reply = getPost(replyNodeRef, Status.STATUS_OK);
      assertEquals("testR", reply.getString("title"));
      assertEquals("testR", reply.getString("content"));
      
      // Note - you can't fetch a reply by name, only by noderef
      // It only works for primary posts as they share the topic name
      getPost(replyName, Status.STATUS_NOT_FOUND);
      
      
      // Check the main post, ensure the replies show up
      item = getPost(postName, Status.STATUS_OK);
      assertEquals(1, item.getInt("totalReplyCount"));
      assertEquals(1, item.getInt("replyCount"));

      
      // Delete the reply
      deletePost(replyNodeRef, Status.STATUS_OK);
      
      // These nodes don't really get deleted at the moment
      // Due to threading, we just add special marker text
      // TODO Really we should probably delete posts with no attached replies
      reply = getPost(replyNodeRef, Status.STATUS_OK);
      assertEquals(DELETED_REPLY_POST_MARKER, reply.get("title"));
      assertEquals(DELETED_REPLY_POST_MARKER, reply.get("content"));
      
      
      // Fetch the top level post again, replies stay because they
      //  haven't really been deleted...
      // TODO Really we should probably delete posts with no attached replies
      item = getPost(postName, Status.STATUS_OK);
      assertEquals(1, item.getInt("totalReplyCount"));
      assertEquals(1, item.getInt("replyCount"));
    }
    
    /**
     * Test for the various listings:
     *  All, New, Hot (Most Active), Mine 
     */
    public void testListings() throws Exception
    {
      String url = URL_FORUM_SITE_POSTS;
      Response response = sendRequest(new GetRequest(url), 200);
      JSONObject result = new JSONObject(response.getContentAsString());
      
      // TODO Expand
      
      // we should have posts.size + drafts.size together
      assertEquals(this.posts.size(), result.getInt("total"));
    }
    
}