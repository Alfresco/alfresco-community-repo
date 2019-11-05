/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 * #L%
 */
package org.alfresco.rest.rm.community.audit;

import static java.util.Arrays.asList;

import static org.alfresco.rest.rm.community.base.TestData.HOLD_DESCRIPTION;
import static org.alfresco.rest.rm.community.base.TestData.HOLD_REASON;
import static org.alfresco.rest.rm.community.model.audit.AuditEvents.CREATE_HOLD;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import com.google.common.collect.ImmutableMap;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.audit.AuditEntry;
import org.alfresco.rest.rm.community.model.user.UserRoles;
import org.alfresco.rest.v0.HoldsAPI;
import org.alfresco.rest.v0.RMAuditAPI;
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

    @Autowired
    private RMAuditAPI rmAuditAPI;
    @Autowired
    private HoldsAPI holdsAPI;
    @Autowired
    private RoleService roleService;

    private UserModel rmAdmin, rmManager;
    private List<AuditEntry> auditEntries;

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
        STEP("Clean audit logs.");
        rmAuditAPI.clearAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword());

        STEP("Create a new hold.");
        holdsAPI.createHold(rmAdmin.getUsername(), rmAdmin.getPassword(), HOLD1, HOLD_REASON, HOLD_DESCRIPTION);

        STEP("Get the list of audit entries for the create hold event.");
        auditEntries = rmAuditAPI.getRMAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword(), 100,
                CREATE_HOLD.event);

        STEP("Check the audit log contains the entry for the created hold with the hold details.");
        assertFalse("The list of events should contain Create Hold entry ", auditEntries.isEmpty());
        AuditEntry auditEntry = auditEntries.get(0);
        assertTrue("The list of events is not filtered by Create Hold",
                auditEntry.getEvent().equals(CREATE_HOLD.eventDisplayName));
        assertTrue("The hold name value for the hold created is not audited.", auditEntry.getNodeName().equals(HOLD1));
        assertTrue("The hold reason value for the hold created is not audited.",
                auditEntry.getChangedValues().contains(
                        ImmutableMap.of("new", HOLD_REASON, "previous", "", "name", "Hold Reason")));
        assertTrue("The user who created the hold is not audited.",
                auditEntry.getUserName().equals(rmAdmin.getUsername()));
        assertFalse("The date when the hold creation occurred is not audited.", auditEntry.getTimestamp().isEmpty());
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
        holdsAPI.createHold(rmAdmin.getUsername(), rmAdmin.getPassword(), HOLD2, HOLD_REASON, HOLD_DESCRIPTION);

        STEP("Clean audit logs.");
        rmAuditAPI.clearAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword());

        STEP("Try to create again the same hold.");
        holdsAPI.createHold(rmAdmin.getUsername(), rmAdmin.getPassword(), HOLD2, HOLD_REASON, HOLD_DESCRIPTION);

        STEP("Get the list of audit entries for the create hold event.");
        auditEntries = rmAuditAPI.getRMAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword(), 100,
                CREATE_HOLD.event);

        STEP("Check the audit log doesn't contain the entry for the second create hold event.");
        assertTrue("The list of events should not contain Create Hold entry ", auditEntries.isEmpty());
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
        STEP("Clean audit logs.");
        rmAuditAPI.clearAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword());

        STEP("Create a new hold.");
        holdsAPI.createHold(rmAdmin.getUsername(), rmAdmin.getPassword(), holdName, HOLD_REASON, HOLD_DESCRIPTION);

        STEP("Get the list of audit entries for the create hold event.");
        auditEntries = rmAuditAPI.getRMAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword(), 100,
                CREATE_HOLD.event);

        STEP("Delete the created hold.");
        holdsAPI.deleteHold(rmAdmin.getUsername(), rmAdmin.getPassword(), holdName);

        STEP("Get again the list of audit entries for the create hold event.");
        List<AuditEntry> auditEntriesAfterDelete = rmAuditAPI.getRMAuditLog(getAdminUser().getUsername(),
                getAdminUser().getPassword(), 100, CREATE_HOLD.event);

        STEP("Check that the audit entry for the created hold didn't change after hold deletion.");
        assertEquals("The list of events is not filtered by Create Hold",
                auditEntries, auditEntriesAfterDelete);
    }

    /**
     * Given a new hold is created
     * When I view the audit log as an user with no Read permissions over the created hold
     * Then the create hold entry isn't visible
     */
    @Test
    public void createHoldAuditEntryNotVisible()
    {
        STEP("Clean audit logs.");
        rmAuditAPI.clearAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword());

        STEP("Create a new hold.");
        holdsAPI.createHold(rmAdmin.getUsername(), rmAdmin.getPassword(), HOLD3, HOLD_REASON, HOLD_DESCRIPTION);

        STEP("Get the list of audit entries for the create hold event as an user with no Read permissions over the hold.");
        auditEntries = rmAuditAPI.getRMAuditLog(rmManager.getUsername(), rmManager.getPassword(), 100,
                CREATE_HOLD.event);

        STEP("Check the audit log doesn't contain the entry for the create hold event.");
        assertTrue("The list of events should not contain Create Hold entry ", auditEntries.isEmpty());
    }

    @AfterClass (alwaysRun = true)
    public void cleanUpAuditHoldTests()
    {
        asList(HOLD1, HOLD2, HOLD3).forEach(hold ->
                holdsAPI.deleteHold(getAdminUser().getUsername(), getAdminUser().getPassword(), hold));
        asList(rmAdmin, rmManager).forEach(user -> getDataUser().usingAdmin().deleteUser(user));
    }
}
