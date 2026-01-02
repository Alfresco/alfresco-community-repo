/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.audit.AuditEntry;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.v0.RMAuditAPI;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.UserModel;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAspects.ASPECTS_COMPLETED_RECORD;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createRecordModel;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.util.AssertionErrors.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;

public class ElectronicRecordAuditLogTest extends BaseRMRestTest {

    private Optional<UserModel> rmAdmin;
    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;
    @Autowired
    private RMAuditAPI auditLog;
    @Autowired
    private RecordsAPI recordApi;
    /* electronic record details */
    private static final String AUDIT_ELECTRONIC_RECORD = generateTestPrefix(ElectronicRecordAuditLogTest.class) + "electronic record";
    private static final String AUDIT_COMPLETE_REOPEN_ELECTRONIC_RECORD = "Complete Reopen Electronic Record";
    public static final String TITLE = "Title";
    public static final String DESCRIPTION = "Description";
    private RecordCategory category1;
    private RecordCategoryChild recordFolder1;
    private Record electronicRecord, electronicRecord2;

    @BeforeClass(alwaysRun = true)
    public void electronicRecordsAuditLogSetup()
    {
        createRMSiteIfNotExists();
        rmAdmin = Optional.ofNullable(getDataUser().createRandomTestUser());
        rmRolesAndActionsAPI.assignRoleToUser(
            getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(),
            rmAdmin.get().getUsername(),
            "Administrator");
        auditLog.clearAuditLog(rmAdmin.get().getUsername(),rmAdmin.get().getPassword());
        category1 = createRootCategory(TITLE, DESCRIPTION);
        recordFolder1 = createFolder(category1.getId(),TITLE);

        electronicRecord = createElectronicRecord(recordFolder1.getId(),AUDIT_ELECTRONIC_RECORD,rmAdmin.get());
    }

    @Test(description = "Audit log for newly filed electronic record")
    @AlfrescoTest(jira="RM-4303")
    public void newElectronicRecordAudit() {
        List<AuditEntry> auditEntries= auditLog.getRMAuditLogAll(getAdminUser().getUsername(),getAdminUser().getPassword(),100);

        // newly created record contains 2 events: "file to" and metadata update
        // the order in which object creation and metadata update are listed isn't always identical due to
        // both happening in the same transaction
        assertTrue("File To Event is not present.",auditEntries.stream().anyMatch(x -> x.getEvent().startsWith("File to")));
        assertTrue("Updated metadata Event is not present.",auditEntries.stream().anyMatch(x -> x.getEvent().startsWith("Updated Metadata")));
    }

    @Test
        (
            dependsOnMethods = "newElectronicRecordAudit",
            description = "Viewing electronic record audit log is itself an auditable event"
        )
    @AlfrescoTest(jira="RM-4303")
    public void electronicRecordAuditIsEvent()
    {
        List<AuditEntry> auditEntries= auditLog.getRMAuditLogAll(getAdminUser().getUsername(),getAdminUser().getPassword(),100);
        assertTrue("Audit View Event is not present.",auditEntries.stream().anyMatch(x -> x.getEvent().startsWith("Audit View")));
    }

    @Test
        (
            dependsOnMethods = "electronicRecordAuditIsEvent",
            description = "Rename electronic record is an edit metadata event"
        )
    @AlfrescoTest(jira="RM-4303")
    public void renameElectronicRecord() {
        auditLog.clearAuditLog(rmAdmin.get().getUsername(),rmAdmin.get().getPassword());
        Record renameElectronicRecord = createRecordModel("edited " + electronicRecord.getName(), "", "");

        // rename record
        getRestAPIFactory().getRecordsAPI().updateRecord(renameElectronicRecord, electronicRecord.getId());
        assertStatusCode(OK);

        // we expect 1 new event: "metadata update"
        List<AuditEntry> auditEntries= auditLog.getRMAuditLogAll(getAdminUser().getUsername(),getAdminUser().getPassword(),100);
        assertTrue("Updated metadata Event is not present.",auditEntries.stream().anyMatch(x -> x.getEvent().startsWith("Updated Metadata")));
    }

