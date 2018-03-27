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
package org.alfresco.rest.rm.community.records;

import static org.alfresco.rest.rm.community.base.TestData.NONELECTRONIC_RECORD_NAME;
import static org.alfresco.rest.rm.community.base.TestData.RECORD_FOLDER_NAME;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.NON_ELECTRONIC_RECORD_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.user.UserRoles.ROLE_RM_SECURITY_OFFICER;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.IMAGE_FILE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createElectronicUnfiledContainerChildModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createNonElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createNonElectronicUnfiledContainerChildModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.getFile;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChild;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChildCollection;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChildProperties;
import org.alfresco.rest.rm.community.model.user.UserPermissions;
import org.alfresco.rest.rm.community.requests.gscore.api.FilePlanAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RMUserAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordCategoryAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordsAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.UnfiledContainerAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.UnfiledRecordFolderAPI;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Update records tests
 * <br>
 * These tests only test the update of electronic and non-electronic tests
 * <p>
 * @author Kristijan Conkas
 * @since 2.6
 */
public class UpdateRecordsTests extends BaseRMRestTest
{
    /* to be used to append to modifications */
    private final String MODIFIED_PREFIX = "modified_";

    /** Incomplete electronic and non electronic records created in one record folder, unfiled records container and one unfiled record folder */
    @DataProvider(name = "incompleteRecords")
    public Object[][] getIncompleteRecords() throws Exception
    {
        //create electronic and nonElectronic record in record folder
        String recordFolderId = createCategoryFolderInFilePlan().getId();
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();

        Record electronicRecord = recordFolderAPI.createRecord(createElectronicRecordModel(), recordFolderId, getFile(IMAGE_FILE));
        assertStatusCode(CREATED);

        Record nonElectronicRecord = recordFolderAPI.createRecord(createNonElectronicRecordModel(), recordFolderId);
        assertStatusCode(CREATED);

        //create electronic record and nonElectronic record in unfiled records container
        UnfiledContainerAPI unfiledContainersAPI = getRestAPIFactory().getUnfiledContainersAPI();
        UnfiledContainerChild electronicRecord1 = unfiledContainersAPI.uploadRecord(createElectronicUnfiledContainerChildModel(), UNFILED_RECORDS_CONTAINER_ALIAS, getFile(IMAGE_FILE));
        assertStatusCode(CREATED);

        UnfiledContainerChild nonElectronicRecord1 = unfiledContainersAPI.createUnfiledContainerChild(createNonElectronicUnfiledContainerChildModel(), UNFILED_RECORDS_CONTAINER_ALIAS);
        assertStatusCode(CREATED);

        //create electronic record and nonElectronic record in unfiled record folder
        String unfiledRecordFolderId = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS, "Unfiled Folder " + getRandomAlphanumeric(), UNFILED_RECORD_FOLDER_TYPE).getId();
        UnfiledRecordFolderAPI unfiledRecordFoldersAPI = getRestAPIFactory().getUnfiledRecordFoldersAPI();
        UnfiledContainerChild electronicRecord2 = unfiledRecordFoldersAPI.uploadRecord(createElectronicUnfiledContainerChildModel(), unfiledRecordFolderId, getFile(IMAGE_FILE));
        assertStatusCode(CREATED);

        UnfiledContainerChild nonElectronicRecord2 = unfiledRecordFoldersAPI.createUnfiledRecordFolderChild(createNonElectronicUnfiledContainerChildModel(), unfiledRecordFolderId);
        assertStatusCode(CREATED);

