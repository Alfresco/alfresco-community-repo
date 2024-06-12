/*-
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.rest.rm.community.retentionschedule;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;

import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.retentionschedule.RetentionSchedule;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.springframework.http.HttpStatus.*;

/**
 * This class contains the tests for the Retention Schedule CRUD V1 API
 *
 * @author Manish Kumar
 */
public class RetentionScheduleTests extends BaseRMRestTest
{
    private RecordCategory recordCategory;

    @BeforeClass(alwaysRun = true)
    public void preconditionForRetentionScheduleTests()
    {
        recordCategory = createRootCategory(getRandomName("recordCategory"));
    }
    /**
     * <pre>
     * Given that a record category exists
     * When I ask the API to create a retention schedule
     * Then it is created
     * </pre>
     */
    @Test
    public void createRetentionSchedule()
    {
        String authority = "authority" + getRandomAlphanumeric();
        String instructions = "instructions" + getRandomAlphanumeric();
        Boolean isRecordLevel = false;
        // Create the retentionSchedule
        RetentionSchedule retentionSchedule = new RetentionSchedule();
        retentionSchedule.setAuthority(authority);
        retentionSchedule.setInstructions(instructions);
        retentionSchedule.setIsRecordLevel(isRecordLevel);
        RetentionSchedule createdRetentionSchedule = getRestAPIFactory().getRetentionScheduleAPI()
            .createRetentionSchedule(retentionSchedule, recordCategory.getId());

        // Verify the status code
        assertStatusCode(CREATED);

        AssertJUnit.assertEquals(createdRetentionSchedule.getAuthority(), authority);
        AssertJUnit.assertEquals(createdRetentionSchedule.getInstructions(), instructions);
        AssertJUnit.assertEquals(createdRetentionSchedule.getIsRecordLevel(), isRecordLevel.booleanValue());
        Assert.assertNotNull(createdRetentionSchedule.getId());
    }

    @Test
    public void getRetentionSchedule()
    {
        RetentionSchedule receiveRetentionSchedule = getRestAPIFactory().getRetentionScheduleAPI().getRetentionSchedule(recordCategory.getId());
        Assert.assertNotNull(receiveRetentionSchedule.getId());
    }

    @AfterClass(alwaysRun = true)
    public void cleanUpRetentionScheduleTests()
    {
        getRestAPIFactory().getRecordCategoryAPI(getAdminUser()).deleteRecordCategory(recordCategory.getId());
    }
}
