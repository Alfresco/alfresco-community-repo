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
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.calendar.cannedqueries.CalendarEntity;
import org.alfresco.repo.calendar.cannedqueries.GetCalendarEntriesCannedQuery;
import org.alfresco.repo.calendar.cannedqueries.GetCalendarEntriesCannedQueryFactory;
import org.alfresco.repo.calendar.cannedqueries.GetCalendarEntriesCannedQueryTestHook;
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
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.registry.NamedObjectRegistry;
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
    private static GetCalendarEntriesCannedQueryFactory CALENDAR_CQ_FACTORY;
    
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

        // Get the canned query registry, and from that the factory
        NamedObjectRegistry<CannedQueryFactory<? extends Object>> calendarCannedQueryRegistry =
           (NamedObjectRegistry<CannedQueryFactory<? extends Object>>)testContext.getBean("calendarCannedQueryRegistry");
        CALENDAR_CQ_FACTORY = (GetCalendarEntriesCannedQueryFactory)
           calendarCannedQueryRegistry.getNamedObject(CalendarServiceImpl.CANNED_QUERY_GET_ENTRIES);
        
        // Do the setup as admin
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
        createUser(TEST_USER);
        
        // We need to create the test site as the test user so that they can contribute content to it in tests below.
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
        createTestSites();
    }
    
    @Test public void createNewEntry() throws Exception
    {
       CalendarEntry entry;
       
       // Nothing to start with
       PagingResults<CalendarEntry> results = 
          CALENDAR_SERVICE.listCalendarEntries(CALENDAR_SITE.getShortName(), new PagingRequest(10));
       assertEquals(0, results.getPage().size());

       
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
       testNodesToTidy.add(entry.getNodeRef());
       
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
       
       // Check it as-is
       assertEquals(3, entry.getTags().size()); // Includes duplicate tag until re-loaded
       assertEquals(true, entry.getTags().contains(TAG_1));
       assertEquals(false, entry.getTags().contains(TAG_2));
       assertEquals(true, entry.getTags().contains(TAG_3));
       
       // Now load and re-check
       entry = CALENDAR_SERVICE.getCalendarEntry(CALENDAR_SITE.getShortName(), entry.getSystemName());
       assertEquals(2, entry.getTags().size()); // Duplicate now gone
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
       
       // Nothing to start with
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

    /**
     * Checks that the correct permission checking occurs on fetching
     *  calendar listings (which go through canned queries)
     */
    @Test public void calendarListingPermissionsChecking() throws Exception
    {
       PagingRequest paging = new PagingRequest(10);
       PagingResults<CalendarEntry> results;
     
       // Nothing to start with in either site
       results = CALENDAR_SERVICE.listCalendarEntries(CALENDAR_SITE.getShortName(), paging);
       assertEquals(0, results.getPage().size());
       results = CALENDAR_SERVICE.listCalendarEntries(ALTERNATE_CALENDAR_SITE.getShortName(), paging);
       assertEquals(0, results.getPage().size());

       // Double check that we're only allowed to see the 1st site
       assertEquals(true,  SITE_SERVICE.isMember(CALENDAR_SITE.getShortName(), TEST_USER));
       assertEquals(false, SITE_SERVICE.isMember(ALTERNATE_CALENDAR_SITE.getShortName(), TEST_USER));

       
       // Add two events to one site and three to the other
       // Note - add the events as a different user for the site that the
       //  test user isn't a member of!
       CALENDAR_SERVICE.createCalendarEntry(CALENDAR_SITE.getShortName(), new CalendarEntryDTO(
             "TitleA", "Description", "Location", new Date(1302431400), new Date(1302435000)
       ));
       CALENDAR_SERVICE.createCalendarEntry(CALENDAR_SITE.getShortName(), new CalendarEntryDTO(
             "TitleB", "Description", "Location", new Date(1302431400), new Date(1302442200)
       ));
       
       AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
       CALENDAR_SERVICE.createCalendarEntry(ALTERNATE_CALENDAR_SITE.getShortName(), new CalendarEntryDTO(
             "PrivateTitleA", "Description", "Location", new Date(1302431400), new Date(1302435000)
       ));
       CALENDAR_SERVICE.createCalendarEntry(ALTERNATE_CALENDAR_SITE.getShortName(), new CalendarEntryDTO(
             "PrivateTitleB", "Description", "Location", new Date(1302431400), new Date(1302442200)
       ));
       NodeRef priv3 = CALENDAR_SERVICE.createCalendarEntry(ALTERNATE_CALENDAR_SITE.getShortName(), new CalendarEntryDTO(
             "PrivateTitleC", "Description", "Location", new Date(1302431400), new Date(1302442200)
       )).getNodeRef();
       AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
       
       
       // Check again, as we're not in the 2nd site won't see any there  
       results = CALENDAR_SERVICE.listCalendarEntries(CALENDAR_SITE.getShortName(), paging);
       assertEquals(2, results.getPage().size());
       results = CALENDAR_SERVICE.listCalendarEntries(ALTERNATE_CALENDAR_SITE.getShortName(), paging);
       assertEquals(0, results.getPage().size());
       
       
       // Join the site, now we can see both
       TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
       {
          @Override
          public Void execute() throws Throwable
          {
             AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
             SITE_SERVICE.setMembership(ALTERNATE_CALENDAR_SITE.getShortName(), TEST_USER, SiteModel.SITE_COLLABORATOR);
             AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
             return null;
          }
       });
       
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
       TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
       {
          @Override
          public Void execute() throws Throwable
          {
             AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
             SITE_SERVICE.removeMembership(ALTERNATE_CALENDAR_SITE.getShortName(), TEST_USER);
             AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
             return null;
          }
       });
       
       results = CALENDAR_SERVICE.listCalendarEntries(CALENDAR_SITE.getShortName(), paging);
       assertEquals(2, results.getPage().size());
       results = CALENDAR_SERVICE.listCalendarEntries(ALTERNATE_CALENDAR_SITE.getShortName(), paging);
       assertEquals(0, results.getPage().size());
       
       
       // Tidy
       AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
       paging = new PagingRequest(10);
       results = CALENDAR_SERVICE.listCalendarEntries(CALENDAR_SITE.getShortName(), paging);
       for(CalendarEntry entry : results.getPage())
       {
          testNodesToTidy.add(entry.getNodeRef());
       }
       results = CALENDAR_SERVICE.listCalendarEntries(ALTERNATE_CALENDAR_SITE.getShortName(), paging);
       for(CalendarEntry entry : results.getPage())
       {
          testNodesToTidy.add(entry.getNodeRef());
       }
       AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
    }
    
    /**
     * Test that we can retrieve (with date filtering) events from
     *  multiple sites
     */
    @Test public void calendarMultiSiteListing() throws Exception
    {
       PagingRequest paging = new PagingRequest(10);
       PagingResults<CalendarEntry> results;
       
       
       // Nothing to start
       results = CALENDAR_SERVICE.listCalendarEntries(CALENDAR_SITE.getShortName(), paging);
       assertEquals(0, results.getPage().size());
       results = CALENDAR_SERVICE.listCalendarEntries(ALTERNATE_CALENDAR_SITE.getShortName(), paging);
       assertEquals(0, results.getPage().size());
       
       results = CALENDAR_SERVICE.listCalendarEntries(new String[] {
             CALENDAR_SITE.getShortName(), ALTERNATE_CALENDAR_SITE.getShortName()}, paging);
       assertEquals(0, results.getPage().size());
       
       
       // You can pass invalid names in too, won't affect things
       results = CALENDAR_SERVICE.listCalendarEntries(new String[] {
             CALENDAR_SITE.getShortName(), ALTERNATE_CALENDAR_SITE.getShortName(),
             "MadeUpNumber1", "MadeUpTwo", "MadeUp3"}, paging);
       assertEquals(0, results.getPage().size());
       
       
       // Now add some events to one site
       NodeRef c1 = CALENDAR_SERVICE.createCalendarEntry(CALENDAR_SITE.getShortName(), new CalendarEntryDTO(
             "TitleA", "Description", "Location", new Date(1302431400), new Date(1302442200)
       )).getNodeRef();
       NodeRef c2 = CALENDAR_SERVICE.createCalendarEntry(CALENDAR_SITE.getShortName(), new CalendarEntryDTO(
             "TitleB", "Description", "Location", new Date(1302435000), new Date(1302435000)
       )).getNodeRef();
       NodeRef c3 = CALENDAR_SERVICE.createCalendarEntry(CALENDAR_SITE.getShortName(), new CalendarEntryDTO(
             "TitleC", "Description", "Location", new Date(1302431400), new Date(1302435000)
       )).getNodeRef();
       testNodesToTidy.add(c1);
       testNodesToTidy.add(c2);
       testNodesToTidy.add(c3);

       
       // Check
       results = CALENDAR_SERVICE.listCalendarEntries(new String[] {
             CALENDAR_SITE.getShortName(), ALTERNATE_CALENDAR_SITE.getShortName()}, paging);
       assertEquals(3, results.getPage().size());
       
       // Should be date ordered, from then too
       assertEquals("TitleC", results.getPage().get(0).getTitle()); // Same start as A, earlier end
       assertEquals("TitleA", results.getPage().get(1).getTitle());
       assertEquals("TitleB", results.getPage().get(2).getTitle());

       
       // Add some to the other site, which the user isn't a member of
       AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
       NodeRef ca1 = CALENDAR_SERVICE.createCalendarEntry(ALTERNATE_CALENDAR_SITE.getShortName(), new CalendarEntryDTO(
             "PrivateTitleA", "Description", "Location", new Date(1302131400), new Date(1302135000)
       )).getNodeRef();
       NodeRef ca2 = CALENDAR_SERVICE.createCalendarEntry(ALTERNATE_CALENDAR_SITE.getShortName(), new CalendarEntryDTO(
             "PrivateTitleB", "Description", "Location", new Date(1302731400), new Date(1302472200)
       )).getNodeRef();
       AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
       testNodesToTidy.add(ca1);
       testNodesToTidy.add(ca2);
       
       // Our nodes are now, in start+end order:
       //  PrivateTitleA   1302131400 -> 1302135000
       //  TitleC          1302431400 -> 1302435000
       //  TitleA          1302431400 -> 1302442200
       //  TitleB          1302435000 -> 1302435000
       //  PrivateTitleB   1302731400 -> 1302472200
       
       
       // Check, they won't show up due to permissions
       results = CALENDAR_SERVICE.listCalendarEntries(new String[] {
             CALENDAR_SITE.getShortName(), ALTERNATE_CALENDAR_SITE.getShortName()}, paging);
       assertEquals(3, results.getPage().size());
       
       
       // Make a member of the site, they should now show up
       TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
       {
          @Override
          public Void execute() throws Throwable
          {
             AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
             SITE_SERVICE.setMembership(ALTERNATE_CALENDAR_SITE.getShortName(), TEST_USER, SiteModel.SITE_COLLABORATOR);
             AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
             return null;
          }
       });
       
       // Check we now see all the sites
       results = CALENDAR_SERVICE.listCalendarEntries(new String[] {
             CALENDAR_SITE.getShortName(), ALTERNATE_CALENDAR_SITE.getShortName()}, paging);
       assertEquals(5, results.getPage().size());

       // Should be date ordered, from then too
       assertEquals("PrivateTitleA", results.getPage().get(0).getTitle());
       assertEquals("TitleC", results.getPage().get(1).getTitle());
       assertEquals("TitleA", results.getPage().get(2).getTitle());
       assertEquals("TitleB", results.getPage().get(3).getTitle());
       assertEquals("PrivateTitleB", results.getPage().get(4).getTitle());
       
       
       // Filter by start date:
       
       // Date into the past
       results = CALENDAR_SERVICE.listCalendarEntries(new String[] {
             CALENDAR_SITE.getShortName(), ALTERNATE_CALENDAR_SITE.getShortName()},
             new Date(1300031400), null, paging);
       assertEquals(5, results.getPage().size());
       
       // Date in the middle, several just finishing
       results = CALENDAR_SERVICE.listCalendarEntries(new String[] {
             CALENDAR_SITE.getShortName(), ALTERNATE_CALENDAR_SITE.getShortName()},
             new Date(1302435000), null, paging);
       assertEquals(4, results.getPage().size());
       assertEquals("TitleC", results.getPage().get(0).getTitle());
       assertEquals("TitleA", results.getPage().get(1).getTitle());
       assertEquals("TitleB", results.getPage().get(2).getTitle());
       assertEquals("PrivateTitleB", results.getPage().get(3).getTitle());
       
       // Date in the middle, past the finish of many
       results = CALENDAR_SERVICE.listCalendarEntries(new String[] {
             CALENDAR_SITE.getShortName(), ALTERNATE_CALENDAR_SITE.getShortName()},
             new Date(1302441000), null, paging);
       assertEquals(2, results.getPage().size());
       assertEquals("TitleA", results.getPage().get(0).getTitle());
       assertEquals("PrivateTitleB", results.getPage().get(1).getTitle());
       
       // Date in the future
       results = CALENDAR_SERVICE.listCalendarEntries(new String[] {
             CALENDAR_SITE.getShortName(), ALTERNATE_CALENDAR_SITE.getShortName()},
             new Date(1400000000), null, paging);
       assertEquals(0, results.getPage().size());
       
       
       // Filter by end date:
       
       // Date in the past
       results = CALENDAR_SERVICE.listCalendarEntries(new String[] {
             CALENDAR_SITE.getShortName(), ALTERNATE_CALENDAR_SITE.getShortName()},
             null, new Date(1300031400), paging);
       assertEquals(0, results.getPage().size());
       
       // Date in the middle, with some touching on the end date
       results = CALENDAR_SERVICE.listCalendarEntries(new String[] {
             CALENDAR_SITE.getShortName(), ALTERNATE_CALENDAR_SITE.getShortName()},
             null, new Date(1302435000), paging);
       assertEquals(4, results.getPage().size());
       assertEquals("PrivateTitleA", results.getPage().get(0).getTitle());
       assertEquals("TitleC", results.getPage().get(1).getTitle());
       assertEquals("TitleA", results.getPage().get(2).getTitle());
       assertEquals("TitleB", results.getPage().get(3).getTitle());
       
       // Date in the middle, before the start date of several
       results = CALENDAR_SERVICE.listCalendarEntries(new String[] {
             CALENDAR_SITE.getShortName(), ALTERNATE_CALENDAR_SITE.getShortName()},
             null, new Date(1302432400), paging);
       assertEquals(3, results.getPage().size());
       assertEquals("PrivateTitleA", results.getPage().get(0).getTitle());
       assertEquals("TitleC", results.getPage().get(1).getTitle());
       assertEquals("TitleA", results.getPage().get(2).getTitle());
       
       // Date in the future
       results = CALENDAR_SERVICE.listCalendarEntries(new String[] {
             CALENDAR_SITE.getShortName(), ALTERNATE_CALENDAR_SITE.getShortName()},
             null, new Date(1400000000), paging);
       assertEquals(5, results.getPage().size());
       
       
       // Filter by both start and end
       results = CALENDAR_SERVICE.listCalendarEntries(new String[] {
             CALENDAR_SITE.getShortName(), ALTERNATE_CALENDAR_SITE.getShortName()},
             new Date(1302431400), new Date(1302432000), paging);
       assertEquals(2, results.getPage().size());
       assertEquals("TitleC", results.getPage().get(0).getTitle());
       assertEquals("TitleA", results.getPage().get(1).getTitle());
       
       results = CALENDAR_SERVICE.listCalendarEntries(new String[] {
             CALENDAR_SITE.getShortName(), ALTERNATE_CALENDAR_SITE.getShortName()},
             new Date(1302131400), new Date(1302432000), paging);
       assertEquals(3, results.getPage().size());
       assertEquals("PrivateTitleA", results.getPage().get(0).getTitle());
       assertEquals("TitleC", results.getPage().get(1).getTitle());
       assertEquals("TitleA", results.getPage().get(2).getTitle());
       
       
       // Filter on just one site, won't see from the other
       results = CALENDAR_SERVICE.listCalendarEntries(new String[] {
             CALENDAR_SITE.getShortName()},
             new Date(1302131400), new Date(1302432000), paging);
       assertEquals(2, results.getPage().size());
       assertEquals("TitleC", results.getPage().get(0).getTitle());
       assertEquals("TitleA", results.getPage().get(1).getTitle());
       
       results = CALENDAR_SERVICE.listCalendarEntries(new String[] {
             ALTERNATE_CALENDAR_SITE.getShortName()},
             new Date(1302131400), new Date(1302432000), paging);
       assertEquals(1, results.getPage().size());
       assertEquals("PrivateTitleA", results.getPage().get(0).getTitle());
    }
    
    /**
     * Ensure that the canned query returns the right entity objects
     *  for the underlying calendar entries.
     * Checks both the low level filtering, and the DB fetching of the
     *  properties used in the filter 
     */
    @Test public void testCannedQueryEntityResults() throws Exception
    {
       PagingRequest paging = new PagingRequest(10);
       NodeRef[] containers = new NodeRef[] {
             SITE_SERVICE.getContainer(CALENDAR_SITE.getShortName(), CalendarServiceImpl.CALENDAR_COMPONENT),
             SITE_SERVICE.getContainer(ALTERNATE_CALENDAR_SITE.getShortName(), CalendarServiceImpl.CALENDAR_COMPONENT),
       };
       Date from = new Date(1302431400);
       Date to = new Date(1302442200);
       
       
       // To capture the low level results
       final List<CalendarEntity> full = new ArrayList<CalendarEntity>();
       final List<CalendarEntity> filtered = new ArrayList<CalendarEntity>();
       GetCalendarEntriesCannedQueryTestHook hook = new GetCalendarEntriesCannedQueryTestHook()
       {
          @Override
          public void notifyComplete(List<CalendarEntity> fullList,
                List<CalendarEntity> filteredList) {
             full.clear();
             filtered.clear();
             full.addAll(fullList);
             filtered.addAll(filteredList);
          }
       };
       
       
       // With no entries, won't find anything
       GetCalendarEntriesCannedQuery cq = (GetCalendarEntriesCannedQuery)CALENDAR_CQ_FACTORY.getCannedQuery(
             containers, from, to, paging
       );
       cq.setTestHook(hook);
       cq.execute();
       
       assertEquals(0, full.size());
       assertEquals(0, filtered.size());
       
       
       // Add some events, with a mixture of repeating and non
       CalendarEntry c1 = CALENDAR_SERVICE.createCalendarEntry(CALENDAR_SITE.getShortName(), new CalendarEntryDTO(
             "SiteNormal", "Description", "Location", new Date(1302431400), new Date(1302442200)
       ));
       CalendarEntry c2 = CALENDAR_SERVICE.createCalendarEntry(CALENDAR_SITE.getShortName(), new CalendarEntryDTO(
             "SiteRepeating", "Description", "Location", new Date(1302435000), new Date(1302435000)
       ));
       CalendarEntry c3 = CALENDAR_SERVICE.createCalendarEntry(ALTERNATE_CALENDAR_SITE.getShortName(), new CalendarEntryDTO(
             "AltSiteNormal", "Description", "Location", new Date(1302431400), new Date(1302435000)
       ));
       
       
       // Do a fetch that'll include all of them
       cq = (GetCalendarEntriesCannedQuery)CALENDAR_CQ_FACTORY.getCannedQuery(
             containers, from, to, paging
       );
       cq.setTestHook(hook);
       cq.execute();
       
       assertEquals(3, full.size());
       assertEquals(3, filtered.size());
       
       // Check they have the right details on them, and are correctly sorted
       assertEquals(c3.getSystemName(),                      filtered.get(0).getName());
       assertEquals(ISO8601DateFormat.format(c3.getStart()), filtered.get(0).getFromDate());
       assertEquals(ISO8601DateFormat.format(c3.getEnd()),   filtered.get(0).getToDate());
       assertEquals(c3.getRecurrenceRule(),                  filtered.get(0).getRecurrenceRule());
       assertEquals(null,                                    filtered.get(0).getRecurrenceLastMeeting());
       
       assertEquals(c1.getSystemName(),                      filtered.get(1).getName());
       assertEquals(ISO8601DateFormat.format(c1.getStart()), filtered.get(1).getFromDate());
       assertEquals(ISO8601DateFormat.format(c1.getEnd()),   filtered.get(1).getToDate());
       assertEquals(c1.getRecurrenceRule(),                  filtered.get(1).getRecurrenceRule());
       assertEquals(null,                                    filtered.get(1).getRecurrenceLastMeeting());
       
       assertEquals(c2.getSystemName(),                      filtered.get(2).getName());
       assertEquals(ISO8601DateFormat.format(c2.getStart()), filtered.get(2).getFromDate());
       assertEquals(ISO8601DateFormat.format(c2.getEnd()),   filtered.get(2).getToDate());
       assertEquals(c2.getRecurrenceRule(),                  filtered.get(2).getRecurrenceRule());
       assertEquals(null,                                    filtered.get(2).getRecurrenceLastMeeting());
       
       
       // Now do one that'll only have some
       from = new Date(1302431400-10);
       to = new Date(1302431400+10);
       cq = (GetCalendarEntriesCannedQuery)CALENDAR_CQ_FACTORY.getCannedQuery(
             containers, from, to, paging
       );
       cq.setTestHook(hook);
       cq.execute();
       
       assertEquals(3, full.size());
       assertEquals(2, filtered.size());
       
       // Check the ordering and filtering
       assertEquals(c3.getSystemName(), filtered.get(0).getName());
       assertEquals(c1.getSystemName(), filtered.get(1).getName());
       
       
       // Now make one repeating and check the correct info comes through
       c3.setRecurrenceRule("FREQ=WEEKLY;BYDAY=TH;INTERVAL=1");
       c3.setLastRecurrence(new Date(1303431400));
       CALENDAR_SERVICE.updateCalendarEntry(c3);
       
       cq = (GetCalendarEntriesCannedQuery)CALENDAR_CQ_FACTORY.getCannedQuery(
             containers, from, to, paging
       );
       cq.setTestHook(hook);
       cq.execute();
       assertEquals(3, full.size());
       assertEquals(2, filtered.size());
       
       // Check the details
       assertEquals(c3.getSystemName(),                      filtered.get(0).getName());
       assertEquals(ISO8601DateFormat.format(c3.getStart()), filtered.get(0).getFromDate());
       assertEquals(ISO8601DateFormat.format(c3.getEnd()),   filtered.get(0).getToDate());
       assertEquals(ISO8601DateFormat.format(c3.getLastRecurrence()), filtered.get(0).getRecurrenceLastMeeting());
       assertEquals(c3.getRecurrenceRule(),                  filtered.get(0).getRecurrenceRule());
       
       assertEquals(c1.getSystemName(),                      filtered.get(1).getName());
       assertEquals(ISO8601DateFormat.format(c1.getStart()), filtered.get(1).getFromDate());
       assertEquals(ISO8601DateFormat.format(c1.getEnd()),   filtered.get(1).getToDate());
       assertEquals(c1.getRecurrenceRule(),                  filtered.get(1).getRecurrenceRule());
       assertEquals(null,                                    filtered.get(1).getRecurrenceLastMeeting());
       
       
       
       // Do a recurring query
       Calendar c20110718mon = Calendar.getInstance();
       Calendar c20110719tue = Calendar.getInstance();
       Calendar c20110720wed = Calendar.getInstance();
       Calendar c20110722fri = Calendar.getInstance();
       c20110718mon.set(2011, 7-1, 18, 0, 0, 0);
       c20110719tue.set(2011, 7-1, 19, 0, 0, 0);
       c20110720wed.set(2011, 7-1, 20, 0, 0, 0);
       c20110722fri.set(2011, 7-1, 22, 0, 0, 0);
       
       c3.setStart(c20110719tue.getTime());
       c3.setEnd(c20110719tue.getTime());
       c3.setLastRecurrence(c20110722fri.getTime());
       CALENDAR_SERVICE.updateCalendarEntry(c3);
       
       
       // Monday-Tuesday will find it for itself
       cq = (GetCalendarEntriesCannedQuery)CALENDAR_CQ_FACTORY.getCannedQuery(
             containers, c20110718mon.getTime(), c20110719tue.getTime(), paging
       );
       cq.setTestHook(hook);
       cq.execute();
       assertEquals(3, full.size());
       assertEquals(1, filtered.size());
       assertEquals(c3.getSystemName(), filtered.get(0).getName());
       
       // Monday-Wednesday will find it for itself
       cq = (GetCalendarEntriesCannedQuery)CALENDAR_CQ_FACTORY.getCannedQuery(
             containers, c20110718mon.getTime(), c20110720wed.getTime(), paging
       );
       cq.setTestHook(hook);
       cq.execute();
       assertEquals(3, full.size());
       assertEquals(1, filtered.size());
       assertEquals(c3.getSystemName(), filtered.get(0).getName());
       
       // Wednesday-Friday will find it as a repeating event on the Thursday
       cq = (GetCalendarEntriesCannedQuery)CALENDAR_CQ_FACTORY.getCannedQuery(
             containers, c20110720wed.getTime(), c20110722fri.getTime(), paging
       );
       cq.setTestHook(hook);
       cq.execute();
       assertEquals(3, full.size());
       assertEquals(1, filtered.size());
       assertEquals(c3.getSystemName(), filtered.get(0).getName());
       
       // Bring the last recurrence date back, will no longer show up 
       c3.setLastRecurrence(c20110720wed.getTime());
       CALENDAR_SERVICE.updateCalendarEntry(c3);
       
       cq = (GetCalendarEntriesCannedQuery)CALENDAR_CQ_FACTORY.getCannedQuery(
             containers, c20110720wed.getTime(), c20110722fri.getTime(), paging
       );
       cq.setTestHook(hook);
       cq.execute();
       assertEquals(3, full.size());
       assertEquals(0, filtered.size());
    }
    
    
    // --------------------------------------------------------------------------------

    
    private static void createTestSites() throws Exception
    {
        final CalendarServiceImpl privateCalendarService = (CalendarServiceImpl)testContext.getBean("calendarService");
        
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
                  privateCalendarService.getSiteCalendarContainer(site.getShortName(), true);
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
                  privateCalendarService.getSiteCalendarContainer(site.getShortName(), true);
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
