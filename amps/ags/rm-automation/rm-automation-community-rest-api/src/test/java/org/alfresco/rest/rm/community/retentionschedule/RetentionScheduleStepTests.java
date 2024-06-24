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
import org.alfresco.rest.rm.community.model.retentionschedule.RetentionScheduleActionDefinition;
import org.alfresco.rest.rm.community.model.retentionschedule.RetentionScheduleCollection;
import org.alfresco.rest.rm.community.model.retentionschedule.RetentionScheduleStepCollection;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.alfresco.rest.core.v0.BaseAPI.RM_SITE_ID;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;

public class RetentionScheduleStepTests extends BaseRMRestTest
{
    private RecordCategory recordCategory;
    private RetentionSchedule createdRetentionSchedule;

    private RetentionScheduleActionDefinition retentionScheduleActionDefinition = new RetentionScheduleActionDefinition();

    private RetentionScheduleActionDefinition createdRetentionScheduleActionDefinition;
    private UserModel nonRMuser;
    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;

    @BeforeClass(alwaysRun = true)
    public void preconditionForRetentionScheduleStepTests()
    {
        createRMSiteIfNotExists();
        // create a non rm user
        nonRMuser = dataUser.createRandomTestUser("testUser");
        //Create record category
        recordCategory = createRootCategory(getRandomName("recordCategory"));
    }
    @Test(priority = 1)
    public void createRetentionScheduleStepFor201()
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

        retentionScheduleActionDefinition.setName("retain");
        retentionScheduleActionDefinition.setDescription("randomDescription");
        retentionScheduleActionDefinition.setPeriodAmount(2);
        retentionScheduleActionDefinition.setPeriod("month");
        retentionScheduleActionDefinition.setCombineDispositionStepConditions(false);
        retentionScheduleActionDefinition.setEligibleOnFirstCompleteEvent(true);
        retentionScheduleActionDefinition.setEvents(Arrays.asList("case_closed","abolished"));

        //Create retention schedule action definition
        createdRetentionScheduleActionDefinition = getRestAPIFactory().getRetentionScheduleAPI().createRetentionScheduleStep(retentionScheduleActionDefinition,createdRetentionSchedule.getId());

        // Verify the status code
        assertStatusCode(CREATED);

        // Find this retention schedule is created one or not
        assertEquals(createdRetentionScheduleActionDefinition.getName(),retentionScheduleActionDefinition.getName());
        assertEquals(createdRetentionScheduleActionDefinition.getDescription(),retentionScheduleActionDefinition.getDescription());
        assertEquals(createdRetentionScheduleActionDefinition.getPeriod(),retentionScheduleActionDefinition.getPeriod());
        assertEquals(createdRetentionScheduleActionDefinition.getPeriodAmount(),retentionScheduleActionDefinition.getPeriodAmount());
        assertEquals(createdRetentionScheduleActionDefinition.getCombineDispositionStepConditions(),retentionScheduleActionDefinition.getCombineDispositionStepConditions());
        assertEquals(createdRetentionScheduleActionDefinition.getEligibleOnFirstCompleteEvent(),retentionScheduleActionDefinition.getEligibleOnFirstCompleteEvent());
    }

    @Test(priority = 2)
    public void createRetentionScheduleStepFor401()
    {
        //Create retention schedule action definition
        createdRetentionScheduleActionDefinition = getRestAPIFactory().getRetentionScheduleAPI(new UserModel(getAdminUser().getUsername(), "wrongPassword")).createRetentionScheduleStep(retentionScheduleActionDefinition,createdRetentionSchedule.getId());

        // Verify the status code
        assertStatusCode(UNAUTHORIZED);
    }

    @Test(priority = 3)
    public void createRetentionScheduleStepFor403()
    {
        //Create retention schedule action definition
        createdRetentionScheduleActionDefinition = getRestAPIFactory().getRetentionScheduleAPI(nonRMuser).createRetentionScheduleStep(retentionScheduleActionDefinition,createdRetentionSchedule.getId());

        // Verify the status code
        assertStatusCode(FORBIDDEN);
    }

    @Test(priority = 4)
    public void createdRetentionScheduleStepFor404()
    {
        getRestAPIFactory().getRetentionScheduleAPI().getRetentionScheduleStep(recordCategory.getId());

        // Verify the status code
        assertStatusCode(NOT_FOUND);
    }
    @Test(priority = 5)
    public void getRetentionScheduleStepFor403()
    {
        // Get retention schedule steps with user having no rights
        getRestAPIFactory().getRetentionScheduleAPI(nonRMuser).getRetentionScheduleStep(createdRetentionSchedule.getId());

        // Verify the status code
        assertStatusCode(FORBIDDEN);
    }
    @Test(priority = 6)
    public void getRetentionScheduleStepFor401()
    {
        getRestAPIFactory().getRetentionScheduleAPI(new UserModel(getAdminUser().getUsername(), "wrongPassword")).getRetentionScheduleStep(createdRetentionSchedule.getId());

        // Verify the status code
        assertStatusCode(UNAUTHORIZED);
    }
    @Test(priority = 7)
    public void getRetentionScheduleStepWith200()
    {
        RetentionScheduleStepCollection receiveRetentionScheduleStepCollection = getRestAPIFactory().getRetentionScheduleAPI().getRetentionScheduleStep(createdRetentionSchedule.getId());
        // Verify the status code
        assertStatusCode(OK);
        receiveRetentionScheduleStepCollection.getEntries().forEach(c ->
        {
            RetentionScheduleActionDefinition retentionScheduleActionDefinition1 = c.getEntry();
            String retentionScheduleActionDefinitionId = retentionScheduleActionDefinition1.getId();
            assertNotNull(retentionScheduleActionDefinitionId);
            logger.info("Checking retention schedule " + retentionScheduleActionDefinitionId);

            // Find this retention schedule is created one or not
            assertEquals(createdRetentionScheduleActionDefinition.getId(),retentionScheduleActionDefinitionId);
            assertEquals(createdRetentionScheduleActionDefinition.getName(),retentionScheduleActionDefinition1.getName());
            assertEquals(createdRetentionScheduleActionDefinition.getDescription(),retentionScheduleActionDefinition1.getDescription());
            assertEquals(createdRetentionScheduleActionDefinition.getPeriod(),retentionScheduleActionDefinition1.getPeriod());
            assertEquals(createdRetentionScheduleActionDefinition.getPeriodAmount(),retentionScheduleActionDefinition1.getPeriodAmount());
            assertEquals(createdRetentionScheduleActionDefinition.getCombineDispositionStepConditions(),retentionScheduleActionDefinition1.getCombineDispositionStepConditions());
            assertEquals(createdRetentionScheduleActionDefinition.getEligibleOnFirstCompleteEvent(),retentionScheduleActionDefinition1.getEligibleOnFirstCompleteEvent());
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
