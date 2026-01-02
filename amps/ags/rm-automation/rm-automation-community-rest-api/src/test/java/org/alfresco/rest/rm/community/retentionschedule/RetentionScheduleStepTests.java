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
import org.alfresco.rest.rm.community.model.retentionschedule.RetentionScheduleActionDefinition;
import org.alfresco.rest.rm.community.model.retentionschedule.RetentionScheduleStepCollection;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.alfresco.rest.core.v0.BaseAPI.RM_SITE_ID;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;

/**
 * Retention schedule step test case
 */
public class RetentionScheduleStepTests extends BaseRMRestTest
{
    private RecordCategory recordCategory;
    private RetentionSchedule createdRetentionSchedule;
    private final RetentionScheduleActionDefinition retentionScheduleActionDefinition = new RetentionScheduleActionDefinition();
    private RetentionScheduleActionDefinition createdRetentionActionDefinition;
    private UserModel nonRMuser;
    private final List<String> recordCategories = new ArrayList<>();
    private static final String TEST_USER = "testUser";
    private static final String RECORD_CATEGORY = "recordCategory";
    private static final String PERIOD_PROPERTY = "cm:created";
    private static final String AUTHORITY = "authority";
    private static final String INSTRUCTIONS = "instructions";
    private static final int PERIOD_AMOUNT = 5;
    private static final String PERIOD = "month";
    private static final List<String> EVENTS = Arrays.asList("case_closed","abolished");
    private static final String TRANSFER_STEP = "transfer";
    private static final String RETAIN_STEP = "retain";
    private static final String INVALID_PERIOD = "random";
    private static final String CUTOFF_STEP = "cutoff";
    private static final String DESTROY_STEP = "destroyContent";
    private static final String INVALID_PASSWORD = "wrongPassword";

    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;

    @BeforeClass(alwaysRun = true)
    public void preconditionForRetentionScheduleStepTests()
    {
        createRMSiteIfNotExists();
        // create a non rm user
        nonRMuser = dataUser.createRandomTestUser(TEST_USER);
        //Create record category
        recordCategory = createRootCategory(getRandomName(RECORD_CATEGORY));
        recordCategories.add(recordCategory.getId());
        RetentionSchedule retentionSchedule = new RetentionSchedule();
        retentionSchedule.setAuthority(AUTHORITY + getRandomAlphanumeric());
        retentionSchedule.setInstructions(INSTRUCTIONS + getRandomAlphanumeric());
        retentionSchedule.setIsRecordLevel(false);
        //Create retention schedule with a valid user
        createdRetentionSchedule = getRestAPIFactory().getRetentionScheduleAPI()
            .createRetentionSchedule(retentionSchedule, recordCategory.getId());

        retentionScheduleActionDefinition.setName(RETAIN_STEP);
        retentionScheduleActionDefinition.setDescription(INSTRUCTIONS);
        retentionScheduleActionDefinition.setPeriodAmount(PERIOD_AMOUNT);
        retentionScheduleActionDefinition.setPeriodProperty(PERIOD_PROPERTY);
        retentionScheduleActionDefinition.setPeriod(PERIOD);
        retentionScheduleActionDefinition.setCombineRetentionStepConditions(false);
        retentionScheduleActionDefinition.setEligibleOnFirstCompleteEvent(true);
        retentionScheduleActionDefinition.setEvents(EVENTS);
    }

    @Test(priority = 1)
    public void createRetentionScheduleStepFor422()
    {
        RetentionScheduleActionDefinition actionDefinition = getRetentionScheduleActionDefinition();
        //Creating the first action "transfer" should give 422
        actionDefinition.setName(TRANSFER_STEP);
        actionDefinition.setLocation("location");
        //Create retention schedule action definition
        getRestAPIFactory().getRetentionScheduleAPI().createRetentionScheduleStep(actionDefinition,createdRetentionSchedule.getId());
        // Verify the status code
        assertStatusCode(UNPROCESSABLE_ENTITY);
    }

    @Test(priority = 2)
    public void createRetentionScheduleStepWithInvalidPeriodValue()
    {
        RetentionScheduleActionDefinition actionDefinition = getRetentionScheduleActionDefinition();
        actionDefinition.setName(RETAIN_STEP);
        //Invalid period value
        actionDefinition.setPeriod(INVALID_PERIOD);
        //Create retention schedule action definition
        getRestAPIFactory().getRetentionScheduleAPI().createRetentionScheduleStep(actionDefinition,createdRetentionSchedule.getId());
        // Verify the status code
        assertStatusCode(BAD_REQUEST);
    }

