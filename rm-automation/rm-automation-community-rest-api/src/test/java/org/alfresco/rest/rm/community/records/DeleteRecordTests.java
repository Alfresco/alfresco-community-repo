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

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.NON_ELECTRONIC_RECORD_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_CONTAINER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.user.UserPermissions.PERMISSION_FILING;
import static org.alfresco.rest.rm.community.model.user.UserRoles.ROLE_RM_POWER_USER;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.IMAGE_FILE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createNonElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createElectronicUnfiledContainerChildModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createNonElectronicUnfiledContainerChildModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.getFile;
import static org.alfresco.utility.constants.UserRole.SiteCollaborator;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChild;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordCategoryAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordsAPI;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.Test;

/**
 * Delete records tests
 *
 * @author Kristijan Conkas
 * @since 2.6
 */
public class DeleteRecordTests extends BaseRMRestTest
{
    /**
     * <pre>
     * Given an electronic record
     * And that I have the "Delete Record" capability
     * And write permissions
     * When I delete the record
     * Then it is deleted from the file plan
     * </pre>
     */
    @Test
    (
        dataProvider = "validRootContainers",
        description = "Admin user can delete an electronic record"
    )
    @AlfrescoTest(jira="RM-4363")
    public void adminCanDeleteElectronicRecord(String folderId, String type) throws Exception
    {
        // Create record
        String recordId;

        if(RECORD_FOLDER_TYPE.equalsIgnoreCase(type))
        {
            recordId = getRestAPIFactory().getRecordFolderAPI().createRecord(createElectronicRecordModel(), folderId, getFile(IMAGE_FILE)).getId();
        }
        else if(UNFILED_CONTAINER_TYPE.equalsIgnoreCase(type))
        {
            recordId = getRestAPIFactory().getUnfiledContainersAPI().uploadRecord(createElectronicUnfiledContainerChildModel(), folderId, getFile(IMAGE_FILE)).getId();
        }
        else if(UNFILED_RECORD_FOLDER_TYPE.equalsIgnoreCase(type))
        {
            recordId = getRestAPIFactory().getUnfiledRecordFoldersAPI().uploadRecord(createElectronicUnfiledContainerChildModel(), folderId, getFile(IMAGE_FILE)).getId();
        }
        else
        {
            throw new Exception("Unsuported type = " + type);
        }

        // Assert status
        assertStatusCode(CREATED);

        // Delete record and verify successful deletion
        deleteAndVerify(recordId);
    }

    /**
     * <pre>
     * Given a non-electronic record
     * And that I have the "Delete Record" capability
     * And write permissions
     * When I delete the record
     * Then it is deleted from the file plan
     * </pre>
     */
    @Test
    (
        dataProvider = "validRootContainers",
        description = "Admin user can delete a non-electronic record"
    )
    @AlfrescoTest(jira="RM-4363")
    public void adminCanDeleteNonElectronicRecord(String folderId, String type) throws Exception
    {
        // Create record
        String nonElectronicRecordId;

        if(RECORD_FOLDER_TYPE.equalsIgnoreCase(type))
        {
            nonElectronicRecordId = getRestAPIFactory().getRecordFolderAPI().createRecord(createNonElectronicRecordModel(), folderId).getId();
        }
        else if(UNFILED_CONTAINER_TYPE.equalsIgnoreCase(type))
        {
            nonElectronicRecordId = getRestAPIFactory().getUnfiledContainersAPI().createUnfiledContainerChild(createNonElectronicUnfiledContainerChildModel(), folderId).getId();
        }
        else if(UNFILED_RECORD_FOLDER_TYPE.equalsIgnoreCase(type))
        {
            nonElectronicRecordId = getRestAPIFactory().getUnfiledRecordFoldersAPI().createUnfiledRecordFolderChild(createNonElectronicUnfiledContainerChildModel(), folderId).getId();
        }
        else
        {
            throw new Exception("Unsuported type = " + type);
        }

        // Assert status
        assertStatusCode(CREATED);

        // Delete record and verify successful deletion
        deleteAndVerify(nonElectronicRecordId);
    }

