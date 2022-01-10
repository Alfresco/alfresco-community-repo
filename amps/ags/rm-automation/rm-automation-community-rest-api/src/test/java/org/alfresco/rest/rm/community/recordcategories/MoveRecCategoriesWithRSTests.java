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
package org.alfresco.rest.rm.community.recordcategories;

import static org.alfresco.rest.rm.community.model.recordcategory.RetentionPeriodProperty.CREATED_DATE;
import static org.alfresco.rest.rm.community.model.recordcategory.RetentionPeriodProperty.CUT_OFF_DATE;
import static org.alfresco.rest.rm.community.model.recordcategory.RetentionPeriodProperty.DATE_FILED;
import static org.alfresco.rest.rm.community.utils.CoreUtil.createBodyForMoveCopy;
import static org.alfresco.rest.rm.community.utils.CoreUtil.toContentModel;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.OK;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.alfresco.rest.core.v0.BaseAPI.RM_ACTIONS;
import org.alfresco.rest.core.v0.RMEvents;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.v0.service.DispositionScheduleService;
import org.alfresco.test.AlfrescoTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for moving record categories between record categories with different retention schedule
 */
public class MoveRecCategoriesWithRSTests extends BaseRMRestTest
{
    private RecordCategory rootCategory, rootCategory2;
    private Record elRecord, nonElRecord;
    @Autowired
    private DispositionScheduleService dispositionScheduleService;

    /**
     * Create two root categories with some retention schedules on record level
     */
    @BeforeMethod
    private void setUpMoveRecCategoriesWithRSTests()
    {
        STEP("Create record category with retention schedule and apply it to records.");
        rootCategory = createRootCategory(getRandomName("rootCategory1"));
        dispositionScheduleService.createCategoryRetentionSchedule(rootCategory.getName(), true);

        STEP("Create record category with retention schedule and apply it to records.");
        rootCategory2 = createRootCategory(getRandomName("rootCategory2"));
        dispositionScheduleService.createCategoryRetentionSchedule(rootCategory2.getName(), true);
    }

    /**
     * Given following structure is created:
     * rootCategory1 with RS applied on record level with cut off and destroy after 1 day
     * - subCategory1 without RS
     * - recFolder
     * - incomplete electronic record
     * - complete non-electronic record
     * rootCategory2 with RS with retain and destroy both after  2 day
     * When moving subcategory1 within rootCategory2
     * Then the records will inherit the RS from rootCategory2
     */
    @Test
    @AlfrescoTest (jira = "APPS-1005")
    public void testInheritWhenMoveToDifferentRSStep() throws Exception
    {
        STEP("Add retention schedule cut off step after 1 day period.");
        dispositionScheduleService.addCutOffAfterPeriodStep(rootCategory.getName(), "day|1", CREATED_DATE);

        STEP("Add retention schedule destroy step after 1 Day period.");
        dispositionScheduleService.addDestroyWithGhostingAfterPeriodStep(rootCategory.getName(), "day|1", CUT_OFF_DATE);

        STEP("Create a subcategory with a record folder and records.");
        RecordCategoryChild subCategory = createSubCategoryWithRecords();

        STEP("Add retention schedule retain step after 2 day period.");
        dispositionScheduleService.addRetainAfterPeriodStep(rootCategory2.getName(), "day|2");

        STEP("Add retention schedule destroy step after 2 Day period.");
        dispositionScheduleService.addDestroyWithGhostingAfterPeriodStep(rootCategory2.getName(), "day|2", DATE_FILED);

        STEP("Move the subcategory within the rootCategory2.");
        getRestAPIFactory().getNodeAPI(toContentModel(subCategory.getId())).move(createBodyForMoveCopy(rootCategory2.getId()));
        assertStatusCode(OK);

        STEP("Check that both records inherit rootCategory2 retention schedule");
        elRecord = getRestAPIFactory().getRecordsAPI().getRecord(elRecord.getId());
        nonElRecord = getRestAPIFactory().getRecordsAPI().getRecord(nonElRecord.getId());
        assertTrue(elRecord.getProperties().getRecordSearchDispositionActionName().equalsIgnoreCase(RM_ACTIONS.END_RETENTION.getAction()),
                "Disposition action should be retain");
        assertTrue(elRecord.getProperties().getRecordSearchDispositionPeriod().equalsIgnoreCase("day"),
                "Disposition period property should be day");
        assertTrue(elRecord.getProperties().getRecordSearchDispositionPeriodExpression().equalsIgnoreCase("2"),
                "Disposition period expression should be 2");
        assertTrue(nonElRecord.getProperties().getRecordSearchDispositionActionName().equalsIgnoreCase(RM_ACTIONS.END_RETENTION.getAction()),
                "Disposition action should be retain");
        assertTrue(nonElRecord.getProperties().getRecordSearchDispositionPeriod().equalsIgnoreCase("day"),
                "Disposition period property should be day");
        assertTrue(nonElRecord.getProperties().getRecordSearchDispositionPeriodExpression().equalsIgnoreCase("2"),
                "Disposition period expression should be 2");
    }

