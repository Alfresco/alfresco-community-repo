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
package org.alfresco.rest.rm.community.smoke;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.v0.RecordFoldersAPI;
import org.alfresco.rest.v0.service.DispositionScheduleService;
import org.alfresco.test.AlfrescoTest;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.alfresco.rest.rm.community.model.recordcategory.RetentionPeriodProperty.CREATED_DATE;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;

public class FoldersDispositionScheduleTests extends BaseRMRestTest {

    private RecordCategory Category1;
    @Autowired
    private DispositionScheduleService dispositionScheduleService;
    @Autowired
    private RecordFoldersAPI recordFoldersAPI;
    private final String TEST_PREFIX = generateTestPrefix(FoldersDispositionScheduleTests.class);
    private final String folderDisposition = TEST_PREFIX + "RM-2937 folder ghosting";
    private final String electronicRecord = "RM-2937 electronic 2 record";
    private final String nonElectronicRecord = "RM-2937 non-electronic record";

    @BeforeClass(alwaysRun = true)
    private void setUp(){

        STEP("Create the RM site if doesn't exist");
        createRMSiteIfNotExists();

        STEP("Create record category");
        Category1 = createRootCategory(getRandomName("Title"));
    }

    @Test
    @AlfrescoTest (jira = "RM-2937")
    public void foldersDispositionScheduleWithGhosting() {

        //create retention schedule
        dispositionScheduleService.createCategoryRetentionSchedule(Category1.getName(), false);

        // add cut off step
        dispositionScheduleService.addCutOffAfterPeriodStep(Category1.getName(), "day|2", CREATED_DATE);

        // add destroy step with ghosting
        dispositionScheduleService.addDestroyWithGhostingImmediatelyAfterCutOff(Category1.getName());

        //create folders
        RecordCategoryChild FOLDER_DESTROY = createFolder(getAdminUser(),Category1.getId(),folderDisposition);

        Record elRecord = createElectronicRecord(FOLDER_DESTROY.getId(),electronicRecord);
        Record nonElRecord = createNonElectronicRecord(FOLDER_DESTROY.getId(),nonElectronicRecord);

        // complete records
        completeRecord(elRecord.getId());
        completeRecord(nonElRecord.getId());

        // edit disposition date
        recordFoldersAPI.postFolderAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),editDispositionDateJson(),FOLDER_DESTROY.getName());

        // cut off the FOLDER_DESTROY
        recordFoldersAPI.postFolderAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","cutoff"),FOLDER_DESTROY.getName());

        // Destroy the FOLDER_DESTROY
        recordFoldersAPI.postFolderAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","destroy"),FOLDER_DESTROY.getName());
    }

    @AfterMethod(alwaysRun = true)
    private  void deletePreconditions() {
        deleteRecordCategory(Category1.getId());
    }
}