    @Test (
        dependsOnMethods = "newElectronicRecordAudit",
        description = "Complete and reopen electronic record")
    @AlfrescoTest(jira="RM-4303")
    public void completeAndReopenElectronicRecord() {
        electronicRecord2 = createElectronicRecord(recordFolder1.getId(),AUDIT_COMPLETE_REOPEN_ELECTRONIC_RECORD);

        // complete record
        recordApi.completeRecord(rmAdmin.get().getUsername(),rmAdmin.get().getPassword(),
            electronicRecord2.getName());

        try
        {
            Utility.sleep(1000, 30000, () ->
            {
                org.alfresco.rest.rm.community.requests.gscore.api.RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();
                List<String> aspects = recordsAPI.getRecord(electronicRecord2.getId()).getAspectNames();
                // a record must be completed
                assertTrue("Record is not completed.",aspects.contains(ASPECTS_COMPLETED_RECORD));
            });
        }
        catch (InterruptedException e)
        {
            fail("InterruptedException received while waiting for results.");
        }

        List<AuditEntry> auditEntries= auditLog.getRMAuditLogAll(getAdminUser().getUsername(),getAdminUser().getPassword(),100);
        assertTrue("Complete Record Event is not present.",auditEntries.stream().anyMatch(x -> x.getEvent().startsWith("Complete Record")));

        // Reopen record
        recordApi.reOpenRecord(rmAdmin.get().getUsername(),rmAdmin.get().getPassword(),
            electronicRecord2.getName());

        try
        {
            Utility.sleep(1000, 30000, () ->
            {
                org.alfresco.rest.rm.community.requests.gscore.api.RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();
                List<String> aspects = recordsAPI.getRecord(electronicRecord2.getId()).getAspectNames();
                // a record mustn't be completed
                assertFalse(aspects.contains(ASPECTS_COMPLETED_RECORD));
            });
        }
        catch (InterruptedException e)
        {
            fail("InterruptedException received while waiting for results.");
        }

        auditEntries= auditLog.getRMAuditLogAll(getAdminUser().getUsername(),getAdminUser().getPassword(),100);
        assertTrue("Reopen Record Event is not present.",auditEntries.stream().anyMatch(x -> x.getEvent().startsWith("Reopen Record")));
    }

    @Test
        (
            dependsOnMethods = "completeAndReopenElectronicRecord",
            description = "File electronic record's audit log as record"
        )
    @AlfrescoTest(jira="RM-4303")
    public void fileElectronicRecordAuditLogAsRecord()
    {
        // audit log is stored in the same folder, refresh it so that it appears in the list
        HttpResponse auditRecordHttpResponse = auditLog.logsAuditLogAsRecord(rmAdmin.get().getUsername(),rmAdmin.get().getPassword(),
        getRecordNodeRef(electronicRecord2.getId()),getFolderNodeRef(recordFolder1.getId()));
        JSONObject auditRecordProperties = getAuditPropertyValues(auditRecordHttpResponse);
        Record auditRecord = getRestAPIFactory().getRecordsAPI().getRecord(auditRecordProperties.get("record").toString()
            .replace("workspace://SpacesStore/",""));
        // check audit log
        AssertJUnit.assertTrue(auditRecordProperties.get("recordName").toString().endsWith(".html"));
        AssertJUnit.assertTrue(auditRecord.getAspectNames().stream().noneMatch(x -> x.startsWith(ASPECTS_COMPLETED_RECORD)));
    }

    private String getFolderNodeRef(String folderId) {
        return "workspace://SpacesStore/" + folderId;
    }

    private String getRecordNodeRef(String recordId) {
        return "workspace/SpacesStore/" + recordId;
    }

    private JSONObject getAuditPropertyValues(HttpResponse httpResponse) {
        HttpEntity entity = httpResponse.getEntity();
        String responseString = null;
        try {
            responseString = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JSONObject result = new JSONObject(responseString);
        return result;
    }

    @AfterMethod
    private void closeAuditLog() {
        auditLog.clearAuditLog(rmAdmin.get().getUsername(),rmAdmin.get().getPassword());
    }

    @AfterClass(alwaysRun = true)
    private void electronicRecordAuditLogCleanup() {
        deleteRecord(electronicRecord.getId());
        deleteRecordFolder(recordFolder1.getId());
        deleteRecordCategory(category1.getId());
        dataUser.usingAdmin().deleteUser(new UserModel(rmAdmin.get().getUsername(), rmAdmin.get().getPassword()));
    }
}
