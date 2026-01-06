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

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.util.AssertionErrors.assertTrue;

import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.alfresco.utility.report.log.Step.STEP;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.audit.AuditEntry;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.v0.RMAuditAPI;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.model.UserModel;

public class RecordCategoryAuditLogTest extends BaseRMRestTest
{
    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;
    @Autowired
    private RMAuditAPI auditLog;

    private final String TEST_PREFIX = generateTestPrefix(RecordCategoryAuditLogTest.class);
    private final String RM_ADMIN = TEST_PREFIX + "rm_admin";
    private static final String AUDIT_CATEGORY = generateTestPrefix(RecordCategoryAuditLogTest.class) + "category";
    private RecordCategory recordCategoryAudit;

    @BeforeClass(alwaysRun = true)
    public void recordCategoryAuditLogSetup()
    {
        STEP("Create RM Site");
        createRMSiteIfNotExists();

        STEP("Create RM Admin user");
        rmRolesAndActionsAPI.createUserAndAssignToRole(getAdminUser().getUsername(), getAdminUser().getPassword(), RM_ADMIN,
                getAdminUser().getPassword(),
                "Administrator");
    }

    @Test
    @AlfrescoTest(jira = "RM-2768")
    public void recordCategoryAudit() throws Exception
    {
        STEP("Create root level category");
        recordCategoryAudit = createRootCategory(AUDIT_CATEGORY);
        List<AuditEntry> auditEntries = auditLog.getRMAuditLogAll(getAdminUser().getUsername(), getAdminUser().getPassword(), 100);
        // newly created record category contains 3 events: object creation, inherited permissions set to false and metadata update
        // the order in which object creation and metadata update are listed isn't always identical due to
        // both happening in the same transaction
        assertTrue("Created Object Event is not present.", auditEntries.stream().anyMatch(x -> x.getEvent().startsWith("Created Object")));
        assertTrue("Updated metadata Event is not present.", auditEntries.stream().anyMatch(x -> x.getEvent().startsWith("Updated Metadata")));
    }

    @Test(
            dependsOnMethods = "recordCategoryAudit",
            description = "Viewing audit log is itself an auditable event")
    @AlfrescoTest(jira = "RM-4303")
    public void recordCategoryAuditIsEvent()
    {
        List<AuditEntry> auditEntries = auditLog.getRMAuditLogAll(getAdminUser().getUsername(), getAdminUser().getPassword(), 100);
        assertTrue("Audit View Event is not present.", auditEntries.stream().anyMatch(x -> x.getEvent().startsWith("Audit View")));
    }

    @Test(
            dependsOnMethods = "recordCategoryAuditIsEvent",
            description = "Record category rename is an edit metadata event")
    @AlfrescoTest(jira = "RM-4303")
    public void renameRecordCategory()
    {
        String categoryName = "Category name " + getRandomAlphanumeric();
        RecordCategory rootRecordCategory = createRootCategory(categoryName);
        String newCategoryName = "Rename " + categoryName;
        RecordCategory recordCategoryUpdated = RecordCategory.builder().name(newCategoryName).build();
        RecordCategory renamedRecordCategory = getRestAPIFactory().getRecordCategoryAPI().updateRecordCategory(recordCategoryUpdated, rootRecordCategory.getId());

        assertStatusCode(OK);
        // we expect 1 new event: "metadata update"
        List<AuditEntry> auditEntries = auditLog.getRMAuditLogAll(getAdminUser().getUsername(), getAdminUser().getPassword(), 100);
        assertTrue("Updated metadata Event is not present.", auditEntries.stream().anyMatch(x -> x.getEvent().startsWith("Updated Metadata")));
    }

    @AfterClass(alwaysRun = true)
    private void electronicRecordAuditLogCleanup()
    {
        deleteRecordCategory(recordCategoryAudit.getId());
        dataUser.deleteUser(new UserModel(RM_ADMIN,
                getAdminUser().getPassword()));
        auditLog.clearAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword());
    }
}
