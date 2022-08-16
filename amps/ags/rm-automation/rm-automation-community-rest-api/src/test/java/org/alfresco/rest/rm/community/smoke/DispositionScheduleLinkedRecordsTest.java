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


package org.alfresco.rest.rm.community.smoke;

import org.alfresco.rest.model.RestNodeBodyMoveCopyModel;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.requests.Node;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.fileplan.FilePlan;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.record.RecordBodyFile;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.service.DispositionScheduleService;
import org.alfresco.test.AlfrescoTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.alfresco.rest.rm.community.base.TestData.DEFAULT_PASSWORD;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.recordcategory.RetentionPeriodProperty.CREATED_DATE;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.rest.rm.community.utils.CoreUtil.createBodyForMoveCopy;
import static org.alfresco.rest.rm.community.utils.CoreUtil.toContentModel;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.CREATED;
import static org.testng.Assert.assertEquals;

public class DispositionScheduleLinkedRecordsTest extends BaseRMRestTest {


    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;
    @Autowired
    private DispositionScheduleService dispositionScheduleService;

    private final String TEST_PREFIX = generateTestPrefix(DispositionScheduleLinkedRecordsTest.class);
    private final String RM_ADMIN = TEST_PREFIX + "rm_admin";
    private final String RM_MANAGER = TEST_PREFIX + "rm_admin";
    private RecordCategory Category1;
    private RestNodeModel CopyCategory1;
    private final String folderDisposition = TEST_PREFIX + "RM-2801 folder";
    private RecordCategoryChild CopyCatFolder,CatFolder;




    @BeforeClass(alwaysRun = true)
    public void setupDispositionScheduleLinkedRecordsTest() {

        createRMSiteIfNotExists();

        // create "rm admin" user if it does not exist and assign it to RM Administrator role
        rmRolesAndActionsAPI.createUserAndAssignToRole(
            getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(),
            RM_ADMIN, DEFAULT_PASSWORD, "Administrator");

        // create "rm Manager" user if it does not exist and assign it to RM Administrator role
        rmRolesAndActionsAPI.createUserAndAssignToRole(
            getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(),
            RM_MANAGER, DEFAULT_PASSWORD, "Administrator");

        // add transfer step
        //dispositionScheduleService.addTransferAfterEventStep(Category1.getName(),"transferred records","all_allowances_granted_are_terminated");

    }


    @Test
    @AlfrescoTest(jira = "RM-1622")
    public void dispositionScheduleLinkedRecords() throws Exception {
        STEP("Create record category");
        Category1 = createRootCategory(getRandomName("Title"));

        //create retention schedule
        dispositionScheduleService.createCategoryRetentionSchedule(Category1.getName(), false);
        // add cut off step
        dispositionScheduleService.addCutOffAfterPeriodStep(Category1.getName(), "day|2", CREATED_DATE);

        //create a copy of the category recordsCategory
        FilePlan filePlan = getRestAPIFactory().getFilePlansAPI().getFilePlan(FILE_PLAN_ALIAS);
       CopyCategory1= getRestAPIFactory().getNodeAPI(toContentModel(Category1.getId())).copy(createBodyForMoveCopy(filePlan.getId()));


        // create folders in both categories
        CatFolder = createRecordFolder(Category1.getId(), getRandomName("recFolder"));
       // CopyCatFolder = createRecordFolder(CopyCategory1.getId(), getRandomName("recFolder"));

        // create record  files
        String electronicRecord = "RM-2801 electronic record";
        Record elRecord = createElectronicRecord(CatFolder.getId(), electronicRecord);
        String nonElectronicRecord = "RM-2801 non-electronic record";
        Record nonElRecord = createNonElectronicRecord(CatFolder.getId(), nonElectronicRecord);


        // link the records to copy folder, then complete them
       /* Record recordLink = fileRecordToFolder(unfiledRecordId, folderToLink);
        assertStatusCode(CREATED);
        assertEquals(recordLink.getParentId(), targetFolderId);

        private Record fileRecordToFolder(String recordId, String targetFolderId)
        {
            RecordBodyFile recordBodyFile = RecordBodyFile.builder().targetParentId(targetFolderId).build();
            return getRestAPIFactory().getRecordsAPI().fileRecord(recordBodyFile, recordId);
        }*/












    }




    }

