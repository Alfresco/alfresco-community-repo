/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.web.scripts.discussion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Unit Test to test Discussions Web Script API
 */
public class DiscussionRestApiTest extends BaseWebScriptTest
{
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(DiscussionRestApiTest.class);
    
    private static final String DELETED_REPLY_POST_MARKER = "[[deleted]]";

    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private TransactionService transactionService;
    private BehaviourFilter policyBehaviourFilter;
    private PermissionService permissionService;
    private PersonService personService;
    private SiteService siteService;
    private NodeService nodeService;
    private NodeService internalNodeService;
    private NodeArchiveService nodeArchiveService;
    
    private static final String USER_ONE = "UserOneThird";
    private static final String USER_TWO = "UserTwoThird";
    private static final String SITE_SHORT_NAME_DISCUSSION = "DiscussionSiteShortNameThree";
    private static final String COMPONENT_DISCUSSION = "discussions";

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
        this.policyBehaviourFilter = (BehaviourFilter)getServer().getApplicationContext().getBean("policyBehaviourFilter");
        this.transactionService = (TransactionService)getServer().getApplicationContext().getBean("transactionService");
        this.permissionService = (PermissionService)getServer().getApplicationContext().getBean("PermissionService");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        this.siteService = (SiteService)getServer().getApplicationContext().getBean("SiteService");
        this.nodeService = (NodeService)getServer().getApplicationContext().getBean("NodeService");
        this.internalNodeService = (NodeService)getServer().getApplicationContext().getBean("nodeService");
        this.nodeArchiveService = (NodeArchiveService)getServer().getApplicationContext().getBean("nodeArchiveService");
        
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
        final NodeRef siteNodeRef = siteInfo.getNodeRef();
        
