/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.blogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Unit Test to test Blog Web Script API
 * 
 * TODO Add unit tests for the Blog Integration part
 * 
 * @author mruflin
 */
public class BlogServiceTest extends BaseWebScriptTest
{
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(BlogServiceTest.class);

    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    private SiteService siteService;
    
    private static final String USER_ONE = "UserOneSecondToo";
    private static final String USER_TWO = "UserTwoSecondToo";
    private static final String SITE_SHORT_NAME_BLOG = "BlogSiteShortNameTest";
    private static final String COMPONENT_BLOG = "blog";

    private static final String URL_BLOG_POST = "/api/blog/post/site/" + SITE_SHORT_NAME_BLOG + "/" + COMPONENT_BLOG + "/";
    private static final String URL_BLOG_POSTS = "/api/blog/site/" + SITE_SHORT_NAME_BLOG + "/" + COMPONENT_BLOG + "/posts";
    private static final String URL_MY_DRAFT_BLOG_POSTS = "/api/blog/site/" + SITE_SHORT_NAME_BLOG +
                                                          "/" + COMPONENT_BLOG + "/posts/mydrafts";
    private static final String URL_MY_PUBLISHED_BLOG_POSTS = "/api/blog/site/" + SITE_SHORT_NAME_BLOG +
                                                          "/" + COMPONENT_BLOG + "/posts/mypublished";


    private List<String> posts;
    private List<String> drafts;

    
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
        SiteInfo siteInfo = this.siteService.getSite(SITE_SHORT_NAME_BLOG);
        if (siteInfo == null)
        {
            this.siteService.createSite("BlogSitePreset", SITE_SHORT_NAME_BLOG, "BlogSiteTitle", "BlogSiteDescription", SiteVisibility.PUBLIC);
        }
        
        // Create users
        createUser(USER_ONE, SiteModel.SITE_COLLABORATOR);
        createUser(USER_TWO, SiteModel.SITE_COLLABORATOR);

        // Blank our lists used to track things the test creates
        posts = new ArrayList<String>(5);
        drafts = new ArrayList<String>(5);
        
        // Do tests as inviter user
        this.authenticationComponent.setCurrentUser(USER_ONE);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        // admin user required to delete things
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        // delete invite site
        siteService.deleteSite(SITE_SHORT_NAME_BLOG);
        
        // delete the users
        personService.deletePerson(USER_ONE);
        if (this.authenticationService.authenticationExists(USER_ONE))
        {
           this.authenticationService.deleteAuthentication(USER_ONE);
        }
        