    /**
     * Given following structure is created:
     * rootCategory1 with RS applied on record level with retain and destroy after 1 day
     * - subCategory without RS
     * - recFolder
     * - incomplete electronic record
     * - complete non-electronic record
     * rootCategory2 with RS with cut off on event case closed and destroy both after  2 day
     * When moving subcategory within rootCategory2
     * Then the records will inherit the RS from rootCategory2
     */
    @Test
    @AlfrescoTest (jira = "APPS-1004")
    public void testInheritWhenMoveToDifferentRSStepOnEventBase() throws Exception
    {
        STEP("Add retention schedule retain step after 1 day period.");
        dispositionScheduleService.addRetainAfterPeriodStep(rootCategory.getName(), "day|1");

        STEP("Add retention schedule destroy step after 1 Day period.");
        dispositionScheduleService.addDestroyWithGhostingAfterPeriodStep(rootCategory.getName(), "day|1", CUT_OFF_DATE);

        STEP("Create a subcategory with a record folder and records.");
        RecordCategoryChild subCategory = createSubCategoryWithRecords();

        STEP("Add retention schedule cut off step on event case closed.");
        dispositionScheduleService.addCutOffAfterEventStep(rootCategory2.getName(), RMEvents.CASE_CLOSED.getEventName());

        STEP("Add retention schedule destroy step after 1 Day period.");
        dispositionScheduleService.addDestroyWithGhostingAfterPeriodStep(rootCategory2.getName(), "day|2", DATE_FILED);

        STEP("Move the subcategory within the rootCategory2.");
        getRestAPIFactory().getNodeAPI(toContentModel(subCategory.getId())).move(createBodyForMoveCopy(rootCategory2.getId()));
        assertStatusCode(OK);

        STEP("Check that both records inherit rootCategory2 retention schedule");
        elRecord = getRestAPIFactory().getRecordsAPI().getRecord(elRecord.getId());
        nonElRecord = getRestAPIFactory().getRecordsAPI().getRecord(nonElRecord.getId());
        assertTrue(elRecord.getProperties().getRecordSearchDispositionActionName().equalsIgnoreCase(RM_ACTIONS.CUT_OFF.getAction()),
                "Disposition action should be cut off");
        assertTrue(elRecord.getProperties().getRecordSearchDispositionPeriod().equalsIgnoreCase("none"),
                "Disposition period property should none");
        assertTrue(elRecord.getProperties().getRecordSearchDispositionPeriodExpression().equalsIgnoreCase("0"),
                "Disposition period expression should be 0");
        assertTrue(elRecord.getProperties().getRecordSearchDispositionEvents().contains(RMEvents.CASE_CLOSED.getEventName()),
                "Disposition event list doesn't contain case closed event");
        assertTrue(nonElRecord.getProperties().getRecordSearchDispositionActionName().equalsIgnoreCase(RM_ACTIONS.CUT_OFF.getAction()),
                "Disposition action should be cut off");
        assertTrue(nonElRecord.getProperties().getRecordSearchDispositionPeriod().equalsIgnoreCase("none"),
                "Disposition period property should be none");
        assertTrue(nonElRecord.getProperties().getRecordSearchDispositionPeriodExpression().equalsIgnoreCase("0"),
                "Disposition period expression should be 0");
        assertTrue(nonElRecord.getProperties().getRecordSearchDispositionEvents().contains(RMEvents.CASE_CLOSED.getEventName()),
                "Disposition event list doesn't contain case closed event");
    }

