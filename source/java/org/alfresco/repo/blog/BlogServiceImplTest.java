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
package org.alfresco.repo.blog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
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
 * Test cases for {@link BlogServiceImpl}.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 */
public class BlogServiceImplTest
{

    private static final ApplicationContext testContext = ApplicationContextHelper.getApplicationContext();
    
    // injected services
    private static MutableAuthenticationService AUTHENTICATION_SERVICE;
    private static BehaviourFilter              BEHAVIOUR_FILTER;
    private static BlogService                  BLOG_SERVICE;
    private static DictionaryService            DICTIONARY_SERVICE;
    private static NodeService                  NODE_SERVICE;
    private static PersonService                PERSON_SERVICE;
    private static RetryingTransactionHelper    TRANSACTION_HELPER;
    private static SiteService                  SITE_SERVICE;
    private static TaggingService               TAGGING_SERVICE;
    
    private static final String TEST_USER = BlogServiceImplTest.class.getSimpleName() + "_testuser";
    private static final String ADMIN_USER = AuthenticationUtil.getAdminUserName();

    
    /**
     * Temporary test nodes (created during a test method) that need deletion after the test method.
     */
    private List<NodeRef> testNodesToTidy = new ArrayList<NodeRef>();
    /**
     * Temporary test nodes (created BeforeClass) that need deletion after this test class.
     */
    private static List<NodeRef> CLASS_TEST_NODES_TO_TIDY = new ArrayList<NodeRef>();

    private static NodeRef BLOG_CONTAINER_NODE;
    
    @BeforeClass public static void initTestsContext() throws Exception
    {
        AUTHENTICATION_SERVICE = (MutableAuthenticationService)testContext.getBean("authenticationService");
        BEHAVIOUR_FILTER       = (BehaviourFilter)testContext.getBean("policyBehaviourFilter");
        BLOG_SERVICE           = (BlogService)testContext.getBean("blogService");
        DICTIONARY_SERVICE     = (DictionaryService)testContext.getBean("dictionaryService");
        NODE_SERVICE           = (NodeService)testContext.getBean("nodeService");
        PERSON_SERVICE         = (PersonService)testContext.getBean("personService");
        TRANSACTION_HELPER     = (RetryingTransactionHelper)testContext.getBean("retryingTransactionHelper");
        SITE_SERVICE           = (SiteService)testContext.getBean("siteService");
        TAGGING_SERVICE        = (TaggingService)testContext.getBean("TaggingService");
        
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
        createUser(TEST_USER);
        
        // We need to create the test site as the test user so that they can contribute content to it in tests below.
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
        createTestSiteWithBlogContainer();
    }
    
