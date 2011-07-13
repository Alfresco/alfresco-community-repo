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
package org.alfresco.repo.calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
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
import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarEntryDTO;
import org.alfresco.service.cmr.calendar.CalendarService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Test cases for {@link CalendarServiceImpl}.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class CalendarServiceImplTest
{
    private static final String TEST_SITE_PREFIX = "CalendarSiteTest";

    private static final ApplicationContext testContext = ApplicationContextHelper.getApplicationContext();
    
    // injected services
    private static MutableAuthenticationService AUTHENTICATION_SERVICE;
    private static BehaviourFilter              BEHAVIOUR_FILTER;
    private static CalendarService              CALENDAR_SERVICE;
    private static DictionaryService            DICTIONARY_SERVICE;
    private static NodeService                  NODE_SERVICE;
    private static NodeService                  PUBLIC_NODE_SERVICE;
    private static PersonService                PERSON_SERVICE;
    private static RetryingTransactionHelper    TRANSACTION_HELPER;
    private static PermissionService            PERMISSION_SERVICE;
    private static SiteService                  SITE_SERVICE;
    private static TaggingService               TAGGING_SERVICE;
    
    private static final String TEST_USER = CalendarServiceImplTest.class.getSimpleName() + "_testuser";
    private static final String ADMIN_USER = AuthenticationUtil.getAdminUserName();

    private static SiteInfo CALENDAR_SITE;
    private static SiteInfo ALTERNATE_CALENDAR_SITE;
    
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
        CALENDAR_SERVICE       = (CalendarService)testContext.getBean("CalendarService");
        DICTIONARY_SERVICE     = (DictionaryService)testContext.getBean("dictionaryService");
        NODE_SERVICE           = (NodeService)testContext.getBean("nodeService");
        PUBLIC_NODE_SERVICE    = (NodeService)testContext.getBean("NodeService");
        PERSON_SERVICE         = (PersonService)testContext.getBean("personService");
        TRANSACTION_HELPER     = (RetryingTransactionHelper)testContext.getBean("retryingTransactionHelper");
        PERMISSION_SERVICE     = (PermissionService)testContext.getBean("permissionService");
        SITE_SERVICE           = (SiteService)testContext.getBean("siteService");
        TAGGING_SERVICE        = (TaggingService)testContext.getBean("TaggingService");
        
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
        createUser(TEST_USER);
        
        // We need to create the test site as the test user so that they can contribute content to it in tests below.
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
        createTestSites();
    }
    
    @Test public void createNewEntry() throws Exception
    {
       CalendarEntry entry;
       
       // TODO List to check there aren't any yet
       
       // Get with an arbitrary name gives nothing
       entry = CALENDAR_SERVICE.getCalendarEntry(CALENDAR_SITE.getShortName(), "madeUp");
       assertEquals(null, entry); 
       
       entry = CALENDAR_SERVICE.getCalendarEntry(CALENDAR_SITE.getShortName(), "madeUp2");
       assertEquals(null, entry);
       
       
       // Create one
       entry = new CalendarEntryDTO(
             "Title", "Description", "Location", new Date(1), new Date(1234)
       );
       entry.setOutlook(true);
       entry.setOutlookUID("12345LookOut!");
       
       // Can't be got until saved
       assertEquals(null, entry.getSystemName());
       assertEquals(null, entry.getNodeRef());
       
       
       // Can't call update yet
       try
       {
          CALENDAR_SERVICE.updateCalendarEntry(entry);
          fail("Shouldn't be able to update a brand new entry");
       }
       catch(IllegalArgumentException e)
       {}

       
       // Have it saved
       entry = CALENDAR_SERVICE.createCalendarEntry(CALENDAR_SITE.getShortName(), entry);
       
       // Ensure it got a noderef, and the correct site
       assertNotNull(entry.getNodeRef());
       assertNotNull(entry.getSystemName());
       
       NodeRef container = NODE_SERVICE.getPrimaryParent(entry.getNodeRef()).getParentRef();
       NodeRef site = NODE_SERVICE.getPrimaryParent(container).getParentRef();
       assertEquals(CALENDAR_SITE.getNodeRef(), site);
       
       
       // Check the details on the object
       assertEquals("Title", entry.getTitle());
       assertEquals("Description", entry.getDescription());
       assertEquals("Location", entry.getLocation());
       assertEquals(1, entry.getStart().getTime());
       assertEquals(1234, entry.getEnd().getTime());
       assertEquals(null, entry.getRecurrenceRule());
       assertEquals(null, entry.getLastRecurrence());
       assertEquals(true, entry.isOutlook());
       assertEquals("12345LookOut!", entry.getOutlookUID());
       
       
       // Fetch it, and check the details
       entry = CALENDAR_SERVICE.getCalendarEntry(CALENDAR_SITE.getShortName(), entry.getSystemName());
       assertEquals("Title", entry.getTitle());
       assertEquals("Description", entry.getDescription());
       assertEquals("Location", entry.getLocation());
       assertEquals(1, entry.getStart().getTime());
       assertEquals(1234, entry.getEnd().getTime());
       assertEquals(null, entry.getRecurrenceRule());
       assertEquals(null, entry.getLastRecurrence());
       assertEquals(true, entry.isOutlook());
       assertEquals("12345LookOut!", entry.getOutlookUID());
       
       
       // Mark it as done with
       testNodesToTidy.add(entry.getNodeRef());
    }
    
    @Test public void createUpdateDeleteEntry() throws Exception
    {
       CalendarEntry entry;
       
       
       // Create an entry
       entry = new CalendarEntryDTO(
             "Title", "Description", "Location", new Date(1), new Date(1234)
       );
       entry.setOutlook(true);
       entry.setOutlookUID("12345LookOut!");
       entry = CALENDAR_SERVICE.createCalendarEntry(CALENDAR_SITE.getShortName(), entry);
       
       
       // Check it
       assertEquals("Title", entry.getTitle());
       assertEquals("Description", entry.getDescription());
       assertEquals("Location", entry.getLocation());
       assertEquals(1, entry.getStart().getTime());
       assertEquals(1234, entry.getEnd().getTime());
       assertEquals(null, entry.getRecurrenceRule());
       assertEquals(null, entry.getLastRecurrence());
       assertEquals(true, entry.isOutlook());
       assertEquals("12345LookOut!", entry.getOutlookUID());
       
       
       // Change it
       entry.setTitle("New Title");
       entry.setStart(new Date(1234567));
       entry.setEnd(new Date(1294567));
       entry.setRecurrenceRule("1w");
       entry.setLastRecurrence(new Date(1234567));
       entry.setOutlook(false);
       entry.setOutlookUID(null);
       
       CALENDAR_SERVICE.updateCalendarEntry(entry);
       
       
       // Fetch, and check
       entry = CALENDAR_SERVICE.getCalendarEntry(CALENDAR_SITE.getShortName(), entry.getSystemName());
       assertEquals("New Title", entry.getTitle());
       assertEquals("Description", entry.getDescription());
       assertEquals("Location", entry.getLocation());
       assertEquals(1234567, entry.getStart().getTime());
       assertEquals(1294567, entry.getEnd().getTime());
       assertEquals("1w", entry.getRecurrenceRule());
       assertEquals(1234567, entry.getLastRecurrence().getTime());
       assertEquals(false, entry.isOutlook());
       assertEquals(null, entry.getOutlookUID());
       
       
       // Delete it
       CALENDAR_SERVICE.deleteCalendarEntry(entry);
       
       // Check it went
       assertEquals(null, CALENDAR_SERVICE.getCalendarEntry(CALENDAR_SITE.getShortName(), entry.getSystemName()));
       
       
       // Finally, check the all day flag detection
       Calendar c = Calendar.getInstance();
       c.set(Calendar.HOUR_OF_DAY, 0);
       c.set(Calendar.MINUTE, 0);
       c.set(Calendar.SECOND, 0);
       c.set(Calendar.MILLISECOND, 0);
       
       // Neither start nor end are at midnight to start
       assertEquals(false, CalendarEntryDTO.isAllDay(entry));
       
       // Set the start to midnight
       entry.setStart(c.getTime());
       assertEquals(false, CalendarEntryDTO.isAllDay(entry));
       
       // And end, will then count as all day
       entry.setEnd(c.getTime());
       assertEquals(true, CalendarEntryDTO.isAllDay(entry));
    }
    
    /**
     * Ensures that when we try to write an entry to the
     *  container of a new site, it is correctly setup for us.
     * This test does it's own transactions
     */
    @Test public void newContainerSetup() throws Exception
    {
       final String TEST_SITE_NAME = "CalendarTestNewTestSite";
       
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
             assertFalse(SITE_SERVICE.hasContainer(TEST_SITE_NAME, CalendarServiceImpl.CALENDAR_COMPONENT));

             // Create a calendar entry
             CalendarEntry entry = new CalendarEntryDTO(
                   "Title", "Description", "Location", new Date(1), new Date(1234)
             );
             CALENDAR_SERVICE.createCalendarEntry(TEST_SITE_NAME, entry);

             // It will now exist
             assertTrue(SITE_SERVICE.hasContainer(TEST_SITE_NAME, CalendarServiceImpl.CALENDAR_COMPONENT));

             // It'll be a tag scope too
             NodeRef container = SITE_SERVICE.getContainer(TEST_SITE_NAME, CalendarServiceImpl.CALENDAR_COMPONENT);
             assertTrue(TAGGING_SERVICE.isTagScope(container));

             // Tidy up
             SITE_SERVICE.deleteSite(TEST_SITE_NAME);
             return null;
          }
       });
    }
    
    @Test public void tagging() throws Exception
    {
       CalendarEntry entry;
       final String TAG_1 = "calendar_tag_1";
       final String TAG_2 = "calendar_tag_2";
       final String TAG_3 = "calendar_tag_3";
       
       // Create one without tagging
       entry = new CalendarEntryDTO(
             "Title", "Description", "Location", new Date(1), new Date(1234)
       );
       entry = CALENDAR_SERVICE.createCalendarEntry(CALENDAR_SITE.getShortName(), entry);
       
       // Check
       assertEquals(0, entry.getTags().size());
       
       entry = CALENDAR_SERVICE.getCalendarEntry(CALENDAR_SITE.getShortName(), entry.getSystemName());       
       assertEquals(0, entry.getTags().size());
       
       
       // Update it to have tags
       entry.getTags().add(TAG_1);
       entry.getTags().add(TAG_2);
       entry.getTags().add(TAG_1);
       assertEquals(3, entry.getTags().size());
       CALENDAR_SERVICE.updateCalendarEntry(entry);
       
       // Check
       entry = CALENDAR_SERVICE.getCalendarEntry(CALENDAR_SITE.getShortName(), entry.getSystemName());       
       assertEquals(2, entry.getTags().size());
       assertEquals(true, entry.getTags().contains(TAG_1));
       assertEquals(true, entry.getTags().contains(TAG_2));
       assertEquals(false, entry.getTags().contains(TAG_3));
       
       
       // Update it to have different tags
       entry.getTags().remove(TAG_2);
       entry.getTags().add(TAG_3);
       entry.getTags().add(TAG_1);
       CALENDAR_SERVICE.updateCalendarEntry(entry);
       
       // Check
       entry = CALENDAR_SERVICE.getCalendarEntry(CALENDAR_SITE.getShortName(), entry.getSystemName());       
       assertEquals(2, entry.getTags().size());
       assertEquals(true, entry.getTags().contains(TAG_1));
       assertEquals(false, entry.getTags().contains(TAG_2));
       assertEquals(true, entry.getTags().contains(TAG_3));

       
       // Update it to have no tags
       entry.getTags().clear();
       CALENDAR_SERVICE.updateCalendarEntry(entry);
       
       // Check
       entry = CALENDAR_SERVICE.getCalendarEntry(CALENDAR_SITE.getShortName(), entry.getSystemName());       
       assertEquals(0, entry.getTags().size());

       
       // Update it to have tags again
       entry.getTags().add(TAG_1);
       entry.getTags().add(TAG_2);
       entry.getTags().add(TAG_3);
       CALENDAR_SERVICE.updateCalendarEntry(entry);
       
       // Check
       entry = CALENDAR_SERVICE.getCalendarEntry(CALENDAR_SITE.getShortName(), entry.getSystemName());       
       assertEquals(3, entry.getTags().size());
       assertEquals(true, entry.getTags().contains(TAG_1));
       assertEquals(true, entry.getTags().contains(TAG_2));
       assertEquals(true, entry.getTags().contains(TAG_3));
       
       // Tidy
       CALENDAR_SERVICE.deleteCalendarEntry(entry);
       
       
       // Create an event with tags
       entry = new CalendarEntryDTO(
             "Title", "Description", "Location", new Date(1), new Date(1234)
       );
       entry.getTags().add(TAG_1);
       entry.getTags().add(TAG_1);
       entry.getTags().add(TAG_2);
       entry = CALENDAR_SERVICE.createCalendarEntry(CALENDAR_SITE.getShortName(), entry);
       
       // Check
       entry = CALENDAR_SERVICE.getCalendarEntry(CALENDAR_SITE.getShortName(), entry.getSystemName());       
       assertEquals(2, entry.getTags().size());
       assertEquals(true, entry.getTags().contains(TAG_1));
       assertEquals(true, entry.getTags().contains(TAG_2));
       assertEquals(false, entry.getTags().contains(TAG_3));
       
       
       // Update it to have different tags
       entry.getTags().remove(TAG_2);
       entry.getTags().add(TAG_3);
       entry.getTags().add(TAG_1);
       CALENDAR_SERVICE.updateCalendarEntry(entry);
       
       // Check 
       entry = CALENDAR_SERVICE.getCalendarEntry(CALENDAR_SITE.getShortName(), entry.getSystemName());       
       assertEquals(2, entry.getTags().size());
       assertEquals(true, entry.getTags().contains(TAG_1));
       assertEquals(false, entry.getTags().contains(TAG_2));
       assertEquals(true, entry.getTags().contains(TAG_3));
       
       // Tidy
       CALENDAR_SERVICE.deleteCalendarEntry(entry);
    }
    
    /**
     * Simplest tests for listing on just one site, with no filtering 
     */
    @Test public void calendarSingleSiteListing() throws Exception
    {
       PagingRequest paging = new PagingRequest(10);
       
       // Nothing to start
       PagingResults<CalendarEntry> results = 
          CALENDAR_SERVICE.listCalendarEntries(CALENDAR_SITE.getShortName(), paging);
       assertEquals(0, results.getPage().size());
       
       // Add a few
       CALENDAR_SERVICE.createCalendarEntry(CALENDAR_SITE.getShortName(), new CalendarEntryDTO(
             "TitleA", "Description", "Location", new Date(1302431400), new Date(1302435000)
       ));
       CALENDAR_SERVICE.createCalendarEntry(CALENDAR_SITE.getShortName(), new CalendarEntryDTO(
             "TitleB", "Description", "Location", new Date(1302431400), new Date(1302442200)
       ));
       CALENDAR_SERVICE.createCalendarEntry(CALENDAR_SITE.getShortName(), new CalendarEntryDTO(
             "TitleC", "Description", "Location", new Date(1302435000), new Date(1302442200)
       ));
       
       // Check now
       results = CALENDAR_SERVICE.listCalendarEntries(CALENDAR_SITE.getShortName(), paging);
       assertEquals(3, results.getPage().size());
       assertEquals("TitleA", results.getPage().get(0).getTitle());
       assertEquals("TitleB", results.getPage().get(1).getTitle());
       assertEquals("TitleC", results.getPage().get(2).getTitle());
       
       // Add one more, before those, and drop the page size 
       CALENDAR_SERVICE.createCalendarEntry(CALENDAR_SITE.getShortName(), new CalendarEntryDTO(
             "TitleD", "Description", "Location", new Date(1302417000), new Date(1302420600)
       ));
       
       paging = new PagingRequest(3);
       results = CALENDAR_SERVICE.listCalendarEntries(CALENDAR_SITE.getShortName(), paging);
       assertEquals(3, results.getPage().size());
       assertEquals("TitleD", results.getPage().get(0).getTitle());
       assertEquals("TitleA", results.getPage().get(1).getTitle());
       assertEquals("TitleB", results.getPage().get(2).getTitle());
       
       paging = new PagingRequest(3, 3);
       results = CALENDAR_SERVICE.listCalendarEntries(CALENDAR_SITE.getShortName(), paging);
       assertEquals(1, results.getPage().size());
       assertEquals("TitleC", results.getPage().get(0).getTitle());
       
       
       // Tidy
       paging = new PagingRequest(10);
       results = CALENDAR_SERVICE.listCalendarEntries(CALENDAR_SITE.getShortName(), paging);
       for(CalendarEntry entry : results.getPage())
       {
          testNodesToTidy.add(entry.getNodeRef());
       }
    }

    @Test public void calendarMultiSiteListing() throws Exception
    {
       // TODO
    }

    /**
     * Checks that the correct permission checking occurs on fetching
     *  calendar listings (which go through canned queries)
     * TODO FIX
     */
    public void DISABLEDcalendarListingPermissionsChecking() throws Exception
    {
       PagingRequest paging = new PagingRequest(10);
       PagingResults<CalendarEntry> results;
     
       // TODO This shouldn't be needed...
       PERMISSION_SERVICE.clearPermission(ALTERNATE_CALENDAR_SITE.getNodeRef(), TEST_USER);
       System.err.println(PERMISSION_SERVICE.getPermissions(ALTERNATE_CALENDAR_SITE.getNodeRef()));
       System.err.println(PERMISSION_SERVICE.getAllSetPermissions(ALTERNATE_CALENDAR_SITE.getNodeRef()));
       System.err.println(NODE_SERVICE.getChildAssocs(ALTERNATE_CALENDAR_SITE.getNodeRef()));
       System.err.println(PUBLIC_NODE_SERVICE.getChildAssocs(ALTERNATE_CALENDAR_SITE.getNodeRef()));
       
       
       // Nothing to start with in either site
       results = CALENDAR_SERVICE.listCalendarEntries(CALENDAR_SITE.getShortName(), paging);
//       assertEquals(0, results.getPage().size());
       results = CALENDAR_SERVICE.listCalendarEntries(ALTERNATE_CALENDAR_SITE.getShortName(), paging);
       assertEquals(0, results.getPage().size());

       // Double check that we're only allowed to see the 1st site
       assertEquals(true,  SITE_SERVICE.isMember(CALENDAR_SITE.getShortName(), TEST_USER));
       assertEquals(false, SITE_SERVICE.isMember(ALTERNATE_CALENDAR_SITE.getShortName(), TEST_USER));
       assertEquals(AccessStatus.ALLOWED, PERMISSION_SERVICE.hasReadPermission(CALENDAR_SITE.getNodeRef()));
       assertEquals(AccessStatus.DENIED,  PERMISSION_SERVICE.hasReadPermission(ALTERNATE_CALENDAR_SITE.getNodeRef()));

       
       // Add two events to one site and three to the other
       CALENDAR_SERVICE.createCalendarEntry(CALENDAR_SITE.getShortName(), new CalendarEntryDTO(
             "TitleA", "Description", "Location", new Date(1302431400), new Date(1302435000)
       ));
       CALENDAR_SERVICE.createCalendarEntry(CALENDAR_SITE.getShortName(), new CalendarEntryDTO(
             "TitleB", "Description", "Location", new Date(1302431400), new Date(1302442200)
       ));
       
       CALENDAR_SERVICE.createCalendarEntry(ALTERNATE_CALENDAR_SITE.getShortName(), new CalendarEntryDTO(
             "PrivateTitleA", "Description", "Location", new Date(1302431400), new Date(1302435000)
       ));
       CALENDAR_SERVICE.createCalendarEntry(ALTERNATE_CALENDAR_SITE.getShortName(), new CalendarEntryDTO(
             "PrivateTitleB", "Description", "Location", new Date(1302431400), new Date(1302442200)
       ));
       NodeRef priv3 = CALENDAR_SERVICE.createCalendarEntry(ALTERNATE_CALENDAR_SITE.getShortName(), new CalendarEntryDTO(
             "PrivateTitleC", "Description", "Location", new Date(1302431400), new Date(1302442200)
       )).getNodeRef();
       
       
       // Check again, as we're not in the 2nd site won't see any there  
       results = CALENDAR_SERVICE.listCalendarEntries(CALENDAR_SITE.getShortName(), paging);
       assertEquals(2, results.getPage().size());
       results = CALENDAR_SERVICE.listCalendarEntries(ALTERNATE_CALENDAR_SITE.getShortName(), paging);
       assertEquals(0, results.getPage().size());
       
       
       // Join the site, now we can see both
       SITE_SERVICE.setMembership(ALTERNATE_CALENDAR_SITE.getShortName(), TEST_USER, SiteModel.SITE_CONTRIBUTOR);
       
       results = CALENDAR_SERVICE.listCalendarEntries(CALENDAR_SITE.getShortName(), paging);
       assertEquals(2, results.getPage().size());
       results = CALENDAR_SERVICE.listCalendarEntries(ALTERNATE_CALENDAR_SITE.getShortName(), paging);
       assertEquals(3, results.getPage().size());
       
       
       // Explicitly remove their permissions from one node, check it vanishes from the list
       PERMISSION_SERVICE.setInheritParentPermissions(priv3, false);
       PERMISSION_SERVICE.clearPermission(priv3, TEST_USER);
       
       results = CALENDAR_SERVICE.listCalendarEntries(CALENDAR_SITE.getShortName(), paging);
       assertEquals(2, results.getPage().size());
       results = CALENDAR_SERVICE.listCalendarEntries(ALTERNATE_CALENDAR_SITE.getShortName(), paging);
       assertEquals(2, results.getPage().size());
       
       
       // Leave, they go away again
       SITE_SERVICE.removeMembership(ALTERNATE_CALENDAR_SITE.getShortName(), TEST_USER);
       
       results = CALENDAR_SERVICE.listCalendarEntries(CALENDAR_SITE.getShortName(), paging);
       assertEquals(2, results.getPage().size());
       results = CALENDAR_SERVICE.listCalendarEntries(ALTERNATE_CALENDAR_SITE.getShortName(), paging);
       assertEquals(0, results.getPage().size());
       
       
       // Tidy
       paging = new PagingRequest(10);
       results = CALENDAR_SERVICE.listCalendarEntries(CALENDAR_SITE.getShortName(), paging);
       for(CalendarEntry entry : results.getPage())
       {
          testNodesToTidy.add(entry.getNodeRef());
       }
    }
    
    private static void createTestSites() throws Exception
    {
        CALENDAR_SITE = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<SiteInfo>()
           {
              @Override
              public SiteInfo execute() throws Throwable
              {
                  SiteInfo site = SITE_SERVICE.createSite(
                        TEST_SITE_PREFIX, 
                        CalendarServiceImplTest.class.getSimpleName() + "_testSite" + System.currentTimeMillis(),
                        "test site title", "test site description", 
                        SiteVisibility.PUBLIC
                  );
                  CLASS_TEST_NODES_TO_TIDY.add(site.getNodeRef());
                  return site;
              }
         });
        
         // Create the alternate site as admin
         AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
         ALTERNATE_CALENDAR_SITE = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<SiteInfo>()
            {
               @Override
               public SiteInfo execute() throws Throwable
               {
                  SiteInfo site = SITE_SERVICE.createSite(
                        TEST_SITE_PREFIX, 
                        CalendarServiceImplTest.class.getSimpleName() + "_testAltSite" + System.currentTimeMillis(),
                        "alternate site title", "alternate site description", 
                        SiteVisibility.PRIVATE
                  );
                  SITE_SERVICE.createContainer(
                        site.getShortName(), CalendarServiceImpl.CALENDAR_COMPONENT, null, null 
                  );
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
