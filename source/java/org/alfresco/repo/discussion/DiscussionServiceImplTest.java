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
package org.alfresco.repo.discussion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.discussion.DiscussionService;
import org.alfresco.service.cmr.discussion.PostInfo;
import org.alfresco.service.cmr.discussion.TopicInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Test cases for {@link DiscussionServiceImpl}.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class DiscussionServiceImplTest
{
    private static final String TEST_SITE_PREFIX = "DiscussionsSiteTest";
    private static final long ONE_DAY_MS = 24*60*60*1000;

    private static final ApplicationContext testContext = ApplicationContextHelper.getApplicationContext();
    
    // injected services
    private static MutableAuthenticationService AUTHENTICATION_SERVICE;
    private static BehaviourFilter              BEHAVIOUR_FILTER;
    private static DictionaryService            DICTIONARY_SERVICE;
    private static DiscussionService            DISCUSSION_SERVICE;
    private static NodeService                  NODE_SERVICE;
    private static NodeService                  PUBLIC_NODE_SERVICE;
    private static PersonService                PERSON_SERVICE;
    private static RetryingTransactionHelper    TRANSACTION_HELPER;
    private static TransactionService           TRANSACTION_SERVICE;
    private static PermissionService            PERMISSION_SERVICE;
    private static SiteService                  SITE_SERVICE;
    private static TaggingService               TAGGING_SERVICE;
    
    private static final String TEST_USER = DiscussionServiceImplTest.class.getSimpleName() + "_testuser";
    private static final String ADMIN_USER = AuthenticationUtil.getAdminUserName();

    private static SiteInfo DISCUSSION_SITE;
    private static SiteInfo ALTERNATE_DISCUSSION_SITE;
    private static NodeRef FORUM_NODE;
    
    /**
     * Temporary test nodes (created during a test method) that need deletion after the test method.
     */
    private List<NodeRef> testNodesToTidy = new ArrayList<NodeRef>();
    /**
     * Temporary test nodes (created BeforeClass) that need deletion after this test class.
     */
    private static List<NodeRef> CLASS_TEST_NODES_TO_TIDY = new ArrayList<NodeRef>();

    @BeforeClass public static void initTestsContext() throws Exception
    {
        AUTHENTICATION_SERVICE = (MutableAuthenticationService)testContext.getBean("authenticationService");
        BEHAVIOUR_FILTER       = (BehaviourFilter)testContext.getBean("policyBehaviourFilter");
        DISCUSSION_SERVICE     = (DiscussionService)testContext.getBean("DiscussionService");
        DICTIONARY_SERVICE     = (DictionaryService)testContext.getBean("dictionaryService");
        NODE_SERVICE           = (NodeService)testContext.getBean("nodeService");
        PUBLIC_NODE_SERVICE    = (NodeService)testContext.getBean("NodeService");
        PERSON_SERVICE         = (PersonService)testContext.getBean("personService");
        TRANSACTION_HELPER     = (RetryingTransactionHelper)testContext.getBean("retryingTransactionHelper");
        TRANSACTION_SERVICE    = (TransactionService)testContext.getBean("TransactionService");
        PERMISSION_SERVICE     = (PermissionService)testContext.getBean("permissionService");
        SITE_SERVICE           = (SiteService)testContext.getBean("siteService");
        TAGGING_SERVICE        = (TaggingService)testContext.getBean("TaggingService");

        // Do the setup as admin
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
        createUser(TEST_USER);
        
        // We need to create the test site as the test user so that they can contribute content to it in tests below.
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
        createTestSites();
    }
    
    @Test public void createNewTopic() throws Exception
    {
       TopicInfo siteTopic;
       TopicInfo nodeTopic;
       
       // Nothing to start with
       PagingResults<TopicInfo> results = 
          DISCUSSION_SERVICE.listTopics(DISCUSSION_SITE.getShortName(), new PagingRequest(10));
       assertEquals(0, results.getPage().size());
       
       results = DISCUSSION_SERVICE.listTopics(FORUM_NODE, new PagingRequest(10));
       assertEquals(0, results.getPage().size());

       
       // Get with an arbitrary name gives nothing
       siteTopic = DISCUSSION_SERVICE.getTopic(DISCUSSION_SITE.getShortName(), "madeUp");
       assertEquals(null, siteTopic); 
       
       siteTopic = DISCUSSION_SERVICE.getTopic(DISCUSSION_SITE.getShortName(), "madeUp2");
       assertEquals(null, siteTopic);
       
       siteTopic = DISCUSSION_SERVICE.getTopic(FORUM_NODE, "madeUp");
       assertEquals(null, siteTopic); 
       
       siteTopic = DISCUSSION_SERVICE.getTopic(FORUM_NODE, "madeUp2");
       assertEquals(null, siteTopic); 
       
       
       // Create a topic on the site
       siteTopic = DISCUSSION_SERVICE.createTopic(DISCUSSION_SITE.getShortName(), "Site Title");
       
       
       // Ensure it got a noderef, and the correct site
       assertNotNull(siteTopic.getNodeRef());
       assertNotNull(siteTopic.getSystemName());
       
       NodeRef container = NODE_SERVICE.getPrimaryParent(siteTopic.getNodeRef()).getParentRef();
       NodeRef site = NODE_SERVICE.getPrimaryParent(container).getParentRef();
       assertEquals(DISCUSSION_SITE.getNodeRef(), site);
       
       
       // Check the details on the object
       assertEquals("Site Title", siteTopic.getTitle());
       assertEquals(ADMIN_USER,   siteTopic.getCreator());
       assertEquals(0, siteTopic.getTags().size());
       
       
       // Fetch it, and check the details
       siteTopic = DISCUSSION_SERVICE.getTopic(DISCUSSION_SITE.getShortName(), siteTopic.getSystemName());
       assertEquals("Site Title", siteTopic.getTitle());
       assertEquals(ADMIN_USER,   siteTopic.getCreator());
       assertEquals(0, siteTopic.getTags().size());
       
       
       // Now create one with on the forum
       nodeTopic = DISCUSSION_SERVICE.createTopic(FORUM_NODE, "Node Title");
       assertNotNull(nodeTopic.getNodeRef());
       assertNotNull(nodeTopic.getSystemName());
       
       // Check it went in the right place
       assertEquals(FORUM_NODE, NODE_SERVICE.getPrimaryParent(nodeTopic.getNodeRef()).getParentRef());
       
       // Check the details
       assertEquals("Node Title", nodeTopic.getTitle());
       assertEquals(ADMIN_USER,   nodeTopic.getCreator());
       assertEquals(0, siteTopic.getTags().size());
       
       // Fetch and re-check
       nodeTopic = DISCUSSION_SERVICE.getTopic(FORUM_NODE, nodeTopic.getSystemName());
       assertEquals("Node Title", nodeTopic.getTitle());
       assertEquals(ADMIN_USER,   nodeTopic.getCreator());
       assertEquals(0, siteTopic.getTags().size());
       
       
       // Mark them as done with
       testNodesToTidy.add(siteTopic.getNodeRef());
       testNodesToTidy.add(nodeTopic.getNodeRef());
    }
    
    @Test public void createNewTopicAndPostAndReply() throws Exception
    {
       TopicInfo siteTopic;
       TopicInfo nodeTopic;
       PostInfo post;
       PostInfo reply1;
       PostInfo reply2;
       Pair<TopicInfo,PostInfo> objects;
       
       // Nothing to start with
       PagingResults<TopicInfo> results = 
          DISCUSSION_SERVICE.listTopics(DISCUSSION_SITE.getShortName(), new PagingRequest(10));
       assertEquals(0, results.getPage().size());
       
       results = DISCUSSION_SERVICE.listTopics(FORUM_NODE, new PagingRequest(10));
       assertEquals(0, results.getPage().size());

       
       // Create two topics, one node and one site based
       siteTopic = DISCUSSION_SERVICE.createTopic(DISCUSSION_SITE.getShortName(), "Site Title");
       nodeTopic = DISCUSSION_SERVICE.createTopic(FORUM_NODE, "Node Title");
       
       testNodesToTidy.add(siteTopic.getNodeRef());
       testNodesToTidy.add(nodeTopic.getNodeRef());

       
       // Check these actions in turn
       for(TopicInfo topic : new TopicInfo[] {siteTopic, nodeTopic})
       {
          AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
          
          // There are no posts to start with
          PagingResults<PostInfo> posts =
             DISCUSSION_SERVICE.listPosts(topic, new PagingRequest(10));
          assertEquals(0, posts.getPage().size());
          
          
          // The topic has no primary post
          assertEquals(null, DISCUSSION_SERVICE.getPrimaryPost(topic));
          
          
          // Get with an arbitrary name gives nothing
          post = DISCUSSION_SERVICE.getPost(topic, "madeUp");
          assertEquals(null, post);
          post = DISCUSSION_SERVICE.getPost(topic, "madeUp2");
          assertEquals(null, post);
          
          
          // Create the first post
          String contents = "This Is Some Content";
          post = DISCUSSION_SERVICE.createPost(topic, contents);
          
          // Ensure it got a NodeRef, a Name and the Topic
          assertNotNull(post.getNodeRef());
          assertNotNull(post.getSystemName());
          assertEquals(topic, post.getTopic());
          
          // Ensure it shares a name with the topic
          assertEquals(topic.getSystemName(), post.getSystemName());
          
          // As this is the primary post, it'll share the topic title
          assertEquals(topic.getTitle(), post.getTitle());
          
          // It will have contents and a creator
          assertEquals(contents, post.getContents());
          assertEquals(ADMIN_USER, post.getCreator());
          
          
          // Fetch and check
          post = DISCUSSION_SERVICE.getPost(topic, post.getSystemName());
          assertNotNull(post.getNodeRef());
          assertNotNull(post.getSystemName());
          assertEquals(topic, post.getTopic());
          assertEquals(topic.getTitle(), post.getTitle());
          assertEquals(contents, post.getContents());
          assertEquals(ADMIN_USER, post.getCreator());
          
          
          // Topic will now have a primary post
          assertNotNull(DISCUSSION_SERVICE.getPrimaryPost(topic));
          assertEquals(post.getNodeRef(), DISCUSSION_SERVICE.getPrimaryPost(topic).getNodeRef());

          
          // Topic will now have one post listed
          posts = DISCUSSION_SERVICE.listPosts(topic, new PagingRequest(10));
          assertEquals(1, posts.getPage().size());
          
          
          // Add a reply
          String reply1Contents = "Reply Contents";
          reply1 = DISCUSSION_SERVICE.createReply(post, reply1Contents);
          assertNotNull(reply1.getNodeRef());
          assertNotNull(reply1.getSystemName());
          assertEquals(topic, reply1.getTopic());
          assertEquals(null,  reply1.getTitle()); // No title by default for replies
          assertEquals(reply1Contents, reply1.getContents());
          assertEquals(ADMIN_USER, reply1.getCreator());
          
          // Fetch and check
          reply1 = DISCUSSION_SERVICE.getPost(topic, reply1.getSystemName());
          assertNotNull(reply1.getNodeRef());
          assertNotNull(reply1.getSystemName());
          assertEquals(topic, reply1.getTopic());
          assertEquals(null,  reply1.getTitle()); // No title by default for replies
          assertEquals(reply1Contents, reply1.getContents());
          assertEquals(ADMIN_USER, reply1.getCreator());
          
          
          // Create another reply, as a different user
          AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
          
          String reply2Contents = "2Reply 2Contents";
          reply2 = DISCUSSION_SERVICE.createReply(post, reply2Contents);
          assertNotNull(reply2.getNodeRef());
          assertNotNull(reply2.getSystemName());
          assertEquals(topic, reply2.getTopic());
          assertEquals(null,  reply2.getTitle()); // No title by default for replies
          assertEquals(reply2Contents, reply2.getContents());
          assertEquals(TEST_USER, reply2.getCreator());
          
          // Fetch and check
          reply2 = DISCUSSION_SERVICE.getPost(topic, reply2.getSystemName());
          assertNotNull(reply2.getNodeRef());
          assertNotNull(reply2.getSystemName());
          assertEquals(topic, reply2.getTopic());
          assertEquals(null,  reply2.getTitle()); // No title by default for replies
          assertEquals(reply2Contents, reply2.getContents());
          assertEquals(TEST_USER, reply2.getCreator());
          
          
          // Ensure that by noderef, we get the correct things
          objects = DISCUSSION_SERVICE.getForNodeRef(FORUM_NODE);
          assertEquals(null, objects);
          
          objects = DISCUSSION_SERVICE.getForNodeRef(topic.getNodeRef());
          assertNotNull(objects);
          assertEquals(topic.getNodeRef(), objects.getFirst().getNodeRef());
          assertEquals(null, objects.getSecond());
          
          objects = DISCUSSION_SERVICE.getForNodeRef(post.getNodeRef());
          assertNotNull(objects);
          assertEquals(topic.getNodeRef(), objects.getFirst().getNodeRef());
          assertEquals(post.getNodeRef(), objects.getSecond().getNodeRef());
          
          objects = DISCUSSION_SERVICE.getForNodeRef(reply1.getNodeRef());
          assertNotNull(objects);
          assertEquals(topic.getNodeRef(), objects.getFirst().getNodeRef());
          assertEquals(reply1.getNodeRef(), objects.getSecond().getNodeRef());

          
          // Check the overall count now
          posts = DISCUSSION_SERVICE.listPosts(topic, new PagingRequest(10));
          assertEquals(3, posts.getPage().size());
       }
    }
    
    @Test public void createUpdateDeleteEntries() throws Exception
    {
       TopicInfo siteTopic;
       TopicInfo nodeTopic;
       PostInfo post;
       PostInfo reply;
       
       // Run as the test user instead
       AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
       
       
       // Create two topics
       siteTopic = DISCUSSION_SERVICE.createTopic(DISCUSSION_SITE.getShortName(), "Site Title");
       nodeTopic = DISCUSSION_SERVICE.createTopic(FORUM_NODE, "Node Title");
       testNodesToTidy.add(siteTopic.getNodeRef());
       testNodesToTidy.add(nodeTopic.getNodeRef());
       
       
       // Check them
       assertEquals("Site Title", siteTopic.getTitle());
       assertEquals(TEST_USER,    siteTopic.getCreator());
       assertEquals(0,            siteTopic.getTags().size());
       
       assertEquals("Node Title", nodeTopic.getTitle());
       assertEquals(TEST_USER,    nodeTopic.getCreator());
       assertEquals(0,            nodeTopic.getTags().size());
       
       
       // Change them
       siteTopic.setTitle("Site Changed");
       nodeTopic.setTitle("Node Changed");
       
       siteTopic = DISCUSSION_SERVICE.updateTopic(siteTopic);
       nodeTopic = DISCUSSION_SERVICE.updateTopic(nodeTopic);
       
       
       // Fetch, and check
       siteTopic = DISCUSSION_SERVICE.getTopic(DISCUSSION_SITE.getShortName(), siteTopic.getSystemName());
       nodeTopic = DISCUSSION_SERVICE.getTopic(FORUM_NODE, nodeTopic.getSystemName());
       
       assertEquals("Site Changed", siteTopic.getTitle());
       assertEquals(TEST_USER,      siteTopic.getCreator());
       assertEquals(0,              siteTopic.getTags().size());
       
       assertEquals("Node Changed", nodeTopic.getTitle());
       assertEquals(TEST_USER,      nodeTopic.getCreator());
       assertEquals(0,              nodeTopic.getTags().size());
       
       
       // For each, create and edit some posts
       for(TopicInfo topic : new TopicInfo[] {siteTopic, nodeTopic})
       {
          // Create a post and a reply
          String contents = "This Is Some Content";
          post = DISCUSSION_SERVICE.createPost(topic, contents);
          
          String replyContents = "Reply Contents";
          reply = DISCUSSION_SERVICE.createReply(post, replyContents);
          
          
          // Check them
          assertEquals(topic.getTitle(), post.getTitle());
          assertEquals(contents, post.getContents());
          assertEquals(null, post.getUpdatedAt());
          
          assertEquals(null, reply.getTitle());
          assertEquals(replyContents, reply.getContents());
          assertEquals(null, reply.getUpdatedAt());

          
          // Fetch and re-check
          post = DISCUSSION_SERVICE.getPost(topic, post.getSystemName());
          reply = DISCUSSION_SERVICE.getPost(topic, reply.getSystemName());
          
          assertEquals(topic.getTitle(), post.getTitle());
          assertEquals(contents, post.getContents());
          assertEquals(null, post.getUpdatedAt());
          
          assertEquals(null, reply.getTitle());
          assertEquals(replyContents, reply.getContents());
          assertEquals(null, reply.getUpdatedAt());
          
          
          // Edit them
          String pTitle = "Title on the Post";
          String rTitle = "Title on the Reply";
          String pContents = "Changed! Changed!";
          String rContents = "Reply was changed...";
          post.setTitle(pTitle);
          post.setContents(pContents);
          reply.setTitle(rTitle);
          reply.setContents(rContents);
          
          post = DISCUSSION_SERVICE.updatePost(post);
          reply = DISCUSSION_SERVICE.updatePost(reply);
          
          assertEquals(pTitle, post.getTitle());
          assertEquals(pContents, post.getContents());
          
          assertEquals(rTitle, reply.getTitle());
          assertEquals(rContents, reply.getContents());

          // Check that the modified and updated dates were set
          assertNotNull(post.getUpdatedAt());
          assertNotNull(post.getModifiedAt());
          assertNotNull(reply.getUpdatedAt());
          assertNotNull(reply.getModifiedAt());
          
          
          // Check the changes were taken
          post = DISCUSSION_SERVICE.getPost(topic, post.getSystemName());
          reply = DISCUSSION_SERVICE.getPost(topic, reply.getSystemName());
          
          assertEquals(pTitle, post.getTitle());
          assertEquals(pContents, post.getContents());
          
          assertEquals(rTitle, reply.getTitle());
          assertEquals(rContents, reply.getContents());

          // Check that the modified and updated dates were set
          assertNotNull(post.getUpdatedAt());
          assertNotNull(post.getModifiedAt());
          assertNotNull(reply.getUpdatedAt());
          assertNotNull(reply.getModifiedAt());

          
          // Remove the title from the topic manually
          String oldTitle = topic.getTitle(); 
          PUBLIC_NODE_SERVICE.setProperty(topic.getNodeRef(), ContentModel.PROP_TITLE, null);
          
          // Check that we pick it up from the primary post instead
          // (That's the pre-swift Share fallback case)
          assertEquals(oldTitle, topic.getTitle());
          topic = DISCUSSION_SERVICE.getTopic(topic.getContainerNodeRef(), topic.getSystemName());
          assertEquals(pTitle, topic.getTitle());
          
          
          // Delete the reply and the post
          DISCUSSION_SERVICE.deletePost(reply);
          DISCUSSION_SERVICE.deletePost(post);
          
          // Check they went
          post = DISCUSSION_SERVICE.getPost(topic, post.getSystemName());
          reply = DISCUSSION_SERVICE.getPost(topic, reply.getSystemName());
          
          assertEquals(null, post);
          assertEquals(null, reply);
       }
       
       // Delete the topics
       DISCUSSION_SERVICE.deleteTopic(siteTopic);
       DISCUSSION_SERVICE.deleteTopic(nodeTopic);
       
       // Check they went
       siteTopic = DISCUSSION_SERVICE.getTopic(DISCUSSION_SITE.getShortName(), siteTopic.getSystemName());
       nodeTopic = DISCUSSION_SERVICE.getTopic(FORUM_NODE, nodeTopic.getSystemName());
       assertEquals(null, siteTopic);
       assertEquals(null, nodeTopic);
    }
    
    /**
     * Tests listing of topics in a site/node, and posts in a topic
     */
    @Test public void basicListings() throws Exception
    {
       PagingResults<TopicInfo> topics;
       PagingResults<PostInfo> posts;
       
       
       // To start with, there will be no topics
       topics = DISCUSSION_SERVICE.listTopics(DISCUSSION_SITE.getShortName(), new PagingRequest(10));
       assertEquals(0, topics.getPage().size());
       
       topics = DISCUSSION_SERVICE.listTopics(FORUM_NODE, new PagingRequest(10));
       assertEquals(0, topics.getPage().size());

       
       // Create several
       TopicInfo siteT1 = DISCUSSION_SERVICE.createTopic(DISCUSSION_SITE.getShortName(), "ST1");
       TopicInfo siteT2 = DISCUSSION_SERVICE.createTopic(DISCUSSION_SITE.getShortName(), "ST2");
       TopicInfo nodeT1 = DISCUSSION_SERVICE.createTopic(FORUM_NODE, "NT1");
       TopicInfo nodeT2 = DISCUSSION_SERVICE.createTopic(FORUM_NODE, "NT2");
       TopicInfo nodeT3 = DISCUSSION_SERVICE.createTopic(FORUM_NODE, "NT3");
       testNodesToTidy.add(siteT1.getNodeRef());
       testNodesToTidy.add(siteT2.getNodeRef());
       testNodesToTidy.add(nodeT1.getNodeRef());
       testNodesToTidy.add(nodeT2.getNodeRef());
       testNodesToTidy.add(nodeT3.getNodeRef());
       
       
       // Check now, will order by creation date
       topics = DISCUSSION_SERVICE.listTopics(DISCUSSION_SITE.getShortName(), new PagingRequest(10));
       assertEquals(2, topics.getPage().size());
       assertEquals("ST1", topics.getPage().get(0).getTitle());
       assertEquals("ST2", topics.getPage().get(1).getTitle());
       
       topics = DISCUSSION_SERVICE.listTopics(FORUM_NODE, new PagingRequest(10));
       assertEquals(3, topics.getPage().size());
       assertEquals("NT1", topics.getPage().get(0).getTitle());
       assertEquals("NT2", topics.getPage().get(1).getTitle());
       assertEquals("NT3", topics.getPage().get(2).getTitle());
       
       
       // Alter the creation date on a couple, see the ordering change
       pushAuditableDatesBack(siteT2, 2, 2);
       pushAuditableDatesBack(nodeT3, 3, 3);
       pushAuditableDatesBack(nodeT1, 1, 1);
       
       topics = DISCUSSION_SERVICE.listTopics(DISCUSSION_SITE.getShortName(), new PagingRequest(10));
       assertEquals(2, topics.getPage().size());
       assertEquals("ST2", topics.getPage().get(0).getTitle());
       assertEquals("ST1", topics.getPage().get(1).getTitle());
       
       topics = DISCUSSION_SERVICE.listTopics(FORUM_NODE, new PagingRequest(10));
       assertEquals(3, topics.getPage().size());
       assertEquals("NT3", topics.getPage().get(0).getTitle());
       assertEquals("NT1", topics.getPage().get(1).getTitle());
       assertEquals("NT2", topics.getPage().get(2).getTitle());
       
       
       // Now create a couple of check posts
       PostInfo siteCP1 = DISCUSSION_SERVICE.createPost(siteT2, "Check");
       PostInfo nodeCP1 = DISCUSSION_SERVICE.createPost(nodeT2, "Check");
       testNodesToTidy.add(siteCP1.getNodeRef());
       testNodesToTidy.add(nodeCP1.getNodeRef());
       
       
       // For both site and node based topics, check adding and
       //  removing posts correctly affects counts
       for(TopicInfo topic : new TopicInfo[] {siteT1, nodeT1})
       {
          // None to start with
          posts = DISCUSSION_SERVICE.listPosts(topic, new PagingRequest(10));
          assertEquals(0, posts.getPage().size());
          
          // Create one post
          PostInfo post = DISCUSSION_SERVICE.createPost(topic, "Post");
          assertEquals(post.getNodeRef(), DISCUSSION_SERVICE.getPrimaryPost(topic).getNodeRef());
          
          // Check the count
          posts = DISCUSSION_SERVICE.listPosts(topic, new PagingRequest(10));
          assertEquals(1, posts.getPage().size());
          assertEquals("Post", posts.getPage().get(0).getContents());
          
          // Add two replies
          PostInfo reply1 = DISCUSSION_SERVICE.createReply(post, "R1");
          PostInfo reply2 = DISCUSSION_SERVICE.createReply(post, "R2");
          
          // Check
          posts = DISCUSSION_SERVICE.listPosts(topic, new PagingRequest(10));
          assertEquals(3, posts.getPage().size());
          assertEquals("Post", posts.getPage().get(0).getContents());
          assertEquals("R1", posts.getPage().get(1).getContents());
          assertEquals("R2", posts.getPage().get(2).getContents());
          
          
          // Alter the date of one, order changes
          pushAuditableDatesBack(reply1, -1, -1);
          
          posts = DISCUSSION_SERVICE.listPosts(topic, new PagingRequest(10));
          assertEquals(3, posts.getPage().size());
          assertEquals("Post", posts.getPage().get(0).getContents());
          assertEquals("R2", posts.getPage().get(1).getContents());
          assertEquals("R1", posts.getPage().get(2).getContents());

          
          // Delete one reply
          DISCUSSION_SERVICE.deletePost(reply1);
          
          posts = DISCUSSION_SERVICE.listPosts(topic, new PagingRequest(10));
          assertEquals(2, posts.getPage().size());
          assertEquals("Post", posts.getPage().get(0).getContents());
          assertEquals("R2", posts.getPage().get(1).getContents());
          
          
          // Delete the main post
          DISCUSSION_SERVICE.deletePost(post);
          
          posts = DISCUSSION_SERVICE.listPosts(topic, new PagingRequest(10));
          assertEquals(1, posts.getPage().size());
          assertEquals("R2", posts.getPage().get(0).getContents());
          
          
          // Check the last reply now counts as the "primary"
          assertEquals(reply2.getNodeRef(), DISCUSSION_SERVICE.getPrimaryPost(topic).getNodeRef());

          
          // Zap the last one
          DISCUSSION_SERVICE.deletePost(reply2);
          posts = DISCUSSION_SERVICE.listPosts(topic, new PagingRequest(10));
          assertEquals(0, posts.getPage().size());
       }
    }
    
    /**
     * Ensures that when we try to write an entry to the
     *  container of a new site, it is correctly setup for us.
     * This test does it's own transactions
     */
    @Test public void newContainerSetup() throws Exception
    {
       final String TEST_SITE_NAME = "DiscussionsTestNewTestSite";
       
       TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
       {
          @Override
          public Void execute() throws Throwable
          {
             if(SITE_SERVICE.getSite(TEST_SITE_NAME) != null)
             {
                SITE_SERVICE.deleteSite(TEST_SITE_NAME);
             }
             SITE_SERVICE.createSite(
                   TEST_SITE_PREFIX, TEST_SITE_NAME, "Test", "Test", SiteVisibility.PUBLIC
             );

             // Won't have the container to start with
             assertFalse(SITE_SERVICE.hasContainer(TEST_SITE_NAME, DiscussionServiceImpl.DISCUSSION_COMPONENT));

             // Create a link
             DISCUSSION_SERVICE.createTopic(TEST_SITE_NAME, "Title");
             
             // It will now exist
             assertTrue(SITE_SERVICE.hasContainer(TEST_SITE_NAME, DiscussionServiceImpl.DISCUSSION_COMPONENT));

             // It'll be a tag scope too
             NodeRef container = SITE_SERVICE.getContainer(TEST_SITE_NAME, DiscussionServiceImpl.DISCUSSION_COMPONENT);
             assertTrue(TAGGING_SERVICE.isTagScope(container));

             // Tidy up
             SITE_SERVICE.deleteSite(TEST_SITE_NAME);
             return null;
          }
       });
    }
    
    @Test public void tagging() throws Exception
    {
       TopicInfo topic;
       final String TAG_1 = "link_tag_1";
       final String TAG_2 = "link_tag_2";
       final String TAG_3 = "link_tag_3";
       
       // Create one without tagging
       topic = DISCUSSION_SERVICE.createTopic(DISCUSSION_SITE.getShortName(), "Title");
       testNodesToTidy.add(topic.getNodeRef());
       
       // Check
       assertEquals(0, topic.getTags().size());
       
       topic = DISCUSSION_SERVICE.getTopic(DISCUSSION_SITE.getShortName(), topic.getSystemName());       
       assertEquals(0, topic.getTags().size());
       
       
       // Update it to have tags
       topic.getTags().add(TAG_1);
       topic.getTags().add(TAG_2);
       topic.getTags().add(TAG_1);
       assertEquals(3, topic.getTags().size());
       DISCUSSION_SERVICE.updateTopic(topic);
       
       // Check
       topic = DISCUSSION_SERVICE.getTopic(DISCUSSION_SITE.getShortName(), topic.getSystemName());       
       assertEquals(2, topic.getTags().size());
       assertEquals(true, topic.getTags().contains(TAG_1));
       assertEquals(true, topic.getTags().contains(TAG_2));
       assertEquals(false, topic.getTags().contains(TAG_3));
       
       
       // Update it to have different tags
       topic.getTags().remove(TAG_2);
       topic.getTags().add(TAG_3);
       topic.getTags().add(TAG_1);
       DISCUSSION_SERVICE.updateTopic(topic);       
       
       // Check it as-is
       assertEquals(3, topic.getTags().size()); // Includes duplicate tag until re-loaded
       assertEquals(true, topic.getTags().contains(TAG_1));
       assertEquals(false, topic.getTags().contains(TAG_2));
       assertEquals(true, topic.getTags().contains(TAG_3));
       
       // Now load and re-check
       topic = DISCUSSION_SERVICE.getTopic(DISCUSSION_SITE.getShortName(), topic.getSystemName());
       assertEquals(2, topic.getTags().size()); // Duplicate now gone
       assertEquals(true, topic.getTags().contains(TAG_1));
       assertEquals(false, topic.getTags().contains(TAG_2));
       assertEquals(true, topic.getTags().contains(TAG_3));

       
       // Update it to have no tags
       topic.getTags().clear();
       DISCUSSION_SERVICE.updateTopic(topic);
       
       // Check
       topic = DISCUSSION_SERVICE.getTopic(DISCUSSION_SITE.getShortName(), topic.getSystemName());       
       assertEquals(0, topic.getTags().size());

       
       // Update it to have tags again
       topic.getTags().add(TAG_1);
       topic.getTags().add(TAG_2);
       topic.getTags().add(TAG_3);
       DISCUSSION_SERVICE.updateTopic(topic);
       
       // Check
       topic = DISCUSSION_SERVICE.getTopic(DISCUSSION_SITE.getShortName(), topic.getSystemName());       
       assertEquals(3, topic.getTags().size());
       assertEquals(true, topic.getTags().contains(TAG_1));
       assertEquals(true, topic.getTags().contains(TAG_2));
       assertEquals(true, topic.getTags().contains(TAG_3));
       
       // Tidy
       DISCUSSION_SERVICE.deleteTopic(topic);
    }
    
    /**
     * Tests for listing the wiki pages of a site, possibly by user or date range
     */