    /**
     * <pre>
     * Given a non-electronic record
     * And that I don't have write permissions
     * When I try to delete the record
     * Then nothing happens
     * And error gets reported
     * </pre>
     */
    @Test
    (
        description = "User without write permissions can't delete a record"
    )
    @AlfrescoTest(jira="RM-4363")
    public void userWithoutWritePermissionsCantDeleteRecord() throws Exception
    {
        // Create a non-electronic record in unfiled records
        UnfiledContainerChild nonElectronicRecord = UnfiledContainerChild.builder()
                .name("Record " + RandomData.getRandomAlphanumeric())
                .nodeType(NON_ELECTRONIC_RECORD_TYPE)
                .build();
        UnfiledContainerChild newRecord = getRestAPIFactory().getUnfiledContainersAPI().createUnfiledContainerChild(nonElectronicRecord, UNFILED_RECORDS_CONTAINER_ALIAS);

        assertStatusCode(CREATED);

        // Create test user and add it with collaboration privileges
        UserModel deleteUser = getDataUser().createRandomTestUser("delnoperm");
        String username = deleteUser.getUsername();
        logger.info("Test user: " + username);
        getDataUser().addUserToSite(deleteUser, new SiteModel(getRestAPIFactory().getRMSiteAPI().getSite().getId()), SiteCollaborator);

        // Add RM role to user
        getRestAPIFactory().getRMUserAPI().assignRoleToUser(username, ROLE_RM_POWER_USER);
        assertStatusCode(OK);

        // Try to delete newRecord
        getRestAPIFactory().getRecordsAPI(deleteUser).deleteRecord(newRecord.getId());
        assertStatusCode(FORBIDDEN);
    }

    /**
     * <pre>
     * Given a record
     * And that I don't have the "Delete Record" capability
     * When I try to delete the record
     * Then nothing happens
     * And error gets reported
     * </pre>
     */
    @Test
    (
        description = "User without delete records capability can't delete a record"
    )
    @AlfrescoTest(jira="RM-4363")
    public void userWithoutDeleteRecordsCapabilityCantDeleteRecord() throws Exception
    {
        // Create test user and add it with collaboration privileges
        UserModel deleteUser = getDataUser().createRandomTestUser("delnoperm");
        getDataUser().addUserToSite(deleteUser, new SiteModel(getRestAPIFactory().getRMSiteAPI().getSite().getId()), SiteCollaborator);
        String username = deleteUser.getUsername();
        logger.info("Test user: " + username);

        // Add RM role to user, RM Power User doesn't have the "Delete Record" capabilities
        getRestAPIFactory().getRMUserAPI().assignRoleToUser(username, ROLE_RM_POWER_USER);
        assertStatusCode(OK);

        // Create random folder
        RecordCategoryChild recordFolder = createCategoryFolderInFilePlan();
        logger.info("Random folder:" + recordFolder.getName());

        // Grant "deleteUser" filing permissions on "randomFolder" parent, this will be inherited to randomFolder
        RecordCategoryAPI recordCategoryAPI = getRestAPIFactory().getRecordCategoryAPI();
        getRestAPIFactory().getRMUserAPI().addUserPermission(recordCategoryAPI.getRecordCategory(recordFolder.getParentId()).getId(), deleteUser, PERMISSION_FILING);
        assertStatusCode(OK);

        // Create a non-electronic record in "randomFolder"
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        Record newRecord = recordFolderAPI.createRecord(createNonElectronicRecordModel(), recordFolder.getId());
        assertStatusCode(CREATED);

        // Verify the user can see "newRecord"
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI(deleteUser);
        recordsAPI.getRecord(newRecord.getId());
        assertStatusCode(OK);

        // Try to delete "newRecord"
        recordsAPI.deleteRecord(newRecord.getId());
        assertStatusCode(FORBIDDEN);
    }

    /**
     * Utility method to delete a record and verify successful deletion
     *
     * @param recordId The id of the record
     */
    private void deleteAndVerify(String recordId)
    {
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();

        // Delete record and verify status
        recordsAPI.deleteRecord(recordId);
        assertStatusCode(NO_CONTENT);

        // Try to get deleted record
        recordsAPI.deleteRecord(recordId);
        assertStatusCode(NOT_FOUND);
    }

}