    @Test(priority = 3)
    public void createRetentionScheduleWithInvalidStep()
    {
        RecordCategory recordCategory = createRootCategory(getRandomName(RECORD_CATEGORY));
        recordCategories.add(recordCategory.getId());
        RetentionSchedule retentionSchedule = createRetentionSchedule(recordCategory);
        RetentionScheduleActionDefinition actionDefinition = getRetentionScheduleActionDefinition();
        actionDefinition.setName(RETAIN_STEP);
        getRestAPIFactory().getRetentionScheduleAPI().createRetentionScheduleStep(actionDefinition,retentionSchedule.getId());

        assertStatusCode(CREATED);

        RetentionScheduleActionDefinition actionDefinition1 = getRetentionScheduleActionDefinition();
        actionDefinition1.setName(TRANSFER_STEP);
        actionDefinition1.setLocation("location");
        getRestAPIFactory().getRetentionScheduleAPI().createRetentionScheduleStep(actionDefinition1,retentionSchedule.getId());

        assertStatusCode(CREATED);

        RetentionScheduleActionDefinition actionDefinition2 = getRetentionScheduleActionDefinition();
        actionDefinition2.setName(CUTOFF_STEP);
        //Create retention schedule action definition
        getRestAPIFactory().getRetentionScheduleAPI().createRetentionScheduleStep(actionDefinition2,retentionSchedule.getId());
        // Verify the status code
        assertStatusCode(CONFLICT);
    }

    @Test(priority = 4)
    public void createRetentionScheduleWithInvalidStepAfterDestroy()
    {
        RecordCategory recordCategory = createRootCategory(getRandomName(RECORD_CATEGORY));
        recordCategories.add(recordCategory.getId());
        RetentionSchedule retentionSchedule = createRetentionSchedule(recordCategory);
        RetentionScheduleActionDefinition actionDefinition = getRetentionScheduleActionDefinition();
        actionDefinition.setName(RETAIN_STEP);
        getRestAPIFactory().getRetentionScheduleAPI().createRetentionScheduleStep(actionDefinition,retentionSchedule.getId());

        assertStatusCode(CREATED);

        RetentionScheduleActionDefinition actionDefinition1 = getRetentionScheduleActionDefinition();
        actionDefinition1.setName(DESTROY_STEP);
        getRestAPIFactory().getRetentionScheduleAPI().createRetentionScheduleStep(actionDefinition1,retentionSchedule.getId());

        assertStatusCode(CREATED);

        RetentionScheduleActionDefinition actionDefinition2 = getRetentionScheduleActionDefinition();
        actionDefinition2.setName(CUTOFF_STEP);
        //Create retention schedule action definition
        getRestAPIFactory().getRetentionScheduleAPI().createRetentionScheduleStep(actionDefinition2,retentionSchedule.getId());
        // Verify the status code
        assertStatusCode(CONFLICT);
    }

    @Test(priority = 5)
    public void combineRetentionStepConditionsNotValidForNonAccessionStep()
    {
        RecordCategory recordCategory = createRootCategory(getRandomName(RECORD_CATEGORY));
        recordCategories.add(recordCategory.getId());
        RetentionSchedule retentionSchedule = createRetentionSchedule(recordCategory);
        RetentionScheduleActionDefinition actionDefinition = getRetentionScheduleActionDefinition();
        actionDefinition.setName(RETAIN_STEP);
        actionDefinition.setCombineRetentionStepConditions(true);
        getRestAPIFactory().getRetentionScheduleAPI().createRetentionScheduleStep(actionDefinition,retentionSchedule.getId());

        assertStatusCode(BAD_REQUEST);
    }

