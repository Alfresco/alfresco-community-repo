/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.legacy.service;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditEntry;
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditQueryParameters;
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.module.org_alfresco_module_rm.audit.event.AuditEvent;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;

/**
 * @see RecordsManagementAuditService
 *
 * @author Derek Hulley
 * @author Roy Wetherall
 *
 * @since 3.2
 */
public class RecordsManagementAuditServiceImplTest extends BaseRMTestCase
                                                   implements RMPermissionModel
{
    /** Test record */
    private NodeRef record;

    /** Test start time */
    private Date testStartTime;

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
		        // test start time recorded
		        testStartTime = new Date();

		        // Stop and clear the log
		        rmAuditService.stopAuditLog(filePlan);
		        rmAuditService.clearAuditLog(filePlan);
		        rmAuditService.startAuditLog(filePlan);

		        // check that audit service is started
		        assertTrue(rmAuditService.isAuditLogEnabled(filePlan));

		        return null;
            }
        });
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#isUserTest()
     */
    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#setupTestDataImpl()
     */
    @Override
    protected void setupTestDataImpl()
    {
        super.setupTestDataImpl();

        record = utils.createRecord(rmFolder, "AuditTest.txt");
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#setupTestUsersImpl(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void setupTestUsersImpl(NodeRef filePlan)
    {
        super.setupTestUsersImpl(filePlan);

        // Give all the users file permission objects
        for (String user : testUsers)
        {
            filePlanPermissionService.setPermission(filePlan, user, FILING);
            filePlanPermissionService.setPermission(rmContainer, user, FILING);
        }
    }

    public void testGetAuditEvents()
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                List<AuditEvent> events = rmAuditService.getAuditEvents();

                System.out.println("Found audit events:");
                for (AuditEvent event : events)
                {
                    System.out.println("  - " + event.getName() + " (" + event.getLabel() + ")");
                }

                return null;
            }
        }, ADMIN_USER);
    }

    /**
     * Test getAuditTrail method to check that deleted items always show in the audit.
     * 
     * @see RM-2391 (last addressed isue)
     */
    public void testGetAuditTrailForDeletedItem()
    {
        // We have only one entry for the event "audit.start":
        List<RecordsManagementAuditEntry> entries = getAuditTrail(1, ADMIN_USER);

        assertEquals(entries.get(0).getEvent(), "audit.start");

        // Event "audit.view" was generated but will be visible on the next call to getAuditTrail().

        // Make a change:
        updateTitle(filePlan, ADMIN_USER); // event=Update RM Object

        // Show the audit has been updated; at this point we have three entries for the three events up to now:
        // "audit.start", "audit.view" and "Update RM Object";
        entries = getAuditTrail(3, ADMIN_USER);

        assertEquals(entries.get(2).getEvent(), "audit.start");
        assertEquals(entries.get(1).getEvent(), "audit.view");
        assertEquals(entries.get(0).getEvent(), "Update RM Object");

        // New "audit.view" event was generated - will be visible on next getAuditTrail().

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                nodeService.deleteNode(record);
                List<RecordsManagementAuditEntry> entries = getAuditTrail(5, ADMIN_USER);

                assertEquals(entries.get(4).getEvent(), "audit.start");
                assertEquals(entries.get(3).getEvent(), "audit.view");
                assertEquals(entries.get(2).getEvent(), "Update RM Object");
                assertEquals(entries.get(1).getEvent(), "audit.view");

                // Show the audit contains a reference to the deleted item:
                assertEquals(entries.get(0).getEvent(), "Delete RM Object");
                assertEquals(entries.get(0).getNodeRef(), record);

                return null;
            }
        });
    }
    
    /**
     * Test getAuditTrail method and parameter filters.
     */
    public void testGetAuditTrail()
    {
        // show the audit is empty
        getAuditTrail(1, ADMIN_USER);

        // make a change
        final String updatedProperty = updateTitle(filePlan, ADMIN_USER);

        // show the audit has been updated
        List<RecordsManagementAuditEntry> entries = getAuditTrail(3, ADMIN_USER);
        final RecordsManagementAuditEntry entry = entries.get(0);
        assertNotNull(entry);

        // investigate the contents of the audit entry
        doTestInTransaction(new Test<Void>()
        {
            @SuppressWarnings("unchecked")
            @Override
            public Void run() throws Exception
            {
                assertEquals(filePlan, entry.getNodeRef());

                String id = (String)nodeService.getProperty(filePlan, PROP_IDENTIFIER);
                assertEquals(id, entry.getIdentifier());

                Map<QName, Serializable> after = entry.getAfterProperties();
                Map<QName, Pair<Serializable, Serializable>> changed = entry.getChangedProperties();

                assertTrue(after.containsKey(PROP_TITLE));
                assertTrue(changed.containsKey(PROP_TITLE));

                Serializable value = ((Map<Locale, Serializable>)after.get(PROP_TITLE)).get(Locale.ENGLISH);
                assertEquals(updatedProperty, value);
                value = ((Map<Locale, Serializable>)changed.get(PROP_TITLE).getSecond()).get(Locale.ENGLISH);
                assertEquals(updatedProperty, value);

                return null;
            }
        }, ADMIN_USER);

        // add some more title updates
        updateTitle(rmContainer, ADMIN_USER);
        updateTitle(rmFolder, ADMIN_USER);
        updateTitle(record, ADMIN_USER);

        // show the audit has been updated
        getAuditTrail(7, ADMIN_USER);

        // snap shot date
        Date snapShot = new Date();

        // show the audit results can be limited
        RecordsManagementAuditQueryParameters params = new RecordsManagementAuditQueryParameters();
        params.setMaxEntries(2);
        getAuditTrail(params, 2, ADMIN_USER);

        // test filter by user
        updateTitle(rmContainer, recordsManagerName);
        updateTitle(rmFolder, recordsManagerName);
        updateTitle(record, recordsManagerName);

        params = new RecordsManagementAuditQueryParameters();
        params.setUser(recordsManagerName);
        getAuditTrail(params, 3, ADMIN_USER);

        // test filter by date
        params = new RecordsManagementAuditQueryParameters();
        params.setDateFrom(snapShot);
        getAuditTrail(params, 13, ADMIN_USER);
        params = new RecordsManagementAuditQueryParameters();
        params.setDateTo(snapShot);
        getAuditTrail(params, 14, ADMIN_USER);
        params.setDateFrom(testStartTime);
        getAuditTrail(params, 15, ADMIN_USER);

        // test filter by object
        updateTitle(record, ADMIN_USER);
        updateTitle(record, ADMIN_USER);
        updateTitle(record, ADMIN_USER);
        params = new RecordsManagementAuditQueryParameters();
        params.setNodeRef(record);
        getAuditTrail(params, 5, ADMIN_USER);

        // test filter by event
        params = new RecordsManagementAuditQueryParameters();
     //   params.setEvent("cutoff");
     //   getAuditTrail(params, 0, ADMIN_USER);
        params.setEvent("Update RM Object");
        getAuditTrail(params, 10, ADMIN_USER);

        // test filter by property
       // params = new RecordsManagementAuditQueryParameters();
        //params.setProperty(PROP_ADDRESSEES);
        //getAuditTrail(params, 0, ADMIN_USER);
       // params.setProperty(PROP_TITLE);
       // getAuditTrail(params, 10, ADMIN_USER);
    }

    /**
     * Tests the following methods:
     *   - start()
     *   - stop()
     *   - clear()
     *   - isEnabled()
     *   - getDateLastStopped()
     *   - getDateLastStarted()
     *
     * @throws InterruptedException
     */
    public void testAdminMethods() throws InterruptedException
    {
        // Stop the audit
        rmAuditService.stopAuditLog(filePlan);

        Thread.sleep(5000);

        List<RecordsManagementAuditEntry> result1 = getAuditTrail(ADMIN_USER);
        assertNotNull(result1);

        // Update the fileplan
        updateTitle(filePlan, ADMIN_USER);

        Thread.sleep(5000);

        // There should be no new audit entries
        List<RecordsManagementAuditEntry> result2 = getAuditTrail(ADMIN_USER);
        assertNotNull(result2);
        assertEquals(
                "Audit results should not have changed after auditing was disabled",
                result1.size(), result2.size());

        // repeat with a start
        rmAuditService.startAuditLog(filePlan);
        updateTitle(filePlan, ADMIN_USER);

        Thread.sleep(5000);

        List<RecordsManagementAuditEntry> result3 = getAuditTrail(ADMIN_USER);
        assertNotNull(result3);
        assertTrue(
                "Expected more results after enabling audit",
                result3.size() > result1.size());

        Thread.sleep(5000);

        // Stop and delete all entries
        rmAuditService.stopAuditLog(filePlan);
        rmAuditService.clearAuditLog(filePlan);

        // There should be no entries
        List<RecordsManagementAuditEntry> result4 = getAuditTrail(ADMIN_USER);
        assertNotNull(result4);
        assertEquals(
                "Audit entries should have been cleared",
                0, result4.size());
    }

    // TODO testAuditRMAction

    // TODO testGetAuditTrailFile

    // TODO testFileAuditTrailAsRecord

    public void xtestAuditAuthentication()
    {
        rmAuditService.stopAuditLog(filePlan);
        rmAuditService.clearAuditLog(filePlan);
        rmAuditService.startAuditLog(filePlan);

        //MutableAuthenticationService authenticationService = serviceRegistry.getAuthenticationService();
        //PersonService personService = serviceRegistry.getPersonService();

        try
        {
            personService.deletePerson("baboon");
            authenticationService.deleteAuthentication("baboon");
        }
        catch (Throwable e)
        {
            // Not serious
        }

        // Failed login attempt ...
        try
        {
            AuthenticationUtil.pushAuthentication();
            authenticationService.authenticate("baboon", "lskdfj".toCharArray());
            fail("Expected authentication failure");
        }
        catch (AuthenticationException e)
        {
            // Good
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
        rmAuditService.stopAuditLog(filePlan);
        List<RecordsManagementAuditEntry> result1 = getAuditTrail(ADMIN_USER);
        // Check that the username is reflected correctly in the results
        assertFalse("No audit results were generated for the failed login.", result1.isEmpty());
        boolean found = false;
        for (RecordsManagementAuditEntry entry : result1)
        {
            String userName = entry.getUserName();
            if (userName.equals("baboon"))
            {
                found = true;
                break;
            }
        }
        assertTrue("Expected to hit failed login attempt for user", found);

        // Test successful authentication
        try
        {
            personService.deletePerson("cdickons");
            authenticationService.deleteAuthentication("cdickons");
        }
        catch (Throwable e)
        {
            // Not serious
        }
        authenticationService.createAuthentication("cdickons", getName().toCharArray());
        Map<QName, Serializable> personProperties = new HashMap<QName, Serializable>();
        personProperties.put(ContentModel.PROP_USERNAME, "cdickons");
        personProperties.put(ContentModel.PROP_FIRSTNAME, "Charles");
        personProperties.put(ContentModel.PROP_LASTNAME, "Dickons");
        personService.createPerson(personProperties);

        rmAuditService.clearAuditLog(filePlan);
        rmAuditService.startAuditLog(filePlan);
        try
        {
            AuthenticationUtil.pushAuthentication();
            authenticationService.authenticate("cdickons", getName().toCharArray());
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
        rmAuditService.stopAuditLog(filePlan);
        List<RecordsManagementAuditEntry> result2 = getAuditTrail(ADMIN_USER);
        found = false;
        for (RecordsManagementAuditEntry entry : result2)
        {
            String userName = entry.getUserName();
            String fullName = entry.getFullName();
            if (userName.equals("cdickons") && EqualsHelper.nullSafeEquals(fullName, "Charles Dickons"))
            {
                found = true;
                break;
            }
        }
        assertTrue("Expected to hit successful login attempt for Charles Dickons (cdickons)", found);
    }

    /** === Helper methods === */

    private List<RecordsManagementAuditEntry> getAuditTrail(String asUser)
    {
        return getAuditTrail(-1, asUser);
    }

    private List<RecordsManagementAuditEntry> getAuditTrail(final int expectedCount, String asUser)
    {
        return getAuditTrail(new RecordsManagementAuditQueryParameters(), expectedCount, asUser);
    }

    private List<RecordsManagementAuditEntry> getAuditTrail(final RecordsManagementAuditQueryParameters params, final int expectedCount, final String asUser)
    {
        return doTestInTransaction(new Test<List<RecordsManagementAuditEntry>>()
        {
            @Override
            public List<RecordsManagementAuditEntry> run() throws Exception
            {
                return rmAuditService.getAuditTrail(params);
            }

            @Override
            public void test(List<RecordsManagementAuditEntry> result) throws Exception
            {
                assertNotNull(result);
                if (expectedCount != -1)
                {
                    assertEquals(expectedCount, result.size());
                }
            }
        }, asUser);
    }

    private String updateTitle(final NodeRef nodeRef, final String asUser)
    {
        return doTestInTransaction(new Test<String>()
        {
            @Override
            public String run() throws Exception
            {
                String updatedProperty = "Updated - " + System.currentTimeMillis();
                nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, updatedProperty);
                return updatedProperty;
            }
        }, asUser);
    }
}