        return new String[][]
        {
            // an arbitrary record folder
            { electronicRecord.getId(), nonElectronicRecord.getId()},
            // unfiled records root
            { electronicRecord1.getId(), nonElectronicRecord1.getId()},
            // an arbitrary unfiled records folder
            { electronicRecord2.getId(), nonElectronicRecord2.getId()}
        };
    }

    /** Complete electronic and non electronic records created in one record folder, unfiled records container and one unfiled record folder */
    @DataProvider(name = "completeRecords")
    public Object[][] getCompleteRecords() throws Exception
    {
        //create electronic and nonElectronic record in record folder
        String recordFolderId = createCategoryFolderInFilePlan().getId();
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();

        Record electronicRecord = recordFolderAPI.createRecord(createElectronicRecordModel(), recordFolderId, getFile(IMAGE_FILE));
        assertStatusCode(CREATED);
        completeRecord(electronicRecord.getId());

        Record nonElectronicRecord = recordFolderAPI.createRecord(createNonElectronicRecordModel(), recordFolderId);
        assertStatusCode(CREATED);
        completeRecord(nonElectronicRecord.getId());

        //create electronic record and nonElectronic record in unfiled records container
        UnfiledContainerAPI unfiledContainersAPI = getRestAPIFactory().getUnfiledContainersAPI();
        UnfiledContainerChild electronicRecord1 = unfiledContainersAPI.uploadRecord(createElectronicUnfiledContainerChildModel(), UNFILED_RECORDS_CONTAINER_ALIAS, getFile(IMAGE_FILE));
        assertStatusCode(CREATED);
        completeRecord(electronicRecord1.getId());

        UnfiledContainerChild nonElectronicRecord1 = unfiledContainersAPI.createUnfiledContainerChild(createNonElectronicUnfiledContainerChildModel(), UNFILED_RECORDS_CONTAINER_ALIAS);
        assertStatusCode(CREATED);
        completeRecord(nonElectronicRecord1.getId());

        //create electronic record and nonElectronic record in unfiled record folder
        String unfiledRecordFolderId = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS, "Unfiled Folder " + getRandomAlphanumeric(), UNFILED_RECORD_FOLDER_TYPE).getId();
        UnfiledRecordFolderAPI unfiledRecordFoldersAPI = getRestAPIFactory().getUnfiledRecordFoldersAPI();
        UnfiledContainerChild electronicRecord2 = unfiledRecordFoldersAPI.uploadRecord(createElectronicUnfiledContainerChildModel(), unfiledRecordFolderId, getFile(IMAGE_FILE));
        assertStatusCode(CREATED);
        completeRecord(electronicRecord2.getId());

        UnfiledContainerChild nonElectronicRecord2 = unfiledRecordFoldersAPI.createUnfiledRecordFolderChild(createNonElectronicUnfiledContainerChildModel(), unfiledRecordFolderId);
        assertStatusCode(CREATED);
        completeRecord(nonElectronicRecord2.getId());

        return new String[][]
                    {
                        // an arbitrary record folder
                        { electronicRecord.getId(), nonElectronicRecord.getId()},
                        // unfiled records root
                        { electronicRecord1.getId(), nonElectronicRecord1.getId()},
                        // an arbitrary unfiled records folder
                        { electronicRecord2.getId(), nonElectronicRecord2.getId()}
                    };
    }

    /**
     * <pre>
     * Given an incomplete record
     * When I try to update the records meta-data
     * Then the record is successfully updated
     * </pre>
     * @throws Exception
     */
    @Test
    (
        dataProvider = "incompleteRecords",
        description = "Incomplete records can be updated"
    )
    @AlfrescoTest(jira="RM-4362")
    public void incompleteRecordsCanBeUpdated(String electronicRecordId, String nonElectronicRecordId) throws Exception
    {
        // Get the recordsAPI
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();
        Record electronicRecord = recordsAPI.getRecord(electronicRecordId);
        Record nonElectronicRecord = recordsAPI.getRecord(nonElectronicRecordId);

        for (Record record: Arrays.asList(electronicRecord, nonElectronicRecord)) {
            // Generate update metadata
            String newName = getModifiedPropertyValue(record.getName());
            String newTitle = getModifiedPropertyValue(record.getProperties().getTitle());
            String newDescription = getModifiedPropertyValue(record.getProperties().getDescription());
            Record recordModel = createRecordModel(newName, newDescription, newTitle);

            // Update record
            recordsAPI.updateRecord(recordModel, record.getId());
            assertStatusCode(OK);

            // Verify the original record meta data has been retained
            Record updatedRecord = recordsAPI.getRecord(record.getId());
            assertEquals(updatedRecord.getName(), newName);
            assertEquals(updatedRecord.getProperties().getTitle(), newTitle);
            assertEquals(updatedRecord.getProperties().getDescription(), newDescription);
        }
    }

    /**
     * <pre>
     * Given an incomplete record
     * And that I am a non-admin user with metadata update capabilities
     * When I try to update the records meta-data
     * Then the record is successfully updated
     * </pre>
     * @throws Exception
     */
    @Test
    (
        description = "User with Edit Metadata capabilities can update incomplete record's metadata"
    )
    @AlfrescoTest(jira="RM-4362")
    public void userWithEditMetadataCapsCanUpdateMetadata() throws Exception
    {
        RMUserAPI rmUserAPI = getRestAPIFactory().getRMUserAPI();
        // Create test user and add it with collab. privileges
        UserModel updateUser = getDataUser().createRandomTestUser("updateuser");
        updateUser.setUserRole(UserRole.SiteCollaborator);
        getDataUser().addUserToSite(updateUser, new SiteModel(getRestAPIFactory().getRMSiteAPI().getSite().getId()), UserRole.SiteCollaborator);

        // RM Security Officer is the lowest role with Edit Record Metadata capabilities
        rmUserAPI.assignRoleToUser(updateUser.getUsername(), ROLE_RM_SECURITY_OFFICER.roleId);
        assertStatusCode(OK);

        // Create random folder
        RecordCategoryChild recordFolder = createCategoryFolderInFilePlan();
        logger.info("random folder:" + recordFolder.getName());

        // Grant updateUser Filing privileges on randomFolder category, this will be
        // Inherited to randomFolder
        RecordCategoryAPI recordCategoryAPI = getRestAPIFactory().getRecordCategoryAPI();
        rmUserAPI.addUserPermission(recordCategoryAPI.getRecordCategory(recordFolder.getParentId()).getId(),
            updateUser, UserPermissions.PERMISSION_FILING);
        assertStatusCode(OK);

        // Create electronic and non-electronic records in a folder
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        Record electronicRecord = recordFolderAPI.createRecord(createElectronicRecordModel(), recordFolder.getId(), getFile(IMAGE_FILE));
        assertStatusCode(CREATED);
        Record nonElectronicRecord = recordFolderAPI.createRecord(createNonElectronicRecordModel(), recordFolder.getId());
        assertStatusCode(CREATED);

        // Get recordsAPI instance initialised to updateUser
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI(updateUser);

        for (Record record: Arrays.asList(electronicRecord, nonElectronicRecord))
        {
            recordsAPI.getRecord(record.getId());
            assertStatusCode(OK);

            // Generate update metadata
            String newName = getModifiedPropertyValue(record.getName());
            String newTitle = getModifiedPropertyValue(record.getProperties().getTitle());
            String newDescription = getModifiedPropertyValue(record.getProperties().getDescription());
            Record recordModel = createRecordModel(newName, newDescription, newTitle);

            // Update record
            recordsAPI.updateRecord(recordModel, record.getId());
            assertStatusCode(OK);

            // Verify the update got applied
            Record updatedRecord = recordsAPI.getRecord(record.getId());
            assertEquals(updatedRecord.getName(), newName);
            assertEquals(updatedRecord.getProperties().getTitle(), newTitle);
            assertEquals(updatedRecord.getProperties().getDescription(), newDescription);
            assertEquals(updatedRecord.getModifiedByUser().getId(), updateUser.getUsername());
        }
    }

    /**
     * <pre>
     * Given a complete record
     * When I try to update the records meta-data
     * Then it fails
     * And and the records meta-data is unchanged
     * </pre>
     * @throws Exception
     */
    @Test
    (
        dataProvider = "completeRecords",
        description = "Complete records can't be updated"
    )
    @AlfrescoTest(jira="RM-4362")
    public void completeRecordsCantBeUpdated(String electronicRecordId, String nonElectronicRecordId) throws Exception
    {
        // Get the recordsAPI
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();
        Record electronicRecord = recordsAPI.getRecord(electronicRecordId);
        Record nonElectronicRecord = recordsAPI.getRecord(nonElectronicRecordId);

        for (Record record: Arrays.asList(electronicRecord, nonElectronicRecord)) {
            // Generate update metadata
            String newName = getModifiedPropertyValue(record.getName());
            String newTitle = getModifiedPropertyValue(record.getProperties().getTitle());
            String newDescription = getModifiedPropertyValue(record.getProperties().getDescription());
            Record recordModel = createRecordModel(newName, newDescription, newTitle);

            // Update record
            recordsAPI.updateRecord(recordModel, record.getId());
            assertStatusCode(FORBIDDEN);

            // Verify the original record meta data has been retained
            Record updatedRecord = recordsAPI.getRecord(record.getId());
            assertEquals(updatedRecord.getName(), record.getName());
            assertEquals(updatedRecord.getProperties().getTitle(), record.getProperties().getTitle());
            assertEquals(updatedRecord.getProperties().getDescription(), record.getProperties().getDescription());
        }
    }

    /**
     * Helper method to generate modified property value based on original value
     * @param originalValue original value
     * @return modified value
     */
    private String getModifiedPropertyValue(String originalValue)
    {
        return MODIFIED_PREFIX + originalValue;
    }
    /**
     * <pre>
     * Given a created record
     * When I try to update the record aspects with an empty list
     * Then it fails
     * </pre>
     * @throws Exception
     */
    @Test(description = "Cannot remove mandatory aspects from record")
    @AlfrescoTest(jira = "RM-4926")
    public void electronicRecordMandatoryAspectsCannotBeRemoved() throws Exception
    {
        // Get the recordsAPI
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();

        RecordCategoryChild recordFolder = createCategoryFolderInFilePlan();

        // Create an electronic record
        Record recordModel = createElectronicRecordModel();
        String recordId = recordFolderAPI.createRecord(recordModel, recordFolder.getId(), getFile(IMAGE_FILE)).getId();

        Record electronicRecord = recordsAPI.getRecord(recordId);
        List<String> aspects = electronicRecord.getAspectNames();

        // this operation is only valid for records
        assertTrue(aspects.contains("rma:record"));
        assertTrue(aspects.contains("rma:filePlanComponent"));
        assertTrue(aspects.contains("rma:recordComponentIdentifier"));
        assertTrue(aspects.contains("rma:commonRecordDetails"));

        List<String> emptyAspectList = new ArrayList<String>();
        Record recordModelToUpdate = Record.builder().aspectNames(emptyAspectList).build();

        // Update record
        recordsAPI.updateRecord(recordModelToUpdate, electronicRecord.getId());
        assertStatusCode(UNPROCESSABLE_ENTITY);

        // Get the recordsAPI
        UnfiledRecordFolderAPI unfiledRecordFolderAPI = getRestAPIFactory().getUnfiledRecordFoldersAPI();

        // Create root unfiled record folder
        UnfiledContainerChild unfiledFolder = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS, RECORD_FOLDER_NAME,
                UNFILED_RECORD_FOLDER_TYPE);

        // Create an electronic record
        UnfiledContainerChild unfiledRecordModel = UnfiledContainerChild.builder()
                .properties(UnfiledContainerChildProperties.builder().description(NONELECTRONIC_RECORD_NAME).title("Title").build())
                .name(NONELECTRONIC_RECORD_NAME).nodeType(NON_ELECTRONIC_RECORD_TYPE.toString()).build();
        UnfiledContainerChild unfiledRecord = unfiledRecordFolderAPI.createUnfiledRecordFolderChild(unfiledRecordModel,
                unfiledFolder.getId());

        aspects = unfiledRecord.getAspectNames();

        assertTrue(aspects.contains("rma:record"));
        assertTrue(aspects.contains("rma:filePlanComponent"));
        assertTrue(aspects.contains("rma:recordComponentIdentifier"));
        assertTrue(aspects.contains("rma:commonRecordDetails"));

        Record recordModelToUpdateToUnfiled = Record.builder().aspectNames(emptyAspectList).build();
        // Update record
        recordsAPI.updateRecord(recordModelToUpdateToUnfiled, unfiledRecord.getId());
        assertStatusCode(UNPROCESSABLE_ENTITY);
    }

    @AfterClass (alwaysRun = true)
    public void tearDown() throws Exception
    {
        FilePlanAPI filePlansAPI = getRestAPIFactory().getFilePlansAPI();
        RecordCategoryAPI recordCategoryAPI = getRestAPIFactory().getRecordCategoryAPI();

        filePlansAPI.getRootRecordCategories(FILE_PLAN_ALIAS).getEntries().forEach(recordCategoryEntry ->
        {
            recordCategoryAPI.deleteRecordCategory(recordCategoryEntry.getEntry().getId());
        });
        
        UnfiledContainerChildCollection listedChildren = getRestAPIFactory().getUnfiledContainersAPI()
                .getUnfiledContainerChildren(UNFILED_RECORDS_CONTAINER_ALIAS);

        listedChildren.getEntries().forEach(UnfiledContainerChildEntry -> 
        {
            if (UnfiledContainerChildEntry.getEntry().getIsRecord())
            {
                getRestAPIFactory().getRecordsAPI().deleteRecord(UnfiledContainerChildEntry.getEntry().getId());
            }
            else
            {
                getRestAPIFactory().getUnfiledRecordFoldersAPI().deleteUnfiledRecordFolder(UnfiledContainerChildEntry.getEntry().getId());
            }
        });
    }
}