    private static void createTestSiteWithBlogContainer() throws Exception
    {
        BLOG_CONTAINER_NODE = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
            {
                @Override
                public NodeRef execute() throws Throwable
                {
                    SiteInfo site = SITE_SERVICE.createSite("BlogSitePreset", BlogServiceImplTest.class.getSimpleName() + "_testSite" + System.currentTimeMillis(),
                                            "test site title", "test site description", SiteVisibility.PUBLIC);
                    CLASS_TEST_NODES_TO_TIDY.add(site.getNodeRef());
                    
                    NodeRef result = SITE_SERVICE.getContainer(site.getShortName(), "blog");
                    
                    if (result == null)
                    {
                        result = NODE_SERVICE.createNode(site.getNodeRef(), ContentModel.ASSOC_CONTAINS,
                                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "blog"), ContentModel.TYPE_FOLDER, null).getChildRef();
                        CLASS_TEST_NODES_TO_TIDY.add(result);
                    }
                    
                    return result;
                }
            });
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
    
    @Test public void createDraftBlogPostsAndGetPagedResults() throws Exception
    {
        final int arbitraryNumberGreaterThanPageSize = 42;
        final List<NodeRef> submittedBlogPosts = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<List<NodeRef>>()
            {
                @Override
                public List<NodeRef> execute() throws Throwable
                {
                    List<NodeRef> results = new ArrayList<NodeRef>();
                    
                    for (int i = 0; i < arbitraryNumberGreaterThanPageSize; i++)
                    {
                        ChildAssociationRef newBlogPost = BLOG_SERVICE.createBlogPost(BLOG_CONTAINER_NODE, "title_" + i, "Hello world", true);
                        
                        results.add(newBlogPost.getChildRef());
                        testNodesToTidy.add(newBlogPost.getChildRef());
                    }
                    
                    return results;
                }
            });
        
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    List<BlogPostInfo> recoveredBlogPosts = new ArrayList<BlogPostInfo>(arbitraryNumberGreaterThanPageSize);
                    
                    final int pageSize = 10;
                    PagingRequest pagingReq = new PagingRequest(0, pageSize, null);
                    
                    PagingResults<BlogPostInfo> pagedResults = BLOG_SERVICE.getDrafts(BLOG_CONTAINER_NODE, ADMIN_USER, pagingReq);
                    assertEquals("Wrong total result count.", arbitraryNumberGreaterThanPageSize, (int)pagedResults.getTotalResultCount().getFirst());
                    
                    while (pagedResults.hasMoreItems())
                    {
                        recoveredBlogPosts.addAll(pagedResults.getPage());
                        pagingReq = new PagingRequest(pagingReq.getSkipCount() + pageSize, pageSize, null);
                        pagedResults = BLOG_SERVICE.getDrafts(BLOG_CONTAINER_NODE, ADMIN_USER, pagingReq);
                    }
                    // and the last page, which only has 2 items in it.
                    recoveredBlogPosts.addAll(pagedResults.getPage());
                    
                    assertEquals("Wrong number of blog posts.", submittedBlogPosts.size(), recoveredBlogPosts.size());
                    
                    // Check the list is sorted by cm:created, descending order.
                    assertNodeRefsAreSortedBy(recoveredBlogPosts, ContentModel.PROP_CREATED, false);
                    
                    return null;
                }
            });
    }
    
    /**
     * This method asserts that the given List<BlogPostInfo> has NodeRefs in order of the specified date property.
     * 
     * @param blogPosts
     * @param property a Date property
     * @param ascendingOrder <tt>true</tt> if ascending order, <tt>false</tt> for descending.
     */
    private void assertNodeRefsAreSortedBy(List<BlogPostInfo> blogPosts, QName property, boolean ascendingOrder)
    {
        final PropertyDefinition propertyDef = DICTIONARY_SERVICE.getProperty(property);
        assertNotNull("Property not recognised.", propertyDef);
        assertEquals("Property was not a Date", DataTypeDefinition.DATETIME, propertyDef.getDataType().getName());
        
        if (blogPosts.size() > 1)
        {
            for (int i = 0; i < blogPosts.size() - 1; i++)
            {
                NodeRef nodeRef1 = blogPosts.get(i).getNodeRef();
                NodeRef nodeRef2 = blogPosts.get(i + 1).getNodeRef();
                Date date1 = (Date) NODE_SERVICE.getProperty(nodeRef1, property);
                Date date2 = (Date) NODE_SERVICE.getProperty(nodeRef2, property);
                
                if (ascendingOrder)
                {
                    assertTrue("BlogPosts not asc-sorted by " + property + ". Error at index " + i, date1.before(date2));
                }
                else
                {
                    assertTrue("BlogPosts not desc-sorted by " + property + ". Error at index " + i, date1.after(date2));
                }
            }
        }
    }
    
    @Test public void createTaggedDraftBlogPost() throws Exception
    {
        final List<String> tags = Arrays.asList(new String[]{"foo", "bar"});
        
        final NodeRef blogPost = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
            {
                
                @Override
                public NodeRef execute() throws Throwable
                {
                    ChildAssociationRef newBlogPost = BLOG_SERVICE.createBlogPost(BLOG_CONTAINER_NODE, "draftWithTag", "Hello world", true);
                    TAGGING_SERVICE.addTags(newBlogPost.getChildRef(), tags);
                    testNodesToTidy.add(newBlogPost.getChildRef());
                    
                    return newBlogPost.getChildRef();
                }
            });
        
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    PagingRequest pagingReq = new PagingRequest(0, 10, null);
                    
                    PagingResults<BlogPostInfo> pagedResults = BLOG_SERVICE.getDrafts(BLOG_CONTAINER_NODE, ADMIN_USER, pagingReq);
                    assertEquals("Expected one blog post", 1, pagedResults.getPage().size());
                    
                    NodeRef blogNode = pagedResults.getPage().get(0).getNodeRef();
                    assertEquals("Incorrect NodeRef.", blogNode, blogPost);
                    
                    List<String> recoveredTags = TAGGING_SERVICE.getTags(blogNode);
                    assertEquals("Incorrect tags.", tags, recoveredTags);
                    
                    return null;
                }
            });
    }
    
    /**
     * This test uses two different users to create draft and internally published blog posts.
     * Then it ensures that each user sees the correct posts when they retrieve them from the service.
     */
    @Test public void multipleUsersCreateDraftsAndPublishedPostsAndBrowse() throws Exception
    {
        // Admin creates a draft and an internally-published blog post.
        final Pair<NodeRef, NodeRef> adminPosts = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Pair<NodeRef, NodeRef>>()
            {
                
                @Override
                public Pair<NodeRef, NodeRef> execute() throws Throwable
                {
                    ChildAssociationRef newDraftBlogPost = BLOG_SERVICE.createBlogPost(BLOG_CONTAINER_NODE, "adminDraft", "", true);
                    testNodesToTidy.add(newDraftBlogPost.getChildRef());
                    
                    ChildAssociationRef newPublishedBlogPost = BLOG_SERVICE.createBlogPost(BLOG_CONTAINER_NODE, "adminPublished", "", false);
                    testNodesToTidy.add(newPublishedBlogPost.getChildRef());
                    
                    return new Pair<NodeRef, NodeRef>(newDraftBlogPost.getChildRef(), newPublishedBlogPost.getChildRef());
                }
            });
        
        // Then another user does the same.
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
        final Pair<NodeRef, NodeRef> userPosts = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Pair<NodeRef, NodeRef>>()
            {
                
                @Override
                public Pair<NodeRef, NodeRef> execute() throws Throwable
                {
                    ChildAssociationRef newDraftBlogPost = BLOG_SERVICE.createBlogPost(BLOG_CONTAINER_NODE, "userDraft", "", true);
                    testNodesToTidy.add(newDraftBlogPost.getChildRef());
                    
                    ChildAssociationRef newPublishedBlogPost = BLOG_SERVICE.createBlogPost(BLOG_CONTAINER_NODE, "userPublished", "", false);
                    testNodesToTidy.add(newPublishedBlogPost.getChildRef());
                    
                    return new Pair<NodeRef, NodeRef>(newDraftBlogPost.getChildRef(), newPublishedBlogPost.getChildRef());
                }
            });
        
        // Now check what we see from the service.
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
        
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    PagingRequest pagingReq = new PagingRequest(0, 10, null);
                    
                    PagingResults<BlogPostInfo> pagedDraftResults = BLOG_SERVICE.getDrafts(BLOG_CONTAINER_NODE, ADMIN_USER, pagingReq);
                    assertEquals("Wrong number of admin draft blog posts", 1, pagedDraftResults.getPage().size());
                    NodeRef blogNode = pagedDraftResults.getPage().get(0).getNodeRef();
                    assertEquals("Incorrect admin draft NodeRef.", blogNode, adminPosts.getFirst());
                    
                    PagingResults<BlogPostInfo> pagedPublishedResults = BLOG_SERVICE.getPublished(BLOG_CONTAINER_NODE, null, null, ADMIN_USER, pagingReq);
                    assertEquals("Wrong number of admin published blog posts", 1, pagedPublishedResults.getPage().size());
                    blogNode = pagedPublishedResults.getPage().get(0).getNodeRef();
                    assertEquals("Incorrect admin published NodeRef.", blogNode, adminPosts.getSecond());
                    
                    return null;
                }
            });
        
        
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
        
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    PagingRequest pagingReq = new PagingRequest(0, 10, null);
                    
                    PagingResults<BlogPostInfo> pagedDraftResults = BLOG_SERVICE.getDrafts(BLOG_CONTAINER_NODE, TEST_USER, pagingReq);
                    assertEquals("Wrong number of user draft blog posts", 1, pagedDraftResults.getPage().size());
                    NodeRef blogNode = pagedDraftResults.getPage().get(0).getNodeRef();
                    assertEquals("Incorrect user draft NodeRef.", blogNode, userPosts.getFirst());
                    
                    PagingResults<BlogPostInfo> pagedPublishedResults = BLOG_SERVICE.getPublished(BLOG_CONTAINER_NODE, null, null, TEST_USER, pagingReq);
                    assertEquals("Wrong number of user published blog posts", 1, pagedPublishedResults.getPage().size());
                    blogNode = pagedPublishedResults.getPage().get(0).getNodeRef();
                    assertEquals("Incorrect user published NodeRef.", blogNode, userPosts.getSecond());
                    
                    return null;
                }
            });
    }
    
    @Test public void getBlogPostsFilteredByDateRange() throws Exception
    {
        final int numberOfPosts = 31 + 31 + 29;
        
        final List<NodeRef> posts = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<List<NodeRef>>()
            {
                @Override
                public List<NodeRef> execute() throws Throwable
                {
                    List<NodeRef> results = new ArrayList<NodeRef>();
                    
                    for (int i = 0; i < numberOfPosts; i++)
                    {
                        ChildAssociationRef newBlogPost =
                                BLOG_SERVICE.createBlogPost(BLOG_CONTAINER_NODE, "date-specific-post" + i, "", false);
                        testNodesToTidy.add(newBlogPost.getChildRef());
                        
                        results.add(newBlogPost.getChildRef());
                    }
                    
                    return results;
                }
            });
        
        // Now go through and set their creation dates to specific points in the past.
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    // FROM 1st December 1999
                    final Calendar current = Calendar.getInstance();
                    current.set(1999, 11, 1, 11, 0);
                    
                    // should give us:
                    // 31 posts in december 99
                    // 31 posts in january 00
                    // 29 posts in february 00
                    
                    Date currentDate = current.getTime();
                    for (NodeRef nr : posts)
                    {
                        // We'll permanently turn off auditing on this node.
                        // This should allow us to set the cm:created date without auditing overwriting our value.
                        // These nodes get deleted after the test anyway.
                        BEHAVIOUR_FILTER.disableBehaviour(nr, ContentModel.ASPECT_AUDITABLE);
                        
                        // Yes, cm:published will be before cm:created. But I don't think that matter.
                        NODE_SERVICE.setProperty(nr, ContentModel.PROP_PUBLISHED, currentDate);
                        
                        current.add(Calendar.DATE, 1);
                        currentDate = current.getTime();
                        
                    }
                    
                    return null;
                }
            });
        
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    PagingRequest pagingReq = new PagingRequest(0, 100, null);
                    
                    Calendar cal = Calendar.getInstance();
                    cal.set(1999, 11, 1, 0, 0, 0);
                    Date firstDec99 = cal.getTime();
                    
                    cal.set(2000, 0, 1, 0, 0, 0);
                    Date firstJan00 = cal.getTime();
                    
                    cal.set(2000, 1, 1, 0, 0, 0);
                    Date firstFeb00 = cal.getTime();
                    
                    cal.set(2000, 2, 1, 0, 0, 0);
                    Date firstMar00 = cal.getTime();
                    
                    PagingResults<BlogPostInfo> pagedResults = BLOG_SERVICE.getPublished(BLOG_CONTAINER_NODE, firstDec99, firstJan00, null, pagingReq);
                    assertEquals("Wrong number of user blog posts", 31, pagedResults.getPage().size());
                    
                    pagedResults = BLOG_SERVICE.getPublished(BLOG_CONTAINER_NODE, firstFeb00, firstMar00, null, pagingReq);
                    assertEquals("Wrong number of user blog posts", 29, pagedResults.getPage().size());
                    
                    pagedResults = BLOG_SERVICE.getPublished(BLOG_CONTAINER_NODE, firstJan00, firstMar00, null, pagingReq);
                    assertEquals("Wrong number of user blog posts", 31 + 29, pagedResults.getPage().size());
                    
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
