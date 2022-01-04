/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.legacy.service;

import java.io.Serializable;
import java.util.Arrays;
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
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.GUID;
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
    /** A QName to display for the hold name. */
    private static final QName HOLD_NAME = QName.createQName(RecordsManagementModel.RM_URI, "Hold Name");

    /** Test record */
    private NodeRef record;

    /** Test start time */
    private Date testStartTime;

    /**
     * Remove from hold audit event name.
     */
    private static final String REMOVE_FROM_HOLD_AUDIT_EVENT = "Remove From Hold";


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
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#isCollaborationSiteTest()
     */
    @Override
    protected boolean isCollaborationSiteTest()
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
        Map<QName, Serializable> personProperties = new HashMap<>();
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

    /**
     * Given I have deleted a user
     * When I will get the RM audit filter by delete user event
     * Then there will be an entry for the deleted user
     * And the audit entry has the username property value audited
     * @throws Exception
     */
    @org.junit.Test
    public void testAuditForDeletedUser() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            final static String DELETE_USER_AUDIT_EVENT = "Delete Person";
            String userName = "auditDeleteUser";
            NodeRef user;
            List<RecordsManagementAuditEntry> entry;

            @Override
            public void given() throws Exception
            {
                // create a user
                user = createPerson(userName);
                personService.deletePerson(userName);
            }

            @Override
            public void when() throws Exception
            {
                // set the audit wuery param
                RecordsManagementAuditQueryParameters params = createAuditQueryParameters(DELETE_USER_AUDIT_EVENT);

                // get the audit events for "Delete Person"
                entry = getAuditTrail(params, 1, ADMIN_USER);
            }

            @Override
            public void then() throws Exception
            {
                assertEquals("Delete user event is not audited.", DELETE_USER_AUDIT_EVENT, entry.get(0).getEvent());
                assertEquals(user.getId(), entry.get(0).getNodeName());
                assertEquals("Unexpected nr of properties audited for cm:person type when deleting a user.",
                        1, entry.get(0).getBeforeProperties().size());
                assertEquals("Wrong value for username property is  audited",
                        userName, entry.get(0).getBeforeProperties().get(ContentModel.PROP_USERNAME));
            }
            @Override
            public void after()
            {
                // Stop and delete all entries
                rmAuditService.stopAuditLog(filePlan);
                rmAuditService.clearAuditLog(filePlan);
            }

        });
    }

    /**
     * Given I have created a user
     * When I will get the RM audit filter by create user event
     * Then there will be an entry for the created user
     *
     * @throws Exception
     */
    @org.junit.Test
    public void testAuditForCreateUser() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            final static String CREATE_USER_AUDIT_EVENT = "Create Person";
            String userName = "auditCreateUser";
            NodeRef user;
            List<RecordsManagementAuditEntry> entry;

            @Override
            public void given() throws Exception
            {
                // create a user
                user = createPerson(userName);
            }

            @Override
            public void when() throws Exception
            {
                // set the audit query param
                RecordsManagementAuditQueryParameters params = createAuditQueryParameters(CREATE_USER_AUDIT_EVENT);

                // get the audit events for "Create Person"
                entry = getAuditTrail(params, 1, ADMIN_USER);
            }

            @Override
            public void then() throws Exception
            {
                assertEquals("Create user event is not audited.",
                        CREATE_USER_AUDIT_EVENT, entry.get(0).getEvent());
            }

            @Override
            public void after()
            {
                // Stop and delete all entries
                rmAuditService.stopAuditLog(filePlan);
                rmAuditService.clearAuditLog(filePlan);
            }

        });
    }

    /**
     * Given I have created a hold
     * When I will get the RM audit filter by create hold event
     * Then there will be an entry for the created hold, including the hold name and reason
     */
    public void testAuditForCreateHold()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            final static String CREATE_HOLD_AUDIT_EVENT = "Create Hold";

            String holdName = "Hold " + GUID.generate();
            String holdReason = "Reason " + GUID.generate();

            Map<QName, Serializable> auditEventProperties;

            @Override
            public void given()
            {
                rmAuditService.clearAuditLog(filePlan);
                utils.createHold(filePlan, holdName, holdReason);
            }

            @Override
            public void when()
            {
                auditEventProperties = getAuditEntry(CREATE_HOLD_AUDIT_EVENT).getAfterProperties();
            }

            @Override
            public void then()
            {
                // check create hold audit event includes the hold name
                assertEquals("Create Hold event does not include hold name.", holdName,
                    auditEventProperties.get(HOLD_NAME));

                // check create hold audit event includes the hold reason
                assertEquals("Create Hold event does not include hold reason.", holdReason,
                    auditEventProperties.get(PROP_HOLD_REASON));
            }

            @Override
            public void after()
            {
                // Stop and delete all entries
                rmAuditService.stopAuditLog(filePlan);
                rmAuditService.clearAuditLog(filePlan);
            }
        });
    }

    /**
     * Given I have created a hold
     * When I delete the hold and get the RM audit filter by delete hold event
     * Then there will be an entry for the deleted hold, including the hold name
     */
    public void testAuditForDeleteHold()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            final static String DELETE_HOLD_AUDIT_EVENT = "Delete Hold";

            String holdName = "Hold " + GUID.generate();

            NodeRef hold;
            Map<QName, Serializable> auditEventProperties;

            @Override
            public void given()
            {
                rmAuditService.clearAuditLog(filePlan);
                hold = utils.createHold(filePlan, holdName, "Reason " + GUID.generate());
            }

            @Override
            public void when()
            {
                utils.deleteHold(hold);
                auditEventProperties = getAuditEntry(DELETE_HOLD_AUDIT_EVENT).getBeforeProperties();
            }

            @Override
            public void then()
            {
                // check delete hold audit event includes the hold name
                assertEquals("Delete Hold event does not include hold name.", holdName,
                        auditEventProperties.get(HOLD_NAME));
            }

            @Override
            public void after()
            {
                // Stop and delete all entries
                rmAuditService.stopAuditLog(filePlan);
                rmAuditService.clearAuditLog(filePlan);
            }
        });
    }

    /**
     * Given I have added an item of content to a hold
     * When I get the RM audit filter by add to hold event
     * Then there will be an entry for the item added to the hold, including both the item name and hold name
     */
    public void testAuditForAddContentToHold()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            final static String ADD_TO_HOLD_AUDIT_EVENT = "Add To Hold";

            String holdName = "Hold " + GUID.generate();
            NodeRef hold;

            Map<QName, Serializable> auditEventProperties;

            @Override
            public void given()
            {
                rmAuditService.clearAuditLog(filePlan);
                hold = utils.createHold(filePlan, holdName, "Reason " + GUID.generate());
                utils.addItemToHold(hold, dmDocument);
            }

            @Override
            public void when()
            {
                auditEventProperties = getAuditEntry(ADD_TO_HOLD_AUDIT_EVENT).getAfterProperties();
            }

            @Override
            public void then()
            {
                // check add to hold audit event includes the hold name
                assertEquals("Add To Hold event does not include hold name.", holdName,
                        auditEventProperties.get(HOLD_NAME));

                // check add to hold audit event includes the content name
                String contentName = (String) nodeService.getProperty(dmDocument, PROP_NAME);
                assertEquals("Add To Hold event does not include content name.", contentName,
                        auditEventProperties.get(PROP_NAME));
            }

            @Override
            public void after()
            {
                // Stop and delete all entries
                rmAuditService.stopAuditLog(filePlan);
                rmAuditService.clearAuditLog(filePlan);
            }
        });
    }


    /**
     * Given I have an item in a hold
     * When I remove the item from the hold
     * Then there will be an audit entry for the item removed from the hold, including both the item name and hold name
     */
    @org.junit.Test
    public void testAuditForRemoveContentFromHold()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            String holdName = "Hold " + GUID.generate();
            NodeRef hold;

            @Override
            public void given()
            {
                rmAuditService.clearAuditLog(filePlan);
                hold = utils.createHold(filePlan, holdName, "Reason " + GUID.generate());
                utils.addItemToHold(hold, dmDocument);
            }

            @Override
            public void when()
            {
                utils.removeItemFromHold(hold, dmDocument);
            }

            @Override
            public void then()
            {
                Map<QName, Serializable> auditEventProperties = getAuditEntry(REMOVE_FROM_HOLD_AUDIT_EVENT).getBeforeProperties();

                // check remove from hold audit event includes the hold name
                assertEquals("Remove From Hold event does not include hold name.", holdName,
                        auditEventProperties.get(HOLD_NAME));

                // check remove from hold audit event includes the content name
                String contentName = (String) nodeService.getProperty(dmDocument, PROP_NAME);
                assertEquals("Remove From Hold event does not include content name.", contentName,
                        auditEventProperties.get(PROP_NAME));
            }

            @Override
            public void after()
            {
                // Stop and delete all entries
                rmAuditService.stopAuditLog(filePlan);
                rmAuditService.clearAuditLog(filePlan);
            }
        });

    }


    /**
     * Given I have removed an item from multiple holds
     * When I will get the RM audit filter by remove from hold events
     * Then there will be entries for the item removed from each hold, including both the item name and hold name
     */
    @org.junit.Test
    public void testAuditForRemoveContentFromMultipleHolds()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            String holdName1 = "Hold " + GUID.generate();
            String holdName2 = "Hold " + GUID.generate();
            NodeRef hold1, hold2;

            @Override
            public void given()
            {
                rmAuditService.clearAuditLog(filePlan);

                hold1 = utils.createHold(filePlan, holdName1, "Reason " + GUID.generate());
                hold2 = utils.createHold(filePlan, holdName2, "Reason " + GUID.generate());
                utils.addItemToHold(hold1, dmDocument);
                utils.addItemToHold(hold2, dmDocument);
            }

            @Override
            public void when()
            {
                utils.removeItemsFromHolds(Arrays.asList(hold1, hold2), Arrays.asList(dmDocument));
            }

            @Override
            public void then()
            {
                List<RecordsManagementAuditEntry> auditEntries = getAuditEntries(REMOVE_FROM_HOLD_AUDIT_EVENT);

                // check remove from hold audit event exists for both holds
                assertEquals(2, auditEntries.size());
            }

            @Override
            public void after()
            {
                // Stop and delete all entries
                rmAuditService.stopAuditLog(filePlan);
                rmAuditService.clearAuditLog(filePlan);
            }
        });

    }


    /**
     * Given I have removed items from a hold
     * When I will get the RM audit filter by remove from hold events
     * Then there will be entries for the items removed from the hold, including both the item name and hold name
     */
    @org.junit.Test
    public void testAuditForRemoveMultipleContentFromHold()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            String holdName = "Hold " + GUID.generate();
            NodeRef hold;

            @Override
            public void given()
            {
                rmAuditService.clearAuditLog(filePlan);

                hold = utils.createHold(filePlan, holdName, "Reason " + GUID.generate());
                utils.addItemToHold(hold, dmDocument);
                utils.addItemToHold(hold, dmDocument1);
            }

            @Override
            public void when()
            {
                utils.removeItemsFromHolds(Arrays.asList(hold), Arrays.asList(dmDocument, dmDocument1));
            }

            @Override
            public void then()
            {
                List<RecordsManagementAuditEntry> auditEntries = getAuditEntries(REMOVE_FROM_HOLD_AUDIT_EVENT);

                // check remove from hold audit event exists for both documents
                assertEquals(2, auditEntries.size());
            }

            @Override
            public void after()
            {
                // Stop and delete all entries
                rmAuditService.stopAuditLog(filePlan);
                rmAuditService.clearAuditLog(filePlan);
            }
        });

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

    private RecordsManagementAuditEntry getAuditEntry(String auditEvent)
    {
        // create the audit query parameters for the given event
        RecordsManagementAuditQueryParameters params = createAuditQueryParameters(auditEvent);

        // get the audit entries for the given event
        List<RecordsManagementAuditEntry> auditEntries = getAuditEntryAssertOnlyOne(params);

        // verify we have the expected audit event
        RecordsManagementAuditEntry auditEntry = auditEntries.get(0);
        assertEquals(auditEvent + " event is not audited.", auditEvent, auditEntry.getEvent());

        // return the properties of the audit event
        return auditEntry;
    }

    private List<RecordsManagementAuditEntry> getAuditEntryAssertOnlyOne(RecordsManagementAuditQueryParameters params)
    {
        List<RecordsManagementAuditEntry> auditEntries;
        auditEntries = getAuditTrail(params, 1, ADMIN_USER);
        return auditEntries;
    }

    private List<RecordsManagementAuditEntry> getAuditEntries(String auditEvent)
    {
        // create the audit query parameters for the given event
        RecordsManagementAuditQueryParameters params = createAuditQueryParameters(auditEvent);

        // get the audit entries for the given event
        List<RecordsManagementAuditEntry> auditEntries = getAllAuditEntries(params);

        return auditEntries;
    }

    private List<RecordsManagementAuditEntry> getAllAuditEntries(RecordsManagementAuditQueryParameters params)
    {
        List<RecordsManagementAuditEntry> auditEntries;
        auditEntries = getAuditTrail(params, -1, ADMIN_USER);
        return auditEntries;
    }

    private RecordsManagementAuditQueryParameters createAuditQueryParameters(String auditEvent)
    {
        RecordsManagementAuditQueryParameters params = new RecordsManagementAuditQueryParameters();
        params.setEvent(auditEvent);
        return params;
    }

}
