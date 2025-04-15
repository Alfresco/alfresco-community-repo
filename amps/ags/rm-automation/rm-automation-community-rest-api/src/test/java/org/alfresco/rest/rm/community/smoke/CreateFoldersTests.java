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
import org.alfresco.rest.rm.community.model.common.ReviewPeriod;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.recordfolder.RecordFolder;
import org.alfresco.rest.rm.community.model.recordfolder.RecordFolderProperties;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordCategoryAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.test.AlfrescoTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.rest.rm.community.utils.CoreUtil.createBodyForMoveCopy;
import static org.alfresco.rest.rm.community.utils.CoreUtil.toContentModel;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.*;

public class CreateFoldersTests extends BaseRMRestTest {

    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;

    private final String TEST_PREFIX = generateTestPrefix(CreateCategoriesTests.class);
    private final String RM_ADMIN = TEST_PREFIX + "rm_admin";
    private RecordCategory Category1;
    private RecordCategory Category2;
    private RecordCategoryChild recordCategoryChild;

    @BeforeClass(alwaysRun = true)
    public void preconditionForCreateFolderTests() {
        STEP("Create the RM site if doesn't exist");
        createRMSiteIfNotExists();

        STEP("Create RM Admin user");
        rmRolesAndActionsAPI.createUserAndAssignToRole(getAdminUser().getUsername(), getAdminUser().getPassword(), RM_ADMIN,
            getAdminUser().getPassword(),
            "Administrator");

        STEP("Create two category");
        Category1 = createRootCategory(getRandomName("Category1"));

        Category2 = createRootCategory(getRandomName("Category2"));

        // Create a record folder inside the category 1
        recordCategoryChild = createRecordFolder(Category1.getId(), getRandomName("recFolder"));

    }

    @Test
    @AlfrescoTest(jira = "RM-2757")
    public void createFolders() throws Exception {

        // Create record category first
        String folderDescription = "The folder description is updated" + getRandomAlphanumeric();
        String folderName = "The folder name is updated" + getRandomAlphanumeric();
        String folderTitle = "Update title " + getRandomAlphanumeric();
        String location = "Location "+ getRandomAlphanumeric();

        // Create the record folder properties to update
        RecordFolder recordFolder = RecordFolder.builder()
            .name(folderName)
            .properties(RecordFolderProperties.builder()
                .title(folderTitle)
                .description(folderDescription)
                .vitalRecordIndicator(true)
                .reviewPeriod(new ReviewPeriod("month","1"))
                .location(location)
                .build())
            .build();

        // Update the record folder
        RecordFolder updatedRecordFolder = getRestAPIFactory().getRecordFolderAPI().updateRecordFolder(recordFolder, recordCategoryChild.getId());

        // Check the Response Status Code
        assertStatusCode(OK);

        STEP("copy updated Record in category 1 and category 2");
        getRestAPIFactory().getNodeAPI(toContentModel(updatedRecordFolder.getId())).copy(createBodyForMoveCopy(Category1.getId()));
        //assertStatusCode(OK);
        getRestAPIFactory().getNodeAPI(toContentModel(updatedRecordFolder.getId())).copy(createBodyForMoveCopy(Category2.getId()));
        //assertStatusCode(OK);


        // Delete the Updated folder
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        String recordFolderId = updatedRecordFolder.getId();
        recordFolderAPI.deleteRecordFolder(recordFolderId);

        // Check the response status code
        assertStatusCode(NO_CONTENT);

        // Check the record folder is not found
        recordFolderAPI.getRecordFolder(recordFolderId);

        // Check the response status code
        assertStatusCode(NOT_FOUND);

        STEP("move updated Record from category 1 to category 2");
        getRestAPIFactory().getNodeAPI(toContentModel(updatedRecordFolder.getId())).move(createBodyForMoveCopy(Category2.getId()));

        // move category 2 to category 1
        getRestAPIFactory().getNodeAPI(toContentModel(Category2.getId())).move(createBodyForMoveCopy(Category1.getId()));

        // Delete the record category
        RecordCategoryAPI recordCategoryAPI = getRestAPIFactory().getRecordCategoryAPI();
        String recordCategoryId = Category1.getId();
        recordCategoryAPI.deleteRecordCategory(recordCategoryId);

        // Verify the status code
        assertStatusCode(NO_CONTENT);

    }

}
