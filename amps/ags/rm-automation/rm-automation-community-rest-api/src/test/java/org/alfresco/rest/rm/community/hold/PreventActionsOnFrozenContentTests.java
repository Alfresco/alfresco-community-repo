/*-
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
package org.alfresco.rest.rm.community.hold;

import static org.alfresco.rest.rm.community.base.TestData.HOLD_DESCRIPTION;
import static org.alfresco.rest.rm.community.base.TestData.HOLD_REASON;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAspects.ASPECTS_VITAL_RECORD;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAspects.ASPECTS_VITAL_RECORD_DEFINITION;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.rest.rm.community.utils.CoreUtil.createBodyForMoveCopy;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.File;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.v0.BaseAPI.RM_ACTIONS;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.common.ReviewPeriod;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.recordfolder.RecordFolder;
import org.alfresco.rest.rm.community.model.recordfolder.RecordFolderProperties;
import org.alfresco.rest.v0.HoldsAPI;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.service.DispositionScheduleService;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.apache.commons.httpclient.HttpStatus.SC_INTERNAL_SERVER_ERROR;
/**
 * API tests to check actions on frozen content
 *
 * @author Rodica Sutu
 * @since 3.2
 */
@AlfrescoTest (jira = "RM-6903")
public class PreventActionsOnFrozenContentTests extends BaseRMRestTest
{
    private static final String HOLD_ONE = "HOLD" + generateTestPrefix(PreventActionsOnFrozenContentTests.class);
    private static String holdNodeRef;
    private static FileModel contentHeld;
    private static File updatedFile;
    private static FolderModel folderModel;
    private static RecordCategoryChild recordFolder;
    private static Record recordFrozen, recordNotHeld;
    private static RecordCategory categoryWithRS;

    @Autowired
    private HoldsAPI holdsAPI;

    @Autowired
    private DispositionScheduleService dispositionScheduleService;

    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;

    @BeforeClass (alwaysRun = true)
    public void preconditionForPreventActionsOnFrozenContent()
    {
        STEP("Create a hold.");
        holdNodeRef = holdsAPI.createHoldAndGetNodeRef(getAdminUser().getUsername(), getAdminUser().getUsername(),
                HOLD_ONE, HOLD_REASON, HOLD_DESCRIPTION);

        STEP("Create a test file.");
        testSite = dataSite.usingAdmin().createPublicRandomSite();
        contentHeld = dataContent.usingAdmin().usingSite(testSite)
                                 .createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        STEP("Add the file to the hold.");
        holdsAPI.addItemToHold(getAdminUser().getUsername(), getAdminUser().getPassword(), contentHeld
                .getNodeRefWithoutVersion(), HOLD_ONE);

        STEP("Get a file resource.");
        updatedFile = Utility.getResourceTestDataFile("SampleTextFile_10kb.txt");

        STEP("Create a folder withing the test site .");
        folderModel = dataContent.usingAdmin().usingSite(testSite)
                                 .createFolder();

        STEP("Create a record folder with some records");
        recordFolder = createCategoryFolderInFilePlan();
        recordFrozen = createElectronicRecord(recordFolder.getId(), getRandomName("elRecordFrozen"));
        recordNotHeld = createElectronicRecord(recordFolder.getId(), getRandomName("elRecordNotHeld"));
        assertStatusCode(CREATED);

        STEP("Add the record to the hold.");
        holdsAPI.addItemToHold(getAdminUser().getUsername(), getAdminUser().getPassword(), recordFrozen.getId(), HOLD_ONE);
    }

    /**
     * Given active content on hold
     * When I try to edit the properties
     * Or perform an action that edits the properties
     * Then I am not successful
     *
     */
    @Test
    public void editPropertiesForContentHeld() throws Exception
    {
        STEP("Update name property of the held content");
        JsonObject nameUpdated = Json.createObjectBuilder().add("name", "HeldNameUpdated").build();
        restClient.authenticateUser(getAdminUser()).withCoreAPI().usingNode(contentHeld).updateNode(nameUpdated.toString());

        STEP("Check the request failed.");
        restClient.assertStatusCodeIs(FORBIDDEN);
        restClient.assertLastError().containsSummary("Frozen content can't be updated.");
    }

