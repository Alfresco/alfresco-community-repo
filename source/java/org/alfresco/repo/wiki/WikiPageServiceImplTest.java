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
package org.alfresco.repo.wiki;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.links.LinkInfo;
import org.alfresco.service.cmr.links.LinksService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.cmr.wiki.WikiPageInfo;
import org.alfresco.service.cmr.wiki.WikiPageService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Test cases for {@link WikiPageServiceImpl}.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class WikiPageServiceImplTest
{
    private static final String TEST_SITE_PREFIX = "WikiSiteTest";
    private static final long ONE_DAY_MS = 24*60*60*1000;

    private static final ApplicationContext testContext = ApplicationContextHelper.getApplicationContext();
    
    // injected services
    private static MutableAuthenticationService AUTHENTICATION_SERVICE;
    private static BehaviourFilter              BEHAVIOUR_FILTER;
    private static WikiPageService              WIKI_PAGE_SERVICE;
    private static DictionaryService            DICTIONARY_SERVICE;
    private static NodeService                  NODE_SERVICE;
    private static NodeService                  PUBLIC_NODE_SERVICE;
    private static PersonService                PERSON_SERVICE;
    private static RetryingTransactionHelper    TRANSACTION_HELPER;
    private static TransactionService           TRANSACTION_SERVICE;
    private static PermissionService            PERMISSION_SERVICE;
    private static SiteService                  SITE_SERVICE;
    private static TaggingService               TAGGING_SERVICE;
    
    private static final String TEST_USER = WikiPageServiceImplTest.class.getSimpleName() + "_testuser";
    private static final String ADMIN_USER = AuthenticationUtil.getAdminUserName();

    private static SiteInfo WIKI_SITE;
    private static SiteInfo ALTERNATE_WIKI_SITE;
    
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
        WIKI_PAGE_SERVICE      = (WikiPageService)testContext.getBean("WikiPageService");
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
    
    @Test public void createNewEntry() throws Exception
    {
       WikiPageInfo page;
       
       // Nothing to start with
       PagingResults<WikiPageInfo> results = 
          WIKI_PAGE_SERVICE.listWikiPages(WIKI_SITE.getShortName(), new PagingRequest(10));
       assertEquals(0, results.getPage().size());

       
       // Get with an arbitrary name gives nothing
       page = WIKI_PAGE_SERVICE.getWikiPage(WIKI_SITE.getShortName(), "madeUp");
       assertEquals(null, page); 
       
       page = WIKI_PAGE_SERVICE.getWikiPage(WIKI_SITE.getShortName(), "madeUp2");
       assertEquals(null, page);
       
       // Create one
       page = WIKI_PAGE_SERVICE.createWikiPage(
             WIKI_SITE.getShortName(), "Title", "This Is Some Content"
       );
       
       
       // Ensure it got a noderef, and the correct site
       assertNotNull(page.getNodeRef());
       assertNotNull(page.getSystemName());
       
       NodeRef container = NODE_SERVICE.getPrimaryParent(page.getNodeRef()).getParentRef();
       NodeRef site = NODE_SERVICE.getPrimaryParent(container).getParentRef();
       assertEquals(WIKI_SITE.getNodeRef(), site);
       
       
       // Check the details on the object
       assertEquals("Title", page.getTitle());
       assertEquals("This Is Some Content", page.getContents());
       assertEquals(ADMIN_USER, page.getCreator());
       assertEquals(0, page.getTags().size());
       
       
       // Fetch it, and check the details
       page = WIKI_PAGE_SERVICE.getWikiPage(WIKI_SITE.getShortName(), page.getSystemName());
       assertEquals("Title", page.getTitle());
       assertEquals("This Is Some Content", page.getContents());
       assertEquals(ADMIN_USER, page.getCreator());
       assertEquals(0, page.getTags().size());
       
       
       // Mark it as done with
       testNodesToTidy.add(page.getNodeRef());
    }
    
    @Test public void createUpdateDeleteEntry() throws Exception
    {
       WikiPageInfo page;
       
       // Run as the test user instead
       AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
       
       
       // Create a link
       page = WIKI_PAGE_SERVICE.createWikiPage(
             WIKI_SITE.getShortName(), "Title", "This Is Some Content"
       );
       
       
       // Check it
       assertEquals("Title", page.getSystemName());
       assertEquals("Title", page.getTitle());
       assertEquals("This Is Some Content", page.getContents());
       assertEquals(TEST_USER, page.getCreator());
       assertEquals(0, page.getTags().size());
       
       
       // Change it
       page.setTitle("New Title");
       page.setContents("This is new content");
       
       page = WIKI_PAGE_SERVICE.updateWikiPage(page);
       
       
       // Fetch, and check
       page = WIKI_PAGE_SERVICE.getWikiPage(WIKI_SITE.getShortName(), page.getSystemName());
       assertEquals("New Title", page.getSystemName());
       assertEquals("New Title", page.getTitle());
       assertEquals("This is new content", page.getContents());
       assertEquals(TEST_USER, page.getCreator());
       assertEquals(0, page.getTags().size());
       
       
       // Delete it
       WIKI_PAGE_SERVICE.deleteWikiPage(page);
       
       // Check it went
       assertEquals(null, WIKI_PAGE_SERVICE.getWikiPage(WIKI_SITE.getShortName(), page.getSystemName()));
    }
    
    /**
     * Ensures that when we try to write an entry to the
     *  container of a new site, it is correctly setup for us.
     * This test does it's own transactions
     */
    @Test public void newContainerSetup() throws Exception
    {
       final String TEST_SITE_NAME = "WikiTestNewTestSite";
       
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
             assertFalse(SITE_SERVICE.hasContainer(TEST_SITE_NAME, WikiPageServiceImpl.WIKI_COMPONENT));

             // Create a link
             WIKI_PAGE_SERVICE.createWikiPage(
                   TEST_SITE_NAME, "Title", "TextTextText"
             );
             
             // It will now exist
             assertTrue(SITE_SERVICE.hasContainer(TEST_SITE_NAME, WikiPageServiceImpl.WIKI_COMPONENT));

             // It'll be a tag scope too
             NodeRef container = SITE_SERVICE.getContainer(TEST_SITE_NAME, WikiPageServiceImpl.WIKI_COMPONENT);
             assertTrue(TAGGING_SERVICE.isTagScope(container));

             // Tidy up
             SITE_SERVICE.deleteSite(TEST_SITE_NAME);
             return null;
          }
       });
    }
    
