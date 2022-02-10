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
package org.alfresco.rest.rm.community.recordcategories;

import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.v0.service.DispositionScheduleService;
import org.alfresco.test.AlfrescoTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DispositionScheduleInheritanceTests extends BaseRMRestTest
{
    private static final String RETAIN_STEP = "retain";
    private static final String CUTOFF_STEP = "cutoff";

    @Autowired
    private DispositionScheduleService dispositionScheduleService;

    /**
     * Given following structure is created:
     * rootCategory with RS applied on records level
     *      - subCategory without RS
     *              - recFolder
     *                      - incomplete electronic record
     *                      - complete non-electronic record
     * Then both records should inherit the RS from rootCategory
     */
    @AlfrescoTest (jira = "MNT-19967")
    @Test
    public void testRSInheritanceOnRecordsWhenOnlyACategoryHasRS()
    {
        STEP("Create record category with retention schedule and apply it to records.");
        RecordCategory rootCategory = createRootCategory(getRandomName("rootCategory"));
        dispositionScheduleService.createCategoryRetentionSchedule(rootCategory.getName(), true);

        STEP("Add retention schedule cut off step with immediate period.");
        dispositionScheduleService.addCutOffImmediatelyStep(rootCategory.getName());

        STEP("Add retention schedule retain step with immediate period.");
        dispositionScheduleService.addRetainAfterPeriodStep(rootCategory.getName(), "immediately");

        STEP("Create a subcategory with a record folder");
        RecordCategoryChild subCategory = createRecordCategory(rootCategory.getId(), getRandomName("subCategory"));
        RecordCategoryChild recFolder = createFolder(subCategory.getId(), getRandomName("recFolder"));

        STEP("Create 2 records in the record folder. Complete one of them.");
        Record elRecord = createElectronicRecord(recFolder.getId(), getRandomName("elRecord"));
        Record nonElRecord = createNonElectronicRecord(recFolder.getId(), getRandomName("nonElRecord"));
        getRestAPIFactory().getRecordsAPI().completeRecord(nonElRecord.getId());

        STEP("Check that both records inherit root category retention schedule");
        Assert.assertTrue(elRecord.getProperties().getRecordSearchHasDispositionSchedule(),
                "rma:recordSearchHasDispositionSchedule property should be true");
        Assert.assertTrue(nonElRecord.getProperties().getRecordSearchHasDispositionSchedule(),
                "rma:recordSearchHasDispositionSchedule property should be true");
    }

    /**
     * Given following structure is created:
     * rootCategory with RS applied on records folder level
     *      - subCategory without RS
     *              - recFolder
     * Then recFolder should inherit the RS from rootCategory
     */
    @Test
    public void testRSInheritanceOnRecordFoldersWhenOnlyACategoryHasRS()
    {
        STEP("Create record category with retention schedule and apply it to record folders.");
        RecordCategory rootCategory = createRootCategory(getRandomName("rootCategory"));
        dispositionScheduleService.createCategoryRetentionSchedule(rootCategory.getName(), false);

        STEP("Add retention schedule cut off step with immediate period.");
        dispositionScheduleService.addCutOffImmediatelyStep(rootCategory.getName());

        STEP("Add retention schedule retain step with immediate period.");
        dispositionScheduleService.addRetainAfterPeriodStep(rootCategory.getName(), "immediately");

        STEP("Create a subcategory with a record folder");
        RecordCategoryChild subCategory = createRecordCategory(rootCategory.getId(), getRandomName("subCategory"));
        RecordCategoryChild recFolder = createFolder(subCategory.getId(), getRandomName("recFolder"));

        STEP("Check that recFolder inherits root category retention schedule");
        Assert.assertTrue(recFolder.getProperties().getRecordSearchHasDispositionSchedule(),
                "rma:recordSearchHasDispositionSchedule property should be true");
    }

    /**
     * Given following structure is created:
     * rootCategory with RS applied on records level
     *      - subCategory1 with another RS applied on records level
     *              - subCategory2 without RS
     *                      - recFolder
     *                              - incomplete electronic record
     *                              - complete non-electronic record
     * Then both records should inherit the RS from subCategory1
     */
    @Test
    public void testRSInheritanceOnRecordsWhen2CategoriesHaveRS()
    {
        STEP("Create record category with retention schedule and apply it to records.");
        RecordCategory rootCategory = createRootCategory(getRandomName("rootCategory"));
        dispositionScheduleService.createCategoryRetentionSchedule(rootCategory.getName(), true);

        STEP("Add retention schedule cut off step with immediate period.");
        dispositionScheduleService.addCutOffImmediatelyStep(rootCategory.getName());

        STEP("Create a subcategory with retention schedule and apply it to records.");
        RecordCategoryChild subCategory1 = createRecordCategory(rootCategory.getId(), getRandomName("subCategory"));
        String subcategory1Path = rootCategory.getName() + "/" + subCategory1.getName();
        dispositionScheduleService.createCategoryRetentionSchedule(subcategory1Path, true);

        STEP("Add retention schedule retain step with 1 day after created date.");
        dispositionScheduleService.addRetainAfterPeriodStep(subcategory1Path, "day|1");

        STEP("Create a subcategory2 in subcategory1");
        RecordCategoryChild subCategory2 = createRecordCategory(subCategory1.getId(), getRandomName("subCategory"));

        STEP("Create a record folder with 2 records. Complete one of them.");
        RecordCategoryChild recFolder = createFolder(subCategory2.getId(), getRandomName("recFolder"));
        Record elRecord = createElectronicRecord(recFolder.getId(), getRandomName("elRecord"));
        Record nonElRecord = createNonElectronicRecord(recFolder.getId(), getRandomName("nonElRecord"));
        getRestAPIFactory().getRecordsAPI().completeRecord(nonElRecord.getId());

        STEP("Check that both records inherit subCategory1 retention schedule");
        Assert.assertTrue(elRecord.getProperties().getRecordSearchHasDispositionSchedule(),
                "rma:recordSearchHasDispositionSchedule property should be true for incomplete record");
        Assert.assertTrue(nonElRecord.getProperties().getRecordSearchHasDispositionSchedule(),
                "rma:recordSearchHasDispositionSchedule property should be true for complete record");
        Assert.assertEquals(elRecord.getProperties().getRecordSearchDispositionActionName(),
                RETAIN_STEP,
                "Disposition action should be retain and not cutoff for incomplete record");
        Assert.assertEquals(nonElRecord.getProperties().getRecordSearchDispositionActionName(),
                RETAIN_STEP,
                "Disposition action should be retain and not cutoff for complete record");
    }

    /**
     * Given following structure is created:
     * rootCategory with RS applied on records folder level
     *      - subCategory1 with another RS applied on records folder level
     *          - subCategory2 without RS
     *              - recFolder
     * Then recFolder should inherit the RS from subCategory1
     */
    @Test
    public void testRSInheritanceOnRecordFoldersWhen2CategoriesHaveRS()
    {
        STEP("Create record category with retention schedule and apply it to record folders.");
        RecordCategory rootCategory = createRootCategory(getRandomName("rootCategory"));
        dispositionScheduleService.createCategoryRetentionSchedule(rootCategory.getName(), false);

        STEP("Add retention schedule retain step with 2 days after created date.");
        dispositionScheduleService.addRetainAfterPeriodStep(rootCategory.getName(), "day|2");

        STEP("Create a subcategory with retention schedule and apply it to record folders.");
        RecordCategoryChild subCategory1 = createRecordCategory(rootCategory.getId(), getRandomName("subCategory"));
        String subcategory1Path = rootCategory.getName() + "/" + subCategory1.getName();
        dispositionScheduleService.createCategoryRetentionSchedule(subcategory1Path, false);

        STEP("Add retention schedule cut off step with immediate period.");
        dispositionScheduleService.addCutOffImmediatelyStep(subcategory1Path);

        STEP("Create a subcategory2 with a record folder in subcategory1");
        RecordCategoryChild subCategory2 = createRecordCategory(subCategory1.getId(), getRandomName("subCategory"));
        RecordCategoryChild recFolder = createFolder(subCategory2.getId(), getRandomName("recFolder"));

        STEP("Check that recFolder inherits subCategory1 retention schedule");
        Assert.assertTrue(recFolder.getProperties().getRecordSearchHasDispositionSchedule(),
                "rma:recordSearchHasDispositionSchedule property should be true");
        Assert.assertEquals(recFolder.getProperties().getRecordSearchDispositionActionName(),
                CUTOFF_STEP,
                "Disposition action should be cutoff and not retain for the record folder");
    }

    /**
     * Given following structure is created:
     * rootCategory with RS applied on folder records level
     *      - subCategory with another RS applied on records level
     *              - recFolder
     *                      - incomplete electronic record
     *                      - complete non-electronic record
     * Then both records should inherit the RS from subCategory
     */
    @Test
    public void testMixedRSInheritanceWhenFirstParentHasRSOnRecords()
    {
        STEP("Create record category with retention schedule and apply it to folder records.");
        RecordCategory rootCategory = createRootCategory(getRandomName("rootCategory"));
        dispositionScheduleService.createCategoryRetentionSchedule(rootCategory.getName(), false);

        STEP("Add retention schedule cut off step with immediate period.");
        dispositionScheduleService.addCutOffImmediatelyStep(rootCategory.getName());

        STEP("Create a subcategory with retention schedule and apply it to records.");
        RecordCategoryChild subCategory = createRecordCategory(rootCategory.getId(), getRandomName("subCategory"));
        String subcategoryPath = rootCategory.getName() + "/" + subCategory.getName();
        dispositionScheduleService.createCategoryRetentionSchedule(subcategoryPath, true);

        STEP("Add retention schedule retain step with 1 day after created date.");
        dispositionScheduleService.addRetainAfterPeriodStep(subcategoryPath, "day|1");

        STEP("Create a record folder with 2 records. Complete one of them.");
        RecordCategoryChild recFolder = createFolder(subCategory.getId(), getRandomName("recFolder"));
        Record elRecord = createElectronicRecord(recFolder.getId(), getRandomName("elRecord"));
        Record nonElRecord = createNonElectronicRecord(recFolder.getId(), getRandomName("nonElRecord"));
        getRestAPIFactory().getRecordsAPI().completeRecord(nonElRecord.getId());

        STEP("Check that both records inherit subCategory retention schedule");
        Assert.assertTrue(elRecord.getProperties().getRecordSearchHasDispositionSchedule(),
                "rma:recordSearchHasDispositionSchedule property should be true for incomplete record");
        Assert.assertTrue(nonElRecord.getProperties().getRecordSearchHasDispositionSchedule(),
                "rma:recordSearchHasDispositionSchedule property should be true for complete record");
        Assert.assertEquals(elRecord.getProperties().getRecordSearchDispositionActionName(),
                RETAIN_STEP,
                "Disposition action should be retain and not cutoff for incomplete record");
        Assert.assertEquals(nonElRecord.getProperties().getRecordSearchDispositionActionName(),
                RETAIN_STEP,
                "Disposition action should be retain and not cutoff for complete record");
    }

    /**
     * Given following structure is created:
     * rootCategory with RS applied on records level
     *      - subCategory with another RS applied on folder records level
     *              - recFolder
     *                      - incomplete electronic record
     *                      - complete non-electronic record
     * Then both records should not have RS (rma:recordSearchHasDispositionSchedule property is set to false)
     * and record folder inherits the RS from subCategory
     */
    @Test
    public void testMixedRSInheritanceWhenFirstParentHasRSOnFolders()
    {
        STEP("Create record category with retention schedule and apply it to records.");
        RecordCategory rootCategory = createRootCategory(getRandomName("rootCategory"));
        dispositionScheduleService.createCategoryRetentionSchedule(rootCategory.getName(), true);

        STEP("Add retention schedule cut off step with immediate period.");
        dispositionScheduleService.addCutOffImmediatelyStep(rootCategory.getName());

        STEP("Create a subcategory with retention schedule and apply it to record folders.");
        RecordCategoryChild subCategory = createRecordCategory(rootCategory.getId(), getRandomName("subCategory"));
        String subcategoryPath = rootCategory.getName() + "/" + subCategory.getName();
        dispositionScheduleService.createCategoryRetentionSchedule(subcategoryPath, false);

        STEP("Add retention schedule retain step with 1 day after created date.");
        dispositionScheduleService.addRetainAfterPeriodStep(subcategoryPath, "day|1");

        STEP("Create a record folder with 2 records. Complete one of them.");
        RecordCategoryChild recFolder = createFolder(subCategory.getId(), getRandomName("recFolder"));
        Record elRecord = createElectronicRecord(recFolder.getId(), getRandomName("elRecord"));
        Record nonElRecord = createNonElectronicRecord(recFolder.getId(), getRandomName("nonElRecord"));
        getRestAPIFactory().getRecordsAPI().completeRecord(nonElRecord.getId());

        STEP("Check that the records don't have retention schedule");
        Assert.assertFalse(elRecord.getProperties().getRecordSearchHasDispositionSchedule(),
                "rma:recordSearchHasDispositionSchedule property should be false for incomplete record");
        Assert.assertFalse(nonElRecord.getProperties().getRecordSearchHasDispositionSchedule(),
                "rma:recordSearchHasDispositionSchedule property should be false for complete record");

        STEP("Check that recFolder inherits subCategory retention schedule");
        Assert.assertTrue(recFolder.getProperties().getRecordSearchHasDispositionSchedule(),
                "rma:recordSearchHasDispositionSchedule property should be true");
        Assert.assertEquals(recFolder.getProperties().getRecordSearchDispositionActionName(),
                RETAIN_STEP,
                "Disposition action should be retain and not cutoff for the record folder");
    }
}