    /*
     * Given active content on hold
     * When I try to update the content
     * Then I am not successful
     */
    @Test
    @AlfrescoTest (jira = "RM-6925")
    public void updateContentForFrozenFile() throws Exception
    {
        STEP("Update content of the held file");
        restClient.authenticateUser(getAdminUser()).withCoreAPI().usingNode(contentHeld).updateNodeContent(updatedFile);

        STEP("Check the request failed.");
        //TODO change this to FORBIDDEN when REPO-4632 is fixed
        restClient.assertStatusCodeIs(INTERNAL_SERVER_ERROR);
        restClient.assertLastError().containsSummary("Frozen content can't be updated.");
    }

    /*
     * Given active content on hold
     * When I try to delete the content
     * Then I am not successful
     */
    @Test
    public void deleteFrozenFile() throws Exception
    {
        STEP("Delete frozen file");
        restClient.authenticateUser(getAdminUser()).withCoreAPI().usingNode(contentHeld).deleteNode(contentHeld.getNodeRefWithoutVersion());

        STEP("Check the request failed.");
        restClient.assertStatusCodeIs(FORBIDDEN);
        restClient.assertLastError().containsSummary("Frozen content can't be deleted.");
    }

    /**
     * Given active content on hold
     * When I try to copy the content
     * Then I am not successful
     */
    @Test
    @AlfrescoTest(jira = "RM-6924")
    public void copyFrozenFile()
    {
        STEP("Copy frozen file");
        String postBody = JsonBodyGenerator.keyValueJson("targetParentId",folderModel.getNodeRef());
        getRestAPIFactory().getNodeAPI(contentHeld).copyNode(postBody);

        STEP("Check the request failed.");
        assertStatusCode(FORBIDDEN);
        getRestAPIFactory().getRmRestWrapper().assertLastError().containsSummary("Permission was denied");
    }

    /**
     * Given active content on hold
     * When I try to move the content
     * Then I am not successful
     *
     */
    @Test
    public void moveFrozenFile() throws Exception
    {
        STEP("Move frozen file");
        getRestAPIFactory().getNodeAPI(contentHeld).move(createBodyForMoveCopy(folderModel.getNodeRef()));

        STEP("Check the request failed.");
        assertStatusCode(FORBIDDEN);
        getRestAPIFactory().getRmRestWrapper().assertLastError().containsSummary("Frozen content can't be moved.");
    }

    /**
     * Given a record folder with a frozen record and another record not held
     * When I update the record folder and make the records as vital
     * Then I am successful and the records not held are marked as vital
     * And the frozen nodes have the vital record search properties updated
     */
    @Test
    @AlfrescoTest (jira = "RM-6929")
    public void updateRecordFolderVitalProperties()
    {
        STEP("Update the vital record properties for the record folder");
        // Create the record folder properties to update
        RecordFolder recordFolderToUpdate = RecordFolder.builder()
                                                        .properties(RecordFolderProperties.builder()
                                                                                          .vitalRecordIndicator(true)
                                                                                          .reviewPeriod(new ReviewPeriod("month", "1"))
                                                                                          .build())
                                                        .build();

        // Update the record folder
        RecordFolder updatedRecordFolder = getRestAPIFactory().getRecordFolderAPI().updateRecordFolder
                (recordFolderToUpdate,
                        recordFolder.getId());
        assertStatusCode(OK);
        assertTrue(updatedRecordFolder.getAspectNames().contains(ASPECTS_VITAL_RECORD_DEFINITION));


        STEP("Check the frozen record was not marked as vital");
        recordFrozen = getRestAPIFactory().getRecordsAPI().getRecord(recordFrozen.getId());
        assertFalse(recordFrozen.getAspectNames().contains(ASPECTS_VITAL_RECORD));
        assertTrue(recordFrozen.getProperties().getRecordSearchVitalRecordReviewPeriod().contains("month"));
        assertTrue(recordFrozen.getProperties().getRecordSearchVitalRecordReviewPeriodExpression().contains("1"));

        STEP("Check the record not held was marked as vital");
        recordNotHeld = getRestAPIFactory().getRecordsAPI().getRecord(recordNotHeld.getId());
        assertTrue(recordNotHeld.getAspectNames().contains(ASPECTS_VITAL_RECORD));
        assertNotNull(recordNotHeld.getProperties().getReviewAsOf());
        assertTrue(recordNotHeld.getProperties().getRecordSearchVitalRecordReviewPeriod().contains("month"));
        assertTrue(recordNotHeld.getProperties().getRecordSearchVitalRecordReviewPeriodExpression().contains("1"));
    }

