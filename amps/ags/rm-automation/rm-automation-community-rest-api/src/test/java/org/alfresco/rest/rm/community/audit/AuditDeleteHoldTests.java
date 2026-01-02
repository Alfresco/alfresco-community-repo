/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
import static org.alfresco.rest.rm.community.model.audit.AuditEvents.DELETE_HOLD;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import com.google.common.collect.ImmutableMap;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.hold.Hold;
import org.alfresco.rest.rm.community.model.hold.HoldDeletionReason;
import org.alfresco.rest.rm.community.model.user.UserRoles;
import org.alfresco.rest.v0.service.RMAuditService;
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
    private final String hold = PREFIX + "holdToBeDeleted";
    private final String hold2 = PREFIX + "deleteHold";
    private final String hold3 = PREFIX + "deleteHoldWithReason";

    @Autowired
    private RMAuditService rmAuditService;
    @Autowired
    private RoleService roleService;

    private UserModel rmAdmin, rmManager;
    private String holdNodeRef;

    @BeforeClass (alwaysRun = true)
    public void preconditionForAuditDeleteHoldTests()
    {
        STEP("Create a new hold.");
        holdNodeRef = getRestAPIFactory()
            .getFilePlansAPI(rmAdmin)
            .createHold(Hold.builder().name(hold).description(HOLD_DESCRIPTION).reason(HOLD_REASON).build(), FILE_PLAN_ALIAS)
            .getId();

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
        String holdRef = getRestAPIFactory()
            .getFilePlansAPI(rmAdmin)
            .createHold(Hold.builder().name(hold2).description(HOLD_DESCRIPTION).reason(HOLD_REASON).build(), FILE_PLAN_ALIAS)
            .getId();

        rmAuditService.clearAuditLog();

        STEP("Delete the created hold.");
        getRestAPIFactory().getHoldsAPI(rmAdmin).deleteHold(holdRef);

        STEP("Check the audit log contains the entry for the deleted hold with the hold details.");
        rmAuditService.checkAuditLogForEvent(getAdminUser(), DELETE_HOLD, rmAdmin, hold2,
                List.of(ImmutableMap.of("new", "", "previous", hold2, "name", "Hold Name"),
                    ImmutableMap.of("new", "", "previous", "", "name", "Hold deletion reason")));
    }

    /**
     * Given a hold is deleted with a reason
     * When I view the audit log
     * Then an entry has been created in the audit log which contains the following:
     *      name of the hold
     *      hold deletion reason
     *      user who deleted the hold
     *      date the delete occurred
     */
    @Test
    public void deleteHoldWithReasonEventIsAudited()
    {
        STEP("Create a new hold.");
        String holdRef = getRestAPIFactory()
            .getFilePlansAPI(rmAdmin)
            .createHold(Hold.builder().name(hold3).description(HOLD_DESCRIPTION).reason(HOLD_REASON).build(), FILE_PLAN_ALIAS)
            .getId();

        String deletionReason = "Test reason";

        rmAuditService.clearAuditLog();

        STEP("Delete the created hold with a reason.");
        getRestAPIFactory().getHoldsAPI(rmAdmin).deleteHoldWithReason(HoldDeletionReason.builder().reason(deletionReason).build(), holdRef);

        STEP("Check the audit log contains the entry for the deleted hold with the hold details.");
        rmAuditService.checkAuditLogForEvent(getAdminUser(), DELETE_HOLD, rmAdmin, hold3,
            List.of(ImmutableMap.of("new", "", "previous", hold3, "name", "Hold Name"),
                ImmutableMap.of("new", "", "previous", deletionReason, "name", "Hold deletion reason")));
    }

    /**
     * Given an unsuccessful delete hold action
     * When I view the audit log
     * Then the delete hold event isn't audited
     */
    @Test
    public void unsuccessfulDeleteHoldIsNotAudited()
    {
        rmAuditService.clearAuditLog();

        STEP("Try to delete a hold by an user with no Read permissions over the hold.");
        getRestAPIFactory().getHoldsAPI(rmManager).deleteHold(holdNodeRef);
        assertStatusCode(FORBIDDEN);

        STEP("Check the audit log doesn't contain the entry for the unsuccessful delete hold.");
        assertTrue("The list of events should not contain Delete Hold entry ",
                rmAuditService.getAuditEntriesFilteredByEvent(getAdminUser(), DELETE_HOLD).isEmpty());
    }

    @AfterClass (alwaysRun = true)
    public void cleanUpAuditDeleteHoldTests()
    {
        getRestAPIFactory().getHoldsAPI(rmManager).deleteHold(holdNodeRef);
        asList(rmAdmin, rmManager).forEach(user -> getDataUser().usingAdmin().deleteUser(user));
    }
}
