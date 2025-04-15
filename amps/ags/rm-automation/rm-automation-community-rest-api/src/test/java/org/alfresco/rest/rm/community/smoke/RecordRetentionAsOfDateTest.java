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

package org.alfresco.rest.rm.community.smoke;

import org.alfresco.rest.core.v0.BaseAPI;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.RecordCategoriesAPI;
import org.alfresco.rest.v0.RecordFoldersAPI;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.rest.v0.service.DispositionScheduleService;
import org.alfresco.test.AlfrescoTest;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import static org.alfresco.rest.rm.community.base.TestData.DEFAULT_PASSWORD;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.testng.Assert.assertTrue;

public class RecordRetentionAsOfDateTest extends BaseRMRestTest {

    /** data prep 6services */
    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;
    @Autowired
    private RecordsAPI recordsAPI;
    @Autowired
    private RecordFoldersAPI recordFoldersAPI;
    @Autowired
    private RecordCategoriesAPI recordCategoriesAPI;
    @Autowired
    private DispositionScheduleService dispositionScheduleService;
    private RecordCategory Category1;
    private final String TEST_PREFIX = generateTestPrefix(RecordRetentionAsOfDateTest.class);
    private final String RM_ADMIN = TEST_PREFIX + "rm_admin";
    private final String recordsCategory = TEST_PREFIX + "RM-5733 category";
    private final String folderDisposition = TEST_PREFIX + "RM-5733 folder";

    private static final String YEAR_MONTH_DAY = "yyyy-MM-dd";

    @Test
    @AlfrescoTest (jira = "RM-5733,RM-5799")
    public void checkRetentionAsOfDateForTransferStepWithRetentionAction() {

        // create test precondition
        createTestPrecondition(recordsCategory);

        // create disposition schedule
        dispositionScheduleService.createCategoryRetentionSchedule(Category1.getName(), true);

        // add cut off step
        dispositionScheduleService.addCutOffImmediatelyStep(Category1.getName());

        // add transfer step
        HashMap<BaseAPI.RETENTION_SCHEDULE, String> transferStep = new HashMap<>();
        transferStep.put(BaseAPI.RETENTION_SCHEDULE.RETENTION_PERIOD, "day|1");
        transferStep.put(BaseAPI.RETENTION_SCHEDULE.NAME, "transfer");
        transferStep.put(BaseAPI.RETENTION_SCHEDULE.RETENTION_PERIOD_PROPERTY, "rma:cutOffDate");
        transferStep.put(BaseAPI.RETENTION_SCHEDULE.COMBINE_DISPOSITION_STEP_CONDITIONS, "false");
        transferStep.put(BaseAPI.RETENTION_SCHEDULE.RETENTION_ELIGIBLE_FIRST_EVENT, "true");
        transferStep.put(BaseAPI.RETENTION_SCHEDULE.RETENTION_GHOST, "on");
        transferStep.put(BaseAPI.RETENTION_SCHEDULE.DESCRIPTION, "Transfer after 1 day");
        recordCategoriesAPI.addDispositionScheduleSteps(getAdminUser().getUsername(),
            getAdminUser().getPassword(), Category1.getName(), transferStep);

        // create a folder and an electronic and a non-electronic record in it
        RecordCategoryChild FOLDER = createFolder(getAdminUser(),Category1.getId(),folderDisposition);

        String nonElectronicRecord = TEST_PREFIX + "RM-5733 non-electronic record";
        Record nonElRecord = createNonElectronicRecord(FOLDER.getId(), nonElectronicRecord);

        // complete records and cut them off
        String nonElRecordName = recordsAPI.getRecordFullName(getAdminUser().getUsername(),
            getAdminUser().getPassword(), folderDisposition, nonElectronicRecord);

        // complete records and cut them off
        completeRecord(nonElRecord.getId());

        String nonElRecordNameNodeRef = recordsAPI.getRecordNodeRef(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), nonElRecordName, "/" + Category1.getName() + "/" + folderDisposition);
        recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","cutoff"),nonElRecordNameNodeRef);

        JSONObject nextDispositionActionJson = recordCategoriesAPI.getNextDispositionAction(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(),nonElRecord.getId());

        assertTrue(getAsOfDate(nextDispositionActionJson).startsWith(getTomorrow()),
            "The retention as of date is not set to tomorrow.");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        // delete category
        deleteRecordCategory(Category1.getId());
    }

    private void createTestPrecondition(String categoryName) {
        createRMSiteIfNotExists();

        // create "rm admin" user if it does not exist and assign it to RM Administrator role
        rmRolesAndActionsAPI.createUserAndAssignToRole(
            getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(),
            RM_ADMIN, DEFAULT_PASSWORD, "Administrator");

        // create category
        STEP("Create category");
        Category1 = createRootCategory(categoryName,"Title");
    }

    private String getAsOfDate(JSONObject nextDispositionActionJson) {
        return nextDispositionActionJson.getJSONObject("data").get("asOf").toString();
    }

    private static String getTomorrow() {
        Date today = new Date();
        Date tomorrow = DateUtils.addDays(today, 1);
        SimpleDateFormat dateFormat = new SimpleDateFormat(YEAR_MONTH_DAY);
        return dateFormat.format(tomorrow);
    }

}
