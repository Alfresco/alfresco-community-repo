/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
    @Autowired
    private DispositionScheduleService dispositionScheduleService;

    @AlfrescoTest (jira = "MNT-19967")
    @Test (
            description = "Folders and records under a child record category should inherit the retention schedule of the parent record category."
    )
    public void testDispositionScheduleInheritance() throws Exception
    {
        STEP("Create record category with retention schedule and apply it to records.");
        RecordCategory rootCategory = createRootCategory(getRandomName("rootCategory"));
        dispositionScheduleService.createCategoryRetentionSchedule(rootCategory.getName(), true);

        STEP("Add retention schedule cut off step with immediate period.");
        dispositionScheduleService.addCutOffAfterPeriodStep(rootCategory.getName(), "immediately");

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
}