/*
    @Test public void pagesListing() throws Exception
    {
       PagingRequest paging = new PagingRequest(10);
       AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
       
       // Nothing to start with
       PagingResults<WikiPageInfo> results = 
          DISCUSSION_SERVICE.listWikiPages(DISCUSSION_SITE.getShortName(), paging);
       assertEquals(0, results.getPage().size());
       
       // Add a few
       WikiPageInfo pageA = DISCUSSION_SERVICE.createWikiPage(
             DISCUSSION_SITE.getShortName(), "TitleA", "ContentA"
       );
       WikiPageInfo pageB = DISCUSSION_SERVICE.createWikiPage(
             DISCUSSION_SITE.getShortName(), "TitleB", "ContentB"
       );
       WikiPageInfo pageC = DISCUSSION_SERVICE.createWikiPage(
             DISCUSSION_SITE.getShortName(), "TitleC", "ContentC"
       );
       testNodesToTidy.add(pageA.getNodeRef());
       testNodesToTidy.add(pageB.getNodeRef());
       testNodesToTidy.add(pageC.getNodeRef());
       
       // Check now, should be newest first
       results = DISCUSSION_SERVICE.listWikiPages(DISCUSSION_SITE.getShortName(), paging);
       assertEquals(3, results.getPage().size());
       assertEquals("TitleC", results.getPage().get(0).getTitle());
       assertEquals("TitleB", results.getPage().get(1).getTitle());
       assertEquals("TitleA", results.getPage().get(2).getTitle());
       
       // Add one more, as a different user, and drop the page size
       AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
       WikiPageInfo pageD = DISCUSSION_SERVICE.createWikiPage(
             DISCUSSION_SITE.getShortName(), "TitleD", "ContentD"
       );
       testNodesToTidy.add(pageD.getNodeRef());
       AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
       
       paging = new PagingRequest(3);
       results = DISCUSSION_SERVICE.listWikiPages(DISCUSSION_SITE.getShortName(), paging);
       assertEquals(3, results.getPage().size());
       assertEquals("TitleD", results.getPage().get(0).getTitle());
       assertEquals("TitleC", results.getPage().get(1).getTitle());
       assertEquals("TitleB", results.getPage().get(2).getTitle());
       
       paging = new PagingRequest(3, 3);
       results = DISCUSSION_SERVICE.listWikiPages(DISCUSSION_SITE.getShortName(), paging);
       assertEquals(1, results.getPage().size());
       assertEquals("TitleA", results.getPage().get(0).getTitle());
       
       
       // Now check filtering by user
       paging = new PagingRequest(10);
       
       results = DISCUSSION_SERVICE.listWikiPages(DISCUSSION_SITE.getShortName(), TEST_USER, paging);
       assertEquals(3, results.getPage().size());
       assertEquals("TitleC", results.getPage().get(0).getTitle());
       assertEquals("TitleB", results.getPage().get(1).getTitle());
       assertEquals("TitleA", results.getPage().get(2).getTitle());
       
       results = DISCUSSION_SERVICE.listWikiPages(DISCUSSION_SITE.getShortName(), ADMIN_USER, paging);
       assertEquals(1, results.getPage().size());
       assertEquals("TitleD", results.getPage().get(0).getTitle());

       
       // Now check filtering by date range
       // Arrange it so that the orders are:
       //   Created ->  C B A D
       //   Modified -> D C B A
       pushAuditableDatesBack(pageB, 10, 0);
       pushAuditableDatesBack(pageC, 100, 10);
       pushAuditableDatesBack(pageD, 0, 100);
       pageA.setContents("UpdatedContentsA");
       pageA = DISCUSSION_SERVICE.updateWikiPage(pageA);
       
       
       Date today = new Date();
       Date tomorrow = new Date(today.getTime()+ONE_DAY_MS);
       Date yesterday = new Date(today.getTime()-ONE_DAY_MS);
       Date twoWeeksAgo = new Date(today.getTime()-14*ONE_DAY_MS);
       
       
       // Check by created date
       
       // Very recent ones
       results = DISCUSSION_SERVICE.listWikiPagesByCreated(DISCUSSION_SITE.getShortName(), yesterday, tomorrow, paging);
       assertEquals(2, results.getPage().size());
       assertEquals("TitleD", results.getPage().get(0).getTitle());
       assertEquals("TitleA", results.getPage().get(1).getTitle());
       
       // Fairly old ones
       results = DISCUSSION_SERVICE.listWikiPagesByCreated(DISCUSSION_SITE.getShortName(), twoWeeksAgo, yesterday, paging);
       assertEquals(1, results.getPage().size());
       assertEquals("TitleB", results.getPage().get(0).getTitle());
       
       // Fairly old to current
       results = DISCUSSION_SERVICE.listWikiPagesByCreated(DISCUSSION_SITE.getShortName(), twoWeeksAgo, tomorrow, paging);
       assertEquals(3, results.getPage().size());
       assertEquals("TitleD", results.getPage().get(0).getTitle());
       assertEquals("TitleA", results.getPage().get(1).getTitle());
       assertEquals("TitleB", results.getPage().get(2).getTitle());

       
       // Check by modified date
       
       // Very recent ones
       results = DISCUSSION_SERVICE.listWikiPagesByModified(DISCUSSION_SITE.getShortName(), yesterday, tomorrow, paging);
       assertEquals(2, results.getPage().size());
       assertEquals("TitleA", results.getPage().get(0).getTitle());
       assertEquals("TitleB", results.getPage().get(1).getTitle());
       
       // Fairly old ones
       results = DISCUSSION_SERVICE.listWikiPagesByModified(DISCUSSION_SITE.getShortName(), twoWeeksAgo, yesterday, paging);
       assertEquals(1, results.getPage().size());
       assertEquals("TitleC", results.getPage().get(0).getTitle());
       
       // Fairly old to current
       results = DISCUSSION_SERVICE.listWikiPagesByModified(DISCUSSION_SITE.getShortName(), twoWeeksAgo, tomorrow, paging);
       assertEquals(3, results.getPage().size());
       assertEquals("TitleA", results.getPage().get(0).getTitle());
       assertEquals("TitleB", results.getPage().get(1).getTitle());
       assertEquals("TitleC", results.getPage().get(2).getTitle());
       
       
       // Bring C back to current and re-check
       pageC.setContents("Changed C");
       pageC = DISCUSSION_SERVICE.updateWikiPage(pageC);
       
       // Order doesn't change, sorting is by created date not modified date
       results = DISCUSSION_SERVICE.listWikiPagesByModified(DISCUSSION_SITE.getShortName(), twoWeeksAgo, tomorrow, paging);
       assertEquals(3, results.getPage().size());
       assertEquals("TitleA", results.getPage().get(0).getTitle());
       assertEquals("TitleB", results.getPage().get(1).getTitle());
       assertEquals("TitleC", results.getPage().get(2).getTitle());
       
       
       // Tidy
       paging = new PagingRequest(10);
       AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
       results = DISCUSSION_SERVICE.listWikiPages(DISCUSSION_SITE.getShortName(), paging);
       for(WikiPageInfo link : results.getPage())
       {
          PUBLIC_NODE_SERVICE.deleteNode(link.getNodeRef());
       }
       results = DISCUSSION_SERVICE.listWikiPages(ALTERNATE_DISCUSSION_SITE.getShortName(), paging);
       for(WikiPageInfo link : results.getPage())
       {
          PUBLIC_NODE_SERVICE.deleteNode(link.getNodeRef());
       }
    }
*/

    /**
     * Checks that the correct permission checking occurs on fetching
     *  links listings (which go through canned queries)
     */