        // Create the forum
        final String forumNodeName = "TestForum";
        FORUM_NODE = nodeService.getChildByName(siteInfo.getNodeRef(), ContentModel.ASSOC_CONTAINS, forumNodeName);
        if (FORUM_NODE == null)
        {
           FORUM_NODE = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
              @Override
              public NodeRef execute() throws Throwable {
                 Map<QName, Serializable> props = new HashMap<QName, Serializable>(5);
                 props.put(ContentModel.PROP_NAME, forumNodeName);
                 props.put(ContentModel.PROP_TITLE, forumNodeName);

                 return nodeService.createNode(
                       siteNodeRef, ContentModel.ASSOC_CONTAINS,
                       QName.createQName(forumNodeName), ForumModel.TYPE_FORUM, props 
                 ).getChildRef();
              }
           });
        }
        
        // Create users
        createUser(USER_ONE, SiteModel.SITE_COLLABORATOR, SITE_SHORT_NAME_DISCUSSION);
        createUser(USER_TWO, SiteModel.SITE_CONTRIBUTOR, SITE_SHORT_NAME_DISCUSSION);
        
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
        if(personService.personExists(USER_ONE))
        {
           personService.deletePerson(USER_ONE);
        }
        if (this.authenticationService.authenticationExists(USER_ONE))
        {
           this.authenticationService.deleteAuthentication(USER_ONE);
        }
        
        if(personService.personExists(USER_TWO))
        {
           personService.deletePerson(USER_TWO);
        }
        if (this.authenticationService.authenticationExists(USER_TWO))
        {
           this.authenticationService.deleteAuthentication(USER_TWO);
        }
        
        SiteInfo siteInfo = this.siteService.getSite(SITE_SHORT_NAME_DISCUSSION);
        if (siteInfo != null)
        {
           // delete discussions test site
           RetryingTransactionCallback<Void> deleteCallback = new RetryingTransactionCallback<Void>()
           {
               @Override
               public Void execute() throws Throwable
               {
                   siteService.deleteSite(SITE_SHORT_NAME_DISCUSSION);
                   return null;
               }
            };
            transactionService.getRetryingTransactionHelper().doInTransaction(deleteCallback);
            nodeArchiveService.purgeArchivedNode(nodeArchiveService.getArchivedNode(siteInfo.getNodeRef()));
        }
    }
    
    private void createUser(String userName, String role, String siteName)
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
        this.siteService.setMembership(siteName, userName, role);
        
        // Give the test user access to the test node
        // They need to be able to read it, and create children of it
        permissionService.setPermission(FORUM_NODE, userName, PermissionService.READ, true);
        permissionService.setPermission(FORUM_NODE, userName, PermissionService.CREATE_CHILDREN, true);
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
    
    private JSONObject getReplies(String name, int expectedStatus) throws Exception
    {
       return doGetReplies(getRepliesUrl(name), expectedStatus);
    }
    
    private JSONObject getReplies(NodeRef nodeRef, int expectedStatus) throws Exception
    {
       return doGetReplies(getRepliesUrl(nodeRef), expectedStatus);
    }
    
    private JSONObject doGetReplies(String url, int expectedStatus) throws Exception
    {
       Response response = sendRequest(new GetRequest(url), expectedStatus);
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
    
    private JSONObject getPosts(String type, int expectedStatus) throws Exception
    {
       return doGetPosts(URL_FORUM_SITE_POSTS, type, expectedStatus);
    }
    
    private JSONObject getPosts(NodeRef nodeRef, String type, int expectedStatus) throws Exception
    {
       return doGetPosts(getPostsUrl(nodeRef), type, expectedStatus);
    }
    
    private JSONObject doGetPosts(String baseUrl, String type, int expectedStatus) throws Exception
    {
       String url = null;
       if (type == null)
       {
          url = baseUrl;
       }
       else if (type == "limit")
       {
          url = baseUrl + "?pageSize=1";
       }
       else if (type == "hot")
       {
          url = baseUrl + "/hot";
       }
       else if (type == "mine")
       {
          url = baseUrl + "/myposts";
       }
       else if (type.startsWith("new"))
       {
          url = baseUrl + "/" + type;
       }
       else
       {
          throw new IllegalArgumentException("Invalid search type " + type);
       }
       
       Response response = sendRequest(new GetRequest(url), expectedStatus);
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
    
    private JSONObject updateComment(NodeRef nodeRef, String title, String content, 
          int expectedStatus) throws Exception
    {
       JSONObject comment = new JSONObject();
       comment.put("title", title);
       comment.put("content", content);
       Response response = sendRequest(new PutRequest(getPostUrl(nodeRef), comment.toString(), "application/json"), expectedStatus);

       if (expectedStatus != Status.STATUS_OK)
       {
          return null;
       }

       //logger.debug("Comment updated: " + response.getContentAsString());
       JSONObject result = new JSONObject(response.getContentAsString());
       return result.getJSONObject("item");
    }

    /**
     * Monkeys with the created and published dates on a topic+posts
     */
    private void pushCreatedDateBack(NodeRef node, int daysAgo) throws Exception
    {
       Date created = (Date)nodeService.getProperty(node, ContentModel.PROP_CREATED);
       Date newCreated = new Date(created.getTime() - daysAgo*24*60*60*1000);
       Date published = (Date)nodeService.getProperty(node, ContentModel.PROP_PUBLISHED);
       if(published == null) published = created;
       Date newPublished = new Date(published.getTime() - daysAgo*24*60*60*1000);
       
       UserTransaction txn = transactionService.getUserTransaction();
       txn.begin();

       this.policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
       internalNodeService.setProperty(node, ContentModel.PROP_CREATED, newCreated);
       internalNodeService.setProperty(node, ContentModel.PROP_MODIFIED, newCreated);
       internalNodeService.setProperty(node, ContentModel.PROP_PUBLISHED, newPublished);
       this.policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
       
       txn.commit();
       
       // Now chance something else on the node to have it re-indexed
       nodeService.setProperty(node, ContentModel.PROP_CREATED, newCreated);
       nodeService.setProperty(node, ContentModel.PROP_MODIFIED, newCreated);
       nodeService.setProperty(node, ContentModel.PROP_PUBLISHED, newPublished);
       nodeService.setProperty(node, ContentModel.PROP_DESCRIPTION, "Forced change");
       
       // Finally change any children (eg if updating a topic, do the posts)
       for(ChildAssociationRef ref : nodeService.getChildAssocs(node))
       {
          pushCreatedDateBack(ref.getChildRef(), daysAgo);
       }
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
       
    /**
     * ALF-1973 - If the user who added a reply has been deleted, don't break
     */
    public void testViewReplyByDeletedUser() throws Exception
    {
       // Create a post
       JSONObject item = createSitePost("test", "test", Status.STATUS_OK);
       String name = item.getString("name");
       NodeRef topicNodeRef = new NodeRef(item.getString("nodeRef"));
       
       // Now create a reply as a different user
       this.authenticationComponent.setCurrentUser(USER_TWO);
       createReply(topicNodeRef, "Reply", "By the other user", Status.STATUS_OK);
       
       // Should see the reply
       item = getReplies(name, Status.STATUS_OK);
       assertEquals(1, item.getJSONArray("items").length());
       
       // Delete the user, check that the reply still shows
       this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
       personService.deletePerson(USER_TWO);
       this.authenticationComponent.setCurrentUser(USER_ONE);
       
       item = getReplies(name, Status.STATUS_OK);
       assertEquals(1, item.getJSONArray("items").length());
    }
    
    public void testAddReply() throws Exception
    {
        // Create a root post
        JSONObject item = createSitePost("test", "test", Status.STATUS_OK);
        String topicName = item.getString("name");
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
        JSONObject result = getReplies(topicNodeRef, Status.STATUS_OK);
        // check the number of replies
        assertEquals(1, result.getJSONArray("items").length());
        
        // Check the replies by name too
        result = getReplies(topicName, Status.STATUS_OK);
        assertEquals(1, result.getJSONArray("items").length());

        
        // Fetch the top level post again, and check the counts there
        // That post should have one direct reply, and one reply to it's reply
        item = getPost(topicName, Status.STATUS_OK);
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
      JSONObject result;
      JSONObject item;
      
      
      // Check all of the listings, none should have anything yet
      result = getPosts(null, Status.STATUS_OK);
      assertEquals(0, result.getInt("total"));
      assertEquals(0, result.getInt("itemCount"));
      assertEquals(0, result.getJSONArray("items").length());
      
      result = getPosts("hot", Status.STATUS_OK);
      assertEquals(0, result.getInt("total"));
      assertEquals(0, result.getInt("itemCount"));
      assertEquals(0, result.getJSONArray("items").length());

      result = getPosts("mine", Status.STATUS_OK);
      assertEquals(0, result.getInt("total"));
      assertEquals(0, result.getInt("itemCount"));
      assertEquals(0, result.getJSONArray("items").length());

      result = getPosts("new?numdays=100", Status.STATUS_OK);
      assertEquals(0, result.getInt("total"));
      assertEquals(0, result.getInt("itemCount"));
      assertEquals(0, result.getJSONArray("items").length());

      
      // Check with a noderef too
      result = getPosts(FORUM_NODE, null, Status.STATUS_OK);
      assertEquals(0, result.getInt("total"));
      assertEquals(0, result.getInt("itemCount"));
      assertEquals(0, result.getJSONArray("items").length());
      
      result = getPosts(FORUM_NODE, "hot", Status.STATUS_OK);
      assertEquals(0, result.getInt("total"));
      assertEquals(0, result.getInt("itemCount"));
      assertEquals(0, result.getJSONArray("items").length());

      result = getPosts(FORUM_NODE, "mine", Status.STATUS_OK);
      assertEquals(0, result.getInt("total"));
      assertEquals(0, result.getInt("itemCount"));
      assertEquals(0, result.getJSONArray("items").length());

      result = getPosts(FORUM_NODE, "new?numdays=100", Status.STATUS_OK);
      assertEquals(0, result.getInt("total"));
      assertEquals(0, result.getInt("itemCount"));
      assertEquals(0, result.getJSONArray("items").length());
      
      
      // Now add a few topics with replies
      // Some of these will be created as different users
      item = createSitePost("SiteTitle1", "Content", Status.STATUS_OK);
      NodeRef siteTopic1 = new NodeRef(item.getString("nodeRef"));
      this.authenticationComponent.setCurrentUser(USER_TWO);
      item = createSitePost("SiteTitle2", "Content", Status.STATUS_OK);
      NodeRef siteTopic2 = new NodeRef(item.getString("nodeRef"));
      
      item = createNodePost(FORUM_NODE, "NodeTitle1", "Content", Status.STATUS_OK);
      NodeRef nodeTopic1 = new NodeRef(item.getString("nodeRef"));
      this.authenticationComponent.setCurrentUser(USER_ONE);
      item = createNodePost(FORUM_NODE, "NodeTitle2", "Content", Status.STATUS_OK);
      NodeRef nodeTopic2 = new NodeRef(item.getString("nodeRef"));
      item = createNodePost(FORUM_NODE, "NodeTitle3", "Content", Status.STATUS_OK);
      NodeRef nodeTopic3 = new NodeRef(item.getString("nodeRef"));
      
      item = createReply(siteTopic1, "Reply1a", "Content", Status.STATUS_OK);
      NodeRef siteReply1A = new NodeRef(item.getString("nodeRef"));
      item = createReply(siteTopic1, "Reply1b", "Content", Status.STATUS_OK);
      NodeRef siteReply1B = new NodeRef(item.getString("nodeRef"));
      
      this.authenticationComponent.setCurrentUser(USER_TWO);
      item = createReply(siteTopic2, "Reply2a", "Content", Status.STATUS_OK);
      NodeRef siteReply2A = new NodeRef(item.getString("nodeRef"));
      item = createReply(siteTopic2, "Reply2b", "Content", Status.STATUS_OK);
      NodeRef siteReply2B = new NodeRef(item.getString("nodeRef"));
      item = createReply(siteTopic2, "Reply2c", "Content", Status.STATUS_OK);
      NodeRef siteReply2C = new NodeRef(item.getString("nodeRef"));

      item = createReply(siteReply2A, "Reply2aa", "Content", Status.STATUS_OK);
      NodeRef siteReply2AA = new NodeRef(item.getString("nodeRef"));
      item = createReply(siteReply2A, "Reply2ab", "Content", Status.STATUS_OK);
      NodeRef siteReply2AB = new NodeRef(item.getString("nodeRef"));
      this.authenticationComponent.setCurrentUser(USER_ONE);
      item = createReply(siteReply2AA, "Reply2aaa", "Content", Status.STATUS_OK);
      NodeRef siteReply2AAA = new NodeRef(item.getString("nodeRef"));
      
      item = createReply(nodeTopic1, "ReplyN1a", "Content", Status.STATUS_OK);
      NodeRef nodeReply1A = new NodeRef(item.getString("nodeRef"));
      item = createReply(nodeReply1A, "ReplyN1aa", "Content", Status.STATUS_OK);
      NodeRef nodeReply1AA = new NodeRef(item.getString("nodeRef"));
      item = createReply(nodeReply1AA, "ReplyN1aaa", "Content", Status.STATUS_OK);
      NodeRef nodeReply1AAA = new NodeRef(item.getString("nodeRef"));
      
      
      // Check for totals
      // We should get all the topics
      result = getPosts(null, Status.STATUS_OK);
      assertEquals(2, result.getInt("total"));
      assertEquals(2, result.getInt("itemCount"));
      assertEquals(2, result.getJSONArray("items").length());
      assertEquals("SiteTitle1", result.getJSONArray("items").getJSONObject(1).getString("title"));
      assertEquals("SiteTitle2", result.getJSONArray("items").getJSONObject(0).getString("title"));
      assertEquals(2, result.getJSONArray("items").getJSONObject(1).getInt("replyCount"));
      assertEquals(3, result.getJSONArray("items").getJSONObject(0).getInt("replyCount"));
      
      result = getPosts(FORUM_NODE, null, Status.STATUS_OK);
      assertEquals(3, result.getInt("total"));
      assertEquals(3, result.getInt("itemCount"));
      assertEquals(3, result.getJSONArray("items").length());
      assertEquals("NodeTitle1", result.getJSONArray("items").getJSONObject(2).getString("title"));
      assertEquals("NodeTitle2", result.getJSONArray("items").getJSONObject(1).getString("title"));
      assertEquals("NodeTitle3", result.getJSONArray("items").getJSONObject(0).getString("title"));
      assertEquals(1, result.getJSONArray("items").getJSONObject(2).getInt("replyCount"));
      assertEquals(0, result.getJSONArray("items").getJSONObject(1).getInt("replyCount"));
      assertEquals(0, result.getJSONArray("items").getJSONObject(0).getInt("replyCount"));
      
      
      // Check for "mine"
      // User 1 has Site 1, and Nodes 2 + 3
      result = getPosts("mine", Status.STATUS_OK);
      assertEquals(1, result.getInt("total"));
      assertEquals(1, result.getInt("itemCount"));
      assertEquals(1, result.getJSONArray("items").length());
      assertEquals("SiteTitle1", result.getJSONArray("items").getJSONObject(0).getString("title"));
      assertEquals(2, result.getJSONArray("items").getJSONObject(0).getInt("replyCount"));
      
      result = getPosts(FORUM_NODE, "mine", Status.STATUS_OK);
      assertEquals(2, result.getInt("total"));
      assertEquals(2, result.getInt("itemCount"));
      assertEquals(2, result.getJSONArray("items").length());
      assertEquals("NodeTitle2", result.getJSONArray("items").getJSONObject(0).getString("title"));
      assertEquals("NodeTitle3", result.getJSONArray("items").getJSONObject(1).getString("title"));
      assertEquals(0, result.getJSONArray("items").getJSONObject(0).getInt("replyCount"));
      assertEquals(0, result.getJSONArray("items").getJSONObject(1).getInt("replyCount"));
      
      
      // Check for recent (new)
      // We should get all the topics, with the newest one first (rather than last as with others)
      result = getPosts("new?numdays=2", Status.STATUS_OK);
      assertEquals(2, result.getInt("total"));
      assertEquals(2, result.getInt("itemCount"));
      assertEquals(2, result.getJSONArray("items").length());
      assertEquals("SiteTitle2", result.getJSONArray("items").getJSONObject(0).getString("title"));
      assertEquals("SiteTitle1", result.getJSONArray("items").getJSONObject(1).getString("title"));
      assertEquals(3, result.getJSONArray("items").getJSONObject(0).getInt("replyCount"));
      assertEquals(2, result.getJSONArray("items").getJSONObject(1).getInt("replyCount"));
      
      result = getPosts(FORUM_NODE, "new?numdays=2", Status.STATUS_OK);
      assertEquals(3, result.getInt("total"));
      assertEquals(3, result.getInt("itemCount"));
      assertEquals(3, result.getJSONArray("items").length());
      assertEquals("NodeTitle3", result.getJSONArray("items").getJSONObject(0).getString("title"));
      assertEquals("NodeTitle2", result.getJSONArray("items").getJSONObject(1).getString("title"));
      assertEquals("NodeTitle1", result.getJSONArray("items").getJSONObject(2).getString("title"));
      assertEquals(0, result.getJSONArray("items").getJSONObject(0).getInt("replyCount"));
      assertEquals(0, result.getJSONArray("items").getJSONObject(1).getInt("replyCount"));
      assertEquals(1, result.getJSONArray("items").getJSONObject(2).getInt("replyCount"));
      
      
      // Check for hot
      // Will only show topics with replies. Sorting is by replies, not date
      result = getPosts("hot", Status.STATUS_OK);
      assertEquals(2, result.getInt("total"));
      assertEquals(2, result.getInt("itemCount"));
      assertEquals(2, result.getJSONArray("items").length());
      assertEquals("SiteTitle2", result.getJSONArray("items").getJSONObject(0).getString("title"));
      assertEquals("SiteTitle1", result.getJSONArray("items").getJSONObject(1).getString("title"));
      assertEquals(3, result.getJSONArray("items").getJSONObject(0).getInt("replyCount"));
      assertEquals(2, result.getJSONArray("items").getJSONObject(1).getInt("replyCount"));
      
      result = getPosts(FORUM_NODE, "hot", Status.STATUS_OK);
      assertEquals(1, result.getInt("total"));
      assertEquals(1, result.getInt("itemCount"));
      assertEquals(1, result.getJSONArray("items").length());
      assertEquals("NodeTitle1", result.getJSONArray("items").getJSONObject(0).getString("title"));
      assertEquals(1, result.getJSONArray("items").getJSONObject(0).getInt("replyCount"));
      
      
      // Shift some of the posts into the past
      // (Update the created and published dates)
      pushCreatedDateBack(siteTopic1, 10);
      pushCreatedDateBack(siteReply1B, -2); // Make it newer
      
      pushCreatedDateBack(nodeTopic2, 10);
      pushCreatedDateBack(nodeTopic3, 4);
      pushCreatedDateBack(nodeReply1AAA, -1); // Make it newer
      
      
      // Re-check totals, only ordering changes
      result = getPosts(null, Status.STATUS_OK);
      assertEquals(2, result.getInt("total"));
      assertEquals(2, result.getInt("itemCount"));
      assertEquals(2, result.getJSONArray("items").length());
      assertEquals("SiteTitle1", result.getJSONArray("items").getJSONObject(1).getString("title"));
      assertEquals("SiteTitle2", result.getJSONArray("items").getJSONObject(0).getString("title"));
      assertEquals(2, result.getJSONArray("items").getJSONObject(1).getInt("replyCount"));
      assertEquals(3, result.getJSONArray("items").getJSONObject(0).getInt("replyCount"));
      
      result = getPosts(FORUM_NODE, null, Status.STATUS_OK);
      assertEquals(3, result.getInt("total"));
      assertEquals(3, result.getInt("itemCount"));
      assertEquals(3, result.getJSONArray("items").length());
      assertEquals("NodeTitle2", result.getJSONArray("items").getJSONObject(2).getString("title"));
      assertEquals("NodeTitle3", result.getJSONArray("items").getJSONObject(1).getString("title"));
      assertEquals("NodeTitle1", result.getJSONArray("items").getJSONObject(0).getString("title"));
      assertEquals(0, result.getJSONArray("items").getJSONObject(2).getInt("replyCount"));
      assertEquals(0, result.getJSONArray("items").getJSONObject(1).getInt("replyCount"));
      assertEquals(1, result.getJSONArray("items").getJSONObject(0).getInt("replyCount"));
      
      
      // Re-check recent, old ones vanish
      result = getPosts("new?numdays=2", Status.STATUS_OK);
      assertEquals(1, result.getInt("total"));
      assertEquals(1, result.getInt("itemCount"));
      assertEquals(1, result.getJSONArray("items").length());
      assertEquals("SiteTitle2", result.getJSONArray("items").getJSONObject(0).getString("title"));
      assertEquals(3, result.getJSONArray("items").getJSONObject(0).getInt("replyCount"));
      
      result = getPosts(FORUM_NODE, "new?numdays=6", Status.STATUS_OK);
      assertEquals(2, result.getInt("total"));
      assertEquals(2, result.getInt("itemCount"));
      assertEquals(2, result.getJSONArray("items").length());
      assertEquals("NodeTitle1", result.getJSONArray("items").getJSONObject(0).getString("title"));
      assertEquals("NodeTitle3", result.getJSONArray("items").getJSONObject(1).getString("title"));
      assertEquals(1, result.getJSONArray("items").getJSONObject(0).getInt("replyCount"));
      assertEquals(0, result.getJSONArray("items").getJSONObject(1).getInt("replyCount"));
      
      result = getPosts(FORUM_NODE, "new?numdays=2", Status.STATUS_OK);
      assertEquals(1, result.getInt("total"));
      assertEquals(1, result.getInt("itemCount"));
      assertEquals(1, result.getJSONArray("items").length());
      assertEquals("NodeTitle1", result.getJSONArray("items").getJSONObject(0).getString("title"));
      assertEquals(1, result.getJSONArray("items").getJSONObject(0).getInt("replyCount"));
      
      
      // Re-check "mine", no change except ordering
      result = getPosts("mine", Status.STATUS_OK);
      assertEquals(1, result.getInt("total"));
      assertEquals(1, result.getInt("itemCount"));
      assertEquals(1, result.getJSONArray("items").length());
      assertEquals("SiteTitle1", result.getJSONArray("items").getJSONObject(0).getString("title"));
      assertEquals(2, result.getJSONArray("items").getJSONObject(0).getInt("replyCount"));
      
      result = getPosts(FORUM_NODE, "mine", Status.STATUS_OK);
      assertEquals(2, result.getInt("total"));
      assertEquals(2, result.getInt("itemCount"));
      assertEquals(2, result.getJSONArray("items").length());
      assertEquals("NodeTitle2", result.getJSONArray("items").getJSONObject(0).getString("title"));
      assertEquals("NodeTitle3", result.getJSONArray("items").getJSONObject(1).getString("title"));
      assertEquals(0, result.getJSONArray("items").getJSONObject(0).getInt("replyCount"));
      assertEquals(0, result.getJSONArray("items").getJSONObject(1).getInt("replyCount"));
      
      
      // Re-check hot, some old ones vanish
      result = getPosts("hot", Status.STATUS_OK);
      assertEquals(2, result.getInt("total"));
      assertEquals(2, result.getInt("itemCount"));
      assertEquals(2, result.getJSONArray("items").length());
      assertEquals("SiteTitle2", result.getJSONArray("items").getJSONObject(0).getString("title"));
      assertEquals("SiteTitle1", result.getJSONArray("items").getJSONObject(1).getString("title"));
      assertEquals(3, result.getJSONArray("items").getJSONObject(0).getInt("replyCount"));
      assertEquals(2, result.getJSONArray("items").getJSONObject(1).getInt("replyCount"));
      
      result = getPosts(FORUM_NODE, "hot", Status.STATUS_OK);
      assertEquals(1, result.getInt("total"));
      assertEquals(1, result.getInt("itemCount"));
      assertEquals(1, result.getJSONArray("items").length());
      assertEquals("NodeTitle1", result.getJSONArray("items").getJSONObject(0).getString("title"));
      assertEquals(1, result.getJSONArray("items").getJSONObject(0).getInt("replyCount"));
      
      
      // Check paging
      result = getPosts("limit", Status.STATUS_OK);
      assertEquals(2, result.getInt("total"));
      assertEquals(1, result.getInt("itemCount"));
      assertEquals(1, result.getJSONArray("items").length());
      assertEquals("SiteTitle2", result.getJSONArray("items").getJSONObject(0).getString("title"));
      assertEquals(3, result.getJSONArray("items").getJSONObject(0).getInt("replyCount"));
      
      result = getPosts(FORUM_NODE, "limit", Status.STATUS_OK);
      assertEquals(3, result.getInt("total"));
      assertEquals(1, result.getInt("itemCount"));
      assertEquals(1, result.getJSONArray("items").length());
      assertEquals("NodeTitle1", result.getJSONArray("items").getJSONObject(0).getString("title"));
      assertEquals(1, result.getJSONArray("items").getJSONObject(0).getInt("replyCount"));
    }
    
    /**
     * https://issues.alfresco.com/jira/browse/ALF-17443 reports that site contributors are unable
     * to edit replies that they have made.
     */
    public void testContributorCanEditReply() throws Exception
    {
        authenticationComponent.setCurrentUser(USER_ONE);
        JSONObject post = createSitePost("Can contributors edit replies?", "The title says it all", Status.STATUS_OK);
        NodeRef postNodeRef = new NodeRef(post.getString("nodeRef"));

        authenticationComponent.setCurrentUser(USER_TWO);
        JSONObject reply = createReply(postNodeRef, "", "Let's see.", Status.STATUS_OK);
        NodeRef replyNodeRef = new NodeRef(reply.getString("nodeRef"));
        updateComment(replyNodeRef, "", "Yes I can", Status.STATUS_OK);
        
        authenticationComponent.setCurrentUser(USER_ONE);

        post = getPost(postNodeRef, Status.STATUS_OK);
        assertEquals("Can contributors edit replies?", post.getString("title"));
        assertEquals("The title says it all", post.getString("content"));
        assertEquals(1, post.getInt("replyCount"));
        
        JSONObject replies = getReplies(postNodeRef, Status.STATUS_OK);
        JSONArray items = replies.getJSONArray("items");
        assertEquals(1, items.length());
        
        reply = items.getJSONObject(0);
        assertEquals("Yes I can", reply.getString("content"));

    }
    
    /**
     * Test for <a href=https://issues.alfresco.com/jira/browse/MNT-11964>MNT-11964</a>
     * @throws Exception 
     */
    public void testCreateForumPermission() throws Exception
    {
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        String siteName = SITE_SHORT_NAME_DISCUSSION + GUID.generate();
        this.siteService.createSite("ForumSitePreset", siteName, "SiteTitle", "SiteDescription", SiteVisibility.PUBLIC);
        
        String userName = USER_ONE + GUID.generate();
        createUser(userName, SiteModel.SITE_COLLABORATOR, siteName);

        // Check permissions for admin
        checkForumPermissions(siteName);
        
        // Check permissions for user
        this.authenticationComponent.setCurrentUser(userName);
        checkForumPermissions(siteName);

        // Cleanup
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        this.siteService.deleteSite(siteName);
        
        // Create a new site as user
        this.authenticationComponent.setCurrentUser(userName);
        siteName = SITE_SHORT_NAME_DISCUSSION + GUID.generate();
        this.siteService.createSite("BlogSitePreset", siteName, "SiteTitle", "SiteDescription", SiteVisibility.PUBLIC);
        
        // Check permissions for user
        checkForumPermissions(siteName);
        
        // Check permissions for admin
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        checkForumPermissions(siteName);
        
        // Cleanup
        this.siteService.deleteSite(siteName);
        this.personService.deletePerson(userName);
    }
    
    private void checkForumPermissions(String siteName) throws Exception
    {
        String url = "/api/forum/site/" + siteName + "/" + COMPONENT_DISCUSSION + "/posts";
        Response response = sendRequest(new GetRequest(url), 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        
        assertTrue("The user sould have permission to create a new discussion.", result.getJSONObject("forumPermissions").getBoolean("create"));
    }
}
