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
import static org.testng.Assert.fail;

import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createRecordFolderModel;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.audit.AuditEntry;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.recordfolder.RecordFolder;
import org.alfresco.rest.v0.RMAuditAPI;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.RecordFoldersAPI;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.UserModel;

public class RecordFolderAuditLogTest extends BaseRMRestTest
{

    private Optional<UserModel> rmAdmin;
    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;
    @Autowired
    private RMAuditAPI auditLog;

    @Autowired
    private RecordFoldersAPI recordFoldersAPI;
    private RecordCategory category1;
    private RecordCategoryChild recordFolder1;
    public static final String TITLE = "Title";
    public static final String DESCRIPTION = "Description";

    @BeforeClass(alwaysRun = true)
    public void recordFolderAuditLogSetup()
    {
        createRMSiteIfNotExists();
        rmAdmin = Optional.ofNullable(getDataUser().createRandomTestUser());
        rmRolesAndActionsAPI.assignRoleToUser(
                getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(),
                rmAdmin.get().getUsername(),
                "Administrator");
    }

    @Test(description = "Audit log for empty record folder")
    @AlfrescoTest(jira = "RM-4303")
    public void recordFolderAudit()
    {
        category1 = createRootCategory(TITLE, DESCRIPTION);
        recordFolder1 = createFolder(category1.getId(), TITLE);
        List<AuditEntry> auditEntries = auditLog.getRMAuditLogAll(getAdminUser().getUsername(), getAdminUser().getPassword(), 100);
        assertTrue("Created Object Event is not present.", auditEntries.stream().anyMatch(x -> x.getEvent().startsWith("Created Object")));
        assertTrue("Updated metadata Event is not present.", auditEntries.stream().anyMatch(x -> x.getEvent().startsWith("Updated Metadata")));

    }

    @Test(
            dependsOnMethods = "recordFolderAudit",
            description = "Viewing record folder audit log is itself an auditable event")
    @AlfrescoTest(jira = "RM-4303")
    public void recordFolderAuditIsEvent()
    {
        List<AuditEntry> auditEntries = auditLog.getRMAuditLogAll(getAdminUser().getUsername(), getAdminUser().getPassword(), 100);
        assertTrue("Audit View Event is not present.", auditEntries.stream().anyMatch(x -> x.getEvent().startsWith("Audit View")));

    }

    @Test(
            dependsOnMethods = "recordFolderAuditIsEvent",
            description = "Record folder rename is an edit metadata event")
    @AlfrescoTest(jira = "RM-4303")
    public void renameRecordFolder()
    {
        auditLog.clearAuditLog(rmAdmin.get().getUsername(), rmAdmin.get().getPassword());
        RecordFolder renameRecordFolder = createRecordFolderModel(category1.getId(), "edited");
        getRestAPIFactory().getRecordFolderAPI().updateRecordFolder(renameRecordFolder, recordFolder1.getId());
        assertStatusCode(OK);
        // we expect 1 new event: "metadata update"
        List<AuditEntry> auditEntries = auditLog.getRMAuditLogAll(getAdminUser().getUsername(), getAdminUser().getPassword(), 100);
        // assertTrue("Move To Event is not present.",auditEntries.stream().anyMatch(x -> x.getEvent().startsWith("Move to")));
        assertTrue("Updated metadata Event is not present.", auditEntries.stream().anyMatch(x -> x.getEvent().startsWith("Updated Metadata")));

    }

    @Test(dependsOnMethods = "recordFolderAudit",
            description = "Close and reopen folder")
    @AlfrescoTest(jira = "RM-4303")
    public void closeReopenFolder()
    {
        // close folder
        recordFoldersAPI.closeRecordFolder(rmAdmin.get().getUsername(), rmAdmin.get().getPassword(),
                recordFolder1.getName());
        try
        {
            Utility.sleep(1000, 30000, () -> {
                List<AuditEntry> auditEntries = auditLog.getRMAuditLogAll(getAdminUser().getUsername(), getAdminUser().getPassword(), 100);
                assertTrue("Folder Close Record Event is not present.", auditEntries.stream().anyMatch(x -> x.getEvent().startsWith("Close Record Folder")));

            });
        }
        catch (InterruptedException e)
        {
            fail("InterruptedException received while waiting for results.");
        }

        // reopen folder
        recordFoldersAPI.reOpenRecordFolder(rmAdmin.get().getUsername(), rmAdmin.get().getPassword(),
                recordFolder1.getName());
        try
        {
            Utility.sleep(1000, 30000, () -> {

                List<AuditEntry> auditEntries = auditLog.getRMAuditLogAll(getAdminUser().getUsername(), getAdminUser().getPassword(), 100);
                assertTrue("Reopen Record Event is not present.", auditEntries.stream().anyMatch(x -> x.getEvent().startsWith("Open Record Folder")));

            });
        }
        catch (InterruptedException e)
        {
            fail("InterruptedException received while waiting for results.");
        }
    }

    @AfterMethod
    private void closeAuditLog()
    {
        auditLog.clearAuditLog(rmAdmin.get().getUsername(), rmAdmin.get().getPassword());
    }

    @AfterClass(alwaysRun = true)
    public void recordFolderAuditLogCleanup()
    {
        deleteRecordFolder(recordFolder1.getId());
        deleteRecordCategory(category1.getId());
        dataUser.usingAdmin().deleteUser(new UserModel(rmAdmin.get().getUsername(), rmAdmin.get().getPassword()));
    }

}