        personService.deletePerson(USER_TWO);
        if (this.authenticationService.authenticationExists(USER_TWO))
        {
           this.authenticationService.deleteAuthentication(USER_TWO);
        }
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
        this.siteService.setMembership(SITE_SHORT_NAME_BLOG, userName, role);
    }
    
    
    // Test helper methods 
    
    private JSONObject getRequestObject(String title, String content, String[] tags, boolean isDraft)
    throws Exception
    {
        JSONObject post = new JSONObject();
        if (title != null)
        {
            post.put("title", title);
        }
        if (content != null)
        {
            post.put("content", content);
        }
        if (tags != null)
        {
            JSONArray arr = new JSONArray();
            for (String s : tags)
            {
                arr.put(s);
            }
            post.put("tags", arr);
        }
        post.put("draft", isDraft);
        return post;
    }
    
    private JSONObject createPost(String title, String content, String[] tags, boolean isDraft, int expectedStatus)
    throws Exception
    {
        JSONObject post = getRequestObject(title, content, tags, isDraft);
        Response response = sendRequest(new PostRequest(URL_BLOG_POSTS, post.toString(), "application/json"), expectedStatus);

        if (expectedStatus != 200)
        {
            return null;
        }
        
        //logger.debug(response.getContentAsString());
        JSONObject result = new JSONObject(response.getContentAsString());
        JSONObject item = result.getJSONObject("item");
        if (isDraft)
        {
            this.drafts.add(item.getString("name"));
        }
        else
        {
            this.posts.add(item.getString("name"));
        }
        return item;
    }
    
    private JSONObject updatePost(String name, String title, String content, String[] tags, boolean isDraft, int expectedStatus)
    throws Exception
    {
        JSONObject post = getRequestObject(title, content, tags, isDraft);
        Response response = sendRequest(new PutRequest(URL_BLOG_POST + name, post.toString(), "application/json"), expectedStatus);
        
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
        Response response = sendRequest(new GetRequest(URL_BLOG_POST + name), expectedStatus);
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
    
    private String getCommentsUrl(String nodeRef)
    {
        return "/api/node/" + nodeRef.replace("://", "/") + "/comments";
    }
    
    private String getCommentUrl(String nodeRef)
    {
        return "/api/comment/node/" + nodeRef.replace("://", "/");
    }
    
    private JSONObject createComment(String nodeRef, String title, String content, int expectedStatus)
    throws Exception
    {
        JSONObject comment = new JSONObject();
        comment.put("title", title);
        comment.put("content", content);
        Response response = sendRequest(new PostRequest(getCommentsUrl(nodeRef), comment.toString(), "application/json"), expectedStatus);

        if (expectedStatus != 200)
        {
            return null;
        }

        //logger.debug("Comment created: " + response.getContentAsString());
        JSONObject result = new JSONObject(response.getContentAsString());
        return result.getJSONObject("item");
    }
    
    private JSONObject updateComment(String nodeRef, String title, String content, int expectedStatus)
    throws Exception
    {
        JSONObject comment = new JSONObject();
        comment.put("title", title);
        comment.put("content", content);
        Response response = sendRequest(new PutRequest(getCommentUrl(nodeRef), comment.toString(), "application/json"), expectedStatus);

        if (expectedStatus != 200)
        {
            return null;
        }

        //logger.debug("Comment updated: " + response.getContentAsString());
        JSONObject result = new JSONObject(response.getContentAsString());
        return result.getJSONObject("item");
    }
    
    
    // Tests
    
    public void testCreateDraftPost() throws Exception
    {
        String title = "test";
        String content = "test";
        JSONObject item = createPost(title, content, null, true, 200);
        
        // check that the values
        assertEquals(title, item.get("title"));
        assertEquals(content, item.get("content"));
        assertEquals(true, item.get("isDraft"));
        
        // check that other user doesn't have access to the draft
        this.authenticationComponent.setCurrentUser(USER_TWO);
        getPost(item.getString("name"), 404);
        this.authenticationComponent.setCurrentUser(USER_ONE);
        
        // Now we'll GET my-drafts to ensure that the post is there.
        Response response = sendRequest(new GetRequest(URL_MY_DRAFT_BLOG_POSTS), 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        assertTrue("Wrong number of posts", result.length() > 0);
    }
    
    /**
     * @since 4.0
     */
    public void testCreateDraftPostWithTagsAndComment() throws Exception
    {
        String[] tags = new String[]{"foo", "bar"};
        String title = "test";
        String content = "test";
        JSONObject item = createPost(title, content, tags, true, 200);
        
        // check that the values
        assertEquals(title, item.get("title"));
        assertEquals(content, item.get("content"));
        assertEquals(true, item.get("isDraft"));
        JSONArray reportedTags = (JSONArray)item.get("tags");
        assertEquals("Tags size was wrong.", 2, reportedTags.length());
        List<String> recoveredTagsList = Arrays.asList(new String[]{reportedTags.getString(0), reportedTags.getString(1)});
        assertEquals("Tags were wrong.", Arrays.asList(tags), recoveredTagsList);
        
        // comment on the blog post.
        NodeRef blogPostNode = new NodeRef(item.getString("nodeRef"));
        // Currently (mid-Swift dev) there is no Java CommentService, so we have to post a comment via the REST API.
        String commentsPostUrl = "/api/node/" + blogPostNode.getStoreRef().getProtocol() +
                                 "/" + blogPostNode.getStoreRef().getIdentifier() + "/" +
                                 blogPostNode.getId() + "/comments";
        
        String jsonToPost = new JSONStringer().object()
                                                  .key("title").value("Commented blog title")
                                                  .key("content").value("Some content.")
                                              .endObject().toString();
                          
        Response response = sendRequest(new PostRequest(commentsPostUrl, jsonToPost, "application/json"), 200);
        
        // check that other user doesn't have access to the draft
        this.authenticationComponent.setCurrentUser(USER_TWO);
        getPost(item.getString("name"), 404);
        this.authenticationComponent.setCurrentUser(USER_ONE);
        
        // Now we'll GET my-drafts to ensure that the post is there.
        response = sendRequest(new GetRequest(URL_MY_DRAFT_BLOG_POSTS), 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        
        // Ensure it reports the tag correctly on GET.
        JSONArray items = result.getJSONArray("items");
        JSONArray tagsArray = items.getJSONObject(0).getJSONArray("tags");
        assertEquals("Wrong number of tags", 2, tagsArray.length());
        assertEquals("Tag wrong", tags[0], tagsArray.getString(0));
        assertEquals("Tag wrong", tags[1], tagsArray.getString(1));
        
        // Ensure the comment count is accurate
        assertEquals("Wrong comment count", 1, items.getJSONObject(0).getInt("commentCount"));
        
        // and that there is content at the commentsURL.
        String commentsUrl = "/api" + items.getJSONObject(0).getString("commentsUrl");
        response = sendRequest(new GetRequest(commentsUrl), 200);
        
        
        // Now get blog-post by tag.
        // 1. No such tag
        response = sendRequest(new GetRequest(URL_BLOG_POSTS + "?tag=NOSUCHTAG"), 200);
        result = new JSONObject(response.getContentAsString());
        
        assertEquals(0, result.getInt("total"));
        
        // tag created above
        response = sendRequest(new GetRequest(URL_BLOG_POSTS + "?tag=foo"), 200);
        result = new JSONObject(response.getContentAsString());
        
        assertEquals(1, result.getInt("total"));
        
        //TODO More assertions on recovered node.
    }
    
    public void testCreatePublishedPost() throws Exception
    {
        String title = "published";
        String content = "content";
        
        JSONObject item = createPost(title, content, null, false, 200);
        final String postName = item.getString("name");
        
        // check the values
        assertEquals(title, item.get("title"));
        assertEquals(content, item.get("content"));
        assertEquals(false, item.get("isDraft"));
        
        // check that user two has access to it as well
        this.authenticationComponent.setCurrentUser(USER_TWO);
        getPost(item.getString("name"), 200);
        this.authenticationComponent.setCurrentUser(USER_ONE);

        // Now we'll GET my-published to ensure that the post is there.
        Response response = sendRequest(new GetRequest(URL_MY_PUBLISHED_BLOG_POSTS), 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        
        // we should have posts.size + drafts.size together
        assertEquals(this.posts.size() + this.drafts.size(), result.getInt("total"));
        
        // Finally, we'll delete the blog-post to test the REST DELETE call.
        response = sendRequest(new DeleteRequest(URL_BLOG_POST + postName), 200);

    }
    
    public void testCreateEmptyPost() throws Exception
    {
        JSONObject item = createPost(null, null, null, false, 200);
        
        // check the values
        assertEquals("", item.get("title"));
        assertEquals("", item.get("content"));
        assertEquals(false, item.get("isDraft"));
        
        // check that user two has access to it as well
        this.authenticationComponent.setCurrentUser(USER_TWO);
        getPost(item.getString("name"), 200);
        this.authenticationComponent.setCurrentUser(USER_ONE);
    }
    
    public void testUpdated() throws Exception
    {
        JSONObject item = createPost("test", "test", null, false, 200);
        String name = item.getString("name");
        assertEquals(false, item.getBoolean("isUpdated"));
        
        item = updatePost(name, "new title", "new content", null, false, 200);
        assertEquals(true, item.getBoolean("isUpdated"));
        assertEquals("new title", item.getString("title"));
        assertEquals("new content", item.getString("content"));
    }
    
    public void testUpdateWithEmptyValues() throws Exception
    {
        JSONObject item = createPost("test", "test", null, false, 200);
        String name = item.getString("name");
        assertEquals(false, item.getBoolean("isUpdated"));
        
        item = updatePost(item.getString("name"), null, null, null, false, 200);
        assertEquals("", item.getString("title"));
        assertEquals("", item.getString("content"));
    }
    
    public void testPublishThroughUpdate() throws Exception
    {
        JSONObject item = createPost("test", "test", null, true, 200);
        String name = item.getString("name");
        assertEquals(true, item.getBoolean("isDraft"));
        
        // check that user two does not have access
        this.authenticationComponent.setCurrentUser(USER_TWO);
        getPost(name, 404);
        this.authenticationComponent.setCurrentUser(USER_ONE);
        
        item = updatePost(name, "new title", "new content", null, false, 200);
        assertEquals("new title", item.getString("title"));
        assertEquals("new content", item.getString("content"));
        assertEquals(false, item.getBoolean("isDraft"));
        
        // check that user two does have access
        this.authenticationComponent.setCurrentUser(USER_TWO);
        getPost(name, 200);
        this.authenticationComponent.setCurrentUser(USER_ONE);
    }

    public void testCannotDoUnpublish() throws Exception
    {
        JSONObject item = createPost("test", "test", null, false, 200);
        String name = item.getString("name");
        assertEquals(false, item.getBoolean("isDraft"));
        
        item = updatePost(name, "new title", "new content", null, true, 400); // should return bad request
    }
    
    public void testGetAll() throws Exception
    {
        String url = URL_BLOG_POSTS;
        Response response = sendRequest(new GetRequest(url), 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        JSONObject blog;
        
        // We shouldn't have any posts at this point
        assertEquals(0, this.posts.size());
        assertEquals(0, this.drafts.size());
        
        assertEquals(0, result.getInt("total"));
        assertEquals(0, result.getInt("startIndex"));
        assertEquals(0, result.getInt("itemCount"));
        assertEquals(0, result.getJSONArray("items").length());
        
        // Check that the permissions are correct
        JSONObject metadata = result.getJSONObject("metadata");
        JSONObject perms = metadata.getJSONObject("blogPermissions");
        assertEquals(false, metadata.getBoolean("externalBlogConfig"));
        assertEquals(false, perms.getBoolean("delete")); // No container yet
        assertEquals(true, perms.getBoolean("edit"));
        assertEquals(true, perms.getBoolean("create"));
        

        // Create a draft and a full post
        String TITLE_1 = "Published";
        String TITLE_2 = "Draft";
        String TITLE_3 = "Another Published";
        createPost(TITLE_1, "Stuff", null, false, Status.STATUS_OK);
        createPost(TITLE_2, "Draft Stuff", null, true, Status.STATUS_OK);
        
        // Check now
        response = sendRequest(new GetRequest(url), 200);
        result = new JSONObject(response.getContentAsString());
        assertEquals(2, result.getInt("total"));
        assertEquals(0, result.getInt("startIndex"));
        assertEquals(2, result.getInt("itemCount"));
        assertEquals(2, result.getJSONArray("items").length());

        // Check the core permissions
        metadata = result.getJSONObject("metadata");
        perms = metadata.getJSONObject("blogPermissions");
        assertEquals(false, metadata.getBoolean("externalBlogConfig"));
        assertEquals(true, perms.getBoolean("delete")); // On the container itself
        assertEquals(true, perms.getBoolean("edit"));
        assertEquals(true, perms.getBoolean("create"));
        
        // Check each one in detail, they'll come back Published
        //  then draft (newest first within that)
        blog = result.getJSONArray("items").getJSONObject(0);
        assertEquals(TITLE_1, blog.get("title"));
        assertEquals(false, blog.getBoolean("isDraft"));
        perms = blog.getJSONObject("permissions");
        assertEquals(true, perms.getBoolean("delete"));
        assertEquals(true, perms.getBoolean("edit"));
        
        blog = result.getJSONArray("items").getJSONObject(1);
        assertEquals(TITLE_2, blog.get("title"));
        assertEquals(true, blog.getBoolean("isDraft"));
        perms = blog.getJSONObject("permissions");
        assertEquals(true, perms.getBoolean("delete"));
        assertEquals(true, perms.getBoolean("edit"));
        
        
        // Add a third post
        createPost(TITLE_3, "Still Stuff", null, false, Status.STATUS_OK);
        
        response = sendRequest(new GetRequest(url), 200);
        result = new JSONObject(response.getContentAsString());
        assertEquals(3, result.getInt("total"));
        assertEquals(0, result.getInt("startIndex"));
        assertEquals(3, result.getInt("itemCount"));
        assertEquals(3, result.getJSONArray("items").length());

        // Published then draft, newest first
        blog = result.getJSONArray("items").getJSONObject(0);
        assertEquals(TITLE_3, blog.get("title"));
        blog = result.getJSONArray("items").getJSONObject(1);
        assertEquals(TITLE_1, blog.get("title"));
        blog = result.getJSONArray("items").getJSONObject(2);
        assertEquals(TITLE_2, blog.get("title"));

        
        // Ensure that paging behaves properly
        response = sendRequest(new GetRequest(url + "?pageSize=2&startIndex=0"), 200);
        result = new JSONObject(response.getContentAsString());
        assertEquals(3, result.getInt("total"));
        assertEquals(0, result.getInt("startIndex"));
        assertEquals(2, result.getInt("itemCount"));
        assertEquals(2, result.getJSONArray("items").length());

        assertEquals(TITLE_3, result.getJSONArray("items").getJSONObject(0).get("title"));
        assertEquals(TITLE_1, result.getJSONArray("items").getJSONObject(1).get("title"));
        
        
        response = sendRequest(new GetRequest(url + "?pageSize=2&startIndex=1"), 200);
        result = new JSONObject(response.getContentAsString());
        assertEquals(3, result.getInt("total"));
        assertEquals(1, result.getInt("startIndex"));
        assertEquals(2, result.getInt("itemCount"));
        assertEquals(2, result.getJSONArray("items").length());

        assertEquals(TITLE_1, result.getJSONArray("items").getJSONObject(0).get("title"));
        assertEquals(TITLE_2, result.getJSONArray("items").getJSONObject(1).get("title"));
        
        
        response = sendRequest(new GetRequest(url + "?pageSize=2&startIndex=2"), 200);
        result = new JSONObject(response.getContentAsString());
        assertEquals(3, result.getInt("total"));
        assertEquals(2, result.getInt("startIndex"));
        assertEquals(1, result.getInt("itemCount"));
        assertEquals(1, result.getJSONArray("items").length());

        assertEquals(TITLE_2, result.getJSONArray("items").getJSONObject(0).get("title"));

        
        // Switch user, check that permissions are correct
        // (Drafts won't be seen)
        this.authenticationComponent.setCurrentUser(USER_TWO);
        
        response = sendRequest(new GetRequest(url), 200);
        result = new JSONObject(response.getContentAsString());
        assertEquals(2, result.getInt("total"));
        assertEquals(0, result.getInt("startIndex"));
        assertEquals(2, result.getInt("itemCount"));
        
        assertEquals(2, result.getJSONArray("items").length());
        blog = result.getJSONArray("items").getJSONObject(0);
        assertEquals(TITLE_3, blog.get("title"));
        assertEquals(false, blog.getBoolean("isDraft"));
        perms = blog.getJSONObject("permissions");
        assertEquals(false, perms.getBoolean("delete"));
        assertEquals(true, perms.getBoolean("edit"));
        
        blog = result.getJSONArray("items").getJSONObject(1);
        assertEquals(TITLE_1, blog.get("title"));
        assertEquals(false, blog.getBoolean("isDraft"));
        perms = blog.getJSONObject("permissions");
        assertEquals(false, perms.getBoolean("delete"));
        assertEquals(true, perms.getBoolean("edit"));
    }
    
    public void testGetNew() throws Exception
    {
        String url = URL_BLOG_POSTS + "/new";
        Response response = sendRequest(new GetRequest(url), 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        
        // we should have posts.size
        assertEquals(this.posts.size(), result.getInt("total"));
    }
    
    public void testGetDrafts() throws Exception
    {
        String url = URL_BLOG_POSTS + "/mydrafts";
        Response response = sendRequest(new GetRequest(URL_BLOG_POSTS), 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        
        // we should have drafts.size resultss
        assertEquals(this.drafts.size(), result.getInt("total"));
        
        // the second user should have zero
        this.authenticationComponent.setCurrentUser(USER_TWO);
        response = sendRequest(new GetRequest(url), 200);
        result = new JSONObject(response.getContentAsString());
        assertEquals(0, result.getInt("total"));
        this.authenticationComponent.setCurrentUser(USER_ONE);

    }
    
    public void testMyPublished() throws Exception
    {
        String url = URL_BLOG_POSTS + "/mypublished";
        Response response = sendRequest(new GetRequest(url), 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        
        // we should have posts.size results
        assertEquals(this.drafts.size(), result.getInt("total"));
        
        // the second user should have zero
        this.authenticationComponent.setCurrentUser(USER_TWO);
        response = sendRequest(new GetRequest(url), 200);
        result = new JSONObject(response.getContentAsString());
        assertEquals(0, result.getInt("total"));
        this.authenticationComponent.setCurrentUser(USER_ONE);
    }

    public void testComments() throws Exception
    {
        JSONObject item = createPost("test", "test", null, false, 200);
        String name = item.getString("name");
        String nodeRef = item.getString("nodeRef");
        
        JSONObject commentOne = createComment(nodeRef, "comment", "content", 200);
        JSONObject commentTwo = createComment(nodeRef, "comment", "content", 200);
        
        // fetch the comments
        Response response = sendRequest(new GetRequest(getCommentsUrl(nodeRef)), 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        assertEquals(2, result.getInt("total"));
        
        // add another one
        JSONObject commentThree = createComment(nodeRef, "comment", "content", 200);
        
        response = sendRequest(new GetRequest(getCommentsUrl(nodeRef)), 200);
        result = new JSONObject(response.getContentAsString());
        assertEquals(3, result.getInt("total"));
        
        // delete the last comment
        response = sendRequest(new DeleteRequest(getCommentUrl(commentThree.getString("nodeRef"))), 200);
        
        response = sendRequest(new GetRequest(getCommentsUrl(nodeRef)), 200);
        result = new JSONObject(response.getContentAsString());
        assertEquals(2, result.getInt("total"));
        
        JSONObject commentTwoUpdated = updateComment(commentTwo.getString("nodeRef"), "new title", "new content", 200);
        assertEquals("new title", commentTwoUpdated.getString("title"));
        assertEquals("new content", commentTwoUpdated.getString("content"));
    }
 
    /**
     * Does some stress tests.
     * 
     * Currently observed errors:
     * 1. [repo.action.AsynchronousActionExecutionQueueImpl] Failed to execute asynchronous action: Action[ id=485211db-f117-4976-9530-ab861a19f563, node=null ]
     * org.alfresco.repo.security.permissions.AccessDeniedException: Access Denied.  You do not have the appropriate permissions to perform this operation. 
     * 
     * 2. JSONException, but with root cause being
     *   get(assocs) failed on instance of org.alfresco.repo.template.TemplateNode
     *   The problematic instruction:
     *   ----------
     *   ==> if person.assocs["cm:avatar"]?? [on line 4, column 7 in org/alfresco/repository/blogs/blogpost.lib.ftl]
     *   
     * @throws Exception
     */
    public void _testTagsStressTest() throws Exception
    {
        final List<Exception> exceptions = Collections.synchronizedList(new ArrayList<Exception>());
        List<Thread> threads = new ArrayList<Thread>();

        System.err.println("Creating and starting threads...");
        for (int x=0; x < 3; x++)
        {
            Thread t = new Thread(new Runnable() 
            {
                public void run() 
                {
                    // set the correct user
                    authenticationComponent.setCurrentUser(USER_ONE);

                    // now do some requests
                    try 
                    {
                        for (int y=0; y < 3; y++)
                        {
                            off_testPostTags();
                            off_testClearTags();
                        }
                        System.err.println("------------- SUCCEEDED ---------------");
                    } catch (Exception e)
                    {
                        System.err.println("------------- ERROR ---------------");
                        exceptions.add(e);
                        e.printStackTrace();
                        return;
                    }
            }});
            
            threads.add(t);
            t.start();
        } 
        /*for (Thread t : threads)
        {
            t.start();
        }*/
        
        for (Thread t : threads)
        {
            t.join();
        }
        
        System.err.println("------------- STACK TRACES ---------------");
        for (Exception e : exceptions)
        {
            e.printStackTrace();
        }
        System.err.println("------------- STACK TRACES END ---------------");
        if (exceptions.size() > 0)
        {
            throw exceptions.get(0);
        }
    }
    
    public void off_testPostTags() throws Exception
    {
        String[] tags = { "first", "test" };
        JSONObject item = createPost("tagtest", "tagtest", tags, false, 200);
        assertEquals(2, item.getJSONArray("tags").length());
        assertEquals("first", item.getJSONArray("tags").get(0));
        assertEquals("test", item.getJSONArray("tags").get(1));
        
        item = updatePost(item.getString("name"), null, null, new String[] { "First", "Test", "Second" }, false, 200);
        assertEquals(3, item.getJSONArray("tags").length());
        assertEquals("first", item.getJSONArray("tags").get(0));
        assertEquals("test", item.getJSONArray("tags").get(1));
        assertEquals("second", item.getJSONArray("tags").get(2));
    }
    
    public void off_testClearTags() throws Exception
    {
        String[] tags = { "abc", "def"};
        JSONObject item = createPost("tagtest", "tagtest", tags, false, 200);
        assertEquals(2, item.getJSONArray("tags").length());
        
        item = updatePost(item.getString("name"), null, null, new String[0], false, 200);
        assertEquals(0, item.getJSONArray("tags").length());
    }

}