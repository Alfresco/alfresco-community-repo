/*-
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.rest.rm.community.records;

import static java.util.Arrays.asList;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.rest.rm.community.utils.CoreUtil.createBodyForMoveCopy;
import static org.alfresco.rest.rm.community.utils.CoreUtil.toContentModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.getFile;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.IMAGE_FILE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createRecordModel;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
/**
 * This class contains the tests for
 * CreateElectronicRecordsTests Action REST API
 *
 * @author Shishuraj Bisht
 */
public class CreateElectronicRecordsTests extends BaseRMRestTest {

    private RecordCategory rootCategory;
    private UserModel updateUser;
    /**
     * data prep services
     */

    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;

    private final String TEST_PREFIX = generateTestPrefix(CreateElectronicRecordsTests.class);
    private final String RM_ADMIN = TEST_PREFIX + "rm_admin";

    @BeforeClass (alwaysRun = true)
    public void preConditions()
    {
        STEP("Create RM Site");
        createRMSiteIfNotExists();

        STEP("Create RM Admin user");
        rmRolesAndActionsAPI.createUserAndAssignToRole(getAdminUser().getUsername(), getAdminUser().getPassword(), RM_ADMIN,
            getAdminUser().getPassword(),
            "Administrator");

        STEP("Create root level category");
        rootCategory = createRootCategory(getRandomName("Category"));

        STEP("Create the record folder1 inside the rootCategory");
        String recordFolder1 = createCategoryFolderInFilePlan().getId();
    }

    /**
     * Test v0 methods to create and get electronic records.
     */
    @Test
    @AlfrescoTest (jira = "RM-2768")
    public void createElectronicRecordTest() throws Exception {

        //create electronic record in record folder
        String recordFolder1 = createRecordFolder(rootCategory.getId(), getRandomName("recFolder")).getId();
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();

        Record electronicRecord = recordFolderAPI.createRecord(createElectronicRecordModel(), recordFolder1, getFile(IMAGE_FILE));
        assertStatusCode(CREATED);
        STEP("Check the electronic record has been created");
        assertStatusCode(CREATED);

        // Get recordsAPI instance initialised to updateUser
        org.alfresco.rest.rm.community.requests.gscore.api.RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI(updateUser);

        for (Record record: asList(electronicRecord)) {
            recordsAPI.getRecord(record.getId());
            assertStatusCode(OK);

            // Generate update metadata
            String newName = getModifiedPropertyValue(record.getName());
            String newTitle = getModifiedPropertyValue(record.getProperties().getTitle());
            String newDescription = getModifiedPropertyValue(record.getProperties().getDescription());

            // Update record
            recordsAPI.updateRecord(createRecordModel(newName, newDescription, newTitle), record.getId());
            assertStatusCode(OK);
        }
        // move the record from one folder1 to folder2
        STEP("Create the record folder2 inside the rootCategory");
        String recordFolder2 = createCategoryFolderInFilePlan().getId();

        STEP("Move record from folder1 to folder2");
        RestNodeModel electronicDocRestNodeModel = getRestAPIFactory()
            .getNodeAPI(toContentModel(electronicRecord.getId()))
            .move(createBodyForMoveCopy(recordFolder2));
        assertStatusCode(OK);
    }

    private String getModifiedPropertyValue(String originalValue) {
        /* to be used to append to modifications */
        String MODIFIED_PREFIX = "modified_";
        return MODIFIED_PREFIX + originalValue;
    }

    @AfterClass (alwaysRun = true)
    public void deletePreConditions() {
        STEP("Delete the created rootCategory along with corresponding record folders/records present in it");
        getRestAPIFactory().getRecordCategoryAPI().deleteRecordCategory(rootCategory.getId());
    }

    }