//    @Test public void tagging() throws Exception
//    {
//       LinkInfo link;
//       final String TAG_1 = "link_tag_1";
//       final String TAG_2 = "link_tag_2";
//       final String TAG_3 = "link_tag_3";
//       
//       // Create one without tagging
//       link = WIKI_PAGE_SERVICE.createLink(
//             WIKI_SITE.getShortName(), "Title", "Description",
//             "http://www.alfresco.com/", false
//       );
//       testNodesToTidy.add(link.getNodeRef());
//       
//       // Check
//       assertEquals(0, link.getTags().size());
//       
//       link = WIKI_PAGE_SERVICE.getLink(WIKI_SITE.getShortName(), link.getSystemName());       
//       assertEquals(0, link.getTags().size());
//       
//       
//       // Update it to have tags
//       link.getTags().add(TAG_1);
//       link.getTags().add(TAG_2);
//       link.getTags().add(TAG_1);
//       assertEquals(3, link.getTags().size());
//       WIKI_PAGE_SERVICE.updateLink(link);
//       
//       // Check
//       link = WIKI_PAGE_SERVICE.getLink(WIKI_SITE.getShortName(), link.getSystemName());       
//       assertEquals(2, link.getTags().size());
//       assertEquals(true, link.getTags().contains(TAG_1));
//       assertEquals(true, link.getTags().contains(TAG_2));
//       assertEquals(false, link.getTags().contains(TAG_3));
//       
//       
//       // Update it to have different tags
//       link.getTags().remove(TAG_2);
//       link.getTags().add(TAG_3);
//       link.getTags().add(TAG_1);
//       WIKI_PAGE_SERVICE.updateLink(link);
//       
//       // Check it as-is
//       assertEquals(3, link.getTags().size()); // Includes duplicate tag until re-loaded
//       assertEquals(true, link.getTags().contains(TAG_1));
//       assertEquals(false, link.getTags().contains(TAG_2));
//       assertEquals(true, link.getTags().contains(TAG_3));
//       
//       // Now load and re-check
//       link = WIKI_PAGE_SERVICE.getLink(WIKI_SITE.getShortName(), link.getSystemName());       
//       assertEquals(2, link.getTags().size()); // Duplicate now gone
//       assertEquals(true, link.getTags().contains(TAG_1));
//       assertEquals(false, link.getTags().contains(TAG_2));
//       assertEquals(true, link.getTags().contains(TAG_3));
//
//       
//       // Update it to have no tags
//       link.getTags().clear();
//       WIKI_PAGE_SERVICE.updateLink(link);
//       
//       // Check
//       link = WIKI_PAGE_SERVICE.getLink(WIKI_SITE.getShortName(), link.getSystemName());       
//       assertEquals(0, link.getTags().size());
//
//       
//       // Update it to have tags again
//       link.getTags().add(TAG_1);
//       link.getTags().add(TAG_2);
//       link.getTags().add(TAG_3);
//       WIKI_PAGE_SERVICE.updateLink(link);
//       
//       // Check
//       link = WIKI_PAGE_SERVICE.getLink(WIKI_SITE.getShortName(), link.getSystemName());       
//       assertEquals(3, link.getTags().size());
//       assertEquals(true, link.getTags().contains(TAG_1));
//       assertEquals(true, link.getTags().contains(TAG_2));
//       assertEquals(true, link.getTags().contains(TAG_3));
//       
//       // Tidy
//       WIKI_PAGE_SERVICE.deleteLink(link);
//    }
//    
//    /**
//     * Tests for listing the links of a site, possibly by user or date range
//     */
//    @Test public void linksListing() throws Exception
//    {
//       PagingRequest paging = new PagingRequest(10);
//       AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
//       
//       // Nothing to start with
//       PagingResults<LinkInfo> results = 
//          WIKI_PAGE_SERVICE.listLinks(WIKI_SITE.getShortName(), paging);
//       assertEquals(0, results.getPage().size());
//       
//       // Add a few
//       LinkInfo linkA = WIKI_PAGE_SERVICE.createLink(
//             WIKI_SITE.getShortName(), "TitleA", "Description",
//             "http://www.alfresco.com/", false
//       );
//       LinkInfo linkB = WIKI_PAGE_SERVICE.createLink(
//             WIKI_SITE.getShortName(), "TitleB", "Description",
//             "http://www.alfresco.com/", false
//       );
//       LinkInfo linkC = WIKI_PAGE_SERVICE.createLink(
//             WIKI_SITE.getShortName(), "TitleC", "Description",
//             "http://www.alfresco.com/", false
//       );
//       testNodesToTidy.add(linkA.getNodeRef());
//       testNodesToTidy.add(linkB.getNodeRef());
//       testNodesToTidy.add(linkC.getNodeRef());
//       
//       // Check now, should be newest first
//       results = WIKI_PAGE_SERVICE.listLinks(WIKI_SITE.getShortName(), paging);
//       assertEquals(3, results.getPage().size());
//       assertEquals("TitleC", results.getPage().get(0).getTitle());
//       assertEquals("TitleB", results.getPage().get(1).getTitle());
//       assertEquals("TitleA", results.getPage().get(2).getTitle());
//       
//       // Add one more, as a different user, and drop the page size
//       AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
//       LinkInfo linkD = WIKI_PAGE_SERVICE.createLink(
//             WIKI_SITE.getShortName(), "TitleD", "Description",
//             "http://www.alfresco.com/", false
//       );
//       testNodesToTidy.add(linkD.getNodeRef());
//       AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
//       
//       paging = new PagingRequest(3);
//       results = WIKI_PAGE_SERVICE.listLinks(WIKI_SITE.getShortName(), paging);
//       assertEquals(3, results.getPage().size());
//       assertEquals("TitleD", results.getPage().get(0).getTitle());
//       assertEquals("TitleC", results.getPage().get(1).getTitle());
//       assertEquals("TitleB", results.getPage().get(2).getTitle());
//       
//       paging = new PagingRequest(3, 3);
//       results = WIKI_PAGE_SERVICE.listLinks(WIKI_SITE.getShortName(), paging);
//       assertEquals(1, results.getPage().size());
//       assertEquals("TitleA", results.getPage().get(0).getTitle());
//       
//       
//       // Now check filtering by user
//       paging = new PagingRequest(10);
//       
//       results = WIKI_PAGE_SERVICE.listLinks(WIKI_SITE.getShortName(), TEST_USER, paging);
//       assertEquals(3, results.getPage().size());
//       assertEquals("TitleC", results.getPage().get(0).getTitle());
//       assertEquals("TitleB", results.getPage().get(1).getTitle());
//       assertEquals("TitleA", results.getPage().get(2).getTitle());
//       
//       results = WIKI_PAGE_SERVICE.listLinks(WIKI_SITE.getShortName(), ADMIN_USER, paging);
//       assertEquals(1, results.getPage().size());
//       assertEquals("TitleD", results.getPage().get(0).getTitle());
//
//       
//       // Now check filtering by date range
//       pushCreatedDateBack(linkB, 10);
//       pushCreatedDateBack(linkC, 100);
//       
//       Date today = new Date();
//       Date tomorrow = new Date(today.getTime()+ONE_DAY_MS);
//       Date yesterday = new Date(today.getTime()-ONE_DAY_MS);
//       Date twoWeeksAgo = new Date(today.getTime()-14*ONE_DAY_MS);
//       
//       // Very recent ones
//       results = WIKI_PAGE_SERVICE.listLinks(WIKI_SITE.getShortName(), yesterday, tomorrow, paging);
//       assertEquals(2, results.getPage().size());
//       assertEquals("TitleD", results.getPage().get(0).getTitle());
//       assertEquals("TitleA", results.getPage().get(1).getTitle());
//       
//       // Fairly old ones
//       results = WIKI_PAGE_SERVICE.listLinks(WIKI_SITE.getShortName(), twoWeeksAgo, yesterday, paging);
//       assertEquals(1, results.getPage().size());
//       assertEquals("TitleB", results.getPage().get(0).getTitle());
//       
//       // Fairly old to current
//       results = WIKI_PAGE_SERVICE.listLinks(WIKI_SITE.getShortName(), twoWeeksAgo, tomorrow, paging);
//       assertEquals(3, results.getPage().size());
//       assertEquals("TitleD", results.getPage().get(0).getTitle());
//       assertEquals("TitleA", results.getPage().get(1).getTitle());
//       assertEquals("TitleB", results.getPage().get(2).getTitle());
//
//       
//       // Tidy
//       paging = new PagingRequest(10);
//       AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
//       results = WIKI_PAGE_SERVICE.listLinks(WIKI_SITE.getShortName(), paging);
//       for(LinkInfo link : results.getPage())
//       {
//          PUBLIC_NODE_SERVICE.deleteNode(link.getNodeRef());
//       }
//       results = WIKI_PAGE_SERVICE.listLinks(ALTERNATE_WIKI_SITE.getShortName(), paging);
//       for(LinkInfo link : results.getPage())
//       {
//          PUBLIC_NODE_SERVICE.deleteNode(link.getNodeRef());
//       }
//    }
//
//    /**
//     * Checks that the correct permission checking occurs on fetching
//     *  links listings (which go through canned queries)
//     */
//    @Test public void linksListingPermissionsChecking() throws Exception
//    {
//       PagingRequest paging = new PagingRequest(10);
//       PagingResults<LinkInfo> results;
//     
//       // Nothing to start with in either site
//       results = WIKI_PAGE_SERVICE.listLinks(WIKI_SITE.getShortName(), paging);
//       assertEquals(0, results.getPage().size());
//       results = WIKI_PAGE_SERVICE.listLinks(ALTERNATE_WIKI_SITE.getShortName(), paging);
//       assertEquals(0, results.getPage().size());
//
//       // Double check that we're only allowed to see the 1st site
//       assertEquals(true,  SITE_SERVICE.isMember(WIKI_SITE.getShortName(), TEST_USER));
//       assertEquals(false, SITE_SERVICE.isMember(ALTERNATE_WIKI_SITE.getShortName(), TEST_USER));
//       
//       
//       // Now become the test user
//       AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
//
//       
//       // Add two events to one site and three to the other
//       // Note - add the events as a different user for the site that the
//       //  test user isn't a member of!
//       LinkInfo linkA = WIKI_PAGE_SERVICE.createLink(
//             WIKI_SITE.getShortName(), "TitleA", "Description",
//             "http://www.alfresco.com/", false
//       );
//       LinkInfo linkB = WIKI_PAGE_SERVICE.createLink(
//             WIKI_SITE.getShortName(), "TitleB", "Description",
//             "http://www.alfresco.com/", false
//       );
//       testNodesToTidy.add(linkA.getNodeRef());
//       testNodesToTidy.add(linkB.getNodeRef());
//       
//       AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
//       LinkInfo linkPrivA = WIKI_PAGE_SERVICE.createLink(
//             ALTERNATE_WIKI_SITE.getShortName(), "PrivTitleA", "Description",
//             "http://team.alfresco.com/", false
//       );
//       LinkInfo linkPrivB = WIKI_PAGE_SERVICE.createLink(
//             ALTERNATE_WIKI_SITE.getShortName(), "PrivTitleB", "Description",
//             "http://team.alfresco.com/", false
//       );
//       LinkInfo linkPrivC = WIKI_PAGE_SERVICE.createLink(
//             ALTERNATE_WIKI_SITE.getShortName(), "PrivTitleC", "Description",
//             "http://team.alfresco.com/", false
//       );
//       testNodesToTidy.add(linkPrivA.getNodeRef());
//       testNodesToTidy.add(linkPrivB.getNodeRef());
//       testNodesToTidy.add(linkPrivC.getNodeRef());
//       AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
//       
//       
//       // Check again, as we're not in the 2nd site won't see any there
//       results = WIKI_PAGE_SERVICE.listLinks(WIKI_SITE.getShortName(), paging);
//       assertEquals(2, results.getPage().size());
//       results = WIKI_PAGE_SERVICE.listLinks(ALTERNATE_WIKI_SITE.getShortName(), paging);
//       assertEquals(0, results.getPage().size());
//       
//       
//       // Join the site, now we can see both
//       TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
//       {
//          @Override
//          public Void execute() throws Throwable
//          {
//             AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
//             SITE_SERVICE.setMembership(ALTERNATE_WIKI_SITE.getShortName(), TEST_USER, SiteModel.SITE_COLLABORATOR);
//             AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
//             return null;
//          }
//       });
//       
//       results = WIKI_PAGE_SERVICE.listLinks(WIKI_SITE.getShortName(), paging);
//       assertEquals(2, results.getPage().size());
//       results = WIKI_PAGE_SERVICE.listLinks(ALTERNATE_WIKI_SITE.getShortName(), paging);
//       assertEquals(3, results.getPage().size());
//       
//       
//       // Explicitly remove their permissions from one node, check it vanishes from the list
//       PERMISSION_SERVICE.setInheritParentPermissions(linkPrivC.getNodeRef(), false);
//       PERMISSION_SERVICE.clearPermission(linkPrivC.getNodeRef(), TEST_USER);
//       
//       results = WIKI_PAGE_SERVICE.listLinks(WIKI_SITE.getShortName(), paging);
//       assertEquals(2, results.getPage().size());
//       results = WIKI_PAGE_SERVICE.listLinks(ALTERNATE_WIKI_SITE.getShortName(), paging);
//       assertEquals(2, results.getPage().size());
//       
//       
//       // Leave, they go away again
//       TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
//       {
//          @Override
//          public Void execute() throws Throwable
//          {
//             AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
//             SITE_SERVICE.removeMembership(ALTERNATE_WIKI_SITE.getShortName(), TEST_USER);
//             AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
//             return null;
//          }
//       });
//       
//       results = WIKI_PAGE_SERVICE.listLinks(WIKI_SITE.getShortName(), paging);
//       assertEquals(2, results.getPage().size());
//       results = WIKI_PAGE_SERVICE.listLinks(ALTERNATE_WIKI_SITE.getShortName(), paging);
//       assertEquals(0, results.getPage().size());
//
//
//       // Tidy
//       paging = new PagingRequest(10);
//       AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
//       results = WIKI_PAGE_SERVICE.listLinks(WIKI_SITE.getShortName(), paging);
//       for(LinkInfo link : results.getPage())
//       {
//          PUBLIC_NODE_SERVICE.deleteNode(link.getNodeRef());
//       }
//       results = WIKI_PAGE_SERVICE.listLinks(ALTERNATE_WIKI_SITE.getShortName(), paging);
//       for(LinkInfo link : results.getPage())
//       {
//          PUBLIC_NODE_SERVICE.deleteNode(link.getNodeRef());
//       }
//    }
    
    
    // --------------------------------------------------------------------------------

    
    /**
     * Alters the created date on a wiki page for testing
     */
    private void pushCreatedDateBack(WikiPageInfo page, int daysAgo) throws Exception
    {
       final NodeRef node = page.getNodeRef();
       
       final Date created = page.getCreatedAt();
       final Date newCreated = new Date(created.getTime() - daysAgo*ONE_DAY_MS);
       
       // Update the created date
       TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
          @Override
          public Void execute() throws Throwable {
             BEHAVIOUR_FILTER.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
             NODE_SERVICE.setProperty(node, ContentModel.PROP_CREATED, newCreated);
             return null;
          }
       }, false, true);
       // Change something else too in the public nodeservice, to force a re-index
       TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
          @Override
          public Void execute() throws Throwable {
             BEHAVIOUR_FILTER.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
             PUBLIC_NODE_SERVICE.setProperty(node, ContentModel.PROP_CREATED, newCreated);
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
             return null;
          }
       }, false, true);
       
       // Update the object itself
       ((WikiPageInfoImpl)page).setCreatedAt(newCreated);
    }
    
    private static void createTestSites() throws Exception
    {
        final WikiPageServiceImpl privateWikiPageService = (WikiPageServiceImpl)testContext.getBean("wikiPageService");
        
        WIKI_SITE = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<SiteInfo>()
           {
              @Override
              public SiteInfo execute() throws Throwable
              {
                  SiteInfo site = SITE_SERVICE.createSite(
                        TEST_SITE_PREFIX, 
                        WikiPageServiceImplTest.class.getSimpleName() + "_testSite" + System.currentTimeMillis(),
                        "test site title", "test site description", 
                        SiteVisibility.PUBLIC
                  );
                  privateWikiPageService.getSiteWikiContainer(site.getShortName(), true);
                  CLASS_TEST_NODES_TO_TIDY.add(site.getNodeRef());
                  return site;
              }
         });
        
         // Create the alternate site as admin
         AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
         ALTERNATE_WIKI_SITE = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<SiteInfo>()
            {
               @Override
               public SiteInfo execute() throws Throwable
               {
                  SiteInfo site = SITE_SERVICE.createSite(
                        TEST_SITE_PREFIX, 
                        WikiPageServiceImplTest.class.getSimpleName() + "_testAltSite" + System.currentTimeMillis(),
                        "alternate site title", "alternate site description", 
                        SiteVisibility.PRIVATE
                  );
                  privateWikiPageService.getSiteWikiContainer(site.getShortName(), true);
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
