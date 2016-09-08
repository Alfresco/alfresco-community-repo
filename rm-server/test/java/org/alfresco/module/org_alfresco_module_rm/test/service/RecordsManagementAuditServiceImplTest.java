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
package org.alfresco.module.org_alfresco_module_rm.test.service;

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
    /** Records management audit service */
    private RecordsManagementAuditService auditService;

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
		        auditService.stopAuditLog(filePlan);
		        auditService.clearAuditLog(filePlan);
		        auditService.startAuditLog(filePlan);

		        // check that audit service is started
		        assertTrue(auditService.isAuditLogEnabled(filePlan));

		        return null;
            }
        });
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#initServices()
     */
    @Override
    protected void initServices()
    {
        super.initServices();

        // get the audit service
        auditService = (RecordsManagementAuditService)applicationContext.getBean("RecordsManagementAuditService");
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
                List<AuditEvent> events = auditService.getAuditEvents();

                System.out.println("Found audit events:");
                for (AuditEvent event : events)
                {
                    System.out.println("  - " + event.getName() + " (" + event.getLabel() + ")");
                }

                return null;
            }
        }, rmAdminName);
    }

    /**
     * Test getAuditTrail method and parameter filters.
     */
    public void testGetAuditTrail()
    {
        // show the audit is empty
        getAuditTrail(1, rmAdminName);

        // make a change
        final String updatedProperty = updateTitle(filePlan, rmAdminName);

        // show the audit has been updated
        List<RecordsManagementAuditEntry> entries = getAuditTrail(3, rmAdminName);
        final RecordsManagementAuditEntry entry = entries.get(2);
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
        }, rmAdminName);

        // add some more title updates
        updateTitle(rmContainer, rmAdminName);
        updateTitle(rmFolder, rmAdminName);
        updateTitle(record, rmAdminName);

        // show the audit has been updated
        getAuditTrail(7, rmAdminName);

        // snap shot date
        Date snapShot = new Date();

        // show the audit results can be limited
        RecordsManagementAuditQueryParameters params = new RecordsManagementAuditQueryParameters();
        params.setMaxEntries(2);
        getAuditTrail(params, 2, rmAdminName);

        // test filter by user
        updateTitle(rmContainer, recordsManagerName);
        updateTitle(rmFolder, recordsManagerName);
        updateTitle(record, recordsManagerName);

        params = new RecordsManagementAuditQueryParameters();
        params.setUser(recordsManagerName);
        getAuditTrail(params, 3, rmAdminName);

        // test filter by date
        params = new RecordsManagementAuditQueryParameters();
        params.setDateFrom(snapShot);
        getAuditTrail(params, 13, rmAdminName);
        params = new RecordsManagementAuditQueryParameters();
        params.setDateTo(snapShot);
        getAuditTrail(params, 14, rmAdminName);
        params.setDateFrom(testStartTime);
        getAuditTrail(params, 15, rmAdminName);

        // test filter by object
        updateTitle(record, rmAdminName);
        updateTitle(record, rmAdminName);
        updateTitle(record, rmAdminName);
        params = new RecordsManagementAuditQueryParameters();
        params.setNodeRef(record);
        getAuditTrail(params, 5, rmAdminName);

        // test filter by event
        params = new RecordsManagementAuditQueryParameters();
     //   params.setEvent("cutoff");
     //   getAuditTrail(params, 0, rmAdminName);
        params.setEvent("Update RM Object");
        getAuditTrail(params, 10, rmAdminName);

        // test filter by property
       // params = new RecordsManagementAuditQueryParameters();
        //params.setProperty(PROP_ADDRESSEES);
        //getAuditTrail(params, 0, rmAdminName);
       // params.setProperty(PROP_TITLE);
       // getAuditTrail(params, 10, rmAdminName);
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
        auditService.stopAuditLog(filePlan);

        Thread.sleep(5000);

        List<RecordsManagementAuditEntry> result1 = getAuditTrail(rmAdminName);
        assertNotNull(result1);

        // Update the fileplan
        updateTitle(filePlan, rmAdminName);

        Thread.sleep(5000);

        // There should be no new audit entries
        List<RecordsManagementAuditEntry> result2 = getAuditTrail(rmAdminName);
        assertNotNull(result2);
        assertEquals(
                "Audit results should not have changed after auditing was disabled",
                result1.size(), result2.size());

        // repeat with a start
        auditService.startAuditLog(filePlan);
        updateTitle(filePlan, rmAdminName);

        Thread.sleep(5000);

        List<RecordsManagementAuditEntry> result3 = getAuditTrail(rmAdminName);
        assertNotNull(result3);
        assertTrue(
                "Expected more results after enabling audit",
                result3.size() > result1.size());

        Thread.sleep(5000);

        // Stop and delete all entries
        auditService.stopAuditLog(filePlan);
        auditService.clearAuditLog(filePlan);

        // There should be no entries
        List<RecordsManagementAuditEntry> result4 = getAuditTrail(rmAdminName);
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
        auditService.stopAuditLog(filePlan);
        auditService.clearAuditLog(filePlan);
        auditService.startAuditLog(filePlan);

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
        auditService.stopAuditLog(filePlan);
        List<RecordsManagementAuditEntry> result1 = getAuditTrail(rmAdminName);
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

        auditService.clearAuditLog(filePlan);
        auditService.startAuditLog(filePlan);
        try
        {
            AuthenticationUtil.pushAuthentication();
            authenticationService.authenticate("cdickons", getName().toCharArray());
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
        auditService.stopAuditLog(filePlan);
        List<RecordsManagementAuditEntry> result2 = getAuditTrail(rmAdminName);
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
                return auditService.getAuditTrail(params);
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
