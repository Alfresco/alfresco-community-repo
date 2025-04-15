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

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.RecordFoldersAPI;
import org.alfresco.rest.v0.service.DispositionScheduleService;
import org.alfresco.test.AlfrescoTest;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.alfresco.rest.rm.community.model.recordcategory.RetentionPeriodProperty.CREATED_DATE;
import static org.alfresco.rest.rm.community.model.recordcategory.RetentionPeriodProperty.CUT_OFF_DATE;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.rest.rm.community.utils.CoreUtil.createBodyForMoveCopy;
import static org.alfresco.rest.rm.community.utils.CoreUtil.toContentModel;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.OK;
import static org.testng.AssertJUnit.assertNotNull;


public class DestroyRecordFolderActionsTest extends BaseRMRestTest {

    private RecordCategory Category1,CATEGORY_TO_MOVE;
    @Autowired
    private DispositionScheduleService dispositionScheduleService;
    @Autowired
    private RecordFoldersAPI recordFoldersAPI;
    private final String TEST_PREFIX = generateTestPrefix(DestroyRecordFolderActionsTest.class);
    private final String folderDisposition = TEST_PREFIX + "RM-2937 folder ghosting";


    @BeforeClass(alwaysRun = true)
    private void setUp(){

        STEP("Create the RM site if doesn't exist");
        createRMSiteIfNotExists();

        STEP("Create two record category");
        Category1 = createRootCategory(getRandomName("Category1"));
        CATEGORY_TO_MOVE = createRootCategory(getRandomName("CATEGORY_TO_MOVE"));

        //create retention schedule
        dispositionScheduleService.createCategoryRetentionSchedule(Category1.getName(), false);

        // add cut off step
        dispositionScheduleService.addCutOffAfterPeriodStep(Category1.getName(), "day|2", CREATED_DATE);

        // add destroy step with ghosting
        dispositionScheduleService.addDestroyWithGhostingImmediatelyAfterCutOff(Category1.getName());

    }

    @Test
    @AlfrescoTest (jira = "RM-1621")
    public void moveOnCutOffDestroyFolders() throws Exception {

        //create folders
        RecordCategoryChild FOLDER_DESTROY = createFolder(getAdminUser(),Category1.getId(),folderDisposition);

        // edit disposition date
        recordFoldersAPI.postFolderAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),editDispositionDateJson(),FOLDER_DESTROY.getName());

        // cut off the FOLDER_DESTROY
        recordFoldersAPI.postFolderAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","cutoff"),FOLDER_DESTROY.getName());


        // Destroy the FOLDER_DESTROY
        recordFoldersAPI.postFolderAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","destroy"),FOLDER_DESTROY.getName());


       //Move the FOLDER_DESTROY within the CATEGORY_TO_MOVE.");
        getRestAPIFactory().getNodeAPI(toContentModel(FOLDER_DESTROY.getId())).move(createBodyForMoveCopy(CATEGORY_TO_MOVE.getId()));
        assertStatusCode(OK);

    }

    @AfterMethod(alwaysRun = true)
    private  void deletePreconditions() {

            deleteRecordCategory(Category1.getId());
            deleteRecordCategory(CATEGORY_TO_MOVE.getId());

        }

    }