    /**
     * Given a record folder with a frozen record and another record not held
     * When I add a disposition schedule
     * Then I am successful
     * And the record search disposition schedule properties are updated
     */
    @Test
    @AlfrescoTest (jira = "RM-6929")
    public void createDispositionScheduleOnCategoryWithHeldChildren()
    {
        STEP("Create a retention schedule on the category with frozen children");
        RecordCategory categoryWithRS = getRestAPIFactory().getRecordCategoryAPI()
                                                           .getRecordCategory(recordFolder.getParentId());
        dispositionScheduleService.createCategoryRetentionSchedule(categoryWithRS.getName(), false);
        dispositionScheduleService.addCutOffImmediatelyStep(categoryWithRS.getName());
        dispositionScheduleService.addDestroyWithGhostingImmediatelyAfterCutOff(categoryWithRS.getName());

        STEP("Check the record folder has a disposition schedule");
        RecordFolder folderWithRS = getRestAPIFactory().getRecordFolderAPI().getRecordFolder(recordFolder.getId());
        assertNotNull(folderWithRS.getProperties().getRecordSearchDispositionAuthority());
        assertNotNull(folderWithRS.getProperties().getRecordSearchDispositionInstructions());

    }

    /**
     * Given a record category with a disposition schedule applied to records
     * And the disposition schedule has a retain step  immediately and destroy step immediately
     * And a complete record added to one hold
     * When I execute the retain action
     * Then the action is executed
     * And the record search disposition schedule properties are updated
     */
    @Test
    @AlfrescoTest (jira = "RM-6931")
    public void retainActionOnFrozenHeldRecords()
    {
        STEP("Add a category with a disposition schedule.");
        categoryWithRS = createRootCategory(getRandomName("CategoryWithRS"));
        dispositionScheduleService.createCategoryRetentionSchedule(categoryWithRS.getName(), true);
        dispositionScheduleService.addRetainAfterPeriodStep(categoryWithRS.getName(), "immediately");
        dispositionScheduleService.addDestroyWithGhostingImmediatelyAfterCutOff(categoryWithRS.getName());

        STEP("Create record folder with a record.");
        RecordCategoryChild folder = createFolder(categoryWithRS.getId(), getRandomName("RecFolder"));
        Record record = createElectronicRecord(folder.getId(), getRandomName("elRecord"));
        completeRecord(record.getId());

        STEP("Add the record to the hold");
        holdsAPI.addItemToHold(getAdminUser().getUsername(), getAdminUser().getPassword(), record.getId(), HOLD_ONE);

        STEP("Execute the retain action");
        rmRolesAndActionsAPI.executeAction(getAdminUser().getUsername(), getAdminUser().getPassword(), record.getName(),
            RM_ACTIONS.END_RETENTION, null, SC_INTERNAL_SERVER_ERROR);

        STEP("Check the record search disposition properties");
        Record recordUpdated = getRestAPIFactory().getRecordsAPI().getRecord(record.getId());
        assertTrue(recordUpdated.getProperties().getRecordSearchDispositionActionName().contains(RM_ACTIONS.END_RETENTION.getAction()));
        assertTrue(recordUpdated.getProperties().getRecordSearchDispositionPeriod().contains("immediately"));
    }

    @AfterClass (alwaysRun = true)
    public void cleanUpPreventActionsOnFrozenContent()
    {
        holdsAPI.deleteHold(getAdminUser(), holdNodeRef);
        dataSite.usingAdmin().deleteSite(testSite);
        deleteRecordCategory(recordFolder.getParentId());
        deleteRecordCategory(categoryWithRS.getId());
    }
}
