/*-
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
package org.alfresco.rest.rm.community.retentionschedule;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.retentionschedule.RetentionSchedule;
import org.alfresco.rest.rm.community.model.retentionschedule.RetentionScheduleCollection;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.alfresco.rest.core.v0.BaseAPI.RM_SITE_ID;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;

/**
 * This class contains the tests for the Retention Schedule CRUD V1 API
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
        recordCategory = createRootCategory(getRandomName("recordCategory"));


    }
    /**
     * <pre>
     * Given that a record category exists
     * When I ask the API to create a retention schedule with a user having no rights
     * Then it will give 403 as status code
     * </pre>
     */
    @Test(priority = 1)
    public void createRetentionScheduleFor403()
    {
        RetentionSchedule retentionSchedule = new RetentionSchedule();

        // Create retention schedule with user having no rights
        getRestAPIFactory().getRetentionScheduleAPI(nonRMuser).createRetentionSchedule(retentionSchedule, recordCategory.getId());

        // Verify the status code
        assertStatusCode(FORBIDDEN);
    }

    /**
     * <pre>
     * Given that a record category does not exists
     * When I ask the API to create a retention schedule on a category Id
     * Then it will give 404 as a status code
     * </pre>
     */
    @Test(priority = 2)
    public void createRetentionScheduleFor404()
    {
        RetentionSchedule retentionSchedule = new RetentionSchedule();

        //Create retention schedule with category id not exist
        getRestAPIFactory().getRetentionScheduleAPI().createRetentionSchedule(retentionSchedule, getRandomAlphanumeric());

        // Verify the status code
        assertStatusCode(NOT_FOUND);
    }

    /**
     * <pre>
     * Given that a record category exists
     * When I ask the API to create a retention schedule on a category id with a user having unauthorized access
     * Then it will give 401 as a status code
     * </pre>
     */
    @Test(priority = 3)
    public void createRetentionScheduleFor401()
    {
        RetentionSchedule retentionSchedule = new RetentionSchedule();

        //Create retention schedule with a user with unauthorized access
        createdRetentionSchedule = getRestAPIFactory().getRetentionScheduleAPI(new UserModel(getAdminUser().getUsername(), "wrongPassword")).createRetentionSchedule(retentionSchedule, recordCategory.getId());

        // Verify the status code
        assertStatusCode(UNAUTHORIZED);
    }

    /**
     * <pre>
     * Given that a record category exists
     * When I ask the API to create a retention schedule with a user having access
     * Then it is created with a 201 status code
     * </pre>
     */
    @Test(priority = 4)
    public void createRetentionScheduleFor201()
    {
        RetentionSchedule retentionSchedule = new RetentionSchedule();
        String authority = "authority" + getRandomAlphanumeric();
        String instructions = "instructions" + getRandomAlphanumeric();
        boolean isRecordLevel = false;
        retentionSchedule.setAuthority(authority);
        retentionSchedule.setInstructions(instructions);
        retentionSchedule.setIsRecordLevel(isRecordLevel);

        //Create retention schedule with a valid user
        createdRetentionSchedule = getRestAPIFactory().getRetentionScheduleAPI()
            .createRetentionSchedule(retentionSchedule, recordCategory.getId());

        // Verify the status code
        assertStatusCode(CREATED);
        assertEquals(createdRetentionSchedule.getAuthority(), authority);
        assertEquals(createdRetentionSchedule.getInstructions(), instructions);
        assertFalse(createdRetentionSchedule.getIsRecordLevel());
        assertNotNull(createdRetentionSchedule.getId());
    }

    /**
     * <pre>
     * Given that a record category exists
     * When I ask the API to create a retention schedule on a category id having retention schedule already
     * Then it will give 409 as a status code
     * </pre>
     */
    @Test(priority = 5)
    public void createRetentionScheduleFor409()
    {
        RetentionSchedule retentionSchedule = new RetentionSchedule();
        //Create retention schedule on a category with already having retention schedule
        getRestAPIFactory().getRetentionScheduleAPI()
            .createRetentionSchedule(retentionSchedule, recordCategory.getId());

        assertStatusCode(CONFLICT);
    }

    /**
     * <pre>
     * Given that a record category exists
     * When I ask the API to get a retention schedule on a given categoryId with a user having no rights
     * Then it will give 403
     * </pre>
     */
    @Test(priority = 6)
    public void retentionScheduleWith403()
    {
        //Get retention schedule with user having no rights
        getRestAPIFactory().getRetentionScheduleAPI(nonRMuser).getRetentionSchedule(recordCategory.getId());

        // Verify the status code
        assertStatusCode(FORBIDDEN);
    }

    /**
     * <pre>
     * Given that a record category does not exists
     * When I ask the API to get a retention schedule on a category Id
     * Then it will give 404 as a status code
     * </pre>
     */
    @Test(priority = 7)
    public void retentionScheduleWith404()
    {

        //Get retention schedule with category id that does not exist
        getRestAPIFactory().getRetentionScheduleAPI().getRetentionSchedule(getRandomAlphanumeric());

        // Verify the status code
        assertStatusCode(NOT_FOUND);
    }

    /**
     * <pre>
     * Given that a record category exists
     * When I ask the API to get a retention schedule on a categoryId with a user having unauthorized access
     * Then it will give 401 as a status code
     * </pre>
     */
    @Test(priority = 8)
    public void retentionScheduleWith401()
    {
        //Create retention schedule with a user with unauthorized access
        getRestAPIFactory().getRetentionScheduleAPI(new UserModel(getAdminUser().getUsername(), "wrongPassword")).getRetentionSchedule(recordCategory.getId());

        // Verify the status code
        assertStatusCode(UNAUTHORIZED);
    }

    /**
     * <pre>
     * Given that a record category exists
     * When I ask the API to get a retention schedule on a categoryId with a user having access
     * Then it will give retentionSchedule with 200 as a status code
     * </pre>
     */
    @Test(priority = 9)
    public void retentionScheduleWith200()
    {
        RetentionScheduleCollection retentionScheduleCollection = getRestAPIFactory().getRetentionScheduleAPI().getRetentionSchedule(recordCategory.getId());
        // Verify the status code
        assertStatusCode(OK);
        retentionScheduleCollection.getEntries().forEach(c ->
        {
            RetentionSchedule retentionSchedule = c.getEntry();
            String retentionScheduleId = retentionSchedule.getId();
            assertNotNull(retentionScheduleId);
            logger.info("Checking retention schedule " + retentionScheduleId);

            // Find this retention schedule is created one or not
            assertEquals(createdRetentionSchedule.getId(), retentionScheduleId);
            assertEquals(createdRetentionSchedule.getParentId(),retentionSchedule.getParentId());
            assertEquals(createdRetentionSchedule.getAuthority(), retentionSchedule.getAuthority());
            assertEquals(createdRetentionSchedule.getInstructions(), retentionSchedule.getInstructions());
            assertEquals(createdRetentionSchedule.getIsRecordLevel(), retentionSchedule.getIsRecordLevel());
            assertEquals(createdRetentionSchedule.isUnpublishedUpdates(), retentionSchedule.isUnpublishedUpdates());
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
