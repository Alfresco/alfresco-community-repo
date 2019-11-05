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
import static org.alfresco.rest.rm.community.model.audit.AuditEvents.DELETE_HOLD;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.List;

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
 * This class contains the tests that check the delete hold event is audited
 *
 * @author Claudia Agache
 * @since 3.3
 */
@AlfrescoTest (jira = "RM-6859")
public class AuditDeleteHoldTests extends BaseRMRestTest
{
    private final String PREFIX = generateTestPrefix(AuditDeleteHoldTests.class);
    private final String HOLD = PREFIX + "holdToBeDeleted";
    private final String HOLD2 = PREFIX + "deleteHold";

    @Autowired
    private RMAuditAPI rmAuditAPI;
    @Autowired
    private HoldsAPI holdsAPI;
    @Autowired
    private RoleService roleService;

    private UserModel rmAdmin, rmManager;
    private List<AuditEntry> auditEntries;

    @BeforeClass (alwaysRun = true)
    public void preconditionForAuditDeleteHoldTests()
    {
        STEP("Create a new hold.");
        holdsAPI.createHold(getAdminUser().getUsername(), getAdminUser().getPassword(), HOLD, HOLD_REASON, HOLD_DESCRIPTION);

        STEP("Create 2 users with different permissions for the created hold.");
        rmAdmin = roleService.createUserWithRMRole(UserRoles.ROLE_RM_ADMIN.roleId);
        rmManager = roleService.createUserWithRMRole(UserRoles.ROLE_RM_MANAGER.roleId);
    }

    /**
     * Given a hold is deleted
     * When I view the audit log
     * Then an entry has been created in the audit log which contains the following:
     *      name of the hold
     *      user who deleted the hold
     *      date the delete occurred
     */
    @Test
    public void deleteHoldEventIsAudited()
    {
        STEP("Create a new hold.");
        holdsAPI.createHold(rmAdmin.getUsername(), rmAdmin.getPassword(), HOLD2, HOLD_REASON, HOLD_DESCRIPTION);

        STEP("Clean audit logs.");
        rmAuditAPI.clearAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword());

        STEP("Delete the created hold.");
        holdsAPI.deleteHold(rmAdmin.getUsername(), rmAdmin.getPassword(), HOLD2);

        STEP("Get the list of audit entries for the delete hold event.");
        auditEntries = rmAuditAPI.getRMAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword(), 100,
                DELETE_HOLD.event);

        STEP("Check the audit log contains the entry for the deleted hold with the hold details.");
        assertFalse("The list of events should contain Delete Hold entry ", auditEntries.isEmpty());
        AuditEntry auditEntry = auditEntries.get(0);
        assertTrue("The list of events is not filtered by Delete Hold",
                auditEntry.getEvent().equals(DELETE_HOLD.eventDisplayName));
        assertTrue("The hold name value for the deleted hold is not audited.",
                auditEntry.getNodeName().equals(HOLD2));
        assertTrue("The user who deleted the hold is not audited.",
                auditEntry.getUserName().equals(rmAdmin.getUsername()));
        assertFalse("The date when the hold deletion occurred is not audited.", auditEntry.getTimestamp().isEmpty());
    }

    /**
     * Given an unsuccessful delete hold action
     * When I view the audit log
     * Then the delete hold event isn't audited
     */
    @Test
    public void unsuccessfulDeleteHoldIsNotAudited()
    {
        STEP("Clean audit logs.");
        rmAuditAPI.clearAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword());

        STEP("Try to delete a hold by an user with no Read permissions over the hold.");
        try
        {
            holdsAPI.deleteHold(rmManager.getUsername(), rmManager.getPassword(), HOLD);
            fail("Delete hold action was successful.");
        }
        catch (Exception e)
        {
            STEP("Get the list of audit entries for the delete hold event.");
            auditEntries = rmAuditAPI.getRMAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword(), 100,
                    DELETE_HOLD.event);

            STEP("Check the audit log doesn't contain the entry for the unsuccessful delete hold.");
            assertTrue("The list of events should not contain Delete Hold entry ", auditEntries.isEmpty());
        }
    }

    /**
     * Given a hold is deleted
     * When I view the audit log as an user with no Read permissions over the deleted hold
     * Then the delete hold entry isn't visible
     */
    @Test
    public void deleteHoldAuditEntryNotVisible()
    {
        STEP("Create a new hold.");
        holdsAPI.createHold(rmAdmin.getUsername(), rmAdmin.getPassword(), HOLD2, HOLD_REASON, HOLD_DESCRIPTION);

        STEP("Clean audit logs.");
        rmAuditAPI.clearAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword());

        STEP("Delete the created hold.");
        holdsAPI.deleteHold(rmAdmin.getUsername(), rmAdmin.getPassword(), HOLD2);

        STEP("Get the list of audit entries for the delete hold event as an user with no Read permissions over the hold.");
        auditEntries = rmAuditAPI.getRMAuditLog(rmManager.getUsername(), rmManager.getPassword(), 100,
                DELETE_HOLD.event);

        STEP("Check the audit log doesn't contain the entry for the delete hold event.");
        assertTrue("The list of events should not contain Delete Hold entry ", auditEntries.isEmpty());
    }

    @AfterClass (alwaysRun = true)
    public void cleanUpAuditHoldTests()
    {
        holdsAPI.deleteHold(getAdminUser().getUsername(), getAdminUser().getPassword(), HOLD);
        asList(rmAdmin, rmManager).forEach(user -> getDataUser().usingAdmin().deleteUser(user));
    }
}