/*    
    @Test public void pagesListingPermissionsChecking() throws Exception
    {
       PagingRequest paging = new PagingRequest(10);
       PagingResults<WikiPageInfo> results;
     
       // Nothing to start with in either site
       results = DISCUSSION_SERVICE.listWikiPages(DISCUSSION_SITE.getShortName(), paging);
       assertEquals(0, results.getPage().size());
       results = DISCUSSION_SERVICE.listWikiPages(ALTERNATE_DISCUSSION_SITE.getShortName(), paging);
       assertEquals(0, results.getPage().size());

       // Double check that we're only allowed to see the 1st site
       assertEquals(true,  SITE_SERVICE.isMember(DISCUSSION_SITE.getShortName(), TEST_USER));
       assertEquals(false, SITE_SERVICE.isMember(ALTERNATE_DISCUSSION_SITE.getShortName(), TEST_USER));
       
       
       // Now become the test user
       AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);

       
       // Add two events to one site and three to the other
       // Note - add the events as a different user for the site that the
       //  test user isn't a member of!
       WikiPageInfo pageA = DISCUSSION_SERVICE.createWikiPage(
             DISCUSSION_SITE.getShortName(), "TitleA", "ContentA"
       );
       WikiPageInfo pageB = DISCUSSION_SERVICE.createWikiPage(
             DISCUSSION_SITE.getShortName(), "TitleB", "ContentB"
       );
       testNodesToTidy.add(pageA.getNodeRef());
       testNodesToTidy.add(pageB.getNodeRef());
       
       AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
       WikiPageInfo pagePrivA = DISCUSSION_SERVICE.createWikiPage(
             ALTERNATE_DISCUSSION_SITE.getShortName(), "PrivTitleA", "Contents A"
       );
       WikiPageInfo pagePrivB = DISCUSSION_SERVICE.createWikiPage(
             ALTERNATE_DISCUSSION_SITE.getShortName(), "PrivTitleB", "Contents B"
       );
       WikiPageInfo pagePrivC = DISCUSSION_SERVICE.createWikiPage(
             ALTERNATE_DISCUSSION_SITE.getShortName(), "PrivTitleC", "Contents C"
       );
       testNodesToTidy.add(pagePrivA.getNodeRef());
       testNodesToTidy.add(pagePrivB.getNodeRef());
       testNodesToTidy.add(pagePrivC.getNodeRef());
       AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
       
       
       // Check again, as we're not in the 2nd site won't see any there
       results = DISCUSSION_SERVICE.listWikiPages(DISCUSSION_SITE.getShortName(), paging);
       assertEquals(2, results.getPage().size());
       results = DISCUSSION_SERVICE.listWikiPages(ALTERNATE_DISCUSSION_SITE.getShortName(), paging);
       assertEquals(0, results.getPage().size());
       
       
       // Join the site, now we can see both
       TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
       {
          @Override
          public Void execute() throws Throwable
          {
             AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
             SITE_SERVICE.setMembership(ALTERNATE_DISCUSSION_SITE.getShortName(), TEST_USER, SiteModel.SITE_COLLABORATOR);
             AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
             return null;
          }
       });
       
       results = DISCUSSION_SERVICE.listWikiPages(DISCUSSION_SITE.getShortName(), paging);
       assertEquals(2, results.getPage().size());
       results = DISCUSSION_SERVICE.listWikiPages(ALTERNATE_DISCUSSION_SITE.getShortName(), paging);
       assertEquals(3, results.getPage().size());
       
       
       // Explicitly remove their permissions from one node, check it vanishes from the list
       PERMISSION_SERVICE.setInheritParentPermissions(pagePrivC.getNodeRef(), false);
       PERMISSION_SERVICE.clearPermission(pagePrivC.getNodeRef(), TEST_USER);
       
       results = DISCUSSION_SERVICE.listWikiPages(DISCUSSION_SITE.getShortName(), paging);
       assertEquals(2, results.getPage().size());
       results = DISCUSSION_SERVICE.listWikiPages(ALTERNATE_DISCUSSION_SITE.getShortName(), paging);
       assertEquals(2, results.getPage().size());
       
       
       // Leave, they go away again
       TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
       {
          @Override
          public Void execute() throws Throwable
          {
             AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
             SITE_SERVICE.removeMembership(ALTERNATE_DISCUSSION_SITE.getShortName(), TEST_USER);
             AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
             return null;
          }
       });
       
       results = DISCUSSION_SERVICE.listWikiPages(DISCUSSION_SITE.getShortName(), paging);
       assertEquals(2, results.getPage().size());
       results = DISCUSSION_SERVICE.listWikiPages(ALTERNATE_DISCUSSION_SITE.getShortName(), paging);
       assertEquals(0, results.getPage().size());


       // Tidy
       paging = new PagingRequest(10);
       AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
       results = DISCUSSION_SERVICE.listWikiPages(DISCUSSION_SITE.getShortName(), paging);
       for(WikiPageInfo link : results.getPage())
       {
          PUBLIC_NODE_SERVICE.deleteNode(link.getNodeRef());
       }
       results = DISCUSSION_SERVICE.listWikiPages(ALTERNATE_DISCUSSION_SITE.getShortName(), paging);
       for(WikiPageInfo link : results.getPage())
       {
          PUBLIC_NODE_SERVICE.deleteNode(link.getNodeRef());
       }
    }
*/
    
    
    // --------------------------------------------------------------------------------

    
    /**
     * Alters the created date on a wiki page for testing
     */
    private void pushAuditableDatesBack(final Object thing, final int createdDaysAgo, final int modifiedDaysAgo) throws Exception
    {
       NodeRef tmpNodeRef;
       if(thing instanceof NodeRef)
       {
          tmpNodeRef = (NodeRef)thing;
       }
       else if(thing instanceof TopicInfo)
       {
          tmpNodeRef = ((TopicInfo)thing).getNodeRef();
       }
       else if(thing instanceof PostInfo)
       {
          tmpNodeRef = ((PostInfo)thing).getNodeRef();
       }
       else
       {
          throw new IllegalArgumentException("Unknown thing " + thing);
       }
       final NodeRef node = tmpNodeRef;
       
       final Date created = (Date)PUBLIC_NODE_SERVICE.getProperty(node, ContentModel.PROP_CREATED);
       final Date newCreated = new Date(created.getTime() - createdDaysAgo*ONE_DAY_MS);
       final Date modified = (Date)PUBLIC_NODE_SERVICE.getProperty(node, ContentModel.PROP_MODIFIED);
       final Date newModified = new Date(modified.getTime() - modifiedDaysAgo*ONE_DAY_MS);
       
       // Update the created date
       TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
          @Override
          public Void execute() throws Throwable {
             BEHAVIOUR_FILTER.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
             NODE_SERVICE.setProperty(node, ContentModel.PROP_CREATED, newCreated);
             NODE_SERVICE.setProperty(node, ContentModel.PROP_MODIFIED, newModified);
             return null;
          }
       }, false, true);
       // Change something else too in the public nodeservice, to force a re-index
       TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
          @Override
          public Void execute() throws Throwable {
             BEHAVIOUR_FILTER.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
             PUBLIC_NODE_SERVICE.setProperty(node, ContentModel.PROP_CREATED, newCreated);
             PUBLIC_NODE_SERVICE.setProperty(node, ContentModel.PROP_MODIFIED, newModified);
             PUBLIC_NODE_SERVICE.setProperty(node, ContentModel.PROP_DESCRIPTION, "Forced Change");
             return null;
          }
       }, false, true);
       // Check it was taken
       TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
          @Override
          public Void execute() throws Throwable {
             assertEquals(newCreated, NODE_SERVICE.getProperty(node, ContentModel.PROP_CREATED));
             assertEquals(newCreated, PUBLIC_NODE_SERVICE.getProperty(node, ContentModel.PROP_CREATED));
             assertEquals(newModified, NODE_SERVICE.getProperty(node, ContentModel.PROP_MODIFIED));
             assertEquals(newModified, PUBLIC_NODE_SERVICE.getProperty(node, ContentModel.PROP_MODIFIED));
             return null;
          }
       }, false, true);

       // Update the object itself
       if(thing instanceof TopicInfo)
       {
          ((TopicInfoImpl)thing).setCreatedAt(newCreated);
          ((TopicInfoImpl)thing).setModifiedAt(newModified);
       }
       if(thing instanceof PostInfo)
       {
          ((PostInfoImpl)thing).setCreatedAt(newCreated);
          ((PostInfoImpl)thing).setModifiedAt(newModified);
       }
    }
    
    private static void createTestSites() throws Exception
    {
        final DiscussionServiceImpl privateDiscussionService = (DiscussionServiceImpl)testContext.getBean("discussionService");
        
        DISCUSSION_SITE = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<SiteInfo>()
           {
              @Override
              public SiteInfo execute() throws Throwable
              {
                  SiteInfo site = SITE_SERVICE.createSite(
                        TEST_SITE_PREFIX, 
                        DiscussionServiceImplTest.class.getSimpleName() + "_testSite" + System.currentTimeMillis(),
                        "test site title", "test site description", 
                        SiteVisibility.PUBLIC
                  );
                  privateDiscussionService.getSiteDiscussionsContainer(site.getShortName(), true);
                  CLASS_TEST_NODES_TO_TIDY.add(site.getNodeRef());
                  return site;
              }
         });
        
         // Add a forum within it, used for the alternate testing
         FORUM_NODE = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
            {
               @Override
               public NodeRef execute() throws Throwable
               {
                  NodeRef node = NODE_SERVICE.createNode(
                        DISCUSSION_SITE.getNodeRef(), ForumModel.ASSOC_DISCUSSION,
                        QName.createQName("Forum"), ForumModel.TYPE_FORUM
                  ).getChildRef();
                  NODE_SERVICE.setProperty(node, ContentModel.PROP_NAME, "Forum");
                  NODE_SERVICE.setProperty(node, ContentModel.PROP_TITLE, "Forum");
                  CLASS_TEST_NODES_TO_TIDY.add(node);
                  return node;
               }
         });
         
         // Create the alternate site as admin
         AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
         ALTERNATE_DISCUSSION_SITE = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<SiteInfo>()
            {
               @Override
               public SiteInfo execute() throws Throwable
               {
                  SiteInfo site = SITE_SERVICE.createSite(
                        TEST_SITE_PREFIX, 
                        DiscussionServiceImplTest.class.getSimpleName() + "_testAltSite" + System.currentTimeMillis(),
                        "alternate site title", "alternate site description", 
                        SiteVisibility.PRIVATE
                  );
                  privateDiscussionService.getSiteDiscussionsContainer(site.getShortName(), true);
                  CLASS_TEST_NODES_TO_TIDY.add(site.getNodeRef());
                  return site;
               }
         });
         AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
    }
    
    /**
     * By default, all tests are run as the admin user.
     */
    @Before public void setAdminUser()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
    }
    
    @After public void deleteTestNodes() throws Exception
    {
        performDeletionOfNodes(testNodesToTidy);
    }
    
    @AfterClass public static void deleteClassTestNodesAndUsers() throws Exception
    {
        performDeletionOfNodes(CLASS_TEST_NODES_TO_TIDY);
        deleteUser(TEST_USER);
    }

    /**
     * Deletes the specified NodeRefs, if they exist.
     * @param nodesToDelete
     */
    private static void performDeletionOfNodes(final List<NodeRef> nodesToDelete)
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
           @Override
           public Void execute() throws Throwable
           {
              AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);

              for (NodeRef node : nodesToDelete)
              {
                 if (NODE_SERVICE.exists(node))
                 {
                    // st:site nodes can only be deleted via the SiteService
                    if (NODE_SERVICE.getType(node).equals(SiteModel.TYPE_SITE))
                    {

                       SiteInfo siteInfo = SITE_SERVICE.getSite(node);
                       SITE_SERVICE.deleteSite(siteInfo.getShortName());
                    }
                    else
                    {
                       NODE_SERVICE.deleteNode(node);
                    }
                 }
              }

              return null;
           }
        });
    }
    
    private static void createUser(final String userName)
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
           @Override
           public Void execute() throws Throwable
           {
              if (!AUTHENTICATION_SERVICE.authenticationExists(userName))
              {
                 AUTHENTICATION_SERVICE.createAuthentication(userName, "PWD".toCharArray());
              }

              if (!PERSON_SERVICE.personExists(userName))
              {
                 PropertyMap ppOne = new PropertyMap();
                 ppOne.put(ContentModel.PROP_USERNAME, userName);
                 ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
                 ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
                 ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
                 ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

                 PERSON_SERVICE.createPerson(ppOne);
              }

              return null;
           }
        });
    }

    private static void deleteUser(final String userName)
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
           @Override
           public Void execute() throws Throwable
           {
              if (PERSON_SERVICE.personExists(userName))
              {
                 PERSON_SERVICE.deletePerson(userName);
              }

              return null;
           }
        });
    }
}
