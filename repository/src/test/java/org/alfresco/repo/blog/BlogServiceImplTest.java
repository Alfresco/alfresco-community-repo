/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.blog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.blog.BlogPostInfo;
import org.alfresco.service.cmr.blog.BlogService;
import org.alfresco.service.cmr.blog.BlogService.RangedDateProperty;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.testing.category.LuceneTests;
import org.alfresco.util.testing.category.RedundantTests;

/**
 * Test cases for {@link BlogServiceImpl}.
 *
 * @author Neil Mc Erlean
 * @since 4.0
 */
@Category(LuceneTests.class)
public class BlogServiceImplTest extends BaseSpringTest
{
    // injected services
    private static MutableAuthenticationService AUTHENTICATION_SERVICE;
    private static BehaviourFilter BEHAVIOUR_FILTER;
    private static BlogService BLOG_SERVICE;
    private static DictionaryService DICTIONARY_SERVICE;
    private static NodeService NODE_SERVICE;
    private static PersonService PERSON_SERVICE;
    private static RetryingTransactionHelper TRANSACTION_HELPER;
    private static SiteService SITE_SERVICE;
    private static TaggingService TAGGING_SERVICE;

    private static final String TEST_USER = BlogServiceImplTest.class.getSimpleName() + GUID.generate();
    private static final String ADMIN_USER = AuthenticationUtil.getAdminUserName();

    /**
     * Temporary test nodes (created during a test method) that need deletion after the test method.
     */
    private List<NodeRef> testNodesToTidy = new ArrayList<NodeRef>();
    /**
     * Temporary test nodes (created BeforeClass) that need deletion after this test class.
     */
    private static List<NodeRef> CLASS_TEST_NODES_TO_TIDY = new ArrayList<NodeRef>();

    private static SiteInfo BLOG_SITE;
    private static NodeRef BLOG_CONTAINER_NODE;

    @Before
    public void before() throws Exception
    {
        AUTHENTICATION_SERVICE = (MutableAuthenticationService) applicationContext.getBean("authenticationService");
        BEHAVIOUR_FILTER = (BehaviourFilter) applicationContext.getBean("policyBehaviourFilter");
        BLOG_SERVICE = (BlogService) applicationContext.getBean("blogService");
        DICTIONARY_SERVICE = (DictionaryService) applicationContext.getBean("dictionaryService");
        NODE_SERVICE = (NodeService) applicationContext.getBean("nodeService");
        PERSON_SERVICE = (PersonService) applicationContext.getBean("personService");
        TRANSACTION_HELPER = (RetryingTransactionHelper) applicationContext.getBean("retryingTransactionHelper");
        SITE_SERVICE = (SiteService) applicationContext.getBean("siteService");
        TAGGING_SERVICE = (TaggingService) applicationContext.getBean("TaggingService");

        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
        createUser(TEST_USER);

        // We need to create the test site as the test user so that they can contribute content to it in tests below.
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
        createTestSiteWithBlogContainer();
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
    }

    private static void createTestSiteWithBlogContainer() throws Exception
    {
        BLOG_SITE = TRANSACTION_HELPER.doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<SiteInfo>() {
                    @Override
                    public SiteInfo execute() throws Throwable
                    {
                        SiteInfo site = SITE_SERVICE.createSite("BlogSitePreset", BlogServiceImplTest.class.getSimpleName() + "_testSite" + GUID.generate(),
                                "test site title", "test site description", SiteVisibility.PUBLIC);
                        CLASS_TEST_NODES_TO_TIDY.add(site.getNodeRef());
                        return site;
                    }
                });