    /**
     * Given following structure is created:
     * rootCategory1 with RS applied on record level with cut off on event case closed and destroy after 1 day
     * - subCategory2 without RS
     * - recFolder
     * - incomplete electronic record
     * - complete non-electronic record
     * rootCategory2 with cut off on event Obsolete and destroy both after 2 day
     * When moving subcategory2 within rootCategory2
     * Then the records will inherit the RS from rootCategory2
     */
    @Test
    @AlfrescoTest (jira = "APPS-1004")
    public void testInheritWhenMoveToSameStepDifferentEvent() throws Exception
    {
        STEP("Add retention schedule cut off on case closed.");
        dispositionScheduleService.addCutOffAfterEventStep(rootCategory.getName(), RMEvents.CASE_CLOSED.getEventName());

        STEP("Add retention schedule destroy step after 1 Day period.");
        dispositionScheduleService.addDestroyWithGhostingAfterPeriodStep(rootCategory.getName(), "day|1", CUT_OFF_DATE);

        STEP("Create a subcategory with a record folder and records.");
        RecordCategoryChild subCategory = createSubCategoryWithRecords();

        STEP("Add retention schedule cut off step on event separation.");
        dispositionScheduleService.addCutOffAfterEventStep(rootCategory2.getName(), RMEvents.OBSOLETE.getEventName());

        STEP("Add retention schedule destroy step after 2 Day period.");
        dispositionScheduleService.addDestroyWithGhostingAfterPeriodStep(rootCategory2.getName(), "day|2", DATE_FILED);

        STEP("Move the subcategory within the rootCategory2.");
        getRestAPIFactory().getNodeAPI(toContentModel(subCategory.getId())).move(createBodyForMoveCopy(rootCategory2.getId()));
        assertStatusCode(OK);

        STEP("Check that both records inherit rootCategory2 retention schedule");
        elRecord = getRestAPIFactory().getRecordsAPI().getRecord(elRecord.getId());
        nonElRecord = getRestAPIFactory().getRecordsAPI().getRecord(nonElRecord.getId());
        assertTrue(elRecord.getProperties().getRecordSearchDispositionActionName().equalsIgnoreCase(RM_ACTIONS.CUT_OFF.getAction()),
                "Disposition action should be cut off");
        assertTrue(elRecord.getProperties().getRecordSearchDispositionPeriod().equalsIgnoreCase("none"),
                "Disposition period property should be none");
        assertTrue(elRecord.getProperties().getRecordSearchDispositionPeriodExpression().equalsIgnoreCase("0"),
                "Disposition period expression should be 0");
        assertFalse(elRecord.getProperties().getRecordSearchDispositionEvents().contains(RMEvents.CASE_CLOSED.getEventName()),
                "Event list contain the event from the previous RS ");
        assertTrue(elRecord.getProperties().getRecordSearchDispositionEvents().contains(RMEvents.OBSOLETE.getEventName()),
                "Event list doesn't contain the event from the current RS ");
        assertTrue(nonElRecord.getProperties().getRecordSearchDispositionActionName().equalsIgnoreCase(RM_ACTIONS.CUT_OFF.getAction()),
                "Disposition action should be cut off");
        assertTrue(nonElRecord.getProperties().getRecordSearchDispositionPeriod().equalsIgnoreCase("none"),
                "Disposition period property should be none");
        assertTrue(nonElRecord.getProperties().getRecordSearchDispositionPeriodExpression().equalsIgnoreCase("0"),
                "Disposition period expression should be 0");
        assertFalse(nonElRecord.getProperties().getRecordSearchDispositionEvents().contains(RMEvents.CASE_CLOSED.getEventName()),
                "Event list contain the event from the previous RS ");
        assertTrue(nonElRecord.getProperties().getRecordSearchDispositionEvents().contains(RMEvents.OBSOLETE.getEventName()),
                "Event list doesn't contain the event from the current RS ");
    }

    @AfterMethod (alwaysRun = true)
    public void cleanupMoveRecCategoriesWithRSTests()
    {
        getRestAPIFactory().getRecordCategoryAPI().deleteRecordCategory(rootCategory.getId());
        getRestAPIFactory().getRecordCategoryAPI().deleteRecordCategory(rootCategory2.getId());
    }

    /**
     * Helper method to create a sub-category with a folder, an incomplete electronic record and a complete
     * electronic record
     * @return
     */
    private RecordCategoryChild createSubCategoryWithRecords()
    {
        STEP("Create a subcategory with a record folder");
        RecordCategoryChild subCategory = createRecordCategory(rootCategory.getId(), getRandomName("subCategory"));
        RecordCategoryChild recFolder = createFolder(subCategory.getId(), getRandomName("recFolder"));

        STEP("Create 2 records in the record folder. Complete one of them.");
        elRecord = createElectronicRecord(recFolder.getId(), getRandomName("elRecord"));
        nonElRecord = createNonElectronicRecord(recFolder.getId(), getRandomName("nonElRecord"));
        getRestAPIFactory().getRecordsAPI().completeRecord(nonElRecord.getId());
        return subCategory;
    }

}