    @Test(priority = 6)
    public void createRetentionScheduleWithSameStep()
    {
        RecordCategory recordCategory = createRootCategory(getRandomName(RECORD_CATEGORY));
        recordCategories.add(recordCategory.getId());
        RetentionSchedule retentionSchedule = createRetentionSchedule(recordCategory);
        RetentionScheduleActionDefinition actionDefinition = getRetentionScheduleActionDefinition();
        actionDefinition.setName(RETAIN_STEP);
        getRestAPIFactory().getRetentionScheduleAPI().createRetentionScheduleStep(actionDefinition,retentionSchedule.getId());

        assertStatusCode(CREATED);

        RetentionScheduleActionDefinition actionDefinition1 = getRetentionScheduleActionDefinition();
        actionDefinition1.setName(RETAIN_STEP);
        getRestAPIFactory().getRetentionScheduleAPI().createRetentionScheduleStep(actionDefinition1,retentionSchedule.getId());

        // Verify the status code
        assertStatusCode(CONFLICT);
    }

    @Test(priority = 7)
    public void createRetentionScheduleWithMultipleTransferStep()
    {
        RecordCategory recordCategory = createRootCategory(getRandomName(RECORD_CATEGORY));
        recordCategories.add(recordCategory.getId());
        RetentionSchedule retentionSchedule = createRetentionSchedule(recordCategory);
        RetentionScheduleActionDefinition actionDefinition = getRetentionScheduleActionDefinition();
        actionDefinition.setName(RETAIN_STEP);
        getRestAPIFactory().getRetentionScheduleAPI().createRetentionScheduleStep(actionDefinition,retentionSchedule.getId());

        assertStatusCode(CREATED);

        RetentionScheduleActionDefinition actionDefinition1 = getRetentionScheduleActionDefinition();
        actionDefinition1.setName(TRANSFER_STEP);
        actionDefinition1.setLocation("location");

        getRestAPIFactory().getRetentionScheduleAPI().createRetentionScheduleStep(actionDefinition1, retentionSchedule.getId());

        RetentionScheduleActionDefinition actionDefinition2 = getRetentionScheduleActionDefinition();
        actionDefinition2.setName(TRANSFER_STEP);
        actionDefinition2.setLocation("location");
        getRestAPIFactory().getRetentionScheduleAPI().createRetentionScheduleStep(actionDefinition2, retentionSchedule.getId());
        // Verify the status code
        assertStatusCode(CREATED);
    }

    @Test(priority = 8)
    public void createRetentionScheduleStepFor201()
    {
        //Create retention schedule action definition
        createdRetentionActionDefinition = getRestAPIFactory().getRetentionScheduleAPI().createRetentionScheduleStep(retentionScheduleActionDefinition,createdRetentionSchedule.getId());
        // Verify the status code
        assertStatusCode(CREATED);
        // Find this retention schedule is created one or not
        assertEquals(createdRetentionActionDefinition.getName(), retentionScheduleActionDefinition.getName());
        assertEquals(createdRetentionActionDefinition.getDescription(), retentionScheduleActionDefinition.getDescription());
        assertEquals(createdRetentionActionDefinition.getPeriodAmount(), retentionScheduleActionDefinition.getPeriodAmount());
        assertEquals(createdRetentionActionDefinition.isCombineRetentionStepConditions(), retentionScheduleActionDefinition.isCombineRetentionStepConditions());
        assertEquals(createdRetentionActionDefinition.isEligibleOnFirstCompleteEvent(), retentionScheduleActionDefinition.isEligibleOnFirstCompleteEvent());
    }

    @Test(priority = 9)
    public void createRetentionScheduleStepFor401()
    {
        //Create retention schedule action definition
        getRestAPIFactory().getRetentionScheduleAPI(new UserModel(getAdminUser().getUsername(), INVALID_PASSWORD)).createRetentionScheduleStep(retentionScheduleActionDefinition,createdRetentionSchedule.getId());
        // Verify the status code
        assertStatusCode(UNAUTHORIZED);
    }

    @Test(priority = 10)
    public void createRetentionScheduleStepFor403()
    {
        //Create retention schedule action definition
        getRestAPIFactory().getRetentionScheduleAPI(nonRMuser).createRetentionScheduleStep(retentionScheduleActionDefinition,createdRetentionSchedule.getId());
        // Verify the status code
        assertStatusCode(FORBIDDEN);
    }

    @Test(priority = 11)
    public void retentionScheduleStepFor400()
    {
        getRestAPIFactory().getRetentionScheduleAPI().getRetentionScheduleStep(recordCategory.getId());
        // Verify the status code
        assertStatusCode(BAD_REQUEST);
    }

