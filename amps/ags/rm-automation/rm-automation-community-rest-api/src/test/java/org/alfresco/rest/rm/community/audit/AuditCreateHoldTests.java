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
package org.alfresco.rest.rm.community.audit;

import static java.util.Arrays.asList;

import static org.alfresco.rest.rm.community.base.TestData.HOLD_DESCRIPTION;
import static org.alfresco.rest.rm.community.base.TestData.HOLD_REASON;
import static org.alfresco.rest.rm.community.model.audit.AuditEvents.CREATE_HOLD;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.apache.commons.httpclient.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableMap;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.audit.AuditEntry;
import org.alfresco.rest.rm.community.model.user.UserRoles;
import org.alfresco.rest.v0.HoldsAPI;
import org.alfresco.rest.v0.service.RMAuditService;
import org.alfresco.rest.v0.service.RoleService;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class contains the tests that check the create hold event is audited
 *
 * @author Claudia Agache
 * @since 3.3
 */
@AlfrescoTest (jira = "RM-6859")
public class AuditCreateHoldTests extends BaseRMRestTest
{
    private final String PREFIX = generateTestPrefix(AuditCreateHoldTests.class);
    private final String HOLD1 = PREFIX + "createHold";
    private final String HOLD2 = PREFIX + "createHold2";
    private final String HOLD3 = PREFIX + "createHold3";
    private final List<String> holdsListRef = new ArrayList<>();

    @Autowired
    private RMAuditService rmAuditService;
    @Autowired
    private HoldsAPI holdsAPI;
    @Autowired
    private RoleService roleService;

    private UserModel rmAdmin, rmManager;

    @BeforeClass (alwaysRun = true)
    public void preconditionForAuditCreateHoldTests()
    {
        STEP("Create test users.");
        rmAdmin = roleService.createUserWithRMRole(UserRoles.ROLE_RM_ADMIN.roleId);
        rmManager = roleService.createUserWithRMRole(UserRoles.ROLE_RM_MANAGER.roleId);
    }

    /**
     * Given a new hold is created
     * When I view the audit log
     * Then an entry has been created in the audit log which contains the following:
     *      name of the hold
     *      reason for hold
     *      user who created the hold
     *      date the creation occurred
     */
    @Test
    public void createHoldEventIsAuditedForNewHold()
    {
        rmAuditService.clearAuditLog();

        STEP("Create a new hold.");
        String hold1NodeRef = holdsAPI.createHoldAndGetNodeRef(rmAdmin.getUsername(), rmAdmin.getPassword(), HOLD1,
                HOLD_REASON, HOLD_DESCRIPTION);
        holdsListRef.add(hold1NodeRef);
        STEP("Check the audit log contains the entry for the created hold with the hold details.");
        rmAuditService.checkAuditLogForEvent(getAdminUser(), CREATE_HOLD, rmAdmin, HOLD1,
                asList(ImmutableMap.of("new", HOLD_REASON, "previous", "", "name", "Hold Reason"),
                        ImmutableMap.of("new", HOLD1, "previous", "", "name", "Hold Name")));
    }

    /**
     * Given an unsuccessful create hold action
     * When I view the audit log
     * Then the create hold event isn't audited
     */
    @Test
    public void createHoldEventIsNotAuditedForExistingHold()
    {
        STEP("Create a new hold.");
        String hold2NodeRef = holdsAPI.createHoldAndGetNodeRef(rmAdmin.getUsername(), rmAdmin.getPassword(), HOLD2, HOLD_REASON, HOLD_DESCRIPTION);
        holdsListRef.add(hold2NodeRef);
        rmAuditService.clearAuditLog();

        STEP("Try to create again the same hold and expect action to fail.");
        holdsAPI.createHold(rmAdmin.getUsername(), rmAdmin.getPassword(), HOLD2, HOLD_REASON, HOLD_DESCRIPTION,
                SC_INTERNAL_SERVER_ERROR);

        STEP("Check the audit log doesn't contain the entry for the second create hold event.");
        assertTrue("The list of events should not contain Create Hold entry ",
                rmAuditService.getAuditEntriesFilteredByEvent(getAdminUser(), CREATE_HOLD).isEmpty());
    }

    /**
     * Given a new hold is created and then deleted
     * When I view the audit log
     * Then the create hold entry still contains the initial details
     */
    @Test
    public void createHoldAuditEntryIsNotLost()
    {
        final String holdName = PREFIX + "holdToBeDeleted";
        rmAuditService.clearAuditLog();

        STEP("Create a new hold.");
        holdsAPI.createHold(rmAdmin.getUsername(), rmAdmin.getPassword(), holdName, HOLD_REASON, HOLD_DESCRIPTION);

        STEP("Get the list of audit entries for the create hold event.");
        List<AuditEntry> auditEntries = rmAuditService.getAuditEntriesFilteredByEvent(getAdminUser(), CREATE_HOLD);

        STEP("Delete the created hold.");
        holdsAPI.deleteHold(rmAdmin.getUsername(), rmAdmin.getPassword(), holdName);

        STEP("Get again the list of audit entries for the create hold event.");
        List<AuditEntry> auditEntriesAfterDelete = rmAuditService.getAuditEntriesFilteredByEvent(getAdminUser(), CREATE_HOLD);

        STEP("Check that the audit entry for the created hold didn't change after hold deletion.");
        assertEquals("The audit entry for Create Hold has been changed", auditEntries, auditEntriesAfterDelete);
    }

    /**
     * Given a new hold is created
     * When I view the audit log as an user with no Read permissions over the created hold
     * Then the create hold entry isn't visible
     */
    @Test
    public void createHoldAuditEntryNotVisible()
    {
        rmAuditService.clearAuditLog();

        STEP("Create a new hold.");
        String hold3NodeRef = holdsAPI.createHoldAndGetNodeRef(rmAdmin.getUsername(), rmAdmin.getPassword(), HOLD3,
                HOLD_REASON, HOLD_DESCRIPTION);
        holdsListRef.add(hold3NodeRef);

        STEP("Check that an user with no Read permissions over the hold can't see the entry for the create hold event");
        assertTrue("The list of events should not contain Create Hold entry ",
                rmAuditService.getAuditEntriesFilteredByEvent(rmManager, CREATE_HOLD).isEmpty());
    }

    @AfterClass (alwaysRun = true)
    public void cleanUpAuditCreateHoldTests()
    {
        holdsListRef.forEach(holdRef -> holdsAPI.deleteHold(getAdminUser(), holdRef));
        asList(rmAdmin, rmManager).forEach(user -> getDataUser().usingAdmin().deleteUser(user));
    }
}