        BLOG_CONTAINER_NODE = TRANSACTION_HELPER.doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>() {
                    @Override
                    public NodeRef execute() throws Throwable
                    {
                        SiteInfo site = BLOG_SITE;
                        NodeRef result = SITE_SERVICE.getContainer(site.getShortName(), BlogServiceImpl.BLOG_COMPONENT);

                        if (result == null)
                        {
                            result = SITE_SERVICE.createContainer(site.getShortName(), BlogServiceImpl.BLOG_COMPONENT,
                                    ContentModel.TYPE_FOLDER, null);
                            CLASS_TEST_NODES_TO_TIDY.add(result);
                        }

                        return result;
                    }
                });
    }

    @Test
    public void createDraftBlogPostsAndGetPagedResults() throws Exception
    {
        final int arbitraryNumberGreaterThanPageSize = 42;
        final List<NodeRef> submittedBlogPosts = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<List<NodeRef>>() {
            @Override
            public List<NodeRef> execute() throws Throwable
            {
                List<NodeRef> results = new ArrayList<NodeRef>();

                for (int i = 0; i < arbitraryNumberGreaterThanPageSize; i++)
                {
                    BlogPostInfo newBlogPost;
                    if (i % 2 == 0)
                    {
                        // By container ref
                        newBlogPost = BLOG_SERVICE.createBlogPost(BLOG_CONTAINER_NODE, "title_" + i, "Hello world", true);
                    }
                    else
                    {
                        // By site name
                        newBlogPost = BLOG_SERVICE.createBlogPost(BLOG_SITE.getShortName(), "title_" + i, "Hello world", true);
                    }

                    results.add(newBlogPost.getNodeRef());
                    testNodesToTidy.add(newBlogPost.getNodeRef());
                }

                return results;
            }
        });

        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
            @Override
            public Void execute() throws Throwable
            {
                List<BlogPostInfo> recoveredBlogPosts = new ArrayList<BlogPostInfo>(arbitraryNumberGreaterThanPageSize);

                final int pageSize = 10;
                PagingRequest pagingReq = new PagingRequest(0, pageSize, null);
                pagingReq.setRequestTotalCountMax(arbitraryNumberGreaterThanPageSize); // must be set if calling getTotalResultCount() later

                PagingResults<BlogPostInfo> pagedResults = BLOG_SERVICE.getDrafts(BLOG_CONTAINER_NODE, ADMIN_USER, pagingReq);
                assertEquals("Wrong total result count.", arbitraryNumberGreaterThanPageSize, (int) pagedResults.getTotalResultCount().getFirst());

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
     *            List<BlogPostInfo>
     * @param property
     *            a Date property
     * @param ascendingOrder
     *            <tt>true</tt> if ascending order, <tt>false</tt> for descending.
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

                // Equal dates are applicable to either sort order
                if (date1.equals(date2))
                {
                    continue;
                }

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

    @Category(RedundantTests.class)
    @Test
    public void createTaggedDraftBlogPost() throws Exception
    {
        // Our tags, which are a mixture of English, Accented European and Chinese
        final List<String> tags = Arrays.asList(new String[]{
                "alpha", "beta", "gamma", "fran\u00e7ais", "chinese_\u535a\u5ba2"});

        // Create a list of Blog Posts, all drafts, each with one of the tags above.
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<List<NodeRef>>() {

            @Override
            public List<NodeRef> execute() throws Throwable
            {
                List<NodeRef> results = new ArrayList<NodeRef>();

                for (String tag : tags)
                {
                    final String blogTitle = "draftWithTag" + tag;
                    BlogPostInfo newBlogPost = BLOG_SERVICE.createBlogPost(BLOG_CONTAINER_NODE, blogTitle, "Hello world", true);
                    TAGGING_SERVICE.addTags(newBlogPost.getNodeRef(), Arrays.asList(new String[]{tag}));
                    testNodesToTidy.add(newBlogPost.getNodeRef());
                    results.add(newBlogPost.getNodeRef());
                }

                return results;
            }
        });

        // Check we get the correct tags back
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
            @Override
            public Void execute() throws Throwable
            {
                // Now we'll recover these blogposts & we should expect to find the same tags.
                Set<String> expectedTags = new HashSet<String>();
                expectedTags.addAll(tags);

                PagingRequest pagingReq = new PagingRequest(0, 10, null);

                PagingResults<BlogPostInfo> pagedResults = BLOG_SERVICE.getDrafts(BLOG_CONTAINER_NODE, ADMIN_USER, pagingReq);
                assertEquals("Wrong number of blog posts", tags.size(), pagedResults.getPage().size());

                for (BlogPostInfo bpi : pagedResults.getPage())
                {
                    NodeRef blogNode = bpi.getNodeRef();
                    List<String> recoveredTags = TAGGING_SERVICE.getTags(blogNode);
                    assertEquals("Wrong number of tags", 1, recoveredTags.size());

                    String tag = recoveredTags.get(0);
                    assertTrue("Tag found on node but not expected: " + tag, expectedTags.remove(tag));
                }
                assertTrue("Not all tags were recovered from a blogpost", expectedTags.isEmpty());

                return null;
            }
        });

        // Check we can find the posts by their tags
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
            @Override
            public Void execute() throws Throwable
            {
                PagingRequest pagingReq = new PagingRequest(0, 10, null);
                RangedDateProperty dates = new RangedDateProperty(null, null, ContentModel.PROP_CREATED);
                for (String tag : tags)
                {
                    PagingResults<BlogPostInfo> pagedResults = BLOG_SERVICE.findBlogPosts(BLOG_CONTAINER_NODE, dates, tag, pagingReq);

                    // Check we found our post
                    assertEquals("Wrong number of blog posts for " + tag, 1, pagedResults.getPage().size());
                }
                return null;
            }
        });
    }

    /**
     * This test method uses the eventually consistent find*() method and so may fail if Lucene is disabled.
     */
    @Category(RedundantTests.class)
    @Test
    public void findBlogPostsByPublishedDate() throws Exception
    {
        final List<String> tags = Arrays.asList(new String[]{"hello", "goodbye"});

        // Going to set some specific published dates on these blog posts & query by date.
        final Calendar cal = Calendar.getInstance();
        cal.set(1971, 6, 15);
        final Date _1971 = cal.getTime();
        cal.set(1975, 0, 1);
        final Date _1975 = cal.getTime();
        cal.set(1980, 0, 1);
        final Date _1980 = cal.getTime();
        cal.set(1981, 0, 1);
        final Date _1981 = cal.getTime();
        cal.set(1985, 6, 15);
        final Date _1985 = cal.getTime();
        cal.set(1991, 6, 15);
        final Date _1991 = cal.getTime();

        final Map<Integer, NodeRef> blogPosts = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Map<Integer, NodeRef>>() {
            @Override
            public Map<Integer, NodeRef> execute() throws Throwable
            {
                Map<Integer, NodeRef> result = new HashMap<Integer, NodeRef>();

                // Create some blog posts. They'll all be published 'now' of course...
                final BlogPostInfo blogPost1971 = BLOG_SERVICE.createBlogPost(BLOG_CONTAINER_NODE, "publishedPostWithTags1971", "Hello world", true);
                final BlogPostInfo blogPost1981 = BLOG_SERVICE.createBlogPost(BLOG_CONTAINER_NODE, "publishedPostWithTags1981", "Hello world", true);
                final BlogPostInfo blogPost1991 = BLOG_SERVICE.createBlogPost(BLOG_CONTAINER_NODE, "publishedPostWithTags1991", "Hello world", true);

                TAGGING_SERVICE.addTags(blogPost1971.getNodeRef(), tags);
                TAGGING_SERVICE.addTags(blogPost1981.getNodeRef(), tags);
                TAGGING_SERVICE.addTags(blogPost1991.getNodeRef(), tags);

                testNodesToTidy.add(blogPost1971.getNodeRef());
                testNodesToTidy.add(blogPost1981.getNodeRef());
                testNodesToTidy.add(blogPost1991.getNodeRef());

                // We need to 'cheat' and set the nodes' cm:published dates to specific values.
                NODE_SERVICE.setProperty(blogPost1971.getNodeRef(), ContentModel.PROP_PUBLISHED, _1971);
                NODE_SERVICE.setProperty(blogPost1981.getNodeRef(), ContentModel.PROP_PUBLISHED, _1981);
                NODE_SERVICE.setProperty(blogPost1991.getNodeRef(), ContentModel.PROP_PUBLISHED, _1991);

                result.put(1971, blogPost1971.getNodeRef());
                result.put(1981, blogPost1981.getNodeRef());
                result.put(1991, blogPost1991.getNodeRef());

                return result;
            }
        });

        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
            @SuppressWarnings("deprecation")
            @Override
            public Void execute() throws Throwable
            {
                // Quick sanity check: Did our cheating with the cm:created dates work?
                assertEquals("Incorrect published date", 71, ((Date) NODE_SERVICE.getProperty(blogPosts.get(1971), ContentModel.PROP_PUBLISHED)).getYear());

                PagingRequest pagingReq = new PagingRequest(0, 10, null);

                final RangedDateProperty publishedBefore1980 = new RangedDateProperty(null, _1980, ContentModel.PROP_PUBLISHED);
                final RangedDateProperty publishedAfter1980 = new RangedDateProperty(_1980, null, ContentModel.PROP_PUBLISHED);
                final RangedDateProperty publishedBetween1975And1985 = new RangedDateProperty(_1975, _1985, ContentModel.PROP_PUBLISHED);

                // Find all
                PagingResults<BlogPostInfo> pagedResults = BLOG_SERVICE.findBlogPosts(BLOG_CONTAINER_NODE, null, null, pagingReq);
                assertEquals("Wrong number of blog posts", 3, pagedResults.getPage().size());
                Set<NodeRef> recoveredBlogNodes = new HashSet<NodeRef>();
                for (BlogPostInfo bpi : pagedResults.getPage())
                {
                    recoveredBlogNodes.add(bpi.getNodeRef());
                }

                assertTrue("Missing expected BlogPost NodeRef 71", recoveredBlogNodes.contains(blogPosts.get(1971)));
                assertTrue("Missing expected BlogPost NodeRef 81", recoveredBlogNodes.contains(blogPosts.get(1981)));
                assertTrue("Missing expected BlogPost NodeRef 91", recoveredBlogNodes.contains(blogPosts.get(1991)));

                // Find posts before date
                pagedResults = BLOG_SERVICE.findBlogPosts(BLOG_CONTAINER_NODE, publishedBefore1980, null, pagingReq);
                assertEquals("Wrong blog post count", 1, pagedResults.getPage().size());

                NodeRef blogNode = pagedResults.getPage().get(0).getNodeRef();
                assertEquals("Incorrect NodeRef.", blogNode, blogPosts.get(1971));

                List<String> recoveredTags = TAGGING_SERVICE.getTags(blogNode);
                assertEquals("Incorrect tags.", tags, recoveredTags);

                // Find posts after date
                pagedResults = BLOG_SERVICE.findBlogPosts(BLOG_CONTAINER_NODE, publishedAfter1980, "hello", pagingReq);
                assertEquals("Wrong blog post count", 2, pagedResults.getPage().size());

                blogNode = pagedResults.getPage().get(0).getNodeRef();
                assertEquals("Incorrect NodeRef.", blogNode, blogPosts.get(1981));

                // Find posts between dates
                pagedResults = BLOG_SERVICE.findBlogPosts(BLOG_CONTAINER_NODE, publishedBetween1975And1985, "hello", pagingReq);
                assertEquals("Wrong blog post count", 1, pagedResults.getPage().size());

                blogNode = pagedResults.getPage().get(0).getNodeRef();
                assertEquals("Incorrect NodeRef.", blogNode, blogPosts.get(1981));

                return null;
            }
        });
    }

    @Test
    public void ensureBlogPostsAreCorrectlySorted() throws Exception
    {
        final int testBlogCount = 3;

        // Set up some test data to check sorting. We don't need to retain references to these posts.
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
            @Override
            public Void execute() throws Throwable
            {
                // Create some blog posts. They'll all be published 'now' but the slight delay between each should ensure they
                // are given distinct creation dates
                final long slightDelay = 50;

                for (int i = 0; i < testBlogCount; i++)
                {
                    BlogPostInfo newDraft = BLOG_SERVICE.createBlogPost(BLOG_CONTAINER_NODE, "draftPost_ensureBlogPostsAreCorrectlySorted" + i, "x", true);

                    Thread.sleep(slightDelay);

                    // And the same for some published posts...
                    BlogPostInfo newPublished = BLOG_SERVICE.createBlogPost(BLOG_CONTAINER_NODE, "publishedPost_ensureBlogPostsAreCorrectlySorted" + i, "x", false);

                    Thread.sleep(slightDelay);

                    testNodesToTidy.add(newDraft.getNodeRef());
                    testNodesToTidy.add(newPublished.getNodeRef());
                }

                return null;
            }
        });

        final PagingRequest pagingReq = new PagingRequest(100);

        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
            @SuppressWarnings("deprecation")
            @Override
            public Void execute() throws Throwable
            {
                String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();

                // get DRAFTS
                PagingResults<BlogPostInfo> resultsPage = BLOG_SERVICE.getDrafts(BLOG_CONTAINER_NODE, currentUser, pagingReq);
                List<BlogPostInfo> blogPosts = resultsPage.getPage();
                assertTrue("Expected more draft blog posts than " + blogPosts.size(),
                        blogPosts.size() >= testBlogCount);

                assertSortingIsCorrect(blogPosts);

                // And the published ones
                resultsPage = BLOG_SERVICE.getPublished(BLOG_CONTAINER_NODE, null, null, currentUser, pagingReq); // Date filtering tested elsewhere.
                blogPosts = resultsPage.getPage();
                assertTrue("Expected more published blog posts than " + blogPosts.size(),
                        blogPosts.size() >= testBlogCount);

                assertSortingIsCorrect(blogPosts);

                // And the combination. This should be ordered:
                // published posts, most recent cm:published first - followed by
                // draft posts, most recent cm:created first
                System.out.println("  getMyDraftsAndAllPublished");

                resultsPage = BLOG_SERVICE.getMyDraftsAndAllPublished(BLOG_CONTAINER_NODE, null, null, pagingReq);
                blogPosts = resultsPage.getPage();

                assertSortingIsCorrect(blogPosts);

                return null;
            }
        });
    }

    private void assertSortingIsCorrect(List<BlogPostInfo> blogPosts)
    {
        // Sometimes you just have to see the data...
        for (BlogPostInfo bpi : blogPosts)
        {
            System.out.println("  -----");
            Date published = (Date) NODE_SERVICE.getProperty(bpi.getNodeRef(), ContentModel.PROP_PUBLISHED);
            Date created = (Date) NODE_SERVICE.getProperty(bpi.getNodeRef(), ContentModel.PROP_CREATED);
            System.out.print("    published: " + (published == null ? "             " : published.getTime()));
            System.out.println("    created  : " + created.getTime());
        }

        for (int i = 0; i < blogPosts.size() - 1; i++) // We only want to iterate to the second-last item
        {
            BlogPostInfo nextBPI = blogPosts.get(i);
            BlogPostInfo followingBPI = blogPosts.get(i + 1);

            Date nextPublishedDate = (Date) NODE_SERVICE.getProperty(nextBPI.getNodeRef(), ContentModel.PROP_PUBLISHED);
            Date followingPublishedDate = (Date) NODE_SERVICE.getProperty(followingBPI.getNodeRef(), ContentModel.PROP_PUBLISHED);
            Date nextCreatedDate = (Date) NODE_SERVICE.getProperty(nextBPI.getNodeRef(), ContentModel.PROP_CREATED);
            Date followingCreatedDate = (Date) NODE_SERVICE.getProperty(followingBPI.getNodeRef(), ContentModel.PROP_CREATED);

            // published must precede draft
            if (nextPublishedDate == null && followingPublishedDate != null)
            {
                fail("Published posts must precede draft posts");
            }
            else if (nextPublishedDate != null && followingPublishedDate != null)
            {
                assertTrue("Error in BlogPostInfo sorting. Published dates in wrong order.", !nextPublishedDate.before(followingPublishedDate));
            }
            else if (nextPublishedDate == null && followingPublishedDate == null)
            {
                assertTrue("Error in BlogPostInfo sorting. Created dates in wrong order.", !nextCreatedDate.before(followingCreatedDate));
            }
        }
    }

    /**
     * This test uses two different users to create draft and internally published blog posts. Then it ensures that each user sees the correct posts when they retrieve them from the service.
     */
    @Test
    public void multipleUsersCreateDraftsAndPublishedPostsAndBrowse() throws Exception
    {
        // Admin creates a draft and an internally-published blog post.
        final Pair<NodeRef, NodeRef> adminPosts = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Pair<NodeRef, NodeRef>>() {

            @Override
            public Pair<NodeRef, NodeRef> execute() throws Throwable
            {
                BlogPostInfo newDraftBlogPost = BLOG_SERVICE.createBlogPost(BLOG_CONTAINER_NODE, "adminDraft", "", true);
                testNodesToTidy.add(newDraftBlogPost.getNodeRef());

                BlogPostInfo newPublishedBlogPost = BLOG_SERVICE.createBlogPost(BLOG_CONTAINER_NODE, "adminPublished", "", false);
                testNodesToTidy.add(newPublishedBlogPost.getNodeRef());

                return new Pair<NodeRef, NodeRef>(newDraftBlogPost.getNodeRef(), newPublishedBlogPost.getNodeRef());
            }
        });

        // Then another user does the same.
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
        final Pair<NodeRef, NodeRef> userPosts = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Pair<NodeRef, NodeRef>>() {

            @Override
            public Pair<NodeRef, NodeRef> execute() throws Throwable
            {
                BlogPostInfo newDraftBlogPost = BLOG_SERVICE.createBlogPost(BLOG_CONTAINER_NODE, "userDraft", "", true);
                testNodesToTidy.add(newDraftBlogPost.getNodeRef());

                BlogPostInfo newPublishedBlogPost = BLOG_SERVICE.createBlogPost(BLOG_CONTAINER_NODE, "userPublished", "", false);
                testNodesToTidy.add(newPublishedBlogPost.getNodeRef());

                return new Pair<NodeRef, NodeRef>(newDraftBlogPost.getNodeRef(), newPublishedBlogPost.getNodeRef());
            }
        });

        // Now check what we see from the service.
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);

        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
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

        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
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

    @Test
    public void getBlogPostsFilteredByDateRange() throws Exception
    {
        final int numberOfPosts = 31 + 31 + 29;

        final List<NodeRef> posts = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<List<NodeRef>>() {
            @Override
            public List<NodeRef> execute() throws Throwable
            {
                List<NodeRef> results = new ArrayList<NodeRef>();

                for (int i = 0; i < numberOfPosts; i++)
                {
                    BlogPostInfo newBlogPost = BLOG_SERVICE.createBlogPost(BLOG_CONTAINER_NODE, "date-specific-post" + i, "", false);
                    testNodesToTidy.add(newBlogPost.getNodeRef());

                    results.add(newBlogPost.getNodeRef());
                }

                return results;
            }
        });

        // Now go through and set their creation dates to specific points in the past.
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
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

        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
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

    /**
     * Test that correct paging info is returned when searching for tagged blog posts.
     */
    @Test
    @Category(RedundantTests.class)
    public void testGetBlogPostsByTagPaging() throws Exception
    {
        final String tagToSearchBy = "testtag";
        final int numberOfBlogPostsTagged = 2;
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<List<NodeRef>>() {
            @Override
            public List<NodeRef> execute() throws Throwable
            {
                List<NodeRef> results = new ArrayList<NodeRef>();

                do
                {
                    final String blogTitle = "blogTitle" + GUID.generate();
                    BlogPostInfo newBlogPost = BLOG_SERVICE.createBlogPost(BLOG_CONTAINER_NODE, blogTitle, "Hello world", false);
                    TAGGING_SERVICE.addTags(newBlogPost.getNodeRef(), Arrays.asList(new String[]{tagToSearchBy}));
                    testNodesToTidy.add(newBlogPost.getNodeRef());
                    results.add(newBlogPost.getNodeRef());
                } while (results.size() < numberOfBlogPostsTagged);

                return results;
            }
        });

        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
            @Override
            public Void execute() throws Throwable
            {
                PagingRequest pagingReq = new PagingRequest(0, 1, null);
                RangedDateProperty dates = new RangedDateProperty(null, null, ContentModel.PROP_CREATED);
                PagingResults<BlogPostInfo> pagedResults = BLOG_SERVICE.findBlogPosts(BLOG_CONTAINER_NODE, dates, tagToSearchBy, pagingReq);

                assertEquals("Wrong number of blog posts on page 1 for " + tagToSearchBy, 1, pagedResults.getPage().size());

                assertEquals("Wrong total number of blog posts for " + tagToSearchBy, new Pair<Integer, Integer>(2, 2), pagedResults.getTotalResultCount());

                assertEquals("There should still be blog posts available to be retrieved for " + tagToSearchBy, true, pagedResults.hasMoreItems());

                pagingReq = new PagingRequest(1, 1, null);
                pagedResults = BLOG_SERVICE.findBlogPosts(BLOG_CONTAINER_NODE, dates, tagToSearchBy, pagingReq);

                assertEquals("Wrong number of blog posts on page 2 for " + tagToSearchBy, 1, pagedResults.getPage().size());

                assertEquals("Wrong total number of blog posts for " + tagToSearchBy, new Pair<Integer, Integer>(2, 2), pagedResults.getTotalResultCount());

                assertEquals("All blog posts should have been retrieved by now for " + tagToSearchBy, false, pagedResults.hasMoreItems());
                return null;
            }
        });
    }

    private static void createUser(final String userName)
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
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
}