    @Test(priority = 12)
    public void createRetentionScheduleStepFor404()
    {
        //Create retention schedule action definition
        getRestAPIFactory().getRetentionScheduleAPI().createRetentionScheduleStep(retentionScheduleActionDefinition,getRandomAlphanumeric());
        // Verify the status code
        assertStatusCode(NOT_FOUND);
    }

    @Test(priority = 13)
    public void retentionScheduleStepFor403()
    {
        // Get retention schedule steps with user having no rights
        getRestAPIFactory().getRetentionScheduleAPI(nonRMuser).getRetentionScheduleStep(createdRetentionSchedule.getId());
        // Verify the status code
        assertStatusCode(FORBIDDEN);
    }

    @Test(priority = 14)
    public void retentionScheduleStepFor401()
    {
        getRestAPIFactory().getRetentionScheduleAPI(new UserModel(getAdminUser().getUsername(), INVALID_PASSWORD)).getRetentionScheduleStep(createdRetentionSchedule.getId());
        // Verify the status code
        assertStatusCode(UNAUTHORIZED);
    }

    @Test(priority = 15)
    public void retentionScheduleStepWith200()
    {
        RetentionScheduleStepCollection receiveRetentionStepCollection = getRestAPIFactory().getRetentionScheduleAPI().getRetentionScheduleStep(createdRetentionSchedule.getId());
        // Verify the status code
        assertStatusCode(OK);
        receiveRetentionStepCollection.getEntries().forEach(c ->
        {
            RetentionScheduleActionDefinition retentionActionDef = c.getEntry();
            assertNotNull(retentionActionDef.getId());
            // Find this retention schedule is created one or not
            assertEquals(createdRetentionActionDefinition.getId(), retentionActionDef.getId());
            assertEquals(createdRetentionActionDefinition.getName(), retentionActionDef.getName());
            assertEquals(createdRetentionActionDefinition.getDescription(), retentionActionDef.getDescription());
            assertEquals(createdRetentionActionDefinition.getPeriod(), retentionActionDef.getPeriod());
            assertEquals(createdRetentionActionDefinition.getPeriodAmount(), retentionActionDef.getPeriodAmount());
            assertEquals(createdRetentionActionDefinition.isCombineRetentionStepConditions(), retentionActionDef.isCombineRetentionStepConditions());
            assertEquals(createdRetentionActionDefinition.isEligibleOnFirstCompleteEvent(), retentionActionDef.isEligibleOnFirstCompleteEvent());
        });
    }

    private RetentionSchedule createRetentionSchedule(RecordCategory recordCategory)
    {
        RetentionSchedule retentionSchedule = new RetentionSchedule();
        retentionSchedule.setAuthority(AUTHORITY + getRandomAlphanumeric());
        retentionSchedule.setInstructions(INSTRUCTIONS + getRandomAlphanumeric());
        retentionSchedule.setIsRecordLevel(false);
        //Create retention schedule with a valid user
        retentionSchedule = getRestAPIFactory().getRetentionScheduleAPI()
            .createRetentionSchedule(retentionSchedule, recordCategory.getId());
        // Verify the status code
        assertStatusCode(CREATED);
        return retentionSchedule;
    }

    private static RetentionScheduleActionDefinition getRetentionScheduleActionDefinition()
    {
        RetentionScheduleActionDefinition actionDefinition = new RetentionScheduleActionDefinition();
        actionDefinition.setDescription(INSTRUCTIONS);
        actionDefinition.setPeriodAmount(PERIOD_AMOUNT);
        actionDefinition.setPeriodProperty(PERIOD_PROPERTY);
        actionDefinition.setPeriod(PERIOD);
        actionDefinition.setCombineRetentionStepConditions(false);
        actionDefinition.setEligibleOnFirstCompleteEvent(true);
        actionDefinition.setEvents(EVENTS);
        return actionDefinition;
    }

    @AfterClass(alwaysRun = true)
    public void cleanUpRetentionScheduleStepTests()
    {
        rmRolesAndActionsAPI.deleteAllItemsInContainer(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), RM_SITE_ID, recordCategory.getName());
        recordCategories.forEach(this::deleteRecordCategory);
        dataUser.deleteUser(nonRMuser);
    }
}
