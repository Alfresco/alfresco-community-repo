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

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAspects.CUT_OFF_ASPECT;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;

import org.alfresco.rest.rm.community.requests.gscore.api.RecordsAPI;
import org.alfresco.rest.v0.service.DispositionScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public class AutomaticDispositionTest extends BaseRMRestTest
{
    @Autowired
    private DispositionScheduleService dispositionScheduleService;

    private RecordCategory categoryWithRSOnRecords;

    /**
     * Given there is a complete record eligible for cut off
     * When the correct duration of time passes
     * Then the record will be automatically cut off
     */
    @Test(enabled = false)
    public void testAutomaticCutOff() throws Exception
    {
        STEP("Create record category with retention schedule and apply it to records.");
        categoryWithRSOnRecords = createRootCategory(getRandomName("categoryWithRSOnRecords"));
        dispositionScheduleService.createCategoryRetentionSchedule(categoryWithRSOnRecords.getName(), true);

        STEP("Add retention schedule cut off step with immediate period.");
        dispositionScheduleService.addCutOffImmediatelyStep(categoryWithRSOnRecords.getName());

        STEP("Create a record folder with a record");
        RecordCategoryChild recordFolder = createRecordFolder(categoryWithRSOnRecords.getId(), getRandomName
                ("recordFolder"));
        Record record = createElectronicRecord(recordFolder.getId(), getRandomName("elRecord"));

        STEP("Complete the record and wait upon to 5 minutes for automatic job to execute");
        completeRecord(record.getId());

        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();
        List<String> aspects = recordsAPI.getRecord(record.getId()).getAspectNames();
        int count = 0;
        while (!aspects.contains(CUT_OFF_ASPECT) && count < 30)
        {
            // sleep .. allowing the job time to execute
            Thread.sleep(10000);
            count++;
            aspects = recordsAPI.getRecord(record.getId()).getAspectNames();
        }
        assertTrue(aspects.contains(CUT_OFF_ASPECT), "Record should now be cut off");
    }

    @AfterClass (alwaysRun = true)
    public void deleteCategory()
    {
        deleteRecordCategory(categoryWithRSOnRecords.getId());
    }
}
