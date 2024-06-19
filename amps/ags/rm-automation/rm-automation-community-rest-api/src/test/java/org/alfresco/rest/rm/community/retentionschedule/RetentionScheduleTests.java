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
import org.alfresco.rest.rm.community.model.retentionschedule.RetentionScheduleCollection;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import static org.alfresco.rest.core.v0.BaseAPI.RM_SITE_ID;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.springframework.http.HttpStatus.*;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;

/**
 * This class contains the tests for the Retention Schedule CRUD V1 API
 *
 * @author Manish Kumar
 */
public class RetentionScheduleTests extends BaseRMRestTest
{
    private RecordCategory recordCategory;
    private RetentionSchedule createdRetentionSchedule;
    private UserModel nonRMuser;
    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;

    @BeforeClass(alwaysRun = true)
    public void preconditionForRetentionScheduleTests()
    {
        createRMSiteIfNotExists();
        // create a non rm user
        nonRMuser = dataUser.createRandomTestUser("testUser");
        //Create record category
        recordCategory = createRootCategory(getRandomName("recordCategory1"));
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
        RetentionSchedule retentionSchedule = new RetentionSchedule();
        retentionSchedule.setAuthority(authority);
        retentionSchedule.setInstructions(instructions);
        retentionSchedule.setIsRecordLevel(isRecordLevel);

        // Create retention schedule with user having no rights
        getRestAPIFactory().getRetentionScheduleAPI(nonRMuser).createRetentionSchedule(retentionSchedule,recordCategory.getId());

        // Verify the status code
        assertStatusCode(FORBIDDEN);

        //Create retention schedule with category id not exist
        getRestAPIFactory().getRetentionScheduleAPI().createRetentionSchedule(retentionSchedule,getRandomAlphanumeric());

        // Verify the status code
        assertStatusCode(NOT_FOUND);

        //Create retention schedule with a user with unauthorized access
        createdRetentionSchedule = getRestAPIFactory().getRetentionScheduleAPI(new UserModel(getAdminUser().getUsername(),"wrongPassword")).createRetentionSchedule(retentionSchedule, recordCategory.getId());

        // Verify the status code
        assertStatusCode(UNAUTHORIZED);

        //Create retention schedule with a valid user
        createdRetentionSchedule = getRestAPIFactory().getRetentionScheduleAPI()
            .createRetentionSchedule(retentionSchedule, recordCategory.getId());

        // Verify the status code
        assertStatusCode(CREATED);
        AssertJUnit.assertEquals(createdRetentionSchedule.getAuthority(), authority);
        AssertJUnit.assertEquals(createdRetentionSchedule.getInstructions(), instructions);
        AssertJUnit.assertEquals(createdRetentionSchedule.getIsRecordLevel(), isRecordLevel.booleanValue());
        Assert.assertNotNull(createdRetentionSchedule.getId());

        //Create retention schedule on a category with already having retention schedule
        getRestAPIFactory().getRetentionScheduleAPI()
            .createRetentionSchedule(retentionSchedule, recordCategory.getId());

        assertStatusCode(CONFLICT);

    }

    @Test
    public void getRetentionSchedule()
    {
        //Get retention schedule with user having no rights
        getRestAPIFactory().getRetentionScheduleAPI(nonRMuser).getRetentionSchedule(recordCategory.getId());

        // Verify the status code
        assertStatusCode(FORBIDDEN);

        //Get retention schedule with category id that does not exist
        getRestAPIFactory().getRetentionScheduleAPI().getRetentionSchedule(getRandomAlphanumeric());

        // Verify the status code
        assertStatusCode(NOT_FOUND);

        //Create retention schedule with a user with unauthorized access
        getRestAPIFactory().getRetentionScheduleAPI(new UserModel(getAdminUser().getUsername(),"wrongPassword")).getRetentionSchedule(recordCategory.getId());

        // Verify the status code
        assertStatusCode(UNAUTHORIZED);

        RetentionScheduleCollection receiveRetentionScheduleCollection = getRestAPIFactory().getRetentionScheduleAPI().getRetentionSchedule(recordCategory.getId());
        // Verify the status code
        assertStatusCode(OK);

        receiveRetentionScheduleCollection.getEntries().forEach(c ->
            {
                RetentionSchedule retentionSchedule = c.getEntry();
                String retentionScheduleId = retentionSchedule.getId();
                assertNotNull(retentionScheduleId);
                logger.info("Checking retention schedule " + retentionScheduleId);

                // Find this retention schedule is created one or not
                assertEquals(createdRetentionSchedule.getId(),retentionScheduleId);
                assertEquals(createdRetentionSchedule.getParentId(),retentionSchedule.getParentId());
                assertEquals(createdRetentionSchedule.getAuthority(), retentionSchedule.getAuthority());
                assertEquals(createdRetentionSchedule.getInstructions(), retentionSchedule.getInstructions());
                assertEquals(createdRetentionSchedule.getIsRecordLevel(), retentionSchedule.getIsRecordLevel());
                assertEquals(createdRetentionSchedule.getIsUnpublishedUpdates(), retentionSchedule.getIsUnpublishedUpdates());
        });
    }

    @AfterClass(alwaysRun = true)
    public void cleanUpRetentionScheduleTests()
    {
        rmRolesAndActionsAPI.deleteAllItemsInContainer(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), RM_SITE_ID, recordCategory.getName());
        deleteRecordCategory(recordCategory.getId());
        dataUser.deleteUser(nonRMuser);
    }
}
